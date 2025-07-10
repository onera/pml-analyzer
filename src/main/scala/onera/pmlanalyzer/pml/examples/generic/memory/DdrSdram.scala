package onera.pmlanalyzer.pml.examples.generic.memory

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.*
import onera.pmlanalyzer.pml.model.utils.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

final class DdrSdram(
    name: String,
    bankCnt: Int,
    bankGroupCnt: Int,
    rankCnt: Int,
    ddrInfo: ReflexiveInfo,
    ddrContext: Context
) extends Composite(Symbol(name), ddrInfo, ddrContext) {

  def this(
      _name: String,
      _bankCnt: Int,
      _bankGroupCnt: Int,
      _rankCnt: Int,
      dummy: Int = 0
  )(using
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(_name, _bankCnt, _bankGroupCnt, _rankCnt, givenInfo, givenContext)
  }

  def this(
      _bankCnt: Int,
      _bankGroupCnt: Int,
      _rankCnt: Int
  )(using
      implicitName: Name,
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(
      implicitName.value,
      _bankCnt,
      _bankGroupCnt,
      _rankCnt,
      givenInfo,
      givenContext
    )
  }

  // Transporter modelling the DDR memory device
  val phy: SimpleTransporter = SimpleTransporter("PHY")

  // Transporter modelling the DDR memory ranks and bank groups
  val banks: Seq[Target] =
    for (i <- 0 until bankGroupCnt * rankCnt * bankCnt)
      yield Target(s"Banks$i")

  val bankGroups: Seq[SimpleTransporter] =
    for (i <- 0 until bankGroupCnt * rankCnt)
      yield SimpleTransporter(s"BankGroup$i")

  val ranks: Seq[SimpleTransporter] =
    for (i <- 0 until rankCnt)
      yield SimpleTransporter(s"Rank$i")

  // Connections
  for (rank <- ranks) {
    phy link rank
  }

  for (r <- 0 until rankCnt) {
    for (g <- 0 until bankGroupCnt) {
      ranks(r) link bankGroups(g + rankCnt * r)
      for (b <- 0 until bankCnt) {
        bankGroups(g + rankCnt * r) link banks(
          b + bankCnt * g + rankCnt * bankCnt * r
        )
      }
    }
  }
}
