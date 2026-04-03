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
package org.bedework.util.struts;

import org.bedework.util.webaction.UploadFileInfo;
import org.bedework.util.webaction.WebActionForm;

import org.apache.struts2.dispatcher.multipart.UploadedFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class provides some convenience methods for use by Action objects.
 *
 * @author Mike Douglass
 */
public class UtilActionForm implements WebActionForm {
  /* URL Components are here for the benefit of jsp to avoid cluttering up
     pages with code.
   */

  /* ...........................................................
   *                       Uploads and exports
   * ........................................................... */

  private List<UploadedFile> uploadFiles;

  /* ==========================================================
   *                          Methods
     ========================================================== */

  /* ================ Properties methods ============== */

  @Override
  public void reset(final HttpServletRequest request) {
    // Default implementation does nothing
  }

  /* ==========================================================
   *                   Uploads and exports
   * ========================================================== */

  /**
   * @param val Form files
   */
  public void setUploadFiles(final List<UploadedFile> val) {
    uploadFiles = val;
  }

  /**
   * @return FormFile
   */
  public List<UploadedFile> getUploadedFiles() {
    return uploadFiles;
  }

  @Override
  public UploadFileInfo getUploadFileInfo(final String name) {
    if (uploadFiles == null) {
      return null;
    }

    final AtomicReference<UploadFileInfo> info = new AtomicReference<>();

    uploadFiles
        .stream()
        .filter(uf -> uf.getInputName().equals(name))
        .findFirst()
        .ifPresent(uf -> {
          final File theFile;
          if (uf.getContent() instanceof final File file) {
            theFile = file;
          } else {
            theFile = new File(uf.getAbsolutePath());
          }
          info.set(new UploadFileInfoImpl(theFile,
                                          uf.getName(),
                                          uf.getContentType()));
        });

    return info.get();
  }
}

