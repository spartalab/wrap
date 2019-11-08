package edu.utexas.wrap.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

import edu.utexas.wrap.distribution.CostBasedFrictionFactorMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;

/**
 * This class provides static methods to read FrictionFactor information.
 */
public class FrictionFactorFactory {
	/**
	 * This method takes an input file and produces a FrictionFactorMap
	 * It expects a .csv file with the values in the following order:
	 * |Time, Factor|
	 * @param file Input file
	 * @param header boolean whether the file has a header
	 * @param skim array of skim values
	 * @return A FrictionFactorMap
	 * @throws IOException
	 */
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
