<%@ page contentType="text/xml;charset=UTF-8" buffer="none" language="java" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri='struts-bean' prefix='bean' %>
<%@ taglib uri='struts-logic' prefix='logic' %>
<%@ taglib uri='struts-html' prefix='html' %>
<%@ taglib uri='struts-genurl' prefix='genurl' %>
<%@ taglib uri='bedework' prefix='bw' %>
<html:xhtml/>

<bedework>
<%@ include file="/docs/header.jsp" %>

<page>selectCalForEvent</page>
<% /* Used when selecting a calendar while adding or editing an event.

      This page will be called when
      a) we add an event by date with no specific calendar selected
      b) we import an event
      c) we add an event ref
      d) we edit an event and change it's calendar (or change it while adding)

      The intention is to load the calendar listing in a pop-up window as a
      tree of myCalendars and writable calendars associated with subscriptions.
      The xml for the tree is already in header.jsp, so we don't have to
      recreate it here.
      */ %>

<%@ include file="/docs/footer.jsp" %>
</bedework>
