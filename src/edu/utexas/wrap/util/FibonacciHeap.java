package edu.utexas.wrap.util;

import java.util.AbstractQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class FibonacciHeap<E> extends AbstractQueue<FibonacciLeaf<E>>{
	private Integer n;
	private FibonacciLeaf<E> min;
	private List<FibonacciLeaf<E>> rootList;
	private Map<E,FibonacciLeaf<E>> map;
	
	public FibonacciHeap() {
		this(16,0.75f);
	}
	
	public FibonacciHeap(Integer size) {
		this(size,0.75f);
	}
	
	public FibonacciHeap(Integer size, Float loadFactor){
		n = 0;
		min = null;
		rootList = new LinkedList<FibonacciLeaf<E>>();
		map = new Object2ObjectOpenHashMap<E,FibonacciLeaf<E>>(size,loadFactor);
	}
	
	public boolean add(E node, Float d) {
		if (map.containsKey(node)) throw new UnsupportedOperationException("Duplicate node in Fibonacci Heap. Keys must be unique.");
		FibonacciLeaf<E> e = new FibonacciLeaf<E>(node,d.floatValue());
		map.put(node, e);
		
		return offer(e);

	}

	private void cascadingCut(FibonacciLeaf<E> y) {
		FibonacciLeaf<E> z = y.parent;
		if (z != null) {
			if (!y.mark) y.mark = true;
			else {
				cut(y,z);
				cascadingCut(z);
			}
		}
	}


	private void consolidate() {
		Map<Integer, FibonacciLeaf<E>> A = new Int2ObjectOpenHashMap<FibonacciLeaf<E>>();
		Set<FibonacciLeaf<E>> ignore = new ObjectOpenHashSet<FibonacciLeaf<E>>();
		for (FibonacciLeaf<E> w : rootList) {
			if (ignore.contains(w)) continue;
			FibonacciLeaf<E> x = w;
			Integer d = x.degree;
			while (A.get(d) != null) {
				FibonacciLeaf<E> y = A.get(d);
				if (x.key > y.key) {
					FibonacciLeaf<E> temp = x;
					x = y;
					y = temp;
				}
				link(x,y, ignore);
				A.remove(d);
				d++;
			}
			A.put(d, x);
		}
		for (FibonacciLeaf<E> w : ignore) rootList.remove(w);
		min = null;
		for (Integer i : new PriorityQueue<Integer>(A.keySet())) {
			FibonacciLeaf<E> ai = A.get(i);
			if (ai != null) {
				if (min == null) {
					rootList = new LinkedList<FibonacciLeaf<E>>();
					rootList.add(ai);
					min = ai;
				}
				else {
					rootList.add(ai);
					if (ai.key < min.key) {
						min = ai;
					}
				}
			}
		}
	}

	private void cut(FibonacciLeaf<E> x, FibonacciLeaf<E> y) {
		y.child.remove(x);
		y.degree--;
		rootList.add(x);
		x.parent = null;
		x.mark = false;
	}

	public void decreaseKey(FibonacciLeaf<E> x, Float alt) {
		if (alt > x.key) return; //throw new Exception();
		x.key = alt.floatValue();
		FibonacciLeaf<E> y = x.parent;
		if (y != null && x.key < y.key) {
			cut(x,y);
			cascadingCut(y);
		}
		if (x.key < min.key) min = x; 
	}

	public void delete(E n) throws Exception {
		FibonacciLeaf<E> x = map.remove(n);
		decreaseKey(x,-Float.MAX_VALUE);
		poll();
	}

	public FibonacciLeaf<E> getLeaf(E head) {
		return map.get(head);
	}

	@Override
	public boolean isEmpty() {
		return n == 0;
	}

	@Override
	public Iterator<FibonacciLeaf<E>> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	private void link(FibonacciLeaf<E> x, FibonacciLeaf<E> y, Set<FibonacciLeaf<E>> ignore) {
		ignore.add(y);
		x.child.add(y);
		x.degree++;
		y.parent = x;
		y.mark = false;
	}

	@Override
	public boolean offer(FibonacciLeaf<E> e) {
		rootList.add(e);
		if (min == null || e.key < min.key) min = e;
		
		n++;
		return true;
	}
	
	@Override
	public FibonacciLeaf<E> peek() {
		return min;
	}

	//TODO: Discuss usage of this method re: duplicating leaves before release
//	public FibonacciHeap<E> merge(FibonacciHeap<E> h2) {
//		FibonacciHeap<E> h = new FibonacciHeap<E>();
//		h.min = min;
//		h.rootList = rootList;
//		h.rootList.addAll(h2.rootList);
//		
//		if (min==null || (h2.min!=null && h2.min.key < min.key)) {
//			h.min = h2.min;
//		}
//		h.n = n+h2.n;
//		return h;
//	}

	@Override
	public FibonacciLeaf<E> poll() {
		FibonacciLeaf<E> z = min;
		if (z != null) {
			for (FibonacciLeaf<E> x : z.child) {
				rootList.add(x);
				x.parent = null;
			}
			rootList.remove(z);
			if (rootList.isEmpty()) {
				min = null;
			}
			else {
				min = rootList.get(0);
				consolidate();
			}
			n--;
		}
		return z;
	}

	@Override
	public int size() {
		return n;
	}
	
	@Override
	public String toString() {
		return "Heap size="+size()+"\tmin="+min.toString();
	}
}
