package edu.utexas.wrap.modechoice;

import java.util.Collection;

import edu.utexas.wrap.net.Node;

public abstract class UtilityModel {

	protected abstract Double getUtility(Mode m, Node origin, Node destination);

	protected abstract Collection<Mode> getModes();
	
}
