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

/**
 * This class provides static methods relating to information about the Peak and Off peak rates
 */
public class PeakFactory {
    /**
     * This method reads a file that provides information about the peak and offpeak split rates for specific market segments
     * It expects a .csv file with information in the following order:
     * |IncomeGroup Market Segment, Rate|
     * @param file File object with information of the peak and off peak rates
     * @param header boolean whether the file has a header row
     * @param segs Collection of market segments of interest
     * @return Mapping between the market segment and the peak rate
     * @throws IOException
     */
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
