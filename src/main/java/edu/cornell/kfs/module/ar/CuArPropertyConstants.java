package edu.cornell.kfs.module.ar;

import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

public class CuArPropertyConstants {
    
    public static class ContractsAndGrantsBillingAwardFields {
        public static final String FINAL_BILL = "finalBill";
        public static final String PARTIAL_BILL = "partialBill";
        public static final String TOTAL_PROGRAM_OUTLAYS_TO_DATE = "totalProgramOutlaysToDate";
    }

    public static class ContractsGrantsInvoiceAccountDetailFields {
        public static final String CHART_OF_ACCOUNTS_CODE = ArPropertyConstants.ACCOUNT_DETAILS
                + KFSConstants.DELIMITER + KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE;
        public static final String ACCOUNT_NUMBER = ArPropertyConstants.ACCOUNT_DETAILS
                + KFSConstants.DELIMITER + KFSPropertyConstants.ACCOUNT_NUMBER;
        public static final String CONTRACT_CONTROL_CHART_OF_ACCOUNTS_CODE = ArPropertyConstants.ACCOUNT_DETAILS
                + KFSConstants.DELIMITER + KFSPropertyConstants.ACCOUNT
                + KFSConstants.DELIMITER + KFSPropertyConstants.CONTRACT_CONTROL_CHART_OF_ACCOUNTS_CODE;
        public static final String CONTRACT_CONTROL_ACCOUNT_NUMBER = ArPropertyConstants.ACCOUNT_DETAILS
                + KFSConstants.DELIMITER + KFSPropertyConstants.ACCOUNT
                + KFSConstants.DELIMITER + KFSPropertyConstants.CONTRACT_CONTROL_ACCOUNT_NUMBER;
    }

}
