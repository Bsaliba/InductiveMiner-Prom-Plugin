package bPrime.mining;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.model.XLog;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class MiningDialog extends JPanel {
	
	private static final long serialVersionUID = 7693870370139578439L;

	public MiningDialog(XLog log, final MiningParameters parameters) {
		
		SlickerFactory factory = SlickerFactory.instance();
		SlickerDecorator decorator = SlickerDecorator.instance();
		
		JPanel thresholdsPanel = factory.createRoundedPanel(15, Color.gray);
		thresholdsPanel.setLayout(null);
		thresholdsPanel.setBounds(0, 0, 520, 240);
		
		JLabel thresholdTitle = factory.createLabel("Thresholds");
		thresholdTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 18));
		thresholdsPanel.add(thresholdTitle);
		thresholdTitle.setBounds(10, 10, 200, 30);
		
		//noise threshold
		JLabel noiseLabel = factory.createLabel("Noise threshold");
		thresholdsPanel.add(noiseLabel);
		noiseLabel.setBounds(20, 50, 100, 20);
		
		final JLabel noiseValue = factory.createLabel(String.format("%.3f", parameters.getNoiseThreshold()));
		thresholdsPanel.add(noiseValue);
		noiseValue.setBounds(485, 50, 100, 20);
		
		JLabel noiseExplanation = factory.createLabel("If set to 0.000, perfect log fitness is guaranteed.");
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
		noiseSlider.setBounds(115, 52, 360, 20);
		
		setLayout(null);
		add(thresholdsPanel);
		validate();
		repaint();
	}
	
}
