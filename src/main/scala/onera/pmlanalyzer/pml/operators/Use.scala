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

package onera.pmlanalyzer.pml.operators

import onera.pmlanalyzer.pml.model.hardware.{Initiator, Target}
import onera.pmlanalyzer.pml.model.relations.{AuthorizeRelation, UseRelation}
import onera.pmlanalyzer.pml.model.service.{Load, Service, Store}
import onera.pmlanalyzer.pml.model.software.{Application, Data}

/**
  * Base trait for use operator
  *
  * @tparam L the left type
  * @tparam R the right type
  */
trait Use[L, R] {
  def apply(l: L, r: R): Unit
}

object Use {

  /** ------------------------------------------------------------------------------------------------------------------
    * EXTENSION METHODS
    * --------------------------------------------------------------------------------------------------------------- */

  /**
    * If an element l can use an element r then the following operators can be used
    *
    * l (e.g. an [[pml.model.software.Application]]) is hosted by an element r (e.g. a [[pml.model.hardware.Initiator]])
    * {{{ l hostedBy r}}}
    * l (e.g. an [[pml.model.software.Application]]) reads an element r (e.g. a [[pml.model.software.Data]])
    * {{{ l read r}}}
    * l writes an element r
    * {{{ l write r}}}
    *
    * @note [[pml.model.software]] uses [[pml.model.hardware.Hardware]] through hostedBy keyword
    *       and [[pml.model.software.Application]] uses [[pml.model.software.Data]] or directly [[pml.model.hardware.Target]]
    *       through read and write keywords
    * @see usage can be found in [[pml.examples.simpleKeystone.SimpleSoftwareAllocation]]
    */
  trait Ops {

    /**
      * Extension method class
      */
    extension[L <: Application | Initiator] (self: L) {

      private def use[B](b: B)(using ev: Use[L, B]): (L, B) = {
        ev(self, b)
        self -> b
      }

      private def use[B](b: Set[B])(using ev: Use[L, B]): Set[(L, B)] = b.map(x => use(x))

      /**
        * The PML keyword specify that self reads something
        *
        * @param b  the element to read
        * @param p  that the element provide load services
        * @param ev the proof that an application can use load services
        * @tparam B the type of b
        * @return the link
        */
      def read[B](b: B)(using p: Provided[B, Load], ev: Use[L, Load]): Set[(L, Load)] = use(b.loads)

      /**
        * The PML keyword specify that self uses some load services
        *
        * @param b  the set of services
        * @param ev the proof that an application can use load services
        * @return the link
        */
      def read(b: Set[Service])(using ev: Use[L, Load]): Set[(L, Load)] = use(b.collect { case l: Load => l })

      /**
        * The PML keyword specify that self reads something
        *
        * @param b  the set of elements to read
        * @param p  that the element provide load services
        * @param ev the proof that an application can use load services
        * @tparam B the type of b
        * @return the link
        */
      def read[B](b: Set[B])(using p: Provided[B, Load], ev: Use[L, Load]): Set[(L, Load)] = use(b.loads)

      /**
        * The PML keyword specify that self writes something
        *
        * @param b  the element to write
        * @param p  that the element provide store services
        * @param ev the proof that an application can use store services
        * @tparam B the type of b
        * @return the link
        */
      def write[B](b: B)(using p: Provided[B, Store], ev: Use[L, Store]): Set[(L, Store)] = use(b.stores)

      /**
        * The PML keyword specify that self uses some store services
        *
        * @param b  the set of services
        * @param ev the proof that an application can use store services
        * @return the link
        */
      def write(b: Set[Service])(using ev: Use[L, Store]): Set[(L, Store)] = use(b.collect { case l: Store => l })
    }

    extension [L<: Application](self:L) {
      /**
       * The PML keyword to allocate self on an initiator
       *
       * @param b  the initiator
       * @param ev the proof that self can use an initiator
       * @return the link
       */
      def hostedBy(b: Initiator)(using ev: Use[L, Initiator]): (L, Initiator) = {
        ev(self, b)
        self -> b
      }
    }


    /**
      * Extension method class
      *
      * @param self the element on which keyword can be used
      * @tparam L the concrete type of the element
      */
    extension[L <: Data] (self: L) {

      /**
        * The PML keyword to allocate self on an target
        *
        * @param b  the target
        * @param ev the proof that self can use an target
        * @return the link
        */
      def hostedBy(b: Target)(using ev: Use[Data, Target]): (Data, Target) = {
        ev(self, b)
        self -> b
      }
    }

  }

  /** ------------------------------------------------------------------------------------------------------------------
    * INFERENCE RULES
    * --------------------------------------------------------------------------------------------------------------- */

  given [I <: Initiator, S<:Service] (using l:UseRelation[I,Service]): Use[I,S] with {
    def apply(a: I, b: S): Unit = l.add(a,b)
  }

  given [A <: Application, S<:Service] (using l: UseRelation[Application, Service], aR: AuthorizeRelation[Application, Service]): Use[A, S] with {
    def apply(a: A, b: S): Unit = {
      l.add(a, b)
      aR.add(a, b)
    }
  }

  given [AD<:Application | Data, H] (using l: UseRelation[AD, H]): Use[AD, H] with {
    def apply(a: AD, b: H): Unit = l.add(a, b)
  }
}