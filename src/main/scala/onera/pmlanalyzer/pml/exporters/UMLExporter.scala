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

    final case class DOTAssociation(left: Int, right: Int, name: String)
      extends Association {
      override def toString: String =
        s"$left -> $right [${
          if (name.nonEmpty) s"label=$name, " else ""
        }arrowhead=none]\n"
    }

    def getHeader: String =
      """digraph hierarchy {
         |\tsize="5,5"
         |\tnode[shape=record,style=filled]
         |\tedge[arrowtail=empty]
         |\t""".stripMargin.replace("\\t", "\u0009")

    def getFooter: String = "}"
  }

  trait DOTNamer {

    private val colorMap = Map(
      1 -> "\"#D6EBA0\"",
      2 -> "\"#EBBFA0\"",
      3 -> "\"#A0E3EB\"",
      4 -> "\"#D4A0EB\"",
      5 -> "\"#769296\"",
      6 -> "\"#606B42\""
    ).withDefaultValue("white")

    final case class DOTElement(name: String, color: String) extends Element {
      override def toString: String =
        s"""$id[label = "{$name}", fillcolor=$color]\n"""
    }

    final case class DOTCluster(
                                 name: String,
                                 subElements: Set[DOTCluster | DOTElement]
                               ) extends Element {

      private val depth: Int = name.count(_ == '_')

      def contains(element: DOTCluster | DOTElement): Boolean =
        subElements.contains(element) || subElements.exists {
          case c: DOTCluster => c.contains(element)
          case _ => false
        }
      override def toString: String =
        s"""subgraph cluster_$name {
           |\tlabel = "$name"
           |\tlabeljust=l
           |\tstyle = filled
           |\tcolor = ${colorMap(depth)}
           |${
          subElements.toSeq
            .sortBy(_.name)
            .map(_.toString.replace(s"${name}_", ""))
            .mkString("\t", "\t", "")
        }
           |\t}
           |""".stripMargin
    }
  }

  trait ServiceSetNamer extends DOTNamer {

    protected val _memoServiceSetId: MHashMap[Set[Service], DOTElement] =
      MHashMap.empty

    def resetServiceSet(): Unit = _memoServiceSetId.clear()

    def getElement(x: Set[Service]): Option[DOTElement]

    def getServiceSetElement(id: Int): Option[DOTElement] =
      _memoServiceSetId.values.find(_.id == id)

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
          x,
          DOTElement(getName(x), "green")
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

    def getAssociation(
                        from: Set[Service],
                        to: Set[Service],
                        tyype: String
                      ): Option[DOTAssociation]
  }

  trait FullServiceSetExporter extends ServiceSetExporter {
    self: ServiceSetNamer with RelationExporter =>

    def getAssociation(
                        from: Set[Service],
                        to: Set[Service],
                        tyype: String
                      ): Option[DOTAssociation] = {
      for {f <- getId(from); t <- getId(to)} yield DOTAssociation(f, t, tyype)
    }
  }

  trait NullServiceSetExporter extends ServiceSetExporter {

    def getAssociation(
                        from: Set[Service],
                        to: Set[Service],
                        tyype: String
                      ): Option[DOTAssociation] = None
  }

  trait ServiceNamer extends DOTNamer {

    protected val _memoServiceId: MHashMap[Service, DOTElement] = MHashMap.empty

    def getServiceElement(id: Int): Option[DOTElement] =
      _memoServiceId.values.find(_.id == id)

    def resetService(): Unit = _memoServiceId.clear()

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

    /** Print the export representation of a link between two services
      * @param from
      *   the origin service
      * @param to
      *   the destination service
      */
    def getAssociation(
                        from: Service,
                        to: Service,
                        tyype: String
                      ): Option[DOTAssociation]
  }

  trait NullServiceNamer extends ServiceNamer {

    def getElement(x: Service): Option[DOTElement] = None
  }

  trait NullServiceExporter extends ServiceExporter {

    def getAssociation(
                        from: Service,
                        to: Service,
                        tyype: String
                      ): Option[DOTAssociation] =
      None
  }

  trait FullDOTServiceNamer extends ServiceNamer {

    def getElement(x: Service): Option[DOTElement] = Some(
      _memoServiceId.getOrElseUpdate(
        x, {
          x match
            case a: ArtificialService => DOTElement(a.name.name, "gray")
            case s => DOTElement(s.name.name, "green")
        }
      )
    )
  }

  trait FullServiceExporter extends ServiceExporter {
    self: ServiceNamer with RelationExporter =>

    def getAssociation(
                        from: Service,
                        to: Service,
                        tyype: String
                      ): Option[DOTAssociation] = {
      for {f <- getId(from); t <- getId(to)} yield DOTAssociation(f, t, tyype)
    }
  }

  trait HWNamer extends DOTNamer {

    protected val _memoHWId: MHashMap[Hardware, DOTElement | DOTCluster] =
      MHashMap.empty

    def getHWElement(id: Int): Option[DOTElement | DOTCluster] =
      _memoHWId.values.find(_.id == id)

    def getContainers(e: DOTElement | DOTCluster): Seq[DOTCluster] = {
      (_memoHWId.values.collect {
        case c: DOTCluster if c.contains(e) => c
      }).toSeq
    }

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
                                                                    pb: Provided[Hardware, Service]
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
              val color = a match {
                case _: Transporter => "mediumpurple1"
                case _: Target => "darkolivegreen1"
                case _: Initiator => "brown1"
              }
              DOTElement(x.name.name, color)
            case c: Composite =>
              val elements =
                for {
                  h <- c.hardware
                  e <- getElement(h)
                } yield e
              DOTCluster(x.name.name, elements)
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

    protected val _memoSWId: MHashMap[Application, DOTElement] =
      MHashMap.empty

    def getSWElement(id: Int): Option[DOTElement] =
      _memoSWId.values.find(_.id == id)

    def resetSW(): Unit = _memoSWId.clear()

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
        _memoSWId.getOrElseUpdate(
          x,
          DOTElement(
            x.name.name,
            "deepskyblue1"
          )
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

  trait PlatformNamer extends DOTNamer {
    self: HWNamer with ServiceNamer with SWNamer with ServiceSetNamer =>

    def reset(): Unit = {
      resetService()
      resetHW()
      resetServiceSet()
      resetSW()
    }

    /** Build the unique id of the platform
      * @param x
      *   the platform
      * @return
      *   the id
      */
    def getId(x: Platform): Option[String]

    def getElement(id: Int): Option[DOTElement | DOTCluster] = {
      getSWElement(id) match
        case Some(value) => Some(value)
        case None =>
          getServiceElement(id) match
            case Some(value) => Some(value)
            case None =>
              getHWElement(id) match
                case Some(value) => Some(value)
                case None => getServiceSetElement(id)
    }
  }

  trait PlatformExporter extends DOTRelationExporter {
    self: PlatformNamer
      with HWNamer
      with ServiceNamer
      with SWNamer
      with ServiceSetNamer =>

    val name: Symbol
    val extension: Symbol

    /** Export the platform as an UML diagram
      * @param platform
      *   the platform to export
      * @param writer
      *   the implicit writer
      */
    def exportUML(platform: Platform)(implicit writer: Writer): Unit

    def exportUML(platform: Platform, associations: Iterable[DOTAssociation])(
      implicit writer: Writer
    ): Unit = {
      import platform.*

      val allClusters = for {
        case c: Composite <- platform.directHardware
        case e: DOTCluster <- getElement(c)
      } yield e

      val elements =
        for {
          a <- associations
          id <- List(a.left, a.right)
          e <- getElement(id)
        } yield e

      val clusters =
        for {
          c <- allClusters
          if elements.exists(e => getContainers(e).contains(c))
        } yield c

      val primaryElements =
        for {
          e <- elements
          if getContainers(e).isEmpty
        } yield e

      for {
        e <- (clusters ++ primaryElements).toSeq.distinct.sortBy(_.name)
      }
        writer.write(s" $e".replace(s"${platform.fullName}_", ""))

      for {
        as <- associations
      }
        writer.write(s" $as")
    }

  }

  trait FullDOTPlatformNamer extends PlatformNamer {
    self: HWNamer with ServiceNamer with SWNamer with ServiceSetNamer =>

    def getId(x: Platform): Option[String] = Some(x.name.name)

  }

  trait NullPlatformNamer extends PlatformNamer {
    self: HWNamer with ServiceNamer with SWNamer with ServiceSetNamer =>

    def getId(x: Platform): Option[String] = None

  }

  trait FullPlatformExporter extends PlatformExporter {
    self: HWExporter
      with SWExporter
      with ServiceSetExporter
      with ServiceExporter
      with RelationExporter
      with PlatformNamer
      with HWNamer
      with ServiceNamer
      with SWNamer
      with ServiceSetNamer =>

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
      reset()
      writer.write(getHeader)

      val hwLinkAssociations =
        for {
          (k, v) <- platform.PLLinkableToPL.edges
          x <- v
          as <- getAssociation(k, x, "")
        } yield as

      val applicationAssociations =
        for {
          a <- platform.applications
          as <- getAssociation(a, "")
        } yield as

      val serviceAssociations =
        for {
          (k, v) <- platform.ServiceLinkableToService.edges
          x <- v
          as <- getAssociation(k, x, "")
        } yield as

      val serviceSetGraph = platform.fullServiceGraphWithInterfere()
      val serviceSetLinks =
        (serviceSetGraph flatMap { p => p._2 map { x => Set(p._1, x) } }).toSet
      val serviceSetAssociations =
        for {
          p <- serviceSetLinks
          as <- getAssociation(p.head, p.last, "")
        } yield as

      exportUML(
        platform,
        hwLinkAssociations
          ++ applicationAssociations
          ++ serviceAssociations
          ++ serviceSetAssociations
      )

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
      with ServiceNamer
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
      reset()
      writer.write(getHeader)
      val hwGraph = platform.hardwareGraph()
      val hwLinks = hwGraph.keySet flatMap { k =>
        hwGraph(k) map { x => Set(k, x) }
      }

      val hardwareAssociations = for {
        p <- hwLinks
        as <- getAssociation(p.head, p.last, "")
      } yield as

      val applicationAssociations = for {
        a <- platform.applications
        as <- getAssociation(a, "")
      } yield as

      val serviceGraph = platform.serviceGraph()
      val serviceLinks = serviceGraph flatMap { p =>
        p._2 map { x => Set(p._1, x) }
      }
      val serviceAssociations = for {
        p <- serviceLinks
        as <- getAssociation(p.head, p.last, "")
      } yield as

      val serviceSetGraph = platform.serviceGraphWithInterfere()
      val serviceSetLinks =
        (serviceSetGraph flatMap { p => p._2 map { x => Set(p._1, x) } }).toSet

      val serviceSetAssociations = for {
        p <- serviceSetLinks
        as <- getAssociation(p.head, p.last, "")
      } yield as

      exportUML(
        platform,
        hardwareAssociations
          ++ applicationAssociations
          ++ serviceAssociations
          ++ serviceSetAssociations
      )

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
      reset()
      writer.write(getHeader)

      val applicationAssociations =
        for {
          x <- getAssociation(toPrint, "")
        } yield x

      val hardwareAssociations = for {
        p <- platform.hardwareGraphOf(toPrint)
        x <- p._2
        as <- getAssociation(p._1, x, "")
      } yield as

      val serviceAssociations = for {
        p <- platform.serviceGraphOf(toPrint)
        x <- p._2
        as <- getAssociation(p._1, x, "")
      } yield as

      exportUML(
        platform,
        applicationAssociations
          ++ hardwareAssociations
          ++ serviceAssociations
      )

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
