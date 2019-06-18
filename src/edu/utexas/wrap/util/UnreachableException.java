package edu.utexas.wrap.util;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.net.Node;

public class UnreachableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6856927021111691245L;
	public Float demand;
	public Node to, from;

	public UnreachableException() {
	}
	
	public UnreachableException(Node to, Bush from) {
		this(to.toString()+" unreachable from "+from.getOrigin().getNode().toString()+", demand="+from.getDemand(to));
		this.to = to;
		this.from = from.getOrigin().getNode();
		demand = from.getDemand(to);
	}

	public UnreachableException(String arg0) {
		super(arg0);
	}

	public UnreachableException(Throwable cause) {
		super(cause);
	}

	public UnreachableException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnreachableException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
