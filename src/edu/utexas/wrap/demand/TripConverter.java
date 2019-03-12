package edu.utexas.wrap.demand;

import edu.utexas.wrap.demand.containers.ModalODHashMatrix;

public abstract class TripConverter {

	public abstract ModalODHashMatrix convert(ModalPAMatrix in);
}
