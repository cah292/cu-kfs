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
    private LaborExpenseTransferAccountTypesValidation newValidation;

    @Before
    public void setUp() throws Exception {
        oldValidation = new LaborExpenseTransferAccountTypesValidation();
        newValidation = new LaborExpenseTransferAccountTypesValidation();
    }

    @Test
    public void testSingletonParameter() throws Exception {
        assertAccountTypeSetupsAreIdentical("key1=value1");
    }

    @Test
    public void testMultiValueParameter() throws Exception {
        assertAccountTypeSetupsAreIdentical("key1=value1", "key2=value2", "key1=value1b", "key3=value3", "key3=value3b");
    }

    @Test
    public void testEmptyParameter() throws Exception {
        assertAccountTypeSetupsAreIdentical();
    }

    private void assertAccountTypeSetupsAreIdentical(String... sourceTargetTypePairs) {
        ParameterService mockParameterService = createMockParameterService(sourceTargetTypePairs);
        
        oldValidation.setParameterService(mockParameterService);
        newValidation.setParameterService(mockParameterService);
        
        oldValidation.setInvalidTransferAccountTypesMap();
        newValidation.setInvalidTransferAccountTypesMap_Collectors();
        
        assertEquals("Account types maps should have matched",
                oldValidation.getInvalidTransferAccountTypesMap(), newValidation.getInvalidTransferAccountTypesMap());
        assertEquals("Account types sets should have matched",
                oldValidation.getInvalidTransferTargetAccountTypesInParam(), newValidation.getInvalidTransferTargetAccountTypesInParam());
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
