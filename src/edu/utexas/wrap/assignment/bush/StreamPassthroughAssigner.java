package edu.utexas.wrap.assignment.bush;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import edu.utexas.wrap.ModelInput;
import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.util.io.output.ODMatrixStreamWriter;

public class StreamPassthroughAssigner implements Assigner {
	ModelInput model;
	Collection<ODMatrix> mtxs;
	TimePeriod period;

	public StreamPassthroughAssigner(ModelInput model, Map<TimePeriod, Collection<ODMatrix>> flatODs) {
		// TODO Auto-generated constructor stub
		this.model = model;
		mtxs = new HashSet<ODMatrix>();
		period = TimePeriod.AM_PK;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			System.out.println("Streaming " + period.toString());
			String tapExec = new File("./tap").getAbsolutePath();
			String netFile = new File(model.getNetFile()).getAbsolutePath();
			String convTable = new File(model.getConversionTableFile()).getAbsolutePath();
			ProcessBuilder builder = new ProcessBuilder(tapExec,netFile,"STREAM",convTable);
			File outputDirectory = new File(model.getOutputDirectory() + period.toString() + "/");
			outputDirectory.mkdirs();
			builder.directory(outputDirectory);
			File out = new File(outputDirectory.getAbsolutePath() + "/log" + System.currentTimeMillis() + ".txt");
			out.getParentFile().mkdirs();
			out.createNewFile();
			builder.redirectOutput(out);
			builder.redirectError(out);
			Process proc = builder.start();
			OutputStream stdin = proc.getOutputStream();
			streamODs(mtxs, stdin);

			int exit = proc.waitFor();
			System.out.println(period.toString() + ":TA Process finished with exit code "+ exit);
		} catch (IOException e) {
			System.out.println(period.toString() + " unable to stream data");
			e.printStackTrace();			
		} catch (InterruptedException e) {
			System.out.println(period.toString() + " traffic assignment was interrupted");
			e.printStackTrace();
		}
	}

	private void streamODs(Collection<ODMatrix> ods, OutputStream o) {
		ODMatrixStreamWriter.write(period.toString(), ods, o);
	}

	@Override
	public void process(ODProfile profile) {
		// TODO Auto-generated method stub
		mtxs.add(profile.getMatrix(period));
	}



}