package net.pernek.jim.exercisedetector.alg;


public class CircularArrayInt{
	private int front, rear, count;
	private int[] queue;

	public CircularArrayInt(int size) {
		front = rear = count = 0;
		queue = new int[size];
	}

	public void enqueue(int element) {
		if (size() == queue.length){
			front = (front + 1) % queue.length;
			count--;
		}

		queue[rear] = element;
		rear = (rear + 1) % queue.length;

		count++;
	}

	public int dequeue() throws Exception {
		if (isEmpty())
			throw new Exception("empty");

		int result = queue[front];
		front = (front + 1) % queue.length;

		count--;

		return result;
	}

	public int first() throws Exception {
		if (isEmpty())
			throw new Exception("empty");

		return queue[front];
	}
	
	public int last() throws Exception {
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
	
	// careful!! Those values are not ordered from head to tail
	// their order is set by the underlying array
	public int[] getFullValues() throws Exception{
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