package com.wideplay.warp.widgets.aplenty;

import com.wideplay.warp.widgets.rendering.EmbedAs;
import com.wideplay.warp.widgets.rendering.CallWith;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@EmbedAs("Case") @CallWith("When")
public class CaseWidget {
    private Object choice;

    public Object getChoice() {
        return choice;
    }

    public void setChoice(Object choice) {
        this.choice = choice;
    }
}
