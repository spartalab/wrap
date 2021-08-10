package edu.utexas.wrap.gui;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class AttachPurposeController extends NewPurposeController {

	@Override
	protected void browsePurpose(ActionEvent event) {
		FileChooser purposeChooser = new FileChooser();
		purposeChooser.setTitle("Attach Purpose");
		purposeChooser.setInitialDirectory(currentMarket.getDirectory().toFile());
		purposeChooser.getExtensionFilters().add(new ExtensionFilter("wrap Purpose Files","*.wrppr"));
		
		File selectedFile = purposeChooser.showOpenDialog(scene.getWindow());
		
		if (selectedFile != null) {
			purposeSourceURI.setText(currentMarket.getDirectory().toUri().relativize(selectedFile.toURI()).getPath());
		}
	}
}
