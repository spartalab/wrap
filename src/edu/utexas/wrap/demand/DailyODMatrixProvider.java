package edu.utexas.wrap.demand;

import java.util.stream.Stream;

public interface DailyODMatrixProvider {

	Stream<ODMatrix> getDailyODMatrices();
}
