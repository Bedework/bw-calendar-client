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
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

/** Some image processing code -makes use of the library from
 * http://www.thebuzzmedia.com/software/imgscalr-java-image-scaling-library/
 *
 * @author  Mike Douglass douglm  rpi.edu
 */
public class ImageProcessing {
  private ImageProcessing() {}

  private static class ImageInputStreamWrapper extends InputStream {
    private final ImageInputStream iis;

    private ImageInputStreamWrapper(final ImageInputStream iis) {
      this.iis = iis;
    }

    @Override
    public int read() throws IOException {
      return iis.read();
    }

    public void close() throws IOException {
      iis.close();
    }
  }

  private static class ImagesImpl implements Images {
    private BufferedImage image;
    private BufferedImage thumbImage;

    private ImageInputStream imageIs;
    private ImageInputStream thumbIs;

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
      try {
        return getIs().length();
      } catch (final IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }

    @Override
    public long getThumbLength() {
      if (thumbImage == null) {
        return 0;
      }

      try {
        return getThumbIs().length();
      } catch (final IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }

    @Override
    public InputStream getInputStream() {
      return new ImageInputStreamWrapper(getIs());
    }

    @Override
    public InputStream getThumbInputStream() {
      if (thumbImage == null) {
        return null;
      }

      return new ImageInputStreamWrapper(getThumbIs());
    }

    @Override
    public void close() throws IOException {
      if (imageIs != null) {
        imageIs.close();
      }

      if (thumbIs != null) {
        thumbIs.close();
      }
    }

    private ImageInputStream getIs() {
      if (imageIs != null) {
        return imageIs;
      }

      try {
        imageIs = ImageIO.createImageInputStream(image);
      } catch (final IOException ioe) {
        throw new RuntimeException(ioe);
      }

      return imageIs;
    }

    private ImageInputStream getThumbIs() {
      if (thumbIs != null) {
        return thumbIs;
      }

      try {
        thumbIs = ImageIO.createImageInputStream(image);
      } catch (final IOException ioe) {
        throw new RuntimeException(ioe);
      }

      return thumbIs;
    }
  }

  /**
   * @param fileContent for full image
   * @param imageType for thumb - null if none desired
   * @param targetSize for thumb
   * @return Object with one or more images
   */
  public static Images createImages(
          final InputStream fileContent,
          final String imageType,
          final int targetSize) {
    final ImagesImpl ii = new ImagesImpl();
    try {
      ii.image = ImageIO.read(fileContent);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }

    if (imageType != null) {
      ii.thumbImage = Scalr.resize(ii.image, targetSize);
    }

    return ii;
  }
}

