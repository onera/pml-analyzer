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

package onera.pmlanalyzer.pml.operators

import onera.pmlanalyzer.pml.model.hardware.{Hardware, Initiator}
import onera.pmlanalyzer.pml.model.relations.{LinkRelation, ProvideRelation, UseRelation}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.Application
import sourcecode.{File, Line}

/** Base trait for deactivation operations
  *
  * @tparam T
  *   the type of the deactivatable component
  */
trait Deactivate[-T] {
  def apply(a: T)(using line: Line, file: File): Unit
}

/** Extension methods and inferences rules
  */
object Deactivate {

  def apply[T](using m: Deactivate[T]): Deactivate[T] = m

  /** ------------------------------------------------------------------------------------------------------------------
    * EXTENSION METHODS
    * ---------------------------------------------------------------------------------------------------------------
    */

  /** If an element x of type T is deactavitable then the operator can be used
    * as follows
    * {{{
    *   x.deactivate
    * }}}
    *
    * @note
    *   currently any [[pml.model.hardware.Hardware]] or
    *   [[pml.model.software.Application]] is deactivatable and the deactivation
    *   can be made only within a platform container
    */

  trait Ops {
    extension [T: Deactivate](a: T) {
      def deactivated(using line: Line, file: File): Unit = Deactivate[T](a)
    }
  }

  /** ------------------------------------------------------------------------------------------------------------------
    * INFERENCE RULES
    * ---------------------------------------------------------------------------------------------------------------
    */

  /** An application can be deactivated by removing all the services its uses
    * @return
    *   the implementation of application deactivation
    */
  given (using
      uSL: UseRelation[Application, Service],
      uAS: UseRelation[Application, Initiator]
  ): Deactivate[Application] with {
    def apply(a: Application)(using line: Line, file: File): Unit = {
      uSL.remove(a)
      uAS.remove(a)
    }
  }

  /** A physical component can be deactivated by removing all the services its
    * provides, the software using it and the services connected to his
    * @return
    *   the implementation of physical component deactivation
    */
  given (using
      l: LinkRelation[Service],
      p: ProvideRelation[Hardware, Service],
      u: UseRelation[Application, Service]
  ): Deactivate[Hardware] with {
    def apply(a: Hardware)(using line: Line, file: File): Unit = {
      p(a) foreach { s =>
        {
          l.remove(s)
          u.inverse(s) foreach {
            u.remove(_, s)
          }
        }
      }
      p.remove(a)
    }
  }
}
