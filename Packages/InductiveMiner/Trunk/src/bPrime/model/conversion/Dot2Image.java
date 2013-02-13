package bPrime.model.conversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Dot2Image {
	public static boolean dot2image(String dot, File file) {
		//convert to graph and write to file
		try {
			File dotFile = File.createTempFile("dot2image", ".dot");
			FileWriter out = new FileWriter(dotFile);
			out.write(dot);
			out.flush();
			out.close();
			String command = "\"C:\\Program Files (x86)\\Graphviz2.30\\bin\\dot.exe\" -Tpng -o\""+file.getAbsolutePath()+"\" \""+dotFile.getAbsolutePath()+"\"";
			Runtime.getRuntime().exec(command);
			dotFile.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
