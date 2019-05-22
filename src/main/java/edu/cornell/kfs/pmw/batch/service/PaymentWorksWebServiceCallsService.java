package edu.cornell.kfs.pmw.batch.service;

import java.util.List;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.sys.util.ByteContent;

public interface PaymentWorksWebServiceCallsService {
    
    List<String> obtainPmwIdentifiersForApprovedNewVendorRequests();
    
    PaymentWorksVendor obtainPmwNewVendorRequestDetailForPmwIdentifier(String pmwNewVendorRequestId, PaymentWorksNewVendorRequestsBatchReportData reportData);
    
    void sendProcessedStatusToPaymentWorksForNewVendor(String processedVendorId);
    
    void refreshPaymentWorksAuthorizationToken();
    
    int uploadVendorsToPaymentWorks(ByteContent vendorCsvData);
}
