package views.interference

/**
  * Package containing the operators related to interference computation that can be used on a PML model.
  * Examples are provided in [[views.interference.examples.simpleKeystone.SimpleKeystoneInterferenceGeneration]]
  * @note This package should be imported in all pml models as so
  *       {{{import views.interference.operators._}}}
  * @see [[Analyse.Ops]] provides the operators related to interference computation with Monosat [[https://github.com/sambayless/monosat]]
  * @see [[PostProcess.Ops]] provides the operators related to the post processing of the interference computation
  * @see [[Interfere.Ops]] provides the operators related to interference assumptions
  * @see [[Exclusive.Ops]] provides the operators related to exclusive assumptions (e.g., two [[pml.model.software.Application]]
  *      will not execute simultaneously)
  * @see [[Transparent.Ops]] proves the operators related to transparency assumptions (e.g., a [[pml.model.configuration.TransactionLibrary.Transaction]]
  *      is discarded)
  *
  *
  */
package object operators extends Analyse.Ops
  with PostProcess.Ops
  with Interfere.Ops
  with Exclusive.Ops
  with Transparent.Ops
  with Equivalent.Ops