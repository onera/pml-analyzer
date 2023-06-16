package pml.model.service

import pml.model.PMLNodeBuilder
import pml.model.utils.Owner
import sourcecode.Name


/**
  * Base trait for all hardware node builder
  * the name of the transporter is implicitly derived from the name of the variable used during instantiation.
  * Usually an hardware can be constructed without arguments, where T
  * can be [[Load]], [[Store]], [[ArtificialService]]
  * {{{
  *          val myService = T()
  * }}}
  *
  * It is also possible to give a specific name, for instance when creating the component in a loop then the following
  * constructor can bee used
  * {{{
  *            val serviceSeq = for { i <- O to N } yield T(s"myService\$i")
  * }}}
  * @see usage are available in [[pml.examples.simpleKeystone.SimpleKeystonePlatform]]
  * @tparam T the concrete type of built object
  * @group builder
  **/
trait BaseServiceBuilder[T <: Service] extends PMLNodeBuilder[T] {

  /**
    * The builder that must be implemented by specific builder
    * @param name the name of the object
    * @return the object
    */
  protected def builder(name: Symbol): T

  /**
    * A service can be defined by the name provided by the implicit declaration context
    * (the name of the value enclosing the object)
    *
    * @param name  the implicit service name
    * @param owner implicitly retrieved name of the platform
    * @return the service
    */
  def apply()(implicit name: Name, owner: Owner): T = apply(Symbol(name.value))

  /**
    * A service can be defined by its name
    *
    * @param name  the service name
    * @param owner implicitly retrieved name of the platform
    * @return the service
    */
  def apply(name: Symbol)(implicit owner: Owner): T = {
    _memo.getOrElseUpdate((name, owner.s), builder(name))
  }
}
