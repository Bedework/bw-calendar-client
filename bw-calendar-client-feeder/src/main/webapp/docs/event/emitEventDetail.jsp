<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='bedework' prefix='bw' %>

    <%-- Output any event fields with forms specific to short format displays --%>
    <logic:present  name="event" property="location">
      <bean:define id="location" name="event" property="location"/>
      <location>
        <uid><bean:write name="location" property="uid"/></uid><%--
            Value: location uid --%>
        <bw:emitText name="location" property="address.value" tagName="address"/><%--
          Value: string - physical address of the location --%>
        <bw:emitText name="location" property="link"/><%--
            Value: URI - link to a web address for the location --%>
        <logic:present name="location" property="subaddress">
          <bw:emitText name="location" property="subaddress.value" tagName="subaddress"/><%--
            Value: string - more address information --%>
        </logic:present>
        <bw:emitText name="location" property="creatorHref" tagName="creator" /><%--
          Value: string - location creator id --%>
      </location>
    </logic:present>
    <logic:notPresent  name="event" property="location">
      <location>
        <address></address>
        <id></id><%--
          Value: integer - location id --%>
        <subaddress></subaddress><%--
          Value: string - more address information --%>
        <link></link><%--
          Value: URI - link to a web address for the location --%>
        <creator></creator><%--
          Value: string - location creator id --%>
      </location>
    </logic:notPresent>
    <categories>
      <logic:present name="event" property="categories">
        <logic:iterate id="category" name="event" property="categories">
          <%@include file="/docs/category/emitCategory.jsp"%>
        </logic:iterate>
      </logic:present>
    </categories>
    <jsp:include page="/docs/event/emitRecur.jsp"/>
    <bw:emitText name="event" property="description" /><%--
        Value: string - long description of the event.  Limited to 500 characters. --%>
    <bw:emitText name="event" property="cost" /><%--
        Value: string - cost information --%>
    <sequence><bean:write name="event" property="sequence"/></sequence><%--
        RFC sequence number for the event --%>

    <logic:present name="event" property="contact">
      <bean:define id="contact" name="event" property="contact"/>
      <contact>
        <id><bean:write name="contact" property="id"/></id><%--
          Value: integer - contact id --%>
        <bw:emitText name="contact" property="name.value" tagName="name"/><%--
          Value: string - contact's name --%>
        <bw:emitText name="contact" property="phone"/><%--
          Value (example): x7777 - contact's phone number --%>
        <bw:emitText name="contact" property="email"/><%--
          Value (example): nobody@nowhere.edu - contact's email address --%>
        <bw:emitText name="contact" property="link"/><%--
          Value: URI - link to contact web page --%>
      </contact>
    </logic:present>

