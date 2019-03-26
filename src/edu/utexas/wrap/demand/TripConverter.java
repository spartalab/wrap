package edu.utexas.wrap.demand;

import edu.utexas.wrap.demand.containers.ModalHashMatrix;

public abstract class TripConverter {

	public abstract ModalHashMatrix convert(ModalPAMatrix in);
}
