package edu.cornell.kfs.concur.service;

import java.text.ParseException;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.rest.xmlObjects.ConcurEventNotificationDTO;

public interface ConcurEventNotificationConversionService {
    
    ConcurEventNotification convertConcurEventNotification(ConcurEventNotificationDTO concurEventNotificationDTO) throws ParseException ;

}
