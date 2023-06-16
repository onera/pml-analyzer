package views.interference.model.relations

import pml.model.configuration.TransactionLibrary.UserScenarioId
import pml.model.relations.{AntiReflexiveSymmetricEndomorphism, Endomorphism}
import pml.model.software.Application
import sourcecode.Name
import views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

case class ExclusiveRelation[A] private(iniValues: Map[A, Set[A]])(using n:Name) extends AntiReflexiveSymmetricEndomorphism[A](iniValues)

object ExclusiveRelation{
  trait GeneralInstances {
    /**
      * Relation gathering user defined exclusive transactions
      * @group exclusive_relation
      */
    final implicit val transactionExclusive: ExclusiveRelation[PhysicalTransactionId] = ExclusiveRelation(Map.empty)
  }
  trait LibraryInstances {
    /**
      * Relation gathering user defined exclusive scenarios
      * @group exclusive_relation
      */
    final implicit val userScenarioExclusive: ExclusiveRelation[UserScenarioId]= ExclusiveRelation(Map.empty)
  }
  trait ApplicationInstances {
    /**
      * Relation gather user defined exclusive software
      * @group exclusive_relation
      */
    final implicit val swExclusive: ExclusiveRelation[Application] = ExclusiveRelation(Map.empty)
  }
}