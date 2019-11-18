package edu.utexas.wrap;

import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.marketsegmentation.ChildSegment;
import edu.utexas.wrap.marketsegmentation.CollegeSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupChildSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupIndustrySegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegmenter;
import edu.utexas.wrap.marketsegmentation.IncomeGroupWorkerHouseholdSizeSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupWorkerVehicleSegment;
import edu.utexas.wrap.marketsegmentation.IndustryClass;
import edu.utexas.wrap.marketsegmentation.IndustrySegment;
import edu.utexas.wrap.marketsegmentation.IndustrySegmenter;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.marketsegmentation.StudentSegment;
import edu.utexas.wrap.marketsegmentation.VehicleSegmenter;
import edu.utexas.wrap.marketsegmentation.WorkerHouseholdSizeSegment;
import edu.utexas.wrap.marketsegmentation.WorkerSegment;
import edu.utexas.wrap.marketsegmentation.WorkerSegmenter;
import edu.utexas.wrap.marketsegmentation.WorkerVehicleSegment;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.io.FrictionFactorFactory;
import edu.utexas.wrap.util.io.GraphFactory;
import edu.utexas.wrap.util.io.ProductionAttractionFactory;
import edu.utexas.wrap.util.io.SkimFactory;

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
    private Map<TripPurpose, Map<TimePeriod, Map<MarketSegment, FrictionFactorMap>>> frictionFactors;
    private Map<TripPurpose, Map<MarketSegment, Map<Mode, Double>>> modalShares;
    private Map<Mode,Double> occupancyRates;
    private Map<TripPurpose, Map<MarketSegment, Map<TimePeriod, Double>>> departureRates;
    private Map<TripPurpose, Map<MarketSegment, Map<TimePeriod, Double>>> arrivalRates;
	private Map<String, Class<? extends MarketSegment>> headerToSegment;



	

    public ModelInputNCTCOG(String inputFile) throws IOException {
        InputStream input = new FileInputStream(inputFile);
        inputs = new Properties();
        inputs.load(input);
        
		headerToSegment = new HashMap<String,Class<? extends MarketSegment>>();
		headerToSegment.put("WRKCNT,", WorkerSegment.class);
		headerToSegment.put("COLTYPE,", CollegeSegment.class);
		headerToSegment.put("NUMOFCHILD,", ChildSegment.class);
		headerToSegment.put("WRKCNT,HHSIZE,", WorkerHouseholdSizeSegment.class);
		headerToSegment.put("WRKCNT,VEHCNT,", WorkerVehicleSegment.class);
		headerToSegment.put("EDUCATION,", StudentSegment.class);
		headerToSegment.put("INDUSTRY,", IndustrySegment.class);
		headerToSegment.put("INCOME,INDUSTRY,", IncomeGroupIndustrySegment.class);
		headerToSegment.put("INCOME,WRKCNT,VEHCNT,", IncomeGroupWorkerVehicleSegment.class);
		headerToSegment.put("INCOME,NUMOFCHILD,", IncomeGroupChildSegment.class);
		headerToSegment.put("INCOME,WRKCNT,HHSIZE,", IncomeGroupWorkerHouseholdSizeSegment.class);
    }

    private static void readHouseholdData(Graph graph, Path igFile, Path igWkrVehFile, Path sizeWkrIGFile, Path wkrIGChildFile) throws IOException {
        // TODO Auto-generated method stub
        Files.lines(igFile).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line ->{
            String[] args = line.split(",");
            int tszID = Integer.parseInt(args[0]);

            Map<Integer, Double> hhByIG = IntStream.range(1, 5).parallel().boxed().collect(
                    Collectors.toMap(Function.identity(), ig -> Double.parseDouble(args[ig])));

            TravelSurveyZone tsz = graph.getNode(tszID).getZone();
            tsz.setHouseholdsByIncomeGroup(hhByIG);
        });

        Files.lines(igWkrVehFile).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line ->{
            String[] args = line.split(",");
            int tszID = Integer.parseInt(args[0]);


            if (args.length < 65) {
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
        
        Files.lines(sizeWkrIGFile).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line ->{
        	String[] args = line.split(",");
        	
        	TravelSurveyZone tsz = graph.getNode(Integer.parseInt(args[0])).getZone();
        	
        	IntStream.range(1, 65).parallel().boxed().forEach(idx ->{
        		double val = Double.parseDouble(args[idx]);
        		int size, wkr, ig;
        		ig = (idx-1)%4+1;
        		size = ((idx-1)/4)%4+1;
        		wkr = ((idx-1)/16)%4+1;
        		
        		tsz.setHouseholdsBySizeThenWorkersThenIncomeGroup(size, wkr, ig, val);
        	});
        });
        
        Files.lines(wkrIGChildFile).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line ->{
        	String[] args = line.split(",");
        	
        	TravelSurveyZone tsz = graph.getNode(Integer.parseInt(args[0])).getZone();
        	
        	IntStream.range(1, 49).parallel().boxed().forEach(idx ->{
        		double val = Double.parseDouble(args[idx]);
        		int wkr, ig, child;
        		child = (idx-1)%3;
        		ig = ((idx-1)/3)%4+1;
        		wkr = ((idx-1)/12)%4;
        		
        		tsz.setHouseholdsByWorkersThenIncomeGroupThenChildren(wkr, ig, child, val);
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
            Map<IndustryClass,Double> ig1 = new HashMap<IndustryClass,Double>(4,1.0f);
            ig1.put(IndustryClass.BASIC, Double.parseDouble(args[2]));
            ig1.put(IndustryClass.RETAIL, Double.parseDouble(args[3]));
            ig1.put(IndustryClass.SERVICE, Double.parseDouble(args[4]));
            empByIGthenIC.put(1, ig1);

            Map<IndustryClass,Double> ig2 = new HashMap<IndustryClass,Double>(4,1.0f);
            ig2.put(IndustryClass.BASIC, Double.parseDouble(args[5]));
            ig2.put(IndustryClass.RETAIL, Double.parseDouble(args[6]));
            ig2.put(IndustryClass.SERVICE, Double.parseDouble(args[7]));
            empByIGthenIC.put(2, ig2);

            Map<IndustryClass,Double> ig3 = new HashMap<IndustryClass,Double>(4,1.0f);
            ig3.put(IndustryClass.BASIC, Double.parseDouble(args[8]));
            ig3.put(IndustryClass.RETAIL, Double.parseDouble(args[9]));
            ig3.put(IndustryClass.SERVICE, Double.parseDouble(args[10]));
            empByIGthenIC.put(3, ig3);

            Map<IndustryClass,Double> ig4 = new HashMap<IndustryClass,Double>(4,1.0f);
            ig4.put(IndustryClass.BASIC, Double.parseDouble(args[11]));
            ig4.put(IndustryClass.RETAIL, Double.parseDouble(args[12]));
            ig4.put(IndustryClass.SERVICE, Double.parseDouble(args[13]));
            empByIGthenIC.put(4, ig4);

            TravelSurveyZone tsz = graph.getNode(tszID).getZone();
            tsz.setAreaClass(ac);
            tsz.setEmploymentByIncomeGroupThenIndustry(empByIGthenIC);
        });
    }
    
    private static void readChildData(Graph graph, Path file) throws IOException{
    	Files.lines(file).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line ->{
    		String[] args = line.split(",");
    		Integer tszID = Integer.parseInt(args[0]);
    		
    		Map<Integer, Double> hhByChild = new HashMap<Integer, Double>();
    		hhByChild.put(0, Double.parseDouble(args[1]));
    		hhByChild.put(1, Double.parseDouble(args[2]));
    		hhByChild.put(2, Double.parseDouble(args[3]));
    		
    		graph.getNode(tszID).getZone().setHouseholdsByChildren(hhByChild);
    	});
    }
    
    @Override
    public synchronized Graph getNetwork() {
        if (this.graph != null) {
            return this.graph;
        }
        //Model inputs
        String graphFile = inputs.getProperty("network.graphFile");
        String thruNode = inputs.getProperty("network.firstThruNode");
        String hhIG = inputs.getProperty("network.hhIG");
        String hhVeh = inputs.getProperty("network.hhIGWkrVeh");
        String emp = inputs.getProperty("network.emp");
        String child = inputs.getProperty("network.child");
        String hhSize = inputs.getProperty("network.hhSzWkrIG");
        String hhWkrIGChild = inputs.getProperty("network.hhWkrIGChild");

        Graph graph = null;
        try {
            graph = GraphFactory.readEnhancedGraph(new File(graphFile),Integer.parseInt(thruNode));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //TODO read RAAs
        // add demographic data to zones
        try {
            readHouseholdData(graph, Paths.get(hhIG), Paths.get(hhVeh), Paths.get(hhSize), Paths.get(hhWkrIGChild));
            readEmploymentData(graph, Paths.get(emp));
            readChildData(graph,Paths.get(child));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        this.graph = graph;

        return this.graph;
    }

    @Override
    public synchronized Map<MarketSegment, Double> getGeneralProdRates(TripPurpose purpose) {
        if(productionRates != null && productionRates.containsKey(purpose))
            return productionRates.get(purpose);
        
        if (productionRates == null) productionRates = new HashMap<TripPurpose,Map<MarketSegment,Double>>();
        
        String purposeDetailsFile = inputs.getProperty("productions.general." + purpose);
        Map<MarketSegment, Double> rates = ProductionAttractionFactory.readGeneralRates(purposeDetailsFile, this);
        productionRates.put(purpose, rates);
        return rates;
    }

    @Override
    public synchronized Map<MarketSegment, Map<AreaClass, Double>> getAreaClassProdRates(TripPurpose purpose) {
        if (areaClassProdRates == null) areaClassProdRates = new HashMap<TripPurpose,Map<MarketSegment,Map<AreaClass,Double>>>();
        else if (areaClassProdRates.containsKey(purpose))
            return areaClassProdRates.get(purpose);
        
        String purposeDetailsFile = inputs.getProperty("productions.area." + purpose.toString());
        Map<MarketSegment, Map<AreaClass, Double>> rates = ProductionAttractionFactory.readAreaRates(purposeDetailsFile, this);
        areaClassProdRates.put(purpose, rates);
        return rates;
    }

    @Override
    public synchronized Map<MarketSegment, Double> getGeneralAttrRates(TripPurpose purpose) {
        if (attractionRates == null) attractionRates = new HashMap<TripPurpose,Map<MarketSegment,Double>>();
        else if (attractionRates.containsKey(purpose))
            return attractionRates.get(purpose);
        
        
        
        String purposeDetailsFile = inputs.getProperty("attractions.general." + purpose.toString());
        Map<MarketSegment, Double> rates = ProductionAttractionFactory.readGeneralRates(purposeDetailsFile, this);
        attractionRates.put(purpose, rates);
        return rates;
    }

    @Override
    public synchronized Map<MarketSegment, Map<AreaClass, Double>> getAreaClassAttrRates(TripPurpose purpose) {
        if (areaClassAttrRates == null) areaClassAttrRates = new HashMap<TripPurpose,Map<MarketSegment,Map<AreaClass,Double>>>();

        else if(areaClassAttrRates.containsKey(purpose))
            return areaClassAttrRates.get(purpose);
        
        String purposeDetailsFile = inputs.getProperty("attractions.area." + purpose.toString());
        Map<MarketSegment, Map<AreaClass, Double>> rates = ProductionAttractionFactory.readAreaRates(purposeDetailsFile, this);
        areaClassAttrRates.put(purpose, rates);
        return rates;
    }

    @Override
    public float[][] getRoadwaySkim(TimePeriod timePeriod) {
    	synchronized(timePeriod) {
    		if (skimFactors != null && skimFactors.containsKey(timePeriod)) 
    			return skimFactors.get(timePeriod);
    		if (skimFactors == null) skimFactors = new HashMap<TimePeriod,float[][]>();
    		
    		String skimFile = inputs.getProperty("skims."+timePeriod.toString());
    		float[][] skim = SkimFactory.readSkimFile(Paths.get(skimFile.trim()), false, getNetwork());
    		skimFactors.put(timePeriod, skim);
    		return skim;
    	}
    }

    @Override
    public synchronized FrictionFactorMap getFrictionFactors(TripPurpose purpose, TimePeriod timePeriod, MarketSegment segment) {
        if (frictionFactors != null 
        		&& frictionFactors.get(purpose) != null 
        		&& frictionFactors.get(purpose).get(timePeriod) != null
        		&& frictionFactors.get(purpose).get(timePeriod).get(segment) != null)
        	return frictionFactors.get(purpose).get(timePeriod).get(segment);

        if (frictionFactors == null) frictionFactors = new HashMap<TripPurpose, Map<TimePeriod, Map<MarketSegment, FrictionFactorMap>>>();
        frictionFactors.putIfAbsent(purpose, new HashMap<TimePeriod, Map<MarketSegment,FrictionFactorMap>>());
        frictionFactors.get(purpose).putIfAbsent(timePeriod, new HashMap<MarketSegment,FrictionFactorMap>());
        
        

        String frictionFile = inputs.getProperty("frictionFactors."+purpose.toString()+"."+timePeriod.toString()+
        		(segment == null? "" : "."+getLabel(segment)));
        float[][] skim = getRoadwaySkim(timePeriod);
        FrictionFactorMap map = FrictionFactorFactory.readFactorFile(Paths.get(frictionFile), true, skim);
        frictionFactors.get(purpose).get(timePeriod).put(segment,map);
        
    	return map;
    }

    @Override
    public synchronized Map<MarketSegment, Map<Mode, Double>> getModeShares(TripPurpose purpose) {
    	if (modalShares != null && modalShares.get(purpose) != null) return modalShares.get(purpose);
    	if (modalShares == null) modalShares = new HashMap<TripPurpose, Map<MarketSegment,Map<Mode,Double>>>();
    	//TODO finish this
    	
    }

    @Override
    public synchronized Map<Mode, Double> getOccupancyRates() {
    	// TODO
    	
    }

    @Override
    public synchronized Map<TimePeriod, Double> getDepartureRates(TripPurpose purpose, MarketSegment segment) {
    	// TODO
    	
    }

    @Override
    public synchronized Map<TimePeriod, Double> getArrivalRates(TripPurpose purpose, MarketSegment segment) {
    	// TODO
    	
    }

	@Override
	public synchronized Map<MarketSegment, Double> getPeakShares(TripPurpose purpose) {
		try {
			String shareFile = inputs.getProperty("splitting.peakShares");
			return Files.lines(Paths.get(shareFile)).parallel().filter(line -> !line.startsWith("I"))
					.map(line -> line.split(","))
					.collect(Collectors.toMap(
							args ->	new IncomeGroupSegment(Integer.parseInt(args[0])),
							args -> Double.parseDouble(args[1])));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-6);
			return null;
		}
	}

	@Override
	public synchronized Map<MarketSegment, Map<TravelSurveyZone, Double>> getWorkerVehicleSplits(MarketSegment segment, TripPurpose purpose) {
		// TODO Auto-generated method stub
		String splitFile = inputs.getProperty("splitting.wkrVehSplits."+purpose.toString());
		try {
			Files.lines(Paths.get(splitFile)).parallel().filter(line -> !line.startsWith("T")).forEach(line ->{
				String[] args = line.split(",");
				TravelSurveyZone tsz = getNetwork().getNode(Integer.parseInt(args[0])).getZone();
				
				
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-9);
			return null;
		}
	}

	@Override
	public String getOutputDirectory() {
		return inputs.getProperty("outputDirectory");
	}

	@Override
	public Class<? extends MarketSegment> getSegmenterByString(String segmentLabel) {
		// TODO Auto-generated method stub
		return headerToSegment.get(segmentLabel);
	}
	
	@Override
	public String getLabel(MarketSegment segment) {
		StringBuilder sb = new StringBuilder();
		
		if (segment instanceof WorkerSegmenter) {
			sb.append("W");
			sb.append(((WorkerSegmenter) segment).getNumberOfWorkers());
		}
		
		if (segment instanceof VehicleSegmenter) {
			sb.append("V");
			sb.append(((VehicleSegmenter) segment).getNumberOfVehicles());
		}
		
		if (segment instanceof IncomeGroupSegmenter) {
			sb.append("IG");
			sb.append(((IncomeGroupSegmenter) segment).getIncomeGroup());
		}
		
		if (segment instanceof IndustrySegmenter) {
			String ic = ((IndustrySegmenter) segment).getIndustryClass().toString();
			sb.append(ic.substring(0,1).toUpperCase());
			sb.append(ic.substring(1, 2).toLowerCase());
		}
		
		return sb.toString();
	}

}
