package edu.utexas.wrap.gui;

import edu.utexas.wrap.marketsegmentation.BasicPurpose;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

public class EditPurposeController {

	@FXML
	private TableView<?> votTable;

	@FXML
	private TableColumn<?, ?> timePeriodColumn;

	@FXML
	private TableColumn<?, ?> votColumn;

	@FXML
	private RadioButton prodToAttrButton;

	@FXML
	private RadioButton attrToProdButton;

	@FXML
	private RadioButton prodBasicDemo;

	@FXML
	private RadioButton prodProportionalDemo;

	@FXML
	private ComboBox<?> prodSourceDemo;

	@FXML
	private RadioButton prodGenericRate;

	@FXML
	private RadioButton prodTypeRate;

	@FXML
	private TableView<?> prodRateTable;

	@FXML
	private TableColumn<?, ?> prodDemoCol;

	@FXML
	private RadioButton attrBasicDemo;

	@FXML
	private RadioButton attrProportionalDemo;

	@FXML
	private ComboBox<?> attrSourceDemo;

	@FXML
	private RadioButton attrGenericRate;

	@FXML
	private RadioButton attrTypeRate;

	@FXML
	private TableView<?> attrRateTable;

	@FXML
	private TableColumn<?, ?> attrDemoCol;

	@FXML
	private ListView<?> distributorList;

	@FXML
	private Button addDistributor;

	@FXML
	private Button removeDistributor;

	@FXML
	private VBox distributionBox;

	@FXML
	private Spinner<?> scalingFactorChooser;

	@FXML
	private ComboBox<?> frictionFunction;

	@FXML
	private TextField zoneSourceURI;

	@FXML
	private Button zoneSourceBrowse;

	@FXML
	private TableView<?> timeDistTable;

	@FXML
	private TableColumn<?, ?> timePeriodDistCol;

	@FXML
	private TableColumn<?, ?> depRateCol;

	@FXML
	private TableColumn<?, ?> arrRateCol;

	@FXML
	private Button okButton;

	@FXML
	private Button cancelButton;

	@FXML
	private Button applyButton;

	@FXML
	private Label purposeID;

	@FXML
	private Label purposeSource;
	
	private BasicPurpose purpose;
	
	private HostServices svcs;
	
	private Image wrapIcon;
	
	private Scene scene;

	public void setPurpose(BasicPurpose selected) {
		purpose = selected;
	}

	public void setServices(HostServices svcs) {
		this.svcs = svcs;
	}

	public void setIcon(Image wrapIcon) {
		this.wrapIcon = wrapIcon;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void exit(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
