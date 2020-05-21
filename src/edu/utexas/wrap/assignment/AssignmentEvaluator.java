package edu.utexas.wrap.assignment;

import java.util.stream.Stream;

public interface AssignmentEvaluator<T extends AssignmentContainer> {

	public double getValue(Stream<T> collectorStream);
}
