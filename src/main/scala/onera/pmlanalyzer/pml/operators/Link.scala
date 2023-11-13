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

package onera.pmlanalyzer.pml.operators

import onera.pmlanalyzer.pml.model.hardware.{Hardware, Initiator, Target, Transporter}
import onera.pmlanalyzer.pml.model.relations.LinkRelation
import onera.pmlanalyzer.pml.model.service.{Load, Service, Store}

import scala.reflect.{ClassTag, classTag}

/**
  * Base trait for link operation
  *
  * @tparam L the left type
  * @tparam R the right type
  */
trait Link[L, R] {
  def link(a: L, b: R): Unit
  def unlink(a: L, b: R): Unit
}

/**
  * Extension methods and inferences rules of high priority
  */
object Link {

  protected sealed trait HardwareLink[-L,-R]{
    def apply(x:L|R):Hardware
  }
  given HardwareLink[Initiator, Target] with {
    def apply(x: Initiator|Target): Hardware = x
  }
  given HardwareLink[Initiator, Transporter] with {
    def apply(x: Initiator|Transporter): Hardware = x
  }
  given HardwareLink[Transporter, Transporter] with {
    def apply(x: Transporter): Hardware = x
  }
  given HardwareLink[Transporter, Target] with {
    def apply(x: Transporter | Target): Hardware = x
  }

  protected sealed trait ServiceLink[L,R]{
    def apply(x: L|R): Service
  }
  given ServiceLink[Load,Load] with {
    def apply(x: Load): Service = x
  }
  given ServiceLink[Store,Store] with {
    def apply(x: Store): Service = x
  }

  /** ------------------------------------------------------------------------------------------------------------------
    * EXTENSION METHODS
    * --------------------------------------------------------------------------------------------------------------- */

  /**
    * If an element x of type T is linkable then the operator can be used as follows
    *
    * to link an element l to an element r
    * {{{ l link r }}}
    * to unlink an element l with an element r
    * {{{ l unlink r }}}
    *
    * @note currently any [[pml.model.hardware.Hardware]] or [[pml.model.service.Service]] are linkable
    *       with soundness restriction. Additionally, the link can be made only within a platform container
    * @see usage are available in [[pml.examples.simpleKeystone.SimpleKeystonePlatform]]
    */
  trait Ops {

    extension[L] (self: L) {
      /**
        * PML keyword to link two objects
        *
        * @param b        the other object
        * @param linkable the proof that self and b can be linked
        * @tparam R the type of the other object
        */
      def link[R](b: R)(using linkable: Link[L, R]): Unit = linkable.link(self, b)

      /**
        * PML keyword to unlink two objects
        *
        * @param b        the other object
        * @param linkable the proof that self and b can be linked
        * @tparam R the type of the other object
        */
      def unlink[R](b: R)(using linkable: Link[L, R]): Unit = linkable.unlink(self, b)

    }
  }

  /** ------------------------------------------------------------------------------------------------------------------
    * INFERENCE RULES
    * --------------------------------------------------------------------------------------------------------------- */

  /**
    * A linking implementation between two types can be derived from an endomorphism over a supertype
    *
    * @return the implementation of the link
    */
  given [LS,RS] (using canLink:ServiceLink[LS,RS], l: LinkRelation[Service]): Link[LS,RS] with {
    def link(a: LS, b: RS): Unit = l.add(canLink(a), canLink(b))
    def unlink(a: LS, b: RS): Unit = l.remove(canLink(a), canLink(b))
  }

  /**
    * A linking implementation can be provided for all physical component
    *
    * @return the implementation of the link for two physical components
    */
  given [LH,RH] (using canLink:HardwareLink[LH,RH],
                 mAB: LinkRelation[Hardware],
                 mLL: Link[Load, Load],
                 mSS: Link[Store, Store],
                 pAS: Provided[LH, Store],
                 pAL: Provided[LH, Load],
                 pBS: Provided[RH, Store],
                 pBL: Provided[RH, Load]): Link[LH, RH] with {
    def link(a: LH, b: RH): Unit = {
      mAB.add(canLink(a), canLink(b))
      a.loads foreach { l =>
        b.loads.foreach {
          l link _
        }
      }
      a.stores foreach { s =>
        b.stores.foreach {
          s link _
        }
      }
    }

    def unlink(a: LH, b: RH): Unit = {
      mAB.remove(canLink(a), canLink(b))
      a.loads foreach { l =>
        b.loads.foreach {
          l unlink _
        }
      }
      a.stores foreach { s =>
        b.stores.foreach {
          s unlink _
        }
      }
    }
  }
}