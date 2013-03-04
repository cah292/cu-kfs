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
package com.rsmart.kuali.kfs.pdp.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.rice.kns.util.KualiInteger;

/**
 * A helper service for ACH bundler related mods.
 */
public interface AchBundlerHelperService {
    /**
     * Returns all PaymentDetail records for pending ACH payments for a given bank code and disbursement number
     * @param disbursementNumber
     * @param bankCode the bank code of the payment group of payment details to find
     * @return an iterator of PaymentDetail records matching the given criteria
     */
    public abstract Iterator<PaymentDetail> getPendingAchPaymentDetailsByDisbursementNumberAndBank(Integer disbursementNumber, String bankCode);
  
    /**
     * Returns all PaymentDetail records for pending ACH payments for a given bank code
     * @param bankCode the bank code of the payment group of payment details to find
     * @return an iterator of PaymentDetail records matching the given criteria
     */
    public abstract Iterator<PaymentDetail> getPendingAchPaymentDetailsByBank(String bankCode);
    
    /**
     * Returns a unique set of bank codes for all pending ACH payments.
     * 
     * @return
     */
    public abstract HashSet<String> getDistinctBankCodesForPendingAchPayments();
    
    /**
     * Returns a unique set of disbursement numbers for pending ACH payments against a specific bank code.
     * 
     * @param bankCode
     * @return
     */
    public abstract HashSet<Integer> getDistinctDisbursementNumbersForPendingAchPaymentsByBankCode(String bankCode);
    
    
    //KFSPTS-1460 -- Added to code received
    /**
     * Retrieves whether the ACH Bundler system parameter is set to Y or not.
     * 
     * @return
     */
    public abstract boolean shouldBundleAchPayments(); 
    
}

