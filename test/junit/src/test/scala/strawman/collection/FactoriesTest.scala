package strawman.collection

import strawman.collection.mutable.ArrayBuffer
import org.junit.{Assert, Test}
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(classOf[JUnit4])
class FactoriesTest {

  val seq: Seq[Int] = ArrayBuffer(1, 2, 3)

  @Test def buildFromUsesSourceCollectionFactory(): Unit = {

    def cloneCollection[A, C](xs: Iterable[A])(implicit bf: BuildFrom[xs.type, A, C]): C =
      bf.fromSpecificIterable(xs)(xs)

    Assert.assertEquals("ArrayBuffer", cloneCollection(seq).className)
  }

  @Test def factoryIgnoresSourceCollectionFactory(): Unit = {

    def cloneElements[A, C](xs: Iterable[A])(cb: Factory[A, C]): C =
      cb.fromSpecificIterable(xs)

    Assert.assertEquals("List", cloneElements(seq)(Seq).className)
  }

}
