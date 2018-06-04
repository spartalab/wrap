package edu.utexas.wrap;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
			System.setOut(new PrintStream("results.csv"));
			//System.out.println("Initializing optimizer...");
			Optimizer opt = new AlgorithmBOptimizer(network);
			
			//Get base (un-altered) equilibrium
			while (!converged(network)) {
				opt.optimize();
				iteration ++;
			}
			
			//Retrieve base case stats: SP time, SP length, SP Cap, and all flows
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
			
			//Print legend
			String legend = "Focus,Altered,Focus Indegree,Focus Outdegree,Focus cap,Focus b,Focus power,Focus FFT,Focus len,Focus v/c,";
			legend += "Adj Indegree,Adj Outdegree,Adj cap,Adj b,Adj power,Adj FFT,Adj len,Adj v/c,Adj %%,"
					+"SPLength F2A,SPLength A2F,SPTime F2A,SPTime A2F,SPCap F2A,SPCap A2F,"
					+"TimeMean F2A,TimeVar F2A,DistMean F2A,DistVar F2A,TimeMean A2F,TimeVar A2F,DistMean A2F,DistVar A2F,Focus %%Change";
			System.out.println(legend);
			
			//For each % change level
			for (Double level = Double.valueOf(.5); level <= 2.01; level += 0.1) {
				//For every link that could be congested
				for (Link a : network.getLinks()) {
					//Decrease the capacity by a factor of "level"
					a.artDem = level;
					//Get a new equilibrium
					opt = new AlgorithmBOptimizer(network);
					iteration = 0;
					while (!converged(network)) {
						opt.optimize();
						iteration ++;
					}
					
					Map<Integer, Map<Integer, Double>> kDistMean = new HashMap<Integer, Map<Integer, Double>>();
					Map<Integer, Map<Integer, Double>> kDistVar = new HashMap<Integer, Map<Integer, Double>>();
					Map<Integer, Map<Integer, Double>> kTimeMean = new HashMap<Integer, Map<Integer, Double>>();
					Map<Integer, Map<Integer, Double>> kTimeVar = new HashMap<Integer, Map<Integer, Double>>();
					
					
					for (Node n : nodes.values()) {
						Map<Integer, List<Path>> kshorts = getK(5,n,nodes, network.getGraph());
						Map<Integer, Double> distMean = new HashMap<Integer, Double>();
						Map<Integer, Double> distVar = new HashMap<Integer, Double>();
						Map<Integer, Double> timeMean = new HashMap<Integer, Double>();
						Map<Integer, Double> timeVar = new HashMap<Integer, Double>();
						
						for (Node m : nodes.values()) {
							//if (m.equals(n)) continue;
							List<Double> times = new LinkedList<Double>();
							List<Double> lens = new LinkedList<Double>();
							List<Path> ks = kshorts.get(m.getID());
							for (Path p : ks) {
								times.add(p.getPrice());
								lens.add(p.getLength());
							}
							
							distMean.put(m.getID(), getMean(lens));
							distVar.put(m.getID(), getVarianceInv(lens));
							timeMean.put(m.getID(), getMean(times));
							timeVar.put(m.getID(), getVarianceInv(times));
							
						}
						
						kDistMean.put(n.getID(), distMean);
						kDistVar.put(n.getID(), distVar);
						kTimeMean.put(n.getID(), timeMean);
						kTimeVar.put(n.getID(), timeVar);
						//Get mean of k shortest dists
						//Get var of k shortest dists
						//get mean of k shortest lengths
						//get var of k shortest lengths
					}

					for (Link f : network.getLinks()) {
						String line = "";
						line += f.toString()+",";
						line += a.toString()+",";
						
						line += f.getTail().getIncomingLinks().size() + ",";
						line += f.getHead().getOutgoingLinks().size() + ",";
						line += f.getCapacity()+",";
						line += f.getBValue() + ",";
						line += f.getPower() + ",";
						line += f.getFfTime()+ ",";
						line += f.getLength()+ ",";
						line += baseFlows.get(f)/f.getCapacity()+",";
						
						line += a.getTail().getIncomingLinks().size() + ",";
						line += a.getHead().getOutgoingLinks().size() + ",";
						line += a.getCapacity()+",";
						line += a.getBValue() + ",";
						line += a.getPower() + ",";
						line += a.getFfTime()+ ",";
						line += a.getLength()+ ",";
						line += baseFlows.get(a)/a.getCapacity()+",";
						
						line += level + ",";
						line += baseSPLengths.get(f.getHead().getID()).get(a.getTail().getID())+",";
						line += baseSPLengths.get(a.getHead().getID()).get(f.getTail().getID())+",";
						line += baseSPTimes.get(f.getHead().getID()).get(a.getTail().getID())+",";
						line += baseSPTimes.get(a.getHead().getID()).get(f.getTail().getID())+",";
						line += baseSPCaps.get(f.getHead().getID()).get(a.getTail().getID())+",";
						line += baseSPCaps.get(a.getHead().getID()).get(f.getTail().getID())+",";
						
						line += kTimeMean.get(f.getHead().getID()).get(a.getTail().getID())+",";
						line += kTimeVar.get(f.getHead().getID()).get(a.getTail().getID())+",";
						line += kDistMean.get(f.getHead().getID()).get(a.getTail().getID())+",";
						line += kDistVar.get(f.getHead().getID()).get(a.getTail().getID())+",";
						
						line += kTimeMean.get(a.getHead().getID()).get(f.getTail().getID())+",";
						line += kTimeVar.get(a.getHead().getID()).get(f.getTail().getID())+",";
						line += kDistMean.get(a.getHead().getID()).get(f.getTail().getID())+",";
						line += kDistVar.get(a.getHead().getID()).get(f.getTail().getID())+",";
						
						line += f.getFlow()/baseFlows.get(f)+",";
						System.out.println(line);
						
					}
					//System.out.println(network.relativeGap());
					a.artDem = 1.0;
				}
			}
			
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Boolean converged(Network network) throws Exception {
		return iteration > 40;
		//return network.relativeGap() < Math.pow(10.0, -6.0);
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
			Link q = qShort.get(node.getID());
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
	
	public static Map<Integer, List<Path>> getK(int k, Node n, Map<Integer, Node> nodes, Graph g) {
		Map<Integer, List<Path>> ret = new HashMap<Integer, List<Path>>();
		for (Node m : nodes.values()) {
			try {
				ret.put(m.getID(), SPAlgorithms.kShortestPaths(g, n, m, k));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				ret.put(m.getID(),new LinkedList<Path>());
				e.printStackTrace();
			}
		}
		return ret;
	}
	
    static double getMean(List<Double> data) {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum/data.size();
    }

    static double getVarianceInv(List<Double> data) {
    	//if (data.size() == 1) return 0.0;
        double mean = getMean(data);
        double temp = 0;
        for(double a : data)
            temp += (a-mean)*(a-mean);
        if (temp == 0.0) return Double.MAX_VALUE; 
        return (data.size()-1)/temp;
    }
}

