package strawman.collection.mutable

import strawman.collection.immutable.List

import org.junit.Test

class LinkedListTest {

  @Test
  def fromTerminates(): Unit = {
    LinkedList.from(List(1)) // Doesn’t throw a StackOverflowException
  }

}
