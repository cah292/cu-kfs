package edu.cornell.kfs.pdp.document;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.document.PayeeACHAccountMaintainableImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.actiontaken.ActionTakenValue;
import org.kuali.rice.kew.actiontaken.service.ActionTakenService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.identity.entity.EntityDefault;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;

import edu.cornell.kfs.pdp.CUPdpConstants;

public class CuPayeeACHAccountMaintainableImpl extends PayeeACHAccountMaintainableImpl {

    @Override
    public void refresh(String refreshCaller, Map fieldValues, MaintenanceDocument document) {
        
        super.refresh(refreshCaller, fieldValues, document);
        
        if (StringUtils.isNotEmpty(refreshCaller) && refreshCaller.equalsIgnoreCase("achPayeeLookupable")) {
            PayeeACHAccount payeeAchAccount = (PayeeACHAccount) document.getNewMaintainableObject().getBusinessObject();
            String payeeIdNumber = payeeAchAccount.getPayeeIdNumber();
            String payeeIdentifierTypeCode = payeeAchAccount.getPayeeIdentifierTypeCode();
            
            if(StringUtils.isNotEmpty(payeeAchAccount.getPayeeIdNumber())){
              // for Employee, retrieve from Person table by employee ID
              if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.EMPLOYEE)) {
                  Person person = SpringContext.getBean(PersonService.class).getPersonByEmployeeId(payeeIdNumber);
                  if (ObjectUtils.isNotNull(person)) {
                      payeeAchAccount.setPayeeEmailAddress( person.getEmailAddress());
                  }
              }
              // for Entity, retrieve from Entity table by entity ID then from Person table
              else if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.ENTITY)) {
                  if (ObjectUtils.isNotNull(payeeIdNumber)) {
                      EntityDefault entity = KimApiServiceLocator.getIdentityService().getEntityDefault(payeeIdNumber);
                      if (ObjectUtils.isNotNull(entity)) {
                          List<Principal> principals = entity.getPrincipals();
                          if (principals.size() > 0 && ObjectUtils.isNotNull(principals.get(0))) {
                              String principalId = principals.get(0).getPrincipalId();
                              Person person = SpringContext.getBean(PersonService.class).getPerson(principalId);
                              if (ObjectUtils.isNotNull(person)) {
                                  payeeAchAccount.setPayeeEmailAddress( person.getEmailAddress());
                              }
                          }
                      }
                  }
              }
            }
        }
    }

    @Override
    public boolean answerSplitNodeQuestion(String nodeName) {
        if (StringUtils.equals(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_REQUIRES_PDP_APPROVAL_NODE, nodeName)) {
            return payeeACHAccountMaintenanceRequiresPdpApproval();
        }
        
        return super.answerSplitNodeQuestion(nodeName);
    }

    protected boolean payeeACHAccountMaintenanceRequiresPdpApproval() {
        return StringUtils.equals(KFSConstants.MAINTENANCE_EDIT_ACTION, getMaintenanceAction())
                && payeeACHAccountMaintenanceWasGeneratedByExtractBatchJob();
    }

    protected boolean payeeACHAccountMaintenanceWasGeneratedByExtractBatchJob() {
        Person kfsSystemUser = getPersonService().getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        List<ActionTakenValue> actionsTaken = getActionTakenService().findByDocumentIdPrincipalId(
                getDocumentNumber(), kfsSystemUser.getPrincipalId());
        return actionsTaken.stream()
                .anyMatch(this::isDocumentSubmitActionFromPayeeACHAccountExtractBatchJob);
    }

    protected boolean isDocumentSubmitActionFromPayeeACHAccountExtractBatchJob(ActionTakenValue actionTaken) {
        return StringUtils.equals(KewApiConstants.ACTION_TAKEN_COMPLETED_CD, actionTaken.getActionTaken())
                && StringUtils.equals(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_ROUTE_ANNOTATION, actionTaken.getAnnotation());
    }

    protected ActionTakenService getActionTakenService() {
        return KEWServiceLocator.getActionTakenService();
    }

}
