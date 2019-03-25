package edu.utexas.wrap.demand;

import java.util.Set;

import edu.utexas.wrap.modechoice.Mode;
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

	public Set<Integer> getProducers();

	public Set<Integer> getAttractors();

	public Float getAttractions(Integer z);

	public Float getProductions(Integer z);

	public Float getVOT() ;

	public Mode getVehicleClass() ;
	
}
