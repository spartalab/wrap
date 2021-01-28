package edu.utexas.wrap.util;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FibonacciHeap<E> extends AbstractQueue<FibonacciLeaf<E>>{
	private Integer n;
	private FibonacciLeaf<E> min;
	private List<FibonacciLeaf<E>> rootList;
	private Map<E,FibonacciLeaf<E>> map;
	private final double phi = Math.log10((1+Math.sqrt(5))/2);
	
	public FibonacciHeap() {
		this(16,0.75f);
	}
	
	public FibonacciHeap(Integer size) {
		this(size,0.75f);
	}
	
	public FibonacciHeap(Integer size, Float loadFactor){
		n = 0;
		min = null;
//		rootList = new ConcurrentLinkedQueue<FibonacciLeaf<E>>();
		map = new HashMap<E,FibonacciLeaf<E>>(size,loadFactor);
		rootList = new ArrayList<FibonacciLeaf<E>>();
	}
	
	public boolean add(E node, Double d) {
		if (map.containsKey(node)) throw new UnsupportedOperationException("Duplicate node in Fibonacci Heap. Keys must be unique.");
		FibonacciLeaf<E> e = new FibonacciLeaf<E>(node,d);
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
		double dn = Math.log10(n)/phi;
		int D = (int) Math.ceil(dn);
		FibonacciLeaf<E>[] AA = new FibonacciLeaf[D];
		
//		Map<Integer, FibonacciLeaf<E>> A = new ConcurrentHashMap<Integer,FibonacciLeaf<E>>();
		Set<FibonacciLeaf<E>> ignore = (new HashSet<FibonacciLeaf<E>>());
		rootList.stream()
		.filter(x -> !ignore.contains(x))
		.forEach(w->{
			FibonacciLeaf<E> x = w;
			Integer d = x.degree;
			FibonacciLeaf<E> y = AA[d];
			while (y != null) {
				
				if (x.key > y.key) {
					FibonacciLeaf<E> temp = x;
					x = y;
					y = temp;
				}
				link(x,y, ignore);
				AA[d++]=null;
				y = AA[d];
			}
			AA[d]= x;
		});

		rootList.removeAll(ignore);

		min = null;
		for (int i = 0; i < D; i++) {
			FibonacciLeaf<E> ai = AA[i];
			if (ai != null) {
				if (min == null) {
					rootList = new ArrayList<FibonacciLeaf<E>>();
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

	public void decreaseKey(FibonacciLeaf<E> x, double maxValue) {
		if (maxValue > x.key) return; //throw new Exception();
		x.key = maxValue;
		FibonacciLeaf<E> y = x.parent;
		if (y != null && x.key < y.key) {
			cut(x,y);
			cascadingCut(y);
		}
		if (x.key < min.key) min = x; 
	}

	public void delete(E n) throws Exception {
		FibonacciLeaf<E> x = map.remove(n);
		decreaseKey(x,-Double.MAX_VALUE);
		poll();
	}

	public FibonacciLeaf<E> getLeaf(E head) {
		return map.get(head);
	}

	public boolean isEmpty() {
		return n == 0;
	}

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

	public boolean offer(FibonacciLeaf<E> e) {
		rootList.add(e);
		if (min == null || e.key < min.key) min = e;
		
		n++;
		return true;
	}
	
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

	public int size() {
		return n;
	}
	
	public String toString() {
		return "Heap size="+size()+"\tmin="+min.toString();
	}
}
