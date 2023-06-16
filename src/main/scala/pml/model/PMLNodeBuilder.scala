package pml.model

import pml.model.hardware.Composite
import pml.model.utils.Owner

import scala.collection.mutable.{HashMap => MHashMap}

/**
  * Trait for pml node builder (usually companion object) that must
  * adopt an h-consign like object handling
  * @tparam T the concrete type of built object
  */
trait PMLNodeBuilder[T] {

  //TODO WARNING IF TWO PLATFORMS CONTAINS THE SAME NAMED COMPOSITE THEN MIX IN THE _memo OF THE COMPOSITES' SUBCOMPONENT
  protected val _memo: MHashMap[(Symbol, Symbol), T] = MHashMap.empty

  /**
    * Provide all the object of the current type created for the platform, including
    * the ones created in composite components
    * @group embedFunctions
    * @param owner the name of the platform owning the objects
    * @return set of created objects
    */
  def all(implicit owner: Owner): Set[T] = {
    allDirect ++ Composite.allDirect.flatMap(c => all(c.currentOwner))
  }

  /**
    * Provide all the object of the current type created for the platform, without
    * the ones created in composite components
    * @group embedFunctions
    * @param owner the name of the platform owning the objects
    * @return set of created objects
    */
  def allDirect(implicit owner: Owner): Set[T] =
    _memo.filter(_._1._1 == owner.s).values.toSet

}
