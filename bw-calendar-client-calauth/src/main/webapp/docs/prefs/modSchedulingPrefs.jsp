<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>modSchedulingPrefs</page>
<bean:define id="userPrefs" name="calForm" property="userPreferences"/>
<schPrefs>
  <user><bean:write name="userPrefs" property="ownerHref"/></user>
  <%--inbox>
    <bw:emitAcl name="inBoxInfo" property="currentAccess" />
  </inbox --%>
  <bw:emitText name="userPrefs" property="scheduleAutoRespond"/>
  <bw:emitText name="userPrefs" property="scheduleAutoCancelAction"/>
  <bw:emitText name="userPrefs" property="scheduleDoubleBook"/>
  <bw:emitText name="userPrefs" property="scheduleAutoProcessResponses"/>
</schPrefs>

<%@include file="/docs/footer.jsp"%>


</bedework>
