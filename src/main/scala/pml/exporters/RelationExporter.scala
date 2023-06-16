/*******************************************************************************
 * Copyright (c) 2021. ONERA
 * This file is part of PML Analyzer
 *
 * PML Analyzer is free software ;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation ;
 * either version 2 of  the License, or (at your option) any later version.
 *
 * PML  Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program ;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package pml.exporters

import pml.model.configuration.TransactionLibrary
import pml.model.hardware.Platform
import pml.model.software._
import pml.operators._

import java.io.FileWriter

object RelationExporter {

  /**
    * If an element x contains relations then these relations can be exported.
    *
    * The routing relation can be exported as a table, the format is provided [[pml.exporters.Ops.exportRouteTable]]
    * {{{
    *   x.exportRouteTable()
    * }}}
    * The allocation of application to initiators can be exported as a table, the format is provided [[pml.exporters.Ops.exportAllocationTable]]
    * {{{
    *   x.exportAllocationTable()
    * }}}
    * The allocation of data to targets can be exported as a table, the format is provided [[pml.exporters.Ops.exportDataAllocationTable]]
    * {{{
    *   x.exportDataAllocationTable()
    * }}}
    * The service targeted by software can be exported as a table, the format is provided [[pml.exporters.Ops.exportSWTargetUsageTable]]
    * {{{
    *   x.exportSWTargetUsageTable()
    * }}}
    * The deactivated components can be exported as a table, the format is provided [[pml.exporters.Ops.exportDeactivatedComponents]]
    * {{{
    *   x.exportDeactivatedComponents()
    * }}}
    */
  trait Ops {

    /**
      * Implicit class used for method extension of platform to provide
      * export features
      *
      * @param platform the platform providing the export features
      */
    implicit class Ops(platform: Platform) {
      private val routingExportName: String = platform.fullName + "RouteTable.txt"
      private val swAllocationExportName: String = platform.fullName + "AllocationTable.txt"
      private val dataAllocationExportName: String = platform.fullName + "DataTable.txt"
      private val swTargetUsage: String = platform.fullName + "TargetedServiceTable.txt"
      private val componentStatus: String = platform.fullName + "ComponentStatusTable.txt"

      private def getWriter(name: String): FileWriter = {
        val file = FileManager.exportDirectory.getFile(name)
        new FileWriter(file)
      }

      /**
        * Export the table providing for each service the select next service(s) w.r.t. the initiator
        * and the target service
        * FORMAT:
        * Initiator, TargetService, Router, NextService(s)
        * (current_service_name, initiator_name, target_service_name(, next_service_name )+)+
        */
      def exportRouteTable(): Unit = {
        val writer = getWriter(routingExportName)
        writer.write(s"Initiator, TargetService, Router, NextService(s)\n")
        platform.InitiatorRouting._values
          .map(p => s"${p._1._1}, ${p._1._2}, ${p._1._3}, ${p._2.toSeq.sortBy(_.name.name).mkString(", ")}\n")
          .toSeq
          .sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      /**
        * Export the allocation table providing for each software its initiator
        * FORMAT:
        * Software, Initiator(s)
        * (software_name(, initiator_name )+)+
        */
      def exportAllocationTable(): Unit = {
        val writer = getWriter(swAllocationExportName)
        writer.write(s"Software, Initiator(s)\n")
        platform.SWUseInitiator._values
          .map(p => s"${p._1}, ${p._2.toSeq.sortBy(_.name.name).mkString(", ")}\n")
          .toSeq
          .sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      /**
        * Export the allocation table providing for each data its target
        * FORMAT:
        * Data, Target
        * (data_name, target_name)+
        */
      def exportDataAllocationTable(): Unit = {
        val writer = getWriter(dataAllocationExportName)
        writer.write(s"Data, Target\n")
        import platform._
        for (d <- Data.all.toSeq.sortBy(_.name.name))
          writer.write(s"$d, ${d.hostingTargets.mkString(",")}\n")
        writer.flush()
        writer.close()
      }

      /**
        * Export the service requested by software
        * FORMAT:
        * Software, Target Service(s)
        * (software_name(, service_name )+)+
        */
      def exportSWTargetUsageTable(): Unit = {
        val writer = getWriter(swTargetUsage)
        writer.write(s"Software, Target Service(s)\n")
        platform.SWUseService._values
          .map(p => s"${p._1}, ${p._2.toSeq.sortBy(_.name.name).mkString(", ")}\n")
          .toSeq
          .sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      /**
        * Export the utilisation status of the components.
        * Three possibilities, either:
        * - not used and deactivated,
        * - used and activated,
        * - no used and activated (consider modifying the PML model in such case)
        * FORMAT:
        * Component, Activated, Used
        * (component_name, (Yes|No), (Yes|No))+
        */
      def exportDeactivatedComponents(): Unit = {
        val writer = getWriter(componentStatus)
        writer.write(s"Component, Activated, Used\n")
        import platform._
        val restricted = platform.hardwareGraph()
        val hwLinks = restricted flatMap { p => p._2 map { x => (p._1, x) } }
        val used = hwLinks.flatMap { p => Set(p._1, p._2) }.toSet
        for (c <- platform.directHardware.toSeq.sortBy(_.name.name)) {
          if (c.services.isEmpty)
            writer.write(s"$c, No, No\n")
          else if (used.contains(c))
            writer.write(s"$c, Yes, Yes\n")
          else
            writer.write(s"$c, Yes, No\n")
        }
        writer.flush()
        writer.close()
      }
    }

    /**
      * Extension export methods for configured platform
      *
      * @param platform the configured platforms
      */
    implicit class OpsConfig(platform: Platform) {

      private def getWriter(name: String): FileWriter = {
        val file = FileManager.exportDirectory.getFile(name)
        new FileWriter(file)
      }

      private val transactionTable: String = platform.fullName + "UsedTransactionTable.txt"

      /**
        * Export the transactions used by a platform
        * FORMAT:
        * Transaction Name, Transaction Path
        * (transaction_name, service_name(.service_name)*)+
        */
      def exportPhysicalTransactions(): Unit = {
        val writer = getWriter(transactionTable)
        writer.write(s"Transaction Name, Transaction Path\n")
        import platform._
        for {
          (n,t) <- transactionsByName.toSeq.sortBy(_.toString())
        }
          writer.write(s"$n, ${t.mkString("\u2219")}\n")
        writer.flush()
        writer.close()
      }
    }

    /**
      * Extension export methods for configured platform
      *
      * @param platform the configured platform with a library
      */
    implicit class OpsLibrary(platform: Platform with TransactionLibrary) {

      private def getWriter(name: String): FileWriter = {
        val file = FileManager.exportDirectory.getFile(name)
        new FileWriter(file)
      }

      private val transactionTable: String = platform.fullName + "UserTransactionTable.txt"
      private val scenarioTable: String = platform.fullName + "UserScenarioTable.txt"

      /**
        * Export the transactions used by a platform
        * FORMAT:
        * Transaction Name, Transaction Path
        * (transaction_name, service_name(.service_name)*)+
        */
      def exportUserTransactions(): Unit = {
        val writer = getWriter(transactionTable)
        writer.write(s"Transaction Name, Transaction Path\n")
        import platform._
        for {
          (n,t) <- transactionByUserName.toSeq.sortBy(_.toString())
        } yield
          writer.write(s"$n, ${transactionsByName(t).mkString("\u2219")}\n")
        writer.flush()
        writer.close()
      }

      /**
        * Export the scenarios used by a platform
        * FORMAT:
        * Scenario Name, Scenario Path
        * (scenario_name, service_name(.service_name)*)+
        */
      def exportUserScenarios(): Unit = {
        val writer = getWriter(scenarioTable)
        writer.write(s"Scenario Name, Scenario Path\n")
        import platform._
        for {
          (n,s) <- scenarioByUserName.toSeq.sortBy(_._1.toString)
          t <- s.map(transactionsByName).toSeq.sortBy(_.toString())
        }
          writer.write(s"$n, ${t.mkString("\u2219")}\n")
        writer.flush()
        writer.close()
      }
    }
  }
}
