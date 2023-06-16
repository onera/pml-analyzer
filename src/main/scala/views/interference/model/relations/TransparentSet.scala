package views.interference.model.relations

import sourcecode.Name
import views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

import scala.collection.mutable.Set as MSet

case class TransparentSet[T]private(value:MSet[T])(using n:Name){
  val name:String = n.value
}

object TransparentSet{
  trait Instances {
    /**
      * Set gathering discarded transactions
      * @group transparent_relation
      */
    final implicit val transactionIsTransparent: TransparentSet[PhysicalTransactionId] = TransparentSet(MSet.empty)
  }
}
