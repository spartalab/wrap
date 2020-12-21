package edu.utexas.wrap.demand;

/**This interface defines the origin point of a PAMap;
 * that is, any class implementing this interface must
 * provide a means to generate a balanced PAMap
 * @author William
 *
 */
public interface PAMapProvider {
	
	PAMap getPAMap();

}
