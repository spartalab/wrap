package edu.utexas.wrap.marketsegmentation;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.gui.RunnerController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

public class MarketRunner extends Task<Collection<ODProfile>> {
	
	private final Market market;
	private Collection<PurposeRunner> purposeRunners;
	private RunnerController parent;
	private SimpleIntegerProperty completedPurposes;

	public MarketRunner(Market market, RunnerController parent) {
		// TODO Auto-generated constructor stub
		this.market = market;
		this.parent = parent;
		purposeRunners = new HashSet<PurposeRunner>();
	}

	
	@Override
	public String toString() {
		return market.toString();
	}
	
	public void attach(PurposeRunner runner) {
		//TODO
		purposeRunners.add(runner);
	}

	@Override
	protected Collection<ODProfile> call() throws Exception {
		completedPurposes = new SimpleIntegerProperty(0);
		
		//TODO load surrogate data
		
		
		completedPurposes.addListener((obs, oldValue, newValue) ->{
			updateProgress((int)newValue,purposeRunners.size());
		});
		
		updateProgress(0,purposeRunners.size());
		
		
		// run subtasks for each purpose
		purposeRunners.parallelStream().forEach(Task::run);
		Thread.sleep(1);
		
		
		return purposeRunners.stream().flatMap(runner -> { try {
			return runner.get().stream();
		} catch (ExecutionException e) {
			return Stream.empty();
		} catch (InterruptedException e) {
			return Stream.empty();
		}
		})
		.collect(Collectors.toSet());
		
	}
	
	@Override
	protected void succeeded() {
		parent.increaseCompletedMarkets();
	}


	public void incrementCompletedPurposes() {
		completedPurposes.set(completedPurposes.get()+1);
	}

}
