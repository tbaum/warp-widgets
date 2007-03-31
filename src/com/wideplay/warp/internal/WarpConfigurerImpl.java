package com.wideplay.warp.internal;

import com.wideplay.warp.Warp;
import com.google.inject.Module;

import java.util.List;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * On: Mar 24, 2007 10:50:28 PM
 *
 * @author Dhanji R. Prasanna
 */
class WarpConfigurerImpl implements Warp {
    private final List<Module> guiceModules = new LinkedList<Module>();

    public WarpConfigurerImpl(Module module) {
        guiceModules.add(module);
    }

    public void install(Module guiceModule) {
        guiceModules.add(guiceModule);
    }


    public List<Module> getGuiceModules() {
        return guiceModules;
    }
}
