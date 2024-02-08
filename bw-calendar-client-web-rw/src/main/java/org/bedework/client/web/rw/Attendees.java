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
   * @param val set of attendees
   */
  public void setQueriedExternalAttendees(final Set<BwAttendee> val) {
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
  public void setAttendees(final Set<BwAttendee> val) {
    attendees = val;
  }

  /* (non-Javadoc)
   * @see org.bedework.calfacade.base.AttendeesEntity#getAttendees()
   */
  public Set<BwAttendee> getAttendees() {
    return attendees;
  }

  @Override
  public int getNumAttendees() {
    final Set<BwAttendee> as = getAttendees();
    if (as == null) {
      return 0;
    }

    return as.size();
  }

  @Override
  public void addAttendee(final BwAttendee val) {
    Set<BwAttendee> as = getAttendees();
    if (as == null) {
      as = new TreeSet<>();
      setAttendees(as);
    }

    if (!as.contains(val)) {
      as.add(val);

      if (addedAttendees == null) {
        addedAttendees = new TreeSet<>();
      }

      addedAttendees.add(val);
    }

    if ((deletedAttendees != null)) {
      deletedAttendees.remove(val);
    }
  }

  @Override
  public boolean removeAttendee(final BwAttendee val) {
    final Set<BwAttendee> as = getAttendees();
    if (as == null) {
      return false;
    }

    if (!as.remove(val)) {
      return false;
    }

    if (deletedAttendees == null) {
      deletedAttendees = new TreeSet<>();
    }

    deletedAttendees.add(val);

    if ((addedAttendees != null)) {
      addedAttendees.remove(val);
    }

    return true;
  }

  @Override
  public Set<BwAttendee> copyAttendees() {
    if (getNumAttendees() == 0) {
      return null;
    }

    return new TreeSet<>(getAttendees());
  }

  @Override
  public Set<BwAttendee> cloneAttendees() {
    if (getNumAttendees() == 0) {
      return null;
    }

    return new TreeSet<>(getAttendees());
  }

  /** Find an attendee entry for the given uri (calendar address).
   *
   * @param uri of attendee
   * @return BwAttendee or null if none exists
   */
  public BwAttendee findAttendee(final String uri) {
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

    for (final BwAttendee att: getAttendees()) {
      final String auri = att.getAttendeeUri();
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

  @Override
  public void setRecipients(final Set<String> val) {
    recipients = val;
  }

  @Override
  public Set<String> getRecipients() {
    return recipients;
  }

  @Override
  public int getNumRecipients() {
    final Set<String> rs = getRecipients();
    if (rs == null) {
      return 0;
    }

    return rs.size();
  }

  @Override
  public void addRecipient(final String val) {
    Set<String> rs = getRecipients();
    if (rs == null) {
      rs = new TreeSet<>();
      setRecipients(rs);
    }

    rs.add(val);
  }

  @Override
  public boolean removeRecipient(final String val) {
    final Set<String> rs = getRecipients();
    if (rs == null) {
      return false;
    }

    return rs.remove(val);
  }
}
