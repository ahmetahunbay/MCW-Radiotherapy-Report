package sample.control;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedHashMap;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;



import java.util.Scanner;
import java.util.Set;



public class readARTfile {
	
	
	String ref_plan_name;
	String ref_studyset_name;
	double[] reg_matrix = new double[3];
	double[] ref_isocenter = new double[3];
	String daily_studyset_name;
	String assigned_studyset_name;
	int method;
	int daily_isocenter_type;
	String[] iso_shift = new String[3];
	double[] mr_mv = {0.05 , 0.18, -0.02};
	
	public readARTfile(File file) {
		// TODO Auto-generated constructor stub
		try {
		Scanner scanner = new Scanner(file);
		Hashtable<String, String[]> nameSet = new Hashtable<String, String[]>();
		
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.contains(" ")) {
				String key = line.substring(0, line.indexOf(' '));
				line = line.substring(line.indexOf(' ')).trim();
				String[] value = line.split(" +");
				nameSet.put(key,value);
			}
		}
		
		ref_plan_name = nameSet.get("!REFERENCE_PLAN_NAME")[0];
		
		ref_studyset_name = nameSet.get("!REFERENCE_STUDYSET_NAME")[0];
		
		reg_matrix[0] = Double.parseDouble(nameSet.get("!REGISTRATION_MATRIX")[3]);
		reg_matrix[1]=Double.parseDouble(nameSet.get("!REGISTRATION_MATRIX")[7]);
		reg_matrix[2] = Double.parseDouble(nameSet.get("!REGISTRATION_MATRIX")[11]);
		
		ref_isocenter[0] = Double.parseDouble(nameSet.get("!REFERENCE_ISOCENTER_POINT")[0]);
		ref_isocenter[1] = Double.parseDouble(nameSet.get("!REFERENCE_ISOCENTER_POINT")[1]);
		ref_isocenter[2] = Double.parseDouble(nameSet.get("!REFERENCE_ISOCENTER_POINT")[2]);
		
		daily_studyset_name = nameSet.get("!DAILY_STUDYSET_NAME")[0];
		
		assigned_studyset_name = nameSet.get("!ASSIGNED_STUDYSET_NAME")[0];
		
		method = Integer.parseInt(nameSet.get("!METHOD")[0]);
		
		daily_isocenter_type = Integer.parseInt(nameSet.get("!DAILY_ISOCENTER_TYPE")[0]);
		

		
		for(int i=0; i< 3; i++) {
			iso_shift[i] = String.format("%.2f",(ref_isocenter[i]/10. - reg_matrix[i]/10. - mr_mv[i]));
		}
		
		
		
//		System.out.println("ref_plan_name " + ref_plan_name + "\n");
//		System.out.println("ref_studyset_name " + ref_studyset_name + "\n");
//		System.out.println("reg_matrix " + reg_matrix[0] + " " + reg_matrix[1] + " " + reg_matrix[2] + "\n");
//		System.out.println("ref_isocenter " + ref_isocenter[0] + " " + ref_isocenter[1] + " " + ref_isocenter[2] + "\n");
//		System.out.println("daily_studyset_name " + daily_studyset_name + "\n");
//		System.out.println("assigned_studyset_name " + assigned_studyset_name + "\n");
//		System.out.println("method " + method + "\n");
//		System.out.println("daily_isocenter_type " + daily_isocenter_type + "\n");
		
		

//		String ref_studyset_name;
//		double[] reg_matrix = new double[3];
//		double[] ref_isocenter = new double[3];
//		String daily_studyset_name;
//		String assigned_studyset_name;
//		int method;
//		int daily_isocenter_type;
//	
//		!ASSIGNED_STUDYSET_NAME
//		!METHOD
//		!DAILY_ISOCENTER_TYPE
		
		}catch(FileNotFoundException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }		
	}
	
}