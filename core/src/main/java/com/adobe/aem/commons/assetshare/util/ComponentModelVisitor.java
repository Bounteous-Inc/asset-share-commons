package com.adobe.aem.commons.assetshare.util;

import com.adobe.aem.commons.assetshare.search.providers.request.ResourceOverridingRequestWrapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;

import java.util.ArrayList;
import java.util.Collection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Utility visitor that walks a Page and collects the models for resources matching at least one of the provided resource Types.
 *
 * This visitor only visits resources with a sling:resourceType.
 *
 * @param <T> The Model type to collect.
 */
public final class ComponentModelVisitor<T> extends ResourceTypeVisitor {
    final Collection<T> models = new ArrayList<>();

    private final SlingHttpServletRequest request;
    private final ModelFactory modelFactory;
    private final Class<T> clazz;

    /**
     * @param request the SlingHttpServletRequest object
     * @param modelFactory the ModelFactory object used to construct the Model
     * @param resourceTypes the resource types that will be attempted to be resolved to the T type.
     * @param clazz the Model class the resources should be made into.
     */
    public ComponentModelVisitor(SlingHttpServletRequest request,
                                 ModelFactory modelFactory,
                                 String[] resourceTypes,
                                 Class<T> clazz) {
        super(resourceTypes);
        this.request = request;
        this.modelFactory = modelFactory;
        this.clazz = clazz;
    }

    /**
     * Note that getModels() may return a SUBSET of getResources(). If a resource matches the resource type check but cannot be turned into a model, the resources will be in getResources() but not in getModels().
     * @return a list of Models representing the visited resources (assuming they match the resourceTypes and can be made into the clazz model type.
     */
    public final Collection<T> getModels() {
        return models;
    }

    @Override
    protected final void visit(Resource resource) {
        for (final String resourceType : resourceTypes) {
            if (handleResourceVisit(resource, resourceType)) {
                handleModelVisit(resource);
                break;
            }
        }
    }

    private void handleModelVisit(Resource resource) {
        if (clazz != null) {
            AdapterManager adapterManager = getService(AdapterManager.class);
            ResourceOverridingRequestWrapper resourceWrapper = new ResourceOverridingRequestWrapper(request, resource, adapterManager);
            final T model = resourceWrapper.adaptTo(clazz);

            if (model != null) {
                models.add(model);
            }
        }
    }

    private <T> T getService(Class<T> serviceInterface) {
        BundleContext bundleContext = FrameworkUtil.getBundle(serviceInterface).getBundleContext();
        ServiceReference factoryRef = bundleContext.getServiceReference(serviceInterface.getName());

        T service = null;
        if (factoryRef != null) {
            service = (T) bundleContext.getService(factoryRef);
        }

        return service;
    }
}
