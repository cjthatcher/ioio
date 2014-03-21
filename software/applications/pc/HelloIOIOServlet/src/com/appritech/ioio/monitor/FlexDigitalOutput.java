package com.appritech.ioio.monitor;

import org.w3c.dom.Element;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class FlexDigitalOutput extends FlexIOBase
{
	public FlexDigitalOutput(int pinNum, Element xml)
	{
		super(pinNum);
		this.xmlElement = xml;
		eventName = Integer.toString(pinNum);
	}
	private Element xmlElement;
	private DigitalOutput dout;
	private Boolean needsInvert = false;
	private float lastValue = -1.0f;
	@SuppressWarnings("unused")
	private String eventName;
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException
	{
		if(xmlElement == null)
		{
			//This is for the LED. 
			dout = ioio.openDigitalOutput(pinNum);
			return;
		}
		
		String subType = xmlElement.getAttribute("subtype");
		if(subType.endsWith("OD"))
		{
			dout = ioio.openDigitalOutput(pinNum, DigitalOutput.Spec.Mode.OPEN_DRAIN, true);
			needsInvert = true;			//Open Drain mode works backwards. True leaves pin floating (i.e. LED not on), and false pulls it to ground (i.e. LED on)
		}
		else if(subType.endsWith("FL"))		//Floating
		{
			dout = ioio.openDigitalOutput(pinNum);
		}
	}
	
	@Override
	public void close()
	{
		dout.close();
	}
	
	@Override
	public float update(float val) throws InterruptedException, ConnectionLostException
	{
		if(dout == null)
			return val;
		
		if(val != lastValue)
		{
			lastValue = val;
			Boolean output = val > 0.5f;
			if(needsInvert)
				output = !output;
			
			System.out.println("Dout valueChanged. pinNum: " + pinNum + "\t output: " + output);
			
			dout.write(output);
		}
		return lastValue;
	}
	
	@Override
	public float getCalibratedValue() {
		return lastValue;
	}
}
