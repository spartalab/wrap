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

public class NewDemographicController {

	@FXML
	private TextField demographicID;

	@FXML
	private TextField demographicSourceURI;

	@FXML
	private Button marketSourceBrowse;
	
	private Scene scene;
	
	private Market market;

	@FXML
	void browseMarket(ActionEvent event) {
		//TODO
		FileChooser demographicChooser = new FileChooser();
		demographicChooser.setTitle("Open Demographic File");
		demographicChooser.setInitialDirectory(market.getDirectory().toFile());
		demographicChooser.getExtensionFilters().add(new ExtensionFilter("Comma-Separated Values","*.csv"));
		
		File selectedFile = demographicChooser.showOpenDialog(scene.getWindow());
		if (selectedFile != null) {
			demographicSourceURI.setText(market.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
		}
	}

	public BooleanBinding notReady() {
		return Bindings.or(demographicID.textProperty().isEmpty(), 
				demographicSourceURI.textProperty().isEmpty());
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void setMarket(Market market) {
		this.market = market;
	}

	public String getDemographicID() {
		return demographicID.getText();
	}

	public String getDemographicSourceURI() {
		return demographicSourceURI.getText();
	}

}
