package strawman.collection

import strawman.collection.immutable.{List, Nil}
import org.junit.{Assert, Test}

class NonEmptyTest {

  @Test
  def nonEmptiness(): Unit = {
    val xs = List(1, 2, 3)
    val nel = NonEmpty.cons(0, xs)

    Assert.assertEquals(4, nel.size)
    // This partial method is now guaranteed to be total!
    Assert.assertEquals(0, nel.min)

    // The following tests just check that non emptiness is preserved when
    // we apply structure preserving operations or when we grow the collection
    val nel2 = nel.map(x => x.toString)
    val nel2T: NonEmpty[String, List[String]] = nel2

    val nel4 = nel ++ Nil
    val nel4T: NonEmpty[Int, List[Int]] = nel4

    val nel5 = -1 +: nel
    val nel5T: NonEmpty[Int, List[Int]] = nel5

    val nel6 = nel :+ 1
    val nel6T: NonEmpty[Int, List[Int]] = nel6

    val nel7 = nel.zipWithIndex
    val nel7T: NonEmpty[(Int, Int), List[(Int, Int)]] = nel7

    val nel8 = nel.zip(nel)
    val nel8T: NonEmpty[(Int, Int), List[(Int, Int)]] = nel8

    val nel9 = -1 :: nel
    val nel9T: NonEmpty[Int, List[Int]] = nel9

    // Also works with different “kinds” of collections
    val ys = immutable.SortedSet(1, 2, 3)
    val nes = NonEmpty.cons(4, ys)
    val nesT: NonEmpty[Int, immutable.SortedSet[Int]] = nes

    val nes2 = nes + 0
    val nes2T: NonEmpty[Int, immutable.SortedSet[Int]] = nes2

    val zs = immutable.SortedMap(1 -> "a", 2 -> "b")
    val nem = NonEmpty.cons(3 -> "c", zs)
    val nemT: NonEmpty[(Int, String), immutable.SortedMap[Int, String]] = nem

    val nem2 = nem + (4 -> "d")
    val nem2T: NonEmpty[(Int, String), immutable.SortedMap[Int, String]] = nem2

    // Operations specific to the wrapped collection are
    // available in `NonEmpty`
    Assert.assertEquals(Set(1, 3, 4), nes - 2)
    Assert.assertEquals(2, nel.indexOf(2))
  }

  @Test
  def factory(): Unit = {
    val nel = NonEmpty[List[Int]](1, 2, 3)
    val nelT: NonEmpty[Int, List[Int]] = nel
    Assert.assertEquals(1 :: 2 :: 3 :: Nil, nel.coll)

    val nem = NonEmpty[Map[Int, Boolean]](1 -> true, 2 -> true, 3 -> false)
    val nemT: NonEmpty[(Int, Boolean), Map[Int, Boolean]] = nem
    Assert.assertEquals(Map(1 -> true, 2 -> true, 3 -> false), nem.coll)

    val nebs = NonEmpty[immutable.BitSet](1, 2, 3)
    val nebsT: NonEmpty[Int, immutable.BitSet] = nebs
    Assert.assertEquals(immutable.BitSet(1, 2, 3), nebs.coll)

    val ness = NonEmpty[immutable.TreeSet[Int]](1, 2, 3)
    val nessT: NonEmpty[Int, immutable.TreeSet[Int]] = ness
    Assert.assertEquals(immutable.TreeSet(1, 2, 3), ness.coll)

    val nesm = NonEmpty[immutable.SortedMap[Int, Boolean]](1 -> true, 2 -> true, 3 -> false)
    val nesmT: NonEmpty[(Int, Boolean), immutable.SortedMap[Int, Boolean]] = nesm
    Assert.assertEquals(immutable.SortedMap(1 -> true, 2 -> true, 3 -> false), nesm.coll)
  }

}
