package edu.utexas.wrap;

import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.marketsegmentation.IndustryClass;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.io.GraphFactory;
import edu.utexas.wrap.util.io.ProductionAttractionFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModelInputNCTCOG implements ModelInput {
    private Properties inputs;
    private Graph graph;
    private Map<TripPurpose, Map<MarketSegment, Double>> productionRates;
    private Map<TripPurpose, Map<MarketSegment, Map<AreaClass, Double>>> areaClassProdRates;
    private Map<TripPurpose, Map<MarketSegment, Double>> attractionRates;
    private Map<TripPurpose, Map<MarketSegment, Map<AreaClass, Double>>> areaClassAttrRates;
    private Map<TimePeriod, float[][]> skimFactors;
    private Map<MarketSegment, FrictionFactorMap> frictionFactors;
    private Map<MarketSegment, Map<Mode, Double>> modalShares;
    private Map<Mode,Double> occupancyRates;
    private Map<TripPurpose, Map<MarketSegment, Map<TimePeriod, Double>>> departureRates;
    private Map<TripPurpose, Map<MarketSegment, Map<TimePeriod, Double>>> arrivalRates;


    public ModelInputNCTCOG(String inputFile) throws IOException {
        InputStream input = new FileInputStream(inputFile);
        inputs = new Properties();
        inputs.load(input);
    }

    private static void readHouseholdData(Graph graph, Path igFile, Path igWkrVehFile) throws IOException {
        // TODO Auto-generated method stub
        Files.lines(igFile).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line ->{
            String[] args = line.split(",");
            int tszID = Integer.parseInt(args[0]);

            Map<Integer, Double> hhByIG = IntStream.range(1, 4).parallel().boxed().collect(
                    Collectors.toMap(Function.identity(), ig -> Double.parseDouble(args[ig])));

            TravelSurveyZone tsz = graph.getNode(tszID).getZone();
            tsz.setHouseholdsByIncomeGroup(hhByIG);
        });

        Files.lines(igWkrVehFile).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line ->{
            String[] args = line.split(",");
            int tszID = Integer.parseInt(args[0]);


            if (args.length < 72) {
                args = new String[]{args[0],"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"};
            }
            String[] newArgs = args;

            TravelSurveyZone tsz = graph.getNode(tszID).getZone();

            IntStream.range(1, 65).parallel().boxed().forEach(idx ->{
                double val = Double.parseDouble(newArgs[idx]);
                int wkr, veh, ig;
                ig = (idx-1) % 4 + 1;
                veh = ((idx-1)/4) % 4;
                wkr = ((idx-1)/16) % 4;
                tsz.setHouseholdsByIncomeGroupThenWorkersThenVehicles(ig, wkr, veh, val);
            });
        });
    }

    private static void readEmploymentData(Graph graph, Path file) throws IOException {
        Files.lines(file).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line -> {
            String[] args = line.split(",");

            if (args.length < 11) {
                args = new String[]{args[0],args[1],"0","0","0","0","0","0","0","0","0"};
            }
            int tszID = Integer.parseInt(args[0]);
            AreaClass ac;
            switch (Integer.parseInt(args[1])) {
                case 1:
                    ac = AreaClass.CBD;
                    break;
                case 2:
                    ac = AreaClass.OBD;
                    break;
                case 3:
                    ac = AreaClass.URBAN_RESIDENTIAL;
                    break;
                case 4:
                    ac = AreaClass.SUBURBAN_RESIDENTIAL;
                    break;
                case 5:
                    ac = AreaClass.RURAL;
                    break;
                default:
                    throw new RuntimeException("Unknown area type");
            }

            Map<Integer,Map<IndustryClass,Double>> empByIGthenIC = new HashMap<Integer,Map<IndustryClass,Double>>();
            Map<IndustryClass,Double> ig1 = new HashMap<IndustryClass,Double>();
            ig1.put(IndustryClass.BASIC, Double.parseDouble(args[2]));
            ig1.put(IndustryClass.RETAIL, Double.parseDouble(args[3]));
            ig1.put(IndustryClass.SERVICE, Double.parseDouble(args[4]));
            empByIGthenIC.put(1, ig1);

            Map<IndustryClass,Double> ig2 = new HashMap<IndustryClass,Double>();
            ig2.put(IndustryClass.BASIC, Double.parseDouble(args[5]));
            ig2.put(IndustryClass.RETAIL, Double.parseDouble(args[6]));
            ig2.put(IndustryClass.SERVICE, Double.parseDouble(args[7]));
            empByIGthenIC.put(2, ig2);

            Map<IndustryClass,Double> ig3 = new HashMap<IndustryClass,Double>();
            ig3.put(IndustryClass.BASIC, Double.parseDouble(args[8]));
            ig3.put(IndustryClass.RETAIL, Double.parseDouble(args[9]));
            ig3.put(IndustryClass.SERVICE, Double.parseDouble(args[10]));
            empByIGthenIC.put(3, ig3);

            TravelSurveyZone tsz = graph.getNode(tszID).getZone();
            tsz.setAreaClass(ac);
            tsz.setEmploymentByIncomeGroupThenIndustry(empByIGthenIC);
        });
    }
    
    @Override
    public Graph getNetwork() {
        if (this.graph != null) {
            return this.graph;
        }
        System.out.print("Reading network... ");
        long ms = System.currentTimeMillis();
        //Model inputs
        String graphFile = inputs.getProperty("network.graphFile");
        String thruNode = inputs.getProperty("network.firstThruNode");
        String hhIG = inputs.getProperty("network.hhIG");
        String hhVeh = inputs.getProperty("network.hhVeh");
        String emp = inputs.getProperty("network.emp");


        Graph graph = null;
        try {
            graph = GraphFactory.readEnhancedGraph(new File(graphFile),Integer.parseInt(thruNode));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        long nms = System.currentTimeMillis();
        System.out.println(""+(nms-ms)/1000.0+" s");

        //TODO read RAAs
        // add demographic data to zones
        System.out.print("Reading household demographic data... ");
        ms = System.currentTimeMillis();
        try {
            readHouseholdData(graph, Paths.get(hhIG), Paths.get(hhVeh));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        nms = System.currentTimeMillis();
        System.out.println(""+(nms-ms)/1000.0+" s");


        System.out.print("Reading employment demographic data... ");
        ms = System.currentTimeMillis();
        try {
            readEmploymentData(graph, Paths.get(emp));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        nms = System.currentTimeMillis();
        System.out.println(""+(nms-ms)/1000.0+" s");
        this.graph = graph;

        return this.graph;
    }

    @Override
    public Map<MarketSegment, Double> getGeneralProdRates(TripPurpose purpose) {
        if(productionRates.containsKey(purpose))
            return productionRates.get(purpose);
        String purposeDetailsFile = inputs.getProperty("productions.general." + purpose);
        Map<MarketSegment, Double> rates = ProductionAttractionFactory.readGeneralRates(purposeDetailsFile);
        productionRates.put(purpose, rates);
        return rates;
    }

    @Override
    public Map<MarketSegment, Map<AreaClass, Double>> getAreaClassProdRates(TripPurpose purpose) {
        if(areaClassProdRates.containsKey(purpose))
            return areaClassProdRates.get(purpose);
        String purposeDetailsFile = inputs.getProperty("productions.area." + purpose.toString());
        Map<MarketSegment, Map<AreaClass, Double>> rates = ProductionAttractionFactory.readAreaRates(purposeDetailsFile);
        areaClassProdRates.put(purpose, rates);
        return rates;
    }

    @Override
    public Map<MarketSegment, Double> getGeneralAttrRates(TripPurpose purpose) {
        if(attractionRates.containsKey(purpose))
            return attractionRates.get(purpose);
        String purposeDetailsFile = inputs.getProperty("attractions.general." + purpose.toString());
        Map<MarketSegment, Double> rates = ProductionAttractionFactory.readGeneralRates(purposeDetailsFile);
        attractionRates.put(purpose, rates);
        return rates;
    }

    @Override
    public Map<MarketSegment, Map<AreaClass, Double>> getAreaClassAttrRates(TripPurpose purpose) {
        if(areaClassAttrRates.containsKey(purpose))
            return areaClassAttrRates.get(purpose);
        String purposeDetailsFile = inputs.getProperty("attractions.area." + purpose.toString());
        Map<MarketSegment, Map<AreaClass, Double>> rates = ProductionAttractionFactory.readAreaRates(purposeDetailsFile);
        areaClassAttrRates.put(purpose, rates);
        return rates;
    }


    @Override
    public float[][] getRoadwaySkim(TimePeriod timePeriod) {
        return new float[0][];
    }

    @Override
    public Map<MarketSegment, FrictionFactorMap> getFrictionFactors(TripPurpose purpose, TimePeriod timePeriod) {
        return null;
    }

    @Override
    public Map<MarketSegment, Map<Mode, Double>> getModeShares(TripPurpose purpose) {
        return null;
    }

    @Override
    public Map<Mode, Double> getOccupancyRates() {
        return null;
    }

    @Override
    public Map<TimePeriod, Double> getDepartureRates(TripPurpose purpose, MarketSegment segment) {
        return null;
    }

    @Override
    public Map<TimePeriod, Double> getArrivalRates(TripPurpose purpose, MarketSegment segment) {
        return null;
    }

	@Override
	public Map<MarketSegment, Double> getPeakOffpeakSplit(TripPurpose purpose) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<MarketSegment, Map<TravelSurveyZone, Double>> getWorkerVehicleSplits(MarketSegment segment) {
		// TODO Auto-generated method stub
		return null;
	}

}
