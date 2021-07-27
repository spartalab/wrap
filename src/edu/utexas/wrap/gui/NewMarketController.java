package edu.utexas.wrap.gui;

import java.io.File;

import edu.utexas.wrap.Project;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

public class NewMarketController {
	
	@FXML
	protected TextField marketSourceURI;
	
	@FXML
	protected TextField marketID;
	
	@FXML
	protected Button marketSourceBrowse;
	
	protected Project currentProject;
	
	protected Window parent;
	
	@FXML
	private void browseMarket(ActionEvent event) {
		FileChooser marketChooser = new FileChooser();
		marketChooser.setTitle("New Market");
		marketChooser.setInitialDirectory(currentProject.getDirectory().toFile());
		marketChooser.getExtensionFilters().add(new ExtensionFilter("wrap Market file","*.wrm"));
		
		File selectedFile = marketChooser.showSaveDialog(parent);
		if (selectedFile != null) {
			marketSourceURI.setText(currentProject.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			marketSourceURI.getOnAction().handle(event);
			//TODO open dialog to customize new market
		}
	}
	
	@FXML
	private void changeMarketSourceURI(ActionEvent event) {
		
	}
	
	protected void setProject(Project project) {
		currentProject = project;
	}
	
	protected void setWindow(Window parent) {
		this.parent = parent;
	}
	
	protected String getMarketSourceURI() {
		return marketSourceURI.getText();
	}
	
	protected String getMarketID() {
		return marketID.getText();
	}
	
	protected BooleanBinding notReady() {
		return Bindings.or(marketSourceURI.textProperty().isEmpty(), marketID.textProperty().isEmpty());
	}

}
