package pml

/**
  * Package containing the extension methods to export PML model as tables or Graphviz.
  *
  * Must be imported as following to enable export extension
  * {{{import pml.exporters._}}}
  *
  * The available extension methods are provided in [[UMLExporter.Ops]] and [[RelationExporter.Ops]]
  *
  * Example of usages are provided in [[pml.examples.simpleKeystone.SimpleKeystoneExport]]
  */
package object exporters extends UMLExporter.Ops
  with RelationExporter.Ops
