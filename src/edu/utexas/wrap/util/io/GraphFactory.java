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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.PressureFunction;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.CentroidConnector;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.Ring;
import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TolledBPRLink;
import edu.utexas.wrap.net.TolledEnhancedLink;
import edu.utexas.wrap.net.TurningMovement;

/**
 * This class provides static methods to read Graph information
 */
public class GraphFactory {
	/**
	 * This method produces a Graph object after reading it in from the TNTP Format
	 * More information about the TNTP Format can be found here:
	 * https://github.com/bstabler/TransportationNetworks
	 * @param linkFile File with the network information
	 * @param pressureFunction 
	 * @return Graph object representation of input network
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void readTNTPLinks(
			
			File linkFile, 
			Graph g, 
			ToDoubleFunction<Link> tollingPolicy,
			PressureFunction pressureFunction, 
			Map<Integer, Float> greenShares,
			Map<Integer, Float> cycleLengths,
			Map<Integer,Map<Integer, Integer>> signalGroups,
			Map<Integer,Map<Integer, Integer>> ringMap,
			Map<Integer,Map<Integer,Double>> turningMovementShares
			) throws FileNotFoundException, IOException {

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			DigestInputStream dis = new DigestInputStream(
					new FileInputStream(linkFile), md);

			String line;
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

			int numLinks = 0;
			AtomicInteger numTurningMovements = new AtomicInteger(0);
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
					nodeIDs.put(tail, 
						greenShares == null ?	
							new Node(tail, nodeIdx.getAndIncrement(),g.getZone(tail)) :
							new SignalizedNode(
									tail, 
									nodeIdx.getAndIncrement(),
									g.getZone(tail))
							
							);

				}

				if (!nodeIDs.containsKey(head)) {
					nodeIDs.put(head, 
						greenShares == null ?
							new Node(head, nodeIdx.getAndIncrement(),g.getZone(head)) :
							new SignalizedNode(
									head, 
									nodeIdx.getAndIncrement(),
									g.getZone(head))
							
							);
				}
				
				// Construct new link and add to the list
				Link link;
				if (tail >= ftn && head >= ftn) {
					Float B = parse(cols[5]);
					Float power = parse(cols[6]);
					link = new TolledBPRLink(
							nodeIDs.get(tail), 
							nodeIDs.get(head), 
							capacity, length, fftime, B, power, 
							toll, numLinks++, tollingPolicy, pressureFunction);
				}
				else {
					link = new CentroidConnector(
							nodeIDs.get(tail), 
							nodeIDs.get(head), 
							capacity, length, fftime, toll,numLinks++, 
							tollingPolicy);
				}
				
				g.add(link);
			}
			lf.close();
			g.setMD5(md.digest());
			g.complete();
			
			Map<Link,Collection<TurningMovement>> mvmts = g.getLinks().stream()
			.collect(
				Collectors.toMap(
					Function.identity(),
					inLink -> inLink.getHead() instanceof SignalizedNode?
						// if it's a signalized node
						Stream.of(inLink.getHead().forwardStar())
						.filter(
								//disallowing u-turns
							outlink -> outlink.getHead() != inLink.getTail())
						.map(
							outLink -> new TurningMovement(
									inLink,outLink,
									numTurningMovements.getAndIncrement()))
						.collect(Collectors.toSet())
						
						: //else
							
						Collections.emptySet()
					)
					);
			Map<TurningMovement,Double> ringShares = getRingShares(
					mvmts.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()),
					turningMovementShares);
			Map<TurningMovement,Ring> rings = getRings(mvmts,ringMap,ringShares);
			g.setSignalTimings(greenShares, 
					cycleLengths, 
					signalGroups, 
					mvmts, rings);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	private static Map<TurningMovement, Double> getRingShares(
			Set<TurningMovement> movements,
			Map<Integer, Map<Integer, Double>> turningMovementShares) {
		// TODO Auto-generated method stub
		return movements.stream().collect(
				Collectors.toMap(
						Function.identity(), 
						tm -> turningMovementShares.get(tm.getTail().hashCode())
								.get(tm.getHead().hashCode())));
	}

	private static Map<TurningMovement, Ring> getRings(Map<Link, Collection<TurningMovement>> mvmts,
			Map<Integer, Map<Integer, Integer>> ringMap,Map<TurningMovement,Double> ringShares) {
		// TODO Auto-generated method stub
		Map<SignalizedNode,Map<Integer,Ring>> nodeRings = new HashMap<SignalizedNode,Map<Integer,Ring>>();
		return mvmts.values().stream().flatMap(Collection::stream)
		.collect(
			Collectors.toMap(
					Function.identity(),
					tm -> {
						SignalizedNode intx = (SignalizedNode) tm.getTail().getHead();
						nodeRings.putIfAbsent(intx, new HashMap<Integer,Ring>());
						
						Map<Integer,Ring> rings = nodeRings.get(intx);
						int ringID = ringMap.get(tm.getTail().hashCode())
								.get(tm.getHead().hashCode());
						rings.putIfAbsent(ringID, new Ring(ringShares));
						return rings.get(ringID);
					}
					));
	}

	/**
	 * This method produces a Graph object after reading it from a CSV file
	 * The file contains information about the CONAC parameters and uses EnhancedLinks
	 * to build the graph
	 * @param f File with the network information
	 * @param pressureFunction 
	 * @return Graph object representation of input network
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void readConicLinks(File f, Graph g, ToDoubleFunction<Link> tollingPolicy, PressureFunction pressureFunction) throws FileNotFoundException, IOException {
		try {
		MessageDigest md = MessageDigest.getInstance("MD5");
		AtomicInteger numNodes = new AtomicInteger(0), numLinks = new AtomicInteger(0);
		DigestInputStream dis = new DigestInputStream(new FileInputStream(f), md);
		BufferedReader lf = new BufferedReader(new InputStreamReader(dis));
		Map<Integer, Node> nodes = new ConcurrentHashMap<Integer,Node>();
		
		lf.lines().parallel()
		.filter(line -> line != null && !line.startsWith("I") && !line.equals(""))
		.forEach(line -> {

			String[] args = line.split(",");
			Integer linkID = Integer.parseInt(args[0]);
			Integer nodeA = Integer.parseInt(args[1]);
			Integer nodeB = Integer.parseInt(args[2]);

			if (!nodes.containsKey(nodeA)) {
//				if (nodeA < thruNode) {
//					newZone(g, numZones, numNodes, nodes, nodeA, classes.get(nodeA));
//				} else
					nodes.put(nodeA, new Node(nodeA, numNodes.getAndIncrement(),g.getZone(nodeA)));
			}

			if (!nodes.containsKey(nodeB)) {
//				if (nodeB < thruNode) {
//					newZone(g, numZones, numNodes, nodes, nodeB, classes.get(nodeB));
//				} else
					nodes.put(nodeB, new Node(nodeB, numNodes.getAndIncrement(),g.getZone(nodeB)));

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
				if (g.getZone(nodeA) == null && g.getZone(nodeB) == null) {
					AB = new TolledEnhancedLink(nodes.get(nodeA), nodes.get(nodeB), aCap, length, ffTimeA, alpha,
							epsilon, sParA, uParA, satFlowA, minDel, opCostA, caA, cbA, ccA, cdA, tollA, allowed,linkID, tollingPolicy);
				} else {
					AB = new CentroidConnector(nodes.get(nodeA), nodes.get(nodeB), aCap, length, ffTimeA,
							opCostA.floatValue(), linkID, tollingPolicy);
				}
				numLinks.incrementAndGet();
				g.add(AB);

			if (Boolean.parseBoolean(args[36].trim())) {
				Link BA = null;
				if (g.getZone(nodeA) == null && g.getZone(nodeB) == null) {
					BA = new TolledEnhancedLink(nodes.get(nodeB), nodes.get(nodeA), bCap, length, ffTimeB, alpha,
							epsilon, sParB, uParB, satFlowB, minDel, opCostB, caB, cbB, ccB, cdB, tollB, allowed, -linkID, tollingPolicy);
				} else {
					BA = new CentroidConnector(nodes.get(nodeB), nodes.get(nodeA), bCap, length, ffTimeB,
							opCostB.floatValue(), -linkID, tollingPolicy);
				}
				numLinks.incrementAndGet();
				g.add(BA);
			}
		});
		lf.close();
		g.setMD5(md.digest());
		g.complete();
		
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
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
