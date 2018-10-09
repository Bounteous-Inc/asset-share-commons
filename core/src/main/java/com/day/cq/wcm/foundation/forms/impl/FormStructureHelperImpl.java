package com.day.cq.wcm.foundation.forms.impl;

import com.day.cq.wcm.foundation.forms.FormStructureHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate=true)
@Service({FormStructureHelper.class})
public class FormStructureHelperImpl
        implements FormStructureHelper
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FormStructureHelperImpl.class.getName());

    public Resource getFormResource(Resource resource)
    {
        if (ResourceUtil.getName(resource).equals("jcr:content")) {
            return null;
        }
        if (resource.getPath().lastIndexOf("/") == 0) {
            return null;
        }
        if (resource.isResourceType("foundation/components/form/start")) {
            return resource;
        }
        Resource parent = ResourceUtil.getParent(resource);
        List<Resource> predecessor = new ArrayList();
        Iterator<Resource> i = ResourceUtil.listChildren(parent);
        while (i.hasNext())
        {
            Resource current = (Resource)i.next();
            if (current.getPath().equals(resource.getPath())) {
                break;
            }
            predecessor.add(current);
        }
        Collections.reverse(predecessor);

        Iterator<Resource> rsrcIter = predecessor.iterator();
        while (rsrcIter.hasNext())
        {
            Resource current = (Resource)rsrcIter.next();
            if (current.isResourceType("foundation/components/form/start")) {
                return resource;
            }
            if (current.isResourceType("foundation/components/form/end")) {
                return null;
            }
        }
        return getFormResource(parent);
    }

    public Iterable<Resource> getFormElements(Resource resource)
    {
        List<Resource> list = new ArrayList();

        Iterator<Resource> iter = resource.getParent().listChildren();
        while (!((Resource)iter.next()).getPath().equals(resource.getPath())) {}
        collectFormElements(list, iter, resource.getResourceResolver());
        return list;
    }

    private static boolean collectFormElements(List<Resource> list, Iterator<Resource> iter, ResourceResolver resolver)
    {
        boolean stop = false;
        while ((!stop) && (iter.hasNext()))
        {
            Resource n = (Resource)iter.next();
            if (n.isResourceType("foundation/components/form/end"))
            {
                stop = true;
                break;
            }
            if ((n.getResourceType().startsWith("foundation/components/form/")) || (
                    (n.getResourceSuperType() != null) && (n.getResourceSuperType().startsWith("foundation/components/form/"))))
            {
                list.add(n);
            }
            else
            {
                Iterator<Resource> cI = ResourceUtil.listChildren(n);
                if (cI != null) {
                    stop = collectFormElements(list, cI, resolver);
                }
            }
        }
        return stop;
    }

    public boolean canManage(Resource resource)
    {
        if ((resource == null) || (resource.getName().equals("jcr:content")) || (resource.getPath().lastIndexOf("/") == 0)) {
            return false;
        }
        if ((resource.isResourceType("foundation/components/form/start")) || (resource.isResourceType("foundation/components/form/end"))) {
            return true;
        }
        return canManage(resource.getParent());
    }

    public Resource updateFormStructure(Resource resource)
    {
        if (resource.isResourceType("foundation/components/form/start"))
        {
            ModifiableValueMap map = (ModifiableValueMap)resource.adaptTo(ModifiableValueMap.class);
            if (!map.containsKey("actionType")) {
                setDefaultActionType(map, resource);
            }
            if (!map.containsKey("formid")) {
                setDefaultFormId(map, resource);
            }
            Iterator<Resource> iter = resource.getParent().listChildren();
            while (!((Resource)iter.next()).getPath().equals(resource.getPath())) {}
            Resource formEnd = null;
            Resource nextPar = null;
            boolean stop = false;
            while ((iter.hasNext()) && (formEnd == null) && (!stop))
            {
                Resource current = (Resource)iter.next();
                if (nextPar == null) {
                    nextPar = current;
                }
                if (current.isResourceType("foundation/components/form/end")) {
                    formEnd = current;
                } else if (current.isResourceType("foundation/components/form/start")) {
                    stop = true;
                }
            }
            if (formEnd == null)
            {
                Node parent = (Node)resource.getParent().adaptTo(Node.class);
                if (parent != null) {
                    try
                    {
                        String nodeName = "form_end_" + System.currentTimeMillis();
                        Node node = parent.addNode(nodeName);
                        ValueMap props = ResourceUtil.getValueMap(resource);
                        String resourceType = "foundation/components/form/end";
                        resourceType = (String)props.get("endResourceType", resourceType);
                        node.setProperty("sling:resourceType", resourceType);
                        if (!resourceType.equals("foundation/components/form/end")) {
                            node.setProperty("sling:resourceSuperType", "foundation/components/form/end");
                        }
                        if (nextPar != null) {
                            parent.orderBefore(node.getName(), nextPar.getName());
                        }
                        Iterator<Resource> i = resource.getParent().listChildren();
                        while (i.hasNext())
                        {
                            Resource r = (Resource)i.next();
                            if (nodeName.equals(r.getName())) {
                                return r;
                            }
                        }
                    }
                    catch (RepositoryException re)
                    {
                        LOGGER.error("Unable to create missing form end element for form start " + resource, re);
                    }
                }
                LOGGER.error("Resource is not adaptable to node - unable to add missing form end element for " + resource);
            }
        }
        else if (resource.isResourceType("foundation/components/form/end"))
        {
            Resource formStart = getFormResource(resource);
            if (formStart == null)
            {
                Node node = (Node)resource.adaptTo(Node.class);
                Node parent = (Node)resource.getParent().adaptTo(Node.class);
                if ((node != null) && (parent != null)) {
                    try
                    {
                        node.remove();
                        return resource;
                    }
                    catch (RepositoryException re)
                    {
                        LOGGER.error("Unable to create missing form end element for form start " + resource, re);
                    }
                } else {
                    LOGGER.error("Resource is not adaptable to node - unable to remove form element " + resource);
                }
            }
        }
        return null;
    }

    private void setDefaultFormId(ModifiableValueMap map, Resource resource)
    {
        String defaultFormId = resource.getPath().replaceAll("[/:.]", "_");
        map.put("formid", defaultFormId);
    }

    private void setDefaultActionType(ModifiableValueMap map, Resource resource)
    {
        map.put("actionType", "foundation/components/form/actions/store");

        String defaultContentPath = "/content/usergenerated" + resource.getPath().replaceAll("^.content", "").replaceAll("jcr.content.*", "") + "cq-gen" + System.currentTimeMillis() + "/";
        map.put("action", defaultContentPath);
    }
}
