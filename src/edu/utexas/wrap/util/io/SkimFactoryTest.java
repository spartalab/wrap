package edu.utexas.wrap.util.io;

import edu.utexas.wrap.net.Graph;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class SkimFactoryTest {

    @Test
    public void testSkimReader() throws IOException {
        Graph graph = GraphFactory.readEnhancedGraph(new File("../nctcogFiles/NCTCOG_net.csv"),50000);
        //Map<TravelSurveyZone, Map<TravelSurveyZone, Float>> skim = SkimFactory.readSkimFile(new File("../nctcogFiles/PKNOHOV.csv"), false, graph);
    }
}
