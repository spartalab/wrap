package edu.utexas.wrap.util;

import java.util.Collection;
import java.util.HashSet;

import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughModalPAMatrix;
import edu.utexas.wrap.demand.containers.ODPassthroughMatrix;
import edu.utexas.wrap.demand.containers.TransposeModalPAMatrix;

public class DepartureArrivalConverter {
	
	double departureRate, arrivalRate;
	
	public DepartureArrivalConverter(double departureRate, double arrivalRate) {
		this.departureRate = departureRate;
		this.arrivalRate = arrivalRate;
	}
	
	public ODMatrix convert(ModalPAMatrix pa, Double vehOccRate) {
		//Take a modal pa matrix and get its transpose
		ModalPAMatrix transpose = new TransposeModalPAMatrix(pa);
		Collection<ModalPAMatrix> matrices = new HashSet<ModalPAMatrix>(2,1.0f);
		
		//multiply the original matrix by the departure rate
		matrices.add(new FixedMultiplierPassthroughModalPAMatrix(departureRate*vehOccRate, pa));
		//multiply the transpose matrix by the arrival rate
		matrices.add(new FixedMultiplierPassthroughModalPAMatrix(arrivalRate*vehOccRate,transpose));

		//sum the two matrices and pass on as an OD matrix
		
		return new ODPassthroughMatrix(matrices.stream().collect(new ModalPAMatrixCollector()));
	}
}
