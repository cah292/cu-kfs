package edu.cornell.kfs.sys.businessobject.lookup;

import edu.cornell.kfs.sys.batch.service.CreateDoneBatchFileAuthorizationService;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CreateDoneBatchFileLookupableHelperServiceImpl extends CuBatchFileLookupableHelperServiceImpl {
	
	protected CreateDoneBatchFileAuthorizationService createDoneAuthorizationService; 

	@Override
    public List<HtmlData> getCustomActionUrls(BusinessObject businessObject, List pkNames) {
        List<HtmlData> links = new ArrayList<HtmlData>();
        
        BatchFile batchFile = (BatchFile) businessObject;
        if (canCreateDoneFile(batchFile)) {
            links.add(getCreateDoneUrl(batchFile));
        }
        return links;
    }

    protected boolean canCreateDoneFile(BatchFile batchFile) {
    	boolean isDoneFile = StringUtils.endsWith(batchFile.getFileName(), ".done");
    	return (!isDoneFile && createDoneAuthorizationService.canCreateDoneFile(batchFile, GlobalVariables.getUserSession().getPerson()));
    }
    
    protected HtmlData getCreateDoneUrl(BatchFile batchFile) {
        Properties parameters = new Properties();
        parameters.put("filePath", BatchFileUtils.pathRelativeToRootDirectory(batchFile.retrieveFile().getAbsolutePath()));
        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, "createDone");
        String href = UrlFactory.parameterizeUrl("../createDoneBatchFileAdmin.do", parameters);
        return new AnchorHtmlData(href, "createDone", "Create Done");
    }
    
    public CreateDoneBatchFileAuthorizationService getCreateDoneAuthorizationService() {
		return createDoneAuthorizationService;
	}

	public void setCreateDoneAuthorizationService(
			CreateDoneBatchFileAuthorizationService createDoneAuthorizationService) {
		this.createDoneAuthorizationService = createDoneAuthorizationService;
	}

}
