/*
 * Copyright 2010 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.prje.document;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.maintenance.MaintenanceDocument;
import org.kuali.rice.krad.util.ObjectUtils;

import com.rsmart.kuali.kfs.prje.businessobject.PRJEAccountLine;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEBaseAccount;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEBaseObject;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEType;

public class PRJETypeMaintainable extends FinancialSystemMaintainable {
    private static Logger LOG = Logger.getLogger(PRJETypeMaintainable.class);
    
    @Override
    public void processAfterCopy(MaintenanceDocument document, Map<String, String[]> parameters) {
        super.processAfterCopy(document, parameters);
        PRJEType type = (PRJEType)document.getNewMaintainableObject().getPersistableBusinessObject();
        
        for ( PRJEBaseAccount baseAccount : type.getBaseAccounts() )
            clearPrimaryKeyFields(baseAccount);
        
        for ( PRJEAccountLine accountLine : type.getAccountLines() )
            clearPrimaryKeyFields(accountLine);

        for ( PRJEBaseObject baseObject : type.getBaseObjects() )
            clearPrimaryKeyFields(baseObject);
    }
    
    protected void clearPrimaryKeyFields(PersistableBusinessObject bo) {
        // get business object being maintained and its keys
        List<String> keyFieldNames = getBusinessObjectMetaDataService().listPrimaryKeyFieldNames(bo.getClass());

        for (String keyFieldName : keyFieldNames) {
            try {
                ObjectUtils.setObjectProperty(bo, keyFieldName, null);
            }
            catch (Exception e) {
                LOG.error("Unable to clear primary key field: " + e.getMessage());
                throw new RuntimeException("Unable to clear primary key field: " + e.getMessage());
            }
        }
    }
}
