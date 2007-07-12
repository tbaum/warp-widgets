package com.wideplay.warp.internal;

import com.wideplay.warp.module.componentry.ClassReflectionCache;
import com.wideplay.warp.util.reflect.ReflectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 26/03/2007
 * Time: 16:36:14
 *
 * This is a simple impl that caches reflection (introspection) results so they can be speeded up.
 *
 * @author dprasanna
 * @since 1.0
 */
class ClassReflectionCacheImpl implements ClassReflectionCache {
    private final Map<Class<?>, Map<String, String>> classPropertyLabelMap = new HashMap<Class<?>, Map<String, String>>();
    private final Log log = LogFactory.getLog(getClass());

    public Map<String, String> getPropertyLabelMap(Object object) {
        Class<? extends Object> objectClass = object.getClass();
        Map<String, String> propertyLabels = classPropertyLabelMap.get(objectClass);

        if (null != propertyLabels)
            log.debug("cache hit! returning class: " + objectClass);

        //build a property map via reflection
        if (null == propertyLabels) {
            log.debug("cache miss, introspecting and caching class: " + objectClass);

            propertyLabels = buildPropertiesAndLabels(objectClass);
            classPropertyLabelMap.put(objectClass, propertyLabels);
        }

        return propertyLabels;
    }

    //this method is deliberately unsynchronized so 2 threads caching the same class can overwrite the other, thus avoiding competition for the map
    private Map<String, String> buildPropertiesAndLabels(Class<? extends Object> aClass) {
        Map<String, String> propertyLabels = new LinkedHashMap<String, String>();   //MUST preserve order

        ResourceBundle labels;
        try {
            labels = PropertyResourceBundle.getBundle(aClass.getName());
        } catch (MissingResourceException mre) {
            labels = null;
        }

        for (Method method : aClass.getMethods()) {

            //check for getters and cache them as a property
            String name = method.getName();
            if (0 == method.getParameterTypes().length && name.length() > 3 && !void.class.equals(method.getReturnType())
                    && name.startsWith("get")) {

                //skip reserved
                if ("getClass".equals(name))
                    continue;

                String key = ReflectUtils.extractPropertyNameFromAccessor(method.getName());

                if (null != labels) {
                    //watch for column hides (empty property)
                    String value = labels.getString(key);
                    if (null != value && !"".equals(value.trim()))
                        propertyLabels.put(key, value);
                } else
                    propertyLabels.put(key, key);
            }
        }

        return propertyLabels;
    }

}
