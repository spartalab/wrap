package edu.utexas.wrap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bush extends Network{
	private Origin origin;
	
	public Bush(List<Link> links, List<Origin> origin) {
		super(links,origin);
		this.origin = origin.get(0);
	}
	
	public Bush(List<Link> links, Origin origin) {
		super(links, origin);
		this.origin = origin;
	}
	
	
}
