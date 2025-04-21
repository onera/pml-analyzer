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

package onera.pmlanalyzer.pml.model.hardware

import onera.pmlanalyzer.pml.model.*
import onera.pmlanalyzer.pml.model.relations.Relation
import onera.pmlanalyzer.pml.model.service.{Load, Store}
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.model.utils.{
  Context,
  Message,
  Owner,
  ReflexiveInfo
}
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalScenario,
  PhysicalScenarioId,
  PhysicalTransaction,
  PhysicalTransactionId
}
import sourcecode.{Enclosing, File, Line}

import scala.collection.mutable.HashMap as MHashMap
import scala.language.implicitConversions

/** Base class for a platform
 *
 * @see
  *   usage are available in
  *   [[pml.examples.simpleKeystone.SimpleKeystonePlatform]]
  * @param name
  *   the name of the node
 * @param file
  *   the implicit descriptor of the source file where the platform is defined
  * @group hierarchical_class
  */
abstract class Platform(val name: Symbol, line: Line, file: File)
    extends PMLNode(ReflexiveInfo(line, file, Owner.empty))
    with ContainerLike {

  implicit val context: Context = Context.EmptyContext()

  def this(n: Symbol, dummy: Int = 0)(using
      givenLine: Line,
      givenFile: File
  ) = {
    this(n, givenLine, givenFile)
  }

  /** the current owner id becomes the id of the current node
    * @group identifier
    */
  implicit val currentOwner: Owner = Owner(name)

  /** notify the initialisation of a new platform to the companion object
    */
  Platform.add(this)

  /** The full name of a platform is its base name concatenated with the
    * configuration if available
    * @group identifier
    */
  final lazy val fullName: String = currentOwner.toString

  /** Map from the physical transaction id and their service sequence
    * representation computed through an analysis of the platform WARNING: this
    * lazy variable MUST NOT be called during platform object initialisation
    * @group transaction
    */
  final lazy val transactionsByName
      : Map[PhysicalTransactionId, PhysicalTransaction] =
    this.usedTransactions.groupMapReduce(transactionId)(t => t)((l, _) => l)

  /** Set of physical transactions WARNING: this lazy variable MUST NOT be
    * called during platform object initialisation
    * @group transaction
    */
  final lazy val transactions: Set[PhysicalTransactionId] =
    transactionsByName.keySet

  /** Map from the sw to the physical transaction id (default is emptySet)
    * WARNING: this lazy variable MUST NOT be called during platform object
    * initialisation
    * @group transaction
    */
  final lazy val transactionsBySW
      : Map[Application, Set[PhysicalTransactionId]] =
    Application.all.groupMapReduce(a => a)(a => {
      val targetServices = a.targetService
      val initServices = a.hostingInitiators.flatMap(_.services)
      transactionsByName
        .collect({
          case (id, path)
              if targetServices.contains(path.last) && initServices
                .contains(path.head) =>
            id
        })
        .toSet
    })(_ ++ _)

  private val _transactionId =
    collection.mutable.HashMap.empty[PhysicalTransaction, PhysicalTransactionId]
  private val _scenarioId =
    collection.mutable.HashMap.empty[PhysicalScenario, PhysicalScenarioId]

  /** Build the transaction id as "head_last_i" where i is the number of path
    * with the same origin and destination as the one on build (possible when
    * multiple paths in the architecture)
    *
    * @param t
    *   the sequence of services
    * @return
    *   the unique transaction id
    */
  protected final def transactionId(
      t: PhysicalTransaction
  ): PhysicalTransactionId = _transactionId.getOrElseUpdate(
    t, {
      val sameHT =
        _transactionId.keys.count(tp => t.head == tp.head && t.last == tp.last)
      PhysicalTransactionId(Symbol(s"${t.head}_${t.last}_$sameHT"))
    }
  )

  /** Map from the service sequence representation to their id WARNING: this
    * lazy variable MUST NOT be called during platform object initialisation
    * @group transaction
    */
  final lazy val transactionsName
      : Map[PhysicalTransaction, PhysicalTransactionId] =
    transactionsByName.groupMapReduce(_._2)(_._1)((l, _) => l)

  /** Build the scenario id as "t_1|...|t_n"
    *
    * @param s
    *   the set of physical transactions forming the scenario
    * @return
    *   the unique id of the scenario
    */
  final def scenarioId(s: PhysicalScenario): PhysicalScenarioId =
    _scenarioId.getOrElseUpdate(
      s, {
        PhysicalScenarioId(
          Symbol(s.map(t => t.id.name).toArray.sorted.mkString("|"))
        )
      }
    )

  given ToServicePath[PhysicalTransactionId] with {
    def apply(x: PhysicalTransactionId): Set[PhysicalTransaction] = Set(
      transactionsByName(x)
    )
  }
}

/** Static methods of Platform
  * @group utilFun
  */
object Platform {

  private val _memo: MHashMap[Symbol, Platform] = MHashMap.empty

  def get(id: Symbol): Option[Platform] = _memo.get(id)

  def add(v: Platform): Unit = {
    for { l <- _memo.get(v.name) } {
      println(
        Message.errorMultipleInstantiation(
          s"$l in ${l.sourceFile} at line ${l.lineInFile}",
          s"${v.sourceFile} at line ${v.lineInFile}"
        )
      )
    }
    _memo.addOne((v.name, v))
  }

  def getOrElseUpdate(name: Symbol, v: => Platform): Platform = {
    for { l <- _memo.get(name) } {
      println(
        Message.errorMultipleInstantiation(
          s"$l in ${l.sourceFile} at line ${l.lineInFile}",
          s"${v.sourceFile} at line ${v.lineInFile}"
        )
      )
    }
    _memo.getOrElseUpdate(name, v)
  }

  /** Provide all the platforms defined in the project
    * @return
    *   the set of platforms
    */
  def all: Set[Platform] = _memo.values.toSet
}
