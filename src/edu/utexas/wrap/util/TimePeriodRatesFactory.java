package edu.utexas.wrap.util;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.distribution.CostBasedFrictionFactorMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.net.Node;
import sun.jvm.hotspot.oops.Mark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class TimePeriodRatesFactory {
        public static Map<MarketSegment, Map<TimePeriod, Double>> readArrivalFile(File file,Collection<MarketSegment> segs) throws IOException {
            Map<MarketSegment, Map<TimePeriod, Double>> arrivalRates = new HashMap<MarketSegment, Map<TimePeriod, Double>>();
            BufferedReader in = null;

            try {
                in = new BufferedReader(new FileReader(file));
                String headers = in.readLine();
                String[] headersL = headers.split(",");
                HashMap<MarketSegment, Integer> positions = new HashMap<MarketSegment, Integer>();
                int i = 0;
                while(positions.size() < 3 && i < headersL.length) { //TODO Handle more than HWB Segments
                    if(headersL[i].contains("DEP")) {
                        for(MarketSegment m: segs) {
                            IncomeGroupSegment seg = (IncomeGroupSegment) m;
                            if(headersL[i].contains(seg.getIncomeGroup() +"")) {
                                positions.put(m, i);
                            }
                        }
                    }
                }
                String line = in.readLine();

                while (line != null) {
                    String[] args = headers.split(",");
                    TimePeriod tod = TimePeriod.valueOf(args[0]);
                    for(MarketSegment m: segs) {
                        Map<TimePeriod, Double> segInfo = arrivalRates.getOrDefault(m, new HashMap<>());
                        segInfo.put(tod, Double.parseDouble(args[positions.get(m)]));
                        arrivalRates.put(m,segInfo);
                    }
                    line = in.readLine();
                }
            } finally {
                if (in != null) in.close();
            }
            return arrivalRates;
        }

    public static Map<MarketSegment, Map<TimePeriod, Double>> readDepartureFile(File file,Collection<MarketSegment> segs) throws IOException {
        Map<MarketSegment, Map<TimePeriod, Double>> departureRates = new HashMap<MarketSegment, Map<TimePeriod, Double>>();
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(file));
            String headers = in.readLine();
            String[] headersL = headers.split(",");
            HashMap<MarketSegment, Integer> positions = new HashMap<MarketSegment, Integer>();
            int i = 0;
            while(positions.size() < 3 && i < headersL.length) { //TODO Handle more than HWB Segments
                if(headersL[i].contains("RET")) {
                    for(MarketSegment m: segs) {
                        IncomeGroupSegment seg = (IncomeGroupSegment) m;
                        if(headersL[i].contains(seg.getIncomeGroup() +"")) {
                            positions.put(m, i);
                        }
                    }
                }
            }
            String line = in.readLine();

            while (line != null) {
                String[] args = headers.split(",");
                TimePeriod tod = TimePeriod.valueOf(args[0]);
                for(MarketSegment m: segs) {
                    Map<TimePeriod, Double> segInfo = departureRates.getOrDefault(m, new HashMap<>());
                    segInfo.put(tod, Double.parseDouble(args[positions.get(m)]));
                    departureRates.put(m,segInfo);
                }
                line = in.readLine();
            }
        } finally {
            if (in != null) in.close();
        }
        return departureRates;
    }
}
