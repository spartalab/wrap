package edu.utexas.wrap.gui;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.marketsegmentation.BasicPurpose;
import edu.utexas.wrap.net.Demographic;

import javafx.application.HostServices;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.converter.FloatStringConverter;

public class EditPurposeController {

	@FXML
	private TableView<TimePeriod> votTable;

	@FXML
	private TableColumn<TimePeriod, String> timePeriodColumn;

	@FXML
	private TableColumn<TimePeriod, Float> votColumn;

	@FXML
	private RadioButton prodToAttrButton;

	@FXML
	private RadioButton attrToProdButton;

	@FXML
	private RadioButton prodBasicDemo;

	@FXML
	private RadioButton prodProportionalDemo;

	@FXML
	private ComboBox<String> prodSourceDemo;

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
	private ComboBox<String> attrSourceDemo;

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
	private TableView<TimePeriod> timeDistTable;

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

	private Boolean unsavedChanges;

	private ToggleGroup scaling;

	private ToggleGroup prodDemoType;

	private ToggleGroup attrDemoType;

	private ToggleGroup prodRateType;

	private ToggleGroup attrRateType;

	public void setPurpose(BasicPurpose selected) {
		purpose = selected;
		purposeID.setText(purpose.toString());
		purposeSource.setText(purpose.getDirectory().toString());
		
		ActionEvent event = new ActionEvent();
		
		switch(purpose.getBalancingMethod()) {
		case "prodProportional":
			prodToAttrButton.setSelected(true);
			break;
		case "attrProportional":
			attrToProdButton.setSelected(true);
			break;
		}
		
		switch (purpose.getProducerDemographicType()) {
		case "prodProportional":
			prodProportionalDemo.setSelected(true);
			setProportionalProdDemo(event);
			break;
		case "basic":
			prodBasicDemo.setSelected(true);
			setBasicProdDemo(event);

			break;
		}
		
		switch (purpose.getAttractorDemographicType()) {
		case "attrProportional":
			attrProportionalDemo.setSelected(true);
			setProportionalAttrDemo(event);

			break;
		case "basic":
			attrBasicDemo.setSelected(true);
			setBasicAttrDemo(event);

			break;
		}
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
		if (!promptToSaveChanges(arg0)) {
			arg0.consume();
			return;
		}
		closeWindow(arg0);

	}

	private boolean promptToSaveChanges(WindowEvent arg0) {
		if (unsavedChanges) {
			Alert alert = new Alert(AlertType.CONFIRMATION,"Save purpose changes?",ButtonType.YES,ButtonType.NO,ButtonType.CANCEL);
			alert.setTitle("Current purpose has unsaved changes");
			((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(wrapIcon);
			Optional<ButtonType> choice = alert.showAndWait();
			if (choice.isPresent()) {
				ButtonType type = choice.get();
				if (type==ButtonType.YES) {
					if (!savePurpose(arg0)) return false;
				}
				else if (type == ButtonType.CANCEL) return false;
			}
		}
		return true;
	}


	@FXML
	private void initialize() {
		unsavedChanges = false;

		votTable.getItems().setAll(TimePeriod.values());
		timePeriodColumn.setCellValueFactory(new Callback<CellDataFeatures<TimePeriod,String>,ObservableValue<String>>(){

			@Override
			public ObservableValue<String> call(CellDataFeatures<TimePeriod, String> arg0) {
				return new ReadOnlyObjectWrapper<String>(arg0.getValue().toString());
			}

		});



		votColumn.setCellFactory(TextFieldTableCell.<TimePeriod,Float>forTableColumn(new FloatStringConverter()));
		votColumn.setCellValueFactory(new Callback<CellDataFeatures<TimePeriod,Float>,ObservableValue<Float>>(){

			@Override
			public ObservableValue<Float> call(CellDataFeatures<TimePeriod, Float> arg0) {
				// TODO Auto-generated method stub
				return new SimpleFloatProperty(purpose.getVOT(arg0.getValue())).asObject();
			}

		});
		votColumn.setOnEditCommit(new EventHandler<CellEditEvent<TimePeriod,Float>>(){

			@Override
			public void handle(CellEditEvent<TimePeriod, Float> arg0) {
				// TODO Auto-generated method stub
				TimePeriod row = arg0.getRowValue();
				Float newValue = arg0.getNewValue();
				purpose.setVOT(row,newValue);
				System.out.println("Edited "+row+"\t"+newValue);

			}

		});

		scaling = new ToggleGroup();
		prodToAttrButton.setToggleGroup(scaling);
		attrToProdButton.setToggleGroup(scaling);


		prodDemoType = new ToggleGroup();
		prodBasicDemo.setToggleGroup(prodDemoType);
		prodProportionalDemo.setToggleGroup(prodDemoType);

		attrDemoType = new ToggleGroup();
		attrBasicDemo.setToggleGroup(attrDemoType);
		attrProportionalDemo.setToggleGroup(attrDemoType);

		prodRateType = new ToggleGroup();
		prodGenericRate.setToggleGroup(prodRateType);
		prodTypeRate.setToggleGroup(prodRateType);

		attrRateType = new ToggleGroup();
		attrGenericRate.setToggleGroup(attrRateType);
		attrTypeRate.setToggleGroup(attrRateType);
		

		


	}

	@FXML
	private void closeWindow(Event e) {
		if (scene != null) scene.getWindow().hide();
		if (purpose != null)
			try {
				purpose.reloadProperties();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}

	private boolean savePurpose(Event e) {
		if (unsavedChanges) {
			//TODO
			return true;
		}
		return  true;
	}

	private void markChanged() {
		unsavedChanges = true;
		okButton.setDisable(false);
		applyButton.setDisable(false);
		if (!purposeID.getText().endsWith("*")) {
			purposeID.setText(purposeID.getText()+"*");
		}
	}



	@FXML
	void scaleAttrsToProds(ActionEvent event) {
		purpose.setBalancingMethod("attrProportional");
	}

	@FXML
	void scaleProdsToAttrs(ActionEvent event) {
		purpose.setBalancingMethod("prodProportional");
	}

	@FXML
	void setBasicAttrDemo(ActionEvent event) {
		//TODO mark changed
		purpose.setAttractorDemographicType("basic");
		attrSourceDemo.setDisable(false);
		attrSourceDemo.getItems().clear();
		attrSourceDemo.getItems().setAll(purpose.getMarket().getDemographics().stream().map(Demographic::toString).collect(Collectors.toSet()));
		attrSourceDemo.getSelectionModel().select(purpose.getAttractionDemographicSource());

	}

	@FXML
	void setBasicProdDemo(ActionEvent event) {
		//TODO mark changed
		purpose.setProducerDemographicType("basic");
		prodSourceDemo.setDisable(false);
		prodSourceDemo.getItems().clear();
		prodSourceDemo.getItems().setAll(purpose.getMarket().getDemographics().stream().map(Demographic::toString).collect(Collectors.toSet()));
		prodSourceDemo.getSelectionModel().select(purpose.getProductionDemographicSource());

	}

	@FXML
	void setGenericAttrRate(ActionEvent event) {

	}

	@FXML
	void setGenericProdRate(ActionEvent event) {
		
	}

	@FXML
	void setProportionalAttrDemo(ActionEvent event) {
		//TODO mark changed
		purpose.setAttractorDemographicType("attrProportional");
		attrSourceDemo.setDisable(false);
		attrSourceDemo.getItems().clear();
		attrSourceDemo.getItems().setAll(purpose.getMarket().getBasicPurposes().stream().map(BasicPurpose::toString).collect(Collectors.toSet()));
		attrSourceDemo.getSelectionModel().select(purpose.getAttractionDemographicSource());

		//	TODO prevent cyclic references
	}

	@FXML
	void setProportionalProdDemo(ActionEvent event) {
		//TODO mark changed
		purpose.setProducerDemographicType("prodProportional");
		prodSourceDemo.setDisable(false);
		prodSourceDemo.getItems().clear();
		prodSourceDemo.getItems().setAll(purpose.getMarket().getBasicPurposes().stream().map(BasicPurpose::toString).collect(Collectors.toSet()));
		prodSourceDemo.getSelectionModel().select(purpose.getProductionDemographicSource());

		// TODO prevent cyclic references

	}

	@FXML
	void setSpecificAttrRate(ActionEvent event) {

	}

	@FXML
	void setSpecificProdRate(ActionEvent event) {

	}
}
