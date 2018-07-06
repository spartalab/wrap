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
	static Integer maxIterations = 160;
	
	public static void main(String[] args) {
		// The very first line of code!
		Long start = System.currentTimeMillis();
		
		File links = new File(args[0]);
		File odMatrix = new File(args[1]);
		File votBreakdown = new File(args[2]);
		
		Network network;
		try {
			System.out.println("Reading network...");
			network = Network.fromFiles(links, odMatrix, votBreakdown);
			System.out.println("Initializing optimizer...");
			Optimizer opt = new AlgorithmBOptimizer(network);
			
			System.out.println("Starting " + opt.toString() + "...");
			System.out.println();
			System.out.println("ITERATION #\tAEC\t\t\tTSTT\t\t\tBeckmann\t\tRelative Gap");
			System.out.println("-------------------------------------------------------------------------------------------------------------");
			do {
				
				opt.optimize();
				System.out.println("Iteration "+iteration+"\t"+network.toString());
				iteration ++;
			} while (!converged(network));
			Long end = System.currentTimeMillis();
			Double runtime = (end - start)/1000.0;
			System.out.println("Runtime "+runtime+" seconds");
			
			//System.setOut(new PrintStream("VOTflow.csv"));
//			System.out.println("\r\n\r\nLink,VOT0.2 Flow,VOT0.5 Flow,VOT0.8 Flow");
//			for (Link l : network.links) {
//				Double vot0 = 0.0;
//				Double vot1 = 0.0;
//				Double vot2 = 0.0;
//				for (Origin o : network.origins) {
//					for (Bush b : o.getBushes()) {
//							vot0 += b.getBushFlow(l);
//						
//						//System.out.println(l+"\t"+b.getVOT()+"\t"+b.getBushFlow(l));
//					}
//				}
//				System.out.println(l+","+vot0+","+vot1+","+vot2);
//			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Boolean converged(Network network) {
		try {
			return iteration > maxIterations || network.relativeGap() < Math.pow(10, -6);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return iteration > maxIterations;
		}
	}
}

