/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.coa.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.KeyValuesService;

import edu.cornell.kfs.coa.businessobject.ReversionCategory;

/**
 * This class creates a new finder for our forms view (creates a drop-down of {@link OrganizationReversionCategory}s)
 */
public class ReversionCategoryValuesFinder extends KeyValuesBase {

    /**
     * Creates a list of {@link MandatoryTransferEliminationCode}s using their code as their key, and their name as the display
     * value
     * 
     * @see org.kuali.kfs.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {

        Collection<ReversionCategory> codes = SpringContext.getBean(KeyValuesService.class).findAll(ReversionCategory.class);
        List<KeyValue> labels = new ArrayList<KeyValue>();
        labels.add(new ConcreteKeyValue("", ""));
        for (ReversionCategory reversionCategory : codes) {
            if (reversionCategory.isActive()) {
                labels.add(new ConcreteKeyValue(reversionCategory.getReversionCategoryCode(), reversionCategory.getReversionCategoryName()));
            }
        }

        return labels;
    }

}