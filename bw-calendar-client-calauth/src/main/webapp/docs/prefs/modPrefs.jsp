<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@include file="/docs/header.jsp"%>

<page>modPrefs</page>
<bean:define id="userPrefs" name="calForm" property="userPreferences"/>
<prefs>
  <user><bean:write name="userPrefs" property="ownerHref"/></user>
  <bw:emitText name="userPrefs" property="email"/>
  <!-- default calendar into which events will be placed -->
  <defaultCalendar>
    <path><bean:write name="userPrefs" property="defaultCalendarPath"/></path>
    <subName></subName>
  </defaultCalendar>
  <!-- name of default view (collection of subscriptions) that will appear upon login -->
  <bw:emitText name="userPrefs" property="preferredView"/>
  <!-- default period that will appear upon login (day, week, month, year, today) -->
  <bw:emitText name="userPrefs" property="preferredViewPeriod"/>
  <!-- whether user will use 12 (am/pm) or 24 hour mode when entering events -->
  <bw:emitText name="userPrefs" property="hour24"/>
  <!-- skinName is XSL skin name; skinStyle is intended for CSS stylesheet name -->
  <bw:emitText name="userPrefs" property="skinName"/>
  <bw:emitText name="userPrefs" property="skinStyle"/>
  <!-- string of chars representing the days -->
  <bw:emitText name="userPrefs" property="workDays"/>
  <!-- start and end in minutes: e.g. 14:30 is 870 and 17:30 is 1050 -->
  <bw:emitText name="userPrefs" property="workdayStart"/>
  <bw:emitText name="userPrefs" property="workdayEnd"/>
  <!-- pref end type = date or duration -->
  <bw:emitText name="userPrefs" property="preferredEndType"/>
  <!-- user mode: 0 = basicMode, 1 = simpleMode, 2 = advancedMode -->
  <bw:emitText name="userPrefs" property="userMode"/>
  <bw:emitText name="userPrefs" property="defaultTzid"/>

  <defaultCategories>
    <logic:present name="bw_default_categories_list" scope="session">
      <logic:iterate id="category" name="bw_default_categories_list"
                     scope="session">
        <%@include file="/docs/category/emitCategory.jsp"%>
      </logic:iterate>
    </logic:present>
  </defaultCategories>
</prefs>

<formElements>
 <form>
   <!-- user's writable calendars -->
   <calendars>
     <bean:define id="addContentCalendarCollections"
                  name="bw_addcontent_collection_list" scope="session" />
     <html:select name="calForm" property="calendarId">
       <html:optionsCollection name="addContentCalendarCollections"
                                     label="path"
                                     value="path"/>
     </html:select>
   </calendars>
 </form>
</formElements>

<%@include file="/docs/footer.jsp"%>

</bedework>
