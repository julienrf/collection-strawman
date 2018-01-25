package fix

import scala.collection.mutable

object Collectionstrawman_v0_mutableMultiMap {

  val xs: mutable.DeprecatedMultiMap[Int, String] =
    new mutable.HashMap[Int, mutable.Set[String]] with mutable.DeprecatedMultiMap[Int, String]

}
