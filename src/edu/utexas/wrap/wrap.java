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

	/**
	 * Maximum number of optimization iterations
	 */
	static Integer maxIterations = 150;
	/**
	 * Power to which the relative gap limit is raised, 
	 * i.e. the algorithm terminates when relativeGap is
	 * less than 10^relativeGapExp
	 */
	static Integer relativeGapExp = -6;
	/**
	 * Maximum number of decimal places past zero that 
	 * links should care about for flow values. Default
	 * rounding mode is RoundingMode.HALF_EVEN
	 */
	static Integer decimalPlaces = 16;
	/**
	 * Whether flows should be printed once converged
	 */
	static Boolean printFlows = false;
	
	/**
	 * @param network whose metrics are measured in the convergence criteria
	 * @return whether or not a convergence criterion has been met
	 */
	private static Boolean converged(Network network) {
		try {
			return iteration > maxIterations || network.relativeGap() < Math.pow(10, relativeGapExp);
		} catch (Exception e) {
			e.printStackTrace();
			return iteration > maxIterations;
		}
	}
	
	private static Integer iteration = 1;
	
	public static void main(String[] args) {
		
		File links = new File(args[0]);
		File odMatrix = new File(args[1]);
		File votBreakdown = new File(args[2]);
		
		try {
			System.out.println("Reading network...");
			Network network = Network.fromFiles(links, odMatrix, votBreakdown);
			System.out.println("Initializing optimizer...");
			Optimizer opt = new AlgorithmBOptimizer(network);
			
			System.out.println("Starting " + opt.toString() + "...");
			System.out.println();
			System.out.println("ITERATION #\tAEC\t\t\tTSTT\t\t\tBeckmann\t\tRelative Gap");
			System.out.println("-------------------------------------------------------------------------------------------------------------");
			Long start = System.currentTimeMillis();
			do {
				System.out.print("Iteration "+iteration);
				System.out.flush();
				opt.optimize();
				System.out.print("\t"+network.toString()+"\r");
				System.out.flush();
				iteration++;
			} while (!converged(network));
			Long end = System.currentTimeMillis();
			Double runtime = (end - start)/1000.0;
			System.out.println("Runtime "+runtime+" seconds");
			
			//System.setOut(new PrintStream("VOTflow.csv"));
			if (printFlows) network.printFlows(System.out);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

