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

package onera.pmlanalyzer

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

/**
  * Package containing all general modelling features of PML
  * @see For basic classes used to model a platform see [[pml.model]]
  * @see For operators used to manipulate the classes see [[pml.operators]]
  * @see For exporters see [[pml.exporters]]
  * @see For examples on operator and class usage see [[pml.examples]] and
  *      the related documentation in src/main/doc-resources/pml/examples
  * @groupname software_class Software PML nodes
  * @groupprio software_class 4
  * @groupname service_class Service PML nodes
  * @groupprio service_class 4
  * @groupname target_class Target PML nodes
  * @groupprio target_class 4
  * @groupname hardware_class Hardware PML nodes
  * @groupprio hardware_class 4
  * @groupname initiator_class Initiator PML nodes
  * @groupprio initiator_class 4
  * @groupname hierarchical_class Hierarchical PML nodes
  * @groupprio hierarchical_class 4
  * @groupname transporter_class Transporter PML nodes
  * @groupprio transporter_class 4
  * @groupname builder PML node builders
  * @groupprio builder 4
  * @groupname transaction_class Transaction classes
  * @groupprio transaction_class 4
  * @groupname scenario_class Scenario classes
  * @groupprio scenario_class 4
  * @groupname transaction_operation Transaction operators
  * @groupprio transaction_operation 2
  * @groupname scenario_operation Scenario operators
  * @groupprio scenario_operation 2
  * @groupname user_transaction_relation Used user transaction relations
  * @groupprio user_transaction_relation 3
  * @groupname user_scenario_relation Used user scenario relations
  * @groupprio user_scenario_relation 3
  * @groupname transaction_def User transaction definition
  * @groupprio transaction_def 0
  * @groupname scenario_def User scenario definition
  * @groupprio scenario_def 0
  * @groupname identifier Identifiers
  * @groupprio identifier 3
  * @groupname target Target components
  * @groupprio target 0
  * @groupname transporter Transporter components
  * @groupprio transporter 0
  * @groupname initiator Initiator components
  * @groupprio initiator 0
  * @groupname composite Composite components
  * @groupprio composite 0
  * @groupname composite_def Composite definition
  * @groupprio composite_def 1
  * @groupname application Applications
  * @groupprio application 0
  * @groupname data Data
  * @groupprio data 0
  * @groupname load Load services
  * @groupprio load 0
  * @groupname store Store services
  * @groupprio store 0
  * @groupname component_access Internal component accessors
  * @groupprio component_access 5
  * @groupname printer_function Print functions
  * @groupprio printer_function 8
  * @groupname route_relation Route relations
  * @groupprio route_relation 3
  * @groupname platform_def Platform definition
  * @groupprio platform_def 0
  * @groupname embedFunctions Contained node functions
  * @groupprio embedFunctions 5
  * @groupname utilFun Utility functions
  * @groupprio utilFun 5
  * @groupname publicConstructor Public constructors
  * @groupprio publicConstructor 5
  * @groupname transaction Transaction relations
  * @groupprio transaction 0
  * @groupname auth_relation Authorize relation
  * @groupprio auth_relation 3
  * @groupname link_relation Link relations
  * @groupprio link_relation 3
  * @groupname provide_relation Provide relations
  * @groupprio provide_relation 3
  * @groupname use_relation Use relations
  * @groupprio use_relation 3
  */
package object pml