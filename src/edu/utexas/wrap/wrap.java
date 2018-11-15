package edu.utexas.wrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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

	/**
	 * Whether flows should be printed once converged
	 */
	static Boolean printFlows = false;

	public static void main(String[] args) {


		Network network;
		try {
			NetworkFactory n = new NetworkFactory();
			if (args.length < 3) {
				System.err.println("Uniform VOT usage: wrap network.tntp odMatrix.tntp votSplit.tntp");
				System.err.println("Variable VOT usage: wrap {-v || --variable} network.tntp odMatrix.tntp");
				System.err.println("Enahnced usage: wrap {-e || --enhanced} network.csv odMatrix.csv");
				System.exit(3);
			}
			if ((args[0].trim().equals("-e") || args[0].trim().equals("--enhanced"))) {
				File links		= new File(args[1]);
				File odMatrix	= new File(args[2]);
				Integer firstThruNode = Integer.parseInt(args[3]);
				
				System.out.println("Reading network...");
				n.readEnhancedGraph(links, firstThruNode);
				
				System.out.println("Reading trips...");				
				n.readEnhancedTrips(odMatrix);
			}
			else if (!( args[0].trim().equals("-v") || args[0].trim().equals("--variable") )) {
				File links 		= new File(args[0]);
				File odMatrix	= new File(args[1]);
				File votFile 	= new File(args[2]);
				
				System.out.println("Reading network...");
				n.readTNTPGraph(links);
				
				System.out.println("Reading trips...");
				n.readTNTPUniformVOTtrips(votFile, odMatrix);
			}
			else {
				//TODO handle variable VOT usage
				System.err.println("Not yet implemented");
			}
			network = n.getNetwork();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("Initializing optimizer...");
		Optimizer opt = new AlgorithmBOptimizer(network);

		System.out.println("Starting " + opt.toString() + "...");
		opt.optimize();

		//System.setOut(new PrintStream("VOTflow.csv"));
		if (printFlows) network.printFlows(System.out);

		for(Link l: network.getGraph().getLinks()) {
			l.removeTable();
		}

		for(Link l: network.getGraph().getLinks()) {
			l.removeTable();
		}
	}
}

