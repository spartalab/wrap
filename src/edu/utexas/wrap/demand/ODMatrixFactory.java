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
		else g.setNumZones(numZones);
		
		numZones = 0;
		FixedSizeODMatrix<FixedSizeDemandMap> od = new FixedSizeODMatrix<FixedSizeDemandMap>(1.0f,null,g);
		
		while (true) {
			while (line != null && !line.startsWith("O"))
				line = of.readLine(); // Read in the origin header
			if (line == null || line.trim().equals(""))
				break; // If the origin header is empty, we've reached the end of the list

			Integer origID = Integer.parseInt(line.trim().split("\\s+")[1]);
			Node root = g.getNode(origID); // Retrieve the existing node with that ID
			
			TravelSurveyZone zone = root.getZone();
			
			if (zone == null) {
				zone = new TravelSurveyZone(root,numZones++,null);
				root.setTravelSurveyZone(zone);
				g.addZone(zone);
			}
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
		FixedSizeDemandMap dests = new FixedSizeDemandMap(g);

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
					if (tsz == null) { 
						tsz = new TravelSurveyZone(g.getNode(destID),zoneCount++,null);
						g.getNode(destID).setTravelSurveyZone(tsz);
						g.addZone(tsz);
					}
					dests.put(tsz, demand);
				}
			}
		}
		return dests;
	}
	
	public static Collection<ODMatrix> readConicTrips(File odMatrix, Graph g) throws FileNotFoundException,IOException {
		
		BufferedReader matrixFile = Files.newBufferedReader(odMatrix.toPath());
		
		FixedSizeODMatrix<FixedSizeDemandMap> 
		solo17 = new FixedSizeODMatrix<FixedSizeDemandMap>(0.17f, Mode.SINGLE_OCC, g), 
		solo35 = new FixedSizeODMatrix<FixedSizeDemandMap>(0.35f, Mode.SINGLE_OCC, g), 
		solo45 = new FixedSizeODMatrix<FixedSizeDemandMap>(0.45f, Mode.SINGLE_OCC, g), 
		solo90 = new FixedSizeODMatrix<FixedSizeDemandMap>(0.90f, Mode.SINGLE_OCC, g),

		hov17 = new FixedSizeODMatrix<FixedSizeDemandMap>(0.17f, Mode.HOV, g), 
		hov35 = new FixedSizeODMatrix<FixedSizeDemandMap>(0.35f, Mode.HOV, g), 
		hov45 = new FixedSizeODMatrix<FixedSizeDemandMap>(0.45f, Mode.HOV, g), 
		hov90 = new FixedSizeODMatrix<FixedSizeDemandMap>(0.90f, Mode.HOV, g),

		medTrucks = new FixedSizeODMatrix<FixedSizeDemandMap>(1f, Mode.MED_TRUCK, g), 
		hvyTrucks = new FixedSizeODMatrix<FixedSizeDemandMap>(1f, Mode.MED_TRUCK, g);
		

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
//
//			put(medTrucks, orig, dest, args[10]);
//			put(hvyTrucks, orig, dest, args[11]);

			
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
//		ret.add(medTrucks);
//		ret.add(hvyTrucks);
		
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
			matrix.setDemandMap(orig, new FixedSizeDemandMap(matrix.getGraph()));
		matrix.put(orig, dest, da17);
	}

}
