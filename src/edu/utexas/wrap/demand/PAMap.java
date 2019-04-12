package edu.utexas.wrap.demand;

import java.util.Set;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

/**A mapping from a zone to the number of productions
 * and attractions that begin and end, respectively,
 * at that zone. This is the result of the trip 
 * generation step of the four-step model.
 * 
 * Any PA Map should have a method of retrieving the
 * metadata that will be used in the mode-choice models.
 * 
 * @author William
 *
 */
public interface  PAMap {

	public Set<Node> getProducers();

	public Set<Node> getAttractors();

	public Float getAttractions(Node z);

	public Float getProductions(Node z);

	public Graph getGraph();

	public void putAttractions(Node z, Float amt);

	public void putProductions(Node z, Float amt);
}
