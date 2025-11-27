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

import onera.pmlanalyzer.views.dependability.exporters.CeciliaExporter.Aux
import onera.pmlanalyzer.views.dependability.exporters.GenericImage.*
import onera.pmlanalyzer.views.dependability.exporters.PhylogFolder.phylogInitiatorFolder
import onera.pmlanalyzer.views.dependability.model.{
  Application,
  Descriptor,
  TargetId
}
import onera.pmlanalyzer.views.dependability.operators.*

trait SoftwareCeciliaExporter {
  self: TypeCeciliaExporter
    with BasicOperationCeciliaExporter
    with ExprCeciliaExporter =>

  implicit def dependencySpecifiedSoftwareIsExportable[
      FM: IsFinite: IsCriticityOrdering
  ]: Aux[Application[FM], EquipmentModel] =
    new CeciliaExporter[Application[FM]] {
      type R = EquipmentModel

      def toCecilia(x: Application[FM]): EquipmentModel = {
        val tyype = typeModel[FM]
        val rTyype = typeModel[TargetId, FM]
        val coreState = Flow(x.coreState.id.name, tyype, In)
        val requestO = Flow(x.storeO.id.name, rTyype, Out)
        val stateO = Flow(x.stateO.id.name, tyype, Out)
        val accessI = Flow(x.loadI.id.name, rTyype, In)
        val stateExpr =
          ExprCeciliaExporter.toCecilia(x.softwareState(x.coreState, x.loadI))
        val storeExpr =
          ExprCeciliaExporter.toCecilia(x.stores(x.coreState, x.loadI))
        val subComponents =
          stateExpr.subComponentAssertions.keySet ++ storeExpr.subComponentAssertions.keySet
        val subComponentAssertions =
          (stateExpr.subComponentAssertions.values ++ storeExpr.subComponentAssertions.values).flatten.toList.sorted
        EquipmentModel(
          x.id.name,
          phylogInitiatorFolder,
          phylogBlockBlue,
          phylogBlockGreen :: phylogBlockRed :: Nil,
          coreState :: accessI :: requestO :: stateO :: Nil,
          subComponents.toList,
          Nil,
          Nil,
          s"""assert
           |// subcomponent assertions
           |${subComponentAssertions.sorted.mkString("\n")}
           |
           |// provide store status according to dependencies
           |${allOf[TargetId]
              .map(k =>
                s"$requestO^$k = ${(for (s <- storeExpr.result.get(k)) yield s) getOrElse noneOf[FM].name.name};"
              )
              .sorted
              .mkString("\n")}
           |
           |// state of the software
           |$stateO = ${stateExpr.result.values.head};
           |
           |// icon management
           |icone = (if $stateO = ${min[FM].name.name} then 1 else 2);
           |""".stripMargin
        )
      }
    }

  implicit def descriptorIsExportable[FM: IsFinite: IsCriticityOrdering]
      : Aux[Descriptor[FM], EquipmentModel] =
    new CeciliaExporter[Descriptor[FM]] {
      type R = EquipmentModel
      def toCecilia(x: Descriptor[FM]): EquipmentModel = {
        val requestType = typeModel[TargetId, FM]
        val loadI = Flow(x.loadI.id.name, requestType, In)
        val storeO = Flow(x.storeO.id.name, requestType, Out)
        val storeDependency = x.transferts
          .flatMap(c => c.targetWritten.map(store => store -> c.targetNeeded))
          .filter(_._2.nonEmpty)
          .toMap
        val worst = storeDependency.transform((_, v) =>
          mkWorstSub(v.map(t => s"$loadI^$t"))
        )
        val storeAssertions = allOf[TargetId].map(tId =>
          worst.get(tId) match {
            case None              => s"$storeO^$tId = ${noneOf[FM].name.name};"
            case Some((out, _, _)) => s"$storeO^$tId = $out;"
          }
        )
        EquipmentModel(
          x.id.name,
          phylogInitiatorFolder,
          phylogBlockBlue,
          phylogBlockGreen :: phylogBlockRed :: Nil,
          loadI :: storeO :: Nil,
          worst.values.map(_._3).toList,
          Nil,
          Nil,
          s"""assert
           |// worst value computation
           |${worst.values.flatMap(_._2).toList.distinct.sorted.mkString("\n")}
           |
           |// impact computation
           |${storeAssertions.toList.distinct.sorted.mkString("\n")}
           |
           |// icon management
           |icone = 1;
           |""".stripMargin
        )
      }
    }
}
