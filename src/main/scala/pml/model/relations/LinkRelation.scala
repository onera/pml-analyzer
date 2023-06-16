package pml.model.relations

import pml.model.hardware.Hardware
import pml.model.service.Service
import sourcecode.Name

/**
  * The endomorphisms used to encode platform links
  *
  * @param iniValues initial values of the relation
  * @tparam A the elements type
  */
case class LinkRelation[A] private(iniValues: Map[A, Set[A]])(using n:Name) extends Endomorphism[A](iniValues)

object LinkRelation {
  /**
    * The instances for the links
    */
  trait Instances {

    /**
      * [[pml.model.service.Service]] linked to [[pml.model.service.Service]]
      * @group link_relation
      */
    final implicit val ServiceLinkableToService: LinkRelation[Service] = LinkRelation(Map.empty)

    /**
      * [[pml.model.hardware.Hardware]] linked to [[pml.model.hardware.Hardware]]
      * @group link_relation
      */
    final implicit val PLLinkableToPL: LinkRelation[Hardware] = LinkRelation(Map.empty)

  }
}

