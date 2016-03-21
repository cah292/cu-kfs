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
package com.rsmart.kuali.kfs.prje.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import com.rsmart.kuali.kfs.prje.service.PRJEFiscalYear;

/**
 * PRJE Fiscal Year Step Impl
 * 
 * @author Derek Helbert
 */
public class PRJEFiscalYearStep extends AbstractStep {
    
    private PRJEFiscalYear prjeFiscalYear;
    
    /**
     * Execute
     * 
     * @param jobName
     * @param jobRunDate
     * @return
     * @throws InterruptedException
     */
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        return getPrjeFiscalYear().process();
    }

    /**
     * Get Prje Fiscal Year
     * 
     * @return
     */
    public PRJEFiscalYear getPrjeFiscalYear() {
        return prjeFiscalYear;
    }

    /**
     * Set Prje Fiscal Year
     * 
     * @param prjeFiscalYear
     */
    public void setPrjeFiscalYear(PRJEFiscalYear prjeFiscalYear) {
        this.prjeFiscalYear = prjeFiscalYear;
    }

}
