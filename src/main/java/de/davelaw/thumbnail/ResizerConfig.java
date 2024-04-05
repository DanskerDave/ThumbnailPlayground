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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This Proggy was inspired by some inadequacies in the Progressive Bilinear Resizer
 * of Thumbnailator, a widespread Thumbnail Utility.
 * <p>
 * Those were.:<br>
 * - poorly structured & overcomplicated<br>
 * - strange size-progression with target sizes larger than the source size<br>
 * - when iteratively resizing, the Image is redundantly drawn an extra time<br>
 * - intermediate result Image is too large<br>
 * <p>
 * See also.:
 * <a href="https://github.com/coobird/thumbnailator/blob/master/src/main/java/net/coobird/thumbnailator/resizers/ProgressiveBilinearResizer.java">
 * Thumbnailator ProgressiveBilinearResizer</a>
 * 
 * @author DaveLaw (david.law@apconsult.de)
 */
public class ResizerConfig {

	/** Resize Multiplier */ private static final int  RESIZE_FACTOR         = 2;
	/** A "few" Pixels    */ public  static final int  RESIZE_SKIP_THRESHOLD = 3;

	private ResizerConfig() {/* Please do not instantiate */}

	/**
	 * Returns a
	 * {@link  Dimension}
	 * containing the maximum Width & Height from <code>targets</code>.
	 * 
	 * @param targets
	 * @return
	 */
	public static Dimension getMaxWidthAndHeight(final List<Dimension> targets) {

		int width  = Integer.MIN_VALUE;
		int height = Integer.MIN_VALUE;

		for (final Dimension target : targets) {
			width  = StrictMath.max(width,  target.width);
			height = StrictMath.max(height, target.height);
		}
		return new Dimension(width, height);
	}

	/**
	 * Get the Intermediate-Target sizes.
	 * <br>
	 * (this is achieved by merging separate lists of the intermediate sizes for Width & Height)
	 * <p>
	 * Restricting the result only to intermediate sizes makes it easier for the calling logic to:<br>
	 * a) resize directly from Source Image to Target Image if there are no intermediate targets<br>
	 * b) repeatedly resize the intermediate targets, overwriting a single (suitably sized) temporary Image<br>
	 * c) resize the final intermediate target to the destination Image
	 * <p>
	 * Note......: the result does <b>not</b> include the Source & Target sizes.
	 * <p>
	 * Exception.: if the number of steps required to resize each Axis is different,
	 *             Source Width or Source Height will be used to pad whichever List is shorter.
	 * 
	 * @param   sourceWidth
	 * @param   sourceHeight
	 * @param   targetWidth
	 * @param   targetHeight
	 * 
	 * @return  a {@link List} of {@link Dimension} containing the intermediate sizes (which may be empty)
	 */
	public  static List<Dimension> getIntermediateTargets(final int sourceWidth, final int sourceHeight, final int targetWidth, final int targetHeight) {
		/*
		 * Get a List of intermediate Target sizes for each Axis...
		 */
		final List<Integer> targetWidths  = getIntermediateTargetSizesForWidthOrHeight(sourceWidth,  targetWidth);
		final List<Integer> targetHeights = getIntermediateTargetSizesForWidthOrHeight(sourceHeight, targetHeight);
		/*
		 * If the List sizes differ, expand whichever is shorter by repeatedly inserting its Source value until the sizes match...
		 */
		for (int t=0, delta=targetWidths .size() - targetHeights.size(); t < delta; t++) {targetHeights.add(0, sourceHeight);}
		for (int t=0, delta=targetHeights.size() - targetWidths .size(); t < delta; t++) {targetWidths .add(0, sourceWidth);}
		/*
		 * Now merge the Widths & Heights (which now have the same number of entries) into a List of Dimension...
		 */
		final  List<Dimension> targets = new ArrayList<Dimension>();

		for (int t=0; t < targetWidths.size(); t++) {

			targets.add(new Dimension(
					targetWidths .get(t),
					targetHeights.get(t)));
		}
		return Collections.unmodifiableList(targets);
	}

	/**
	 * Build a List of <b>Intermediate</b> Target sizes <i>between</i>
	 * {@code  sourceSize}
	 * and
	 * {@code  targetSize}.
	 * <p>
	 * The following description assumes <code>sourceSize >= targetSize</code>.<br>
	 * (if this is not the case, the sizes are swapped before & after, so the same logic can be used)
	 * <p>
	 * The values are calculated by starting with <i>targetSize</i>
	 * and repeatedly multiplying by
	 * {@link  #RESIZE_FACTOR}.
	 * <p>
	 * Finally, if the first resize is insignificant (less than
	 * {@link  #RESIZE_SKIP_THRESHOLD}
	 * pixels) it will be skipped, as it probably reduces quality.
	 * 
	 * @param   sourceSize
	 * @param   targetSize
	 * 
	 * @return  a {@link List} of {@link Integer} containing the intermediate sizes (which may be empty)
	 */
	private static List<Integer> getIntermediateTargetSizesForWidthOrHeight(int sourceSize, int targetSize) {
		/*
		 * Depending on whether we're reducing or expanding the Image,
		 * we may need to prepare by swapping Source & Target to make the following logic simpler...
		 */
		final boolean enlarging = sourceSize < targetSize;

		if (enlarging) {
			final int sourceSizeMemory = sourceSize;
			/**/      sourceSize       = targetSize;
			/**/      targetSize       = sourceSizeMemory;
		}
		/*
		 * COMMON LOGIC BEGIN : use following reducing logic for both reduction & enlargement
		 * >--------------------------------------------------------------------------------<
		 * 
		 * Create a List containing only INTERMEDIATE Target sizes...
		 */
		final  List<Integer> targets = new ArrayList<Integer>();

		long   intermediateTarget    = targetSize * RESIZE_FACTOR; // ("long" makes overflow logic unnecessary)

		while (intermediateTarget    < sourceSize) {

			targets.add(0, (int) intermediateTarget); // Reducing..: Prepend (i.e. List Entries are in Descending order)
			/*
			 * It is just fine to cast the result to int  (above)....
			 * ....as the value can only overflow to long (below) on the final pass & will not be used. 
			 */
			intermediateTarget *= RESIZE_FACTOR;
		}
		/*
		 * If the sizes of the first intermediate result & the Source value only differ by a "few" pixels...
		 * ...skip the first resize as this is more likely to introduce "noise" than quality.
		 */
		if (targets.isEmpty() == false
		&&  targets.get   (0) >= sourceSize - RESIZE_SKIP_THRESHOLD) {
			targets.remove(0);
		}
		/*
		 * >-------------------------------------------------------------------------------<
		 * COMMON LOGIC END : now reverse the target order if it was actually an enlargement
		 */
		if (enlarging) {
			Collections.reverse(targets);
		}
		return targets;
	}
}
