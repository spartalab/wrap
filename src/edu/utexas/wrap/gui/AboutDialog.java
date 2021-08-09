package edu.utexas.wrap.gui;

import java.lang.module.ModuleDescriptor.Version;
import java.util.Optional;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

public class AboutDialog {

	@FXML
	private Hyperlink gpl;
	
	@FXML
	private Hyperlink sparta;
	
	private HostServices svcs;
	
	@FXML
	private Label versionLabel;
	
	@FXML
	private void initialize() {
		Optional<Version> version = this.getClass().getModule().getDescriptor().version();
		versionLabel.setText(version.isPresent()? version.get().toString() : "Unknown version");
	}
	
	@FXML
	private void openGPL() {
		svcs.showDocument(gpl.getText());
	}
	
	@FXML
	private void openSparta() {
		svcs.showDocument(sparta.getText());
	}

	public void setHostServices(HostServices hostServices) {
		this.svcs = hostServices;
	}
}
