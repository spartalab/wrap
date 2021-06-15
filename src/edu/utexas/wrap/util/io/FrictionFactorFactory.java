/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.utexas.wrap.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

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
	public static FrictionFactorMap readFactorFile(Path file) {
		NavigableMap<Integer, Float> tree = new ConcurrentSkipListMap<Integer, Float>();
		BufferedReader in = null;

		try {
			in = new BufferedReader(Files.newBufferedReader(file));
			in.readLine();

			in.lines().parallel().forEach(line -> {
				String[] args = line.split(",");
				Integer cost = Integer.parseInt(args[0]);
				Float factor = Float.parseFloat(args[1]);

				tree.put(cost, factor);
			});
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-4);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(-4);
				}
		}
		return new CostBasedFrictionFactorMap(tree);
	}
}
