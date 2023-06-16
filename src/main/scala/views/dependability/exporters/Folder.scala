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

package views.dependability.exporters

import views.dependability.exporters.Folder.date
import views.dependability.exporters.Model._

import java.lang.{System => OS}
import scala.xml.Elem

sealed trait Folder {
  val id: Int = Folder.folderNb
  val name: Symbol
  lazy val absolutePath: String
  Folder.folderNb = Folder.folderNb + 1

  override def toString: String = s"Folder(name=${name.name},id=$id)"
}

object Folder {
  var folderNb = 100
  val date: Long = OS.currentTimeMillis()
}

object RootFolder extends Folder {
  lazy val absolutePath: String = ""
  override val name: Symbol = Symbol("")
}

sealed trait CeciliaFolder extends Folder {
  val parent: Folder
  lazy val absolutePath: String = parent match {
    case RootFolder => name.name
    case _  => s"${parent.absolutePath}/${name.name}"
  }

  def toElem: Elem
}

sealed trait SubFolder[T] extends CeciliaFolder

class FamilyFolder[T] (val name: Symbol)(implicit m: ModelDescriptor[T]) extends SubFolder[T] {
  val parent: RootFolder.type = RootFolder
  private val elements = scala.collection.mutable.Set.empty[SubFolder[T]]
  def add(a:SubFolder[T]):Unit = elements += a

  def toElem: Elem = m.getFamilyFamilyFlag match {
    case Some(value) =>
      <cec.folder id={s"$id"} name={s"${name.name}"} family={s"${m.getName}"} type="folder" familyFlag={s"$value"} dateCreate={date.toString} dateModify={date.toString}>
        {elements.toList.sortBy(_.id).map(_.toElem)}
      </cec.folder>
    case None =>
      <cec.folder id={s"$id"} name={s"${name.name}"} family={s"${m.getName}"} type="folder" dateCreate={date.toString} dateModify={date.toString}>
        {elements.toList.sortBy(_.id).map(_.toElem)}
      </cec.folder>
  }
}

object FamilyFolder {
  def apply[T](name: Symbol)(implicit m: ModelDescriptor[T]): FamilyFolder[T] = m.getFolder(name)
}

class SubFamilyFolder[T] (val name: Symbol, val parent: FamilyFolder[T])(implicit m: ModelDescriptor[T]) extends SubFolder[T] {
  private val elements = scala.collection.mutable.Set.empty[EntityFolder[T]]
  parent.add(this)
  def add(a:EntityFolder[T]):Unit =  elements += a
  def toElem: Elem = {
    <cec.folder id={s"$id"} name={s"${name.name}"} family={s"${m.getName}"} type="folder" dateCreate={date.toString} dateModify={date.toString}>
      {elements.toList.sortBy(_.id).map(_.toElem)}
    </cec.folder>
  }
}

object SubFamilyFolder {
  def apply[T](name: Symbol, parent:FamilyFolder[T])(implicit m: ModelDescriptor[T]): SubFamilyFolder[T] = m.getFolder(name,parent)
}

case class EntityFolder[T](name: Symbol, parent: SubFolder[T])(implicit m: ModelDescriptor[T]) extends SubFolder[T] {
  private val elements = scala.collection.mutable.Set.empty[VersionFolder[T]]
  def getVersionNb : Int = elements.size
  parent match {
    case f:FamilyFolder[T] => f.add(this)
    case f:SubFamilyFolder[T] => f.add(this)
    case _ =>
  }
  def add(a:VersionFolder[T]):Unit = elements += a
  def toElem: Elem = m.getEntityFamilyFlag match {
    case Some(tag) =>
    <cec.folder id={s"$id"} name={s"${name.name}"} family={s"${m.getName}"} type="model" familyFlag={tag} dateCreate={date.toString} dateModify={date.toString}>
      {elements.toList.sortBy(_.id).map(_.toElem)}
    </cec.folder>
    case None =>
      <cec.folder id={s"$id"} name={s"${name.name}"} family={s"${m.getName}"} type="model" dateCreate={date.toString} dateModify={date.toString}>
        {elements.toList.sortBy(_.id).map(_.toElem)}
      </cec.folder>
  }
}

case class VersionFolder[T](parent: EntityFolder[T])(implicit m: ModelDescriptor[T]) extends CeciliaFolder {
  val name: Symbol = Symbol((parent.getVersionNb + 1).toString)
  override lazy val absolutePath: String = s"${parent.absolutePath};${name.name}"
  private var model: Option[T] = None
  def add(a:T):Unit = model = Some(a)
  parent.add(this)
  lazy val familyTag:String = {
    (for(m <- model) yield m match {
      case x:RecordType => "record"
      case x:EnumeratedType => "enum"
      case _ => ""
    }) getOrElse("")
  }
  def toElem: Elem = m.getVersionFamilyFlag match {
    case None =>
      <cec.folder id={s"$id"} name={s"${name.name}"} family={s"${m.getName}"} type="version" dateCreate={date.toString} dateModify={date.toString}>
        {(for (mdl <- model) yield m.toElem(mdl))getOrElse ""}
        <cec.property name="object.release" type="Long" value="43"/>
      </cec.folder>
    case Some(tag)  =>
    <cec.folder id={s"$id"} name={s"${name.name}"} family={s"${m.getName}"} type="version" familyFlag={tag} dateCreate={date.toString} dateModify={date.toString}>
      {(for (mdl <- model) yield m.toElem(mdl))getOrElse ""}
      <cec.property name='object.creator' type='String' value='admin'/>
      <cec.property name='object.release' type='Long' value='43'/>
    </cec.folder>
  }
}