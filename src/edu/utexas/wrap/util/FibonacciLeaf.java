package edu.utexas.wrap.util;

import java.util.LinkedList;
import java.util.List;

public class FibonacciLeaf<E>{
	public E n;
	public Double key;
	public Integer degree;
	public FibonacciLeaf<E> parent;
	public List<FibonacciLeaf<E>> child;
	public Boolean mark;
	
	public FibonacciLeaf(E n, Double d) {
		this.n = n;
		this.key = d;
		degree = 0;
		parent = null;
		child = new LinkedList<FibonacciLeaf<E>>();
		mark = false;
	}

	@Override
	public String toString() {
		return "Leaf\t"+n.toString()+"\t"+key.toString();
	}

}