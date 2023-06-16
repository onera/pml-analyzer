package pml.model.software

import pml.model.PMLNodeBuilder
import pml.model.utils.Owner
import sourcecode.Name

/**
  * Base trait for all hardware node builder
  * the name of the transporter is implicitly derived from the name of the variable used during instantiation.
  * Usually an hardware can be constructed without arguments, where T
  * can be [[Application]], [[Data]]
  * {{{
  *          val mySoftware = T()
  * }}}
  *
  * It is also possible to give a specific name, for instance when creating the component in a loop then the following
  * constructor can bee used
  * {{{
  *            val softwareSeq = for { i <- O to N } yield T(s"mySoftware\$i")
  * }}}
  * @see usage are available in [[pml.examples.simpleKeystone.SimpleKeystonePlatform]]
  * @tparam T the concrete type of built object
  * @group builder
  **/
trait BaseSoftwareNodeBuilder[T <: Application] extends PMLNodeBuilder[T] {

  /**
    * The builder that must be implemented by specific builder
    * @param name the name of the object
    * @return the object
    */
  protected def builder(name: Symbol): T

  /**
    * A software component can be defined only its name
    *
    * @param name  the software name
    * @param owner implicitly retrieved name of the platform
    * @return the software
    */
  def apply(name: Symbol)(implicit owner: Owner): T =
    _memo.getOrElseUpdate((owner.s, name), builder(name))

  /**
    * A software component can be defined by the name provided by the implicit declaration context
    * (the name of the value enclosing the object)
    *
    * @param name  the implicit software name
    * @param owner implicitly retrieved name of the platform
    * @return the software
    */
  def apply()(implicit name: Name, owner: Owner): T =
    apply(Symbol(name.value))
}
