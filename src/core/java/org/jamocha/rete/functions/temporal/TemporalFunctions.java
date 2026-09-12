package org.jamocha.rete.functions.temporal;

import java.util.ArrayList;
import java.util.List;

import org.jamocha.rete.Function;
import org.jamocha.rete.FunctionGroup;
import org.jamocha.rete.Rete;

public class TemporalFunctions implements FunctionGroup {

	/**
	 * 
	 */
		private ArrayList<Function> funcs = new ArrayList<>();

	public TemporalFunctions() {
		super();
	}

	public String getName() {
		return TemporalFunctions.class.getSimpleName();
	}

	public List<Function> listFunctions() {
		return funcs;
	}

	public void loadFunctions(Rete engine) {
		CalculateTemporalDistanceFunction ctd = new CalculateTemporalDistanceFunction();
		funcs.add(ctd);
		engine.declareFunction(ctd);
		SetTemporalDistanceFunction setdist = new SetTemporalDistanceFunction();
		funcs.add(setdist);
		engine.declareFunction(setdist);
	}

}
