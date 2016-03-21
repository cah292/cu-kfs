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
package com.rsmart.kuali.kfs.prje.dataaccess.impl;

import java.util.Collection;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import com.rsmart.kuali.kfs.prje.businessobject.PRJESet;
import com.rsmart.kuali.kfs.prje.businessobject.PRJEType;
import com.rsmart.kuali.kfs.prje.dataaccess.PRJEDataAccess;

public class PRJEDataAccessImpl extends PlatformAwareDaoBaseOjb implements PRJEDataAccess {
    public static final String ACTV_CD = "active";
    public static final String PRJE_TYPE_ID = "typeId";
    public static final String PRJE_SET_ID = "setId";
    
    @SuppressWarnings("unchecked")
    public Collection<PRJEType> getPRJETypes(PRJESet set, Boolean active) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(PRJE_SET_ID, set.getSetId());        
        if ( active ) 
            criteria.addEqualTo(ACTV_CD, active);
        QueryByCriteria qbc = new QueryByCriteria(PRJEType.class, criteria);
        return getPersistenceBrokerTemplate().getCollectionByQuery(qbc);
    }
    
    public PRJEType getPRJEType(Integer id) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(PRJE_TYPE_ID, id);
        QueryByCriteria qbc = new QueryByCriteria(PRJEType.class, criteria);        
        return (PRJEType) getPersistenceBrokerTemplate().getObjectByQuery(qbc);
    }
    
    @SuppressWarnings("unchecked")
    public Collection<PRJESet> getPRJESets(Boolean active) {
        Criteria criteria = new Criteria();
        if ( active ) 
            criteria.addEqualTo(ACTV_CD, active);
        QueryByCriteria qbc = new QueryByCriteria(PRJESet.class, criteria);
        return getPersistenceBrokerTemplate().getCollectionByQuery(qbc);
    }
    
    public PRJESet getPRJESet(Integer id) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(PRJE_SET_ID, id);
        QueryByCriteria qbc = new QueryByCriteria(PRJESet.class, criteria);        
        return (PRJESet) getPersistenceBrokerTemplate().getObjectByQuery(qbc);
    }
}
