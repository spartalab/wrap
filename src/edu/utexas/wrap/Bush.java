package edu.utexas.wrap;

import java.util.List;
import java.util.Map;

public class Bush extends Network{
	private Origin origin;
	
	public Bush(Map<Integer,Node> nodes, List<Link> links, List<Origin> origin) {
		super(nodes,links,origin);
		this.origin = origin.get(0);
	}
}
