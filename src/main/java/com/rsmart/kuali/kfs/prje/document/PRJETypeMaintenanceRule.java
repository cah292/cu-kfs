/*
 * Copyright 2009 The Kuali Foundation.
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

import org.apache.log4j.Logger;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.maintenance.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;

import com.rsmart.kuali.kfs.prje.PRJEConstants.ProrateOptions;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEAccountLine;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEBaseAccount;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEBaseObject;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEType;

public class PRJETypeMaintenanceRule extends MaintenanceDocumentRuleBase {
    private static Logger LOG = Logger.getLogger(PRJETypeMaintenanceRule.class);
    
    @Override
    public boolean processRouteDocument(Document document) {
        MaintenanceDocument maintenanceDocument = (MaintenanceDocument)document;
        PRJEType type = (PRJEType)maintenanceDocument.getNewMaintainableObject().getPersistableBusinessObject();
        return isTypeValid(type) && super.processSaveDocument(document);        
    }
    
    @Override
    public boolean processSaveDocument(Document document) {
        processRouteDocument(document);
        return true;
    }
    
    public boolean isTypeValid(PRJEType type) {
        String prorateOptions = type.getProrateOptions();
        List<PRJEBaseObject> baseObjects = type.getBaseObjects();
        List<PRJEBaseAccount> baseAccounts = type.getBaseAccounts();           
        List<PRJEAccountLine> accountLines = type.getAccountLines();            

        boolean valid = true;
        
        if ( baseAccounts.size() == 0 ) {
            putFieldError("baseAccounts", "error.prje.noBaseAccounts");            
            valid = false;
        }
        
        if ( accountLines.size() == 0 ) {
            putFieldError("accountLines", "error.prje.noAccountLines");            
            valid = false;
        }
        
        if ( baseAccounts.size() > 1  
                && ( ProrateOptions.SINGLE_TO_MULTIPLE.getKey().equals(prorateOptions) 
                || ProrateOptions.SINGLE_TO_SINGLE.equals(prorateOptions)) ) {
            putFieldError("baseAccounts", "error.prje.manyBaseAccounts");              
            valid = false;
        }
        
        if ( accountLines.size() > 1  
                && ( ProrateOptions.MULTIPLE_TO_SINGLE.equals(prorateOptions) 
                || ProrateOptions.SINGLE_TO_SINGLE.getKey().equals(prorateOptions)) ) {
            putFieldError("accountLines", "error.prje.manyAccountLines");              
            valid = false;
        }
        
        return valid;
    }
}
