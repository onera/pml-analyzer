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

package onera.pmlanalyzer.pml.model.utils

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.{UserScenarioId, UserTransactionId}
import onera.pmlanalyzer.pml.model.hardware.{Hardware, Initiator, Target}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{PhysicalScenarioId, PhysicalTransaction, PhysicalTransactionId}

import scala.collection.mutable

/**
  * Listing all information, warning or error messages displayed to the user
  */
object Message {
  inline def impossibleTransactionWarning(userName: UserTransactionId): String =
    s"[WARNING] The physical transaction $userName is not possible, check your link and route constraints"

  inline def impossibleRouteWarning(t:Service, from:Option[Application]): String =
    s"""[WARNING] The target service $t cannot be reached ${if (from.isDefined) s"from ${from.getOrElse("unknown application")}" else ""}"""

  inline def multiPathTransactionWarning(userName: UserTransactionId, list: Iterable[(PhysicalTransactionId, PhysicalTransaction)]): String =
    s"""[WARNING] The transaction $userName addresses multiple physical transactions:
       |${list.map(_._1).mkString("\n")}
       |so $userName will be considered as a scenario""".stripMargin

  inline def multiPathRouteWarning(from:Service, to:Service, transactions:Set[PhysicalTransaction]):String = {
    s"""[WARNING] Multiple paths have been detected from $from to $to
       |${transactions.map(_.mkString("<->")).mkString("\n")}""".stripMargin
  }

  inline def transactionNoInLibraryWarning(name: PhysicalTransactionId): String =
    s"[WARNING] The physical transaction $name is not in the library"

  inline def transactionHasSeveralNameWarning(name: PhysicalTransactionId, names: Iterable[UserTransactionId]): String =
    s"[WARNING] The physical transaction $name has ${names.size} distinct names ${names.map(_.id.name).mkString(", ")}"

  inline def impossibleScenarioWarning(userName: UserScenarioId): String =
    s"[WARNING] The physical scenario $userName is not possible, check your link and route constraints"

  inline def multiPathScenarioWarning(userName: UserScenarioId, list: Iterable[(PhysicalTransactionId, PhysicalTransaction)]): String =
    s"""[WARNING] Some transactions in scenario $userName addresses multiple physical transactions:
       |${list.map(_._1).mkString("\n")}
       |all paths will be considered in the scenario, consider routing constraints to avoid multi-path""".stripMargin

  inline def scenarioNotInLibraryWarning(name: PhysicalScenarioId): String =
    s"[WARNING] The physical scenario $name is considered but not defined in the library"

  inline def applicationNotUsingServicesWarning(a:Application): String =
    s"[WARNING] $a is not using any service"

  inline def applicationNotAllocatedWarning(a:Application): String =
    s"[WARNING] $a is not allocated on any initiator"

  inline def noServiceInitiatorWarning(a:Application, s:Initiator): String =
    s"[WARNING] $a is allocated on $s that does not provide any basic service"

  inline def uselessRoutingConstraintWarning(from:Hardware, to:Hardware): String =
    s"[WARNING] Useless routing constraints: $from services are not linked to the ones of $to"

  val cyclicGraphWarning: String = "[WARNING] The paths computed on the graph my be incorrect since the graph is cyclic"

  inline def cycleWarning(visited:Seq[(Any,Any)], ini:Any, tgt:Any) : String =
    s"[WARNING] cycle found on edge ${visited.map(p => s"${p._1} -> ${p._2}").mkString(" , ")} from initiator $ini to reach $tgt"

  inline def successfulExportInfo(name:Any, time:Any): String =
    s"[INFO] $name exported successfully in $time s"

  inline def analysisResultFoundInfo(folder:Any, platform:Any) : String =
    s"[INFO] $folder already contains result files for $platform, computation discarded"

  inline def successfulModelBuildInfo(platform:Any, time:Any) : String =
    s"[INFO] $platform MonoSat model successfully built in $time s"

  inline def startingNonExclusiveScenarioEstimationInfo(platform:Any) : String =
    s"[INFO] Starting  $platform estimation of number of non exclusive scenarios"

  inline def successfulNonExclusiveScenarioEstimationInfo(platform:Any, time:Any) : String =
    s"[INFO] $platform estimation of number of non exclusive scenarios completed in $time s"

  inline def iterationCompletedInfo(i:Any, n:Any, time:Any): String =
    s"[INFO] Iteration $i / $n completed in $time s"

  inline def analysisCompletedInfo(analysis:Any, time:Any): String =
    s"[INFO] $analysis completed in $time s"

  inline def iterationResultsInfo(isFree: Boolean, computed: mutable.Map[Int, Int], over: Map[Int, BigInt]): String =
    s"""[INFO] Interference ${if (isFree) "free " else ""}computed so far
         |${printScenarioNumber(computed, over)}""".stripMargin

  inline def printScenarioNumber(computed: mutable.Map[Int, Int], over: Map[Int, BigInt]): String = {
    s"""${
      computed
        .toSeq
        .sortBy(_._1)
        .map(p => s"[INFO] size ${p._1}: ${p._2} over ${over(p._1)} (${if(over(p._1) == 0) "0" else if (p._2 * 100 / over(p._1) == 0) "< 1" else math.round(p._2 * 100 / over(p._1).toDouble).toInt}%)")
        .mkString("\n")
    }
       |""".stripMargin
  }

  inline def errorReflexivityViolation(x:Any, in:Any): String =
    s"[WARNING] cannot remove edge $x -> $x in relation $in since it should be reflexive"

  inline def errorAntiReflexivityViolation(x: Any, in: Any): String =
    s"[WARNING] cannot add edge $x -> $x in relation $in since it should be anti-reflexive"

  inline def errorAntiSymmetryViolation(l:Any, r:Any, in:Any): String =
    s"[ERROR] cannot add edge $l -> $r in relation $in since edge $r -> $l already exists and $in should be antisymmetric"

  inline def successfulITFDifferenceExportInfo(size:Any, x:Any, y:Any, file:Any): String =
    s"[INFO] The $size-itf differences between $x and $y have been exported to $file"
}
