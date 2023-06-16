package views.interference.operators

import views.interference.model.relations.EquivalenceRelation

private[operators] trait Equivalent[T] {
  def equivalent(l:T,r:T):Unit
}

object Equivalent{

  trait Ops {
    extension[T](l:T){
      def equivalent(r:T)(implicit ev:Equivalent[T]): Unit = ev.equivalent(l,r)
    }
  }

  given [T] (using ev:EquivalenceRelation[T]): Equivalent[T] with {
    def equivalent(l:T, r:T): Unit = ev.add(l,r)
  }

}
