package com.wideplay.warp.widgets.rendering;

import com.wideplay.warp.widgets.Evaluator;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class EvaluatorCompilerTest {
    private static final String A_NAME = "Dhanji";

    @Test
    public final void compileEvaluatorFromExpression() throws ExpressionCompileException {
        Evaluator compiled = new MvelEvaluatorCompiler(AType.class)
                                    .compile("name");

        //reading expression
        Object value = compiled.evaluate(null, new AType(A_NAME));

        assert A_NAME == value;
    }

    @Test
    public final void compileExpressionInvokingArbitraryMethod() throws ExpressionCompileException {
        Evaluator compiled = new MvelEvaluatorCompiler(AType.class)
                                    .compile("b.sigmatron('Hi')");

        //reading expression
        assert "Hi".equals(compiled.evaluate(null, new AType(A_NAME)));

    }

    @Test
    public final void compileExpressionInvokingArbitraryMethodAndTestReturn() throws ExpressionCompileException {
        Evaluator compiled = new MvelEvaluatorCompiler(AType.class)
                                    .compile("b.sigmatron(null)");

        //reading expression
        assert null == (compiled.evaluate(null, new AType(A_NAME)));

    }

    @Test(expectedExceptions = ExpressionCompileException.class)
    public final void failCompileExpressionInvokingArbitraryMethodThruInterface() throws ExpressionCompileException {
        Evaluator compiled = new MvelEvaluatorCompiler(AType.class)
                                    .compile("b.bkind.sigmatron(null)");

        //reading expression
//        assert null == (compiled.evaluate(null, new AType(A_NAME)));

    }

    @Test
    public final void compileExpressionInvokingArbitraryMethodThruInterface() throws ExpressionCompileException {
        Evaluator compiled = new MvelEvaluatorCompiler(AType.class)
                                    .compile("bkind.getDubdub()");

        //reading expression
        final AType anA = new AType(A_NAME);
        assert anA.getB().getDubdub().equals(compiled.evaluate(null, anA));

    }

    @Test
    public final void compileExpressionInvokingArbitraryMethodThruInterfaceAndRegular() throws ExpressionCompileException {
        Evaluator compiled = new MvelEvaluatorCompiler(AType.class)
                                    .compile("bkind.getDubdub() == b.dubdub");

        //reading expression
        //noinspection ConstantConditions
        assert (Boolean)compiled.evaluate(null, new AType(A_NAME));

    }

    @Test(expectedExceptions = ExpressionCompileException.class)
    public final void failCompileExpressionInvokingArbitraryMethodWithWrongArgs() throws ExpressionCompileException {
        Evaluator compiled = new MvelEvaluatorCompiler(AType.class)
                                    .compile("b.sigmatron()");

    }

    @Test(expectedExceptions = ExpressionCompileException.class)
    public final void failCompileDueToNameMismatch() throws ExpressionCompileException {
        new MvelEvaluatorCompiler(AType.class)
                                    .compile("anythingaling");


    }

    @Test(expectedExceptions = ExpressionCompileException.class)
    public final void failCompileDueToNameMismatchInDeeperObjectGraph() throws ExpressionCompileException {
        new MvelEvaluatorCompiler(AType.class)
                                    .compile("name.anythingaling");

    }

    @Test(expectedExceptions = ExpressionCompileException.class)
    public final void failCompileDueToNameMismatchInDeeperObjectGraph2() throws ExpressionCompileException {
        new MvelEvaluatorCompiler(AType.class)
                                    .compile("name.b.anythingaling");

    }

    @Test(expectedExceptions = ExpressionCompileException.class)
    public final void failCompileDueToMethodMismatchInDeeperObjectGraph() throws ExpressionCompileException {
        new MvelEvaluatorCompiler(AType.class)
                                    .compile("b.a.b.name.substring(1)");

    }

    @Test
    public final void compileMethodMatchInDeeperObjectGraph() throws ExpressionCompileException {
        assert A_NAME.substring(1).equals(new MvelEvaluatorCompiler(AType.class)
                                            .compile("b.aString.substring(1)")
                                            .evaluate(null, new AType(A_NAME)));


    }

    @Test
    public final void compileExpressionIntegerTypeMatchInDeeperObjectGraph() throws ExpressionCompileException {
        new MvelEvaluatorCompiler(AType.class)
                                    .compile("b.a.b.a.b.name / 44");

    }

    @Test
    public final void compileExpressionNumericTypeMatchInDeeperObjectGraph() throws ExpressionCompileException {
        new MvelEvaluatorCompiler(AType.class)
                                    .compile("b.a.b.a.b.dubdub / new Double(44.0)");

    }

    @Test(expectedExceptions = ExpressionCompileException.class)
    public final void failCompileExpressionNumericTypeMismatchInDeeperObjectGraph() throws ExpressionCompileException {
        new MvelEvaluatorCompiler(AType.class)
                                    .compile("b.a.b.a.b.name / new Double(44.0)");

    }

    @Test(expectedExceptions = ExpressionCompileException.class)
    public final void failCompileDueToPathMismatchInDeeperObjectGraph() throws ExpressionCompileException {
        new MvelEvaluatorCompiler(AType.class)
                                    .compile("name.b.a.b.name + 2");

    }

    @Test(expectedExceptions = ExpressionCompileException.class)
    public final void failCompileDueToTypeMismatchInDeeperObjectGraph() throws ExpressionCompileException {
        new MvelEvaluatorCompiler(AType.class)
                                    .compile("b.a.name - 2");
    }

    @Test
    public final void compileTypeMatchInDeeperObjectGraph() throws ExpressionCompileException {
        new MvelEvaluatorCompiler(AType.class)
                                    .compile("b.name - 2");
    }

    @Test(expectedExceptions = ExpressionCompileException.class)
    public final void failCompileDueToTypeMismatch() throws ExpressionCompileException {
        new MvelEvaluatorCompiler(AType.class)
                                    .compile("name - 2");
    }

    public static class AType {
        private String name;
        private BType b = new BType(45);
        private BKind bkind = new BType(400);

        public AType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public BType getB() {
            return b;
        }

        public BKind getBkind() {
            return bkind;
        }
    }

    public static class BType implements BKind {
        private Integer name;
        private Double dubdub = 100.0;
        private String aString = A_NAME;

        public AType getA() {
            return a;
        }

        private AType a;

        public BType(Integer name) {
            this.name = name;
        }

        public Integer getName() {
            return name;
        }

        public Double getDubdub() {
            return dubdub;
        }

        public String sigmatron(String s) {
            return s;
        }

        public String getAString() {
            return aString;
        }
    }

    public static interface BKind {
        Double getDubdub();
    }
}
