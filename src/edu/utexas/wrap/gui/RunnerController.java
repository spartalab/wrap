package edu.utexas.wrap.gui;

import java.util.Collection;
import java.util.HashSet;

import edu.utexas.wrap.Project;
import edu.utexas.wrap.marketsegmentation.MarketRunner;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
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
	private TreeTableView<Task<Double>> marketTable;

	@FXML
	private TreeTableColumn<Task<Double>, String> IDColumn;

	@FXML
	private TreeTableColumn<Task<Double>, Double> marketProgressColumn;

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
	private TableView<?> skimTable;

	@FXML
	private TableColumn<?, ?> skimIDColumn;

	@FXML
	private TableColumn<?, ?> skimProgressColumn;
	
	private Project project;
	
	private Collection<MarketRunner> marketRunners;
	
	private TreeItem<Task<Double>> marketRoot;
	
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
		IDColumn.setCellValueFactory(new Callback<CellDataFeatures<Task<Double>,String>,ObservableValue<String>>(){

			@Override
			public ObservableValue<String> call(CellDataFeatures<Task<Double>, String> arg0) {
				// TODO Auto-generated method stub
				return new ReadOnlyObjectWrapper<String>(arg0.getValue().getValue().toString());
			}
			
		});
		
		marketProgressColumn.setCellValueFactory(new TreeItemPropertyValueFactory<Task<Double>, Double>("progress"));
		marketProgressColumn.setCellFactory(ProgressBarTreeTableCell.<Task<Double>>forTreeTableColumn());
	
		
		
		marketRoot = new TreeItem<Task<Double>>();
		marketRoot.setExpanded(true);
		
		
		marketTable.setRoot(marketRoot);

	}


	@Override
	protected Integer call() throws Exception {
		// TODO Auto-generated method stub
		updateProgress(0,project.getMaxIterations());
		updateValue(1);
		
		for (int i = 1; i < project.getMaxIterations()+1;i++) {
			
			if (isCancelled()) {
				break;
			}
			marketRoot.getChildren().clear();
			completedMarketCount.set(0);
			project.getMarkets().forEach(market -> {
				MarketRunner marketRunner = new MarketRunner(market,this);
				marketRunners.add(marketRunner);
				TreeItem<Task<Double>> marketItem = new TreeItem<Task<Double>>(marketRunner);
				
				
				
				
				marketRoot.getChildren().add(marketItem);
//				market.getBasicPurposes().forEach(purpose -> {
//					PurposeRunner purposeRunner = new PurposeRunner(purpose);
//					marketRunner.attach(purposeRunner);
//					TreeItem<Task<Double>> purposeItem = new TreeItem<Task<Double>>(purposeRunner);
//					marketItem.getChildren().add(purposeItem);
//				});
			});
			
			//TODO run the subthreads
			marketRunners.parallelStream().forEach(Task::run);
			
			

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
