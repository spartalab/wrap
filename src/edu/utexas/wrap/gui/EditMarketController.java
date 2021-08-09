package edu.utexas.wrap.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.marketsegmentation.BasicPurpose;
import edu.utexas.wrap.marketsegmentation.DummyPurpose;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.net.Demographic;
import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class EditMarketController {
	
	@FXML
	private Tab demographicTab;
	
	@FXML
	private Tab frictFuncTab;
	
	@FXML
	private Tab purposeTab;
	
	@FXML
	private Tab surrogateTab;
	
	@FXML
	private ListView<Demographic> demographicList;
	
	@FXML
	private Button demographicAdd;
	
	@FXML
	private Button demographicRemove;
	
	@FXML
	private VBox demographicBox;
	
	@FXML
	private TextField demographicSourceURI;
	
	@FXML
	private Button demographicBrowse;
	
	@FXML
	private Button demographicEdit;
	
	@FXML
	private ListView<FrictionFactorMap> frictionFunctionList;
	
	@FXML
	private Button frictionFunctionAdd;
	
	@FXML
	private Button frictionFunctionRemove;
	
	@FXML
	private VBox frictionFunctionBox;
	
	@FXML
	private RadioButton frictionFunctionDiscretized;
	
	@FXML
	private RadioButton frictionFunctionGamma;
	
	@FXML
	private TextField frictionFunctionSourceURI;
	
	@FXML
	private Button frictionFunctionBrowse;
	
	@FXML
	private Button frictionFunctionEdit;
	
	@FXML
	private ListView<BasicPurpose> purposeList;
	
	@FXML
	private Button purposeAdd;
	
	@FXML
	private Button purposeRemove;

	@FXML
	private VBox purposeBox;
	
	@FXML
	private TextField purposeSourceURI;
	
	@FXML
	private Button purposeBrowse;
	
	@FXML
	private Button purposeEdit;
	
	@FXML
	private ListView<DummyPurpose> surrogateList;
	
	@FXML
	private Button surrogateAdd;
	
	@FXML
	private Button surrogateRemove;
	
	@FXML
	private VBox surrogateBox;
	
	@FXML
	private TextField surrogateSourceURI;
	
	@FXML
	private Button surrogateBrowse;
	
	@FXML
	private Button surrogateEdit;
	
	@FXML
	private Label marketName;
	
	@FXML
	private Label marketPath;
	
	@FXML
	private Button marketOK;
	
	@FXML
	private Button marketCancel;
	
	private Market market;
	
	private Boolean unsavedChanges;
	
	private HostServices svcs;

	private Scene scene;
	
	private Image wrapIcon;
	
	@FXML
	private TabPane tabBox;

	@FXML
	private void initialize() {
		unsavedChanges = false;
		demographicList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Demographic>() {

			@Override
			public void changed(ObservableValue<? extends Demographic> arg0, Demographic oldValue, Demographic newValue) {
				if (newValue != null) {
					demographicBox.setDisable(false);
					demographicRemove.setDisable(false);
					Path newURI = market.getDirectory().resolve(market.getDemographicFile(newValue.toString()));
					demographicSourceURI.setText(market.getDirectory().toUri().relativize(newURI.toUri()).getPath());
				} else {
					demographicBox.setDisable(true);
					demographicRemove.setDisable(true);
					demographicSourceURI.clear();
				}
			}
			
		});
		
		frictionFunctionList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FrictionFactorMap>() {

			@Override
			public void changed(ObservableValue<? extends FrictionFactorMap> arg0, FrictionFactorMap oldValue,
					FrictionFactorMap newValue) {
				if (newValue != null) {
					frictionFunctionBox.setDisable(false);
					frictionFunctionRemove.setDisable(false);
					Path newURI = market.getDirectory().resolve(market.getFrictionFunctionSource(newValue.toString()));
					frictionFunctionSourceURI.setText(market.getDirectory().toUri().relativize(newURI.toUri()).getPath());
					
				} else {
					frictionFunctionBox.setDisable(true);
					frictionFunctionSourceURI.clear();
					frictionFunctionRemove.setDisable(true);
				}
			}
			
		});
		
		purposeList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<BasicPurpose>() {

			@Override
			public void changed(ObservableValue<? extends BasicPurpose> arg0, BasicPurpose oldValue, BasicPurpose newValue) {
				if (newValue != null) {
					purposeBox.setDisable(false);
					purposeRemove.setDisable(false);
					Path newURI = market.getDirectory().resolve(market.getPurposeSource(newValue.toString()));
					purposeSourceURI.setText(market.getDirectory().toUri().relativize(newURI.toUri()).getPath());
				} else {
					purposeBox.setDisable(true);
					purposeRemove.setDisable(true);
					purposeSourceURI.clear();
				}
			}
			
		});

	}
	
	public void setMarket(Market selected) {
		market = selected;
	
		
		marketName.setText(market.toString());
		marketPath.setText(market.getDirectory().toString());
		tabBox.getSelectionModel().getSelectedItem().getOnSelectionChanged().handle(new ActionEvent());

	}
	
	private void markChanged() {
		unsavedChanges = true;
		marketOK.setDisable(false);
		if (!marketName.getText().endsWith("*")) {
			marketName.setText(marketName.getText()+"*");
		}
	}
	
	@FXML
	private boolean promptToSaveChanges(Event event) {
		if (unsavedChanges) {
			//TODO
			Alert alert = new Alert(AlertType.CONFIRMATION,"Save market changes?",ButtonType.YES,ButtonType.NO,ButtonType.CANCEL);
			alert.setTitle("Current market has unsaved changes");
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(wrapIcon);
			Optional<ButtonType> choice = alert.showAndWait();
			if (choice.isPresent()) {
				ButtonType type = choice.get();
				if (type == ButtonType.YES) {
					if (!saveMarket(event)) return false;
				}
				else if (type == ButtonType.CANCEL) return false;
			}
		}
		return true;
	}
	
	private boolean saveMarket(Event e) {
		if (unsavedChanges) {
			//TODO
			return true;
		}
		return true;
	}

	@FXML
	private void saveAndExit(ActionEvent e) {
		try{ 
			if (saveMarket(e)) {
				market.reloadProperties();
				closeWindow(e);
			}
		} catch (IOException except) {
			//TODO
			except.printStackTrace();
		}
	}
	
	protected void setServices(HostServices svcs) {
		this.svcs = svcs;
	}
	
	protected void setScene(Scene scene) {
		this.scene = scene;
	}
	
	@FXML
	private void closeWindow(Event e) {
		scene.getWindow().hide();
	}
	
	protected void exit(Event arg0) {

		try {
			if (!promptToSaveChanges(arg0)) {
				arg0.consume();
				return;
			}
			market.reloadProperties();
			closeWindow(arg0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	protected void setIcon(Image wrapIcon) {
		this.wrapIcon = wrapIcon;
		
	}
	
	
	
	
	
	@FXML
	private void editDemographic(ActionEvent event) {
		svcs.showDocument(market.getDirectory().resolve(demographicSourceURI.getText()).toString());
	}
	
	@FXML
	private void browseDemographic(ActionEvent event) {
		FileChooser demoChooser = new FileChooser();
		demoChooser.setTitle("Open Demographic");
		demoChooser.setInitialDirectory(market.getDirectory().toFile());
		demoChooser.getExtensionFilters().add(new ExtensionFilter("Comma-Separated Values","*.csv"));
		
		File selectedFile = demoChooser.showOpenDialog(scene.getWindow());
		if (selectedFile != null) {
			demographicSourceURI.setText(market.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			demographicSourceURI.getOnAction().handle(event);
		}
	}
	
	@FXML
	private void changeDemographicSourceURI(ActionEvent event) {
		
		Demographic demographic = demographicList.getSelectionModel().getSelectedItem();
		if (!demographicSourceURI.getText().equals(
				market.getDemographicFile(demographic.toString())
				)) {
			market.setDemographicFile(demographic.toString(),demographicSourceURI.getText());
			markChanged();
		}
	}
	
	@FXML
	private void updateDemographics(Event event) {

		if (demographicTab.isSelected()) {
			demographicList.getItems().clear();
			demographicSourceURI.clear();
			demographicBox.setDisable(true);
			if (market != null) {
				demographicList.getItems().setAll(market.getDemographics());

				demographicList.getItems().sort(new Comparator<Demographic>() {

					@Override
					public int compare(Demographic o1, Demographic o2) {
						return o1.toString().compareTo(o2.toString());
					}

				});
			}
		}

	}
	
	@FXML
	private void addDemographic(ActionEvent event) {
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		try {
			FXMLLoader loader = new FXMLLoader();
			VBox vbox = loader.load(getClass().getResource("/edu/utexas/wrap/gui/newDemographicDialog.fxml").openStream());
			NewDemographicController controller = loader.getController();
			DialogPane pane = new DialogPane();
			
			pane.setContent(vbox);
			pane.getButtonTypes().add(ButtonType.CANCEL);
			pane.getButtonTypes().add(ButtonType.OK);
			
			dialog.setDialogPane(pane);
			dialog.setTitle("New Demographic");
			Button ok = (Button) pane.lookupButton(ButtonType.OK);
			ok.disableProperty().bind(controller.notReady());
			((Stage) pane.getScene().getWindow()).getIcons().add(wrapIcon);
			
			controller.setScene(pane.getScene());
			controller.setMarket(market);
			
			dialog.showAndWait();
			if (dialog.getResult() == ButtonType.OK) {
				market.addDemographic(controller.getDemographicID(),controller.getDemographicSourceURI());
				demographicList.getItems().setAll(market.getDemographics());
				demographicList.getItems().sort(new Comparator<Demographic>() {
					@Override
					public int compare(Demographic o1, Demographic o2) {
						return o1.toString().compareTo(o2.toString());
					}
					
				});
				markChanged();
			}
		} catch (IOException e) {
			//TODO
		}
	
	}
	
	@FXML
	private void removeDemographic(ActionEvent event) {
		//TODO
		Demographic selected = demographicList.getSelectionModel().getSelectedItem();
		
		if (selected != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION,"Remove demographic "+selected+" from market?",ButtonType.YES,ButtonType.NO);
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(wrapIcon);
			
			alert.showAndWait();
			if (alert.getResult() == ButtonType.YES) {
				demographicList.getItems().remove(selected);
				market.removeDemographic(selected);
				markChanged();
			}
		}
	}
	
	
	
	
		
	@FXML
	private void updateFrictionFunctions(Event event) {
		
		if (frictFuncTab.isSelected()) {
			frictionFunctionList.getItems().clear();
			frictionFunctionSourceURI.clear();
			frictionFunctionBox.setDisable(true);
			if (market != null){
				frictionFunctionList.getItems().setAll(market.getFrictionFunctions());
				frictionFunctionList.getItems().sort(new Comparator<FrictionFactorMap>() {

					@Override
					public int compare(FrictionFactorMap o1, FrictionFactorMap o2) {
						return o1.toString().compareTo(o2.toString());
					}

				});
			}
		}
	}
	
	@FXML
	private void browseFrictionFunction(ActionEvent event) {
		FileChooser fricFuncChooser = new FileChooser();
		fricFuncChooser.setTitle("Open Friction Function");
		fricFuncChooser.setInitialDirectory(market.getDirectory().toFile());
		fricFuncChooser.getExtensionFilters().add(new ExtensionFilter("Comma-Separated Values","*.csv"));
		
		File selectedFile = fricFuncChooser.showOpenDialog(scene.getWindow());
		if (selectedFile != null) {
			frictionFunctionSourceURI.setText(market.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			frictionFunctionSourceURI.getOnAction().handle(event);
		}
	
	}
	
	@FXML
	private void editFrictionFunction(ActionEvent event) {
		svcs.showDocument(market.getDirectory().resolve(frictionFunctionSourceURI.getText()).toString());
	}
	
	@FXML
	private void changeFrictionFunctionSourceURI(ActionEvent event) {
		FrictionFactorMap ffunc = frictionFunctionList.getSelectionModel().getSelectedItem();
		
		if (!frictionFunctionSourceURI.getText().equals(market.getFrictionFunctionSource(ffunc.toString()))) {
			market.setFrictionFunctionSource(ffunc.toString(),frictionFunctionSourceURI.getText());
			markChanged();
		}
	}
	
	@FXML
	private void addFrictionFunction(ActionEvent event) {
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		try {
			FXMLLoader loader = new FXMLLoader();
			VBox vbox = loader.load(getClass().getResource("/edu/utexas/wrap/gui/newFrictionFunctionDialog.fxml").openStream());
			NewFrictionFunctionController controller = loader.getController();
			DialogPane pane = new DialogPane();
			
			pane.setContent(vbox);
			pane.getButtonTypes().add(ButtonType.CANCEL);
			pane.getButtonTypes().add(ButtonType.OK);
			
			dialog.setDialogPane(pane);
			dialog.setTitle("New Friction Function");
			Button ok = (Button) pane.lookupButton(ButtonType.OK);
			ok.disableProperty().bind(controller.notReady());
			((Stage) pane.getScene().getWindow()).getIcons().add(wrapIcon);
			
			controller.setScene(pane.getScene());
			controller.setMarket(market);
			
			dialog.showAndWait();
			if (dialog.getResult() == ButtonType.OK) {
				market.addFrictionFunction(controller.getFunctionID(), controller.getFunctionSourceURI());
				frictionFunctionList.getItems().setAll(market.getFrictionFunctions());
				frictionFunctionList.getItems().sort(new Comparator<FrictionFactorMap>() {

					@Override
					public int compare(FrictionFactorMap o1, FrictionFactorMap o2) {
						return o1.toString().compareTo(o2.toString());
					}
					
				});
				markChanged();
			}
		} catch (IOException e) {
			//TODO
		}
	}
	
	@FXML
	private void removeFrictionFunction(ActionEvent event) {
	
		FrictionFactorMap selected = frictionFunctionList.getSelectionModel().getSelectedItem();
		
		if (selected != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION,"Remove friction function "+selected+" from market?", ButtonType.YES,ButtonType.NO);
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(wrapIcon);
			
			alert.showAndWait();
			
			if (alert.getResult() == ButtonType.YES) {
				frictionFunctionList.getItems().remove(selected);
				market.removeFrictionFunction(selected);
				markChanged();
			}
		}
	}	
	
	
	
	
	
	@FXML
	private void updatePurposes(Event event) {
		if (purposeTab.isSelected()) {
			purposeList.getItems().clear();
			purposeSourceURI.clear();
			purposeBox.setDisable(true);
			
			if (market != null) {
				purposeList.getItems().setAll(market.getBasicPurposes());
				
				purposeList.getItems().sort(new Comparator<BasicPurpose>() {

					@Override
					public int compare(BasicPurpose o1, BasicPurpose o2) {
						return o1.toString().compareTo(o2.toString());
					}
					
				});
			}
		}
	}
	
	@FXML
	private void browsePurpose(ActionEvent event) {
		FileChooser purposeChooser = new FileChooser();
		purposeChooser.setTitle("Open Purpose");
		purposeChooser.setInitialDirectory(market.getDirectory().toFile());
		purposeChooser.getExtensionFilters().add(new ExtensionFilter("wrap Purpose Files","*.wrppr"));
		
		
		File selectedFile = purposeChooser.showOpenDialog(scene.getWindow());
		if (selectedFile != null) {
			purposeSourceURI.setText(market.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			purposeSourceURI.getOnAction().handle(event);
		}
	}
	
	@FXML
	private void editPurpose(ActionEvent event) {
		//TODO
	}
	
	@FXML
	private void changePurposeSourceURI(ActionEvent event) {
		BasicPurpose purpose = purposeList.getSelectionModel().getSelectedItem();
		
		if (!purposeSourceURI.getText().equals(market.getPurposeSource(purpose.toString()))) {
			market.setPurposeSource(purpose.toString(),purposeSourceURI.getText());
			markChanged();
		}
	}
	
	@FXML
	private void addPurpose(ActionEvent event) {
		//TODO
	}
	
	@FXML
	private void removePurpose(ActionEvent event) {
		//TODO
	}
	
	
	
	
	
	@FXML
	private void updateSurrogates(Event event) {
		//TODO
	}
	
}
