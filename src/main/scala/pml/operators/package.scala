package pml
/**
  * Package containing all the extension methods provided by operators
  *
  * @note This package should be imported in all pml models as so
  * {{{
  * import pml.operators._
  * }}}
  * @see [[Link.Ops]] for link (e.g. link/unlink) keywords
  * @see [[Linked.Ops]] for linked (e.g. linked/reverse) keywords
  * @see [[Deactivate.Ops]] for deactivate (e.g. deactivate) keywords
  * @see [[Provided.Ops]] for provide (e.g. services/loads/stores) keywords
  * @see [[Use.Ops]] for use (e.g. use/hostedBy) keywords
  * @see [[Used.Ops]] for used (e.g. used/targetLoads) keywords
  * @see [[Merge.Ops]] for and (e.g. and) keywords
  * @see [[Restrict.Ops]] for restrict (e.g. restrictedTo) keywords
  * @see [[Route.Ops]] for route (e.g. useLink/cannotUseLink) keywords
  */
package object operators extends Link.Ops
  with Linked.Ops
  with Deactivate.Ops
  with Provided.Ops
  with Use.Ops
  with Used.Ops
  with Merge.Ops
  with Restrict.Ops
  with Route.Ops 
  with AsTransaction.Ops
