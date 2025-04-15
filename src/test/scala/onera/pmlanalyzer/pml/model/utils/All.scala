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

package onera.pmlanalyzer.pml.model.utils

import onera.pmlanalyzer.pml.model.hardware.{
  Initiator,
  Platform,
  SimpleTransporter,
  Target,
  Transporter,
  Virtualizer
}
import onera.pmlanalyzer.pml.model.software.{Application, Data}

trait All[T] {
  def apply(): Set[T]
}

object All {
  trait Instances {
    self: Platform =>

    given All[Target] with {
      def apply(): Set[Target] = Target.all
    }

    given All[Transporter] with {
      def apply(): Set[Transporter] = SimpleTransporter.all ++ Virtualizer.all
    }

    given All[Initiator] with {
      def apply(): Set[Initiator] = Initiator.all
    }

    given All[Application] with {
      def apply(): Set[Application] = Application.all
    }

    given All[Data] with {
      def apply(): Set[Data] = Data.all
    }

  }
}
