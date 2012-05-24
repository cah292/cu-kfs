/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.kfs.fp.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPaymentReasonService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kim.util.KIMPropertyConstants;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.exception.ValidationException;
import org.kuali.rice.kns.lookup.CollectionIncomplete;
import org.kuali.rice.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.rice.kns.lookup.Lookupable;
import org.kuali.rice.kns.util.BeanPropertyComparator;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSPropertyConstants;
import org.kuali.rice.kns.util.MessageList;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.web.struts.form.LookupForm;
import org.kuali.rice.kns.web.ui.ResultRow;

public class DisbursementPayeeLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementPayeeLookupableHelperServiceImpl.class);

    private Lookupable vendorLookupable;
    private DisbursementVoucherPaymentReasonService disbursementVoucherPaymentReasonService;
    
    private static final int NAME_REQUIRED_FILLED_WITH_WILDCARD = 4;

    /**
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#performLookup(org.kuali.rice.kns.web.struts.form.LookupForm,
     *      java.util.Collection, boolean)
     */
    @Override
    public Collection performLookup(LookupForm lookupForm, Collection resultTable, boolean bounded) {
        Map<String, String> fieldValues = lookupForm.getFieldsForLookup();
        String paymentReasonCode = fieldValues.get(KFSPropertyConstants.PAYMENT_REASON_CODE);

        List<DisbursementPayee> displayList = (List<DisbursementPayee>) super.performLookup(lookupForm, resultTable, bounded);

        this.filterReturnUrl((List<ResultRow>) resultTable, displayList, paymentReasonCode);

        MessageList messageList = GlobalVariables.getMessageList();
        disbursementVoucherPaymentReasonService.postPaymentReasonCodeUsage(paymentReasonCode, messageList);

        return displayList;
    }

    /**
     * @see org.kuali.rice.kns.lookup.KualiLookupableHelperServiceImpl#getSearchResults(java.util.Map)
     */
    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        List<DisbursementPayee> searchResults = new ArrayList<DisbursementPayee>();

        if (StringUtils.isNotBlank(fieldValues.get(KFSPropertyConstants.VENDOR_NUMBER)) || StringUtils.isNotBlank(fieldValues.get(KFSPropertyConstants.VENDOR_NAME))) {
            searchResults.addAll(this.getVendorsAsPayees(fieldValues));
        }
        else if (StringUtils.isNotBlank(fieldValues.get(KIMPropertyConstants.Person.EMPLOYEE_ID))) {
            searchResults.addAll(this.getPersonAsPayees(fieldValues));
        }
        else {
            searchResults.addAll(this.getVendorsAsPayees(fieldValues));
            searchResults.addAll(this.getPersonAsPayees(fieldValues));
        }

        CollectionIncomplete results = new CollectionIncomplete(searchResults, Long.valueOf(searchResults.size()));

        // sort list if default sort column given
        List<String> defaultSortColumns = getDefaultSortColumns();
        if (defaultSortColumns.size() > 0) {
            Collections.sort(results, new BeanPropertyComparator(getDefaultSortColumns(), true));
        }

        return results;
    }

    /**
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#validateSearchParameters(java.util.Map)
     */
    @Override
    public void validateSearchParameters(Map fieldValues) {
        super.validateSearchParameters(fieldValues);
        
        final String vendorName = (String) fieldValues.get(KFSPropertyConstants.VENDOR_NAME);
        final String vendorNumber = (String) fieldValues.get(KFSPropertyConstants.VENDOR_NUMBER);
        final String employeeId = (String) fieldValues.get(KIMPropertyConstants.Person.EMPLOYEE_ID);
        final String firstName = (String)fieldValues.get(KIMPropertyConstants.Person.FIRST_NAME);
        final String lastName = (String)fieldValues.get(KIMPropertyConstants.Person.LAST_NAME);

        if (StringUtils.isBlank(vendorNumber) && StringUtils.isBlank(employeeId) && StringUtils.isBlank(firstName) && StringUtils.isBlank(lastName) && StringUtils.isBlank(vendorName)) {
            final String vendorNumberLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NUMBER);
            final String employeeIdLabel = this.getAttributeLabel(KIMPropertyConstants.Person.EMPLOYEE_ID);
            final String vendorNameLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NAME);
            final String firstNameLabel = this.getAttributeLabel(KIMPropertyConstants.Person.FIRST_NAME);
            final String lastNameLabel = this.getAttributeLabel(KIMPropertyConstants.Person.LAST_NAME);
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.VENDOR_NUMBER, KFSKeyConstants.ERROR_DV_LOOKUP_NEEDS_SOME_FIELD, new String[] {vendorNumberLabel, employeeIdLabel, vendorNameLabel, firstNameLabel, lastNameLabel});
        } else {
            final boolean isVendorInfoEntered = StringUtils.isNotBlank(vendorName) || StringUtils.isNotBlank(vendorNumber);
            if (isVendorInfoEntered && StringUtils.isNotBlank(employeeId)) {
                // only can use the vendor name and vendor number fields or the employee id field, but not both.
                String messageKey = KFSKeyConstants.ERROR_DV_VENDOR_EMPLOYEE_CONFUSION;
    
                String vendorNameLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NAME);
                String vendorNumberLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NUMBER);
                String employeeIdLabel = this.getAttributeLabel(KIMPropertyConstants.Person.EMPLOYEE_ID);
    
                GlobalVariables.getMessageMap().putError(KIMPropertyConstants.Person.EMPLOYEE_ID, messageKey, employeeIdLabel, vendorNameLabel, vendorNumberLabel);
            }
            if (StringUtils.isBlank(vendorNumber) && !StringUtils.isBlank(vendorName) && !filledEnough(vendorName)) {
                final String vendorNameLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NAME);
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.VENDOR_NAME, KFSKeyConstants.ERROR_DV_NAME_NOT_FILLED_ENOUGH, new String[] {vendorNameLabel, Integer.toString(getNameLengthWithWildcardRequirement())});
            }
    
            final boolean isPersonNameEntered = StringUtils.isNotBlank(firstName) || StringUtils.isNotBlank(lastName);
            if (isPersonNameEntered && StringUtils.isNotBlank(vendorName)) {
                // only can use the person first and last name fields or the vendor name field, but not both.
                String messageKey = KFSKeyConstants.ERROR_DV_VENDOR_NAME_PERSON_NAME_CONFUSION;
    
                String vendorNameLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NAME);
                String firstNameLabel = this.getAttributeLabel(KIMPropertyConstants.Person.FIRST_NAME);
                String lastNameLabel = this.getAttributeLabel(KIMPropertyConstants.Person.LAST_NAME);
    
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.VENDOR_NAME, messageKey, vendorNameLabel, firstNameLabel, lastNameLabel);
            }
            if (StringUtils.isBlank(employeeId)) {
                if (StringUtils.isBlank(firstName) && !StringUtils.isBlank(lastName) && !filledEnough(lastName)) {
                    final String label = getAttributeLabel(KIMPropertyConstants.Person.LAST_NAME);
                    GlobalVariables.getMessageMap().putError(KIMPropertyConstants.Person.LAST_NAME, KFSKeyConstants.ERROR_DV_NAME_NOT_FILLED_ENOUGH, new String[] { label, Integer.toString(getNameLengthWithWildcardRequirement() ) } );
                } else if (StringUtils.isBlank(lastName) && !StringUtils.isBlank(firstName) && !filledEnough(firstName)) {
                    final String label = getAttributeLabel(KIMPropertyConstants.Person.FIRST_NAME);
                    GlobalVariables.getMessageMap().putError(KIMPropertyConstants.Person.FIRST_NAME, KFSKeyConstants.ERROR_DV_NAME_NOT_FILLED_ENOUGH, new String[] { label, Integer.toString(getNameLengthWithWildcardRequirement() ) } );
                }
            }
        }
        
        if (GlobalVariables.getMessageMap().hasErrors()) {
            throw new ValidationException("errors in search criteria");
        }
    }
    
    /**
     * Determines if a String is "filled enough" - ie, is not null, has a length greater than zero and if a wildcard is present, has a length greater than 4 (3 characters, plus a wildcard)
     * @param s the String to test
     * @return true if the given String is "filled" by the definition above, false otherwise
     */
    protected boolean filledEnough(String s) {
        final boolean containsWildcard = containsLookupWildcard(s);
        return s != null && s.length() > 0 && ((containsWildcard && s.length() >= getNameLengthWithWildcardRequirement()) || !containsWildcard);
    }
    
    /**
     * @return the number of characters a name field must be filled in for the search to be valid
     */
    protected int getNameLengthWithWildcardRequirement() {
        return DisbursementPayeeLookupableHelperServiceImpl.NAME_REQUIRED_FILLED_WITH_WILDCARD;
    }
    
    /**
     * Determines if the given String contains a lookup wildcard
     * @param s the String to test
     * @return true if a lookup wildcard is in the String, false otherwise
     */
    protected boolean containsLookupWildcard(String s) {
        return s != null && (s.indexOf('*') > -1 || s.indexOf('%') > -1); 
    }

    // get the label for the given attribute of the current business object
    protected String getAttributeLabel(String attributeName) {
        return this.getDataDictionaryService().getAttributeLabel(getBusinessObjectClass(), attributeName);
    }

    // perform vendor search
    protected List<DisbursementPayee> getVendorsAsPayees(Map<String, String> fieldValues) {
        List<DisbursementPayee> payeeList = new ArrayList<DisbursementPayee>();

        Map<String, String> fieldsForLookup = this.getVendorFieldValues(fieldValues);
        vendorLookupable.setBusinessObjectClass(VendorDetail.class);
        vendorLookupable.validateSearchParameters(fieldsForLookup);

        List<BusinessObject> vendorList = vendorLookupable.getSearchResults(fieldsForLookup);
        for (BusinessObject vendor : vendorList) {
            VendorDetail vendorDetail = (VendorDetail) vendor;
            DisbursementPayee payee = getPayeeFromVendor(vendorDetail, fieldValues);
            payeeList.add(payee);
        }
        
        return payeeList;
    }

    protected DisbursementPayee getPayeeFromVendor(VendorDetail vendorDetail, Map<String, String> fieldValues) {
        DisbursementPayee payee = DisbursementPayee.getPayeeFromVendor(vendorDetail);
        payee.setPaymentReasonCode(fieldValues.get(KFSPropertyConstants.PAYMENT_REASON_CODE));
        
        return payee;
    }

    // get the search criteria valid for vendor lookup
    private Map<String, String> getVendorFieldValues(Map<String, String> fieldValues) {
        Map<String, String> vendorFieldValues = new HashMap<String, String>();
        vendorFieldValues.putAll(fieldValues);

        Map<String, String> fieldConversionMap = DisbursementPayee.getFieldConversionBetweenPayeeAndVendor();
        this.replaceFieldKeys(vendorFieldValues, fieldConversionMap);

        String vendorName = this.getVendorName(vendorFieldValues);
        if (StringUtils.isNotBlank(vendorName)) {
            vendorFieldValues.put(KFSPropertyConstants.VENDOR_NAME, vendorName);
        }

        vendorFieldValues.remove(VendorPropertyConstants.VENDOR_FIRST_NAME);
        vendorFieldValues.remove(VendorPropertyConstants.VENDOR_LAST_NAME);

        return vendorFieldValues;
    }

    // get the vendor name from the given field value map
    private String getVendorName(Map<String, String> vendorFieldValues) {
        String firstName = vendorFieldValues.get(VendorPropertyConstants.VENDOR_FIRST_NAME);
        String lastName = vendorFieldValues.get(VendorPropertyConstants.VENDOR_LAST_NAME);

        if (StringUtils.isNotBlank(lastName)) {
            return lastName + VendorConstants.NAME_DELIM + firstName;
        }
        else if (StringUtils.isNotBlank(firstName)) {
            return KFSConstants.WILDCARD_CHARACTER + VendorConstants.NAME_DELIM + firstName;
        }

        return StringUtils.EMPTY;
    }

    // perform person search
    protected List<DisbursementPayee> getPersonAsPayees(Map<String, String> fieldValues) {
        List<DisbursementPayee> payeeList = new ArrayList<DisbursementPayee>();
        
        Map<String, String> fieldsForLookup = this.getPersonFieldValues(fieldValues); 
        
        List<? extends Person> persons = KIMServiceLocator.getPersonService().findPeople(fieldsForLookup);   
        
        for (Person personDetail : persons) {   
            if (personDetail.hasAffiliationOfType("STDNT") || personDetail.hasAffiliationOfType("ALUMNI") ) {
            	//|| personDetail.hasAffiliationOfType("FCLTY") || personDetail.hasAffiliationOfType("STAFF")) {
        		//if (!ObjectUtils.isNotNull(personDetail.getEmployeeStatusCode()) && personDetail.getEmployeeStatusCode().equals("A")) {
        			DisbursementPayee payee = getPayeeFromPerson(personDetail, fieldValues);
        			payeeList.add(payee);
        		//}
            } else if (personDetail.hasAffiliationOfType("FACULTY") || personDetail.hasAffiliationOfType("STAFF")) {
            	if (ObjectUtils.isNotNull(personDetail.getEmployeeStatusCode()) && personDetail.getEmployeeStatusCode().equals("A")) {
        			DisbursementPayee payee = getPayeeFromPerson(personDetail, fieldValues);
        			payeeList.add(payee);
        		}
        	}
        }
        
        return payeeList;
    }

    protected DisbursementPayee getPayeeFromPerson(Person personDetail, Map<String, String> fieldValues) {
        DisbursementPayee payee = DisbursementPayee.getPayeeFromPerson(personDetail);
        payee.setPaymentReasonCode(fieldValues.get(KFSPropertyConstants.PAYMENT_REASON_CODE));
        
        return payee;
    }

    // get the search criteria valid for person lookup
    private Map<String, String> getPersonFieldValues(Map<String, String> fieldValues) {
        Map<String, String> personFieldValues = new HashMap<String, String>();
        personFieldValues.putAll(fieldValues);

        Map<String, String> fieldConversionMap = DisbursementPayee.getFieldConversionBetweenPayeeAndPerson();
        this.replaceFieldKeys(personFieldValues, fieldConversionMap);

        if (StringUtils.isNotBlank(personFieldValues.get(KIMPropertyConstants.Person.EXTERNAL_ID))) {
            personFieldValues.put(KIMPropertyConstants.Person.EXTERNAL_IDENTIFIER_TYPE_CODE, VendorConstants.TAX_TYPE_TAX);
        }

        return personFieldValues;
    }

    // replace the keys in fieldValues with the corresponding values defined in fieldConversionMap
    private void replaceFieldKeys(Map<String, String> fieldValues, Map<String, String> fieldConversionMap) {
        for (String key : fieldConversionMap.keySet()) {
            if (fieldValues.containsKey(key)) {
                String value = fieldValues.get(key);
                String newKey = fieldConversionMap.get(key);

                fieldValues.remove(key);
                fieldValues.put(newKey, value);
            }
        }
    }

    // remove its return URLs if a row is not qualified for returning
    protected void filterReturnUrl(List<ResultRow> resultRowList, List<DisbursementPayee> payeeList, String paymentReasonCode) {
        List<String> payeeTypeCodes = disbursementVoucherPaymentReasonService.getPayeeTypesByPaymentReason(paymentReasonCode);
        if (payeeTypeCodes == null || payeeTypeCodes.isEmpty()) {
            return;
        }

        for (int index = 0; index < payeeList.size(); index++) {
            DisbursementPayee payee = payeeList.get(index);

            boolean isQualified = disbursementVoucherPaymentReasonService.isPayeeQualifiedForPayment(payee, paymentReasonCode, payeeTypeCodes);
            if (!isQualified) {
                resultRowList.get(index).setReturnUrl(StringUtils.EMPTY);
            }
        }
    }

    /**
     * Sets the vendorLookupable attribute value.
     * 
     * @param vendorLookupable The vendorLookupable to set.
     */
    public void setVendorLookupable(Lookupable vendorLookupable) {
        this.vendorLookupable = vendorLookupable;
    }

    /**
     * Sets the disbursementVoucherPaymentReasonService attribute value.
     * 
     * @param disbursementVoucherPaymentReasonService The disbursementVoucherPaymentReasonService to set.
     */
    public void setDisbursementVoucherPaymentReasonService(DisbursementVoucherPaymentReasonService disbursementVoucherPaymentReasonService) {
        this.disbursementVoucherPaymentReasonService = disbursementVoucherPaymentReasonService;
    }
}
