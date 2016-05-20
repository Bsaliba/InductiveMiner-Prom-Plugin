package org.processmining.plugins.InductiveMiner;

import gnu.trove.set.hash.THashSet;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers.AttributeClassifier;

/**
 * Multi-selection combobox to select a classifier. It shows the classifiers
 * from the event log, as well as an option to construct one from data
 * attributes from the event log.
 * 
 * @author sleemans
 *
 */
public class ClassifiersChooser extends JPanel {

	private static final long serialVersionUID = 3348039386637737989L;
	private final MultiComboBox<AttributeClassifier> combobox;

	/**
	 * Notice: this constructor walks through the event log to gather
	 * attributes.
	 * 
	 * @param log
	 */
	public ClassifiersChooser(XLog log) {
		setLayout(new BorderLayout());
		setOpaque(false);
		this.combobox = new MultiComboBox<>(AttributeClassifier.class, new AttributeClassifier[0]);
		add(combobox, BorderLayout.CENTER);
		
		//walk through the log to gather attributes
		THashSet<String> attributes = new THashSet<>();
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				attributes.addAll(event.getAttributes().keySet());
			}
		}
		String[] attributesArray = attributes.toArray(new String[attributes.size()]);
		
		Pair<AttributeClassifier[], AttributeClassifier> p = AttributeClassifiers.getAttributeClassifiers(log, attributesArray, false);
		AttributeClassifier[] attributeClassifiers = p.getA();
		AttributeClassifier defaultAttributeClassifier = p.getB();
		
		//add the classifiers/attributes one by one to set singularity
		combobox.removeAllItems();
		for (AttributeClassifier classifier: attributeClassifiers) {
			combobox.addItem(classifier, classifier.isClassifier());
		}
		combobox.setSelectedItem(defaultAttributeClassifier);
	}

	public void addActionListener(ActionListener actionListener) {
		combobox.addActionListener(actionListener);
	}

	public XEventClassifier getSelectedClassifier() {
		return AttributeClassifiers.constructClassifier(combobox.getSelectedObjects());
	}
	
	public MultiComboBox<AttributeClassifier> getMultiComboBox() {
		return combobox;
	}
}
