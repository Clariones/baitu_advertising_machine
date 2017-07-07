package com.skynet.mediaserver.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import com.skynet.mediaserver.MediaConstants;

public class ImageUtilsTest {

	@Test
	public void test() {
		calcResize("fill", 100, 80, 1000, 800);
		calcResize("fill", 0, 80, 1000, 800);
		calcResize("fill", 100, 0, 1000, 800);
		// fill: 1000x800 => 100x80 (1.250 => 1.250)
		// fill: 1000x800 => 100x80 (1.250 => 1.250)
		// fill: 1000x800 => 100x80 (1.250 => 1.250)
		calcResize("fill", 100, 100, 1000, 800);
		calcResize("fill", 0, 100, 1000, 800);
		calcResize("fill", 100, 0, 1000, 800);
		// fill: 1000x800 => 100x100 (1.250 => 1.000)
		// fill: 1000x800 => 125x100 (1.250 => 1.250)
		// fill: 1000x800 => 100x80 (1.250 => 1.250)
		
		calcResize("fit", 100, 100, 1000, 800);
		calcResize("fit", 0, 100, 1000, 800);
		calcResize("fit", 100, 0, 1000, 800);
	}

	public void calcResize(String scaleType, int resizeW, int resizeH, int orgW, int orgH) {
		// calc the resized result width and height
		double orgScale = orgW * 1.0 / orgH;
		if ("fill".equalsIgnoreCase(scaleType)) {
			if (resizeW == 0) {
				resizeW = (int) (resizeH * orgScale);
			} else if (resizeH == 0) {
				resizeH = (int) (resizeW / orgScale);
			} else {

			}
		} else {
			if (resizeW == 0) {
				resizeW = (int) (resizeH * orgScale);
			} else if (resizeH == 0) {
				resizeH = (int) (resizeW / orgScale);
			} else {
				resizeW = (int) Math.min(resizeW, resizeH * orgScale);
				resizeH = (int) Math.min(resizeH, resizeW / orgScale);
			}
		}

		System.out.printf("%s: %dx%d => %dx%d (%5.3f => %5.3f)\n", scaleType, orgW, orgH, resizeW, resizeH,
				orgW * 1.0 / orgH, resizeW * 1.0 / resizeH);
	}

}
