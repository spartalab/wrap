package edu.utexas.wrap.demand;

import java.util.stream.Stream;

/**This interface defines the origin point of ModalPAMatrices;
 * that is, any class implementing this interface must provide
 * a means to generate a Stream of ModalPAMatrices
 * 
 * @author William
 *
 */
public interface ModalPAMatrixProvider {
	
	Stream<ModalPAMatrix> getModalPAMatrices();
	
}
