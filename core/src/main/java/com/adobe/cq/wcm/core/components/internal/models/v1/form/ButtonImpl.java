/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2017 Adobe Systems Incorporated
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package com.adobe.cq.wcm.core.components.internal.models.v1.form;

import com.adobe.cq.wcm.core.components.internal.form.FormConstants;
import com.adobe.cq.wcm.core.components.models.form.Button;
import com.day.cq.i18n.I18n;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;

@Model(adaptables = {SlingHttpServletRequest.class, Resource.class},
       adapters = {Button.class})
public class ButtonImpl extends AbstractFieldImpl implements Button {

    private static final String PROP_TYPE_DEFAULT = "submit";
    private static final String PN_TYPE = "type";
    private static final String ID_PREFIX = "form-button";

    @ValueMapValue(name = PN_TYPE)
    @Default(values = PROP_TYPE_DEFAULT)
    private String typeString;

    private Type type;

    @Self(injectionStrategy = InjectionStrategy.OPTIONAL)
    private SlingHttpServletRequest request;

    @Self(injectionStrategy = InjectionStrategy.OPTIONAL)
    private Resource resource;

    private I18n i18n;

    @PostConstruct
    private void initModel() {
        if (request != null) {
            i18n = new I18n(request);
        }
        type = Type.fromString(typeString);
    }

    @Override
    protected String getIDPrefix() {
        return ID_PREFIX;
    }

    @Override
    public String getHelpMessage() {
        return null;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public String getTitle() {
        if (StringUtils.isBlank(this.title)) {
            this.title = (i18n != null ? i18n.getVar(StringUtils.capitalize(this.typeString)) : StringUtils.capitalize(this.typeString));
        }
        return this.title;
    }

    @Override
    protected String getDefaultName() {
        return "";
    }

    @Override
    protected String getDefaultValue() {
        return "";
    }

    @Override
    protected String getDefaultTitle() {
        return null;
    }
}
