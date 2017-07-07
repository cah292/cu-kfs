package com.rsmart.kuali.kfs.sys.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.rice.core.api.config.property.ConfigurationService;

public class BatchFeedHelperServiceImplTest {

    private static final String TEST_DOCID1 = "1234567";
    private static final String TEST_DOCID2 = "6666666";
    private static final String TEST_NAMESPACE1 = "KFS-NS1";
    private static final String TEST_NAMESPACE2 = "KFS-NMSPC2";
    private static final String TEST_NAME1 = "ApproverGroup";
    private static final String TEST_NAME2 = "AuthorizerGroup";
    private static final String DOCID_PROPERTY = "documentId";
    private static final String GROUP_NAME_PROPERTY = "groupName";
    private static final String RUNTIME_EXCEPTION_MESSAGE_PREFIX = "Cannot find message for error key: ";
    private static final String NON_EXISTENT_ERROR_KEY = "nonexistent.error";

    private enum ConfigProperty {
        SUCCESS_MESSAGE1("success.message1", "Validation for document {0} succeeded"),
        SUCCESS_MESSAGE2("success.message2", "Document {0} was validated successfully"),
        ERROR_BAD_DOCUMENT_ID("error.bad.documentid", "Could not find document with ID {0}"),
        ERROR_BLANK_DOCUMENT_ID("error.blank.documentid", "Document ID should not have been blank"),
        ERROR_BAD_GROUP_NAME("error.bad.group.name", "Could not find group with namespace '{0}' and name '{1}'");
        
        public final String propertyName;
        public final String propertyValue;
        
        private ConfigProperty(String propertyName, String propertyValue) {
            this.propertyName = propertyName;
            this.propertyValue = propertyValue;
        }
    }

    private ConfigurationService configurationService;
    private BatchFeedHelperServiceImpl batchFeedHelperService;

    @Before
    public void setUp() throws Exception {
        configurationService = new MockConfigurationService();
        batchFeedHelperService = new BatchFeedHelperServiceImpl();
        batchFeedHelperService.setKualiConfigurationService(configurationService);
    }

    @Test
    public void testEmptyMessageMap() throws Exception {
        MessageMap errorMap = new MessageMap();
        assertStreamedAuditMessageMatchesIterativeMessage(ConfigProperty.SUCCESS_MESSAGE1, TEST_DOCID1, errorMap);
    }

    @Test
    public void testEmptyMessageMapWithDifferentSuccessKey() throws Exception {
        MessageMap errorMap = new MessageMap();
        assertStreamedAuditMessageMatchesIterativeMessage(ConfigProperty.SUCCESS_MESSAGE2, TEST_DOCID1, errorMap);
    }

    @Test
    public void testEmptyMessageMapWithDifferentDocumentNumber() throws Exception {
        MessageMap errorMap = new MessageMap();
        assertStreamedAuditMessageMatchesIterativeMessage(ConfigProperty.SUCCESS_MESSAGE1, TEST_DOCID2, errorMap);
    }

    @Test
    public void testMessageMapWithUnparameterizedError() throws Exception {
        MessageMap errorMap = new MessageMap();
        errorMap.putError(DOCID_PROPERTY, ConfigProperty.ERROR_BLANK_DOCUMENT_ID.propertyName);
        
        assertStreamedAuditMessageMatchesIterativeMessage(ConfigProperty.SUCCESS_MESSAGE1, TEST_DOCID1, errorMap);
    }

    @Test
    public void testMessageMapWithSingleParameterError() throws Exception {
        MessageMap errorMap = new MessageMap();
        errorMap.putError(DOCID_PROPERTY, ConfigProperty.ERROR_BAD_DOCUMENT_ID.propertyName, TEST_DOCID2);
        
        assertStreamedAuditMessageMatchesIterativeMessage(ConfigProperty.SUCCESS_MESSAGE1, TEST_DOCID1, errorMap);
    }

    @Test
    public void testMessageMapWithMultiParameterError() throws Exception {
        MessageMap errorMap = new MessageMap();
        errorMap.putError(GROUP_NAME_PROPERTY, ConfigProperty.ERROR_BAD_GROUP_NAME.propertyName, TEST_NAMESPACE1, TEST_NAME1);
        
        assertStreamedAuditMessageMatchesIterativeMessage(ConfigProperty.SUCCESS_MESSAGE1, TEST_DOCID1, errorMap);
    }

    @Test
    public void testMessageMapWithMultipleErrors() throws Exception {
        MessageMap errorMap = new MessageMap();
        errorMap.putError(DOCID_PROPERTY, ConfigProperty.ERROR_BLANK_DOCUMENT_ID.propertyName);
        errorMap.putError(GROUP_NAME_PROPERTY, ConfigProperty.ERROR_BAD_GROUP_NAME.propertyName, TEST_NAMESPACE2, TEST_NAME2);
        
        assertStreamedAuditMessageMatchesIterativeMessage(ConfigProperty.SUCCESS_MESSAGE1, TEST_DOCID1, errorMap);
    }

    @Test
    public void testMessageMapWithInvalidErrorKey() throws Exception {
        String successfulErrorKey = ConfigProperty.SUCCESS_MESSAGE1.propertyName;
        MessageMap errorMap = new MessageMap();
        errorMap.putError(DOCID_PROPERTY, NON_EXISTENT_ERROR_KEY);
        
        assertOperationFailsDueToNonExistentErrorKey(
                NON_EXISTENT_ERROR_KEY, () -> batchFeedHelperService.getAuditMessage(successfulErrorKey, TEST_DOCID1, errorMap));
        assertOperationFailsDueToNonExistentErrorKey(
                NON_EXISTENT_ERROR_KEY, () -> batchFeedHelperService.getAuditMessageUsingStreams(successfulErrorKey, TEST_DOCID1, errorMap));
        assertOperationFailsDueToNonExistentErrorKey(
                NON_EXISTENT_ERROR_KEY, () -> batchFeedHelperService.getAuditMessageUsingStreamsAndMethodRefs(successfulErrorKey, TEST_DOCID1, errorMap));
    }

    private void assertStreamedAuditMessageMatchesIterativeMessage(
            ConfigProperty successfulErrorProperty, String documentNumber, MessageMap errorMap) throws Exception {
        assertStreamedAuditMessageMatchesIterativeMessage(
                successfulErrorProperty.propertyName, documentNumber, errorMap);
    }

    private void assertStreamedAuditMessageMatchesIterativeMessage(
            String successfulErrorKey, String documentNumber, MessageMap errorMap) throws Exception {
        String expectedMessage = batchFeedHelperService.getAuditMessage(successfulErrorKey, documentNumber, errorMap);
        String streamedMessage = batchFeedHelperService.getAuditMessageUsingStreams(successfulErrorKey, documentNumber, errorMap);
        String methodRefMessage = batchFeedHelperService.getAuditMessageUsingStreamsAndMethodRefs(successfulErrorKey, documentNumber, errorMap);
        
        assertEquals("Wrong value for stream-derived message", expectedMessage, streamedMessage);
        assertEquals("Wrong value for stream-and-method-ref-derived message", expectedMessage, methodRefMessage);
    }

    private void assertOperationFailsDueToNonExistentErrorKey(String badErrorKey, Supplier<String> auditMessageOperation) throws Exception {
        try {
            auditMessageOperation.get();
            fail("The operation should have thrown a RuntimeException");
        } catch (Exception e) {
            assertEquals("Wrong exception type", RuntimeException.class, e.getClass());
            assertEquals("Wrong exception message", RUNTIME_EXCEPTION_MESSAGE_PREFIX + badErrorKey, e.getMessage());
        }
    }

    private static class MockConfigurationService implements ConfigurationService {
        
        private Map<String, String> propertyValues;
        
        private MockConfigurationService() {
            propertyValues = Arrays.stream(ConfigProperty.values())
                    .collect(Collectors.collectingAndThen(
                            Collectors.<ConfigProperty, String, String>toMap(
                                    (configProp) -> configProp.propertyName, (configProp) -> configProp.propertyValue),
                            (newMap) -> Collections.unmodifiableMap(newMap)));
        }
        
        @Override
        public Map<String, String> getAllProperties() {
            return propertyValues;
        }

        @Override
        public boolean getPropertyValueAsBoolean(String propertyName, boolean defaultValue) {
            String value = propertyValues.getOrDefault(propertyName, Boolean.toString(defaultValue));
            return Boolean.parseBoolean(value);
        }

        @Override
        public boolean getPropertyValueAsBoolean(String propertyName) {
            return Boolean.parseBoolean(propertyValues.get(propertyName));
        }

        @Override
        public String getPropertyValueAsString(String propertyName) {
            return propertyValues.get(propertyName);
        }
    }

}
