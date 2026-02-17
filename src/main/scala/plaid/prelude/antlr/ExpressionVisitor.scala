package plaid.prelude.antlr

import plaid.prelude.PreludeBaseVisitor
import plaid.prelude.PreludeParser.*
import plaid.prelude.ast.*

import scala.jdk.CollectionConverters.*

object ExpressionVisitor extends PreludeBaseVisitor[Expr] {

  override def visitVectorExpr(ctx: VectorExprContext) = VectorExpr(
    es = ctx.expr().asScala.map(visit).toList)

  override def visitBVConcatExpr(ctx: BVConcatExprContext) = BVConcatExpr(
    e1 = visit(ctx.expr(0)),
    e2 = visit(ctx.expr(1)))

  override def visitBVAddExpr(ctx: BVAddExprContext) = BVAddExpr(
    e1 = visit(ctx.expr(0)),
    e2 = visit(ctx.expr(1)))

  override def visitBVSubExpr(ctx: BVSubExprContext) = BVSubExpr(
    e1 = visit(ctx.expr(0)),
    e2 = visit(ctx.expr(1)))

  override def visitBVMultExpr(ctx: BVMultExprContext) = BVMultExpr(
    e1 = visit(ctx.expr(0)),
    e2 = visit(ctx.expr(1)))

  override def visitParenExpr(ctx: ParenExprContext): Expr =
    visit(ctx.expr())

  override def visitFunctionCallExpr(ctx: FunctionCallExprContext) = CallExpr(
    id = Identifier(ctx.ident().getText),
    parms = ctx.expr().asScala.map(visit).toList)

  override def visitLetExpr(ctx: LetExprContext) = LetExpr(
    y = Identifier(ctx.ident().getText),
    e1 = visit(ctx.expr(0)),
    e2 = visit(ctx.expr(1)))

  override def visitAtExpr(ctx: AtExprContext) = AtExpr(
    e1 = visit(ctx.expr(0)),
    e2 = visit(ctx.expr(1)))

  override def visitFieldExpr(ctx: FieldExprContext) = FieldExpr(
    elements = ctx.flddecl().asScala
      .map(x => Identifier(x.ident().getText) -> visit(x.expr()))
      .toMap)

  override def visitFieldSelectExpr(ctx: FieldSelectExprContext) = FieldSelectExpr(
    e = visit(ctx.expr()),
    l = Identifier(ctx.ident().getText))

  override def visitSecretExpr(ctx: SecretExprContext) = SecretExpr(
    e = visit(ctx.expr()))

  override def visitRandomExpr(ctx: RandomExprContext) = RandomExpr(
    e = visit(ctx.expr()))

  override def visitMessageExpr(ctx: MessageExprContext) = MessageExpr(
    e = visit(ctx.expr()))

  override def visitPublicExpr(ctx: PublicExprContext) = PublicExpr(
    e = visit(ctx.expr()))

  override def visitOutputExpr(ctx: OutputExprContext) = OutputExpr(
    if ctx.getChildCount > 1
    then visit(ctx.expr())
    else Str("~"))

  override def visitPlusExpr(ctx: PlusExprContext) = PlusExpr(
    e1 = visit(ctx.expr(0)),
    e2 = visit(ctx.expr(1)))

  override def visitMinusExpr(ctx: MinusExprContext) = MinusExpr(
    e = visit(ctx.expr()))

  override def visitConcatExpr(ctx: ConcatExprContext) = ConcatExpr(
    e1 = visit(ctx.expr(0)),
    e2 = visit(ctx.expr(1)))

  override def visitTimesExpr(ctx: TimesExprContext) = TimesExpr(
    e1 = visit(ctx.expr(0)),
    e2 = visit(ctx.expr(1)))

  override def visitStr(ctx: StrContext) = Str(
    str = ctx.getText.replaceAll("\"", ""))

  override def visitNum(ctx: NumContext) = Num(
    num = ctx.getText.toInt)

  override def visitIdentExpr(ctx: IdentExprContext) = Identifier(
    name = ctx.getText)
}
