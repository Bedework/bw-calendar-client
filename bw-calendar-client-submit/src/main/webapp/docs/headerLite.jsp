<%@ page contentType="text/xml;charset=UTF-8" language="java" %>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<%
try {
%>

<bedework>
  <publicview><bean:write name="calForm" property="publicView" /></publicview><%--
    Values: true, false - Flag if we are in the guest (public) view  --%>
  <guest><bean:write name="calForm" property="guest" /></guest><%--
    Value: true, false - Flag if we are a guest --%>
  <logic:equal name="calForm" property="guest" value="false">
    <userid><bean:write name="calForm" property="currentUser" /></userid><%--
      Value: string - Userid of non-guest user --%>
  </logic:equal>

  <logic:iterate id="msg" name="calForm" property="msg.msgList">
    <message>
      <id><bean:write name="msg" property="msgId" /></id>
      <logic:iterate id="param" name="msg" property="params" >
        <param><bean:write name="param" /></param>
      </logic:iterate>
    </message>
  </logic:iterate>

  <logic:iterate id="errBean" name="calForm" property="err.msgList">
    <error>
      <id><bean:write name="errBean" property="msgId" /></id>
      <logic:iterate id="param" name="errBean" property="params" >
        <param><bean:write name="param" /></param>
      </logic:iterate>
    </error>
  </logic:iterate>

  <urlPrefixes>
    <%--urlPrefixes are used to generate appropriately encoded urls for
        calls into the application; these are required for use within portals
        and are generated in header.jsp. --%>

    <%-- render urls --%>
    <initialise><genurl:rewrite forward="initialise"/></initialise>
    <eventMore><genurl:rewrite forward="eventMore"/></eventMore>
    <initUpload><genurl:rewrite forward="initUpload"/></initUpload>

    <%-- action urls --%>
    <setup><genurl:rewrite action="setup.do?b=de"/></setup>
    <setSelection><genurl:rewrite action="setSelection.do?b=de"/></setSelection>
    <setViewPeriod><genurl:rewrite action="setViewPeriod.do?b=de"/></setViewPeriod>
    <eventView><genurl:rewrite action="eventView.do?b=de"/></eventView>
    <mailEvent><genurl:rewrite action="mailEvent.do?b=de"/></mailEvent>
    <showPage><genurl:rewrite action="showPage.do?b=de"/></showPage>

    <export><genurl:link page="/export.gdo?b=de"/></export>
    <stats><genurl:rewrite action="stats.do?be=d"/></stats>

    <fetchPublicCalendars><genurl:rewrite action="fetchPublicCalendars"/></fetchPublicCalendars>
    <fetchCalendars><genurl:rewrite action="fetchCalendars"/></fetchCalendars>

    <!-- The following URLs are used only in the personal client -->
    <logic:equal name="calForm" property="guest" value="false">
      <initEvent><genurl:rewrite action="initEvent.do?b=de"/></initEvent>
      <addEventUsingPage><genurl:rewrite action="addEventUsingPage.do?b=de"/></addEventUsingPage>
      <editEvent><genurl:rewrite action="editEvent.do?b=de"/></editEvent>
      <delEvent><genurl:rewrite action="delEvent.do?b=de"/></delEvent>
      <event>
        <setAccess><genurl:link page="/event/setAccess.do?b=de"/></setAccess>
        <addEventRefComplete><genurl:link page="/event/addEventRefComplete.do?b=de"/></addEventRefComplete>
        <selectCalForEvent><genurl:link page="/event/selectCalForEvent.do?b=de"/></selectCalForEvent>
      </event>

      <freeBusy>
        <fetch><genurl:link page="/freeBusy/getFreeBusy.do?b=de"/></fetch>
        <setAccess><genurl:link page="/freeBusy/setAccess.do?b=de"/></setAccess>
      </freeBusy>

      <calendar>
        <fetch><genurl:link page="/calendar/showUpdateList.rdo?b=de"/></fetch><!-- keep -->
        <fetchDescriptions><genurl:link page="/calendar/showDescriptionList.rdo?b=de"/></fetchDescriptions><!-- keep -->
        <initAdd><genurl:link page="/calendar/initAdd.do?b=de"/></initAdd><!-- keep -->
        <delete><genurl:link page="/calendar/delete.do?b=de"/></delete>
        <fetchForDisplay><genurl:link page="/calendar/fetchForDisplay.do?b=de"/></fetchForDisplay>
        <fetchForUpdate><genurl:link page="/calendar/fetchForUpdate.do?b=de"/></fetchForUpdate><!-- keep -->
        <update><genurl:link page="/calendar/update.do?b=de"/></update><!-- keep -->
        <setAccess><genurl:link page="/calendar/setAccess.do?b=de"/></setAccess>
      </calendar>

      <subscriptions> <!-- only those listed are used here (no need to clean up) -->
        <fetch><genurl:link page="/subs/fetch.do?b=de"/></fetch>
        <fetchForUpdate><genurl:link page="/subs/fetchForUpdate.do?b=de"/></fetchForUpdate>
        <addSubByUri><genurl:link page="/subs/showAddByUriForm.rdo?b=de"/></addSubByUri>
        <subscribeByUri><genurl:link page="/subs/subscribeByUri.do?b=de"/></subscribeByUri>
        <initAdd><genurl:link page="/subs/initAdd.do?b=de"/></initAdd>
        <subscribe><genurl:link page="/subs/subscribe.do?b=de"/></subscribe>
      </subscriptions>

      <prefs>
        <fetchForUpdate><genurl:link page="/prefs/fetchForUpdate.do?b=de"/></fetchForUpdate>
        <update><genurl:link page="/prefs/update.do?b=de"/></update>
      </prefs>

      <initEventAlarm><genurl:rewrite action="initEventAlarm.do?b=de"/></initEventAlarm>
      <setAlarm><genurl:rewrite action="setAlarm.do?b=de"/></setAlarm>
      <addEventRef><genurl:rewrite action="addEventRef.do?b=de"/></addEventRef>
      <upload><genurl:rewrite action="upload.do?b=de"/></upload>
    </logic:equal>
  </urlPrefixes>
  <confirmationid><bean:write name="calForm" property="confirmationId"/></confirmationid><%--
        Value: String - a 16 character random string used to allow users to confirm
        additions to thier private calendar --%>
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

      <logic:equal name="appvar" property="key" value="summaryMode"><%--
        This is a special use of the appvar feature.  Normally, we don't return
        all details about events except when we display a single event (to keep the
        XML lighter).  To return all event details in an events listing, append a
        query string with setappvar=summaryMode(details).  Turn the detailed view
        off with setappvar=summaryMode(summary).--%>
        <logic:equal name="appvar" property="value" value="details">
          <bean:define id="detailView" value="true" toScope="request"/><%--
            Send this bean to the request scope so we can test for it on the page
            that builds the calendar tree (main.jsp) --%>
        </logic:equal>
      </logic:equal>
    </appvar>
  </logic:iterate>

  <logic:present name="bw_feature_flags" scope="session" >
    <featureFlags><bean:write name="bw_feature_flags" scope="session" /></featureFlags>
  </logic:present>


<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

