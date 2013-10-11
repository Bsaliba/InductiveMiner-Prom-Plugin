package org.processmining.plugins.InductiveMiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Matrix<X extends Comparable<X>, Y extends Number> {
	private Map<Pair<Maybe<X>, Maybe<X>>, Y> matrix;
	private List<X> activities;
	private boolean includeStartEnd;

	private class Maybe<X> {
		private X x;

		public Maybe(X x) {
			this.x = x;
		}

		public X get() {
			return x;
		}

		@Override
		public int hashCode() {
			if (x != null) {
				return x.hashCode();
			} else {
				return 0;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			} else if (o instanceof Matrix<?, ?>.Maybe<?>) {
				Matrix<?, ?>.Maybe<?> o2 = (Matrix<?, ?>.Maybe<?>) o;
				if (get() == null && o2.get() == null) {
					return true;
				} else if (get() == null || o2.get() == null) {
					return false;
				}
				return this.get().equals(o2.get());
			}
			return false;
		}
	}

	public Matrix(Set<X> activities, boolean includeStartEnd) {
		matrix = new HashMap<Pair<Maybe<X>, Maybe<X>>, Y>();
		this.activities = new ArrayList<X>(activities);
		Collections.sort(this.activities);
		this.includeStartEnd = includeStartEnd;
	}

	public Y get(X from, X to) {
		return matrix.get(new Pair<Maybe<X>, Maybe<X>>(new Maybe<X>(from), new Maybe<X>(to)));
	}

	public void set(X from, X to, Y newValue) {
		matrix.put(new Pair<Maybe<X>, Maybe<X>>(new Maybe<X>(from), new Maybe<X>(to)), newValue);
	}

	public List<X> getActivities() {
		return getActivities();
	}

	public String toString() {
		return toString(false);
	}

	public String toString(boolean useHTML) {
		StringBuilder s = new StringBuilder();

		String newLine = "\n";
		String newCell = "";
		if (useHTML) {
			newLine = "\n<tr>";
			newCell = "<td>";
			s.append("<table>");
		}

		//titles
		if (useHTML) {
			s.append(newLine);
		}
		s.append(newCell);
		s.append(placeHolder());
		for (X from : activities) {
			s.append(newCell);
			s.append(shortenString(from.toString()));
		}
		s.append(newCell);
		if (includeStartEnd) {
			s.append(" -E-");
			s.append(newLine);
		}

		{
			if (includeStartEnd) {
				s.append(newCell);
				s.append(" -S- ");
				for (X to : activities) {
					s.append(newCell);
					Y x = get(null, to);
					if (x != null) {
						s.append(shortenNumber(x));
					} else {
						s.append(placeHolder());
					}
				}

				Y x = get(null, null);
				s.append(newCell);
				if (x != null) {
					s.append(shortenNumber(x));
				} else {
					s.append(placeHolder());
				}
			}
			
			s.append(newLine);
		}

		for (X from : activities) {
			s.append(newCell);
			s.append(shortenString(from.toString()));
			for (X to : activities) {
				Y x = get(from, to);
				s.append(newCell);
				if (x != null) {
					s.append(shortenNumber(x));
				} else {
					s.append(placeHolder());
				}
			}

			if (includeStartEnd) {
				Y x = get(from, null);
				s.append(newCell);
				if (x != null) {
					s.append(shortenNumber(x));
				} else {
					s.append(placeHolder());
				}
			}

			s.append(newLine);
		}

		return s.toString();
	}

	public static String placeHolder() {
		return "  .  ";
	}

	public static String shortenString(String name) {
		String s = name.substring(0, Math.min(name.length(), 3));
		return String.format("%1$" + 4 + "s ", s);
	}

	public static String shortenNumber(Number n) {
		if (n instanceof Double) {
			return String.format("%1.2f ", n);
		} else {
			return String.format("%3d  ", n);
		}
	}

}
