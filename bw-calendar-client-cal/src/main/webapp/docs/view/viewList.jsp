<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>views</page>

<views>
  <logic:iterate name="calForm" property="views" id="view">
    <view>
      <name><bean:write name="view" property="name" /></name>
      <paths>
        <logic:iterate name="view" property="collectionPaths" id="path">
          <path><bean:write name="path"/></path>
        </logic:iterate>
      </paths>
    </view>
  </logic:iterate>
</views>


<%@include file="/docs/footer.jsp"%>
</bedework>
