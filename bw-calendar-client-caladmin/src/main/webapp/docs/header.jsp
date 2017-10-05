<%@ page contentType="text/xml;charset=UTF-8" language="java" %>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>
<%
try {
%>

  <bean:define id="bwconfig" name="calForm" property="config" toScope="session" />
  <bean:define id="moduleState" name="bw_module_state" scope="request" />

  <now><%-- The actual date right "now" - this may not be the same as currentdate --%>
    <bw:emitText name="calForm" property="defaultTzid" />
    <bean:define id="fmtnow" name="calForm" property="today.formatted" />
    <date><bean:write name="fmtnow" property="date"/></date><%-- Value: YYYYMMDD --%>
  </now>

  <bean:define id="ctView" name="moduleState" property="curTimeView"/>
  <currentdate><%-- The current user-selected date --%>
    <date><bean:write name="ctView" property="curDayFmt.dateDigits"/></date><%--
      Value: yyyymmdd - date value --%>
    <longdate><bean:write name="ctView"
                          property="curDayFmt.fullDateString"/></longdate><%--
      Value (example): Wednesday, February 11, 2004 --%>
    <shortdate><bean:write name="ctView" property="curDayFmt.shortDateString"/></shortdate><%--
      Value (example): 2/8/04 - short representation of the date --%>
    <monthname><bean:write name="ctView" property="curDayFmt.monthName"/></monthname><%--
      Value (example): January - full month name --%>
  </currentdate>

  <%-- get the default number of days for event lists --%>
  <bean:define id="bwauthpars" name="calForm" property="authpars" toScope="session" />
  <defaultdays><bean:write name="bwauthpars" property="defaultWebCalPeriod"/></defaultdays>
  <maxdays><bean:write name="bwauthpars" property="maxWebCalPeriod"/></maxdays>

  <%-- messages --%>
  <logic:iterate id="msg" name="calForm" property="msg.msgList">
    <message>
      <id><bean:write name="msg" property="msgId" /></id>
      <logic:iterate id="param" name="msg" property="params" >
        <param><bean:write name="param" /></param>
      </logic:iterate>
    </message>
  </logic:iterate>

  <%-- errors --%>
  <logic:iterate id="errBean" name="calForm" property="err.msgList">
    <error>
      <id><bean:write name="errBean" property="msgId" /></id>
      <logic:iterate id="param" name="errBean" property="params" >
        <param><bean:write name="param" /></param>
      </logic:iterate>
    </error>
  </logic:iterate>

  <bean:define id="presentationState"
               name="bw_presentationstate" scope="request" />
  <bw:emitText name="presentationState" property="appRoot" tagName="appRoot" /><%--
        Value: URI - the location of web resources used by the code to find the
        XSLT files.  This element is defined prior to build in
        ../../../../clones/democal.properties
        as pubevents.app.root and personal.app.root. Note that references to
        html web resources such as images are set in the xsl stylesheets. --%>
  <bw:emitText name="presentationState" property="browserResourceRoot"/>
  <urlprefix><bean:write name="calForm" property="urlPrefix"/></urlprefix><%--
        Value: URI - this is prefix of the calendar application.
        e.g. http://localhost:8080/cal
        Use this value to prefix calls to the application actions in your XSLT.
        e.g. <a href="{$urlPrefix}/eventView.do?guid=...">View Event</a> --%>
  <urlpattern><genurl:rewrite action="DUMMYACTION.DO" /></urlpattern>

  <%-- URLs of other Bedework web clients --%>
  <personaluri><bean:write name="bwconfig" property="personalCalendarUri"/></personaluri>
  <publicuri><bean:write name="bwconfig" property="publicCalendarUri"/></publicuri>
  <adminuri><bean:write name="bwconfig" property="publicAdminUri"/></adminuri>

  <logic:equal name="calForm" property="suggestionEnabled" value="true" >
      <suggestionEnabled>true</suggestionEnabled>
  </logic:equal>
  <logic:notEqual name="calForm" property="suggestionEnabled" value="true" >
      <suggestionEnabled>false</suggestionEnabled>
  </logic:notEqual>

  <logic:equal name="calForm" property="workflowEnabled" value="true" >
      <workflowEnabled>true</workflowEnabled>
  </logic:equal>
  <logic:notEqual name="calForm" property="workflowEnabled" value="true" >
      <workflowEnabled>false</workflowEnabled>
  </logic:notEqual>

  <%-- Path to collections for public event submissions --%>
  <submissionsRoot>
    <encoded><bean:write name="calForm" property="encodedSubmissionRoot"/></encoded>
    <unencoded><bean:write name="bwconfig" property="submissionRoot"/></unencoded>
  </submissionsRoot>

  <%-- Path to collections for public event workflow --%>
  <workflowRoot>
    <encoded><bean:write name="calForm" property="encodedWorkflowRoot"/></encoded>
    <unencoded><bean:write name="calForm" property="workflowRoot"/></unencoded>
  </workflowRoot>

  <%-- Use URL prefixes when writing hyperlinks; these use the "genurl"
       struts tag to correctly build up application links within the
       container. "b=de" in the query string of each prefix has no meaning to
       the application and is not processed: it ensures that if we need to
       append the query string, we can always begin with an ampersand. --%>
  <urlPrefixes>
    <setup><bw:rewrite actionURL="true" page="/setup.do?b=de"/></setup>
    <initPendingTab><bw:rewrite actionURL="true" page="/main/initPendingTab.do?b=de"/></initPendingTab>
    <nextPendingTab><bw:rewrite actionURL="true" page="/main/nextPendingTab.do?b=de"/></nextPendingTab>
    <initApprovalQueueTab><bw:rewrite actionURL="true" page="/main/initApprovalQueueTab.do?b=de"/></initApprovalQueueTab>
    <nextApprovalQueueTab><bw:rewrite actionURL="true" page="/main/nextApprovalQueueTab.do?b=de"/></nextApprovalQueueTab>
    <initSuggestionQueueTab><bw:rewrite actionURL="true" page="/main/initSuggestionQueueTab.do?b=de"/></initSuggestionQueueTab>
    <nextSuggestionQueueTab><bw:rewrite actionURL="true" page="/main/nextSuggestionQueueTab.do?b=de"/></nextSuggestionQueueTab>
    <showCalsuiteTab><bw:rewrite renderURL="true" page="/main/showCalsuiteTab.rdo?b=de"/></showCalsuiteTab>
    <showUsersTab><bw:rewrite renderURL="true" page="/main/showUsersTab.rdo?b=de"/></showUsersTab>
    <showSystemTab><bw:rewrite renderURL="true" page="/main/showSystemTab.rdo?b=de"/></showSystemTab>
    <logout><bw:rewrite actionURL="true" page="/setup.do?logout=true"/></logout>
    <search>
      <unindex><bw:rewrite actionURL="true" page="/index/unindex.do?b=de"/></unindex>
      <search><bw:rewrite renderURL="true" page="/search/search.rdo?b=de"/></search>
      <next><bw:rewrite actionURL="true" page="/search/next.do?b=de"/></next>
    </search>
    <event>
      <showEvent><bw:rewrite renderURL="true" page="/event/showEvent.rdo?b=de"/></showEvent>
      <showModForm><bw:rewrite renderURL="true" page="/event/showModForm.rdo?b=de"/></showModForm>
      <showUpdateList><bw:rewrite renderURL="true" page="/event/showUpdateList.rdo?b=de"/></showUpdateList>
      <nextUpdateList><bw:rewrite actionURL="true" page="/event/nextUpdateList.do?b=de"/></nextUpdateList>
      <showDeleteConfirm><bw:rewrite renderURL="true" page="/event/showDeleteConfirm.rdo?b=de"/></showDeleteConfirm>
      <initAddEvent><bw:rewrite actionURL="true" page="/event/initAddEvent.do?b=de"/></initAddEvent>
      <initUpdateEvent><bw:rewrite actionURL="true" page="/event/initUpdateEvent.do?b=de"/></initUpdateEvent>
      <delete><bw:rewrite actionURL="true" page="/event/delete.do?b=de"/></delete>
      <deletePending><bw:rewrite actionURL="true" page="/event/deletePending.do?b=de"/></deletePending>
      <deleteApprovalQueue><bw:rewrite actionURL="true" page="/event/deleteApprovalQueue.do?b=de"/></deleteApprovalQueue>
      <fetchForDisplay><bw:rewrite actionURL="true" page="/event/fetchForDisplay.do?b=de"/></fetchForDisplay>
      <fetchForUpdate><bw:rewrite actionURL="true" page="/event/fetchForUpdate.do?b=de"/></fetchForUpdate>
      <fetchUpdateList><bw:rewrite actionURL="true" page="/event/fetchUpdateList.do?b=de"/></fetchUpdateList>
      <fetchForUpdatePending><bw:rewrite actionURL="true" page="/event/fetchForUpdatePending.do?b=de"/></fetchForUpdatePending>
      <fetchForUpdateApprovalQueue><bw:rewrite actionURL="true" page="/event/fetchForUpdateApprovalQueue.do?b=de"/></fetchForUpdateApprovalQueue>
      <fetchForUpdateSuggestionQueue><bw:rewrite actionURL="true" page="/event/fetchForUpdateSuggestionQueue.do?b=de"/></fetchForUpdateSuggestionQueue>
      <update><bw:rewrite actionURL="true" page="/event/update.do?b=de"/></update>
      <updatePending><bw:rewrite actionURL="true" page="/event/updatePending.do?b=de"/></updatePending>
      <updateApprovalQueue><bw:rewrite actionURL="true" page="/event/updateApprovalQueue.do?b=de"/></updateApprovalQueue>
      <selectCalForEvent><bw:rewrite actionURL="true" page="/event/selectCalForEvent.do?b=de"/></selectCalForEvent>
      <initUpload><bw:rewrite renderURL="true" page="/event/initUpload.rdo?b=de"/></initUpload>
      <upload><bw:rewrite actionURL="true" page="/event/upload.do?b=de"/></upload>
    </event>
    <suggest>
      <setStatus><bw:rewrite actionURL="true" page="/suggest/setStatus.gdo?b=de"/></setStatus>
      <setStatusForUpdate><bw:rewrite actionURL="true" page="/suggest/setStatusForUpdate.gdo?b=de"/></setStatusForUpdate>
    </suggest>
    <contact>
      <showContact><bw:rewrite actionURL="true" page="/contact/showContact.do?b=de"/></showContact>
      <showReferenced><bw:rewrite actionURL="true" page="/contact/showReferenced.do?b=de"/></showReferenced>
      <showModForm><bw:rewrite actionURL="true" page="/contact/showModForm.do?b=de"/></showModForm>
      <showUpdateList><bw:rewrite actionURL="true" page="/contact/showUpdateList.do?b=de"/></showUpdateList>
      <showDeleteConfirm><bw:rewrite actionURL="true" page="/contact/showDeleteConfirm.do?b=de"/></showDeleteConfirm>
      <initAdd><bw:rewrite actionURL="true" page="/contact/initAdd.do?b=de"/></initAdd>
      <initUpdate><bw:rewrite actionURL="true" page="/contact/initUpdate.do?b=de"/></initUpdate>
      <delete><bw:rewrite actionURL="true" page="/contact/delete.do?b=de"/></delete>
      <fetchForDisplay><bw:rewrite actionURL="true" page="/contact/fetchForDisplay.do?b=de"/></fetchForDisplay>
      <fetchForUpdate><bw:rewrite actionURL="true" page="/contact/fetchForUpdate.do?b=de"/></fetchForUpdate>
      <fetchUpdateList><bw:rewrite actionURL="true" page="/contact/fetchUpdateList.do?b=de"/></fetchUpdateList>
      <update><bw:rewrite actionURL="true" page="/contact/update.do?b=de"/></update>
    </contact>
    <location>
      <showLocation><bw:rewrite actionURL="true" page="/location/showLocation.do?b=de"/></showLocation>
      <showReferenced><bw:rewrite actionURL="true" page="/location/showReferenced.do?b=de"/></showReferenced>
      <showModForm><bw:rewrite actionURL="true" page="/location/showModForm.do?b=de"/></showModForm>
      <showUpdateList><bw:rewrite actionURL="true" page="/location/showUpdateList.do?b=de"/></showUpdateList>
      <showDeleteConfirm><bw:rewrite actionURL="true" page="/location/showDeleteConfirm.do?b=de"/></showDeleteConfirm>
      <initAdd><bw:rewrite actionURL="true" page="/location/initAdd.do?b=de"/></initAdd>
      <initUpdate><bw:rewrite actionURL="true" page="/location/initUpdate.do?b=de"/></initUpdate>
      <delete><bw:rewrite actionURL="true" page="/location/delete.do?b=de"/></delete>
      <fetchForDisplay><bw:rewrite actionURL="true" page="/location/fetchForDisplay.do?b=de"/></fetchForDisplay>
      <fetchForUpdate><bw:rewrite actionURL="true" page="/location/fetchForUpdate.do?b=de"/></fetchForUpdate>
      <fetchUpdateList><bw:rewrite actionURL="true" page="/location/fetchUpdateList.do?b=de"/></fetchUpdateList>
      <update><bw:rewrite actionURL="true" page="/location/update.do?b=de"/></update>
    </location>
    <category>
      <showReferenced><bw:rewrite actionURL="true" page="/category/showReferenced.do?b=de"/></showReferenced>
      <showModForm><bw:rewrite actionURL="true" page="/category/showModForm.do?b=de"/></showModForm>
      <showUpdateList><bw:rewrite actionURL="true" page="/category/showUpdateList.do?b=de"/></showUpdateList>
      <showDeleteConfirm><bw:rewrite actionURL="true" page="/category/showDeleteConfirm.do?b=de"/></showDeleteConfirm>
      <initAdd><bw:rewrite actionURL="true" page="/category/initAdd.do?b=de"/></initAdd>
      <initUpdate><bw:rewrite actionURL="true" page="/category/initUpdate.do?b=de"/></initUpdate>
      <delete><bw:rewrite actionURL="true" page="/category/delete.do?b=de"/></delete>
      <fetchForUpdate><bw:rewrite actionURL="true" page="/category/fetchForUpdate.do?b=de"/></fetchForUpdate>
      <fetchUpdateList><bw:rewrite actionURL="true" page="/category/fetchUpdateList.do?b=de"/></fetchUpdateList>
      <update><bw:rewrite actionURL="true" page="/category/update.do?b=de"/></update>
    </category>
    <calendar>
      <calOpenCloseMod><bw:rewrite actionURL="true" page="/calendar/openCloseMod.do?b=de"/></calOpenCloseMod>
      <calOpenCloseSelect><bw:rewrite actionURL="true" page="/calendar/openCloseSelect.do?b=de"/></calOpenCloseSelect>
      <calOpenCloseDisplay><bw:rewrite actionURL="true" page="/calendar/openCloseDisplay.do?b=de"/></calOpenCloseDisplay>
      <fetch><bw:rewrite renderURL="true" page="/calendar/showUpdateList.rdo?b=de"/></fetch>
      <fetchDescriptions><bw:rewrite renderURL="true" page="/calendar/showDescriptionList.rdo?b=de"/></fetchDescriptions>
      <initAdd><bw:rewrite actionURL="true" page="/calendar/initAdd.do?b=de"/></initAdd>
      <delete><bw:rewrite actionURL="true" page="/calendar/delete.do?b=de"/></delete>
      <fetchForDisplay><bw:rewrite actionURL="true" page="/calendar/fetchForDisplay.do?b=de"/></fetchForDisplay>
      <fetchForUpdate><bw:rewrite actionURL="true" page="/calendar/fetchForUpdate.do?b=de"/></fetchForUpdate>
      <update><bw:rewrite actionURL="true" page="/calendar/update.do?b=de"/></update>
      <calOpenCloseMove><bw:rewrite actionURL="true" page="/calendar/openCloseMove.do?b=de"/></calOpenCloseMove>
      <move><bw:rewrite renderURL="true" page="/calendar/showMoveForm.rdo?b=de"/></move>
    </calendar>
    <notifications>
      <remove><bw:rewrite actionURL="true" page="/notify/remove.do?b=de"/></remove>
      <removeTrans><bw:rewrite actionURL="true" page="/notify/removeTrans.do?b=de"/></removeTrans>
    </notifications>
    <subscriptions>
      <subOpenCloseMod><bw:rewrite actionURL="true" page="/subs/openCloseMod.do?b=de"/></subOpenCloseMod>
      <fetch><bw:rewrite renderURL="true" page="/subs/showSubs.rdo?b=de"/></fetch>
      <fetchForUpdate><bw:rewrite actionURL="true" page="/subs/fetchForUpdate.do?b=de"/></fetchForUpdate>
      <delete><bw:rewrite actionURL="true" page="/subs/delete.do?b=de"/></delete>
      <initAdd><bw:rewrite actionURL="true" page="/subs/initAdd.do?b=de"/></initAdd>
      <update><bw:rewrite actionURL="true" page="/subs/update.do?b=de"/></update>
    </subscriptions>
    <view>
      <fetch><bw:rewrite renderURL="true" page="/view/showViews.rdo?b=de"/></fetch>
      <fetchForUpdate><bw:rewrite actionURL="true" page="/view/fetchForUpdate.do?b=de"/></fetchForUpdate>
      <addView><bw:rewrite actionURL="true" page="/view/addView.do?b=de"/></addView>
      <update><bw:rewrite actionURL="true" page="/view/update.do?b=de"/></update>
      <remove><bw:rewrite actionURL="true" page="/view/removeView.do?b=de"/></remove>
    </view>
    <calsuite>
      <fetch><bw:rewrite renderURL="true" page="/calsuite/showCalSuites.rdo?b=de"/></fetch>
      <fetchForUpdate><bw:rewrite actionURL="true" page="/calsuite/fetchForUpdate.do?b=de"/></fetchForUpdate>
      <add><bw:rewrite actionURL="true" page="/calsuite/addCalSuite.do?b=de"/></add>
      <update><bw:rewrite actionURL="true" page="/calsuite/update.do?b=de"/></update>
      <showAddForm><bw:rewrite renderURL="true" page="/calsuite/showAddForm.rdo?b=de"/></showAddForm>
      <showModForm><bw:rewrite renderURL="true" page="/calsuite/showModForm.rdo?b=de"/></showModForm>
      <fetchPrefsForUpdate><bw:rewrite actionURL="true" page="/calsuite/fetchPrefsForUpdate.do?b=de"/></fetchPrefsForUpdate>
      <updatePrefs><bw:rewrite actionURL="true" page="/calsuite/updatePrefs.do?b=de"/></updatePrefs>
    </calsuite>
    <calsuiteresources>
      <fetch><bw:rewrite renderURL="true" page="/resource/showResources.rdo?b=de"/></fetch>
      <edit><bw:rewrite actionURL="true" page="/resource/editResource.rdo?b=de"/></edit>
      <add><bw:rewrite actionURL="true" page="/resource/addResource.do?b=de"/></add>
      <update><bw:rewrite actionURL="true" page="/resource/updateResource.do?b=de"/></update>
      <remove><bw:rewrite actionURL="true" page="/resource/removeResource.do?b=de"/></remove>
    </calsuiteresources>
    <globalresources>
      <fetch><bw:rewrite renderURL="true" page="/globalres/showResources.rdo?b=de"/></fetch>
      <edit><bw:rewrite actionURL="true" page="/globalres/editResource.rdo?b=de"/></edit>
      <add><bw:rewrite actionURL="true" page="/globalres/addResource.do?b=de"/></add>
      <update><bw:rewrite actionURL="true" page="/globalres/updateResource.do?b=de"/></update>
      <remove><bw:rewrite actionURL="true" page="/globalres/removeResource.do?b=de"/></remove>
    </globalresources>
    <system>
      <fetch><bw:rewrite actionURL="true" page="/syspars/fetch.do?b=de"/></fetch>
      <update><bw:rewrite actionURL="true" page="/syspars/update.do?b=de"/></update>
    </system>
    <stats>
      <update><bw:rewrite actionURL="true" page="/stats/update.do?b=de"/></update>
    </stats>
    <timezones>
      <showUpload><bw:rewrite renderURL="true" page="/timezones/showUpload.rdo?b=de"/></showUpload>
      <initUpload><bw:rewrite actionURL="true" page="/timezones/initUpload.do?b=de"/></initUpload>
      <upload><bw:rewrite actionURL="true" page="/timezones/upload.do?b=de"/></upload>
      <fix><bw:rewrite actionURL="true" page="/timezones/fix.do?b=de"/></fix>
    </timezones>
    <authuser>
      <showModForm><bw:rewrite actionURL="true" page="/authuser/showModForm.do?b=de"/></showModForm>
      <showUpdateList><bw:rewrite actionURL="true" page="/authuser/showUpdateList.do?b=de"/></showUpdateList>
      <getAuthUsers><bw:rewrite actionURL="true" page="/authuser/getAuthUsers.do?b=de"/></getAuthUsers>
      <initUpdate><bw:rewrite actionURL="true" page="/authuser/initUpdate.do?b=de"/></initUpdate>
      <fetchForUpdate><bw:rewrite actionURL="true" page="/authuser/fetchForUpdate.do?b=de"/></fetchForUpdate>
      <update><bw:rewrite actionURL="true" page="/authuser/update.do?b=de"/></update>
    </authuser>
    <prefs>
      <fetchForUpdate><bw:rewrite actionURL="true" page="/prefs/fetchForUpdate.do?b=de"/></fetchForUpdate>
      <update><bw:rewrite actionURL="true" page="/prefs/update.do?b=de"/></update>
    </prefs>
    <admingroup>
      <showModForm><bw:rewrite renderURL="true" page="/admingroup/showModForm.rdo?b=de"/></showModForm>
      <showModMembersForm><bw:rewrite renderURL="true" page="/admingroup/showModMembersForm.rdo?b=de"/></showModMembersForm>
      <showUpdateList><bw:rewrite renderURL="true" page="/admingroup/showUpdateList.rdo?b=de"/></showUpdateList>
      <showChooseGroup><bw:rewrite renderURL="true" page="/admingroup/showChooseGroup.rdo?b=de"/></showChooseGroup>
      <showDeleteConfirm><bw:rewrite renderURL="true" page="/admingroup/showDeleteConfirm.rdo?b=de"/></showDeleteConfirm>
      <initAdd><bw:rewrite actionURL="true" page="/admingroup/initAdd.do?b=de"/></initAdd>
      <initUpdate><bw:rewrite actionURL="true" page="/admingroup/initUpdate.do?b=de"/></initUpdate>
      <delete><bw:rewrite actionURL="true" page="/admingroup/delete.do?b=de"/></delete>
      <fetchForUpdate><bw:rewrite actionURL="true" page="/admingroup/fetchForUpdate.do?b=de"/></fetchForUpdate>
      <fetchForUpdateMembers><bw:rewrite actionURL="true" page="/admingroup/fetchForUpdateMembers.do?b=de"/></fetchForUpdateMembers>
      <fetchUpdateList><bw:rewrite actionURL="true" page="/admingroup/fetchUpdateList.do?b=de"/></fetchUpdateList>
      <update><bw:rewrite actionURL="true" page="/admingroup/update.do?b=de"/></update>
      <updateMembers><bw:rewrite actionURL="true" page="/admingroup/updateMembers.do?b=de"/></updateMembers>
      <switch><bw:rewrite actionURL="true" page="/admingroup/switch.do?b=de"/></switch>
    </admingroup>
    <filter>
      <showAddForm><bw:rewrite renderURL="true" page="/filter/getFilters.rdo?b=de"/></showAddForm>
      <add><bw:rewrite actionURL="true" page="/filter/add.do?b=de"/></add>
      <delete><bw:rewrite actionURL="true" page="/filter/delete.do?b=de"/></delete>
    </filter>
  </urlPrefixes>

  <bw:emitText name="calForm" property="calSuiteName" />

  <logic:present name="calForm" property="currentCalSuite" >
    <currentCalSuite>
      <bw:emitText name="calForm" property="currentCalSuite.name" tagName="name" />
      <bw:emitText name="calForm" property="currentCalSuite.group.account" tagName="group" />
      <bw:emitText name="calForm" property="currentCalSuite.group.principalRef" tagName="groupHref" />
      <bw:emitText name="calForm" property="currentCalSuite.resourcesHome" tagName="resourcesHome" />
      <bw:emitCurrentPrivs name="calForm" property="currentCalSuite.currentAccess" tagName="currentAccess"/>
    </currentCalSuite>
  </logic:present>

  <notifications>
    <logic:present name="calForm" property="notificationInfo" >
        <bean:define id="notificationInfo" name="calForm" property="notificationInfo" />
        <%@include file="/docs/notifications/notificationInfo.jsp"%>
    </logic:present>
  </notifications>

  <userInfo>
    <%-- user type --%>
    <bw:emitText name="calForm" property="curUserContentAdminUser"
                 tagName="contentAdminUser" />

    <bw:emitText name="calForm" property="curUserApproverUser"
                 tagName="approverUser" />

    <bw:emitText name="calForm" property="curUserSuperUser"
                 tagName="superUser" />

    <logic:equal name="calForm" property="userMaintOK" value="true" >
      <userMaintOK>true</userMaintOK>
    </logic:equal>
    <logic:notEqual name="calForm" property="userMaintOK" value="true" >
      <userMaintOK>false</userMaintOK>
    </logic:notEqual>

    <logic:equal name="calForm" property="adminGroupMaintOK" value="true">
      <adminGroupMaintOk>true</adminGroupMaintOk>
    </logic:equal>
    <logic:notEqual name="calForm" property="adminGroupMaintOK" value="true">
      <adminGroupMaintOk>false</adminGroupMaintOk>
    </logic:notEqual>

    <%-- user and group --%>
    <bw:emitText name="calForm" property="currentUser"/>
    <bw:emitText name="calForm" property="adminUserId" tagName="user"/>
    <logic:present name="calForm" property="adminGroupName" >
      <bw:emitText name="calForm" property="adminGroupName" tagName="group"/>
      <bw:emitText name="calForm" property="oneGroup"/>
    </logic:present>
    <groups>
      <logic:present name="bw_user_admin_search_groups" scope="session" >
        <logic:iterate id="adminGroup" name="bw_user_admin_search_groups" scope="session" >
          <group>
            <name><bean:write name="adminGroup" property="account" /></name>
            <href><bean:write name="adminGroup" property="principalRef" /></href>
            <ownerHref><bean:write name="adminGroup" property="ownerHref" /></ownerHref>
            <desc><bean:write name="adminGroup" property="description" /></desc>
          </group>
        </logic:iterate>
      </logic:present>
    </groups>
  </userInfo>

  <%-- System parameters and directory info--%>
  <syspars>
    <logic:present name="calForm" property="dirInfo" >
      <bean:define id="dir" name="calForm" property="dirInfo" />
      <bw:emitText name="dir" property="userPrincipalRoot" />
      <bw:emitText name="dir" property="groupPrincipalRoot" />
      <bw:emitText name="dir" property="ticketPrincipalRoot" />
      <bw:emitText name="dir" property="resourcePrincipalRoot" />
      <bw:emitText name="dir" property="hostPrincipalRoot" />
      <bw:emitText name="dir" property="venuePrincipalRoot" />
    </logic:present>
  </syspars>

  <logic:present name="calForm" property="imageUploadDirectory" >
    <bw:emitText name="calForm" property="imageUploadDirectory" />
  </logic:present>

  <logic:present name="bw_feature_flags" scope="session" >
      <featureFlags><bean:write name="bw_feature_flags" scope="session" /></featureFlags>
  </logic:present>

  <logic:iterate id="appvar" name="calForm" property="appVars">
    <appvar><%--
        Application variables can be set arbitrarily by the stylesheet designer.
        Use an "appvar" by adding setappvar=key(value) to the query string of
        a URL.  This feature is useful for setting up state during a user's session.
        e.g. <a href="{$urlPrefix}/eventView.do?guid=...&setappvar=currentTab(event)">View Event</a>
        To change the value of an appvar, call the same key with a different value.
        e.g. <a href="{$urlPrefix}/setup.do?setappvar=currentTab(home)">Return Home</a>
        If appvars exist, they will be output in the following form:  --%>
      <key><bean:write name="appvar" property="key" /></key>
      <value><bean:write name="appvar" property="value" /></value>
    </appvar>
  </logic:iterate>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>
