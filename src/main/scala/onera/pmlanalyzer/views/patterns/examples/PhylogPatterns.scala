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

package onera.pmlanalyzer.views.patterns.examples

import onera.pmlanalyzer.views.patterns.exporters.LatexCodePrinter._
import onera.pmlanalyzer.views.patterns.exporters.LatexDiagramPrinter._
import onera.pmlanalyzer.views.patterns.model.DSLImplicits._

import scala.sys.process.Process

object PhylogPatterns extends App {

  val patternWidth = 930

  val alterationIdentified =
    conclusion("All critical and innocuous configuration settings alterations $\\mathcal{A}lt$ identified (no case missing)").
      strategy("Safety Analysis" backing "Architecture mastery").
      evidence("Completeness: former alterations data-base").
      `given`("Safety objectives $\\mathcal{R}eq$ identified (ARP4754)").
      `given`("Configuration settings $\\mathcal{C}s$").
      `given`("Application mapping")

  val alterationMitigated =
    conclusion ("Design of adequate mitigation means against inadvertent changes to Configuration Settings" size 8).
      strategy("Check all identified settings alterations are mitigated ($\\forall a_i \\in \\mathcal{A}lt$ $a_i$ mitigated)").
      evidence(alterationIdentified).
      evidence("$a_i$ is mitigated")

  val RU2 =
    conclusion (
      "Designed, implemented and verified adequate mitigation means against " +
      "inadvertent critical configuration setting alterations"
        short "RU2"
        size 8
        width patternWidth).
      strategy ("DO178: Implementation compliant with specification \\\\ DO254: Hardware usage compliant with specification" size 11 ).
      evidence (alterationMitigated).`given` ("Mitigation means implementation verified \\\\ (DO178B/C compliance)").
      `given`("Hardware configuration verified \\\\ (DO254 compliance)")


  RU2.printDiagram()
  RU2.printCode()

  val platformAnalysis =
    conclusion("Safety objectives $\\mathcal{R}eq$ are fulfilled").
      strategy("Safety Analysis at platform level").
      evidence("Real-time objectives are fulfilled").
      `given`("Safety objectives $\\mathcal{R}eq$,\\\\ configuration settings \\\\and application mapping").
      `given`("Mitigation means of RU2, RU3 and (E7)").
      `given`("Critical alteration from RU2")

  val equipmentAnalysis =  conclusion("Errors contained within equipment" short "equipmentAnalysis" size 8).
    strategy("Safety analysis at equipment level").
    evidence(platformAnalysis).
    evidence("Design of adequate safety net")

  val failure_mode_identified =
    conclusion("All failures and all theirs safety effects are identified").
      strategy("Safety Analysis " backing "Architecture mastery").
      evidence("Completeness: former accident data-base (e.g errata)").
      `given`("Safety objectives $\\mathcal{R}eq$ identified (ARP4754)").
      `given`("Configuration settings $\\mathcal{C}s$").
      `given`("Application mapping")

  val failure_mode_mitigated =
    conclusion("Design of adequate mitigation means").
      strategy("Traceability analysis: \\\\ Check all identified failure modes are mitigated" size 6).
      evidence(failure_mode_identified).
      evidence("$fm_i$ is mitigated")

  val implementationCompliant = conclusion("Designed, implemented and verified adequate mitigation means for identified failure modes" short "implementationCompliant" size 8).
      strategy("DO178: Implementation compliant with specification \\\\ DO254: Hardware usage compliant with specification"  size 8).
    evidence(failure_mode_mitigated).
    `given`("Mitigation means implementation verified \\\\ (DO178B/C compliance)").
    `given`("Hardware configuration verified \\\\ (DO254 compliance)")


  val EH =
    conclusion("Mitigation means commensurate with the safety objectives" short "EH" width patternWidth size 8).
      strategy("Check matching between equipment safety analysis and mitigation means implementation").
      evidence(implementationCompliant).
      evidence(equipmentAnalysis)

  EH.printDiagram()
  EH.printCode()


  //TODO short must be derived from variable name and purify from LaTeX forbidden characters (_,$)
  val EHSplit = conclusion("Mitigation means commensurate with the safety objectives" short "EHSplit" width patternWidth size 8).
    strategy("Check matching between equipment safety analysis and mitigation means implementation").
    evidenceRef(implementationCompliant).
    evidenceRef(equipmentAnalysis)

  EHSplit.printDiagram()
  EHSplit.printCode()


  val interference_identification =
    conclusion("Identification of all interference $\\mathcal{I}$").
      strategy("Interference calculus").
      `given`("Configuration settings $\\mathcal{C}s$").
      `given`("Application mapping")

  val interference_identified_classified =
    conclusion("Classification of interference effects $( \\forall i \\in \\mathcal{I}, c(i))$").
      strategy("Safety analysis" backing "Architecture mastery").
      evidence(interference_identification).
      evidence("Identification of $i$ effects").
      `given`("Configuration settings $\\mathcal{C}s$ and application mapping and temporal constraints on applications (e.g. WCET)")

  val identified_mitigated =
    conclusion("Design of adequate means of mitigation for interference").
    strategy("Check all identified interference are mitigated ($\\forall i \\in \\mathcal{I}$, $i$ mitigated)").
    evidence(interference_identified_classified).
    evidence("$i$ mitigated\\\\(e.g. prevention / blocking with run-time mechanism; impossible due to usage domain restriction; tolerance)")

  val RU3 =
    conclusion("Identification of interference and verified means of mitigation" short "RU3" width patternWidth).
      strategy("DO178: Implementation compliant with specification \\\\ DO254: Hardware usage compliant with specification"  size 11 ).
      evidence(identified_mitigated).
      `given`("Mitigation means implementation verified \\\\ (DO178B/C compliance)").
      `given`("Hardware configuration verified \\\\ (DO254 compliance)")


  RU3.printDiagram()
  RU3.printCode()

  val interference_identified_classified_short =
    conclusion("Classification of interference effects $( \\forall i \\in \\mathcal{I}, c(i))$").
      strategy("Safety analysis" backing "Architecture mastery").
      evidence("Identification of all interference $\\mathcal{I}$ \\\\ Given: Configuration settings $\\mathcal{C}s$").
      evidence("Identification of $i$ effects").
      `given`("Configuration settings $\\mathcal{C}s$ and application mapping and temporal constraints on applications (e.g. WCET)")

  val identified_mitigated_short =
    conclusion("Design of adequate means of mitigation for interference").
      strategy("Check all identified interference are mitigated ($\\forall i \\in \\mathcal{I}$, $i$ mitigated)").
      evidence(interference_identified_classified_short).
      evidence("$i$ mitigated\\\\(e.g. prevention / blocking with run-time mechanism; impossible due to usage domain restriction; tolerance")

  val RU3_short =
    conclusion("Identification of interference and verified means of mitigation" short "RU3Short" width patternWidth).
      strategy("DO178: Implementation compliant with specification \\\\ DO254: Hardware usage compliant with specification"  size 11 ).
      evidence(identified_mitigated_short).
      `given`("Mitigation means implementation verified \\\\ (DO178B/C compliance)").
      `given`("Hardware configuration verified \\\\ (DO254 compliance)")


  RU3_short.printDiagram()
  RU3_short.printCode()

  val software_demand_identified =
    conclusion("Identify demand of all software").
      strategy("Formal methods and tests").
      `given`("- Execution model \\\\- Final configuration settings \\\\- Software implementation").
      evidence("Definition of \\emph{software demand}")

  val hardware_capacity_identified =
    conclusion("HW resources with their capacities identified").
      strategy("Reading and stressing benchmarks" backing "architecture mastery").
      `given`("MCP resources").
      `given`("Configuration settings $\\mathcal{C}s$ and application mapping").
      evidence("Synthesis of HW documentation").
      evidence("Definition of \\emph{amount of resources available}")

  val RU4 =
    conclusion("Identify the available MCP resources, how they are allocated and verify demand does not exceed the amount of resources available "
      short "RU4"
      size 10
      width patternWidth).
      strategy("Check demand on resources is correct").
      evidence(hardware_capacity_identified).
      evidence(software_demand_identified.label)

  RU4.printDiagram()
  RU4.printCode()

  val configuration_mitigation = conclusion("Configuration settings $i$ contributes to at least one requirement").
    strategy("Check $i$ contributes to at least one requirement").
    `given`("List of CAST32A objectives").
    evidence("List of PHYLOG diagrams to which $i$ contributes").
    evidence("Rationale of the contribution of $i$ to each PHYLOG diagrams")

  val correct_config_spec =
    conclusion("Configuration settings specification enables the hardware and the software to satisfy the requirements").
      strategy("Check contribution of each configuration settings $(\\forall i \\in \\mathcal{C}s)$ to the requirements").
      evidence("Description of the configuration settings $\\mathcal{C}s$").
      evidence(configuration_mitigation).
      evidence("Configuration settings $i$ not contributing to requirements is innocuous")

  val RU1 = conclusion(
    "MCP configuration settings enable the hardware and the software hosted on the MCP to satisfy the functional, timing and safety requirements"
      short "RU1"
      size 10
      width patternWidth ).
    strategy("DO178: Implementation compliant with specification \\\\ DO254: Hardware usage compliant with specification"  size 11 ).
    evidence(correct_config_spec).
    `given`("Configuration settings means implementation verified \\\\ (DO178B/C compliance)").
    `given`("Hardware configuration verified \\\\ (DO254 compliance)")

  RU1.printDiagram()
  RU1.printCode()

  System.getProperty("os.name").toLowerCase match {
    case "linux" | "mac" => Process("make patterns").!
    case _ =>
  }
}