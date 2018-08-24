package com.day.cq.wcm.foundation.forms;

import org.apache.sling.api.resource.Resource;

public abstract interface FormStructureHelperFactory
{
    public abstract FormStructureHelper getFormStructureHelper(Resource paramResource);
}
