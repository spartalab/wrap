package edu.utexas.wrap.util;

import java.util.ArrayList;
import java.util.List;
import edu.utexas.wrap.net.Node;

public class FibonacciLeaf{
	public Node node;
	public double key;
	public int degree;
	public FibonacciLeaf parent;
	public List<FibonacciLeaf> child;
	public boolean mark;
	
	public FibonacciLeaf(Node n, Double d) {
		this.node = n;
		this.key = d;
		degree = 0;
		parent = null;
		child = new ArrayList<FibonacciLeaf>();
		mark = false;
	}

	@Override
	public String toString() {
		return "Leaf\t"+node.toString()+"\t"+key;
	}

}