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
package org.bedework.client.web.rw.category;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.util.misc.Util;
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
public class UpdateCategoryAction extends RWActionBase {
  @Override
  public String doAction(final BwRequest request,
                         final RWClient cl) {
    final var form = getRwForm();
    final var reqpar = request.getReqPar("delete");

    if (reqpar != null) {
      return forwardDelete;
    }

    final boolean add = form.getAddingCategory();

    boolean added = false;

    /* We are just updating from the current form values.
     */
    final ValidateCategoryResult vcr = validateCategory(request, form);
    if (!vcr.ok) {
      return forwardRetry;
    }

    /* if a category with the same name and creator exist
       in categories table then retrieve its categoryid, otherwise add this
       category to the database and then retrieve its categoryid.
     */

    final BwCategory cat = form.getCategory();

    if (cl.isSuperUser()) {
      cat.setArchived(request.getBooleanReqPar("archived", false));
    }

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
        request.message(ClientMessage.addedCategories, 1);
      } else {
        request.error(ClientError.duplicateCategory);
      }
    } else if (vcr.changed) {
      request.message(ClientMessage.updatedCategory);
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

  private ValidateCategoryResult validateCategory(
          final BwRequest request,
          final BwRWActionForm form) {
    final ValidateCategoryResult vcr = new ValidateCategoryResult();

    final BwCategory cat = form.getCategory();

    final BwString str = cat.getWord();
    final String reqstr = request.getReqPar("categoryWord.value");

    if (str == null) {
      if (reqstr != null) {
        vcr.changed = true;
        cat.setWord(new BwString(null, reqstr));
      }
    } else if (reqstr == null) {
      vcr.changed = true;
      cat.deleteWord();
    } else if (str.update(new BwString(null, reqstr))) {
      vcr.changed = true;
    }

    BwString desc = cat.getDescription();
    final String reqDesc = Util.checkNull(request.getReqPar("categoryDescription"));

    if (desc == null) {
      if (reqDesc != null) {
        vcr.changed = true;
        cat.setDescriptionVal(reqDesc);
        desc = cat.getDescription();
      }
    } else if (reqDesc == null) {
      vcr.changed = desc.getValue() != null;
      desc.setValue(null);
    } else if (!reqDesc.equals(desc.getValue())) {
      desc.setValue(reqDesc);
      vcr.changed = true;
    }
    
    final String reqSt = request.getReqPar("categoryStatus");

    if (desc == null) {
      if (reqSt != null) {
        vcr.changed = true;
        desc = new BwString(reqSt, null);
        cat.setDescription(desc);
      }
    } else if (reqSt == null) {
      vcr.changed = desc.getLang() != null;
      desc.setLang(null);
    } else if (!reqSt.equals(desc.getLang())) {
      desc.setLang(reqSt);
      vcr.changed = true;
    }

    if (cat.getWord() == null) {
      request.error(ValidationError.missingCategoryKeyword);
      vcr.ok = false;
    } else if (desc != null) {
      if ((desc.getLang() == null) && (desc.getValue() == null)) {
        cat.deleteDescription();
      }
    }

    return vcr;
  }
}
