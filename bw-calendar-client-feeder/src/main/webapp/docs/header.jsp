<%@ page contentType="text/xml;charset=UTF-8" language="java" %>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<%
try {
%>

  <bean:define id="bwconfig" name="calForm" property="config" toScope="session" />
  <bean:define id="moduleState" name="bw_module_state" scope="request" />

  <now><%-- The actual date right "now" - this may not be the same as currentdate --%>
    <bean:define id="fmtnow" name="calForm" property="today.formatted" />
    <date><bean:write name="fmtnow" property="date"/></date><%--
      Value: YYYYMMDD --%>
    <longdate><bean:write name="fmtnow" property="longDateString"/></longdate><%--
      Value (example): February 8, 2004 - long representation of the date --%>
    <shortdate><bean:write name="fmtnow" property="dateString"/></shortdate><%--
      Value (example): 2/8/04 - short representation of the date --%>
    <time><bean:write name="fmtnow" property="timeString"/></time><%--
      Value (example): 10:15 PM --%>
    <twodigithour24><bean:write name="fmtnow" property="twoDigitHour24"/></twodigithour24>
    <utc><bean:write name="calForm" property="today.utcDate" /></utc>
    <bw:emitText name="calForm" property="defaultTzid" />
  </now>
  <bean:define id="ctView" name="moduleState" property="curTimeView"/>
  <periodname><bean:write name="ctView" property="periodName"/></periodname><%--
    Values: Day, Week, Month, Year - The current time period name.   --%>
  <bw:emitText name="calForm" property="hour24" /><%--
    Values: true, false - Flag if we are using 24 hour time --%>

  <publicview><bean:write name="calForm" property="publicView" /></publicview><%--
    Values: true, false - Flag if we are in the guest (public) view  --%>
  <guest><bean:write name="calForm" property="guest" /></guest><%--
    Value: true, false - Flag if we are a guest --%>
  <logic:equal name="calForm" property="guest" value="false">
    <userid><bean:write name="calForm" property="currentUser" /></userid><%--
      Value: string - Userid of non-guest user --%>
      <logic:iterate id="group" name="calForm" property="currentGroups" >
        <memberOf><bean:write name="group" property="principalRef" /></memberOf>
      </logic:iterate>
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

  <urlPrefixes>
    <%-- Use URL prefixes when writing hyperlinks; these use the "genurl"
       struts tag to correctly build up application links within the
       container. "b=de" in the query string of each prefix has no meaning to
       the application and is not processed: it ensures that if we need to
       append the query string, we can always begin with an ampersand. --%>

    <%-- Public and personal client URLs --%>
    <setup><bw:rewrite actionURL="true" page="/setup.do?b=de"/></setup>

    <main>
      <initialise><genurl:rewrite forward="initialise"/></initialise>
      <eventsFeed><bw:rewrite actionURL="true" page="/main/eventsFeed.do?b=de"/></eventsFeed>
      <setSelection><bw:rewrite actionURL="true" page="/main/setSelection.do?b=de"/></setSelection>
      <setSelectionList><bw:rewrite actionURL="true" page="/main/setSelectionList.do?b=de"/></setSelectionList>
      <setMainEventList><bw:rewrite actionURL="true" page="/main/setMainEventList.do?b=de"/></setMainEventList>
      <nextMainEventList><bw:rewrite actionURL="true" page="/main/nextMainEventList.do?b=de"/></nextMainEventList>
      <setOngoingList><bw:rewrite actionURL="true" page="/main/setOngoingList.do?b=de"/></setOngoingList>
      <setViewPeriod><bw:rewrite actionURL="true" page="/main/setViewPeriod.do?b=de"/></setViewPeriod>
      <listEvents><bw:rewrite actionURL="true" page="/main/listEvents.do?b=de"/></listEvents>
      <showPage><bw:rewrite actionURL="true" page="/main/showPage.do?b=de"/></showPage>
    </main>

    <event>
      <eventMore><genurl:rewrite forward="eventMore"/></eventMore>
      <eventView><bw:rewrite actionURL="true" page="/event/eventView.do?b=de"/></eventView>
      <addEventRef><bw:rewrite actionURL="true" page="/event/addEventRef.do?b=de"/></addEventRef>
    </event>

    <calendar>
      <fetchPublicCalendars><bw:rewrite actionURL="true" page="/calendar/fetchPublicCalendars.do?b=de"/></fetchPublicCalendars>
      <fetchCalendars><bw:rewrite actionURL="true" page="/calendar/fetchCalendars.do?b=de"/></fetchCalendars>
      <fetchForExport><bw:rewrite actionURL="true" page="/calendar/fetchForExport.do?b=de"/></fetchForExport>
    </calendar>

    <search>
      <search><bw:rewrite renderURL="true" page="/search/search.rdo?b=de"/></search>
      <next><bw:rewrite actionURL="true" page="/search/next.do?b=de"/></next>
    </search>

    <misc>
      <export><bw:rewrite resourceURL="true" page="/misc/export.gdo?b=de"/></export>
      <showPage><bw:rewrite renderURL="true" page="/misc/showPage.rdo?b=de"/></showPage>
      <async><bw:rewrite renderURL="true" page="/misc/async.do?b=de"/></async>
    </misc>

    <mail>
      <mailEvent><bw:rewrite actionURL="true" page="/mail/mailEvent.do?b=de"/></mailEvent>
    </mail>

    <stats>
      <stats><bw:rewrite actionURL="true" page="/stats/stats.do?b=de"/></stats>
    </stats>

    <%-- The following URLs are used only in the personal client --%>
    <%-- ======================================================= --%>
    <logic:equal name="calForm" property="guest" value="false">
      <event>
        <initEvent><bw:rewrite actionURL="true" page="/event/initEvent.do?b=de"/></initEvent>
        <addEvent><bw:rewrite actionURL="true" page="/event/addEvent.do?b=de"/></addEvent>
        <attendeesForEvent><bw:rewrite actionURL="true" page="/event/attendeesForEvent.do?b=de"/></attendeesForEvent>
        <showAttendeesForEvent><bw:rewrite renderURL="true" page="/event/showAttendeesForEvent.rdo?b=de"/></showAttendeesForEvent>
        <initMeeting><bw:rewrite actionURL="true" page="/event/initMeeting.do?b=de"/></initMeeting>
        <editEvent><bw:rewrite actionURL="true" page="/event/editEvent.do?b=de"/></editEvent>
        <gotoEditEvent><bw:rewrite actionURL="true" page="/event/gotoEditEvent.do?b=de"/></gotoEditEvent>
        <delInboxEvent><bw:rewrite actionURL="true" page="/event/delInboxEvent.do?b=de"/></delInboxEvent>
        <showAccess><bw:rewrite renderURL="true" page="/event/showAccess.rdo?b=de"/></showAccess>
        <addEventRefComplete><bw:rewrite actionURL="true" page="/event/addEventRefComplete.do?b=de"/></addEventRefComplete>
      </event>

      <freeBusy>
        <fetch><bw:rewrite actionURL="true" page="/freeBusy/getFreeBusy.do?b=de"/></fetch>
      </freeBusy>

      <calendar>
        <fetchDescriptions><bw:rewrite renderURL="true" page="/calendar/showDescriptionList.rdo?b=de"/></fetchDescriptions>
        <initAdd><bw:rewrite actionURL="true" page="/calendar/initAdd.do?b=de"/></initAdd>
        <fetchForDisplay><bw:rewrite actionURL="true" page="/calendar/fetchForDisplay.do?b=de"/></fetchForDisplay>
        <listForExport><bw:rewrite renderURL="true" page="/calendar/listForExport.rdo?b=de"/></listForExport>
        <setPropsInGrid><bw:rewrite actionURL="true" page="/calendar/setPropsInGrid.do?b=de"/></setPropsInGrid>
        <setPropsInList><bw:rewrite actionURL="true" page="/calendar/setPropsInList.do?b=de"/></setPropsInList>
      </calendar>
    </logic:equal>
  </urlPrefixes>

  <confirmationid><bean:write name="calForm" property="confirmationId"/></confirmationid><%--
        Value: String - a 16 character random string used to allow users to confirm
        additions to their private calendar.  DEPRECATED. --%>

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

  <selectionState><%--
    What type of information have we selected to display?  Used to
    branch between different templates in the xsl based on user selections. --%>
    <selectionType><bean:write name="moduleState" property="selectionType"/></selectionType><%--
        Value: view,search,collections,filter
        Used to branch into different presentation depending on the type of
        output we expect --%>
    <filter></filter> <%-- unimplemented --%>
  </selectionState>

  <%-- System parameters --%>
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
<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

