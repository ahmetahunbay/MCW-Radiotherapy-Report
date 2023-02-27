package sample.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;



import java.util.Scanner;
import java.util.Set;



public class readRXAlog {
	
	
	
		String optim_time ;
		String optim_time_ATS = "skip";
	String optim_init = "skip";
	String premature = "NO";
	String rescale = "1.0";
	String type = "ATP";
	String optim_method = "From Segments";


	
	
	public double time_s_d(String time_s) {
		double time_d;
		
		String[] date_sl = time_s.split(" ");
		String[] time_sl = date_sl[3].split(":");
		
		time_d = Double.parseDouble(time_sl[2]) + 
				Double.parseDouble(time_sl[1]) * 60. + 
				Double.parseDouble(time_sl[0])* 3600. ;
		
		
		
		
		return time_d;
	}
	
	
	
	
	public readRXAlog(File file) {
		// TODO Auto-generated constructor stub
		try {
		Scanner scanner = new Scanner(file);

			double ref_time = -1;
			double latest_time = -1; 
		while (scanner.hasNextLine()) {
		   String line = scanner.nextLine();
		   
		   if(line.contains("<Time>")) {
			   String time_s = line.substring(line.indexOf("<Time>")+6, line.indexOf("/Time")-1);
//			   System.out.println(time_s+"\n");
			   latest_time = time_s_d(time_s);
			   if(ref_time == -1) 
				   ref_time = latest_time;
		   }
		   
		   
		   
		   
		   if(line.contains("seconds for optimization.")) {
               optim_time = String.format("%.1f", (latest_time - ref_time)/60.);
    	 	    optim_time_ATS = line.substring(line.indexOf("<Message>")+9, line.indexOf("seconds")-1);
                type = "ATS";
		   }
				   
				   
				  if( line.contains("Warm-Start Optimization Completed") ) 
		               optim_time = String.format("%.1f", (latest_time - ref_time)/60.);

				  if( line.contains("Computing beamlet doses") ) 
		               optim_method = "From Fluence";
				   
				   if(line.contains("seconds for optimization init")) 
					   optim_init = line.substring(line.indexOf("<Message>")+9, line.indexOf("seconds")-1);
				   
				  //			   if(line.contains("optimization init"))
//			   optim_init = String.format("%.1f", Double.parseDouble(line.substring(line.indexOf("<Message>")+9, line.indexOf("seconds")-1))/60.);
//			   else
//	 	    optim_time = String.format("%.1f", Double.parseDouble(line.substring(line.indexOf("<Message>")+9, line.indexOf("seconds")-1))/60.);
			   
			   
		   if(line.contains("prematurely"))
			   premature="YES";

		   if(line.contains("Rescale:"))
			   rescale = line.substring(line.indexOf("Rescale:")+8, line.indexOf("</Message>")-1);
		   		
		}
		
//		if( ! optim_init.contentEquals("skip")  && ! optim_time_ATS.contentEquals("skip") )
// 	    optim_time = String.format("%.1f", ( Double.parseDouble(optim_init) + Double.parseDouble(optim_time_ATS) )/60.);

		
		if(type.contentEquals("ATP")) {
			if(optim_method.contentEquals("From Fluence")) {
				optim_method = "Shapes";
			} else {
				optim_method = "Weights";
			}
		}
		
		
		
		
		}catch(FileNotFoundException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }		
}
	
}