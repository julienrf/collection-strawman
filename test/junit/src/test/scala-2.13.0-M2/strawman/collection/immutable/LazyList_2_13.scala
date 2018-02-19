package strawman.collection.immutable

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.{Assert, Test}

@RunWith(classOf[JUnit4])
class LazyList_2_13 {
  @Test
  def laziness(): Unit = {
    lazy val fibs: LazyList[Int] = 0 #:: 1 #:: fibs.zip(fibs.tail).map { n => n._1 + n._2 }
    assert(List(0, 1, 1, 2) == fibs.take(4).to(List))

    var lazeCount = 0
    def lazeL(i: Int) = { lazeCount += 1; i }
    val xs21 = lazeL(1) #:: lazeL(2) #:: lazeL(3) #:: LazyList.empty

    Assert.assertEquals(0, lazeCount)
  }
}