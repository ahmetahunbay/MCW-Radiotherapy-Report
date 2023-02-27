package sample.control;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class readDoCi_xml_better {

	//  THis works!!  Reads the dosimetric criteria from the file isodosesettings.xml
	
//	ArrayList <StructsWithGoals> structlist;
	
public class DoseGoal {
	int GoalType ;
	double Dose ;
	double Volume;
	double Tolerance;
}
	

public class StructsWithGoals {
	String StructureName;
	ArrayList <DoseGoal> dosegoals ;
}


ArrayList <StructsWithGoals> structs;

ArrayList <StructsWithGoals> structs_sorted = new ArrayList<StructsWithGoals>();





public void sort_structs() {

	int[] priority = new int[structs.size()];
	double[] dose_Val = new double[structs.size()]; 
	for(int i=0;i<structs.size();i++) {
		
		
		for(int j=0; j<structs.get(i).dosegoals.size(); j++) {
			if(structs.get(i).dosegoals.get(j).GoalType == 3 ||
					structs.get(i).dosegoals.get(j).GoalType == 5 ||
					structs.get(i).dosegoals.get(j).GoalType == 6 ||
					structs.get(i).dosegoals.get(j).GoalType == 9 ||
					structs.get(i).dosegoals.get(j).GoalType == 10) {

				if(structs.get(i).dosegoals.get(j).Dose + 0.01* i> dose_Val[i])
					dose_Val[i] = structs.get(i).dosegoals.get(j).Dose + 0.01* i;		
			}else {
				if( -structs.get(i).dosegoals.get(j).Dose - 0.01* i < dose_Val[i])
					dose_Val[i] = -structs.get(i).dosegoals.get(j).Dose - 0.01* i;				

			}
			
		}
		
	}
	
	Hashtable indices = new Hashtable();
	
	for(int i =0; i<dose_Val.length;i++) {
		indices.put(dose_Val[i],i);
	}
		Arrays.sort(dose_Val);
		
	
		for(int i=dose_Val.length-1; i>=0 ;i--) {
			if(dose_Val[i] > 0) {
			int j = (int) indices.get(dose_Val[i]);
			StructsWithGoals SWG = structs.get(j);
			
			structs_sorted.add(SWG);
			}
		}
		
		for(int i=0; i<dose_Val.length ;i++) {
			if(dose_Val[i] < 0) {
			int j = (int) indices.get(dose_Val[i]);
			StructsWithGoals SWG = structs.get(j);
			
			structs_sorted.add(SWG);
			}
		}
		
		
		
		
		structs = structs_sorted;
		
}









public readDoCi_xml_better( File textfile) {
	
    	structs = new ArrayList <StructsWithGoals>();
      	 
    	
  	  try {
		  Scanner myReader = new Scanner(textfile);




		  
		  myReader.useDelimiter("<DoseStructureParameter>");
 		  String doseStructureParameter;
 		  int iii = 0;
 		   while (myReader.hasNext()) {
 				  doseStructureParameter = myReader.next();
			     	 StructsWithGoals struc = new StructsWithGoals();
			     	 String strname = get_In(doseStructureParameter,"StructureName");
			     	 iii = iii + 1;
			     	 
			     	 
			     	 



			     	 
			     	String goallist = get_In(doseStructureParameter,"DoseGoalList");
			     	
 

			     	
                  if (strname.length() > 0 && goallist.length() > 0) {
                	  struc.StructureName = strname; 
                	  struc.dosegoals = new ArrayList <DoseGoal>();
                	  
                	  String[] goalstr = get_In_multi(goallist,"DoseGoal");
                	  

                	  
                	  
                	  
                	  for(int i=0; i<goalstr.length;i++) {
                		//  System.out.println(i+" "+goalstr[i]);
                		  if (goalstr[i].contains("<GoalType>")) {
                			  DoseGoal dg = new DoseGoal();

                			  if (goalstr[i].length() > 0 ){
                				  dg.GoalType = Integer.parseInt(get_In(goalstr[i],"GoalType"));
                				  dg.Dose = Double.parseDouble(get_In(goalstr[i],"Dose"));
                				  dg.Volume = Double.parseDouble(get_In(goalstr[i],"Volume"));
                				  dg.Tolerance = Double.parseDouble(get_In(goalstr[i],"Tolerance"));

                				  
                				  // volumes (cc) in the xml file are 1000 times as big
                				  if(dg.GoalType == 6 || dg.GoalType == 8 || dg.GoalType == 10 || dg.GoalType == 12)
                					  dg.Volume = dg.Volume *(float) 0.001;
                				  if(dg.GoalType == 10 || dg.GoalType == 12)
                					  dg.Tolerance = dg.Tolerance *(float) 0.001;
                				  
                			  }
                			  struc.dosegoals.add(dg);
                			  

                            	  

                		  }
                	  }
                	  
                	  

		    	  structs.add(struc);
                  }
					
                  
		  
 		   }
		  
	
		      myReader.close();
	  }catch(FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
    	
    	

    	
    sort_structs();	


    	
    	

	
}


public String get_In(String m, String delim) {    // this returns the inbetween string of input string m, between <delim> and </delim>
	String sbegin = "<"+delim+">";
	String send = "</"+delim+">";
	String[] sspl = m.split(sbegin);
	if (sspl.length > 1) {
	String sin = sspl[1];
	sspl = sin.split(send);
	sin = sspl[0];

	return sin;
	}
	else {
		return "";
	}
	
}
    	

public String[] get_In_multi(String m, String delim) {    // this returns the multiple inbetween strings of input string m, between <delim> and </delim>
	String s_begin = "<"+delim+">";
	String s_end = "</"+delim+">";
    ArrayList <String> s_in = new ArrayList<String>();

	String[] s_spl = m.split(s_begin);
	
       for (int i=0; i<s_spl.length; i++) {
    	   String sin = s_spl[i];
    		if(sin.contains(s_end)) {
    				String[] sspl = sin.split(s_end);
    		    s_in.add(sspl[0]);
       }
       }
		if(s_in.size()>0) {
String[] s_in_ = new String[s_in.size()];
for (int i=0; i< s_in.size(); i++) 	
	s_in_[i] = s_in.get(i);

	return s_in_;
		}else
			return new String[] {""};
	
}






}
	

