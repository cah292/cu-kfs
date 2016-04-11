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
package com.rsmart.kuali.kfs.prje.service.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.FiscalYearMakerStep;

import com.rsmart.kuali.kfs.prje.businessobject.PRJEAccountLine;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEBaseAccount;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEBaseObject;
import com.rsmart.kuali.kfs.prje.businessobject.PRJESet;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEType;
import com.rsmart.kuali.kfs.prje.service.PRJEFiscalYear;

/**
 * PRJE Fiscal Year Impl
 * 
 * @author Derek Helbert
 */
public class PRJEFiscalYearImpl extends PRJEServiceBase implements PRJEFiscalYear {

    private static Logger LOG = Logger.getLogger(PRJEFiscalYearImpl.class);
    
    /**
     * 
     * @see com.rsmart.kuali.kfs.prje.PRJEFiscalYear#process()
     */
    public boolean process() {
        LOG.info("Starting PRJEFiscalYearImpl Process");
        
        String parmBaseYear = getParameterService().getParameterValueAsString(FiscalYearMakerStep.class, KFSConstants.ChartApcParms.FISCAL_YEAR_MAKER_SOURCE_FISCAL_YEAR);
        
        if( parmBaseYear == null ) {
            return false;
        }
        
        Integer sourceFiscalYear = new Integer(parmBaseYear);
        Integer nextFiscalYear   = new Integer(sourceFiscalYear.intValue() + 1);
        
        LOG.info("Source Fiscal Year : " + sourceFiscalYear);
        LOG.info("Next Fiscal Year   : " + nextFiscalYear);
        
        boolean replaceMode = getParameterService().getParameterValueAsBoolean(FiscalYearMakerStep.class, KFSConstants.ChartApcParms.FISCAL_YEAR_MAKER_REPLACE_MODE);
        
        LOG.info("Replace Mode       : " + replaceMode);
        
        int purgeCnt = 0;
        
        // Purge all sets for next year
        if( replaceMode ) {
            purgeCnt = purgePRJESets(nextFiscalYear);
            LOG.info("Purged PRJE Sets   : " + purgeCnt);
        }
        
        Collection<PRJESet> sets = getPRJESets(sourceFiscalYear,true);
        
        for(PRJESet s : sets) {
            PRJESet newSet = getActivePRJESet(nextFiscalYear, s.getSetName());
            
            if( newSet == null ) {
                newSet = new PRJESet();
                
                LOG.info("Copying Set   : " + s.getSetName());
                
                newSet.setFiscalYear(nextFiscalYear);
                newSet.setActive(Boolean.TRUE);
                newSet.setLastUpdate(new Timestamp(new java.util.Date().getTime()));
                newSet.setSetDescription(s.getSetDescription());
                newSet.setSetName(s.getSetName());
                
                //remove inactive types
                List<PRJEType> types = removeInactiveTypes(s.getTypes());
                
                for(PRJEType t : types) {
                    PRJEType newType = new PRJEType();
                    newType.setActive(t.getActive());
                    newType.setLastUpdate(new Timestamp(new java.util.Date().getTime()));
                    newType.setEntryName(t.getEntryName());
                    newType.setProrateOptions(t.getProrateOptions());
                    
                    List<PRJEAccountLine> accountLines = t.getAccountLines();
                    
                    for(PRJEAccountLine acl : accountLines) {
                        PRJEAccountLine accountLine = new PRJEAccountLine();
                        
                        accountLine.setActive(acl.getActive());
                        accountLine.setAccountNumber(acl.getAccountNumber());
                        accountLine.setSubAccountNumber(acl.getSubAccountNumber());
                        accountLine.setChartCode(acl.getChartCode());
                        
                        // Get current calendar
                        Calendar cal = Calendar.getInstance();
                        
                        if( acl.getEffectiveDateFrom() != null ) {
                            cal.setTimeInMillis(acl.getEffectiveDateFrom().getTime()); // Set time
                            cal.add(Calendar.YEAR, 1);                                 // Move up 1 year
                        
                            accountLine.setEffectiveDateFrom(new Date(cal.getTimeInMillis()));
                        }
                        
                        if( acl.getEffectiveDateTo() != null ) {
                            cal = Calendar.getInstance();
                            cal.setTimeInMillis(acl.getEffectiveDateTo().getTime()); // Set time
                            cal.add(Calendar.YEAR, 1);                               // Move up 1 year
                        
                            accountLine.setEffectiveDateTo(new Date(cal.getTimeInMillis()));
                        }
                        
                        accountLine.setLastUpdate(new Timestamp(new java.util.Date().getTime()));
                        accountLine.setObjectCode(acl.getObjectCode());
                        accountLine.setProjectCode(acl.getProjectCode());
                        accountLine.setOverrideAmount(acl.getOverrideAmount());
                        accountLine.setOverridePercent(acl.getOverridePercent());
                        accountLine.setOverrideProrateType(acl.getOverrideProrateType());
                        
                        // Add Account Line
                        newType.getAccountLines().add(accountLine);
                    }
                    
                    List<PRJEBaseAccount> baseAccounts = t.getBaseAccounts();
                    
                    for(PRJEBaseAccount ba : baseAccounts) {
                        PRJEBaseAccount baseAccount = new PRJEBaseAccount();
                        
                        baseAccount.setBaseAccount(ba.getBaseAccount());
                        baseAccount.setBaseChart(ba.getBaseChart());
                        baseAccount.setBaseSubAccount(ba.getBaseSubAccount());
                        baseAccount.setFrequency(ba.getFrequency());
                        baseAccount.setProrateType(ba.getProrateType());
                        baseAccount.setProrateAmount(ba.getProrateAmount());
                        baseAccount.setProratePercent(ba.getProratePercent());
                        baseAccount.setFromAccount(ba.getFromAccount());
                        baseAccount.setFromChart(ba.getFromChart());
                        baseAccount.setFromObjectCode(ba.getFromObjectCode());
                        baseAccount.setFromSubAccount(ba.getFromSubAccount());
                        baseAccount.setActive(ba.getActive());
                        baseAccount.setLastUpdate(new Timestamp(new java.util.Date().getTime()));
                        
                        // Add Base Account
                        newType.getBaseAccounts().add(baseAccount);                    
                    }
                    
                    List<PRJEBaseObject> baseObjects = t.getBaseObjects();
                    
                    for(PRJEBaseObject bo : baseObjects) {
                        PRJEBaseObject baseObject = new PRJEBaseObject();
                        
                        baseObject.setInclude(bo.getInclude());
                        baseObject.setObjectCodeRangeName(bo.getObjectCodeRangeName());
                        baseObject.setBaseChartCode(bo.getBaseChartCode());
                        baseObject.setBaseObjectCodeHigh(bo.getBaseObjectCodeHigh());
                        baseObject.setBaseObjectCodeLow(bo.getBaseObjectCodeLow());
                        baseObject.setSubObjectCodeHigh(bo.getSubObjectCodeHigh());
                        baseObject.setSubObjectCodeLow(bo.getSubObjectCodeLow());
                        baseObject.setActive(bo.getActive());
                        baseObject.setLastUpdate(new Timestamp(new java.util.Date().getTime()));
                        
                        // Add Base Object
                        newType.getBaseObjects().add(baseObject);                    
                    }
                    
                    // Add New Type
                    newSet.getTypes().add(newType);
                }
            }
            else {
                LOG.info("Ignoring Set  : " + s.getSetName());
            }
            
            // Save new set
            getBusinessObjectService().save(newSet);
        }
        
        LOG.info("Ending PRJEFiscalYearImpl Process");
        
        return true;
    }
    
    /**
     * Purge PRJE Sets
     * 
     * @param nextFiscalYear
     */
    private int purgePRJESets(Integer nextFiscalYear) {
        Collection<PRJESet> sets = getPRJESets(nextFiscalYear,false);
        
        for(PRJESet s : sets) {
            getBusinessObjectService().delete(s);
        }
        
        return sets.size();
    }
    
    /**
     * Get PRJE Sets for Source Fiscal Year
     * 
     * @param sourceFiscalYear
     * 
     * @return Collection
     */
    private Collection<PRJESet> getPRJESets(Integer sourceFiscalYear, boolean isActive) {
        Map<String,Object> fieldValues = new HashMap<String,Object>();
        
        fieldValues.put(FISCAL_YEAR, sourceFiscalYear);
        
        if( isActive) {
            fieldValues.put(ACTIVE, Boolean.TRUE);
        }
        
        Collection<PRJESet> sets = getBusinessObjectService().findMatching(PRJESet.class, fieldValues);
        return sets;
    }
    
    /**
     * Get PRJE Set for Next Fiscal Year
     * 
     * @param sourceFiscalYear
     * @param setName
     * 
     * @return PRJESet
     */
    private PRJESet getActivePRJESet(Integer sourceFiscalYear, String setName) {
        Map<String,Object> fieldValues = new HashMap<String,Object>();
        
        fieldValues.put(FISCAL_YEAR, sourceFiscalYear);
        fieldValues.put(SET_NAME, setName);
        fieldValues.put(ACTIVE, Boolean.TRUE);
        
        Collection<PRJESet> sets = getBusinessObjectService().findMatching(PRJESet.class, fieldValues);
        
        if( sets.size() > 0 )
            return sets.iterator().next();
        else
            return null;
    }
    
    /**
     * 
     * This method removes inactive types from the list
     * @param types
     * @return
     */
    private List<PRJEType> removeInactiveTypes(List<PRJEType> types){
        LOG.info("Removing inactive types from the list of types.");
        for(int i=0;i<types.size();i++){
            LOG.info("Type Id: " + types.get(i).getTypeId());
            LOG.info("Type Name: " + types.get(i).getEntryName());
            LOG.info("Type is active: " + types.get(i).getActive());
            if(types.get(i).getActive() != true){
                LOG.info("Removing type ID " + types.get(i).getTypeId() + " from the list of active types.");
                types.remove(i);
            }
        }
        LOG.info("Finished removing inactive voucher types from the list of types.  There are " + types.size() + " types to copy.");
        return types;
    }
}
