package edu.utexas.wrap;

import java.util.List;

public class Network {

	private List<Link> links;
	private List<Node> nodes;
	private List<Origin> origins;
	
	
	public Network(List<Node> nodes, List<Link> links, List<Origin> origins) {
		setNodes(nodes);
		setLinks(links);
		setOrigins(origins);
	}
	
	public List<Link> getLinks() {
		return links;
	}
	private void setLinks(List<Link> links) {
		this.links = links;
	}
	public List<Node> getNodes() {
		return nodes;
	}
	private void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	public List<Origin> getOrigins() {
		return origins;
	}
	private void setOrigins(List<Origin> origins) {
		this.origins = origins;
	}
	
	
	public Double tstt(){
		Double tstt = 0.0;
		
		for(Link l:links){
			tstt += l.getBprValue()*l.getCapacity();
		}
		return tstt;
	}
}
