package edu.utexas.wrap.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.CentroidConnector;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TolledBPRLink;
import edu.utexas.wrap.net.TolledEnhancedLink;
import edu.utexas.wrap.net.TravelSurveyZone;

/**
 * This class provides static methods to read Graph information
 */
public class GraphFactory {
	/**
	 * This method produces a Graph object after reading it in from the TNTP Format
	 * More information about the TNTP Format can be found here:
	 * https://github.com/bstabler/TransportationNetworks
	 * @param linkFile File with the network information
	 * @return Graph object representation of input network
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Graph readTNTPGraph(File linkFile, Map<Integer,TravelSurveyZone> zoneIDs) throws FileNotFoundException, IOException {

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			DigestInputStream dis = new DigestInputStream(new FileInputStream(linkFile), md);

			String line;
			Graph g = new Graph(zoneIDs.values());
			BufferedReader lf = new BufferedReader(new InputStreamReader(dis));
			Map<Integer, Node> nodeIDs = new HashMap<Integer,Node>();
			AtomicInteger nodeIdx = new AtomicInteger(0);
			Integer ftn = 1;
			do { // Move past headers in the file
				line = lf.readLine();
				if (line.startsWith("<FIRST THRU NODE>")) {
					ftn = Integer.parseInt(line.trim().split("\\s+")[3]);
				}
			} while (!line.startsWith("~"));

			while (true) { // Iterate through each link (row)
				line = lf.readLine();
				if (line == null)
					break; // End of link list reached
				if (line.startsWith("~") || line.trim().equals(""))
					continue;
				line = line.trim();
				String[] cols = line.split("\\s+");
				Integer tail = Integer.parseInt(cols[0]);
				Integer head = Integer.parseInt(cols[1]);
				Float capacity = parse(cols[2]);
				Float length = parse(cols[3]);
				Float fftime = parse(cols[4]);
				Float toll = parse(cols[8]);

				// Create new node(s) if new, then add to map
				if (!nodeIDs.containsKey(tail)) {
					nodeIDs.put(tail, new Node(tail, nodeIdx.getAndIncrement(),zoneIDs.get(tail)));

				}

				if (!nodeIDs.containsKey(head)) {
					nodeIDs.put(head, new Node(head, nodeIdx.getAndIncrement(),zoneIDs.get(head)));
				}
				
				// Construct new link and add to the list
				Link link;
				if (tail >= ftn && head >= ftn) {
					Float B = parse(cols[5]);
					Float power = parse(cols[6]);
					link = new TolledBPRLink(nodeIDs.get(tail), nodeIDs.get(head), capacity, length, fftime, B, power, toll);
				}
				else {
					link = new CentroidConnector(nodeIDs.get(tail), nodeIDs.get(head), capacity, length, fftime, toll);
				}
				g.add(link);
			}
			lf.close();
			g.setMD5(md.digest());
			g.complete();
			return g;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * This method produces a Graph object after reading it from a CSV file
	 * The file contains information about the CONAC paramters and uses EnhancedLinks
	 * to build the graph
	 * @param f File with the network information
	 * @return Graph object representation of input network
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Graph readConicGraph(File f, Map<Integer,TravelSurveyZone> zoneIDs) throws FileNotFoundException, IOException {
		try {
		MessageDigest md = MessageDigest.getInstance("MD5");
		Graph g = new Graph(zoneIDs.values());
		AtomicInteger numNodes = new AtomicInteger(0);
		DigestInputStream dis = new DigestInputStream(new FileInputStream(f), md);
		BufferedReader lf = new BufferedReader(new InputStreamReader(dis));
		Map<Integer, Node> nodes = new ConcurrentHashMap<Integer,Node>();
		
		lf.lines().parallel()
		.filter(line -> line != null && !line.startsWith("I") && !line.equals(""))
		.forEach(line -> {

			String[] args = line.split(",");

			Integer nodeA = Integer.parseInt(args[1]);
			Integer nodeB = Integer.parseInt(args[2]);

			if (!nodes.containsKey(nodeA)) {
//				if (nodeA < thruNode) {
//					newZone(g, numZones, numNodes, nodes, nodeA, classes.get(nodeA));
//				} else
					nodes.put(nodeA, new Node(nodeA, numNodes.getAndIncrement(),zoneIDs.get(nodeA)));
			}

			if (!nodes.containsKey(nodeB)) {
//				if (nodeB < thruNode) {
//					newZone(g, numZones, numNodes, nodes, nodeB, classes.get(nodeB));
//				} else
					nodes.put(nodeB, new Node(nodeB, numNodes.getAndIncrement(),zoneIDs.get(nodeB)));

			}
			

			Float aCap = parse(args[3]);
			Float bCap = parse(args[4]);
			Float length = parse(args[5]);
			Float ffTimeA = parse(args[6]);
			Float ffTimeB = parse(args[7]);

			Float alpha = parse(args[8]);
			Float epsilon = parse(args[9]);
			Float sParA = parse(args[10]);
			Float sParB = parse(args[11]);
			Float satFlowA = parse(args[12]);
			Float satFlowB = parse(args[13]);

			Float caA = parse(args[14]);
			Float cbA = parse(args[15]);
			Float ccA = parse(args[16]);
			Float cdA = parse(args[17]);

			Float caB = parse(args[18]);
			Float cbB = parse(args[19]);
			Float ccB = parse(args[20]);
			Float cdB = parse(args[21]);

			Float minDel = parse(args[22]);
			Float uParA = parse(args[23]);
			Float uParB = parse(args[24]);
			Float opCostA = parse(args[25]);
			Float opCostB = parse(args[26]);

			float[] tollA = new float[Mode.values().length];
			float[] tollB = new float[Mode.values().length];
			

			tollA[Mode.SINGLE_OCC.ordinal()] = parse(args[27]);
			tollB[Mode.SINGLE_OCC.ordinal()] = parse(args[28]);
			tollA[Mode.HOV_2_PSGR.ordinal()] = parse(args[29]);
			tollB[Mode.HOV_2_PSGR.ordinal()] = parse(args[30]);
			tollA[Mode.MED_TRUCK.ordinal()] = parse(args[31]);
			tollB[Mode.MED_TRUCK.ordinal()] = parse(args[32]);
			tollA[Mode.HVY_TRUCK.ordinal()] = parse(args[33]);
			tollB[Mode.HVY_TRUCK.ordinal()] = parse(args[34]);

			boolean[] allowed = new boolean[Mode.values().length];
			Arrays.fill(allowed, true);
			Boolean a = !Boolean.parseBoolean(args[35].trim());
			allowed[Mode.SINGLE_OCC.ordinal()] = a;
			allowed[Mode.HVY_TRUCK.ordinal()] = a;
			allowed[Mode.MED_TRUCK.ordinal()] = a;

				Link AB = null;
				if (aCap > 0 && satFlowA > 0) {
					AB = new TolledEnhancedLink(nodes.get(nodeA), nodes.get(nodeB), aCap, length, ffTimeA, alpha,
							epsilon, sParA, uParA, satFlowA, minDel, opCostA, caA, cbA, ccA, cdA, tollA, allowed);
				} else {
					AB = new CentroidConnector(nodes.get(nodeA), nodes.get(nodeB), aCap, length, ffTimeA,
							opCostA.floatValue());
				}
				g.add(AB);

			if (bCap > 0.0) {
				Link BA = null;
				if (satFlowB > 0) {
					BA = new TolledEnhancedLink(nodes.get(nodeB), nodes.get(nodeA), bCap, length, ffTimeB, alpha,
							epsilon, sParB, uParB, satFlowB, minDel, opCostB, caB, cbB, ccB, cdB, tollB, allowed);
				} else {
					BA = new CentroidConnector(nodes.get(nodeB), nodes.get(nodeA), bCap, length, ffTimeB,
							opCostB.floatValue());
				}
				g.add(BA);
			}
		});
		lf.close();
		g.setMD5(md.digest());
		g.complete();
		return g;
		
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Float parse(String s) {
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException e) {
			return 0.0F;
		}
	}

}
