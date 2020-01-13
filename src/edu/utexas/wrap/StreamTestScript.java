package edu.utexas.wrap;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Random;


public class StreamTestScript {

    public static void main(String[] args) throws IOException, InterruptedException {
        ModelInput model = new ModelInputNCTCOG("inputs.properties");
        ProcessBuilder builder = new ProcessBuilder("./tap",model.getInputs().getProperty("network.graphFile"),"STREAM", model.getInputs().getProperty("ta.conversionTable"));
        File out = new File("testing" + "/log" + System.currentTimeMillis() + ".txt");
        out.getParentFile().mkdirs();
        out.createNewFile();
        builder.redirectOutput(out);
        builder.redirectError(out);
        Process proc = builder.start();
        OutputStream stdin = proc.getOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(48).order(ByteOrder.LITTLE_ENDIAN);
        Random r = new Random();
        for (int i = 0; i < 100000; i++) {
            WritableByteChannel channel = Channels.newChannel(stdin);
            buffer.clear();
            buffer.putInt(9697);
            buffer.putInt(7616);
            buffer.putInt(Float.floatToRawIntBits(r.nextFloat()));
            buffer.putInt(Float.floatToRawIntBits(r.nextFloat()));
            buffer.putInt(Float.floatToRawIntBits(r.nextFloat()));
            buffer.putInt(Float.floatToRawIntBits(r.nextFloat()));
            buffer.putInt(Float.floatToRawIntBits(r.nextFloat()));
            buffer.putInt(Float.floatToRawIntBits(r.nextFloat()));
            buffer.putInt(Float.floatToRawIntBits(r.nextFloat()));
            buffer.putInt(Float.floatToRawIntBits(r.nextFloat()));
            // MED_TRUCKS and HEAVY_TRUCKS
            buffer.putFloat(Float.floatToRawIntBits(0f));
            buffer.putFloat(Float.floatToRawIntBits(0f));
            try {
                buffer.rewind();
                String b = "r:" + buffer.getInt() + " s:" + buffer.getInt() + " "
                                    + buffer.getFloat() + " " + buffer.getInt() + " "
                                    + buffer.getInt() + " " + buffer.getInt() + " "
                                    + buffer.getInt() + " " + buffer.getInt() + " "
                                    + buffer.getInt() + " " + buffer.getInt() + " "
                                    + buffer.getInt() + " " + buffer.getInt();
                System.out.println(b);
                buffer.rewind();
                buffer.flip();
                stdin.write(buffer.array());
                stdin.flush();
            }catch (IOException e) {
                if (e.toString().contains("Broken pipe")) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
        stdin.write("Sto".getBytes());
        stdin.flush();
        stdin.close();

        proc.waitFor();
    }

}
