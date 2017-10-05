package strawman
package collection.mutable

import strawman.collection.{IterableFactory, IterableOnce, MapFactory}
import strawman.collection.decorators._

/**
  * A class for mutable sets with the ability to count the occurrences of their elements.
  *
  * @example {{{
  *   import collection.mutable.MultiSet
  *   val ms = MultiSet.empty[String]
  *
  *   ms += "foo"
  *   ms += "bar"
  *   ms += "foo"
  *
  *   assert(ms.get("foo") == 2)
  *   assert(ms.get("bar") == 1)
  * }}}
  *
  * @param elems Underlying elements
  * @param newBuilder Factory for building a `Map` of the same type as `elems`
  * @tparam A Type of elements
  * @define coll multiset
  * @define Coll MultiSet
  */
class MultiSet[A](private val elems: Map[A, Int], newBuilder: () => Builder[(A, Int), Map[A, Int]])
  extends collection.Iterable[(A, Int)]
    with collection.IterableOps[(A, Int), collection.Iterable, MultiSet[A]]
    with Growable[A]
    with Shrinkable[A]
    with Equals {

  def this(elems: Map[A, Int]) = this(elems, () => elems.mapFactory.newBuilder())

  protected[this] def coll: MultiSet[A] = this

  def iterableFactory: IterableFactory[collection.Iterable] = collection.Iterable

  protected[this] def fromSpecificIterable(coll: strawman.collection.Iterable[(A, Int)]): MultiSet[A] =
    new MultiSet[A](coll.to(elems.mapFactory))

  protected[this] def newSpecificBuilder(): Builder[(A, Int), MultiSet[A]] =
    newBuilder().mapResult(fromSpecificIterable)

  def iterator(): strawman.collection.Iterator[(A, Int)] = elems.iterator()

  def add(elem: A): this.type = {
    elems.updateWith(elem) {
      case None    => Some(1)
      case Some(n) => Some(n + 1)
    }
    this
  }

  def subtract(elem: A): this.type = {
    elems.updateWith(elem) {
      case Some(n) => if (n > 1) Some(n - 1) else None
    }
    this
  }

  def clear(): Unit = elems.clear()

  def canEqual(that: Any) = true

  override def equals(that: Any): Boolean = that match {
    case that: MultiSet[A] => this.elems == that.elems
    case _ => false
  }

  override def hashCode(): Int = strawman.collection.Set.unorderedHash(this, "MultiSet".##)

  /**
    * @return The number of occurrences of `elem` in this `MultiSet`
    */
  def get(elem: A): Int = elems.getOrElse(elem, 0)

  def toMutableMap: Map[A, Int] = elems

}

class MultiSetFactory(mapFactory: MapFactory[Map]) extends IterableFactory[MultiSet] {

  def from[A](source: IterableOnce[A]) = Growable.from(empty[A], source)

  def empty[A] = new MultiSet(mapFactory.empty[A, Int])

  def newBuilder[A]() = new GrowableBuilder(empty[A])

}

/**
  * The default MultiSet factory uses the default `mutable.Map` to store
  * its elements.
  */
object MultiSet extends MultiSetFactory(mapFactory = Map)