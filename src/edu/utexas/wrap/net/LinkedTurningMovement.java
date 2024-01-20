package edu.utexas.wrap.net;

import java.util.Collection;

public class LinkedTurningMovement extends TurningMovement {
	private Collection<TurningMovement> parents;
	
	public LinkedTurningMovement(Link tailLink, Link headLink, 
			int id, Collection<TurningMovement> parents) {
		super(tailLink, headLink, id);
		// TODO Auto-generated constructor stub
		this.parents = parents;
	}
	
	public Collection<TurningMovement> getParents(){
		return parents;
	}

}
