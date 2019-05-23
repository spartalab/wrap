package edu.utexas.wrap.distribution;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.DBPAMap;
import edu.utexas.wrap.demand.containers.DBPAMatrix;
import edu.utexas.wrap.net.Graph;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBGravityDistributor extends TripDistributor {

    private Connection db;
    private Graph g;


    public DBGravityDistributor(Graph g, Connection db) {
        this.g = g;
        this.db = db;
    }

    @Override
    public AggregatePAMatrix distribute(PAMap pa) {
        DBPAMap dbpaMap = (DBPAMap) pa;
        DBPAMatrix dbmtx = new DBPAMatrix(g,db,MarketSegment.paMtxPrefix+dbpaMap.getMarketSeg(), pa.getVOT());
        sqlDistribute(dbpaMap, dbmtx);
        return dbmtx;
    }

    private void sqlDistribute(DBPAMap dbpaMap, DBPAMatrix dbmtx) {
        String skim = "skim_"+dbpaMap.getMarketSeg();
        String ffTable = "fftable_"+dbpaMap.getMarketSeg();
        String pamapTable = dbpaMap.getTableName();
        String pamtxTable = dbmtx.getTableName();
        String segment = dbpaMap.getMarketSeg();
        String aTable = "avals"+segment;
        String bTable = "bvals"+segment;
        String createFF = "CREATE OR REPLACE FUNCTION getff"+segment+"(org integer,dest integer)\n" +
                "    RETURNS real\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    VOLATILE\n" +
                "    PARALLEL UNSAFE\n" +
                "    COST 100\n" +
                "AS $BODY$DECLARE\n" +
                "\t ffr REAL := 0.0;\n" +
                "\t skim REAL := 0.0;" +
                "\t roundedSkim INTEGER := 0;" +
                "\t down REAL := 0.0;" +
                "\t up REAL := 0.0;" +
                "BEGIN\n" +
                "\t skim:= (SELECT pktime FROM "+skim+" WHERE origin=org AND destination=dest);\n" +
                "\t roundedSkim:= ROUND(skim);\n" +
                "\t down:= (SELECT ff FROM "+ffTable+" WHERE pktime=roundedSkim);\n" +
                "\t up:= (SELECT ff FROM "+ffTable+" WHERE pktime=roundedSkim+1);\n" +
                "\t ffr:= (skim-roundedSkim) * abs(up-down) + down + 1;\n" +
                "\tRETURN ffr;\n" +
                "END;$BODY$;";
        String createDist = "CREATE OR REPLACE FUNCTION gravityDist"+segment+"()\n" +
                "    RETURNS void\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    VOLATILE\n" +
                "    PARALLEL UNSAFE\n" +
                "    COST 100\n" +
                "AS $BODY$DECLARE\n" +
                "   counter INTEGER := 1;\n" +
                "   denom  DOUBLE PRECISION := 0.0;\n" +
                "   temp_d DOUBLE PRECISION := 0.0;\n" +
                "   producer INTEGER;\n" +
                "   attractor INTEGER;\n" +
                "   productions DOUBLE PRECISION;\n" +
                "   attractions DOUBLE PRECISION;\n" +
                "   f DOUBLE PRECISION := 0.0 ;\n" +
                "BEGIN\n" +
                "DROP TABLE IF EXISTS "+aTable+";\n" +
                "DROP TABLE IF EXISTS "+bTable+";\n" +
                "\n" +
                "CREATE TABLE "+aTable+" (node INTEGER, val DOUBLE PRECISION DEFAULT 1.0);\n" +
                "CREATE TABLE "+bTable+" (node INTEGER, val DOUBLE PRECISION DEFAULT 1.0);\n" +
                "\n" +
                "INSERT INTO "+aTable+" (node) SELECT d.node FROM (SELECT * from demdata) as d group by node;\n" +
                "INSERT INTO "+bTable+" (node) SELECT d.node FROM (SELECT * from demdata) as d group by node;\n" +
                "\n" +
                "LOOP \n" +
                " EXIT WHEN counter = 100 ; \n" +
                " counter := counter + 1 ; \n" +
                " \n" +
                " FOR producer IN\n" +
                "   SELECT p.node\n" +
                "   FROM  "+pamapTable+" p\n" +
                "   WHERE  p.productions > 0\n" +
                " LOOP\n" +
                "   denom := 0.0;\n" +
                "   FOR attractor, attractions IN\n" +
                "   \tSELECT a.node, a.attractions\n" +
                "   \tFROM  "+pamapTable+" a\n" +
                "   \tWHERE  a.attractions > 0\n" +
                "   LOOP\n" +
                "    temp_d := 0.0;\n" +
                "    f:= getff"+segment+"(producer, attractor);" +
                "   \tSELECT (select val from "+bTable+" where node=attractor) * attractions * f INTO temp_d;\n" +
                "\tdenom := denom + temp_d;\n" +
                "\ttemp_d := 1 / denom;\n" +
                "\tUPDATE "+aTable+" SET val = temp_d WHERE node=attractor;\n" +
                "   END LOOP;\n" +
                " END LOOP;\n" +
                " \n" +
                " \n" +
                " FOR attractor IN\n" +
                "   SELECT node\n" +
                "   FROM "+pamapTable+" a\n" +
                "   WHERE  a.attractions > 0\n" +
                " LOOP\n" +
                "   denom := 0.0;\n" +
                "   FOR producer, productions IN\n" +
                "   \tSELECT p.node, p.productions\n" +
                "   \tFROM "+pamapTable+" p\n" +
                "   \tWHERE  p.productions > 0 \n" +
                "   LOOP\n" +
                "    temp_d := 0.0;\n" +
                "    f:= getff"+segment+"(producer, attractor);" +
                "   \tSELECT (select val from "+aTable+" where node=producer) * productions * f INTO temp_d;\n" +
                "\tdenom := denom + temp_d;\n" +
                "\ttemp_d := 1 / denom;\n" +
                "\tUPDATE "+bTable+" SET val = temp_d WHERE node=attractor;\n" +
                "   END LOOP;\n" +
                " END LOOP;\n" +
                "END LOOP ;\n" +
                "FOR attractor, attractions IN\n" +
                " SELECT a.node, a.attractions\n" +
                " FROM  "+pamapTable+" a\n" +
                " WHERE  a.attractions > 0\n" +
                " LOOP\n" +
                " FOR producer, productions IN\n" +
                "  SELECT p.node, p.productions\n" +
                "  FROM "+pamapTable+" p\n" +
                "  WHERE  p.productions > 0\n" +
                " LOOP\n" +
                "  INSERT INTO "+pamtxTable+" (origin, destination, demand) VALUES (producer, attractor, (SELECT val from "+aTable+" where node=producer) * (SELECT val from "+bTable+" where node=attractor) * productions * attractions * (SELECT getFF"+segment+"(producer, attractor)));\n" +
                " END LOOP;\n" +
                "END LOOP;\n" +
                "DROP TABLE IF EXISTS "+aTable+";\n" +
                "DROP TABLE IF EXISTS "+bTable+";\n" +
                "END;$BODY$;\n";
        try (PreparedStatement ps = db.prepareStatement(createFF)) {
            ps.execute();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
        try (PreparedStatement ps = db.prepareStatement(createDist)) {
            ps.execute();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
        String executeStatement = "SELECT getff"+segment+"(1,1);\n" +
                "SELECT gravityDist"+segment+"();";
        try (PreparedStatement ps = db.prepareStatement(executeStatement)) {
            ps.execute();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }

    public Graph getGraph(){return g;}

}
