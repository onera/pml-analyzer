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

package onera.pmlanalyzer.pml.model.relations

import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.hardware.PlatformArbitrary.{*, given}
import onera.pmlanalyzer.pml.model.service.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class UseRelationTest  extends AnyFlatSpec
  with ScalaCheckPropertyChecks
  with should.Matchers {

  //FIXME modify the generator of use to directly consider a linking relation?
  "UseRelation" should "encode properly use" in {
    forAll(minSuccessful(10)) {
      (p:PopulatedPlatform) =>
        import p.given
        import p.*
        forAll(minSuccessful(10)) {
          (link:Map[Hardware,Set[Hardware]]) =>
            applyAll(link, link=true)
            forAll(minSuccessful(10)) {
              (use:Map[Initiator,Set[Service]]) =>
                for {
                  (i,ss) <- use
                } yield {
                  i read ss
                  i write ss
                  for {
                    s <- ss
                  }
                  i.targetService should contain (s)
                }
            }
            applyAll(link, link=false)
        }
    }
  }
}
