package edu.utexas.wrap.util.io.output;

import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.net.TravelSurveyZone;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;


public class ODMatrixStreamWriter {

    public static byte[] reverse(byte[] array) {
        if (array == null) {
            return null;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
        return array;
    }

    public static void write(Collection<ODMatrix> ods, OutputStream stdin) {
        try{
            stdin.write("Start".getBytes());
            stdin.flush();
            ByteBuffer buffer = ByteBuffer.allocate(48);
            List<ODMatrix> temp = new ArrayList(ods);
            // Assuming that there is something in the ods
            if (temp.size() == 0) {
                System.out.println("ODs has nothing...");
                System.exit(1);
            }

            Collection<TravelSurveyZone> origins = temp.get(0).getGraph().getTSZs();
            Collection<TravelSurveyZone> demands = temp.get(0).getGraph().getTSZs();

            for(TravelSurveyZone orig : origins) {
                for(TravelSurveyZone dest : demands) {
                    buffer.putInt(orig.getNode().getID());
                    buffer.putInt(dest.getNode().getID());
                    for (ODMatrix od : ods) {
                        float demand = od.getDemand(orig, dest);
                        if (demand > 0) {
                            buffer.putFloat(demand);
                        }
                        System.out.println(od.getMode() + "_" + od.getVOT());
                    }
                    try {
                        stdin.write(reverse(buffer.array()));
                        stdin.flush();
                        buffer.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
