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

package pml.operators

import pml.model.relations.LinkRelation
import pml.model.service.{Load, Service, Store}
import scala.reflect._
/**
  * Base trait for linked operation
  * @tparam L the left type
  * @tparam R the right type
  */
trait Linked[L, R] {
  def apply(a: L): Set[R]
  def inverse(b: R): Set[L]
}

/**
  * Extension methods and inferences rules
  */
object Linked {

  /** ------------------------------------------------------------------------------------------------------------------
    * EXTENSION METHODS
    * --------------------------------------------------------------------------------------------------------------- */

  /**
    * If an element l of type L is linked to other elements of type R then the operator can be used
    *
    * To access to the element of type R linked to l
    * {{{l.linked[R]}}}
    * To access to the element of type R pointing to l
    * {{{ l.inverse[R] }}}
    * @note Linked operators is an advanced feature and should not be necessary for basic models
    */
  trait Ops {

    /**
      * Extension method
      */
     extension [L](self: L) {

      /**
        * PML keyword to retrieve elements linked to self
        * @param linked the proof that elements of type R can be linked to self
        * @tparam R the type of linked elements
        * @return the set of linked elements
        */
      def linked[R](using linked: Linked[L, R]) : Set[R] = linked(self)

      /**
        * PML keyword to retrieve elements pointing to self
        * @param linked the proof that elements of type L can point to self
        * @tparam R the type of pointing elements
        * @return the set of pointing elements
        */
      def inverse[R](using linked: Linked[R,L]) : Set[R] = linked.inverse(self)
    }
  }

  /** ------------------------------------------------------------------------------------------------------------------
    * INFERENCE RULES
    * --------------------------------------------------------------------------------------------------------------- */

  /**
    * A linked implementation can be derived from an endomorphism over a type
    *
    * @return the implementation of the linked
    */
  given[L](using l: LinkRelation[L]): Linked[L, L] with {
    def apply(a: L): Set[L] = l(a)
    def inverse(b: L): Set[L] = l.inverse(b)
  }

  /**
    * A linked implementation over loads can be derived from an endomorphism over a services
    * @return the implementation of the linked
    */
  given [T <: Load | Store : Typeable](using l: Linked[Service, Service]): Linked[T, T] with {
    def apply(a: T): Set[T] = l(a) collect { case l: T => l }
    def inverse(b: T): Set[T] = l.inverse(b) collect { case l: T => l }
  }
}