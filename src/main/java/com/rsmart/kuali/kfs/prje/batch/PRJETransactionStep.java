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
package com.rsmart.kuali.kfs.prje.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import com.rsmart.kuali.kfs.prje.service.PRJEService;

public class PRJETransactionStep extends AbstractStep {
    private PRJEService prjeService;
    
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        return getPrjeService().process();
    }

    public PRJEService getPrjeService() {
        return prjeService;
    }

    public void setPrjeService(PRJEService prjeService) {
        this.prjeService = prjeService;
    }
}
