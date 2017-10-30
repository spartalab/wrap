package edu.utexas.wrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class NetworkConstructor {

	public static Network construct(File nodes, File links, File odMatrix) throws FileNotFoundException {
		// TODO Auto-generated method stub
		List<Node> nodeList = new ArrayList<Node>();
		List<Link> linkList = new ArrayList<Link>();
		List<Origin> origins = new ArrayList<Origin>();
		
		FileReader nf = new FileReader(nodes);
		FileReader lf = new FileReader(links);
		FileReader of = new FileReader(odMatrix);
		
		//TODO: Read files, create objects from file
		
		return new Network(nodeList, linkList, origins);
		
	}

}
