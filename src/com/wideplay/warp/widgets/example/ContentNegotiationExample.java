package com.wideplay.warp.widgets.example;

import com.wideplay.warp.widgets.*;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@At("/aPage")
@Switch(value = "Accept", kind = SwitchKind.HEADER)
public class ContentNegotiationExample {

    @Get("text/plain") @Show("text.txt")
    public void textPage() {

    }

    @Get("text/html") @Show("text.html")
    public void htmlPage() {

    }
}
