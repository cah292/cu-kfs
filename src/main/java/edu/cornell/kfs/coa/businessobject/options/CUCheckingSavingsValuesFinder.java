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
import java.util.List;

import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.core.util.KeyLabelPair;

/**
 * This class returns list containg 22 = Checking or 32 = Savings
 */
public class CUCheckingSavingsValuesFinder extends KeyValuesBase {

    /**
     * Creates a simple list of static values for either checking or savings
     * 
     * @see org.kuali.rice.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        List keyValues = new ArrayList();
        keyValues.add(new KeyLabelPair("22PPD", "Personal Checking (22PPD)"));
        keyValues.add(new KeyLabelPair("32PPD", "Personal Savings (32PPD)"));
        keyValues.add(new KeyLabelPair("22CTX", "Corporate Checking (22CTX)"));
        keyValues.add(new KeyLabelPair("32CTX", "Corporate Savings (32CTX)"));
        return keyValues;
    }

}
