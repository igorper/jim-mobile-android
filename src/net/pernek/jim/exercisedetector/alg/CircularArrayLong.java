package net.pernek.jim.exercisedetector.alg;


public class CircularArrayLong{
	private int front, rear, count;
	private long[] queue;

	public CircularArrayLong(int size) {
		front = rear = count = 0;
		queue = new long[size];
	}

	public void enqueue(long element) {
		if (size() == queue.length){
			front = (front + 1) % queue.length;
			count--;
		}

		queue[rear] = element;
		rear = (rear + 1) % queue.length;

		count++;
	}

	public long dequeue() throws Exception {
		if (isEmpty())
			throw new Exception("empty");

		long result = queue[front];
		front = (front + 1) % queue.length;

		count--;

		return result;
	}

	public long first() throws Exception {
		if (isEmpty())
			throw new Exception("empty");

		return queue[front];
	}
	
	public long last() throws Exception {
		if (isEmpty())
			throw new Exception("empty");

		return queue[(rear + queue.length - 1) % queue.length];
	}

	public boolean isEmpty() {
		return count == 0;
	}

	public int size() {
		return count;
	}
	
	public boolean isFull(){
		return count == queue.length;
	}
	
	public long[] getFullValues() throws Exception{
		if(!isFull()){
			throw new Exception("Array has to be full for this operation");
		}
		
		return queue;
	}
	
	public void removeFromHead(int numValues) throws Exception{
		for(int i=numValues; i > 0; i--){
			dequeue();
		}
	}
}