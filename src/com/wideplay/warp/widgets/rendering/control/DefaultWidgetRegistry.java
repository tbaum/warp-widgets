package com.wideplay.warp.widgets.rendering.control;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.wideplay.warp.widgets.Evaluator;
import com.wideplay.warp.widgets.Renderable;
import com.wideplay.warp.widgets.compiler.EvaluatorCompiler;
import com.wideplay.warp.widgets.compiler.ExpressionCompileException;
import com.wideplay.warp.widgets.compiler.Parsing;
import com.wideplay.warp.widgets.compiler.RepeatToken;
import com.wideplay.warp.widgets.routing.PageBook;
import net.jcip.annotations.ThreadSafe;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe @Singleton
class DefaultWidgetRegistry implements WidgetRegistry {
    public static final String TEXT_WIDGET = "__w:wRawText_Widget"; 

    private final Injector injector;
    private final Evaluator evaluator;
    private final PageBook pageBook;

    private final ConcurrentMap<String, WidgetWrapper> widgets = new ConcurrentHashMap<String, WidgetWrapper>();

    @Inject
    public DefaultWidgetRegistry(Evaluator evaluator, PageBook pageBook, Injector injector) {
        this.evaluator = evaluator;
        this.pageBook = pageBook;
        this.injector = injector;


        //register core widgets
        addCoreControllers();
    }

    private void addCoreControllers() {
        //TODO make these case sensitive
        add("textfield", TextFieldWidget.class);
        add("repeat", RepeatWidget.class);
        add("showif", ShowIfWidget.class);
        add("meta", HeaderWidget.class);
        add("choose", ChooseWidget.class);
        add("include", IncludeWidget.class);
    }

    public void add(String key, Class<? extends Renderable> widget) {
        widgets.put(key.toLowerCase().trim(), WidgetWrapper.forWidget(key, widget));
    }

    public boolean isSelfRendering(String widget) {
        WidgetWrapper wrapper = widgets.get(widget);

        if (null == wrapper)
            throw new NoSuchWidgetException("No widget found matching the name: @" + widget + " ; Did you forget to" +
                    " annotate your widget class with @Embed?");
        
        return wrapper.isSelfRendering();
    }
    

    public RepeatToken parseRepeat(String expression) {
        //parse and convert widget into metadata annotation
        final Map<String, String> bindMap = Parsing.toBindMap(expression);

        //noinspection OverlyComplexAnonymousInnerClass
        return new RepeatToken() {

            public String items() {
                return bindMap.get(RepeatToken.ITEMS);
            }

            public String var() {
                final String var = bindMap.get(RepeatToken.VAR);

                return null != var ? Parsing.stripQuotes(var) : null;
            }

            public String pageVar() {
                final String pageVar = bindMap.get(RepeatToken.PAGE_VAR);

                return null == pageVar ? RepeatToken.DEFAULT_PAGEVAR : pageVar;
            }
        };
    }


    public XmlWidget xmlWidget(WidgetChain childsChildren, String elementName, Map<String, String> attribs,
                                EvaluatorCompiler compiler) throws ExpressionCompileException {

        final XmlWidget widget = new XmlWidget(childsChildren, elementName, compiler, attribs);
        injector.injectMembers(widget);

        return widget;
    }

    public Renderable newWidget(String key, String expression, WidgetChain widgetChain, EvaluatorCompiler compiler)
            throws ExpressionCompileException {
        if (!widgets.containsKey(key))
            throw new NoSuchWidgetException("No such widget registered (did you add it correctly in module setup?): " + key);

        if (TEXT_WIDGET.equals(key))
            return new TextWidget(null, compiler);

        //otherwise construct via reflection (all widgets MUST have
        // a constructor with: widgetchain, expression, evaluator; in that order)
        final Renderable widget = widgets
                .get(key)
                .newWidget(widgetChain, expression, evaluator, pageBook);

        //add some injection (some widgets require it). It's a bit hacky, maybe we can reimplement some stuff later with @AssistedInject
        injector.injectMembers(widget);

        return widget;
    }

    public Renderable requireWidget(String template, EvaluatorCompiler compiler) throws ExpressionCompileException {
        return new RequireWidget(template, compiler);
    }

    public Renderable textWidget(String template, EvaluatorCompiler compiler) throws ExpressionCompileException {
        return new TextWidget(template, compiler);
    }

    public void addEmbed(String embedAs) {
        add(embedAs, EmbedWidget.class);
    }

    public void addArgument(String callWith) {
        add(callWith, ArgumentWidget.class);
    }

}
