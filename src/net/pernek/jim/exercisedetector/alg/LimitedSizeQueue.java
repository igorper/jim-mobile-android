package net.pernek.jim.exercisedetector.alg;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;

public class LimitedSizeQueue<T> extends AbstractQueue<T> {

	private int mMaxSize;
	
	private LinkedList<T> mQueue;
	
	public static <T> LimitedSizeQueue<T> create(int maxSize){
		LimitedSizeQueue<T> retVal = new LimitedSizeQueue<T>();
		retVal.mQueue = new LinkedList<T>();
		retVal.mMaxSize = maxSize;
		
		return retVal;
	}
	
	@Override
	public boolean offer(T e) {
		boolean res = mQueue.add(e);
		
		while(mQueue.size() > mMaxSize){
			mQueue.remove();
		}
		
		return res;
	}

	@Override
	public T peek() {
		return mQueue.peek();
	}

	@Override
	public T poll() {
		return mQueue.poll();
	}

	@Override
	public Iterator<T> iterator() {
		return mQueue.iterator();
	}

	@Override
	public int size() {
		return mQueue.size();
	}
	
	public int getMaxSize(){
		return mMaxSize;
	}
	
	public T getLast(){
		return mQueue.getLast();
	}
	
	public boolean isFull(){
		return mQueue.size() == mMaxSize;
	}

}