package edu.utexas.wrap.demand;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

/**This will be used in trip interchange splitting to map
 * from a given zone to its production and attraction values,
 * as aggregated across all modes.
 * 
 * @author William
 *
 */
public interface AggregatePAMatrix extends PAMatrix {
    public Mode getMode();

    public Graph getGraph();

    public Float getDemand(Node origin, Node destination);

}
