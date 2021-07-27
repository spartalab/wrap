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

public class NewAssignerController {

	@FXML
	protected TextField assignerSourceURI;
	
	@FXML
	protected TextField assignerID;
	
	@FXML
	protected ComboBox<String> assignerClassChooser;
	
	@FXML
	protected Button assignerSourceBrowse;
	
	protected Project currentProject;
	
	protected Window parent;
	
	@FXML
	private void initialize() {
		assignerClassChooser.getItems().addAll("Built-in Assigner","External Assigner","File Output");
	}
	
	@FXML
	private void browseAssigner(ActionEvent event) {
		FileChooser assignerChooser = new FileChooser();
		assignerChooser.setTitle("New Assigner");
		assignerChooser.setInitialDirectory(currentProject.getDirectory().toFile());
		assignerChooser.getExtensionFilters().add(new ExtensionFilter("wrap Assigner file","*.wrapr"));
		
		File selectedFile = assignerChooser.showSaveDialog(parent);
		if (selectedFile != null) {
			assignerSourceURI.setText(currentProject.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			assignerSourceURI.getOnAction().handle(event);
		}
		//TODO create assigner file
	}
	
	@FXML
	private void changeAssignerSourceURI(ActionEvent event) {
		
	}
	
	@FXML
	private void changeAssignerClass(ActionEvent event) {
		
	}
	
	protected void setProject(Project project) {
		currentProject = project;
	}
	
	protected void setWindow(Window parent) {
		this.parent = parent;
	}
	
	protected String getAssignerSourceURI() {
		return assignerSourceURI.getText();
	}
	
	protected String getAssignerID() {
		return assignerID.getText();
	}
	
	protected String getAssignerClass() {
		return assignerClassChooser.getSelectionModel().getSelectedItem();
	}
	
	protected BooleanBinding notReady() {
		return Bindings.or(Bindings.or(assignerClassChooser.getSelectionModel().selectedItemProperty().isNull(), assignerID.textProperty().isEmpty()), assignerSourceURI.textProperty().isEmpty());
	}
}
