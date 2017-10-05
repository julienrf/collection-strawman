package strawman
package collection
package mutable

import org.junit.{Assert, Test}

class MultiSetTest {

  @Test
  def multiSet(): Unit = {
    val ms = MultiSet.empty[String]
    ms += "foo"
    ms += "bar"
    ms += "foo"
    Assert.assertEquals(2, ms.get("foo"))
    Assert.assertEquals(1, ms.get("bar"))
    Assert.assertEquals(0, ms.get("baz"))
    val ss = for ((elem, n) <- ms) yield elem * n
    Assert.assertEquals(Set("foofoo", "bar"), ss.to(Set))
    ms -= "foo"
    ms -= "bar"
    Assert.assertEquals(1, ms.get("foo"))
    Assert.assertEquals(0, ms.get("bar"))

    val ms2 = MultiSet("foo")
    Assert.assertTrue(ms == ms2)
    Assert.assertTrue(ms2 == ms)
    Assert.assertEquals(ms.##, ms2.##)
  }

  @Test
  def sortedMultiSet(): Unit = {
    val ms = new MultiSet[String](TreeMap.empty, () => TreeMap.newBuilder[String, Int]())
    ms += "foo"
    ms += "bar"
    ms += "foo"
    Assert.assertTrue(ms.iterator().sameElements(Iterator("bar" -> 1, "foo" -> 2)))
  }

}
