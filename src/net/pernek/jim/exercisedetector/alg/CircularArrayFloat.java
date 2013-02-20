package net.pernek.jim.exercisedetector.alg;


public class CircularArrayFloat{
	private int front, rear, count;
	private float[] queue;

	public CircularArrayFloat(int size) {
		front = rear = count = 0;
		queue = new float[size];
	}

	public void enqueue(float element) {
		if (size() == queue.length){
			front = (front + 1) % queue.length;
			count--;
		}

		queue[rear] = element;
		rear = (rear + 1) % queue.length;

		count++;
	}

	public float dequeue() throws Exception {
		if (isEmpty())
			throw new Exception("empty");

		float result = queue[front];
		front = (front + 1) % queue.length;

		count--;

		return result;
	}

	public float first() throws Exception {
		if (isEmpty())
			throw new Exception("empty");

		return queue[front];
	}
	
	public float last() throws Exception {
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
	
	public float[] getFullValues() throws Exception{
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