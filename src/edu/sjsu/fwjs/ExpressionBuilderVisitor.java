package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.fwjs.parser.FeatherweightJavaScriptBaseVisitor;
import edu.sjsu.fwjs.parser.FeatherweightJavaScriptParser;

public class ExpressionBuilderVisitor extends FeatherweightJavaScriptBaseVisitor<Expression>{
    @Override
    public Expression visitProg(FeatherweightJavaScriptParser.ProgContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i=0; i<ctx.stat().size(); i++) {
            Expression exp = visit(ctx.stat(i));
            if (exp != null) stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    @Override
    public Expression visitBareExpr(FeatherweightJavaScriptParser.BareExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitIfThenElse(FeatherweightJavaScriptParser.IfThenElseContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block(0));
        Expression els = visit(ctx.block(1));
        return new IfExpr(cond, thn, els);
    }

    @Override
    public Expression visitIfThen(FeatherweightJavaScriptParser.IfThenContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block());
        return new IfExpr(cond, thn, null);
    }

    @Override
    public Expression visitInt(FeatherweightJavaScriptParser.IntContext ctx) {
        int val = Integer.valueOf(ctx.INT().getText());
        return new ValueExpr(new IntVal(val));
    }

    @Override
    public Expression visitParens(FeatherweightJavaScriptParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitFullBlock(FeatherweightJavaScriptParser.FullBlockContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i=1; i<ctx.getChildCount()-1; i++) {
            Expression exp = visit(ctx.getChild(i));
            stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    /**
     * Converts a list of expressions to one sequence expression,
     * if the list contained more than one expression.
     */
    private Expression listToSeqExp(List<Expression> stmts) {
        if (stmts.isEmpty()) return null;
        Expression exp = stmts.get(0);
        for (int i=1; i<stmts.size(); i++) {
            exp = new SeqExpr(exp, stmts.get(i));
        }
        return exp;
    }

    @Override
    public Expression visitSimpBlock(FeatherweightJavaScriptParser.SimpBlockContext ctx) {
        return visit(ctx.stat());
    }


    // New code

    @Override
    public Expression visitWhile(FeatherweightJavaScriptParser.WhileContext ctx) {
      Expression cond = visit(ctx.expr());
      Expression body = visit(ctx.block());

      return new WhileExpr(cond, body);
    }

    @Override
    public Expression visitPrint(FeatherweightJavaScriptParser.PrintContext ctx) {
      Expression e = visit(ctx.stat());
      return new PrintExpr(e);
    }

    @Override
   	public Expression visitEmpty(FeatherweightJavaScriptParser.EmptyContext ctx) {
    	return new ValueExpr(new NullVal());
    }

    @Override
    public Expression visitReference(FeatherweightJavaScriptParser.ReferenceContext ctx) {
    	String name = ctx.IDENTIFIER().getText();
    	return new VarExpr(name);
    }

    @Override
    public Expression visitMulDivMod(FeatherweightJavaScriptParser.MulDivModContext ctx) {
    	String mulDivModOp = ctx.op.getText();
    	Expression exprLeft = visit(ctx.expr(0));
    	Expression exprRight = visit(ctx.expr(1));

    	Op operator = null;

    	switch(mulDivModOp) {
    		case "*":
    			operator = Op.MULTIPLY;
    			break;
			case "/":
				operator = Op.DIVIDE;
				break;
			case "%":
				operator = Op.MOD;
				break;
    	}

    	return new BinOpExpr(operator, exprLeft, exprRight);
    }

	@Override
    public Expression visitBool(FeatherweightJavaScriptParser.BoolContext ctx) {
    	boolean bool = Boolean.valueOf(ctx.BOOL().getText());
    	return new ValueExpr(new BoolVal(bool));
    }

    @Override
    public Expression visitNull(FeatherweightJavaScriptParser.NullContext ctx) {
        return new ValueExpr(new NullVal());
    }

    @Override
    public Expression visitAddSub(FeatherweightJavaScriptParser.AddSubContext ctx) {
    	String addSubOp = ctx.op.getText();
    	Expression exprLeft = visit(ctx.expr(0));
    	Expression exprRight = visit(ctx.expr(1));

    	Op operator = null;

    	switch(addSubOp) {
    		case "+":
    			operator = Op.ADD;
    			break;
			case "-":
				operator = Op.SUBTRACT;
				break;
    	}

    	return new BinOpExpr(operator, exprLeft, exprRight);
    }

    @Override
    public Expression visitFunctDec(FeatherweightJavaScriptParser.FunctDecContext ctx) {
    	List<String> params = new ArrayList<String>();
    	Expression body;

    	/*for (int i = 0; ctx.IDENTIFIER(i) != null; i++) {
    		params.add(ctx.IDENTIFIER(i).getText());
    	}*/

    	for (int i = 2; i < ctx.getChildCount() - 2; i = i + 2){
    		params.add(ctx.getChild(i).getText());
    	}
    	body = visit(ctx.block());

    	return new FunctionDeclExpr(params, body);
    }

    @Override
    public Expression visitVarDec(FeatherweightJavaScriptParser.VarDecContext ctx) {
    	String varName = ctx.IDENTIFIER().getText();
    	Expression e = visit(ctx.expr());

    	return new VarDeclExpr(varName, e);
    }

    @Override
    public Expression visitFunctApp(FeatherweightJavaScriptParser.FunctAppContext ctx) {
    	Expression funcIdent = new VarExpr(ctx.getChild(0).getText());
    	List<Expression> args = new ArrayList<Expression>();

    	/*for (int i = 0; ctx.expr(i) != null; i++) {
    		args.add(visit(ctx.expr(i)));
    	}*/

    	for (int i = 2; i < ctx.getChildCount() - 1; i = i + 2) {
    		args.add(visit(ctx.getChild(i)));
    	}

    	return new FunctionAppExpr(funcIdent, args);
    }

    @Override
    public Expression visitAssign(FeatherweightJavaScriptParser.AssignContext ctx) {
    	String varName = ctx.IDENTIFIER().getText();
    	Expression e = visit(ctx.expr());

    	return new AssignExpr(varName, e);
    }

    @Override
    public Expression visitComp(FeatherweightJavaScriptParser.CompContext ctx) {
    	String compOp = ctx.op.getText();
    	Expression expLeft = visit(ctx.expr(0));
    	Expression expRight = visit(ctx.expr(1));

    	Op operator = null;

    	switch(compOp) {
    		case "<":
    			operator = Op.LT;
    			break;
    		case ">":
    			operator = Op.GT;
    			break;
    		case "<=":
    			operator = Op.LE;
    			break;
    		case ">=":
    			operator = Op.GE;
    			break;
    		case "==":
    			operator = Op.EQ;
    			break;
    	}

    	return new BinOpExpr(operator, expLeft, expRight);
    }
}
