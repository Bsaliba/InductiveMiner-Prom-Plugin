package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeMetrics;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;

public class LoopATauTau2flower implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int loop) {

		//look for loop( A, tau3, tau4)
		if (tree.isLoop(loop)) {
			int body = tree.getChild(loop, 0);
			int tau3 = tree.getChild(loop, 1);
			int tau4 = tree.getChild(loop, 2);

			if (tree.isTau(tau3) && tree.isTau(tau4)) {

				/*
				 * perform behaviour analysis: if this subprocess can produce
				 * the single activity (for every occurring activity), we might
				 * as well replace it with a semi-flower model
				 */

				//loop through all nodes for activities
				int countActivities = 0;
				for (int i = 0; i < tau4; i++) {
					if (tree.isActivity(i)) {
						countActivities++;
						int activity = tree.getActivity(i);

						//check whether the redo can produce this activity as a single activity
						if (!EfficientTreeMetrics.canProduceSingleActivity(tree, body, activity)) {
							return false;
						}
					}
				}

				//gather the activities
				int[] activities = new int[countActivities];
				int j = 0;
				for (int i = 0; i < tau4; i++) {
					if (tree.isActivity(i)) {
						activities[j] = tree.getTree()[i];
						j++;
					}
				}

				/*
				 * Two possibilities: the body can produce tau or not. If it
				 * cannot; loop((...), tau3, tau4). If it can: loop(tau, (...),
				 * tau4).
				 */
				if (!EfficientTreeMetrics.canProduceTau(tree, body)) {

					//make sure we are terminating: xor(a, b, ....) does not require rewriting by this rule
					if ((tau3 - body) <= countActivities + 1) {
						return false;
					}

					//before: loop((....), tau3, tau4)

					//remove every node that will be useless in the future
					for (int child = tau3 - 1; child > loop + countActivities + 1; child--) {
						tree.getTree()[child] = EfficientTree.tau;
						tree.removeChild(body, child);
					}

					//set the xor
					tree.getTree()[body] = EfficientTree.xor - countActivities * EfficientTree.childrenFactor;

					//set the activities
					for (int i = 0; i < countActivities; i++) {
						tree.getTree()[body + i + 1] = activities[i];
					}

					//after: loop(xor( ... ), tau3, tau4)
				} else {
					//before: loop((....), tau3, tau4)

					//remove every node that will be useless in the future
					for (int child = tau3; child > loop + countActivities + 2; child--) {
						tree.getTree()[child] = EfficientTree.tau;
						tree.removeChild(body, child);
					}

					//set the body tau
					tree.getTree()[body] = EfficientTree.tau;

					//set the xor
					tree.getTree()[body + 1] = EfficientTree.xor - countActivities * EfficientTree.childrenFactor;

					//set the activities
					for (int i = 0; i < countActivities; i++) {
						tree.getTree()[body + 2 + i] = activities[i];
					}

					//after: loop(tau, xor( ... ), tau4)
				}

				return true;
			}
		}

		return false;
	}
}
