package org.processmining.plugins.InductiveMiner.mining;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.model.XLog;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class MiningDialog extends JPanel {
	
	private static final long serialVersionUID = 7693870370139578439L;

	public MiningDialog(XLog log, final MiningParameters parameters) {
		
		SlickerFactory factory = SlickerFactory.instance();
		
		JPanel thresholdsPanel = factory.createRoundedPanel(15, Color.gray);
		thresholdsPanel.setLayout(null);
		thresholdsPanel.setBounds(0, 0, 570, 240);
		
		JLabel thresholdTitle = factory.createLabel("Thresholds");
		thresholdTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 18));
		thresholdsPanel.add(thresholdTitle);
		thresholdTitle.setBounds(10, 10, 200, 30);
		
		//noise threshold
		JLabel noiseLabel = factory.createLabel("Noise threshold");
		thresholdsPanel.add(noiseLabel);
		noiseLabel.setBounds(20, 50, 100, 20);
		
		final JLabel noiseValue = factory.createLabel(String.format("%.2f", parameters.getNoiseThreshold()));
		thresholdsPanel.add(noiseValue);
		noiseValue.setBounds(535, 50, 100, 20);
		
		JLabel noiseExplanation = factory.createLabel("If set to 0.00, perfect log fitness is guaranteed.");
		thresholdsPanel.add(noiseExplanation);
		noiseExplanation.setBounds(20, 70, 400, 20);
		
		final JSlider noiseSlider = factory.createSlider(SwingConstants.HORIZONTAL);
		noiseSlider.setMinimum(0);
		noiseSlider.setMaximum(1000);
		noiseSlider.setValue((int) (parameters.getNoiseThreshold() * 1000));
		noiseSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				parameters.setNoiseThreshold((float) (noiseSlider.getValue() / 1000.0));
				noiseValue.setText(String.format("%.2f", parameters.getNoiseThreshold()));
			}
		});
		thresholdsPanel.add(noiseSlider);
		noiseSlider.setBounds(165, 52, 360, 20);
		
		//incomplete threshold
		JLabel incompleteLabel = factory.createLabel("Incomplete threshold");
		thresholdsPanel.add(incompleteLabel);
		incompleteLabel.setBounds(20, 110, 150, 20);
		
		final JLabel incompleteValue = factory.createLabel(String.format("%.2f", parameters.getIncompleteThreshold()));
		thresholdsPanel.add(incompleteValue);
		incompleteValue.setBounds(535, 110, 100, 20);
		
		final JSlider incompleteSlider = factory.createSlider(SwingConstants.HORIZONTAL);
		incompleteSlider.setMinimum(0);
		incompleteSlider.setMaximum(1000);
		incompleteSlider.setValue((int) (parameters.getIncompleteThreshold() * 1000));
		incompleteSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				parameters.setIncompleteThreshold((float) (incompleteSlider.getValue() / 1000.0));
				incompleteValue.setText(String.format("%.2f", parameters.getIncompleteThreshold()));
			}
		});
		thresholdsPanel.add(incompleteSlider);
		incompleteSlider.setBounds(165, 112, 360, 20);
		
		//JLabel incompleteExplanation = factory.createLabel("The degree to which incompleteness is to be ignored.");
		//thresholdsPanel.add(incompleteExplanation);
		//incompleteExplanation.setBounds(20, 120, 400, 20);
		
		//JLabel bothExplanation = factory.createLabel("If both set to 0.00 and the log is complete, rediscoverability of some systems is guaranteed.");
		//thresholdsPanel.add(bothExplanation);
		//bothExplanation.setBounds(20, 160, 560, 20);
		
		setLayout(null);
		add(thresholdsPanel);
		validate();
		repaint();
	}
	
}
