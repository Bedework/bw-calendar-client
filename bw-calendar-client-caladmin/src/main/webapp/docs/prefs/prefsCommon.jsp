<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<bean:define id="userPrefs" name="calForm" property="userPreferences"/>
<prefs>
  <user><bean:write name="userPrefs" property="ownerHref"/></user>
  <!-- name of default view (collection of subscriptions) that will appear upon login -->
  <preferredView><bean:write name="userPrefs" property="preferredView"/></preferredView>
  <!-- default mode of view:
       daily - a list of events grouped by date showing entire view period
       list - a list of discrete events from now into the future
       grid - calendar grid - in week and month view periods
  -->
  <defaultViewMode><bean:write name="userPrefs" property="defaultViewMode"/></defaultViewMode>
  <!-- default period that will appear upon login (day, week, month, year, today) -->
  <preferredViewPeriod><bean:write name="userPrefs" property="preferredViewPeriod"/></preferredViewPeriod>
  <!-- whether user will use 12 (am/pm) or 24 hour mode when entering events -->
  <bw:emitText name="userPrefs" property="hour24"/>
  <bw:emitText name="userPrefs" property="noNotifications"/>
  <skinName><bean:write name="userPrefs" property="skinName"/></skinName>
  <skinStyle><bean:write name="userPrefs" property="skinStyle"/></skinStyle>
  <!-- pref end type = date or duration -->
  <bw:emitText name="userPrefs" property="preferredEndType"/>
  <defaultImageDirectory><bean:write name="userPrefs" property="defaultImageDirectory"/></defaultImageDirectory>
  <maxEntitySize><bean:write name="userPrefs" property="maxEntitySize"/></maxEntitySize>

  <bw:emitText name="userPrefs" property="pageSize"/>
  <bw:emitText name="userPrefs" property="adminResourcesDirectory"/>
  <bw:emitText name="userPrefs" property="suiteResourcesDirectory"/>
  <bw:emitText name="userPrefs" property="calsuiteApprovers"/>
  <approvers>
    <logic:iterate id="approver" name="userPrefs" property="calsuiteApproversList">
      <bw:emitText name="approver"/>
    </logic:iterate>
  </approvers>
</prefs>

<categories>
  <all>
    <logic:iterate id="category" name="bw_categories_list" scope="session">
      <%@include file="/docs/category/emitCategory.jsp"%>
    </logic:iterate>
  </all>
  <current>
    <logic:present name="bw_default_categories_list" scope="session">
      <logic:iterate id="category" name="bw_default_categories_list"
                     scope="session">
        <%@include file="/docs/category/emitCategory.jsp"%>
      </logic:iterate>
    </logic:present>
  </current>
</categories>


