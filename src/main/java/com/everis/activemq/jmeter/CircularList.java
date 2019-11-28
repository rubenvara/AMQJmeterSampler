package com.everis.activemq.jmeter;

import java.util.ArrayList;
import java.util.List;

public class CircularList<E> {

	List<E> items = null;

	private int currentPosition = -1;

	public CircularList(int maxSize) {
		items = new ArrayList(maxSize);
	}

	/**
	 * Enqueue elements to rear.
	 */
	public void add(E item) {
		if (currentPosition < 0) {
			currentPosition = 0;
		}
		items.add(item);
	}

	/**
	 * Dequeue element from Front.
	 */
	public E hasNext() {
		if (currentPosition != -1) {
			if (currentPosition >= items.size()) {
				currentPosition = 0;
			}
			return items.get(currentPosition++);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return "CircularList [" + (items.size()) + "]";
	}

}
