package edu.utexas.wrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.AggregatePAHashMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class ProductionAttractionFactory {
	public static PAMap readMap(File file, boolean header, Graph g) throws IOException {
		PAMap ret = new AggregatePAHashMap(g);
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(file));
			if (header) in.readLine();
			String line = in.readLine();

			while (line != null) {
				String[] args = line.split(",");
				Node n = g.getNode(Integer.parseInt(args[0]));
				Float prods = Float.parseFloat(args[1]);
				Float attrs = Float.parseFloat(args[2]);

				ret.putProductions(n, prods);
				ret.putAttractions(n, attrs);

				line = in.readLine();
			}

		} finally {
			if (in != null) in.close();
		}
		return ret;
	}
	
	//TODO read matrices from files
}
