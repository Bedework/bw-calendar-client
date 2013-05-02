<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<%@include file="/docs/header.jsp"%>

<page>categoryList</page>

<categories>
  <logic:present name="calForm" property="editableCategories" >
    <logic:iterate id="category" name="calForm" property="editableCategories" >
      <%@include file="/docs/category/emitCategory.jsp"%>
    </logic:iterate>
  </logic:present>
</categories>

<%@include file="/docs/footer.jsp"%>

