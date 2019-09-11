package edu.utexas.wrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import edu.utexas.wrap.distribution.CostBasedFrictionFactorMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FrictionFactorFactory {
	public static FrictionFactorMap readFactorFile(File file, boolean header, float[][] skim) throws IOException {
		TreeMap<Integer, Float> tree = new TreeMap<Integer, Float>();
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(file));
			if (header) in.readLine();
			String line = in.readLine();

			while (line != null) {
				String[] args = line.split(",");
				Integer cost = Integer.parseInt(args[0]);
				Float factor = Float.parseFloat(args[1]);

				tree.put(cost, factor);

				line = in.readLine();
			}
		} finally {
			if (in != null) in.close();
		}
		return new CostBasedFrictionFactorMap(skim, tree);
	}
}
