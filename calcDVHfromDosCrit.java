package sample.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

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
import java.util.stream.Collectors;
/**
 * writes stuff
 */



public class calcDVHfromDosCrit {
	XMimCommandFactory com_fac;
	XMimContour mimcontour;
	XMimDose mimdose;
	Integer type;
	double doseval;
	double volume;
	
	
	public  calcDVHfromDosCrit( XMimCommandFactory comFac, XMimContour xc, XMimDose dose, Integer dc_type, double DoseVal, double Vol) {
          com_fac = comFac;
		  mimcontour = xc;
          mimdose = dose;
          type = dc_type;
          doseval = DoseVal;
          volume = Vol;

		
		
	}

	
	
	public Float calc( ) {
		List<XMimContour> thiscontour = new ArrayList<XMimContour>();
		    thiscontour.clear();
			thiscontour.add(mimcontour);
			double contour_volume = mimcontour.getStatistic(new XMimContourStatId("VOLUME", "MIM")).doubleValue();
			
			switch (type) {
			case 1:   // dmin
				return  (float)100. * com_fac.makeDVHValueCommand(mimdose, thiscontour, 
        				XMimCommandFactory.XMimDoseContourConstraintType.STATISTIC,
        				XMimCommandFactory.XMimMinMeanMax.MIN,
        				XMimCommandFactory.XMimConformityConstraintType.CI,
        				(float) 10.0,
        				XMimCommandFactory.XMimDoseUnit.cGy,
        				XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
        				).execute();			
			case 2:   // dmax
				return  (float)100. * com_fac.makeDVHValueCommand(mimdose, thiscontour, 
        				XMimCommandFactory.XMimDoseContourConstraintType.STATISTIC,
        				XMimCommandFactory.XMimMinMeanMax.MAX,
        				XMimCommandFactory.XMimConformityConstraintType.CI,
        				(float) 10.0,
        				XMimCommandFactory.XMimDoseUnit.cGy,
        				XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
        				).execute();
			case 3:  // mean > D
				return (float)100. * com_fac.makeDVHValueCommand(mimdose, thiscontour, 
						XMimCommandFactory.XMimDoseContourConstraintType.STATISTIC,
						XMimCommandFactory.XMimMinMeanMax.MEAN,
						XMimCommandFactory.XMimConformityConstraintType.CI,
						(float) 0.0,
						XMimCommandFactory.XMimDoseUnit.cGy,
						XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
						).execute();
				case 4:  // mean
				return (float)100. * com_fac.makeDVHValueCommand(mimdose, thiscontour, 
						XMimCommandFactory.XMimDoseContourConstraintType.STATISTIC,
						XMimCommandFactory.XMimMinMeanMax.MEAN,
						XMimCommandFactory.XMimConformityConstraintType.CI,
						(float) 0.0,
						XMimCommandFactory.XMimDoseUnit.cGy,
						XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
						).execute();
			case 5:  // D at % vol > ...

				return (float)100. * com_fac.makeDVHValueCommand(mimdose, thiscontour, 
						XMimCommandFactory.XMimDoseContourConstraintType.D,
						XMimCommandFactory.XMimMinMeanMax.MEAN,
						XMimCommandFactory.XMimConformityConstraintType.CI,
						(float) contour_volume * (float)volume / (float) 100.,
						XMimCommandFactory.XMimDoseUnit.cGy,
						XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
						).execute();
			case 6:  // D at abs vol >  ...
				if (contour_volume < (float) volume)    // if the volume > contour volume, return min dose 
					return  (float)100. * com_fac.makeDVHValueCommand(mimdose, thiscontour, 
	        				XMimCommandFactory.XMimDoseContourConstraintType.STATISTIC,
	        				XMimCommandFactory.XMimMinMeanMax.MIN,
	        				XMimCommandFactory.XMimConformityConstraintType.CI,
	        				(float) 10.0,
	        				XMimCommandFactory.XMimDoseUnit.cGy,
	        				XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
	        				).execute();		
				else 				
				return (float)100. * com_fac.makeDVHValueCommand(mimdose, thiscontour, 
						XMimCommandFactory.XMimDoseContourConstraintType.D,
						XMimCommandFactory.XMimMinMeanMax.MEAN,
						XMimCommandFactory.XMimConformityConstraintType.CI,
						(float) volume,
						XMimCommandFactory.XMimDoseUnit.cGy,
						XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
						).execute();
				
			case 7:  // D at % vol <  ... cGy
				return (float)100. * com_fac.makeDVHValueCommand(mimdose, thiscontour, 
						XMimCommandFactory.XMimDoseContourConstraintType.D,
						XMimCommandFactory.XMimMinMeanMax.MEAN,
						XMimCommandFactory.XMimConformityConstraintType.CI,
						(float) volume,
						XMimCommandFactory.XMimDoseUnit.cGy,
						XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
						).execute();
			case 8:  // Dose at .. cc < ... cGy
				if (contour_volume < (float) volume)
					return  (float)100. * com_fac.makeDVHValueCommand(mimdose, thiscontour, 
	        				XMimCommandFactory.XMimDoseContourConstraintType.STATISTIC,
	        				XMimCommandFactory.XMimMinMeanMax.MIN,
	        				XMimCommandFactory.XMimConformityConstraintType.CI,
	        				(float) 10.0,
	        				XMimCommandFactory.XMimDoseUnit.cGy,
	        				XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
	        				).execute();			
				
				else 
				return (float) 100.* com_fac.makeDVHValueCommand(mimdose, thiscontour, 
						XMimCommandFactory.XMimDoseContourConstraintType.D,
						XMimCommandFactory.XMimMinMeanMax.MAX,
						XMimCommandFactory.XMimConformityConstraintType.CI,
						(float) volume,
						XMimCommandFactory.XMimDoseUnit.cGy,
						XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
						).execute();	
			case 9:  //volume .. Gy > ... %
				return (float) 100.* com_fac.makeDVHValueCommand(mimdose, thiscontour, 
						XMimCommandFactory.XMimDoseContourConstraintType.V,
						XMimCommandFactory.XMimMinMeanMax.MAX,
						XMimCommandFactory.XMimConformityConstraintType.CI,
						(float) doseval,
						XMimCommandFactory.XMimDoseUnit.cGy,
						XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
						).execute() / (float) contour_volume;					
			case 10:  //volume .. Gy > ...cc
			return (float)  com_fac.makeDVHValueCommand(mimdose, thiscontour, 
					XMimCommandFactory.XMimDoseContourConstraintType.V,
					XMimCommandFactory.XMimMinMeanMax.MAX,
					XMimCommandFactory.XMimConformityConstraintType.CI,
					(float) doseval,
					XMimCommandFactory.XMimDoseUnit.cGy,
					XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
					).execute() ;					
			case 11:  //volume .. Gy > ... %
				return (float) (100.* com_fac.makeDVHValueCommand(mimdose, thiscontour, 
						XMimCommandFactory.XMimDoseContourConstraintType.V,
						XMimCommandFactory.XMimMinMeanMax.MAX,
						XMimCommandFactory.XMimConformityConstraintType.CI,
						(float) doseval,
						XMimCommandFactory.XMimDoseUnit.cGy,
						XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
						).execute() / (float) contour_volume);					
			case 12:  //volume .. Gy < ... cc
				return (float)  com_fac.makeDVHValueCommand(mimdose, thiscontour, 
						XMimCommandFactory.XMimDoseContourConstraintType.V,
						XMimCommandFactory.XMimMinMeanMax.MAX,
						XMimCommandFactory.XMimConformityConstraintType.CI,
						(float) doseval,
						XMimCommandFactory.XMimDoseUnit.cGy,
						XMimCommandFactory.XMimVolumeUnit.ml, (Boolean) true
						).execute() ;								
				default:
					return (float) 0.0;
					
			}
			
			
			}
	
		
	}
	
	

