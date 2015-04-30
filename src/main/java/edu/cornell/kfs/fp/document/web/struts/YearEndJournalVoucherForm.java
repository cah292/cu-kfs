package edu.cornell.kfs.fp.document.web.struts;

import java.sql.Date;
import java.util.ArrayList;

import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.BalanceType;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.fp.document.web.struts.JournalVoucherForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.datetime.DateTimeService;

public class YearEndJournalVoucherForm extends JournalVoucherForm {
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(YearEndJournalVoucherForm.class);

	/**
	 * Constructs a YearEndJournalVoucherForm instance and sets up the
	 * appropriately casted document.
	 */
	public YearEndJournalVoucherForm() {
		super();
	}

	@Override
	protected String getDefaultDocumentTypeName() {
		return "YEJV";
	}
	
	@Override
	protected void populateBalanceTypeListForRendering() {
		balanceTypes = new ArrayList<BalanceType>();
        BalanceType balanceTypeAC = SpringContext.getBean(BalanceTypeService.class).getBalanceTypeByCode(KFSConstants.BALANCE_TYPE_ACTUAL);
        balanceTypes.add(balanceTypeAC);

        this.setBalanceTypes(balanceTypes);

        String selectedBalanceTypeCode = KFSConstants.BALANCE_TYPE_ACTUAL;

        setSelectedBalanceType(getPopulatedBalanceTypeInstance(selectedBalanceTypeCode));
        getJournalVoucherDocument().setBalanceTypeCode(selectedBalanceTypeCode);
	}
	
	@Override
	public void populateDefaultSelectedAccountingPeriod() {
        int currentUniversityFiscalYear  = SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
        AccountingPeriod accountingPeriod = SpringContext.getBean(AccountingPeriodService.class).getByPeriod("13", currentUniversityFiscalYear);

        StringBuffer sb = new StringBuffer();
        sb.append(accountingPeriod.getUniversityFiscalPeriodCode());
        sb.append(accountingPeriod.getUniversityFiscalYear());

        setSelectedAccountingPeriod(sb.toString());
	}

}
