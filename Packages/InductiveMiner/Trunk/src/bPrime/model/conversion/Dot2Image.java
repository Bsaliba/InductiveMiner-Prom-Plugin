package bPrime.model.conversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Dot2Image {
	public static boolean dot2image(String dot, File pngFile, File pdfFile) {
		//convert to graph and write to file
		try {
			File dotFile = File.createTempFile("dot2image", ".dot");
			FileWriter out = new FileWriter(dotFile);
			out.write(dot);
			out.flush();
			out.close();
			if (pngFile != null) {
				String command = "\"C:\\Program Files (x86)\\Graphviz2.30\\bin\\dot.exe\" -Tpng -o\""+pngFile.getAbsolutePath()+"\" \""+dotFile.getAbsolutePath()+"\"";
				Runtime.getRuntime().exec(command);
				dotFile.deleteOnExit();
			}
			if (pdfFile != null) {
				String command = "\"C:\\Program Files (x86)\\Graphviz2.30\\bin\\dot.exe\" -Tpdf -o\""+pdfFile.getAbsolutePath()+"\" \""+dotFile.getAbsolutePath()+"\"";
				Runtime.getRuntime().exec(command);
				dotFile.deleteOnExit();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
