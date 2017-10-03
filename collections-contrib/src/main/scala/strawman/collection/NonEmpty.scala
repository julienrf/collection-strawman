package strawman
package collection

import collection.immutable.List

/**
  * A wrapper around a collection that is guaranteed to have at least one element.
  *
  * A `NonEmpty` instance is implicitly enriched with the same operations as the
  * wrapped collection and also has its own operations that preserve non-emptiness.
  *
  * @param coll Wrapped collection
  * @tparam A Type of elements (e.g. `Int`, `String`)
  * @tparam C Type of collection (e.g. `List[Int]`, `Map[Int, Boolean]`)
  */
final class NonEmpty[+A, C <: Iterable[A]] private (val coll: C)
  extends AnyVal {

  def map[B, C2 <: Iterable[B]](f: A => B)(implicit bf: BuildFrom[C, B, C2]): NonEmpty[B, C2] =
    new NonEmpty[B, C2](bf.fromSpecificIterable(coll)(coll.toIterable.map(f)))

  def flatMap[B, C2 <: Iterable[B]](f: A => IterableOnce[B])(implicit bf: BuildFrom[C, B, C2]): NonEmpty[B, C2] =
    new NonEmpty[B, C2](bf.fromSpecificIterable(coll)(coll.toIterable.flatMap(f)))

  def concat[B >: A, C2 <: Iterable[B]](that: Iterable[B])(implicit bf: BuildFrom[C, B, C2]): NonEmpty[B, C2] =
    new NonEmpty[B, C2](bf.fromSpecificIterable(coll)(coll.toIterable ++ that))

  @`inline` def ++ [B >: A, C2 <: Iterable[B]](that: Iterable[B])(implicit bf: BuildFrom[C, B, C2]): NonEmpty[B, C2] =
    concat(that)

  def zip[A0 >: A, B, C2 <: Iterable[(A0, B)]](that: NonEmpty[B, _ <: Iterable[B]])(implicit bf: BuildFrom[C, (A0, B), C2]): NonEmpty[(A0, B), C2] =
    new NonEmpty[(A0, B), C2](bf.fromSpecificIterable(coll)(coll.zip(that.coll)))

  def zipWithIndex[A0 >: A, C2 <: Iterable[(A0, Int)]](implicit bf: BuildFrom[C, (A0, Int), C2]): NonEmpty[(A0, Int), C2] =
    new NonEmpty[(A0, Int), C2](bf.fromSpecificIterable(coll)(coll.zipWithIndex))

  def prepended[B >: A, C2 <: Seq[B]](elem: B)(implicit bf: BuildFrom[C, B, C2], ev: C <:< Seq[B]): NonEmpty[B, C2] =
    new NonEmpty[B, C2](bf.fromSpecificIterable(coll)(elem +: coll))

  @`inline` def +: [B >: A, C2 <: Seq[B]](elem: B)(implicit bf: BuildFrom[C, B, C2], ev: C <:< Seq[B]): NonEmpty[B, C2] =
    prepended(elem)

  @`inline` def :: [B >: A, C2 <: List[B]](elem: B)(implicit bf: BuildFrom[C, B, C2], ev: C <:< List[B]): NonEmpty[B, C2] =
    prepended(elem)

  def appended[B >: A, C2 <: Iterable[B]](elem: B)(implicit bf: BuildFrom[C, B, C2], ev: C <:< Seq[B]): NonEmpty[B, C2] =
    new NonEmpty[B, C2](bf.fromSpecificIterable(coll)(coll :+ elem))

  @`inline` def :+ [B >: A, C2 <: Iterable[B]](elem: B)(implicit bf: BuildFrom[C, B, C2], ev: C <:< Seq[B]): NonEmpty[B, C2] =
    appended(elem)

  def incl[B >: A, C2 <: immutable.Set[B]](elem: B)(implicit bf: BuildFrom[C, B, C2], ev: C <:< immutable.Set[B]): NonEmpty[B, C2] =
    new NonEmpty[B, C2](bf.fromSpecificIterable(coll)(coll.incl(elem).toIterable))

  @`inline` def + [B >: A, C2 <: immutable.Set[B]](elem: B)(implicit bf: BuildFrom[C, B, C2], ev: C <:< immutable.Set[B]): NonEmpty[B, C2] =
    incl(elem)

  def updated[K, V, C2 <: immutable.Map[K, V]](key: K, value: V)(implicit bf: BuildFrom[C, (K, V), C2], ev: C <:< immutable.Map[K, V]): NonEmpty[(K, V), C2] =
    new NonEmpty[(K, V), C2](bf.fromSpecificIterable(coll)(coll.updated(key, value)))

  @`inline` def + [K, V, C2 <: immutable.Map[K, V]](kv: (K, V))(implicit bf: BuildFrom[C, (K, V), C2], ev: C <:< immutable.Map[K, V]): NonEmpty[(K, V), C2] =
    updated(kv._1, kv._2)

}

object NonEmpty {

  import scala.language.implicitConversions

  implicit def toCollection[A, C <: Iterable[A]](nonEmpty: NonEmpty[A, C]): C =
    nonEmpty.coll

  def fromIterable[A, C <: Iterable[A]](it: C): Option[NonEmpty[A, C]] =
    if (it.isEmpty) None
    else Some(new NonEmpty[A, C](it))

  /**
    * Create a `NonEmpty` instance that contains `elem` and all the elements of `coll` (which
    * might be empty).
    *
    * If the wrapped collection is a sequence, then `elem` is prepended to `coll`.
    *
    * @param bf Builder for the wrapped collection
    * @tparam A Type of the elements
    * @tparam C Type of the wrapped collection
    */
  def apply[A, C <: Iterable[A]](elem: A, coll: C)(implicit bf: BuildFrom[C, A, C]): NonEmpty[A, C] =
    new NonEmpty[A, C](bf.fromSpecificIterable(coll)(View.Prepend(elem, coll.toIterable)))

}