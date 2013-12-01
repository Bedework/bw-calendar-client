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
package org.bedework.webcommon.category;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;
import org.bedework.webcommon.BwSession;

/** This action updates a category.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAccess"     user not authorised.</li>
 *      <li>"notFound"     no such event.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class UpdateCategoryAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    Client cl = request.getClient();

    /** Check access
     */
    if (cl.isGuest() ||
            (cl.getPublicAdmin() && !form.getAuthorisedUser())) {
      return forwardNoAccess;
    }

    String reqpar = request.getReqPar("delete");

    if (reqpar != null) {
      return forwardDelete;
    }

    boolean add = form.getAddingCategory();

    boolean added = false;

    /** We are just updating from the current form values.
     */
    ValidateCategoryResult vcr = validateCategory(form);
    if (!vcr.ok) {
      return forwardRetry;
    }

    /* if a category with the same name and creator exist
       in categories table then retrieve its categoryid, otherwise add this
       category to the database and then retrieve its categoryid.
     */

    BwCategory cat = form.getCategory();

    if (add) {
      cat.setPublick(cl.getPublicAdmin());

      if (cl.addCategory(cat)) {
        added = true;
      }
    } else if (vcr.changed) {
      cl.updateCategory(cat);
    }

    form.assignAddingCategory(false);

    if (add) {
      if (added) {
        form.getMsg().emit(ClientMessage.addedCategories, 1);
      } else {
        form.getErr().emit(ClientError.duplicateCategory);
      }
    } else if (vcr.changed) {
      form.getMsg().emit(ClientMessage.updatedCategory);
    }

    /* refresh lists */
    request.getSess().embedCategories(request, true,
                                      BwSession.ownersEntity);
    request.getSess().embedCategories(request, true,
                                      BwSession.defaultEntity);

    return forwardContinue;
  }

  /* ====================================================================
   *                   Validation methods
   * ==================================================================== */

  /** */
  private static class ValidateCategoryResult {
    /** */
    public boolean ok = true;
    /** */
    public boolean changed;
  }

  private ValidateCategoryResult validateCategory(final BwActionFormBase form)
          throws Throwable {
    ValidateCategoryResult vcr = new ValidateCategoryResult();

    BwCategory cat = form.getCategory();

    BwString str = cat.getWord();
    BwString frmstr = form.getCategoryWord();
    if (frmstr != null) {
      if (frmstr.checkNulls() && (frmstr.getValue() == null)) {
        frmstr = null;
      }
    }

    if (str == null) {
      if (frmstr != null) {
        vcr.changed = true;
        cat.setWord(frmstr);
      }
    } else if (frmstr == null) {
      vcr.changed = true;
      cat.deleteWord();
    } else if (str.update(frmstr)) {
      vcr.changed = true;
    }

    str = cat.getDescription();
    frmstr = form.getCategoryDesc();
    if (frmstr != null) {
      if (frmstr.checkNulls() && (frmstr.getValue() == null)) {
        frmstr = null;
      }
    }

    if (str == null) {
      if (frmstr != null) {
        vcr.changed = true;
        cat.setDescription(frmstr);
      }
    } else if (frmstr == null) {
      vcr.changed = true;
      cat.deleteDescription();
    } else if (str.update(frmstr)) {
      vcr.changed = true;
    }

    if (cat.getWord() == null) {
      form.getErr().emit(ValidationError.missingCategoryKeyword);
      vcr.ok = false;
    }

    return vcr;
  }
}
