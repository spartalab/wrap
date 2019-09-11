package edu.utexas.wrap.util;

import edu.utexas.wrap.marketsegmentation.IncomeGroupSegmenter;
import edu.utexas.wrap.marketsegmentation.MarketSegment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PeakFactory {
    public static Map<MarketSegment, Double> readPkOPkSplitRates(File file, boolean header,Collection<MarketSegment> segs) throws IOException {
        BufferedReader in = null;
        Map<MarketSegment,Double> map = new HashMap<>();

        try {
            in = new BufferedReader(new FileReader(file));
            if (header) in.readLine();
            in.lines().parallel().forEach(line -> {
                String[] args = line.split(",");
                int income = Integer.parseInt((args[0]));
                double factor = Double.parseDouble((args[1]));

                
                segs.parallelStream().filter(seg -> seg instanceof IncomeGroupSegmenter && ((IncomeGroupSegmenter) seg).getIncomeGroup() == income)
                .forEach(seg -> map.put(seg, factor));
            });

        } finally {
            if (in != null) in.close();
        }
        return map;
    }
}
