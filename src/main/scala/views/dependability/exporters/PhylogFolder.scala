/*******************************************************************************
 * Copyright (c) 2021. ONERA
 * This file is part of PML Analyzer
 *
 * PML Analyzer is free software ;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation ;
 * either version 2 of  the License, or (at your option) any later version.
 *
 * PML  Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program ;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package views.dependability.exporters

object PhylogFolder {
  val genericImageFolder: FamilyFolder[ImageModel] = FamilyFolder(Symbol("generic"))
  val phylogComponentFolder: FamilyFolder[ComponentModel] = FamilyFolder(Symbol("phylog"))
  val genericOperatorFolder: FamilyFolder[OperatorModel] = FamilyFolder(Symbol("generic"))
  val phylogEquipmentFolder: FamilyFolder[EquipmentModel] = FamilyFolder(Symbol("phylog"))
  val phylogRecordTypeFolder: FamilyFolder[RecordType] = FamilyFolder(Symbol("phylogRecord"))
  val phylogEnumTypeFolder: FamilyFolder[EnumeratedType] = FamilyFolder(Symbol("phylogEnum"))
  val fmOperatorsFamilyFolder: FamilyFolder[ComponentModel] = FamilyFolder(Symbol("operators"))
  val phylogSystemFamilyFolder: FamilyFolder[SystemModel] = FamilyFolder[SystemModel](Symbol("phylog"))

  val phylogInitiatorFolder: SubFamilyFolder[EquipmentModel] = SubFamilyFolder(Symbol("initiator"), phylogEquipmentFolder)
  val phylogTargetFolder: SubFamilyFolder[EquipmentModel] = SubFamilyFolder(Symbol("target"), phylogEquipmentFolder)
  val phylogTransporterFolder: SubFamilyFolder[EquipmentModel] = SubFamilyFolder(Symbol("transporter"), phylogEquipmentFolder)
  val automatonFamilyFolder: SubFamilyFolder[ComponentModel] = SubFamilyFolder(Symbol("block"), phylogComponentFolder)
  val phylogFrameworkComponentFolder: SubFamilyFolder[ComponentModel] = SubFamilyFolder(Symbol("framework"), phylogComponentFolder)
  val phylogFMTypeFolder: SubFamilyFolder[EnumeratedType] = SubFamilyFolder(Symbol("failureModeTypes"), phylogEnumTypeFolder)
  val phylogCustomTypeFolder: SubFamilyFolder[RecordType] = SubFamilyFolder(Symbol("customTypes"), phylogRecordTypeFolder)
  val phylogSystemExampleFolder: SubFamilyFolder[SystemModel] = SubFamilyFolder(Symbol("example"), phylogSystemFamilyFolder)

  val allFamilies: List[FamilyFolder[_]] =
    (genericImageFolder :: phylogComponentFolder :: genericOperatorFolder ::
      phylogEquipmentFolder :: fmOperatorsFamilyFolder :: phylogEnumTypeFolder :: phylogRecordTypeFolder :: phylogSystemFamilyFolder :: Nil).sortBy(_.id)
}