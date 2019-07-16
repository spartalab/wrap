package edu.utexas.wrap.util;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FibonacciLeaf<E>{
	public E n;
	public double key;
	public int degree;
	public FibonacciLeaf<E> parent;
	public List<FibonacciLeaf<E>> child;
	public boolean mark;
	
	public FibonacciLeaf(E n, Double d) {
		this.n = n;
		this.key = d;
		degree = 0;
		parent = null;
		child = new ObjectArrayList<FibonacciLeaf<E>>();
		mark = false;
	}

	@Override
	public String toString() {
		return "Leaf\t"+n.toString()+"\t"+key;
	}

}