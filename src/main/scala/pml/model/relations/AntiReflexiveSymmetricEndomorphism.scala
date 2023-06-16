package pml.model.relations

import pml.model.utils.Message
import sourcecode.Name

abstract class AntiReflexiveSymmetricEndomorphism[A](iniValues: Map[A, Set[A]])(using n:Name) extends Endomorphism[A](iniValues)  {
  override def add(a: A, b: A): Unit = if(a != b){
    super.add(a, b)
    super.add(b, a)
  } else
    println(Message.errorAntiReflexivityViolation(a,name))

  override def remove(a: A, b: A): Unit = {
    super.remove(a, b)
    super.remove(b, a)
  }
}
