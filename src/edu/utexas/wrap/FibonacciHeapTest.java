package edu.utexas.wrap;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Random;

class FibonacciHeapTest {
	private Random random = new Random();
	@Test
	void test() throws Exception {
		FibonacciHeap<Node> heap = new FibonacciHeap<Node>();
		random.setSeed(10L);
		for (Integer i = 0; i < 50000; i++) {
			Double key = random.nextDouble();
			Node p = new Node(i);
			heap.add(p,key);
		}
		assertTrue(isOrdered(heap));
	}
	
	boolean isOrdered(FibonacciHeap<Node> heap) {
		Leaf<Node> last = null, comp = null;
		while (!heap.isEmpty()) {
			if (last == null) last = heap.poll();
			comp = heap.poll();
			if (comp.key < last.key) return false;
			last = comp;
		}
		return true;
	}

}
