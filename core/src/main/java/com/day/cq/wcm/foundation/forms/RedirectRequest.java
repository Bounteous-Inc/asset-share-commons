package com.day.cq.wcm.foundation.forms;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;

public class RedirectRequest
        extends SlingHttpServletRequestWrapper
{
    private final String redirectUrl;

    public RedirectRequest(SlingHttpServletRequest wrappedRequest, String redirectUrl)
    {
        super(wrappedRequest);
        this.redirectUrl = redirectUrl;
    }

    public String getParameter(String name)
    {
        if (":redirect".equals(name)) {
            return this.redirectUrl;
        }
        return super.getParameter(name);
    }

    public String[] getParameterValues(String name)
    {
        if (":redirect".equals(name)) {
            return new String[] { this.redirectUrl };
        }
        return super.getParameterValues(name);
    }
}
