package sample.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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

public class ea_read_dosimetric_criteria_v02 {

	private static final String desc = "reading dosimetric criteria from isodosesettings-  java";

	/* 
	 * 	@XMimEntryPoint tells MIM that this function can be used to start a MIMextension
	 * 		Meta-data entered here will display in the extension launcher inside MIM
	 */
	//  this works EA

	/*
	 * 
	 */

	@XMimEntryPoint(
			name="Read Dosimetric Criteria v02",
			author="Ergun Ahunbay",
			category="Dose",
			description=desc, 
			outputTypes={String.class})
	public static Object[] runOnSession(XMimSession session, XMimDose dose, String in_file) {
		//	XMimSeriesView view = null;


		XMimImage image = dose.getOwner();
		XMimCommandFactory comFac = session.getCommandFactory();
		Collection mimcontours = image.getContours();

		
		readDoCi_xml_better rdoci = new readDoCi_xml_better(new File( in_file) );





		//   Trying to send out all text as delimited with ; so it can be put to a table in the mim workflow
		String structureName ="";

		for (int i=0; i<rdoci.structs.size();i++) {
			structureName = structureName +rdoci.structs.get(i).StructureName+ "__  __  __0;"; 

			if (rdoci.structs.get(i).dosegoals.size() > 0) {
				XMimContour xc = null;
				Iterator iteratorc = mimcontours.iterator();
				while(iteratorc.hasNext()){
					xc = (XMimContour) iteratorc.next();
					if(rdoci.structs.get(i).StructureName.contentEquals(xc.getInfo().getName()) ) {
						break;
					}
				}
if (  !xc.isEmpty() ) {
				for (int j=0; j< rdoci.structs.get(i).dosegoals.size(); j++) {
					Integer t = rdoci.structs.get(i).dosegoals.get(j).GoalType;
					double D = rdoci.structs.get(i).dosegoals.get(j).Dose;
					double V = rdoci.structs.get(i).dosegoals.get(j).Volume;
					double to = rdoci.structs.get(i).dosegoals.get(j).Tolerance;
                    String met = "3";
					String goalstructure = ""; 


					calcDVHfromDosCrit A = new calcDVHfromDosCrit(comFac,xc,dose,t , D, V);

	

					
					
                    double achieved = A.calc();
                    String achieved_string = String.format("%.2f", achieved);
                    String D_s = String.format("%.2f", D);
                    String V_s = String.format("%.2f", V);
                    String to_s = String.format("%.2f", to);
                    ea_format EAF = new ea_format(achieved_string);                    achieved_string = EAF.calc();
                    ea_format EAF2 = new ea_format(D_s);                    D_s = EAF2.calc();
                    ea_format EAF3 = new ea_format(V_s);                    V_s = EAF3.calc();
                    ea_format EAF4 = new ea_format(to_s);                    to_s = EAF4.calc();
                    
                    

					
                    
					switch (t ) {
					case 1:   //dmin
						if (to > 0) {
							goalstructure =  "     Dmin > "+ D_s + " cGy " + " (-" + to_s +" cGy) ";
							if (achieved >= D) met = "1";
							else if (achieved >= (D - to)) met = "2";
						}
						else     {    goalstructure =  "     Dmin > "+ D_s + " cGy " ;
						if (achieved >= D) met = "1";

						}						
						break;
					case 2:              // dmax
						if (to > 0) {
							goalstructure =  "     Dmax < "+ D_s + " cGy " + " (+" + to_s +" cGy) ";
							if (achieved <= D) met = "1";
							else if (achieved <= D + to) met = "2";
						}
						else     {    goalstructure =  "     Dmax < "+ D_s + " cGy " ;
						if (achieved <= D) met = "1";

						}
						break;
					case 3:            // dmean > ... cGy
						if (to > 0) {
							goalstructure = "     Dmean > "+ D_s + " cGy " + " (-" + to_s +" cGy) ";
						if (achieved >= D) met = "1";
						else if (achieved >= D - to) met = "2";
					}
						else {
							goalstructure = "     Dmean > "+ D_s + " cGy "  ;
						if (achieved >= D) met = "1";
						}
						break;
					case 4:            // dmean < ... cGy
						if (to > 0) {
							goalstructure = "     Dmean < "+ D_s + " cGy " + " (+" + to_s +" cGy) ";
						if (achieved <= D) met = "1";
						else if (achieved <= D + to) met = "2";
					}
						else {
							goalstructure = "     Dmean < "+ D_s + " cGy "  ;
						if (achieved <= D) met = "1";
						}
						break;

					case 5:   // D at ...% > ... cGy
						if (to > 0) {
							goalstructure = "     D"+ V_s +"% > "+ D_s + " cGy " + " (-" + to_s +" cGy) ";
							if (achieved >= D) met = "1";
							else if (achieved >= D - to) met = "2";
						}
						else {
							goalstructure = "     D"+V_s+"% > "+ D_s + " cGy  ";
							if (achieved >= D) met = "1";
						}
						break;
					case 6:   // D at .. cc > ... cGy
						if (to > 0) {

							goalstructure = "     D"+V_s+"cc > "+ D_s + " cGy " + " (-" + to_s +" cGy) ";
							if (achieved >= D) met = "1";
							else if (achieved >= D - to) met = "2";
						}
						else {
							goalstructure = "     D"+V_s+"cc > "+ D_s + " cGy  ";
							if (achieved >= D) met = "1";
						}
						break;
					case 7:   // D at ...% < ... cGy
						if (to > 0) {
							goalstructure = "     D"+V_s+"% < "+ D_s + " cGy " + " (+" + to_s +" cGy) ";
							if (achieved <= D) met = "1";
							else if (achieved <= D - to) met = "2";
						}
						else {
							goalstructure = "     D"+V_s+"% < "+ D_s + " cGy  ";
							if (achieved <= D) met = "1";
						}
						break;				
					
					case 8:   // D at ...cc < ... cGy
						if (to > 0) {
							goalstructure = "     D"+V_s+"cc < "+ D_s + " cGy " + " (+" + to_s +" cGy) ";
							if (achieved <= D) met = "1";
							else if (achieved <= D + to) met = "2";
						}
						else {
							goalstructure = "     D"+V_s+"cc < "+ D_s + " cGy  ";
							if (achieved <= D) met = "1";
						}
						break;

					case 9:   //  V ... Gy > ... %
						achieved_string = achieved_string + " %";
						if (to > 0) {
							goalstructure = "     V"+ D_s + "cGy > " + V_s + " % (-" + to_s +" %) ";
							if (achieved >= V) met = "1";
							else if (achieved >= V - to) met = "2";
						}
						else {
							goalstructure = "     V"+ D_s + "cGy > " + V_s + " % ";
						if (achieved >= V) met = "1";
						}
						break;
					case 10:   //  V ... Gy > ... cc
						if (to > 0) {
							goalstructure = "     V"+ D_s + "cGy > " + V_s + " cc (-" + to_s +" cc) ";
							if (achieved >= V) met = "1";
							else if (achieved >= V - to) met = "2";
						}
						else {
							goalstructure = "     V"+ D_s + "cc > " + V_s + " cc ";
						if (achieved >= V) met = "1";
						}
						break;
					case 11:  // v...cGy < ...%
						achieved_string = achieved_string + " %";

						if (to > 0) {
							goalstructure = "     V"+ D_s + "cGy < " + V_s + " % (+" + to_s +" %) ";
							if (achieved <= V) met = "1";
							else if (achieved <= (V + to)) met = "2";
						} else {
							goalstructure = "     V"+ D_s + "cGy < " + V_s + " % ";
							if (achieved <= V) met = "1";

						}

						break;	
					case 12:  // v...cGy < ...cc
						if (to > 0) {
							goalstructure = "     V"+ D_s + "cGy < " + V_s + " cc (+" + to_s +" cc) ";
							if (achieved <= V) met = "1";
							else if (achieved <= V + to) met = "2";
						} else {
							goalstructure = "     V"+ D_s + "cGy < " + V_s + " cc ";
							if (achieved <= V) met = "1";

						}

						break;	


					}
					structureName = structureName + " __"+goalstructure+" __ "+ achieved_string+ " __" + met + ";";
				}

			} 
			}else
				structureName = structureName + "no struct found __ __  __4;";

		}


		return new Object[]{structureName};

	}

}

