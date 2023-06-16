package views.interference.model.relations

import pml.model.hardware.Hardware
import pml.model.relations.Relation
import pml.model.service.Service
import sourcecode.Name
import views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

case class NotInterfereRelation[L,R] private(iniValues: Map[L, Set[R]])(using n:Name) extends Relation[L,R](iniValues)

object NotInterfereRelation{
  trait Instances {
    /**
      * Relation gathering user defined service non-interference caused by a transaction
      * @group interfere_relation
      */
    final implicit val physicalTransactionIdNotInterfereWithService: NotInterfereRelation[PhysicalTransactionId,Service] = NotInterfereRelation(Map.empty)

    /**
      * Relation gathering user defined service non-interferences
      * @group interfere_relation
      */
    final implicit val serviceNotInterfereWithService: NotInterfereRelation[Service, Service] = NotInterfereRelation(Map.empty)

    /**
      * Relation gathering user defined non-interfering hardware
      * @group interfere_relation
      */
    final implicit val hardwareNotExclusive: NotInterfereRelation[Hardware, Hardware] = NotInterfereRelation(Map.empty)
  }
}