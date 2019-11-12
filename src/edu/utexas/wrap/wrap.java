package edu.utexas.wrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import edu.utexas.wrap.assignment.bush.AlgorithmBOptimizer;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushOptimizer;
import edu.utexas.wrap.assignment.bush.BushOriginFactory;
import edu.utexas.wrap.assignment.bush.BushOrigin;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.io.GraphFactory;
import edu.utexas.wrap.util.io.OriginFactory;

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
		Set<BushOrigin> origins = null;
		int innerIters = 1;
		try {
			//TODO rewrite argument parsing for more flexibility
			if (args.length < 3) {
				printHelp();
			}
//			if (args[args.length-1].trim().startsWith("-") && args[args.length-1].trim().contains("d")) Bush.cachingAllowed = false;
			if (args[args.length-1].trim().startsWith("-") && args[args.length-1].trim().contains("p")) BushOptimizer.printProgress = false;
			
			if ((args[0].trim().equals("-e") || args[0].trim().equals("--enhanced"))) {
				File links		= new File(args[1]);
				File odMatrix	= new File(args[2]);
				Integer firstThruNode = Integer.parseInt(args[3]);
				
				System.out.print("Reading network... ");
				g = GraphFactory.readEnhancedGraph(links, firstThruNode);
				StringBuilder sb = new StringBuilder("MD5 hash: ");
				for (byte b : g.getMD5()) {
					sb.append(String.format("%02X", b));
				}
				System.out.println(sb);
				
				BushOriginFactory dl = new BushOriginFactory(g);
				System.out.println("Reading trips...");				
				OriginFactory.readEnhancedTrips(odMatrix, g, dl);
				origins = dl.finishAll();
			} 
			else if (!( args[0].trim().equals("-v") || args[0].trim().equals("--variable") )) {
				File links 		= new File(args[0]);
				File odMatrix	= new File(args[1]);
				File votFile 	= new File(args[2]);
//				try {
//					innerIters = Integer.parseInt(args[3]);
//				} catch (Exception e) {}
				
				System.out.print("Reading network... ");
				g = GraphFactory.readTNTPGraph(links);
				StringBuilder sb = new StringBuilder("MD5 hash: ");
				for (byte b : g.getMD5()) {
					sb.append(String.format("%02X", b));
				}
				System.out.println(sb);
				
				System.out.println("Reading trips...");
				BushOriginFactory dl = new BushOriginFactory(g);
				OriginFactory.readTNTPUniformVOTtrips(votFile, odMatrix, g, dl);
				origins = dl.finishAll();
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
		
		BushOptimizer opt = new AlgorithmBOptimizer(g, origins);
		opt.setInnerIters(innerIters);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				opt.shuttingDown = true;
			}
		});
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
		System.err.println("Uniform VOT usage: wrap network.tntp odMatrix.tntp votSplit.tntp [-d]");
		System.err.println("Variable VOT usage: wrap {-v || --variable} network.tntp odMatrix.tntp [-d]");
		System.err.println("Enahnced usage: wrap {-e || --enhanced} network.csv odMatrix.csv firstThruNode [-d]");
		System.exit(3);
	}
	
	
}

