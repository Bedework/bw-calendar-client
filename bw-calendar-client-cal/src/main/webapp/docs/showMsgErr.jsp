<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>

<%@ include file="header.jsp" %>
  <%-- branch to an arbitrary page (an xslt template) using the
       "appvar" application variable on a link like so:

       URL/showPage.do?setappvar=page(mypage)

       Test for page="other" and branch based on the value of the
       application variable you set. (See the main template in the
       original default.xsl for illustration.)

       This page is merely a header and footer wrapper for
       hard-coded content in the XSLT template.  This could be
       used for static information you would like to present within
       the calendar template.
  --%>

  <page>showMsgErr</page>

<%@ include file="footer.jsp" %>
