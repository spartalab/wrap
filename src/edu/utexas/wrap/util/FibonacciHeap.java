package edu.utexas.wrap.util;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.utexas.wrap.net.Node;

public class FibonacciHeap extends AbstractQueue<FibonacciLeaf>{
	private Integer n;
	private FibonacciLeaf min;
	private List<FibonacciLeaf> rootList;
	private Map<Node,FibonacciLeaf> map;
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
		map = new HashMap<Node,FibonacciLeaf>(size,loadFactor);
		rootList = new ArrayList<FibonacciLeaf>();
	}
	
	public boolean add(Node node, Double d) {
		if (map.containsKey(node)) throw new UnsupportedOperationException("Duplicate node in Fibonacci Heap. Keys must be unique.");
		FibonacciLeaf e = new FibonacciLeaf(node,d);
		map.put(node, e);
		
		return offer(e);

	}

	private void cascadingCut(FibonacciLeaf y) {
		FibonacciLeaf z = y.parent;
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
		FibonacciLeaf[] AA = new FibonacciLeaf[D];
		
//		Map<Integer, FibonacciLeaf<E>> A = new ConcurrentHashMap<Integer,FibonacciLeaf<E>>();
		Set<FibonacciLeaf> ignore = (new HashSet<FibonacciLeaf>());
		rootList.parallelStream().filter(x -> !ignore.contains(x)).sequential().forEach(w->{
			FibonacciLeaf x = w;
			Integer d = x.degree;
			FibonacciLeaf y = AA[d];
			while (y != null) {
				
				if (x.key > y.key) {
					FibonacciLeaf temp = x;
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
			FibonacciLeaf ai = AA[i];
			if (ai != null) {
				if (min == null) {
					rootList = new ArrayList<FibonacciLeaf>();
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

	private void cut(FibonacciLeaf x, FibonacciLeaf y) {
		y.child.remove(x);
		y.degree--;
		rootList.add(x);
		x.parent = null;
		x.mark = false;
	}

	public void decreaseKey(FibonacciLeaf x, double maxValue) {
		if (maxValue > x.key) return; //throw new Exception();
		x.key = maxValue;
		FibonacciLeaf y = x.parent;
		if (y != null && x.key < y.key) {
			cut(x,y);
			cascadingCut(y);
		}
		if (x.key < min.key) min = x; 
	}

	public void delete(Node n) throws Exception {
		FibonacciLeaf x = map.remove(n);
		decreaseKey(x,-Double.MAX_VALUE);
		poll();
	}

	public FibonacciLeaf getLeaf(Node head) {
		return map.get(head);
	}

	public boolean isEmpty() {
		return n == 0;
	}

	public Iterator<FibonacciLeaf> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	private void link(FibonacciLeaf x, FibonacciLeaf y, Set<FibonacciLeaf> ignore) {
		ignore.add(y);
		x.child.add(y);
		x.degree++;
		y.parent = x;
		y.mark = false;
	}

	public boolean offer(FibonacciLeaf e) {
		rootList.add(e);
		if (min == null || e.key < min.key) min = e;
		
		n++;
		return true;
	}
	
	public FibonacciLeaf peek() {
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

	public FibonacciLeaf poll() {
		FibonacciLeaf z = min;
		if (z != null) {
			for (FibonacciLeaf x : z.child) {
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
