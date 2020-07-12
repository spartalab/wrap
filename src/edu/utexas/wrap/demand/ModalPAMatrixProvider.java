package edu.utexas.wrap.demand;

import java.util.stream.Stream;

public interface ModalPAMatrixProvider {
	
	Stream<ModalPAMatrix> getModalPAMatrices();
	
}
