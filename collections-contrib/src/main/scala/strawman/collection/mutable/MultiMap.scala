package strawman
package collection
package mutable

class MultiMap[K, V](underlying: Map[K, Set[V]])
  extends collection.MultiMap[K, V]
    with collection.MultiMapOps[K, V, MultiMap, MultiMap[K, V]]
    with Growable[(K, V)]
    with Shrinkable[(K, V)] {

  def toIterableByValue: collection.Iterable[(K, V)] =
    underlying.view.flatMap { case (k, vs) => vs.map(k -> _) }
  def multiMapFactory: MapFactory[MultiMap] = MultiMap
  protected[this] def multiMapFromIterable[K2, V2](coll: collection.Iterable[(K2, collection.Set[V2])]): MultiMap[K2, V2] =
    new MultiMap(underlying.mapFactory.from(coll.view.map { case (k, vs) => (k, vs.to(Set)) }))
  protected[this] def multiMapFromIterableByValue[K2, V2](coll: collection.Iterable[(K2, V2)]): MultiMap[K2, V2] = multiMapFactory.from(coll)
  protected[this] def fromSpecificIterable(coll: collection.Iterable[(K, collection.Set[V])]): MultiMap[K, V] = multiMapFromIterable(coll)
  protected[this] def newSpecificBuilder(): Builder[(K, collection.Set[V]), MultiMap[K, V]] =
    Map.newBuilder[K, collection.Set[V]]()
      .mapResult(m => new MultiMap(m.map { case (k, vs) => (k, vs.to(Set)) }))

  def get(key: K): collection.Set[V] = underlying.getOrElse(key, Set.empty)

  def keySet: collection.Set[K] = underlying.keySet

  def values: collection.Iterable[V] = underlying.values.flatten

  def iterator(): Iterator[(K, collection.Set[V])] = underlying.iterator()

  def iteratorByValue(): Iterator[(K, V)] = toIterableByValue.iterator()

  def subtract(elem: (K, V)): this.type = {
    val (key, value) = elem
    underlying.get(key).foreach { vs =>
      vs -= value
      if (vs.isEmpty) underlying -= key
    }
    this
  }

  def add(elem: (K, V)): this.type = {
    val (key, value) = elem
    underlying.get(key) match {
      case None     => underlying += key -> Set(value)
      case Some(vs) => vs += value
    }
    this
  }

  def clear(): Unit = underlying.clear()

}

object MultiMap extends MapFactory[MultiMap] {

  def empty[K, V] = new MultiMap[K, V](Map.empty)

  def from[K, V](it: IterableOnce[(K, V)]) = it match {
    case mm: MultiMap[K, V] => mm
    case _ => Growable.from(empty[K, V], it)
  }

  def newBuilder[K, V]() = new GrowableBuilder[(K, V), MultiMap[K, V]](empty)

}