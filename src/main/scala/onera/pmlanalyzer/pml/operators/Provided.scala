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

import onera.pmlanalyzer.pml.model.hardware.{Composite, Hardware, Initiator, Platform, SimpleTransporter, Target, Transporter, Virtualizer}
import onera.pmlanalyzer.pml.model.relations.ProvideRelation
import onera.pmlanalyzer.pml.model.service.{Load, Service, Store}
import onera.pmlanalyzer.pml.model.software.Data
import onera.pmlanalyzer.pml.model.utils.Owner
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.ProvideRelation
import onera.pmlanalyzer.pml.model.service.{Load, Service, Store}
import onera.pmlanalyzer.pml.model.software.Data
import onera.pmlanalyzer.pml.model.utils.Owner

import scala.reflect.*

/**
  * Base trait for provide operator
  *
  * @tparam L the provider (left) type
  * @tparam R the provided (right) type
  */
trait Provided[L, R] {
  def apply(a: L): Set[R]

  def owner(b: R): Set[L]
}

/**
  * Extension methods and inferences rules
  */
object Provided {

  /** ------------------------------------------------------------------------------------------------------------------
    * EXTENSION METHODS
    * --------------------------------------------------------------------------------------------------------------- */

  /**
    * If an element l of type L can provide element of type R then the operator can be used
    *
    * To access to the load services provided by an element l (e.g. a [[Hardware]])
    * {{{ l.loads }}}
    * To access to the store services provided by an element l
    * {{{  l.stores }}}
    * To access to the services provided by an element l
    * {{{ l.services }}}
    * To access to the initiators provided by an element l (e.g. a [[pml.model.hardware.Platform]])
    * {{{ l.initiators }}}
    * To access to the targets provided by an element l
    * {{{ l.targets }}}
    * To access to the transporters provided by an element l
    * {{{ l.transporters }}}
    * To access to the hardware provided by an element l
    * {{{ l.hardware }}}
    * To access to the initiator providing an element l (e.g. a [[Service]])
    * {{{ l.initiatorOwner }}}
    * To access to the target providing an element l
    * {{{ l.targetOwner }}}
    * To access to the transporter providing an element l
    * {{{ l.transporterOwner }}}
    * To access to the hardware providing an element l
    * {{{ l.hardwareOwner }}}
    * To check if an hardware r is providing an element l
    * {{{ l.hardwareOwnerIs(r)  }}}
 *
    * @note currently provide operators are maily applicable on [[Hardware]] and [[pml.model.software.Data]]
    * @see provide usage can be found in [[views.interference.examples.simpleKeystone.SimpleKeystonePhysicalTableBasedInterferenceSpecification]]
    */
  trait Ops {

    /**
      * Extension methods
      */
    extension [L](self: L) {
      
      def provided[U](using ev: Provided[L, U]): Set[U] = ev(self)

      /**
        * PML keyword to get the loads provided by self
        * @param ev the proof that self provides loads
        * @return the set of provided loads
        */
      def loads(using ev: Provided[L, Load]): Set[Load] = ev(self)

      /**
        * PML keyword to get the stores provided by self
        * @param ev the proof that self provides stores
        * @return the set of provided stores
        */
      def stores(using ev: Provided[L, Store]): Set[Store] = ev(self)

      /**
        * PML keyword to get the services provided by self
        * @param ev the proof that self provides services
        * @return the set of provided services
        */
      def services(using ev: Provided[L, Service]): Set[Service] = ev(self)

    }

    /**
      * Extension methods
      */
    extension [L <: Platform](self:L) {

      def initiators(using ev: Provided[L, Hardware]): Set[Initiator] = ev(self).collect({case x: Initiator => x})

      def targets(using ev: Provided[L, Hardware]): Set[Target] = ev(self).collect({case x: Target => x})

      def hardware(using ev: Provided[L, Hardware]): Set[Hardware] = ev(self)

      def transporters(using ev: Provided[L, Hardware]): Set[Transporter] = ev(self).collect({case x: Transporter => x})

      /**
        * Provide all the physical elements declared inside the composite
        *
        * @return set of declared component
        */
       def directHardware: Set[Hardware] = {
        import self._
        Initiator.allDirect ++ Target.allDirect ++ Virtualizer.allDirect ++ SimpleTransporter.allDirect ++ Composite.allDirect
      }
    }

    /**
      * Extension methods for iterable
      */
    extension [L](self: Iterable[L]) {
      def loads(using ev: Provided[L, Load]): Set[Load] = self.flatMap(ev.apply).toSet

      def stores(using ev: Provided[L, Store]): Set[Store] = self.flatMap(ev.apply).toSet

      def services(using ev: Provided[L, Service]): Set[Service] = self.flatMap(ev.apply).toSet
    }

    /**
      * Extension methods for owner
      */
    extension[R](self: R) {

      def hardwareOwner(using ev: Provided[Hardware, R]): Set[Hardware] = ev.owner(self)

      def hardwareOwnerIs(that: Hardware)(using ev: Provided[Hardware, R]): Boolean = hardwareOwner.contains(that)

      def targetOwner(using ev: Provided[Target, R]): Set[Target] = ev.owner(self)

      def initiatorOwner(using ev: Provided[Initiator, R]): Set[Initiator] = ev.owner(self)

      def transporterOwner(using ev: Provided[Transporter, R]): Set[Transporter] = ev.owner(self)
    }

  }

  /** ------------------------------------------------------------------------------------------------------------------
    * INFERENCE RULES
    * --------------------------------------------------------------------------------------------------------------- */
  /**
    * An implementation of the provide operator between two object is derivable from a relation
    *
    * @return the implementation of the provided
    */
  given[L, R](using p: ProvideRelation[L, R]): Provided[L, R] with {
    def apply(a: L): Set[R] = p(a)

    def owner(b: R): Set[L] = p.inverse(b)
  }

  /**
    * An implementation of the services provided by an hardware component is derivable from the
    * services provided by a physical component
    *
    * @return the implementation of the provided
    */
  given[T <: Hardware : Typeable, S <: Service : Typeable](using ev: Provided[Hardware, Service]): Provided[T, S] with {
    def apply(a: T): Set[S] = ev(a) collect {
      case s : S => s
    }

    def owner(b: S): Set[T] = ev.owner(b) collect {
      case s : T => s
    }
  }

  /**
    * An implementation of the initiators provided by a platform
    *
    * @return the implementation of the provided
    */
  given[L <: Platform: Typeable]: Provided[L, Hardware] with {

    def apply(a: L): Set[Hardware] = {
      import a._
      Initiator.all ++ Target.all ++ Virtualizer.all ++ SimpleTransporter.all
    }

    def owner(b: Hardware): Set[L] = Platform.all.collect {
      case p : L if (Initiator.all(p.currentOwner)
        ++ Target.all(p.currentOwner)
        ++ Virtualizer.all(p.currentOwner)
        ++ SimpleTransporter.all(p.currentOwner) ).contains(b) => p
    }
  }

  /**
    * An implementation of the services provided by platforms
    */
  given[T <: Platform : Typeable] : Provided[T, Service] with {
    def apply(a: T): Set[Service] = a.PLProvideService.targetSet

    def owner(b: Service): Set[T] = Platform.all.collect {
      case p : T if p.PLProvideService.targetSet.contains(b) => p
    }
  }

  /**
    * An implementation of the services provided by a target on which a data is allocated is derivable from the
    * services provided by a target and the name if the platform
    *
    * @return the implementation of the provided
    */
  given[T](using p: Provided[Target, T], u:Used[Data,Target], r:Used[Target, Data], dOwner: Owner): Provided[Data, T] with {
    def apply(a: Data): Set[T] =
      for {
        t <- a.hostingTargets
        b <- p(t)
      } yield b

    def owner(b: T): Set[Data] =
      for {
        t <- b.targetOwner
        d <- t.hostedData
      } yield d
  }
}