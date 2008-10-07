package com.wideplay.warp.widgets.client;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public interface WebClient<T> {
    WebResponse get();

    WebResponse post(T t);

    WebResponse put(T t);

    WebResponse delete();
}
