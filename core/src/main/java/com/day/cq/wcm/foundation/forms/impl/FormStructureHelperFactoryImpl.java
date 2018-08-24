package com.day.cq.wcm.foundation.forms.impl;

import com.day.cq.wcm.foundation.forms.FormStructureHelper;
import com.day.cq.wcm.foundation.forms.FormStructureHelperFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

@Component
@Service({FormStructureHelperFactory.class})
@Reference(name="formStructureHelperRef", referenceInterface=FormStructureHelper.class, cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE, policy=ReferencePolicy.DYNAMIC)
public class FormStructureHelperFactoryImpl
        implements FormStructureHelperFactory
{
    List<ServiceReference> formStructureHelperReferences = new ArrayList();
    protected BundleContext bundleContext;
    NavigableMap<ServiceReference, FormStructureHelper> formStructureHelperMap = new TreeMap();

    @Activate
    protected void activate(ComponentContext ctx)
    {
        this.bundleContext = ctx.getBundleContext();
        for (ServiceReference ref : this.formStructureHelperReferences)
        {
            FormStructureHelper formStructureHelper = (FormStructureHelper)this.bundleContext.getService(ref);
            if (formStructureHelper != null) {
                this.formStructureHelperMap.put(ref, formStructureHelper);
            }
        }
        this.formStructureHelperReferences.clear();
    }

    protected void bindFormStructureHelperRef(ServiceReference ref)
    {
        if (this.bundleContext == null)
        {
            this.formStructureHelperReferences.add(ref);
        }
        else
        {
            FormStructureHelper formStructureHelper = (FormStructureHelper)this.bundleContext.getService(ref);
            if (formStructureHelper != null) {
                this.formStructureHelperMap.put(ref, formStructureHelper);
            }
        }
    }

    protected void unbindFormStructureHelperRef(ServiceReference ref)
    {
        if (this.bundleContext != null)
        {
            FormStructureHelper formStructureHelper = (FormStructureHelper)this.bundleContext.getService(ref);

            this.formStructureHelperMap.remove(ref);
        }
    }

    public FormStructureHelper getFormStructureHelper(Resource resource)
    {
        for (Map.Entry<ServiceReference, FormStructureHelper> entry : this.formStructureHelperMap.descendingMap().entrySet()) {
            if (((FormStructureHelper)entry.getValue()).canManage(resource)) {
                return (FormStructureHelper)entry.getValue();
            }
        }
        return null;
    }
}
