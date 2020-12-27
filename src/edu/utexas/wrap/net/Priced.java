package edu.utexas.wrap.net;

import edu.utexas.wrap.modechoice.Mode;

/**A facility that incurs a price based on the mode of transport and the value of time
 * 
 * @author William
 *
 */
public interface Priced {

	public double getPrice(Float vot, Mode c);

}
