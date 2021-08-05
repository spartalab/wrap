package edu.utexas.wrap.gui;

import java.util.Comparator;

import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.marketsegmentation.BasicPurpose;
import edu.utexas.wrap.marketsegmentation.DummyPurpose;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.net.Demographic;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class EditMarketController {
	
	@FXML
	private ListView<Demographic> demographicList;
	
	@FXML
	private Button demographicAdd;
	
	@FXML
	private Button demographicRemove;
	
	@FXML
	private VBox demographicBox;
	
	@FXML
	private TextField demographicSourceURI;
	
	@FXML
	private Button demographicBrowse;
	
	@FXML
	private Button demographicEdit;
	
	@FXML
	private ListView<FrictionFactorMap> frictionFunctionList;
	
	@FXML
	private Button frictionFunctionAdd;
	
	@FXML
	private Button frictionFunctionRemove;
	
	@FXML
	private VBox frictionFunctionBox;
	
	@FXML
	private RadioButton frictionFunctionDiscretized;
	
	@FXML
	private RadioButton frictionFunctionGamma;
	
	@FXML
	private TextField frictionFunctionSourceURI;
	
	@FXML
	private Button frictionFunctionBrowse;
	
	@FXML
	private Button frictionFunctionEdit;
	
	@FXML
	private ListView<BasicPurpose> purposeList;
	
	@FXML
	private Button purposeAdd;
	
	@FXML
	private Button purposeRemove;

	@FXML
	private VBox purposeBox;
	
	@FXML
	private TextField purposeSourceURI;
	
	@FXML
	private Button purposeBrowse;
	
	@FXML
	private Button purposeEdit;
	
	@FXML
	private ListView<DummyPurpose> surrogateList;
	
	@FXML
	private Button surrogateAdd;
	
	@FXML
	private Button surrogateRemove;
	
	@FXML
	private VBox surrogateBox;
	
	@FXML
	private TextField surrogateSourceURI;
	
	@FXML
	private Button surrogateBrowse;
	
	@FXML
	private Button surrogateEdit;
	
	@FXML
	private Label marketName;
	
	@FXML
	private Label marketDirectory;
	
	@FXML
	private Button marketOK;
	
	@FXML
	private Button marketBrowse;
	
	private Market market;

	public void setMarket(Market selected) {
		// TODO Auto-generated method stub
		market = selected;
		demographicList.getItems().setAll(market.getDemographics());
		frictionFunctionList.getItems().setAll(market.getFrictionFunctions());
		purposeList.getItems().setAll(market.getBasicPurposes());
		
		demographicList.getItems().sort(new Comparator<Demographic>() {

			@Override
			public int compare(Demographic o1, Demographic o2) {
				return o1.toString().compareTo(o2.toString());
			}
			
		});
		frictionFunctionList.getItems().sort(new Comparator<FrictionFactorMap>() {

			@Override
			public int compare(FrictionFactorMap o1, FrictionFactorMap o2) {
				return o1.toString().compareTo(o2.toString());
			}
			
		});
		purposeList.getItems().sort(new Comparator<BasicPurpose>() {

			@Override
			public int compare(BasicPurpose o1, BasicPurpose o2) {
				// TODO Auto-generated method stub
				return o1.toString().compareTo(o2.toString());
			}
			
		});
	}

}
