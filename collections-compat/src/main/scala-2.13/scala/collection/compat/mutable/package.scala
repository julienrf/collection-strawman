package scala.collection.compat

package object mutable {

  type MultiMap[A, B] = scala.collection.mutable.DeprecatedMultiMap[A, B]

}
