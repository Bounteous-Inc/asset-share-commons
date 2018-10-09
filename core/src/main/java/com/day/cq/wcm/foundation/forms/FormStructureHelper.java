package com.day.cq.wcm.foundation.forms;

import org.apache.sling.api.resource.Resource;

public abstract interface FormStructureHelper
{
    public abstract boolean canManage(Resource paramResource);

    public abstract Resource getFormResource(Resource paramResource);

    public abstract Iterable<Resource> getFormElements(Resource paramResource);

    public abstract Resource updateFormStructure(Resource paramResource);
}
