package edu.utexas.wrap.tests.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.FibonacciHeap;
import edu.utexas.wrap.util.FibonacciLeaf;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class FibonacciHeapTest {
	private Random random = new Random();
	@Test
	void test() throws Exception {
		Integer numCases = 50000;
		FibonacciHeap<Node> heap = new FibonacciHeap<Node>();
		Map<Integer, Node> map = new HashMap<Integer,Node>();
		random.setSeed(90210L);
		
		for (Integer i = 0; i < numCases; i++) {
			Double key = random.nextDouble();
			//System.out.println(key);
			Node p = new Node(i,i,null);
			map.put(i, p);
			heap.add(p,key);
		}
		System.out.println("-----");
		for (Integer j = 0; j < 2; j++) {
			for (Integer i = 0; i < numCases; i++) {
				Float key = random.nextFloat();
				//System.out.println(key);
				FibonacciLeaf<Node> p = heap.getLeaf(map.get(i));
				heap.decreaseKey(p, key);
			}
			System.out.println("-----");
		}
		assertTrue(valid(heap,numCases));
	}
	
	boolean valid(FibonacciHeap<Node> heap, Integer numCases) {
		FibonacciLeaf<Node> last = null, comp = null;
		Integer count = 0;
		while (!heap.isEmpty()) {
			if (last == null) {
				last = heap.poll();
				count++;
				//System.out.println(last);
			}
			comp = heap.poll();
			count++;
			//System.out.println(comp);
			if (comp.key < last.key) return false;
			last = comp;
		}
		return count.equals(numCases);
	}

}
