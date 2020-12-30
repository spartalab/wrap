package edu.utexas.wrap.net;

import edu.utexas.wrap.modechoice.Mode;

/**A roadway which charges a toll for various modes of travel
 * 
 * @author William
 *
 */
public abstract class TolledLink extends Link {
	

	public TolledLink(Node tail, Node head, Float capacity, Float length, Float fftime, Integer linkID) {
		super(tail, head, capacity, length, fftime,linkID);
	}

	public abstract Float getToll(Mode c);
	
	public abstract double tollPrime();
}

