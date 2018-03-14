package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.List;

import org.joda.time.DateTime;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum AccountingXmlDocumentListWrapperFixture {
    BASE_WRAPPER("09/28/2017", "abc123@cornell.edu", "Example XML file", documents()),
    BASE_IB_WRAPPER("02/26/2018", "xyz789@cornell.edu", "Example IB XML file", documents()),

    MULTI_DI_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC3,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC4)),
    MULTI_DI_DOCUMENT_WITH_BAD_CONVERSION_SECOND_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.BAD_CONVERSION_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC3)),
    MULTI_DI_DOCUMENT_WITH_BAD_RULES_FIRST_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC3)),
    MULTI_DI_DOCUMENT_WITH_BAD_ATTACHMENTS_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC4,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER)),
    SINGLE_DI_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_DI_DOCUMENT_TEST_DOC1)),
    DI_FULL_ACCOUNT_LINE_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.DI_FULL_ACCOUNT_LINE_TEST_DOC1)),
    DI_SINGLE_ELEMENT_LISTS_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.DI_SINGLE_ELEMENT_LISTS_TEST_DOC1)),
    DI_EMPTY_ELEMENT_LISTS_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.DI_EMPTY_ELEMENT_LISTS_TEST_DOC1)),
    DI_WITHOUT_ELEMENT_LISTS_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.DI_WITHOUT_ELEMENT_LISTS_TEST_DOC1)),
    EMPTY_DOCUMENT_LIST_TEST(
            BASE_WRAPPER, documents()),
    NO_DOCUMENT_LIST_TEST(
            BASE_WRAPPER, documents()),
    SINGLE_IB_DOCUMENT_TEST(
            BASE_IB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_IB_DOCUMENT_TEST_DOC1)),
    SINGLE_IB_DOCUMENT_NO_ITEMS_TEST(
            BASE_IB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_IB_NO_ITEMS_DOCUMENT_TEST_DOC1)),
    MULTI_IB_DOCUMENT_TEST(
            BASE_IB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_IB_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_IB_DOCUMENT_TEST_DOC2)),
    MULTI_IB_DOCUMENT_WITH_BAD_RULES_THIRD_DOCUMENT_TEST(
            BASE_IB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_IB_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_IB_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER)),
    MULTI_DOCUMENT_TYPES_TEST(
            "02/26/2018", "xyz789@cornell.edu", "Example multi-doc-type XML file",
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_DI,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_IB)),
    DI_WITH_IB_ITEMS_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.DI_WITH_IB_ITEMS_TEST_DOC1));

    public final String createDate;
    public final String reportEmail;
    public final String overview;
    public final List<AccountingXmlDocumentEntryFixture> documents;

    private AccountingXmlDocumentListWrapperFixture(
            AccountingXmlDocumentListWrapperFixture baseFixture, AccountingXmlDocumentEntryFixture[] documents) {
        this(baseFixture.createDate, baseFixture.reportEmail, baseFixture.overview, documents);
    }

    private AccountingXmlDocumentListWrapperFixture(String createDate, String reportEmail,
            String overview, AccountingXmlDocumentEntryFixture[] documents) {
        this.createDate = createDate;
        this.reportEmail = reportEmail;
        this.overview = overview;
        this.documents = AccountingXmlDocumentFixtureUtils.toImmutableList(documents);
    }

    public AccountingXmlDocumentListWrapper toDocumentListWrapperPojo() {
        DateTime parsedCreateDate = StringToJavaDateAdapter.parseToDateTime(createDate);
        AccountingXmlDocumentListWrapper listWrapper = new AccountingXmlDocumentListWrapper();
        listWrapper.setCreateDate(parsedCreateDate.toDate());
        listWrapper.setReportEmail(reportEmail);
        listWrapper.setOverview(overview);
        listWrapper.setDocuments(
                AccountingXmlDocumentFixtureUtils.convertToPojoList(documents, AccountingXmlDocumentEntryFixture::toDocumentEntryPojo));
        return listWrapper;
    }

    // This method is only meant to improve the setup and readability of this enum's constants.
    private static AccountingXmlDocumentEntryFixture[] documents(AccountingXmlDocumentEntryFixture... fixtures) {
        return fixtures;
    }

}
