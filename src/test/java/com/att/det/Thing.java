package com.att.det;

import javax.validation.constraints.Pattern;

/**
 * @att.det.validationConstraints
 * @author dk068x
 *
 */
public class Thing {
    private int stuff;
    private String str;
    
    public int getStuff() { return stuff; }
    @Pattern(regexp = "[0-9]{3-5}")
    public String getStr() { return str; }
    
    public void setStuff(int stuff) { this.stuff = stuff; }
    public void setStr(String str) { this.str = str; }
    
    
}
