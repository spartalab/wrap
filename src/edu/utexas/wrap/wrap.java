package edu.utexas.wrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import edu.utexas.wrap.assignment.Optimizer;
import edu.utexas.wrap.assignment.bush.AlgorithmBOptimizer;
import edu.utexas.wrap.assignment.bush.BushLoader;
import edu.utexas.wrap.assignment.bush.BushOrigin;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.GraphFactory;
import edu.utexas.wrap.util.OriginFactory;

/** wrap: an Algorithm B implementation
 * @author William E. Alexander
 * @author Rahul Patel
 * @author Adam Nodjomian
 * @author Prashanth Venkatraman
 * 
 * 
 * This project began as part of the requirements for the 
 * CE 392C Transportation Network Analysis course at 
 * The University of Texas at Austin during the Fall 2017
 * semester, with further development thereafter.
 * 
 * This project implements Algorithm B, as presented 
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
	static Boolean printFlows = true;

	public static void main(String[] args) {
		Graph g = null; 
		Set<BushOrigin> o = null;
		try {
			//TODO rewrite argument parsing for more flexibility
			if (args.length < 3) {
				printHelp();
			}
			if ((args[0].trim().equals("-e") || args[0].trim().equals("--enhanced"))) {
				File links		= new File(args[1]);
				File odMatrix	= new File(args[2]);
				Integer firstThruNode = Integer.parseInt(args[3]);
				
				System.out.println("Reading network...");
				g = GraphFactory.readEnhancedGraph(links, firstThruNode);
				
				BushLoader dl = new BushLoader(g);
				System.out.println("Reading trips...");				
				OriginFactory.readEnhancedTrips(odMatrix, g, dl);
				o = dl.conclude();
			}
			else if (!( args[0].trim().equals("-v") || args[0].trim().equals("--variable") )) {
				File links 		= new File(args[0]);
				File odMatrix	= new File(args[1]);
				File votFile 	= new File(args[2]);
				
				System.out.println("Reading network...");
				g = GraphFactory.readTNTPGraph(links);
				 
				
				System.out.println("Reading trips...");
				BushLoader dl = new BushLoader(g);
				OriginFactory.readTNTPUniformVOTtrips(votFile, odMatrix, g, dl);
				o = dl.conclude();
			}
			else {
				//TODO handle variable VOT usage
				System.err.println("Not yet implemented");
			}
 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		Optimizer opt = new AlgorithmBOptimizer(g, o);

		System.out.println("Starting " + opt.toString() + "...");
		opt.optimize();

		try {
			System.setOut(new PrintStream("VOTflow.txt"));
			if (printFlows) g.printFlows(System.out);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void printHelp() {
		System.err.println("Uniform VOT usage: wrap network.tntp odMatrix.tntp votSplit.tntp");
		System.err.println("Variable VOT usage: wrap {-v || --variable} network.tntp odMatrix.tntp");
		System.err.println("Enahnced usage: wrap {-e || --enhanced} network.csv odMatrix.csv firstThruNode");
		System.exit(3);
	}
}

