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

package onera.pmlanalyzer.views.interference.operators

import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.hardware.Hardware
import onera.pmlanalyzer.views.interference.model.relations.{
  InterfereRelation,
  NotInterfereRelation
}
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId
import sourcecode.{File, Line}

private[operators] trait Interfere[L, R] {
  def interfereWith(l: L, r: R)(using line: Line, file: File): Unit

  def notInterfereWith(l: L, r: R)(using line: Line, file: File): Unit
}

object Interfere {

  /** If an element x of type L and an element y of type R can interfere then
    * the operator can be used as follows:
    *
    * x interferes with y {{{x interfereWith y}}} x does not interfere with y
    * {{{x notInterfereWith y}}}
    */
  trait Ops {

    extension [L](self: L) {
      def interfereWith[R](
          that: R
      )(using ev: Interfere[L, R], line: Line, file: File): Unit =
        ev.interfereWith(self, that)
      def interfereWith[R](
          that: Iterable[R]
      )(using ev: Interfere[L, R], line: Line, file: File): Unit =
        for { x <- that } ev.interfereWith(self, x)
      def notInterfereWith[R](
          that: R
      )(using ev: Interfere[L, R], line: Line, file: File): Unit =
        ev.notInterfereWith(self, that)
      def notInterfereWith[R](that: Iterable[R])(using
          ev: Interfere[L, R],
          line: Line,
          file: File
      ): Unit = for { x <- that } ev.notInterfereWith(self, x)
    }

    /** If an element h of type Hardware has interfering services then the
      * operator can be used as follows:
      *
      * h hasInterferingServices {{{h hasInterferingServices}}} h does not have
      * any interfering services {{{h hasNonInterferingServices}}}
      */
    extension (self: Hardware) {
      def hasInterferingServices(using
          prv: Provided[Hardware, Service],
          ev: Interfere[Service, Service],
          line: Line,
          file: File
      ): Unit =
        for { s1 <- self.services; s2 <- self.services }
          ev.interfereWith(s1, s2)

      def hasNonInterferingServices(using
          prv: Provided[Hardware, Service],
          ev: Interfere[Service, Service],
          line: Line,
          file: File
      ): Unit =
        for { s1 <- self.services; s2 <- self.services }
          ev.notInterfereWith(s1, s2)
    }
  }

  given [L, R](using
      a: InterfereRelation[L, R],
      n: NotInterfereRelation[L, R]
  ): Interfere[L, R] with {
    def interfereWith(l: L, r: R)(using line: Line, file: File): Unit =
      a.add(l, r)

    def notInterfereWith(l: L, r: R)(using line: Line, file: File): Unit =
      n.add(l, r)
  }

  given [LS <: Service, RS <: Service](using
      ev: Interfere[Service, Service]
  ): Interfere[LS, RS] with {
    def interfereWith(l: LS, r: RS)(using line: Line, file: File): Unit =
      ev.interfereWith(l, r)

    def notInterfereWith(l: LS, r: RS)(using line: Line, file: File): Unit =
      ev.notInterfereWith(l, r)
  }

  given [R <: Service](using
      ev: Interfere[PhysicalTransactionId, Service]
  ): Interfere[PhysicalTransactionId, R] with {
    def interfereWith(l: PhysicalTransactionId, r: R)(using
        line: Line,
        file: File
    ): Unit =
      ev.interfereWith(l, r)

    def notInterfereWith(l: PhysicalTransactionId, r: R)(using
        line: Line,
        file: File
    ): Unit =
      ev.notInterfereWith(l, r)
  }

  given [L, RS <: Service](using
      transformation: Transform[L, Set[PhysicalTransactionId]],
      ev: Interfere[PhysicalTransactionId, Service]
  ): Interfere[L, RS] with {
    def interfereWith(l: L, r: RS)(using line: Line, file: File): Unit =
      for { id <- transformation(l) }
        ev.interfereWith(id, r)

    def notInterfereWith(l: L, r: RS)(using line: Line, file: File): Unit =
      for { id <- transformation(l) }
        ev.notInterfereWith(id, r)
  }

  given [L, RH <: Hardware](using
      transformation: Transform[L, Set[PhysicalTransactionId]],
      ev: Interfere[PhysicalTransactionId, Service],
      p: Provided[RH, Service]
  ): Interfere[L, RH] with {
    def interfereWith(l: L, r: RH)(using line: Line, file: File): Unit =
      for {
        id <- transformation(l)
        s <- r.services
      }
        ev.interfereWith(id, s)

    def notInterfereWith(l: L, r: RH)(using line: Line, file: File): Unit =
      for {
        id <- transformation(l)
        s <- r.services
      }
        ev.notInterfereWith(id, s)
  }

  given [LP, RS <: Service](using
      transformation: Transform[LP, Option[PhysicalTransactionId]],
      ev: Interfere[PhysicalTransactionId, Service]
  ): Interfere[LP, RS] with {
    def interfereWith(l: LP, r: RS)(using line: Line, file: File): Unit =
      for { id <- transformation(l) }
        ev.interfereWith(id, r)

    def notInterfereWith(l: LP, r: RS)(using line: Line, file: File): Unit =
      for { id <- transformation(l) }
        ev.notInterfereWith(id, r)
  }

  given [LH <: Hardware, RH <: Hardware](using
      ev: Interfere[Hardware, Hardware]
  ): Interfere[LH, RH] with {
    def interfereWith(lh: LH, rh: RH)(using line: Line, file: File): Unit =
      ev.interfereWith(lh, rh)

    def notInterfereWith(lh: LH, rh: RH)(using line: Line, file: File): Unit =
      ev.notInterfereWith(lh, rh)
  }

}
