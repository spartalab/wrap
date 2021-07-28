package edu.utexas.wrap.gui;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class AboutDialog {

	@FXML
	private Hyperlink gpl;
	
	@FXML
	private Hyperlink sparta;
	
	private HostServices svcs;
	
	@FXML
	private void openGPL() {
		svcs.showDocument(gpl.getText());
	}
	
	@FXML
	private void openSparta() {
		svcs.showDocument(sparta.getText());
	}

	public void setHostServices(HostServices hostServices) {
		// TODO Auto-generated method stub
		this.svcs = hostServices;
	}
}
