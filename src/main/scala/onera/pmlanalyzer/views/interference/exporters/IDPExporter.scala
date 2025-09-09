/*******************************************************************************
 * Copyright (c)  2023. ONERA
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

package onera.pmlanalyzer.views.interference.exporters

import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.hardware.{
  Initiator,
  Platform,
  Target,
  Transporter
}
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.pml.operators.*
import scalaz.Memo.immutableHashMapMemo
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.*

import java.io.{FileWriter, Writer}
import scala.collection.mutable.HashMap as MHashMap

object IDPExporter {

  trait Ops {

    implicit class IdpExporterOps(
        platform: Platform with InterferenceSpecification
    ) {
      def exportAsIDP()(implicit exporter: IDPPlatformExporter.type): Unit = {
        val writer = new FileWriter(
          FileManager.exportDirectory.getFile(platform.fullName + ".pia")
        )
        exporter.exportIDP(platform)(writer)
        writer.close()
      }
    }
  }

  implicit object IDPPlatformExporter {

    // memoization of path id
    private val _memoPathId = MHashMap.empty[AtomicTransaction, String]

    // path id is formatted as "$head_$last_$i_path" where i is the number of path with the same
    // origin and destination as the one on build (possible when multiple paths in the architecture)
    def pathId(p: AtomicTransaction): String = _memoPathId.getOrElseUpdate(
      p, {
        val sameHT =
          _memoPathId.keys.count(k => k.head == p.head && k.last == p.last)
        s"${p.head.name.name}_${p.last.name.name}_${sameHT}_path"
      }
    )

    /** Compute all the path from a given element to the leaf services This
      * methods handle cyclic graphs by simply cutting the loop when traversing
      * it
      * @param from
      *   the initial service
      * @param graph
      *   the edges of the graph
      * @tparam A
      *   the type of the parent nodes
      * @tparam B
      *   the type of the son nodes
      * @return
      *   all the possible paths
      */
    def pathsIn[A, B <: A](from: A, graph: Map[A, Set[B]]): Set[Path[A]] = {

      /** This function value compute the path from a node of the graph to its
        * leaf nodes (first element of the Pair). A set of visited nodes is also
        * provided (second element of the Pair) to cut cycles The result are
        * memoized to avoid multiple computation of the paths
        */
      lazy val _paths: ((A, Set[A])) => Set[Path[A]] = immutableHashMapMemo {
        s =>
          if (s._2.contains(s._1)) {
            println(Message.cyclicGraphWarning)
            Set(Nil)
          } else if (!graph.contains(s._1) || graph(s._1).isEmpty)
            Set(Nil)
          else
            graph(s._1).flatMap(next =>
              _paths((next, s._2 + s._1)) map { next +: _ }
            )
      }

      // remove empty paths (i.e. from is not connected to anyone in the graph) and add from as path head
      _paths((from, Set.empty)) collect {
        case p if p.nonEmpty => from +: p
      }
    }

    def exportIDP(
        platform: Platform with InterferenceSpecification
    )(implicit writer: Writer): Unit = {
      import platform._

      writer.write(s"""|/* --------------------------------------------- */
           |// PIA MODEL GENERATED FOR ${platform.name.name}
           |/* --------------------------------------------- */
           |
           |""".stripMargin)

      // the hw connection graph only contains physical connection used by at least one transaction
      val hwGraph = platform.hardwareGraph()
      val hwLinks = hwGraph flatMap { p => p._2 map { x => (p._1, x) } }
      val hwComponents = hwLinks.flatMap { p => Set(p._1, p._2) }.toSet

      writer.write("/* Targets components used by at least one software */\n")
      writer.write(
        hwComponents
          .collect { case t: Target => t }
          .map(_.name.name)
          .toList
          .sorted
          .mkString("Targets = {\n\t", ";\n\t", "\n}\n\n")
      )

      writer.write(
        "/* Transporter components used by at least one software */\n"
      )
      writer.write(
        hwComponents
          .collect { case t: Transporter => t }
          .map(_.name.name)
          .toList
          .sorted
          .mkString("Transporters= {\n\t", ";\n\t", "\n}\n\n")
      )

      writer.write("/* Smart Initiators used by at least one software */\n")
      writer.write(
        hwComponents
          .collect { case s: Initiator => s.name.name }
          .toList
          .sorted
          .mkString("SmartInitiators = {\n\t", ";\n\t", "\n}\n\n")
      )

      writer.write("/* Initiator number */\n")
      writer.write(s"Initiator_Number = ${hwComponents
          .count { case _: Initiator => true; case _ => false }}\n\n")

      val transactions = platform.transactionsByName

      // the interference analysis do not consider copy services, so these services must be discarded
      val services = transactions.values.toSet.flatten

      // utility used by interference analysis to model the absence of transaction
      val nopServices = hwComponents.collect { case i: Initiator =>
        i.name.name -> s"${i.name.name}_NOP"
      }

      writer.write("/* Services used by at least one software */\n")
      writer.write(
        (services.map(_.name.name) ++ nopServices.map(_._2)).toList.sorted
          .mkString("Services = {\n\t", ";\n\t", "\n}\n\n")
      )

      writer.write("/* Type of used services */\n")
      writer.write(
        (services.map { s =>
          s"${s.name.name} -> ${s.typeName.name.toUpperCase}"
        } ++
          nopServices.map(kv => s"${kv._2} -> NOP")).toList.sorted
          .mkString("ServiceType = {\n\t", ";\n\t", "\n}\n\n")
      )

      writer.write("/* Provider of used services */\n")
      writer.write(
        (services.flatMap { s =>
          context.PLProvideService.inverse(s) map { owner =>
            s"${s.name.name} -> ${owner.name.name}"
          }
        } ++
          nopServices.map(kv => s"${kv._2} -> ${kv._1}")).toList.sorted
          .mkString("ProvidedBy = {\n\t", ";\n\t", "\n}\n\n")
      )

      val pathNames = platform match {
        case l: TransactionLibrary =>
          val names = l.transactionUserName
          transactions.keySet.map { k =>
            k -> {
              if (names(k).isEmpty) Set(s"${k}_path")
              else names(k).map(t => s"${t}_path")
            }
          }.toMap
        case _ => transactions.keySet.map { k => k -> Set(s"${k}_path") }.toMap
      }

      writer.write("/* Services paths used by at least one transaction */\n")
      writer.write(
        (pathNames.values.flatten ++
          nopServices.map(kv => s"${kv._2}_${kv._2}_path")).toList.sorted
          .mkString("Paths = {\n\t", ";\n\t", "\n}\n\n")
      )

      // the successors of a nop transaction is always nop
      val nopSuccessor =
        nopServices.map(kv => s"${kv._2}_${kv._2}_path, ${kv._2} -> ${kv._2}")

      writer.write("/* Successor relation for paths */\n")
      writer.write(
        (pathNames
          .transform { (k, v) =>
            v flatMap { name =>
              transactions(k).sliding(2) map { l =>
                s"$name, ${l.head} -> ${l.last}"
              }
            }
          }
          .values
          .flatten ++
          nopSuccessor).toList.sorted
          .mkString("NextPathService = {\n\t", ";\n\t", "\n}\n\n")
      )

      val transactionNames = platform match {
        case l: TransactionLibrary =>
          val names = l.transactionUserName
          transactions.keySet.map { k =>
            k -> {
              if (names(k).isEmpty) Set(k.id.name) else names(k).map(_.id.name)
            }
          }.toMap
        case _ => transactions.keySet.map { k => k -> Set(k.id.name) }.toMap
      }

      writer.write("/* Single transactions triggered by software */\n")
      writer.write(
        (transactionNames.values.flatten ++
          nopServices.map(kv => s"${kv._2}_${kv._2}")).toList.sorted
          .mkString("SingleTransactions = {\n\t", ";\n\t", "\n}\n\n")
      )

      writer.write("/* Head of service path of the single transaction */\n")
      writer.write(
        (transactionNames
          .transform { (k, v) =>
            v map { name => s"$name -> ${transactions(k).head.name.name}" }
          }
          .values
          .flatten ++
          nopServices.map { kv =>
            s"${kv._2}_${kv._2} -> ${kv._2}"
          }).toList.sorted
          .mkString("TransactionInitiatorService = {\n\t", ";\n\t", "\n}\n\n")
      )

      writer.write("/* Target service of the single transaction */\n")
      writer.write(
        (transactionNames
          .transform { (k, v) =>
            v map { name => s"$name -> ${transactions(k).last.name.name}" }
          }
          .values
          .flatten ++
          nopServices.map { kv =>
            s"${kv._2}_${kv._2} -> ${kv._2}"
          }).toList.sorted
          .mkString("TransactionTargetService = {\n\t", ";\n\t", "\n}\n\n")
      )

      writer.write("/* Path of the single transaction */\n")
      writer.write(
        (transactions.keySet.flatMap { k =>
          transactionNames(k) flatMap { trName =>
            pathNames(k) map { pName => s"$trName -> $pName" }
          }
        } ++
          nopServices.map { kv =>
            s"${kv._2}_${kv._2} -> ${kv._2}_${kv._2}_path"
          }).toList.sorted
          .mkString("TransactionPath = {\n\t", ";\n\t", "\n}\n\n")
      )

      writer.write(
        s"""
          |/* ----------------------------- */
          |// Specific to Interference View
          |/* ----------------------------- */
          |
          |// Transaction "t" with which topological interference are discarded
          |TransparentTransactions = ${transactions.keySet
            .filter { platform.isTransparentTransaction }
            .flatMap { transactionNames }
            .toList
            .sorted
            .mkString("{\n\t", ";\n\t", "\n}\n\n")}
          |
          |// Transaction "t" affects a service "s" not contained in the transaction path
          |// (non topologically explicable dependency)
          |TransactionAffect = ${transactions.keySet
            .flatMap { k =>
              platform
                .transactionInterfereWith(k)
                .flatMap(s => transactionNames(k) map { name => s"$name,$s" })
            }
            .toList
            .sorted
            .mkString("{\n\t", ";\n\t", "\n}\n\n")}
          |
          |// Transaction "t" does not affect a service "s" contained in the transaction path
          |// (non topologically explicable tolerance)
          |TransactionNotAffect  = ${transactions.keySet
            .flatMap(k =>
              platform
                .transactionNotInterfereWith(k)
                .flatMap(s => transactionNames(k) map { name => s"$name,$s" })
            )
            .toList
            .sorted
            .mkString("{\n\t", ";\n\t", "\n}\n\n")}
          |
          |// Two initiators "i" and "i'" cannot trigger transactions simultaneously
          |InitiatorsAreExclusive = ${platform.initiators
            .subsets(2)
            .collect {
              case si if platform.finalInterfereWith(si.head, si.last) =>
                si.mkString(",")
            }
            .toList
            .sorted
            .mkString("{\n\t", ";\n\t", "\n}\n\n")}
          |
          |// Two transactions "t" and "t'" cannot be triggered simultaneously
          |TransactionsAreExclusive  = ${transactions.keySet
            .subsets(2)
            .filter { s => platform.finalExclusive(s.head, s.last) }
            .flatMap(s =>
              transactionNames(s.head) flatMap { leftName =>
                transactionNames(s.last) map { rightName =>
                  s"$rightName, $leftName"
                }
              }
            )
            .toList
            .sorted
            .mkString("{\n\t", ";\n\t", "\n}\n\n")}
          |
          |// Two services "s" and "s'" provided by the same component can be used simultaneously
          |Parallel = ${services
            .subsets(2)
            .filter { ss =>
              ss.head.hardwareOwner
                .intersect(ss.last.hardwareOwner)
                .nonEmpty && !platform.finalInterfereWith(ss.head, ss.last)
            }
            .map { ss => ss.mkString(",") }
            .toList
            .sorted
            .mkString("{\n\t", ";\n\t", "\n}\n\n")}
          |
          |// Two services "s" and "s'" are equivalent
          |Equivalence = ${platform
            .serviceEquivalenceClasses(services)
            .filter { _.size >= 2 }
            .flatMap { _.sliding(2) }
            .map { _.mkString(",") }
            .toList
            .sorted
            .mkString("{\n\t", ";\n\t", "\n}\n\n")}
        """.stripMargin
      )
      writer.flush()
      writer.close()
    }
  }

}
