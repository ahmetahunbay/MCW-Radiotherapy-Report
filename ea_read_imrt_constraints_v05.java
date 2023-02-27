package sample.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;

import java.io.FileNotFoundException; 

import com.mimvista.external.commands.XMimCommandFactory;
import com.mimvista.external.contouring.XMimContour;
import com.mimvista.external.control.XMimEntryPoint;
import com.mimvista.external.control.XMimSession;
import com.mimvista.external.data.XMimMutableNDArray;

import com.mimvista.external.points.XMimNoxelPointI;
import com.mimvista.external.series.XMimDose;
import com.mimvista.external.series.XMimImage;
import com.mimvista.external.series.XMimSeriesView;
import com.mimvista.external.stats.XMimContourStatId;



import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ea_read_imrt_constraints_v05 {

	private static final String desc = "reading IMRT constraints from HYP file-  java";

	/* 
	 * 	@XMimEntryPoint tells MIM that this function can be used to start a MIMextension
	 * 		Meta-data entered here will display in the extension launcher inside MIM
	 */ 
	//  readind art, xlog and reference plan imrt constraints
	//  have 4 outputs for EP
	//  having Ahmet's comparison

	/*
	 * 
	 */
 
	@XMimEntryPoint(
			name="Read IMRT constraints HYP v06", 
			author="Ergun Ahunbay",
			category="Dose",
			description=desc, 
			outputTypes={String.class, String.class , String.class , String.class})
	public static Object[] runOnSession(XMimSession session,  String in_file) {
		//	XMimSeriesView view = null;


		Path path;
		path = Paths.get(in_file);
        

		String hypfilename = path.getFileName().toString();
		String plan_folder = path.getParent().toString();
		String daily_plan = path.getParent().getFileName().toString();

		
		String[] filenames = hypfilename.split("\\.");
		String artfilename = filenames[0] + ".art";
		
		String logfile = plan_folder + "/RxA.xlog";
		String artfile = plan_folder + "/"+ artfilename;
		readARTfile R = new readARTfile(new File(artfile));
		readRXAlog inf = new readRXAlog(new File(logfile));		

		
        String plans_folder = path.getParent().getParent().toString();
        String ref_plan_hypfile = plans_folder + "/" + R.ref_plan_name +"/" + hypfilename;

			
		String ref_artfilename = plans_folder + "/" + R.ref_plan_name +"/" + artfilename;
       String ref_logfile = plans_folder + "/" + R.ref_plan_name +"/RxA.xlog";
        File ref_artfile = new File(ref_artfilename);
        
	    // trying to see if ATP on pATS etc
	    String ref_plan_dosefile = plans_folder + "/" + R.ref_plan_name +"/dose.1";
	    String this_plan_dosefile = plan_folder + "/dose.1";
		Path path_ref_dose = Paths.get(ref_plan_dosefile);
		Path path_this_dose = Paths.get(this_plan_dosefile);


		readIMRTconstr_v05 IC_daily = new readIMRTconstr_v05(new File(in_file));
		readIMRTconstr_v05 IC_ref = new readIMRTconstr_v05(new File(ref_plan_hypfile));

		readIMRTconstr_v05 IC_orig = null;
		delta_IMRTconstr_AA_v02 compareIC = new delta_IMRTconstr_AA_v02 (IC_ref, IC_daily);;
		delta_IMRTconstr_AA_v02 compareIC2 = null;

		
		
		double time_diff = 2. ;
		try {
			time_diff =  ( Files.readAttributes(path_this_dose, BasicFileAttributes.class).lastModifiedTime().toMillis() -
					              Files.readAttributes(path_ref_dose, BasicFileAttributes.class).lastModifiedTime().toMillis() ) / (24.*60.*60000.);
			
		} catch (IOException e) {
            e.printStackTrace();
        }
        
		String out_string = "";
		
		if(ref_artfile.exists() && time_diff < 0.04) {
			
			readARTfile R_ref = new readARTfile(ref_artfile);
			readRXAlog inf_ref = new readRXAlog(new File(ref_logfile));	
			
	        String orig_plan_hypfile = plans_folder + "/" + R_ref.ref_plan_name +"/" + hypfilename;
			 IC_orig = new readIMRTconstr_v05(new File(orig_plan_hypfile));

			 compareIC2 = new delta_IMRTconstr_AA_v02 (IC_orig, IC_ref);

			out_string = inf.type + " upon " + inf_ref.type + "\n" + make_string_out(R, inf) + " \n UPON \n" + make_string_out(R_ref, inf_ref);
			
		} else {


		
		out_string = make_string_out(R, inf);
		
		}
		
		readIMRTconstr_v05 IC = new readIMRTconstr_v05(new File(in_file));
		readIMRTconstr_v05 ICr = new readIMRTconstr_v05(new File(ref_plan_hypfile));
	
		//   Trying to send out all text as delimited with ; so it can be put to a table in the mim workflow
		
		
		
	
		
		String out_string4;
							if(inf.type.contentEquals("ATS") && inf.optim_method.contentEquals("From Segments")) 
									out_string4 = "pseudo-ATS";
								else 
									out_string4 = inf.type;
				

							delta_IMRTconstr_AA_v02 compare = new delta_IMRTconstr_AA_v02(ICr, IC);

		return new Object[]{IC.generate_string_output(compare,0), out_string,  ICr.generate_string_output(compare,1), out_string4};


	}
	
	
	
	static String make_string_out(readARTfile R ,readRXAlog inf ) {
		
		String concluded_early;
		if(inf.premature.contentEquals("YES"))
			concluded_early = " (concluded early)";
		else
			concluded_early = "";
		String out_string = inf.type + " (" + inf.optim_method + ") from  "+ R.ref_plan_name + 
				" (" + R.ref_studyset_name+ "). \n Optimization Time: "
				+inf.optim_time+ "min." + concluded_early + "\n Isocenter Shift (cm.):  x:" 
						+ R.iso_shift[0]+ " ,y: "+ R.iso_shift[1] + " ,z: "+ R.iso_shift[2] +" (Internal Coordinates)\n Rescale: " +
						inf.rescale
						;
		
		return out_string;
	}
	
	
	

}

