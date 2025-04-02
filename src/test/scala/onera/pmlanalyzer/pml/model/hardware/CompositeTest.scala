/*******************************************************************************
 * Copyright (c)  2023. ONERA
 * This file is part of PML Analyzer
 *
 * PML Analyzer is free software ;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation ;
 * either version 2 of  the License, or (at your option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program ;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package onera.pmlanalyzer.pml.model.hardware

import onera.pmlanalyzer.pml.model.hardware.{
  Composite,
  Initiator,
  Platform,
  SimpleTransporter,
  Target
}
import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
import onera.pmlanalyzer.pml.operators.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sourcecode.{File, Line, Name}

class CompositeTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  object CompositeTestPlatform
      extends Platform(Symbol("CompositeTestPlatform")) {

    final class Core private (
        val id: Symbol,
        info: ReflexiveInfo
    ) extends Composite(id, info) {
      def this()(using
          givenName: Name,
          givenInfo: ReflexiveInfo
      ) = {
        this(Symbol(givenName.value), givenInfo)
      }
    }
  }

  import CompositeTestPlatform.*

  "Hardware in different instances of a nested composite" should "have different names" in {
    final class Cluster private (
        val id: Symbol,
        info: ReflexiveInfo
    ) extends Composite(id, info) {
      def this()(using
          givenName: Name,
          givenInfo: ReflexiveInfo
      ) = {
        this(Symbol(givenName.value), givenInfo)
      }

      val c0 = Core()
      val c1 = Core()
    }

    val cl0 = Cluster()
    val cl1 = Cluster()
    cl0.name should not equal (cl1.name)
    cl0.c0.name should not equal (cl1.c0.name)
    cl0.c1.name should not equal (cl1.c1.name)
    cl0.c1.name should not equal (cl1.c1.name)
  }

  it should "raise a warning when using multiple implementation with the same name" in {

    /**
     * Such a pattern MUST NEVER be used since the
     * givenName will be used to name c0 and c1 and
     * givenInfo will be used to provide source code traceability info
     * to c0 and c1
     *
     * @param givenName implicit name at instantiation
     * @param info code traceability information at instantiation
     */
    final class IncorrectCluster private (info: ReflexiveInfo)(using
        givenName: Name
    ) extends Composite(Symbol(givenName.value), info) {

      def this()(using
          otherGivenName: Name,
          givenInfo: ReflexiveInfo
      ) = {
        this(givenInfo)
      }

      // Here the cores will use the givenName provided by givenName
      // since it is visible in the scope so they will be be both
      // named according to the name of the platform
      val c0 = Core()
      val c1 = Core()
    }

    val cl2 = IncorrectCluster()

    cl2.c0.name should equal(cl2.c1.name)
  }
}
