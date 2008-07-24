package com.wideplay.warp.widgets;

import com.wideplay.warp.util.Token;
import com.wideplay.warp.widgets.rendering.EvaluatorCompiler;
import com.wideplay.warp.widgets.rendering.ExpressionCompileException;
import com.wideplay.warp.widgets.rendering.SelfRendering;
import net.jcip.annotations.ThreadSafe;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe @SelfRendering
class TextWidget implements Renderable {
    private final List<Token> tokenizedTemplate;     //TODO store some metrics to allocate buffers later

    TextWidget(String template, EvaluatorCompiler compiler) throws ExpressionCompileException {

        //compile token stream
        tokenizedTemplate = compiler.tokenizeAndCompile(template);
    }

    public void render(Object bound, Respond respond) {

        //render template from tokens
        StringBuilder builder = new StringBuilder();
        for (Token token : tokenizedTemplate) {
            builder.append(token.render(bound));
        }

        respond.write(builder.toString());
    }


    public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return Collections.emptySet();
    }
}
