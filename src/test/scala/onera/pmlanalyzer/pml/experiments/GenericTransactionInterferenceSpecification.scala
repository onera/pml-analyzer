package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.operators.*

trait GenericTransactionInterferenceSpecification
    extends PhysicalTableBasedInterferenceSpecification {
  self: GenericPlatform =>
}
