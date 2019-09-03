package edu.utexas.wrap.balancing;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.database.DBPAMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public  class DBTripBalancer implements TripBalancer{


    Connection db;
    public DBTripBalancer(Connection db) {
        this.db = db;

    }
    @Override
    public  void balance(PAMap paMap) {
        String sumQuery = "SELECT sum(productions) AS prodTotal, sum(attractions) AS attrTotal FROM " + ((DBPAMap) paMap).getTableName();
        try(PreparedStatement ps = db.prepareStatement(sumQuery)) {
            ResultSet output = ps.executeQuery();
            if (output.next()) {
                float prodTotal = output.getFloat("prodTotal");
                float attrTotal = output.getFloat("attrTotal");
                float factor = attrTotal / prodTotal;
                String factorQuery = "UPDATE " + ((DBPAMap) paMap).getTableName() + " SET productions = productions * ?";
                PreparedStatement fq = db.prepareStatement(factorQuery);
                fq.setFloat(1, factor);
                fq.executeUpdate();
            }
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }
}
