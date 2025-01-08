package onera.pmlanalyzer.pml.model.cycle

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*
trait CyclotronCannotUseConstraint {
  self: CyclotronPlatform =>

  dma targeting eth cannotUseLink group.other_port to group.L0
}
