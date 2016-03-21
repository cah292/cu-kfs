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
package com.rsmart.kuali.kfs.prje.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.rice.krad.bo.TransientBusinessObjectBase;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import com.rsmart.kuali.kfs.prje.PRJEConstants.ProrateCreditType;
import com.rsmart.kuali.kfs.prje.util.PRJETransferRecord;

/**
 * This class is only used in PRJE Reporting
 */
public class PRJEAuditItem extends TransientBusinessObjectBase {
    private String docType;
    private String docNumber;
    private String description;
    private String fromAccount;
    private String fromSubAccount;
    private String fromObjectCode;
    private String fromSubObject;
    private KualiDecimal fromBaseAmount;
    private String fromRate;
    private KualiDecimal fromFromAmount;
    private String fromDebitCredit;
    private String toAccount;
    private String toSubAccount;
    private String toObjectCode;
    private String toSubObject;
    private KualiDecimal toAmount;
    private String toDebitCredit;
    
    public PRJEAuditItem() {
        
    }
    
    public PRJEAuditItem(PRJETransferRecord transferRecord) {
        OriginEntryFull debitEntry = transferRecord.getDebitEntry();
        OriginEntryFull creditEntry = transferRecord.getCreditEntry();
        PRJEBaseAccount baseAccount = transferRecord.getBaseAccount();
        PRJEAccountLine accountLine = transferRecord.getAccountLine();
        
        this.docType = debitEntry.getFinancialDocumentTypeCode();
        this.docNumber = debitEntry.getDocumentNumber();
        this.fromAccount = debitEntry.getAccountNumber();
        this.fromSubAccount = debitEntry.getSubAccountNumber();
        this.fromObjectCode = debitEntry.getFinancialObjectCode();
        this.fromSubObject = debitEntry.getFinancialSubObjectCode();
        this.fromBaseAmount = transferRecord.getBalance();
        
        if ( ProrateCreditType.NO_OVERRIDE.getKey().equals(accountLine.getOverrideProrateType()))
            this.fromRate = baseAccount.getProratePercent().toString();
        else
            this.fromRate = baseAccount.getProratePercent() + "*";
        
        this.fromFromAmount = debitEntry.getTransactionLedgerEntryAmount();
        this.fromDebitCredit = debitEntry.getTransactionDebitCreditCode() + "R";
        this.toAccount = creditEntry.getAccountNumber();
        this.toSubAccount = creditEntry.getSubAccountNumber();
        this.toObjectCode = creditEntry.getFinancialObjectCode();
        this.toSubObject = creditEntry.getFinancialSubObjectCode();
        this.toAmount = creditEntry.getTransactionLedgerEntryAmount();
        this.toDebitCredit = creditEntry.getTransactionDebitCreditCode() + "R";
    }
    
    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getFromSubAccount() {
        return fromSubAccount;
    }

    public void setFromSubAccount(String fromSubAccount) {
        this.fromSubAccount = fromSubAccount;
    }

    public String getFromObjectCode() {
        return fromObjectCode;
    }

    public void setFromObjectCode(String fromObjectCode) {
        this.fromObjectCode = fromObjectCode;
    }

    public String getFromSubObject() {
        return fromSubObject;
    }

    public void setFromSubObject(String fromSubObject) {
        this.fromSubObject = fromSubObject;
    }

    public KualiDecimal getFromBaseAmount() {
        return fromBaseAmount;
    }

    public void setFromBaseAmount(KualiDecimal fromBaseAmount) {
        this.fromBaseAmount = fromBaseAmount;
    }

    public String getFromRate() {
        return fromRate;
    }

    public void setFromRate(String fromRate) {
        this.fromRate = fromRate;
    }

    public KualiDecimal getFromFromAmount() {
        return fromFromAmount;
    }

    public void setFromFromAmount(KualiDecimal fromFromAmount) {
        this.fromFromAmount = fromFromAmount;
    }

    public String getFromDebitCredit() {
        return fromDebitCredit;
    }

    public void setFromDebitCredit(String fromDebitCredit) {
        this.fromDebitCredit = fromDebitCredit;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public String getToSubAccount() {
        return toSubAccount;
    }

    public void setToSubAccount(String toSubAccount) {
        this.toSubAccount = toSubAccount;
    }

    public String getToObjectCode() {
        return toObjectCode;
    }

    public void setToObjectCode(String toObjectCode) {
        this.toObjectCode = toObjectCode;
    }

    public String getToSubObject() {
        return toSubObject;
    }

    public void setToSubObject(String toSubObject) {
        this.toSubObject = toSubObject;
    }

    public KualiDecimal getToAmount() {
        return toAmount;
    }

    public void setToAmount(KualiDecimal toAmount) {
        this.toAmount = toAmount;
    }

    public String getToDebitCredit() {
        return toDebitCredit;
    }

    public void setToDebitCredit(String toDebitCredit) {
        this.toDebitCredit = toDebitCredit;
    }

    @SuppressWarnings("unchecked")
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();

        m.put("docType", getDocType());
        m.put("docNumber", getDocNumber());
        m.put("description", getDescription());
        m.put("fromAccount", getFromAccount());
        m.put("fromSubAccount", getFromSubAccount());
        m.put("fromObjectCode", getFromObjectCode());
        m.put("fromSubObject", getFromSubObject());
        m.put("fromBaseAmount", getFromBaseAmount());
        m.put("fromRate", getFromRate());
        m.put("fromFromAmount", getFromFromAmount());
        m.put("fromDebitCredit", getFromDebitCredit());
        m.put("toAccount", getToAccount());
        m.put("toSubAccount", getToSubAccount());
        m.put("toObjectCode", getToObjectCode());
        m.put("toSubObject", getToSubObject());
        m.put("toAmount", getToAmount());
        m.put("toDebitCredit", getToDebitCredit());
                
        return m;
    }    
}
