/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.client.web.rw;

import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.base.AttendeesEntity;
import org.bedework.calfacade.exc.CalFacadeException;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/** Class to manage current set of attendees
 *
 * @author Mike Douglass
 */
public class Attendees implements AttendeesEntity, Serializable {

  private Set<BwAttendee> attendees;

  private Set<BwAttendee> queriedExternalAttendees;

  private Set<String> recipients;

  private Set<BwAttendee> addedAttendees;

  private Set<BwAttendee> deletedAttendees;

  /**
   * @param val
   */
  public void setQueriedExternalAttendees(Set<BwAttendee> val) {
    queriedExternalAttendees = val;
  }

  /** Get Set of attendees external to this system that have been
   * queried. This avoids multiple requests being sent by email to those
   * users.
   *
   * @return Set<BwAttendee>
   */
  public Set<BwAttendee> getQueriedExternalAttendees() {
    return queriedExternalAttendees;
  }

  /** Get Set of attendees that were deleted from the list. These must
   * be sent a cancel.
   *
   * @return Set<BwAttendee>
   */
  public Set<BwAttendee> getDeletedAttendees() {
    return deletedAttendees;
  }

  /** Get Set of attendees that were added to the list aftre it was set. These must
   * receive invitations.
   *
   * @return Set<BwAttendee>
   */
  public Set<BwAttendee> getAddedAttendees() {
    return addedAttendees;
  }

  /* ====================================================================
   *                   AttendeesEntity interface methods
   * ==================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#setAttendees(java.util.Set)
   */
  public void setAttendees(Set<BwAttendee> val) {
    attendees = val;
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#getAttendees()
   */
  public Set<BwAttendee> getAttendees() {
    return attendees;
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#getNumAttendees()
   */
  public int getNumAttendees() {
    Set<BwAttendee> as = getAttendees();
    if (as == null) {
      return 0;
    }

    return as.size();
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#addAttendee(org.bedework.calfacade.BwAttendee)
   */
  public void addAttendee(BwAttendee val) {
    Set<BwAttendee> as = getAttendees();
    if (as == null) {
      as = new TreeSet<BwAttendee>();
      setAttendees(as);
    }

    if (!as.contains(val)) {
      as.add(val);

      if (addedAttendees == null) {
        addedAttendees = new TreeSet<BwAttendee>();
      }

      addedAttendees.add(val);
    }

    if ((deletedAttendees != null) && deletedAttendees.contains(val)) {
      deletedAttendees.remove(val);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#removeAttendee(org.bedework.calfacade.BwAttendee)
   */
  public boolean removeAttendee(BwAttendee val) {
    Set<BwAttendee> as = getAttendees();
    if (as == null) {
      return false;
    }

    if (!as.remove(val)) {
      return false;
    }

    if (deletedAttendees == null) {
      deletedAttendees = new TreeSet<BwAttendee>();
    }

    deletedAttendees.add(val);

    if ((addedAttendees != null) && addedAttendees.contains(val)) {
      addedAttendees.remove(val);
    }

    return true;
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#copyAttendees()
   */
  public Set<BwAttendee> copyAttendees() {
    if (getNumAttendees() == 0) {
      return null;
    }
    TreeSet<BwAttendee> ts = new TreeSet<BwAttendee>();

    for (BwAttendee att: getAttendees()) {
      ts.add(att);
    }

    return ts;
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#cloneAttendees()
   */
  public Set<BwAttendee> cloneAttendees() {
    if (getNumAttendees() == 0) {
      return null;
    }
    TreeSet<BwAttendee> ts = new TreeSet<BwAttendee>();

    for (BwAttendee att: getAttendees()) {
      ts.add((BwAttendee)att.clone());
    }

    return ts;
  }

  /** Find an attendee entry for the given uri (calendar address).
   *
   * @param uri
   * @return BwAttendee or null if none exists
   * @throws CalFacadeException
   */
  public BwAttendee findAttendee(String uri) throws CalFacadeException {
    if (getNumAttendees() == 0) {
      return null;
    }

    int uriLen = uri.length();
    while (uri.charAt(uriLen - 1) == '/') {
      uriLen--;
      if (uriLen == 0) {
        return null;
      }
    }

    for (BwAttendee att: getAttendees()) {
      String auri = att.getAttendeeUri();
      int auriLen = auri.length();
      while (auri.charAt(auriLen - 1) == '/') {
        auriLen--;
        if (auriLen == 0) {
          return null;
        }
      }

      if ((auriLen == uriLen) && (uri.regionMatches(0, auri, 0, uriLen))) {
        return att;
      }
    }

    return null;
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#setRecipients(java.util.Set)
   */
  public void setRecipients(Set<String> val) {
    recipients = val;
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#getRecipients()
   */
  public Set<String> getRecipients() {
    return recipients;
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#getNumRecipients()
   */
  public int getNumRecipients() {
    Set<String> rs = getRecipients();
    if (rs == null) {
      return 0;
    }

    return rs.size();
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#addRecipient(java.lang.String)
   */
  public void addRecipient(String val) {
    Set<String> rs = getRecipients();
    if (rs == null) {
      rs = new TreeSet<>();
      setRecipients(rs);
    }

    if (!rs.contains(val)) {
      rs.add(val);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#removeRecipient(java.lang.String)
   */
  public boolean removeRecipient(String val) {
    Set<String> rs = getRecipients();
    if (rs == null) {
      return false;
    }

    return rs.remove(val);
  }
}
