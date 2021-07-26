/**
 * Sample Skeleton for 'wrap.fxml' Controller Class
 */

package edu.utexas.wrap.gui;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import edu.utexas.wrap.Project;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.marketsegmentation.Purpose;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.TravelSurveyZone;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;

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
	private MenuItem about;
	
	
	
	
	
	@FXML
	private ToolBar toolBar;

	@FXML
	private Button modelRun;

	@FXML
	private Spinner<Integer> modelFeedbackSpinner;

	@FXML
	private TabPane tabPane;

	@FXML
	private Label modelName;

	@FXML
	private Label modelDirectory;
	
	
	
	
	
	@FXML
	private Tab zoneTab;

	@FXML
	private HBox zoneBox;

	@FXML
	private TableView<TravelSurveyZone> zoneList;

	@FXML
	private Button zoneAdd;

	@FXML
	private Button zoneRemove;

	@FXML
	private Button zoneEdit;
	
	@FXML
	private Button zoneBrowse;
	
	@FXML
	private TextField zoneSourceURI;

	@FXML
	private TableColumn<TravelSurveyZone,Integer> zoneIDList;
	
	@FXML
	private TableColumn<TravelSurveyZone,String> zoneClassList;
	
	
	
	
	
	@FXML
	private Tab skimTab;
	
	@FXML
	private VBox skimBox;

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
	private Tab marketTab;
	
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
	private Tab assignerTab;
	
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
	
	
	
	
	
	

	private Project currentProject;
	
	private Boolean unsavedChanges;
	
	String getDisplayString(String keyword) {
		if (keyword == null) return null;
		switch (keyword) {
		case "travelTimeSingleOcc":
			return "Travel time (exclude HOV)";
			
		case "travelTime":
			return "Travel time";
		default:
			return null;
		}
	}
	
	String getFunctionID(String displayString) {
		if (displayString == null) return null;
		switch (displayString) {
		case "Travel time (exclude HOV)":
			return "travelTimeSingleOcc";
		case "Travel time":
			return "travelTime";
		default:
			return null;
		}
	}
	

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		currentProject = null;
		unsavedChanges = false;
		modelFeedbackSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,Integer.MAX_VALUE));
		modelFeedbackSpinner.getEditor().setDisable(true);
		modelFeedbackSpinner.valueProperty().addListener(new ChangeListener<Integer>() {

			@Override
			public void changed(ObservableValue<? extends Integer> arg0, Integer oldValue, Integer newValue) {
				// TODO Auto-generated method stub
				if (currentProject != null && newValue != currentProject.getMaxIterations()) {
					markChanged();
				}
			}
			
		});

		zoneIDList.setCellValueFactory(new Callback<CellDataFeatures<TravelSurveyZone, Integer>, ObservableValue<Integer>>(){

			@Override
			public ObservableValue<Integer> call(CellDataFeatures<TravelSurveyZone, Integer> arg0) {
				// TODO Auto-generated method stub
				return new ReadOnlyObjectWrapper<Integer>(arg0.getValue().getID());
			}
			
		});
		
		zoneClassList.setCellValueFactory(new Callback<CellDataFeatures<TravelSurveyZone,String>,ObservableValue<String>>(){

			@Override
			public ObservableValue<String> call(CellDataFeatures<TravelSurveyZone, String> arg0) {
				// TODO Auto-generated method stub
				return new ReadOnlyObjectWrapper<String>(arg0.getValue().getAreaClass().name());
			}
			
		});
		
		skimFunctionChooser.getItems().addAll("Travel time","Travel time (exclude HOV)");



		skimList.getSelectionModel().selectedItemProperty().addListener( 

				new ChangeListener<String>() {

					@Override
					public void changed(ObservableValue<? extends String> arg0, String oldValue, String newValue) {

						if (newValue != null) {

							skimBox.setDisable(false);
							skimRemove.setDisable(false);
							Path newURI = currentProject.getDirectory().resolve(currentProject.getSkimFile(newValue));
							skimSourceURI.setText(currentProject.getDirectory().toUri().relativize(newURI.toUri()).getPath());
							skimAssignerChooser.getSelectionModel().select(currentProject.getSkimAssigner(newValue));
							skimFunctionChooser.getSelectionModel().select(getDisplayString(currentProject.getSkimFunction(newValue)));
						}
						else {
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
	private boolean newModel(Event event) {
		
		if (currentProject != null && !closeModel(event)) return false;
		
		FileChooser modelChooser = new FileChooser();
		modelChooser.setTitle("New Model");
		modelChooser.getExtensionFilters().add(new ExtensionFilter("wrap Project Files","*.wrp"));
		
		File projFile = modelChooser.showSaveDialog(null);
		if (projFile == null) {
			return false;
		}

		
		try {
			currentProject = new Project(projFile.toPath());
			
			loadModel(event);
			markChanged();
			return true;
			
		} catch (Exception exception) {
			Alert alert = new Alert(AlertType.ERROR,exception.getLocalizedMessage());
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.show();
			return false;
		}
		
	}

	@FXML
	private void openModel(ActionEvent e) {
		FileChooser modelChooser = new FileChooser();
		modelChooser.setTitle("Open Model");
		modelChooser.getExtensionFilters().add(new ExtensionFilter("wrap Project Files","*.wrp"));

		File selectedFile = modelChooser.showOpenDialog(null);

		if (selectedFile != null) try {

			currentProject = new Project(selectedFile.toPath());
			currentProject.loadPropsFromFile();

			loadModel(e);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}

	private void loadModel(Event e) {
		modelName.setText(currentProject.toString());
		modelDirectory.setText(currentProject.getDirectory().toString());
		modelFeedbackSpinner.getValueFactory().setValue(currentProject.getMaxIterations());

		
		tabPane.getSelectionModel().getSelectedItem().getOnSelectionChanged().handle(e);
		tabPane.setDisable(false);
		toolBar.setDisable(false);
		modelClose.setDisable(false);
		modelSaveAs.setDisable(false);
	}

	private void markChanged() {
		unsavedChanges = true;
		modelSave.setDisable(false);
		if (!modelName.getText().endsWith("*")) {
			modelName.setText(modelName.getText()+"*");
		}
	}
	
	@FXML
	private void saveModel(Event e) {
		if (unsavedChanges) {
			//TODO write project to file
			markUnchanged();
		}
	}
	
	@FXML
	private void saveModelAs(Event event) {
		// create new project
		if (newModel(event))
		
		// write project to file
		saveModel(event);
	}

	private void markUnchanged() {
		unsavedChanges = false;
		modelSave.setDisable(true);
		if (modelName.getText().endsWith("*")) {
			modelName.setText(modelName.getText().replace('*', '\0'));
		}
	}
	
	@FXML
	private boolean closeModel(Event e) {
		if (unsavedChanges) {
			 if (!promptToSaveChanges(e)) return false;
		}
		currentProject = null;
		markUnchanged();
		tabPane.getSelectionModel().getSelectedItem().getOnSelectionChanged().handle(e);
		tabPane.setDisable(true);
		toolBar.setDisable(true);
		modelFeedbackSpinner.getValueFactory().setValue(1);
		modelClose.setDisable(true);
		modelSave.setDisable(true);
		modelSaveAs.setDisable(true);
		return true;
	}
	
	@FXML 
	private boolean promptToSaveChanges(Event e) {
		//TODO prompt if changes should be saved
		Alert alert = new Alert(AlertType.CONFIRMATION, "Save model before closing?",ButtonType.YES,ButtonType.NO,ButtonType.CANCEL);
		alert.setTitle("Current model has unsaved changes");
		
		Optional<ButtonType> choice = alert.showAndWait();
		if (choice.isPresent()) {
			
			ButtonType type = choice.get();
			if (type == ButtonType.YES) saveModel(e); 
			else if (type == ButtonType.CANCEL) return false;
		}
		return true;

	}
	
	@FXML
	public void exit(Event arg0) {
		if (closeModel(arg0)) System.exit(0);
	}
	
	
	
	
	@FXML
	private void updateZones(Event e) {
		if (zoneTab.isSelected()) {
			//TODO (re)populate zone list
			
			if (currentProject != null) {
				String zoneFile = currentProject.getZoneFile();
				
				if (zoneFile != null && !zoneFile.isBlank()) {
					Path zonePath = currentProject.getDirectory().resolve(zoneFile);
					String relativePath = currentProject.getDirectory().toUri()
							.relativize(zonePath.toUri()).getPath();
					
					
					if (reloadZones(currentProject.getDirectory().resolve(currentProject.getZoneFile()))) {
						zoneSourceURI.setText(relativePath.toString());
						zoneBox.setDisable(false);
					}
				} else {
					zoneSourceURI.clear();
					zoneBox.setDisable(true);
					
				}
			} else {
				zoneSourceURI.clear();
				zoneList.getItems().clear();
				
			}
		}
	}
	
	private boolean reloadZones(Path path) {
		try {
			System.out.println("Reload zones");
			BufferedReader reader = Files.newBufferedReader(path);
			reader.readLine();
			AtomicInteger idx = new AtomicInteger(0);

			Set<TravelSurveyZone> zones = reader.lines()
					.map(string -> string.split(","))
					.map(args -> new TravelSurveyZone(Integer.parseInt(args[0]),idx.getAndIncrement(),AreaClass.values()[Integer.parseInt(args[1])-1]))
					.collect(Collectors.toSet());
			
			zoneList.getItems().setAll(zones);
			zoneList.getSortOrder().add(zoneIDList);
			return true;
			
		} catch (Exception e) {
			zoneSourceURI.clear();
			Alert alert = new Alert(AlertType.ERROR,"Error encountered while loading zone file. It may be corrupted.\n\nFile: "+path.toString()+"\n\nDetails: "+e.getLocalizedMessage());
			alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.show();
			return false;
		}
	}
	
	@FXML
	private void browseZones(ActionEvent e) {
		FileChooser zoneChooser = new FileChooser();
		zoneChooser.setTitle("Open Zones");
		zoneChooser.setInitialDirectory(currentProject.getDirectory().toFile());
		zoneChooser.getExtensionFilters().add(new ExtensionFilter("Comma-Separated Values","*.csv"));
		
		File selectedFile = zoneChooser.showOpenDialog(null);
		if (selectedFile != null) {
			zoneSourceURI.setText(currentProject.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			zoneSourceURI.getOnAction().handle(e);
		}
	}
	
	@FXML
	private void changeZoneSourceURI(ActionEvent e) {
		if (!zoneSourceURI.getText().equals(
				currentProject.getZoneFile()
				)) {
			if (reloadZones(currentProject.getDirectory().resolve(zoneSourceURI.getText()))) {
				currentProject.setZoneFile(zoneSourceURI.getText());
				zoneBox.setDisable(false);
				markChanged();
			}
		}
	}
	
	
	
	
	@FXML
	private void updateSkims(Event e) {

		if (skimTab.isSelected()) {

			if (!skimList.getSelectionModel().isEmpty()) {
				skimList.getSelectionModel().clearSelection();
				skimSourceURI.clear();
				skimAssignerChooser.getSelectionModel().clearSelection();
				skimFunctionChooser.getSelectionModel().clearSelection();
				skimRemove.setDisable(true);

			}
			
			if (currentProject != null) {
				skimAssignerChooser.getItems().addAll(currentProject.getAssignerIDs());
				skimList.setItems(FXCollections.observableArrayList(currentProject.getSkimIDs()));
				skimBox.setDisable(true);
			} else {
				skimList.getItems().clear();
				
				
			}
		}

	}

	@FXML
	private void changeSkimSourceURI(ActionEvent e) {
		String curSkimID = skimList.getSelectionModel().getSelectedItem();
		if (!skimSourceURI.getText()
				.equals(
						currentProject.getSkimFile(
								curSkimID))) {
			currentProject.setSkimFile(curSkimID,skimSourceURI.getText());
			markChanged();
		}
		
			
	}
	
	@FXML
	private void changeSkimAssigner(ActionEvent e) {
		String curSkimID = skimList.getSelectionModel().getSelectedItem();
		if ( !skimAssignerChooser.getSelectionModel().isEmpty() &&
				
				!skimAssignerChooser.getSelectionModel().getSelectedItem()
				.equals(
						currentProject.getSkimAssigner(
								curSkimID))) {
			currentProject.setSkimAssigner(curSkimID, skimAssignerChooser.getSelectionModel().getSelectedItem());
			markChanged();
			}
	}
	
	@FXML
	private void changeSkimFunction(ActionEvent e) {
		String curSkimID = skimList.getSelectionModel().getSelectedItem();

		if ( !skimFunctionChooser.getSelectionModel().isEmpty() && 
				!skimFunctionChooser.getSelectionModel().getSelectedItem()
				.equals(
						getDisplayString(
								currentProject.getSkimFunction(
										curSkimID)))) {
			currentProject.setSkimFunction(curSkimID, getFunctionID(skimFunctionChooser.getSelectionModel().getSelectedItem()));
			markChanged();
			}
	}
	
	@FXML
	private void browseSkim(Event e) {
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
	
	@FXML
	private void addSkim(Event e) {
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
	
	@FXML
	private void removeSkim(Event e) {

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
	private void updateMarkets(Event e) {
		if (marketTab.isSelected()) {
			
			//TODO populate market list
			//TODO populate demographic list
			//TODO populate frinction factor function list
			System.out.println("Updating markets");
		}

	}
	
	
	
	
	@FXML
	private void updateAssigners(Event e) {
		if (assignerTab.isSelected()) {
			
			//TODO populate assigner list
			System.out.println("Updating assigners");
		}

	}
	
}
