package pml.model.relations

import pml.model.hardware.{Initiator, Target}
import pml.model.service.Service
import pml.model.software.{Application, Data}
import sourcecode.Name

/**
  * The relations used to encode the service use
  *
  * @param iniValues initial values of the relation
  * @tparam L the left type
  * @tparam R the right type
  */
case class UseRelation[L, R] private(iniValues: Map[L, Set[R]])(using n:Name) extends Relation[L, R](iniValues)

object UseRelation {
  /**
    * The instances for the use relations
    */
  trait Instances {
    /**
     * [[pml.model.service.Service]] directly used by [[pml.model.hardware.Initiator]]
     *
     * @group use_relation
     */
    final implicit val InitiatorUseService: UseRelation[Initiator, Service] = UseRelation(Map.empty)
    
    /**
      * [[pml.model.software.Application]] hosted on [[pml.model.hardware.Initiator]]
      * @group use_relation
      */
    final implicit val SWUseInitiator: UseRelation[Application, Initiator] = UseRelation(Map.empty)

    /**
      * [[pml.model.service.Service]] used by [[pml.model.software.Application]]
      * @group use_relation
      */
    final implicit val SWUseService: UseRelation[Application, Service] = UseRelation(Map.empty)

    /**
      * [[pml.model.software.Data]] hosted on [[pml.model.hardware.Target]]
      * @group use_relation
      */
    final implicit val DataUseTarget: UseRelation[Data, Target] = UseRelation(Map.empty)

  }
}
