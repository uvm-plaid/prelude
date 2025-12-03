package plaid.prelude.logic

import io.github.cvc5.{Kind, Solver, Term}
import plaid.prelude.ast.AndConstraint
import plaid.prelude.cvc.TermFactory
import plaid.prelude.logic.VerificationStatus.{FAIL, PASS, SKIP}

extension (trg: TermFactory)
  /** Determine if the term has a set of assignments that make it true. */
  def satisfiable(e: Term): Boolean =
    findModelSatisfying(e).nonEmpty

  /** Find a set of assignments that make a term true, if one exists. */
  def findModelSatisfying(term: Term): Option[Map[Term, Int]] =
    val solver = Solver(trg.termManager)
    solver.setOption("produce-models", "true")
    solver.assertFormula(term)
    val result = solver.checkSat()
    if (!result.isSat) return None

    val mod = Integer.parseInt(trg.sort.getFiniteFieldSize)
    val model = Some(trg.memories.map(m =>
      val value = solver.getValue(m.term)
      val finiteFieldValue = Integer.parseInt(value.getFiniteFieldValue)
      m.term -> Math.floorMod(finiteFieldValue, mod)).toMap)

    println(s"Found a model: $model")
    model

  /** E1 entails E2 if there is no model that satisfies (E1 AND not E2) */
  def entails(e1: Term, e2: Term): Boolean =
    val notE2 = trg.termManager.mkTerm(Kind.NOT, e2)
    val e1EntailsNotE2 = trg.termManager.mkTerm(Kind.AND, e1, notE2)
    !satisfiable(e1EntailsNotE2)

enum VerificationStatus {
  case PASS
  case FAIL
  case SKIP
}

extension (trg: Contract)
  /** Finds any entailments in a function contract that are false. */
  def verify(cvc: TermFactory): VerificationStatus =
    if !trg.check then return SKIP
    val bindings = trg.f.typedVariables.map(x => x.y -> x.t.freshValue()).toMap
    val customPre = trg.customPre.expand(bindings = bindings)
    val customPost = trg.customPost.expand(bindings = bindings)
    val defaultPre = trg.defaultPre.expand(bindings = bindings)
    val defaultPost = trg.defaultPost.expand(bindings = bindings)

//      // TODO Validation shouldn't really go here, it needs to be revisited
//      val ctx = trg.f.id.name
//      val constraintErrors = a.checkProperExpansion(ctx) ++ b.checkProperExpansion(ctx)
//      constraintErrors.foreach(x => println(s"ERROR ${x.ctx}: ${x.msg}: ${x.offender}"))
//      if constraintErrors.nonEmpty then
//        throw Exception("Found errors in the constraints, bailing out.")

    val pre = cvc.entails(cvc.toTerm(customPre), cvc.toTerm(defaultPre))
    val post = cvc.entails(cvc.toTerm(AndConstraint(customPre, defaultPost)), cvc.toTerm(customPost))
    if pre && post then PASS else FAIL
