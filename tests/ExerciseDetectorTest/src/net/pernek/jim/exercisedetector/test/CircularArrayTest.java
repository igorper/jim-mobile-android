package net.pernek.jim.exercisedetector.test;

import junit.framework.TestCase;
import net.pernek.jim.exercisedetector.alg.CircularArrayFloat;

public class CircularArrayTest extends TestCase {

	public void testBasicOperations() throws Exception {
		CircularArrayFloat queue = new CircularArrayFloat(5);

		assertEquals(0, queue.size());

		queue.enqueue(1);
		queue.enqueue(2);
		queue.enqueue(3);
		assertEquals(3, queue.size());
		queue.enqueue(4);
		assertFalse(queue.isFull());
		queue.enqueue(5);
		assertEquals(1f, queue.first());
		assertTrue(queue.isFull());

		assertEquals(1f, queue.first());
		queue.enqueue(6);
		assertEquals(2f, queue.first());
		assertEquals(6f, queue.last());
		queue.enqueue(7);
		assertEquals(7f, queue.last());
		queue.enqueue(8);
		assertEquals(8f, queue.last());
		queue.enqueue(9);
		assertEquals(9f, queue.last());
		queue.enqueue(10);
		assertEquals(10f, queue.last());
		queue.enqueue(11);
		assertEquals(11f, queue.last());
		queue.enqueue(12);
		assertEquals(12f, queue.last());
		//queue.removeFromHead(4);
		//assertEquals(1, queue.size());
		//queue.removeFromHead(10);
		//assertEquals(0, queue.size());
	}
}
