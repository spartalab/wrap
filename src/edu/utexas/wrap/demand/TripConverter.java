package edu.utexas.wrap.demand;

public abstract class TripConverter {

	public abstract ModalOriginDestinationMatrix convert(ModalProductionAttractionMatrix in);
}
