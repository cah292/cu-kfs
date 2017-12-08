package edu.cornell.kfs.module.ld.document.validation.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.module.ld.LaborConstants;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants.LdParameterConstants;

public class LaborExpenseTransferAccountTypesValidationTest {

    private LaborExpenseTransferAccountTypesValidation oldValidation;
    private LaborExpenseTransferAccountTypesValidation newValidationUsingCollectors;
    private LaborExpenseTransferAccountTypesValidation newValidationUsingSingleStream;

    @Before
    public void setUp() throws Exception {
        oldValidation = new LaborExpenseTransferAccountTypesValidation();
        newValidationUsingCollectors = new LaborExpenseTransferAccountTypesValidation();
        newValidationUsingSingleStream = new LaborExpenseTransferAccountTypesValidation();
    }

    @Test
    public void testSingletonParameter() throws Exception {
        assertAccountTypeSetupsAreIdentical("EN=CC");
    }

    @Test
    public void testMultiValueParameter() throws Exception {
        assertAccountTypeSetupsAreIdentical("EN=CC", "CC=EN", "JI=CC", "CC=JI", "XX=ZZ");
    }

    @Test
    public void testEmptyParameter() throws Exception {
        assertAccountTypeSetupsAreIdentical();
    }

    private void assertAccountTypeSetupsAreIdentical(String... sourceTargetTypePairs) {
        ParameterService mockParameterService = createMockParameterService(sourceTargetTypePairs);
        
        oldValidation.setParameterService(mockParameterService);
        newValidationUsingCollectors.setParameterService(mockParameterService);
        newValidationUsingSingleStream.setParameterService(mockParameterService);
        
        oldValidation.setInvalidTransferAccountTypesMap();
        newValidationUsingCollectors.setInvalidTransferAccountTypesMapUsingCollectors();
        newValidationUsingSingleStream.setInvalidTransferAccountTypesMapUsingSingleStream();
        
        assertEquals("Account types maps should have matched when using collectors",
                oldValidation.getInvalidTransferAccountTypesMap(),
                newValidationUsingCollectors.getInvalidTransferAccountTypesMap());
        assertEquals("Account types sets should have matched when using collectors",
                oldValidation.getInvalidTransferTargetAccountTypesInParam(),
                newValidationUsingCollectors.getInvalidTransferTargetAccountTypesInParam());
        
        assertEquals("Account types maps should have matched when using single stream",
                oldValidation.getInvalidTransferAccountTypesMap(),
                newValidationUsingSingleStream.getInvalidTransferAccountTypesMap());
        assertEquals("Account types sets should have matched when using single stream",
                oldValidation.getInvalidTransferTargetAccountTypesInParam(),
                newValidationUsingSingleStream.getInvalidTransferTargetAccountTypesInParam());
    }

    private ParameterService createMockParameterService(String... sourceTargetTypePairs) {
        ParameterService parameterService = EasyMock.createMock(ParameterService.class);
        List<String> sourceTargetTypes = Arrays.asList(sourceTargetTypePairs);
        
        EasyMock.expect(
                parameterService.getParameterValuesAsString(LaborConstants.LABOR_MODULE_CODE,
                        ParameterConstants.DOCUMENT_COMPONENT,LdParameterConstants.INVALID_TO_ACCOUNT_BY_FROM_ACCOUNT))
                .andStubReturn(sourceTargetTypes);
        
        EasyMock.replay(parameterService);
        return parameterService;
    }

}
