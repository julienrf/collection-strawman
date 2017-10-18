package strawman.collection

import org.junit.{Assert, Test}

class MultiMapTest {

  @Test
  def equality(): Unit = {
    val mm1 = MultiMap(1 -> "a", 2 -> "b", 1 -> "c")
    val mm2 = MultiMap(1 -> "a", 2 -> "b", 1 -> "c")

    Assert.assertEquals(mm2, mm1)
    Assert.assertEquals(mm1, mm2)
    Assert.assertEquals(mm1.##, mm2.##)
  }

  @Test
  def byValue(): Unit = {
    Assert.assertEquals(
      MultiMap(1 -> "b"),
      MultiMap(1 -> "a").concat(MultiMap(1 -> "b"))
    )
    Assert.assertEquals(
      MultiMap(1 -> "a", 1 -> "b"),
      MultiMap(1 -> "a").concatByValue(MultiMap(1 -> "b").toIterableByValue)
    )
  }
}
