package edu.utexas.wrap.net;

import java.util.Collection;

/**
 * A {@link TurningMovement} whose green time allocation is linked to
 * one or more parent turning movements. This is used to model signal
 * phasing where certain movements share green time with or are
 * constrained by other movements (e.g., permitted left turns that
 * share time with opposing through movements).
 *
 * @author Will Alexander
 * @see TurningMovement
 * @see SignalizedNode
 */
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
