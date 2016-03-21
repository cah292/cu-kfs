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
package com.rsmart.kuali.kfs.prje.util;

import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import com.rsmart.kuali.kfs.prje.businessobject.PRJEAccountLine;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEBaseAccount;

/**
 * Encapsulates Withdrawal and Deposit data for PRJE Transactions.  Used as
 * a common representation of transfer data between the PRJE Service and
 * Reporting system.  
 */
public class PRJETransferRecord {
    // Variables assigned in the constructor
    private PRJEBaseAccount baseAccount;
    private PRJEAccountLine accountLine;
    
    // Variables assigned over the life of the record 
    private KualiDecimal balance = new KualiDecimal(0.0);
    private KualiDecimal amount = new KualiDecimal(0.0);
    private OriginEntryFull debitEntry;
    private OriginEntryFull creditEntry;
    
    public PRJETransferRecord(PRJEBaseAccount baseAccount, PRJEAccountLine accountLine) {
        this.baseAccount = baseAccount;
        this.accountLine = accountLine;
    }
    
    public PRJEBaseAccount getBaseAccount() {
        return baseAccount;
    }

    public void setBaseAccount(PRJEBaseAccount baseAccount) {
        this.baseAccount = baseAccount;
    }

    public PRJEAccountLine getAccountLine() {
        return accountLine;
    }

    public void setAccountLine(PRJEAccountLine accountLine) {
        this.accountLine = accountLine;
    }

    public KualiDecimal getBalance() {
        return balance;
    }

    public void setBalance(KualiDecimal balance) {
        this.balance = balance;
    }
    
    public KualiDecimal getAmount() {
        return amount;
    }

    public void setAmount(KualiDecimal amount) {
        this.amount = amount;
    }

    public OriginEntryFull getDebitEntry() {
        return debitEntry;
    }

    public void setDebitEntry(OriginEntryFull debitEntry) {
        this.debitEntry = debitEntry;
    }

    public OriginEntryFull getCreditEntry() {
        return creditEntry;
    }

    public void setCreditEntry(OriginEntryFull creditEntry) {
        this.creditEntry = creditEntry;
    }        
}
