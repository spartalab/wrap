package edu.utexas.wrap;

import java.io.File;
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
	static Integer iteration = 1;
	static Integer maxIterations = 1000;
	
	public static void main(String[] args) {
		// The very first line of code!
		Long start = System.currentTimeMillis();
		
		File links = new File(args[0]);
		File odMatrix = new File(args[1]);
		
		Network network;
		try {
			System.out.println("Reading network...");
			network = Network.fromFiles(links, odMatrix);
			System.out.println("Initializing optimizer...");
			Optimizer opt = new AlgorithmBOptimizer(network);
			
			System.out.println("Starting " + opt.toString() + "...");
			System.out.println();
			System.out.println("ITERATION #\tRELGAP\t        TSTT");
			System.out.println("--------------------------------------------------");
//			System.out.println(opt.getResults());
			while (!converged()) {
//				System.out.println("Iteration "+iteration+"\t");
				opt.optimize();
				System.out.println("Iteration "+iteration+"\t"+opt.getResults().get(0) + "\t" + opt.getResults().get(1));
				iteration ++;
			}
			Long end = System.currentTimeMillis();
			Double runtime = (end - start)/1000.0;
			System.out.println("Runtime "+runtime+" seconds");
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Boolean converged() {
		return iteration > maxIterations;
	}
}

