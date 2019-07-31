package edu.cornell.kfs.sys.cynergykimfeed.dataaccess.impl;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;

import cynergy.CynergyKimFeed;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.cynergykimfeed.CynergyKimFeedConstants;
import edu.cornell.kfs.sys.cynergykimfeed.businessobject.EdwPerson;
import edu.cornell.kfs.sys.cynergykimfeed.dataaccess.EdwPersonDao;

public class EdwPersonDaoImpl extends PlatformAwareDaoBaseJdbc implements EdwPersonDao {
    private static final Logger LOG = LogManager.getLogger(EdwPersonDaoImpl.class);
    
    protected ParameterService parameterService;
    
    public EdwPersonDaoImpl() {
        super();
    }

    @Override
    public Collection<EdwPerson> getEdwPersons(boolean full) {
        StringBuilder sqlBuilder = new StringBuilder();
        String commaSpace = KFSConstants.COMMA + StringUtils.SPACE;
        sqlBuilder.append("Select ").append(CynergyKimFeedConstants.EdwPerson.Fields.CU_PERSON_SID).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.EMPLID).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.NETID).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.NATIONAL_ID).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.ACADEMIC).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.STAFF).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.FACULTY).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.STUDENT).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.ALUMNI).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.AFFILIATE).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.EXCEPTION).append(" as ").append(CynergyKimFeedConstants.EdwPerson.Fields.EXCEPTION_AS);
        sqlBuilder.append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.PRIMARY_AFFILIATION).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.CU_SUPPRESS_ADDR).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.LDAP_SUPPRESS).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.NAME).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.NAME_PREFIX).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.NAME_SUFFIX).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.LAST_NAME).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.FIRST_NAME).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.MIDDLE_NAME).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.PREF_NAME).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.HOME_ADDRESS1).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.HOME_ADDRESS2).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.HOME_ADDRESS3).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.HOME_CITY).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.HOME_STATE).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.HOME_POSTAL).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.HOME_COUNTRY).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.HOME_PHONE).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.EMAIL_ADDRESS).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.CAMPUS_ADDRESS).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.CAMPUS_CITY).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.CAMPUS_STATE).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.CAMPUS_POSTAL).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.CAMPUS_PHONE).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.PRIMARY_JOBCODE).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.PRIMARY_DEPTID).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.PRIMARY_UNITID).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.PRIMARY_ORG_CODE).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.PRIMARY_EMPL_STATUS).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.ACTIVE).append(commaSpace);
        sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Fields.LDAP_SUPPRESS);
        
        sqlBuilder.append(" from ");
        if (full) {
            sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Tables.CU_PERSON_DATA);
        } else {
            sqlBuilder.append(CynergyKimFeedConstants.EdwPerson.Tables.CU_PERSON_DATA_KFS_DELTA);
        }
        
        String and = " and ";
        
        sqlBuilder.append(" where ").append(CynergyKimFeedConstants.EdwPerson.Fields.NETID).append(" != ' '");
        sqlBuilder.append(and).append(CynergyKimFeedConstants.EdwPerson.Fields.CU_PERSON_SID).append(" = ").append(CynergyKimFeedConstants.EdwPerson.Fields.CU_MASTER_PERSON_SID);
        
        if (!full) {
            sqlBuilder.append(and).append(CynergyKimFeedConstants.EdwPerson.Fields.PROCESS_FLAG).append(" not in ('S', 'D'");
        }
        
        /*
        
        if (!full) {
            getSQL += "AND PROCESS_FLAG != 'S' AND PROCESS_FLAG != 'D'";
            
            if (shouldOnlyUseDeltaLoadWithHighestSequenceNumber()) {
                LOG.info("getEDWData: Preparing to get EDW data for only the most recent load, regardless of whether it has already been read");
                getSQL += " AND LOAD_SEQ = (SELECT MAX(LOAD_SEQ) from EDW.CU_PERSON_DATA_KFS_DELTA_MSTR)";
            } else if (shouldOnlyUseDeltaLoadWithSpecificDate()) {
                LOG.info("getEDWData: Preparing to get EDW data for the load with this date, regardless of whether it has already been read: "
                        + getDeltaLoadDate());
                getSQL += " AND LOAD_SEQ in (SELECT LOAD_SEQ from EDW.CU_PERSON_DATA_KFS_DELTA_MSTR WHERE FILE_DATE BETWEEN ? AND ?)";
                DateTime deltaLoadDate = getParsedDeltaLoadDate();
                queryArgs = new Object[] { getTimestampForStartOfDay(deltaLoadDate), getTimestampForEndOfDay(deltaLoadDate) };
            } else {
                LOG.info("getEDWData: Preparing to get EDW data for only the loads that have not been read yet");
                getSQL += " AND LOAD_SEQ in (SELECT LOAD_SEQ from EDW.CU_PERSON_DATA_KFS_DELTA_MSTR WHERE READ_BY_KFS = 'N')";
            }
            getSQL += " ORDER BY LOAD_SEQ, EMPLID";
        }
        
        LOG.info("getEDWData, the generated SQL: " + getSQL);
        if (queryArgs != null) {
            for (Object arg : queryArgs) {
                LOG.info("getEDWData, query argument: " + arg);
            }
        }
        
        //Get data from the EDW
        LOG.info("getEDWData: getting edw data");
        return serverTemplate.queryForRowSet(getSQL, queryArgs); 
         * 
         */
        
        // TODO Auto-generated method stub
        return null;
    }
    
    private boolean shouldOnlyUseDeltaLoadWithHighestSequenceNumber() {
        String deltasToLoad = parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.KFS, KfsParameterConstants.BATCH_COMPONENT,
                CUKFSParameterKeyConstants.KIM_FEED_DELTAS_TO_LOAD);
        boolean loadWIthHighest = false;
        if (StringUtils.equals(CUKFSConstants.KimFeedConstants.ALL_UNPROCESSED_DELTAS_MODE, deltasToLoad)) {
    loadWIthHighest = true;
    
}            
        
        LOG.info("shouldOnlyUseDeltaLoadWithHighestSequenceNumber, deltasToLoad paraemter value: '" + deltasToLoad + "' and returning " + loadWIthHighest);
        return loadWIthHighest;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
