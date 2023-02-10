package edu.utexas.wrap.gui;

import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.assignment.AssignmentBuilder;
import edu.utexas.wrap.assignment.AssignmentEvaluator;
import edu.utexas.wrap.assignment.AssignmentInitializer;
import edu.utexas.wrap.assignment.AssignmentOptimizer;
import edu.utexas.wrap.assignment.AssignmentProvider;
import edu.utexas.wrap.assignment.StaticAssigner;
import edu.utexas.wrap.assignment.bush.BushEvaluator;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.TolledBPRLink;
import edu.utexas.wrap.net.TolledEnhancedLink;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

public class EditAssignerController {

	@FXML
	private ComboBox<TimePeriod> timePeriodChooser;

	@FXML
	private Spinner<Integer> outerIterationChooser;

	@FXML
	private CheckBox sovCheckBox;

	@FXML
	private CheckBox hov2CheckBox;

	@FXML
	private CheckBox hov3CheckBox;

	@FXML
	private CheckBox medTruckCheckBox;

	@FXML
	private CheckBox lgTruckCheckBox;

	@FXML
	private CheckBox wkTransCheckBox;

	@FXML
	private CheckBox drvTransCheckBox;

	@FXML
	private RadioButton bprButton;

	@FXML
	private RadioButton conicButton;

	@FXML
	private VBox demographicBox;

	@FXML
	private TextField linksSourceURI;

	@FXML
	private Button linksBrowse;

	@FXML
	private Button linksEdit;

	@FXML
	private CheckBox distBasedCheckBox;

	@FXML
	private Spinner<Double> distMultiplierChooser;

	@FXML
	private CheckBox overlayCheckBox;

	@FXML
	private Spinner<Double> overlayTollChooser;

	@FXML
	private TextField overlayLinksSourceURI;
	
	@FXML
	private Button overlayBrowse;

	@FXML
	private Button overlayEdit;

	@FXML
	private CheckBox routeMaxCheckBox;

	@FXML
	private RadioButton linkButton;

	@FXML
	private RadioButton pathButton;

	@FXML
	private RadioButton bushButton;

	@FXML
	private ComboBox<AssignmentProvider<?>> containerProviderChooser;

	@FXML
	private ComboBox<AssignmentInitializer<?>> containerLoaderChooser;

	@FXML
	private ComboBox<AssignmentBuilder<?>> containerBuilderChooser;

	@FXML
	private VBox demographicBox1;

	@FXML
	private TextField containerSourceURI;

	@FXML
	private Button containerBrowse;

	@FXML
	private ComboBox<AssignmentEvaluator<?>> evaluatorChooser;

	@FXML
	private Spinner<Double> evaluationThreshold;

	@FXML
	private ComboBox<AssignmentOptimizer<?>> optimizerChooser;

	@FXML
	private ComboBox<BushEvaluator> iterationEvalChooser;

	@FXML
	private Spinner<Double> iterationThreshold;
	
	private StaticAssigner<?> assigner;
	
	private ToggleGroup containerType;
	
	private ToggleGroup linkType;

	
	@FXML
	private void initialize() {
		linkType = new ToggleGroup();
		linkType.getToggles().addAll(conicButton, bprButton);
		
		containerType = new ToggleGroup();
		containerType.getToggles().addAll(linkButton,pathButton,bushButton);
		
		timePeriodChooser.getItems().setAll(TimePeriod.values());
		outerIterationChooser.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,Integer.MAX_VALUE));
		distMultiplierChooser.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0., Float.MAX_VALUE,0.,0.01));
		distMultiplierChooser.valueProperty().addListener(new ChangeListener<Double>() {

			@Override
			public void changed(ObservableValue<? extends Double> arg0, Double arg1, Double arg2) {
				// TODO Auto-generated method stub
				Double multiplier = distMultiplierChooser.getValue();
				assigner.setTollingPolicy(link -> link.getLength() * multiplier);
			}
			
		});
	}
	
	@FXML
	void browseContainer(ActionEvent event) {

	}

	@FXML
	void browseLinks(ActionEvent event) {

	}

	@FXML
	void browseOverlayLinks(ActionEvent event) {

	}

	@FXML
	void change2ovMode(ActionEvent event) {

	}

	@FXML
	void change3ovMode(ActionEvent event) {

	}

	@FXML
	void changeContainerBuilder(ActionEvent event) {

	}

	@FXML
	void changeContainerEvaluator(ActionEvent event) {

	}

	@FXML
	void changeContainerLoader(ActionEvent event) {

	}

	@FXML
	void changeContainerOptimizer(ActionEvent event) {

	}

	@FXML
	void changeContainerProvider(ActionEvent event) {

	}

	@FXML
	void changeContainerSourceURI(ActionEvent event) {

	}

	@FXML
	void changeContainerType(ActionEvent event) {

	}

	@FXML
	void changeDistBasedPolicy(ActionEvent event) {
		if (distBasedCheckBox.isSelected()) {
			distMultiplierChooser.setDisable(false);
			Double multiplier = distMultiplierChooser.getValue();
			assigner.setTollingPolicy(link -> link.getLength() * multiplier);
		} else {
			distMultiplierChooser.setDisable(true);
			assigner.setTollingPolicy(link -> 0.0);
		}
	}

	@FXML
	void changeDrvTransMode(ActionEvent event) {

	}

	@FXML
	void changeIterationEvaluator(ActionEvent event) {

	}

	@FXML
	void changeLPF(ActionEvent event) {

	}

	@FXML
	void changeLgTruckMode(ActionEvent event) {

	}

	@FXML
	void changeLinksSourceURI(ActionEvent event) {

	}

	@FXML
	void changeMedTruckMode(ActionEvent event) {

	}

	@FXML
	void changeOverlayLinksSourceURI(ActionEvent event) {

	}

	@FXML
	void changeOverlayPolicy(ActionEvent event) {

	}

	@FXML
	void changeRouteMaxPolicy(ActionEvent event) {

	}

	@FXML
	void changeSovMode(ActionEvent event) {

	}

	@FXML
	void changeWkTransMode(ActionEvent event) {

	}

	@FXML
	void chooseTimePeriod(ActionEvent event) {

	}

	@FXML
	void editLinks(ActionEvent event) {

	}

	@FXML
	void editOverlay(ActionEvent event) {

	}

	public void setAssigner(StaticAssigner<?> selected) {
		// TODO Auto-generated method stub
		assigner = selected;
		
		Class<? extends Link> linkType = assigner.getLinkType();
		if (TolledBPRLink.class.isAssignableFrom(linkType)) {
			bprButton.setSelected(true);
		} else if (TolledEnhancedLink.class.isAssignableFrom(linkType)) {
			conicButton.setSelected(true);
		}
		
		Collection<Mode> modes = assigner.assignedModes();
		sovCheckBox.setSelected(modes.contains(Mode.SINGLE_OCC));
		hov2CheckBox.setSelected(modes.contains(Mode.HOV_2_PSGR) || modes.contains(Mode.HOV));
		hov3CheckBox.setSelected(modes.contains(Mode.HOV_3_PSGR));
		medTruckCheckBox.setSelected(modes.contains(Mode.MED_TRUCK));
		lgTruckCheckBox.setSelected(modes.contains(Mode.HVY_TRUCK));
		wkTransCheckBox.setSelected(modes.contains(Mode.WALK_TRANSIT));
		drvTransCheckBox.setSelected(modes.contains(Mode.DRIVE_TRANSIT));
		
		timePeriodChooser.getSelectionModel().select(assigner.getTimePeriod());
		
		outerIterationChooser.getValueFactory().setValue(assigner.maxIterations());
		
	}

	@FXML
	void exit(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
