package edu.utexas.wrap.assignment.bush;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.utexas.wrap.ModelInput;
import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.util.io.output.ODMatrixStreamWriter;

public class StreamPassthroughAssigner implements Runnable {
	ModelInput model;
	Map<TimePeriod, Collection<ODMatrix>> reducedODs;

	public StreamPassthroughAssigner(ModelInput model, Map<TimePeriod, Collection<ODMatrix>> flatODs) {
		// TODO Auto-generated constructor stub
		this.model = model;
		reducedODs = flatODs;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		HashMap<String, Process> tas = new HashMap<String,Process>();
		   reducedODs.entrySet().forEach(entry -> {
	            try {
//					if(!entry.getKey().equals(TimePeriod.AM_PK)) return;
	                System.out.println("Streaming " + entry.getKey().toString());
	                String tapExec = new File("./tap").getAbsolutePath();
	                String netFile = new File(model.getNetFile()).getAbsolutePath();
	                String convTable = new File(model.getConversionTableFile()).getAbsolutePath();
	                ProcessBuilder builder = new ProcessBuilder(tapExec,netFile,"STREAM",convTable);
	                File outputDirectory = new File(model.getOutputDirectory() + entry.getKey().toString() + "/");
	                outputDirectory.mkdirs();
	                builder.directory(outputDirectory);
	                File out = new File(outputDirectory.getAbsolutePath() + "/log" + System.currentTimeMillis() + ".txt");
	                out.getParentFile().mkdirs();
	                out.createNewFile();
	                builder.redirectOutput(out);
	                builder.redirectError(out);
	                Process proc = builder.start();
	                OutputStream stdin = proc.getOutputStream();
	                streamODs(entry, stdin);
	                tas.put(entry.getKey().toString(), proc);
	            } catch(IOException e) {
	                System.out.println(entry.getKey().toString() + " unable to stream data");
	                e.printStackTrace();
	            }
	        });
	        tas.entrySet().parallelStream().forEach(entry -> {
	            try {
	                int exit = entry.getValue().waitFor();
//	                printTimeStamp();
	                System.out.println(entry.getKey() + ":TA Process finished with exit code "+ exit);
	            } catch (InterruptedException e) {
//	                printTimeStamp();
	                System.out.println(entry.getKey() + " traffic assignment was interrupted");
	                e.printStackTrace();
	            }
	        });
	}
	
	private static void streamODs(Entry<TimePeriod, Collection<ODMatrix>> ods, OutputStream o) {
		ODMatrixStreamWriter.write(ods.getKey().toString(), ods.getValue(), o);
	}


	
}