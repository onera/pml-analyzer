package views.interference

//FIXME The usage of exporters is not illustrated in examples
/**
  * Package containing the interference related exporters
  * {{{
  * scala> import views.interference.exporters._
  * }}}
  * The available extension methods are provided in [[IDPExporter.Ops]] and [[InterferenceGraphExporter.Ops]]
  * Example of usages are provided in ???
  */
package object exporters extends IDPExporter.Ops
  with InterferenceGraphExporter.Ops
