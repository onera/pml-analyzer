package views.interference.model.relations

import pml.model.hardware.Hardware
import pml.model.relations.Relation
import pml.model.service.Service
import views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

case class InterfereRelation[L,R] private(iniValues: Map[L, Set[R]]) extends Relation[L,R](iniValues)

//FIXME TO ENSURE CORRECTNESS THE INTERFERE ENDOMORPHISMS SHOULD BE ANTI-REFLEXIVE AND SYMMETRIC TO BE
//  CONSISTENT WITH INTERFERENCE SPECIFICATION BASE TRAIT
object InterfereRelation{
  trait Instances {
    /**
      * Relation gathering user defined service interferences caused by a transaction
      * @group interfere_relation
      */
    final implicit val physicalTransactionIdInterfereWithService: InterfereRelation[PhysicalTransactionId, Service] = InterfereRelation(Map.empty)

    /**
      * Relation gathering user defined service interferences
      * @group interfere_relation
      */
    final implicit val serviceInterfereWithService: InterfereRelation[Service, Service] = InterfereRelation(Map.empty)

    /**
      * Relation gathering user defined interfering hardware
      * @group interfere_relation
      */
    final implicit val hardwareExclusive: InterfereRelation[Hardware, Hardware] = InterfereRelation(Map.empty)
  }
}