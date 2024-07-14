/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.admin;

import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.base.BwShareableDbentity;
import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.client.rw.RWClient;

import java.util.Collection;
import java.util.List;

/**
 * User: mike Date: 3/7/21 Time: 16:50
 */
public interface AdminClient extends RWClient {
  /** Show whether admin group maintenance is available.
   * Some sites may use other mechanisms.
   *
   * @return boolean    true if admin group maintenance is implemented.
   */
  boolean getAdminGroupMaintOK();

  /* ------------------------------------------------------------
   *                     Admin users
   * ------------------------------------------------------------ */

  /** Show whether user entries can be displayed or modified with this
   * class. Some sites may use other mechanisms.
   *
   * <p>This may need supplementing with changes to the jsp. For example,
   * it's hard to deal programmatically with the case of directory/roles
   * based authorisation and db based user information.
   *
   * @return boolean    true if user maintenance is implemented.0
   */
  boolean getUserMaintOK();

  /** Add an entry for the user.
   *
   * @param account for user
   */
  void addUser(String account);

  /** Add the user entry
   *
   * @param  val      AuthUser users entry
   */
  void addAuthUser(BwAuthUser val);

  /** Return the given authorised user. Will always return an entry (except for
   * exceptional conditions.) An unauthorised user will have a usertype of
   * noPrivileges.
   *
   * @param  pr the principal
   * @return BwAuthUser    users entry
   */
  BwAuthUser getAuthUser(BwPrincipal<?> pr);

  /** Return the current authorised user. Will always return an entry (except for
   * exceptional conditions.) An unauthorised user will have a usertype of
   * noPrivileges.
   *
   * @return BwAuthUser    users entry
   */
  BwAuthUser getAuthUser();

  /** Find the user with the given account name. Create if not there.
   *
   * @param val           String user id
   * @return BwUser       representing the user
   */
  BwPrincipal<?> getUserAlways(String val);

  /**
   * @return true if current auth user is an approver
   */
  boolean isApprover();

  /** Update the user entry
   *
   * @param  val      AuthUser users entry
   */
  void updateAuthUser(BwAuthUser val);

  /** Return a collection of all authorised users
   *
   * @return Collection      of BwAuthUser for users with any special authorisation.
   */
  Collection<BwAuthUser> getAllAuthUsers();

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
   */
  BwGroup<?> getAdminGroup(String href);

  /**
   * @return groups that are calsuite owners
   */
  Collection<BwGroup<?>> getCalsuiteAdminGroups();

  /**
   * Force a refetch of the groups when getAdminGroups is called
   */
  void refreshAdminGroups();

  /** Add a group
   *
   * @param  group           BwGroup group object to add
   */
  void addAdminGroup(BwAdminGroup group);

  /** Delete a group
   *
   * @param  group           BwGroup group object to delete
   */
  void removeAdminGroup(BwAdminGroup group);

  /** update a group. This may have no meaning in some directories.
   *
   * @param  group           BwGroup group object to update
   */
  void updateAdminGroup(BwAdminGroup group);

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
   */
  void getAdminGroupMembers(BwAdminGroup group);

  /** Add a principal to a group
   *
   * @param group          a group principal
   * @param val            BwPrincipal new member
   */
  void addAdminGroupMember(BwAdminGroup group,
                           BwPrincipal<?> val);

  /** Remove a member from a group
   *
   * @param group          a group principal
   * @param val            BwPrincipal<?> new member
   */
  void removeAdminGroupMember(BwAdminGroup group,
                              BwPrincipal<?> val);

  /* ------------------------------------------------------------
   *                     Groups
   * ------------------------------------------------------------ */

  /** Find a group given its name
   *
   * @param  name           String group name
   * @return BwGroup        group object
   */
  BwGroup<?> findGroup(String name);

  /**
   * @param group the group
   * @return Collection of groups of which this is a member
   */
  Collection<BwGroup<?>> findGroupParents(BwGroup<?> group);

  /** Return all groups of which the given principal is a member. Never returns null.
   *
   * <p>Does not check the returned groups for membership of other groups.
   *
   * @param val            a principal
   * @return Collection    of BwGroup
   */
  Collection<BwGroup<?>> getGroups(BwPrincipal<?> val);

  /** Return all groups to which this user has some access. Never returns null.
   *
   * @param  populate      boolean populate with members
   * @return Collection    of BwGroup
   */
  Collection<BwGroup<?>> getAllGroups(boolean populate);

  /** Populate the group with a (possibly empty) Collection of members. Does not
   * populate groups which are members.
   *
   * @param  group           BwGroup group object to add
   */
  void getMembers(BwGroup<?> group);

  /* ------------------------------------------------------------
   *                   Calendar Suites
   * ------------------------------------------------------------ */

  /** Set the calendar suite we are running as. Must be running as an
   * unauthenticated user.
   *
   */
  void setCalSuite(BwCalSuite cs);

  /** Get a calendar suite given the 'owning' group
   *
   * @param  group     BwAdminGroup
   * @return BwCalSuiteWrapper null for unknown calendar suite
   */
  BwCalSuiteWrapper getCalSuite(BwAdminGroup group)
         ;

  /** Create a new calendar suite
   *
   * @param name ofsuite
   * @param adminGroupName - name of the admin group
   * @param rootCollectionPath - root for suite
   * @param submissionsPath where submitted events go
   * @return BwCalSuiteWrapper for new object
   */
  BwCalSuiteWrapper addCalSuite(String name,
                                String adminGroupName,
                                String rootCollectionPath,
                                String submissionsPath);

  /** Update a calendar suite. Any of the parameters to be changed may be null
   * or the current value to indicate no change.
   *
   * @param cs     BwCalSuiteWrapper object
   * @param adminGroupName - name of the admin group
   * @param rootCollectionPath - root for suite
   * @param description of calsuite
   */
  void updateCalSuite(BwCalSuiteWrapper cs,
                      String adminGroupName,
                      String rootCollectionPath,
                      String description);

  /** Delete a calendar suite object
   *
   * @param  val     BwCalSuiteWrapper object
   */
  void deleteCalSuite(BwCalSuiteWrapper val);

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
   */
  List<BwResource> getCSResources(BwCalSuite suite,
                                  String rc);

  /** Get named resource. The content is fetched.
   *
   * @param suite - calendar suite
   * @param name of resource
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @return resource or null
   */
  BwResource getCSResource(BwCalSuite suite,
                           String name,
                           String rc);

  /** Add a resource. The supplied object has all fields set except for the
   * path. This will be determined by the cl parameter and set in the object.
   *
   * <p>The parent collection will be created if necessary.
   *
   * @param suite - calendar suite
   * @param res resource to add
   * @param rc - define class of resource "calsuite", "admin"or "global"
   */
  void addCSResource(BwCalSuite suite,
                     BwResource res,
                     String rc);

  /** Delete named resource
   *
   * @param suite - calendar suite
   * @param name of resource
   * @param rc - define class of resource "calsuite", "admin"or "global"
   */
  void deleteCSResource(BwCalSuite suite,
                        String name,
                        String rc);

  /* ------------------------------------------------------------
   *                     State of current admin group
   * ------------------------------------------------------------ */

  /**
   * @param val true if group is set
   */
  void setGroupSet(boolean val);

  /**
   * @return true for group set
   */
  boolean getGroupSet();

  /**
   * @param val true if choosing group
   */
  void setChoosingGroup(boolean val);

  /**
   * @return true for choosing group
   */
  boolean getChoosingGroup();

  /**
   * @param val true if only 1 group available
   */
  void setOneGroup(boolean val);

  /**
   * @return true if there is only one group
   */
  boolean getOneGroup();

  /** Current admin group name, or null for none
   *
   * @param val      BwAdminGroup representing users group or null
   */
  void setAdminGroupName(String val);

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
   */
  UpdateFromTimeZonesInfo updateFromTimeZones(String colHref,
                                              int limit,
                                              boolean checkOnly,
                                              UpdateFromTimeZonesInfo info)
         ;
}
