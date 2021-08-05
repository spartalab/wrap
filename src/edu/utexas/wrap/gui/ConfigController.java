/**
 * Sample Skeleton for 'wrap.fxml' Controller Class
 */

package edu.utexas.wrap.gui;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import edu.utexas.wrap.Project;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import javafx.application.HostServices;
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
import javafx.scene.image.Image;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ConfigController {
	
	@FXML
	private Scene scene;

	private Image wrapIcon;

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
	private ListView<NetworkSkim> skimList;

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
	private ListView<Market> marketList;

	@FXML
	private VBox marketBox;

	@FXML
	private Button marketAttach;
	
	@FXML
	private Button marketCreate;

	@FXML
	private Button marketRemove;

	@FXML
	private Button marketEdit;

	@FXML
	private Button marketBrowse;

	@FXML
	private TextField marketSourceURI;
	
	
	
	
	
	
	@FXML
	private Tab assignerTab;
	
	@FXML
	private ListView<String> assignerList;

	@FXML
	private VBox assignerBox;
	
	@FXML
	private Button assignerAttach;

	@FXML
	private Button assignerRemove;

	@FXML
	private ComboBox<String> assignerClass;

	@FXML
	private TextField assignerSourceURI;

	@FXML
	private Button assignerBrowse;

	@FXML
	private Button assignerEdit;
	
	@FXML
	private Button assignerCreate;
	
	
	private HostServices svcs;
	
	private Project currentProject;
	
	private Boolean unsavedChanges;
	
	private String getDisplayString(String optionID) {
		if (optionID == null) return null;
		switch (optionID) {
		case "travelTimeSingleOcc":
			return "Travel time (exclude HOV)";
			
		case "travelTime":
			return "Travel time";
			
		case "builtin":
			return "Built-in Static Assigner";
		case "file":
			return "File Output";
		case "stream":
			return "External Static Assigner";
		default:
			return null;
		}
	}
	
	private String getOptionID(String displayString) {
		if (displayString == null) return null;
		switch (displayString) {
		case "Travel time (exclude HOV)":
			return "travelTimeSingleOcc";
		case "Travel time":
			return "travelTime";
		case "Built-in Static Assigner":
			return "builtin";
		case "File Output":
			return "file";
		case "External Static Assigner":
			return "stream";
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
		wrapIcon = new Image("/edu/utexas/wrap/gui/wrap.png");
		modelFeedbackSpinner.valueProperty().addListener(new ChangeListener<Integer>() {

			@Override
			public void changed(ObservableValue<? extends Integer> arg0, Integer oldValue, Integer newValue) {
				if (currentProject != null && newValue != currentProject.getMaxIterations()) {
					//TODO modify project to reflect new maxIteration value
					markChanged();
				}
			}
			
		});

		zoneIDList.setCellValueFactory(new Callback<CellDataFeatures<TravelSurveyZone, Integer>, ObservableValue<Integer>>(){

			@Override
			public ObservableValue<Integer> call(CellDataFeatures<TravelSurveyZone, Integer> arg0) {
				return new ReadOnlyObjectWrapper<Integer>(arg0.getValue().getID());
			}
			
		});
		
		zoneClassList.setCellValueFactory(new Callback<CellDataFeatures<TravelSurveyZone,String>,ObservableValue<String>>(){

			@Override
			public ObservableValue<String> call(CellDataFeatures<TravelSurveyZone, String> arg0) {
				return new ReadOnlyObjectWrapper<String>(arg0.getValue().getAreaClass().name());
			}
			
		});
		
		skimFunctionChooser.getItems().addAll("Travel time","Travel time (exclude HOV)");

		assignerClass.getItems().addAll("Built-in Static Assigner","External Static Assigner","File Output");

		skimList.getSelectionModel().selectedItemProperty().addListener( 

				new ChangeListener<NetworkSkim>() {

					@Override
					public void changed(ObservableValue<? extends NetworkSkim> arg0, NetworkSkim oldValue, NetworkSkim newValue) {

						if (newValue != null) {

							skimBox.setDisable(false);
							skimRemove.setDisable(false);
							Path newURI = currentProject.getDirectory().resolve(currentProject.getSkimFile(newValue.toString()));
							skimSourceURI.setText(currentProject.getDirectory().toUri().relativize(newURI.toUri()).getPath());
							skimAssignerChooser.getSelectionModel().select(currentProject.getSkimAssigner(newValue.toString()));
							skimFunctionChooser.getSelectionModel().select(getDisplayString(currentProject.getSkimFunction(newValue.toString())));
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
		
		marketList.getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<Market>() {

					@Override
					public void changed(ObservableValue<? extends Market> arg0, Market oldValue, Market newValue) {

						if (newValue != null) {
							marketBox.setDisable(false);
							marketRemove.setDisable(false);
							marketEdit.setDisable(false);
							Path newURI = currentProject.getDirectory().resolve(currentProject.getMarketFile(newValue.toString()));
							marketSourceURI.setText(currentProject.getDirectory().toUri().relativize(newURI.toUri()).getPath());
							
						} else {
							marketSourceURI.clear();
							marketRemove.setDisable(true);
							marketEdit.setDisable(true);
							marketBox.setDisable(true);
						}
					}
				}
				);
		
		assignerList.getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<String>() {

					@Override
					public void changed(ObservableValue<? extends String> arg0, String oldValue, String newValue) {
						if (newValue != null) {
							assignerBox.setDisable(false);
							assignerRemove.setDisable(false);
							Path newURI = currentProject.getDirectory().resolve(currentProject.getAssignerFile(newValue));
							assignerSourceURI.setText(currentProject.getDirectory().toUri().relativize(newURI.toUri()).getPath());
							assignerClass.getSelectionModel().select(getDisplayString(currentProject.getAssignerClass(newValue)));
						} else {
							assignerSourceURI.clear();
							assignerClass.getSelectionModel().clearSelection();
							assignerRemove.setDisable(true);
							assignerBox.setDisable(true);
						}
					}
					
				}
				);
		
	}
	
	@FXML
	private boolean newModel(Event event) {
		
		if (currentProject != null && !closeModel(event)) return false;
		
		FileChooser modelChooser = new FileChooser();
		modelChooser.setTitle("New Model");
		modelChooser.getExtensionFilters().add(new ExtensionFilter("wrap Project Files","*.wrp"));
		
		File projFile = modelChooser.showSaveDialog(scene.getWindow());
		if (projFile == null) return false;

		
		try {

			currentProject = new Project(projFile.toPath());
			
			loadModel(event);
			markChanged();
			return true;
			
		} catch (Exception exception) {
			exception.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR,exception.getLocalizedMessage());
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(wrapIcon);

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

		File selectedFile = modelChooser.showOpenDialog(scene.getWindow());

		if (selectedFile != null) try {

			currentProject = new Project(selectedFile.toPath());
//			currentProject.loadPropsFromFile();

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
		modelName.setText("");
		modelDirectory.setText("");
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
		// prompt if changes should be saved
		Alert alert = new Alert(AlertType.CONFIRMATION, "Save model before closing?",ButtonType.YES,ButtonType.NO,ButtonType.CANCEL);
		alert.setTitle("Current model has unsaved changes");
		((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(wrapIcon);
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
	public void about(ActionEvent arg0) {
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		try {
			FXMLLoader loader = new FXMLLoader();
			VBox vbox = loader.load(getClass().getResource("/edu/utexas/wrap/gui/aboutDialog.fxml").openStream());
			AboutDialog controller = loader.getController();
			
			controller.setHostServices(svcs);
			DialogPane pane = new DialogPane();

			pane.setContent(vbox);
			dialog.setTitle("About wrap");
			pane.getButtonTypes().add(ButtonType.OK);
			dialog.setDialogPane(pane);
			((Stage) pane.getScene().getWindow()).getIcons().add(wrapIcon);
			dialog.showAndWait();
			
		} catch (IOException e) {
			//TODO
			e.printStackTrace();
		}
	}
	
	
	
	@FXML
	private void updateZones(Event e) {
		if (zoneTab.isSelected()) {
			// (re)populate zone list
			zoneList.getItems().clear();
			zoneSourceURI.clear();
			zoneBox.setDisable(true);
			zoneEdit.setDisable(true);
			
			if (currentProject != null) {
				String zoneFile = currentProject.getZoneFile();
				
				if (zoneFile != null && !zoneFile.isBlank()) {
					Path zonePath = currentProject.getDirectory().resolve(zoneFile);
					String relativePath = currentProject.getDirectory().toUri()
							.relativize(zonePath.toUri()).getPath();
					
					
					if (reloadZones(currentProject.getDirectory().resolve(currentProject.getZoneFile()))) {
						zoneSourceURI.setText(relativePath.toString());
						zoneEdit.setDisable(false);
						zoneBox.setDisable(false);
					}
				} 
			} 
		}
	}
	
	private boolean reloadZones(Path path) {
		try {
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
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(wrapIcon);

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
		
		File selectedFile = zoneChooser.showOpenDialog(scene.getWindow());
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
	private void editZones(ActionEvent e) {
		//TODO open external zone editing stage
		svcs.showDocument(currentProject.getDirectory().resolve(zoneSourceURI.getText()).toString());
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
				skimAssignerChooser.getItems().clear();
				skimAssignerChooser.getItems().addAll(currentProject.getAssignerIDs());
				skimList.setItems(FXCollections.observableArrayList(currentProject.getSkims()));
				skimList.getItems().sort(new Comparator<NetworkSkim>() {

					@Override
					public int compare(NetworkSkim o1, NetworkSkim o2) {
						return o1.toString().compareTo(o2.toString());
					}
					
				});
				skimBox.setDisable(true);
			} else {
				skimList.getItems().clear();
				
				
			}
		}

	}

	@FXML
	private void changeSkimSourceURI(ActionEvent e) {
		NetworkSkim curSkim = skimList.getSelectionModel().getSelectedItem();
		if (!skimSourceURI.getText()
				.equals(
						currentProject.getSkimFile(
								curSkim.toString()))) {
			currentProject.setSkimFile(curSkim.toString(),skimSourceURI.getText());
			markChanged();
		}
		
			
	}
	
	@FXML
	private void changeSkimAssigner(ActionEvent e) {
		NetworkSkim curSkim = skimList.getSelectionModel().getSelectedItem();
		if ( !skimAssignerChooser.getSelectionModel().isEmpty() &&
				
				!skimAssignerChooser.getSelectionModel().getSelectedItem()
				.equals(
						currentProject.getSkimAssigner(
								curSkim.toString()))) {
			currentProject.setSkimAssigner(curSkim.toString(), skimAssignerChooser.getSelectionModel().getSelectedItem());
			markChanged();
			}
	}
	
	@FXML
	private void changeSkimFunction(ActionEvent e) {
		NetworkSkim curSkim = skimList.getSelectionModel().getSelectedItem();

		if ( !skimFunctionChooser.getSelectionModel().isEmpty() && 
				!skimFunctionChooser.getSelectionModel().getSelectedItem()
				.equals(
						getDisplayString(
								currentProject.getSkimFunction(
										curSkim.toString())))) {
			currentProject.setSkimFunction(curSkim.toString(), getOptionID(skimFunctionChooser.getSelectionModel().getSelectedItem()));
			markChanged();
			}
	}
	
	@FXML
	private void browseSkim(Event e) {
		FileChooser skimChooser = new FileChooser();
		skimChooser.setTitle("Open Skim");
		skimChooser.setInitialDirectory(currentProject.getDirectory().toFile());
		skimChooser.getExtensionFilters().add(new ExtensionFilter("Comma-Separated Values","*.csv"));
		
		File selectedFile = skimChooser.showOpenDialog(scene.getWindow());
		if (selectedFile != null) {
			skimSourceURI.setText(currentProject.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			skimSourceURI.getOnAction().handle(new ActionEvent());
		}
	}
	
	@FXML
	private void addSkim(Event e) {
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
			dialog.setTitle("New Skim");
			Button ok = (Button) pane.lookupButton(ButtonType.OK);
			ok.disableProperty().bind(controller.notReady());	
			((Stage) pane.getScene().getWindow()).getIcons().add(wrapIcon);

			dialog.showAndWait();
		
			if (dialog.getResult() == ButtonType.OK) {
				currentProject.addSkim(controller.getSkimID(), 
						controller.getSkimAssigner(), 
						controller.getSkimFunction(), 
						controller.getSkimSourceURI());
				skimList.getItems().setAll(currentProject.getSkims());
				skimList.getItems().sort(new Comparator<NetworkSkim>() {

					@Override
					public int compare(NetworkSkim o1, NetworkSkim o2) {
						return o1.toString().compareTo(o2.toString());
					}
					
				});
				markChanged();
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}
	
	@FXML
	private void removeSkim(Event e) {

		NetworkSkim curSkim = skimList.getSelectionModel().getSelectedItem();
		if (curSkim != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION,"Remove skim "+curSkim+" from model?",ButtonType.YES,ButtonType.NO);
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(wrapIcon);
			alert.showAndWait();
			
			if (alert.getResult() == ButtonType.YES) {
				skimList.getItems().remove(curSkim);
				currentProject.removeSkim(curSkim);
				markChanged();
			}
		}
	}
	
	
	
	
	@FXML
	private void updateMarkets(Event e) {
		if (marketTab.isSelected()) {
			
			// populate market list
			if (!marketList.getSelectionModel().isEmpty()) {
				marketList.getSelectionModel().clearSelection();
				marketSourceURI.clear();
				marketRemove.setDisable(true);
			}
			
			if (currentProject != null) {
				marketList.setItems(FXCollections.observableArrayList(currentProject.getMarkets()));
				marketList.getItems().sort(new Comparator<Market>() {

					@Override
					public int compare(Market o1, Market o2) {
						// TODO Auto-generated method stub
						return o1.toString().compareTo(o2.toString());
					}
					
				});
				marketBox.setDisable(true);
			} else {
				marketList.getItems().clear();
			}
			
		}

	}
	
	@FXML
	private void browseMarket(ActionEvent e) {
		FileChooser marketChooser = new FileChooser();
		marketChooser.setTitle("Open Market");
		marketChooser.setInitialDirectory(currentProject.getDirectory().toFile());
		marketChooser.getExtensionFilters().add(new ExtensionFilter("wrap Market file","*.wrm"));
		
		File selectedFile = marketChooser.showOpenDialog(scene.getWindow());
		if (selectedFile != null) {
			marketSourceURI.setText(currentProject.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			marketSourceURI.getOnAction().handle(e);
		}
	}
	
	@FXML
	private void editMarket(ActionEvent e) {
		//TODO
		Market selected = marketList.getSelectionModel().getSelectedItem();
		try {
			FXMLLoader loader = new FXMLLoader();
			VBox vbox = loader.load(getClass().getResource("/edu/utexas/wrap/gui/marketDialog.fxml").openStream());
			
			EditMarketController controller = loader.getController();
			controller.setMarket(selected);
			
			Scene pane = new Scene(vbox);


			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(pane);
			stage.setTitle("Edit Market");
			stage.getIcons().add(wrapIcon);
			
			stage.showAndWait();
		} catch (IOException except) {
			//TODO
			except.printStackTrace();
		}
	}
	
	@FXML
	private void createMarket(ActionEvent e) {
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		try {
			FXMLLoader loader = new FXMLLoader();
			VBox vbox = loader.load(getClass().getResource("/edu/utexas/wrap/gui/newMarketDialog.fxml").openStream());
			NewMarketController controller = loader.getController();
			controller.setProject(currentProject);
			DialogPane pane = new DialogPane();
			

			pane.setContent(vbox);
			pane.getButtonTypes().add(ButtonType.CANCEL);
			pane.getButtonTypes().add(ButtonType.OK);
						
			dialog.setDialogPane(pane);
			dialog.setTitle("New Market");
			Button ok = (Button) pane.lookupButton(ButtonType.OK);
			ok.setText("Create...");
			ok.disableProperty().bind(controller.notReady());
			((Stage) pane.getScene().getWindow()).getIcons().add(wrapIcon);
			dialog.showAndWait();
			
			if (dialog.getResult() == ButtonType.OK) {
				String id = controller.getMarketID();
				currentProject.addMarket(id,controller.getMarketSourceURI());
				marketList.getItems().setAll(currentProject.getMarkets());
				marketList.getItems().sort(new Comparator<Market>() {

					@Override
					public int compare(Market o1, Market o2) {
						// TODO Auto-generated method stub
						return o1.toString().compareTo(o2.toString());
					}
					
				});
				//TODO open dialog to edit newly created market
				markChanged();
				
			}
		} catch (IOException exception) {
			//TODO
		}
	}
	
	@FXML
	private void attachMarket(ActionEvent e) {
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		try {
			FXMLLoader loader = new FXMLLoader();
			VBox vbox = loader.load(getClass().getResource("/edu/utexas/wrap/gui/attachMarketDialog.fxml").openStream());
			AttachMarketController controller = loader.getController();
			controller.setProject(currentProject);
			DialogPane pane = new DialogPane();
			

			
			pane.setContent(vbox);
			pane.getButtonTypes().add(ButtonType.CANCEL);
			pane.getButtonTypes().add(ButtonType.OK);
			
			dialog.setDialogPane(pane);
			dialog.setTitle("Attach Market");
			Button ok = (Button) pane.lookupButton(ButtonType.OK);
			ok.disableProperty().bind(controller.notReady());
			((Stage) pane.getScene().getWindow()).getIcons().add(wrapIcon);
			dialog.showAndWait();
			
			if (dialog.getResult() == ButtonType.OK) {
				currentProject.addMarket(controller.getMarketID(),controller.getMarketSourceURI());
				marketList.getItems().setAll(currentProject.getMarkets());
				marketList.getItems().sort(new Comparator<Market>() {

					@Override
					public int compare(Market o1, Market o2) {
						// TODO Auto-generated method stub
						return o1.toString().compareTo(o2.toString());
					}
					
				});
				markChanged();
			}
		} catch (IOException exception) {
			//TODO
		}
	}
	
	@FXML
	private void removeMarket(ActionEvent e) {
		Market market = marketList.getSelectionModel().getSelectedItem();
		if (market != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION,"Remove market "+market+" from model?",ButtonType.YES,ButtonType.NO);
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(wrapIcon);

			alert.showAndWait();
			
			if (alert.getResult() == ButtonType.YES) {
				marketList.getItems().remove(market);
				currentProject.removeMarket(market);
				markChanged();
			}
		}
	}
	
	@FXML
	private void changeMarketSourceURI(ActionEvent e) {
		
		Market curMarket = marketList.getSelectionModel().getSelectedItem();
		if (!marketSourceURI.getText()
				.equals(
						currentProject.getMarketFile(curMarket.toString())
						)) {
			currentProject.setMarketFile(curMarket.toString(),marketSourceURI.getText());
			markChanged();
		}
	}
	
	
	
	@FXML
	private void updateAssigners(Event e) {
		if (assignerTab.isSelected()) {
			
			// populate assigner list
			if (!assignerList.getSelectionModel().isEmpty()) {
				assignerList.getSelectionModel().clearSelection();
				assignerSourceURI.clear();
				assignerRemove.setDisable(true);
			}
			
			if (currentProject != null) {
				assignerList.setItems(FXCollections.observableArrayList(currentProject.getAssignerIDs()));
				assignerBox.setDisable(true);
			} else {
				assignerList.getItems().clear();
			}
		}

	}
	
	@FXML
	private void changeAssignerClass(ActionEvent e) {
		String curAssignerID = assignerList.getSelectionModel().getSelectedItem();
		
		if (!assignerClass.getSelectionModel().isEmpty() && 
				!assignerClass.getSelectionModel().getSelectedItem()
				.equals(
						getDisplayString(currentProject.getAssignerClass(curAssignerID))
						)) {
			currentProject.setAssignerClass(curAssignerID,getOptionID(assignerClass.getSelectionModel().getSelectedItem()));
			markChanged();
		}
	}
	
	@FXML
	private void changeAssignerSourceURI(ActionEvent e) {
		String curAssignerID = assignerList.getSelectionModel().getSelectedItem();
		
		if (!assignerSourceURI.getText().equals(
				currentProject.getAssignerFile(curAssignerID)
				)) {
			currentProject.setAssignerFile(curAssignerID,assignerSourceURI.getText());
			markChanged();
		}
	}
	
	@FXML
	private void browseAssigner(ActionEvent e) {
		FileChooser assignerChooser = new FileChooser();
		assignerChooser.setTitle("Open Assigner");
		assignerChooser.setInitialDirectory(currentProject.getDirectory().toFile());
		assignerChooser.getExtensionFilters().add(new ExtensionFilter("wrap Assigner file","*.wrapr"));
		
		File selectedFile = assignerChooser.showOpenDialog(scene.getWindow());
		if (selectedFile != null) {
			assignerSourceURI.setText(currentProject.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			assignerSourceURI.getOnAction().handle(e);
		}
	}
	
	@FXML
	private void attachAssigner(ActionEvent e) {
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		try {
			FXMLLoader loader = new FXMLLoader();
			VBox vbox = loader.load(getClass().getResource("/edu/utexas/wrap/gui/attachAssignerDialog.fxml").openStream());
			AttachAssignerController controller = loader.getController();
			controller.setProject(currentProject);
			DialogPane pane = new DialogPane();
			

			pane.setContent(vbox);
			pane.getButtonTypes().add(ButtonType.OK);
			pane.getButtonTypes().add(ButtonType.CANCEL);
			
			dialog.setDialogPane(pane);
			dialog.setTitle("Attach Assigner");
			Button ok = (Button) pane.lookupButton(ButtonType.OK);
			ok.disableProperty().bind(controller.notReady());
			((Stage) pane.getScene().getWindow()).getIcons().add(wrapIcon);
			dialog.showAndWait();
			
			if (dialog.getResult() == ButtonType.OK) {
				currentProject.addAssigner(controller.getAssignerID(),getOptionID(controller.getAssignerClass()),controller.getAssignerSourceURI());
				assignerList.getItems().add(controller.getAssignerID());
				markChanged();
			}
		} catch (IOException exception) {
			//TODO
		}
	}
	
	@FXML
	private void editAssigner(ActionEvent e) {
		//TODO
	}
	
	@FXML
	private void createAssigner(ActionEvent e) {
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		try {
			FXMLLoader loader = new FXMLLoader();
			VBox vbox = loader.load(getClass().getResource("/edu/utexas/wrap/gui/newAssignerDialog.fxml").openStream());
			NewAssignerController controller = loader.getController();
			controller.setProject(currentProject);
			DialogPane pane = new DialogPane();

			pane.setContent(vbox);
			pane.getButtonTypes().add(ButtonType.OK);
			pane.getButtonTypes().add(ButtonType.CANCEL);
			
			dialog.setDialogPane(pane);
			dialog.setTitle("New Assigner");
			Button ok = (Button) pane.lookupButton(ButtonType.OK);
			ok.setText("Create...");
			ok.disableProperty().bind(controller.notReady());
			((Stage) pane.getScene().getWindow()).getIcons().add(wrapIcon);
			dialog.showAndWait();
			//TODO open dialog to configure assigner
			
			if (dialog.getResult() == ButtonType.OK) {
				currentProject.addAssigner(controller.getAssignerID(),getOptionID(controller.getAssignerClass()),controller.getAssignerSourceURI());
				assignerList.getItems().add(controller.getAssignerID());
				markChanged();
			}
		} catch (IOException exception) {
			//TODO
		}
	}

	@FXML
	private void removeAssigner(ActionEvent e) {
		String assignerName = assignerList.getSelectionModel().getSelectedItem();
		if (assignerName != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION,"Remove assigner "+assignerName+" from model?",ButtonType.YES,ButtonType.NO);
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(wrapIcon);

			alert.showAndWait();
			
			if (alert.getResult() == ButtonType.YES) {
				assignerList.getItems().remove(assignerName);
				currentProject.removeAssigner(assignerName);
				markChanged();
			}
		}
	}

	public void setHostServices(HostServices hostServices) {
		// TODO Auto-generated method stub
		svcs = hostServices;
	}
}
