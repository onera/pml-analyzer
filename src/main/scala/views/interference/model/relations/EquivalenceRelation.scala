package views.interference.model.relations

import pml.model.relations.{ReflexiveSymmetricEndomorphism, Endomorphism}
import pml.model.service.Service
import pml.model.utils.Message
import sourcecode.Name

case class EquivalenceRelation[A] private(iniValues: Map[A, Set[A]])(using n:Name) extends ReflexiveSymmetricEndomorphism[A](iniValues)

object EquivalenceRelation{
  trait Instances {
    /**
      * Relation gathering user defined equivalent services
      * @group equivalence_relation
      */
    final implicit val serviceEquivalent: EquivalenceRelation[Service] = EquivalenceRelation(Map.empty)
  }
}