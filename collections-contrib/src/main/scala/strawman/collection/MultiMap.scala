package strawman
package collection

trait MultiMap[K, V]
  extends Iterable[(K, Set[V])]
    with MultiMapOps[K, V, MultiMap, MultiMap[K, V]]
    with Equals {

  final protected[this] def coll: this.type = this

  def canEqual(that: Any): Boolean = true

  override def equals(o: Any): Boolean = o match {
    case that: MultiMap[K, _] =>
      (this eq that) ||
        (that canEqual this) &&
          (this.size == that.size) && {
          try {
            this forall { case (k, vs) => that.get(k) == vs }
          } catch {
            case _: ClassCastException => false
          }
        }
    case _ => false
  }

  override def hashCode(): Int = collection.Set.unorderedHash(toIterable, "MultiMap".##)

}

trait MultiMapOps[K, V, +CC[X, Y] <: MultiMap[X, Y], +C]
  extends IterableOps[(K, Set[V]), Iterable, C] {

  def iterableFactory: IterableFactory[Iterable] = Iterable

  def toIterableByValue: Iterable[(K, V)]
  def multiMapFactory: MapFactory[CC]
  protected[this] def multiMapFromIterable[K2, V2](coll: Iterable[(K2, Set[V2])]): CC[K2, V2]
  protected[this] def multiMapFromIterableByValue[K2, V2](coll: Iterable[(K2, V2)]): CC[K2, V2]

  def mapByValue[K2, V2](f: ((K, V)) => (K2, V2)): CC[K2, V2] =
    multiMapFromIterableByValue(View.Map(toIterableByValue, f))

  def map[K2, V2](f: ((K, Set[V])) => (K2, Set[V2])): CC[K2, V2] =
    multiMapFromIterable(View.Map(toIterable, f))

  def flatMapByValue[K2, V2](f: ((K, V)) => IterableOnce[(K2, V2)]): CC[K2, V2] =
    multiMapFromIterableByValue(View.FlatMap(toIterableByValue, f))

  def flatMap[K2, V2](f: ((K, Set[V])) => IterableOnce[(K2, collection.Set[V2])]): CC[K2, V2] =
    multiMapFromIterable(View.FlatMap(toIterable, f))

  def concatByValue(that: Iterable[(K, V)]): CC[K, V] =
    multiMapFromIterableByValue(View.Concat(toIterableByValue, that))

  def concat(that: Iterable[(K, Set[V])]): CC[K, V] =
    multiMapFromIterable(View.Concat(toIterable, that))

  @`inline` final def ++ (that: Iterable[(K, Set[V])]): CC[K, V] = concat(that)

  def get(key: K): Set[V]

  def contains(key: K): Boolean = get(key).nonEmpty

  def keySet: Set[K]

  def values: Iterable[V]

  def iterator(): Iterator[(K, Set[V])]

  def iteratorByValue(): Iterator[(K, V)]

  def foreachValue[U](f: ((K, V)) => U): Unit = iteratorByValue().foreach(f)

  def entryExists(key: K, p: V => Boolean): Boolean = get(key).exists(p)

}

object MultiMap extends MapFactory.Delegate[MultiMap](mutable.MultiMap)