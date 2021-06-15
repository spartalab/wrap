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
import java.util.Map;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.AggregatePAHashMap;
import edu.utexas.wrap.demand.containers.FixedSizeAggregatePAMatrix;
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
	public static PAMap readMap(Path path, boolean header, Map<Integer,TravelSurveyZone> zones) throws IOException {
		PAMap ret = new AggregatePAHashMap();
		BufferedReader in = null;

		try {
			in = Files.newBufferedReader(path);
			if (header) in.readLine();
			in.lines().parallel().forEach(line -> {
				String[] args = line.replaceAll("[^\\x00-\\xff]", "").split(",");
				TravelSurveyZone tsz = zones.get(Integer.parseInt(args[0]));
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
	public static AggregatePAMatrix readMatrix(Path path, boolean header, Map<Integer, TravelSurveyZone> zones) throws IOException {
		AggregatePAMatrix ret = new FixedSizeAggregatePAMatrix(zones.values());
		BufferedReader in = null;

		try {
			in = Files.newBufferedReader(path);
			if (header) in.readLine();
			in.lines().parallel().forEach(line -> {
				String[] args = line.split(",");
				TravelSurveyZone prod = zones.get(Integer.parseInt(args[0]));
				TravelSurveyZone attr = zones.get(Integer.parseInt(args[1]));
				Float trips = Float.parseFloat(args[2]);

				ret.put(prod, attr, trips);
			});

		} finally {
			if (in != null) in.close();
		}
		return ret;
	}

}
