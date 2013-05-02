<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@include file="/docs/header.jsp"%>

<page>contactReferenced</page>
<tab>main</tab>

<contact>
  <name>
    <bean:write name="calForm" property="contact.name.value" />
  </name>
  <phone><bean:write name="calForm" property="contact.phone" /></phone>
  <logic:present name="calForm" property="contact.email">
    <email><bean:write name="calForm" property="contact.email"/></email>
  </logic:present>
  <logic:present name="calForm" property="contact.link">
    <link><bean:write name="calForm" property="contact.link" /></link>
  </logic:present>
</contact>

<propRefs>
  <logic:present name="calForm" property="propRefs">
    <logic:iterate id="propRef" name="calForm" property="propRefs" >
      <propRef>
        <logic:present name="propRef" property="collection">
          <bw:emitText name="propRef" property="collection" tagName="isCollection" />
        </logic:present>
        <logic:notPresent name="propRef" property="collection">
          <isCollection></isCollection>
        </logic:notPresent>
        <logic:present name="propRef" property="path">
          <bw:emitText name="propRef" property="path" tagName="path" />
        </logic:present>
        <logic:notPresent name="propRef" property="path">
          <path></path>
        </logic:notPresent>
        <logic:present name="propRef" property="uid">
          <bw:emitText name="propRef" property="uid" tagName="uid" />
        </logic:present>
        <logic:notPresent name="propRef" property="uid">
          <uid></uid>
        </logic:notPresent>
      </propRef>
    </logic:iterate>
  </logic:present>
</propRefs>

<%@include file="/docs/footer.jsp"%>

