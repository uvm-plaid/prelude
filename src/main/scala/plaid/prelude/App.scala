package plaid.prelude

import picocli.CommandLine
import picocli.CommandLine.{Command, Option, Parameters}
import plaid.prelude.antlr.Loader
import plaid.prelude.ast.ListConstraintFuncExt.expandAll
import plaid.prelude.ast.ListExprFuncExt.expandAll
import plaid.prelude.cvc.TermFactory
import plaid.prelude.logic.VerificationStatus.FAIL
import plaid.prelude.logic.{contracts, verify}

import java.io.File
import java.nio.file.Files
import scala.compiletime.uninitialized

@main
def main(args: String*): Unit =
  val exitCode = CommandLine(App()).execute(args*)
  System.exit(exitCode)

@Command(
  name = "prelude",
  version = Array("prelude-dev"),
  mixinStandardHelpOptions = true)
class App extends Runnable {

  @Option(names = Array("--field-size", "-s"), description = Array("Order of the finite field"))
  var fieldSize: String = "2"

  @Parameters(paramLabel = "<path>", description = Array("Path to a prelude source file"))
  var path: String = uninitialized

  override def run(): Unit =
    val src = Files.readString(File(path).toPath)
    val ast = Loader.program(src)
    val exprFns = ast.exprFuncs.expandAll()
    val constraintFns = ast.constraintFuncs.expandAll(exprFns)
    val contracts = ast.cmdFuncs.contracts(exprFns, constraintFns)
    val statuses = contracts.map(_.verify(TermFactory(fieldSize)))
    val failures = statuses.count(x => x == FAIL)

    contracts.zip(statuses).foreach((contract, status) =>
      println(s"$status ${contract.f.id.name}"))

    println(s"$failures verification failures")
}
