package plaid.prelude.logic

import io.github.cvc5.{Kind, Term}
import plaid.prelude.ast.AndConstraint
import plaid.prelude.cvc.TermFactory
import plaid.prelude.logic.VerificationStatus.{FAIL, PASS, SKIP}

extension (trg: TermFactory)
  /** Determine if the term has a set of assignments that make it true. */
  def satisfiable(e: Term): Boolean =
    findModelSatisfying(e).nonEmpty

  /** Find a set of assignments that make a term true, if one exists. */
  def findModelSatisfying(term: Term): Option[Map[String, String]] =
    val solver = trg.createSolver()
    solver.setOption("produce-models", "true")
    solver.setOption("incremental", "false")
    solver.assertFormula(term)
    val result = solver.checkSat()
    if (result.isNull || result.isUnknown) throw Exception("Failed to establish satisfiability or unsatisfiability")
    if (result.isUnsat) return None

    Some(trg.memories.map(m =>
      val value = solver.getValue(m.term)
      m.name -> trg.valueOf(value)).toMap)

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
    println(s"* ${trg.f.id.name}")
    if !trg.check then return SKIP
    println("  Generating fresh values...")
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

    println("  Converting to cvc5 terms...")
    val customPreTerm = cvc.toTerm(customPre)
    val defaultPreTerm = cvc.toTerm(defaultPre)
    val andTerm = cvc.toTerm(AndConstraint(customPre, defaultPost))
    val customPostTerm = cvc.toTerm(customPost)

    println("  Solving...")
    val pre = cvc.entails(customPreTerm, defaultPreTerm)
    val post = cvc.entails(andTerm, customPostTerm)
    if pre && post then PASS else FAIL
