package edu.utexas.wrap.marketsegmentation;

import edu.utexas.wrap.net.TravelSurveyZone;

import java.util.function.ToDoubleFunction;

public class CollegeSegment implements MarketSegment {

    private CollegeType colType;
    private int hash;

    public CollegeSegment(CollegeType type) {colType = type;}

    public CollegeSegment(String arguments) {
        String[] args = arguments.split(",");
        if(args.length != 1) {
            throw new IllegalArgumentException("Mismatch number of arguments expected 1 got " + args.length);
        }
        this.colType = CollegeType.valueOf(args[0]);
    }

    @Override
    public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
        return tsz -> tsz.getCollegesByType(colType);
    }

    @Override
    public String toString() {
        return "MS: College of type " + colType ;
    }

    @Override
    public int hashCode() {
        if(hash == 0) {
            hash = toString().hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            CollegeSegment other = (CollegeSegment) obj;
            return other.colType == colType;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
