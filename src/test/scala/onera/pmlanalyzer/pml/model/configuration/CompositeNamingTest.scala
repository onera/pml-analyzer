package onera.pmlanalyzer.pml.model.configuration

import onera.pmlanalyzer.pml.model.hardware.{Composite, Initiator, Platform, SimpleTransporter, Target}
import onera.pmlanalyzer.pml.operators.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sourcecode.{File, Line, Name}

class CompositeNamingTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  class NamingPlatform(name: Symbol) extends Platform(name) {
    def this()(implicit implicitName: Name) = {
      this(Symbol(implicitName.value))
    }

    final class Core private (
        val id: Symbol,
        loc: Line,
        foc: File
    ) extends Composite(id, loc, foc) {
      def this()(implicit
                 givenName: Name,
                 givenLine: Line,
                 givenFile: File
      ) = {
        this(Symbol(givenName.value), givenLine, givenFile)
      }
    }

    final class Cluster private (
        val id: Symbol,
        loc: Line,
        foc: File
    ) extends Composite(id, loc, foc) {
      def this()(implicit
          givenName: Name,
          givenLine: Line,
          givenFile: File
      ) = {
        this(Symbol(givenName.value), givenLine, givenFile)
      }
      val c0 = Core()
      val c1 = Core()
    }

    val cl0 = Cluster()
    val cl1 = Cluster()
  }

  object InstantiatedNamingPlatform extends NamingPlatform

  "Hardware in different instances of a nested composite" should "have different names" in {
    assert(InstantiatedNamingPlatform.cl0.name != InstantiatedNamingPlatform.cl1.name)
    assert(InstantiatedNamingPlatform.cl0.c0.name != InstantiatedNamingPlatform.cl1.c0.name)
    assert(InstantiatedNamingPlatform.cl0.c1.name != InstantiatedNamingPlatform.cl1.c1.name)
    assert(InstantiatedNamingPlatform.cl0.c1.name != InstantiatedNamingPlatform.cl1.c1.name)
  }

}
