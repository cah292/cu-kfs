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
package com.rsmart.kuali.kfs.prje.service;

import java.util.List;

import com.rsmart.kuali.kfs.prje.util.PRJETransferRecord;

public interface PRJEReports {
    /**
     * Will generate all of the Reports made available by this module
     * @param transferRecords
     */
    void writeReports(List<PRJETransferRecord> transferRecords);
    
    void writePostingStatistics(List<PRJETransferRecord> transferRecords);
    
    void writePostingLedger(List<PRJETransferRecord> transferRecords);
    
    void writeAuditReport(List<PRJETransferRecord> transferRecords);
}
