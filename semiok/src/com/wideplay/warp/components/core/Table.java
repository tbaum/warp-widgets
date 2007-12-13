package com.wideplay.warp.components.core;

import com.google.inject.Inject;
import com.wideplay.warp.annotations.Component;
import com.wideplay.warp.components.AttributesInjectable;
import com.wideplay.warp.module.componentry.ClassReflectionCache;
import com.wideplay.warp.module.componentry.PropertyDescriptor;
import com.wideplay.warp.module.componentry.Renderable;
import com.wideplay.warp.rendering.ComponentHandler;
import com.wideplay.warp.rendering.HtmlWriter;
import com.wideplay.warp.rendering.RenderingContext;
import com.wideplay.warp.util.beans.BeanUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * On: 24/03/2007
 *
 * Generates a table from a collection of values with columns mapped to JavaBean
 * properties of the first bean's class.
 *
 *  w:property and w:title are mutually exclusive
 *
 * @author Dhanji R. Prasanna (dhanji at gmail com)
 * @since 1.0
 */
@Component
public class Table implements Renderable, AttributesInjectable {
    private Object items;
    private String var;
    private String rowClass;
    private String oddRowClass;

    private final ClassReflectionCache classCache;
    private Map<String, ComponentHandler> columns;
    private Map<String, ComponentHandler> customColumns;    //non-property columns

    private Map<String, Object> attribs;

    @Inject
    public Table(ClassReflectionCache classCache) {
        this.classCache = classCache;
    }

    public void render(RenderingContext context, List<? extends ComponentHandler> nestedComponents) {
        HtmlWriter writer = context.getWriter();
        String id = writer.makeIdFor(this);
        writer.elementWithAttrs("table", new Object[] { "id", id }, ComponentSupport.getTagAttributesExcept(attribs, "id"));

        //obtain the bound object
        Object itemsObject = items;//BeanUtils.getFromPropertyExpression(items, page);

        //build a cache of child columns (if we havent already) TODO validate these "columns" in the template at startup time
        if (null == columns) {
            this.columns = new LinkedHashMap<String, ComponentHandler>();
            this.customColumns = new LinkedHashMap<String, ComponentHandler>();

            //only populate the cache if there are child components
            if (null != nestedComponents)
                for (ComponentHandler columnHandler : nestedComponents) {
                    Map<String, PropertyDescriptor> map = columnHandler.getPropertyValueExpressions();

                    //look for custom columns, w:property and w:title are mutually exclusive (title takes precedence)
                    if (map.containsKey(Column.TITLE)) {
                        this.customColumns.put(map.get(Column.TITLE).getValue(), columnHandler);

                    } else if (map.containsKey(Column.PROPERTY)) {    //use a property-based column override
                        PropertyDescriptor propertyDescriptor = map.get(Column.PROPERTY);
                        this.columns.put(propertyDescriptor.getValue(), columnHandler);
                    }
                }
        }

        //see if it is an iterable
        if (itemsObject instanceof Iterable) {
            Map<String, String> propertiesAndLabels = null;
            Iterator iter = ((Iterable) itemsObject).iterator();

            int rowCtr = 0;
            while(iter.hasNext()) {
                Object item = iter.next();

                if (0 == rowCtr) {
                    //get the resource bundle associated with this model object (if any)
                    propertiesAndLabels = classCache.getPropertyLabelMap(item);
                    writeHeader(writer, propertiesAndLabels);
                    writer.element("tbody");
                }

                writeRow(item, writer, propertiesAndLabels, context, rowCtr % 2 == 0);
                rowCtr++;
            }

            writer.end("tbody");

        } else {    //is an array
            Map<String, String> propertiesAndLabels = null;
            Object[] array = ((Object[]) itemsObject);

            for (int i = 0 ; i < array.length; i++) {
                Object item = array[i];

                if (0 == i) {
                    propertiesAndLabels = classCache.getPropertyLabelMap(item);
                    writeHeader(writer, propertiesAndLabels);
                    writer.element("tbody");
                }

                writeRow(item, writer, propertiesAndLabels, context, i % 2 == 0);
            }
            writer.end("tbody");
        }
        writer.end("table");
    }

    private void writeHeader(HtmlWriter writer, Map<String, String> propertiesAndLabels) {
        //write out header
        writer.element("thead");
        writer.element("tr");
        for (String label : propertiesAndLabels.values()) {
            writer.element("th");
            writer.writeRaw(label);
            writer.end("th");
        }

        //render custom (non-property) column headings
        for (String customColumnLabel : customColumns.keySet()) {
            //render using column component override (children)
            writer.element("th");
            writer.writeRaw(customColumnLabel);
            writer.end("th");
        }
        
        writer.end("tr");
        writer.end("thead");
    }

    private void writeRow(Object item, HtmlWriter writer, Map<String, String> propertiesAndLabels, RenderingContext context, boolean isEvenRow) {
        //writes odd row class if necessary but both MUST be set or NEITHER must be set
        if (null != rowClass)
            writer.element("tr", "class", isEvenRow ? rowClass : oddRowClass);
        else
            writer.element("tr");

        //place the item in the context
        context.getContextVars().put(var, item);

        for (String property : propertiesAndLabels.keySet()) {
            writer.element("td");

            //see if this particular property should be rendered by an overriding column component
            ComponentHandler child = columns.get(property);
            if (null != child) {
                //render using column component override (children)
                child.handleRender(context);

            } else {    //write normally
                //stringize the property value only if it is not null (prevent NPE), also format the string into an expr
                Object value = BeanUtils.getFromPropertyExpression(String.format("%s.%s", var, property), context.getContextVars());
                if (null != value)
                    writer.writeRaw(value.toString());
                else
                    writer.writeRaw(null);
            }

            writer.end("td");
        }

        //render non-property columns
        for (ComponentHandler customCol : customColumns.values()) {
            //render using column component override (children)
            writer.element("td");
            customCol.handleRender(context);
            writer.end("td");
        }

        //clear the item from context
        context.getContextVars().remove(var);

        writer.end("tr");
    }


    public void setItems(Object items) {
        this.items = items;
    }


    public void setOddRowClass(String oddRowClass) {
        this.oddRowClass = oddRowClass;
    }

    public void setRowClass(String rowClass) {
        this.rowClass = rowClass;
    }

    public void setAttributeNameValuePairs(Map<String, Object> attribs) {
        this.attribs = attribs;
    }

    public Map<String, Object> getAttributeNameValuePairs() {
        return attribs;
    }

    public void setVar(String var) {
        this.var = var;
    }
}