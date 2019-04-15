/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.rest.resource.businessobject;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.kfs.kns.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.kns.datadictionary.control.MultiselectControlDefinition;
import org.kuali.kfs.kns.service.BusinessObjectDictionaryService;
import org.kuali.kfs.kns.service.BusinessObjectMetaDataService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.bo.DataObjectRelationship;
import org.kuali.kfs.krad.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.krad.datadictionary.LookupAttributeDefinition;
import org.kuali.kfs.krad.datadictionary.RelationshipDefinition;
import org.kuali.kfs.krad.datadictionary.control.ControlDefinition;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.keyvalues.HierarchicalControlValuesFinder;
import org.kuali.kfs.krad.keyvalues.HierarchicalData;
import org.kuali.kfs.krad.keyvalues.KeyValuesFinder;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.LookupSearchService;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.permission.PermissionService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LookupResource {

    private static final Log LOG = LogFactory.getLog(LookupResource.class);

    private BusinessObjectDictionaryService businessObjectDictionaryService;
    private BusinessObjectMetaDataService businessObjectMetaDataService;
    private PermissionService permissionService;
    private PersistenceStructureService persistenceStructureService;

    private HttpServletRequest servletRequest;
    private Gson gson = new Gson();
    private BusinessObjectEntry businessObjectEntry;

    LookupResource(HttpServletRequest servletRequest, BusinessObjectEntry businessObjectEntry) {
        this.servletRequest = servletRequest;

        if (businessObjectEntry == null) {
            throw new NotFoundException();
        }

        this.businessObjectEntry = businessObjectEntry;
    }

    @GET
    public Response getLookupForm() {
        Class classForType = businessObjectEntry.getBusinessObjectClass();
        if (!isAuthorizedForLookup(classForType)) {
            Person user = KRADUtils.getUserSessionFromRequest(this.servletRequest).getPerson();
            AuthorizationException authorizationException = new AuthorizationException(user.getPrincipalName(),
                    "lookup", classForType.getName());
            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.FORBIDDEN);
            responseBuilder.entity(authorizationException);
            throw new ForbiddenException(responseBuilder.build());
        }

        List<LookupAttributeDefinition> lookupAttributeDefns = getLookupAttributeDefinitionsForClass(classForType);

        for (LookupAttributeDefinition lookupAttributeDefn : lookupAttributeDefns) {
            setNestedLookupFields(lookupAttributeDefn, classForType);
        }

        String title = businessObjectDictionaryService.getLookupTitle(classForType);
        if (StringUtils.isEmpty(title)) {
            title = businessObjectEntry.getObjectLabel() + " Lookup";
        }
        LookupSearchService searchService = getBusinessObjectDictionaryService().getLookupSearchServiceForLookup(classForType);
        if (searchService == null) {
            LOG.error(businessObjectEntry.getName() + " seems to be missing a LookupSearchService! A lookup cannot " +
                    "be queried without a LookupSearchService.");
            throw new InternalServerErrorException(gson.toJson("The requested lookup is currently unavailable."));
        }

        Map<String, Object> resultsList = new LinkedHashMap<>();
        resultsList.put("fields", searchService.getSearchResultsAttributes(classForType));
        resultsList.put("defaultSortFields", businessObjectDictionaryService.getLookupDefaultSortFieldNames(classForType));

        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("title", title);
        if (shouldCreateNewUrlBeIncluded(classForType)) {
            responseData.put("create", getCreateBlock(classForType));
        }
        responseData.put("form", lookupAttributeDefns);
        responseData.put("results", resultsList);

        String json = gson.toJson(responseData);
        return Response.ok(json).build();
    }

    @GET
    @Path("values")
    public Response getLookupControlValues() {
        Map<String, Object> controlValuesMap = buildLookupControlValuesMap(businessObjectEntry);
        return Response.ok(gson.toJson(controlValuesMap)).build();
    }

    @GET
    @Path("values/{attrDefnName}")
    public Response getLookupControlValues(@PathParam("attrDefnName") String attrDefnName) {
        Map<String, Object> controlValuesMap = buildLookupControlValuesMap(businessObjectEntry);
        Object value = controlValuesMap.get(attrDefnName);
        if (value == null) {
            if (attrDefnName != null && !doesAttrDefnWithGivenNameExistForClass(businessObjectEntry, attrDefnName)) {
                throw new NotFoundException(gson.toJson("Could not find the " + attrDefnName + " attribute for the "
                                + businessObjectEntry.getName() + " business object."));
            }
            value = Collections.emptyList();
        }
        String json = gson.toJson(value);
        return Response.ok(json).build();
    }

    protected void setNestedLookupFields(LookupAttributeDefinition lookupAttributeDefn, Class boClass) {
        String attributeName = lookupAttributeDefn.getName();

        boolean disableLookup = lookupAttributeDefn.getDisableLookup();

        DataObjectRelationship relationship;

        if (!disableLookup) {
            relationship = getBusinessObjectMetaDataService().getBusinessObjectRelationship(null, boClass, attributeName, "", false);

            if (relationship == null) {
                Class c = ObjectUtils.getPropertyType(businessObjectEntry, lookupAttributeDefn.getName(), getPersistenceStructureService());

                if (c != null) {
                    if (lookupAttributeDefn.getName().contains(".")) {
                        attributeName = StringUtils.substringBeforeLast(attributeName, ".");
                    }

                    RelationshipDefinition ddReference = getBusinessObjectMetaDataService().getBusinessObjectRelationshipDefinition(boClass, attributeName);
                    relationship = getBusinessObjectMetaDataService().getBusinessObjectRelationship(ddReference, null, boClass, attributeName, "", false);
                }
            }

            if (relationship != null) {
                lookupAttributeDefn.setCanLookup(true);
                String lookupClassName = relationship.getRelatedClass().getSimpleName();
                lookupAttributeDefn.setLookupClassName(lookupClassName);
                String lookupChildAttribute = relationship.getChildAttributeForParentAttribute(attributeName);
                lookupAttributeDefn.setLookupChildAttribute(lookupChildAttribute);
            }
        }
    }

    private boolean doesAttrDefnWithGivenNameExistForClass(BusinessObjectEntry businessObjectEntry, String attrDefnName) {
        Class boClass = businessObjectEntry.getBusinessObjectClass();
        List<LookupAttributeDefinition> attributeDefinitions = getLookupAttributeDefinitionsForClass(boClass);
        for (LookupAttributeDefinition attributeDefn : attributeDefinitions) {
            if (attributeDefn.getName().equalsIgnoreCase(attrDefnName)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> buildLookupControlValuesMap(BusinessObjectEntry businessObjectEntry) {
        Class classForType = businessObjectEntry.getBusinessObjectClass();
        if (!isAuthorizedForLookup(classForType)) {
            throw new ForbiddenException();
        }

        Map<String, Object> valuesMap = new LinkedHashMap<>();
        List<LookupAttributeDefinition> attributeDefns = getLookupAttributeDefinitionsForClass(classForType);
        for (LookupAttributeDefinition attributeDefn: attributeDefns) {
            ControlDefinition controlDefn = attributeDefn.getControl();
            if (controlDefn != null) {
                String singleAttributeDefnName = attributeDefn.getName();
                if (isHierarchical(controlDefn)) {
                    MultiselectControlDefinition multiselectControlDefn = (MultiselectControlDefinition) controlDefn;
                    HierarchicalControlValuesFinder valuesFinder = multiselectControlDefn
                            .getHierarchicalControlValuesFinder();
                    List<HierarchicalData> values = valuesFinder.getHierarchicalControlValues();
                    valuesMap.put(singleAttributeDefnName, values);
                } else {
                    KeyValuesFinder valuesFinder = controlDefn.getValuesFinder();
                    if (valuesFinder != null) {
                        List<KeyValue> keyValues = valuesFinder.getKeyValues();
                        valuesMap.put(singleAttributeDefnName, keyValues);
                    }
                }
            }
        }
        return valuesMap;
    }

    private Map<String, String> getCreateBlock(Class classForType) {
        String url = getCreateNewUrl(classForType);
        Map<String, String> createBlock = new LinkedHashMap<>();
        createBlock.put("url", "kr/" + url);
        createBlock.put("label", "Create New");
        return createBlock;
    }

    private String getCreateNewUrl(Class<? extends BusinessObjectBase> classForType) {
        Properties parameters = new Properties();
        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.MAINTENANCE_NEW_METHOD_TO_CALL);
        parameters.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, classForType.getName());
        return UrlFactory.parameterizeUrl(KRADConstants.MAINTENANCE_ACTION, parameters);
    }

    private boolean shouldCreateNewUrlBeIncluded(Class<? extends BusinessObjectBase> classForType) {
        BusinessObjectAdminService adminService = getBusinessObjectDictionaryService().getBusinessObjectAdminService(
                classForType);
        if (adminService == null) {
            LOG.debug(classForType.getSimpleName() + "doesn't have a BusinessObjectAdminService!");
            return false;
        }

        Person person = KRADUtils.getUserSessionFromRequest(this.servletRequest).getPerson();
        return adminService.allowsNew(classForType, person) && adminService.allowsCreate(classForType, person);
    }

    private boolean isHierarchical(ControlDefinition controlDefn) {
        return controlDefn instanceof MultiselectControlDefinition &&
                ((MultiselectControlDefinition) controlDefn).isHierarchical();
    }

    protected List<LookupAttributeDefinition> getLookupAttributeDefinitionsForClass(Class classForType) {
        return getBusinessObjectDictionaryService().getLookupAttributeDefinitions(classForType);
    }

    private boolean isAuthorizedForLookup(Class boClass) {
        return getPermissionService().isAuthorizedByTemplate(getPrincipalId(), KRADConstants.KNS_NAMESPACE,
                KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS,
                KRADUtils.getNamespaceAndComponentSimpleName(boClass), Collections.emptyMap());
    }

    private String getPrincipalId() {
        return KRADUtils.getPrincipalIdFromRequest(servletRequest);
    }

    private BusinessObjectDictionaryService getBusinessObjectDictionaryService() {
        if (businessObjectDictionaryService == null) {
            businessObjectDictionaryService = SpringContext.getBean(BusinessObjectDictionaryService.class);
        }
        return businessObjectDictionaryService;
    }

    protected void setBusinessObjectDictionaryService(
            BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    private BusinessObjectMetaDataService getBusinessObjectMetaDataService() {
        if (businessObjectMetaDataService == null) {
            businessObjectMetaDataService = KNSServiceLocator.getBusinessObjectMetaDataService();
        }
        return businessObjectMetaDataService;
    }

    protected void setBusinessObjectMetaDataService(BusinessObjectMetaDataService businessObjectMetaDataService) {
        this.businessObjectMetaDataService = businessObjectMetaDataService;
    }

    private PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = SpringContext.getBean(PermissionService.class);
        }
        return permissionService;
    }

    protected void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    private PersistenceStructureService getPersistenceStructureService() {
        if (persistenceStructureService == null) {
            persistenceStructureService = KRADServiceLocator.getPersistenceStructureService();
        }
        return persistenceStructureService;
    }

    protected void setPersistenceStructureService(PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }
}
