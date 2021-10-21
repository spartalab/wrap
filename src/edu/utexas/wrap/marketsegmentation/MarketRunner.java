package edu.utexas.wrap.marketsegmentation;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.gui.RunnerController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

public class MarketRunner extends Task<Collection<ODProfile>> {
	
	private final Market market;
	private Collection<PurposeRunner> purposeRunners;
	private RunnerController parent;
	private SimpleIntegerProperty completedPurposes;
	private Logger logger = Logger.getLogger("wrap.runner.market");

	public MarketRunner(Market market, RunnerController parent) {
		this.market = market;
		this.parent = parent;
		purposeRunners = new HashSet<PurposeRunner>();
	}
	
	@FXML
	private void initialize() {
		logger.info("Initializing MarketRunner for "+market);
		setOnCancelled(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent arg0) {
				logger.info("MarketRunner for "+market+" cancelled");
				purposeRunners.forEach(Task::cancel);
			}
			
		});
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
		logger.info("Starting MarketRunner for "+market);
		completedPurposes = new SimpleIntegerProperty(0);
		
		//TODO load surrogate data
		
		
		completedPurposes.addListener((obs, oldValue, newValue) ->{
			updateProgress((int)newValue,purposeRunners.size());
		});
		
		updateProgress(0,purposeRunners.size());
		
		
		// run subtasks for each purpose
		logger.info("Starting PurposeRunners for "+market);
		purposeRunners.parallelStream().forEach(Task::run);
		Thread.sleep(1);
		
		
		return purposeRunners.stream().flatMap(runner -> { try {
			return runner.get().stream();
		} catch (ExecutionException e) {
			logger.log(Level.SEVERE,"An error was encountered while executing PurposeRunner "+runner,e);
			return Stream.empty();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING,"PurposeRunner "+runner+" was interrupted",e);
			return Stream.empty();
		}
		})
		.collect(Collectors.toSet());
		
	}
	
	@Override
	protected void succeeded() {
		logger.info("MarketRunner for "+market+" completed successfully");
		parent.increaseCompletedMarkets();
	}


	public void incrementCompletedPurposes() {
		completedPurposes.set(completedPurposes.get()+1);
	}

}
