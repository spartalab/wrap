package edu.utexas.wrap.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.HashMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.demand.containers.AggregatePAHashMap;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.marketsegmentation.IndustryClass;
import edu.utexas.wrap.marketsegmentation.IndustrySegmenter;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.marketsegmentation.VehicleSegmenter;
import edu.utexas.wrap.marketsegmentation.WorkerSegmenter;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegmenter;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

/**
 * This class provides static methods to read various files relating to productions, attractions, and their respective rates
 */
public class ProductionAttractionFactory {
	/**
	 * This method takes an input file and produces a PA Map
	 * It expects a .csv file with the values in the following order:
	 * |ZoneID, Productions, Attractions|
	 * @param file File object to be parsed
	 * @param header Boolean whether the file has a header
	 * @param g Graph for associated with this PA Map
	 * @return PA Map from the file
	 * @throws IOException
	 */
	public static PAMap readMap(File file, boolean header, Graph g) throws IOException {
		PAMap ret = new AggregatePAHashMap(g);
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(file));
			if (header) in.readLine();
			in.lines().parallel().forEach(line -> {
				String[] args = line.split(",");
				TravelSurveyZone tsz = g.getNode(Integer.parseInt(args[0])).getZone();
				Float prods = Float.parseFloat(args[1]);
				Float attrs = Float.parseFloat(args[2]);

				ret.putProductions(tsz, prods);
				ret.putAttractions(tsz, attrs);
			});

		} finally {
			if (in != null) in.close();
		}
		return ret;
	}

	/**
	 * This method takes an input file and produces a PA Matrix
	 * It expects a .csv file with the values in the following order:
	 * |ZoneID (Production zone), ZoneID (Attraction zone), Demand|
	 * @param file File object to be parsed
	 * @param header Boolean whether the file has a header
	 * @param g Graph associated with this PA Matrix
	 * @return PA Matrix from the file
	 * @throws IOException
	 */
	public static PAMatrix readMatrix(File file, boolean header, Graph g) throws IOException {
		PAMatrix ret = new AggregatePAHashMatrix(g);
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(file));
			if (header) in.readLine();
			in.lines().parallel().forEach(line -> {
				String[] args = line.split(",");
				TravelSurveyZone prod = g.getNode(Integer.parseInt(args[0])).getZone();
				TravelSurveyZone attr = g.getNode(Integer.parseInt(args[1])).getZone();
				Float trips = Float.parseFloat(args[2]);

				ret.put(prod, attr, trips);
			});

		} finally {
			if (in != null) in.close();
		}
		return ret;
	}


	/**
	 * This method takes an input file and creates a map of market segment to their respective rates based on Vehicle and worker segments
	 * It expects a .csv file with the values in the following order:
	 * |WorkersInSegment, VehiclesInSegment, rate, rate_v1|
	 * @param file object to read prodcution/attraction market segment rates
	 * @param header boolean whether the file has a header
	 * @param v1 boolean whether to read segment rates v1 or normal
	 * @param microProdSegs Collection of segments
	 * @return
	 * @throws IOException
	 */
	public static Map<MarketSegment,Double>  readSegmentRates(File file, boolean header, boolean v1, Collection<MarketSegment> microProdSegs) throws IOException {
		//TODO don't make assumption about the header orders, make a quick check for that
		BufferedReader in = null;
		Map<MarketSegment,Double> map = new ConcurrentHashMap<MarketSegment,Double>();

		try {
			in = new BufferedReader(new FileReader(file));
			if (header) in.readLine();
			in.lines().parallel().forEach(line -> {
				String[] args = line.split(",");
				int workers = Integer.parseInt((args[0]));
				int vehicles = Integer.parseInt((args[1]));
				Double hbwPro = v1 ? Double.parseDouble((args[3])) : Double.parseDouble(args[2]);

				microProdSegs.parallelStream()
				.filter(seg -> seg instanceof VehicleSegmenter && ((VehicleSegmenter) seg).getNumberOfVehicles() == vehicles)
				.filter(seg -> seg instanceof WorkerSegmenter && ((WorkerSegmenter) seg).getNumberOfWorkers() == workers)
				.forEach(seg -> map.put(seg, hbwPro));
			});

		} finally {
			if (in != null) in.close();
		}
		return map;
	}

	/**
	 * This method takes an input file and creates a mapping between market segments to a map between area class and attraction rates
	 * It expects .ccsv file with the values in the following order:
	 * |IncomeGroup, Industry, AreaType, HBW_Attr,....|
	 * The function currently ONLY looks at the HBW_attr, which is assumed to be the column right after the segmenting paramters
	 * Requires a header
	 * @param file
	 * @param segments
	 * @return
	 * @throws IOException
	 */
	public static Map<MarketSegment, Map<AreaClass,Double>> readSegmentAreaRates(File file, Collection<MarketSegment> segments, Set<String> types) throws IOException {
		BufferedReader in = null;
		Map<MarketSegment,Map<AreaClass,Double>> map = new ConcurrentHashMap<MarketSegment,Map<AreaClass,Double>>();
		Set<Integer> indices = new HashSet<Integer>();

		try {
			in = new BufferedReader(new FileReader(file));

			// Header preprocessing, assume header
			String[] headers = in.readLine().split(",");
			for (int i = 0; i < headers.length; i++) {
				if (types.contains(headers[i])) {
					indices.add(i);
				}
			}
			in.lines().parallel().forEach(line -> {
				String[] args = line.split(",");
				int income = Integer.parseInt(args[0]);

				if (income > 3) return; //FIXME temporary for demo
				
				final IndustryClass industryClass;
				switch (args[1].charAt(1)) {
				case 'B':
					industryClass = IndustryClass.BASIC;
					break;
				case 'R':
					industryClass = IndustryClass.RETAIL;
					break;
				case 'S':
					industryClass = IndustryClass.SERVICE;
					break;
				default:
					throw new RuntimeException("Unrecognized industry class");
				}
//				if (industry.equals('B')) {
//					industryClass = IndustryClass.BASIC;
//				} else if (industry.equals('R')) {
//					industryClass = IndustryClass.RETAIL;
//				} else if (industry.equals('S')) {
//					industryClass = IndustryClass.SERVICE;
//				} else {
//					// Error
//				}

				Optional<MarketSegment> segs = segments.parallelStream()
						.filter(seg -> seg instanceof IncomeGroupSegmenter)
						.filter(seg -> seg instanceof IndustrySegmenter)
						.filter(seg -> ((IncomeGroupSegmenter) seg).getIncomeGroup() == income)
						.filter(seg -> ((IndustrySegmenter) seg).getIndustryClass().equals(industryClass)).findAny();

				MarketSegment market = segs
						.orElseThrow(
								() -> 
								new RuntimeException("Unregcognized industry class"));

				AreaClass areaClass = null;
				int areaType = Integer.parseInt(args[2]);
				switch (areaType) {
					case 1:
						areaClass = AreaClass.CBD;
						break;
					case 2:
						areaClass = AreaClass.OBD;
						break;
					case 3:
						areaClass = AreaClass.URBAN_RESIDENTIAL;
						break;
					case 4:
						areaClass = AreaClass.SUBURBAN_RESIDENTIAL;
						break;
					case 5:
						areaClass = AreaClass.RURAL;
						break;
					default:
						throw new RuntimeException("Unrecognized area class");
				}


				Double hbwAttr = 0.0;
				for (Integer index : indices) {
					hbwAttr += Double.parseDouble(args[index]);
				}
				// If we don't have the market segment we add it
				map.computeIfAbsent(market, k -> new HashMap<AreaClass,Double>()).put(areaClass, hbwAttr);

			});

		} finally {
			if (in != null) in.close();
		}
		return map;
	}
}
