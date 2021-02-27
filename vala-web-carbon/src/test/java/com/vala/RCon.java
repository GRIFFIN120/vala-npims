package com.vala;

import org.junit.jupiter.api.Test;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;


public class RCon {

    @Test
    public void test() throws RserveException, REXPMismatchException {
        RConnection c = new RConnection("8.131.72.230");
        REXP x = c.eval("R.version.string");
        System.out.println(x.asString());

    }
}
