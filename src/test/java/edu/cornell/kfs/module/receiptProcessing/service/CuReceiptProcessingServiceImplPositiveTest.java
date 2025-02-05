package edu.cornell.kfs.module.receiptProcessing.service;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import edu.cornell.kfs.module.receiptProcessing.service.ReceiptProcessingService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.rice.core.api.config.property.ConfigurationService;


@ConfigureContext(session = ccs1)
public class CuReceiptProcessingServiceImplPositiveTest extends KualiTestBase {

    private ReceiptProcessingService receiptProcessingService;
    private ConfigurationService  kualiConfigurationService;
    
    private static final Logger LOG = LogManager.getLogger(CuReceiptProcessingServiceImplPositiveTest.class);
    private static final String DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/receiptProcessing/service/fixture/receiptProcessing_test.csv";
    private static final String IMG_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/receiptProcessing/service/attachments/testUnit.pdf";
    private String batchDirectory;  
    private String receiptDir = "/infra/receipt_processing/CIT-csv-archive/";
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        receiptProcessingService = SpringContext.getBean(ReceiptProcessingService.class);
        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        batchDirectory = kualiConfigurationService.getPropertyValueAsString(com.rsmart.kuali.kfs.sys.KFSConstants.STAGING_DIRECTORY_KEY) + "/fp/receiptProcessing";
        
        //make sure we have a batch directory
        //batchDirectory = SpringContext.getBean(ReceiptProcessingService.class).getDirectoryPath();
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.mkdir();

        //copy the data file into place
        File dataFileSrc = new File(DATA_FILE_PATH);
        File dataFileDest = new File(batchDirectory + "/receiptProcessing_test.csv");
        FileUtils.copyFile(dataFileSrc, dataFileDest);

        //create .done file
        String doneFileName = batchDirectory + "/receiptProcessing_test.done";
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }
        
        //make sure we have a pdf directory
        //pdfDirectory = SpringContext.getBean(ReceiptProcessingService.class).getPdfPath();
        File pdfDirectoryFile = new File(receiptDir);
        pdfDirectoryFile.mkdir();

        //copy the data file into place
        File imgFileSrc = new File(IMG_FILE_PATH);
        File imgFileDest = new File(receiptDir + "/testUnit.pdf");
        FileUtils.copyFile(imgFileSrc, imgFileDest);

        
        
    }
    
    public void testCanLoadFiles() {
        assertTrue(receiptProcessingService.loadFiles());       
    }
    
}
