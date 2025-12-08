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

package onera.pmlanalyzer.pml.exporters

import onera.pmlanalyzer.pml.model.relations.Relation
import onera.pmlanalyzer.*

import java.io.FileWriter

private[pmlanalyzer] object RelationExporter {

  private def componentStatus(platformName: String): String =
    s"${platformName}ComponentStatusTable.txt"

  private def exportRelationFileName(
      platformName: String,
      r: Relation[_, _]
  ): String =
    s"$platformName${r.name}.txt"

  private def getWriter(name: String): FileWriter = {
    val file = FileManager.exportDirectory.getFile(name)
    new FileWriter(file)
  }

  private def usedTransactionTable(platformName: String): String =
    s"${platformName}UsedTransactionTable.txt"

  private def userTransactionTable(platformName: String): String =
    s"${platformName}UserTransactionTable.txt"

  /** If an element x contains relations then these relations can be exported.
    *
    * The routing relation can be exported as a table, the format is provided
    * [[pml.exporters.Ops.exportRouteTable]]
    * {{{
    *   x.exportRouteTable()
    * }}}
    * The allocation of application to initiators can be exported as a table,
    * the format is provided [[pml.exporters.Ops.exportAllocationTable]]
    * {{{
    *   x.exportAllocationTable()
    * }}}
    * The allocation of data to targets can be exported as a table, the format
    * is provided [[pml.exporters.Ops.exportDataAllocationTable]]
    * {{{
    *   x.exportDataAllocationTable()
    * }}}
    * The service targeted by software can be exported as a table, the format is
    * provided [[pml.exporters.Ops.exportSWTargetUsageTable]]
    * {{{
    *   x.exportSWTargetUsageTable()
    * }}}
    * The deactivated components can be exported as a table, the format is
    * provided [[pml.exporters.Ops.exportDeactivatedComponents]]
    * {{{
    *   x.exportDeactivatedComponents()
    * }}}
    */
  trait Ops {

    /** Implicit class used for method extension of platform to provide export
      * features
      */
    extension (self: Platform) {

      /** Export the table providing for each service the select next service(s)
        * w.r.t. the initiator and the target service FORMAT: Initiator,
       * TargetService, Router, NextService(s), SourceCodeFile, SourceCodeLine (current_service_name,
       * (initiator_name, target_service_name, next_service_name, source_codefilename, source_codeline )+
        */
      def exportRouteTable(): Unit = {
        val writer = getWriter(
          exportRelationFileName(self.fullName, self.context.InitiatorRouting)
        )
        writer.write(
          "Initiator, TargetService, Router, NextService(s), SourceCodeFile, SourceCodeLine\n"
        )
        val toWrite = for {
          ((ini, target, router), next) <- self.context.InitiatorRouting.edges
          n <- next
          c <- self.context.InitiatorRouting.getModificationsFor(
            (ini, target, router),
            Some(n)
          )
        } yield {
          s"$ini, $target, $router, $n, ${c.sourceFile}, ${c.lineInFile}\n"
        }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      /** Export the allocation table providing for each software its initiator
        * FORMAT: Software, Initiator(s) (software_name(, initiator_name )+)+
        */
      def exportAllocationTable(): Unit = {
        val writer = getWriter(
          exportRelationFileName(self.fullName, self.context.SWUseInitiator)
        )
        writer.write("Software, Initiator(s), SourceCodeFile, SourceCodeLine\n")
        val toWrite =
          for {
            (sw, initiators) <- self.context.SWUseInitiator.edges
            ini <- initiators
            c <- self.context.SWUseInitiator.getModificationsFor(sw, Some(ini))
          } yield {
            s"$sw, $ini, ${c.sourceFile}, ${c.lineInFile}\n"
          }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      /** Export the allocation table providing for each data its target FORMAT:
        * Data, Target (data_name, target_name)+
        */
      def exportDataAllocationTable(): Unit = {
        import self.*
        val writer = getWriter(
          exportRelationFileName(fullName, context.DataUseTarget)
        )
        writer.write("Data, Target, SourceCodeFile, SourceCodeLine\n")
        val toWrite =
          for {
            d <- Data.all
            t <- d.hostingTargets
            c <- context.DataUseTarget.getModificationsFor(d, Some(t))
          } yield {
            s"$d, $t, ${c.sourceFile}, ${c.lineInFile}\n"
          }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      /** Export the service requested by software FORMAT: Software, Target
        * Service(s) (software_name(, service_name )+)+
        */
      def exportSWTargetUsageTable(): Unit = {
        val writer = getWriter(
          exportRelationFileName(self.fullName, self.context.SWUseService)
        )
        writer.write(
          "Software, Target Service(s), SourceCodeFile, SourceCodeLine\n"
        )
        val toWrite =
          for {
            (sw, services) <- self.context.SWUseService.edges
            s <- services
            c <- self.context.SWUseService.getModificationsFor(sw, Some(s))
          } yield {
            s"$sw, $s, ${c.sourceFile}, ${c.lineInFile}\n"
          }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      /** Export the utilisation status of the components. Three possibilities,
        * either:
        *   - not used and deactivated,
        *   - used and activated,
        *   - no used and activated (consider modifying the PML model in such
        *     case) FORMAT: Component, Activated, Used (component_name,
        *     (Yes|No), (Yes|No))+
        */
      def exportDeactivatedComponents(): Unit = {
        import self.*
        val writer = getWriter(componentStatus(fullName))
        writer.write("Component, Activated, Used\n")
        val restricted = self.hardwareGraph()
        val hwLinks = restricted flatMap { p => p._2 map { x => (p._1, x) } }
        val used = hwLinks.flatMap { p => Set(p._1, p._2) }.toSet
        for (c <- self.directHardware.toSeq.sortBy(_.name.name)) {
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

    /** Extension export methods for configured platform
      */
    extension (self: Platform with TransactionLibrary) {

      /** Export the transactions used by a platform FORMAT: Transaction Name,
        * Transaction Path (transaction_name, service_name(.service_name)*)+
        */
      def exportUserTransactionPaths(): Unit = {
        import self.*
        val writer = getWriter(userTransactionTable(fullName))
        writer.write(
          "Transaction Name, Transaction Path, SourceCodeFile, SourceCodeLine\n"
        )
        val toWrite = for {
          sc <- Transaction.all
          s <- transactionByUserName.get(sc.userName)
          t = s
            .map(atomicTransactionsByName)
            .map(x =>
              if (s.size <= 1)
                x.mkString("::")
              else
                x.mkString("(", "::", ")")
            )
            .toSeq
            .sorted
        } yield s"${sc.userName}, ${t.mkString("+")}, ${sc.sourceFile}, ${sc.lineInFile}\n"
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }
    }
  }
}
