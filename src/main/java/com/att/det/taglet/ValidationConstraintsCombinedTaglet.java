package com.att.det.taglet;

import java.util.Map;

import com.sun.tools.doclets.Taglet;

public class ValidationConstraintsCombinedTaglet extends ValidationConstraintsTaglet {

    {
        setCombineConstraints(true);
    }
    
    @Override
    public String getName() { return "att.det.validationConstraintsCombined"; }

    public static void register(Map<String, Taglet> tagletMap)  {
        ValidationConstraintsCombinedTaglet taglet  = new ValidationConstraintsCombinedTaglet();
        registerInstance(tagletMap, taglet, taglet.getName());
    }
}
