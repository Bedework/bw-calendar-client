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

package org.bedework.appcommon;

import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/** Some image processing code -makes use of the library from
 * http://www.thebuzzmedia.com/software/imgscalr-java-image-scaling-library/
 *
 * <p>We need to avoid storing the entire image in memory as it
 * could be very large.</p>
 *
 * <p>These images will also be written to a Blob via an InputTsream</p>
 *
 * <p>Unfortunately none of the image classes provide a way to get
 * an InputStream so we're forced to write out the image to a file
 * so we can get a FileInputStream - even though a BufferedImage is
 * probably already in a file.</p>
 *
 * @author  Mike Douglass douglm
 */
public class ImageProcessing {
  private ImageProcessing() {}

  private static class ImagesImpl implements Images {
    private BufferedImage image;
    private BufferedImage thumbImage;

    private File imageFile;
    private File thumbFile;

    @Override
    public Image getImage() {
      return image;
    }

    @Override
    public Image getThumbnail() {
      return thumbImage;
    }

    @Override
    public long getLength() {
      return imageFile.length();
    }

    @Override
    public long getThumbLength() {
      if (thumbImage == null) {
        return 0;
      }

      return thumbFile.length();
    }

    @Override
    public InputStream getInputStream() {
      try {
        return new FileInputStream(imageFile);
      } catch (final FileNotFoundException fnfe) {
        throw new RuntimeException(fnfe);
      }
    }

    @Override
    public InputStream getThumbInputStream() {
      if (thumbImage == null) {
        return null;
      }

      try {
        return new FileInputStream(thumbFile);
      } catch (final FileNotFoundException fnfe) {
        throw new RuntimeException(fnfe);
      }
    }

    @Override
    public void close() throws IOException {
    }
  }

  /**
   * @param fileContent for full image
   * @param imageType for image, e.g "jpeg"
   * @param thumbType for thumb - null if none desired
   * @param targetSize for thumb
   * @return Object with one or more images
   */
  public static Images createImages(
          final InputStream fileContent,
          final String imageType,
          final String thumbType,
          final int targetSize) {
    final ImagesImpl ii = new ImagesImpl();
    try {
      ii.image = ImageIO.read(fileContent);
      final String itype;

      if (imageType == null) {
        itype = "png";
      } else {
        itype = imageType;
      }

      ii.imageFile = File.createTempFile("uploadedImage", itype);

      final var fileOut = new FileOutputStream(ii.imageFile);
      ImageIO.write(ii.image, imageType, fileOut);
      fileOut.close();

      if (thumbType != null) {
        ii.thumbImage = Scalr.resize(ii.image, targetSize);
        ii.thumbFile = File.createTempFile("generatedThumbnail",
                                           thumbType);

        final var thumbOut = new FileOutputStream(ii.thumbFile);
        ImageIO.write(ii.thumbImage, thumbType, thumbOut);
        thumbOut.close();
      }
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }

    return ii;
  }
}

