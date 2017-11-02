package edu.utexas.wrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Network {

	private List<Link> links;
	private List<Node> nodes;
	private List<Origin> origins;
	private Scanner in;
	
	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(List<Link> links) {
		this.links = links;
	}
	public List<Node> getNodes() {
		return nodes;
	}
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	public List<Origin> getOrigins() {
		return origins;
	}
	public void setOrigins(List<Origin> origins) {
		this.origins = origins;
	}
	
	
	public void readNodes(File f) throws FileNotFoundException{
		in = new Scanner(f);
		nodes = new ArrayList<Node>();
		in.nextLine();
		while(in.hasNext()){
			int id = in.nextInt();
			double latitude = in.nextDouble();
			double longitude = in.nextDouble();
			in.next();
			
			Node n = new Node(id, latitude, longitude);
			nodes.add(n);
		}
	}
	
	public void printNodes(){
		for(Node n:nodes){
			System.out.println("Node " + n.getId() + " latitude: " + n.getLatitude() + " longitude: " + n.getLongitude());
		}
	}
	
	public void readLinks(File f){
		//TODO
	}
	
	public void readStaticOD(File f){
		
	}
	
	public float tstt(){
		float tstt = 0;
		for(Link l:links){
			tstt= tstt + l.getBprValue()*l.getCapacity();
		}
		return tstt;
	}
}
