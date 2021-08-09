package edu.utexas.wrap.gui;

import java.io.File;

import edu.utexas.wrap.marketsegmentation.Market;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class NewFrictionFunctionController {

	@FXML
	private TextField frictionFunctionID;

	@FXML
	private TextField frictionFunctionSourceURI;

	@FXML
	private Button frictionFunctionSourceBrowse;

	private Market currentMarket;

	private Scene scene;

	@FXML
	void browseFrictionFunction(ActionEvent event) {
		FileChooser functionChooser = new FileChooser();
		functionChooser.setTitle("Open Discretized Function");
		functionChooser.setInitialDirectory(currentMarket.getDirectory().toFile());
		functionChooser.getExtensionFilters().add(new ExtensionFilter("Comma-Separated Values","*.csv"));

		File selectedFile = functionChooser.showOpenDialog(scene.getWindow());
		if (selectedFile != null) {
			frictionFunctionSourceURI.setText(currentMarket.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			frictionFunctionSourceURI.getOnAction().handle(event);
		}

	}

	@FXML
	void changeFrictionFunctionSourceURI(ActionEvent event) {
		//TODO
	}

	protected BooleanBinding notReady() {
		return Bindings.or(frictionFunctionID.textProperty().isEmpty(), 
				frictionFunctionSourceURI.textProperty().isEmpty());
	}

	protected void setMarket(Market market) {
		currentMarket = market;
	}
	
	protected void setScene(Scene scene) {
		this.scene = scene;
	}
	
	protected String getFunctionID() {
		return frictionFunctionID.getText();
	}
	
	protected String getFunctionSourceURI() {
		return frictionFunctionSourceURI.getText();
	}
}
