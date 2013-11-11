<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<html:xhtml/>

<bedework>
<%@ include file="/docs/header.jsp" %>

<%
try {
%>

<page>upload</page>

<formElements>
  <form>
    <!-- user's writable calendars -->
    <calendars>
      <bean:define id="addContentCalendarCollections"
                   name="bw_addcontent_collection_list" scope="session" />
      <html:select property="calendarId">
        <html:optionsCollection name="addContentCalendarCollections"
                                label="path"
                                value="path"/>
     </html:select>
   </calendars>
 </form>
</formElements>

<%
} catch (Throwable t) {
  t.printStackTrace();
}
%>

<%@ include file="/docs/footer.jsp" %>

</bedework>
