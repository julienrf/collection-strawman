package strawman.collection

import org.junit.Test

class BitSetTest {

  @Test
  def upcasts(): Unit = {
    val bs: BitSet = BitSet(1, 2, 3)
    val bs2 = bs.map(x => x)
    val bs3: BitSet = bs2
    val ss = bs.toSortedSet.map(x => x.toString)
    val ss2: SortedSet[String] = ss
    val s = bs.unsorted.map(x => x % 2 == 0)
    val s2: Set[Boolean] = s
  }

}
