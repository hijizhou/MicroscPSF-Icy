package plugins.hijizhou.microscpsf;

import plugins.adufour.ezplug.EzGroup;
import plugins.adufour.ezplug.EzPlug;
import plugins.adufour.ezplug.EzVarBoolean;
import plugins.adufour.ezplug.EzVarDouble;
import plugins.adufour.ezplug.EzVarInteger;
import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.gui.dialog.MessageDialog;
import icy.gui.viewer.ZNavigationPanel;
import icy.image.colormap.FireColorMap;
import icy.image.colormap.IcyColorMap;
import icy.image.lut.LUT;
import icy.imagej.ImageJUtil;
import icy.main.Icy;
import icy.sequence.Sequence;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;

public class MicroscPSF extends EzPlug {

	EzVarInteger intNx;
	EzVarInteger intNy;
	EzVarInteger intNz;

	EzVarDouble dbNA;
	EzVarDouble dbLambda;
	EzVarDouble dbNs;
	EzVarDouble dbNg0;
	EzVarDouble dbNg;
	EzVarDouble dbNi0;
	EzVarDouble dbNi;
	EzVarDouble dbTi;
	EzVarDouble dbTi0;

	EzVarDouble dbTg0;
	EzVarDouble dbTg;
	EzVarDouble dbLateral;
	EzVarDouble dbAxial;
	EzVarDouble dbPz;

	EzVarInteger intBasis;
	EzVarInteger intSamp;

	EzVarBoolean varBoolean;

	/*
	 * References: [1] J. Li, F. Xue and T. Blu, “Fast and Accurate 3D PSF
	 * Computation for Fluorescence Microscopy”, J. Opt. Soc. Am. A, vol. 34,
	 * no. 6, 2017. [2] S. F. Gibson and F. Lanni, “Experimental test of an
	 * analytical model of aberration in an oil-immersion objective lens used in
	 * three-dimensional light microscopy”, J. Opt. Soc. Am. A, vol. 9, no. 1,
	 * pp. 154-166, 1992.
	 * 
	 * Author: Jizhou Li (hijizhou@gmail.com)
	 */
	@Override
	protected void initialize() {
		intNx = new EzVarInteger("Size (X)", 256, 64, 1024, 32);
		intNy = new EzVarInteger("Size (Y)", 256, 64, 1024, 32);
		intNz = new EzVarInteger("Size (Z)", 64, 64, 1024, 32);
		intBasis = new EzVarInteger("Basis Number", 100, 100, 1000, 10);
		intSamp = new EzVarInteger("Sampling Number", 1000, 500, 10000, 100);

		dbNA = new EzVarDouble("Numerical Aperture", 1.4, 1.0, 1.5, 0.1);
		dbLambda = new EzVarDouble("Wavelength (nm)", 610, 340, 750, 50);
		dbNs = new EzVarDouble("Specimen RI", 1.33, 1.3, 1.5, 0.05);
		dbNg = new EzVarDouble("Coverslip RI", 1.5, 1.3, 1.5, 0.05);
		dbNi = new EzVarDouble("Immersion RI", 1.5, 1.3, 1.7, 0.05);
		dbTg = new EzVarDouble("Coverslip thickness (um)",170, 50, 200, 10);
		dbTi = new EzVarDouble("Working distance (um)", 150, 50, 300, 10);
		dbPz = new EzVarDouble("Particle position (nm)", 2000, 0, 3000, 100);
		dbLateral = new EzVarDouble("Lateral size (nm)", 100, 10, 500, 10);
		dbAxial = new EzVarDouble("Axial size (nm)", 250, 50, 300, 10);

		varBoolean = new EzVarBoolean("Better visualization?", false);

		EzGroup groupNumeric = new EzGroup("Model parameters", intNx, intNy,
				intNz, dbLambda, dbNA, dbNs, dbNg, dbNi, dbTg, dbTi, dbPz,
				dbLateral, dbAxial);
		super.addEzComponent(groupNumeric);

		EzGroup groupMethod = new EzGroup("Approximation parameters", intBasis,
				intSamp);
		super.addEzComponent(groupMethod);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void execute() {
		GibsonLanni gl = new GibsonLanni();

		super.getUI().setProgressBarMessage("Waiting...");

		// set parameters

		gl.setNx(intNx.getValue());
		gl.setNy(intNy.getValue());
		gl.setNz(intNz.getValue());

		gl.setNA(dbNA.getValue());
		gl.setLambda(dbLambda.getValue()*1E-09D);
		gl.setNs(dbNs.getValue());
		gl.setNg(dbNg.getValue());
		gl.setNi(dbNi.getValue());
		gl.setTg(dbTg.getValue()*1E-06D);
		gl.setTi0(dbTi.getValue()*1E-06D);
		gl.setpZ(dbPz.getValue()*1E-09D);
		gl.setResLateral(dbLateral.getValue()*1E-09D);
		gl.setResAxial(dbAxial.getValue()*1E-09D);

		gl.setNumBasis(intBasis.getValue());
		gl.setNumSamp(intSamp.getValue());

		long startTime = System.currentTimeMillis();

		ImageStack stack = gl.compute();

		long endTime = System.currentTimeMillis();

		ImagePlus ipPSF = new ImagePlus("Computed PSF", stack);

		// Sequence sqPSF = ImageJUtil.convertToIcySequence(ipPSF, null);
		// addSequence(sqPSF);

		// sqPSF.setColormap(0, new FireColorMap(), true);
		//
		// int posC = (int) Math.floor(gl.getNz()/2);
		//
		// try{
		// super.getActiveViewer().setPositionZ(posC);
		// }catch(Exception e)
		// {
		//
		// }
		ipPSF.show();
		IJ.run("Fire");
		super.getUI().setProgressBarMessage((endTime - startTime) + " ms");

	}

	@Override
	public void clean() {
		// TODO Auto-generated by Icy4Eclipse
	}

}
