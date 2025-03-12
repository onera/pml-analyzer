/** *****************************************************************************
  * Copyright (c) 2023. ONERA This file is part of PML Analyzer
  *
  * PML Analyzer is free software ; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation ; either version 2 of the License, or (at your
  * option) any later version.
  *
  * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program ; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */

package onera.pmlanalyzer.pml.exporters

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.service.{ArtificialService, Service}
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.operators.*

import java.io.{FileWriter, Writer}
import scala.collection.immutable.{AbstractSet, SortedSet}
import scala.collection.mutable.HashMap as MHashMap

object UMLExporter {

  /** Extension methods
    */
  trait Ops {

    /** Extension methods of platform to provide uml export features
      *   the platform providing the export features
      */
    extension (platform: Platform) {

      /** The name of the export file will be
        * platform_nameExporter_name.exporter_extension
        * @param exporter
        *   the exporter used for the platform
        * @return
        *   the name of the export file
        */
      def umlExportName(exporter: PlatformExporter): String =
        platform.fullName + exporter.name.name + "." + exporter.extension.name

      // TODO inconsistency with the platform naming format
      /** For a software the name of the export file will be
        * platform_nameSoftware_name.exporter_extension
        * @param sw
        *   the software to export
        * @param exporter
        *   the exporter used for the platform
        * @return
        *   the name of the export file
        */
      def umlExportName(
          sw: Application,
          exporter: RestrictedPlatformExporter
      ): String =
        platform.fullName + sw.name.name + "." + exporter.extension.name

      /** Generate a writer from a file name, located in the export directory
        * provided by [[FileManager]]
        * @param name
        *   the file name
        * @return
        *   the writer
        */
      def getWriter(name: String): FileWriter = {
        val file = FileManager.exportDirectory.getFile(name)
        new FileWriter(file)
      }

      /** Export the software and hardware connection graph (whether used or
        * not) as a graphviz file
        * @param exporter
        *   the implicit graphviz exporter available at method call
        */
      def exportHWAndSWGraph()(implicit
          exporter: DOTRelationExporter
            with FullPlatformExporter
            with FullDOTHWNamer
            with FullDOTSWNamer
            with NullServiceNamer
            with FullHWExporter
            with FullSWExporter
      ): Unit = {
        val writer = getWriter(umlExportName(exporter))
        exporter.exportUML(platform)(writer)
        writer.close()
      }

      /** Export the service connection graph (whether used or not) as a
        * graphviz file
        * @param exporter
        *   the implicit graphviz exporter available at method call
        */
      def exportServiceGraph()(implicit
          exporter: DOTRelationExporter
            with FullPlatformExporter
            with NullHWNamer
            with NullSWNamer
            with FullDOTServiceNamer
            with FullServiceExporter
            with NullHWExporter
            with NullSWExporter
      ): Unit = {
        val writer = getWriter(umlExportName(exporter))
        exporter.exportUML(platform)(writer)
        writer.close()
      }

      /** Export the software and hardware connection graph used by the
        * configuration as a graphviz file
        * @param exporter
        *   the implicit graphviz exporter available at method call
        */
      def exportRestrictedHWAndSWGraph()(implicit
          exporter: DOTRelationExporter
            with RestrictedPlatformExporter
            with FullDOTHWNamer
            with FullDOTSWNamer
            with NullServiceNamer
            with FullHWExporter
            with FullSWExporter
      ): Unit = {
        val writer = getWriter(umlExportName(exporter))
        exporter.exportUML(platform)(writer)
        writer.close()
      }

      /** Export the service connection graph (whether used or not) as a
        * graphviz file
        * @param exporter
        *   the implicit graphviz exporter available at method call
        */
      def exportRestrictedServiceAndSWGraph()(implicit
          exporter: DOTRelationExporter
            with RestrictedPlatformExporter
            with NullHWNamer
            with FullDOTSWNamer
            with FullDOTServiceNamer
            with FullServiceExporter
            with NullHWExporter
            with FullSWExporter
      ): Unit = {
        val writer = getWriter(umlExportName(exporter))
        exporter.exportUML(platform)(writer)
        writer.close()
      }

      /** Export the service connection graph used by given software as a
        * graphviz file
        * @param sw
        *   the software to export
        * @param exporter
        *   the implicit graphviz exporter available at method call
        */
      def exportRestrictedServiceGraphForSW(sw: Application)(implicit
          exporter: DOTRelationExporter
            with RestrictedPlatformExporter
            with NullHWNamer
            with FullDOTSWNamer
            with FullDOTServiceNamer
            with FullServiceExporter
            with NullHWExporter
            with FullSWExporter
      ): Unit = {
        val writer = getWriter(umlExportName(sw, exporter))
        exporter.exportUMLSW(platform, sw)(writer)
        writer.close()
      }

      def exportRestrictedServiceGraphWithInterfere()(implicit
          exporter: DOTRelationExporter
            with RestrictedPlatformExporter
            with NullHWNamer
            with NullSWNamer
            with NullServiceNamer
            with NullServiceExporter
            with FullServiceSetNamer
            with FullServiceSetExporter
            with NullHWExporter
            with NullSWExporter
      ): Unit = {
        val writer = getWriter(umlExportName(exporter))
        exporter.exportUML(platform)(writer)
        writer.close()
      }

      def exportServiceGraphWithInterfere()(implicit
          exporter: DOTRelationExporter
            with FullPlatformExporter
            with NullHWNamer
            with NullSWNamer
            with NullServiceNamer
            with NullServiceExporter
            with FullServiceSetNamer
            with FullServiceSetExporter
            with NullHWExporter
            with NullSWExporter
      ): Unit = {
        val writer = getWriter(umlExportName(exporter))
        exporter.exportUML(platform)(writer)
        writer.close()
      }
    }
  }

  abstract class Element {
    val name: String
    val id: Int = name.hashCode
  }

  trait RelationExporter {

    sealed abstract class Association {
      val left: Int
      val right: Int
      val name: String
    }

    /** the header of a given export
      */
    def getHeader: String

    /** the footer of a given export
      */
    def getFooter: String
  }

  trait DOTRelationExporter extends RelationExporter {

    case class DOTAssociation(left: Int, right: Int, name: String)
      extends Association {
      override def toString: String =
        s"$left -> $right ${if (name.nonEmpty) s"label=$name," else ""}[arrowhead=none]\n"
    }

    def getHeader: String =
      """digraph hierarchy {
          |size="5,5"
          |node[shape=record,style=filled]
          |edge[arrowtail=empty]
        """.stripMargin

    def getFooter: String = "}"
  }

  trait DOTNamer {
    case class DOTElement(name: String, color: String)
      extends Element {
      override def toString: String =
        s"""$id[label = "{$name}", fillcolor=$color]\n"""
    }

    case class DOTCluster(
                           name: String,
                           color: String,
                           subElements: Set[DOTCluster | DOTElement]
                         ) extends Element {
      override def toString: String =
        s"""digraph cluster_$id {
           |label = "{$name}"
           |style = filled
           |color = $color
           |  ${
          subElements.toSeq
            .sortBy(_.name)
        }
           |}
           |""".stripMargin
    }
  }

  trait ServiceSetNamer extends DOTNamer {

    protected val _memoServiceSetId: MHashMap[Set[Service], DOTElement] =
      MHashMap.empty

    def getElement(x: Set[Service]): Option[DOTElement]

    def getName(x: Set[Service]): String

    def getId(x: Set[Service]): Option[Int] =
      for {
        e <- getElement(x)
      } yield e.id
  }

  trait FullServiceSetNamer extends ServiceSetNamer {
    def getElement(x: Set[Service]): Some[DOTElement] =
      Some(
        _memoServiceSetId.getOrElseUpdate(
          x, DOTElement(getName(x), "green")
        )
      )

    def getName(x: Set[Service]): String =
      if (x.size == 1)
        x.head.name.name
      else if (
        x.size == 2 && (x.head.name.name
          .split("_")
          .init sameElements x.last.name.name.split("_").init)
      ) {
        val prefix = x.head.name.name.split("_").init.mkString("_")
        val suffix = List(
          x.head.name.name.split("_").last,
          x.last.name.name.split("_").last
        ).sorted
        s"${prefix}_${suffix.mkString("_")}"
      } else
        x.toList.map(_.name.name).sorted.mkString("_")

  }

  trait NullServiceSetNamer extends ServiceSetNamer {
    def getElement(x: Set[Service]): Option[DOTElement] = None

    def getName(x: Set[Service]): String = ""
  }

  trait ServiceSetExporter extends DOTRelationExporter {
    def resetServiceSet(): Unit

    def getAssociation(
                        from: Set[Service],
                        to: Set[Service],
                        tyype: String
                      ): Option[DOTAssociation]
  }

  trait FullServiceSetExporter extends ServiceSetExporter {
    self: ServiceSetNamer with RelationExporter =>

    def resetServiceSet(): Unit = _memoServiceSetId.clear()

    def getAssociation(
                        from: Set[Service],
                        to: Set[Service],
                        tyype: String
                      ): Option[DOTAssociation] = {
      for {f <- getId(from); t <- getId(to)} yield DOTAssociation(f, t, tyype)
    }
  }

  trait NullServiceSetExporter extends ServiceSetExporter {
    def resetServiceSet(): Unit = {}

    def getAssociation(
                        from: Set[Service],
                        to: Set[Service],
                        tyype: String
                      ): Option[DOTAssociation] = None
  }

  trait ServiceNamer extends DOTNamer {

    protected val _memoServiceId: MHashMap[Service, DOTElement] = MHashMap.empty

    /** Build the id of a service if possible
      * @param x
      *   the service
      * @return
      *   the unique id of the service
      */
    def getId(x: Service): Option[Int] =
      for {
        e <- getElement(x)
      } yield e.id

    /** Build the element declaring the service
      * @param x
      *   the service
      * @return
      *   the element declaration as a string
      */
    def getElement(x: Service): Option[DOTElement]
  }

  trait ServiceExporter extends DOTRelationExporter {

    /** Empty the export caches
      */
    def resetService(): Unit

    /** Print the export representation of a link between two services
      * @param from
      *   the origin service
      * @param to
      *   the destination service
      */
    def getAssociation(from: Service, to: Service, tyype: String): Option[DOTAssociation]
  }

  trait NullServiceNamer extends ServiceNamer {

    def getElement(x: Service): Option[DOTElement] = None
  }

  trait NullServiceExporter extends ServiceExporter {

    def resetService(): Unit = {}

    def getAssociation(from: Service, to: Service, tyype: String): Option[DOTAssociation] =
      None
  }

  trait FullDOTServiceNamer extends ServiceNamer {

    def getElement(x: Service): Option[DOTElement] = Some(
      _memoServiceId.getOrElseUpdate(
        x, {
          x match
            case a: ArtificialService => DOTElement(a.name.name, "green")
            case s => DOTElement(s.name.name, "gray")
        }
      )
    )
  }

  trait FullServiceExporter extends ServiceExporter {
    self: ServiceNamer with RelationExporter =>

    def resetService(): Unit = _memoServiceId.clear()

    def getAssociation(from: Service, to: Service, tyype: String): Option[DOTAssociation] = {
      for {f <- getId(from); t <- getId(to)} yield DOTAssociation(f, t, tyype)
    }
  }

  trait HWNamer extends DOTNamer {

    protected val _memoHWId: MHashMap[Hardware, DOTElement | DOTCluster] =
      MHashMap.empty

    /** Reset the internal caches
      */
    def resetHW(): Unit = _memoHWId.clear()

    /** Build the unique id of a physical element
      *
      * @param x
      *   the physical element
     * @param pb
      *   the implicit relation of the provided basic services
      * @return
      *   the unique id
      */
    def getId(x: Hardware)(using pb: Provided[Hardware, Service]): Option[Int] =
      for {
        e <- getElement(x)
      } yield e.id

    /** Build the element declaration of the physical element
      *
      * @param x
      *   the physical element
      * @return
      *   the element declaration
      */
    def getElement(x: Hardware)(using
                                pb: Provided[Hardware, Service]
    ): Option[DOTElement | DOTCluster]

  }

  trait HWExporter extends DOTRelationExporter {
    def getAssociation(from: Hardware, to: Hardware, tyype: String)(using
                                                                    pb: Provided[Hardware, Service],
    ): Option[DOTAssociation]
  }

  trait NullHWExporter extends HWExporter {
    def getAssociation(from: Hardware, to: Hardware, tyype: String)(using
                                                                    pb: Provided[Hardware, Service]
    ): Option[DOTAssociation] = None
  }

  trait NullHWNamer extends HWNamer {
    def getElement(x: Hardware)(using
                                pb: Provided[Hardware, Service]
    ): Option[DOTElement | DOTCluster] = None
  }

  trait FullDOTHWNamer extends HWNamer {

    self: RelationExporter with ServiceNamer =>

    def getElement(x: Hardware)(using
                                pb: Provided[Hardware, Service]
    ): Option[DOTElement | DOTCluster] = Some(
      _memoHWId.getOrElseUpdate(
        x, {
          x match {
            case a: (Transporter | Target | Initiator) =>
              val serviceElements: Set[DOTElement | DOTCluster] =
                for {
                  s <- x.services
                  e <- getElement(s)
                } yield e
              val color = a match {
                case _: Transporter => "mediumpurple1"
                case _: Target => "darkolivegreen1"
                case _: Initiator => "brown1"
              }
              if (serviceElements.nonEmpty)
                DOTCluster(x.name.name, color, serviceElements)
              else
                DOTElement(x.name.name, color)
            case c: Composite =>
              val elements =
                for {
                  h <- c.hardware
                  e <- getElement(h)
                } yield e
              DOTCluster(x.name.name, "orange", elements)
          }
        }
      )
    )

  }

  trait FullHWExporter extends HWExporter {
    self: ServiceNamer with HWNamer with RelationExporter =>

    def getAssociation(from: Hardware, to: Hardware, tyype: String)(using
        pPB: Provided[Hardware, Service]
    ): Option[DOTAssociation] = {
      for {f <- getId(from); t <- getId(to)} yield DOTAssociation(f, t, tyype)
    }
  }

  trait SWNamer extends DOTNamer {

    /** Build the unique id of a software element
      * @param sw
      *   the software
      * @return
      *   the unique id
      */
    def getId(sw: Application): Option[Int] =
      for {
        e <- getElement(sw)
      } yield e.id

    /** Build the element declaration of the software element
      * @param x
      *   the software element
      * @return
      *   the element declaration
      */
    def getElement(x: Application): Option[DOTElement]
  }

  trait SWExporter extends DOTRelationExporter {

    def getAssociation(sw: Application, tyype: String)(implicit
        pI: Provided[Initiator, Service],
        uSI: Used[Application, Initiator],
        uB: Used[Application, Service],
        pPB: Provided[Hardware, Service]
    ): Set[DOTAssociation]
  }

  trait FullDOTSWNamer extends SWNamer {

    def getElement(x: Application): Option[DOTElement] =
      Some(
        DOTElement(
          x.name.name,
          "deepskyblue1"
        )
      )
  }

  trait NullSWNamer extends SWNamer {
    def getElement(x: Application): Option[DOTElement] = None
  }

  trait FullSWExporter extends SWExporter {
    self: ServiceNamer with HWNamer with SWNamer with RelationExporter =>

    def getAssociation(sw: Application, tyype: String)(implicit
        pI: Provided[Initiator, Service],
        uSI: Used[Application, Initiator],
        uB: Used[Application, Service],
        pPB: Provided[Hardware, Service]
    ): Set[DOTAssociation] = {
      val hosts =
        for {
          c <- sw.hostingInitiators; s <- getId(sw); cs <- getId(c)
        } yield DOTAssociation(s, cs, tyype)
      val services = for {
        c <- sw.hostingInitiators; b <- c.services; s <- getId(sw)
        bs <- getId(b)
      } yield DOTAssociation(s, bs, tyype)
      hosts ++ services
    }
  }

  trait NullSWExporter extends SWExporter {
    def getAssociation(sw: Application, tyype: String)(implicit
        pI: Provided[Initiator, Service],
        uSI: Used[Application, Initiator],
        uB: Used[Application, Service],
        pPB: Provided[Hardware, Service]
    ): Set[DOTAssociation] = Set.empty
  }

  trait PlatformNamer {

    /** Build the unique id of the platform
      * @param x
      *   the platform
      * @return
      *   the id
      */
    def getId(x: Platform): Option[String]
  }

  trait PlatformExporter extends DOTRelationExporter {

    val name: Symbol
    val extension: Symbol

    /** Export the platform as an UML diagram
      * @param platform
      *   the platform to export
      * @param writer
      *   the implicit writer
      */
    def exportUML(platform: Platform)(implicit writer: Writer): Unit
  }

  trait FullDOTPlatformNamer extends PlatformNamer {

    def getId(x: Platform): Option[String] = Some(x.name.name)

  }

  trait NullPlatformNamer extends PlatformNamer {

    def getId(x: Platform): Option[String] = None

  }

  trait FullPlatformExporter extends PlatformExporter {
    self: HWExporter
      with HWNamer
      with SWExporter
      with SWNamer
      with ServiceSetNamer
      with ServiceSetExporter
      with ServiceExporter
      with PlatformNamer
      with RelationExporter =>

    val extension: Symbol = self match {
      case _ => Symbol("dot")
    }

    /** Export the platform with all its software, hardware and services (even
      * the ones that are not used)
      * @param platform
      *   the platform to export
      * @param writer
      *   the implicit writer
      */
    def exportUML(platform: Platform)(implicit writer: Writer): Unit = {
      import platform._
      resetService()
      resetHW()
      resetServiceSet()
      writer.write(getHeader)
      for {
        a <- platform.applications
        aE <- getElement(a)
      }
        writer.write(aE.toString)

      for {
        h <- platform.directHardware
        hE <- getElement(h)
      }
        writer.write(hE.toString)

      for {
        (k, v) <- platform.PLLinkableToPL.edges
        x <- v
        as <- getAssociation(k, x, "")
      }
        writer.write(as.toString)

      for {
        a <- platform.applications
        as <- getAssociation(a, "")
      }
        writer.write(as.toString)

      for {
        (k, v) <- platform.ServiceLinkableToService.edges
        x <- v
        as <- getAssociation(k, x, "")
      }
        writer.write(as.toString)

      val serviceSetGraph = platform.fullServiceGraphWithInterfere()
      val serviceSetLinks =
        (serviceSetGraph flatMap { p => p._2 map { x => Set(p._1, x) } }).toSet
      for {
        p <- serviceSetLinks
        as <- getAssociation(p.head, p.last, "")
      }
        writer.write(as.toString)
      writer.write(getFooter)
      writer.flush()
    }
  }

  trait RestrictedPlatformExporter extends PlatformExporter {
    self: HWExporter
      with HWNamer
      with SWExporter
      with SWNamer
      with ServiceSetNamer
      with ServiceSetExporter
      with ServiceExporter
      with PlatformNamer
      with RelationExporter =>

    val extension: Symbol = self match {
      case _ => Symbol("dot")
    }

    /** Export the platform with only the hardware, software and services that
      * are used
      * @param platform
      *   the platform to export
      * @param writer
      *   the implicit writer
      */
    def exportUML(platform: Platform)(implicit writer: Writer): Unit = {
      import platform._
      resetService()
      resetHW()
      resetServiceSet()
      writer.write(getHeader)
      val hwGraph = platform.hardwareGraph()
      val hwLinks = hwGraph.keySet flatMap { k =>
        hwGraph(k) map { x => Set(k, x) }
      }
      val hwComponents = hwLinks.flatten
      for {
        hw <- hwComponents
        hE <- getElement(hw)
      }
        writer.write(hE.toString)

      for {
        p <- hwLinks
        as <- getAssociation(p.head, p.last, "")
      }
        writer.write(as.toString)

      for {
        a <- platform.applications
        as <- getAssociation(a, "")
      }
        writer.write(as.toString)
      val serviceGraph = platform.serviceGraph()
      val serviceLinks = serviceGraph flatMap { p =>
        p._2 map { x => Set(p._1, x) }
      }
      for {
        p <- serviceLinks
        as <- getAssociation(p.head, p.last, "")
      }
        writer.write(as.toString)
      val serviceSetGraph = platform.serviceGraphWithInterfere()
      val serviceSetLinks =
        (serviceSetGraph flatMap { p => p._2 map { x => Set(p._1, x) } }).toSet
      for {
        p <- serviceSetLinks
        as <- getAssociation(p.head, p.last, "")
      }
        writer.write(as.toString)
      writer.write(getFooter)
      writer.flush()
    }

    /** Export the hardware and services used by a given software in the
      * platform
      * @param platform
      *   the platform owing the software
      * @param toPrint
      *   the software to pring
      * @param writer
      *   the implicit writer
      */
    def exportUMLSW(platform: Platform, toPrint: Application)(implicit
        writer: Writer
    ): Unit = {
      import platform._
      resetService()
      resetHW()
      writer.write(getHeader)
      for {cs <- getElement(toPrint)}
        writer.write(cs.toString)

      for {
        (k, v) <- platform.hardwareGraphOf(toPrint)
        if v.nonEmpty
        hw <- v + k
        cd <- getId(hw)
      }
        writer.write(cd.toString)

      for {x <- getAssociation(toPrint, "")}
        writer.write(x.toString)

      for {
        p <- platform.hardwareGraphOf(toPrint)
        x <- p._2
        as <- getAssociation(p._1, x, "")
      }
        writer.write(as.toString)

      for {
        p <- platform.serviceGraphOf(toPrint)
        x <- p._2
        as <- getAssociation(p._1, x, "")
      }
        writer.write(as.toString)

      writer.write(getFooter)
      writer.flush()
    }
  }

  implicit object FullDOT
      extends DOTRelationExporter
      with FullPlatformExporter
      with FullDOTPlatformNamer
      with FullDOTHWNamer
      with FullDOTSWNamer
      with FullDOTServiceNamer
      with FullServiceExporter
      with FullServiceSetNamer
      with FullServiceSetExporter
      with FullHWExporter
      with FullSWExporter {
    val name: Symbol = Symbol("Full")
  }

  implicit object DOTServiceOnly
      extends DOTRelationExporter
      with FullPlatformExporter
      with NullPlatformNamer
      with NullHWNamer
      with NullSWNamer
      with FullDOTServiceNamer
      with FullServiceExporter
      with NullServiceSetNamer
      with NullServiceSetExporter
      with NullHWExporter
      with NullSWExporter {
    val name: Symbol = Symbol("Service")
  }

  implicit object DOTHWAndSWOnly
      extends DOTRelationExporter
      with FullPlatformExporter
      with NullPlatformNamer
      with FullDOTHWNamer
      with FullDOTSWNamer
      with NullServiceNamer
      with NullServiceExporter
      with NullServiceSetNamer
      with NullServiceSetExporter
      with FullHWExporter
      with FullSWExporter {
    val name: Symbol = Symbol("HWAndSW")
  }

  implicit object DOTServiceSet
      extends DOTRelationExporter
      with FullPlatformExporter
      with NullPlatformNamer
      with NullHWNamer
      with NullSWNamer
      with NullServiceNamer
      with NullServiceExporter
      with FullServiceSetNamer
      with FullServiceSetExporter
      with NullHWExporter
      with NullSWExporter {
    val name: Symbol = Symbol("ServiceWithInterfere")
  }

  implicit object DOTRestrictedServiceSet
      extends DOTRelationExporter
      with RestrictedPlatformExporter
      with NullPlatformNamer
      with NullHWNamer
      with NullSWNamer
      with NullServiceNamer
      with NullServiceExporter
      with FullServiceSetNamer
      with FullServiceSetExporter
      with NullHWExporter
      with NullSWExporter {
    val name: Symbol = Symbol("RestrictedServiceWithInterfere")
  }

  implicit object DOTServiceAndSWClosureOnly
      extends DOTRelationExporter
      with RestrictedPlatformExporter
      with NullPlatformNamer
      with NullHWNamer
      with FullDOTSWNamer
      with FullDOTServiceNamer
      with FullServiceExporter
      with NullServiceSetNamer
      with NullServiceSetExporter
      with NullHWExporter
      with FullSWExporter {
    val name: Symbol = Symbol("RestrictedServiceAndSW")
  }

  implicit object DOTHWAndSWClosureOnly
      extends DOTRelationExporter
      with RestrictedPlatformExporter
      with NullPlatformNamer
      with FullDOTHWNamer
      with FullDOTSWNamer
      with NullServiceNamer
      with NullServiceExporter
      with NullServiceSetNamer
      with NullServiceSetExporter
      with FullHWExporter
      with FullSWExporter {
    val name: Symbol = Symbol("RestrictedHWAndSW")
  }

}
