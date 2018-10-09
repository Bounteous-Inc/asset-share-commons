package com.adobe.cq.wcm.core.components.internal.servlets.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;

public class ExpressionCustomizer {
    private static final String KEY_CACHE = "com.adobe.granite.ui.components.ExpressionCustomizer";
    private Map<String, Object> variables = new HashMap();

    public ExpressionCustomizer() {
    }

    public static ExpressionCustomizer from(ServletRequest request) {
        ExpressionCustomizer instance = (ExpressionCustomizer)request.getAttribute(KEY_CACHE);
        if (instance == null) {
            instance = new ExpressionCustomizer();
            request.setAttribute(KEY_CACHE, instance);
        }

        return instance;
    }

    public Object getVariable(String name) {
        return this.variables.get(name);
    }

    public void setVariable(String name, Object value) {
        this.variables.put(name, value);
    }

    public boolean hasVariable(String name) {
        return this.variables.containsKey(name);
    }
}

