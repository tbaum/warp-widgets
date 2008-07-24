package com.wideplay.warp.widgets;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.wideplay.warp.util.Token;
import com.wideplay.warp.widgets.rendering.Attributes;
import com.wideplay.warp.widgets.rendering.EvaluatorCompiler;
import com.wideplay.warp.widgets.rendering.ExpressionCompileException;
import com.wideplay.warp.widgets.rendering.SelfRendering;
import net.jcip.annotations.ThreadSafe;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * Widget renders an XML-like tag
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe @SelfRendering
class XmlWidget implements Renderable {
    private final WidgetChain widgetChain;
    private final boolean noChildren;
    private final String name;
    private final Map<String, List<Token>> attributes;
    private volatile Provider<HttpServletRequest> request;

    private static final Set<String> CONTEXTUAL_ATTRIBS;

    static {
        Set<String> set = new HashSet<String>();

        set.add("href");
        set.add("action");
        set.add("src");

        CONTEXTUAL_ATTRIBS = Collections.unmodifiableSet(set);
    }


    XmlWidget(WidgetChain widgetChain, String name, EvaluatorCompiler compiler,
              @Attributes Map<String, String> attributes) throws ExpressionCompileException {
        this.widgetChain = widgetChain;
        this.name = name;
        this.attributes = Collections.unmodifiableMap(compile(attributes, compiler));
        this.noChildren = widgetChain instanceof TerminalWidgetChain;
    }

    //converts a map of name:value attrs into a map of name:token attrs
    private Map<String, List<Token>> compile(Map<String, String> attributes, EvaluatorCompiler compiler) throws ExpressionCompileException {
        Map<String, List<Token>> map = new LinkedHashMap<String, List<Token>>();

        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            map.put(attribute.getKey(), compiler.tokenizeAndCompile(attribute.getValue()));
        }

        return map;
    }

    public void render(Object bound, Respond respond) {
        respond.write('<');
        respond.write(name);

        respond.write(' ');

        //write attributes
        for (Map.Entry<String, List<Token>> attribute : attributes.entrySet()) {
            respond.write(attribute.getKey());
            respond.write("=\"");

            final List<Token> tokenList = attribute.getValue();
            for (int i = 0; i < tokenList.size(); i++) {
                Token token = tokenList.get(i);

                if (token.isExpression()) {
                    final Object value = token.render(bound);

                    //normalize nulls to "null" (i.e. let responder take care of writing it)
                    respond.write((null == value) ? (String)value : value.toString());
                }
                else {
                    respond.write(contextualizeIfNeeded(attribute.getKey(), (0 == i), (String) token.render(bound)));
                }
            }

            respond.write("\" ");
        }

        respond.chew();

        //write children
        if (noChildren) {
            respond.write("/>");    //write self-closed tag
        } else {
            respond.write('>');
            widgetChain.render(bound, respond);

            //close tag
            respond.write("</");
            respond.write(name);
            respond.write('>');
        }

    }

    private String contextualizeIfNeeded(String attribute, boolean isFirstToken, String raw) {
        if (isFirstToken && CONTEXTUAL_ATTRIBS.contains(attribute)) {
            //add context to path if needed
            if (raw.startsWith("/"))
                raw = String.format("%s%s", request.get().getContextPath(), raw);
        }
        
        return raw;
    }


    public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return widgetChain.collect(clazz);
    }

    @Inject
    public void setRequestProvider(Provider<HttpServletRequest> request) {
        this.request = request;
    }
}