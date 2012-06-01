package org.sat4j.tools;

import info.monitorenter.gui.chart.ITrace2D;


public class ChartBasedVisualizationTool implements IVisualizationTool {

	private ITrace2D trace;
	private int i;
	
	public ChartBasedVisualizationTool(ITrace2D trace){
		this.trace=trace;
		i=0;
	}

	public void addPoint(double x, double y) {
		if(i==4){
			trace.addPoint(x, y);
			i=0;
		}
		i++;
	}

	public void addInvisiblePoint(double x, double y) {
		
	}

	public void init() {
		trace.removeAllPoints();
		i=0;
		
	}

	public void end() {
		
	}

	

}