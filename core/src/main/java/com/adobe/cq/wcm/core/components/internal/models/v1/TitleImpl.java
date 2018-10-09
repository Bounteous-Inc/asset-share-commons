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

import com.adobe.cq.wcm.core.components.internal.Utils;
import com.adobe.cq.wcm.core.components.models.Title;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.designer.Style;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

@Model(adaptables = SlingHttpServletRequest.class,
       adapters = {Title.class})
public class TitleImpl implements Title {

    protected static final String RESOURCE_TYPE_V1 = "core/wcm/components/title/v1/title";
    protected static final String RESOURCE_TYPE_V2 = "core/wcm/components/title/v2/title";

    @ScriptVariable
    private Resource resource;

    @ScriptVariable
    private Page currentPage;

    @ScriptVariable
    private Style currentStyle;

    @ValueMapValue(optional = true, name = JcrConstants.JCR_TITLE)
    private String title;

    @ValueMapValue(optional = true)
    private String type;

    /**
     * The {@link com.adobe.cq.wcm.core.components.internal.Utils.Heading} object for the type of this title.
     */
    private Utils.Heading heading;

    @PostConstruct
    private void initModel() {
        if (StringUtils.isBlank(title)) {
            title = StringUtils.defaultIfEmpty(currentPage.getPageTitle(), currentPage.getTitle());
        }

        if (heading == null) {
            heading = Utils.Heading.getHeading(type);
            if (heading == null) {
                heading = Utils.Heading.getHeading(currentStyle.get(PN_DESIGN_DEFAULT_TYPE, String.class));
            }
        }
    }

    @Override
    public String getText() {
        return title;
    }

    @Override
    public String getType() {
        if (heading != null) {
            return heading.getElement();
        }
        return null;
    }

}
