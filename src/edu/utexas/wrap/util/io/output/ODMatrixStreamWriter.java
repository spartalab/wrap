package edu.utexas.wrap.util.io.output;

import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Map;

public class ODMatrixStreamWriter {


    public static void write(String timePeriod, Map<Mode,Map<Float,ODMatrix>> ods, OutputStream stdin) {
        try {
//            List<ODMatrix> temp = new ArrayList<ODMatrix>(ods);
            // Assuming that there is something in the ods
        	Collection<TravelSurveyZone> g = 
        			ods.values().stream().flatMap(map -> map.values().stream())
        			.map(ODMatrix::getZones)
        			.findAny()
        			.orElseThrow(RuntimeException::new);
        	
            if (ods.isEmpty()) {
                System.out.println("ODs has nothing...");
                System.exit(1);
            }
            BufferedOutputStream bo = new BufferedOutputStream(stdin);
            Collection<TravelSurveyZone> origins = g;
            Collection<TravelSurveyZone> demands = g;
//            System.out.println("There are " + origins.size() + " zones");
            ByteBuffer buffer = ByteBuffer.allocate(48 * demands.size()).order(ByteOrder.LITTLE_ENDIAN);
//            Map<String, ODMatrix> od_info = new HashMap<>();
//            for (ODMatrix od : ods) {
//                od_info.put(od.toString(), od);
//            }
            int count = 0;
            for(TravelSurveyZone orig : origins) {
                count += 1;
                buffer.clear();
                for(TravelSurveyZone dest : demands) {
                    buffer.putInt(orig.getID());
                    buffer.putInt(dest.getID());

//                    System.out.println("-------------------------------------");
                    buffer.putInt(Float.floatToRawIntBits(ods.get(Mode.SINGLE_OCC).get(0.8f).getDemand(orig,dest)));
                    buffer.putInt(Float.floatToRawIntBits(ods.get(Mode.SINGLE_OCC).get(1.7f).getDemand(orig,dest)));
                    buffer.putInt(Float.floatToRawIntBits(ods.get(Mode.HOV).get(0.8f).getDemand(orig,dest)));
                    buffer.putInt(Float.floatToRawIntBits(ods.get(Mode.HOV).get(1.7f).getDemand(orig,dest)));
                    buffer.putInt(Float.floatToRawIntBits(ods.get(Mode.SINGLE_OCC).get(0.5f).getDemand(orig,dest)));
                    buffer.putInt(Float.floatToRawIntBits(ods.get(Mode.SINGLE_OCC).get(1.0f).getDemand(orig,dest)));
                    buffer.putInt(Float.floatToRawIntBits(ods.get(Mode.HOV).get(0.5f).getDemand(orig,dest)));
                    buffer.putInt(Float.floatToRawIntBits(ods.get(Mode.HOV).get(1.0f).getDemand(orig,dest)));
                    // MED_TRUCKS and HEAVY_TRUCKS
                    buffer.putInt(Float.floatToRawIntBits(ods.get(Mode.MED_TRUCK).get(1.5f).getDemand(orig,dest)));
                    buffer.putInt(Float.floatToRawIntBits(ods.get(Mode.HVY_TRUCK).get(1.5f).getDemand(orig,dest)));
//                    System.out.println("r:" + orig.getNode().getID() + " s:" + dest.getNode().getID() + " "
//                        + od_info.get("SINGLE_OCC_0.35") + " " + od_info.get("SINGLE_OCC_0.9")
//                        + od_info.get("HOV_0.35") + " " + od_info.get("HOV_0.9")
//                        + od_info.get("SINGLE_OCC_0.17") + " " + od_info.get("SINGLE_OCC_0.45")
//                        + od_info.get("HOV_0.17") + " " + od_info.get("HOV_0.45")
////                            + od_info.get("SINGLE_OCC_0.35") + " " + od_info.get("SINGLE_OCC_0.9")
//                    );
                }
                try {
                    int position = buffer.position();
                    buffer.flip();
                    bo.write(buffer.array(), 0, position);
//                        stdin.flush();
                } catch (IOException e) {
                    if(e.toString().contains("Broken Pipe")) {
                        System.err.println("TAP-B has an error");
                    }
                    e.printStackTrace();
                    System.exit(1);
                }
//                printTimeStamp();
                System.out.println(timePeriod + ":Finished " + count + "/"+ origins.size() +" zones so far");
                bo.flush();
//                System.out.println("=============================================");
            }
            bo.write("Sto".getBytes());
            bo.flush();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
