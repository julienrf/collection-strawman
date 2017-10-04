package strawman
package collection.mutable

import scala.{Any, Boolean, None, Option, Some, Unit}
import strawman.collection.{BuildFrom, Factory, IterableOnce, MapFactory, View}

/** A class for mutable maps with multiple values assigned to a key.
 *
 *  @example {{{
 *  import collection.mutable.MultiMap
 *
 *  // to create a `MultiMap` the easiest way is to use the default `MultiMap` factory
 *  val mm = MultiMap.empty[Int, String]
 *
 *  // to add key-value pairs to a multimap you can use `+=`
 *  mm += 1 -> "a"
 *  mm += 2 -> "b"
 *  mm += 1 -> "c"
 *
 *  // mm now contains `Map(2 -> Set(b), 1 -> Set(c, a))`
 *
 *  // to check if the multimap contains a value there is method
 *  // `entryExists`, which allows to traverse the including set
 *  mm.entryExists(1, _ == "a") == true
 *  mm.entryExists(1, _ == "b") == false
 *  mm.entryExists(2, _ == "b") == true
 *
 *  // to remove a previous added value you can use `-=`
 *  mm -= 1 -> "a"
 *  mm.entryExists(1, _ == "a") == false
 *  }}}
 *
 *  @define coll multimap
 *  @define Coll `MultiMap`
 */
class MultiMap[K, V] private (
  underlying: Map[K, Set[V]],
  bf: BuildFrom[Any, (K, Set[V]), Map[K, Set[V]]],
  setFactory: Factory[V, Set[V]]
) extends collection.Map[K, Set[V]]
    with collection.IterableOps[(K, Set[V]), collection.Iterable, MultiMap[K, V]]
    with Growable[(K, V)]
    with Shrinkable[(K, V)] {

  // Note: primary constructors can not use path dependent types, that’s why I’m defining this secondary constructor
  def this(underlying: Map[K, Set[V]], setFactory: Factory[V, Set[V]])(implicit bf: BuildFrom[underlying.type, (K, Set[V]), Map[K, Set[V]]]) =
    this(underlying, bf.asInstanceOf[BuildFrom[Any, (K, Set[V]), Map[K, Set[V]]]], setFactory)

  def iterator(): collection.Iterator[(K, Set[V])] = underlying.iterator()

  def toMutableMap: Map[K, Set[V]] = underlying

  def mapFactory = underlying.mapFactory
  def iterableFactory = collection.Iterable
  protected[this] def mapFromIterable[K2, V2](it: collection.Iterable[(K2, V2)]) = mapFactory.from(it)
  protected[this] def fromSpecificIterable(coll: collection.Iterable[(K, Set[V])]) =
    new MultiMap(bf.fromSpecificIterable(underlying)(coll), bf, setFactory)
  protected[this] def newSpecificBuilder() =
    bf.newBuilder(underlying).mapResult(fromSpecificIterable)

  def empty = new MultiMap(underlying.empty, bf, setFactory)

  def get(key: K): Option[Set[V]] = underlying.get(key)

  /** Adds the specified entry.  If the key
    * already has a binding to equal to `value`, nothing is changed;
    * otherwise a new binding is added for that `key`.
    *
    *  @param elem   The entry to add
    *  @return       A reference to this multimap.
    */
  def add(elem: (K, V)): this.type = {
    val (key, value) = elem
    underlying.get(key) match {
      case None     => underlying.put(key, setFactory.fromSpecific(View.Single(value)))
      case Some(vs) => vs += value
    }
    this
  }

  def clear(): Unit = underlying.clear()

  /** Removes the binding if it exists, otherwise this
    *  operation doesn't have any effect.
    *
    *  If this was the last value assigned to the specified key, the
    *  set assigned to that key will be removed as well.
    *
    *  @param elem    The entry to remove.
    *  @return        A reference to this multimap.
    */
  def subtract(elem: (K, V)): this.type = {
    val (key, value) = elem
    underlying.get(key).foreach { vs =>
      vs -= value
      if (vs.isEmpty) underlying.remove(key)
    }
    this
  }

  /** Checks if there exists a binding to `key` such that it satisfies the predicate `p`.
    *
    *  @param key   The key for which the predicate is checked.
    *  @param p     The predicate which a value assigned to the key must satisfy.
    *  @return      A boolean if such a binding exists
    */
  def entryExists(key: K, p: V => Boolean): Boolean =
    underlying.get(key).exists(_.exists(p))

  // TODO Define equality

}

class MultiMapFactory(mapFactory: MapFactory[Map]) extends MapFactory[MultiMap] {

  def empty[K, V] = new MultiMap[K, V](mapFactory.empty, Set)

  def from[K, V](it: IterableOnce[(K, V)]) = Growable.from(empty[K, V], it)

  def newBuilder[K, V](): Builder[(K, V), MultiMap[K, V]] = new GrowableBuilder(empty[K, V])

}

object MultiMap extends MultiMapFactory(mapFactory = Map)