package edu.utexas.wrap.util;

import java.util.stream.Stream;

import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughModalPAMatrix;
import edu.utexas.wrap.demand.containers.ODPassthroughMatrix;

public class PassengerVehicleTripConverter {

	private final float vot;
	
	public PassengerVehicleTripConverter(float vot) {
		this.vot = vot;
	}
	
	public Stream<ODMatrix> convert(Stream<ModalPAMatrix> modalPAMtxs) {
		return modalPAMtxs.map(
				pa -> new ODPassthroughMatrix(
						new FixedMultiplierPassthroughModalPAMatrix(
								1.0/pa.getMode().occupancy(), 
								pa),
						vot
						)
				);
	}

}
