package edu.utexas.wrap.demand;

/**This interface defines the origin point for AggregatePAMatrices;
 * that is, any class implementing this interface must provide a
 * means to generate an AggregatePAMatrix. The person-trips from
 * this matrix will not yet have been allocated to a particular Mode
 * 
 * @author William
 *
 */
public interface AggregatePAMatrixProvider {

	AggregatePAMatrix getAggregatePAMatrix();
}
