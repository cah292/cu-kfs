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
package com.rsmart.kuali.kfs.prje.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.sys.batch.service.WrappingBatchService;
import org.kuali.kfs.sys.service.ReportWriterService;

import com.rsmart.kuali.kfs.prje.businessobject.PRJEAccountLine;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEAuditItem;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEBaseAccount;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEBaseObject;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEType;
import com.rsmart.kuali.kfs.prje.service.PRJEReports;
import com.rsmart.kuali.kfs.prje.service.PRJEService;
import com.rsmart.kuali.kfs.prje.util.PRJETransferRecord;

public class PRJEReportsImpl extends PRJEServiceBase implements PRJEReports {
    private static Logger LOG = Logger.getLogger(PRJEReportsImpl.class);
        
    private ReportWriterService ledgerReportWriterService;
    private ReportWriterService auditReportWriterService;
    private PRJEService prjeService;
    
    public void writeReports(List<PRJETransferRecord> transferRecords) {
        writePostingStatistics(transferRecords);
        writePostingLedger(transferRecords);
        writeAuditReport(transferRecords);
    }

    public void writePostingStatistics(List<PRJETransferRecord> transferRecords) {
        LOG.info("Writing Posting Statistics Report");
        try {
            // TODO: This            
        }
        catch ( Exception e ) {
            LOG.error("Error running writePostingStatistics", e);
        }
    }
    
    public void writePostingLedger(List<PRJETransferRecord> transferRecords) {
        LOG.info("Writing Posting Ledger Report");        
        try {
            ReportWriterService reportWriterService = getLedgerReportWriterService();
            
            if ( reportWriterService instanceof WrappingBatchService )
                ((WrappingBatchService)reportWriterService).initialize();
            
            // Use the Ledger Summary Report class to produce the report
            
            LedgerSummaryReport ledgerReport = new LedgerSummaryReport();
            
            for ( PRJETransferRecord transferRecord : transferRecords ) {
                ledgerReport.summarizeEntry(transferRecord.getDebitEntry());
                ledgerReport.summarizeEntry(transferRecord.getCreditEntry());
            }
            
            ledgerReport.writeReport(reportWriterService);
            
            if ( reportWriterService instanceof WrappingBatchService )
                ((WrappingBatchService)reportWriterService).destroy();
        }
        catch ( Exception e ) {
            LOG.error("Error running writePostingLedger", e);
        }
    }
    
    public void writeAuditReport(List<PRJETransferRecord> transferRecords) {
        LOG.info("Writing Audit Report");        
        try {
            ReportWriterService reportWriterService = getAuditReportWriterService();
            if ( reportWriterService instanceof WrappingBatchService )
                ((WrappingBatchService)reportWriterService).initialize();

            PRJEType lastType = null;
            List<PRJETransferRecord> group = new ArrayList<PRJETransferRecord>();
            
            for ( PRJETransferRecord transferRecord : transferRecords ) {
                PRJEType type = transferRecord.getBaseAccount().getType();
                
                if ( type != lastType && group.size() > 0 ) {
                    writeAuditReportGroup(group);
                    group.clear();                    
                }
                
                group.add(transferRecord);                    
                lastType = type;
            }
            
            if ( group.size() > 0 )
                writeAuditReportGroup(group);
            
            if ( reportWriterService instanceof WrappingBatchService )
                ((WrappingBatchService)reportWriterService).destroy();
        }
        catch ( Exception e ) {
            LOG.error("Error running writeAuditReport", e);
        }
    }
    
    private void writeAuditReportGroup(List<PRJETransferRecord> transferRecords) {
        ReportWriterService reportWriterService = getAuditReportWriterService();
        PRJEType type = transferRecords.get(0).getBaseAccount().getType();
        
        // Write the Header
        writeAuditReportHeader(type);
        
        // Write the Audit Entries
        List<PRJEAuditItem> auditItems = generateAuditItems(transferRecords);
        reportWriterService.writeTable(auditItems, true, false);
        
        reportWriterService.writeNewLines(3);        
    }
    
    private void writeAuditReportHeader(PRJEType type) {
        ReportWriterService reportWriterService = getAuditReportWriterService();

        reportWriterService.writeFormattedMessageLine("%35s: %-62s %35s: %-62s", 
                "Prorate Type Name", type.getEntryName(), 
                "Prorate Cardinality", getProrateOptionsString(type.getProrateOptions()));

        for ( PRJEBaseAccount baseAccount : type.getBaseAccounts() )
            writeAuditReportBaseAccount(baseAccount);
        
        reportWriterService.writeFormattedMessageLine("%35s: %-165s", 
                "Base Object Code Ranges", getObjectCodeRanges(type));
        
        reportWriterService.writeNewLines(1);
                
        reportWriterService.writeFormattedMessageLine(
                "Generated Transactions for PRJE Type %s (* Indicates Override)",
                type.getEntryName());
        
        reportWriterService.writeNewLines(1);
    }
    
    private void writeAuditReportBaseAccount(PRJEBaseAccount baseAccount) {
        ReportWriterService reportWriterService = getAuditReportWriterService();

        reportWriterService.writeFormattedMessageLine("%35s: %-62s %35s: %-62s",
                "Prorate Percent or Amount", getRateOrAmount(baseAccount),
                "Prorate Source Frequency", getFrequencyString(baseAccount.getFrequency()));

        reportWriterService.writeFormattedMessageLine("%35s: %-62s %35s: %-62s",
                "Base Account Nbr", formatAccount(baseAccount.getBaseAccount()),
                "From Account Number", formatAccount(baseAccount.getFromAccount()));

        reportWriterService.writeFormattedMessageLine("%35s: %-62s %35s: %-62s",        
                "Base Sub-Account Nbr", formatSubAccount(baseAccount.getBaseSubAccount()),
                "From Sub-Account Number", formatSubAccount(baseAccount.getFromSubAccount()));        
    }
    
    private String formatAccount(String account) {
        if ( account != null )
            return account;
        else
            return "-------";
    }
    
    private String formatSubAccount(String subAccount) {
        if ( subAccount != null )
            return subAccount;
        else
            return "-----";
    }
    
    private String getRateOrAmount(PRJEBaseAccount baseAccount) {
        if ( ProrateDebitType.AMOUNT.getKey().equals(baseAccount.getProrateType()) )
            return baseAccount.getProrateAmount().toString();
        else
            return baseAccount.getProratePercent() + "%";
    }
    
    private String getProrateOptionsString(String prorateOption) {        
        ProrateOptions[] options = ProrateOptions.values();
        for ( int i = 0; i < options.length; i++ ) {
            ProrateOptions option = options[i];
            if ( option.getKey().equals(prorateOption) )
                return option.getLabel();
        }
        return "<Unknown Prorate Option>";
    }
    
    private String getFrequencyString(String frequency) {
        Frequency[] freqs = Frequency.values();
        for ( int i = 0; i < freqs.length; i++ ) {
            Frequency freq = freqs[i];
            if ( freq.getKey().equals(frequency) )
                return freq.getLabel();
        }
        return "<Unknown Frequency>";        
    }
    
    private String getObjectCodeRanges(PRJEType type) {
        StringBuffer sb = new StringBuffer();
        
        List<PRJEBaseObject> baseObjects = type.getBaseObjects();
        for ( PRJEBaseObject baseObject : baseObjects ) {
            if ( sb.length() > 0 )
                sb.append(", ");
            
            sb.append(baseObject.getObjectCodeRangeName());
        }
        
        return sb.toString();
    }
    
    private String generateDescription(PRJETransferRecord transferRecord) {
        PRJEBaseAccount baseAccount = transferRecord.getBaseAccount(); 
        PRJEAccountLine accountLine = transferRecord.getAccountLine();
        PRJEType type = baseAccount.getType();
        
        StringBuffer description = new StringBuffer();
        description.append(type.getEntryName());

        if ( ProrateDebitType.PERCENTAGE.getKey().equals(accountLine.getOverrideProrateType()) ) {
            description.append(" ");            
            description.append(accountLine.getOverridePercent());
            description.append("pct of ");
            description.append(transferRecord.getBalance());
        }            
        else if ( ProrateDebitType.PERCENTAGE.getKey().equals(baseAccount.getProrateType()) ) {
            description.append(" ");            
            description.append(baseAccount.getProratePercent());
            description.append("pct of ");
            description.append(transferRecord.getBalance());
        }    
        
        return description.toString();
    }
    
    private List<PRJEAuditItem> generateAuditItems(List<PRJETransferRecord> transferRecords) {
        List<PRJEAuditItem> items = new ArrayList<PRJEAuditItem>();        
        for ( PRJETransferRecord transferRecord : transferRecords ) {
            PRJEAuditItem auditItem = new PRJEAuditItem(transferRecord);
            auditItem.setDescription(generateDescription(transferRecord));
            items.add(auditItem);
        }        
        return items;
    }
    
    public ReportWriterService getLedgerReportWriterService() {
        return ledgerReportWriterService;
    }

    public void setLedgerReportWriterService(ReportWriterService ledgerReportWriterService) {
        this.ledgerReportWriterService = ledgerReportWriterService;
    }

    public ReportWriterService getAuditReportWriterService() {
        return auditReportWriterService;
    }

    public void setAuditReportWriterService(ReportWriterService auditReportWriterService) {
        this.auditReportWriterService = auditReportWriterService;
    }

    public PRJEService getPrjeService() {
        return prjeService;
    }

    public void setPrjeService(PRJEService prjeService) {
        this.prjeService = prjeService;
    }      
}
