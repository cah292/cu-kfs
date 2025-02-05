/*
 * Copyright 2011 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.module.ld.businessobject.lookup;

import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.integration.ld.businessobject.inquiry.AbstractPositionDataDetailsInquirableImpl;
import org.kuali.kfs.module.ld.businessobject.LaborCalculatedSalaryFoundationTracker;
import org.kuali.kfs.module.ld.businessobject.inquiry.AbstractLaborInquirableImpl;
import org.kuali.kfs.module.ld.businessobject.inquiry.PositionDataDetailsInquirableImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.rice.krad.bo.BusinessObject;

import com.rsmart.kuali.kfs.module.ld.businessobject.inquiry.PositionFundingInquirableImpl;

public class PositionFundingLookupableHelperServiceImpl extends org.kuali.kfs.module.ld.businessobject.lookup.PositionFundingLookupableHelperServiceImpl {

    /**
     * @see org.kuali.kfs.kns.lookup.Lookupable#getInquiryUrl(org.kuali.kfs.kns.bo.BusinessObject, java.lang.String)
     */
    @Override
    public HtmlData getInquiryUrl(BusinessObject businessObject, String propertyName) {
        if (KFSPropertyConstants.POSITION_NUMBER.equals(propertyName)) {
            LaborCalculatedSalaryFoundationTracker CSFTracker = (LaborCalculatedSalaryFoundationTracker) businessObject;
            AbstractPositionDataDetailsInquirableImpl positionDataDetailsInquirable = new PositionDataDetailsInquirableImpl();

            Map<String, String> fieldValues = new HashMap<String, String>();
            fieldValues.put(propertyName, CSFTracker.getPositionNumber());

            BusinessObject positionData = positionDataDetailsInquirable.getBusinessObject(fieldValues);

            return positionData == null ? new AnchorHtmlData(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING) : positionDataDetailsInquirable.getInquiryUrl(positionData, propertyName);
        }
        return (new PositionFundingInquirableImpl()).getInquiryUrl(businessObject, propertyName);
    }
}
