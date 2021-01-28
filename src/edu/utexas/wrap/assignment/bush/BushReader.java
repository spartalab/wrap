package edu.utexas.wrap.assignment.bush;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.AssignmentProvider;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class BushReader implements AssignmentProvider<Bush> {
	Graph network;
	private Path inputPath;
	
	public BushReader(Graph network, Path ioPath) {
		this.network = network;
		this.inputPath = ioPath;
	}

	public void getStructure(Bush bush) throws IOException {
		//Load the bush from the network directory's file matching the bush's hash code
		Path p = inputPath
				.resolve(network.toString())
				.resolve(Integer.toString(bush.hashCode()));
		
		
		
		InputStream in = new BufferedInputStream(Files.newInputStream(p));
		readFromStream(bush, in);
		in.close();
	}
	
	public BackVector[] newRead(Path p) throws IOException {
		FileChannel channel = FileChannel.open(p, StandardOpenOption.READ);
		final long fileSize = channel.size();
		final int bufSize = Integer.BYTES*2 + Double.BYTES;
		final long numLinks = fileSize/bufSize;
		
		Collector<
			Map.Entry<Link, Double>, 
			?, 
			Map<
				Node, 
				List<
					Map.Entry<
						Link, 
						Double
					>
				>
			>
		>
		downstream = Collectors.groupingBy(entry -> entry.getKey().getHead());
		
		Function<			
			Map<
				Node, 
				List<
					Map.Entry<
						Link, 
						Double
						>
					>
			>,
			BackVector[]
		> 
		finisher = map -> {
			BackVector[] q = new BackVector[network.numNodes()];
			map.entrySet()
			.forEach(entry -> 
				q[entry.getKey().getOrder()] = 
					entry.getValue().size() == 0? 
							entry.getValue().get(0).getKey() : 
								new BushMerge(entry.getKey(),entry.getValue())
			);
			return q;
		};

		return LongStream.range(0,numLinks)
		.parallel()
		.mapToObj(i -> ByteBuffer.allocate(bufSize))
		.map(bb -> {
			int rs;
			try {
				rs = channel.read(bb);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
			if (rs < bufSize) throw new RuntimeException();
			Integer nid = bb.getInt();
			Integer bvhc = bb.getInt();
			Double split = bb.getDouble();
			Node n = network.getNode(nid);

			//Find the appropriate link instance
//				Link bv = null;
			Optional<Link> bvo = Stream.of(n.reverseStar()).parallel().filter(l -> l.hashCode()==bvhc).findAny();

			//If it can't be found, throw an error
			if (!bvo.isPresent()) throw new RuntimeException("Unknown Link econuntered. Node ID: "+nid+"\tHash: "+bvhc);
			
			return new AbstractMap.SimpleEntry<Link,Double>(bvo.get(),split);
		})
		.collect(Collectors.collectingAndThen(downstream,finisher));
	}
	
	private void readFromStream(Bush bush, InputStream in) throws IOException {
		final int bufSize = Integer.BYTES*2+Double.BYTES;
		final long fileSize = in.available();
		
		
		
		long pos = 0;
		
		//ensure that we're not overwriting an older structure
		bush.clear();
		BackVector[] q = new BackVector[network.numNodes()];
		
		
		
		
		
		
		byte[] b = new byte[bufSize];
		
		//For each link in the bush
		while (fileSize-pos >= bufSize) {
			//File IO, formatting
			in.read(b);
			pos += bufSize;
			ByteBuffer bb = ByteBuffer.wrap(b);
			processBytes(bush, q, bb);

		}
		bush.setQ(q);
	}

	private void processBytes(Bush bush, BackVector[] q, ByteBuffer bb) {
		Integer nid = bb.getInt();
		Integer bvhc = bb.getInt();
		Double split = bb.getDouble();
		Node n = network.getNode(nid);

		//Find the appropriate link instance
//			Link bv = null;
		Optional<Link> bvo = Stream.of(n.reverseStar()).filter(l -> l.hashCode()==bvhc).findAny();

		//If it can't be found, throw an error
		if (!bvo.isPresent()) throw new RuntimeException("Unknown Link econuntered. Node ID: "+nid+"\tHash: "+bvhc);
		
		Link bv = bvo.get();
		
		if (split >= 1.0) {
		
			if (q[n.getOrder()] == null) {
				q[n.getOrder()] = bv;
			} 
			
			else {
				if (q[n.getOrder()] instanceof BushMerge) {
					((BushMerge) q[n.getOrder()]).add(bv);
					((BushMerge) q[n.getOrder()]).setSplit(bv, split);
				} else {
					q[n.getOrder()] = new BushMerge(bush, bv, (Link) q[n.getOrder()]);
				}
			}
		
		} 

		
		else if (split >=0.0) {
			
			if (q[n.getOrder()] == null) {
				BushMerge bm = new BushMerge(bush, n);
				bm.add(bv);
				bm.setSplit(bv, split);
				q[n.getOrder()] = bm;
			} 
			
			
			else {
				
				if (q[n.getOrder()] instanceof BushMerge) {
					((BushMerge) q[n.getOrder()]).add(bv);
					((BushMerge) q[n.getOrder()]).setSplit(bv, split);
				} 
				
				else {
					q[n.getOrder()] = new BushMerge(bush, (Link) q[n.getOrder()], bv);
				}
			}
		}
	}

}
