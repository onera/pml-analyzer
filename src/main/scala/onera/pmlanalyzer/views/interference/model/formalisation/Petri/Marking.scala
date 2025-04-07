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

// package onera.pmlanalyzer.views.interference.model.formalisation.Petri

// import scala.collection.mutable
// import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Place
// import math.Ordering.Implicits.infixOrderingOps
// import math.Ordered.orderingToOrdered

// object Marking {

//   def apply(elems: (Place, Int)*): Marking = mutable.HashMap.from(elems)

//   def empty(): Marking = mutable.HashMap.empty

//   given Ordering[Marking] with {
//     def compare(x: Marking, y: Marking): Int = {
//       if (x.keySet ++ y.keySet == x.keySet) {
//         val compare = for {
//           k <- y.keySet
//         } yield x(k).compare(y(k))
//         if (compare.size == 1)
//           compare.head
//         else -1
//       } else -1
//     }
//   }
// }

// object MarkingApp extends App {

//   import Marking.given

//   val m1 = Marking(Place(1) -> 2, Place(0) -> 0)
//   val m2 = Marking(Place(1) -> 1, Place(0) -> 0)
//   val m3 = Marking(Place(0) -> 0)

//   println(m1 > m2)
//   println(m1 <= m2)
//   println(m3 > m2)
// }
