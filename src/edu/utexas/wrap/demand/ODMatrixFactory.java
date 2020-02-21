package edu.utexas.wrap.demand;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.demand.containers.FixedSizeODMatrix;
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
}
