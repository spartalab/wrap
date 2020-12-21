package edu.utexas.wrap.demand;

import java.util.stream.Stream;

/**This interface defines the origin point of daily ODMatrices;
 * that is, any class which implements this interface must define
 * a manner of generating a Stream of ODMatrices which have not
 * had their trips allocated to the various TimePeriods
 * 
 * @author William
 *
 */
public interface DailyODMatrixProvider {

	Stream<ODMatrix> getDailyODMatrices();
}
