package edu.utexas.wrap.util;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupWorkerVehicleSegment;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class R2ErrorCalculator {
    public static double measureTripGenProdError(Graph g, Map<MarketSegment, PAMap> hbMaps, String productionFile, Collection<MarketSegment> prodSegs, boolean combinedPAFile) throws IOException {
        BufferedReader prodin = null;
        double productionError = 0.0;
        double y_bar = 0.0;
        double exp_err = 0.0;
        int numProds = 0;
        try {
            prodin = new BufferedReader(new FileReader(productionFile));
            String prodHeader = prodin.readLine();
            String[] prodHeaders = prodHeader.split(",");
            HashMap<MarketSegment, Integer> prodPositions = new HashMap<MarketSegment, Integer>();
            int i = 0;
            while(i < prodHeaders.length && prodPositions.size() < prodSegs.toArray().length) { //TODO Handle more than HWB Segments
                if((prodHeaders[i].contains("HBW") && !combinedPAFile) || (prodHeaders[i].contains("HBW_pro") && combinedPAFile)) {
                    for(MarketSegment m: prodSegs) {
                        IncomeGroupSegment seg = (IncomeGroupSegment) m;
                        if(prodHeaders[i].contains(seg.getIncomeGroup() +"_HBW")) {
                            prodPositions.put(m, i);
                        }
                    }
                }
                i++;
            }
            String prodLine = prodin.readLine();
            while (prodLine != null) {
                String[] args = prodLine.split(",");
                TravelSurveyZone tsz = g.getNode(Integer.parseInt(args[0])).getZone();
                for(MarketSegment m: prodSegs) {
                    y_bar += Double.parseDouble(args[prodPositions.get(m)]);
                    productionError += Math.pow(hbMaps.get(m).getProductions(tsz) - Double.parseDouble(args[prodPositions.get(m)]),2);
                }
                prodLine = prodin.readLine();
                numProds += 1;
            }
            y_bar = y_bar/numProds;
            prodin = new BufferedReader(new FileReader(productionFile));
            prodin.readLine();
            prodLine = prodin.readLine();
            while (prodLine != null) {
                String[] args = prodLine.split(",");
                for(MarketSegment m: prodSegs) {
                    exp_err += Math.pow(y_bar - Double.parseDouble(args[prodPositions.get(m)]),2);
                }
                prodLine = prodin.readLine();
            }
        } finally {
            if (prodin != null) {
                prodin.close();
            }
        }
        if(numProds == 0)
            throw new RuntimeException("Unable to read production/attraction files");

        return (1-(productionError/exp_err));

    }

    public static double measureTripGenSegmentedProdError(Graph g, Map<MarketSegment, PAMap> hbMaps, String productionFile, Collection<MarketSegment> prodSegs, Collection<MarketSegment> paProdSegs) throws IOException {
        BufferedReader prodin = null;
        double productionError = 0.0;
        double y_bar = 0.0;
        double exp_err = 0.0;
        int numProds = 0;
        try {
            prodin = new BufferedReader(new FileReader(productionFile));
            String prodHeader = prodin.readLine();
            String[] prodHeaders = prodHeader.split(",");
            HashMap<MarketSegment, ArrayList<Integer>> prodPositions = new HashMap<MarketSegment, ArrayList<Integer>>();
            for(MarketSegment m: prodSegs) {
                IncomeGroupWorkerVehicleSegment seg = (IncomeGroupWorkerVehicleSegment) m;
                ArrayList<Integer> idxs = new ArrayList<>();
                for (int i = 0; i < prodHeaders.length; i++) {
                    String head = prodHeaders[i];
                    if(head.contains("IG" + seg.getIncomeGroup()))
                        idxs.add(i);
                }
                prodPositions.put(seg, idxs);

            }

            String prodLine = prodin.readLine();
            while (prodLine != null) {
                String[] args = prodLine.split(",");
                TravelSurveyZone tsz = g.getNode(Integer.parseInt(args[0])).getZone();
                for(MarketSegment m: paProdSegs) {
                    Double ig_total = 0.0;
                    for (MarketSegment pSeg: prodSegs) {
                        if(((IncomeGroupSegment) m).getIncomeGroup() == ((IncomeGroupWorkerVehicleSegment) pSeg).getIncomeGroup()) {
                            for (int idx: prodPositions.get(pSeg)) {
                                ig_total += Double.parseDouble(args[idx]);
                            }
                        }
                    }
                    y_bar += ig_total;
                    productionError += Math.pow(hbMaps.get(m).getProductions(tsz) - ig_total,2);
                }
                prodLine = prodin.readLine();
                numProds += 1;
            }
            y_bar = y_bar/numProds;
            prodin = new BufferedReader(new FileReader(productionFile));
            prodin.readLine();
            prodLine = prodin.readLine();
            while (prodLine != null) {
                String[] args = prodLine.split(",");
                TravelSurveyZone tsz = g.getNode(Integer.parseInt(args[0])).getZone();
                for(MarketSegment m: paProdSegs) {
                    double ig_total = 0.0;
                    for (MarketSegment pSeg: prodSegs) {
                        if(((IncomeGroupSegment) m).getIncomeGroup() == ((IncomeGroupWorkerVehicleSegment) pSeg).getIncomeGroup()) {
                            for (int idx: prodPositions.get(pSeg)) {
                                ig_total += Double.parseDouble(args[idx]);
                            }
                        }
                    }
                    exp_err += Math.pow(y_bar - ig_total,2);
                }
                prodLine = prodin.readLine();
            }
        } finally {
            if (prodin != null) {
                prodin.close();
            }
        }
        if(numProds == 0)
            throw new RuntimeException("Unable to read production/attraction files");

        return 1-(productionError/exp_err);

    }

    public static double measureTripGenAttrError(Graph g, Map<MarketSegment, PAMap> hbMaps, String attractionFile, Collection<MarketSegment> attrSegs, boolean combinedPAFile) throws IOException {
        BufferedReader attrin = null;
        double attractionError = 0.0;
        double y_bar = 0.0;
        double exp_err = 0.0;
        int numAttrs = 0;
        try {
            attrin = new BufferedReader(new FileReader(attractionFile));
            String attrHeader = attrin.readLine();
            String[] attrHeaders = attrHeader.split(",");
            HashMap<MarketSegment, Integer> attrPositions = new HashMap<MarketSegment, Integer>();
            int i = 0;
            while(i < attrHeaders.length && attrPositions.size() < attrSegs.toArray().length) { //TODO Handle more than HWB Segments
                if((attrHeaders[i].contains("HBW") && !combinedPAFile) || (attrHeaders[i].contains("HBW_att") && combinedPAFile)) {
                    for(MarketSegment m: attrSegs) {
                        IncomeGroupSegment seg = (IncomeGroupSegment) m;
                        if(attrHeaders[i].contains(seg.getIncomeGroup() +"_HBW")) {
                            attrPositions.put(m, i);
                        }
                    }
                }
                i++;
            }
            String attrLine = attrin.readLine();
            while (attrLine != null) {
                String[] args = attrLine.split(",");
                TravelSurveyZone tsz = g.getNode(Integer.parseInt(args[0])).getZone();
                for(MarketSegment m: attrSegs) {
                    y_bar += Double.parseDouble(args[attrPositions.get(m)]);
                    attractionError += Math.pow(hbMaps.get(m).getAttractions(tsz) - Double.parseDouble(args[attrPositions.get(m)]),2);
                }
                attrLine = attrin.readLine();
                numAttrs += 1;
            }
            y_bar = y_bar/numAttrs;
            attrin = new BufferedReader(new FileReader(attractionFile));
            attrin.readLine();
            attrLine = attrin.readLine();
            while (attrLine != null) {
                String[] args = attrLine.split(",");
                for(MarketSegment m: attrSegs) {
                    exp_err += Math.pow(y_bar - Double.parseDouble(args[attrPositions.get(m)]),2);
                }
                attrLine = attrin.readLine();
            }
        } finally {
            if (attrin != null) {
                attrin.close();
            }
        }
        if(numAttrs == 0)
            throw new RuntimeException("Unable to read production/attraction files");

        return (1-(attractionError/exp_err));

    }

}
