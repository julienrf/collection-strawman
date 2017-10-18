package strawman
package collection
package immutable

import collection.mutable.{ ImmutableBuilder, Builder }

class MultiMap[K, V](underlying: Map[K, Set[V]])
  extends collection.MultiMap[K, V]
    with collection.MultiMapOps[K, V, MultiMap, MultiMap[K, V]] {

  def toIterableByValue = underlying.view.flatMap { case (k, vs) => vs.map(k -> _) }
  def multiMapFactory = MultiMap
  protected[this] def multiMapFromIterable[K2, V2](coll: collection.Iterable[(K2, collection.Set[V2])]) =
    new MultiMap(underlying.mapFactory.from(coll.view.map { case (k, vs) => (k, vs.to(Set)) }))
  protected[this] def multiMapFromIterableByValue[K2, V2](coll: collection.Iterable[(K2, V2)]) = multiMapFactory.from(coll)
  protected[this] def fromSpecificIterable(coll: collection.Iterable[(K, collection.Set[V])]) = multiMapFromIterable(coll)
  protected[this] def newSpecificBuilder() =
    Map.newBuilder[K, collection.Set[V]]()
      .mapResult(m => new MultiMap(m.map { case (k, vs) => (k, vs.to(Set)) }))

  def get(key: K): collection.Set[V] = underlying.getOrElse(key, Set.empty)

  def keySet: collection.Set[K] = underlying.keySet

  def values: collection.Iterable[V] = underlying.values.flatten

  def iterator(): Iterator[(K, collection.Set[V])] = underlying.iterator()

  def iteratorByValue(): Iterator[(K, V)] = toIterableByValue.iterator()

  def remove(key: K, value: V): MultiMap[K, V] =
    underlying.get(key) match {
      case None => this
      case Some(vs) =>
        if (vs.contains(value)) {
          val updatedVs = vs - value
          if (updatedVs.isEmpty) new MultiMap(underlying - key)
          else new MultiMap(underlying + (key -> updatedVs))
        } else this
    }

  @`inline` final def - (kv: (K, V)): MultiMap[K, V] = remove(kv._1, kv._2)

  def add(key: K, value: V): MultiMap[K, V] =
    underlying.get(key) match {
      case Some(vs) => new MultiMap(underlying + (key -> (vs + value)))
      case None     => new MultiMap(underlying + (key -> Set(value)))
    }

  @`inline` final def + (kv: (K, V)): MultiMap[K, V] = add(kv._1, kv._2)

  def empty: MultiMap[K, V] = new MultiMap[K, V](underlying.mapFactory.empty)

}

object MultiMap extends MapFactory[MultiMap] {

  def empty[K, V] = new MultiMap(Map.empty)

  def from[K, V](it: IterableOnce[(K, V)]) = it match {
    case mm: MultiMap[K, V] => mm
    case _ => (newBuilder[K, V]() ++= it).result()
  }

  def newBuilder[K, V](): Builder[(K, V), MultiMap[K, V]] =
    new ImmutableBuilder[(K, V), MultiMap[K, V]](empty) {
      def add(elem: (K, V)): this.type = { elems = elems.add(elem._1, elem._2); this }
    }

}