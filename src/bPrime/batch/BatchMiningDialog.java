package bPrime.batch;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class BatchMiningDialog extends JPanel {

	private static final long serialVersionUID = 4593870370139078439L;
	
	public BatchMiningDialog(final BatchMiningParameters parameters) {
		SlickerFactory factory = SlickerFactory.instance();
		SlickerDecorator decorator = SlickerDecorator.instance();
		
		JPanel thresholdsPanel = factory.createRoundedPanel(15, Color.gray);
		thresholdsPanel.setLayout(null);
		thresholdsPanel.setBounds(0, 0, 520, 700);
		
		JLabel thresholdTitle = factory.createLabel("Batch settings");
		thresholdTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 18));
		thresholdsPanel.add(thresholdTitle);
		thresholdTitle.setBounds(10, 10, 200, 30);
		
		//noise threshold
		JLabel noiseLabel = factory.createLabel("noise threshold");
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
		
		//precision/generalisation
		JLabel precisionLabel = factory.createLabel("replay log");
		thresholdsPanel.add(precisionLabel);
		precisionLabel.setBounds(20, 100, 100, 20);
		
		final JCheckBox precisionCheckBox = new JCheckBox();
		precisionCheckBox.setSelected(parameters.getMeasurePrecision());
		precisionCheckBox.setBounds(115, 102, 360, 20);
		precisionCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				parameters.setMeasurePrecision(precisionCheckBox.isSelected());
			}
		});
		decorator.decorate(precisionCheckBox);
		thresholdsPanel.add(precisionCheckBox);
		precisionCheckBox.setBounds(115, 102, 360, 20);
		
		JLabel precisionExplanation = factory.createLabel("Replay log and measure precision and generalisation.");
		thresholdsPanel.add(precisionExplanation);
		precisionExplanation.setBounds(20, 120, 400, 20);
		
		//folder
		/*		
		final JLabel folderValue = factory.createLabel("");
		thresholdsPanel.add(folderValue);
		folderValue.setBounds(485, 100, 100, 20);
		
		final JTextField folderInput = new JTextField(parameters.getFolder());
		folderInput.getDocument().addDocumentListener(new DocumentListener() {
			public void update() {
				parameters.setFolder(folderInput.getText());
				File x = new File(folderInput.getText());
				if (x.exists() && x.isDirectory()) {
					folderValue.setText("ok");
				} else {
					folderValue.setText("--");
				}
			}
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent e) {
				update();
			}
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			});
		//thresholdsPanel.add(folderInput);
		//folderInput.setBounds(115, 102, 360, 20);
		
		JLabel folderExplanation = factory.createLabel("Process all files from this folder having extensions " + parameters.getExtensions());
		thresholdsPanel.add(folderExplanation);
		folderExplanation.setBounds(20, 120, 400, 20);
		 
		*/
		
		JLabel folderLabel = factory.createLabel("input folder");
		thresholdsPanel.add(folderLabel);
		folderLabel.setBounds(20, 150, 100, 20);
		
		final JFileChooser folderChooser = new JFileChooser(parameters.getFolder());
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    folderChooser.setBounds(115, 140, 470, 500);
	    folderChooser.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent arg0) {
				parameters.setFolder(folderChooser.getSelectedFile());
			}
		});
	    thresholdsPanel.add(folderChooser);
		
		setLayout(null);
		add(thresholdsPanel);
		validate();
		repaint();
	}
}
