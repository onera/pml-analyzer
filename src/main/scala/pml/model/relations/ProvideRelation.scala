package pml.model.relations

import pml.model.hardware.Hardware
import pml.model.service.Service
import sourcecode.Name

/**
  * The relations used to encode service providing
  *
  * @param iniValues initial values of the relation
  * @tparam L the left type
  * @tparam R the right type
  */
case class ProvideRelation[L, R] private(iniValues: Map[L, Set[R]])(using n:Name) extends Relation[L, R](iniValues)

object ProvideRelation {
  /**
    * The instances for the provide relations
    */
  trait Instances {

    /**
      * [[pml.model.service.Service]] provided by [[pml.model.hardware.Hardware]]
      * @group provide_relation
      */
    final implicit val PLProvideService: ProvideRelation[Hardware, Service] = ProvideRelation(Map.empty)

  }
}
