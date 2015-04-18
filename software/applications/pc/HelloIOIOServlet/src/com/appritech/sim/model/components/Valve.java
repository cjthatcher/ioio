package com.appritech.sim.model.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.appritech.sim.model.DrawingLine;
import com.appritech.sim.model.MimicContainer;

public class Valve extends Component {
	
	private double openPercentage = 1.0;
	private double maxFlow = Double.MAX_VALUE;
	
	private Component source;
	private Component sink;
	
	private HashMap<Pump, HashMap<Component, Double>> trueFlowPercentagesByPumpAndInput = new HashMap<Pump, HashMap<Component, Double>>();
	
	private String sinkName;
	
	public Valve(String name) {
		super(name);
	}
	
	public Valve(String name, String sinkName) {
		super(name);
		this.sinkName = sinkName;
	}
	
	public void setMaximumVolume(double d) {
		setMaxVolume(d);
	}
	
	public Valve(String name, String sinkName, float x, float y) {
		this(name, sinkName);
		this.x = x;
		this.y = y;
	}
	
	@Override
	public double getMaxVolume() {
		return super.getMaxVolume() * openPercentage;
	}
	
	@Override
	public void connectSelf(HashMap<String, Component> components) {
		sink = components.get(sinkName);
		sink.setSource(this);
	}

	@Override
	public List<DrawingLine> getConnectionLines() {
		if(sink != null) {
			return Collections.singletonList(new DrawingLine(x, y, sink.x, sink.y));
		}
		return null;
	}

	public double getOpenPercentage() {
		return openPercentage;
	}

	public void setOpenPercentage(double openPercentage) {
		this.openPercentage = openPercentage;
	}

	public double getMaxFlow() {
		return maxFlow;
	}

	public void setMaxFlow(double maxFlow) {
		this.maxFlow = maxFlow;
	}
	
	public void setSink(Component sink) {
		this.sink = sink;
	}
	
	public void setSource(Component source) {
		this.source = source;
	}

	@Override
	public double getPossibleFlowDown(Pump originPump, double oldMin, double volumePerSecond, MimicContainer mc, boolean thisIsTheRealDeal, Component input) {
		
		if("v12".equals(this.getName()) && thisIsTheRealDeal)
			System.out.println("asdf");
		
		double currentMin = openPercentage < oldMin ? Double.valueOf(openPercentage) : Double.valueOf(oldMin);
		if (mc.getOverrideMap().containsKey(this) && !thisIsTheRealDeal) {
			System.out.println(mc.getOverrideMap().get(this));
			currentMin = currentMin * mc.getOverrideMap().get(this);
		}
		
		double newMin = 0;
		if (hasMaxVolume) {
			newMin = sink.getPossibleFlowDown(originPump, currentMin, volumePerSecond, mc, false, this);
			
			if (newMin * volumePerSecond > getMaxVolume() && thisIsTheRealDeal) {
				double ratio = getMaxVolume() / (newMin * volumePerSecond);
				newMin = sink.getPossibleFlowDown(originPump, currentMin * ratio, volumePerSecond, mc, thisIsTheRealDeal, this);
			}
		} else {
			newMin = sink.getPossibleFlowDown(originPump, currentMin, volumePerSecond, mc, thisIsTheRealDeal, this);
		}

		addToComplaintLog(originPump, newMin * volumePerSecond, mc);
		if (thisIsTheRealDeal) {
			setTrueFlowPercent(originPump, newMin);
			setTrueFlowVolume(originPump, newMin * volumePerSecond);
		}
		System.out.println("Old Flow in: " + oldMin + ", " + "Override: " + mc.getOverrideMap().get(this) + ", new flow: " + newMin + "\r\n");
		
		
		return newMin;
	}

	@Override
	public double getPossibleFlowUp(Pump originPump, double oldMin, double volumePerSecond, MimicContainer mc, boolean thisIsTheRealDeal, Component output) {
		double currentMin = openPercentage < oldMin ? Double.valueOf(openPercentage) : Double.valueOf(oldMin);
		if (mc.getOverrideMap().containsKey(this)) {
			System.out.println(mc.getOverrideMap().get(this));
			currentMin = currentMin * mc.getOverrideMap().get(this);
		}
		
		double newMin = 0;
		if (hasMaxVolume) {
			newMin = source.getPossibleFlowUp(originPump, currentMin, volumePerSecond, mc, false, this);
			
			if (newMin * volumePerSecond > getMaxVolume() && thisIsTheRealDeal) {
				double ratio = getMaxVolume() / (newMin * volumePerSecond);
				newMin = source.getPossibleFlowUp(originPump, currentMin * ratio, volumePerSecond, mc, thisIsTheRealDeal, this);
			}
		} else {
			newMin = source.getPossibleFlowUp(originPump, currentMin, volumePerSecond, mc, thisIsTheRealDeal, this);
		}

		addToComplaintLog(originPump, newMin * volumePerSecond, mc);
		if (thisIsTheRealDeal) {
			setTrueFlowPercent(originPump, newMin);
			setTrueFlowVolume(originPump, newMin * volumePerSecond);
		}
		System.out.println("Old Flow in: " + oldMin + ", " + "Override: " + mc.getOverrideMap().get(this) + ", new flow: " + newMin + "\r\n");
		
		
		return newMin;

	}
	
}
