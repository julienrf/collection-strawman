package strawman
package collection

import strawman.collection.immutable.NumericRange

import scala.language.implicitConversions
import strawman.collection.mutable.Builder

import scala.{Any, Int, Integral, Nothing, Ordering}
import scala.Predef.implicitly
import scala.annotation.unchecked.uncheckedVariance

/**
  * A factory that builds a collection of type `C` with elements of type `A`.
  *
  * This is a general form of any factory ([[IterableFactory]],
  * [[SortedIterableFactory]], [[MapFactory]] and [[SortedMapFactory]]) whose
  * element type is fixed.
  *
  * @tparam A Type of elements (e.g. `Int`, `Boolean`, etc.)
  * @tparam C Type of collection (e.g. `List[Int]`, `TreeMap[Int, String]`, etc.)
  */
trait Factory[-A, +C] extends Any {

  /**
    * @return A collection of type `C` containing the same elements
    *         as the source collection `it`.
    * @param it Source collection
    */
  def fromSpecificIterable(it: Iterable[A]): C

  /** A strict builder that eventually produces a `C` */
  def newBuilder(): Builder[A, C]
}

/** Base trait for companion objects of unconstrained collection types
  *
  * @define $coll collection
  *
  * @tparam CC Collection type constructor (e.g. `List`)
  */
trait IterableFactory[+CC[_]] { factory =>

  def fromIterable[E](it: Iterable[E]): CC[E]

  def empty[A]: CC[A]

  def apply[A](xs: A*): CC[A] = fromIterable(View.Elems(xs: _*))

  /** Produces a $coll containing repeated applications of a function to a start value.
    *
    *  @param start the start value of the $coll
    *  @param len   the number of elements contained in the $coll
    *  @param f     the function that's repeatedly applied
    *  @return      a $coll with `len` values in the sequence `start, f(start), f(f(start)), ...`
    */
  def iterate[A](start: A, len: Int)(f: A => A): CC[A] = fromIterable(new View.Iterate(start, len)(f))

  /** Produces a $coll containing a sequence of increasing of integers.
    *
    *  @param start the first element of the $coll
    *  @param end   the end value of the $coll (the first value NOT contained)
    *  @return  a $coll with values `start, start + 1, ..., end - 1`
    */
  def range[A : Integral](start: A, end: A): CC[A] = fromIterable(NumericRange(start, end, implicitly[Integral[A]].one))

  /** Produces a $coll containing equally spaced values in some integer interval.
    *  @param start the start value of the $coll
    *  @param end   the end value of the $coll (the first value NOT contained)
    *  @param step  the difference between successive elements of the $coll (must be positive or negative)
    *  @return      a $coll with values `start, start + step, ...` up to, but excluding `end`
    */
  def range[A : Integral](start: A, end: A, step: A): CC[A] = fromIterable(NumericRange(start, end, step))

  def newBuilder[A](): Builder[A, CC[A]]

  implicit def iterableFactory[A]: Factory[A, CC[A]] = IterableFactory.toFactory(this)

  /**
    * @return a `BuildFrom` instance that ignores the factory of the source collection.
    * @tparam A Type of elements
    */
  def toBuildFrom[A]: BuildFrom[Any, A, CC[A]] =
    new BuildFrom[Any, A, CC[A]] {
      def fromSpecificIterable(from: Any)(it: Iterable[A]) = factory.fromIterable(it)
      def newBuilder(from: Any) = factory.newBuilder()
    }

}

object IterableFactory {

  /**
    * Fixes the element type of `factory` to `A`
    * @param factory The factory to fix the element type
    * @tparam A Type of elements
    * @tparam CC Collection type constructor of the factory (e.g. `Seq`, `List`)
    * @return A [[Factory]] that uses the given `factory` to build a collection of elements
    *         of type `A`
    */
  implicit def toFactory[A, CC[_]](factory: IterableFactory[CC]): Factory[A, CC[A]] =
    new Factory[A, CC[A]] {
      def fromSpecificIterable(it: Iterable[A]): CC[A] = factory.fromIterable[A](it)
      def newBuilder(): Builder[A, CC[A]] = factory.newBuilder[A]()
    }

  class Delegate[CC[_]](delegate: IterableFactory[CC]) extends IterableFactory[CC] {
    def empty[A]: CC[A] = delegate.empty
    def fromIterable[E](it: Iterable[E]): CC[E] = delegate.fromIterable(it)
    def newBuilder[A](): Builder[A, CC[A]] = delegate.newBuilder[A]()
  }
}

/**
  * Introduces factory methods `fill` and `tabulate`.
  * @tparam CC Collection type constructor (e.g. `List`)
  */
trait SeqFactory[+CC[_]] extends IterableFactory[CC] {

  /** Produces a $coll containing the results of some element computation a number of times.
    *  @param   n  the number of elements contained in the $coll.
    *  @param   elem the element computation
    *  @return  A $coll that contains the results of `n` evaluations of `elem`.
    */
  def fill[A](n: Int)(elem: => A): CC[A] = fromIterable(View.Fill(n)(elem))

  /** Produces a two-dimensional $coll containing the results of some element computation a number of times.
    *  @param   n1  the number of elements in the 1st dimension
    *  @param   n2  the number of elements in the 2nd dimension
    *  @param   elem the element computation
    *  @return  A $coll that contains the results of `n1 x n2` evaluations of `elem`.
    */
  def fill[A](n1: Int, n2: Int)(elem: => A): CC[CC[A] @uncheckedVariance] = fill(n1)(fill(n2)(elem))

  /** Produces a three-dimensional $coll containing the results of some element computation a number of times.
    *  @param   n1  the number of elements in the 1st dimension
    *  @param   n2  the number of elements in the 2nd dimension
    *  @param   n3  the number of elements in the 3nd dimension
    *  @param   elem the element computation
    *  @return  A $coll that contains the results of `n1 x n2 x n3` evaluations of `elem`.
    */
  def fill[A](n1: Int, n2: Int, n3: Int)(elem: => A): CC[CC[CC[A]] @uncheckedVariance] = fill(n1)(fill(n2, n3)(elem))

  /** Produces a four-dimensional $coll containing the results of some element computation a number of times.
    *  @param   n1  the number of elements in the 1st dimension
    *  @param   n2  the number of elements in the 2nd dimension
    *  @param   n3  the number of elements in the 3nd dimension
    *  @param   n4  the number of elements in the 4th dimension
    *  @param   elem the element computation
    *  @return  A $coll that contains the results of `n1 x n2 x n3 x n4` evaluations of `elem`.
    */
  def fill[A](n1: Int, n2: Int, n3: Int, n4: Int)(elem: => A): CC[CC[CC[CC[A]]] @uncheckedVariance] =
    fill(n1)(fill(n2, n3, n4)(elem))

  /** Produces a five-dimensional $coll containing the results of some element computation a number of times.
    *  @param   n1  the number of elements in the 1st dimension
    *  @param   n2  the number of elements in the 2nd dimension
    *  @param   n3  the number of elements in the 3nd dimension
    *  @param   n4  the number of elements in the 4th dimension
    *  @param   n5  the number of elements in the 5th dimension
    *  @param   elem the element computation
    *  @return  A $coll that contains the results of `n1 x n2 x n3 x n4 x n5` evaluations of `elem`.
    */
  def fill[A](n1: Int, n2: Int, n3: Int, n4: Int, n5: Int)(elem: => A): CC[CC[CC[CC[CC[A]]]] @uncheckedVariance] =
    fill(n1)(fill(n2, n3, n4, n5)(elem))

  /** Produces a $coll containing values of a given function over a range of integer values starting from 0.
    *  @param  n   The number of elements in the $coll
    *  @param  f   The function computing element values
    *  @return A $coll consisting of elements `f(0), ..., f(n -1)`
    */
  def tabulate[A](n: Int)(f: Int => A): CC[A] = fromIterable(new View.Tabulate(n)(f))

  /** Produces a two-dimensional $coll containing values of a given function over ranges of integer values starting from 0.
    *  @param   n1  the number of elements in the 1st dimension
    *  @param   n2  the number of elements in the 2nd dimension
    *  @param   f   The function computing element values
    *  @return A $coll consisting of elements `f(i1, i2)`
    *          for `0 <= i1 < n1` and `0 <= i2 < n2`.
    */
  def tabulate[A](n1: Int, n2: Int)(f: (Int, Int) => A): CC[CC[A] @uncheckedVariance] =
    tabulate(n1)(i1 => tabulate(n2)(f(i1, _)))

  /** Produces a three-dimensional $coll containing values of a given function over ranges of integer values starting from 0.
    *  @param   n1  the number of elements in the 1st dimension
    *  @param   n2  the number of elements in the 2nd dimension
    *  @param   n3  the number of elements in the 3nd dimension
    *  @param   f   The function computing element values
    *  @return A $coll consisting of elements `f(i1, i2, i3)`
    *          for `0 <= i1 < n1`, `0 <= i2 < n2`, and `0 <= i3 < n3`.
    */
  def tabulate[A](n1: Int, n2: Int, n3: Int)(f: (Int, Int, Int) => A): CC[CC[CC[A]] @uncheckedVariance] =
    tabulate(n1)(i1 => tabulate(n2, n3)(f(i1, _, _)))

  /** Produces a four-dimensional $coll containing values of a given function over ranges of integer values starting from 0.
    *  @param   n1  the number of elements in the 1st dimension
    *  @param   n2  the number of elements in the 2nd dimension
    *  @param   n3  the number of elements in the 3nd dimension
    *  @param   n4  the number of elements in the 4th dimension
    *  @param   f   The function computing element values
    *  @return A $coll consisting of elements `f(i1, i2, i3, i4)`
    *          for `0 <= i1 < n1`, `0 <= i2 < n2`, `0 <= i3 < n3`, and `0 <= i4 < n4`.
    */
  def tabulate[A](n1: Int, n2: Int, n3: Int, n4: Int)(f: (Int, Int, Int, Int) => A): CC[CC[CC[CC[A]]] @uncheckedVariance] =
    tabulate(n1)(i1 => tabulate(n2, n3, n4)(f(i1, _, _, _)))

  /** Produces a five-dimensional $coll containing values of a given function over ranges of integer values starting from 0.
    *  @param   n1  the number of elements in the 1st dimension
    *  @param   n2  the number of elements in the 2nd dimension
    *  @param   n3  the number of elements in the 3nd dimension
    *  @param   n4  the number of elements in the 4th dimension
    *  @param   n5  the number of elements in the 5th dimension
    *  @param   f   The function computing element values
    *  @return A $coll consisting of elements `f(i1, i2, i3, i4, i5)`
    *          for `0 <= i1 < n1`, `0 <= i2 < n2`, `0 <= i3 < n3`, `0 <= i4 < n4`, and `0 <= i5 < n5`.
    */
  def tabulate[A](n1: Int, n2: Int, n3: Int, n4: Int, n5: Int)(f: (Int, Int, Int, Int, Int) => A): CC[CC[CC[CC[CC[A]]]] @uncheckedVariance] =
    tabulate(n1)(i1 => tabulate(n2, n3, n4, n5)(f(i1, _, _, _, _)))

}

object SeqFactory {
  class Delegate[CC[_]](delegate: SeqFactory[CC]) extends SeqFactory[CC] {
    def empty[A]: CC[A] = delegate.empty
    def fromIterable[E](it: Iterable[E]): CC[E] = delegate.fromIterable(it)
    def newBuilder[A](): Builder[A, CC[A]] = delegate.newBuilder[A]()
  }
}

trait SpecificIterableFactory[-A, +C] extends Factory[A, C] {
  def empty: C
  def apply(xs: A*): C = fromSpecificIterable(View.Elems(xs: _*))
  def fill(n: Int)(elem: => A): C = fromSpecificIterable(View.Fill(n)(elem))
  def newBuilder(): Builder[A, C]
}

/** Factory methods for collections of kind `* −> * -> *` */
trait MapFactory[+CC[_, _]] { factory =>

  def empty[K, V]: CC[K, V]

  def fromIterable[K, V](it: Iterable[(K, V)]): CC[K, V]

  def apply[K, V](elems: (K, V)*): CC[K, V] = fromIterable(elems.toStrawman)

  def newBuilder[K, V](): Builder[(K, V), CC[K, V]]

  implicit def mapFactory[K, V]: Factory[(K, V), CC[K, V]] = MapFactory.toFactory(this)

  /**
    * @return a `BuildFrom` instance that ignores the factory of the source collection
    * @tparam K Type of keys
    * @tparam V Type of values
    */
  implicit def toBuildFrom[K, V]: BuildFrom[Any, (K, V), CC[K, V]] =
    new BuildFrom[Any, (K, V), CC[K, V]] {
      def fromSpecificIterable(from: Any)(it: Iterable[(K, V)]) = factory.fromIterable(it)
      def newBuilder(from: Any) = factory.newBuilder[K, V]()
    }

}

object MapFactory {

  /**
    * Fixes the key and value types of `factory` to `K` and `V`, respectively
    * @param factory The factory to fix the key and value types
    * @tparam K Type of keys
    * @tparam V Type of values
    * @tparam CC Collection type constructor of the factory (e.g. `Map`, `HashMap`, etc.)
    * @return A [[Factory]] that uses the given `factory` to build a map with keys of type `K`
    *         and values of type `V`
    */
  implicit def toFactory[K, V, CC[_, _]](factory: MapFactory[CC]): Factory[(K, V), CC[K, V]] =
    new Factory[(K, V), CC[K, V]] {
      def fromSpecificIterable(it: Iterable[(K, V)]): CC[K, V] = factory.fromIterable[K, V](it)
      def newBuilder(): Builder[(K, V), CC[K, V]] = factory.newBuilder[K, V]()
    }

  class Delegate[C[X, Y]](delegate: MapFactory[C]) extends MapFactory[C] {
    def fromIterable[K, V](it: Iterable[(K, V)]): C[K, V] = delegate.fromIterable(it)
    def empty[K, V]: C[K, V] = delegate.empty
    def newBuilder[K, V](): Builder[(K, V), C[K, V]] = delegate.newBuilder()
  }
}

/** Base trait for companion objects of collections that require an implicit evidence */
trait SortedIterableFactory[+CC[_]] { factory =>

  def sortedFromIterable[E : Ordering](it: Iterable[E]): CC[E]

  def empty[A : Ordering]: CC[A]

  def apply[A : Ordering](xs: A*): CC[A] = sortedFromIterable(View.Elems(xs: _*))

  def fill[A : Ordering](n: Int)(elem: => A): CC[A] = sortedFromIterable(View.Fill(n)(elem))

  def newBuilder[A : Ordering](): Builder[A, CC[A]]

  implicit def sortedIterableFactory[A : Ordering]: Factory[A, CC[A]] = SortedIterableFactory.toFactory(this)

  /**
    * @return A `BuildFrom` instance that ignores the factory of the source collection.
    * @tparam A Type of elements
    */
  implicit def toBuildFrom[A: Ordering]: BuildFrom[Any, A, CC[A]] =
    new BuildFrom[Any, A, CC[A]] {
      def fromSpecificIterable(from: Any)(it: Iterable[A]): CC[A] = factory.sortedFromIterable[A](it)
      def newBuilder(from: Any): Builder[A, CC[A]] = factory.newBuilder[A]()
    }

}

object SortedIterableFactory {

  /**
    * Fixes the element type of `factory` to `A`
    * @param factory The factory to fix the element type
    * @tparam A Type of elements
    * @tparam CC Collection type constructor of the factory (e.g. `TreeSet`)
    * @return A [[Factory]] that uses the given `factory` to build a collection of elements
    *         of type `A`
    */
  implicit def toFactory[A: Ordering, CC[_]](factory: SortedIterableFactory[CC]): Factory[A, CC[A]] =
    new Factory[A, CC[A]] {
      def fromSpecificIterable(it: Iterable[A]): CC[A] = factory.sortedFromIterable[A](it)
      def newBuilder(): Builder[A, CC[A]] = factory.newBuilder[A]()
    }

  class Delegate[CC[_]](delegate: SortedIterableFactory[CC]) extends SortedIterableFactory[CC] {
    def empty[A : Ordering]: CC[A] = delegate.empty
    def sortedFromIterable[E : Ordering](it: Iterable[E]): CC[E] = delegate.sortedFromIterable(it)
    def newBuilder[A : Ordering](): Builder[A, CC[A]] = delegate.newBuilder[A]()
  }
}

/** Factory methods for collections of kind `* −> * -> *` which require an implicit evidence value for the key type */
trait SortedMapFactory[+CC[_, _]] { factory =>

  def empty[K : Ordering, V]: CC[K, V]

  def sortedFromIterable[K : Ordering, V](it: Iterable[(K, V)]): CC[K, V]

  def apply[K : Ordering, V](elems: (K, V)*): CC[K, V] =
    sortedFromIterable(elems.toStrawman)

  def newBuilder[K : Ordering, V](): Builder[(K, V), CC[K, V]]

  implicit def sortedMapFactory[K : Ordering, V]: Factory[(K, V), CC[K, V]] = SortedMapFactory.toFactory(this)

  /**
    * @return a `BuildFrom` instance that ignores the factory of the source collection.
    * @tparam K Type of keys
    * @tparam V Type of values
    */
  implicit def toBuildFrom[K : Ordering, V]: BuildFrom[Any, (K, V), CC[K, V]] =
    new BuildFrom[Any, (K, V), CC[K, V]] {
      def fromSpecificIterable(from: Any)(it: Iterable[(K, V)]) = factory.sortedFromIterable(it)
      def newBuilder(from: Any) = factory.newBuilder[K, V]()
    }

}

object SortedMapFactory {

  /**
    * Implicit conversion that fixes the key and value types of `factory` to `K` and `V`,
    * respectively.
    *
    * @param factory The factory to fix the key and value types
    * @tparam K Type of keys
    * @tparam V Type of values
    * @tparam CC Collection type constructor of the factory (e.g. `TreeMap`)
    * @return A [[Factory]] that uses the given `factory` to build a map with keys of
    *         type `K` and values of type `V`
    */
  implicit def toFactory[K : Ordering, V, CC[X, Y]](factory: SortedMapFactory[CC]): Factory[(K, V), CC[K, V]] =
    new Factory[(K, V), CC[K, V]] {
      def fromSpecificIterable(it: Iterable[(K, V)]): CC[K, V] = factory.sortedFromIterable[K, V](it)
      def newBuilder(): Builder[(K, V), CC[K, V]] = factory.newBuilder[K, V]()
    }

  class Delegate[CC[_, _]](delegate: SortedMapFactory[CC]) extends SortedMapFactory[CC] {
    def sortedFromIterable[K : Ordering, V](it: Iterable[(K, V)]): CC[K, V] = delegate.sortedFromIterable(it)
    def empty[K : Ordering, V]: CC[K, V] = delegate.empty
    def newBuilder[K : Ordering, V](): Builder[(K, V), CC[K, V]] = delegate.newBuilder()
  }
}
