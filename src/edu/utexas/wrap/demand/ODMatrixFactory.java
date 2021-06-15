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
package edu.utexas.wrap.demand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;

import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.demand.containers.FixedSizeODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ODMatrixFactory {

	public static ODMatrix readTNTPMatrix(File file, Graph g) throws IOException {
		BufferedReader of = Files.newBufferedReader(file.toPath());
		int numZones = 0;
		String line;
		
		do {
			line = of.readLine();
			if (line.toUpperCase().contains("NUMBER OF ZONES")) {
				String[] args = line.split(" ");
				numZones = Integer.parseInt(args[args.length-1]);
			}
				
			
		} while (!line.startsWith("Origin"));
		
		if (numZones <= 0) throw new IOException("Invalid number of zones: "+numZones+"\r\nCheck file metadata");
		
		numZones = g.numZones();
		FixedSizeODMatrix<FixedSizeDemandMap> od = new FixedSizeODMatrix<FixedSizeDemandMap>(null,g.getTSZs());
		
		while (true) {
			while (line != null && !line.startsWith("O"))
				line = of.readLine(); // Read in the origin header
			if (line == null || line.trim().equals(""))
				break; // If the origin header is empty, we've reached the end of the list

			Integer origID = Integer.parseInt(line.trim().split("\\s+")[1]);
			Node root = g.getNode(origID); // Retrieve the existing node with that ID
			
			TravelSurveyZone zone = root.getZone();
			
			if (zone == null) throw new RuntimeException("Demand specified for missing TravelSurveyZone: "+origID);
			System.out.print("\rReading demand for origin " + origID);
			FixedSizeDemandMap unified = readTNTPDemandMap(of, g, numZones);
			
			od.setDemandMap(zone, unified);
				
			
			line = of.readLine();
		}
		of.close();
		System.out.println();
		return od;
	}
	
	/**
	 * This method reads a file containing information about the destination demand
	 * @param of BufferedReader object of the file
	 * @param g The Graph object of the relevant network
	 * @param zoneCount Count of zones in the network
	 * @return A Demand hashmap indicating the demand between an origin and a destination
	 * @throws IOException
	 */
	private static FixedSizeDemandMap readTNTPDemandMap(BufferedReader of, Graph g, Integer zoneCount) throws IOException {
		String[] cols;
		Integer destID;
		Float demand;
		FixedSizeDemandMap dests = new FixedSizeDemandMap(g.getTSZs());

		while (true) {
			String line = of.readLine();
			if (line == null || line.trim().startsWith("O") || line.trim().equals(""))
				break; // If we've reached the gap, move to the next origin
			String[] entries = line.trim().split(";");

			for (String entry : entries) { // For each entry on this line
				cols = entry.split(":"); // Get its values
				destID = Integer.parseInt(cols[0].trim());
				demand = Float.parseFloat(cols[1].trim());
				if (demand > 0.0) {
					TravelSurveyZone tsz = g.getNode(destID).getZone();
					if (tsz == null) throw new RuntimeException("Demand specified for missing TravelSurveyZone: "+destID);
					dests.put(tsz, demand);
				}
			}
		}
		return dests;
	}
	
	public static Collection<ODMatrix> readConicTrips(File odMatrix, Graph g) throws FileNotFoundException,IOException {
		
		BufferedReader matrixFile = Files.newBufferedReader(odMatrix.toPath());
		Collection<TravelSurveyZone> zones = g.getTSZs();
		
		FixedSizeODMatrix<FixedSizeDemandMap> 
		solo17 = new FixedSizeODMatrix<FixedSizeDemandMap>(Mode.SINGLE_OCC, zones), 
		solo35 = new FixedSizeODMatrix<FixedSizeDemandMap>(Mode.SINGLE_OCC, zones), 
		solo45 = new FixedSizeODMatrix<FixedSizeDemandMap>(Mode.SINGLE_OCC, zones), 
		solo90 = new FixedSizeODMatrix<FixedSizeDemandMap>(Mode.SINGLE_OCC, zones),

		hov17 = new FixedSizeODMatrix<FixedSizeDemandMap>(Mode.HOV, zones), 
		hov35 = new FixedSizeODMatrix<FixedSizeDemandMap>(Mode.HOV, zones), 
		hov45 = new FixedSizeODMatrix<FixedSizeDemandMap>(Mode.HOV, zones), 
		hov90 = new FixedSizeODMatrix<FixedSizeDemandMap>(Mode.HOV, zones),

		medTrucks = new FixedSizeODMatrix<FixedSizeDemandMap>(Mode.MED_TRUCK, zones), 
		hvyTrucks = new FixedSizeODMatrix<FixedSizeDemandMap>(Mode.MED_TRUCK, zones);
		

		matrixFile.lines().sequential().forEach(line -> {
			if (line == null || line.trim().equals("")) return;
			String[] args = line.split(",");
//			System.out.print("\rLoading origin "+args[0]);
			TravelSurveyZone orig = g.getNode(Integer.parseInt(args[0])).getZone();
			TravelSurveyZone dest = g.getNode(Integer.parseInt(args[1])).getZone();
			
			put(solo17, orig, dest, args[6]);
			put(solo35, orig, dest, args[2]);
			put(solo45, orig, dest, args[7]);
			put(solo90, orig, dest, args[3]);

			put(hov17, orig, dest, args[8]);
			put(hov35, orig, dest, args[4]);
			put(hov45, orig, dest, args[9]);
			put(hov90, orig, dest, args[5]);

			put(medTrucks, orig, dest, args[10]);
			put(hvyTrucks, orig, dest, args[11]);

			
		});
		
		Collection<ODMatrix> ret = new HashSet<ODMatrix>();
		ret.add(solo17);
		ret.add(solo35);
		ret.add(solo45);
		ret.add(solo90);
		
		ret.add(hov17);
		ret.add(hov35);
		ret.add(hov45);
		ret.add(hov90);
//		
		ret.add(medTrucks);
		ret.add(hvyTrucks);
		
		matrixFile.close();
		return ret;
	}

	private static void put(FixedSizeODMatrix<FixedSizeDemandMap> matrix, TravelSurveyZone orig,
			TravelSurveyZone dest, String demand) {
		Float da17;
		try {
			da17 = Float.parseFloat(demand);
		} catch (NumberFormatException e) {
			da17 = 0.0f;
		}
		
		if (matrix.getDemandMap(orig) == null) 
			matrix.setDemandMap(orig, new FixedSizeDemandMap(matrix.getZones()));
		matrix.put(orig, dest, da17);
	}

}
