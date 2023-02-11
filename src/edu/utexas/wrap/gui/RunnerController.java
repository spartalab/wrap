package edu.utexas.wrap.gui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.Project;
import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.assignment.StaticAssigner;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushForgetter;
import edu.utexas.wrap.assignment.bush.BushReader;
import edu.utexas.wrap.assignment.BasicStaticAssigner;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.marketsegmentation.MarketRunner;
import edu.utexas.wrap.marketsegmentation.Purpose;
import edu.utexas.wrap.util.io.SkimLoader;
import edu.utexas.wrap.marketsegmentation.PurposeRunner;
import edu.utexas.wrap.marketsegmentation.SurrogatePurpose;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.ProgressBarTreeTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.Callback;

public class RunnerController extends Task<Integer> {

	@FXML
	private Label iterationNumber;

	@FXML
	private Label iterationLimit;

	@FXML
	private ProgressBar modelProgress;

	@FXML
	private ProgressBar marketProgress;

	@FXML
	private TreeTableView<Task<Collection<ODProfile>>> marketTable;

	@FXML
	private TreeTableColumn<Task<Collection<ODProfile>>, String> IDColumn;

	@FXML
	private TreeTableColumn<Task<Collection<ODProfile>>, Double> marketProgressColumn;

	@FXML
	private ProgressBar assignerProgress;

	@FXML
	private TableView<AssignerRunner<?>> assignerTable;

	@FXML
	private TableColumn<Task<Graph>, String> assignerIDColumn;

	@FXML
	private TableColumn<Task<Graph>, String> assignerStageColumn;

	@FXML
	private TableColumn<Task<Graph>, Double> assignerProgressColumn;
	
	@FXML
	private TableColumn<Task<Graph>,Double> subtaskProgressColumn;

	@FXML
	private ProgressBar skimProgress;

	@FXML
	private TableView<Task<NetworkSkim>> skimTable;

	@FXML
	private TableColumn<Task<NetworkSkim>, String> skimIDColumn;

	@FXML
	private TableColumn<Task<NetworkSkim>, Double> skimProgressColumn;
	
	private Project project;
	
	private Collection<MarketRunner> marketRunners;
	
	
	
	private TreeItem<Task<Collection<ODProfile>>> marketRoot;
	
	private SimpleIntegerProperty completedMarketCount;
	
	private SimpleIntegerProperty completedAssignerCount;
	
	private Logger logger = Logger.getLogger("wrap.runner");
	
	
	
	public void initialize() {
		logger.info("Initializing RunnerController");
		marketRunners = new HashSet<MarketRunner>();
		modelProgress.progressProperty().bind(this.progressProperty());
		iterationNumber.textProperty().bind(Bindings.convert(valueProperty()));
		
		completedMarketCount = new SimpleIntegerProperty(0);
		completedAssignerCount = new SimpleIntegerProperty(0);
	}
	
	public void setProject(Project project) {
		logger.info("Setting project: "+project.getPath());
		this.project = project;
		
		iterationLimit.setText(project.getMaxIterations().toString());
		marketProgress.progressProperty().bind(completedMarketCount.divide((double) project.getMarkets().size()));
		assignerProgress.progressProperty().bind(completedAssignerCount.divide((double) project.getAssignerIDs().size()));

		
		marketTable.setShowRoot(false);
		IDColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Task<Collection<ODProfile>>,String>,ObservableValue<String>>(){

			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Task<Collection<ODProfile>>, String> arg0) {
				// TODO Auto-generated method stub
				return new ReadOnlyObjectWrapper<String>(arg0.getValue().getValue().toString());
			}
			
		});
		
		marketProgressColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Task<Collection<ODProfile>>, Double>("progress"));
		marketProgressColumn.setCellFactory(ProgressBarTreeTableCell.<Task<Collection<ODProfile>>>forTreeTableColumn());
		
		marketRoot = new TreeItem<Task<Collection<ODProfile>>>();
		marketRoot.setExpanded(true);
		
		marketTable.setRoot(marketRoot);
		
		
		
		project.getSkims().forEach(skim -> skimTable.getItems().add(new SkimLoader(skim,project.getDirectory().resolve(project.getSkimFile(skim.toString())),project.getZones())));
		skimIDColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Task<NetworkSkim>,String>,ObservableValue<String>>(){

			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<Task<NetworkSkim>, String> arg0) {
				// TODO Auto-generated method stub
				return new ReadOnlyObjectWrapper<String>(arg0.getValue().toString());
			}
			
		});
		
		skimProgressColumn.setCellValueFactory(new PropertyValueFactory<Task<NetworkSkim>,Double>("progress"));
		skimProgressColumn.setCellFactory(ProgressBarTableCell.<Task<NetworkSkim>>forTableColumn());
		
		
		assignerIDColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Task<Graph>, String>, ObservableValue<String>>(){

			@Override
			public ObservableValue<String> call(CellDataFeatures<Task<Graph>, String> arg0) {
				// TODO Auto-generated method stub
				return new ReadOnlyObjectWrapper<String>(arg0.getValue().toString());
			}
			
		});
		assignerStageColumn.setCellValueFactory(new PropertyValueFactory<Task<Graph>,String>("message"));
		assignerProgressColumn.setCellValueFactory(new PropertyValueFactory<Task<Graph>,Double>("progress"));
		assignerProgressColumn.setCellFactory(ProgressBarTableCell.<Task<Graph>>forTableColumn());
		
		subtaskProgressColumn.setCellValueFactory(new PropertyValueFactory<Task<Graph>,Double>("subtaskProgress"));
		subtaskProgressColumn.setCellFactory(ProgressBarTableCell.<Task<Graph>>forTableColumn());
		
		setOnCancelled(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent arg0) {
				// TODO Auto-generated method stub
				marketRunners.forEach(Task::cancel);
				skimTable.getItems().forEach(Task::cancel);
				assignerTable.getItems().forEach(Task::cancel);
			}
			
		});
	}


	@Override
	protected Integer call() throws Exception {
		//TODO load surrogate data
		logger.info("Starting run");
		updateProgress(0,project.getMaxIterations());
		logger.info("Number of iterations: "+project.getMaxIterations());
		updateValue(1);
		Collection<ODProfile> profiles = null;
//		Collection<Graph> networks = null;
		
		//TODO load surrogates
		
		// load initial skims from source
		logger.info("Reading initial skims");
		skimTable.getItems().parallelStream().forEach(Task::run);
		
		
		for (int i = 1; i < project.getMaxIterations()+1;i++) {
			logger.info("Beginning iteration "+i);
			if (isCancelled()) {
				logger.warning("Run cancelled");
				break;
			}
			marketRoot.getChildren().clear();
			marketRunners.clear();
			completedMarketCount.set(0);
			logger.info("Creating MarketRunners");
			project.getMarkets().forEach(market -> {
				MarketRunner marketRunner = new MarketRunner(market,this);
				marketRunners.add(marketRunner);
				TreeItem<Task<Collection<ODProfile>>> marketItem = new TreeItem<Task<Collection<ODProfile>>>(marketRunner);
				
				
				
				logger.info("Creating PurposeRunners for "+market.toString());
				marketRoot.getChildren().add(marketItem);
				market.getPurposes().forEach(purpose -> {
					PurposeRunner purposeRunner = new PurposeRunner(purpose, marketRunner);
					marketRunner.attach(purposeRunner);
					TreeItem<Task<Collection<ODProfile>>> purposeItem = new TreeItem<Task<Collection<ODProfile>>>(purposeRunner);
					marketItem.getChildren().add(purposeItem);
				});
			});
			
			// run the subthreads to get market OD profiles
			logger.info("Starting MarketRunners");
			marketRunners.parallelStream().forEach(Task::run);
			
			profiles = marketRunners.stream().flatMap(t -> {
				try {
					return t.get().stream();
				} catch (InterruptedException e) {
					logger.log(Level.WARNING,"MarketRunner "+t+" interrupted.",e);
					return Stream.empty();
				} catch (ExecutionException e) {
					logger.log(Level.SEVERE,"MarketRunner "+t+" encountered an error in execution.",e);
					return Stream.empty();
				}
			}).collect(Collectors.toSet());
			
			logger.info("MarketRunners complete. Number of OD Profiles: "+profiles.size());

			
			
			if (isCancelled()) {
				logger.warning("Run cancelled");
				break;
			}
			// run subthreads for assigners
			logger.info("Creating AssignerRunners");
			Collection<ODProfile> profilesToLoad = profiles;
			
			Collection<SurrogatePurpose> surrogates = project.getSurrogates();
			
			for (SurrogatePurpose surrogate : surrogates) {
				Collection<ODProfile> others = surrogate.getODProfiles(null);
				profilesToLoad.addAll(others);
			}
			
			assignerTable.getItems().clear();
			project.getAssigners().stream().forEach( assigner ->{
				AssignerRunner<?> assignerRunner = new AssignerRunner<>(assigner,profilesToLoad,this);
				assignerTable.getItems().add(assignerRunner);
				
			});
			
			logger.info("Starting AssignerRunners");
			assignerTable.getItems().stream().sequential().forEach(Task::run);
			
//			networks = assignerTable.getItems().stream().map(t -> {
//				try {
//					return t.get();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					return null;
//				} catch (ExecutionException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					return null;
//				}
//			}).collect(Collectors.toSet());
			
			if (isCancelled()) {
				logger.warning("Run cancelled");
				break;
			}
			
			//TODO run subthreads for updating skims
			skimTable.getItems().clear();
			logger.info("Creating SkimUpdaters");
			project.getSkimIDs().forEach(skimID -> skimTable.getItems().add(new SkimUpdater(skimID,project)));
			logger.info("Running SkimUpdaters");
			skimTable.getItems().parallelStream().forEach(Task::run);
			
			skimTable.getItems().stream().map(t -> {
				try {
					return t.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					logger.log(Level.WARNING,"SkimUpdater "+t+" interrupted.",e);
					return null;
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					logger.log(Level.SEVERE,"SkimUpdater "+t+" encountered an error in execution.",e);
					return null;
				}
			}).forEach(skim -> {
				logger.info("Updating skim "+skim.toString()+" in project");
				project.updateSkim(skim.toString(), skim);
			});
			
			if (isCancelled()) {
				logger.warning("Run cancelled");
				break;
			}

			logger.info("Iteration "+i+" completed");
			Thread.sleep(1);
			updateValue(i+1);
			updateProgress(i,project.getMaxIterations());
		}

		logger.info("Writing output metrics");
		try {
			AtomicInteger completed = new AtomicInteger(0);
			Path outputPath = project.metricOutputPath();
			logger.info("Metric output path: "+outputPath);
			BufferedWriter out = Files.newBufferedWriter(outputPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
			logger.info("Created metric file");
			out.write("Market,Purpose,Time Period,Assigner,Mode,Total Travel Time,Total Generalized Cost,Total Distance Traveled\n");
			logger.info("Handling profiles");
			if (profiles != null) {
				int totalMetrics = profiles.size()*project.getAssigners().size();
				profiles.stream().map(profile -> {
					String result = "";

					try {
						Purpose tripPurpose = profile.getTripPurpose();

						Market market = null;
						String purposeLabel = "";
						if (tripPurpose != null) {
							market = tripPurpose.getMarket();
							if (market != null) purposeLabel = market.toString()+","+tripPurpose.toString()+",";
							else purposeLabel = "null,"+tripPurpose.toString()+",";

						}
						else purposeLabel = "null,null,";
						logger.info("Reading values for "+purposeLabel);

						for (TimePeriod tp : TimePeriod.values()) {
							ODMatrix mtx = profile.getMatrix(tp);
							Collection<ToDoubleFunction<Link>> evaluationMetrics = List.of(
									Link::getTravelTime,
									link -> link.getPrice(tripPurpose.getVOT(mtx.getMode(),tp), mtx.getMode()),
									Link::getLength
									);

							for (Assigner<?> assigner : project.getAssigners()) {
								if (assigner instanceof StaticAssigner && ((StaticAssigner<?>) assigner).getTimePeriod() != tp) continue;

								result += purposeLabel+ tp.toString()+","+assigner.toString()+",";
								result += profile.getMode()+",";
								//										Graph network = assigner.getNetwork();

								for (ToDoubleFunction<Link> costFunction : evaluationMetrics) {
									if (assigner instanceof BasicStaticAssigner<?>) {
										Double cost = getMetricFromBush(mtx,tripPurpose.getVOT(mtx.getMode(),tp),(BasicStaticAssigner<?>) assigner,costFunction);
										result += cost.toString()+",";
									}
									//											Double cost = getMetricFromSkim(mtx, network, costFunction);

								}
								result += "\n";
								logger.info("Completed metrics: "+completed.incrementAndGet()+" / "+totalMetrics);
							};
						}

						return result;
					}
					catch (Exception e) {
						logger.log(Level.SEVERE,"An error occurred while calculating output metrics.",e);
						return null;
					}
				})
				// output results
				.forEach(line -> {
					try {
						out.write(line);
						out.flush();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						logger.log(Level.SEVERE,"An error was encountered while writing output metrics.",e);
					}
				});


			}
			logger.info("Completed writing metrics");
			out.close();
			logger.info("Run completed successfully");
			return Integer.parseInt(iterationNumber.getText());
		} catch (Exception e) {
			logger.log(Level.SEVERE,"An error was encountered while writing output metrics.",e);
			return -1;
		}
	}

	public Double getMetricFromBush(ODMatrix mtx, Float vot, BasicStaticAssigner<?> assigner, ToDoubleFunction<Link> costFunction) {
		BushReader reader = new BushReader(assigner.getContainerSource());
		BushForgetter forgetter = new BushForgetter();
		Stream<Map<Link,Double>> str = assigner.getContainers().parallelStream()
		.filter(container -> container.vehicleClass().equals(assigner.getMode(mtx)))
		.filter(container -> container.valueOfTime().equals(vot))
		.map(container -> {
			try {
				
				reader.getStructure((Bush) container, assigner.getNetwork());
				Map<Link, Double> ret = container.flows(false,mtx.getDemandMap(container.root()));
				forgetter.consumeStructure((Bush) container, assigner.getNetwork());
				return ret;
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		});
		
		
		return str
		.flatMap(map -> map.entrySet().stream())
		.mapToDouble(entry -> {
			double val = entry.getValue() * costFunction.applyAsDouble(entry.getKey());
			
			return val;
		})
		.sum()
		;
	}

	public void increaseCompletedMarkets() {
		completedMarketCount.set(completedMarketCount.get()+1);
	}

	public void increaseCompletedAssigners() {
		completedAssignerCount.set(completedAssignerCount.get()+1);
	}

}
