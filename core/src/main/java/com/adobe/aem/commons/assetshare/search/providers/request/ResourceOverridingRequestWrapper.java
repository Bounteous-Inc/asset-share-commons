package com.adobe.aem.commons.assetshare.search.providers.request;

import javax.script.SimpleBindings;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;

public class ResourceOverridingRequestWrapper
        extends SlingHttpServletRequestWrapper
{
    private final Resource resource;
    private final AdapterManager adapterManager;
    private final SlingBindings bindings;

    public ResourceOverridingRequestWrapper(SlingHttpServletRequest wrappedRequest, Resource resource, AdapterManager adapterManager)
    {
        super(wrappedRequest);
        this.resource = resource;
        this.adapterManager = adapterManager;

        SlingBindings existingBindings = (SlingBindings)wrappedRequest.getAttribute(SlingBindings.class.getName());

        SimpleBindings bindings = new SimpleBindings();
        if (existingBindings != null)
        {
            bindings.put("sling", existingBindings.getSling());
            bindings.put("response", existingBindings.getResponse());
            bindings.put("reader", existingBindings.getReader());
            bindings.put("out", existingBindings.getOut());
            bindings.put("log", existingBindings.getLog());
        }
        bindings.put("request", this);
        bindings.put("resource", resource);
        bindings.put("resolver", resource.getResourceResolver());

        SlingBindings slingBindings = new SlingBindings();
        slingBindings.putAll(bindings);

        this.bindings = slingBindings;
    }

    public Object getAttribute(String name)
    {
        if (SlingBindings.class.getName().equals(name)) {
            return this.bindings;
        }
        return super.getAttribute(name);
    }

    public Resource getResource()
    {
        return this.resource;
    }

    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type)
    {
        return (AdapterType)this.adapterManager.getAdapter(this, type);
    }
}
