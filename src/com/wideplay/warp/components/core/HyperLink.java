package com.wideplay.warp.components.core;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.wideplay.warp.components.AttributesInjectable;
import com.wideplay.warp.module.WarpConfiguration;
import com.wideplay.warp.module.componentry.Renderable;
import com.wideplay.warp.module.pages.PageClassReflection;
import com.wideplay.warp.rendering.ComponentHandler;
import com.wideplay.warp.rendering.HtmlWriter;
import com.wideplay.warp.rendering.PageRenderException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dhanji
 * Date: Aug 12, 2007
 * Time: 10:44:15 AM
 *
 * Generates a RESTful hyperlink (i.e. performing a browser GET) to a target page. Note that hyperlinks
 * do *NOT* generate events on the *current* page object (instead they generate an HTTP GET to a target page).
 *
 * Practically, the only event generated by a hyperlink is a @PreRender to the target page. Example:
 *
 * <a w:component="hyperlink" w:target="mytarget/page" w:topic="expression">goto target</a>
 *
 * ...navigates from current page to URI "mytarget/page/{expression}"
 *
 */
public class HyperLink implements Renderable, AttributesInjectable {
    private String target;
    private String topic;
    private Map<String, Object> attribs;

    private final WarpConfiguration configuration;

    @Inject
    public HyperLink(WarpConfiguration configuration) {
        this.configuration = configuration;
    }

    public void render(HtmlWriter writer, List<? extends ComponentHandler> nestedComponents, Injector injector,
                       PageClassReflection reflection, Object page) {
        String href;
        if (null == topic)
            href = target;
        else if (null == target)
            href = topic;
        else
            try {
                href = String.format("%s/%s", target, URLEncoder.encode(topic, configuration.getUrlEncoding()));
            } catch (UnsupportedEncodingException e) {
                throw new PageRenderException("Could not encode topic into URI, the encoding scheme was not supported: "
                        + configuration.getUrlEncoding(), e);
            }

        writer.elementWithAttrs("a", new Object[] { "href", href }, ComponentSupport.getTagAttributesExcept(attribs, "href"));
        ComponentSupport.renderMultiple(writer, nestedComponents, injector, reflection, page);

        writer.end("a");
    }


    public void setTarget(String target) {
        this.target = target;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setAttributeNameValuePairs(Map<String, Object> attribs) {
        this.attribs = attribs;
    }
}
