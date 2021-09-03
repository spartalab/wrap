package edu.utexas.wrap.gui;

import java.util.Collection;
import java.util.HashSet;

import edu.utexas.wrap.Project;
import edu.utexas.wrap.marketsegmentation.MarketRunner;
import edu.utexas.wrap.marketsegmentation.PurposeRunner;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
	
	
	public void initialize() {
		marketRunners = new HashSet<MarketRunner>();
		modelProgress.progressProperty().bind(this.progressProperty());
		iterationNumber.textProperty().bind(Bindings.convert(valueProperty()));
	}
	
	public void setProject(Project project) {
		this.project = project;
		iterationLimit.setText(project.getMaxIterations().toString());

		marketTable.setShowRoot(false);
		IDColumn.setCellValueFactory(new Callback<CellDataFeatures<Task<Double>,String>,ObservableValue<String>>(){

			@Override
			public ObservableValue<String> call(CellDataFeatures<Task<Double>, String> arg0) {
				// TODO Auto-generated method stub
				return new ReadOnlyObjectWrapper<String>(arg0.getValue().getValue().toString());
			}
			
		});
		
		marketProgressColumn.setCellValueFactory(new Callback<CellDataFeatures<Task<Double>,Double>,ObservableValue<Double>>() {

			@Override
			public ObservableValue<Double> call(CellDataFeatures<Task<Double>, Double> arg0) {
				// TODO Auto-generated method stub
				return new ReadOnlyObjectWrapper<Double>(arg0.getValue().getValue().getProgress());
			}
			
		});
		marketProgressColumn.setCellFactory(ProgressBarTreeTableCell.<Task<Double>>forTreeTableColumn());
		
		TreeItem<Task<Double>> marketRoot = new TreeItem<Task<Double>>();
		marketRoot.setExpanded(true);
		project.getMarkets().forEach(market -> {
			MarketRunner marketRunner = new MarketRunner(market);
			marketRunners.add(marketRunner);
			TreeItem<Task<Double>> marketItem = new TreeItem<Task<Double>>(marketRunner);
			marketRoot.getChildren().add(marketItem);
			market.getBasicPurposes().forEach(purpose -> {
				PurposeRunner purposeRunner = new PurposeRunner(purpose);
				marketRunner.attach(purposeRunner);
				TreeItem<Task<Double>> purposeItem = new TreeItem<Task<Double>>(purposeRunner);
				marketItem.getChildren().add(purposeItem);
			});
		});
		
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
			
			//TODO run the subthreads
			Thread.sleep(5000);
			
			updateValue(i+1);
			updateProgress(i,project.getMaxIterations());
		}
		return Integer.parseInt(iterationNumber.getText());
	}

}
