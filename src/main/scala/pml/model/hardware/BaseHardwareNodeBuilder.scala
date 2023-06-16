package pml.model.hardware

import pml.model.PMLNodeBuilder
import pml.model.relations.ProvideRelation
import pml.model.service.{Load, Service, Store}
import pml.model.utils.Owner
import sourcecode.Name

/**
  * Base trait for all hardware node builder
  * the name of the transporter is implicitly derived from the name of the variable used during instantiation.
  * Usually an hardware can be constructed without arguments, where T
  * can be [[Initiator]], [[SimpleTransporter]], [[Virtualizer]], [[Target]]
  * {{{
  *          val myHardware = T()
  * }}}
  *
  * It is also possible to give a specific name, for instance when creating the component in a loop then the following
  * constructor can bee used
  * {{{
  *            val hardwareSeq = for { i <- O to N } yield T(s"myHardware\$i")
  * }}}
  *
  * It is also possible to add specific services, by default each hardware has a [[pml.model.service.Load]]
  * and a [[pml.model.service.Store]] service.
  * {{{
  *          val myLoadService = Load()
  *          val myOtherLoadService = Load()
  *          val myStoreService = Store()
  *          val myHardware = T(Set(myLoadService,myOtherLoadService,myStoreService)))
  * }}}
  *
  * It is also possible to provide the name and the services for instance
  * {{{
  *            val hardwareSeq = for { i <- O to N } yield T(s"myHardware\$i", Set(Load(s"myLoad\$i"), Store(s"myStore\$i"))
  * }}}
  * @see usage are available in [[pml.examples.simpleKeystone.SimpleKeystonePlatform]]
  * @tparam T the concrete type of built object
  * @group builder
 */
trait BaseHardwareNodeBuilder[T <: Hardware] extends PMLNodeBuilder[T] {

  /**
    * Formatting of object name
    * @param name the name of the object
    * @param owner the name of its owner
    * @return the formatted name
    * @note this method should not be used in models
    * @group utilFun
    */
  final def formatName(name: Symbol, owner: Owner): Symbol = Symbol(owner.s.name + "_" + name.name)

  /**
    * The builder that must be implemented by specific builder
    * @param name the name of the object
    * @return the object
    * @note this method is implemented by concrete members (e.g. [[SimpleTransporter]], no further extension should be provided
    */
  protected def builder(name: Symbol): T


  /**
    * A physical component can be defined only with the basic services it provides
    * The name will be retrieved by using the implicit declaration context
    * (the name of the value enclosing the object)
    * @example {{{
    *          val mySimpleTransporter = SimpleTransporter()
    *          }}}
    * @param basics       the set of basic services provided, if empty a default store and load services are added
    * @param implicitName implicitly retrieved name from the declaration context
    * @param p            implicitly retrieved relation linking components to their provided services
    * @param owner        implicitly retrieved name of the platform
    * @return the physical component
    * @group publicConstructor
    */
  def apply(basics: Set[Service] = Set.empty)(implicit implicitName: Name,
                                              p: ProvideRelation[Hardware, Service],
                                              owner: Owner): T =
    apply(Symbol(implicitName.value), basics)

  /**
    * A physical component can be defined by its name and the basic services it provides
    * A transporter is only defined by its name, so if the transporter already exists it will
    * simply add the services provided by basics
    *
    * @param name   the physical component name
    * @param basics the set of basic services provided, if empty a default store and load services are added
    * @param p      implicitly retrieved relation linking components to their provided services
    * @param owner  implicitly retrieved name of the platform
    * @return the physical component
    * @group publicConstructor
    */
  def apply(name: Symbol, basics: Set[Service])(implicit
                                                p: ProvideRelation[Hardware, Service],
                                                owner: Owner): T = {
    val formattedName = formatName(name, owner)
    val result = _memo.getOrElseUpdate((owner.s, formattedName), builder(formattedName))
    val mutableBasic = collection.mutable.Set(basics.toSeq: _*)
    if (!basics.exists(_.isInstanceOf[Load]))
      mutableBasic += Load(Symbol(s"${formattedName.name}_load"))
    if (!basics.exists(_.isInstanceOf[Store]))
      mutableBasic += Store(Symbol(s"${formattedName.name}_store"))
    p.add(result, mutableBasic)
    result
  }

  /**
    * A physical component can be defined only its name, the services will be defined by default
    * @group publicConstructor
    * @param name  the physical component name
    * @param p     implicitly retrieved relation linking components to their provided services
    * @param owner implicitly retrieved name of the platform
    * @return the physical component
    */
  def apply(name: Symbol)(implicit p: ProvideRelation[Hardware, Service],
                          owner: Owner): T =
    apply(name, Set.empty)
}
