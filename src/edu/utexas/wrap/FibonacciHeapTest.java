package edu.utexas.wrap;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;

class FibonacciHeapTest {
	private Random random = new Random();
	@Test
	void test() throws Exception {
		int numCases = 50000;
		FibonacciHeap<Node> heap = new FibonacciHeap<Node>();
		HashMap<Integer, Node> map = new HashMap<Integer, Node>();
		random.setSeed(90210L);
		
		for (Integer i = 0; i < numCases; i++) {
			Double key = random.nextDouble();
			//System.out.println(key);
			Node p = new Node(i);
			map.put(i, p);
			heap.add(p,key);
		}
		System.out.println("-----");
		for (Integer j = 0; j < 2; j++) {
			for (Integer i = 0; i < numCases; i++) {
				Double key = random.nextDouble();
				//System.out.println(key);
				Leaf<Node> p = heap.getLeaf(map.get(i));
				heap.decreaseKey(p, key);
			}
			System.out.println("-----");
		}
		assertTrue(isOrdered(heap));
	}
	
	boolean isOrdered(FibonacciHeap<Node> heap) {
		Leaf<Node> last = null, comp = null;
		while (!heap.isEmpty()) {
			if (last == null) {
				last = heap.poll();
				//System.out.println(last);
			}
			comp = heap.poll();
			//System.out.println(comp);
			if (comp.key < last.key) return false;
			last = comp;
		}
		return true;
	}

}
