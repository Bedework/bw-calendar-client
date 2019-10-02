<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@ include file="/docs/header.jsp" %>
  <%-- branch to an arbitrary page (an xslt template) using the
       "appvar" application variable on a link like so:

       URL/misc/showPage.rdo?setappvar=page(mypage)

       Test for page="showPage" and branch based on the value of the
       application variable you set.  See the default BedeworkTheme
       for illustration (in bedework.xsl).  It is used to include
       the Feed Builder app in an iframe, if enabled in the theme.

       This page is merely a header and footer wrapper for
       hard-coded content in the XSL template.  This could be
       used for static information you would like to present within
       the calendar template.

  --%>

  <page>showPage</page>

<%@ include file="/docs/footer.jsp" %>
</bedework>
