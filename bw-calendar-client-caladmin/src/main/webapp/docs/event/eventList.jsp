<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>eventList</page>
<tab>main</tab>

<%@include file="/docs/event/eventListRoot.jsp"%>

  <%-- Output the writable calendars --%>
  <calendars>
    <logic:iterate id="calendar" name="bw_addcontent_collection_list" scope="session">
      <calendar>
        <bw:emitCollection name="calendar" indent="  " full="false" noTag="true" />
      </calendar>
    </logic:iterate>
  </calendars>

  <%-- Output the categories for UI filtering: --%>
  <categories>
    <logic:iterate id="category" name="bw_categories_list" scope="session">
      <%@include file="/docs/category/emitCategory.jsp"%>
    </logic:iterate>
  </categories>

<%@include file="/docs/footer.jsp"%>

</bedework>
