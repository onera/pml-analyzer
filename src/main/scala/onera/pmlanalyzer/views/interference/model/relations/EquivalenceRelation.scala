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

package onera.pmlanalyzer.views.interference.model.relations

import onera.pmlanalyzer.pml.model.relations.{
  Endomorphism,
  ReflexiveSymmetricEndomorphism
}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.utils.Message
import sourcecode.Name

private[pmlanalyzer] final case class EquivalenceRelation[A] private (
    iniValues: Map[A, Set[A]]
)(using
    n: Name
) extends ReflexiveSymmetricEndomorphism[A](iniValues)

private[pmlanalyzer] object EquivalenceRelation {
  trait Instances {

    /** Relation gathering user defined equivalent services
      * @group equivalence_relation
      */
    final implicit val serviceEquivalent: EquivalenceRelation[Service] =
      EquivalenceRelation(Map.empty)
  }
}
