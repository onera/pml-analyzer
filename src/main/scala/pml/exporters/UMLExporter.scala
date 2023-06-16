/*******************************************************************************
 * Copyright (c)  2021. ONERA
 * This file is part of PML Analyzer
 *
 * PML Analyzer is free software ;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation ;
 * either version 2 of  the License, or (at your option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program ;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package pml.exporters

import pml.model.hardware._
import pml.model.service.{ArtificialService, Service}
import pml.model.software.Application
import pml.operators._

import java.io.{FileWriter, Writer}
import scala.collection.mutable.{HashMap => MHashMap}

object UMLExporter {

  /**
    * Extension methods
    */
  trait Ops {

    /**
      * Extension methods of platform to provide
      * uml export features
      * @param platform the platform providing the export features
      */
    implicit class UmlExporterOps(platform: Platform) {

      /**
        * The name of the export file will be
        * platform_nameExporter_name.exporter_extension
        * @param exporter the exporter used for the platform
        * @return the name of the export file
        */
      def umlExportName(exporter: PlatformExporter) : String =
        platform.fullName + exporter.name.name + "." + exporter.extension.name

      //TODO inconsistency with the platform naming format
      /**
        * For a software the name of the export file will be
        * platform_nameSoftware_name.exporter_extension
        * @param sw the software to export
        * @param exporter the exporter used for the platform
        * @return the name of the export file
        */
      def umlExportName(sw:Application, exporter: RestrictedPlatformExporter) : String =
        platform.fullName  + sw.name.name + "." + exporter.extension.name

      /**
        * Generate a writer from a file name, located in the
        * export directory provided by [[FileManager]]
        * @param name the file name
        * @return the writer
        */
      def getWriter(name:String): FileWriter = {
        val file = FileManager.exportDirectory.getFile(name)
        new FileWriter(file)
      }

      /**
        * Export the software and hardware connection graph (whether used or not)
        * as a graphviz file
        * @param exporter the implicit graphviz exporter available at method call
        */
      def exportHWAndSWGraph()(implicit exporter: DOTRelationExporter
        with FullPlatformExporter
        with FullDOTHWNamer
        with FullDOTSWNamer
        with NullServiceNamer
        with FullHWExporter
        with FullSWExporter):Unit = {
        val writer = getWriter(umlExportName(exporter))
        exporter.exportUML(platform)(writer)
        writer.close()
      }

      /**
        * Export the service connection graph (whether used or not)
        * as a graphviz file
        * @param exporter the implicit graphviz exporter available at method call
        */
      def exportServiceGraph()(implicit exporter: DOTRelationExporter
        with FullPlatformExporter
        with NullHWNamer
        with NullSWNamer
        with FullDOTServiceNamer
        with FullServiceExporter
        with NullHWExporter
        with NullSWExporter):Unit = {
        val writer = getWriter(umlExportName(exporter))
        exporter.exportUML(platform)(writer)
        writer.close()
      }

      /**
        * Export the software and hardware connection graph used by the configuration
        * as a graphviz file
        * @param exporter the implicit graphviz exporter available at method call
        */
      def exportRestrictedHWAndSWGraph()(implicit exporter: DOTRelationExporter
        with RestrictedPlatformExporter
        with FullDOTHWNamer
        with FullDOTSWNamer
        with NullServiceNamer
        with FullHWExporter
        with FullSWExporter):Unit = {
        val writer = getWriter(umlExportName(exporter))
        exporter.exportUML(platform)(writer)
        writer.close()
      }

      /**
        * Export the service connection graph (whether used or not)
        * as a graphviz file
        * @param exporter the implicit graphviz exporter available at method call
        */
      def exportRestrictedServiceAndSWGraph()(implicit exporter: DOTRelationExporter
        with RestrictedPlatformExporter
        with NullHWNamer
        with FullDOTSWNamer
        with FullDOTServiceNamer
        with FullServiceExporter
        with NullHWExporter
        with FullSWExporter):Unit = {
        val writer = getWriter(umlExportName(exporter))
        exporter.exportUML(platform)(writer)
        writer.close()
      }

      /**
        * Export the service connection graph used by given software
        * as a graphviz file
        * @param sw the software to export
        * @param exporter the implicit graphviz exporter available at method call
        */
      def exportRestrictedServiceGraphForSW(sw:Application)(implicit exporter: DOTRelationExporter
        with RestrictedPlatformExporter
        with NullHWNamer
        with FullDOTSWNamer
        with FullDOTServiceNamer
        with FullServiceExporter
        with NullHWExporter
        with FullSWExporter): Unit = {
        val writer = getWriter(umlExportName(sw,exporter))
        exporter.exportUMLSW(platform,sw)(writer)
        writer.close()
      }
    }
  }

  /**
    * Simple string writing in an implicit writer
    * @param a the string to write
    * @param writer the implicit writer
    */
  private def writeElement(a: String)(implicit writer: Writer): Unit = writer.write(s"$a\n")

  trait RelationExporter {

    /**
      * Write a composition relation
      * @param a the owner element as a string
      * @param b the owner element as a string
      * @param writer the implicit writet
      */
    def writeComposition(a: String, b: String)(implicit writer: Writer): Unit

    /**
      * Write an association relation
      * @param a the left element as a string
      * @param b the right element as a string
      * @param name the name of the relation by default empty
      * @param writer the implicit writer
      */
    def writeAssociation(a: String, b: String, name: String = "")(implicit writer: Writer): Unit

    /**
      * Write the header of a given export
      * @param writer the implicit writer
      */
    def writeHeader(implicit writer: Writer): Unit

    /**
      * Write the footer of a given export
      * @param writer the implicit writer
      */
    def writeFooter(implicit writer: Writer): Unit
  }

  trait DOTRelationExporter extends RelationExporter {

    def writeComposition(a: String, b: String)(implicit writer: Writer): Unit =
      writer.write(s"$a -> $b [dir=back, arrowtail=diamond]\n")

    def writeAssociation(a: String, b: String, name: String = "")(implicit writer: Writer): Unit =
      writer.write(s"$a -> $b[${if (name.nonEmpty) s"label=$name," else ""} arrowhead=none]\n")

    def writeHeader(implicit writer: Writer): Unit =
      writeElement(
        """digraph hierarchy {
          |size="5,5"
          |node[shape=record,style=filled]
          |edge[arrowtail=empty]
        """.stripMargin)

    def writeFooter(implicit writer: Writer): Unit = writeElement("}")
  }

  trait ServiceNamer {

    protected val _memoServiceId: MHashMap[Service, String] = MHashMap.empty

    /**
      * Build the id of a service if possible
      * @param x the service
      * @param writer the implicit writer
      * @return the unique id of the service
      */
    def getId(x: Service)(implicit writer: Writer): Option[String]

    /**
      * Build the element declaring the service
      * @param x the service
      * @return the element declaration as a string
      */
    def getElement(x: Service): Option[String]
  }

  trait ServiceExporter {

    /**
      * Empty the export caches
      */
    def resetService(): Unit

    /**
      * Print the export representation of a link between two services
      * @param from the origin service
      * @param to the destination service
      * @param writer the implicit writer
      */
    def exportUML(from: Service, to: Service)(implicit writer: Writer): Unit
  }

  trait NullServiceNamer extends ServiceNamer {

    def getId(x: Service)(implicit writer: Writer): Option[String] = None

    def getElement(x: Service): Option[String] = None
  }

  trait NullServiceExporter extends ServiceExporter {

    def resetService(): Unit = {}

    def exportUML(from: Service, to: Service)(implicit writer: Writer): Unit = {}
  }

  trait FullDOTServiceNamer extends ServiceNamer {

    def getId(x: Service)(implicit writer: Writer): Some[String] = Some(_memoServiceId.getOrElseUpdate(x, {
      writeElement(getElement(x).value)
      x.name.name
    }))

    def getElement(x: Service): Some[String] = x match {
      case a:ArtificialService => Some(s"""${x.name.name}[label = "{${x.name.name} : ${x.typeName.name}}", fillcolor=green]""")
      case s => Some(s"""${x.name.name}[label = "{${x.name.name} : ${x.typeName.name}}", fillcolor=green]""")
    }
  }

  trait FullServiceExporter extends ServiceExporter {
    self: ServiceNamer with RelationExporter =>

    def resetService(): Unit = _memoServiceId.clear()

    def exportUML(from: Service, to: Service)(implicit writer: Writer): Unit = {
      for {f <- getId(from); t <- getId(to)} yield writeAssociation(f, t)
    }
  }

  trait HWNamer {

    protected val _memoHWId: MHashMap[Hardware, String] = MHashMap.empty

    /**
      * Reset the internal caches
      */
    def resetHW(): Unit = _memoHWId.clear()

    /**
      * Build the unique id of a physical element
      *
      * @param x      the physical element
      * @param writer the implicit writer
      * @param pPB    the implicit relation of the provided basic services
      * @return the unique id
      */
    def getId(x: Hardware)(implicit
                           writer: Writer,
                           pPB: Provided[Hardware, Service]): Option[String]

    /**
      * Build the element declaration of the physical element
      *
      * @param x the physical element
      * @return the element declaration
      */
    def getElement(x: Hardware): Option[String]
  }

  trait HWExporter {
    def exportUML(from: Hardware, to: Hardware)(implicit
                                                writer: Writer,
                                                pPB: Provided[Hardware, Service]): Unit
  }

  trait NullHWExporter extends HWExporter {
    def exportUML(from: Hardware, to: Hardware)(implicit
                                                writer: Writer,
                                                pPB: Provided[Hardware, Service]): Unit = {}
  }

  trait NullHWNamer extends HWNamer {
    def getId(x: Hardware)(implicit
                           writer: Writer,
                           pPB: Provided[Hardware, Service]): None.type = None

    def getElement(x: Hardware): None.type = None
  }

  trait FullDOTHWNamer extends HWNamer {

    self: RelationExporter with ServiceNamer =>

    def getId(x: Hardware)(implicit
                           writer: Writer,
                           pPB: Provided[Hardware, Service]): Some[String] = Some(_memoHWId.getOrElseUpdate(x, {
      writeElement(getElement(x).value)
      val id = x.name.name
      for {c <- x.services; cs <- getId(c)} yield writeAssociation(id, cs)
      x match {
        case comp: Composite =>
          for (c <- comp.hardware; cs <- getId(c)) yield writeComposition(id, cs)
        case _ =>
      }
      id
    }))

    def getElement(x: Hardware): Some[String] = x match {
      case _: Transporter => Some(s"""${x.name.name}[label = "{${x.name.name} : ${x.typeName.name}}", fillcolor = mediumpurple1]""")
      case _: Target => Some(s"""${x.name.name}[label = "{${x.name.name} : ${x.typeName.name}}", fillcolor = darkolivegreen1]""")
      case _: Initiator => Some(s"""${x.name.name}[label = "{${x.name.name} : ${x.typeName.name}}", fillcolor = brown1]""")
      case _ => Some(s"""${x.name.name}[label = "{${x.name.name} : ${x.typeName.name}}", fillcolor = orange]""")
    }

  }

  trait FullHWExporter extends HWExporter {
    self: ServiceNamer with HWNamer with RelationExporter =>

    def exportUML(from: Hardware, to: Hardware)(implicit
                                                writer: Writer,
                                                pPB: Provided[Hardware, Service]): Unit = {
      for {f <- getId(from); t <- getId(to)} yield writeAssociation(f, t)
    }
  }

  trait SWNamer {

    /**
      * Build the unique id of a software element
      * @param sw the software
      * @return the unique id
      */
    def getId(sw: Application): Option[String]

    /**
      * Build the element declaration of the software element
      * @param x the software element
      * @return the element declaration
      */
    def getElement(x: Application): Option[String]
  }

  trait SWExporter {

    def exportUML(sw: Application)(implicit
                                   writer: Writer,
                                   pI: Provided[Initiator, Service],
                                   uSI: Used[Application, Initiator],
                                   uB: Used[Application, Service],
                                   pPB: Provided[Hardware, Service]): Unit
  }

  trait FullDOTSWNamer extends SWNamer {

    def getId(x: Application): Option[String] = Some(x.name.name)

    def getElement(x: Application): Option[String] = Some(s"""${x.name.name}[label = "{${x.name.name} : ${x.typeName.name}}", fillcolor = deepskyblue1]""")
  }

  trait NullSWNamer extends SWNamer {

    def getId(sw: Application): Option[String] = None

    def getElement(x: Application): Option[String] = None
  }

  trait FullSWExporter extends SWExporter {
    self: ServiceNamer with HWNamer with SWNamer with RelationExporter =>

    def exportUML(sw: Application)(implicit
                                   writer: Writer,
                                   pI: Provided[Initiator, Service],
                                   uSI: Used[Application, Initiator],
                                   uB: Used[Application, Service],
                                   pPB: Provided[Hardware, Service]): Unit = {
      for {s <- getElement(sw)} yield writeElement(s)
      //      for {c <- sw.targetService; s <- getId(sw); cs <- getId(c)} yield writeAssociation(s, cs, "use") //Activate to see target services
      for {c <- sw.hostingInitiators; s <- getId(sw); cs <- getId(c)} yield writeAssociation(s, cs)
      for {c <- sw.hostingInitiators; b <- c.services; s <- getId(sw); bs <- getId(b)} yield writeAssociation(s, bs)
    }
  }

  trait NullSWExporter extends SWExporter {
    def exportUML(sw: Application)(implicit
                                   writer: Writer,
                                   pI: Provided[Initiator, Service],
                                   uSI: Used[Application, Initiator],
                                   uB: Used[Application, Service],
                                   pPB: Provided[Hardware, Service]): Unit = {}
  }

  trait PlatformNamer {

    /**
      * Build the unique id of the platform
      * @param x the platform
      * @return the id
      */
    def getId(x: Platform): Option[String]

    /**
      * Build the element declaration of the platform
      * @param x the platform
      * @return the element declaration
      */
    def getElement(x: Platform): Option[String]
  }

  trait PlatformExporter {

    val name:Symbol
    val extension:Symbol

    /**
      * Export the platform as an UML diagram
      * @param platform the platform to export
      * @param writer the implicit writer
      */
    def exportUML(platform: Platform)(implicit writer: Writer): Unit
  }

  trait FullDOTPlatformNamer extends PlatformNamer {

    def getId(x: Platform): Option[String] = Some(x.name.name)

    def getElement(platform: Platform): Option[String] = Some(s"${getId(platform)}[label = {${platform.name.name} : ${platform.typeName.name}}, fillcolor = violet]")
  }

  trait NullPlatformNamer extends PlatformNamer {

    def getId(x: Platform): Option[String] = None

    def getElement(x: Platform): Option[String] = None
  }

  trait FullPlatformExporter extends PlatformExporter {
    self: HWExporter
      with HWNamer
      with SWExporter
      with SWNamer
      with ServiceExporter
      with PlatformNamer
      with RelationExporter =>

    val extension: Symbol = self match {
      case _ =>  Symbol("dot")
    }

    /**
      * Export the platform with all its software, hardware and services (even the ones that are not used)
      * @param platform the platform to export
      * @param writer the implicit writer
      */
    def exportUML(platform: Platform)(implicit writer: Writer): Unit = {
      import platform._
      resetService()
      resetHW()
      writeHeader
      for {c <- platform.applications; s <- getId(platform); cs <- getId(c)} yield writeComposition(s, cs)
      for {c <- platform.directHardware; s <- getId(platform); cs <- getId(c)} yield writeComposition(s, cs)
      platform.PLLinkableToPL.edges foreach { p =>
        p._2 foreach {
          exportUML(p._1, _)
        }
      }
      platform.applications.foreach(exportUML)
      platform.ServiceLinkableToService.edges foreach { p =>
        p._2 foreach {
          exportUML(p._1, _)
        }
      }
      writeFooter
      writer.flush()
    }
  }

  trait RestrictedPlatformExporter extends PlatformExporter {
    self: HWExporter
      with HWNamer
      with SWExporter
      with SWNamer
      with ServiceExporter
      with PlatformNamer
      with RelationExporter =>

    val extension: Symbol = self match {
      case _ =>  Symbol("dot")
    }

    /**
      * Export the platform with only the hardware, software and services that are used
      * @param platform the platform to export
      * @param writer the implicit writer
      */
    def exportUML(platform: Platform)(implicit writer: Writer): Unit = {
      import platform._
      resetService()
      resetHW()
      writeHeader
      val hwGraph = platform.hardwareGraph()
      val hwLinks = hwGraph.keySet flatMap { k => hwGraph(k) map { x => Set(k, x) } }
      val hwComponents = hwLinks.flatten
      for {hw <- hwComponents; p <- getId(platform); hwName <- getId(hw)} yield writeComposition(p, hwName)
      hwLinks foreach { p => exportUML(p.head, p.last) }
      platform.applications foreach exportUML
      val serviceGraph = platform.applications flatMap {
        platform.serviceGraphOf
      }
      val serviceLinks = serviceGraph flatMap { p => p._2 map { x => Set(p._1, x) } }
      serviceLinks foreach { p => exportUML(p.head, p.last) }
      writeFooter
      writer.flush()
    }

    /**
      * Export the hardware and services used by a given software in the platform
      * @param platform the platform owing the software
      * @param toPrint the software to pring
      * @param writer the implicit writer
      */
    def exportUMLSW(platform: Platform, toPrint: Application)(implicit writer: Writer): Unit = {
      import platform._
      resetService()
      resetHW()
      writeHeader
      for {s <- getId(platform); cs <- getId(toPrint)} yield writeComposition(s, cs)
      platform.hardwareGraphOf(toPrint).filter(_._2.nonEmpty).flatMap(kv => kv._2 + kv._1).foreach(hw => {
        for {s <- getId(platform); cs <- getId(hw)} yield writeComposition(s, cs)
      })
      exportUML(toPrint)
      platform.hardwareGraphOf(toPrint) foreach { p => p._2 foreach {
        exportUML(p._1, _)
      }
      }
      platform.serviceGraphOf(toPrint) foreach { p => p._2 foreach {
        exportUML(p._1, _)
      }
      }
      writeFooter
      writer.flush()
    }
  }

    implicit object FullDOT extends DOTRelationExporter
      with FullPlatformExporter
      with FullDOTPlatformNamer
      with FullDOTHWNamer
      with FullDOTSWNamer
      with FullDOTServiceNamer
      with FullServiceExporter
      with FullHWExporter
      with FullSWExporter {
      val name: Symbol = Symbol("Full")
    }

    implicit object DOTServiceOnly extends DOTRelationExporter
      with FullPlatformExporter
      with NullPlatformNamer
      with NullHWNamer
      with NullSWNamer
      with FullDOTServiceNamer
      with FullServiceExporter
      with NullHWExporter
      with NullSWExporter {
      val name: Symbol = Symbol("Service")
    }

    implicit object DOTHWAndSWOnly extends DOTRelationExporter
      with FullPlatformExporter
      with NullPlatformNamer
      with FullDOTHWNamer
      with FullDOTSWNamer
      with NullServiceNamer
      with NullServiceExporter
      with FullHWExporter
      with FullSWExporter {
      val name: Symbol = Symbol("HWAndSW")
    }

    implicit object DOTServiceAndSWClosureOnly extends DOTRelationExporter
      with RestrictedPlatformExporter
      with NullPlatformNamer
      with NullHWNamer
      with FullDOTSWNamer
      with FullDOTServiceNamer
      with FullServiceExporter
      with NullHWExporter
      with FullSWExporter {
      val name: Symbol = Symbol("RestrictedServiceAndSW")
    }

    implicit object DOTHWAndSWClosureOnly extends DOTRelationExporter
      with RestrictedPlatformExporter
      with NullPlatformNamer
      with FullDOTHWNamer
      with FullDOTSWNamer
      with NullServiceNamer
      with NullServiceExporter
      with FullHWExporter
      with FullSWExporter {
      val name: Symbol = Symbol("RestrictedHWAndSW")
    }

}