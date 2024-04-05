/*
 * MIT Licence
 * 
 * Copyright (c) 2024 DaveLaw
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicence, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.davelaw.thumbnail;

import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * This Proggy shows how Thumbnailators
 * <a href="https://github.com/coobird/thumbnailator/blob/master/src/main/java/net/coobird/thumbnailator/resizers/ProgressiveBilinearResizer.java">
 * ProgressiveBilinearResizer</a>
 * could be improved.
 * 
 * @author DaveLaw (david.law@apconsult.de)
 */
public class ProgressiveBilinearResizer {

	private static final char TAB = '\t';

	public  static void resize(final BufferedImage srcImage, final BufferedImage tgtImage) {

		/**/  int srcWidth  = srcImage.getWidth();
		/**/  int srcHeight = srcImage.getHeight();

		final int tgtWidth  = tgtImage.getWidth();
		final int tgtHeight = tgtImage.getHeight();

		System.out.println("**RESIZE.: " + srcWidth + '*' + srcHeight + TAB + "-> " + tgtWidth + '*' + tgtHeight);

		final List<Dimension> targets = ResizerConfig.getIntermediateTargets(srcWidth, srcHeight, tgtWidth, tgtHeight);

		final Graphics2D      g2dTgt  = createGraphics(tgtImage);
		/*
		 * If Image size is unchanged or the resize can be done with a single operation,
		 * write the result directly to the Target Image & we're done...
		 */
		if (targets.isEmpty()) {
			drawImageToGraphics("SRC->TGT.: ", srcImage, g2dTgt, tgtWidth, tgtHeight);
			/**/                                         g2dTgt.dispose();
			return;
		}
		/*
		 * More than 1 resize operation is required, so an intermediate Image will be needed.
		 * This Image must be large enough to accomodate the largest intermediate Target Width & Height.
		 */
		final Dimension     tmpImageSize = ResizerConfig.getMaxWidthAndHeight(targets);
		final BufferedImage tmpImage     = new BufferedImage(tmpImageSize.width, tmpImageSize.height, tgtImage.getType());
		
		final Graphics2D    g2dTMP       = createGraphics(tmpImage);

		for (int t=0; t < targets.size(); t++) {

			final Dimension     target = targets.get(t);
			final String        action = t == 0  ?  "SRC->wrk.: "  :  "wrk->wrk.: ";
			final BufferedImage srcTMP = t == 0  ?  srcImage       :  tmpImage;

			drawImageToGraphics(action, srcTMP, g2dTMP, srcWidth, srcHeight, target.width, target.height);

			srcWidth  = target.width;
			srcHeight = target.height;
		}
		g2dTMP.dispose();

		drawImageToGraphics("wrk->TGT.: ", tmpImage, g2dTgt, srcWidth, srcHeight, tgtWidth, tgtHeight);
		/**/                                         g2dTgt.dispose();
	}

	private static Graphics2D createGraphics(final BufferedImage img) {

		final Graphics2D g2d = img.createGraphics();
		/**/             g2d.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
		/**/             g2d.setComposite(AlphaComposite.Src);

		return           g2d;
	}

	private static void drawImageToGraphics(final String txt, final BufferedImage image, final Graphics2D g2d,                                      final int tw, final int th) {
		/**/            drawImageToGraphics(             txt,                     image,                  g2d, image.getWidth(), image.getHeight(),           tw,           th);
	}
	private static void drawImageToGraphics(final String txt, final BufferedImage image, final Graphics2D g2d, final int sw,     final int sh,      final int tw, final int th) {

		g2d.drawImage(image, 0, 0, tw, th, 0, 0, sw, sh, null);

		System.out.println(txt + sw + "*" + sh + TAB + "-> " + tw + "*" + th);
	}
}
