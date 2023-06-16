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

import views.dependability.exporters.PhylogFolder._

object GenericImage {

  private val load = (x:String) => getClass.getClassLoader.getResourceAsStream(x)
  val functionBlueCircle: ImageModel = ImageModel(genericImageFolder, Symbol("function_blue_circle"), load("icons/generic/function_blue_circle.gif"))
  val functionGreenCircle: ImageModel = ImageModel(genericImageFolder, Symbol("function_green_circle"), load("icons/generic/function_green_circle.gif"))
  val functionRedCircle: ImageModel = ImageModel(genericImageFolder, Symbol("function_red_circle"), load("icons/generic/function_red_circle.gif"))
  val functionRedCross: ImageModel = ImageModel(genericImageFolder, Symbol("function_red_cross"), load("icons/generic/function_red_cross.gif"))
  val functionPurpleCircle: ImageModel = ImageModel(genericImageFolder, Symbol("function_purple_circle"), load("icons/generic/function_purple_circle.gif"))
  val sourceBlueCircle: ImageModel = ImageModel(genericImageFolder, Symbol("source_blue_circle"), load("icons/generic/source_blue_circle.gif"))
  val sourceGreenCircle: ImageModel = ImageModel(genericImageFolder, Symbol("source_green_circle"), load("icons/generic/source_green_circle.gif"))
  val sourceRedCross: ImageModel = ImageModel(genericImageFolder, Symbol("source_red_cross"), load("icons/generic/source_red_cross.gif"))
  val minBlue: ImageModel = ImageModel(genericImageFolder, Symbol("andBlue"), load("icons/generic/andBlue.gif"))
  val minRed: ImageModel = ImageModel(genericImageFolder, Symbol("andRed"), load("icons/generic/andRed.gif"))
  val minGreen: ImageModel = ImageModel(genericImageFolder, Symbol("andGreen"), load("icons/generic/andGreen.gif"))
  val maxBlue: ImageModel = ImageModel(genericImageFolder, Symbol("orBlue"), load("icons/generic/orBlue.gif"))
  val maxRed: ImageModel = ImageModel(genericImageFolder, Symbol("orRed"), load("icons/generic/orRed.gif"))
  val maxGreen: ImageModel = ImageModel(genericImageFolder, Symbol("orGreen"), load("icons/generic/orGreen.gif"))
  val equalBlue: ImageModel = ImageModel(genericImageFolder, Symbol("equalBlue"), load("icons/generic/equalBlue.gif"))
  val equalRed: ImageModel = ImageModel(genericImageFolder, Symbol("equalRed"), load("icons/generic/equalRed.gif"))
  val equalGreen: ImageModel = ImageModel(genericImageFolder, Symbol("equalGreen"), load("icons/generic/equalGreen.gif"))
  val selectBlue: ImageModel = ImageModel(genericImageFolder, Symbol("selectBlue"), load("icons/generic/selectBlue.gif"))
  val select1Red: ImageModel = ImageModel(genericImageFolder, Symbol("select1Red"), load("icons/generic/select1Red.gif"))
  val select1Green: ImageModel = ImageModel(genericImageFolder, Symbol("select1Green"), load("icons/generic/select1Green.gif"))
  val select2Red: ImageModel = ImageModel(genericImageFolder, Symbol("select1Red"), load("icons/generic/select1Red.gif"))
  val select2Green: ImageModel = ImageModel(genericImageFolder, Symbol("select1Green"), load("icons/generic/select1Green.gif"))
  val preBlue: ImageModel = ImageModel(genericImageFolder, Symbol("preBlue"), load("icons/generic/preBlue.gif"))
  val preRed: ImageModel = ImageModel(genericImageFolder, Symbol("preRed"), load("icons/generic/preRed.gif"))
  val preGreen: ImageModel = ImageModel(genericImageFolder, Symbol("preGreen"), load("icons/generic/preGreen.gif"))
  val phylogBlockBlue: ImageModel = ImageModel(genericImageFolder, Symbol("phylogBlockBlue"), load("icons/phylog/phylogBlockBlue.gif"))
  val phylogBlockRed: ImageModel = ImageModel(genericImageFolder, Symbol("phylogBlockRed"), load("icons/phylog/phylogBlockRed.gif"))
  val phylogBlockGreen: ImageModel = ImageModel(genericImageFolder, Symbol("phylogBlockGreen"), load("icons/phylog/phylogBlockGreen.gif"))
  val phylogBlockGrey: ImageModel = ImageModel(genericImageFolder, Symbol("phylogBlockGrey"), load("icons/phylog/phylogBlockGrey.gif"))

}
