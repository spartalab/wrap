package edu.utexas.wrap.util;

import java.util.ArrayList;
import java.util.List;

public class FibonacciLeaf<E>{
	public E node;
	public double key;
	public int degree;
	public FibonacciLeaf<E> parent;
	public List<FibonacciLeaf<E>> child;
	public boolean mark;
	
	public FibonacciLeaf(E n, Double d) {
		this.node = n;
		this.key = d;
		degree = 0;
		parent = null;
		child = new ArrayList<FibonacciLeaf<E>>();
		mark = false;
	}

	@Override
	public String toString() {
		return "Leaf\t"+node.toString()+"\t"+key;
	}

}