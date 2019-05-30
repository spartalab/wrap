package edu.utexas.wrap.util;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FibonacciLeaf<E>{
	public E n;
	public Float key;
	public Integer degree;
	public FibonacciLeaf<E> parent;
	public List<FibonacciLeaf<E>> child;
	public Boolean mark;
	
	public FibonacciLeaf(E n, Float d) {
		this.n = n;
		this.key = d;
		degree = 0;
		parent = null;
		child = new ObjectArrayList<FibonacciLeaf<E>>();
		mark = false;
	}

	@Override
	public String toString() {
		return "Leaf\t"+n.toString()+"\t"+key.toString();
	}

}