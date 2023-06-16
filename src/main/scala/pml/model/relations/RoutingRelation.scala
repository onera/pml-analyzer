package pml.model.relations

import pml.model.hardware.Initiator
import pml.model.service.Service
import sourcecode.Name

/**
  * relation used to encode the routing constraints
  *
  * @param iniValues initial values of the relation
  * @tparam L the left type
  * @tparam R the right type
  */
case class RoutingRelation[L, R] private(iniValues: Map[L, Set[R]])(using n:Name) extends Relation[L, R](iniValues)

object RoutingRelation {
  /**
    * Instances of routing relations
    */
  trait Instances {

    /**
      * Relation gathering routing constraints
      * @group route_relation
      */
    final implicit val InitiatorRouting: RoutingRelation[(Initiator, Service, Service), Service] = RoutingRelation(Map.empty)
  }
}
