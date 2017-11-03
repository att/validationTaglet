package com.att.det;

import javax.validation.constraints.Min;

public class BaseStuff {
    private int junk;

    @Min(value = 7)
    public int getJunk() { return junk; }

    public void setJunk(int junk) { this.junk = junk; }
}
