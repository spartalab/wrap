package edu.utexas.wrap.marketsegmentation;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.ImpedanceMatrix;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.util.AggregatePAMatrixCollector;
import javafx.concurrent.Task;

public class PurposeRunner extends Task<Collection<ODProfile>> {

	private final Purpose purpose;
	private MarketRunner parent;
	
	
	public PurposeRunner(Purpose purpose, MarketRunner parent) {
		this.purpose = purpose;
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return purpose.toString();
		
	}

	@Override
	protected Collection<ODProfile> call() throws Exception {
		Collection<TripDistributor> distributors = purpose.getDistributors();
		int maxSteps = 4+distributors.stream().mapToInt(TripDistributor::maxIterations).sum();
		
		updateProgress(0,maxSteps);
		this.updateMessage("Modeling trip generation");
		PAMap map = purpose.getPAMap();
		updateProgress(1,maxSteps);
		
		
		AtomicInteger completedSteps = new AtomicInteger(0);
		
		this.updateMessage("Modeling trip geographic distribution");

		
		AggregatePAMatrix aggregate = distributors.parallelStream().map(distributor -> {
			NetworkSkim currentObservation = purpose.getNetworkSkim(distributor);
			FrictionFactorMap frictionFunction = purpose.getFrictionFunction(distributor);
			ImpedanceMatrix impedances = new ImpedanceMatrix(purpose.getZones(),currentObservation,frictionFunction);
			
			AtomicBoolean converged = new AtomicBoolean(false);
			
			for (int iter = 0; iter < distributor.maxIterations();iter++) {
				if (converged.get()) break;
				converged.set(true);
				distributor.updateProducerWeights(map, impedances,converged);
				distributor.updateAttractorWeights(map,impedances,converged);
				
				updateProgress(1+ completedSteps.incrementAndGet(),maxSteps);
			}
			return distributor.constructMatrix(map,impedances);
		}).collect(new AggregatePAMatrixCollector());
		
		
		
		Thread.sleep(1);
		updateProgress(2.,5.);
		this.updateMessage("Modeling trip mode choice");
		Collection<ModalPAMatrix> modals = purpose.getModalPAMatrices(aggregate);
		Thread.sleep(1);
		updateProgress(3.,5.);
		this.updateMessage("Calculating vehicle trips");
		Collection<ODMatrix> vehicleTrips = purpose.getDailyODMatrices(modals);
		Thread.sleep(1);
		updateProgress(4.,5.);
		this.updateMessage("Modeling trip time distribution");
		Collection<ODProfile> odProfiles = purpose.getODProfiles(vehicleTrips);
		Thread.sleep(1);
		updateProgress(5.,5.);
		Thread.sleep(1);
		return odProfiles;
	}

	@Override
	protected void succeeded() {
		parent.incrementCompletedPurposes();
	}
}
