package com.wideplay.warp.widgets.rendering.control;

import com.google.inject.internal.ToStringBuilder;
import com.wideplay.warp.widgets.Evaluator;
import com.wideplay.warp.widgets.Renderable;
import com.wideplay.warp.widgets.rendering.SelfRendering;
import com.wideplay.warp.widgets.routing.PageBook;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
class WidgetWrapper {
    private final Class<? extends Renderable> clazz;
    private final Constructor<? extends Renderable> constructor;
    private final String key;
    private final boolean selfRendering;
    private final WidgetKind kind;

    private WidgetWrapper(Class<? extends Renderable> clazz, Constructor<? extends Renderable> constructor,
                          WidgetKind kind, String key) {
        this.kind = kind;
        this.clazz = clazz;
        this.constructor = constructor;
        this.key = key;

        selfRendering = clazz.isAnnotationPresent(SelfRendering.class);
    }

    public Renderable newWidget(WidgetChain widgetChain, String expression, Evaluator evaluator, PageBook pageBook) {
        try {


            return WidgetKind.NORMAL.equals(kind) ?
                    constructor.newInstance(widgetChain, expression, evaluator) :
                    constructor.newInstance(toArguments(widgetChain), expression, evaluator, pageBook, key);

        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Malformed Widget (this should never happen): " + clazz);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Could not construct an instance of " + clazz, e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not construct an instance of : " + clazz, e);
        }
    }

    private static Map<String, ArgumentWidget> toArguments(WidgetChain widgetChain) {
        Set<ArgumentWidget> arguments = widgetChain.collect(ArgumentWidget.class);

        Map<String, ArgumentWidget> map = new HashMap<String, ArgumentWidget>();

        for (ArgumentWidget argument : arguments) {
            map.put(argument.getName(), argument);
        }

        return map;
    }

    public static WidgetWrapper forWidget(String key, Class<? extends Renderable> widgetClass) {
        WidgetKind kind = EmbedWidget.class.isAssignableFrom(widgetClass) ? WidgetKind.EMBED : WidgetKind.NORMAL;
        Constructor<? extends Renderable> constructor;

        try {
            switch (kind) {
                case EMBED:
                    constructor = widgetClass
                            .getConstructor(Map.class, String.class, Evaluator.class, PageBook.class, String.class);
                    break;

                case NORMAL:
                default:
                    constructor = widgetClass
                            .getConstructor(WidgetChain.class, String.class, Evaluator.class);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Malformed Widget (this should never happen): " + widgetClass);
        }


        //ugh...
        if (!constructor.isAccessible())
            constructor.setAccessible(true);

        return new WidgetWrapper(widgetClass, constructor, kind, key);
    }

    public boolean isSelfRendering() {
        return selfRendering;
    }

    /**
     * TODO: cut dependency to com.google.inject.internal.ToStringBuilder.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(WidgetWrapper.class)
                .add("key", key)
                .add("class", clazz)
                .add("kind", kind)
                .toString();
    }

    @SuppressWarnings({"InnerClassTooDeeplyNested"})
    private static enum WidgetKind {
        NORMAL, EMBED
    }
}
