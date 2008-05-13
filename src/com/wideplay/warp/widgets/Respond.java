package com.wideplay.warp.widgets;

import com.google.inject.ImplementedBy;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ImplementedBy(StringBuilderRespond.class)
public interface Respond {
    void write(String text);

    HtmlTagBuilder withHtml();

    void write(char c);

    void chew();

    String toString();

    void writeToHead(String text);

    public static interface HtmlTagBuilder {
        void textField(String value, String s);

        void headerPlaceholder();
    }
}