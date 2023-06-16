package views.interference.operators

import views.interference.model.relations.ExclusiveRelation

private[operators] trait Exclusive[T] {
  def apply(l:T, r:T): Unit
}

object Exclusive{

  /**
    * If an element l of type T can be exclusive with another element r of type T, the following operator can be used
    * {{{l exclusiveWith r}}}
    */
  trait Ops {
    extension[T](l:T){
      def exclusiveWith(r:T)(using ev:Exclusive[T]): Unit = ev(l,r)
    }
  }

  given [T] (using relation: ExclusiveRelation[T]): Exclusive[T] with {
    def apply(l: T, r: T): Unit = relation.add(l, r)
  }
}
