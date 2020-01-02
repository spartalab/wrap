package edu.utexas.wrap.util.io.output;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ODMatrixStreamWriter {

    public static void write(ODMatrix matrix, OutputStream stdin) {
//        Path path = Paths.get(outputDirectory,
//                timePeriod.toString(),
//                matrix.getMode().toString(),
//                matrix.getVOT().toString()+".matrix");
        try{
//            Files.createDirectories(path.getParent());
//            BufferedWriter out = Files.newBufferedWriter(path,
//                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            matrix.getGraph().getTSZs().parallelStream().forEach( orig -> {
                matrix.getGraph().getTSZs().parallelStream()
//				.filter(dest -> matrix.getDemand(orig,dest) > 0)
                        .forEach(dest ->{
                            try {
                                float demand = matrix.getDemand(orig, dest);
                                if (demand > 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(orig.getNode().getID());
                                    sb.append(dest.getNode().getID());
                                    sb.append(demand);
                                    sb.append("\r\n");
//                                    out.write(sb.toString());
                                    stdin.write("start".getBytes());
                                    stdin.write(sb.toString().getBytes());
//							        out.flush();
                                }
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
