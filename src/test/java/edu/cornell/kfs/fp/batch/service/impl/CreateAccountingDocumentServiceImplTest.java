package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.coa.service.impl.AccountingPeriodServiceImpl;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense;
import org.kuali.kfs.fp.businessobject.FiscalYearFunctionControl;
import org.kuali.kfs.fp.businessobject.InternalBillingItem;
import org.kuali.kfs.fp.document.AuxiliaryVoucherDocument;
import org.kuali.kfs.fp.document.InternalBillingDocument;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTravelService;
import org.kuali.kfs.fp.service.FiscalYearFunctionControlService;
import org.kuali.kfs.fp.service.impl.FiscalYearFunctionControlServiceImpl;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.service.impl.BatchInputFileServiceImpl;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.resourceloader.ResourceLoaderException;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.impl.parameter.ParameterEvaluatorServiceImpl;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.AccountingXmlDocumentDownloadAttachmentService;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentReportService;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentValidationService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentClassMappingUtils;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingPeriodFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentEntryFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.CuDistributionOfIncomeAndExpenseDocument;
import edu.cornell.kfs.sys.batch.JAXBXmlBatchInputFileTypeBase;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import edu.cornell.kfs.sys.businessobject.fixture.WebServiceCredentialFixture;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.MockDocumentUtils;
import edu.cornell.kfs.sys.util.MockDocumentUtils.TestAdHocRoutePerson;
import edu.cornell.kfs.sys.util.MockDocumentUtils.TestNote;
import edu.cornell.kfs.sys.util.MockPersonUtil;
import edu.cornell.kfs.sys.util.fixture.TestUserFixture;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CuDistributionOfIncomeAndExpenseDocument.class, TestAdHocRoutePerson.class, TestNote.class})
public class CreateAccountingDocumentServiceImplTest {

    private static final String SOURCE_TEST_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml";
    private static final String TARGET_TEST_FILE_PATH = "test/fp/accountingXmlDocument";
    private static final String FULL_FILE_PATH_FORMAT = "%s/%s%s";
    private static final int DOCUMENT_NUMBER_START = 1000;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HH-mm-ss-S");

    private TestCreateAccountingDocumentServiceImpl createAccountingDocumentService;
    private List<AccountingDocument> routedAccountingDocuments;
    private List<String> creationOrderedBaseFileNames;

    @Before
    public void setUp() throws Exception {
        ConfigurationService configurationService = buildMockConfigurationService();
        DateTimeService dateTimeService = buildMockDateTimeService();
        ParameterService parameterService = buildParameterService();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(
                buildMockPersonService(), buildAccountingXmlDocumentDownloadAttachmentService(),
                configurationService, buildMockFiscalYearFunctionControlService(), buildMockDisbursementVoucherTravelService(), buildMockUniversityDateService(),
                buildAccountingPeriodService(), dateTimeService);
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(
                AccountingDocumentMapping.DI_DOCUMENT, AccountingDocumentMapping.IB_DOCUMENT, AccountingDocumentMapping.TF_DOCUMENT,
                AccountingDocumentMapping.BA_DOCUMENT, AccountingDocumentMapping.SB_DOCUMENT, AccountingDocumentMapping.YEDI_DOCUMENT,
                AccountingDocumentMapping.DV_DOCUMENT, AccountingDocumentMapping.YEBA_DOCUMENT, AccountingDocumentMapping.YETF_DOCUMENT,
                AccountingDocumentMapping.AV_DOCUMENT);
        createAccountingDocumentService.setAccountingDocumentBatchInputFileType(buildAccountingXmlDocumentInputFileType(dateTimeService));
        createAccountingDocumentService.setBatchInputFileService(new BatchInputFileServiceImpl());
        createAccountingDocumentService.setFileStorageService(buildFileStorageService());
        createAccountingDocumentService.setConfigurationService(configurationService);
        createAccountingDocumentService.setDocumentService(buildMockDocumentService());
        createAccountingDocumentService.setCreateAccountingDocumentReportService(new TestCreateAccountingDocumentReportService());
        createAccountingDocumentService.setParameterService(parameterService);
        createAccountingDocumentService.setCreateAccountingDocumentValidationService(new TestCreateAccountingDocumentValidationService());
        routedAccountingDocuments = new ArrayList<>();
        creationOrderedBaseFileNames = new ArrayList<>();
        createTargetTestDirectory();
    }

    @After
    public void tearDown() throws Exception {
        deleteTargetTestDirectory();
    }

    @Test
    public void testLoadSingleFileWithSingleDIDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-di-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithZeroDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("empty-document-list-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_LIST_TEST);
    }

    @Test
    public void testLoadMultipleFilesWithDIDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-di-document-test", "multi-di-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST,
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocumentsPlusDocumentWithInvalidDocType() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-plus-invalid-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_WITH_BAD_CONVERSION_SECOND_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocumentsPlusDocumentWithRulesFailure() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_WITH_BAD_RULES_FIRST_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocumentsPlusDocumentsWithBadAttachments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-plus-bad-attachments-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_WITH_BAD_ATTACHMENTS_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleIBDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ib-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_IB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleIBDocumentLackingItems() throws Exception {
        copyTestFilesAndCreateDoneFiles("ib-without-items-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_IB_DOCUMENT_NO_ITEMS_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleIBDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ib-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_IB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleIBDocumentsPlusDocumentWithRulesFailure() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ib-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_IB_DOCUMENT_WITH_BAD_RULES_THIRD_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDocumentTypes() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-doc-types-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DOCUMENT_TYPES_TEST);
    }

    @Test
    public void testLoadSingleFileWithDIDocumentContainingIBItemsToIgnore() throws Exception {
        copyTestFilesAndCreateDoneFiles("di-with-ib-items-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.DI_WITH_IB_ITEMS_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_DOCUMENT_TEST);
    }
    
    @Test
    public void testLoadSingleFileWithSingleYEBADocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-yeba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_YEBA_DOCUMENT_TEST);
    }
    
    @Test
    public void testLoadSingleFileWithMutliYEBADocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-yeba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MUTLI_YEBA_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentLackingBAAccountProperties() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-no-base-or-months-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_NO_BASEAMOUNT_OR_MONTHS_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentUsingNonZeroBaseAmounts() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-nonzero-base-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_NONZERO_BASEAMOUNT_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentUsingMultipleMonthAmounts() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-multi-months-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_MULTI_MONTHS_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleBADocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_BA_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleBADocumentsPlusDocumentsWithRulesFailures() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ba-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_BA_DOCUMENT_WITH_SOME_BAD_RULES_DOCUMENTS_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleSBDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-sb-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_SB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleSBDocumentLackingItems() throws Exception {
        copyTestFilesAndCreateDoneFiles("sb-without-items-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_SB_DOCUMENT_NO_ITEMS_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleSBDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-sb-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_SB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleSBDocumentsPlusDocumentWithRulesFailure() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-sb-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_SB_DOCUMENT_WITH_BAD_RULES_THIRD_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleYEDIDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-yedi-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.SINGLE_YEDI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleYEDIDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-yedi-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.MULTI_YEDI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadMultipleFilesWithYEDIDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-yedi-document-test", "multi-yedi-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.SINGLE_YEDI_DOCUMENT_TEST,
                AccountingXmlDocumentListWrapperFixture.MULTI_YEDI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleYEDIDocumentsPlusDocumentWithInvalidDocType() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-yedi-plus-invalid-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_YEDI_DOCUMENT_WITH_BAD_CONVERSION_SECOND_DOCUMENT_TEST);
    }
    
    @Test
    public void testLoadSingleFileWithSingleYETFDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-yetf-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.SINGLE_YETF_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleAVDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-av-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.SINGLE_AV_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleAVDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-av-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.MULTI_AV_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleAVDocumentsPlusDocumentsWithRulesFailures() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-av-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_AV_DOCUMENT_WITH_SOME_BAD_RULES_DOCUMENTS_TEST);
    }

    @Test
    public void testEmptyFile() throws Exception {
        copyTestFilesAndCreateDoneFiles("empty-file-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_TEST);
    }
    
    @Test
    public void testEmptyFileWithGoodFile() throws Exception {
        copyTestFilesAndCreateDoneFiles("empty-file-test", "multi-yedi-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_TEST,
                AccountingXmlDocumentListWrapperFixture.MULTI_YEDI_DOCUMENT_TEST);
    }
    
    @Test
    public void testBadXmlFile() throws Exception {
        copyTestFilesAndCreateDoneFiles("bad-xml-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.BAD_XML_DOCUMENT_TEST);
    }
    
    @Test
    public void testBadXmlEmptyFileWithGoodFile() throws Exception {
        copyTestFilesAndCreateDoneFiles("bad-xml-test", "empty-file-test", "multi-yedi-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.BAD_XML_DOCUMENT_TEST, 
                AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_TEST, AccountingXmlDocumentListWrapperFixture.MULTI_YEDI_DOCUMENT_TEST);
    }
    
    @Test
    public void testServiceError() throws Exception {
        createAccountingDocumentService.setFailToCreateDocument(true);

        copyTestFilesAndCreateDoneFiles("single-yedi-document-test");
        boolean results = createAccountingDocumentService.createAccountingDocumentsFromXml();
        assertFalse("When there is a problem calling services, the job should fail", results);
    }

    private void assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture... fixtures) {
        boolean actualResults = createAccountingDocumentService.createAccountingDocumentsFromXml();
        assertDocumentsWereCreatedAndRoutedCorrectly(actualResults, fixtures);
        assertDoneFilesWereDeleted();
    }

    private void assertDocumentsWereCreatedAndRoutedCorrectly(boolean actualResults, AccountingXmlDocumentListWrapperFixture... fixtures) {
        Map<String, AccountingXmlDocumentListWrapperFixture> fileNameToFixtureMap = buildFileNameToFixtureMap(fixtures);
        
        AccountingXmlDocumentEntryFixture[] processingOrderedDocumentFixtures = createAccountingDocumentService.getProcessingOrderedBaseFileNames()
                .stream()
                .map(fileNameToFixtureMap::get)
                .flatMap((fixture) -> fixture.documents.stream())
                .toArray(AccountingXmlDocumentEntryFixture[]::new);
        
        assertDocumentsWereCreatedAndRoutedCorrectly(processingOrderedDocumentFixtures);
        
        assertActualResultsAreExpected(actualResults, fileNameToFixtureMap);
    }
    
    private void assertActualResultsAreExpected(boolean actualResults, Map<String, AccountingXmlDocumentListWrapperFixture> fileNameToFixtureMap) {
        boolean expectedResult = true;
        for (AccountingXmlDocumentListWrapperFixture xmlFixture : fileNameToFixtureMap.values()) {
            expectedResult &= xmlFixture.expectedResults;
        }
        assertEquals("The expected result should equal the actual result", expectedResult, actualResults);
    }

    private Map<String, AccountingXmlDocumentListWrapperFixture> buildFileNameToFixtureMap(
            AccountingXmlDocumentListWrapperFixture... fixtures) {
        Map<String, AccountingXmlDocumentListWrapperFixture> fileNameToFixtureMap = new HashMap<>();
        for (int i = 0; i < fixtures.length; i++) {
            String baseFileName = creationOrderedBaseFileNames.get(i);
            fileNameToFixtureMap.put(baseFileName, fixtures[i]);
        }
        return fileNameToFixtureMap;
    }

    private void assertDocumentsWereCreatedAndRoutedCorrectly(AccountingXmlDocumentEntryFixture... fixtures) {
        List<AccountingXmlDocumentEntryFixture> expectedRoutableFixtures = new ArrayList<>(fixtures.length);
        List<AccountingDocument> expectedAccountingDocuments = new ArrayList<>(fixtures.length);
        buildExpectedDocumentsAndAppendToLists(expectedRoutableFixtures::add, expectedAccountingDocuments::add, fixtures);
        
        assertEquals("Wrong number of routed documents", expectedAccountingDocuments.size(), routedAccountingDocuments.size());
        
        for (int i = 0; i < expectedAccountingDocuments.size(); i++) {
            AccountingXmlDocumentEntryFixture fixture = expectedRoutableFixtures.get(i);
            Class<? extends AccountingDocument> expectedDocumentClass = AccountingDocumentClassMappingUtils
                    .getDocumentClassByDocumentType(fixture.documentTypeCode);
            assertAccountingDocumentIsCorrect(expectedDocumentClass, expectedAccountingDocuments.get(i), routedAccountingDocuments.get(i));
        }
    }

    private void buildExpectedDocumentsAndAppendToLists(Consumer<AccountingXmlDocumentEntryFixture> fixtureListAppender,
            Consumer<AccountingDocument> documentListAppender, AccountingXmlDocumentEntryFixture... fixtures) {
        MutableInt idCounter = new MutableInt(DOCUMENT_NUMBER_START);
        
        Stream.of(fixtures)
                .filter(this::isDocumentExpectedToReachInitiationPoint)
                .map((fixture) -> buildDocumentIdToFixtureMapping(idCounter, fixture))
                .filter((mapping) -> isDocumentExpectedToPassBusinessRulesValidation(mapping.getValue()))
                .peek((mapping) -> fixtureListAppender.accept(mapping.getValue()))
                .map(this::buildExpectedDocument)
                .forEach(documentListAppender);
    }

    private Map.Entry<String, AccountingXmlDocumentEntryFixture> buildDocumentIdToFixtureMapping(
            MutableInt idCounter, AccountingXmlDocumentEntryFixture fixture) {
        idCounter.increment();
        return new AbstractMap.SimpleImmutableEntry<>(idCounter.toString(), fixture);
    }

    private AccountingDocument buildExpectedDocument(Map.Entry<String, AccountingXmlDocumentEntryFixture> docIdToFixtureMapping) {
        AccountingXmlDocumentEntryFixture fixture = docIdToFixtureMapping.getValue();
        return fixture.toAccountingDocument(docIdToFixtureMapping.getKey());
    }

    @SuppressWarnings("unchecked")
    private void assertAccountingDocumentIsCorrect(
            Class<? extends AccountingDocument> documentClass, AccountingDocument expectedDocument, AccountingDocument actualDocument) {
        assertTrue("Document was not of the expected type of " + documentClass.getName(), documentClass.isAssignableFrom(actualDocument.getClass()));
        assertEquals("Wrong document number", expectedDocument.getDocumentNumber(), actualDocument.getDocumentNumber());
        
        assertHeaderIsCorrect((FinancialSystemDocumentHeader) expectedDocument.getDocumentHeader(),
                (FinancialSystemDocumentHeader) actualDocument.getDocumentHeader());
        assertObjectListIsCorrect("source accounting lines",
                expectedDocument.getSourceAccountingLines(), actualDocument.getSourceAccountingLines(), this::assertAccountingLineIsCorrect);
        assertObjectListIsCorrect("target accounting lines",
                expectedDocument.getTargetAccountingLines(), actualDocument.getTargetAccountingLines(), this::assertAccountingLineIsCorrect);
        assertObjectListIsCorrect("notes",
                expectedDocument.getNotes(), actualDocument.getNotes(), this::assertNoteIsCorrect);
        assertObjectListIsCorrect("ad hoc persons",
                expectedDocument.getAdHocRoutePersons(), actualDocument.getAdHocRoutePersons(), this::assertAdHocPersonIsCorrect);
        
        if (InternalBillingDocument.class.isAssignableFrom(documentClass)) {
            assertObjectListIsCorrect("items",
                    ((InternalBillingDocument) expectedDocument).getItems(), ((InternalBillingDocument) actualDocument).getItems(),
                    this::assertInternalBillingItemIsCorrect);
        } else if (AuxiliaryVoucherDocument.class.isAssignableFrom(documentClass)) {
            assertAuxiliaryVoucherDocumentIsCorrect((AuxiliaryVoucherDocument) expectedDocument, (AuxiliaryVoucherDocument) actualDocument);
        }
        
        if (actualDocument instanceof CuDisbursementVoucherDocument) {
            assertCuDisbursementVoucherDocumentsCorrect((CuDisbursementVoucherDocument) expectedDocument, (CuDisbursementVoucherDocument) actualDocument);
        }
    }

    private <T> void assertObjectListIsCorrect(String listLabel, List<? extends T> expectedObjects, List<? extends T> actualObjects,
            BiConsumer<? super T, ? super T> objectValidator) {
        assertEquals("Wrong number of " + listLabel, expectedObjects.size(), actualObjects.size());
        for (int i = 0; i < expectedObjects.size(); i++) {
            objectValidator.accept(expectedObjects.get(i), actualObjects.get(i));
        }
    }

    private void assertHeaderIsCorrect(FinancialSystemDocumentHeader expectedHeader, FinancialSystemDocumentHeader actualHeader) {
        assertEquals("Wrong document number", expectedHeader.getDocumentNumber(), actualHeader.getDocumentNumber());
        assertEquals("Wrong document description", expectedHeader.getDocumentDescription(), actualHeader.getDocumentDescription());
        assertEquals("Wrong document explanation", expectedHeader.getExplanation(), actualHeader.getExplanation());
        assertEquals("Wrong org document number", expectedHeader.getOrganizationDocumentNumber(), actualHeader.getOrganizationDocumentNumber());
    }

    private void assertAccountingLineIsCorrect(AccountingLine expectedLine, AccountingLine actualLine) {
        assertEquals("Wrong accounting line implementation class", expectedLine.getClass(), actualLine.getClass());
        assertEquals("Wrong document number", expectedLine.getDocumentNumber(), actualLine.getDocumentNumber());
        assertEquals("Wrong chart code", expectedLine.getChartOfAccountsCode(), actualLine.getChartOfAccountsCode());
        assertEquals("Wrong account number", expectedLine.getAccountNumber(), actualLine.getAccountNumber());
        assertEquals("Wrong sub-account number", expectedLine.getSubAccountNumber(), actualLine.getSubAccountNumber());
        assertEquals("Wrong object code", expectedLine.getFinancialObjectCode(), actualLine.getFinancialObjectCode());
        assertEquals("Wrong sub-object code", expectedLine.getFinancialSubObjectCode(), actualLine.getFinancialSubObjectCode());
        assertEquals("Wrong project code", expectedLine.getProjectCode(), actualLine.getProjectCode());
        assertEquals("Wrong org ref ID", expectedLine.getOrganizationReferenceId(), actualLine.getOrganizationReferenceId());
        assertEquals("Wrong line description", expectedLine.getFinancialDocumentLineDescription(), actualLine.getFinancialDocumentLineDescription());
        assertEquals("Wrong line amount", expectedLine.getAmount(), actualLine.getAmount());
        if (StringUtils.isNotBlank(expectedLine.getDebitCreditCode())) {
            assertEquals("Wrong debit/credit code", expectedLine.getDebitCreditCode(), actualLine.getDebitCreditCode());
        } else {
            assertTrue("Line should not have a debit/credit code set up", StringUtils.isBlank(actualLine.getDebitCreditCode()));
        }
        
        if (expectedLine instanceof BudgetAdjustmentAccountingLine) {
            assertBudgetAdjustmentAccountingLinePropertiesAreCorrect(
                    (BudgetAdjustmentAccountingLine) expectedLine, (BudgetAdjustmentAccountingLine) actualLine);
        }
    }

    private void assertBudgetAdjustmentAccountingLinePropertiesAreCorrect(
            BudgetAdjustmentAccountingLine expectedLine, BudgetAdjustmentAccountingLine actualLine) {
        assertEquals("Wrong base amount", expectedLine.getBaseBudgetAdjustmentAmount(), actualLine.getBaseBudgetAdjustmentAmount());
        assertEquals("Wrong current amount", expectedLine.getCurrentBudgetAdjustmentAmount(), actualLine.getCurrentBudgetAdjustmentAmount());
        assertEquals("Wrong month 01 amount",
                expectedLine.getFinancialDocumentMonth1LineAmount(), actualLine.getFinancialDocumentMonth1LineAmount());
        assertEquals("Wrong month 02 amount",
                expectedLine.getFinancialDocumentMonth2LineAmount(), actualLine.getFinancialDocumentMonth2LineAmount());
        assertEquals("Wrong month 03 amount",
                expectedLine.getFinancialDocumentMonth3LineAmount(), actualLine.getFinancialDocumentMonth3LineAmount());
        assertEquals("Wrong month 04 amount",
                expectedLine.getFinancialDocumentMonth4LineAmount(), actualLine.getFinancialDocumentMonth4LineAmount());
        assertEquals("Wrong month 05 amount",
                expectedLine.getFinancialDocumentMonth5LineAmount(), actualLine.getFinancialDocumentMonth5LineAmount());
        assertEquals("Wrong month 06 amount",
                expectedLine.getFinancialDocumentMonth6LineAmount(), actualLine.getFinancialDocumentMonth6LineAmount());
        assertEquals("Wrong month 07 amount",
                expectedLine.getFinancialDocumentMonth7LineAmount(), actualLine.getFinancialDocumentMonth7LineAmount());
        assertEquals("Wrong month 08 amount",
                expectedLine.getFinancialDocumentMonth8LineAmount(), actualLine.getFinancialDocumentMonth8LineAmount());
        assertEquals("Wrong month 09 amount",
                expectedLine.getFinancialDocumentMonth9LineAmount(), actualLine.getFinancialDocumentMonth9LineAmount());
        assertEquals("Wrong month 10 amount",
                expectedLine.getFinancialDocumentMonth10LineAmount(), actualLine.getFinancialDocumentMonth10LineAmount());
        assertEquals("Wrong month 11 amount",
                expectedLine.getFinancialDocumentMonth11LineAmount(), actualLine.getFinancialDocumentMonth11LineAmount());
        assertEquals("Wrong month 12 amount",
                expectedLine.getFinancialDocumentMonth12LineAmount(), actualLine.getFinancialDocumentMonth12LineAmount());
    }

    private void assertInternalBillingItemIsCorrect(InternalBillingItem expectedItem, InternalBillingItem actualItem) {
        assertEquals("Wrong document number", expectedItem.getDocumentNumber(), actualItem.getDocumentNumber());
        assertEquals("Wrong item sequence number", expectedItem.getItemSequenceId(), actualItem.getItemSequenceId());
        assertEquals("Wrong service date", expectedItem.getItemServiceDate(), actualItem.getItemServiceDate());
        assertEquals("Wrong stock number", expectedItem.getItemStockNumber(), actualItem.getItemStockNumber());
        assertEquals("Wrong item description", expectedItem.getItemStockDescription(), actualItem.getItemStockDescription());
        assertEquals("Wrong item quantity", expectedItem.getItemQuantity(), actualItem.getItemQuantity());
        assertEquals("Wrong unit of measure", expectedItem.getUnitOfMeasureCode(), actualItem.getUnitOfMeasureCode());
        assertEquals("Wrong item cost", expectedItem.getItemUnitAmount(), actualItem.getItemUnitAmount());
    }

    private void assertNoteIsCorrect(Note expectedNote, Note actualNote) {
        assertEquals("Wrong note type", expectedNote.getNoteTypeCode(), actualNote.getNoteTypeCode());
        assertEquals("Wrong note text", expectedNote.getNoteText(), actualNote.getNoteText());
        if (expectedNote.getAttachment() == null) {
            assertNull("Note should not have had an attachment", actualNote.getAttachment());
        } else {
            Attachment expectedAttachment = expectedNote.getAttachment();
            Attachment actualAttachment = actualNote.getAttachment();
            assertNotNull("Note should have had an attachment", actualAttachment);
            assertEquals("Wrong attachment file name", expectedAttachment.getAttachmentFileName(), actualAttachment.getAttachmentFileName());
        }
    }
    
    private void assertCuDisbursementVoucherDocumentsCorrect(CuDisbursementVoucherDocument expectedDvDocument, CuDisbursementVoucherDocument actualDvDocument) {
        assertEquals("Wrong bank code", expectedDvDocument.getDisbVchrBankCode(), actualDvDocument.getDisbVchrBankCode());
        assertEquals("Wrong contact name", expectedDvDocument.getDisbVchrContactPersonName(), actualDvDocument.getDisbVchrContactPersonName());
        assertEquals("payment reason code not correct", expectedDvDocument.getDvPayeeDetail().getDisbVchrPaymentReasonCode(), 
                actualDvDocument.getDvPayeeDetail().getDisbVchrPaymentReasonCode());
        assertEquals("payee type code not correct", expectedDvDocument.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode(), 
                actualDvDocument.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode());
        assertEquals("non employee travel perdiem rate not correct", expectedDvDocument.getDvNonEmployeeTravel().getDisbVchrPerdiemRate(),
                actualDvDocument.getDvNonEmployeeTravel().getDisbVchrPerdiemRate());
        assertEquals("conference destination not correct", expectedDvDocument.getDvPreConferenceDetail().getDvConferenceDestinationName(),
                actualDvDocument.getDvPreConferenceDetail().getDvConferenceDestinationName());
        assertEquals("non employee traveler name not correct", expectedDvDocument.getDvNonEmployeeTravel().getDisbVchrNonEmpTravelerName(), 
                actualDvDocument.getDvNonEmployeeTravel().getDisbVchrNonEmpTravelerName());
        assertEquals("non employee mileage not correct", expectedDvDocument.getDvNonEmployeeTravel().getDvPersonalCarMileageAmount(), 
                actualDvDocument.getDvNonEmployeeTravel().getDvPersonalCarMileageAmount());
        assertObjectListIsCorrect("non employee travel expenses aren't correct", expectedDvDocument.getDvNonEmployeeTravel().getDvNonEmployeeExpenses(), 
                actualDvDocument.getDvNonEmployeeTravel().getDvNonEmployeeExpenses(), this::assertTravelExpenseCorrect);
        assertObjectListIsCorrect("non employee prepaid travel expenses aren't correct", expectedDvDocument.getDvNonEmployeeTravel().getDvPrePaidEmployeeExpenses(), 
                actualDvDocument.getDvNonEmployeeTravel().getDvPrePaidEmployeeExpenses(), this::assertTravelExpenseCorrect);
    }
    
    private void assertTravelExpenseCorrect(DisbursementVoucherNonEmployeeExpense expectedExpense, DisbursementVoucherNonEmployeeExpense actualExpense) {
        assertEquals("expense code not correct", expectedExpense.getDisbVchrExpenseCode(), actualExpense.getDisbVchrExpenseCode());
        assertEquals("expense company name not correct", expectedExpense.getDisbVchrExpenseCompanyName(), actualExpense.getDisbVchrExpenseCompanyName());
        assertEquals("expense amount not correct", expectedExpense.getDisbVchrExpenseAmount(), actualExpense.getDisbVchrExpenseAmount());
        
        
    }

    private void assertAdHocPersonIsCorrect(AdHocRoutePerson expectedAdHocPerson, AdHocRoutePerson actualAdHocPerson) {
        assertEquals("Wrong document number", expectedAdHocPerson.getdocumentNumber(), actualAdHocPerson.getdocumentNumber());
        assertEquals("Wrong recipient netID", expectedAdHocPerson.getId(), actualAdHocPerson.getId());
        assertEquals("Wrong action requested", expectedAdHocPerson.getActionRequested(), actualAdHocPerson.getActionRequested());
    }

    private void assertAuxiliaryVoucherDocumentIsCorrect(AuxiliaryVoucherDocument expectedDocument, AuxiliaryVoucherDocument actualDocument) {
        assertEquals("Wrong accounting period code", expectedDocument.getPostingPeriodCode(), actualDocument.getPostingPeriodCode());
        assertEquals("Wrong accounting period fiscal year", expectedDocument.getPostingYear(), actualDocument.getPostingYear());
        assertEquals("Wrong AV document type", expectedDocument.getTypeCode(), actualDocument.getTypeCode());
        assertEquals("Wrong reversal date", expectedDocument.getReversalDate(), actualDocument.getReversalDate());
    }

    private void createTargetTestDirectory() throws IOException {
        File accountingXmlDocumentTestDirectory = new File(TARGET_TEST_FILE_PATH);
        FileUtils.forceMkdir(accountingXmlDocumentTestDirectory);
    }

    private void copyTestFilesAndCreateDoneFiles(String... baseFileNames) throws IOException {
        for (String baseFileName : baseFileNames) {
            File sourceFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, SOURCE_TEST_FILE_PATH, baseFileName, CuFPConstants.XML_FILE_EXTENSION));
            File targetFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH, baseFileName, CuFPConstants.XML_FILE_EXTENSION));
            File doneFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH, baseFileName,
                            GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION));
            FileUtils.copyFile(sourceFile, targetFile);
            doneFile.createNewFile();
            creationOrderedBaseFileNames.add(baseFileName);
        }
    }

    private void assertDoneFilesWereDeleted() {
        for (String baseFileName : creationOrderedBaseFileNames) {
            File doneFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH, baseFileName,
                            GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION));
            assertFalse("The file '" + baseFileName + GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION
                    + "' should have been deleted", doneFile.exists());
        }
    }

    private void deleteTargetTestDirectory() throws IOException {
        File accountingXmlDocumentTestDirectory = new File(TARGET_TEST_FILE_PATH);
        if (accountingXmlDocumentTestDirectory.exists() && accountingXmlDocumentTestDirectory.isDirectory()) {
            FileUtils.forceDelete(accountingXmlDocumentTestDirectory.getAbsoluteFile());
        }
    }

    private JAXBXmlBatchInputFileTypeBase buildAccountingXmlDocumentInputFileType(DateTimeService dateTimeService) throws Exception {
        JAXBXmlBatchInputFileTypeBase inputFileType = new JAXBXmlBatchInputFileTypeBase();
        inputFileType.setDateTimeService(dateTimeService);
        inputFileType.setCuMarshalService(new CUMarshalServiceImpl());
        inputFileType.setPojoClass(AccountingXmlDocumentListWrapper.class);
        inputFileType.setFileTypeIdentifier("accountingXmlDocumentFileType");
        inputFileType.setFileNamePrefix("accountingXmlDocument_");
        inputFileType.setTitleKey("accountingXmlDocument");
        inputFileType.setFileExtension(StringUtils.substringAfter(CuFPConstants.XML_FILE_EXTENSION, KFSConstants.DELIMITER));
        inputFileType.setDirectoryPath(TARGET_TEST_FILE_PATH);
        return inputFileType;
    }

    private DateTimeService buildMockDateTimeService() throws Exception {
        DateTimeService dateTimeService = Mockito.mock(DateTimeService.class);
        Mockito.when(dateTimeService.toDateTimeStringForFilename(Mockito.any())).then(this::formatDate);
        Mockito.when(dateTimeService.getCurrentDate()).then(this::buildNewDate);
        Mockito.when(dateTimeService.getCurrentSqlDate())
                .then(this::buildStaticSqlDate);
        return dateTimeService;
    }
    
    private String formatDate(InvocationOnMock invocation) {
        Date date = invocation.getArgument(0);
        return DATE_FORMAT.format(date);
    }
    
    private Date buildNewDate(InvocationOnMock invocation) {
        return new Date();
    }

    private java.sql.Date buildStaticSqlDate(InvocationOnMock invocation) {
        DateTime dateTime = StringToJavaDateAdapter.parseToDateTime(CuFPTestConstants.DATE_02_21_2019);
        return new java.sql.Date(dateTime.getMillis());
    }

    private FileStorageService buildFileStorageService() throws Exception {
        FileSystemFileStorageServiceImpl fileStorageService = new FileSystemFileStorageServiceImpl();
        fileStorageService.setPathPrefix(KFSConstants.DELIMITER);
        return fileStorageService;
    }

    private ConfigurationService buildMockConfigurationService() throws Exception {
        ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        Mockito.when(configurationService.getPropertyValueAsString(CuFPTestConstants.TEST_VALIDATION_ERROR_KEY))
            .thenReturn(CuFPTestConstants.TEST_VALIDATION_ERROR_MESSAGE);
        Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_ATTACHMENT_DOWNLOAD))
            .thenReturn(CuFPTestConstants.TEST_ATTACHMENT_DOWNLOAD_FAILURE_MESSAGE);
        Mockito.when(configurationService.getPropertyValueAsString(Mockito.startsWith(CuFPTestConstants.AV_VALIDATION_MESSAGE_KEY_PREFIX)))
            .then(this::buildAuxiliaryVoucherErrorMessage);
        return configurationService;
    }

    private String buildAuxiliaryVoucherErrorMessage(InvocationOnMock invocation) {
        String messageKey = invocation.getArgument(0);
        return messageKey + " {0}";
    }

    private PersonService buildMockPersonService() throws Exception {
        PersonService personService = Mockito.mock(PersonService.class);
        Person systemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
        Mockito.when(personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER)).thenReturn(systemUser);
        Person testUser = MockPersonUtil.createMockPerson(TestUserFixture.TEST_USER);
        Mockito.when(personService.getPersonByEmployeeId(TestUserFixture.TEST_USER.employeeId)).thenReturn(testUser);
        return personService;
    }

    private DocumentService buildMockDocumentService() throws Exception {
        DocumentService documentService = Mockito.mock(DocumentService.class);
        Mockito.when(documentService.routeDocument(Mockito.any(), Mockito.any(), Mockito.any())).then(this::recordAndReturnDocumentIfValid);
        return documentService;
    }
    
    private Document recordAndReturnDocumentIfValid(InvocationOnMock invocation) {
        Document document = invocation.getArgument(0);
        if (!documentPassesBusinessRules(document)) {
            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, CuFPTestConstants.TEST_VALIDATION_ERROR_KEY);
            throw new ValidationException("Simulated business rule validation failure");
        }
        routedAccountingDocuments.add((AccountingDocument) document);
        return document;
    }

    private ParameterEvaluatorService buildParameterEvaluatorService(ParameterService parameterService) {
        ParameterEvaluatorServiceImpl evaluatorService = new ParameterEvaluatorServiceImpl();
        evaluatorService.setParameterService(parameterService);
        return evaluatorService;
    }

    private ParameterService buildParameterService() {
        ParameterService parameterService = Mockito.mock(ParameterService.class);
        Mockito.when(parameterService.getParameterValueAsString(KFSConstants.ParameterNamespaces.FINANCIAL, 
                CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCOUNTING_DOCUMENT_SERVICE_COMPONENT_NAME, 
                CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCT_DOC_REPORT_EMAIL_ADDRESS)).thenReturn("kfs-gl_fp@cornell.edu");
        return parameterService;
    }

    private boolean documentPassesBusinessRules(Document document) {
        return !StringUtils.equalsIgnoreCase(
                CuFPTestConstants.BUSINESS_RULE_VALIDATION_DESCRIPTION_INDICATOR, document.getDocumentHeader().getDocumentDescription());
    }

    private boolean isDocumentExpectedToReachInitiationPoint(AccountingXmlDocumentEntryFixture fixture) {
        return !AccountingXmlDocumentEntryFixture.BAD_CONVERSION_DOCUMENT_PLACEHOLDER.equals(fixture);
    }

    private boolean isDocumentExpectedToPassBusinessRulesValidation(AccountingXmlDocumentEntryFixture fixture) {
        return !AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER.equals(fixture);
    }

    private TestAccountingXmlDocumentDownloadAttachmentService buildAccountingXmlDocumentDownloadAttachmentService() throws Exception {
        TestAccountingXmlDocumentDownloadAttachmentService downloadAttachmentService = new TestAccountingXmlDocumentDownloadAttachmentService();
        downloadAttachmentService.setAttachmentService(buildMockAttachmentService());
        downloadAttachmentService.setWebServiceCredentialService(buildMockWebServiceCredentialService());
        downloadAttachmentService.setClient(buildMockClient());
        return downloadAttachmentService;
    }

    private AttachmentService buildMockAttachmentService() throws Exception {
        AttachmentService attachmentService = Mockito.mock(AttachmentService.class);
        Mockito.when(attachmentService.createAttachment(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.any(), Mockito.any())).then(this::buildSimpleAttachment);
        return attachmentService;
    }
    
    private Attachment buildSimpleAttachment(InvocationOnMock invocation) {
        String fileName = invocation.getArgument(1);
        Attachment attachment = new Attachment();
        attachment.setAttachmentFileName(fileName);
        return attachment;
    }

    private WebServiceCredentialService buildMockWebServiceCredentialService() {
        WebServiceCredentialService credentialService = Mockito.mock(WebServiceCredentialService.class);
        Mockito.when(credentialService.getWebServiceCredentialsByGroupCode(Mockito.anyString())).then(this::findCredentialsFromFixture);
        return credentialService;
    }
    
    private Collection<WebServiceCredential> findCredentialsFromFixture(InvocationOnMock invocation) {
        String groupCode = invocation.getArgument(0);
        return WebServiceCredentialFixture.getCredentialsByCredentialGroupCode(groupCode);
    }

    private FiscalYearFunctionControlService buildMockFiscalYearFunctionControlService() {
        List<FiscalYearFunctionControl> allowedBudgetAdjustmentYears = IntStream.of(CuFPTestConstants.FY_2016, CuFPTestConstants.FY_2018)
                .mapToObj(this::buildFunctionControlAllowingBudgetAdjustment)
                .collect(Collectors.toCollection(ArrayList::new));
        
        FiscalYearFunctionControlService fyService = mock(FiscalYearFunctionControlService.class);
        when(fyService.getBudgetAdjustmentAllowedYears())
                .thenReturn(allowedBudgetAdjustmentYears);
        
        return fyService;
    }

    private FiscalYearFunctionControl buildFunctionControlAllowingBudgetAdjustment(int fiscalYear) {
        FiscalYearFunctionControl functionControl = new FiscalYearFunctionControl();
        functionControl.setUniversityFiscalYear(Integer.valueOf(fiscalYear));
        functionControl.setFinancialSystemFunctionControlCode(FiscalYearFunctionControlServiceImpl.FY_FUNCTION_CONTROL_BA_ALLOWED);
        functionControl.setFinancialSystemFunctionActiveIndicator(true);
        return functionControl;
    }

    private DisbursementVoucherTravelService buildMockDisbursementVoucherTravelService() {
        DisbursementVoucherTravelService travelService = Mockito.mock(DisbursementVoucherTravelService.class);
        Mockito.when(travelService.calculateMileageAmount(Mockito.anyInt(), Mockito.any())).thenReturn(new KualiDecimal(50));
        return travelService;
    }
    
    private UniversityDateService buildMockUniversityDateService() {
        UniversityDateService dateService = Mockito.mock(UniversityDateService.class);
        Mockito.when(dateService.getCurrentFiscalYear()).thenReturn(2019);
        return dateService;
    }

    private AccountingPeriodService buildAccountingPeriodService() {
        AccountingPeriodServiceImpl accountingPeriodService = Mockito.spy(new AccountingPeriodServiceImpl());
        accountingPeriodService.setBusinessObjectService(buildMockBusinessObjectService());
        Mockito.doReturn(buildListOfOpenAccountingPeriods())
                .when(accountingPeriodService).getOpenAccountingPeriods();
        return accountingPeriodService;
    }

    private List<AccountingPeriod> buildListOfOpenAccountingPeriods() {
        return AccountingPeriodFixture.findOpenAccountingPeriodsAsStream()
                .map(AccountingPeriodFixture::toAccountingPeriod)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private BusinessObjectService buildMockBusinessObjectService() {
        BusinessObjectService businessObjectService = Mockito.mock(BusinessObjectService.class);
        Mockito.when(businessObjectService.findByPrimaryKey(Mockito.eq(AccountingPeriod.class), Mockito.anyMap()))
                .then(this::findAccountingPeriod);
        return businessObjectService;
    }

    private AccountingPeriod findAccountingPeriod(InvocationOnMock invocation) {
        Map<?, ?> primaryKeys = invocation.getArgument(1);
        String periodCode = (String) primaryKeys.get(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE);
        Integer fiscalYear = (Integer) primaryKeys.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR);
        return findAccountingPeriod(fiscalYear, periodCode);
    }

    private AccountingPeriod findAccountingPeriod(Integer fiscalYear, String periodCode) {
        Optional<AccountingPeriodFixture> result = AccountingPeriodFixture.findAccountingPeriod(fiscalYear, periodCode);
        if (result.isPresent()) {
            return result.get().toAccountingPeriod();
        } else {
            return null;
        }
    }

    private Client buildMockClient() {
        Client client = Mockito.mock(Client.class);
        Mockito.when(client.target(Mockito.any(URI.class))).then(this::buildMockWebTarget);
        return client;
    }

    private WebTarget buildMockWebTarget(InvocationOnMock invocation) {
        WebTarget target = Mockito.mock(WebTarget.class);
        Mockito.when(target.request()).then(this::buildMockInvocationBuilder);
        return target;
    }

    private Invocation.Builder buildMockInvocationBuilder(InvocationOnMock invocation) {
        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        Mockito.when(builder.header(Mockito.anyString(), Mockito.any())).thenReturn(builder);
        Mockito.when(builder.buildGet()).then(this::buildMockInvocation);
        return builder;
    }
    
    private Invocation buildMockInvocation(InvocationOnMock invocationOnMock) {
        Invocation invocation = Mockito.mock(Invocation.class);
        Mockito.when(invocation.invoke()).then(this::buildMockResponse);
        return invocation;
    }

    private Response buildMockResponse(InvocationOnMock invocation) {
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        Mockito.when(response.readEntity(InputStream.class)).then(this::buildSingleByteInputStream);
        return response;
    }

    private InputStream buildSingleByteInputStream(InvocationOnMock invocation) {
        return new ByteArrayInputStream(new byte[] {1});
    }

    private static class TestCreateAccountingDocumentServiceImpl extends CreateAccountingDocumentServiceImpl {
        private Map<String, AccountingDocumentGenerator<?>> documentGeneratorsByBeanName;
        private PersonService personService;
        private AccountingXmlDocumentDownloadAttachmentService downloadAttachmentService;
        private ConfigurationService configurationService;
        private DisbursementVoucherTravelService disbursementVoucherTravelService;
        private FiscalYearFunctionControlService fiscalYearFunctionControlService;
        private UniversityDateService universityDateService;
        private AccountingPeriodService accountingPeriodService;
        private DateTimeService dateTimeService;
        
        private int nextDocumentNumber;
        private List<String> processingOrderedBaseFileNames;
        private boolean failToCreateDocument;

        public TestCreateAccountingDocumentServiceImpl(
                PersonService personService, AccountingXmlDocumentDownloadAttachmentService downloadAttachmentService,
                ConfigurationService configurationService, FiscalYearFunctionControlService fiscalYearFunctionControlService, 
                DisbursementVoucherTravelService disbursementVoucherTravelService, UniversityDateService universityDateService,
                AccountingPeriodService accountingPeriodService, DateTimeService dateTimeService) {
            this.personService = personService;
            this.downloadAttachmentService = downloadAttachmentService;
            this.disbursementVoucherTravelService = disbursementVoucherTravelService;
            this.configurationService = configurationService;
            this.fiscalYearFunctionControlService = fiscalYearFunctionControlService;
            this.universityDateService = universityDateService;
            this.accountingPeriodService = accountingPeriodService;
            this.dateTimeService = dateTimeService;
            this.nextDocumentNumber = DOCUMENT_NUMBER_START;
            this.processingOrderedBaseFileNames = new ArrayList<>();
            this.failToCreateDocument = false;
        }

        public void setFailToCreateDocument(boolean failToCreateDocument) {
            this.failToCreateDocument = failToCreateDocument;
        }

        public void initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping... documentMappings) {
            this.documentGeneratorsByBeanName = Arrays.stream(documentMappings)
                    .map(AccountingDocumentMapping::getGeneratorConstructor)
                    .map(this::buildAccountingDocumentGenerator)
                    .collect(Collectors.toMap(
                            this::buildGeneratorBeanName, Function.identity()));
        }

        protected AccountingDocumentGenerator<?> buildAccountingDocumentGenerator(
                BiFunction<Supplier<Note>, Supplier<AdHocRoutePerson>, AccountingDocumentGeneratorBase<?>> generatorConstructor) {
            AccountingDocumentGeneratorBase<?> accountingDocumentGenerator = generatorConstructor.apply(
                    MockDocumentUtils::buildMockNote, MockDocumentUtils::buildMockAdHocRoutePerson);
            accountingDocumentGenerator.setPersonService(personService);
            accountingDocumentGenerator.setAccountingXmlDocumentDownloadAttachmentService(downloadAttachmentService);
            accountingDocumentGenerator.setConfigurationService(configurationService);
            if (accountingDocumentGenerator instanceof CuBudgetAdjustmentDocumentGenerator) {
                CuBudgetAdjustmentDocumentGenerator baGenerator = (CuBudgetAdjustmentDocumentGenerator) accountingDocumentGenerator;
                baGenerator.setFiscalYearFunctionControlService(fiscalYearFunctionControlService);
            } else if (accountingDocumentGenerator instanceof CuYearEndBudgetAdjustmentDocumentGenerator) {
                CuYearEndBudgetAdjustmentDocumentGenerator yebaGenerator = (CuYearEndBudgetAdjustmentDocumentGenerator) accountingDocumentGenerator;
                yebaGenerator.setFiscalYearFunctionControlService(fiscalYearFunctionControlService);
            } else  if (accountingDocumentGenerator instanceof CuDisbursementVoucherDocumentGenerator) {
                CuDisbursementVoucherDocumentGenerator dvGenerator = (CuDisbursementVoucherDocumentGenerator) accountingDocumentGenerator;
                dvGenerator.setUniversityDateService(universityDateService);
                dvGenerator.setDisbursementVoucherTravelService(disbursementVoucherTravelService);
            } else if (accountingDocumentGenerator instanceof AuxiliaryVoucherDocumentGenerator) {
                AuxiliaryVoucherDocumentGenerator avGenerator = (AuxiliaryVoucherDocumentGenerator) accountingDocumentGenerator;
                avGenerator.setAccountingPeriodService(accountingPeriodService);
                avGenerator.setDateTimeService(dateTimeService);
            }
            return accountingDocumentGenerator;
        }

        protected String buildGeneratorBeanName(AccountingDocumentGenerator<?> documentGenerator) {
            return CuFPConstants.ACCOUNTING_DOCUMENT_GENERATOR_BEAN_PREFIX
                    + AccountingDocumentClassMappingUtils.getDocumentTypeByDocumentClass(documentGenerator.getDocumentClass());
        }

        public List<String> getProcessingOrderedBaseFileNames() {
            return processingOrderedBaseFileNames;
        }

        @Override
        public boolean createAccountingDocumentsFromXml() {
            Person systemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
            UserSession systemUserSession = MockPersonUtil.createMockUserSession(systemUser);

            boolean result = false;
            try {
            	result = GlobalVariables.doInNewGlobalVariables(systemUserSession, () -> {
                	return super.createAccountingDocumentsFromXml();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return  result;
        }

        @Override
        protected void processAccountingDocumentFromXml(String fileName, CreateAccountingDocumentLogReport logReport) {
            processingOrderedBaseFileNames.add(convertToBaseFileName(fileName));
            super.processAccountingDocumentFromXml(fileName, logReport);
        }

        private String convertToBaseFileName(String fileName) {
            String fileNameWithoutPath = new File(fileName).getName();
            return StringUtils.substringBefore(fileNameWithoutPath, KFSConstants.DELIMITER);
        }

        @Override
        protected AccountingDocumentGenerator<?> findDocumentGenerator(String beanName) {
            if (StringUtils.isBlank(beanName)) {
                throw new IllegalStateException("Document generator bean name should not have been blank");
            } else {
                return documentGeneratorsByBeanName.computeIfAbsent(beanName, this::throwExceptionIfGeneratorIsNotFound);
            }
        }

        protected AccountingDocumentGenerator<?> throwExceptionIfGeneratorIsNotFound(String beanName) {
            throw new IllegalStateException("Unrecognized document generator bean name: " + beanName);
        }

        @Override
        protected Document getNewDocument(Class<? extends Document> documentClass) {
            if (failToCreateDocument) {
                throw new ResourceLoaderException("Emulate problem getting services");
            }
            if (documentClass == null) {
                throw new IllegalStateException("Document class should not have been null");
            } else if (generatorDoesNotExistForDocumentClass(documentClass)) {
                throw new IllegalStateException("Unexpected accounting document class: " + documentClass.getName());
            }
            
            @SuppressWarnings("unchecked")
            Class<? extends AccountingDocument> accountingDocumentClass = (Class<? extends AccountingDocument>) documentClass;
            Document document = MockDocumentUtils.buildMockAccountingDocument(accountingDocumentClass);
            String documentNumber = String.valueOf(++nextDocumentNumber);
            document.setDocumentNumber(documentNumber);
            document.getDocumentHeader().setDocumentNumber(documentNumber);
            
            return document;
        }

        protected boolean generatorDoesNotExistForDocumentClass(Class<? extends Document> documentClass) {
            return documentGeneratorsByBeanName.values().stream()
                    .noneMatch((generator) -> generator.getDocumentClass().equals(documentClass));
        }

        @Override
        protected String buildValidationErrorMessage(ValidationException validationException) {
            String validationErrorMessage = super.buildValidationErrorMessage(validationException);
            assertFalse("The error-message-building process should not have encountered an unexpected exception",
                    StringUtils.equals(CuFPConstants.ALTERNATE_BASE_VALIDATION_ERROR_MESSAGE, validationErrorMessage));
            return validationErrorMessage;
        }
        
    }
    
    private static class TestAccountingXmlDocumentDownloadAttachmentService extends AccountingXmlDocumentDownloadAttachmentServiceImpl {

        private Client mockClient;

        @Override
        protected Client getClient() {
            return mockClient;
        }

        public void setClient(Client client) {
            this.mockClient = client;
        }
        
    }
    
    private class TestCreateAccountingDocumentReportService implements CreateAccountingDocumentReportService {

        @Override
        public void generateReport(CreateAccountingDocumentReportItem reportItem) {
        }

        @Override
        public void sendReportEmail(String toAddress, String fromAddress) {
        }
        
    }
    
    private class TestCreateAccountingDocumentValidationService implements CreateAccountingDocumentValidationService {
        
        @Override
        public boolean isValidXmlFileHeaderData(AccountingXmlDocumentListWrapper accountingXmlDocuments, CreateAccountingDocumentReportItem reportItem) {
            return true;
        }
        
        @Override
        public boolean isAllRequiredDataValid(AccountingXmlDocumentEntry accountingXmlDocument, CreateAccountingDocumentReportItemDetail reportItemDetail) {
            return true;
        }
    }

}
