package onera.pmlanalyzer.pml.model.cycle

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

/* There is no path from the DMA to the group. The constraints is null, but
   required for the issue to appear.
 */
trait CyclotronBlockingConstraint {
  self: CyclotronPlatform =>

  dma targeting eth blockedBy group.input_port
}
