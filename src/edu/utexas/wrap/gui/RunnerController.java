package edu.utexas.wrap.gui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.Project;
import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.assignment.StaticAssigner;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.marketsegmentation.MarketRunner;
import edu.utexas.wrap.marketsegmentation.Purpose;
import edu.utexas.wrap.util.io.SkimFactory;
import edu.utexas.wrap.util.io.SkimLoader;
import edu.utexas.wrap.marketsegmentation.PurposeRunner;
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
	private TableView<AssignerRunner> assignerTable;

	@FXML
	private TableColumn<Task<Graph>, String> assignerIDColumn;

	@FXML
	private TableColumn<Task<Graph>, String> assignerStageColumn;

	@FXML
	private TableColumn<Task<Graph>, Double> assignerProgressColumn;

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
	
	
	
	public void initialize() {
		marketRunners = new HashSet<MarketRunner>();
		modelProgress.progressProperty().bind(this.progressProperty());
		iterationNumber.textProperty().bind(Bindings.convert(valueProperty()));
		completedMarketCount = new SimpleIntegerProperty(0);
	}
	
	public void setProject(Project project) {
		this.project = project;
		iterationLimit.setText(project.getMaxIterations().toString());
		marketProgress.progressProperty().bind(completedMarketCount.divide((double) project.getMarkets().size()));

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
		updateProgress(0,project.getMaxIterations());
		updateValue(1);
		Collection<ODProfile> profiles = null;
//		Collection<Graph> networks = null;
		
		// load initial skims from source
		skimTable.getItems().parallelStream().forEach(Task::run);
		
		
		for (int i = 1; i < project.getMaxIterations()+1;i++) {
			
			if (isCancelled()) {
				break;
			}
			marketRoot.getChildren().clear();
			marketRunners.clear();
			completedMarketCount.set(0);
			
			project.getMarkets().forEach(market -> {
				MarketRunner marketRunner = new MarketRunner(market,this);
				marketRunners.add(marketRunner);
				TreeItem<Task<Collection<ODProfile>>> marketItem = new TreeItem<Task<Collection<ODProfile>>>(marketRunner);
				
				
				
				
				marketRoot.getChildren().add(marketItem);
				market.getPurposes().forEach(purpose -> {
					PurposeRunner purposeRunner = new PurposeRunner(purpose, marketRunner);
					marketRunner.attach(purposeRunner);
					TreeItem<Task<Collection<ODProfile>>> purposeItem = new TreeItem<Task<Collection<ODProfile>>>(purposeRunner);
					marketItem.getChildren().add(purposeItem);
				});
			});
			
			// run the subthreads to get market OD profiles
			marketRunners.parallelStream().forEach(Task::run);
			
			profiles = marketRunners.stream().flatMap(t -> {
				try {
					return t.get().stream();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return Stream.empty();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return Stream.empty();
				}
			}).collect(Collectors.toSet());
			
//			System.out.println("Number of OD Profiles: "+profiles.size());

			
			
			if (isCancelled()) {
				break;
			}
			// run subthreads for assigners
			Collection<ODProfile> profilesToLoad = profiles;
			
			project.getAssigners().parallelStream().forEach(assigner ->{
				AssignerRunner assignerRunner = new AssignerRunner(assigner,profilesToLoad);
				assignerTable.getItems().add(assignerRunner);
				
			});
			
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
				break;
			}
			
			//TODO run subthreads for updating skims
			skimTable.getItems().clear();
			project.getSkimIDs().forEach(skimID -> skimTable.getItems().add(new SkimUpdater(skimID,project)));
			skimTable.getItems().parallelStream().forEach(Task::run);
			
			skimTable.getItems().stream().map(t -> {
				try {
					return t.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}).forEach(skim -> project.updateSkim(skim.toString(), skim));
			
			if (isCancelled()) {
				break;
			}

			Thread.sleep(1);
			updateValue(i+1);
			updateProgress(i,project.getMaxIterations());
		}

		BufferedWriter out = Files.newBufferedWriter(project.metricOutputPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		out.write("Market,Purpose,Time Period,Assigner,Total Travel Time,Total Toll Cost\n");
		
		if (profiles != null) {
			profiles.stream().map(profile -> {
				String result = "";
				Purpose tripPurpose = profile.getTripPurpose();
				

				Market market = null;

				if (tripPurpose != null) {
					market = tripPurpose.getMarket();
					if (market != null) result += market.toString()+","+tripPurpose.toString()+",";
					else result += "null,"+tripPurpose.toString()+",";

				}
				else result += "null,null,";

				//TODO add columns here
				for (TimePeriod tp : TimePeriod.values()) {
					result += tp.toString()+",";
					ODMatrix mtx = profile.getMatrix(tp);
					Collection<ToDoubleFunction<Link>> evaluationMetrics = List.of(
							Link::getTravelTime,
							link -> link.getPrice(tripPurpose.getVOT(tp), mtx.getMode())
							);;
					for (Assigner assigner : project.getAssigners()) {
						if (assigner instanceof StaticAssigner && ((StaticAssigner) assigner).getTimePeriod() != tp) continue;
						
						result += assigner.toString()+",";
						Graph network = assigner.getNetwork();

						for (ToDoubleFunction<Link> costFunction : evaluationMetrics) {

							NetworkSkim costSkim = SkimFactory.calculateSkim(network, costFunction, null);

							Double cost = mtx.getZones().stream().mapToDouble(origin -> 
							mtx.getZones().stream().mapToDouble(destination -> 
							costSkim.getCost(origin,destination)*mtx.getDemand(origin, destination)
									).sum()
									).sum();
							result += cost.toString()+",";
						}
					};
				}
				result += "\n";
				return result;

			})
			//TODO output results
			.forEach(line -> {
				try {
					out.write(line);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			;
		}
		
		return Integer.parseInt(iterationNumber.getText());
	}

	public void increaseCompletedMarkets() {
		// TODO Auto-generated method stub
		completedMarketCount.set(completedMarketCount.get()+1);
		
		
	}

}
