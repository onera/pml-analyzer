/*******************************************************************************
 * Copyright (c)  2021. ONERA
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

package pml.model.relations

import pml.model.hardware.Hardware
import pml.model.service.Service
import sourcecode.Name

/**
  * The endomorphisms used to encode platform links
  *
  * @param iniValues initial values of the relation
  * @tparam A the elements type
  */
case class LinkRelation[A] private(iniValues: Map[A, Set[A]])(using n:Name) extends Endomorphism[A](iniValues)

object LinkRelation {
  /**
    * The instances for the links
    */
  trait Instances {

    /**
      * [[pml.model.service.Service]] linked to [[pml.model.service.Service]]
      * @group link_relation
      */
    final implicit val ServiceLinkableToService: LinkRelation[Service] = LinkRelation(Map.empty)

    /**
      * [[pml.model.hardware.Hardware]] linked to [[pml.model.hardware.Hardware]]
      * @group link_relation
      */
    final implicit val PLLinkableToPL: LinkRelation[Hardware] = LinkRelation(Map.empty)

  }
}

