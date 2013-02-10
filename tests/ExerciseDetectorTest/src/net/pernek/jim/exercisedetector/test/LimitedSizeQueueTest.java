package net.pernek.jim.exercisedetector.test;

import net.pernek.jim.exercisedetector.alg.LimitedSizeQueue;
import junit.framework.TestCase;

public class LimitedSizeQueueTest extends TestCase {

	public void testBasicOperations(){
		LimitedSizeQueue<Integer> queue = LimitedSizeQueue.<Integer>create(5);
		assertEquals(0, queue.size());
		
		queue.add(1);
		queue.add(2);
		queue.add(3);
		assertEquals(3, queue.size());
		queue.add(4);
		queue.add(5);
		
		assertEquals(new Integer(1), queue.peek());
		queue.add(6);
		assertEquals(new Integer(2), queue.peek());
	}
}
