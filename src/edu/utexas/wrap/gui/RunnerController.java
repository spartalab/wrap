package edu.utexas.wrap.gui;

import java.util.Collection;
import java.util.HashSet;

import edu.utexas.wrap.Project;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.marketsegmentation.MarketRunner;
import edu.utexas.wrap.util.io.SkimLoader;
import edu.utexas.wrap.marketsegmentation.PurposeRunner;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
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
	private TableView<?> assignerTable;

	@FXML
	private TableColumn<?, ?> assignerIDColumn;

	@FXML
	private TableColumn<?, ?> assignerStageColumn;

	@FXML
	private TableColumn<?, ?> assignerProgressColumn;

	@FXML
	private ProgressBar skimProgress;

	@FXML
	private TableView<Task<Void>> skimTable;

	@FXML
	private TableColumn<Task<Void>, String> skimIDColumn;

	@FXML
	private TableColumn<Task<Void>, Double> skimProgressColumn;
	
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
		skimIDColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Task<Void>,String>,ObservableValue<String>>(){

			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<Task<Void>, String> arg0) {
				// TODO Auto-generated method stub
				return new ReadOnlyObjectWrapper<String>(arg0.getValue().toString());
			}
			
		});
		
		skimProgressColumn.setCellValueFactory(new PropertyValueFactory<Task<Void>,Double>("progress"));
		skimProgressColumn.setCellFactory(ProgressBarTableCell.<Task<Void>>forTableColumn());
	}


	@Override
	protected Integer call() throws Exception {
		//TODO load surrogate data
		updateProgress(0,project.getMaxIterations());
		updateValue(1);
		
		// load initial skims from source
		skimTable.getItems().parallelStream().forEach(Task::run);
		
		
		for (int i = 1; i < project.getMaxIterations()+1;i++) {
			
			if (isCancelled()) {
				break;
			}
			marketRoot.getChildren().clear();
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
			
			marketRunners.stream().forEach(t -> {
				try {
					t.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

			if (isCancelled()) {
				break;
			}
			//TODO run subthreads for assigners
			
			
			if (isCancelled()) {
				break;
			}
			//TODO run subthreads for updating skims

			Thread.sleep(1);
			updateValue(i+1);
			updateProgress(i,project.getMaxIterations());
		}
		return Integer.parseInt(iterationNumber.getText());
	}

	public void increaseCompletedMarkets() {
		// TODO Auto-generated method stub
		completedMarketCount.set(completedMarketCount.get()+1);
		
		
	}

}
