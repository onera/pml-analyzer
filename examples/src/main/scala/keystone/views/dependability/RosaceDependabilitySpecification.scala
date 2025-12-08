package keystone.views.dependability

import onera.pmlanalyzer.*
import keystone.pml.{KeystonePlatform, RosaceConfiguration}

trait RosaceDependabilitySpecification extends DependabilitySpecification {
  self: KeystonePlatform with RosaceConfiguration =>

  type U = OLE

  val depSpecificationName: Symbol = "ServiceBasedDep"

  // TODO Expression of failure condition should be
  // Application => U : The application'state is U
  // (Initiator,Target) on Basic => U : The request's status between Initiator <-> Target is U on the service Basic
  val failureConditions: Set[(Application, OLE, Int)] = Set(
    (checkA, OLE.Erroneous, 2),
    (checkB, OLE.Erroneous, 2),
    (nnA, OLE.Erroneous, 2),
    (nnB, OLE.Erroneous, 2)
  )

  def mkTargetId(t: Target): TargetId = TargetId(t.name)

  /**
    * The load services mandatory to the functioning of
    *
    * @param p application of the platform
    * @return mandatory load services
    */

  def softwareStoresDependency(
      p: Application
  ): (Variable[OLE], Variable[TargetStatus[OLE]]) => Expr[TargetStatus[OLE]] = {
    if (p == checkA)
      (c, loads) =>
        If(c === min[U]) Then (
          checkA.instruction fmIs loads.fmOf(checkA.instruction)
        ) Else (
          checkA.instruction fmIs c
        )
    else if (p == checkB)
      (c, loads) =>
        If(c === min[U]) Then (
          checkB.instruction fmIs loads.fmOf(checkB.instruction)
        ) Else (
          checkB.instruction fmIs c
        )
    else if (p == ioServer) {
      val loadTargets = p.targetLoads.flatMap(_.targetOwner).map(mkTargetId)
      val storeTargets = p.targetStores
        .flatMap(_.targetOwner)
        .map(mkTargetId)
        .map(_ -> loadTargets)
        .toMap
      (c, loads) =>
        If(c === min[U]) Then (
          storeTargets
            .transform((k, v) =>
              if (
                k == EDMARegister.id || ioServer.instruction.hostingTargets
                  .map(_.id)
                  .contains(k)
              ) loads.fmOf(ioServer.instruction)
              else Worst(v.map(t => loads.fmOf(t)).toSeq: _*)
            )
            .toSeq
        ) Else (
          storeTargets.transform((_, _) => c).toSeq
        )
    } else {
      val loadTargets = p.targetLoads.flatMap(_.targetOwner).map(mkTargetId)
      val storeTargets = p.targetStores
        .flatMap(_.targetOwner)
        .map(mkTargetId)
        .map(_ -> loadTargets)
        .toMap
      (c, loads) =>
        If(c === min[U]) Then (
          storeTargets
            .transform((k, v) =>
              if (p.instruction.hostingTargets.map(_.id).contains(k))
                loads.fmOf(p.instruction)
              else Worst(v.map(t => loads.fmOf(t)).toSeq: _*)
            )
            .toSeq
        ) Else (
          storeTargets.transform((_, _) => c).toSeq
        )
    }

  }

  /**
    * The store that are impacted in case of nominal AND
    * dysfunctional behavior
    *
    * @param p application of the platform
    * @return impacted store services
    */
  def softwareState(
      p: Application
  ): (Variable[OLE], Variable[TargetStatus[OLE]]) => Expr[OLE] =
    if (p == checkA)
      (c, loads) => Worst(c, loads.fmOf(checkA.instruction))
    else if (p == checkB)
      (c, loads) => Worst(c, loads.fmOf(checkB.instruction))
    else {
      val loadTargets = p.targetLoads.flatMap(_.targetOwner).map(mkTargetId)
      (c, loads) => Worst(loadTargets.map(t => loads.fmOf(t)).toSeq :+ c: _*)
    }

  val targetIsInputDep: Set[Target] =
    (DDR.banks ++ MSMC_SRAM.banks ++ corePacs.map(_.dsram)).toSet + EDMARegister
}
