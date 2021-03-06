package de.vogella.rc.intro.first;

import de.jaret.examples.timebars.fancy.model.FancyEvent;
import de.jaret.util.date.IntervalImpl;
import de.jaret.util.ui.timebars.model.DefaultRowHeader;
import de.jaret.util.ui.timebars.model.DefaultTimeBarModel;
import de.jaret.util.ui.timebars.model.DefaultTimeBarRowModel;
import de.jaret.util.ui.timebars.model.TimeBarModel;
import de.vogella.rc.intro.first.ParseTDRSSFile;


public class CreateTDRSSModel {
	 
	
	public TimeBarModel Modelcreation(String inputfile) {
		
		DefaultTimeBarModel ghantChartModel = new DefaultTimeBarModel();
		
		DefaultRowHeader secondRowHeader = new DefaultRowHeader("Pass Schedule");
		DefaultTimeBarRowModel secondRowTBR = new DefaultTimeBarRowModel(secondRowHeader);
	
		ParseTDRSSFile currentPassFile = new ParseTDRSSFile(inputfile);
		
		currentPassFile.OpenFile();
		currentPassFile.parseElements();
		
		boolean aosTrue      = false;
		boolean losTrue      = false;
		boolean eclipseStart = false;
			
		TDRSSInterval aosLosInterval   = new TDRSSInterval();
		USNInterval usnAosLosInterval  = new USNInterval();
		SunInterval sunLightInterval  = new SunInterval();
		EclipseInterval eclipseInterval  = new EclipseInterval();
	
		for (int i = 0; i<currentPassFile.getElementNameArraySize(); i++) {
			
			/**
			 * AOS/LOS 
			 */
			
			
			if (oe.getElementNameArrayValue(i).contains("AOS")){			
				
				aosLosInterval = new TDRSSInterval();
				
				aosTrue = true;
		
				aosLosInterval.setBegin(oe.getElementTimeArrayValue(i));		
				
			}
					
			if ((oe.getElementNameArrayValue(i).contains("LOS") && aosTrue)){
				    
				losTrue = true;
				aosLosInterval.setEnd(oe.getElementTimeArrayValue(i));
				
			}
			
			if (aosTrue && losTrue){
				
				secondRowTBR.addInterval(aosLosInterval);
				
				
				
				aosTrue = false;
				losTrue = false;	
			}
			
			
			/**
			 * Eclipse/Sunlight 
			 */
		
			
			if (oe.getElementNameArrayValue(i).contains("Sunlight_Start")){
				
				sunLightInterval = new SunInterval();
				sunLightInterval.setBegin(oe.getElementTimeArrayValue(i));
			
				/**
				 * Account for not setting the first value to an eclipseEnd
				 */
				
				if (eclipseStart){
				    eclipseInterval.setEnd(oe.getElementTimeArrayValue(i));
				    firstRowTBR.addInterval(eclipseInterval);
				}
			
			}
			
			if (oe.getElementNameArrayValue(i).contains("Sunlight_Stop")){
			    
				sunLightInterval.setEnd(oe.getElementTimeArrayValue(i));
				firstRowTBR.addInterval(sunLightInterval);
		        
				
				eclipseInterval = new EclipseInterval();
				eclipseInterval.setBegin(oe.getElementTimeArrayValue(i));
				
				eclipseStart = true;
				
			}
			
			/**
			 * Event
			 */
			
			
			if (oe.getElementNameArrayValue(i).startsWith("Start")){
				FancyEvent eventInterval = new FancyEvent(oe.getElementTimeArrayValue(i));
				eventInterval.setLabel(oe.getElementNameArrayValue(i));
				thirdRowTBR.addInterval(eventInterval);
				
			}
			
		}
		
		ghantChartModel.addRow(firstRowTBR);
		ghantChartModel.addRow(secondRowTBR);
		ghantChartModel.addRow(thirdRowTBR);
        
		return ghantChartModel;
	
	}
	

	
}
	
