package onera.pmlanalyzer.pml.examples.components.memory

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
      name: String,
      bankCnt: Int,
      bankGroupCnt: Int,
      rankCnt: Int,
      dummy: Int = 0
  )(using
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(name, bankCnt, bankGroupCnt, rankCnt, givenInfo, givenContext)
  }

  def this(
      bankCnt: Int,
      bankGroupCnt: Int,
      rankCnt: Int
  )(using
      implicitName: Name,
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(
      implicitName.value,
      bankCnt,
      bankGroupCnt,
      rankCnt,
      givenInfo,
      givenContext
    )
  }

  // Transporter modelling the DDR memory device
  val phy: SimpleTransporter = SimpleTransporter("PHY")

  // Transporter modelling the DDR memory ranks and bank groups
  val banks: IndexedSeq[Target] =
    (0 until bankGroupCnt * rankCnt * bankCnt).map(s => Target(s"Banks${s}"))

  val bankGroups: IndexedSeq[SimpleTransporter] =
    (0 until bankGroupCnt * rankCnt).map(s =>
      SimpleTransporter(s"BankGroup${s}")
    )

  val ranks: IndexedSeq[SimpleTransporter] =
    (0 until rankCnt).map(s => SimpleTransporter(s"Rank${s}"))

  // Connections
  ranks.foreach(phy link _)

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
