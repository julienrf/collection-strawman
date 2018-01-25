/*
rule = "scala:fix.Collectionstrawman_v0"
 */
package fix

import scala.collection.mutable

object Collectionstrawman_v0_mutableMultiMap {

  val xs: mutable.MultiMap[Int, String] =
    new mutable.HashMap[Int, mutable.Set[String]] with mutable.MultiMap[Int, String]

}
