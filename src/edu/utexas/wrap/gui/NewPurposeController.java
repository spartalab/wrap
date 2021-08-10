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

public class NewPurposeController {

	@FXML
	private TextField purposeID;

	@FXML
	protected TextField purposeSourceURI;

	@FXML
	private Button purposeBrowse;
	
	protected Market currentMarket;
	
	protected Scene scene;

	@FXML
	protected void browsePurpose(ActionEvent event) {
		FileChooser purposeChooser = new FileChooser();
		purposeChooser.setTitle("New Purpose");
		purposeChooser.setInitialDirectory(currentMarket.getDirectory().toFile());
		purposeChooser.getExtensionFilters().add(new ExtensionFilter("wrap Purpose Files","*.wrppr"));
		
		File selectedFile = purposeChooser.showSaveDialog(scene.getWindow());
		
		if (selectedFile != null) {
			purposeSourceURI.setText(currentMarket.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
		}

	}

	public void setMarket(Market market) {
		currentMarket = market;
	}
	
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public BooleanBinding notReady() {
		return Bindings.or(purposeID.textProperty().isEmpty(), purposeSourceURI.textProperty().isEmpty());
	}

	public String getPurposeID() {
		return purposeID.getText();
	}

	public String getPurposeSourceURI() {
		return purposeSourceURI.getText();
	}

}
