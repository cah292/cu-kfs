/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.prje.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import com.rsmart.kuali.kfs.prje.PRJEConstants;
import com.rsmart.kuali.kfs.prje.dataaccess.PRJEDataAccess;
import com.rsmart.kuali.kfs.prje.dataaccess.impl.PRJEDataAccessImpl;

public class PRJEServiceBase implements PRJEConstants {
    private static Logger LOG = Logger.getLogger(PRJEServiceBase.class);
    
    static final String FILENAME = "gl_entry_prje";
    static final String DATA_EXT = GeneralLedgerConstants.BatchFileSystem.EXTENSION;
    static final String DONE_EXT = GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION;
    static final String PROP_EXT = ".properties";
    static final String DATA_FILE = FILENAME + DATA_EXT;
    static final String DONE_FILE = FILENAME + DONE_EXT;
    static final String PROP_FILE = FILENAME + PROP_EXT;
    
    private PRJEDataAccess prjeDataAccess;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;
    private ObjectCodeService objectCodeService;
    private DateTimeService dateTimeService;
    private UniversityDateService universityDateService;  
    private String batchFileDirectoryName;
    private String propertyFileDirectoryName;
    
    public PRJEDataAccess getPrjeDataAccess() {
        if ( prjeDataAccess != null )
            return prjeDataAccess;
        else
            return new PRJEDataAccessImpl();
    }
    
    public Properties getProperties() {
        File propDir = new File(getPropertyFileDirectoryName());
        if ( propDir.exists() && propDir.isDirectory() ) {
            File propFile = new File(propDir, PROP_FILE);
            if ( propFile.exists() && propFile.isFile() ) {
                try {
                    Properties properties = new Properties();
                    properties.load(new FileInputStream(propFile));
                    return properties;
                }
                catch ( IOException e ) {
                    throw new RuntimeException(e);
                }
            }
            else 
                LOG.error(propFile+" is not a regular file");
        }
        else 
            LOG.error(propDir+" is not a regular directory");

        return new Properties();
    }
    
    public void setPrjeDataAccess(PRJEDataAccess prjeDataAccess) {
        this.prjeDataAccess = prjeDataAccess;
    }

    public BusinessObjectService getBusinessObjectService() {
        if ( businessObjectService != null )
            return businessObjectService;
        else
            return SpringContext.getBean(BusinessObjectService.class);
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
    
    public ParameterService getParameterService() {
        if ( parameterService != null )
            return parameterService;
        else
            return SpringContext.getBean(ParameterService.class);
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public ObjectCodeService getObjectCodeService() {
        if ( objectCodeService != null )
            return objectCodeService;
        else
            return SpringContext.getBean(ObjectCodeService.class); 
    }

    public void setObjectCodeService(ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }

    public DateTimeService getDateTimeService() {
        if ( dateTimeService != null )
            return dateTimeService;
        else
            return SpringContext.getBean(DateTimeService.class);
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }   
 
    public UniversityDateService getUniversityDateService() {
        if ( universityDateService != null )
            return universityDateService;
        else
            return SpringContext.getBean(UniversityDateService.class);
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public String getBatchFileDirectoryName() {
        return batchFileDirectoryName;
    }

    public void setBatchFileDirectoryName(String batchFileDirectoryName) {
        this.batchFileDirectoryName = batchFileDirectoryName;
    }

    public String getPropertyFileDirectoryName() {
        return propertyFileDirectoryName;
    }

    public void setPropertyFileDirectoryName(String propertyFileDirectoryName) {
        this.propertyFileDirectoryName = propertyFileDirectoryName;
    }
}
