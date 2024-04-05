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

import static de.davelaw.thumbnail.ResizerConfig.RESIZE_SKIP_THRESHOLD;

import java.awt.Dimension;
import java.util.List;

public class ResizerConfigTest {

	private static final char TAB    = '\t';
	private static final int  EIGHTY = 80;
	private static final int  SIXTY  = 60;
	private static final int  FOUR   =  4;

	public  static void main(final String[] args) {

		test( (EIGHTY     ),                             (SIXTY     ),                                FOUR,   EIGHTY);

		test( (EIGHTY     ),                             (SIXTY     ),                                EIGHTY, SIXTY);

		test( (EIGHTY << 4),                             (SIXTY << 4),                                EIGHTY, SIXTY);
		test( (EIGHTY << 4),                             (SIXTY << 2),                                EIGHTY, SIXTY);
		test( (EIGHTY << 2),                             (SIXTY << 4),                                EIGHTY, SIXTY);

		test( (EIGHTY << 1) + 1 + RESIZE_SKIP_THRESHOLD, (SIXTY << 1) + 1 + RESIZE_SKIP_THRESHOLD,    EIGHTY, SIXTY);
		test( (EIGHTY << 1) + 0 + RESIZE_SKIP_THRESHOLD, (SIXTY << 1) + 0 + RESIZE_SKIP_THRESHOLD,    EIGHTY, SIXTY);
		test( (EIGHTY << 1),                             (SIXTY << 1),                                EIGHTY, SIXTY);
	}

	private static void test(final int sw, final int sh, final int tw, final int th) {

		resize(sw, sh, tw, th);
		resize(tw, th, sw, sh);
	}

	private static void resize(int sw, int sh, final int tw, final int th) {

		final List<Dimension> targets =  ResizerConfig.getIntermediateTargets(sw, sh, tw, th);

		System    .out.println("**RESIZE.: " + sw + '*' + sh + TAB + "-> " + tw           + '*' + th);

		if (targets.isEmpty()) {
			System.out.println("SRC->TGT.: " + sw + '*' + sh + TAB + "-> " + tw           + '*' + th);
			System.out.println();
			return;
		}

		for (int t=0; t < targets.size(); t++) {

			final Dimension target = targets.get(t);

			final String    action = t == 0  ?  "SRC->wrk.: "  :  "wrk->wrk.: ";
			
			System.out.println(action        + sw + '*' + sh + TAB + "-> " + target.width + '*' + target.height);

			sw = target.width;
			sh = target.height;
		}
		System    .out.println("wrk->TGT.: " + sw + '*' + sh + TAB + "-> " + tw           + '*' + th);
		System    .out.println();
	}
}
