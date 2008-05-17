package com.wideplay.warp.widgets.binding;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.wideplay.warp.util.TextTools;
import com.wideplay.warp.widgets.Evaluator;
import net.jcip.annotations.Immutable;
import org.mvel.PropertyAccessException;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable @Singleton
class MvelRequestBinder implements RequestBinder {
    private final Evaluator evaluator;
    private final Provider<FlashCache> cacheProvider;
    private final Logger log = Logger.getLogger(MvelRequestBinder.class.toString());

    private static final String VALID_BINDING_REGEX = "[\\w\\.$]*";

    @Inject
    public MvelRequestBinder(Evaluator evaluator, Provider<FlashCache> cacheProvider) {
        this.evaluator = evaluator;
        this.cacheProvider = cacheProvider;
    }

    public void bind(HttpServletRequest request, Object o) {
        @SuppressWarnings("unchecked")
        final Map<String, String[]> map = request.getParameterMap();

        //bind iteratively (last incoming param-value per key, gets bound)
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String key = entry.getKey();
            String rawValue = entry.getValue()[entry.getValue().length - 1];   //choose last value

            validate(key);

            //bind from collection?
            Object value;
            if (rawValue.startsWith(COLLECTION_BIND_PREFIX)) {
                final String[] binding = rawValue.substring(COLLECTION_BIND_PREFIX.length()).split("/");
                if (binding.length != 2)
                    throw new InvalidBindingException("Collection sources must be bound in the form '[C/collection/hashcode'. " +
                            "Was the request corrupt? Or did you try to bind something manually with a key starting '[C/'? Was: "
                            + rawValue);

                final Collection<?> collection = cacheProvider.get().get(binding[0]);

                value = search(collection, binding[1]);
            } else
                value = rawValue;

            //apply the bound value to the page object property
            try {
                evaluator.write(key, o, value);
            } catch (PropertyAccessException e) {
                //log missing property
                log.warning("A property could not be bound, but not necessarily an error: " + key);
            }
        }
    }

    //Linear collection search by hashcode
    private Object search(Collection<?> collection, String hashKey) {
        int hash = Integer.valueOf(hashKey);

        for (Object o : collection) {
            if (o.hashCode() == hash)
                return o;
        }

        //nothing found
        return null;
    }

    private void validate(String binding) {
        //guard against expression-injection attacks
        if (TextTools.isEmptyString(binding)
                    || !binding.matches(VALID_BINDING_REGEX))
            throw new InvalidBindingException("Binding expression (request/form parameter) contained invalid characters: " + binding);
    }


}
