/** *****************************************************************************
  * Copyright (c) 2023. ONERA This file is part of PML Analyzer
  *
  * PML Analyzer is free software ; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation ; either version 2 of the License, or (at your
  * option) any later version.
  *
  * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program ; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */

package onera.pmlanalyzer.pml.operators

import onera.pmlanalyzer.pml.model.service.Service

/** Concatenation operation over two types FIXME IS THIS REALLY HELPFUL
  * @tparam L
  *   left type
  * @tparam R
  *   right type
  */
trait Merge[-L, -R] {
  type Result
  def apply(l: L, r: R): Result
}

/** Extension methods and inferences rules
  */
object Merge {

  /** ------------------------------------------------------------------------------------------------------------------
    * EXTENSION METHODS
    * ---------------------------------------------------------------------------------------------------------------
    */

  /** If an element l is mergeable with an element r then the following operator
    * can be used {{{l and r}}}
    */
  trait Ops {

    /** Extension method class
      */
    extension [L](x: L) {

      /** PML keyword to merge an element with x
        * @param y
        *   the other element
        * @param ev
        *   the proof that x can be merged with y
        * @tparam R
        *   the type of y
        * @tparam O
        *   the resulting type of the merge
        * @return
        *   the merge of x and y
        */
      def and[R, O](y: R)(implicit ev: Merge.Aux[L, R, O]): O = ev(x, y)
    }
  }

  /** Util type to alleviate type unification problem
    * @tparam L
    *   left type
    * @tparam R
    *   right type
    * @tparam O
    *   result type for the concatenation, functionally defined by inner type
    *   Result
    */
  type Aux[L, R, O] = Merge[L, R] {
    type Result = O
  }

  /** ------------------------------------------------------------------------------------------------------------------
    * INFERENCE RULES
    * ---------------------------------------------------------------------------------------------------------------
    */

  /** Two sets of objects providing basic services can be concatenated as a set
    * of basic services
    * @param pT
    *   the relation capturing the basic services provided by L
    * @param pU
    *   the relation capturing the basic services provided by R
    * @tparam L
    *   the left type
    * @tparam R
    *   the right type
    * @return
    *   the set of basic services provided by collections of L and R
    */
  implicit def setsTAreMergeable[L, R](implicit
      pT: Provided[L, Service],
      pU: Provided[R, Service]
  ): Aux[Set[L], Iterable[R], Set[Service]] = new Merge[Set[L], Iterable[R]] {
    type Result = Set[Service]
    def apply(l: Set[L], r: Iterable[R]): Set[Service] =
      l.services ++ r.services
  }

  /** Two sets of basic services can be concatenated as a set of basic services
    * @tparam L
    *   the left type
    * @tparam R
    *   the right type
    * @return
    *   the set of basic services
    */
  implicit def setsAreMergeable[L <: Service, R <: Service]
      : Aux[Set[L], IterableOnce[R], Set[Service]] =
    new Merge[Set[L], IterableOnce[R]] {
      type Result = Set[Service]
      def apply(l: Set[L], r: IterableOnce[R]): Set[Service] = l ++ r
    }

  /** A of basic services and an object providing services can be concatenated
    * as a set of basic services
    * @param p
    *   the relation capturing the basic services provided by R
    * @tparam L
    *   the left type
    * @tparam R
    *   the right type
    * @return
    *   the set of basic services
    */
  implicit def setAndValAreMergeable[L <: Service, R](implicit
      p: Provided[R, Service]
  ): Aux[Set[L], R, Set[Service]] = new Merge[Set[L], R] {
    type Result = Set[Service]
    def apply(l: Set[L], r: R): Set[Service] = l ++ r.services
  }

  implicit def valAndSetAreMergeable[L, R <: Service](implicit
      provided: Provided[L, Service]
  ): Aux[L, Set[R], Set[Service]] = new Merge[L, Set[R]] {
    type Result = Set[Service]
    def apply(l: L, r: Set[R]): Set[Service] = r ++ l.services
  }

  /** Two objects providing services can be concatenated as a set of basic
    * services
    * @param pT
    *   the relation capturing the basic services provided by L
    * @param pU
    *   the relation capturing the basic services provided by R
    * @tparam L
    *   the left type
    * @tparam R
    *   the right type
    * @return
    *   the set of basic services
    */
  implicit def valAndValAreMergeable[L, R](implicit
      pT: Provided[L, Service],
      pU: Provided[R, Service]
  ): Aux[L, R, Set[Service]] = new Merge[L, R] {
    type Result = Set[Service]
    def apply(l: L, r: R): Set[Service] = r.services ++ l.services
  }

  /** A basic and an object providing services can be concatenated as a set of
    * basic services
    * @param pU
    *   the relation capturing the basic services provided by R
    * @tparam L
    *   the left type
    * @tparam R
    *   the right type
    * @return
    *   the set of basic services
    */
  implicit def basicAndValAreMergeable[L <: Service, R](implicit
      pU: Provided[R, Service]
  ): Aux[L, R, Set[Service]] = new Merge[L, R] {
    type Result = Set[Service]
    def apply(l: L, r: R): Set[Service] = r.services + l
  }

  implicit def valAndBasicAreMergeable[L, R <: Service](implicit
      pT: Provided[L, Service]
  ): Aux[L, R, Set[Service]] = new Merge[L, R] {
    type Result = Set[Service]
    def apply(l: L, r: R): Set[Service] = l.services + r
  }

  /** A set of basic services and a basic service can be concatenated as a set
    * of basic services
    * @tparam L
    *   the left type
    * @tparam R
    *   the right type
    * @return
    *   the set of basic services
    */
  implicit def setsAndBasicAreMergeable[L <: Service, R <: Service]
      : Aux[Set[L], R, Set[Service]] = new Merge[Set[L], R] {
    type Result = Set[Service]
    def apply(l: Set[L], r: R): Set[Service] = l ++ Set(r)
  }

  implicit def basicAndSetAreMergeable[L <: Service, R <: Service]
      : Aux[L, Set[R], Set[Service]] = new Merge[L, Set[R]] {
    type Result = Set[Service]
    def apply(l: L, r: Set[R]): Set[Service] = r ++ Set(l)
  }
}
