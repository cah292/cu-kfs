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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ObjectType;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.api.parameter.Parameter;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;

import com.rsmart.kuali.kfs.prje.ProrateJournalEntry;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEAccountLine;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEBaseAccount;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEBaseObject;
import com.rsmart.kuali.kfs.prje.businessobject.PRJESet;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEType;
import com.rsmart.kuali.kfs.prje.dataaccess.PRJEDataAccess;
import com.rsmart.kuali.kfs.prje.service.PRJEReports;
import com.rsmart.kuali.kfs.prje.service.PRJEService;
import com.rsmart.kuali.kfs.prje.util.PRJETransferRecord;

public class PRJEServiceImpl extends PRJEServiceBase implements PRJEService {
    private static Logger LOG = Logger.getLogger(PRJEServiceImpl.class);
    private static final KualiDecimal ZERO = new KualiDecimal(0.0);

    private PRJEReportsImpl prjeReports;

    public boolean process() {
        LOG.info("Beginning PRJE Service Processing");

        try {
            new WorkContext().run();
            return true;
        }
        catch (Exception e) {
            LOG.error("Error running PRJE Service Proccessing", e);
            return false;
        }
    }

    public PRJEReportsImpl getPrjeReports() {
        return prjeReports;
    }

    public void setPrjeReports(PRJEReportsImpl prjeReports) {
        this.prjeReports = prjeReports;
    }

    // ---------------------------------------------------------------------------

    /**
     * WorkContext
     */
    private class WorkContext implements Runnable {
        // Per-Type Variables
        public int matchingBalances;
        public KualiDecimal availableBalance;

        // Property Strings
        public String propFiscalYear;
        public String propCurrentPeriod;
        public String propObjectTypes;
        public String propProcessOrder;

        // Other Parameters
        public List<PRJETransferRecord> processedTransfers = new ArrayList<PRJETransferRecord>();
        public Map<PRJEType, Integer> typeSequences = new HashMap<PRJEType, Integer>();
        public int lastTypeSequence = 1;
        public int lastOriginSequence = 1;

        public List<String> objectTypes;
        public Integer fiscalYear;
        public Map<Integer, String> processOrder;
        public String currentPeriod;
        public Date transactionDate;

        public WorkContext() {
        }

        public void run() {
            configure();

            // TODO: If a set is missing from the PROCESS_ORDER then we need
            // to output to the report the error condition and abort
            // the process with the reason (and possibly the set IDs
            // that are missing).

            PRJEDataAccess prjeDataAccess = getPrjeDataAccess();
            Collection<PRJESet> sets = prjeDataAccess.getPRJESets(true);
            Map<String, PRJESet> setMap = new HashMap<String, PRJESet>();

            List<String> setOrder;
            if (propProcessOrder != null)
                setOrder = Arrays.asList(propProcessOrder.split(";"));
            else
                setOrder = new ArrayList<String>();

            // Add the Set to a Name Mapping, filtering by Fiscal Year
            for (PRJESet set : sets) {
                if (set.getFiscalYear().equals(fiscalYear)) {
                    setMap.put(set.getSetName(), set);

                    LOG.info("Adding to Set Map: " + set.getSetName());
                }
            }

            if (setMap.isEmpty())
                throw new RuntimeException("List of Sets to Process is Empty");

            // Process the set in PROCESS_ORDER
            for (String id : setOrder) {
                PRJESet set = setMap.get(id.trim());

                if (set == null)
                    throw new RuntimeException("Cannot Find Set: " + id);

                processSet(set);
            }

            // Write the Origin Entries output file
            writeStagingEntries(generateOriginEntries(processedTransfers));

            // Write the Reports
            PRJEReports reports = getPrjeReports();
            reports.writeReports(processedTransfers);
        }

        private void configure() {
            ParameterService parameterService = getParameterService();

            Collection<String> configuration = parameterService.getParameterValuesAsString(ProrateJournalEntry.class, CONFIGURATION);

            for (String type : configuration) {
                if (PROPERTIES.equalsIgnoreCase(type.trim()))
                    configureFromProperties();
                else if (PARAMETERS.equalsIgnoreCase(type.trim()))
                    configureFromParameters();
                else
                    LOG.warn("Invalid Configuration type: " + type);
            }

            // Display gathered Properties
            LOG.info("Property: " + OBJECT_TYPE + "='" + propObjectTypes + "'");
            LOG.info("Property: " + TABLE_FISCAL_YEAR + "='" + propFiscalYear + "'");
            LOG.info("Property: " + TABLE_CURRENT_PERIOD + "='" + propCurrentPeriod + "'");
            LOG.info("Property: " + PROCESS_ORDER + "='" + propProcessOrder + "'");

            DateTimeService dts = getDateTimeService();
            UniversityDateService uds = getUniversityDateService();
            transactionDate = new Date(dts.getCurrentDate().getTime());

            if (StringUtils.isNotBlank(propObjectTypes))
                objectTypes = Arrays.asList(propObjectTypes.split(";"));
            else
                objectTypes = new ArrayList<String>();

            if (StringUtils.isNotBlank(propFiscalYear)) {
                try {
                    fiscalYear = Integer.parseInt(propFiscalYear.trim());
                }
                catch (Exception e) {
                    LOG.warn("Could not parse Fiscal Year");
                }
            }

            if (fiscalYear == null)
                fiscalYear = uds.getCurrentFiscalYear();

            if (StringUtils.isNotBlank(propCurrentPeriod))
                currentPeriod = propCurrentPeriod;
            else
                currentPeriod = uds.getCurrentUniversityDate().getUniversityFiscalAccountingPeriod();

            LOG.info("Processing Fiscal Year: " + fiscalYear);
            LOG.info("Processing Fiscal Period: " + currentPeriod);
        }

        private void configureFromProperties() {
            // check the available Properties object for configuration
            Properties properties = getProperties();

            if (propObjectTypes == null && properties.containsKey(OBJECT_TYPE))
                propObjectTypes = properties.getProperty(OBJECT_TYPE);

            if (propFiscalYear == null && properties.containsKey(TABLE_FISCAL_YEAR))
                propFiscalYear = properties.getProperty(TABLE_FISCAL_YEAR);

            if (propCurrentPeriod == null && properties.containsKey(TABLE_CURRENT_PERIOD))
                propCurrentPeriod = properties.getProperty(TABLE_CURRENT_PERIOD);

            if (propProcessOrder == null && properties.containsKey(PROCESS_ORDER))
                propProcessOrder = properties.getProperty(PROCESS_ORDER);
        }

        private void configureFromParameters() {
            // Retrieve Parameters
            ParameterService parameterService = getParameterService();

            if (propObjectTypes == null) {
                propObjectTypes = parameterService.getParameterValueAsString(ProrateJournalEntry.class, OBJECT_TYPE);
            }

            if (propFiscalYear == null) {
                propFiscalYear = parameterService.getParameterValueAsString(ProrateJournalEntry.class, TABLE_FISCAL_YEAR);
            }

            if (propCurrentPeriod == null) {
                propCurrentPeriod = parameterService.getParameterValueAsString(ProrateJournalEntry.class, TABLE_CURRENT_PERIOD);
            }

            if (propProcessOrder == null) {
                propProcessOrder = parameterService.getParameterValueAsString(ProrateJournalEntry.class, PROCESS_ORDER);
            }
        }

        // ---------------------------------------------------------------------

        private void processSet(PRJESet set) {
            LOG.info("Processing Set: " + set.getSetName());

            // For each From (Base) account entry in the JE Prorate Type Table:
            Collection<PRJEType> types = set.getTypes();

            // This requires two passes, a first to calculate all available
            for (PRJEType type : types) {
                if (Boolean.TRUE.equals(type.getActive())) {
                    // Read a row from the Prorate JE Type Table
                    processType(type);
                }
            }
        }

        private void processType(PRJEType type) {
            LOG.info("- Processing Type: " + type.getEntryName());

            // Read the Prorate Account table and calculate the amount money
            // to distribute from the From (Base) Account based on the amount
            // or percent type. If the type resides at the account level, use
            // the account level type. If the type does not reside at the
            // account, then use the type stored at the Prorate JE Type level.

            // In dealing with multiple accounts, the calculation should be
            // done by first adding the balance amounts, multiply by the
            // rate, then round the total.

            // Perform the transfer for this Type

            if (calculateAvailableBalance(type))
                performTransfer(type);
            else
                LOG.warn("-- No Balances matched: " + type.getEntryName());
        }

        private void performTransfer(PRJEType type) {
            LOG.info("-- Performing Transfer");

            List<PRJETransferRecord> transferRecords = generatePRJETransferRecords(type);

            KualiDecimal amountTransferred = ZERO;
            for (PRJETransferRecord transferRecord : transferRecords) {
                KualiDecimal amount = processPRJETransferRecord(transferRecord);
                amountTransferred = amountTransferred.add(amount.abs());
            }

            if (amountTransferred.isZero())
                LOG.warn("--- Total Amount Transferred is Zero");
        }

        private KualiDecimal processPRJETransferRecord(PRJETransferRecord transferRecord) {
            KualiDecimal amount = calculateTransfer(transferRecord);

            if (amount.isNonZero()) {
                transferRecord.setBalance(availableBalance);
                transferRecord.setAmount(amount);

                // Create the Origin Entries based on the Transfer Record Amount
                OriginEntryFull debitEntry = generateDebitEntry(transferRecord);
                OriginEntryFull creditEntry = generateCreditEntry(transferRecord);

                transferRecord.setDebitEntry(debitEntry);
                transferRecord.setCreditEntry(creditEntry);

                processedTransfers.add(transferRecord);
            }
            else
                LOG.warn("--- Attempting to Transfer Zero");

            return amount;
        }

        // ---------------------------------------------------------------------

        private OriginEntryFull generateDebitEntry(PRJETransferRecord transferRecord) {
            PRJEBaseAccount baseAccount = transferRecord.getBaseAccount();
            KualiDecimal amount = transferRecord.getAmount();
            KualiDecimal balance = transferRecord.getBalance();

            // From (Base) Side:

            // Debit the From (Base) Account for expense object code,
            // or credit the From (Base) Account for revenue object
            // code. However, if the override chart and accounts are
            // defined use the amount under the From (Base) chart-account
            // for calculation, but move the money from the override
            // chart-account.

            String account;
            String subAccount;
            String chartCode;

            // Decide The Source of the Debit (From or Base)
            if (StringUtils.isNotBlank(baseAccount.getFromAccount())) {
                account = baseAccount.getFromAccount();
                subAccount = baseAccount.getFromSubAccount();
                chartCode = baseAccount.getFromChart();
            }
            else {
                account = baseAccount.getBaseAccount();
                subAccount = baseAccount.getBaseSubAccount();
                chartCode = baseAccount.getBaseChart();
            }

            LOG.info("--- Debit " + amount + " from Base Account " + account);

            PRJEType type = baseAccount.getType();

            // Create the origin entry
            OriginEntryFull debitEntry = new OriginEntryFull(ENTRY_DOCTYPE, ENTRY_ORIGIN);
            debitEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);

            debitEntry.setOrganizationDocumentNumber(getOrganizationDocumentNumber(type));
            debitEntry.setDocumentNumber(getDocumentNumber(type));
            debitEntry.setAccountNumber(account);
            debitEntry.setChartOfAccountsCode(chartCode);

            if (subAccount != null)
                debitEntry.setSubAccountNumber(subAccount);

            String objectCode = baseAccount.getFromObjectCode();
            if (objectCode != null) {
                ObjectCodeService ocs = getObjectCodeService();
                ObjectCode oc = ocs.getByPrimaryId(fiscalYear, chartCode, objectCode);

                if (oc != null) {
                    debitEntry.setFinancialObjectTypeCode(oc.getFinancialObjectTypeCode());
                    debitEntry.setFinancialObjectCode(oc.getFinancialObjectCode());
                    ObjectType ot = oc.getFinancialObjectType();
                    if (transferRecord.getBaseAccount().getProrateType().equals(ProrateDebitType.PERCENTAGE.getKey())) {
                        debitEntry.setTransactionDebitCreditCode(deriveDebitSign(ot, balance));
                    }
                    else {
                        debitEntry.setTransactionDebitCreditCode("D");
                    }
                }
                else
                    LOG.warn("---- No Object Code " + objectCode + " in " + fiscalYear);
            }
            else
                LOG.warn("---- No Object Code -- Can't Derive Debit/Credit Code");

            debitEntry.setUniversityFiscalPeriodCode(currentPeriod);
            debitEntry.setUniversityFiscalYear(fiscalYear);

            debitEntry.setTransactionDate(transactionDate);
            debitEntry.setTransactionLedgerEntryAmount(amount.abs());
            debitEntry.setTransactionLedgerEntrySequenceNumber(lastOriginSequence++);
            debitEntry.setTransactionScrubberOffsetGenerationIndicator(true);

            String description = generateDebitDescription(transferRecord);
            debitEntry.setTransactionLedgerEntryDescription(description);

            return debitEntry;
        }

        private OriginEntryFull generateCreditEntry(PRJETransferRecord transferRecord) {
            PRJEAccountLine accountLine = transferRecord.getAccountLine();
            KualiDecimal amount = transferRecord.getAmount();
            KualiDecimal balance = transferRecord.getBalance();

            LOG.info("--- Credit " + amount + " to Account " + accountLine.getAccountNumber());

            // If Sub-Account and Sub-Objects exist, then use them. If
            // Override accounts and objects do not have sub account or
            // sub object, then post them at the override account and
            // object.

            PRJEType type = accountLine.getType();

            // Calculate Amount to Credit
            String prorateType = accountLine.getOverrideProrateType();

            String account = accountLine.getAccountNumber();
            String subAccount = accountLine.getSubAccountNumber();
            String chartCode = accountLine.getChartCode();
            String objectCode = accountLine.getObjectCode();
            String projectCode = accountLine.getProjectCode();

            // To Side:

            // Credit the departmental Account for expense object codes,
            // or debit the departmental account for revenue object
            // codes.

            // Always post the prorated amounts to the account and object
            // code (no sub-account and sub-object codes) since they are
            // not guaranteed to exist.

            // Create the origin entry
            OriginEntryFull creditEntry = new OriginEntryFull(ENTRY_DOCTYPE, ENTRY_ORIGIN);
            creditEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);

            creditEntry.setOrganizationDocumentNumber(getOrganizationDocumentNumber(type));
            creditEntry.setDocumentNumber(getDocumentNumber(type));
            creditEntry.setAccountNumber(account);
            creditEntry.setSubAccountNumber(subAccount);
            creditEntry.setChartOfAccountsCode(chartCode);
            creditEntry.setProjectCode(projectCode);

            if (objectCode == null) {
                LOG.warn("---- No Object Code -- Using Base Account Object Code Instead");
                objectCode = transferRecord.getBaseAccount().getFromObjectCode();
            }

            ObjectCodeService ocs = getObjectCodeService();
            ObjectCode oc = ocs.getByPrimaryId(fiscalYear, chartCode, objectCode);

            if (oc != null) {
                creditEntry.setFinancialObjectTypeCode(oc.getFinancialObjectTypeCode());
                creditEntry.setFinancialObjectCode(oc.getFinancialObjectCode());
                ObjectType ot = oc.getFinancialObjectType();
                if (transferRecord.getBaseAccount().getProrateType().equals(ProrateDebitType.PERCENTAGE.getKey())) {
                    creditEntry.setTransactionDebitCreditCode(deriveCreditSign(ot, balance));
                }
                else {
                    creditEntry.setTransactionDebitCreditCode("C");
                }
            }
            else
                LOG.warn("---- No Object Code " + objectCode + " in " + fiscalYear);

            creditEntry.setUniversityFiscalPeriodCode(currentPeriod);
            creditEntry.setUniversityFiscalYear(fiscalYear);

            creditEntry.setTransactionDate(transactionDate);
            creditEntry.setTransactionLedgerEntryAmount(amount.abs());
            creditEntry.setTransactionLedgerEntrySequenceNumber(lastOriginSequence++);
            creditEntry.setTransactionScrubberOffsetGenerationIndicator(true);

            String description = generateCreditDescription(transferRecord);
            creditEntry.setTransactionLedgerEntryDescription(description);

            return creditEntry;
        }

        private void writeStagingEntries(List<OriginEntryFull> originEntries) {
            LOG.info("Writing Origin Entries to Staging");

            String dataFilename = getBatchFileDirectoryName() + File.separator + DATA_FILE;
            String doneFilename = getBatchFileDirectoryName() + File.separator + DONE_FILE;

            try {
                PrintStream ps = new PrintStream(dataFilename);

                for (OriginEntryFull originEntry : originEntries) {

                    // Do a little bit of house cleaning of entries
                    if (StringUtils.isEmpty(originEntry.getSubAccountNumber()))
                        originEntry.setSubAccountNumber("-----");
                    if (StringUtils.isEmpty(originEntry.getProjectCode()))
                        originEntry.setProjectCode("----------");
                    if (StringUtils.isEmpty(originEntry.getFinancialSubObjectCode()))
                        originEntry.setFinancialSubObjectCode("---");

                    ps.printf("%s\n", originEntry.getLine());

                    LOG.debug("- FLAT FILE: " + originEntry.getLine());
                }

                ps.close();

                // Touch the DONE file
                File doneFile = new File(doneFilename);
                if (!doneFile.exists())
                    doneFile.createNewFile();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // ---------------------------------------------------------------------

        private boolean calculateAvailableBalance(PRJEType type) {
            availableBalance = ZERO;
            int matchingBalances = 0;

            for (PRJEBaseAccount baseAccount : type.getBaseAccounts()) {
                LOG.debug("-- Processing Base Account " + baseAccount.getBaseAccount());

                // Retrieve the balances for this baseAccount
                BusinessObjectService bos = getBusinessObjectService();
                Map<String, String> args = new HashMap<String, String>();
                args.put("universityFiscalYear", Integer.toString(fiscalYear));
                args.put("chartOfAccountsCode", baseAccount.getBaseChart());
                args.put("accountNumber", baseAccount.getBaseAccount());

                if (baseAccount.getBaseSubAccount() != null)
                    args.put("subAccountNumber", baseAccount.getBaseSubAccount());

                List<Balance> balances = (List<Balance>) bos.findMatching(Balance.class, args);
                if (balances != null && balances.size() > 0) {
                    // Calculate the total available balance to leverage
                    KualiDecimal balance;
                    if (Frequency.YEARLY.getKey().equals(baseAccount.getFrequency()))
                        balance = calculateYearly(balances, baseAccount);
                    else
                        balance = calculateMonthly(balances, baseAccount);
                    availableBalance = availableBalance.add(balance);
                    matchingBalances++;

                    LOG.info("--- Balance for Base Account (" + baseAccount.getBaseChart() + "-" + baseAccount.getBaseAccount() + ") is " + balance);
                }
                else
                    LOG.warn("--- No Balances for Base Account " + baseAccount.getBaseAccount());
            }

            LOG.info("-- Available Balance is " + availableBalance);

            return matchingBalances > 0;
        }

        private List<PRJETransferRecord> generatePRJETransferRecords(PRJEType type) {
            // This can technically create a complete cross join for
            // many-to-many processing, but that would probably be rather
            // frightening.

            List<PRJEBaseAccount> baseAccounts = type.getBaseAccounts();
            List<PRJEAccountLine> accountLines = type.getAccountLines();

            List<PRJETransferRecord> transferRecords = new ArrayList<PRJETransferRecord>();

            for (PRJEBaseAccount baseAccount : baseAccounts) {
                for (PRJEAccountLine accountLine : accountLines) {
                    if (isBaseAccountValid(baseAccount) && isAccountLineValid(accountLine))
                        transferRecords.add(new PRJETransferRecord(baseAccount, accountLine));
                }
            }

            if (transferRecords.size() == 0)
                LOG.error("--- No Transfer Records to be processed");

            return transferRecords;
        }

        private List<OriginEntryFull> generateOriginEntries(List<PRJETransferRecord> transferRecords) {
            List<OriginEntryFull> originEntries = new ArrayList<OriginEntryFull>();

            for (PRJETransferRecord transferRecord : transferRecords) {
                originEntries.add(transferRecord.getDebitEntry());
                originEntries.add(transferRecord.getCreditEntry());
            }

            return originEntries;
        }

        private String generateDebitDescription(PRJETransferRecord transferRecord) {
            PRJEBaseAccount baseAccount = transferRecord.getBaseAccount();
            PRJEType type = baseAccount.getType();

            StringBuffer description = new StringBuffer();
            description.append(type.getEntryName());
            description.append(" (Base)");

            return description.toString();
        }

        private String generateCreditDescription(PRJETransferRecord transferRecord) {
            PRJEBaseAccount baseAccount = transferRecord.getBaseAccount();
            PRJEAccountLine accountLine = transferRecord.getAccountLine();
            PRJEType type = baseAccount.getType();

            StringBuffer description = new StringBuffer();
            description.append(type.getEntryName());
            description.append(" (To)");

            return description.toString();
        }

        private KualiDecimal calculateTransfer(PRJETransferRecord transferRecord) {
            PRJEBaseAccount baseAccount = transferRecord.getBaseAccount();
            PRJEAccountLine accountLine = transferRecord.getAccountLine();

            // Calculate Amount to Transfer
            KualiDecimal amount = ZERO;

            String prorateType = baseAccount.getProrateType();
            if (ProrateDebitType.AMOUNT.getKey().equals(prorateType))
                amount = baseAccount.getProrateAmount();
            else if (ProrateDebitType.PERCENTAGE.getKey().equals(prorateType)) {
                BigDecimal pct = baseAccount.getProratePercent().bigDecimalValue();
                BigDecimal bal = availableBalance.bigDecimalValue();
                amount = new KualiDecimal(pct.multiply(bal).divide(new BigDecimal(100.0)));
            }

            // See if there's an override
            prorateType = accountLine.getOverrideProrateType();
            if (ProrateCreditType.AMOUNT.getKey().equals(prorateType))
                amount = accountLine.getOverrideAmount();
            else if (ProrateCreditType.PERCENTAGE.getKey().equals(prorateType)) {
                BigDecimal pct = accountLine.getOverridePercent().bigDecimalValue();
                BigDecimal bal = availableBalance.bigDecimalValue();
                amount = new KualiDecimal(pct.multiply(bal).divide(new BigDecimal(100.0)));
            }

            return amount;
        }

        private boolean isBaseAccountValid(PRJEBaseAccount baseAccount) {
            LOG.debug("--- Validating Base Account " + baseAccount.getBaseAccount());

            // Is Base Account Active?
            if (!Boolean.TRUE.equals(baseAccount.getActive()))
                return false;

            // Does it have a proper ProrateType?
            String prorateType = baseAccount.getProrateType();
            if (!(ProrateDebitType.AMOUNT.getKey().equals(prorateType) || ProrateDebitType.PERCENTAGE.getKey().equals(prorateType)))
                return false;

            return true;
        }

        private boolean isAccountLineValid(PRJEAccountLine accountLine) {
            LOG.debug("--- Validating Account Line " + accountLine.getAccountNumber());

            if (!Boolean.TRUE.equals(accountLine.getActive()))
                return false;

            // Check to see if we're in the effective date range
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Date from = accountLine.getEffectiveDateFrom();
            Date to = accountLine.getEffectiveDateTo();

            if (from != null && to != null && to.before(from)) {
                LOG.warn("---- Effective From is After To");
                return false;
            }

            if ((from != null && now.before(from)) || (to != null && now.after(to))) {
                LOG.warn("---- Out of Account Effective Date Range");
                return false;
            }

            return true;
        }

        private String getOrganizationDocumentNumber(PRJEType type) {
            StringBuffer sb = new StringBuffer();

            sb.append(type.getSet().getSetId());
            sb.append("-");
            sb.append(type.getTypeId());

            return sb.toString();
        }

        private String getDocumentNumber(PRJEType type) {
            Integer seq = typeSequences.get(type);
            if (seq == null) {
                // Associate it with a Sequence Number
                seq = new Integer(lastTypeSequence++);
                typeSequences.put(type, seq);
            }

            StringBuffer sb = new StringBuffer();

            sb.append(type.getTypeId());
            sb.append("-");
            sb.append(seq);

            return sb.toString();
        }

        // ---------------------------------------------------------------------------

        private KualiDecimal calculateYearly(List<Balance> balances, PRJEBaseAccount baseAccount) {
            // If the �Use Annual Balance Amt� option is selected in the
            // type, then calculate the amount based on the
            // ACLN_ANNL_BAL_AMT for the specified object code ranges.

            KualiDecimal result = ZERO;
            for (Balance balance : balances) {
                PRJEType type = baseAccount.getType();
                if (balanceMatchesBaseObjects(type, balance)) {
                    KualiDecimal amount = balance.getAccountLineAnnualBalanceAmount();
                    result = result.add(normalizeBalance(balance, amount));
                }
            }

            LOG.debug("-- Returning " + result + " for Yearly");

            return result;
        }

        private KualiDecimal calculateMonthly(List<Balance> balances, PRJEBaseAccount baseAccount) {
            // Otherwise select the MO_ACCT_LN_AMT line corresponding to the
            // accounting period of the GL Balance Table for the specified
            // object code ranges.

            KualiDecimal result = ZERO;
            for (Balance balance : balances) {
                PRJEType type = baseAccount.getType();
                if (balanceMatchesBaseObjects(type, balance)) {
                    KualiDecimal amount = balance.getAmount(currentPeriod);
                    result = result.add(normalizeBalance(balance, amount));
                }
            }

            LOG.debug("-- Returning " + result + " for Monthly");

            return result;
        }

        private KualiDecimal normalizeBalance(Balance balance, KualiDecimal amount) {
            return amount;
        }

        private boolean balanceMatchesBaseObjects(PRJEType type, Balance balance) {
            if (!KFSConstants.BALANCE_TYPE_ACTUAL.equals(balance.getBalanceTypeCode())) {
                LOG.debug("--- Balance Type is not Actual");
                return false;
            }

            if (objectTypes.size() > 0 && !objectTypes.contains(balance.getObjectTypeCode())) {
                LOG.debug("--- Object Types do not match");
                return false;
            }

            // Note: The object code selection should be made based on �exclude�
            // or �include� flag.

            List<PRJEBaseObject> baseObjects = type.getBaseObjects();
            boolean matches = false;

            for (PRJEBaseObject base : baseObjects) {
                if (!Boolean.TRUE.equals(base.getActive()))
                    continue;

                if (!base.getBaseChartCode().equals(balance.getChartOfAccountsCode())) {
                    LOG.debug("--- Chart Codes do not match");
                    continue;
                }

                boolean include = Containment.INCLUDE.getKey().equals(base.getInclude());

                boolean useSubObject = base.getSubObjectCodeLow() != null && base.getSubObjectCodeHigh() != null;

                // Make sure that the parseInt accepts alpha-numeric objectCodes, lowCodes, and highCodes
                int objectCode = Integer.parseInt(balance.getObjectCode(), 36);
                int lowCode = Integer.parseInt(base.getBaseObjectCodeLow(), 36);
                int highCode = Integer.parseInt(base.getBaseObjectCodeHigh(), 36);
                boolean inSet = (objectCode >= lowCode && objectCode <= highCode);

                if (useSubObject) {
                    if (balance.getSubObjectCode() == null) {
                        LOG.debug("--- Balance does not have a Sub Object Code");
                        continue;
                    }

                    inSet &= (balance.getSubObjectCode().compareTo(base.getSubObjectCodeLow()) >= 0 && balance.getSubObjectCode().compareTo(base.getSubObjectCodeHigh()) <= 0);
                }

                if (include && inSet) {
                    matches = true;
                }
                else if (!include && inSet) {
                    matches = false;
                    break;
                }

            }

            if (matches)
                matchingBalances++;

            return matches;
        }

        private String deriveDebitSign(ObjectType objectType, KualiDecimal amount) {
            return amount.isNegative() ? "C" : "D";
        }

        private String deriveCreditSign(ObjectType objectType, KualiDecimal amount) {
            return amount.isNegative() ? "D" : "C";
        }

    }
}
