package onera.pmlanalyzer.pml.examples.components.memory

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.*
import onera.pmlanalyzer.pml.model.utils.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

final class DdrSdram(
    name: String,
    bank_nb: Int,
    bankGroup_nb: Int,
    rank_nb: Int,
    ddrInfo: ReflexiveInfo,
    ddrContext: Context
) extends Composite(Symbol(name), ddrInfo, ddrContext) {

  def this(
      name: String,
      bank_nb: Int,
      bankGroup_nb: Int,
      rank_nb: Int,
      dummy: Int = 0
  )(using
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(name, bank_nb, bankGroup_nb, rank_nb, givenInfo, givenContext)
  }

  def this(
      bank_nb: Int,
      bankGroup_nb: Int,
      rank_nb: Int
  )(using
      implicitName: Name,
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(
      implicitName.value,
      bank_nb,
      bankGroup_nb,
      rank_nb,
      givenInfo,
      givenContext
    )
  }

  // Transporter modelling the DDR memory device
  val PHY: SimpleTransporter = SimpleTransporter("PHY")

  // Transporter modelling the DDR memory ranks and bank groups
  val banks = for {
    s <- 0 until bankGroup_nb * rank_nb * bank_nb
  } yield {
    Target(s"Bank$s")
  }
  val bankGroups = for {
    s <- 0 until bankGroup_nb * rank_nb
  } yield {
    SimpleTransporter(s"BankGroup$s")
  }
  val ranks = for {
    s <- 0 until rank_nb
  } yield {
    SimpleTransporter(s"Rank$s")
  }

  for (r <- 0 until rank_nb) {
    PHY link ranks(r)
  }

  for (r <- 0 until rank_nb) {
    for (g <- 0 until bankGroup_nb) {
      ranks(r) link bankGroups(g + rank_nb * r)
      for (b <- 0 until bank_nb) {
        bankGroups(g + rank_nb * r) link banks(
          b + bank_nb * g + rank_nb * bank_nb * r
        )
      }
    }
  }

}
