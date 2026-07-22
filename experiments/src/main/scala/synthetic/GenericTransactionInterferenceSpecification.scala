package synthetic

import onera.pmlanalyzer.*
import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification

trait GenericTransactionInterferenceSpecification
    extends PhysicalTableBasedInterferenceSpecification {
  self: GenericPlatform =>
}
