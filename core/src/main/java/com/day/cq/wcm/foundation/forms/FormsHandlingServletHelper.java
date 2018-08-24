package com.day.cq.wcm.foundation.forms;

import com.day.cq.wcm.foundation.security.SaferSlingPostValidator;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormsHandlingServletHelper
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected static final String ATTR_RESOURCE = FormsHandlingServletHelper.class.getName() + "/resource";
    private String[] parameterNameWhitelist;
    private boolean allowExpressions;
    private SaferSlingPostValidator validator;
    private Set<String> formResourceTypes;
    private FormStructureHelperFactory formStructureHelperFactory;

    public FormsHandlingServletHelper(String[] parameterNameWhitelist, SaferSlingPostValidator validator, Set<String> formResourceTypes, boolean allowExpressions, FormStructureHelperFactory formStructureHelperFactory)
    {
        this.parameterNameWhitelist = parameterNameWhitelist;
        this.validator = validator;
        this.formResourceTypes = formResourceTypes;
        this.allowExpressions = allowExpressions;
        this.formStructureHelperFactory = formStructureHelperFactory;
    }

    public void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException, ServletException
    {
        if (this.validator.reject(request, this.parameterNameWhitelist))
        {
            response.sendError(400);
            return;
        }
        if ((ResourceUtil.isNonExistingResource(request.getResource())) ||
                (request.getAttribute(ATTR_RESOURCE) == null))
        {
            this.logger.debug("Received fake request!");
            response.setStatus(500);
            return;
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Validating POST request with form definition stored at {}.", request.getResource().getPath());
        }
        SlingHttpServletRequest formsRequest = new FormsHandlingRequest(request);
        SlingHttpServletResponse formsResponse = new FormsHandlingResponse(response);

        request.setAttribute("cq.form.expressions.enabled", Boolean.valueOf(this.allowExpressions));

        Resource formResource = request.getResource();

        FormsHelper.getFormId(request);

        validate(formsRequest, formsResponse, formResource);
        ValueMap properties = ResourceUtil.getValueMap(formResource);

        String actionType = properties == null ? "" : (String)properties.get("actionType", "");
        ValidationInfo info;
        if (actionType.length() == 0)
        {
            info = ValidationInfo.createValidationInfo(request);
            info.addErrorMessage(null, "Unable to process the form: missing actionType");
        }
        else
        {
            request.setAttribute("cq.form.prop.whitelist", this.parameterNameWhitelist);
            FormsHelper.runAction(actionType, "formservervalidation", formResource, formsRequest, formsResponse);

            info = ValidationInfo.getValidationInfo(request);
        }
        if (info != null)
        {
            this.logger.debug("Form {} is not valid: {}", formResource.getPath(), info);

            Resource rsrc = (Resource)request.getAttribute(ATTR_RESOURCE);
            request.removeAttribute(ATTR_RESOURCE);

            request.getRequestDispatcher(rsrc).forward(formsRequest, response);
            return;
        }
        FormsHelper.runAction(actionType, "forward", formResource, formsRequest, formsResponse);

        String forwardPath = FormsHelper.getForwardPath(request);
        if ((forwardPath != null) && (forwardPath.length() > 0))
        {
            if ((FormsHelper.isRedirectToReferrer(request)) &&
                    (request.getParameter(":redirect") == null))
            {
                String referrerPath = getReferrerPath(request);
                request = new RedirectRequest(request, referrerPath);
            }
            String redirect = FormsHelper.getForwardRedirect(request);
            if (redirect != null) {
                request = new RedirectRequest(request, redirect);
            }
            if (forwardPath.endsWith("/")) {
                forwardPath = forwardPath + '*';
            }
            Resource forwardResource = request.getResourceResolver().resolve(forwardPath);
            request.getRequestDispatcher(forwardResource, FormsHelper.getForwardOptions(request)).forward(request, response);

            FormsHelper.runAction(actionType, "cleanup", formResource, formsRequest, formsResponse);
            return;
        }
        FormsHelper.runAction(actionType, "post", formResource, request, response);
    }

    private boolean checkFormResourceType(Resource resource, ResourceResolver resolver)
    {
        boolean isForm = false;
        for (String resourceType : this.formResourceTypes) {
            if (resolver.isResourceType(resource, resourceType))
            {
                isForm = true;
                break;
            }
        }
        return isForm;
    }

    public void handleFilter(ServletRequest request, ServletResponse response, FilterChain chain, String extensionToAdd, String selectorToAdd)
            throws IOException, ServletException
    {
        if ((request instanceof SlingHttpServletRequest))
        {
            SlingHttpServletRequest req = (SlingHttpServletRequest)request;
            if (("POST".equalsIgnoreCase(req.getMethod())) &&
                    (req.getParameter(":formstart") != null))
            {
                ResourceResolver resolver = req.getResourceResolver();
                String formPath = req.getParameter(":formstart");
                Resource formResource = ((SlingHttpServletRequest)request).getResourceResolver().getResource(formPath);
                if ((formResource != null) && (checkFormResourceType(formResource, resolver)))
                {
                    req.setAttribute(ATTR_RESOURCE, req.getResource());
                    req.setAttribute("cq.form.formstructurehelper", this.formStructureHelperFactory
                            .getFormStructureHelper(formResource));
                    StringBuilder sb = new StringBuilder();
                    if (!formPath.startsWith("/"))
                    {
                        sb.append(req.getResource().getPath());
                        sb.append('/');
                    }
                    sb.append(formPath);
                    sb.append('.');
                    sb.append(selectorToAdd);
                    sb.append('.');
                    sb.append(extensionToAdd);

                    String forwardPath = sb.toString();
                    req.getRequestDispatcher(forwardPath).forward(request, response);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    private String getReferrerPath(SlingHttpServletRequest request)
    {
        String referrer = FormsHelper.getReferrer(request);
        String referrerPath;
        try
        {
            URI referrerUri = new URI(referrer);
            referrerPath = referrerUri.getPath();
        }
        catch (URISyntaxException e)
        {
            this.logger.warn("given redirect target ({}) is not a valid uri: {}", referrer, e);
            return null;
        }

        return referrerPath;
    }

    private ValidationInfo validate(SlingHttpServletRequest request, SlingHttpServletResponse response, final Resource formResource)
            throws ServletException, IOException
    {
        FormStructureHelper formStructureHelper = this.formStructureHelperFactory.getFormStructureHelper(formResource);

        Iterable<Resource> formElements = formStructureHelper.getFormElements(formResource);
        for (Resource formField : formElements)
        {
            FieldHelper.initializeField(request, response, formField);
            FormsHelper.includeResource(request, response, formField, "servervalidation");
        }
        ValueMap properties = ResourceUtil.getValueMap(formResource);
        final String valScriptRT = (String)properties.get("validationRT", formResource.getResourceType());
        if ((valScriptRT != null) && (valScriptRT.length() > 0))
        {
            Resource valScriptResource = formResource;
            if (!formResource.getResourceType().equals(valScriptRT)) {
                valScriptResource = new ResourceWrapper(formResource)
                {
                    public String getResourceType()
                    {
                        return valScriptRT;
                    }

                    public String getResourceSuperType()
                    {
                        return formResource.getResourceType();
                    }
                };
            }
            FormsHelper.includeResource(request, response, valScriptResource, "formservervalidation");
        }
        return ValidationInfo.getValidationInfo(request);
    }
}
