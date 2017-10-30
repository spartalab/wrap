package edu.utexas.wrap;

import java.util.List;

public class Bush extends Network{
	private Origin origin;
	
	public Bush(List<Node> nodes, List<Link> links, List<Origin> origin) {
		super(nodes,links,origin);
		this.origin = origin.get(0);
	}
}
