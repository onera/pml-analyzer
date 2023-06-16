package views.interference.operators

import pml.model.configuration.TransactionLibrary
import pml.model.configuration.TransactionLibrary.{UserScenarioId, UserTransactionId}
import pml.model.hardware.Platform
import pml.model.software.Application
import views.interference.model.specification.InterferenceSpecification.{PhysicalTransaction, PhysicalTransactionId}

private[operators] trait Transform[L,R]{
  def apply(l:L):R
}

object Transform {

  trait BasicInstances {
    self:Platform =>

    /**
      * Convert a physical id to the corresponding path of services
      * @group transform_operator
      */
    given Transform[PhysicalTransactionId,Option[PhysicalTransaction]] with {
      def apply(l: PhysicalTransactionId): Option[PhysicalTransaction] =
        transactionsByName.get(l)
    }

    /**
      * Convert an application to the set of transaction id its trigger
      * @group transform_operator
      */
    given Transform[Application,Set[PhysicalTransactionId]] with {
      def apply(l: Application): Set[PhysicalTransactionId] =
        transactionsBySW.getOrElse(l, Set.empty)
    }
  }

  trait TransactionLibraryInstances {
    self:TransactionLibrary & Platform =>

    /**
      * Convert a user transaction to its physical transaction id
      * @group transform_operator
      */
    given Transform[Transaction,Option[PhysicalTransactionId]] with {
      def apply(l: Transaction): Option[PhysicalTransactionId] =
        transactionByUserName.get(l.userName)
    }

    /**
      * Convert a user defined scenario to the set of its physical scenario ids
      * @group transform_operator
      */
    given Transform[Scenario,Set[PhysicalTransactionId]] with {
      def apply(l: Scenario): Set[PhysicalTransactionId] =
        scenarioByUserName.getOrElse(l.userName, Set.empty)
    }

    /**
      * Convert a user transaction id to its physical transaction id
      * @group transform_operator
      */
    given Transform[UserTransactionId,Option[PhysicalTransactionId]] with {
      def apply(l: UserTransactionId): Option[PhysicalTransactionId] =
        transactionByUserName.get(l)
    }

    /**
      * Convert a user scenario id to the set of its physical scenario ids
      * @group transform_operator
      */
    given Transform[UserScenarioId,Set[PhysicalTransactionId]] with {
      def apply(l: UserScenarioId): Set[PhysicalTransactionId] =
        scenarioByUserName.getOrElse(l, Set.empty)
    }
  }
}
