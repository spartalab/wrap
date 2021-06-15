/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
