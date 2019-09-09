package edu.utexas.wrap.util;

import java.awt.geom.Area;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.demand.containers.AggregatePAHashMap;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.marketsegmentation.IndustryClass;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupIndustrySegment;
import edu.utexas.wrap.marketsegmentation.WorkerVehicleSegment;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ProductionAttractionFactory {
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

	public static Map<MarketSegment,Double>  readProductionRates(File file, boolean header, boolean v1) throws IOException {
		BufferedReader in = null;
		Map<MarketSegment,Double> map = new HashMap<>();

		try {
			in = new BufferedReader(new FileReader(file));
			if (header) in.readLine();
			in.lines().parallel().forEach(line -> {
				String[] args = line.split(",");
				int workers = Integer.parseInt((args[0]));
				int vehicles = Integer.parseInt((args[1]));
				Double hbwPro = v1 ? Double.parseDouble((args[3])) : Double.parseDouble(args[2]);

				MarketSegment workerVehicle = new WorkerVehicleSegment(workers, vehicles);
				map.put(workerVehicle, hbwPro);
			});

		} finally {
			if (in != null) in.close();
		}
		return map;
	}

	public static Map<MarketSegment, Map<AreaClass,Double>> readAttractionRates(File file, boolean header) throws IOException {
		BufferedReader in = null;
		Map<MarketSegment,Map<AreaClass,Double>> map = new HashMap<>();

		try {
			in = new BufferedReader(new FileReader(file));
			if (header) in.readLine();
			in.lines().parallel().forEach(line -> {
				String[] args = line.split(",");
				int income = Integer.parseInt(args[0]);

				IndustryClass industryClass = null;
				String industry = args[1];
				if (industry.equals('B')) {
					industryClass = IndustryClass.BASIC;
				} else if (industry.equals('R')) {
					industryClass = IndustryClass.RETAIL;
				} else if (industry.equals('S')) {
					industryClass = IndustryClass.SERVICE;
				} else {
					// Error
				}

				MarketSegment market = new IncomeGroupIndustrySegment(income, industryClass);

				AreaClass areaClass = null;
				int areaType = Integer.parseInt(args[2]);
				switch(areaType) {
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
						System.out.println("error");
						break;
				}


				Double hbwAttr = Double.parseDouble(args[3]);
				// If we don't have the market segment we add it
				if (!map.containsKey(market)) {
					Map<AreaClass, Double> temp = new HashMap<>();
					temp.put(areaClass, hbwAttr);
					map.put(market, temp);
				} else {
					Map<AreaClass, Double> temp = map.get(market);
					temp.put(areaClass, hbwAttr);
				}

			});

		} finally {
			if (in != null) in.close();
		}
		return map;
	}
}
