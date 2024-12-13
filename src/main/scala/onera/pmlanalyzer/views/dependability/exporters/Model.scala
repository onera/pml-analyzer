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

import onera.pmlanalyzer.views.dependability.exporters.Model.{
  ComponentDescriptor,
  EnumeratedTypeDescriptor,
  EquipmentDescriptor,
  ImgDescriptor,
  ModelDescriptor
}

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File, InputStream}
import java.nio.file.{Path, Paths}
import javax.imageio.ImageIO
import scala.xml.Elem

sealed trait CeciliaType

object CeciliaBoolean extends CeciliaType {
  val name: Symbol = Symbol("bool")
}

sealed trait Orientation {
  val name: String
}

case object In extends Orientation {
  val name: String = "in"
}

case object Out extends Orientation {
  val name: String = "out"
}

case object Local extends Orientation {
  val name: String = "local"
}

final case class Configuration(name: Symbol, conf: Map[State, String]) {
  def toElem: Elem = {
    <alta.config name={name.name}>
      {conf.map(p => <alta.init name={p._1.name.name} value={p._2}/>)}
    </alta.config>
  }
}

final case class State(name: Symbol, tyype: CeciliaType, ini: String) {
  def toElem: Elem = tyype match {
    case CeciliaBoolean =>
      <alta.state name={name.name} type="bool" value={ini}/>
    case tyype: EnumeratedType =>
      <alta.state name={name.name} refID={tyype.parent.id.toString} refPath={
        tyype.parent.absolutePath
      } value={ini}/>
    case tyype: RecordType =>
      <alta.state name={name.name} refID={tyype.parent.id.toString} refPath={
        tyype.parent.absolutePath
      } value={ini}/>
  }

  override def toString: String = name.name
}

final case class Flow(
    name: Symbol,
    tyype: CeciliaType,
    orientation: Orientation
) {
  private var _owner: Option[BlockModel[_]] = None

  override def toString: String = name.name

  def add(to: BlockModel[_]): Unit = _owner = Some(to)

  def owner: Option[BlockModel[_]] = _owner

  def toElem(x: Int, y: Int, id: Int): Elem = tyype match {
    case CeciliaBoolean =>
      <alta.flow name={name.name} type="bool" orientation={orientation.name} x={
        x.toString
      } y={y.toString} id={id.toString}/>
    case tyype: EnumeratedType =>
      <alta.flow name={name.name} refID={tyype.parent.id.toString} refPath={
        tyype.parent.absolutePath
      } orientation={orientation.name} x={x.toString} y={y.toString} id={
        id.toString
      }/>
    case tyype: RecordType =>
      <alta.flow name={name.name} refID={tyype.parent.id.toString} refPath={
        tyype.parent.absolutePath
      } orientation={orientation.name} x={x.toString} y={y.toString} id={
        id.toString
      }/>
  }

  def toElem: Elem = tyype match {
    case CeciliaBoolean =>
      <alta.flow name={name.name} type="bool" orientation={orientation.name}/>
    case tyype: EnumeratedType =>
      <alta.flow name={name.name} refID={tyype.parent.id.toString} refPath={
        tyype.parent.absolutePath
      } orientation={orientation.name}/>
    case tyype: RecordType =>
      <alta.flow name={name.name} refID={tyype.parent.id.toString} refPath={
        tyype.parent.absolutePath
      } orientation={orientation.name}/>
  }
}

sealed trait Model {
  def toElem: Elem
}

object Model {

  def linksToElem(links: List[(Flow, Flow)]): Seq[Elem] = {
    for {
      ((from, to), id) <- links.zipWithIndex
      oFrom <- from.owner
      oTo <- to.owner
    } yield {
      <alta.link id={id.toString} from={
        s"${from.name.name}[${oFrom.idOf(from)}]"
      } to={s"${to.name.name}[${oTo.idOf(to)}]"}/>
    }
  }

  trait ModelDescriptor[T] {
    private val families =
      collection.mutable.HashMap.empty[Path, FamilyFolder[T]]
    private val subFamilies =
      collection.mutable.HashMap.empty[Path, SubFamilyFolder[T]]
    private val models = collection.mutable.HashMap.empty[Path, T]

    def getName: String

    def getVersionFamilyFlag: Option[String]

    def getEntityFamilyFlag: Option[String]

    def getFamilyFamilyFlag: Option[String]

    def toElem(a: T): Elem

    def getFolder(s: Symbol): FamilyFolder[T] =
      families.getOrElseUpdate(Paths.get(s.name), new FamilyFolder[T](s)(this))

    def getFolder(s: Symbol, parent: FamilyFolder[T]): SubFamilyFolder[T] =
      subFamilies.getOrElseUpdate(
        Paths.get(parent.absolutePath, s.name),
        new SubFamilyFolder(s, parent)(this)
      )

    def getModel(of: Path)(d: () => T): T = models.getOrElseUpdate(of, d())
  }

  object ModelDescriptor

  implicit object ImgDescriptor extends ModelDescriptor[ImageModel] {
    def getName: String = "imag"

    def getVersionFamilyFlag: Option[String] = None

    override def getEntityFamilyFlag: Option[String] = None

    override def getFamilyFamilyFlag: Option[String] = None

    def toElem(a: ImageModel): Elem = a.toElem
  }

  implicit object ComponentDescriptor extends ModelDescriptor[ComponentModel] {
    def getName: String = "component"

    def toElem(a: ComponentModel): Elem = a.toElem

    def getVersionFamilyFlag: Option[String] = None

    override def getEntityFamilyFlag: Option[String] = None

    override def getFamilyFamilyFlag: Option[String] = None
  }

  implicit object EnumeratedTypeDescriptor
      extends ModelDescriptor[EnumeratedType] {
    def getName: String = "type"

    def getVersionFamilyFlag: Option[String] = Some("enum")

    def toElem(a: EnumeratedType): Elem = a.toElem

    override def getEntityFamilyFlag: Option[String] = Some("enum")

    override def getFamilyFamilyFlag: Option[String] = None
  }

  implicit object RecordTypeDescriptor extends ModelDescriptor[RecordType] {
    def getName: String = "type"

    def toElem(a: RecordType): Elem = a.toElem

    def getVersionFamilyFlag: Option[String] = Some("record")

    override def getEntityFamilyFlag: Option[String] = Some("record")

    override def getFamilyFamilyFlag: Option[String] = None
  }

  implicit object SystemDescriptor extends ModelDescriptor[SystemModel] {
    def getName: String = "project"

    def getVersionFamilyFlag: Option[String] = Some("model")

    def toElem(a: SystemModel): Elem = a.toElem

    override def getEntityFamilyFlag: Option[String] = Some("model")

    override def getFamilyFamilyFlag: Option[String] = Some("project")
  }

  implicit object EquipmentDescriptor extends ModelDescriptor[EquipmentModel] {
    def getName: String = "equipment"

    def toElem(a: EquipmentModel): Elem = a.toElem

    def getVersionFamilyFlag: Option[String] = None

    override def getEntityFamilyFlag: Option[String] = None

    override def getFamilyFamilyFlag: Option[String] = None
  }

  implicit object OperatorDescriptor extends ModelDescriptor[OperatorModel] {
    def getName: String = "operator"

    def toElem(a: OperatorModel): Elem = a.toElem

    def getVersionFamilyFlag: Option[String] = None

    override def getEntityFamilyFlag: Option[String] = None

    override def getFamilyFamilyFlag: Option[String] = None
  }

  trait FlowPlacer {
    def position(f: Flow): (Int, Int)

    def idOf(f: Flow): Int
  }

  trait EquiBlockFlowPlacer extends FlowPlacer {
    self: BlockModel[_] =>
    private val _basicSep = 10
    private val _inputs = flows.filter(_.orientation == In)
    private val _outputs = flows.filter(_.orientation == Out)
    private val _inputMargin =
      (icon.height - _basicSep * (_inputs.size - 1)) / 2
    private val _outputMargin =
      (icon.height - _basicSep * (_outputs.size - 1)) / 2

    def position(f: Flow): (Int, Int) = f match {
      case Flow(n, _, In) =>
        if (_inputMargin > 0)
          (0, _inputMargin + _inputs.indexWhere(_.name == n) * _basicSep)
        else
          (0, _inputs.indexWhere(_.name == n) * icon.height / _inputs.size)
      case Flow(n, _, Out) =>
        if (_outputMargin > 0)
          (
            icon.width,
            _outputMargin + _outputs.indexWhere(_.name == n) * _basicSep
          )
        else
          (0, _outputs.indexWhere(_.name == n) * icon.height / _outputs.size)
      case _ => (0, 0)
    }

    def idOf(f: Flow): Int = {
      flows.indexWhere(_.name == f.name)
    }
  }

}

class EnumeratedType(
    val name: Symbol,
    val values: List[Symbol],
    val parent: VersionFolder[EnumeratedType]
) extends CeciliaType {
  parent.add(this)

  def toElem: Elem = {
    <cec.model nature="type.enumerated" format="XML" encoding="UTF-8">
      <cec.type.enum>
        {values.map(v => <enum.field value={s"${v.name}"}/>)}
      </cec.type.enum>
    </cec.model>
  }
}

object EnumeratedType {
  def apply(
      name: Symbol,
      values: List[Symbol],
      parent: SubFamilyFolder[EnumeratedType]
  )(implicit ev: ModelDescriptor[EnumeratedType]): EnumeratedType =
    ev.getModel(Paths.get(parent.absolutePath, name.name))(() =>
      new EnumeratedType(
        name,
        values,
        VersionFolder(EntityFolder(name, parent))
      )
    )
}

class RecordType(
    val name: Symbol,
    val parent: VersionFolder[RecordType],
    val fields: List[Flow]
) extends CeciliaType {
  parent.add(this)

  def toElem: Elem = {
    <cec.model nature='type.structured' format='XML' encoding='UTF-8'>
      <cec.type.link type='undefine'>
        {
      fields.collect({
        case Flow(n, CeciliaBoolean, _) =>
          <link.field name={n.name} type="bool" direction="normal"/>
        case Flow(n, tyype: EnumeratedType, _) =>
          <link.field name={n.name} refID={tyype.parent.id.toString} refPath={
            tyype.parent.absolutePath
          } direction="normal"/>
      })
    }
      </cec.type.link>
    </cec.model>
  }
}

object RecordType {
  def apply(
      name: Symbol,
      parent: SubFamilyFolder[RecordType],
      fields: List[Flow]
  )(implicit ev: ModelDescriptor[RecordType]): RecordType =
    ev.getModel(Paths.get(parent.absolutePath, name.name))(() =>
      new RecordType(
        name,
        VersionFolder(EntityFolder(name, parent)),
        fields.distinct
      )
    )
}

sealed trait BlockModel[T] extends Model.EquiBlockFlowPlacer {
  val name: Symbol
  val parent: VersionFolder[T]
  val icon: ImageModel
  val simulationIcons: List[ImageModel]
  val flows: List[Flow]
  val code: String
}

sealed trait EventModel {
  val name: Symbol
}

final case class SynchroEventModel(
    name: Symbol,
    events: List[EventModel],
    tyype: String
) extends EventModel

sealed trait ConcreteEventModel extends EventModel {
  val law: Elem

  def toElem: Elem = {
    <alta.event name={name.name}>
      {law}
    </alta.event>
  }
}

final case class DeterministicEventModel(name: Symbol)
    extends ConcreteEventModel {
  val law: Elem = {
    <law type="Dirac">
      <parameter.value value="0.0"/>
    </law>
  }
}

final case class StochasticEventModel(name: Symbol, lambda: Double = 10e-4)
    extends ConcreteEventModel {
  val law: Elem = {
    <law type="exponential">
      <parameter.value value={lambda.toString}/>
    </law>
  }
}

final case class ComponentModel(
    name: Symbol,
    parent: VersionFolder[ComponentModel],
    icon: ImageModel,
    simulationIcons: List[ImageModel],
    flows: List[Flow],
    events: List[ConcreteEventModel],
    states: List[State],
    code: String
) extends BlockModel[ComponentModel] {
  parent.add(this)
  flows.foreach(_.add(this))

  def toElem: Elem = {
    <cec.model nature="component" format="XML" encoding="UTF-8">
      <cec.component width={icon.width.toString} height={
      icon.height.toString
    } auto-move="true" border="false">
        <alta.icon refID={icon.parent.id.toString} refPath={
      icon.parent.absolutePath
    }/>{
      simulationIcons.map(s =>
        <alta.simul refID={s.parent.id.toString} refPath={
          s.parent.absolutePath
        }/>
      )
    }{
      flows.map(flow =>
        flow.toElem(position(flow)._1, position(flow)._2, idOf(flow))
      )
    }{states.map(_.toElem)}{events.map(_.toElem)}
        <alta.code>{code}</alta.code>
      </cec.component>
    </cec.model>
  }
}

object ComponentModel {
  val _components = collection.mutable.HashMap
    .empty[(Symbol, SubFamilyFolder[ComponentModel]), ComponentModel]
  def apply(
      name: Symbol,
      parent: SubFamilyFolder[ComponentModel],
      icon: ImageModel,
      simul: List[ImageModel],
      flows: List[Flow],
      events: List[ConcreteEventModel],
      states: List[State],
      code: String
  ): ComponentModel = {
    _components.getOrElseUpdate(
      (name, parent),
      ComponentModel(
        name,
        VersionFolder(EntityFolder(name, parent)),
        icon,
        simul,
        flows.distinct,
        events.distinct,
        states.distinct,
        code
      )
    )
  }
}

final case class SubComponent(
    name: Symbol,
    tyype: BlockModel[_],
    x: Int = 0,
    y: Int = 0,
    mirrorV: Boolean = false,
    mirrorH: Boolean = false
) {
  def toElem: Elem = {
    <alta.sub name={name.name} refID={tyype.parent.id.toString} refPath={
      tyype.parent.absolutePath
    }/>
  }

  override def toString: String = name.name
}

class EquipmentModel(
    val name: Symbol,
    val parent: VersionFolder[EquipmentModel],
    val icon: ImageModel,
    val simulationIcons: List[ImageModel],
    val flows: List[Flow],
    val subs: List[SubComponent],
    val links: List[(Flow, Flow)],
    val sync: List[SynchroEventModel] = Nil,
    val code: String
) extends BlockModel[EquipmentModel] {
  parent.add(this)
  flows.foreach(_.add(this))

  def toElem: Elem = {
    <cec.model nature="equipment" format="XML" encoding="UTF-8">
      <cec.equipment width={icon.width.toString} height={
      icon.height.toString
    } auto-move="true" border="false">
        <alta.icon refID={icon.parent.id.toString} refPath={
      icon.parent.absolutePath
    }/>{
      simulationIcons.map(s =>
        <alta.simul refID={s.parent.id.toString} refPath={
          s.parent.absolutePath
        }/>
      )
    }{
      flows.map(flow =>
        flow.toElem(position(flow)._1, position(flow)._2, idOf(flow))
      )
    }{subs.map(_.toElem)}{Model.linksToElem(links)}{
      sync.map(s => {
        <alta.sync name={s.name.name} type={s.tyype}>
          {s.events.map(e => <alta.event name={e.name.name}/>)}
        </alta.sync>
      })
    }<alga.view id="0">
        <alga.sheet id="1" name="Calque 1" visible="true" selected="true">
          {links.indices.map(i => <alga.link id={i.toString}/>)}{
      flows.map(f =>
        <alga.port name={f.name.name} cx={position(f)._1.toString} cy={
          position(f)._2.toString
        } text.position='top'/>
      )
    }{
      subs.map(s =>
        <alga.node name={s.name.name} cx={s.x.toString} cy={
          s.y.toString
        } text.position="top"/>
      )
    }
        </alga.sheet>
      </alga.view>
        <alta.code>{code}
        </alta.code>
      </cec.equipment>
    </cec.model>
  }
}

object EquipmentModel {
  def apply(
      name: Symbol,
      parent: SubFamilyFolder[EquipmentModel],
      icon: ImageModel,
      simul: List[ImageModel],
      flows: List[Flow],
      subs: List[SubComponent],
      links: List[(Flow, Flow)],
      sync: List[SynchroEventModel],
      code: String
  )(implicit ev: ModelDescriptor[EquipmentModel]): EquipmentModel = {
    val version = VersionFolder(EntityFolder(name, parent))
    ev.getModel(Paths.get(version.absolutePath))(() =>
      new EquipmentModel(
        name,
        version,
        icon,
        simul,
        flows.distinct,
        subs.distinct,
        links,
        sync,
        code
      )
    )
  }
}

class SystemModel(
    val name: Symbol,
    val parent: VersionFolder[SystemModel],
    val subs: List[SubComponent],
    val links: List[(Flow, Flow)],
    val sync: List[SynchroEventModel] = Nil,
    val confs: List[Configuration],
    val code: String
) extends Model {
  parent.add(this)

  def toElem: Elem = {
    <cec.model nature="system.local" format="XML" encoding="UTF-8">
      <cec.mbsa>
        {
      subs.map(s =>
        <alta.sub name={s.name.name} refID={
          s.tyype.parent.id.toString
        } refPath={s.tyype.parent.absolutePath}/>
      )
    }{Model.linksToElem(links)}{
      sync.map(s => {
        <alta.sync name={s.name.name} type={s.tyype}>
          {s.events.map(e => <alta.event name={e.name.name}/>)}
        </alta.sync>
      })
    }<alga.view id="0">
        <alga.sheet id="1" name="Calque 1" visible="true" selected="true">
          {links.indices.map(i => <alga.link id={i.toString}/>)}{
      subs.map(s =>
        <alga.node name={s.name.name} cx={s.x.toString} cy={
          s.y.toString
        } text.position="top"/>
      )
    }
        </alga.sheet>
      </alga.view>
        <alta.config name='Default'/>
        {confs.map(_.toElem)}<alta.code>{code}
      </alta.code>
      </cec.mbsa>
    </cec.model>
  }
}

object SystemModel {
  def apply(
      name: Symbol,
      parent: SubFamilyFolder[SystemModel],
      subs: List[SubComponent],
      links: List[(Flow, Flow)],
      sync: List[SynchroEventModel],
      conf: List[Configuration],
      code: String
  )(implicit ev: ModelDescriptor[SystemModel]): SystemModel = {
    val version = VersionFolder(EntityFolder(name, parent))
    ev.getModel(Paths.get(version.absolutePath))(() =>
      new SystemModel(name, version, subs.distinct, links, sync, conf, code)
    )
  }
}

class ImageModel(val parent: VersionFolder[ImageModel], val stream: InputStream)
    extends Model {
  private val data = Array.ofDim[Byte](stream.available())
  // WARNING CAN FAIL TO LOAD ALL BYTE BEFORE READ
  stream.read(data)
  private val encoded = java.util.Base64.getEncoder.encodeToString(data)
  private val img: BufferedImage = ImageIO.read(new ByteArrayInputStream(data))

  val width: Int = img.getWidth()
  val height: Int = img.getHeight

  parent.add(this)

  def toElem: Elem = {
    <cec.model nature="image" format="GIF">
      {encoded}
    </cec.model>
  }
}

object ImageModel {
  def apply(
      parent: FamilyFolder[ImageModel],
      name: Symbol,
      stream: InputStream
  )(implicit m: ModelDescriptor[ImageModel]): ImageModel = {
    val version = VersionFolder(EntityFolder(name, parent))
    m.getModel(Paths.get(version.absolutePath))(() =>
      new ImageModel(version, stream)
    )
  }

}

class OperatorModel(
    val parent: VersionFolder[OperatorModel],
    val inputs: List[Flow],
    val output: Flow,
    val code: String
) extends Model {
  parent.add(this)

  def idOf(f: Flow): Int = inputs.indexOf(f)

  def toElem: Elem = {
    <cec.model nature="operator" format="XML" encoding="UTF-8">
      {
      output match {
        case Flow(_, CeciliaBoolean, _) =>
          <cec.operator type="bool">
          {inputs.map(_.toElem)}<alta.code>
          {code}
        </alta.code>
        </cec.operator>

        case Flow(_, tyype: EnumeratedType, _) =>
          <cec.operator refID={tyype.parent.id.toString} refPath={
            tyype.parent.absolutePath
          }>
          {inputs.map(_.toElem)}<alta.code>
          {code}
        </alta.code>
        </cec.operator>

        case Flow(_, tyype: RecordType, _) =>
          <cec.operator refID={tyype.parent.id.toString} refPath={
            tyype.parent.absolutePath
          }>
          {inputs.map(_.toElem)}<alta.code>
          {code}
        </alta.code>
        </cec.operator>
      }
    }
    </cec.model>
  }
}

object OperatorModel {
  def apply(
      parent: FamilyFolder[OperatorModel],
      inputs: List[Flow],
      output: Flow,
      code: String
  )(implicit ev: ModelDescriptor[OperatorModel]): OperatorModel = {
    val version = output.tyype match {
      case CeciliaBoolean =>
        VersionFolder(
          EntityFolder(
            output.name,
            SubFamilyFolder(CeciliaBoolean.name, parent)
          )
        )
      case tyype: EnumeratedType =>
        VersionFolder(
          EntityFolder(output.name, SubFamilyFolder(tyype.name, parent))
        )
      case tyype: RecordType =>
        VersionFolder(
          EntityFolder(output.name, SubFamilyFolder(tyype.name, parent))
        )
    }
    ev.getModel(Paths.get(version.absolutePath))(() =>
      new OperatorModel(version, inputs, output, code)
    )
  }
}

final case class FailureConditions(fc: Set[(String, String)], size: Int)
    extends Model {
  def fileName(f: String, v: String): String =
    s"${f.replace(".", "_")}_is_$v.seq"
  def toElem: Elem = {
    <targets outputType="file">
      {
      fc.map(p =>
        <target var={p._1} val={p._2} path={
          new java.io.File(".").getCanonicalPath + File.separator + fileName(
            p._1,
            p._2
          )
        } order={size.toString}/>
      )
    }
    </targets>
  }
}
