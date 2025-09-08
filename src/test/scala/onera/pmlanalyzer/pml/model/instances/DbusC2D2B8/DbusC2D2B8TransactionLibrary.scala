/** *****************************************************************************
  * Copyright (c) 2023. ONERA This file is part of PML Analyzer
  *
  * PML Analyzer is free software ; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation ; either version 2 of the License, or (at your
  * option) any later version.
  *
  * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program ; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */

package onera.pmlanalyzer.pml.model.instances.DbusC2D2B8

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.configuration.*

import scala.language.postfixOps

trait DbusC2D2B8TransactionLibrary extends TransactionLibrary {
  self: DbusC2D2B8Platform with DbusC2D2B8Software =>

  val tr_rosace_cg0_cl0_C0_l1_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.cg0.cl0.C0_L1
  )
  tr_rosace_cg0_cl0_C0_l1_ld.used

  val tr_rosace_cg0_cl0_C0_l1_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.cg0.cl0.C0_L1
  )
  tr_rosace_cg0_cl0_C0_l1_st.used

  val tr_rosace_cg0_cl0_C0_l2_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.cg0.cl0.l2
  )
  tr_rosace_cg0_cl0_C0_l2_ld.used

  val tr_rosace_cg0_cl0_C0_l2_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.cg0.cl0.l2
  )
  tr_rosace_cg0_cl0_C0_l2_st.used

  val tr_rosace_cg0_cl0_C0_BK0_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.ddr.BK0
  )
  tr_rosace_cg0_cl0_C0_BK0_ld.used

  val tr_rosace_cg0_cl0_C0_BK0_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.ddr.BK0
  )
  tr_rosace_cg0_cl0_C0_BK0_st.used

  val tr_rosace_cg0_cl0_C0_BK1_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.ddr.BK1
  )
  tr_rosace_cg0_cl0_C0_BK1_ld.used

  val tr_rosace_cg0_cl0_C0_BK1_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.ddr.BK1
  )
  tr_rosace_cg0_cl0_C0_BK1_st.used

  val tr_rosace_cg0_cl0_C0_BK2_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.ddr.BK2
  )
  tr_rosace_cg0_cl0_C0_BK2_ld.used

  val tr_rosace_cg0_cl0_C0_BK2_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.ddr.BK2
  )
  tr_rosace_cg0_cl0_C0_BK2_st.used

  val tr_rosace_cg0_cl0_C0_BK3_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.ddr.BK3
  )
  tr_rosace_cg0_cl0_C0_BK3_ld.used

  val tr_rosace_cg0_cl0_C0_BK3_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.ddr.BK3
  )
  tr_rosace_cg0_cl0_C0_BK3_st.used

  val tr_rosace_cg0_cl0_C1_l1_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 read rosace.cg0.cl0.C1_L1
  )
  tr_rosace_cg0_cl0_C1_l1_ld.used

  val tr_rosace_cg0_cl0_C1_l1_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 write rosace.cg0.cl0.C1_L1
  )
  tr_rosace_cg0_cl0_C1_l1_st.used

  val tr_rosace_cg0_cl0_C1_l2_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 read rosace.cg0.cl0.l2
  )
  tr_rosace_cg0_cl0_C1_l2_ld.used

  val tr_rosace_cg0_cl0_C1_l2_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 write rosace.cg0.cl0.l2
  )
  tr_rosace_cg0_cl0_C1_l2_st.used

  val tr_rosace_cg0_cl0_C1_BK4_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 read rosace.ddr.BK4
  )
  tr_rosace_cg0_cl0_C1_BK4_ld.used

  val tr_rosace_cg0_cl0_C1_BK4_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 write rosace.ddr.BK4
  )
  tr_rosace_cg0_cl0_C1_BK4_st.used

  val tr_rosace_cg0_cl0_C1_BK5_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 read rosace.ddr.BK5
  )
  tr_rosace_cg0_cl0_C1_BK5_ld.used

  val tr_rosace_cg0_cl0_C1_BK5_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 write rosace.ddr.BK5
  )
  tr_rosace_cg0_cl0_C1_BK5_st.used

  val tr_rosace_cg0_cl0_C1_BK6_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 read rosace.ddr.BK6
  )
  tr_rosace_cg0_cl0_C1_BK6_ld.used

  val tr_rosace_cg0_cl0_C1_BK6_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 write rosace.ddr.BK6
  )
  tr_rosace_cg0_cl0_C1_BK6_st.used

  val tr_rosace_cg0_cl0_C1_BK7_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 read rosace.ddr.BK7
  )
  tr_rosace_cg0_cl0_C1_BK7_ld.used

  val tr_rosace_cg0_cl0_C1_BK7_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C1 write rosace.ddr.BK7
  )
  tr_rosace_cg0_cl0_C1_BK7_st.used

  val tr_rosace_dg0_cl0_C0_sram_ld: Transaction = Transaction(
    app_rosace_dg0_cl0_C0 read rosace.dg0.cl0.C0_SRAM
  )
  tr_rosace_dg0_cl0_C0_sram_ld.used

  val tr_rosace_dg0_cl0_C0_sram_st: Transaction = Transaction(
    app_rosace_dg0_cl0_C0 write rosace.dg0.cl0.C0_SRAM
  )
  tr_rosace_dg0_cl0_C0_sram_st.used

  val tr_rosace_dg0_cl0_C1_sram_ld: Transaction = Transaction(
    app_rosace_dg0_cl0_C1 read rosace.dg0.cl0.C1_SRAM
  )
  tr_rosace_dg0_cl0_C1_sram_ld.used

  val tr_rosace_dg0_cl0_C1_sram_st: Transaction = Transaction(
    app_rosace_dg0_cl0_C1 write rosace.dg0.cl0.C1_SRAM
  )
  tr_rosace_dg0_cl0_C1_sram_st.used

  val tr_CIO_dma_reg_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.cfg_bus.dma_reg
  )
  tr_CIO_dma_reg_ld.used

  val tr_CIO_dma_reg_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.cfg_bus.dma_reg
  )
  tr_CIO_dma_reg_st.used

  val tr_CIO_BK0_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.ddr.BK0
  )
  tr_CIO_BK0_ld.used

  val tr_CIO_BK0_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.ddr.BK0
  )
  tr_CIO_BK0_st.used

  private val dma_rd_bk0 = Transaction(app_dma read rosace.ddr.BK0)
  private val dma_wr_eth = Transaction(app_dma write rosace.eth)
  val tr_dma_BK0_eth: Scenario =
    Scenario(dma_rd_bk0, dma_wr_eth)
  tr_dma_BK0_eth.used

  private val dma_wr_bk2 = Transaction(app_dma write rosace.ddr.BK2)
  private val dma_rd_eth = Transaction(app_dma read rosace.eth)
  val tr_dma_eth_BK2: Scenario =
    Scenario(dma_wr_bk2, dma_rd_eth)
  tr_dma_eth_BK2.used

  private val dma_wr_bk4 = Transaction(app_dma write rosace.ddr.BK4)
  val tr_dma_eth_BK4: Scenario =
    Scenario(dma_wr_bk4, dma_rd_eth)
  tr_dma_eth_BK4.used

  private val dma_wr_bk6 = Transaction(app_dma write rosace.ddr.BK6)
  val tr_dma_eth_BK6: Scenario =
    Scenario(dma_wr_bk6, dma_rd_eth)
  tr_dma_eth_BK6.used

}
