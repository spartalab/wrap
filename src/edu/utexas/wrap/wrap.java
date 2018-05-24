package edu.utexas.wrap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** wrap: an Algorithm B implementation
 * @author William E. Alexander
 * @author Rahul Patel
 * @author Adam Nodjomian
 * @author Prashanth Venkatraman
 * 
 * This project will implement Algorithm B, as presented 
 * by Dial (2006) in order to create a solver for the
 * Traffic Assignment Problem as defined by Beckmann et 
 * al. (1956). The aim for this project is to implement a
 * time- and space-efficient framework of Algorithm B
 * which can be studied, extended, and executed properly.
 * We also aim to provide quality documentation and testing
 * of our code, in order that this project will serve as a
 * good exercise in practicing proper software development
 * techniques. 
 * 
 * This project will be completed as part of the requirements
 * for the CE 392C Transportation Network Analysis course 
 * at the University of Texas at Austin during the Fall 2017
 * semester, with the potential for further development to
 * follow. 
 * 
 *            _______
 *          _|_______|_
 *         [ [_  _  _] ]
 *         !|| || || ||!
 *         !|| || || ||!
 *         !|| || || ||!
 *         [[]_[]_[]_[]]
 *         -------------
 *         |  |  .  |  |
 *         |  |..+..|  |
 *      _  |  / _?_ \  |  _
 *     [ ]_|_[]/   \[]_|_[ ]
 *     |     |(__/  )|     |
 *     |_____|_\___/_|_____|
 *    /| ..  |   ~   |  .. |\
 *   [___][ " ][ " ][ " ][___]
 *   |_______________________|
 *   |     |#|  |#|  |#|     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |     |#|  |#|  |#|     |
 *   |     | |  | |  | |     |
 *   |_____|#|__|#|__|#|_____|
 *   [_______________________]  
 *   |    HOOK 'EM HORNS!    |
 *   
 */
public class wrap{
	static Integer iteration = 1;
	static Integer maxIterations = 25;
	
	public static void main(String[] args) {
		// The very first line of code!
		//Long start = System.currentTimeMillis();
		
		File links = new File(args[0]);
		File odMatrix = new File(args[1]);
		
		Network network;
		try {
			//System.out.println("Reading network...");
			network = Network.fromFiles(links, odMatrix);
			
			//System.out.println("Initializing optimizer...");
			Optimizer opt = new AlgorithmBOptimizer(network);
			

			while (!converged()) {
				opt.optimize();
				//List<Double> results = opt.getResults();
				//System.out.println("Iteration "+iteration+"\t"+results.get(0) + "\t" + results.get(1)
				//		+"\t"+results.get(2));
				iteration ++;
			}
			Map<Integer, Node> nodes = network.nodes;
			Map<Integer,Map<Integer, Double>> baseSPTimes = new HashMap<Integer, Map<Integer, Double>>();
			Map<Integer,Map<Integer, Double>> baseSPLengths = new HashMap<Integer, Map<Integer, Double>>();
			Map<Integer,Map<Integer, Double>> baseSPCaps = new HashMap<Integer, Map<Integer, Double>>();
			Map<Link, Double> baseFlows = new HashMap<Link, Double>();
			for (Node n : nodes.values()) {
				Map<Integer, Double>[] res = timeDijkstras(n, nodes);
				baseSPTimes.put(n.getID(), res[0]);
				baseSPLengths.put(n.getID(), lengthDijkstras(n, nodes));
				baseSPCaps.put(n.getID(), res[1]);
			}
			for (Link l : network.links) {
				baseFlows.put(l, l.getFlow());
			}
			String legend = "Focus,Altered,Focus Indegree,Focus Outdegree,Focus cap,Focus b,Focus power,Focus FFT,Focus len,Focus v/c,";
			legend += "Adj Indegree,Adj Outdegree,Adj cap,Adj b,Adj power,Adj FFT,Adj len,Adj v/c,Adj %%,"
					+"SPLength F2A,SPLength A2F,SPTime F2A,SPTime A2F,SPCap F2A,SPCap A2F,"
					+"Focus %%Change";
			System.out.println(legend);
			//TODO: For %change level
			for (Double level = Double.valueOf(.5); level <= 2.01; level += 0.1) {
				for (Link l : network.getLinks()) {
					l.artDem = level;
					opt = new AlgorithmBOptimizer(network);
					iteration = 0;

					while (!converged()) {
						opt.optimize();
						iteration ++;
					}
					//TODO: print results
					for (Link ll : network.getLinks()) {
						String line = "";
						line += ll.toString()+",";
						line += l.toString()+",";
						
						line += ll.getTail().getIncomingLinks().size() + ",";
						line += ll.getHead().getOutgoingLinks().size() + ",";
						line += ll.getCapacity()+",";
						line += ll.getBValue() + ",";
						line += ll.getPower() + ",";
						line += ll.getFfTime()+ ",";
						line += ll.getLength()+ ",";
						line += baseFlows.get(ll)/ll.getCapacity()+",";
						
						line += l.getTail().getIncomingLinks().size() + ",";
						line += l.getHead().getOutgoingLinks().size() + ",";
						line += l.getCapacity()+",";
						line += l.getBValue() + ",";
						line += l.getPower() + ",";
						line += l.getFfTime()+ ",";
						line += l.getLength()+ ",";
						line += baseFlows.get(l)/l.getCapacity()+",";
						
						line += level + ",";
						line += baseSPLengths.get(ll.getHead().getID()).get(l.getTail().getID())+",";
						line += baseSPLengths.get(l.getHead().getID()).get(ll.getTail().getID())+",";
						line += baseSPTimes.get(ll.getHead().getID()).get(l.getTail().getID())+",";
						line += baseSPTimes.get(l.getHead().getID()).get(ll.getTail().getID())+",";
						line += baseSPCaps.get(ll.getHead().getID()).get(l.getTail().getID())+",";
						line += baseSPCaps.get(l.getHead().getID()).get(ll.getTail().getID())+",";
						
						line += ll.getFlow()/baseFlows.get(ll)+",";
						System.out.println(line);
					}
					l.artDem = 1.0;
				}
			}
			
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Boolean converged() {
		return iteration > maxIterations;
	}
	
	public static Map<Integer, Double>[] timeDijkstras(Node origin, Map<Integer,Node> nodes) throws Exception {
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		Set<Integer> eligible  = new HashSet<Integer>();
		
		Map<Integer, Double> nodeL = new HashMap<Integer, Double>();
		Map<Integer, Link> qShort = new HashMap<Integer, Link>();
		for (Integer l : nodes.keySet()) {
			nodeL.put(l, Double.POSITIVE_INFINITY);
			eligible.add(l);
		}
		nodeL.put(origin.getID(), new Double(0.0));
		
		// While not all nodes have been reached
		while (!eligible.isEmpty()) {
			
			// Find eligible node of minimal nodeL
			Node tail = null;
			for (Integer nodeID : eligible) {
				Node node = nodes.get(nodeID);
				//Calculating shortest paths
					if ( tail == null || nodeL.get(node.getID()) < nodeL.get(tail.getID()) ) 
						tail = node;
			}			
			
			// Finalize node by adding to finalized
			// And remove from eligible
			eligible.remove(tail.getID());

			// Update labels and backnodes for links leaving node i
			for (Link link : tail.getOutgoingLinks()) {
				Node head = link.getHead();

				//Shortest paths search
				// nodeL(j) = min( nodeL(j), nodeL(i)+c(ij) )
				Double Lj    = nodeL.get(head.getID());
				Double Licij = nodeL.get(tail.getID())+link.getTravelTime();
				if (Licij < Lj) {
					nodeL.put(head.getID(), Licij);
					qShort.put(head.getID(), link);
				}
				
			}
		}
		Map<Integer, Double> mincaps = new HashMap<Integer, Double>();
		
		for (Node node : nodes.values()) {
			Double min = Double.MAX_VALUE;
			Link q = qShort.get(node);
			while (q != null) {
				min = Double.min(min, q.getCapacity());
				q = qShort.get(q.getTail().getID());
			}
			mincaps.put(node.getID(), min);
		}
		
		Map[] res = {nodeL, mincaps};
		return res;
	}
	
	public static Map<Integer, Double> lengthDijkstras(Node origin, Map<Integer,Node> nodes) throws Exception {
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		Set<Integer> eligible  = new HashSet<Integer>();
		
		Map<Integer, Double> nodeL = new HashMap<Integer, Double>();
		Map<Integer, Link> qShort = new HashMap<Integer, Link>();
		for (Integer l : nodes.keySet()) {
			nodeL.put(l, Double.POSITIVE_INFINITY);
			eligible.add(l);
		}
		nodeL.put(origin.getID(), new Double(0.0));
		
		// While not all nodes have been reached
		while (!eligible.isEmpty()) {
			
			// Find eligible node of minimal nodeL
			Node tail = null;
			for (Integer nodeID : eligible) {
				Node node = nodes.get(nodeID);
				//Calculating shortest paths
					if ( tail == null || nodeL.get(node.getID()) < nodeL.get(tail.getID()) ) 
						tail = node;
			}			
			
			// Finalize node by adding to finalized
			// And remove from eligible
			eligible.remove(tail.getID());

			// Update labels and backnodes for links leaving node i
			for (Link link : tail.getOutgoingLinks()) {
				Node head = link.getHead();

				//Shortest paths search
				// nodeL(j) = min( nodeL(j), nodeL(i)+c(ij) )
				Double Lj    = nodeL.get(head.getID());
				Double Licij = nodeL.get(tail.getID())+link.getLength();
				if (Licij < Lj) {
					nodeL.put(head.getID(), Licij);
					qShort.put(head.getID(), link);
				}
				
			}
		}
		return nodeL;
	}
	
	public static Integer getK() {
		return 0;
	}
}

