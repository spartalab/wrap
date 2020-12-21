package edu.utexas.wrap.distribution;

/**A class which returns the impedance for trips
 * between two TSZs based on the generic cost of
 * travel between the two
 * 
 * @author William
 *
 */
public interface FrictionFactorMap {

	public Float get(float skimCost);

}
