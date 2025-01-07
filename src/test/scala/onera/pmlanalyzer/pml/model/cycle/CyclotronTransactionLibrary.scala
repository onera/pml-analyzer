package onera.pmlanalyzer.pml.model.cycle

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

// A single transaction required for the cycle detection to kick in.
trait CyclotronTransactionLibrary extends TransactionLibrary {
  self: CyclotronPlatform =>

  val tr: Transaction = Transaction(dma read eth)
  tr.used
}
