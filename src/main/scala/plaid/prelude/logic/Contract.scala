package plaid.prelude.logic

import plaid.prelude.ast.ListCmdFuncExt.dependencyOrdered
import plaid.prelude.ast.{AndConstraint, AssertCmd, AssignCmd, CallCmd, Cmd, CmdFunc, Constraint, ConstraintFunc, EqualConstraint, ExprFunc, Identifier, ImpliesConstraint, TrueConstraint}

case class PrePost(pre: Constraint, post: Constraint)

extension (trg: Cmd)
  /** Gets the pre-conditions and post-conditions per the inference rules. */
  def prePost(contracts: List[Contract]): PrePost = trg match
    case AssertCmd(e1, e2, party) =>
      val a = e1.indexParties(Some(party))
      val b = e2.indexParties(Some(party))
      PrePost(EqualConstraint(a, b), TrueConstraint())
    case AssignCmd(e1, e2) => PrePost(
      pre = TrueConstraint(),
      post = EqualConstraint(e1, e2))
    case CallCmd(id, parms) =>
      val contract = contracts.lookup(id)
      val formalParms = contract.f.typedVariables.map(_.y)
      val actualParms = parms.map(_.indexParties())
      val bindings = formalParms.zip(actualParms).toMap
      val pre = contract.customPre.expand(bindings = bindings)
      val post = contract.customPost.expand(bindings = bindings)
      PrePost(pre, post)
    case _ => throw Exception(s"Unsupported $trg")

/**
 * Tracks for a function what its default (and possibly custom) preconditions
 * and postconditions are, as well as whether the function needs hardpack
 * checking.
 */
case class Contract(
  f: CmdFunc,
  customPre: Constraint,
  customPost: Constraint,
  defaultPre: Constraint,
  defaultPost: Constraint,
  check: Boolean)

extension (trg: List[Contract])
  /** Look up the contract for the specified command function identifier. */
  def lookup(id: Identifier): Contract = trg.find(_.f.id == id).get

extension (trg: List[CmdFunc])
  /** Calculate contracts for all the commands (which must be expanded). */
  def contracts(exprFns: List[ExprFunc], constraintFns: List[ConstraintFunc]): List[Contract] = trg
    .dependencyOrdered()
    .foldLeft(Nil) { (acc, x) => x.contract(exprFns, constraintFns, acc) :: acc }

extension (trg: CmdFunc)
  def requiresChecking(): Boolean =
    trg.body.nonEmpty && (trg.precond.nonEmpty || trg.postcond.nonEmpty)

  def contract(exprFns: List[ExprFunc], constraintFns: List[ConstraintFunc], contractCtx: List[Contract]): Contract =
    val body = trg.body.flatMap(_.expand(exprFns))
    val start = PrePost(TrueConstraint(), TrueConstraint())
    val triples = body.map(_.prePost(contractCtx))
    val ctx = triples.foldLeft(start) { (acc, cmd) =>
      PrePost(
        pre = AndConstraint(acc.pre, ImpliesConstraint(acc.post, cmd.pre)),
        post = AndConstraint(acc.post, cmd.post))
    }

    Contract(
      f = trg,
      customPre = trg.precond.getOrElse(ctx.pre).expand(exprFns, constraintFns),
      customPost = trg.postcond.getOrElse(ctx.post).expand(exprFns, constraintFns),
      defaultPre = ctx.pre.expand(exprFns, constraintFns),
      defaultPost = ctx.post.expand(exprFns, constraintFns),
      check = trg.requiresChecking())
