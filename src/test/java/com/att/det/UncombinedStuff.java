package com.att.det;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @att.det.validationConstraints
 */
@ValidStuff(message = "must be valid stuff")
public class UncombinedStuff extends BaseStuff {
    private int foo;
    private String bar;
    private Thing thing;
    private Thing anotherThing;
    
    public int getFoo() { return foo; }
    @Size(min = 3, max = 5)
    @Pattern(regexp = "abc.*")
    public String getBar() { return bar; }
    @Valid
    public Thing getThing() { return thing; }
    @NotNull
    @Valid
    public Thing getAnotherThing() { return anotherThing; }
    
    public void setFoo(int foo) { this.foo = foo; }
    public void setBar(String bar) { this.bar = bar; }
    public void setThing(Thing thing) { this.thing = thing; }
}
