package sample.control;

import java.awt.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;






import java.util.Scanner;
import java.util.Set;

// v02: this is also reading line by line, all lines of the IMRTconstraints file into structures
//  v03: adding functionality to modify IMRT constraints
// adding Ahmet's indexing

public class readIMRTconstr_v05 {
	LinkedHashMap<String, ArrayList<LinkedHashMap<String,String>>> costFunctions = new LinkedHashMap<String, ArrayList<LinkedHashMap<String, String>>>();
	ArrayList <StructsWithConstraints> structs = new ArrayList <StructsWithConstraints>();;
	int pareto;
	int state_p = 0;  // this is for shrink margin OAR if encountered before any OAR
	int current_target = 0;
    linebyline LBL ;
	
	
	public class linebyline {
		ArrayList <String> header = new ArrayList  <String> ();
		   ArrayList <VOIdef> VOIDEFs = new ArrayList <VOIdef>();
		ArrayList <String> footer = new ArrayList <String> ();
	}
	
   public class VOIdef {
	   ArrayList <String> header = new ArrayList <String> ();
	   ArrayList <COSTFUNCTION> CF = new ArrayList <COSTFUNCTION>();
   }
	
	public class COSTFUNCTION {
		ArrayList <String> costfunctionlines  = new ArrayList <String> ();
	}
	
	public class IMRTconstraint {
		String Type ;
		String enabled;
		String manual;
		String weight;
		String refDose;
		String multicrit;
		String power;
		String isoconstraint;
		String isoeffect;
		String relimpact;
		String shrinkmargin;
	}
		

	public class StructsWithConstraints {
		String StructureName;
		ArrayList <IMRTconstraint> IMRTcontraints ;
	}

	
	
	
	
	public void adjust_constraint(String structure_name, String type, double dose,  double new_dose, double val2) {
		fwrite F = new fwrite("C:/Users/eahunbay/Desktop/Working Directory/ic.txt");
		F.write(dose+" "+structure_name);
		F.close();
		
		for(int i =0; i< structs.size(); i++) {
			if(structs.get(i).StructureName.contentEquals(structure_name)) {
				
				if (structs.get(i).IMRTcontraints.size() > 0) {
					F = new fwrite("C:/Users/eahunbay/Desktop/Working Directory/ic3.txt");

					for (int j=0; j< structs.get(i).IMRTcontraints.size(); j++) {
						
						
						if(structs.get(i).IMRTcontraints.get(j).Type.contentEquals(type)) {
							

							F.write(type+" "+structs.get(i).IMRTcontraints.get(j).Type + "\n");

							
							
							
							switch (type) {
							case "Overdose DVH":
								if(Double.parseDouble(structs.get(i).IMRTcontraints.get(j).refDose) == dose) {
									structs.get(i).IMRTcontraints.get(j).refDose = String.format("%.1f",new_dose);
									structs.get(i).IMRTcontraints.get(j).isoconstraint = String.format("%.1f",val2);
									LBL.VOIDEFs.get(i).CF.get(j).costfunctionlines.set(15, "        thresholddose=" +   String.format("%.1f",new_dose*0.01)) ;
									LBL.VOIDEFs.get(i).CF.get(j).costfunctionlines.set(16, "        isoconstraint=" +   String.format("%.1f",val2)) ;
									
									
								}
								break;
							}
							
						}
					}
					F.close();

				}
			}
		}

		
		
		
		
	}
	                  
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	////////////////////////////////////
	//  generate String output
	//
	//  will update with compare unmatched info
	/////////////////////////////////
	
	
	
	public String generate_string_output(delta_IMRTconstr_AA_v02 delta,  Integer i_IC) {
		
		
		//   Trying to send out all text as delimited with ; so it can be put to a table in the mim workflow
		String structureName ="";
     if(pareto == 1) {
    	 structureName = "PARETO" + ";" + delta.pareto_changed + ";" + "^^^";
     }else {
    	 structureName = "Constrained" + ";" + delta.pareto_changed + ";" + "^^^" ;
     }
    	 
    	 int changed_obj = 0;
    	 
		for(int i =0; i< structs.size(); i++) {
			structureName = structureName +structs.get(i).StructureName+"__"; 

		
			if (structs.get(i).IMRTcontraints.size() > 0) {
				int j = 0;
				
				
				changed_obj = 0;
				
				
		if(i_IC == 1 ) {		
				if(! delta.structs_only_in_B.isEmpty()) 
					for(int ii=0; ii< delta.structs_only_in_B.size(); ii++) 
						if(delta.structs_only_in_B.get(ii).contentEquals(structs.get(i).StructureName)) 
							changed_obj = -1;
				if(! delta.unmatched.isEmpty())
					for(int ii=0; ii< delta.unmatched.size(); ii++)
						if(delta.unmatched.get(ii).name.contentEquals(structs.get(i).StructureName)) 
							if(delta.unmatched.get(ii).index1.contentEquals(Integer.toUnsignedString(j))) 
								changed_obj = 1;						
		}					else {
			if(! delta.structs_only_in_A.isEmpty()) 
				for(int ii=0; ii< delta.structs_only_in_A.size(); ii++) 
					if(delta.structs_only_in_A.get(ii).contentEquals(structs.get(i).StructureName)) 
						changed_obj = -1;	if(! delta.unmatched.isEmpty())
				for(int ii=0; ii< delta.unmatched.size(); ii++)
					if(delta.unmatched.get(ii).name.contentEquals(structs.get(i).StructureName)) 
						if(delta.unmatched.get(ii).index2.contentEquals(Integer.toUnsignedString(j))) 
							changed_obj = 1;
		}
									
								

						
					
				
				
				
				
				structureName = structureName   
				        + structs.get(i).IMRTcontraints.get(j).Type+"__"
						+ structs.get(i).IMRTcontraints.get(j).enabled+"__"
						+ structs.get(i).IMRTcontraints.get(j).manual+"__"
						+ structs.get(i).IMRTcontraints.get(j).weight+"__"
						+ structs.get(i).IMRTcontraints.get(j).refDose+"__"
						+ structs.get(i).IMRTcontraints.get(j).multicrit+"__"
						+ structs.get(i).IMRTcontraints.get(j).power+"__"
						+ structs.get(i).IMRTcontraints.get(j).shrinkmargin+"__"
						+ structs.get(i).IMRTcontraints.get(j).isoconstraint+"__"
						+ structs.get(i).IMRTcontraints.get(j).isoeffect+"__"
						+ structs.get(i).IMRTcontraints.get(j).relimpact+"__"
						+ changed_obj +";";
				
				if (structs.get(i).IMRTcontraints.size() > 1) {
					
				for (j=1; j< structs.get(i).IMRTcontraints.size(); j++) {
					
					changed_obj = 0;
				if(i_IC == 1) {
					if(! delta.unmatched.isEmpty())
						for(int ii=0; ii< delta.unmatched.size(); ii++)
							if(delta.unmatched.get(ii).name.contentEquals(structs.get(i).StructureName)) 
								if(delta.unmatched.get(ii).index1.contentEquals(Integer.toUnsignedString(j))) 
									changed_obj = 1;
				}else {
					if(! delta.unmatched.isEmpty())
						for(int ii=0; ii< delta.unmatched.size(); ii++)
							if(delta.unmatched.get(ii).name.contentEquals(structs.get(i).StructureName)) 
								if(delta.unmatched.get(ii).index2.contentEquals(Integer.toUnsignedString(j))) 
									changed_obj = 1;
				}
					
					
					
					structureName = structureName + "  __" 
					        + structs.get(i).IMRTcontraints.get(j).Type+"__"
							+ structs.get(i).IMRTcontraints.get(j).enabled+"__"
							+ structs.get(i).IMRTcontraints.get(j).manual+"__"
							+ structs.get(i).IMRTcontraints.get(j).weight+"__"
							+ structs.get(i).IMRTcontraints.get(j).refDose+"__"
							+ structs.get(i).IMRTcontraints.get(j).multicrit+"__"
							+ structs.get(i).IMRTcontraints.get(j).power+"__"
							+ structs.get(i).IMRTcontraints.get(j).shrinkmargin+"__"
							+ structs.get(i).IMRTcontraints.get(j).isoconstraint+"__"
							+ structs.get(i).IMRTcontraints.get(j).isoeffect+"__"
							+ structs.get(i).IMRTcontraints.get(j).relimpact+"__"
									+ changed_obj+";";
				}
				}
			}else {
					structureName = structureName + "  __  __  __  __  __  __  __  __  __  __ ;";
				}
			}			
							
		return structureName;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/////////////////////
	//   write a IMRTconstraints file
	//
	//
	//
	/////////////////////////////
	
	public void write_file(File out_file) {
		
		
		try {
			FileOutputStream fos = new FileOutputStream(out_file);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			
			
			for (int i = 0; i < LBL.header.size() ; i++) {
				bw.write(LBL.header.get(i));
				bw.newLine();
			}
			
			for (int i = 0; i < LBL.VOIDEFs.size() ; i++) {
				
				bw.write("!VOIDEF");
				bw.newLine();
				for (int j = 0; j< LBL.VOIDEFs.get(i).header.size(); j++){
					bw.write(LBL.VOIDEFs.get(i).header.get(j));
					bw.newLine();
				}
				for (int j = 0; j< LBL.VOIDEFs.get(i).CF.size(); j++){
					bw.write("    !COSTFUNCTION");
					bw.newLine();

					for (int k = 0; k< LBL.VOIDEFs.get(i).CF.get(j).costfunctionlines.size(); k++){
						bw.write(LBL.VOIDEFs.get(i).CF.get(j).costfunctionlines.get(k));
						bw.newLine();
					}

				
					bw.write("    !END");
					bw.newLine();

				
				}

				
				bw.write("!END");
				bw.newLine();
				
			}
			
			for (int i = 0; i < LBL.footer.size() ; i++) {
				bw.write(LBL.footer.get(i));
				bw.newLine();
			}
			bw.close();
			
			
			
			
			
		}catch(IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }		
		 
		
		
	}
	
	
	
	
	
	
	
	
	////////////////////////////////////
	////////////    CONSTRUCTOR
	//
	//
	//
	//
	///////////////////////////////////////
	public readIMRTconstr_v05(File file) {
		// TODO Auto-generated constructor stub
		try {
		Scanner scanner = new Scanner(file);
        LBL = new linebyline();
        
		int reading_state = 0; // 0: in the header, 1: reading a VOI, 
				
		while (scanner.hasNextLine()) {
		   String line = scanner.nextLine();

		   
		   
		   if(line.contentEquals("!VOIDEF")) {
				  VOIdef vd = new VOIdef();
	    		  LBL.VOIDEFs.add(vd);
	    		  reading_state = 1;			   

			   
			   line = scanner.nextLine();
//				  LBL.VOIDEFs.get(LBL.VOIDEFs.size()-1).header.add(line);

			   String nameHeld = line.substring(line.indexOf('=')+1);
			   ArrayList<LinkedHashMap<String,String>> costs = new ArrayList<LinkedHashMap<String,String>>();
			   
			   int j = 0;   //AA

			   while(!line.contentEquals("!END")) {
				   if(line.contentEquals("    !COSTFUNCTION")) {
						  COSTFUNCTION cfnew = new COSTFUNCTION();
						  LBL.VOIDEFs.get(LBL.VOIDEFs.size()-1).CF.add(cfnew);
						  reading_state = 2;
					   
					   
					   line = scanner.nextLine();
		   
					   LinkedHashMap<String, String> ind = new LinkedHashMap<String, String>();
					   
					   ind.put("index", ""+j);   //AA
					   j++;    //AA
					   
					   
					   while(!line.contentEquals("    !END")) {
				    		  LBL.VOIDEFs.get(LBL.VOIDEFs.size()-1).CF.get(LBL.VOIDEFs.get(LBL.VOIDEFs.size()-1).CF.size() -1).costfunctionlines.add(line);

						   if(line.contains("=")) {
							   String potKey =line.substring(0,line.indexOf('=')).trim();
							   if(ind.get(potKey) != null) {
								   int i = 2;
								   while(ind.get(potKey + i) != null) {
									   i++;
								   }
								   potKey = potKey +i;
							   }	
							   
							   String potValue = line.substring(line.indexOf('=') + 1); //AA

							   ind.put(potKey,line.substring(line.indexOf('=') + 1));
						   }
						   line = scanner.nextLine();
					   }
					   costs.add(ind);
				   }
				   
				   if (reading_state == 1) {
						  LBL.VOIDEFs.get(LBL.VOIDEFs.size()-1).header.add(line);
				   }
				   
				   line = scanner.nextLine();
			   }
			   costFunctions.put(nameHeld, costs);
		   } else {
			   
			   if(reading_state == 0 ) 
			    	  LBL.header.add(line);
			   else
				   LBL.footer.add(line);
			   
			   if (line.contains("!PARETOMODE"))
				   pareto = Integer.parseInt(line.substring(12).trim());
		   }
		}
		
		
		
		
		
		separate_constraints();
		
		scanner.close();
		}	catch(FileNotFoundException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }		
	}
	


	
	
	
	
	
	
	
	
	///////////////////////////////
	//   SEPARATE 
	//
	//
	//
	//
	///////////////////////////////////////
	
	
	
	public void separate_constraints() {
		
		state_p = 0;
		Set<String> setOfKeys = costFunctions.keySet();		
		
		for(String key: setOfKeys) {
			StructsWithConstraints S = new StructsWithConstraints();
			S.StructureName = key;
			S.IMRTcontraints = new ArrayList<IMRTconstraint>();
			current_target = 0;
			for(int i=0;i<costFunctions.get(key).size(); i++) {
				IMRTconstraint I = new IMRTconstraint();

				if(Double.parseDouble(costFunctions.get(key).get(i).get("status")) > 0.0) {
					I.enabled = "On";
				}else I.enabled = "Off";
				if(Double.parseDouble(costFunctions.get(key).get(i).get("manual")) > 0.0) {
					I.manual = "On";
				}else I.manual = " ";
				if(Double.parseDouble(costFunctions.get(key).get(i).get("multicriterial")) > 0.0) {
					I.multicrit = "On";
				}else I.multicrit = " ";
				
				I.weight = String.format("%.2f",   Double.parseDouble( costFunctions.get(key).get(i).get("weight") )  );
				if(Double.parseDouble(costFunctions.get(key).get(i).get("relativeimpact")) > 0.0) {
					I.relimpact = String.format("%.2f",Double.parseDouble(costFunctions.get(key).get(i).get("relativeimpact")));
				}else I.relimpact = " ";
				
				
	// SHRINK MARGINS
				if( costFunctions.get(key).get(i).containsKey("shrinkmargintarget") ) {
					I.shrinkmargin = costFunctions.get(key).get(i).get("shrinkmargintarget") + " : " + 
							String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("targetmargin"))*0.1);
				}else {
					I.shrinkmargin = "";
				}

				int i_sm = 2;
				while (costFunctions.get(key).get(i).containsKey("shrinkmargintarget"+i_sm)) {
					I.shrinkmargin = I.shrinkmargin + " <br> "+costFunctions.get(key).get(i).get("shrinkmargintarget"+i_sm) + " : " + 
							String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("targetmargin"+i_sm))*0.1);
					i_sm ++;
				}
				if(state_p == 1 && costFunctions.get(key).get(i).get("applyshrinkmargintooars").contentEquals("1")) {
					if(I.shrinkmargin.isBlank()) {
						I.shrinkmargin = 	"OAR : " + String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("oarsmargin"))*0.1);
					}else
						I.shrinkmargin = I.shrinkmargin + "<br> OAR : " +	String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("oarsmargin"))*0.1);

				}
///////////////////////////////
				
				 I.power = " ";

				if( Double.parseDouble(costFunctions.get(key).get(i).get("thresholddose")) > 0. ) {
					I.refDose = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("thresholddose"))  *100.);
				} else I.refDose = " ";		
				
				
				switch (costFunctions.get(key).get(i).get("type") ) {
				case "qp":
					I.Type = "Target Penalty " + String.format("%.0f",Double.parseDouble(costFunctions.get(key).get(i).get("refvolume")) * 100.) +"%";
					I.isoconstraint = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoconstraint")) * 100.);
					I.isoeffect = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoeffect")) * 100.);
					I.refDose = " ";
					current_target = 1;
					break;
				case "o_q":
					I.Type = "Quadratic Overdose";
					I.isoconstraint = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoconstraint")) * 100.);
					I.isoeffect = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoeffect")) * 100.);
					break;
				case "o_v":
					I.Type = "Overdose DVH";
					I.isoconstraint = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoconstraint")) );
					I.isoeffect = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoeffect")) );

					break;
				case "mxd":
					I.Type = "Maximum Dose";
					I.isoconstraint = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoconstraint")) * 100.);
					I.isoeffect = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoeffect")) * 100.);
					I.refDose = " ";
					break;					
				case "u_v":
					I.Type = "Underdose DVH";
					I.isoconstraint = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoconstraint")) );
					I.isoeffect = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoeffect")) );
					break;					
				case "po":
					I.Type = "Target EUD";
					I.isoconstraint = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoconstraint")) * 100.);
					I.isoeffect = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoeffect")) * 100.);
					I.refDose = " ";
					current_target = 1;
					break;		
				case "pa":
					I.Type = "Parallel";
					I.isoconstraint = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoconstraint")) );
					I.isoeffect = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoeffect")) );
					I.power = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("exponent")));
					break;		
				case "se":
					I.Type = "Serial";
					I.isoconstraint = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoconstraint")) * 100.);
					I.isoeffect = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoeffect")) * 100.);
					I.refDose = " ";
					I.power = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("exponent")));
					break;		
				case "u_q":
					I.Type = "Quadratic Underdose";
					I.isoconstraint = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoconstraint")) * 100.);
					I.isoeffect = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoeffect")) * 100.);
					break;		
				case "conf":
					I.Type = "Conformality";
					I.isoconstraint = String.format("%.2f",Double.parseDouble(costFunctions.get(key).get(i).get("isoconstraint"))) ;
					I.isoeffect = String.format("%.1f",Double.parseDouble(costFunctions.get(key).get(i).get("isoeffect")) * 100.);
					I.refDose = " ";

					break;		
					
					
				}
				S.IMRTcontraints.add(I);
				
			}
			if(current_target == 0) state_p = 1;
			structs.add(S);
			
			
		}
		
	}
	
	

}