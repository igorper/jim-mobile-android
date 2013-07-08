package com.trainerjim.android.test;

import junit.framework.TestCase;

import com.trainerjim.android.ui.CircularProgressControl;

public class CircularProgressControlTest extends TestCase {

	public void testCalculateArc(){

		//----- between [0 and 100]
		// test bellow min
		assertEquals(-360f, CircularProgressControl.calculateArc(-100, 100, 0));
		
		// test min boundary
		assertEquals(0f, CircularProgressControl.calculateArc(0, 100, 0));
		
		// test a number in between
		assertEquals(180f, CircularProgressControl.calculateArc(50, 100, 0));
		
		// test msx boundary
		assertEquals(360f, CircularProgressControl.calculateArc(100, 100, 0));
		
		// test above max boundary
		assertEquals(720f, CircularProgressControl.calculateArc(200, 100, 0));
		
		//----- between [200 and 300]
		// test bellow min
		assertEquals(-360f, CircularProgressControl.calculateArc(100, 300, 200));
		
		// test min boundary
		assertEquals(0f, CircularProgressControl.calculateArc(200, 300, 200));
		
		// test a number in between
		assertEquals(180f, CircularProgressControl.calculateArc(250, 300, 200));
		
		// test msx boundary
		assertEquals(360f, CircularProgressControl.calculateArc(300, 300, 200));
		
		// test above max boundary
		assertEquals(720f, CircularProgressControl.calculateArc(400, 300, 200));
		
		//----- between [400 and 600]
		assertEquals(-360f, CircularProgressControl.calculateArc(200, 600, 400));
		
		// test min boundary
		assertEquals(0f, CircularProgressControl.calculateArc(400, 600, 400));
		
		// test a number in between
		assertEquals(180f, CircularProgressControl.calculateArc(500, 600, 400));
		
		// test msx boundary
		assertEquals(360f, CircularProgressControl.calculateArc(600, 600, 400));
		
		// test above max boundary
		assertEquals(720f, CircularProgressControl.calculateArc(800, 600, 400));
		
		
	}
}
