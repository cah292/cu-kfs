<%--
 Copyright 2007 The Kuali Foundation
 
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl2.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false"
    headerTitle="Batch File Set Upload" docTitle="" renderMultipart="true"
    transactionalDocument="false" htmlFormAction="disencumbranceBatchUploadFileSet" errorKey="foo">
    <html:hidden property="batchUpload.batchInputTypeName" />
    
    <c:set var="batchUploadAttributes" value="${DataDictionary.BatchUpload.attributes}" />

    <strong><h2>    
      <bean:message key="${KualiForm.titleKey}"/> <a href="${ConfigProperties.externalizable.help.url}default.htm?turl=WordDocuments%2Fbatch.htm" tabindex="${KualiForm.nextArbitrarilyHighIndex}" target="helpWindow"  title="[Help]Upload">
                                            <img src="${ConfigProperties.kr.externalizable.images.url}my_cp_inf.gif" title="[Help] Upload" alt="[Help] Upload" hspace=5 border=0  align="middle"></a>
      </h2></strong>
    </br>
    
    <table width="100%" border="0"><tr><td> 
      <kul:errors keyMatch="*" errorTitle="Errors Found On Page:"/>
    </td></tr></table>  
    </br>
        
    <kul:tabTop tabTitle="Manage Batch Files" defaultOpen="true">
      <div class="tab-container" align="center">
          <h3>Add Batch File Set</h3>
          <table width="100%" summary="" cellpadding="0" cellspacing="0">
            <tr>
              <th width="120">&nbsp;</th>
              <th> <div align="left">Browse File</div></th>
              <th> <div align="left"><label for="batchUpload.fileUserIdentifer">File Set Identifier</label></div></th>
              <th width="150"> <div align="center">Actions</div></th>
            </tr>
            
            <c:forEach items="${KualiForm.batchInputFileSetType.fileTypes}" var="fileType" varStatus="loopStatus">
              <tr>
                <th scope="row"><div align="right">add <c:out value="${KualiForm.batchInputFileSetType.fileTypeDescription[fileType]}"/>:</div></th>
                <td class="infoline">
                <c:if test="${fileType eq 'DATA' }">
                    <kul:htmlControlAttribute 
                        attributeEntry="${batchUploadAttributes.fileUserIdentifer}" 
                        property="selectedDataFile" readOnly="true" tabindexOverride="0"/>&nbsp;

                        <kul:lookup boClassName="edu.cornell.kfs.module.ld.businessobject.LaborLedgerBatchFile"
                                    fieldConversions="fileName:selectedDataFile"
                                    lookupParameters="'*.data':fileName"
                                    autoSearch="yes"/>
                   
                </c:if>
                <c:if test="${fileType eq 'RECON' }">
                    <kul:htmlControlAttribute 
                        attributeEntry="${batchUploadAttributes.fileUserIdentifer}" 
                        property="selectedReconFile" readOnly="true" tabindexOverride="0"/>&nbsp;

                        <kul:lookup boClassName="edu.cornell.kfs.module.ld.businessobject.LaborLedgerBatchFile"
                                    fieldConversions="fileName:selectedReconFile"
                                    lookupParameters="'*.recon':fileName"
                                    autoSearch="yes"/>
                   
                </c:if>
                  <span class="fineprint"></span>
                </td>
                <td class="infoline">
                  <c:if test="${loopStatus.first}">
                    <div align="left">
                      <kul:htmlControlAttribute attributeEntry="${batchUploadAttributes.fileUserIdentifer}" property="batchUpload.fileUserIdentifer"/>
                    </div>
                  </c:if>
                  <span class="fineprint">&nbsp;</span>
                </td>
                <td class="infoline"><div align="center">
                  <c:if test="${loopStatus.first}">
                    <html:image src="${ConfigProperties.kr.externalizable.images.url}tinybutton-add1.gif" styleClass="globalbuttons" property="methodToCall.save" title="Upload Batch File" alt="Upload Batch File" />
                  </c:if>
                  &nbsp;
                </td>
              </tr>
            </c:forEach>
         </table>
      </div>
    </kul:tabTop>

</kul:page>