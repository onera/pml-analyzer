package pml.model.relations

import pml.model.service.Service
import pml.model.software.Application

/**
  * The relations used to encode authorized requests
  *
  * @param iniValues initial values of the relation
  * @tparam L the left type
  * @tparam R the right type
  */
case class AuthorizeRelation[L, R] private(iniValues: Map[L, Set[R]]) extends Relation[L, R](iniValues)

object AuthorizeRelation {
  /**
    * The instances for the authorize relation
    */
  trait Instances {

    /**
      * [[pml.model.service.Service]] that can be used by a [[pml.model.software.Application]]
      * @group auth_relation
      */
    final implicit val SWAuthorizeService: AuthorizeRelation[Application, Service] = AuthorizeRelation(Map.empty)

  }
}
