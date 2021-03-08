/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.admin;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.base.BwShareableDbentity;
import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;

import java.util.Collection;
import java.util.List;

/**
 * User: mike Date: 3/7/21 Time: 16:50
 */
public interface AdminClient extends Client {
  /** Show whether admin group maintenance is available.
   * Some sites may use other mechanisms.
   *
   * @return boolean    true if admin group maintenance is implemented.
   */
  boolean getAdminGroupMaintOK();

  /* ------------------------------------------------------------
   *                     Admin users
   * ------------------------------------------------------------ */

  /** Add an entry for the user.
   *
   * @param account for user
   * @throws CalFacadeException on fatal error
   */
  void addUser(String account) throws CalFacadeException;

  /** Add the user entry
   *
   * @param  val      AuthUser users entry
   * @throws CalFacadeException on fatal error
   */
  void addAuthUser(BwAuthUser val) throws CalFacadeException;

  /** Return the given authorised user. Will always return an entry (except for
   * exceptional conditions.) An unauthorised user will have a usertype of
   * noPrivileges.
   *
   * @param  pr the principal
   * @return BwAuthUser    users entry
   * @throws CalFacadeException on fatal error
   */
  BwAuthUser getAuthUser(BwPrincipal pr) throws CalFacadeException;

  /** Update the user entry
   *
   * @param  val      AuthUser users entry
   * @throws CalFacadeException on fatal error
   */
  void updateAuthUser(BwAuthUser val) throws CalFacadeException;

  /** Return a collection of all authorised users
   *
   * @return Collection      of BwAuthUser for users with any special authorisation.
   * @throws CalFacadeException on fatal error
   */
  Collection<BwAuthUser> getAllAuthUsers() throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Admin Groups
   * ------------------------------------------------------------ */

  /**
   * @return String used to prefix administrative group names to distinguish
   *         them from user group names.
   */
  String getAdminGroupsIdPrefix();

  /**
   * @param href  a principal href
   * @return group
   * @throws CalFacadeException  for errors
   */
  BwGroup getAdminGroup(String href) throws CalFacadeException;

  /**
   * @return groups that are calsuite owners
   * @throws CalFacadeException  for errors
   */
  Collection<BwGroup> getCalsuiteAdminGroups() throws CalFacadeException;

  /**
   * Force a refetch of the groups when getAdminGroups is called
   */
  void refreshAdminGroups();

  /** Add a group
   *
   * @param  group           BwGroup group object to add
   * @exception CalFacadeException If there's a problem
   */
  void addAdminGroup(BwAdminGroup group) throws CalFacadeException;

  /** Delete a group
   *
   * @param  group           BwGroup group object to delete
   * @exception CalFacadeException If there's a problem
   */
  void removeAdminGroup(BwAdminGroup group) throws CalFacadeException;

  /** update a group. This may have no meaning in some directories.
   *
   * @param  group           BwGroup group object to update
   * @exception CalFacadeException If there's a problem
   */
  void updateAdminGroup(BwAdminGroup group) throws CalFacadeException;

  /** Find a group given its name
   *
   * @param  name           String group name
   * @return BwAdminGroup        group object
   */
  BwAdminGroup findAdminGroup(String name);

  /** Populate the group with a (possibly empty) Collection of members. Does not
   * populate groups which are members.
   *
   * @param  group           BwGroup group object to add
   * @throws CalFacadeException on fatal error
   */
  void getAdminGroupMembers(BwAdminGroup group) throws CalFacadeException;

  /** Add a principal to a group
   *
   * @param group          a group principal
   * @param val            BwPrincipal new member
   * @exception CalFacadeException   For invalid usertype values.
   */
  void addAdminGroupMember(BwAdminGroup group,
                           BwPrincipal val) throws CalFacadeException;

  /** Remove a member from a group
   *
   * @param group          a group principal
   * @param val            BwPrincipal new member
   * @exception CalFacadeException   For invalid usertype values.
   */
  void removeAdminGroupMember(BwAdminGroup group,
                              BwPrincipal val) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Groups
   * ------------------------------------------------------------ */

  /** Find a group given its name
   *
   * @param  name           String group name
   * @return BwGroup        group object
   */
  BwGroup findGroup(String name);

  /**
   * @param group the group
   * @return Collection of groups of which this is a member
   * @throws CalFacadeException on fatal error
   */
  Collection<BwGroup> findGroupParents(BwGroup group) throws CalFacadeException;

  /** Return all groups of which the given principal is a member. Never returns null.
   *
   * <p>Does not check the returned groups for membership of other groups.
   *
   * @param val            a principal
   * @return Collection    of BwGroup
   * @throws CalFacadeException on fatal error
   */
  Collection<BwGroup> getGroups(BwPrincipal val) throws CalFacadeException;

  /** Return all groups to which this user has some access. Never returns null.
   *
   * @param  populate      boolean populate with members
   * @return Collection    of BwGroup
   * @throws CalFacadeException on fatal error
   */
  Collection<BwGroup> getAllGroups(boolean populate) throws CalFacadeException;

  /** Populate the group with a (possibly empty) Collection of members. Does not
   * populate groups which are members.
   *
   * @param  group           BwGroup group object to add
   * @throws CalFacadeException on fatal error
   */
  void getMembers(BwGroup group) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                   Calendar Suites
   * ------------------------------------------------------------ */

  /** Set the calendar suite we are running as. Must be running as an
   * unauthenticated user.
   *
   * @throws CalFacadeException on fatal error
   */
  void setCalSuite(BwCalSuite cs) throws CalFacadeException;

  /** Get a calendar suite given the 'owning' group
   *
   * @param  group     BwAdminGroup
   * @return BwCalSuiteWrapper null for unknown calendar suite
   * @throws CalFacadeException on fatal error
   */
  BwCalSuiteWrapper getCalSuite(BwAdminGroup group)
          throws CalFacadeException;

  /** Create a new calendar suite
   *
   * @param name ofsuite
   * @param adminGroupName - name of the admin group
   * @param rootCollectionPath - root for suite
   * @param submissionsPath where submitted events go
   * @return BwCalSuiteWrapper for new object
   * @throws CalFacadeException on fatal error
   */
  BwCalSuiteWrapper addCalSuite(String name,
                                String adminGroupName,
                                String rootCollectionPath,
                                String submissionsPath) throws CalFacadeException;

  /** Update a calendar suite. Any of the parameters to be changed may be null
   * or the current value to indicate no change.
   *
   * @param cs     BwCalSuiteWrapper object
   * @param adminGroupName - name of the admin group
   * @param rootCollectionPath - root for suite
   * @param submissionsPath where submitted events go
   * @throws CalFacadeException on fatal error
   */
  void updateCalSuite(BwCalSuiteWrapper cs,
                      String adminGroupName,
                      String rootCollectionPath,
                      String submissionsPath) throws CalFacadeException;

  /** Delete a calendar suite object
   *
   * @param  val     BwCalSuiteWrapper object
   * @throws CalFacadeException on fatal error
   */
  void deleteCalSuite(BwCalSuiteWrapper val) throws CalFacadeException;

  /** Is the current calendar suite the 'owner'.
   *
   * @param ent the entity to test
   * @return true if so
   */
  boolean isCalSuiteEntity(BwShareableDbentity<?> ent);

  /* ------------------------------------------------------------
   *                   Calendar Suite Resources
   * ------------------------------------------------------------ */

  /** Get a list of resources. The content is not fetched.
   *
   * @param suite - calendar suite
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @return list
   * @throws CalFacadeException on fatal error
   */
  List<BwResource> getCSResources(BwCalSuite suite,
                                  String rc) throws CalFacadeException;

  /** Get named resource. The content is fetched.
   *
   * @param suite - calendar suite
   * @param name of resource
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @return resource or null
   * @throws CalFacadeException on fatal error
   */
  BwResource getCSResource(BwCalSuite suite,
                           String name,
                           String rc) throws CalFacadeException;

  /** Add a resource. The supplied object has all fields set except for the
   * path. This will be determined by the cl parameter and set in the object.
   *
   * <p>The parent collection will be created if necessary.
   *
   * @param suite - calendar suite
   * @param res resource to add
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @throws CalFacadeException on fatal error
   */
  void addCSResource(BwCalSuite suite,
                     BwResource res,
                     String rc) throws CalFacadeException;

  /** Delete named resource
   *
   * @param suite - calendar suite
   * @param name of resource
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @throws CalFacadeException on fatal error
   */
  void deleteCSResource(BwCalSuite suite,
                        String name,
                        String rc) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     State of current admin group
   * ------------------------------------------------------------ */

  /**
   * @param val true if group is set
   * @throws CalFacadeException on fatal error
   */
  void setGroupSet(boolean val) throws CalFacadeException;

  /**
   * @return true for group set
   */
  boolean getGroupSet();

  /**
   * @param val true if choosing group
   * @throws CalFacadeException on fatal error
   */
  void setChoosingGroup(boolean val) throws CalFacadeException;

  /**
   * @return true for choosing group
   */
  boolean getChoosingGroup();

  /**
   * @param val true if only 1 group available
   * @throws CalFacadeException on fatal error
   */
  void setOneGroup(boolean val) throws CalFacadeException;

  /**
   * @return true if there is only one group
   */
  boolean getOneGroup();

  /** Current admin group name, or null for none
   *
   * @param val      BwAdminGroup representing users group or null
   * @throws CalFacadeException on fatal error
   */
  void setAdminGroupName(String val) throws CalFacadeException;

  /**
   * @return String admin group name
   */
  String getAdminGroupName();

  /* ------------------------------------------------------------
   *                     Misc
   * ------------------------------------------------------------ */

  /** Update the system after changes to timezones. This is a lengthy process
   * so the method allows the caller to specify how many updates are to take place
   * before returning.
   *
   * <p>To restart the update, call the method again, giving it the result from
   * the last call as a parameter.
   *
   * <p>If called again after all events have been checked the process will be
   * redone using timestamps to limit the check to events added or updated since
   * the first check. Keep calling until the number of updated events is zero.
   *
   * @param limit   -1 for no limit
   * @param checkOnly  don't update if true.
   * @param info    null on first call, returned object from previous calls.
   * @return UpdateFromTimeZonesInfo staus of the update
   * @throws CalFacadeException on fatal error
   */
  UpdateFromTimeZonesInfo updateFromTimeZones(String colHref,
                                              int limit,
                                              boolean checkOnly,
                                              UpdateFromTimeZonesInfo info)
          throws CalFacadeException;
}
