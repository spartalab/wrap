package edu.utexas.wrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.demand.containers.AggregatePAHashMap;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
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
}
