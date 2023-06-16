package pml.model.relations

import pml.model.utils.Message
import sourcecode.Name

abstract class ReflexiveSymmetricEndomorphism[A](iniValues: Map[A, Set[A]])(using n:Name) extends Endomorphism[A](iniValues)  {
  override def add(a: A, b: A): Unit ={
    super.add(a, b)
    super.add(b, a)
    super.add(a, a)
    super.add(b, b)
  }

  override def remove(a: A, b: A): Unit = if(a != b){
    super.remove(a, b)
    super.remove(b, a)
  } else println(Message.errorReflexivityViolation(a,name))
}
