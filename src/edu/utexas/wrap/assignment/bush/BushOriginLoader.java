package edu.utexas.wrap.assignment.bush;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class BushOriginLoader extends BushOriginBuilder {
	Boolean print = true;
	
	public BushOriginLoader(Graph g, Node o, Set<BushOrigin> origins) {
		super(g, o, origins);
	}

	@Override
	public void run() {
		orig = new BushOrigin(o);
		for (Mode c : map.keySet()) {
			for (Float vot : map.get(c).keySet()) {
				AutoDemandMap odm = map.get(c).get(vot);
				if (!odm.isEmpty()) try {
					orig.loadBush(g, vot, odm, c);
				} catch (IOException ex) {
					Bush bush = orig.buildBush(g, vot, odm, c);
					
					if (print) {
						StringBuilder sb = new StringBuilder();
						for (byte b : g.getMD5()) {
							sb.append(String.format("%02X", b));
						}

						File file = new File(sb+"/"+o.getID()+"/"+c+"-"+vot+".bush");
						file.getParentFile().mkdirs();
						try {
							PrintStream out = new PrintStream(file);

							bush.toFile(out);
							out.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		orig.deleteInitMap();
		origins.add(orig);
	}
}
