<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<%@include file="/docs/header.jsp"%>

<page>modSyspars</page>
<tab>system</tab>

<system>
  <bw:emitText name="bwsyspars" property="name"/>
  <bw:emitText name="bwsyspars" property="tzid"/>
  <bw:emitText name="bwsyspars" property="defaultUserHour24"/>
  <bw:emitText name="bwsyspars" property="systemid"/>

  <bw:emitText name="bwsyspars" property="defaultFBPeriod"/>
  <bw:emitText name="bwsyspars" property="maxFBPeriod"/>
  <bw:emitText name="bwsyspars" property="defaultWebCalPeriod"/>
  <bw:emitText name="bwsyspars" property="maxWebCalPeriod"/>

  <bw:emitText name="bwsyspars" property="publicCalendarRoot"/>
  <bw:emitText name="bwsyspars" property="userCalendarRoot"/>
  <bw:emitText name="bwsyspars" property="userDefaultCalendar"/>
  <bw:emitText name="bwsyspars" property="defaultTrashCalendar"/>
  <bw:emitText name="bwsyspars" property="userInbox"/>
  <bw:emitText name="bwsyspars" property="userOutbox"/>
  <bw:emitText name="bwsyspars" property="deletedCalendar"/>
  <bw:emitText name="bwsyspars" property="busyCalendar"/>

  <bw:emitText name="bwsyspars" property="defaultUserViewName"/>
  <bw:emitText name="bwsyspars" property="publicUser"/>

  <bw:emitText name="bwsyspars" property="httpConnectionsPerUser"/>
  <bw:emitText name="bwsyspars" property="httpConnectionsPerHost"/>
  <bw:emitText name="bwsyspars" property="httpConnections"/>

  <bw:emitText name="bwsyspars" property="maxPublicDescriptionLength"/>
  <bw:emitText name="bwsyspars" property="maxUserDescriptionLength"/>
  <bw:emitText name="bwsyspars" property="maxUserEntitySize"/>
  <bw:emitText name="bwsyspars" property="defaultUserQuota"/>

  <bw:emitText name="bwsyspars" property="maxInstances"/>
  <bw:emitText name="bwsyspars" property="maxYears"/>
  <bw:emitText name="bwsyspars" property="maxAttendees"/>

  <bw:emitText name="bwsyspars" property="userauthClass"/>
  <bw:emitText name="bwsyspars" property="mailerClass"/>
  <bw:emitText name="bwsyspars" property="admingroupsClass"/>
  <bw:emitText name="bwsyspars" property="usergroupsClass"/>

  <bw:emitText name="bwsyspars" property="directoryBrowsingDisallowed"/>

  <bw:emitText name="bwsyspars" property="eventregAdminToken"/>

  <bw:emitText name="bwsyspars" property="globalResourcesPath"/>
  
  <bw:emitText name="bwsyspars" property="indexRoot"/>
  <bw:emitText name="bwsyspars" property="useSolr"/>
  <bw:emitText name="bwsyspars" property="solrURL"/>
  <bw:emitText name="bwsyspars" property="solrCoreAdmin"/>
  <bw:emitText name="bwsyspars" property="solrDefaultCore"/>
  
  <bw:emitText name="bwsyspars" property="defaultChangesNotifications"/>

  <bw:emitText name="bwsyspars" property="localeList"/>

  <bw:emitText name="bwsyspars" property="rootUsers"/>
  <subcontexts>
    <logic:iterate id="context" name="bwsyspars" property="contexts">
    <subcontext>
      <bw:emitText name="context" property="contextName"/>
      <bw:emitText name="context" property="calSuite"/>
      <bw:emitText name="context" property="defaultContext"/>
    </subcontext>
    </logic:iterate>
  </subcontexts>
</system>

<timezones>
  <logic:iterate id="tz" name="calForm" property="timeZoneNames">
    <timezone>
      <name><bean:write name="tz" property="name" filter="true"/></name>
      <id><bean:write name="tz" property="id" filter="true"/></id>
    </timezone>
  </logic:iterate>
</timezones>

<views>
  <logic:iterate name="calForm" property="views" id="view">
    <view>
      <name><bean:write name="view" property="name" /></name>
    </view>
  </logic:iterate>
</views>

<%@include file="/docs/footer.jsp"%>
