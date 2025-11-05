package onera.pmlanalyzer.pml.model.instances.keystone

import onera.pmlanalyzer.pml.model.PMLNodeBuilder
import onera.pmlanalyzer.pml.model.configuration.{
  Transaction,
  TransactionLibrary
}
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*
import sourcecode.{File, Line}

import scala.language.postfixOps

/**
  * COM/MON longitudinal flight controller which is an
  * adaptation of RRosace (for redundant Rosace). The purpose
  * is to execute two parallel Rosace– an open source longitudinal
  * flight controller – and to verify regularly that both copies
  * named COM/MON for COMmand and MONitoring agree on the
  * computed orders
  */
trait RosaceConfiguration extends TransactionLibrary {
  self: KeystonePlatform =>

  extension (x: Application) {

    def instruction(implicit line: Line, file: File): Data = Data
      .get(PMLNodeBuilder.formatName(s"${x}Instruction", currentOwner))
      .getOrElse(Data(Symbol(s"${x}Instruction")))

    def data(implicit line: Line, file: File): Data = Data
      .get(PMLNodeBuilder.formatName(s"${x}Data", currentOwner))
      .getOrElse(Data(Symbol(s"${x}Data")))
  }

  /** -----------------------------------------------------------
    * Configuration specification
    * ----------------------------------------------------------- */

  /** -----------------------------------------------------------
    * Data specification
    * ----------------------------------------------------------- */

  // filters outputs: azF, vzF, hF, qF, vaF
  val azFA: Data = Data()
  val vzFA: Data = Data()
  val hFA: Data = Data()
  val qFA: Data = Data()
  val vaFA: Data = Data()

  // environment outputs: a, vz, h, q, va
  val azA: Data = Data()
  val vzA: Data = Data()
  val hA: Data = Data()
  val qA: Data = Data()
  val vaA: Data = Data()

  // commands: hC, VaC, delta_ec, delta_thc
  val hCA: Data = Data()
  val vaCA: Data = Data()
  val δEcA: Data = Data()
  val δThcA: Data = Data()

  // First Rosace data
  val RosaceAData: Set[Data] = Set(
    azFA,
    vzFA,
    hFA,
    qFA,
    vaFA,
    azA,
    vzA,
    hA,
    qA,
    vaA,
    hCA,
    vaCA,
    δEcA,
    δThcA
  )

  // filters outputs: azF, vzF, hF, qF, vaF
  val azFB: Data = Data()
  val vzFB: Data = Data()
  val hFB: Data = Data()
  val qFB: Data = Data()
  val vaFB: Data = Data()

  // environment outputs: a, vz, h, q, va
  val azB: Data = Data()
  val vzB: Data = Data()
  val hB: Data = Data()
  val qB: Data = Data()
  val vaB: Data = Data()

  // commands: hC, VaC, delta_ec, delta_thc
  val hCB: Data = Data()
  val vaCB: Data = Data()
  val δEcB: Data = Data()
  val δThcB: Data = Data()

  // Second Rosace data
  val RosaceBData: Set[Data] = Set(
    azFB,
    vzFB,
    hFB,
    qFB,
    vaFB,
    azB,
    vzB,
    hB,
    qB,
    vaB,
    hCB,
    vaCB,
    δEcB,
    δThcB
  )

  // cross check results
  val rA: Data = Data()
  val rB: Data = Data()

  /** -----------------------------------------------------------
    * Application specification : two cross checked Rosace applications
    * ----------------------------------------------------------- */

  // control software of first chain
  val altitudeHoldA: Application = Application()
  val vzControlA: Application = Application()
  val vaControlA: Application = Application()

  // filtering software of first chain
  val azFilterA: Application = Application()
  val vzFilterA: Application = Application()
  val hFilterA: Application = Application()
  val qFilterA: Application = Application()
  val vaFilterA: Application = Application()

  // aircraft model software of first chain
  val engineA: Application = Application()
  val elevatorA: Application = Application()
  val nnA: Application = Application()

  // control software of second chain
  val altitudeHoldB: Application = Application()
  val vzControlB: Application = Application()
  val vaControlB: Application = Application()

  // filtering software of second chain
  val azFilterB: Application = Application()
  val vzFilterB: Application = Application()
  val hFilterB: Application = Application()
  val qFilterB: Application = Application()
  val vaFilterB: Application = Application()

  // aircraft model software of second chain
  val engineB: Application = Application()
  val elevatorB: Application = Application()
  val nnB: Application = Application()

  // cross check software
  val checkA: Application = Application()
  val checkB: Application = Application()

  // IO Server handling DMA management
  val ioServer: Application = Application()

  // abstract software modelling DMA execution
  val edmaMicroCode: Application = Application()

  /** -----------------------------------------------------------
    * Data allocation
    * ----------------------------------------------------------- */

  // First Rosace data are stored use the MSMC SRAM B0
  RosaceAData foreach {
    _ hostedBy MSMC_SRAM.banks(0)
  }

  // Code sections are stored DDR B0
  altitudeHoldA.instruction hostedBy DDR.banks(0)
  vzControlA.instruction hostedBy DDR.banks(0)
  azFilterA.instruction hostedBy DDR.banks(0)
  hFilterA.instruction hostedBy DDR.banks(0)
  vzFilterA.instruction hostedBy DDR.banks(0)
  qFilterA.instruction hostedBy DDR.banks(0)
  vaFilterA.instruction hostedBy DDR.banks(0)
  vaControlA.instruction hostedBy DDR.banks(0)

  // Code section stored use private SRAM
  elevatorA.instruction hostedBy corePacs(0).isram
  elevatorA.data hostedBy corePacs(0).dsram
  engineA.instruction hostedBy corePacs(0).isram
  engineA.data hostedBy corePacs(0).dsram
  nnA.instruction hostedBy corePacs(0).isram

  // Second Rosace data are stored use the MSMC SRAM B1
  RosaceBData foreach {
    _ hostedBy MSMC_SRAM.banks(1)
  }

  // Code sections are stored DDR B1
  altitudeHoldB.instruction hostedBy DDR.banks(1)
  vzControlB.instruction hostedBy DDR.banks(1)
  azFilterB.instruction hostedBy DDR.banks(1)
  hFilterB.instruction hostedBy DDR.banks(1)
  vzFilterB.instruction hostedBy DDR.banks(1)
  qFilterB.instruction hostedBy DDR.banks(1)
  vaFilterB.instruction hostedBy DDR.banks(1)
  vaControlB.instruction hostedBy DDR.banks(1)

  // Code section stored use private SRAM
  elevatorB.instruction hostedBy corePacs(1).isram
  elevatorB.data hostedBy corePacs(1).dsram
  engineB.instruction hostedBy corePacs(1).isram
  engineB.data hostedBy corePacs(1).dsram
  nnB.instruction hostedBy corePacs(1).isram

  // Code section stored use private SRAM
  ioServer.data hostedBy corePacs(4).dsram
  ioServer.instruction hostedBy corePacs(4).isram

  // Data and code section stored use private SRAM
  rA hostedBy corePacs(2).dsram
  checkA.instruction hostedBy corePacs(2).isram

  // Data and code section stored use private SRAM
  rB hostedBy corePacs(3).dsram
  checkB.instruction hostedBy corePacs(3).isram

  /** -----------------------------------------------------------
    * Application allocation
    * ----------------------------------------------------------- */

  // Executed by ARM 0
  altitudeHoldA hostedBy ARMPac.cores(0).core
  vzControlA hostedBy ARMPac.cores(0).core
  azFilterA hostedBy ARMPac.cores(0).core
  hFilterA hostedBy ARMPac.cores(0).core

  // Executed by ARM 1
  vzFilterA hostedBy ARMPac.cores(1).core
  qFilterA hostedBy ARMPac.cores(1).core
  vaFilterA hostedBy ARMPac.cores(1).core
  vaControlA hostedBy ARMPac.cores(1).core

  // Executed by DSP 0
  elevatorA hostedBy corePacs(0).dsp
  engineA hostedBy corePacs(0).dsp
  nnA hostedBy corePacs(0).dsp

  // Executed by ARM 2
  altitudeHoldB hostedBy ARMPac.cores(2).core
  vzControlB hostedBy ARMPac.cores(2).core
  azFilterB hostedBy ARMPac.cores(2).core
  hFilterB hostedBy ARMPac.cores(2).core

  // Executed by ARM 3
  vzFilterB hostedBy ARMPac.cores(3).core
  qFilterB hostedBy ARMPac.cores(3).core
  vaFilterB hostedBy ARMPac.cores(3).core
  vaControlB hostedBy ARMPac.cores(3).core

  // Executed by DSP 1
  elevatorB hostedBy corePacs(1).dsp
  engineB hostedBy corePacs(1).dsp
  nnB hostedBy corePacs(1).dsp

  // Executed by DSP 2/3
  checkA hostedBy corePacs(2).dsp
  checkB hostedBy corePacs(3).dsp

  // Executed by DSP 4
  ioServer hostedBy corePacs(4).dsp

  // Executed by EDMA
  edmaMicroCode hostedBy EDMA

  /** -----------------------------------------------------------
    * DMA Transfers
    * ----------------------------------------------------------- */

  // Copy provided by EDMA
  private val edma_rd_spi = Transaction(edmaMicroCode read SPI)
  private val edma_rd_io = Transaction(edmaMicroCode read ioServer.data)
  private val edma_wr_hCA = Transaction(edmaMicroCode write hCA)
  private val edma_wr_vaCA = Transaction(edmaMicroCode write vaCA)
  private val edma_wr_hCB = Transaction(edmaMicroCode write hCB)
  private val edma_wr_vaCB = Transaction(edmaMicroCode write vaCB)
  private val edma_wr_spi = Transaction(edmaMicroCode write SPI)
  private val spi_CA = Transaction(
    edma_rd_spi,
    edma_wr_hCA,
    edma_wr_vaCA
  )
  private val spi_CB = Transaction(
    edma_rd_spi,
    edma_wr_hCB,
    edma_wr_vaCB
  )

  private val ioServer_SPI = Transaction(
    edma_rd_io,
    edma_wr_spi
  )

  spi_CA.used
  spi_CB.used
  ioServer_SPI.used

  /** -----------------------------------------------------------
    * Data usage
    * ----------------------------------------------------------- */

  for {
    a <- Application.all
  } yield {
    a read a.data
    a read a.instruction
    a write a.data
    a write a.instruction
  }

  altitudeHoldA read hCA
  altitudeHoldA read hFA
  vzControlA read azFA
  vzControlA read vzFA
  vzControlA read qFA
  vzControlA write δEcA
  azFilterA read azA
  azFilterA write azFA
  hFilterA read hA
  hFilterA write hFA
  vzFilterA read vzA
  vzFilterA write vzFA
  qFilterA read qA
  qFilterA write qFA
  vaFilterA read vaA
  vaFilterA write vaFA
  vaControlA read vzFA
  vaControlA read qFA
  vaControlA read vaFA
  vaControlA read vaCA
  vaControlA write δThcA
  elevatorA read δThcA
  engineA read δEcA
  nnA read engineA.data
  nnA read elevatorA.data
  nnA write azA
  nnA write hA
  nnA write vzA
  nnA write qA
  nnA write vaA

  altitudeHoldB read hCB
  altitudeHoldB read hFB
  vzControlB read azFB
  vzControlB read vzFB
  vzControlB read qFB
  vzControlB write δEcB
  azFilterB read azB
  azFilterB write azFB
  hFilterB read hB
  hFilterB write hFB
  vzFilterB read vzB
  vzFilterB write vzFB
  qFilterB read qB
  qFilterB write qFB
  vaFilterB read vaB
  vaFilterB write vaFB
  vaControlB read vzFB
  vaControlB read qFB
  vaControlB read vaFB
  vaControlB read vaCB
  vaControlB write δThcB
  elevatorB read δThcB
  engineB read δEcB
  nnB read engineB.data
  nnB read elevatorB.data
  nnB write azB
  nnB write hB
  nnB write vzB
  nnB write qB
  nnB write vaB

  checkA read RosaceAData
  checkA read RosaceBData
  checkA write rA

  checkB read RosaceAData
  checkB read RosaceBData
  checkB write rB

  ioServer read hA
  ioServer read hB
  ioServer read rA
  ioServer read rB
  ioServer read EDMARegister
  ioServer read SPI
  ioServer write EDMARegister

  /** -----------------------------------------------------------
    * Deactivation constraints
    * ----------------------------------------------------------- */
  PCIe.deactivated

  corePacs.drop(5) foreach { c =>
    {
      c.dsram.deactivated
      c.dsp.deactivated
      c.mpax.deactivated
    }
  }
}
