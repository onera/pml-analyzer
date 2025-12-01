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

package onera.pmlanalyzer.views.dependability.exporters

import onera.pmlanalyzer.views.dependability.model.{
  Component,
  Direction,
  Fire,
  Variable,
  VariableId,
  System as DepSystem
}
import onera.pmlanalyzer.*
import onera.pmlanalyzer.views.dependability.operators.{
  IsCriticalityOrdering,
  IsFinite,
  IsShadowOrdering
}

private[pmlanalyzer] trait BasicOperationCeciliaExporter {
  self: TypeCeciliaExporter =>

  def pathName(x: DepSystem, c: Component): List[String] =
    x.context.componentOwner.get(c) match {
      case Some(up) => pathName(x, up) :+ c.id.toString
      case None     => c.id.toString :: Nil
    }

  def variablePathName(x: DepSystem, v: Variable[_]): String = {
    s"${pathName(x, x.context.portOwner(v.id)).mkString(".")}.$v"
  }

  def mkVariableName(
      v: VariableId,
      tyype: CeciliaType,
      orientation: Orientation,
      arity: Int
  ): List[Flow] = arity match {
    case 0 => Nil
    case 1 => Flow(v.name, tyype, orientation) :: Nil
    case n =>
      (0 until n).map(i => Flow(Symbol(s"$v$i"), tyype, orientation)).toList
  }

  def configurableCstModel[T: IsFinite: IsCriticalityOrdering]
      : ComponentModel = {
    val tyype = typeModel[T]
    val output = Flow(Symbol("o"), tyype, Out)
    val state = State(Symbol("s"), tyype, min[T].name.name)
    ComponentModel(
      Symbol("constant"),
      SubFamilyFolder(tyype.name, PhylogFolder.fmOperatorsFamilyFolder),
      GenericImage.sourceBlueCircle,
      Nil,
      output :: Nil,
      Nil,
      state :: Nil,
      s"assert\n$output = $state;"
    )
  }

  def configurableCstModel[K: IsFinite, V: IsFinite: IsCriticalityOrdering]
      : ComponentModel = {
    val tyype = typeModel[K, V]
    val output = Flow(Symbol("o"), tyype, Out)
    val states = tyype.fields.map(field =>
      State(field.name, field.tyype, min[V].name.name)
    )
    ComponentModel(
      Symbol("constant"),
      SubFamilyFolder(tyype.name, PhylogFolder.fmOperatorsFamilyFolder),
      GenericImage.sourceBlueCircle,
      Nil,
      output :: Nil,
      Nil,
      states,
      s"assert\n${tyype.fields.map(field => s"$output^$field = $field;").mkString("\n")}"
    )
  }

  def equalModel[T: IsFinite]: ComponentModel = {
    val tyype = typeModel[T]
    ComponentModel(
      Symbol("equal"),
      SubFamilyFolder(tyype.name, PhylogFolder.fmOperatorsFamilyFolder),
      GenericImage.equalBlue,
      GenericImage.equalGreen :: GenericImage.equalRed :: Nil,
      Flow(Symbol("i1"), tyype, In) :: Flow(Symbol("i2"), tyype, In) :: Flow(
        Symbol("o"),
        CeciliaBoolean,
        Out
      ) :: Nil,
      Nil,
      Nil,
      """assert
         |o = (i1 = i2);
         |icone = (if o then 1 else 2);
       """.stripMargin
    )
  }

  def switchModel[T: IsFinite: IsCriticalityOrdering]: ComponentModel = {
    val tyype = typeModel[T]
    ComponentModel(
      Symbol("switch"),
      SubFamilyFolder(tyype.name, PhylogFolder.fmOperatorsFamilyFolder),
      GenericImage.selectBlue,
      GenericImage.select1Green :: GenericImage.select1Red :: GenericImage.select2Green :: GenericImage.select2Red :: Nil,
      Flow(Symbol("select1"), CeciliaBoolean, In) :: Flow(
        Symbol("i1"),
        tyype,
        In
      ) :: Flow(Symbol("i2"), tyype, In) :: Flow(
        Symbol("o"),
        tyype,
        Out
      ) :: Nil,
      Nil,
      Nil,
      s"""assert
         |o = (if select1 then i1 else i2);
         |icone = case {
         |  select1 and o = ${min[T].name.name} : 1,
         |  select1 : 2,
         |  not select1 and o = ${min[T].name.name} : 3,
         |  else 4
         |};
       """.stripMargin
    )
  }

  def preModel[T: IsFinite: IsCriticalityOrdering]: ComponentModel = {
    val tyype = typeModel[T]
    ComponentModel(
      Symbol("pre"),
      SubFamilyFolder(tyype.name, PhylogFolder.fmOperatorsFamilyFolder),
      GenericImage.preBlue,
      GenericImage.preGreen :: GenericImage.preRed :: Nil,
      Flow(Symbol("currentValue"), tyype, In) :: Flow(
        Symbol("preValue"),
        tyype,
        Out
      ) :: Nil,
      DeterministicEventModel(Symbol("epsilon")) :: Nil,
      State(Symbol("storedValue"), tyype, min[T].name.name) :: Nil,
      s"""trans
         |currentValue != storedValue |- epsilon -> storedValue:=currentValue;
         |
         |assert
         |preValue = storedValue;
         |
         |icone = (if storedValue = ${min[T].name.name} then 1 else 2);
       """.stripMargin
    )
  }

  private val _bestMemo = collection.mutable.HashMap
    .empty[Set[String], (String, Seq[String], SubComponent)]

  def mkBestSub[T: IsFinite: IsCriticalityOrdering](
      l: Set[String]
  ): (String, Seq[String], SubComponent) = _bestMemo.getOrElseUpdate(
    l, {
      val model = BestModelHelper[T](l.size)
      val uuid = _bestMemo.keys.count(p => p.size == l.size)
      val subName = s"${model.name.name}$uuid"
      val worstAssertions = model.inputs.map { f => s"$subName.$f" } zip l map (
        p => s"${p._1} = ${p._2};"
      )
      (
        s"$subName.${model.result}",
        worstAssertions,
        SubComponent(Symbol(subName), model.model)
      )
    }
  )

  private final case class BestModelHelper[T: IsFinite: IsCriticalityOrdering](
      size: Int
  ) {
    val tyype: EnumeratedType = typeModel[T]
    val inputs: List[Flow] =
      (1 to size).map(i => Flow(Symbol(s"i$i"), tyype, In)).toList
    val operatorModel: OperatorModel = minOperator[T](size)
    val result: Flow = Flow(Symbol("o"), tyype, Out)
    val name: Symbol = Symbol(s"best$size${nameOf[T].name}")
    val model: ComponentModel = ComponentModel(
      name,
      SubFamilyFolder(tyype.name, PhylogFolder.fmOperatorsFamilyFolder),
      GenericImage.maxBlue,
      GenericImage.maxGreen :: GenericImage.maxRed :: Nil,
      inputs :+ result,
      Nil,
      Nil,
      s"""assert
         |o = ${operatorModel.output}${inputs.mkString("(", ",", ")")};
         |icone = (if o = ${min[T].name.name} then 1 else 2);
       """.stripMargin
    )
  }

  def minOperator[T: IsFinite: IsCriticalityOrdering](
      size: Int
  ): OperatorModel = {
    val tyype = typeModel[T]
    val inputs = (1 to size).map(i => Flow(Symbol(s"i$i"), tyype, In)).toList
    val output = Flow(Symbol(s"min$size${nameOf[T].name}"), tyype, Out)
    OperatorModel(
      PhylogFolder.genericOperatorFolder,
      inputs,
      output,
      s"""assert
         |$output = case {
         |${allOf[T].toList.sorted
          .map(fm =>
            inputs
              .map(f => s"$f = ${fm.name.name}")
              .mkString(" or ") + s": ${fm.name.name},"
          )
          .mkString("", "\n", s"\nelse ${noneOf[T].name.name}")}
         |};
       """.stripMargin
    )
  }

  private val _worstMemo = collection.mutable.HashMap
    .empty[Set[String], (String, Seq[String], SubComponent)]

  def mkWorstSub[T: IsFinite: IsCriticalityOrdering](
      l: Set[String]
  ): (String, Seq[String], SubComponent) = _worstMemo.getOrElseUpdate(
    l, {
      val model = WorstModelHelper[T](l.size)
      val uuid = _worstMemo.keys.count(p => p.size == l.size)
      val subName = s"${model.name.name}$uuid"
      val worstAssertions = model.inputs.map { f => s"$subName.$f" } zip l map (
        p => s"${p._1} = ${p._2};"
      )
      (
        s"$subName.${model.result}",
        worstAssertions,
        SubComponent(Symbol(subName), model.model)
      )
    }
  )

  private final case class WorstModelHelper[T: IsFinite: IsCriticalityOrdering](
      size: Int
  ) {
    private val tyype = typeModel[T]
    val inputs: List[Flow] =
      (1 to size).map(i => Flow(Symbol(s"i$i"), tyype, In)).toList
    val result: Flow = Flow(Symbol("o"), tyype, Out)
    val name: Symbol = Symbol(s"worst$size${nameOf[T].name}")
    val model: ComponentModel = ComponentModel(
      name,
      SubFamilyFolder(tyype.name, PhylogFolder.fmOperatorsFamilyFolder),
      GenericImage.maxBlue,
      GenericImage.maxGreen :: GenericImage.maxRed :: Nil,
      inputs :+ result,
      Nil,
      Nil,
      s"""assert
         |$result = case {
         |${allOf[T].toList.sorted.reverse
          .map(fm =>
            inputs
              .map(f => s"${f.name.name} = ${fm.name.name}")
              .mkString(" or ") + s": ${fm.name.name},"
          )
          .mkString("", "\n", s"\nelse ${noneOf[T]}")}
         |};
       """.stripMargin
    )
  }

  def maxOperator[T: IsFinite: IsCriticalityOrdering](
      size: Int
  ): OperatorModel = {
    val tyype = typeModel[T]
    val inputs = (1 to size).map(i => Flow(Symbol(s"i$i"), tyype, In)).toList
    val output = Flow(Symbol(s"max$size${nameOf[T].name}"), tyype, Out)
    OperatorModel(
      PhylogFolder.genericOperatorFolder,
      inputs,
      output,
      s"""assert
         |$output = case {
         |${allOf[T].toList.sorted.reverse
          .map(fm =>
            inputs
              .map(f => s"${f.name.name} = ${fm.name.name}")
              .mkString(" or ") + s": ${fm.name.name},"
          )
          .mkString("", "\n", s"\nelse ${noneOf[T]}")}
         |};
       """.stripMargin
    )
  }

  def mkContainerShadowSub[T: IsFinite: IsShadowOrdering](
      name: Symbol,
      newMode: String,
      containerMode: String
  ): (String, Seq[String], SubComponent) = {
    val model = new ContainerShadowHelper[T]()
    val worstAssertions =
      s"${name.name}.${model.newMode} = $newMode;" :: s"${name.name}.${model.containerMode} = $containerMode;" :: Nil
    (
      s"${name.name}.${model.output}",
      worstAssertions,
      SubComponent(name, model.model)
    )
  }

  private class ContainerShadowHelper[T: IsFinite: IsShadowOrdering] {
    private val tyype = typeModel[T]
    val newMode: Flow = Flow(Symbol("new"), tyype, In)
    val containerMode: Flow = Flow(Symbol("container"), tyype, In)
    val output: Flow = Flow(Symbol("o"), tyype, Out)
    private val impacted = allOf[T]
      .map(container =>
        container -> allOf[T]
          .filter(current => current.containerShadow(container) == container)
      )
      .filter(_._2.nonEmpty)
    val model: ComponentModel = ComponentModel(
      Symbol(s"containerShadow${nameOf[T].name}"),
      SubFamilyFolder(tyype.name, PhylogFolder.fmOperatorsFamilyFolder),
      GenericImage.maxBlue,
      GenericImage.maxGreen :: GenericImage.maxRed :: Nil,
      newMode :: containerMode :: output :: Nil,
      Nil,
      Nil,
      s"""assert
         |$output = case {
         |${impacted
          .map(p =>
            "(" + p._2
              .map(current => s"$newMode = $current")
              .mkString(
                "(",
                " or ",
                ")"
              ) + s" and $containerMode = ${p._1.name.name})"
          )
          .mkString(" or ")} : $containerMode,
         |else $newMode
         |};
       """.stripMargin
    )
  }

  def containerShadowOperator[T: IsFinite: IsShadowOrdering]: OperatorModel = {
    val tyype = typeModel[T]
    val newMode = Flow(Symbol("new"), tyype, In)
    val containerMode = Flow(Symbol("container"), tyype, In)
    val output = Flow(Symbol(s"containerShadow${nameOf[T].name}"), tyype, Out)
    val impacted = allOf[T]
      .map(container =>
        container -> allOf[T]
          .filter(current => current.containerShadow(container) == container)
      )
      .filter(_._2.nonEmpty)
    OperatorModel(
      PhylogFolder.genericOperatorFolder,
      newMode :: containerMode :: Nil,
      output,
      s"""assert
         |$output = case {
         |${impacted
          .map(p =>
            "(" + p._2
              .map(current => s"$newMode = $current")
              .mkString(
                "(",
                " or ",
                ")"
              ) + s" and $containerMode = ${p._1.name.name})"
          )
          .mkString(" or ")} : $containerMode,
         |else $newMode
         |};
       """.stripMargin
    )
  }

  def inputShadowOperator[T: IsFinite: IsShadowOrdering]: OperatorModel = {
    val tyype = typeModel[T]
    val newMode = Flow(Symbol("new"), tyype, In)
    val containerMode = Flow(Symbol("container"), tyype, In)
    val output = Flow(Symbol(s"inputShadow${nameOf[T].name}"), tyype, Out)
    val impacted = allOf[T]
      .map(container =>
        container -> allOf[T]
          .filter(current => current.inputShadow(container) == container)
      )
      .filter(_._2.nonEmpty)
    OperatorModel(
      PhylogFolder.genericOperatorFolder,
      newMode :: containerMode :: Nil,
      output,
      s"""assert
         |$output = case {
         |${impacted
          .map(p =>
            "(" + p._2
              .map(current => s"$newMode = $current")
              .mkString(
                "(",
                " or ",
                ")"
              ) + s" and $containerMode = ${p._1.name.name})"
          )
          .mkString(" or ")} : $containerMode,
         |else $newMode
         |};
       """.stripMargin
    )
  }

  final case class WorstSchedulerTopHelper(size: Int) {
    val sonDirection: List[Flow] = (1 to size)
      .map(i => Flow(Symbol(s"sonDirection$i"), typeModel[Direction], In))
      .toList
    val fireOrders: List[Flow] = (1 to size)
      .map(i => Flow(Symbol(s"sonFire$i"), typeModel[Fire], Out))
      .toList
    private val fireOrdersAssertions = fireOrders
      .zip(sonDirection)
      .map(p =>
        s"""${p._1} = case {
         |${p._2} = ${Direction.Degradation} : ${Fire.Apply},
         |${sonDirection
            .filterNot(_ == p._2)
            .map(other => s"$other = ${Direction.Degradation}")
            .mkString(" or ")} : ${Fire.Wait},
         |else ${Fire.No}
         |};""".stripMargin
      )
    val model: ComponentModel = ComponentModel(
      Symbol(s"worstScheduler$size"),
      SubFamilyFolder(
        typeModel[Fire].name,
        PhylogFolder.phylogComponentFolder
      ),
      GenericImage.maxBlue,
      GenericImage.maxGreen :: GenericImage.maxRed :: Nil,
      sonDirection ++ fireOrders,
      Nil,
      Nil,
      s"""assert
         |${fireOrdersAssertions.mkString("\n")}
         |""".stripMargin
    )
  }

  def authorizeOperator[FM: IsFinite]: OperatorModel = {
    val tyype = typeModel[FM]
    val initial = Flow(Symbol("initial"), tyype, In)
    val reject = Flow(Symbol("reject"), CeciliaBoolean, In)
    val output = Flow(Symbol(s"authorize${nameOf[FM].name}"), tyype, Out)
    OperatorModel(
      PhylogFolder.genericOperatorFolder,
      initial :: reject :: Nil,
      output,
      s"""assert
         |${s"$output = (if $reject then ${noneOf[FM].name.name} else $initial);"}
       """.stripMargin
    )
  }
}
