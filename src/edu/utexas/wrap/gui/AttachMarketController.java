package edu.utexas.wrap.gui;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class AttachMarketController extends NewMarketController {

	@FXML
	private void browseMarket(ActionEvent event) {
		FileChooser marketChooser = new FileChooser();
		marketChooser.setTitle("Attach Market");
		marketChooser.setInitialDirectory(currentProject.getDirectory().toFile());
		marketChooser.getExtensionFilters().add(new ExtensionFilter("wrap Market file","*.wrm"));
		
		File selectedFile = marketChooser.showOpenDialog(parent);
		if (selectedFile != null) {
			marketSourceURI.setText(currentProject.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			marketSourceURI.getOnAction().handle(event);
		}
	}
}
