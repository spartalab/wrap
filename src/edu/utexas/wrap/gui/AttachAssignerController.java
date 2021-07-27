package edu.utexas.wrap.gui;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class AttachAssignerController extends NewAssignerController {

	@FXML
	private void browseAssigner(ActionEvent event) {
		FileChooser assignerChooser = new FileChooser();
		assignerChooser.setTitle("Attach Assigner");
		assignerChooser.setInitialDirectory(currentProject.getDirectory().toFile());
		assignerChooser.getExtensionFilters().add(new ExtensionFilter("wrap Assigner file","*.wrapr"));
		
		File selectedFile = assignerChooser.showOpenDialog(parent);
		if (selectedFile != null) {
			assignerSourceURI.setText(currentProject.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
			assignerSourceURI.getOnAction().handle(event);
		}
	}
}
