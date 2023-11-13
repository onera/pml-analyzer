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

package onera.pmlanalyzer.views.dependability.exporters

import onera.pmlanalyzer.views.dependability.exporters.CeciliaExporter.Aux
import onera.pmlanalyzer.views.dependability.model.{InputDepTarget, System, Variable}

trait SystemCeciliaExporter {
  self: BasicOperationCeciliaExporter & TypeCeciliaExporter =>

  implicit def systemIsExportable[T <: System]: Aux[T, SystemModel] = new CeciliaExporter[T] {
    type R = SystemModel

    def toCecilia(x: T): SystemModel = {
      val targets =  x.context.portOwner.values.collect{case t:InputDepTarget[_] => t}.toSet
      val epsilons = targets.map{t => DeterministicEventModel(Symbol(s"$t.${t.fMAutomaton}.${t.fMAutomaton.epsilon}"))}
      val fires = targets.toList.sortBy(_.id.name).map{t => s"$t.fire"}
      val directions = targets.toList.sortBy(_.id.name).map{t => s"$t.direction"}
      val scheduler = WorstSchedulerTopHelper(epsilons.size)
      val schedulerModel = SubComponent(Symbol("worstScheduler"), scheduler.model)
      val topLevelPort = (v: Variable[_]) => !x.context.componentOwner.isDefinedAt(x.context.portOwner(v.id))
      val topLevelConnections = x.context.links.collect {
        case (i, s) if topLevelPort(i) =>
          if (s.size != 1)
            s.zipWithIndex.map(p => s"${variablePathName(x, i)}${p._2} = ${variablePathName(x, p._1)};")
          else
            Set(s"${variablePathName(x, i)} = ${variablePathName(x, s.head)};")
      }.flatten
      SystemModel(
        x.name,
        PhylogFolder.phylogSystemExampleFolder,
        x.context.toBuild.values.map(f => f()).toList :+ schedulerModel,
        Nil,
        SynchroEventModel(Symbol("epsilon"),epsilons.toList,"mec") :: Nil,
        Nil,
        s"""assert
           |//component connections
           |${topLevelConnections.toList.sorted.mkString("\n")}
           |
           |//target evolution management
           |${fires.zip(scheduler.fireOrders).map(p => s"${p._1} = $schedulerModel.${p._2};").mkString("\n")}
           |${scheduler.sonDirection.zip(directions).map(p => s"$schedulerModel.${p._1} = ${p._2};").mkString("\n")}
           |""".stripMargin
      )
    }
  }
}
