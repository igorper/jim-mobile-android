package net.pernek.jim.exercisedetector.test;

import junit.framework.TestCase;
import net.pernek.jim.exercisedetector.alg.CircularArrayInt;

public class CircularArrayTest extends TestCase {

	public void testBasicOperations() throws Exception {
		CircularArrayInt queue = new CircularArrayInt(5);

		assertEquals(0, queue.size());

		queue.enqueue(1);
		queue.enqueue(2);
		queue.enqueue(3);
		assertEquals(3, queue.size());
		queue.enqueue(4);
		assertFalse(queue.isFull());
		queue.enqueue(5);
		assertEquals(1, queue.first());
		assertTrue(queue.isFull());

		assertEquals(1, queue.first());
		queue.enqueue(6);
		assertEquals(2, queue.first());
		assertEquals(6, queue.last());
		queue.enqueue(7);
		assertEquals(7, queue.last());
		queue.enqueue(8);
		assertEquals(8, queue.last());
		queue.enqueue(9);
		assertEquals(9, queue.last());
		queue.enqueue(10);
		assertEquals(10, queue.last());
		queue.enqueue(11);
		assertEquals(11, queue.last());
		queue.enqueue(12);
		assertEquals(12, queue.last());
	}
}
