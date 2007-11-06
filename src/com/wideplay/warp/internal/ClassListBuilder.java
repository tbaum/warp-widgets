package com.wideplay.warp.internal;

import com.wideplay.warp.util.reflect.ClassNameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * On: 20/03/2007
 *
 * @author Dhanji R. Prasanna (dhanji at gmail com)
 * @since 1.0
 */
class ClassListBuilder {
    private final Log log = LogFactory.getLog(getClass());

    public List<Class<?>> loadClasses(URL url, String packageName) {
        List<Class<?>> classes = new LinkedList<Class<?>>();

        //recursively iterate package tree and load classes
        List<String> classNames = ClassNameUtils.listRecursive(new File(toUri(url)), ClassNameUtils.CLASS_FILE_FILTER);

        //ensure class is loaded
        for (String className : classNames)
            try {
                classes.add(Class.forName(String.format("%s.%s", packageName, className)));
            } catch (ClassNotFoundException e) {
                log.error("Could not load target page classes", e);
                e.printStackTrace();
            }

        return classes;
    }

    private URI toUri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid class file to page parse: " + url);
        }
    }
}
