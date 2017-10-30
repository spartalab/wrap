package edu.utexas.wrap;

import java.io.File;
import java.util.List;

public class Network {

	private List<Link> links;
	private List<Node> nodes;
	private List<Origin> origins;
	
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
	
	
	public void readNodes(File f){
		//TODO
	}
	
	public void readLinks(File f){
		//TODO
	}
	
	public void readStaticOD(File f){
		//TODO
	}
	
	public float tstt(){
		float tstt = 0;
		for(Link l:links){
			tstt= tstt + l.getBprValue()*l.getCapacity();
		}
		return tstt;
	}
}
