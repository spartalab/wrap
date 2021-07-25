/**
 * Sample Skeleton for 'wrap.fxml' Controller Class
 */

package edu.utexas.wrap.gui;


import java.io.File;
import java.io.IOException;

import edu.utexas.wrap.Project;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.marketsegmentation.Purpose;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.TravelSurveyZone;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class ConfigController {

	@FXML
	private MenuItem modelNew;

	@FXML
	private MenuItem modelOpen;

	@FXML
	private MenuItem modelClose;

	@FXML
	private MenuItem modelSave;

	@FXML
	private MenuItem modelSaveAs;

	@FXML
	private MenuItem exit;

	@FXML
	private Button modelRun;

	@FXML
	private Spinner<Integer> modelFeedbackSpinner;

	@FXML
	private TableView<TravelSurveyZone> zoneList;

	@FXML
	private Button zoneAdd;

	@FXML
	private Button zoneRemove;

	@FXML
	private Button zoneEdit;

	@FXML
	private ListView<String> skimList;

	@FXML
	private Button skimAdd;

	@FXML
	private Button skimRemove;

	@FXML
	private TextField skimSourceURI;

	@FXML
	private Button skimSourceBrowse;

	@FXML
	private ComboBox<String> skimAssignerChooser;

	@FXML
	private ComboBox<String> skimFunctionChooser;

	@FXML
	private ListView<Purpose> purposeList;

	@FXML
	private Button purposeAdd;

	@FXML
	private Button purposeRemove;

	@FXML
	private Button purposeEdit;

	@FXML
	private ListView<Demographic> demographicList;

	@FXML
	private Button demographicAdd;

	@FXML
	private Button demographicRemove;

	@FXML
	private Button demographicEdit;

	@FXML
	private ListView<FrictionFactorMap> frictionFunctionList;

	@FXML
	private Button frictionFunctionAdd;

	@FXML
	private Button frictionFunctionRemove;

	@FXML
	private Button frictionFunctionEdit;

	@FXML
	private ListView<String> assignerList;

	@FXML
	private Button assignerAdd;

	@FXML
	private Button assignerRemove;

	@FXML
	private ComboBox<String> assignerClass;

	@FXML
	private TextField assignerConfigurationSource;

	@FXML
	private Button assignerConfigurationBrowse;

	@FXML
	private Button assignerConfigurationEdit;

	@FXML
	private Label modelName;

	@FXML
	private Label modelDirectory;

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab zoneTab;

	@FXML
	private Tab skimTab;

	@FXML
	private VBox skimBox;

	@FXML
	private Tab marketTab;

	@FXML
	private Tab assignerTab;

	private Project currentProject;
	private Boolean unsavedChanges;

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		currentProject = null;
		unsavedChanges = false;
		modelFeedbackSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,Integer.MAX_VALUE));

		skimFunctionChooser.getItems().addAll("Travel time","Travel time (exclude HOV)");



		skimList.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				if (newValue != null) {
					
					skimBox.setDisable(false);
					skimRemove.setDisable(false);
					skimSourceURI.setText(currentProject.getSkimFile(newValue));
					skimAssignerChooser.getSelectionModel().select(currentProject.getSkimAssigner(newValue));
					String value = "";
					switch (currentProject.getSkimFunction(newValue)) {
					case "travelTimeSingleOcc":
						value = "Travel time (exclude HOV)";
						break;
					case "travelTime":
						value = "Travel time";
					}
					skimFunctionChooser.getSelectionModel().select(value);
				} else {
					skimSourceURI.clear();
					skimAssignerChooser.getSelectionModel().clearSelection();
					skimFunctionChooser.getSelectionModel().clearSelection();
					skimRemove.setDisable(true);
					skimBox.setDisable(true);
				}
			}

		});
	}

	@FXML
	public void openModel(ActionEvent e) {
		FileChooser modelChooser = new FileChooser();
		modelChooser.setTitle("Open Model");
		modelChooser.getExtensionFilters().add(new ExtensionFilter("wrap Project Files","*.wrp"));

		File selectedFile = modelChooser.showOpenDialog(null);

		if (selectedFile != null) try {

			currentProject = new Project(selectedFile.toPath());

			modelName.setText(currentProject.toString());
			modelDirectory.setText(currentProject.getDirectory().toString());
			modelFeedbackSpinner.getValueFactory().setValue(currentProject.getMaxIterations());


			skimAssignerChooser.getItems().addAll(currentProject.getAssignerIDs());


			tabPane.getSelectionModel().getSelectedItem().getOnSelectionChanged().handle(e);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}

	@FXML
	public void updateZones(Event e) {
		if (zoneTab.isSelected()) {
			System.out.println("Updating zones");
		}
	}

	
	
	
	
	@FXML
	public void updateSkims(Event e) {

		if (skimTab.isSelected()) {

			if (!skimList.getSelectionModel().isEmpty()) {
				skimList.getSelectionModel().clearSelection();
				skimSourceURI.clear();
				skimAssignerChooser.getSelectionModel().clearSelection();
				skimFunctionChooser.getSelectionModel().clearSelection();
				skimRemove.setDisable(true);

			}
			
			if (currentProject != null) {
				skimList.setItems(FXCollections.observableArrayList(currentProject.getSkimIDs()));
				skimBox.setDisable(true);
			}
		}

	}

	public void changeSkimSourceURI(ActionEvent e) {
		markChanged();
	}
	
	public void changeSkimAssigner(Event e) {
		markChanged();
	}
	
	public void changeSkimFunction(Event e) {
		markChanged();
	}
	
	@FXML
	public void browseSkim(Event e) {
		FileChooser skimChooser = new FileChooser();
		skimChooser.setTitle("Open Skim");
		skimChooser.setInitialDirectory(currentProject.getDirectory().toFile());
		skimChooser.getExtensionFilters().add(new ExtensionFilter("Comma-Separated Values","*.csv"));
		
		File selectedFile = skimChooser.showOpenDialog(null);
		if (selectedFile != null) {
			skimSourceURI.setText(currentProject.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			skimSourceURI.getOnAction().handle(new ActionEvent());
		}
	}
	
	public void addSkim(Event e) {
		System.out.println("Add skim");
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		try {
			FXMLLoader loader = new FXMLLoader();
			VBox vbox = loader.load(getClass().getResource("/edu/utexas/wrap/gui/newSkimDialog.fxml").openStream());
			NewSkimController controller = loader.getController();
			controller.setProject(currentProject);
			DialogPane pane = new DialogPane();
			
			pane.setContent(vbox);
			pane.getButtonTypes().add(ButtonType.CANCEL);
			pane.getButtonTypes().add(ButtonType.OK);

			
			dialog.setDialogPane(pane);
			dialog.setTitle("New skim");
			Button ok = (Button) pane.lookupButton(ButtonType.OK);
			ok.disableProperty().bind(controller.notReady());	
			dialog.showAndWait();
		
			if (dialog.getResult() == ButtonType.OK) {
				currentProject.addSkim(controller.getSkimID(), 
						controller.getSkimAssigner(), 
						controller.getSkimFunction(), 
						controller.getSkimSourceURI());
				skimList.getItems().add(controller.getSkimID());
				markChanged();
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}
	
	public void removeSkim(Event e) {

		String skimName = skimList.getSelectionModel().getSelectedItem();
		if (skimName != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION,"Delete skim "+skimName+"?",ButtonType.YES,ButtonType.NO);
			alert.showAndWait();
			
			if (alert.getResult() == ButtonType.YES) {
				skimList.getItems().remove(skimName);
				currentProject.removeSkim(skimName);
				markChanged();
			}
		}
	}
	
	
	
	
	@FXML
	public void updateMarkets(Event e) {
		if (marketTab.isSelected()) System.out.println("Updating markets");

	}

	public void updateAssigners(Event e) {
		if (assignerTab.isSelected()) System.out.println("Updating assigners");

	}

	private void markChanged() {
		unsavedChanges = true;
	}
	
	@FXML
	private void saveModel() {
		if (unsavedChanges) {
			//TODO write project to file
			unsavedChanges = false;
		}
	}
}
