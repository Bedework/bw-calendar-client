<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
    <%-- Output any event fields with forms specific to short format displays --%>
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
    <categories>
      <logic:present name="event" property="categories">
        <logic:iterate id="category" name="event" property="categories">
          <%@include file="/docs/category/emitCategory.jsp"%>
        </logic:iterate>
      </logic:present>
    </categories>

