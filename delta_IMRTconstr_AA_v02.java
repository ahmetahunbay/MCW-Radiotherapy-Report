package sample.control;



import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;





public class delta_IMRTconstr_AA_v02 {
	readIMRTconstr_v05 origA;
	readIMRTconstr_v05 origB;
	
		
	ArrayList <String> structs_only_in_A = new ArrayList <String>();
	ArrayList <String> structs_only_in_B = new ArrayList <String>();
	ArrayList<Unmatched> unmatched;	
    int pareto_changed = 0;
	
	
	
	public class Unmatched{	  
	  String index1;
	  String index2;
	  String param;
	  String name;
	  String type;
	  Boolean old;
	  String[] vals = new String[2];
	  
	  
	  public Unmatched(String name, String type, String index1, boolean old) {
	    this.name = name;
	    this.type = type;
	    this.old = old;
	    this.index1 = index1;
	  }
	  
	  public Unmatched(String name, String type, String index1, String index2, 
	      String param, String[] vals) {
	    this.index1 = index1;
	    this.index2 = index2;
        this.name = name;
        this.type = type;
        this.vals = vals;
        this.param = param;
      }
	  
	  @Override
	  public String toString() {
	    if(old != null) {
	      String ret = (old ? "Deleted CF ":"Added CF ") + name + "-" + type+ "(" + index1 + ")";
	      if(!old) {
	    	  LinkedHashMap<String,String> newCF = origB.costFunctions.get(name).get(Integer.parseInt(index1));
	    	  if(type.equals("o_v") || type.equals("o_q") || type.equals("mxt")) {
		    	  String concat = " -- isoconstraint: " + newCF.get("isoconstraint") + " applyshrinkmargintooars: " +
		    			  newCF.get("applyshrinkmargintooars");
		    	  
		    	  if(type.equals("o_v") || type.equals("o_q")) {
		    		  concat = concat + " ref_dose: " + Double.parseDouble(newCF.get("thresholddose"))*100.;
		    	  }
		    	  
		    	  if(newCF.containsKey("shrinkmargintarget")) {
		    		  concat = concat + " shrinkmargintarget: " + newCF.get("shrinkmargintarget");
		    	  }
		    	  
		    	  ret = ret + concat;
	    	  }
  
	      }
	      return ret;
	    } else {
	    	String output_text = "";
	      switch (type) { 
	      case "mxd" :
	    	  output_text =  "Changed " + name + " Maximum Dose " + param 
	          + " " + vals[0] + " (" + index1 + ")" + " --> " + vals[1]+ " (" + index2 + ")";
	    	  break;
	      case "o_v" :
	    	  output_text =  "Changed " + name + " Overdose DVH " + param 
	          + " " + vals[0] + " (" + index1 + ")" + " --> " + vals[1]+ " (" + index2 + ")";
	    	  break;
	      case "o_q" :
	    	  if(param.contentEquals("isoconstraint")) {
	    	  output_text =  "Changed " + name + " Quadratic Overdose " + param 
	          + " " + Double.parseDouble(vals[0])* 100. + " (" + index1 + ")" + " --> " + Double.parseDouble(vals[1]) * 100.+ " (" + index2 + ")";
	    	  }else {
	    		  output_text =  "Changed " + name + " Quadratic Overdose " + param 
	    		          + " " + vals[0] + " (" + index1 + ")" + " --> " + vals[1]+ " (" + index2 + ")"; 
	    	  }
	    	  break;
	      }
	      
	      return output_text;
	      
	    }  
	  }
 
	}
	

	ArrayList <delta> delta_A = new ArrayList <delta> ();
	ArrayList <delta> delta_B = new ArrayList <delta> ();
	public class delta {
		int delta_struct ;
		ArrayList <Integer> delta_constraints ;
	}

	
	
	

	
	
	
	
	
	public delta_IMRTconstr_AA_v02 (readIMRTconstr_v05 A, readIMRTconstr_v05 B) {
		//stores changed costfunctions
		unmatched = new ArrayList<Unmatched>();	
		origA = A;
		origB = B;
			
//		if(A.pareto != B.pareto) 
//			pareto_changed = 1;
		
		Set<String> AKeys = A.costFunctions.keySet();
		Set<String> BKeys = B.costFunctions.keySet();

		
		for(String key: AKeys) {
			//fills SOIA
			if(!B.costFunctions.containsKey(key)) {
				structs_only_in_A.add(key);
				continue;
			}
			
			
						
			ArrayList<LinkedHashMap<String,String>> ACF = 
			    (ArrayList<LinkedHashMap<String,String>>)A.costFunctions.get(key).clone();
			ArrayList<LinkedHashMap<String,String>> BCF = 
			    (ArrayList<LinkedHashMap<String,String>>)B.costFunctions.get(key).clone();

			//iterates through like keys(names)
			for(int i = 0; i< ACF.size();i++) {	
			    //iterates through all of B to check for matches with A
				for(int j = 0; j< BCF.size(); j++) {
				  
				  Set<String> ACFSet = A.costFunctions.get(key).get(i).keySet();
	              Set<String> BCFSet = B.costFunctions.get(key).get(j).keySet();
	              
	              //compares types
	              String AType = ACF.get(i).get("type");
	              String BType = BCF.get(j).get("type");
	              if(!AType.equals(BType)) {
	                continue;
	              }
	              
	              //Iterates through costfunction keys looking for matches
	              boolean same = true;
	              for(String miniKey: ACFSet) {
	                if(miniKey.equals("relativeimpact") || miniKey.equals("isoeffect") 
	                    || miniKey.equals("weight") || miniKey.equals("index")) {
	                    continue;
	                  }
	               
	                if(!BCF.get(j).containsKey(miniKey) || !ACF.get(i).get(miniKey)
	                    .equals(BCF.get(j).get(miniKey))) {
	                  same = false;
	                  
	                  break;
	                }
	              }
	              if(same) {
	                //remove matches
	                ACF.remove(i);
	                BCF.remove(j);
	                i--;
	                j--;
	                break;
	              }

				}
			}

			//iterates for close matches
			for(int i = 0; i< ACF.size();i++) {

			  for(int j = 0; j< BCF.size(); j++) {
                
                Set<String> ACFSet = ACF.get(i).keySet();
                Set<String> BCFSet = BCF.get(j).keySet();
                
                //checks type
                String AType = ACF.get(i).get("type");
                String BType = BCF.get(j).get("type");
                if(!AType.equals(BType)) {
                  continue;
                }
                 
                //adds differences in close match to unmatched arraylist
                for(String miniKey: ACFSet) {
                  if(miniKey.equals("relativeimpact") || miniKey.equals("isoeffect") 
                      || miniKey.equals("weight")|| miniKey.equals("index")){
                    continue;
                  }
                  if(!BCF.get(i).containsKey(miniKey) || !ACF.get(i).get(miniKey)
                      .equals(BCF.get(j).get(miniKey))) {
                  
                    unmatched.add(new Unmatched(key,ACF.get(i).get("type"), ACF.get(i).get("index"),
                        BCF.get(j).get("index"),  miniKey,new String[]{(ACF.get(i).get(miniKey)),
                            (BCF.get(i).get(miniKey))}));
                  }                  
                }
                
                BCF.remove(j);              
                break;

              }
			  
			  ACF.remove(i);
			  i--;
			}
			
			//iterates for new/old cost functions
			for(int i = 0; i< ACF.size();i++) {
              unmatched.add(new Unmatched(key,ACF.get(i).get("type"),ACF.get(i).get("index"),false));
            }		
			for(int i = 0; i< BCF.size();i++) {
			  unmatched.add(new Unmatched(key,BCF.get(i).get("type"),BCF.get(i).get("index"),false));
            }
          
      }	
		
	  for(String key: BKeys) {
        if(!AKeys.contains(key)) {
            structs_only_in_B.add(key);
        }
      }

	}
	
	
}
	
	
		
		