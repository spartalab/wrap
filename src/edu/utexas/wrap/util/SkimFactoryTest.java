package edu.utexas.wrap.util;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SkimFactoryTest {

    @Test
    public void testSkimReader() throws IOException {
        Graph graph = GraphFactory.readEnhancedGraph(new File("../nctcogFiles/NCTCOG_net.csv"),50000);
        //Map<TravelSurveyZone, Map<TravelSurveyZone, Float>> skim = SkimFactory.readSkimFile(new File("../nctcogFiles/PKNOHOV.csv"), false, graph);
    }
}
