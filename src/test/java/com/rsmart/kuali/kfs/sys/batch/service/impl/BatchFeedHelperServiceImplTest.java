package com.rsmart.kuali.kfs.sys.batch.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.kuali.rice.core.api.config.property.ConfigurationService;

public class BatchFeedHelperServiceImplTest {

    private enum ConfigProperty {
        CONFIG1("key1", "value1");
        
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
