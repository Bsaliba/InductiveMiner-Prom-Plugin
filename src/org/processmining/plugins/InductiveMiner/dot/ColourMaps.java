package org.processmining.plugins.InductiveMiner.dot;

import java.awt.Color;

public class ColourMaps {
	public static String colourMapBlackBody(int weight, int maxWeight) {
		float x = weight / (float) maxWeight;

		/*
		 * //blue-yellow x = (x * (float) 0.5) + (float) 0.5; return new
		 * Color(x, x, 1-x);
		 */
		x = (x * (float) 0.75) + (float) 0.25;

		//black-body
		Color colour = new Color(Math.min(Math.max((1 - x) * 3, 0), 1), Math.min(
				Math.max((((1 - x) - 1 / (float) 3) * 3), 0), 1), Math.min(
				Math.max((((1 - x) - 2 / (float) 3) * 3), 0), 1));

		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}

	public static String colourMapRed(int weight, int maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * (float) 0.75) + (float) 0.25;
		Color colour = new Color(1, 1 - x, 1 - x);

		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}

	public static String colourMapGreen(int weight, int maxWeight) {
		float x = weight / (float) maxWeight;

		x = (x * (float) 0.75) + (float) 0.25;
		Color colour = new Color(1 - x, 1, 1 - x);

		String hexColour = Integer.toHexString(colour.getRGB());
		return "#" + hexColour.substring(2, hexColour.length());
	}
}
