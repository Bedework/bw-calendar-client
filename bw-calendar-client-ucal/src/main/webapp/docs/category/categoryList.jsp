<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>categoryList</page>

<categories>
  <logic:present name="bw_editable_categories_list" scope="session">
    <logic:iterate id="category" name="bw_editable_categories_list"
                   scope="session">
      <%@include file="/docs/category/emitCategory.jsp"%>
    </logic:iterate>
  </logic:present>
</categories>

<%@include file="/docs/footer.jsp"%>

</bedework>
