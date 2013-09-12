<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>

<creating><bean:write name="calForm" property="addingEvent"/></creating>
<bw:emitText name="calForm" property="hour24" /><%--
    Values: true, false - Flag if we are using 24 hour time --%>

<%-- formElements sections take advantage of Struts' form processing features. --%>
<formElements>
  <guid><bean:write name="calForm" property="event.uid"/></guid>
  <recurrenceId><bean:write name="calForm" property="event.recurrenceId"/></recurrenceId>

  <logic:equal name="calForm" property="addingEvent" value="false">
    <bw:emitText name="calForm" property="event.creatorHref" tagName="creator"/>
    <bw:emitText name="calForm" property="event.ownerHref" tagName="owner"/>
    <bw:emitText name="calForm" property="event.name" tagName="name"/>
  </logic:equal>
  <bw:emitText name="calForm" property="syspars.eventregAdminToken"/>

  <genurl:form action="event/update" >
    <title><html:text property="summary" size="40" styleId="iTitle" styleClass="edit"/></title>
    <calendar>
      <logic:present name="calForm" property="preferredCalendars">
        <%-- all publishing calendars a user has previously used. --%>
        <preferred>
          <html:select property="prefCalendarId">
              <html:optionsCollection property="preferredCalendars"
                                        label="path"
                                        value="path"/>
          </html:select>
        </preferred>
      </logic:present>
      <%-- all publishing calendars to which user has write access;
           in single calendar model, there will be only one. --%>
      <all>
        <html:select property="calendarId">
          <html:optionsCollection property="addContentCalendarCollections"
                                      label="path"
                                      value="path"/>
        </html:select>
      </all>
      <%-- Output the event's calendar information --%>
      <bw:emitContainer name="calForm" property="event"
                        indent="        "/>
    </calendar>
    <allDay><html:checkbox property="eventStartDate.dateOnly"/></allDay>
    <storeUTC><html:checkbox property="eventStartDate.storeUTC"/></storeUTC>
    <floating><html:checkbox property="eventStartDate.floating"/></floating>
    <start>
      <rfc3339DateTime><bean:write name="calForm" property="eventStartDate.rfc3339DateTime"/></rfc3339DateTime>
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
    <desc><html:textarea property="description" rows="8" cols="55" styleId="iDesc" styleClass="edit"></html:textarea></desc>
    <descLength><bean:write name="calForm" property="maxDescriptionLength" /></descLength>
    <status><bean:write name="calForm" property="event.status"/></status>
    <cost><html:text property="event.cost" size="30" styleId="iCost" styleClass="edit"/></cost>
    <link><html:text property="event.link" size="30" styleId="iLink" styleClass="edit"/></link>
    <location>
      <logic:present name="calForm" property="preferredLocations">
        <preferred>
          <html:select property="prefLocationId">
            <html:optionsCollection property="preferredLocations"
                                    label="address.value"
                                    value="uid"/>
          </html:select>
        </preferred>
      </logic:present>
      <all>
        <html:select property="allLocationId">
          <html:optionsCollection property="locations"
                                    label="address.value"
                                    value="uid"/>
          </html:select>
      </all>
      <logic:equal name="bwconfig" property="autoCreateLocations"
                   value="true">
        <address>
          <html:text size="30" value="" property="location.address.value" styleId="iLocation" styleClass="edit"/>
        </address>
        <link>
          <html:text property="location.link" size="30" styleId="iLocLink" styleClass="edit"/>
        </link>
      </logic:equal>
    </location>

    <categories>
      <logic:present name="calForm" property="preferredCategories">
        <preferred>
          <logic:iterate id="category" name="calForm" property="preferredCategories">
            <%@include file="/docs/category/emitCategory.jsp"%>
          </logic:iterate>
        </preferred>
      </logic:present>
      <all>
        <logic:iterate id="category" name="bw_categories_list"
                       scope="session">
          <%@include file="/docs/category/emitCategory.jsp"%>
        </logic:iterate>
      </all>
      <current>
        <logic:present name="calForm" property="event.categories">
          <logic:iterate id="category" name="calForm" property="event.categories">
            <%@include file="/docs/category/emitCategory.jsp"%>
          </logic:iterate>
        </logic:present>
      </current>
    </categories>

    <!-- Subscriptions are calendar aliases (and folders) associated with
         calendar suites.  Administrators set the collection of subscriptions in which
         they'd like the event to appear based on their access to a calendar suite.
         The back end then tags the events with categories to assure that the events
         appear under a given subscription.  -->
    <subscriptions>
      <!-- need to iterate over all calsuites to which a user has access.
           For now, we'll return just the current one to get things going. -->
      <calsuite>
        <bw:emitText name="calForm" property="currentCalSuite.name" tagName="name" />
        <calendars>
          <logic:present name="calForm" property="userCalendars">
            <bean:define id="calendar" name="calForm" property="userCalendars"
                       toScope="session" />
            <bean:define id="fullTree" toScope="request">true</bean:define>
            <bean:define id="stopDescentAtAliases" toScope="request">false</bean:define>
            <%@include file="/docs/calendar/emitCalendar.jsp"%>
          </logic:present>
        </calendars>
      </calsuite>
    </subscriptions>

    <%--<bw:emitText name="calForm" property="event.trashable" tagName="trashable"/>--%>
    <bw:emitText name="calForm" property="event.recurringEntity" tagName="recurringEntity"/>

    <%@ include file="/docs/event/emitRecur.jsp" %>

    <contact>
      <logic:present name="calForm" property="preferredContacts">
        <preferred>
          <html:select property="prefContactId">
            <html:optionsCollection property="preferredContacts"
                                    label="name.value"
                                    value="uid"/>
          </html:select>
        </preferred>
      </logic:present>
      <all>
        <html:select property="allContactId">
          <html:optionsCollection property="contacts"
                                    label="name.value"
                                    value="uid"/>
        </html:select>
      </all>
      <logic:equal name="bwconfig" property="autoCreateContacts"
                 value="true">
        <%@include file="/docs/contact/modContactCommon.jsp"%>
      </logic:equal>
    </contact>

    <logic:present name="calForm" property="event.comments">
      <comments>
        <logic:iterate id="comment" name="calForm" property="event.comments">
          <bw:emitText name="comment" property="value"/>
        </logic:iterate>
      </comments>
    </logic:present>

    <logic:present name="calForm" property="event.xproperties">
      <xproperties>
        <logic:iterate id="xprop" name="calForm" property="event.xproperties">
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

    <!-- these are the values that may be submitted to the update action -->
    <submitButtons>
      <button type="add">addEvent</button>
      <button type="update">updateEvent</button>
      <button type="cancel">cancelled</button>
      <button type="copy">copy</button>
      <button type="delete">delete</button>
    </submitButtons>
  </genurl:form>
</formElements>

<timezones>
  <logic:iterate id="tz" name="calForm" property="timeZoneNames">
    <timezone>
      <name><bean:write name="tz" property="name" filter="true"/></name>
      <id><bean:write name="tz" property="id" filter="true"/></id>
    </timezone>
  </logic:iterate>
</timezones>

<bean:define id="calInfo" name="calForm" property="calInfo" />
<shortdaynames>
  <logic:iterate id="shortDayName" name="calInfo" property="shortDayNamesAdjusted">
    <val><bean:write name="shortDayName"/></val>
  </logic:iterate>
</shortdaynames>
<recurdayvals>
  <logic:iterate id="recurDayName" name="calInfo" property="recurDayNamesAdjusted">
    <val><bean:write name="recurDayName"/></val>
  </logic:iterate>
</recurdayvals>
<monthlabels>
  <logic:iterate id="monthLabel" name="calInfo" property="monthLabels">
    <val><bean:write name="monthLabel"/></val>
  </logic:iterate>
</monthlabels>
<monthvalues>
  <logic:iterate id="monthVal" name="calInfo" property="monthVals">
    <val><bean:write name="monthVal"/></val>
  </logic:iterate>
  <start><bean:write name="calForm" property="viewStartDate.month"/></start>
</monthvalues>
