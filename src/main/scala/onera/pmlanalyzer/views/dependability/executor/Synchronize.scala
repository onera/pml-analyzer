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

package onera.pmlanalyzer.views.dependability.executor

import onera.pmlanalyzer.views.dependability.model.{
  ConcreteEvent,
  Event,
  SynchroEvent
}

import scala.collection.mutable

private[pmlanalyzer] object Synchronize {

  private var counter = -1

  def freshSyncName(): Symbol = {
    counter += 1
    Symbol(s"synchro$counter")
  }

  private val synchronizations: mutable.Set[SynchroEvent] =
    mutable.Set.empty[SynchroEvent]

  def findSynchroOf(e: ConcreteEvent): Option[SynchroEvent] = {
    synchronizations.find(t => t.synchronizedEvents.contains(e))
  }

  def addSynchro(s: Set[Event]): SynchroEvent = {
    // WARNING synchro operation is transitive ie sync(s1,{a,b}), synch(s2,{b,c} => synch(s3,{a,b,c})
    (for (
        sync <- synchronizations
          .find(s2 => s2.synchronizedEvents.intersect(s).nonEmpty)
      )
      yield {
        val newSync =
          sync.copy(synchronizedEvents = sync.synchronizedEvents.union(s))
        synchronizations -= sync
        synchronizations += newSync
        newSync
      }) getOrElse {
      val r = SynchroEvent(freshSyncName(), s)
      synchronizations += r
      r
    }
  }

  def removeSynchro(s: SynchroEvent): Unit = synchronizations -= s

  def isFireable(s: SynchroEvent): Boolean =
    s.synchronizedEvents.foldLeft(true)((acc, e) =>
      e match {
        case concrete: ConcreteEvent =>
          acc && concrete.owner.fireable(concrete).nonEmpty
        case synchroEvent: SynchroEvent => acc && isFireable(synchroEvent)
      }
    )

  def fireSynchro(s: SynchroEvent): Unit = {
    if (isFireable(s)) {
      s.synchronizedEvents.foreach {
        case concrete: ConcreteEvent =>
          concrete.owner.engage(concrete)
        case synchroEvent: SynchroEvent =>
          fireSynchro(synchroEvent)
      }
      s.synchronizedEvents.foreach {
        case concrete: ConcreteEvent =>
          concrete.owner.update()
        case synchroEvent: SynchroEvent =>
          fireSynchro(synchroEvent)
      }
    } else {
      throw new Exception(s"synchro $s cannot be triggered")
    }
  }
}
