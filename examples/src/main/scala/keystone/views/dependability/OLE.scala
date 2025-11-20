package keystone.views.dependability

import onera.pmlanalyzer.views.dependability.model.BaseEnumeration
import onera.pmlanalyzer.views.dependability.operators.{
  IsFinite,
  IsShadowOrdering
}

enum OLE(i: Int, n: String) extends BaseEnumeration(i, n) {

  case Erroneous extends OLE(3, "erroneous")
  case Lost extends OLE(2, "lost")
  case OK extends OLE(1, "ok")
  case None extends OLE(0, "none")
}

object OLE {

  def containerShadow(init: OLE, containerFM: OLE): OLE =
    (init, containerFM) match {
      case (Lost, _) | (_, Lost)           => Lost
      case (Erroneous, _) | (_, Erroneous) => Erroneous
      case _                               => OK
    }

  def corruptingFM(fm: OLE): Boolean = fm == Erroneous

  def inputShadow(input: OLE, containerState: OLE): OLE = {
    if (input == OK && containerState == Erroneous)
      OK
    else if (input == Erroneous && containerState == OK)
      Erroneous
    else
      containerState
  }

  implicit val isFinite: IsFinite[OLE] = new IsFinite[OLE] {
    val none: OLE = None

    def allWithNone: Seq[OLE] = Seq(Erroneous, Lost, OK, None)

    def name(x: OLE): Symbol = Symbol(x.toString)
  }

  implicit object isShadowOrdering extends IsShadowOrdering[OLE] {
    def containerShadow(init: OLE, containerState: OLE): OLE =
      OLE.containerShadow(init, containerState)

    def corruptingFM(fm: OLE): Boolean = OLE.corruptingFM(fm)

    def inputShadow(input: OLE, containerState: OLE): OLE =
      OLE.inputShadow(input, containerState)
  }
}
