package pml.model.relations

import scalaz.Memo.immutableHashMapMemo
import sourcecode.Name

/**
  * Refinement for endomorphisms (relation on the same type A)
  *
  * @param iniValues initial values of the relation
  * @tparam A the elements type
  */
abstract class Endomorphism[A](iniValues: Map[A, Set[A]])(using n:Name) extends Relation[A, A](iniValues: Map[A, Set[A]]) {

  /**
    * Remove an element from both the input and output set
    *
    * @param a the element to remove
    */
  override def remove(a: A): Unit = {
    apply(a).foreach(remove(a, _))
    inverse(a).foreach(remove(_, a))
  }

  /**
    * Provide the reflexive and transitive closure of a by the endomorphism
    *
    * @param a the input element
    * @return the set of all elements indirectly related to a
    */
  def closure(a: A): Set[A] = {
    lazy val rec: ((A, Set[A])) => Set[A] = immutableHashMapMemo {
      s =>
        if (s._2.contains(s._1))
          Set(s._1)
        else
          apply(s._1).flatMap(rec(_, s._2 + s._1)) + s._1
    }
    rec(a, Set.empty)
  }

  /**
    * Provide the reflexive and transitive inverse closure of a by the endomorphism
    *
    * @param a the input element
    * @return the set of all elements that indirectly relate to a
    */
  def inverseClosure(a: A): Set[A] = {
    lazy val rec: ((A, Set[A])) => Set[A] = immutableHashMapMemo {
      s =>
        if (s._2.contains(s._1))
          Set(s._1)
        else
          inverse(s._1).flatMap(rec(_, s._2 + s._1)) + s._1
    }
    rec(a, Set.empty)
  }
}
