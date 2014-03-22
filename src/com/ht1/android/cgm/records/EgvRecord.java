package com.ht1.android.cgm.records;

import java.io.Serializable;
import java.util.List;

public class EgvRecord implements Serializable {
    public String displayTime = "---";
    public String bGValue = "---";
    public String trend ="---";
    public String trendArrow = "---";
    public String simpleTime = "---";
    public boolean isSpecial = false;
    
    private static final long serialVersionUID = 4654897646L;
    
    public void setDisplayTime (String input) {
    	this.displayTime = input;
    }
    
    public void setBGValue (String input) {
    	this.bGValue = input;
    }
    
    public void setTrend (String input) {
    	this.trend = input;
    }
    
    public void setTrendArrow (String input) {
    	this.trendArrow = input;
    }

	public void setSimpleTime(String format) {
		this.simpleTime = format;
		
	}
    

}

