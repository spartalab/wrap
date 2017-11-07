package edu.utexas.wrap;

import java.util.Set;

public class Bush extends Network{
	private Origin origin;
	
	public Bush(Set<Link> links, Origin origin) {
		super(links, origin);
		this.origin = origin;
	}
	
	
}
