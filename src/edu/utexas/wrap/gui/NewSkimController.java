package edu.utexas.wrap.gui;

import java.io.File;

import edu.utexas.wrap.Project;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

public class NewSkimController {

	@FXML
	private TextField skimSourceURI;
	
	@FXML
	private TextField skimID;

	@FXML
	private Button skimSourceBrowse;

	@FXML
	private ComboBox<String> skimAssignerChooser;

	@FXML
	private ComboBox<String> skimFunctionChooser;
	
	private Project currentProject;
	
	private Window parent;
		

	@FXML
	void initialize() {
		currentProject = null;
		
		skimFunctionChooser.getItems().addAll("Travel time","Travel time (exclude HOV)");

	}

	@FXML
	void browseSkim(ActionEvent event) {
		FileChooser skimChooser = new FileChooser();
		skimChooser.setTitle("Open Skim");
		skimChooser.setInitialDirectory(currentProject.getDirectory().toFile());
		skimChooser.getExtensionFilters().add(new ExtensionFilter("Comma-Separated Values","*.csv"));
		
		File selectedFile = skimChooser.showOpenDialog(parent);
		if (selectedFile != null) {
			skimSourceURI.setText(currentProject.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			skimSourceURI.getOnAction().handle(new ActionEvent());
		}
	}

	@FXML
	void changeSkimAssigner(ActionEvent event) {
	
	}

	@FXML
	void changeSkimFunction(ActionEvent event) {

	}

	@FXML
	void changeSkimSourceURI(ActionEvent event) {

	}
	
	@FXML
	void changeSkimID(ActionEvent event) {
		
	}

	protected void setProject(Project project) {
		currentProject = project;
		skimAssignerChooser.getItems().addAll(currentProject.getAssignerIDs());

	}
	
	protected void setWindow(Window parent) {
		this.parent = parent;
	}

	protected String getSkimSourceURI() {
		return skimSourceURI.getText();
	}
	
	protected String getSkimAssigner() {
		return skimAssignerChooser.getSelectionModel().getSelectedItem();
	}
	
	protected String getSkimFunction() {
		switch (skimFunctionChooser.getSelectionModel().getSelectedItem()) {
		case "Travel time":
			return "travelTime";
		case "Travel time (exclude HOV)":
			return "travelTimeSingleOcc";
		default:
			return null;
		}
	}
	
	protected String getSkimID() {
		return skimID.getText();
	}
	
	protected BooleanBinding notReady() {
		return Bindings.or(Bindings.or(Bindings.or(
				skimAssignerChooser.getSelectionModel().selectedItemProperty().isNull(), 
				skimFunctionChooser.getSelectionModel().selectedItemProperty().isNull()),
				skimSourceURI.textProperty().isEmpty()),
				skimID.textProperty().isEmpty());
		
	}
}
