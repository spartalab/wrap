package edu.utexas.wrap.demand;

import java.util.stream.Stream;

/**This interface defines the origin point for ODProfiles;
 * that is, any class implementing this interface must
 * provide a means to generate a Stream of ODProfiles
 * 
 * @author William
 *
 */
public interface ODProfileProvider {

	public Stream<ODProfile> getODProfiles();
}
