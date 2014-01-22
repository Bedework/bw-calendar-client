<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@ include file="/docs/header.jsp" %>

<%
try {
%>

<page>searchResult</page>

<categories>
  <logic:present name="bw_categories_list" scope="session">
    <logic:iterate id="category" name="bw_categories_list" scope="session">
      <%@include file="/docs/category/emitCategory.jsp"%>
    </logic:iterate>
  </logic:present>
</categories>

<searchResults>
  <bw:emitText name="moduleState" property="query"/>
  <bw:emitText name="moduleState" property="searchLimits"/>
  <logic:notPresent name="bw_search_result" scope="request">
    <resultSize>0</resultSize>
  </logic:notPresent>

  <logic:present name="bw_search_result" scope="request">
    <bean:define id="sres" name="bw_search_result" scope="request"/>
    <bw:emitText name="sres" property="found" tagName="resultSize" />
    <logic:iterate id="sre" name="bw_search_list" scope="request">
      <searchResult>
        <bw:emitText name="sre" property="score" />
        <logic:equal name="sre" property="docType" value="event">
          <bean:define id="eventFmt" name="sre" property="entity" toScope="request"  />
          <bean:define id="eventInfo" name="eventFmt" property="eventInfo" toScope="request"  />
          <bean:define id="event" name="eventFmt" property="event" toScope="request"  />

          <event>
            <entityType><bean:write name="event" property="entityType"/></entityType>
            <scheduleMethod><bean:write name="event" property="scheduleMethod"/></scheduleMethod>
            <start><%-- start date and time --%>
              <noStart><bean:write name="event" property="noStart"/></noStart>
              <bean:define id="date" name="eventFmt"
                           property="start"
                           toScope="request" />
              <%@ include file="/docs/event/emitDate.jsp" %>
            </start>
            <end><%-- end date and time --%>
              <type><bean:write name="event" property="endType"/></type>
              <bean:define id="date" name="eventFmt"
                           property="end"
                           toScope="request" />
              <%@ include file="/docs/event/emitDate.jsp" %>
            </end>
            <bw:emitText name="event" property="creatorHref" tagName="creator"/>
            <bw:emitText name="event" property="ownerHref" tagName="owner"/>

            <id><bean:write name="event" property="id"/></id><%--
              Value: integer - event id --%>
            <bw:emitText name="event" property="uid" tagName="guid" />
            <bw:emitText name="event" property="recurrenceId" tagName="recurrenceId" />
            <bw:emitText name="event" property="summary" /><%--
              Value: string - short description, typically used for the event title  --%>
            <bw:emitText name="event" property="color"/>
            <bw:emitText name="event" property="link"/><%--
              Value: URI - link associated with the event --%>
            <deleted><bean:write name="event" property="deleted"/></deleted>
            <public><bean:write name="event" property="publick"/></public>
            <editable><bean:write name="eventInfo" property="editable"/></editable><%--
              Value: true,false - true if user can edit (and delete) event, false otherwise --%>
            <logic:present  name="event" property="target">
              <isAnnotation/>
            </logic:present>
            <kind><bean:write name="eventInfo" property="kind"/></kind><%--
              Value: 0 - actual event entry
                     1 - 'added event' from a reference
                     2 - from a subscription --%>
            <recurring><bean:write name="event" property="recurring"/></recurring><%--
              Value: true,false - true if the event is recurring --%>
            <bw:emitContainer name="event" indent="    " tagName="calendar" />
            <bw:emitText name="event" property="status" /><%-- Status
                  Value: string, only one of CONFIRMED, TENTATIVE, or CANCELLED --%>

            <!-- always produce the xproperties, if they exist -->
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
                          <%=xparStart%><![CDATA[<bean:write name="xpar" property="value" filter="false"/>]]><%=xparEnd%>
                        </logic:iterate>
                        </parameters>
                      </logic:present>
                      <values>
                        <text><![CDATA[<bean:write name="xprop" property="value" filter="false"/>]]></text>
                      </values>
                    <%=xpropEnd%>
                  </logic:equal>
                </logic:iterate>
              </xproperties>
            </logic:present>

            <logic:present  name="event" property="percentComplete">
              <bw:emitText name="event" property="percentComplete"/>
            </logic:present>

            <logic:present  name="event" property="geo">
              <bw:emitText name="event" property="geo.latitude" tagName="latitude"/>
              <bw:emitText name="event" property="geo.longitude" tagName="longitude"/>
            </logic:present>

            <logic:present  name="event" property="location">
              <bean:define id="location" name="event" property="location"/>
              <location>
                <id><bean:write name="location" property="id"/></id><%--
                    Value: integer - location id --%>
                <address><bean:write name="location" property="address.value"/></address><%--
                  Value: string - physical address of the location --%>
                <link><bean:write name="location" property="link"/></link><%--
                    Value: URI - link to a web address for the location --%>
              </location>
            </logic:present>
            <logic:notPresent  name="event" property="location">
              <location>
                <address></address>
              </location>
            </logic:notPresent>

          </event>


        </logic:equal>
      </searchResult>
    </logic:iterate>
  </logic:present>

</searchResults>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="/docs/footer.jsp" %>

</bedework>
