package strawman
package collection

import strawman.collection.immutable.NumericRange

import scala.language.implicitConversions
import strawman.collection.mutable.Builder

import scala.{Any, Int, Integral, Nothing, Ordering}
import scala.Predef.implicitly
import scala.annotation.unchecked.uncheckedVariance


/** Builds a collection of type `C` from elements of type `A` when a source collection of type `From` is available.
  * Implicit instances of `BuildFrom` are available for all collection types.
  *
  * @tparam From Type of source collection
  * @tparam A Type of elements (e.g. `Int`, `Boolean`, etc.)
  * @tparam C Type of collection (e.g. `List[Int]`, `TreeMap[Int, String]`, etc.)
  */
trait BuildFrom[-From, -A, +C] extends Any {
  def fromSpecific(from: From)(it: IterableOnce[A]): C

  /** Get a Builder for the collection. For non-strict collection types this will use an intermediate buffer.
    * Building collections with `fromSpecificIterable` is preferred because it can be lazy for lazy collections. */
  def newBuilder(from: From): Builder[A, C]
}

/**
  * Builds a collection of type `C` from elements of type `A`
  * @tparam A Type of elements (e.g. `Int`, `Boolean`, etc.)
  * @tparam C Type of collection (e.g. `List[Int]`, `TreeMap[Int, String]`, etc.)
  */
trait CanBuild[-A, +C] extends Any with BuildFrom[Any, A, C] {
  def fromSpecific(from: Any)(it: IterableOnce[A]): C = fromSpecific(it)
  def fromSpecific(it: IterableOnce[A]): C
  def newBuilder(from: Any): Builder[A, C] = newBuilder()
  def newBuilder(): Builder[A, C]
}

/** Base trait for companion objects of unconstrained collection types that may require
  * multiple traversals of a source collection to build a target collection `CC`.
  *
  * @define $coll collection
  * @define $Coll CC
  *
  * @tparam CC Collection type constructor (e.g. `List`)
  */
trait IterableFactoryLike[+CC[_]] {

  /** Type of a source collection to build from */
  type Source[A] >: Iterable[A]

  /** Creates a target $coll from an existing source collection
    *
    * @param source Source collection
    * @tparam A the type of the collection’s elements
    * @return a new $coll with the elements of `source`
    */
  def from[A](source: Source[A]): CC[A]

  /** An empty collection
    * @tparam A      the type of the ${coll}'s elements
    */
  def empty[A]: CC[A]

  /** Creates a $coll with the specified elements.
    * @tparam A     the type of the ${coll}'s elements
    * @param elems  the elements of the created $coll
    * @return a new $coll with elements `elems`
    */
  def apply[A](elems: A*): CC[A] = from(View.Elems(elems: _*))

  /** Produces a $coll containing repeated applications of a function to a start value.
    *
    *  @param start the start value of the $coll
    *  @param len   the number of elements contained in the $coll
    *  @param f     the function that's repeatedly applied
    *  @return      a $coll with `len` values in the sequence `start, f(start), f(f(start)), ...`
    */
  def iterate[A](start: A, len: Int)(f: A => A): CC[A] = from(new View.Iterate(start, len)(f))

  /** Produces a $coll containing a sequence of increasing of integers.
    *
    *  @param start the first element of the $coll
    *  @param end   the end value of the $coll (the first value NOT contained)
    *  @return  a $coll with values `start, start + 1, ..., end - 1`
    */
  def range[A : Integral](start: A, end: A): CC[A] = from(NumericRange(start, end, implicitly[Integral[A]].one))

  /** Produces a $coll containing equally spaced values in some integer interval.
    *  @param start the start value of the $coll
    *  @param end   the end value of the $coll (the first value NOT contained)
    *  @param step  the difference between successive elements of the $coll (must be positive or negative)
    *  @return      a $coll with values `start, start + step, ...` up to, but excluding `end`
    */
  def range[A : Integral](start: A, end: A, step: A): CC[A] = from(NumericRange(start, end, step))

  /**
    * @return A builder for `$Coll` objects.
    * @tparam A the type of the ${$coll}’s elements
    */
  def newBuilder[A](): Builder[A, CC[A]]

}

/** Base trait for companion objects of unconstrained collection types that can
  * build a target collection `CC` from a source collection with a single traversal
  * of the source.
  *
  * @tparam CC Collection type constructor (e.g. `List`)
  */
trait IterableFactory[+CC[_]] extends IterableFactoryLike[CC] {

  // Since most collection factories can build a target collection instance by performing only one
  // traversal of a source collection, the type of this source collection can be refined to be
  // just `IterableOnce`
  type Source[A] = IterableOnce[A]

  implicit def canBuildIterable[A]: CanBuild[A, CC[A]] = IterableFactory.toCanBuild(this)

}

object IterableFactory {
  implicit def toCanBuild[A, CC[_]](factory: IterableFactory[CC]): CanBuild[A, CC[A]] =
    new CanBuild[A, CC[A]] {
      def fromSpecific(it: IterableOnce[A]): CC[A] = factory.from[A](it)
      def newBuilder(): Builder[A, CC[A]] = factory.newBuilder[A]()
    }

  class Delegate[CC[_]](delegate: IterableFactory[CC]) extends IterableFactory[CC] {
    def empty[A]: CC[A] = delegate.empty
    def from[E](it: IterableOnce[E]): CC[E] = delegate.from(it)
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
  def fill[A](n: Int)(elem: => A): CC[A] = from(View.Fill(n)(elem))

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
  def tabulate[A](n: Int)(f: Int => A): CC[A] = from(new View.Tabulate(n)(f))

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
    def from[E](it: IterableOnce[E]): CC[E] = delegate.from(it)
    def newBuilder[A](): Builder[A, CC[A]] = delegate.newBuilder[A]()
  }
}

trait SpecificIterableFactory[-A, +C] extends CanBuild[A, C] {
  def empty: C
  def apply(xs: A*): C = fromSpecific(View.Elems(xs: _*))
  def fill(n: Int)(elem: => A): C = fromSpecific(View.Fill(n)(elem))
  def newBuilder(): Builder[A, C]
}

/** Factory methods for collections of kind `* −> * -> *` */
trait MapFactory[+CC[_, _]] {
  def empty[K, V]: CC[K, V]
  def from[K, V](it: IterableOnce[(K, V)]): CC[K, V]
  def apply[K, V](elems: (K, V)*): CC[K, V] = from(elems.toStrawman)
  def newBuilder[K, V](): Builder[(K, V), CC[K, V]]
  implicit def canBuildMap[K, V]: CanBuild[(K, V), CC[K, V]] = MapFactory.toCanBuild(this)
}

object MapFactory {
  implicit def toCanBuild[K, V, CC[_, _]](factory: MapFactory[CC]): CanBuild[(K, V), CC[K, V]] =
    new CanBuild[(K, V), CC[K, V]] {
      def fromSpecific(it: IterableOnce[(K, V)]): CC[K, V] = factory.from[K, V](it)
      def newBuilder(): Builder[(K, V), CC[K, V]] = factory.newBuilder[K, V]()
    }

  class Delegate[C[_, _]](delegate: MapFactory[C]) extends MapFactory[C] {
    def from[K, V](it: IterableOnce[(K, V)]): C[K, V] = delegate.from(it)
    def empty[K, V]: C[K, V] = delegate.empty
    def newBuilder[K, V](): Builder[(K, V), C[K, V]] = delegate.newBuilder()
  }
}

/** Base trait for companion objects of collections that require an implicit evidence */
trait SortedIterableFactory[+CC[_]] {
  def from[E : Ordering](it: IterableOnce[E]): CC[E]
  def empty[A : Ordering]: CC[A]
  def apply[A : Ordering](xs: A*): CC[A] = from(View.Elems(xs: _*))
  def fill[A : Ordering](n: Int)(elem: => A): CC[A] = from(View.Fill(n)(elem))
  def newBuilder[A : Ordering](): Builder[A, CC[A]]
  implicit def canBuildSortedIterable[A : Ordering]: CanBuild[A, CC[A]] = SortedIterableFactory.toCanBuild(this)
}

object SortedIterableFactory {
  implicit def toCanBuild[A: Ordering, CC[_]](factory: SortedIterableFactory[CC]): CanBuild[A, CC[A]] =
    new CanBuild[A, CC[A]] {
      def fromSpecific(it: IterableOnce[A]): CC[A] = factory.from[A](it)
      def newBuilder(): Builder[A, CC[A]] = factory.newBuilder[A]()
    }

  class Delegate[CC[_]](delegate: SortedIterableFactory[CC]) extends SortedIterableFactory[CC] {
    def empty[A : Ordering]: CC[A] = delegate.empty
    def from[E : Ordering](it: IterableOnce[E]): CC[E] = delegate.from(it)
    def newBuilder[A : Ordering](): Builder[A, CC[A]] = delegate.newBuilder[A]()
  }
}

/** Factory methods for collections of kind `* −> * -> *` which require an implicit evidence value for the key type */
trait SortedMapFactory[+CC[_, _]] {
  def empty[K : Ordering, V]: CC[K, V]
  def from[K : Ordering, V](it: IterableOnce[(K, V)]): CC[K, V]
  def apply[K : Ordering, V](elems: (K, V)*): CC[K, V] = from(elems.toStrawman)
  def newBuilder[K : Ordering, V](): Builder[(K, V), CC[K, V]]
  implicit def canBuildSortedMap[K : Ordering, V]: CanBuild[(K, V), CC[K, V]] = SortedMapFactory.toCanBuild(this)
}

object SortedMapFactory {
  implicit def toCanBuild[K : Ordering, V, CC[X, Y]](factory: SortedMapFactory[CC]): CanBuild[(K, V), CC[K, V]] =
    new CanBuild[(K, V), CC[K, V]] {
      def fromSpecific(it: IterableOnce[(K, V)]): CC[K, V] = factory.from[K, V](it)
      def newBuilder(): Builder[(K, V), CC[K, V]] = factory.newBuilder[K, V]()
    }

  class Delegate[CC[_, _]](delegate: SortedMapFactory[CC]) extends SortedMapFactory[CC] {
    def from[K : Ordering, V](it: IterableOnce[(K, V)]): CC[K, V] = delegate.from(it)
    def empty[K : Ordering, V]: CC[K, V] = delegate.empty
    def newBuilder[K : Ordering, V](): Builder[(K, V), CC[K, V]] = delegate.newBuilder()
  }
}
