package edu.utexas.wrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.CentroidConnector;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TolledBPRLink;
import edu.utexas.wrap.net.TolledEnhancedLink;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class GraphFactory {
	public static Graph readTNTPGraph(File linkFile) throws FileNotFoundException, IOException {

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			DigestInputStream dis = new DigestInputStream(new FileInputStream(linkFile), md);

			String line;
			Graph g = new Graph();
			BufferedReader lf = new BufferedReader(new InputStreamReader(dis));
			Map<Integer, Node> nodes = new Int2ObjectOpenHashMap<Node>();
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
				if (!nodes.containsKey(tail))
					nodes.put(tail, new Node(tail, tail < ftn? true : false));

				if (!nodes.containsKey(head))
					nodes.put(head, new Node(head, head < ftn? true: false));

				// Construct new link and add to the list
				Link link;
				if (tail >= ftn && head >= ftn) {
					Float B = parse(cols[5]);
					Float power = parse(cols[6]);
					link = new TolledBPRLink(nodes.get(tail), nodes.get(head), capacity, length, fftime, B, power, toll);
				}
				else {
					link = new CentroidConnector(nodes.get(tail), nodes.get(head), capacity, length, fftime, toll);
				}
				g.add(link);
			}
			lf.close();
			g.setMD5(md.digest());
			return g;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

	}


	public static Graph readEnhancedGraph(File f, Integer thruNode) throws FileNotFoundException, IOException {
		try {
		MessageDigest md = MessageDigest.getInstance("MD5");
		String line;
		Graph g = new Graph();
		int numZones = 0;
		DigestInputStream dis = new DigestInputStream(new FileInputStream(f), md);
		BufferedReader lf = new BufferedReader(new InputStreamReader(dis));
		Map<Integer, Node> nodes = new Int2ObjectOpenHashMap<Node>();
		lf.readLine(); // skip header

		while (true) {
			line = lf.readLine();
			if (line == null || line.equals(""))
				break;

			String[] args = line.split(",");

			Integer nodeA = Integer.parseInt(args[1]);
			Integer nodeB = Integer.parseInt(args[2]);

			if (!nodes.containsKey(nodeA)) {
				if (nodeA < thruNode) {
					nodes.put(nodeA, new Node(nodeA, true));
					numZones++;
				} else
					nodes.put(nodeA, new Node(nodeA, false));
			}

			if (!nodes.containsKey(nodeB)) {
				if (nodeB < thruNode) {
					nodes.put(nodeB, new Node(nodeB, true));
					numZones++;
				} else
					nodes.put(nodeB, new Node(nodeB, false));

			}

			Float aCap = parse(args[3]);
			Float bCap = parse(args[4]);
			Float length = parse(args[5]);
			Float ffTimeA = parse(args[6]);
			Float ffTimeB = parse(args[7]);

			Float alpha = (parse(args[8]));
			Float epsilon = (parse(args[9]));
			Float sParA = (parse(args[10]));
			Float sParB = (parse(args[11]));
			Float satFlowA = (parse(args[12]));
			Float satFlowB = (parse(args[13]));

			Float caA = (parse(args[14]));
			Float cbA = (parse(args[15]));
			Float ccA = (parse(args[16]));
			Float cdA = (parse(args[17]));

			Float caB = (parse(args[18]));
			Float cbB = (parse(args[19]));
			Float ccB = (parse(args[20]));
			Float cdB = (parse(args[21]));

			Float minDel = (parse(args[22]));
			Float uParA = (parse(args[23]));
			Float uParB = (parse(args[24]));
			Float opCostA = (parse(args[25]));
			Float opCostB = (parse(args[26]));

			Map<Mode, Float> tollsA = new HashMap<Mode, Float>(4,1.0f);
			Map<Mode, Float> tollsB = new HashMap<Mode, Float>(4,1.0f);

			tollsA.put(Mode.SINGLE_OCC, (parse(args[27])));
			tollsB.put(Mode.SINGLE_OCC, (parse(args[28])));
			tollsA.put(Mode.HOV_2, (parse(args[29])));
			tollsB.put(Mode.HOV_2, (parse(args[30])));
			tollsA.put(Mode.MED_TRUCK, (parse(args[31])));
			tollsB.put(Mode.MED_TRUCK, (parse(args[32])));
			tollsA.put(Mode.HVY_TRUCK, (parse(args[33])));
			tollsB.put(Mode.HVY_TRUCK, (parse(args[34])));

			Map<Mode, Boolean> allowed = new HashMap<Mode, Boolean>(3,1.0f);
			Boolean a = !Boolean.parseBoolean(args[35].trim());
			allowed.put(Mode.SINGLE_OCC, a);
			allowed.put(Mode.HVY_TRUCK, a);
			allowed.put(Mode.MED_TRUCK, a);

			if (aCap > 0.0) {
				Link AB = null;
				if (satFlowA > 0) {
					AB = new TolledEnhancedLink(nodes.get(nodeA), nodes.get(nodeB), aCap, length, ffTimeA, alpha,
							epsilon, sParA, uParA, satFlowA, minDel, opCostA, caA, cbA, ccA, cdA, allowed, tollsA);
				} else if (aCap > 0.0) {
					AB = new CentroidConnector(nodes.get(nodeA), nodes.get(nodeB), aCap, length, ffTimeA,
							opCostA.floatValue());
				}
				g.add(AB);
			}

			if (bCap > 0.0) {
				Link BA = null;
				if (satFlowB > 0) {
					BA = new TolledEnhancedLink(nodes.get(nodeB), nodes.get(nodeA), bCap, length, ffTimeB, alpha,
							epsilon, sParB, uParB, satFlowB, minDel, opCostB, caB, cbB, ccB, cdB, allowed, tollsB);
				} else {
					BA = new CentroidConnector(nodes.get(nodeB), nodes.get(nodeA), bCap, length, ffTimeB,
							opCostB.floatValue());
				}
				g.add(BA);
			}
		}
		lf.close();
		g.setNumZones(numZones);
		g.setMD5(md.digest());
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
