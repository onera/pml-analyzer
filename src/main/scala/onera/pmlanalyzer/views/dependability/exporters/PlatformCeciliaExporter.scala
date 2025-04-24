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

package onera.pmlanalyzer.views.dependability.exporters

import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.pml.model.hardware.{
  Hardware,
  Platform,
  Virtualizer,
  Initiator as PMLInitiator,
  SimpleTransporter as PMLSimpleTransporter,
  Target as PMLTarget,
  Transporter as PMLTransporter
}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.Application as PMLApplication
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.dependability.exporters.CeciliaExporter.Aux
import onera.pmlanalyzer.views.dependability.model.CustomTypes.TargetStatus
import onera.pmlanalyzer.views.dependability.model.{
  Expr,
  InitiatorId,
  InputDepTarget,
  InputInDepTarget,
  Software,
  SoftwareId,
  TargetId,
  TransporterId,
  Variable,
  Application as DepApplication,
  BasicTransporter as DepBasicTransporter,
  Initiator as DepInitiator,
  SimpleTransporter as DepSimpleTransporter,
  System as DepSystem,
  Virtualizer as DepVirtualizer
}
import onera.pmlanalyzer.views.dependability.operators.{
  IsCriticityOrdering,
  IsFinite,
  IsShadowOrdering
}

import scala.reflect._
import scala.xml.XML

trait PlatformCeciliaExporter {
  self: BasicOperationCeciliaExporter
    with SystemCeciliaExporter
    with TypeCeciliaExporter =>

  implicit class PlatformExportOps[
      FM: IsCriticityOrdering: IsFinite: IsShadowOrdering,
      T <: Platform with DependabilitySpecification.Aux[FM]: Typeable
  ](a: T) {
    def exportAsCeciliaWithFM(): Unit = {
      a.exportAsCecilia(platformIsExportable[FM, T])
    }
  }

  trait DependabilitySpecification {
    self: Platform =>

    type U

    implicit val toTargetId: PMLTarget => TargetId = mkTargetId

    val depSpecificationName: Symbol

    val failureConditions: Set[(PMLApplication, U, Int)]

    def mkTargetId(t: PMLTarget): TargetId

    def softwareStoresDependency(
        p: PMLApplication
    ): (Variable[U], Variable[TargetStatus[U]]) => Expr[TargetStatus[U]]

    def softwareState(
        p: PMLApplication
    ): (Variable[U], Variable[TargetStatus[U]]) => Expr[U]

    val targetIsInputDep: Set[PMLTarget]
  }

  object DependabilitySpecification {
    type Aux[T] = DependabilitySpecification {
      type U = T
    }
  }

  private def platformIsExportable[
      FM: IsCriticityOrdering: IsFinite: IsShadowOrdering,
      T <: Platform with DependabilitySpecification.Aux[FM]: Typeable
  ]: Aux[T, SystemModel] = new CeciliaExporter[T] {
    type R = SystemModel

    def toCecilia(x: T): SystemModel = {
      import x._
      object TempSystem extends DepSystem(x.name) {

        def getTargetId(b: Service): Set[TargetId] = {
          b.hardwareOwner.collect { case t: PMLTarget => mkTargetId(t) }
        }

        def isReachable(
            from: Service,
            to: Service,
            in: Map[Service, Set[Service]]
        ): Boolean =
          from == to || in.contains(from) && in(from).exists(succ =>
            isReachable(succ, to, in)
          )

        def getRouted(on: Service): (InitiatorId, TargetId) => Boolean =
          (i, t) => {
            val ini = x.initiators.filter { _.name == i.name }
            val tgt = x.targets.filter { _.name == t.name }
            ini.exists(pmlI =>
              x.applications
                .filter(_.hostingInitiators.contains(pmlI))
                .exists(pmlS =>
                  tgt.exists(pmlT =>
                    pmlT.services.exists(tb => {
                      val r = x.serviceGraphOf(pmlS, tb)
                      (r.keySet ++ r.values.flatten).contains(on)
                      //            InitiatorRouting.get((pmlI, tb, on)) match {
                      //              case Some(next) =>
                      //                val r = x.restrictServiceTo(pmlS, tb)
                      //                next.exists(b => isReachable(b, tb, r)) // it exists a next service from which one of the service of t is reachable
                      //              case None => true //not always true, the transaction ini -> on -> tgt must exists
                      //            }
                    })
                  )
                )
            )
          }

        val getAuthorized: (InitiatorId, TargetId) => Boolean = (i, t) => {
          val ini = x.initiators.filter { _.name == i.name }
          val s =
            x.applications.filter(_.hostingInitiators.intersect(ini).nonEmpty)
          val tgt = x.context.PLProvideService.domain.collect {
            case t2: PMLTarget if t.name == t2.name => t2
          }
          s.exists(pmlS =>
            tgt.exists(t =>
              t.services.exists(b => {
                x.context.SWAuthorizeService(pmlS).contains(b)
              })
            )
          )
        }

        // extract hw connection graph only contains physical connection used by at least one transaction (OR NOT ...)
        val hwGraph: Map[Hardware, Set[Hardware]] = x.applications flatMap {
          x.hardwareGraphOf
        } groupBy (_._1) transform ((_, v) => v.flatMap(_._2)) filter {
          _._2.nonEmpty
        }
        private val hwLinks = hwGraph.keySet.flatMap({ k =>
          hwGraph(k) map { x => (k, x) }
        })
        private val hwComponents = hwLinks.flatMap({ p => Set(p._1, p._2) })

        // Transform targets
        private val targets = hwComponents.collect {
          case t: PMLTarget if targetIsInputDep.contains(t) =>
            t -> InputDepTarget[FM](mkTargetId(t))
          case t: PMLTarget => t -> InputInDepTarget[FM](mkTargetId(t))
        }.toMap
        // Transform software to their dependability counterpart where dependencies are load targets and impacts are stores
        val software: Map[PMLApplication, Software[FM]] =
          x.applications.collect { case a: PMLApplication =>
            val stores = softwareStoresDependency(a)
            val state = softwareState(a)
            a -> DepApplication[FM](SoftwareId(a.name), state, stores)
          }.toMap

        // Transform simple transporters, virtualizers and smart to classic transporters and consider authorize and routing
        // as rejection
        val transporters: Map[PMLTransporter, DepBasicTransporter[FM]] =
          hwComponents.collect {
            case t: PMLSimpleTransporter =>
              // add routing table to rejection
              val reject = (p: (InitiatorId, TargetId)) =>
                t.services.exists(b => !getRouted(b)(p._1, p._2))
              t -> DepSimpleTransporter(TransporterId(t.name), reject)
            case v: Virtualizer =>
              // add routing table and authorize to rejection
              val reject = (p: (InitiatorId, TargetId)) =>
                v.services.exists(b =>
                  !(getRouted(b)(p._1, p._2) && getAuthorized(p._1, p._2))
                )
              v -> DepVirtualizer(TransporterId(v.name), reject)
          }.toMap

        val initiators: Map[PMLInitiator, DepInitiator[FM]] =
          hwComponents.collect { case i: PMLInitiator =>
            i -> DepInitiator(InitiatorId(i.name))
          }.toMap

        // Compute inverse relation
        private val use = hwComponents
          .map(in =>
            in -> hwLinks.collect {
              case (b, a) if a == in => b
            }
          )
          .filter(_._2.nonEmpty)
          .toMap

        // connect
        hwGraph.foreach {
          case (in: PMLInitiator, outs) =>
            initiators(in).loadI := outs.collect {
              case t: PMLTransporter =>
                transporters(t).loadO
              case t: PMLTarget =>
                targets(t).loadO
            }.toList
          case (in: PMLTransporter, outs) =>
            transporters(in).loadI := outs.collect {
              case t: PMLTransporter =>
                transporters(t).loadO
              case t: PMLTarget =>
                targets(t).loadO
            }.toList
          case _ => println("should not be reachable...")
        }
        use.foreach {
          case (in: PMLTarget, outs) =>
            targets(in).storeI := outs
              .collect({
                case i: PMLInitiator   => initiators(i).storeO
                case t: PMLTransporter => transporters(t).storeO
              })
              .toList
          case (in: PMLTransporter, outs) =>
            transporters(in).storeI := outs.collect {
              case t: PMLTransporter => transporters(t).storeO
              case t: PMLInitiator   => initiators(t).storeO
            }.toList
          case _ => println("should not be reachable...")
        }
        x.context.SWUseInitiator._inverse.foreach { p =>
          {
            val t = initiators(p._1)
            val sw: List[Software[FM]] = p._2.map(software).toList
            t.storeI := sw.map(_.storeO)
            sw.foreach {
              case a: DepApplication[FM] =>
                a.coreState := t.fMAutomaton.o
                a.loadI := t.loadO
              case d =>
                d.loadI := t.loadO
            }
          }
        }
      }
      val fSets = failureConditions.groupMap(p => p._3)(p =>
        (
          variablePathName(
            TempSystem,
            TempSystem.software(p._1).asInstanceOf[DepApplication[FM]].stateO
          ),
          p._2.toString
        )
      )
      fSets
        .transform((k, v) => FailureConditions(v, k))
        .foreach(p => {
          val file = FileManager.exportDirectory
            .getFile(CeciliaExporter.ceciliaExportName(x) + s"FC${p._1}.xml")
          XML.save(file.getAbsolutePath, p._2.toElem)
        })
      TempSystem.toCecilia
    }
  }
}
