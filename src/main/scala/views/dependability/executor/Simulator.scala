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

package views.dependability.executor

import views.dependability.model._

import scala.collection.mutable

object Simulator {

  var time: Int = 0

  val events: mutable.Set[ConcreteEvent] = mutable.Set.empty

  def addEvent(e: ConcreteEvent): Unit = events += e

  def fireable: Set[Event] = events.filter(e => e.owner.fireable(e).isDefined).toSet

  def fireEvent(e: Event): Unit = {
    e match {
      case sync@SynchroEvent(_, synchronizedEvents) =>
        if (synchronizedEvents.nonEmpty) {
          Synchronize.fireSynchro(sync)
        }
      case concreteEvent: ConcreteEvent =>
        Synchronize.findSynchroOf(concreteEvent) match {
          case Some(sync) => Synchronize.fireSynchro(sync)
          case None => concreteEvent.owner.fire(concreteEvent)
        }
    }

    val immediate = fireable.collect { case e@DetermisticEvent(_, _, 0) => e }
    if (immediate.isEmpty) {
      val det = fireable.collect { case e@DetermisticEvent(_, _, i) if i != 0 => e }
      if (det.nonEmpty) {
        val min = det.minBy(_.delay).delay
        det.filter(_.delay == min) match {
          case minDet if minDet.size == 1 =>
            time += minDet.head.delay
            fireEvent(minDet.head)
          case minDet =>
            // throw new Exception(s"choice between instantaneous events $minDet")
            val synchroEvent = Synchronize.addSynchro(WorstCaseSchedule.schedule(minDet).toSet)
            fireEvent(synchroEvent)
            Synchronize.removeSynchro(synchroEvent)
        }
      }
    }
    else if (immediate.size == 1)
      fireEvent(immediate.head)
    else {
      //          throw new Exception(s"choice between instantaneous events $s")
      val synchroEvent = SynchroEvent(Symbol("schedule"), WorstCaseSchedule.schedule(immediate).toSet)
      fireEvent(synchroEvent)
      Synchronize.removeSynchro(synchroEvent)
    }
  }
}
