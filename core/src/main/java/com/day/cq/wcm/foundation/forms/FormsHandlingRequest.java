package com.day.cq.wcm.foundation.forms;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;

public class FormsHandlingRequest
        extends SlingHttpServletRequestWrapper
{
    public FormsHandlingRequest(SlingHttpServletRequest wrappedRequest)
    {
        super(wrappedRequest);
    }

    public String getMethod()
    {
        return "GET";
    }
}
