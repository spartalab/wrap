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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
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
	private Button marketBrowse;
	
	private Market market;
	
	private Boolean unsavedChanges;
	
	private HostServices svcs;

	private Scene scene;
	
	private Image wrapIcon;

	@FXML
	private void initialize() {
		unsavedChanges = false;
		demographicList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Demographic>() {

			@Override
			public void changed(ObservableValue<? extends Demographic> arg0, Demographic oldValue, Demographic newValue) {
				// TODO Auto-generated method stub
				if (newValue != null) {
					demographicBox.setDisable(false);
					demographicRemove.setDisable(false);
					Path newURI = market.getDirectory().resolve(market.getDemographicFile(newValue.toString()));
					demographicSourceURI.setText(market.getDirectory().toUri().relativize(newURI.toUri()).getPath());
				} else {
					demographicBox.setDisable(true);
					demographicRemove.setDisable(true);
				}
			}
			
		});
	}
	
	public void setMarket(Market selected) {
		// TODO Auto-generated method stub
		market = selected;
		demographicList.getItems().setAll(market.getDemographics());
		frictionFunctionList.getItems().setAll(market.getFrictionFunctions());
		purposeList.getItems().setAll(market.getBasicPurposes());
		
		demographicList.getItems().sort(new Comparator<Demographic>() {

			@Override
			public int compare(Demographic o1, Demographic o2) {
				return o1.toString().compareTo(o2.toString());
			}
			
		});
		frictionFunctionList.getItems().sort(new Comparator<FrictionFactorMap>() {

			@Override
			public int compare(FrictionFactorMap o1, FrictionFactorMap o2) {
				return o1.toString().compareTo(o2.toString());
			}
			
		});
		purposeList.getItems().sort(new Comparator<BasicPurpose>() {

			@Override
			public int compare(BasicPurpose o1, BasicPurpose o2) {
				// TODO Auto-generated method stub
				return o1.toString().compareTo(o2.toString());
			}
			
		});
		
		marketName.setText(market.toString());
		marketPath.setText(market.getDirectory().toString());
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
}
