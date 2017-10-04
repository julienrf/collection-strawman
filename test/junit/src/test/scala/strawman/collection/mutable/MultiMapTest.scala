package strawman
package collection.mutable

import strawman.collection.Iterator

import org.junit.{Assert, Test}
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(classOf[JUnit4])
class MultiMapTest {

  @Test
  def multiMap(): Unit = {
    val mm = MultiMap.empty[Int, String]

    mm += 1 -> "a"
    mm += 2 -> "b"
    mm += 1 -> "c"

    val m = Map(2 -> Set("b"), 1 -> Set("c", "a"))
    Assert.assertEquals(m, mm)
    Assert.assertEquals(mm, m)
    Assert.assertEquals(m.##, mm.##)

    Assert.assertTrue(mm.entryExists(1, _ == "a"))
    Assert.assertFalse(mm.entryExists(1, _ == "b"))
    Assert.assertTrue(mm.entryExists(2, _ == "b"))

    mm -= 1 -> "a"
    Assert.assertFalse(mm.entryExists(1, _ == "a"))
    Assert.assertTrue(mm.entryExists(1, _ == "c"))

    mm -= 2 -> "b"
    Assert.assertFalse(mm.contains(2))

  }

  @Test
  def sortedMultiMap(): Unit = {
    val mm = new MultiMap[Int, String](TreeMap.empty, TreeSet)

    mm += 1 -> "a"
    mm += 2 -> "b"
    mm += 1 -> "c"
    mm += 3 -> "d"
    mm += 1 -> "e"

    Assert.assertTrue(mm.keysIterator().sameElements(Iterator(1, 2, 3)))
    Assert.assertTrue(mm.apply(1).iterator().sameElements(Iterator("a", "c", "e")))

    // “Sortedness” is preserved by transformation operations
    Assert.assertEquals(mm.toMutableMap.className, mm.takeRight(3).toMutableMap.className)
  }

}
