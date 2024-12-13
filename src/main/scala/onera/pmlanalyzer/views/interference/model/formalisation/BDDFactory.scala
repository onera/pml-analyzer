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

package onera.pmlanalyzer.views.interference.model.formalisation

import net.sf.javabdd.{BDD => JavaBDD, BDDFactory => JavaBDDFactory}

import scala.collection.mutable

/** BDDFactory where variables labelled on BDD node are InstBoolIdent
  */
class SymbolBDDFactory extends GenBDDFactory[Symbol] {
  protected val _factory: JavaBDDFactory = initFactory(500, 500)
  protected val varMap: mutable.HashMap[Symbol, Int] = mutable.HashMap.empty
  protected val nbOfVar: Int = 500
}

/** base trait for BDD factories
  * @tparam Var
  *   the type of variable labelled on BDD nodes
  * @tparam MyBDD
  *   the BDD representation which must be a subtype of JavaBDD type
  */
trait BaseBDDFactory[Var, MyBDD <: JavaBDD] {

  /** initialisation of the cache and number of node of the factory
    * @param numberOfVar
    *   maximum number of variables in BDDs
    * @param cacheSize
    *   initial size of the cache table containing BDD nodes
    * @return
    */
  def initFactory(numberOfVar: Int, cacheSize: Int): JavaBDDFactory

  /** Produce a BDD node labelled with a given variable
    * @param variable
    *   the variable
    * @return
    *   a BDD node
    */
  def getVar(variable: Var): MyBDD

  /** Return the ith BDD node in the table
    * @param i
    *   the index of the BDD node
    * @return
    *   BDD node
    */
  def getIthVar(i: Int): MyBDD

  /** Clean the cache and the index table
    */
  def reset(): Unit

  /** Return the zero terminal
    * @return
    *   zero terminal
    */
  def zero(): MyBDD

  /** Return the one terminal
    * @return
    *   one terminal
    */
  def one(): MyBDD

  /** BDD AND
    * @param left
    *   BDD
    * @param right
    *   BDD
    * @return
    *   the resulting BDD
    */
  def andBDD(left: MyBDD, right: MyBDD): MyBDD

  /** n-ary BDD and
    * @param s
    *   set of BDD
    * @return
    *   the resulting BDD
    */
  def andBDD(s: Iterable[MyBDD]): MyBDD =
    s.foldLeft(one())((acc, l) => andBDD(acc, l))

  /** BDD OR
    * @param left
    *   BDD
    * @param right
    *   BDD
    * @return
    *   the resulting BDD
    */
  def orBDD(left: MyBDD, right: MyBDD): MyBDD

  /** n-ary BDD or
    * @param s
    *   the set of BDD
    * @return
    *   the resulting BDD
    */
  def orBDD(s: Iterable[MyBDD]): MyBDD =
    s.foldLeft(zero())((acc, l) => orBDD(acc, l))

  /** BDD negation
    * @param arg
    *   initial BDD
    * @return
    *   negated BDD
    */
  def notBDD(arg: MyBDD): MyBDD

  /** BDD implication
    * @param left
    *   BDD
    * @param right
    *   BDD
    * @return
    *   the resulting BDD
    */
  def mkImplies(left: MyBDD, right: MyBDD): MyBDD =
    orBDD(notBDD(left), right)

  /** Exactly k elements out of an ordered sequence of variables
    * @param vs
    *   the ordered sequence of variables
    * @param k
    *   the number of variables that must be true
    * @return
    *   the resulting BDD
    */
  def mkExactlyK(vs: Seq[Var], k: Int): MyBDD = {
    val ds = vs.distinct
    val oneIdx = (-1, 0)
    val zeroIdx = (0, -1)

    val _memo = mutable.HashMap.empty[(Int, Int), MyBDD]

    def BDDFromTable(
        of: (Int, Int),
        in: Map[(Int, Int), (Var, (Int, Int), (Int, Int))]
    ): MyBDD = _memo.getOrElseUpdate(
      of,
      if (of == oneIdx)
        one()
      else if (of == zeroIdx)
        zero()
      else {
        val (v, h, l) = in(of)
        getVar(v)
        mkNode(v, BDDFromTable(h, in), BDDFromTable(l, in))
      }
    )

    if (k == 0 || ds.size < k)
      zero()
    else {
      val linkMap = (0 to k)
        .flatMap(nbTrue =>
          (0 to ds.size - k)
            .filter(_ + nbTrue < ds.size)
            .map(nbFalse =>
              (nbTrue, nbFalse) -> (
                ds(nbTrue + nbFalse),
                if (nbTrue == k - 1 && nbFalse == ds.size - k) oneIdx
                else if (nbTrue >= k) zeroIdx
                else (nbTrue + 1, nbFalse),
                if (nbTrue == k && nbFalse == ds.size - k - 1) oneIdx
                else if (nbFalse >= ds.size - k) zeroIdx
                else (nbTrue, nbFalse + 1)
              )
            )
        )
        .toMap
      BDDFromTable((0, 0), linkMap)
    }
  }

  /** Replace all BDD nodes labelled by a given to variable to another one
    * @param replace
    *   the initial variable to replace
    * @param by
    *   the new variable
    * @param in
    *   the BDD
    * @return
    *   the modified BDD
    */
  def replaceVar(replace: Var, by: Var, in: MyBDD): MyBDD

  /** Build a MDDNode labelled by a variable where the high and low sons are
    * given
    * @param variable
    *   the variable labelling the BDD
    * @param high
    *   the high son
    * @param low
    *   the low son
    * @return
    *   the resulting BDD
    */
  def mkNode(variable: Var, high: MyBDD, low: MyBDD): MyBDD

  /** Free native data structure if exists
    */
  def dispose(): Unit

  /** @return
    *   the mapping from BDD to labelled variable
    */
  def getVarMap: Map[MyBDD, Var]

  /** Import a BDD in this factory from one coming from another factory
    * @param bdd
    *   the other factory BDD
    * @param bddVar
    *   the map from BDD node to variables
    * @tparam OtherBDD
    *   the type of the other BDD
    * @return
    *   the BDD imported in this factory
    */
  def importBDD[OtherBDD <: JavaBDD](
      bdd: OtherBDD,
      bddVar: Map[Var, OtherBDD]
  ): MyBDD = {
    // add fresh vars in this factory
    bddVar.foreach(kv => kv._1 -> getVar(kv._1).`var`())
    // copy node by node the initial bdd by replacing old variables by freshly created one
    importRec(bdd, bddVar.map(p => p._2.`var`() -> p._1))
  }

  /** Build a BDD in this factory from one coming from another factory
    * @param bdd
    *   the other factory BDD
    * @tparam OtherBDD
    *   the type of the other BDD
    * @return
    *   the BDD imported in this factory
    */
  private def importRec[OtherBDD <: JavaBDD](
      bdd: OtherBDD,
      bddMap: Map[Int, Var]
  ): MyBDD = {
    if (bdd.isOne)
      one()
    else if (bdd.isZero)
      zero()
    else
      mkNode(
        bddMap(bdd.`var`()),
        importRec(bdd.high(), bddMap),
        importRec(bdd.low(), bddMap)
      )
  }

}

/** Class for a classic BDD Factory (variable are integer and BDD are JavaBDD)
  */
class BDDFactory extends BaseBDDFactory[Int, JavaBDD] {

  // the number of variable produced by the factory
  private var _varCount = 0

  // initial maximum number of variables
  private val nbOfVar: Int = 500

  // the JavaBDD factory used to delegate BDD computation
  private val _factory: JavaBDDFactory = initFactory(nbOfVar, 1000)

  /** Initialise a JavaBDD factory
    * @param numberOfVar
    *   maximum number of variables in BDDs
    * @param cacheSize
    *   initial size of the cache table containing BDD nodes
    * @return
    *   JavaBDD factory
    */
  def initFactory(numberOfVar: Int, cacheSize: Int): JavaBDDFactory = {
    var numberOfNodes = numberOfVar * numberOfVar
    numberOfNodes = Math.max(cacheSize, numberOfNodes)
    val fac = JavaBDDFactory.init("java", numberOfNodes, cacheSize)
    if (fac.varNum() < numberOfVar)
      fac.setVarNum(numberOfVar)
    fac.autoReorder(JavaBDDFactory.REORDER_WIN2)
    fac
  }

  /** Build a variable labelled by the current number of variable produced by
    * the factory
    *
    * @return
    *   BDD
    */
  def produceVar(): JavaBDD = {
    _varCount = _varCount + 1
    if ((_varCount % nbOfVar) == 0)
      _factory.setVarNum(_varCount + nbOfVar)

    _factory.ithVar(_varCount)
  }

  /** Increment the number of produced variable and return the corresponding BDD
    * @param variable
    *   the variable
    * @return
    *   a BDD node
    */
  def getVar(variable: Int): JavaBDD = {
    if (variable > _varCount)
      _varCount = variable
    _factory.ithVar(variable)
  }

  /** Return the ith var of the JavaBDD factory
    * @param i
    *   the index of the BDD node
    * @return
    *   BDD node
    */
  def getIthVar(i: Int): JavaBDD = {
    _factory.ithVar(i)
  }

  /** Reinitialise the JavaBDD factory to initial variable number
    */
  def reset(): Unit = {
    _varCount = 0
    _factory.reset()
    _factory.setVarNum(nbOfVar)

  }

  /** @return
    *   zero terminal
    */
  def zero(): JavaBDD = {
    _factory.zero()
  }

  /** @return
    *   one terminal
    */
  def one(): JavaBDD = {
    _factory.one()
  }

  /** Delegate BDD AND to JavaBDD factory
    * @param left
    *   BDD
    * @param right
    *   BDD
    * @return
    *   the resulting BDD
    */
  def andBDD(left: JavaBDD, right: JavaBDD): JavaBDD = {
    left.and(right)
  }

  /** Delegate BDD OR to JavaBDD factory
    * @param left
    *   BDD
    * @param right
    *   BDD
    * @return
    *   the resulting BDD
    */
  def orBDD(left: JavaBDD, right: JavaBDD): JavaBDD = {
    left.or(right)
  }

  /** Delegate BDD NOT to JavaBDD
    * @param arg
    *   initial BDD
    * @return
    *   negated BDD
    */
  def notBDD(arg: JavaBDD): JavaBDD = {
    arg.id().not()
  }

  /** Use JavaBDD replace to build new BDD where a given variable is replaced by
    * another one
    * @param replace
    *   the initial variable to replace
    * @param by
    *   the new variable
    * @param in
    *   the BDD
    * @return
    *   the modified BDD
    */
  def replaceVar(replace: Int, by: Int, in: JavaBDD): JavaBDD = {
    val pair = _factory.makePair(replace, by)
    in.replace(pair)
  }

  /** Build a BDD bu applying the formula v.high + (neg v).low
    * @param variable
    *   the variable labelling the BDD
    * @param high
    *   the high son
    * @param low
    *   the low son
    * @return
    *   the resulting BDD
    */
  def mkNode(variable: Int, high: JavaBDD, low: JavaBDD): JavaBDD = {
    getIthVar(variable).and(high).or(getIthVar(variable).not().and(low))
  }

  /** Send dispose signal to JavaBDD factory
    */
  def dispose(): Unit = {
    _factory.done()
  }

  /** Build the map from BDD identifier to BDD
    * @return
    *   the mapping from BDD to labelled variable
    */
  def getVarMap: Map[JavaBDD, Int] = {
    (0 to _varCount).map(i => getIthVar(i) -> i).toMap
  }
}

/** Generic BDD factory over the type of BDD variables
  * @tparam Var
  *   the type of variable labelled on BDD nodes
  */
trait GenBDDFactory[Var] extends BaseBDDFactory[Var, JavaBDD] {

  object FactoryImplicits {
    import scala.language.implicitConversions

    /** Transform variable to their BDD
      * @param variable
      *   the variable labelled on the root node
      * @return
      *   the BDD "< v, 1, 0>"
      */
    implicit def toJavaBDD(variable: Var): JavaBDD = getVar(variable)
  }

  protected val _factory: JavaBDDFactory
  protected val nbOfVar: Int
  protected val varMap: mutable.HashMap[Var, Int]

  /** Initialise a JavaBDD factory
    * @param numberOfVar
    *   maximum number of variables in BDDs
    * @param cacheSize
    *   initial size of the cache table containing BDD nodes
    * @return
    */
  def initFactory(numberOfVar: Int, cacheSize: Int): JavaBDDFactory = {
    var numberOfNodes = numberOfVar * numberOfVar
    numberOfNodes = Math.max(cacheSize, numberOfNodes)
    val fac = JavaBDDFactory.init("java", numberOfNodes, cacheSize)
    if (fac.varNum() < numberOfVar)
      fac.setVarNum(numberOfVar)
    fac.autoReorder(JavaBDDFactory.REORDER_WIN2)
    fac
  }

  /** Try to find the BDD of the variable in the correspondence table and return
    * it if existing, otherwise generate a new BDD
    * @param variable
    *   the variable
    * @return
    *   a BDD node
    */
  def getVar(variable: Var): JavaBDD = {
    varMap.get(variable) match {
      case Some(i) =>
        getIthVar(i)
      case None =>
        val varId = varMap.size + 1
        if ((varId % nbOfVar) == 0)
          _factory.setVarNum(varMap.size + nbOfVar)
        val bdd = _factory.ithVar(varId)
        varMap += (variable -> bdd.`var`())
        bdd
    }
  }

  /** Reinitialise the JavaBDD factory to initial variable number
    */
  def reset(): Unit = {
    varMap.clear()
    _factory.reset()
    _factory.setVarNum(nbOfVar)
  }

  /** @return
    *   zero terminal
    */
  def zero(): JavaBDD = {
    _factory.zero()
  }

  /** @return
    *   one terminal
    */
  def one(): JavaBDD = {
    _factory.one()
  }

  /** Return the BDD labelled by the ith variable
    * @param i
    *   the index of the BDD node
    * @return
    *   BDD node
    */
  def getIthVar(i: Int): JavaBDD = {
    _factory.ithVar(i)
  }

  /** Delegate BDD AND to JavaBDD factory
    * @param left
    *   BDD
    * @param right
    *   BDD
    * @return
    *   the resulting BDD
    */
  def andBDD(left: JavaBDD, right: JavaBDD): JavaBDD = {
    left.and(right)
  }

  /** Delegate BDD OR to JavaBDD factory
    * @param left
    *   BDD
    * @param right
    *   BDD
    * @return
    *   the resulting BDD
    */
  def orBDD(left: JavaBDD, right: JavaBDD): JavaBDD = {
    left.or(right)
  }

  /** Delegate BDD Not to JavaBDD factory
    * @param arg
    *   initial BDD
    * @return
    *   negated BDD
    */
  def notBDD(arg: JavaBDD): JavaBDD = {
    arg.id().not()
  }

  /** @param replace
    *   the initial variable to replace
    * @param by
    *   the new variable
    * @param in
    *   the BDD
    * @return
    *   the modified BDD
    */
  def replaceVar(replace: Var, by: Var, in: JavaBDD): JavaBDD = {
    {
      for (
        i1 <- varMap.get(replace);
        i2 <- varMap.get(by)
      )
        yield {
          in.replace(_factory.makePair(i1, i2))
        }
    }.getOrElse(throw new Exception(s"unknown variable $replace or $by"))
  }

  /** Build a BDD bu applying the formula v.high + (neg v).low
    * @param variable
    *   the variable labelling the BDD
    * @param high
    *   the high son
    * @param low
    *   the low son
    * @return
    *   the resulting BDD
    */
  def mkNode(variable: Var, high: JavaBDD, low: JavaBDD): JavaBDD = {
    varMap.get(variable) match {
      case Some(index) =>
        getIthVar(index).and(high).or(getIthVar(index).not().and(low))
      case None =>
        throw new Exception(s"unknown variable $variable")
    }

  }

  def getPathCount(bdd: JavaBDD, weights: Map[Var, Int] = Map.empty): BigInt = {
    if (weights.isEmpty)
      BigInt(bdd.pathCount().toLong)
    else {
      val _memo = mutable.HashMap.empty[JavaBDD, BigInt]

      def memoRedSatCount(x: JavaBDD): BigInt = _memo.getOrElseUpdate(
        x,
        if (x.isOne)
          1
        else if (x.isZero)
          0
        else
          (for {
            v <- getVarOf(x)
            w <- weights.get(v)
          } yield memoRedSatCount(x.low()) + w * memoRedSatCount(x.high()))
            .getOrElse(memoRedSatCount(x.low()) + memoRedSatCount(x.high()))
      )

      memoRedSatCount(bdd)
    }
  }

  def getSatCount(bdd: JavaBDD, weights: Map[Var, Int] = Map.empty): BigInt = {
    val _memo = mutable.HashMap.empty[(JavaBDD, Int), BigInt]

    def memoRedSatCount(x: JavaBDD, remainingDecisions: Int): BigInt =
      _memo.getOrElseUpdate(
        (x, remainingDecisions),
        if (x.isOne)
          BigInt(2).pow(remainingDecisions)
        else if (x.isZero)
          0
        else
          (for {
            v <- getVarOf(x)
            w <- weights.get(v)
          } yield memoRedSatCount(
            x.low(),
            remainingDecisions - 1
          ) + w * memoRedSatCount(x.high(), remainingDecisions - 1)).getOrElse(
            memoRedSatCount(x.low(), remainingDecisions - 1) + memoRedSatCount(
              x.high(),
              remainingDecisions - 1
            )
          )
      )

    memoRedSatCount(bdd, varMap.size)
  }

  /** Send dispose signal to JavaBDD factory
    */
  def dispose(): Unit = {
    _factory.done()
    varMap.clear()
  }

  /** @return
    *   the mapping from BDD to labelled variable
    */
  def getVarMap: Map[JavaBDD, Var] = {
    varMap.map(kv => getIthVar(kv._2) -> kv._1).toMap
  }

  def getVarOf(bdd: JavaBDD): Option[Var] =
    for { r <- varMap.find(_._2 == bdd.`var`()) } yield r._1
}
