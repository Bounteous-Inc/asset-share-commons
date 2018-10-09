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
package com.adobe.cq.wcm.core.components.internal.models.v1;

import com.adobe.cq.wcm.core.components.models.Text;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.Nonnull;
import javax.inject.Inject;

@Model(adaptables = SlingHttpServletRequest.class,
       adapters = {Text.class})
public class TextImpl implements Text {

    protected static final String RESOURCE_TYPE_V1 = "core/wcm/components/text/v1/text";
    protected static final String RESOURCE_TYPE_V2 = "core/wcm/components/text/v2/text";

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String text;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Default(booleanValues = false)
    private boolean textIsRich;

    @Inject
    private Resource resource;

    @Override
    public String getText() {
        return text;
    }

    @Override
    public boolean isRichText() {
        return textIsRich;
    }
}
