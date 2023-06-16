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

package pml.model

/**
  * Package containing all hardware PML nodes
  * @note [[SimpleTransporter]], [[Virtualizer]], [[Target]] and [[Initiator]] can only be instantiated
  *       in a [[Platform]] or a [[Composite]] but [[Platform]] and [[Composite]] can be specialised
  * @see usage are available in [[pml.examples.simpleKeystone.SimpleKeystonePlatform]]
  */
package object hardware
