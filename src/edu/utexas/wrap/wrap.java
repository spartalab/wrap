package edu.utexas.wrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	static Integer maxIterations = 250;
	
	public static void main(String[] args) {
		// The very first line of code!
		//Long start = System.currentTimeMillis();
		
		File links = new File(args[0]);
		File odMatrix = new File(args[1]);
		
		Network network = null;
		Float change = new Float(0.25);
		try {
			System.out.println("Reading network...");
			network = Network.fromFiles(links, odMatrix);
			
		} catch (Exception e){
			System.exit(-1);
		}
		String header = change+",";
		Map<Link, String> flows = new HashMap<Link, String>();
		for (Link d : network.links) {
			flows.put(d,"");
		}
		for (Link d : network.links) try {
			for (Origin m : network.getOrigins()) {
				if (m.equals(d.getTail())) {
					d.k = m.getDemand(d.getHead().getID()) * change;
					System.out.print(".");
					break;
				}
			}
			//System.out.println("Initializing optimizer...");
			Optimizer opt = new AlgorithmBOptimizer(network);
			
			//System.out.println("Starting " + opt.toString() + "...");
			//System.out.println();
			//System.out.println("ITERATION #\tAEC\t\t\tTSTT\t\t\t");
			//System.out.println("--------------------------------------------------");
//			System.out.println(opt.getResults());
			while (!converged()) {
//				System.out.println("Iteration "+iteration+"\t");
				opt.optimize();
				List<Double> results = opt.getResults();
			//	System.out.println("Iteration "+iteration+"\t"+results.get(0) + "\t" + results.get(1)
				//		+"\t"+results.get(2));
				iteration ++;
				if (results.get(0) < Math.pow(10.0, -6)) break;
			}
			//Long end = System.currentTimeMillis();
			//Double runtime = (end - start)/1000.0;
			//System.out.println("Runtime "+runtime+" seconds");
			for (Link l : network.links) {
				String q = flows.get(l);
				q += String.format("%f,", (l.getFlow()+l.k));
				flows.put(l, q);
			}
			d.k = 0.0;
			header += d.toString()+",";
//			PrintStream ps = new PrintStream(new FileOutputStream(new File("From"+d.getTail().getID()+"To"+d.getHead().getID()+"By"+change+".txt")));
//			for (Link link : network.links) {
//				ps.println(String.format(link.toString()+" flow:\t%f", link.getFlow()));
//			}
//			ps.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	try {
		PrintStream ps = new PrintStream(new FileOutputStream(new File("FlowResults.csv")));
		ps.println(header);
		for (Link l : flows.keySet()) {
			ps.println(l.toString() + ","+flows.get(l));
		}
		ps.close();
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	}

	private static Boolean converged() {
		return iteration > maxIterations;
	}
}

