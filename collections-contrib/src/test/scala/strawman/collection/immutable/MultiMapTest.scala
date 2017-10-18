package strawman
package collection.immutable

import org.junit.{Assert, Test}
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(classOf[JUnit4])
class MultiMapTest {

  @Test
  def multiMap(): Unit = {
    val mm = MultiMap(1 -> "aa", 2 -> "b", 1 -> "c")

    val m = Map(2 -> Set("b"), 1 -> Set("c", "aa"))
    Assert.assertEquals(m, mm.to(Map))
    Assert.assertEquals(mm.to(Map), m)

    Assert.assertTrue(mm.entryExists(1, _ == "aa"))
    Assert.assertFalse(mm.entryExists(1, _ == "b"))
    Assert.assertTrue(mm.entryExists(2, _ == "b"))

    val mm2 = mm.mapByValue { case (k, v) => (k + v.length, v ++ v) }
    Assert.assertEquals(Map(2 -> Set("cc"), 3 -> Set("aaaa", "bb")), mm2.to(Map))

    val mm3 = mm - (1 -> "a")
    Assert.assertFalse(mm3.entryExists(1, _ == "a"))
    Assert.assertTrue(mm3.entryExists(1, _ == "c"))

    val mm4 = mm3 - (2 -> "b")
    Assert.assertFalse(mm4.contains(2))

  }

}