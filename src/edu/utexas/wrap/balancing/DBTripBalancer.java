package edu.utexas.wrap.balancing;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.DBPAMap;
import edu.utexas.wrap.net.Graph;

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
        String sumQuery = "SELECT sum(productions) AS prodTotal, sum(attractions) AS attrTotal FROM ?";
        try(PreparedStatement ps = db.prepareStatement(sumQuery)) {
            ps.setString(1, ((DBPAMap) paMap).getTableName());
            ResultSet output = ps.executeQuery();
            float prodTotal = output.getFloat("prodTotal");
            float attrTotal = output.getFloat("attrTotal");
            float factor = attrTotal/prodTotal;
            String factorQuery = "UPDATE ? SET productions = productions * ?";
            PreparedStatement fq = db.prepareStatement(factorQuery);
            fq.setString(1, ((DBPAMap) paMap).getTableName());
            fq.setFloat(2, factor);
            fq.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }
}
