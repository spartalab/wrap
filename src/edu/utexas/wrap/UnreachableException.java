package edu.utexas.wrap;

public class UnreachableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6856927021111691245L;
	public Double demand;

	public UnreachableException() {
		// TODO Auto-generated constructor stub
	}
	
	public UnreachableException(Node to, Bush from) {
		this(to.toString()+" unreachable from "+from.getOrigin().toString()+", demand="+from.getDemand(to.getID()));
		demand = from.getDemand(to.getID());
	}

	public UnreachableException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public UnreachableException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public UnreachableException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UnreachableException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
