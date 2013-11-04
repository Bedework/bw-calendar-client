<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>


<%
try {
%>

<bean:define id="event" name="calForm" property="event"/>
<formElements>
  <subscriptionId></subscriptionId>
  <guid><bean:write name="event" property="uid"/></guid>
  <recurrenceId><bean:write name="event" property="recurrenceId"/></recurrenceId>

  <genurl:form action="event/editEvent">
    <bw:emitText name="event" property="scheduleMethod"
                 tagName="scheduleMethod" />
    <bw:emitText name="event" property="entityType"
                 tagName="entityType"/>

    <title>
      <html:text name="calForm" property="summary"/>
    </title>
    <!-- current event's calendar -->
    <bw:emitContainer name="calForm" property="event"
                      indent="    " tagName="calendar" />
    <!-- user's writable calendars -->
    <calendars>
      <bean:define id="addContentCalendarCollections"
                   name="bw_addcontent_collection_list" scope="session" />
      <html:select name="calForm"
                   property="calendarId">
        <html:optionsCollection name="addContentCalendarCollections"
                                label="path"
                                value="path"/>
      </html:select>
    </calendars>
    <allDay><html:checkbox property="eventStartDate.dateOnly"/></allDay>
    <storeUTC><html:checkbox property="eventStartDate.storeUTC"/></storeUTC>
    <floating><html:checkbox property="eventStartDate.floating"/></floating>
    <start>
      <rfc3339DateTime><bean:write name="calForm" property="eventStartDate.rfc3339DateTime"/></rfc3339DateTime>
      <temphour><bean:write name="calForm" property="eventStartDate.hour"/></temphour>
      <month>
        <html:select property="eventStartDate.month">
          <html:options labelProperty="eventStartDate.monthLabels"
                        property="eventStartDate.monthVals"/>
        </html:select>
      </month>
      <day>
        <html:select property="eventStartDate.day">
          <html:options labelProperty="eventStartDate.dayLabels"
                        property="eventStartDate.dayVals"/>
        </html:select>
      </day>
      <year>
        <html:select property="eventStartDate.year">
          <html:options property="yearVals"/>
        </html:select>
      </year>
      <yearText>
        <html:text property="eventStartDate.year" size="4"/>
      </yearText>
      <hour>
        <html:select property="eventStartDate.hour">
          <html:options labelProperty="eventStartDate.hourLabels"
                        property="eventStartDate.hourVals"/>
        </html:select>
      </hour>
      <minute>
        <html:select property="eventStartDate.minute">
          <html:options labelProperty="eventStartDate.minuteLabels"
                        property="eventStartDate.minuteVals"/>
        </html:select>
      </minute>
      <logic:notEqual name="calForm" property="hour24" value="true" >
        <ampm>
          <html:select property="eventStartDate.ampm">
            <html:options property="eventStartDate.ampmLabels"/>
          </html:select>
        </ampm>
      </logic:notEqual>
      <bw:emitText name="calForm" property="eventStartDate.tzid" tagName="tzid"/>
    </start>
    <end>
      <rfc3339DateTime><bean:write name="calForm" property="eventEndDate.rfc3339DateTime"/></rfc3339DateTime>
      <type><bean:write name="calForm" property="eventEndType"/></type>
      <dateTime>
        <month>
          <html:select property="eventEndDate.month">
              <html:options labelProperty="eventEndDate.monthLabels"
                            property="eventEndDate.monthVals"/>
          </html:select>
        </month>
        <day>
          <html:select property="eventEndDate.day">
            <html:options labelProperty="eventEndDate.dayLabels"
                          property="eventEndDate.dayVals"/>
          </html:select>
        </day>
        <year>
          <html:select property="eventEndDate.year">
            <html:options property="yearVals"/>
          </html:select>
          </year>
        <yearText>
          <html:text property="eventEndDate.year" size="4"/>
        </yearText>
        <hour>
          <html:select property="eventEndDate.hour">
            <html:options labelProperty="eventEndDate.hourLabels"
                          property="eventEndDate.hourVals"/>
          </html:select>
        </hour>
        <minute>
          <html:select property="eventEndDate.minute">
            <html:options labelProperty="eventEndDate.minuteLabels"
                          property="eventEndDate.minuteVals"/>
          </html:select>
        </minute>
        <ampm>
          <logic:notEqual name="calForm" property="hour24" value="true" >
            <html:select property="eventEndDate.ampm">
              <html:options property="eventEndDate.ampmLabels"/>
            </html:select>
          </logic:notEqual>
        </ampm>
        <bw:emitText name="calForm" property="eventEndDate.tzid" tagName="tzid"/>
      </dateTime>
      <duration>
        <days><html:text property="eventDuration.daysStr" size="2" /></days>
        <hours><html:text property="eventDuration.hoursStr" size="2" /></hours>
        <minutes><html:text property="eventDuration.minutesStr" size="2" /></minutes>
        <weeks><html:text property="eventDuration.weeksStr" size="2" /></weeks>
      </duration>
    </end>
    <desc><html:textarea property="description"></html:textarea></desc>
    <status><bean:write name="event" property="status"/></status>
    <transparency><bean:write name="event" property="transparency"/></transparency>
    <link><html:text name="event" property="link"/></link>
    <bean:define id="locations"
                 name="bw_locations_list" scope="session" />
    <location>
      <locationmenu>
        <html:select property="locationUid">
          <html:optionsCollection name="locations"
                                  label="address.value"
                                  value="uid"/>
        </html:select>
      </locationmenu>
      <locationtext><html:text property="locationAddress.value" /></locationtext>
    </location>

    <categories>
      <all>
        <logic:iterate id="category" name="bw_categories_list" scope="session">
          <category>
            <bw:emitText name="category" property="word.value" tagName="value" />
            <bw:emitText name="category" property="uid" tagName="uid" />
          </category>
        </logic:iterate>
      </all>
      <current>
        <logic:present name="event" property="categories">
          <logic:iterate id="category" name="event" property="categories">
            <category>
              <bw:emitText name="category" property="word.value" tagName="value" />
              <bw:emitText name="category" property="uid" tagName="uid" />
            </category>
          </logic:iterate>
        </logic:present>
      </current>
    </categories>

    <logic:present  name="event" property="percentComplete">
      <bw:emitText name="event" property="percentComplete"/>
    </logic:present>

    <%--<bw:emitText name="event" property="trashable"/>--%>
    <bw:emitText name="event" property="recurringEntity"/>

    <%@ include file="/docs/event/emitRecur.jsp" %>
    <%@ include file="/docs/schedule/emitEventProperties.jsp" %>

    <logic:present name="event" property="xproperties">
      <xproperties>
        <logic:iterate id="xprop" name="event" property="xproperties">
          <logic:equal name="xprop" property="skipJsp" value="false">
            <bean:define id="xpropName" name="xprop" property="name"/>
            <% String xpropStart = "<" + (String)xpropName + ">";
               String xpropEnd = "</" + (String)xpropName + ">";%>
            <%=xpropStart%>
              <logic:present name="xprop" property="parameters">
                <parameters>
                <logic:iterate id="xpar" name="xprop" property="parameters">
                  <bean:define id="xparName" name="xpar" property="name"/>
                  <% String xparStart = "<" + (String)xparName + ">";
                     String xparEnd = "</" + (String)xparName + ">";%>
                  <%=xparStart%><![CDATA[<bean:write name="xpar" property="value" />]]><%=xparEnd%>
                </logic:iterate>
                </parameters>
              </logic:present>
              <values>
                <text><![CDATA[<bean:write name="xprop" property="value"/>]]></text>
              </values>
            <%=xpropEnd%>
          </logic:equal>
        </logic:iterate>
      </xproperties>
    </logic:present>

  </genurl:form>
</formElements>

<editableAccess>
  <logic:present name="calForm" property="curEventFmt">
    <bean:define id="eventFormatter" name="calForm" property="curEventFmt"/>
    <bw:emitText name="eventFormatter" property="xmlAccess" tagName="access"
                 filter="no"/>
  </logic:present>
</editableAccess>

<%-- Output of timezones is deprecated; now call the timezone server --%>
<%--
<timezones>
  <logic:iterate id="tz" name="calForm" property="timeZoneNames">
    <timezone>
      <name><bean:write name="tz" property="name" filter="true"/></name>
      <id><bean:write name="tz" property="id" filter="true"/></id>
    </timezone>
  </logic:iterate>
</timezones>
--%>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

