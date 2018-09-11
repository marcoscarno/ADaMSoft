/**
* Copyright (c) 2017 MS
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package ADaMSoft.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import ch.javasoft.polco.adapter.PolcoAdapter;

/**
* This class is used inside all the localization procedures
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class EditsAnalyzer
{
	double[][] iq;
	double[][] eq;
	double[][] defeq;
	double[][] defiq;
	Vector<double[]> checkfd;
	String tempdir, filetoprint, tempvarname;
	int timeneeded, totalsolutions, maxvar, numedit, numvar, refvar, tempint, realnumsol, position, finaldimension, numsoldiffzero, refmiq, refmeq, refmv, tempmaxvar, numadded, lastadded, groupbelong, actualmaxvar, actualaddingstep, numsoldetdz, numsolineq, maxsolineq, maxdetsol;
	Vector<Vector<Integer>> disedit;
	Vector<double[][]> diseq;
	Vector<double[][]> disiq;
	boolean[][] listvar;
	boolean verifyresult, firstdz, seconddz, both, consideredit, onesoldiffzero, checkerok, ismaxvarreached;
	double tempval, tempdou, tolerance, cardinality, vala, valb;
	Vector<Integer> activevars;
	Vector<Integer> lasttested;
	double[] recval;
	double[] actualsol;
	double[] tempdval;
	double[] tempcs;
	double[] tempsolfordet;
	Hashtable<Integer, Double> valueweights;
	Vector<Integer> vectrefvname;
	Vector<Double> currentweights;
	Vector<Vector<Double>> iqdef;
	Vector<Vector<Double>> eqdef;
	EditsSolver es;
	EditsSolverReduced esr;
	EditsChecker ec;
	Options opo;
	Vector<double[]> solutions;
	Vector<String[]> defsolutionsvars;
	Vector<String[]> detsolutionsvars;
	Vector<double[]> defsolutions;
	String[] varref;
	Vector<Integer> currentpointer;
	Vector<Vector<Double>> iqfordet;
	Vector<Vector<Double>> eqfordet;
	double[][] viq;
	double[][] veq;
	double[][] tempiqfc;
	double[][] tempeqfc;
	Vector<String> curtempdet;
	Vector<Double> indexsol;
	ch.javasoft.polco.adapter.Options opopa;
	Vector<Integer> totest;
	Vector<Integer> originaltotest;
	Vector<Integer> oldtotest;
	Vector<Integer> tochange;
	Hashtable<Integer, Vector<Integer>> toremove;
	Vector<String> tochangev;
	Vector<String> varindenames;
	double[][] checksol;
	double[][] redchecksol;
	double[][] tempchecksol;
	int varsonestepsol=15;
	PolcoAdapter pa;
	Vector<Vector<Double>> origedit;
	Vector<Vector<Double>> reduedit;
	int[] positions;
	int[] positionsdep;
	Vector<String[]> maxdefsolutionsvars;
	Vector<String[]> maxdetsolutionsvars;
	Vector<double[]> maxdefsolutions;
	Vector<Integer> numdetsolforsol;
	boolean diffdetsol, useonlyintdetsol;
	long start_proc, end_proc;
	int maxcardfordetsol, numsolwithdec, realeqedit;
	double zeroalg;
	int recordprinted;
	/**
	*Initialise the method
	*/
	public EditsAnalyzer(String tempdir)
	{
		recordprinted=0;
		zeroalg=0.00000000001;
		numsolwithdec=0;
		useonlyintdetsol=false;
		maxcardfordetsol=3;
		maxdefsolutionsvars=new Vector<String[]>();
		maxdetsolutionsvars=new Vector<String[]>();
		maxdefsolutions=new Vector<double[]>();
		numdetsolforsol=new Vector<Integer>();
		positions=null;
		positionsdep=null;
		checkfd=new Vector<double[]>();
		varindenames=new Vector<String>();
		tochangev=new Vector<String>();
		toremove=new Hashtable<Integer, Vector<Integer>>();
		tochange=new Vector<Integer>();
		oldtotest=new Vector<Integer>();
		originaltotest=new Vector<Integer>();
		totest=new Vector<Integer>();
		indexsol=new Vector<Double>();
		curtempdet=new Vector<String>();
		iqfordet=new Vector<Vector<Double>>();
		eqfordet=new Vector<Vector<Double>>();
		currentpointer=new Vector<Integer>();
		defsolutionsvars=new Vector<String[]>();
		detsolutionsvars=new Vector<String[]>();
		solutions=new Vector<double[]>();
		defsolutions=new Vector<double[]>();
		varref=null;
		filetoprint="";
		this.tempdir=tempdir;
		activevars=new Vector<Integer>();
		currentweights=new Vector<Double>();
		iqdef=new Vector<Vector<Double>>();
		eqdef=new Vector<Vector<Double>>();
		opo=new Options();
		File flog=new File(tempdir+"Polco.log");
		File tdir=new File(tempdir);
		opo.setLogFile(flog);
		opo.setLogFormat(Options.LogFormat.plain);
		opo.setLoglevel(Level.OFF);
		opo.setTmpDir(tdir);
		try
		{
			es=new EditsSolver(opo);
			ec=new EditsChecker(opo);
			esr=new EditsSolverReduced(opo);
		}
		catch (Exception e){}
		opopa=new ch.javasoft.polco.adapter.Options();
		opopa.setLogFile(flog);
		opopa.setLogFormat(ch.javasoft.polco.adapter.Options.LogFormat.plain);
		opopa.setLoglevel(Level.OFF);
		opopa.setTmpDir(tdir);
		actualmaxvar=0;
		ismaxvarreached=false;
		actualaddingstep=0;
		origedit=new Vector<Vector<Double>>();
		reduedit=new Vector<Vector<Double>>();
		try
		{
			pa=new PolcoAdapter(opopa);
		}
		catch (Exception ex) {}
	}
	public void setzeroalg(double zeroalg)
	{
		this.zeroalg=zeroalg;
	}
	/**
	*Used to specify that the deterministic solutions must be only integer numbers
	*/
	public void setuseonlyintdetsol(boolean useonlyintdetsol)
	{
		this.useonlyintdetsol=useonlyintdetsol;
	}
	/**
	*Used to set the maximum cardininality to look for a deterministic solution
	*/
	public void setmaxcardfordetsol(int maxcardfordetsol)
	{
		this.maxcardfordetsol=maxcardfordetsol;
	}
	/**
	*Used to set the variables to use for the one step solution
	*/
	public void setvarsonestepsol(int tvarsonestepsol)
	{
		varsonestepsol=tvarsonestepsol;
		if (varsonestepsol<5) varsonestepsol=5;
	}
	/**
	*Used to set the maximum dimension of the variable to solve
	*/
	public void setMaxvar(int maxvar)
	{
		this.maxvar=maxvar;
	}
	/**
	*The name of the variables are passed to this class
	*/
	public void setVarName(String[] varref)
	{
		this.varref=varref;
	}
	/**
	*Specifies to print in a file the matrices
	*/
	public void printonfile(String filetoprint)
	{
		recordprinted=0;
		this.filetoprint=filetoprint;
		try
		{
    		BufferedWriter outmat = new BufferedWriter(new FileWriter(filetoprint));
    		outmat.write("START\n");
    		outmat.close();
		}
		catch (Exception e) {}
	}
	/**
	*Receives the edits in vector of double
	*/
	public void setEdits(Vector<double[]> tempiq, Vector<double[]> tempeq)
	{
		numedit=0;
		numvar=0;
		refvar=0;
		if (tempiq.size()>0)
		{
			double[] templ=tempiq.get(0);
			numvar=templ.length;
		}
		else if (tempeq.size()>0)
		{
			double[] templ=tempeq.get(0);
			numvar=templ.length;
		}
		iq=null;
		eq=null;
		if (tempiq.size()>0)
		{
			iq=new double[tempiq.size()][numvar];
			numedit=tempiq.size();
			refvar=numedit;
			for (int i=0; i<tempiq.size(); i++)
			{
				iq[i]=tempiq.get(i);
			}
		}
		if (tempeq.size()>0)
		{
			eq=new double[tempeq.size()][numvar];
			numedit=numedit+tempeq.size();
			for (int i=0; i<tempeq.size(); i++)
			{
				eq[i]=tempeq.get(i);
			}
		}
		listvar=new boolean[numedit][numvar-1];
		if (iq!=null)
		{
			for (int i=0; i<iq.length; i++)
			{
				for (int j=1; j<iq[0].length; j++)
				{
					if (iq[i][j]!=0.0) listvar[i][j-1]=true;
					else listvar[i][j-1]=false;
				}
			}
		}
		if (eq!=null)
		{
			for (int i=0; i<eq.length; i++)
			{
				for (int j=1; j<eq[0].length; j++)
				{
					if (eq[i][j]!=0.0) listvar[refvar+i][j-1]=true;
					else listvar[refvar+i][j-1]=false;
				}
			}
		}
	}
	/*
	*Used to reduce the edits
	*/
	public boolean reduceedits(double[][] iq, double[][] eq)
	{
		origedit.clear();
		reduedit.clear();
		try
		{
			checksol=pa.getDoubleRays(eq, iq);
			if (checksol!=null)
			{
				if (checksol.length>1)
				{
					redchecksol=pa.getDoubleRays(null, checksol);
					if (eq!=null)
					{
						for (int i=0; i<eq.length; i++)
						{
							Vector<Double> tempoe=new Vector<Double>();
							for (int j=0; j<eq[0].length; j++)
							{
								if (eq[i][0]!=0.0) tempoe.add(new Double(eq[i][j]/eq[i][0]));
								else tempoe.add(new Double(eq[i][j]));
							}
							if (!origedit.contains(tempoe)) origedit.add(tempoe);
						}
						for (int i=0; i<eq.length; i++)
						{
							Vector<Double> tempoe=new Vector<Double>();
							for (int j=0; j<eq[0].length; j++)
							{
								if (eq[i][0]!=0.0) tempoe.add(new Double(-1.0*eq[i][j]/(Math.abs(eq[i][0]))));
								else tempoe.add(new Double(-1*eq[i][j]));
							}
							if (!origedit.contains(tempoe)) origedit.add(tempoe);
						}
					}
					for (int i=0; i<iq.length; i++)
					{
						Vector<Double> tempoe=new Vector<Double>();
						for (int j=0; j<iq[0].length; j++)
						{
							if (iq[i][0]!=0.0) tempoe.add(new Double(iq[i][j]/iq[i][0]));
							else tempoe.add(new Double(iq[i][j]));
						}
						if (!origedit.contains(tempoe)) origedit.add(tempoe);
					}
					for (int i=0; i<redchecksol.length; i++)
					{
						Vector<Double> tempoe=new Vector<Double>();
						tempdou=0;
						for (int j=0; j<redchecksol[0].length; j++)
						{
							if (j>0 && Math.abs(redchecksol[i][j])>0) tempdou=tempdou+Math.abs(redchecksol[i][j]);
							if (redchecksol[i][0]!=0.0) tempoe.add(new Double(redchecksol[i][j]/redchecksol[i][0]));
							else tempoe.add(new Double(redchecksol[i][j]));
						}
						if (tempdou>0)
						{
							if (!reduedit.contains(tempoe)) reduedit.add(tempoe);
							if (origedit.contains(tempoe)) origedit.remove(tempoe);
						}
					}
				}
			}
			return true;
		}
		catch (Exception ere)
		{
			return false;
		}
	}
	/**
	*Returns the original edits that were not reduced
	*/
	public Vector<Vector<Double>> getorigedit()
	{
		return origedit;
	}
	/**
	*Returns the reduced edits
	*/
	public Vector<Vector<Double>> getreduedit()
	{
		return reduedit;
	}
	/**
	*Receives the edits in matrices of double
	*/
	public void setEdits(double[][] iq, double[][] eq)
	{
		this.iq=iq;
		this.eq=eq;
		numedit=0;
		numvar=0;
		refvar=0;
		if (iq!=null)
		{
			numedit=numedit+iq.length;
			numvar=iq[0].length;
			refvar=numedit;
		}
		if (eq!=null)
		{
			numedit=numedit+eq.length;
			numvar=iq[0].length;
		}
		listvar=new boolean[numedit][numvar-1];
		if (iq!=null)
		{
			for (int i=0; i<iq.length; i++)
			{
				for (int j=1; j<iq[0].length; j++)
				{
					if (iq[i][j]!=0.0) listvar[i][j-1]=true;
					else listvar[i][j-1]=false;
				}
			}
		}
		if (eq!=null)
		{
			for (int i=0; i<eq.length; i++)
			{
				for (int j=1; j<eq[0].length; j++)
				{
					if (eq[i][j]!=0.0) listvar[refvar+i][j-1]=true;
					else listvar[refvar+i][j-1]=false;
				}
			}
		}
	}
	/**
	*Verify if the edits have solutions
	*/
	public boolean verifysol(double[][] actualiq, double[][] actualeq)
	{
		EditsSearcher esea=null;
		try
		{
			esea=new EditsSearcher(opo);
		}
		catch (Exception eee){}
		long startTime = System.currentTimeMillis();
		totalsolutions=esea.getTotalSolutions(actualeq, actualiq);
		long elapsed = System.currentTimeMillis();
		timeneeded=(int)((elapsed-startTime)/1000);
		if (totalsolutions==0) return false;
		else return true;
	}
	/**
	*Return the solutions of the actual edits
	*/
	public Vector<double[]> getsolutions(int refe)
	{
		double[][] tempiq=disiq.get(refe);
		double[][] tempeq=diseq.get(refe);
		long startTime = System.currentTimeMillis();
		es.setzeroalg(zeroalg);
		es.getTotalSolutions(tempeq, tempiq);
		long elapsed = System.currentTimeMillis();
		timeneeded=(int)((elapsed-startTime)/1000);
		totalsolutions=es.getnumsol();
		if (totalsolutions==0) return null;
		Vector<double[]> retsolutions=new Vector<double[]>();
		Vector<double[]> tempsol=es.getSolutions();
		double[] tarr=null;
		for (int i=0; i<tempsol.size(); i++)
		{
			tarr=tempsol.get(i);
			double[] act_sol=new double[tarr.length];
			for (int j=0; j<act_sol.length; j++)
			{
				act_sol[j]=tarr[j];
			}
			retsolutions.add(act_sol);
		}
		es.cleanMem();
		return retsolutions;
	}
	/**
	*Return a random solution of the actual edits
	*/
	public void localizeallmiss(int refe)
	{
		if (!filetoprint.equals(""))
		{
			recordprinted++;
			if (recordprinted>2) filetoprint="";
		}
		maxdefsolutionsvars.clear();
		maxdetsolutionsvars.clear();
		maxdefsolutions.clear();
		numdetsolforsol.clear();
		tempeqfc=new double[0][0];
		tempiqfc=new double[0][0];
		tempeqfc=null;
		tempiqfc=null;
		currentweights.clear();
		vectrefvname=disedit.get(refe);
		for (int i=0; i<vectrefvname.size(); i++)
		{
			tempval=(valueweights.get(vectrefvname.get(i))).doubleValue();
			currentweights.add(new Double(tempval));
		}
		toremove.clear();
		totest.clear();
		oldtotest.clear();
		originaltotest.clear();
		cardinality=-1;
		defsolutions.clear();
		defsolutionsvars.clear();
		detsolutionsvars.clear();
		double[][] tempiq=disiq.get(refe);
		double[][] tempeq=diseq.get(refe);
		iqdef.clear();
		eqdef.clear();
		if (tempiq!=null)
		{
			for (int i=0; i<tempiq.length; i++)
			{
				Vector<Double> elementiqdef=new Vector<Double>();
				for (int j=0; j<vectrefvname.size()*2+1; j++)
				{
					elementiqdef.add(new Double(0.0));
				}
				tempval=0;
				tempdou=0;
				for (int j=1; j<tempiq[0].length; j++)
				{
					tempval=tempval+recval[j-1]*tempiq[i][j];
					if (tempiq[i][j]!=0.0)
					{
						elementiqdef.set(j, new Double(tempiq[i][j]));
						elementiqdef.set(j+tempiq[0].length-1, new Double(-1*tempiq[i][j]));
					}
					else
					{
						elementiqdef.set(j, new Double(0.0));
						elementiqdef.set(j+tempiq[0].length-1, new Double(0.0));
					}
					tempdou=tempdou+Math.abs(tempiq[i][j]);
				}
				tempval=tempiq[i][0]+tempval;
				elementiqdef.set(0, new Double(tempval));
				if (tempdou>0.0)
				{
					if (!iqdef.contains(elementiqdef)) iqdef.add(elementiqdef);
				}
			}
		}
		for (int i=0; i<vectrefvname.size()*2; i++)
		{
			Vector<Double> elementiqdef=new Vector<Double>();
			for (int j=0; j<vectrefvname.size()*2+1; j++)
			{
				if (i==j-1) elementiqdef.add(new Double(1.0));
				else elementiqdef.add(new Double(0.0));
			}
			if (!iqdef.contains(elementiqdef)) iqdef.add(elementiqdef);
		}
		if (tempeq!=null)
		{
			for (int i=0; i<tempeq.length; i++)
			{
				Vector<Double> elementeqdef=new Vector<Double>();
				for (int j=0; j<vectrefvname.size()*2+1; j++)
				{
					elementeqdef.add(new Double(0.0));
				}
				tempval=0;
				tempdou=0;
				for (int j=1; j<tempeq[0].length; j++)
				{
					tempval=tempval+recval[j-1]*tempeq[i][j];
					if (tempeq[i][j]!=0.0)
					{
						elementeqdef.set(j, new Double(tempeq[i][j]));
						elementeqdef.set(j+tempeq[0].length-1, new Double(-1*tempeq[i][j]));
					}
					else
					{
						elementeqdef.set(j, new Double(0.0));
						elementeqdef.set(j+tempeq[0].length-1, new Double(0.0));
					}
					tempdou=tempdou+Math.abs(tempeq[i][j]);
				}
				tempval=tempeq[i][0]+tempval;
				elementeqdef.set(0, new Double(tempval));
				if (tempdou>0.0)
				{
					if (!eqdef.contains(elementeqdef)) eqdef.add(elementeqdef);
				}
			}
		}
		tempiq=new double[0][0];
		tempeq=new double[0][0];
		tempiq=null;
		tempeq=null;
		findsolutions(refe);
	}
	/**
	*Return the number of the total solutions
	*/
	public int getTotalSolutions()
	{
		return totalsolutions;
	}
	/**
	*Returns the time needed for the current operation
	*/
	public int getTimeNeeded()
	{
		return timeneeded;
	}
	/**
	*Verify if there are disjoint edits
	*/
	public int checkDisjointEdits()
	{
		disedit=new Vector<Vector<Integer>>();
		disiq=new Vector<double[][]>();
		diseq=new Vector<double[][]>();
		boolean toadd=false;
		disedit.clear();
		int lastadded=1;
		int lastdim=0;
		for (int i=0; i<listvar.length-1; i++)
		{
			Vector<Integer> refv=new Vector<Integer>();
			for (int j=0; j<listvar[0].length; j++)
			{
				if (listvar[i][j]) refv.add(new Integer(j));
				listvar[i][j]=false;
			}
			lastdim=refv.size();
			lastadded=refv.size();
			while (lastadded!=0)
			{
				for (int j=i+1; j<listvar.length; j++)
				{
					toadd=false;
					for (int k=0; k<listvar[0].length; k++)
					{
						if ((listvar[j][k]) && (refv.contains(new Integer(k)))) toadd=true;
					}
					if (toadd)
					{
						for (int k=0; k<listvar[0].length; k++)
						{
							if ((listvar[j][k]) && (!refv.contains(new Integer(k)))) refv.add(new Integer(k));
							listvar[j][k]=false;
						}
					}
				}
				lastadded=lastdim-refv.size();
				lastdim=refv.size();
			}
			if (refv.size()>0) disedit.add(refv);
		}
		disiq.clear();
		diseq.clear();
		for (int i=0; i<disedit.size(); i++)
		{
			Vector<Integer> refv=disedit.get(i);
			Vector<double[]> tempeq=new Vector<double[]>();
			Vector<double[]> tempiq=new Vector<double[]>();
			if (eq!=null)
			{
				for (int j=0; j<eq.length; j++)
				{
					toadd=false;
					for (int k=1; k<eq[0].length; k++)
					{
						if ((eq[j][k]!=0.0) && (refv.contains(new Integer(k-1)))) toadd=true;
					}
					if (toadd)
					{
						double[] temped=new double[refv.size()+1];
						temped[0]=eq[j][0];
						for (int k=1; k<temped.length; k++)
						{
							temped[k]=0.0;
						}
						for (int k=0; k<refv.size(); k++)
						{
							position=(refv.get(k)).intValue();
							temped[k+1]=eq[j][position+1];
						}
						tempeq.add(temped);
					}
				}
			}
			if (tempeq.size()>0)
			{
				double[][] mateq=new double[tempeq.size()][refv.size()+1];
				for (int j=0; j<tempeq.size(); j++)
				{
					double[] temped=tempeq.get(j);
					mateq[j]=temped;
				}
				diseq.add(mateq);
			}
			else
			{
				diseq.add(null);
			}
			if (iq!=null)
			{
				for (int j=0; j<iq.length; j++)
				{
					toadd=false;
					for (int k=1; k<iq[0].length; k++)
					{
						if ((iq[j][k]!=0.0) && (refv.contains(new Integer(k-1)))) toadd=true;
					}
					if (toadd)
					{
						double[] temped=new double[refv.size()+1];
						temped[0]=iq[j][0];
						for (int k=1; k<temped.length; k++)
						{
							temped[k]=0.0;
						}
						for (int k=0; k<refv.size(); k++)
						{
							position=(refv.get(k)).intValue();
							temped[k+1]=iq[j][position+1];
						}
						tempiq.add(temped);
					}
				}
			}
			if (tempiq.size()>0)
			{
				double[][] matiq=new double[tempiq.size()][refv.size()+1];
				for (int j=0; j<tempiq.size(); j++)
				{
					double[] temped=tempiq.get(j);
					matiq[j]=temped;
				}
				disiq.add(matiq);
			}
			else
			{
				disiq.add(null);
			}
		}
		return disedit.size();
	}
	/**
	*Checks the dependent variables
	*/
	public Vector<String> getdepvars(Vector<String> depvars, String[] mustmatchvars)
	{
		curtempdet.clear();
		groupbelong=0;
		tochangev.clear();
		tempint=depvars.size();
		if (mustmatchvars!=null)
		{
			if (mustmatchvars.length>0)
			{
				for (int i=0; i<mustmatchvars.length; i++)
				{
					tempvarname=(mustmatchvars[i]).toUpperCase();
					if (!depvars.contains(tempvarname)) tempint++;
				}
			}
		}
		positions=new int[tempint];
		for (int i=0; i<positions.length; i++)
		{
			positions[i]=-1;
		}
		for (int i=0; i<depvars.size(); i++)
		{
			for (int j=0; j<varref.length; j++)
			{
				if (depvars.get(i).equalsIgnoreCase(varref[j]))
				{
					positions[i]=j;
					break;
				}
			}
		}
		if (mustmatchvars!=null)
		{
			for (int i=0; i<mustmatchvars.length; i++)
			{
				for (int j=0; j<varref.length; j++)
				{
					if (mustmatchvars[i].equalsIgnoreCase(varref[j]))
					{
						positions[i+depvars.size()]=j;
						break;
					}
				}
			}
		}
		if (iq!=null)
		{
			for (int i=0; i<iq.length; i++)
			{
				checkerok=false;
				for (int j=0; j<positions.length; j++)
				{
					if (positions[j]>=0)
					{
						if (iq[i][positions[j]+1]!=0.0) checkerok=true;
					}
				}
				if (checkerok)
				{
					for (int j=1; j<iq[0].length; j++)
					{
						if ((iq[i][j]!=0.0) && (!tochangev.contains(varref[j-1].toUpperCase()))) tochangev.add(varref[j-1].toUpperCase());
					}
				}
			}
		}
		if (eq!=null)
		{
			for (int i=0; i<eq.length; i++)
			{
				checkerok=false;
				for (int j=0; j<positions.length; j++)
				{
					if (positions[j]>=0)
					{
						if (eq[i][positions[j]+1]!=0.0) checkerok=true;
					}
				}
				if (checkerok)
				{
					for (int j=1; j<eq[0].length; j++)
					{
						if ((eq[i][j]!=0.0) && (!tochangev.contains(varref[j-1].toUpperCase()))) tochangev.add(varref[j-1].toUpperCase());
					}
				}
			}
		}
		for (int i=0; i<depvars.size(); i++)
		{
			if (tochangev.contains(depvars.get(i))) tochangev.remove(depvars.get(i));
		}
		if (mustmatchvars!=null)
		{
			if (mustmatchvars.length>0)
			{
				for (int i=0; i<mustmatchvars.length; i++)
				{
					tempvarname=(mustmatchvars[i]).toUpperCase();
					if (!tochangev.contains(mustmatchvars[i])) tochangev.add(mustmatchvars[i]);
				}
			}
		}
		return tochangev;
	}
	/**
	*Return the group to which the variables belongs
	*/
	public int getgroup()
	{
		return groupbelong;
	}
	/**
	*Return the name of the variables in the current group
	*/
	public Vector<String> getvaringroup()
	{
		return curtempdet;
	}
	/**
	*Return the variables defined in the disjoint edits according to the reference
	*/
	public Vector<Integer> getvarinde(int refe)
	{
		return disedit.get(refe);
	}
	/**
	*Return the name of the variables defined in the disjoint edits according to the reference
	*/
	public Vector<String> getvarindenames(int refe)
	{
		Vector<Integer> nameinedi=disedit.get(refe);
		Vector<String> retvnam=new Vector<String>();
		for (int i=0; i<nameinedi.size(); i++)
		{
			refvar=(nameinedi.get(i)).intValue();
			retvnam.add(varref[refvar].toUpperCase());
		}
		return retvnam;
	}
	/**
	*Return the disjoint inequalities referred to the reference disjoint edits
	*/
	public double[][] getdisiq(int refe)
	{
		return disiq.get(refe);
	}
	/**
	*Return the disjoint equalities referred to the reference disjoint edits
	*/
	public double[][] getdiseq(int refe)
	{
		return diseq.get(refe);
	}
	/**
	*Sets the tolerance
	*/
	public void setTolerance(double tolerance)
	{
		this.tolerance=tolerance;
	}
	public void setWeights(Hashtable<Integer, Double> valueweights)
	{
		this.valueweights=valueweights;
	}
	/**
	*Return the result of the record verification
	*/
	public boolean recordVerifier(int refe, double[] recval)
	{
		this.recval=recval;
		activevars.clear();
		verifyresult=true;
		double[][] tempiq=disiq.get(refe);
		double[][] tempeq=diseq.get(refe);
		if (tempiq!=null)
		{
			for (int i=0; i<tempiq.length; i++)
			{
				tempval=0;
				for (int j=1; j<tempiq[0].length; j++)
				{
					tempval=tempval+recval[j-1]*tempiq[i][j];
				}
				tempval=tempval+tempiq[i][0];
				if (tempval<(-1*tolerance))
				{
					verifyresult=false;
					for (int j=1; j<tempiq[0].length; j++)
					{
						if ((tempiq[i][j]!=0.0) && (!activevars.contains(new Integer(j-1)))) activevars.add(new Integer(j-1));
					}
				}
			}
		}
		if (tempeq!=null)
		{
			for (int i=0; i<tempeq.length; i++)
			{
				tempval=0;
				for (int j=1; j<tempeq[0].length; j++)
				{
					tempval=tempval+recval[j-1]*tempeq[i][j];
				}
				tempval=tempval+tempeq[i][0];
				if (Math.abs(tempval)>tolerance)
				{
					verifyresult=false;
					for (int j=1; j<tempeq[0].length; j++)
					{
						if ((tempeq[i][j]!=0.0) && (!activevars.contains(new Integer(j-1)))) activevars.add(new Integer(j-1));
					}
				}
			}
		}
		tempiq=new double[0][0];
		tempeq=new double[0][0];
		tempiq=null;
		tempeq=null;
		return verifyresult;
	}
	/**
	*Return the result of the record verification
	*/
	public boolean recordVerifierNoG(double[] recval)
	{
		if (iq!=null)
		{
			for (int i=0; i<iq.length; i++)
			{
				tempval=0;
				for (int j=1; j<iq[0].length; j++)
				{
					tempval=tempval+recval[j-1]*iq[i][j];
				}
				tempval=tempval+iq[i][0];
				if (tempval<(-1*tolerance)) return false;
			}
		}
		if (eq!=null)
		{
			for (int i=0; i<eq.length; i++)
			{
				tempval=0;
				for (int j=1; j<eq[0].length; j++)
				{
					tempval=tempval+recval[j-1]*eq[i][j];
				}
				tempval=tempval+eq[i][0];
				if (Math.abs(tempval)>tolerance) return false;
			}
		}
		return true;
	}
	/**
	*Starts the localization algorithm
	*/
	public void localize(int refe)
	{
		if (!filetoprint.equals(""))
		{
			recordprinted++;
			if (recordprinted>2) filetoprint="";
		}
		start_proc = System.currentTimeMillis();
		maxdefsolutionsvars.clear();
		maxdetsolutionsvars.clear();
		maxdefsolutions.clear();
		numdetsolforsol.clear();
		tempeqfc=new double[0][0];
		tempiqfc=new double[0][0];
		tempeqfc=null;
		tempiqfc=null;
		currentweights.clear();
		vectrefvname=disedit.get(refe);
		for (int i=0; i<vectrefvname.size(); i++)
		{
			tempval=(valueweights.get(vectrefvname.get(i))).doubleValue();
			currentweights.add(new Double(tempval));
		}
		toremove.clear();
		totest.clear();
		oldtotest.clear();
		originaltotest.clear();
		cardinality=-1;
		defsolutions.clear();
		defsolutionsvars.clear();
		detsolutionsvars.clear();
		if (vectrefvname.size()<=varsonestepsol) localizeallvars(refe);
		else localizewithtree(refe);
	}
	/**
	*Return the actual number of maximum variables that the algorithm should have been tested
	*/
	public int getactualmaxvar()
	{
		return actualmaxvar;
	}
	/**
	*Return true in case of maximum variable was reached
	*/
	public boolean ismaxvar()
	{
		return ismaxvarreached;
	}
	/**
	*Localize using the tree algorithm for complex problems
	*/
	private void localizewithtree(int refe)
	{
		actualaddingstep=0;
		ismaxvarreached=false;
		actualmaxvar=0;
		if (maxvar>vectrefvname.size())
			tempmaxvar=vectrefvname.size();
		else
			tempmaxvar=maxvar;
		totest.clear();
		originaltotest.clear();
		for (int i=0; i<activevars.size(); i++)
		{
			tempint=(activevars.get(i)).intValue();
			totest.add(new Integer(tempint));
			originaltotest.add(new Integer(tempint));
		}
		checkerok=prepareforcheck(refe);
		if (checkerok)
		{
			prepareforloc(refe);
			if (cardinality>=0)
			{
				totest.clear();
				oldtotest.clear();
				originaltotest.clear();
				tempeqfc=new double[0][0];
				tempiqfc=new double[0][0];
				tempeqfc=null;
				tempiqfc=null;
				return;
			}
		}
		totest.clear();
		oldtotest.clear();
		for (int i=0; i<originaltotest.size(); i++)
		{
			tempint=(originaltotest.get(i)).intValue();
			totest.add(new Integer(tempint));
			oldtotest.add(new Integer(tempint));
		}
		double[][] tempiq=disiq.get(refe);
		double[][] tempeq=diseq.get(refe);
		if (tempiq!=null)
		{
			tempiqfc=new double[tempiq.length][tempiq[0].length];
			for (int i=0; i<tempiq.length; i++)
			{
				for (int j=1; j<tempiq[0].length; j++)
				{
					tempiqfc[i][j]=tempiq[i][j];
				}
			}
		}
		if (tempeq!=null)
		{
			tempeqfc=new double[tempeq.length][tempeq[0].length];
			for (int i=0; i<tempeq.length; i++)
			{
				for (int j=1; j<tempeq[0].length; j++)
				{
					tempeqfc[i][j]=tempeq[i][j];
				}
			}
			refmeq=0;
			while (refmeq>=0)
			{
				numadded=0;
				refmeq=-1;
				refmv=vectrefvname.size();
				for (int i=0; i<tempeqfc.length; i++)
				{
					tempint=0;
					for (int j=1; j<tempeqfc[0].length; j++)
					{
						if ((tempeqfc[i][j]!=0.0) && (originaltotest.contains(new Integer(j-1)))) tempint++;
					}
					if (tempint>0)
					{
						tempint=0;
						for (int j=1; j<tempeqfc[0].length; j++)
						{
							if ((tempeqfc[i][j]!=0.0) && (!originaltotest.contains(new Integer(j-1))) && (!totest.contains(new Integer(j-1)))) tempint++;
						}
						if ((tempint>0) && (refmv>tempint))
						{
							refmv=tempint;
							refmeq=i;
						}
					}
				}
				if (refmeq>=0)
				{
					for (int j=1; j<tempeqfc[0].length; j++)
					{
						if (tempeqfc[refmeq][j]!=0.0)
						{
							tempeqfc[refmeq][j]=0.0;
							if (!totest.contains(new Integer(j-1)))
							{
								totest.add(new Integer(j-1));
								numadded++;
							}
						}
					}
				}
				if (totest.size()>=tempmaxvar)
				{
					numadded=0;
					refmeq=-1;
				}
				if (numadded>0)
				{
					checkerok=prepareforcheck(refe);
					if (checkerok) prepareforloc(refe);
					if (cardinality>0)
					{
						totest.clear();
						oldtotest.clear();
						originaltotest.clear();
						tempiq=new double[0][0];
						tempeq=new double[0][0];
						tempiq=null;
						tempeq=null;
						tempeqfc=new double[0][0];
						tempiqfc=new double[0][0];
						tempeqfc=null;
						tempiqfc=null;
						return;
					}
					totest.clear();
					for (int i=0; i<originaltotest.size(); i++)
					{
						tempint=(originaltotest.get(i)).intValue();
						totest.add(new Integer(tempint));
					}
					for (int i=0; i<oldtotest.size(); i++)
					{
						tempint=(oldtotest.get(i)).intValue();
						if (!totest.contains(new Integer(tempint))) totest.add(new Integer(tempint));
					}
				}
			}
		}
		if (tempiq!=null)
		{
			tempiqfc=new double[tempiq.length][tempiq[0].length];
			for (int i=0; i<tempiq.length; i++)
			{
				for (int j=1; j<tempiq[0].length; j++)
				{
					tempiqfc[i][j]=tempiq[i][j];
				}
			}
		}
		if (tempeq!=null)
		{
			tempeqfc=new double[tempeq.length][tempeq[0].length];
			for (int i=0; i<tempeq.length; i++)
			{
				for (int j=1; j<tempeq[0].length; j++)
				{
					tempeqfc[i][j]=tempeq[i][j];
				}
			}
		}
		totest.clear();
		for (int i=0; i<originaltotest.size(); i++)
		{
			tempint=(originaltotest.get(i)).intValue();
			totest.add(new Integer(tempint));
		}
		lastadded=0;
		actualaddingstep=0;
		toremove.clear();
		ismaxvarreached=false;
		while (totest.size()>0 && !ismaxvarreached)
		{
			actualaddingstep++;
			lastadded=numadded;
			numadded=0;
			refmiq=-1;
			refmeq=-1;
			refmv=vectrefvname.size();
			if (tempiqfc!=null)
			{
				for (int i=0; i<tempiqfc.length; i++)
				{
					tempint=0;
					for (int j=1; j<tempiqfc[0].length; j++)
					{
						if ((tempiqfc[i][j]!=0.0) && (originaltotest.contains(new Integer(j-1)))) tempint++;
					}
					if ((tempint>0) && (refmv>tempint))
					{
						refmv=tempint;
						refmiq=i;
					}
				}
			}
			refmv=vectrefvname.size();
			if (tempeqfc!=null)
			{
				for (int i=0; i<tempeqfc.length; i++)
				{
					tempint=0;
					for (int j=1; j<tempeqfc[0].length; j++)
					{
						if ((tempeqfc[i][j]!=0.0) && (originaltotest.contains(new Integer(j-1)))) tempint++;
					}
					if ((tempint>0) && (refmv>tempint))
					{
						refmv=tempint;
						refmeq=i;
					}
				}
			}
			if (refmiq>=0)
			{
				for (int j=1; j<tempiqfc[0].length; j++)
				{
					if (tempiqfc[refmiq][j]!=0.0)
					{
						tempiqfc[refmiq][j]=0.0;
						if (!totest.contains(new Integer(j-1)))
						{
							totest.add(new Integer(j-1));
							numadded++;
						}
					}
				}
			}
			if (refmeq>=0)
			{
				for (int j=1; j<tempeqfc[0].length; j++)
				{
					if (tempeqfc[refmeq][j]!=0.0)
					{
						tempeqfc[refmeq][j]=0.0;
						if (!totest.contains(new Integer(j-1)))
						{
							totest.add(new Integer(j-1));
							numadded++;
						}
					}
				}
			}
			if (actualaddingstep==1)
			{
				Vector<Integer> att=new Vector<Integer>();
				for (int i=0; i<totest.size(); i++)
				{
					att.add(totest.get(i));
				}
				toremove.put(new Integer(actualaddingstep), att);
			}
			else
			{
				lasttested=toremove.get(new Integer(actualaddingstep-1));
				Vector<Integer> att=new Vector<Integer>();
				for (int i=0; i<totest.size(); i++)
				{
					if (!lasttested.contains(new Integer(totest.get(i))))
						att.add(totest.get(i));
				}
				toremove.put(new Integer(actualaddingstep), att);
			}
			if (totest.size()>tempmaxvar)
			{
				if (actualaddingstep>3)
				{
					for (int i=(actualaddingstep-3); i>1; i--)
					{
						lasttested=toremove.get(new Integer(i));
						for (int j=0; j< lasttested.size(); j++)
						{
							totest.remove(lasttested.get(j));
						}
						if (totest.size()<tempmaxvar)
						{
							break;
						}
					}
					if (totest.size()>tempmaxvar)
					{
						ismaxvarreached=true;
					}
				}
				else
				{
					ismaxvarreached=true;
				}
			}
			if (numadded>0 && !ismaxvarreached)
			{
				checkerok=prepareforcheck(refe);
				if (checkerok)
				{
					prepareforloc(refe);
					if (cardinality>0)
					{
						totest.clear();
						oldtotest.clear();
						originaltotest.clear();
						tempiq=new double[0][0];
						tempeq=new double[0][0];
						tempiq=null;
						tempeq=null;
						tempeqfc=new double[0][0];
						tempiqfc=new double[0][0];
						tempeqfc=null;
						tempiqfc=null;
						return;
					}
					totest.clear();
					for (int i=0; i<originaltotest.size(); i++)
					{
						tempint=(originaltotest.get(i)).intValue();
						totest.add(new Integer(tempint));
					}
					for (int i=0; i<oldtotest.size(); i++)
					{
						tempint=(oldtotest.get(i)).intValue();
						if (!totest.contains(new Integer(tempint))) totest.add(new Integer(tempint));
					}
				}
				else
				{
					if (actualaddingstep>1)
					{
						totest.clear();
						for (int i=1; i<=actualaddingstep; i++)
						{
							lasttested=toremove.get(new Integer(i));
							for (int j=0; j< lasttested.size(); j++)
							{
								if (!totest.contains(new Integer(lasttested.get(j)))) totest.add(lasttested.get(j));
							}
						}
					}
				}
			}
			else
			{
				if (actualaddingstep>1)
				{
					totest.clear();
					for (int i=1; i<=actualaddingstep; i++)
					{
						lasttested=toremove.get(new Integer(i));
						for (int j=0; j< lasttested.size(); j++)
						{
							if (!totest.contains(new Integer(lasttested.get(j)))) totest.add(lasttested.get(j));
						}
					}
				}
			}
			if ((refmiq==-1) && (refmeq==-1)) totest.clear();
		}
		totest.clear();
		for (int i=0; i<originaltotest.size(); i++)
		{
			tempint=(originaltotest.get(i)).intValue();
			totest.add(new Integer(tempint));
		}
		lastadded=0;
		actualaddingstep=0;
		toremove.clear();
		ismaxvarreached=false;
		while (totest.size()>0 && !ismaxvarreached)
		{
			actualaddingstep++;
			lastadded=numadded;
			numadded=0;
			refmiq=-1;
			refmeq=-1;
			refmv=vectrefvname.size();
			if (tempiqfc!=null)
			{
				for (int i=0; i<tempiqfc.length; i++)
				{
					tempint=0;
					for (int j=1; j<tempiqfc[0].length; j++)
					{
						if ((tempiqfc[i][j]!=0.0) && (originaltotest.contains(new Integer(j-1)))) tempint++;
					}
					if ((tempint>0) && (refmv>tempint))
					{
						refmv=tempint;
						refmiq=i;
					}
				}
			}
			refmv=vectrefvname.size();
			if (tempeqfc!=null)
			{
				for (int i=0; i<tempeqfc.length; i++)
				{
					tempint=0;
					for (int j=1; j<tempeqfc[0].length; j++)
					{
						if ((tempeqfc[i][j]!=0.0) && (originaltotest.contains(new Integer(j-1)))) tempint++;
					}
					if ((tempint>0) && (refmv>tempint))
					{
						refmv=tempint;
						refmeq=i;
					}
				}
			}
			if (refmiq>=0)
			{
				for (int j=1; j<tempiqfc[0].length; j++)
				{
					if (tempiqfc[refmiq][j]!=0.0)
					{
						tempiqfc[refmiq][j]=0.0;
						if (!totest.contains(new Integer(j-1)))
						{
							totest.add(new Integer(j-1));
							numadded++;
						}
					}
				}
			}
			if (refmeq>=0)
			{
				for (int j=1; j<tempeqfc[0].length; j++)
				{
					if (tempeqfc[refmeq][j]!=0.0)
					{
						tempeqfc[refmeq][j]=0.0;
						if (!totest.contains(new Integer(j-1)))
						{
							totest.add(new Integer(j-1));
							numadded++;
						}
					}
				}
			}
			if (actualaddingstep==1)
			{
				Vector<Integer> att=new Vector<Integer>();
				for (int i=0; i<totest.size(); i++)
				{
					att.add(totest.get(i));
				}
				toremove.put(new Integer(actualaddingstep), att);
			}
			else
			{
				lasttested=toremove.get(new Integer(actualaddingstep-1));
				Vector<Integer> att=new Vector<Integer>();
				for (int i=0; i<totest.size(); i++)
				{
					if (!lasttested.contains(new Integer(totest.get(i))))
						att.add(totest.get(i));
				}
				toremove.put(new Integer(actualaddingstep), att);
			}
			if (totest.size()>tempmaxvar)
			{
				if (actualaddingstep>=2)
				{
					for (int i=(actualaddingstep-1); i>1; i--)
					{
						lasttested=toremove.get(new Integer(i));
						for (int j=0; j< lasttested.size(); j++)
						{
							totest.remove(lasttested.get(j));
						}
						if (totest.size()<tempmaxvar)
						{
							break;
						}
					}
					if (totest.size()>tempmaxvar)
					{
						ismaxvarreached=true;
					}
				}
				else
				{
					ismaxvarreached=true;
				}
			}
			if (numadded>0 && !ismaxvarreached)
			{
				checkerok=prepareforcheck(refe);
				if (checkerok)
				{
					prepareforloc(refe);
					if (cardinality>0)
					{
						totest.clear();
						oldtotest.clear();
						originaltotest.clear();
						tempiq=new double[0][0];
						tempeq=new double[0][0];
						tempiq=null;
						tempeq=null;
						tempeqfc=new double[0][0];
						tempiqfc=new double[0][0];
						tempeqfc=null;
						tempiqfc=null;
						return;
					}
					totest.clear();
					for (int i=0; i<originaltotest.size(); i++)
					{
						tempint=(originaltotest.get(i)).intValue();
						totest.add(new Integer(tempint));
					}
					for (int i=0; i<oldtotest.size(); i++)
					{
						tempint=(oldtotest.get(i)).intValue();
						if (!totest.contains(new Integer(tempint))) totest.add(new Integer(tempint));
					}
				}
				else
				{
					if (actualaddingstep>1)
					{
						totest.clear();
						for (int i=1; i<=actualaddingstep; i++)
						{
							lasttested=toremove.get(new Integer(i));
							for (int j=0; j< lasttested.size(); j++)
							{
								if (!totest.contains(new Integer(lasttested.get(j)))) totest.add(lasttested.get(j));
							}
						}
					}
				}
			}
			else
			{
				if (actualaddingstep>1)
				{
					totest.clear();
					for (int i=1; i<=actualaddingstep; i++)
					{
						lasttested=toremove.get(new Integer(i));
						for (int j=0; j< lasttested.size(); j++)
						{
							if (!totest.contains(new Integer(lasttested.get(j)))) totest.add(lasttested.get(j));
						}
					}
				}
			}
			if ((refmiq==-1) && (refmeq==-1)) totest.clear();
		}
		for (int i=0; i<originaltotest.size(); i++)
		{
			tempint=(originaltotest.get(i)).intValue();
			totest.add(new Integer(tempint));
		}
		totest.clear();
		for (int i=0; i<originaltotest.size(); i++)
		{
			tempint=(originaltotest.get(i)).intValue();
			totest.add(new Integer(tempint));
		}
		lastadded=0;
		actualaddingstep=0;
		toremove.clear();
		ismaxvarreached=false;
		while (totest.size()>0 && !ismaxvarreached)
		{
			actualaddingstep++;
			lastadded=numadded;
			numadded=0;
			refmiq=-1;
			refmeq=-1;
			refmv=vectrefvname.size();
			if (tempiqfc!=null)
			{
				for (int i=0; i<tempiqfc.length; i++)
				{
					tempint=0;
					for (int j=1; j<tempiqfc[0].length; j++)
					{
						if ((tempiqfc[i][j]!=0.0) && (originaltotest.contains(new Integer(j-1)))) tempint++;
					}
					if ((tempint>0) && (refmv>tempint))
					{
						refmv=tempint;
						refmiq=i;
					}
				}
			}
			refmv=vectrefvname.size();
			if (tempeqfc!=null)
			{
				for (int i=0; i<tempeqfc.length; i++)
				{
					tempint=0;
					for (int j=1; j<tempeqfc[0].length; j++)
					{
						if ((tempeqfc[i][j]!=0.0) && (originaltotest.contains(new Integer(j-1)))) tempint++;
					}
					if ((tempint>0) && (refmv>tempint))
					{
						refmv=tempint;
						refmeq=i;
					}
				}
			}
			if (refmiq>=0)
			{
				for (int j=1; j<tempiqfc[0].length; j++)
				{
					if (tempiqfc[refmiq][j]!=0.0)
					{
						tempiqfc[refmiq][j]=0.0;
						if (!totest.contains(new Integer(j-1)))
						{
							totest.add(new Integer(j-1));
							numadded++;
						}
					}
				}
			}
			if (refmeq>=0)
			{
				for (int j=1; j<tempeqfc[0].length; j++)
				{
					if (tempeqfc[refmeq][j]!=0.0)
					{
						tempeqfc[refmeq][j]=0.0;
						if (!totest.contains(new Integer(j-1)))
						{
							totest.add(new Integer(j-1));
							numadded++;
						}
					}
				}
			}
			if (actualaddingstep==1)
			{
				Vector<Integer> att=new Vector<Integer>();
				for (int i=0; i<totest.size(); i++)
				{
					att.add(totest.get(i));
				}
				toremove.put(new Integer(actualaddingstep), att);
			}
			else
			{
				lasttested=toremove.get(new Integer(actualaddingstep-1));
				Vector<Integer> att=new Vector<Integer>();
				for (int i=0; i<totest.size(); i++)
				{
					if (!lasttested.contains(new Integer(totest.get(i))))
						att.add(totest.get(i));
				}
				toremove.put(new Integer(actualaddingstep), att);
			}
			if (totest.size()>tempmaxvar)
			{
				if (actualaddingstep>1)
				{
					for (int i=(actualaddingstep-1); i>=0; i--)
					{
						lasttested=toremove.get(new Integer(i));
						for (int j=0; j< lasttested.size(); j++)
						{
							totest.remove(lasttested.get(j));
						}
						if (totest.size()<tempmaxvar)
						{
							break;
						}
					}
					if (totest.size()>tempmaxvar)
					{
						ismaxvarreached=true;
					}
				}
				else
				{
					ismaxvarreached=true;
				}
			}
			if (numadded>0 && !ismaxvarreached)
			{
				checkerok=prepareforcheck(refe);
				if (checkerok)
				{
					prepareforloc(refe);
					if (cardinality>0)
					{
						totest.clear();
						oldtotest.clear();
						originaltotest.clear();
						tempiq=new double[0][0];
						tempeq=new double[0][0];
						tempiq=null;
						tempeq=null;
						tempeqfc=new double[0][0];
						tempiqfc=new double[0][0];
						tempeqfc=null;
						tempiqfc=null;
						return;
					}
					totest.clear();
					for (int i=0; i<originaltotest.size(); i++)
					{
						tempint=(originaltotest.get(i)).intValue();
						totest.add(new Integer(tempint));
					}
					for (int i=0; i<oldtotest.size(); i++)
					{
						tempint=(oldtotest.get(i)).intValue();
						if (!totest.contains(new Integer(tempint))) totest.add(new Integer(tempint));
					}
				}
				else
				{
					if (actualaddingstep>1)
					{
						totest.clear();
						for (int i=1; i<=actualaddingstep; i++)
						{
							lasttested=toremove.get(new Integer(i));
							for (int j=0; j< lasttested.size(); j++)
							{
								if (!totest.contains(new Integer(lasttested.get(j)))) totest.add(lasttested.get(j));
							}
						}
					}
				}
			}
			else
			{
				if (actualaddingstep>1)
				{
					totest.clear();
					for (int i=1; i<=actualaddingstep; i++)
					{
						lasttested=toremove.get(new Integer(i));
						for (int j=0; j< lasttested.size(); j++)
						{
							if (!totest.contains(new Integer(lasttested.get(j)))) totest.add(lasttested.get(j));
						}
					}
				}
			}
			if ((refmiq==-1) && (refmeq==-1)) totest.clear();
		}
		totest.clear();
		for (int i=0; i<originaltotest.size(); i++)
		{
			tempint=(originaltotest.get(i)).intValue();
			totest.add(new Integer(tempint));
		}
		tempiqfc=null;
		tempeqfc=null;
		if (tempiq!=null)
		{
			tempiqfc=new double[tempiq.length][tempiq[0].length];
			for (int i=0; i<tempiq.length; i++)
			{
				for (int j=1; j<tempiq[0].length; j++)
				{
					tempiqfc[i][j]=tempiq[i][j];
				}
			}
		}
		if (tempeq!=null)
		{
			tempeqfc=new double[tempeq.length][tempeq[0].length];
			for (int i=0; i<tempeq.length; i++)
			{
				for (int j=1; j<tempeq[0].length; j++)
				{
					tempeqfc[i][j]=tempeq[i][j];
				}
			}
		}
		while (totest.size()<tempmaxvar)
		{
			numadded=0;
			refmiq=-1;
			refmeq=-1;
			refmv=vectrefvname.size();
			if (tempiqfc!=null)
			{
				for (int i=0; i<tempiqfc.length; i++)
				{
					tempint=0;
					for (int j=1; j<tempiqfc[0].length; j++)
					{
						if ((tempiqfc[i][j]!=0.0) && (totest.contains(new Integer(j-1)))) tempint++;
					}
					if ((tempint>0) && (refmv>tempint))
					{
						refmv=tempint;
						refmiq=i;
					}
				}
			}
			if (tempeqfc!=null)
			{
				for (int i=0; i<tempeqfc.length; i++)
				{
					tempint=0;
					for (int j=1; j<tempeqfc[0].length; j++)
					{
						if ((tempeqfc[i][j]!=0.0) && (totest.contains(new Integer(j-1)))) tempint++;
					}
					if ((tempint>0) && (refmv>tempint))
					{
						refmiq=-1;
						refmv=tempint;
						refmeq=i;
					}
				}
			}
			if (refmiq>=0)
			{
				for (int j=1; j<tempiqfc[0].length; j++)
				{
					if (tempiqfc[refmiq][j]!=0.0)
					{
						tempiqfc[refmiq][j]=0.0;
						if (!totest.contains(new Integer(j-1)))
						{
							totest.add(new Integer(j-1));
							numadded++;
						}
					}
				}
			}
			if (refmeq>=0)
			{
				for (int j=1; j<tempeqfc[0].length; j++)
				{
					if (tempeqfc[refmeq][j]!=0.0)
					{
						tempeqfc[refmeq][j]=0.0;
						if (!totest.contains(new Integer(j-1)))
						{
							totest.add(new Integer(j-1));
							numadded++;
						}
					}
				}
			}
			if (totest.size()>=tempmaxvar)
			{
				ismaxvarreached=true;
				actualmaxvar=totest.size();
				return;
			}
			if (numadded>0)
			{
				checkerok=prepareforcheck(refe);
				if (checkerok)
				{
					prepareforloc(refe);
					if (cardinality>0)
					{
						totest.clear();
						oldtotest.clear();
						originaltotest.clear();
						tempiq=new double[0][0];
						tempeq=new double[0][0];
						tempiq=null;
						tempeq=null;
						tempeqfc=new double[0][0];
						tempiqfc=new double[0][0];
						tempeqfc=null;
						tempiqfc=null;
						return;
					}
					totest.clear();
					for (int i=0; i<oldtotest.size(); i++)
					{
						tempint=(oldtotest.get(i)).intValue();
						totest.add(new Integer(tempint));
					}
				}
			}
		}
	}
	/**
	*Prepare the simple matrices in order to find the variables to change
	*/
	private boolean prepareforcheck(int refe)
	{
		double[][] tempiq=disiq.get(refe);
		double[][] tempeq=diseq.get(refe);
		iqdef.clear();
		eqdef.clear();
		if (tempiq!=null)
		{
			for (int i=0; i<tempiq.length; i++)
			{
				Vector<Double> elementiqdef=new Vector<Double>();
				for (int j=0; j<totest.size()+1; j++)
				{
					elementiqdef.add(new Double(0.0));
				}
				tempval=0;
				tempdou=0;
				for (int j=0; j<totest.size(); j++)
				{
					tempint=(totest.get(j)).intValue();
					tempval=tempval+recval[tempint]*tempiq[i][tempint+1];
					if (tempiq[i][tempint+1]!=0.0) elementiqdef.set(j+1, new Double(tempiq[i][tempint+1]));
					tempdou=tempdou+Math.abs(tempiq[i][tempint+1]);
				}
				for (int j=1; j<tempiq[0].length; j++)
				{
					if (!totest.contains(new Integer(j-1)))
					{
						tempval=tempval+recval[j-1]*tempiq[i][j];
					}
				}
				tempval=tempiq[i][0]+tempval;
				elementiqdef.set(0, new Double(tempval));
				if (tempdou>0.0)
				{
					if (!iqdef.contains(elementiqdef)) iqdef.add(elementiqdef);
				}
			}
		}
		if (tempeq!=null)
		{
			for (int i=0; i<tempeq.length; i++)
			{
				Vector<Double> elementeqdef=new Vector<Double>();
				for (int j=0; j<totest.size()+1; j++)
				{
					elementeqdef.add(new Double(0.0));
				}
				tempval=0;
				tempdou=0;
				for (int j=0; j<totest.size(); j++)
				{
					tempint=(totest.get(j)).intValue();
					tempval=tempval+recval[tempint]*tempeq[i][tempint+1];
					if (tempeq[i][tempint+1]!=0.0) elementeqdef.set(j+1, new Double(tempeq[i][tempint+1]));
					tempdou=tempdou+Math.abs(tempeq[i][tempint+1]);
				}
				for (int j=1; j<tempeq[0].length; j++)
				{
					if (!totest.contains(new Integer(j-1)))
					{
						tempval=tempval+recval[j-1]*tempeq[i][j];
					}
				}
				tempval=tempeq[i][0]+tempval;
				elementeqdef.set(0, new Double(tempval));
				if (tempdou>0.0)
				{
					if (!eqdef.contains(elementeqdef)) eqdef.add(elementeqdef);
				}
			}
		}
		defeq=null;
		defiq=null;
		if (eqdef.size()>0)
		{
			defeq=new double[eqdef.size()][vectrefvname.size()+1];
			for (int i=0; i<eqdef.size(); i++)
			{
				Vector<Double> elementeqdef=eqdef.get(i);
				for (int j=0; j<elementeqdef.size(); j++)
				{
					defeq[i][j]=(elementeqdef.get(j)).doubleValue();
				}
			}
		}
		if (iqdef.size()>0)
		{
			defiq=new double[iqdef.size()][vectrefvname.size()+1];
			for (int i=0; i<iqdef.size(); i++)
			{
				Vector<Double> elementiqdef=iqdef.get(i);
				for (int j=0; j<elementiqdef.size(); j++)
				{
					defiq[i][j]=(elementiqdef.get(j)).doubleValue();
				}
			}
		}
		tempint=0;
		try
		{
			ec.getResult(defeq, defiq);
			tempint=ec.getnumsol();
			if (tempint>0)
			{
				Vector<Integer> temptotest=ec.getCandidates();
				if (temptotest.size()>0)
				{
					oldtotest.clear();
					for (int i=0; i<totest.size(); i++)
					{
						tempint=(totest.get(i)).intValue();
						oldtotest.add(new Integer(tempint));
					}
					totest.clear();
					for (int i=0; i<temptotest.size(); i++)
					{
						tempint=(temptotest.get(i)).intValue();
						totest.add(oldtotest.get(tempint));
					}
					ec.cleanMem();
					tempiq=new double[0][0];
					tempeq=new double[0][0];
					defeq=new double[0][0];
					defiq=new double[0][0];
					iqdef.clear();
					eqdef.clear();
					tempiq=null;
					tempeq=null;
					defeq=null;
					defiq=null;
					return true;
				}
			}
		}
		catch (Exception e) {}
		tempiq=new double[0][0];
		tempeq=new double[0][0];
		defeq=new double[0][0];
		defiq=new double[0][0];
		iqdef.clear();
		eqdef.clear();
		tempiq=null;
		tempeq=null;
		defeq=null;
		defiq=null;
		ec.cleanMem();
		return false;
	}
	/**
	*Prepare the matrices to be tested
	*/
	private void prepareforloc(int refe)
	{
		double[][] tempiq=disiq.get(refe);
		double[][] tempeq=diseq.get(refe);
		iqdef.clear();
		eqdef.clear();
		if (tempiq!=null)
		{
			for (int i=0; i<tempiq.length; i++)
			{
				Vector<Double> elementiqdef=new Vector<Double>();
				for (int j=0; j<totest.size()*2+1; j++)
				{
					elementiqdef.add(new Double(0.0));
				}
				tempval=0;
				tempdou=0;
				for (int j=0; j<totest.size(); j++)
				{
					tempint=(totest.get(j)).intValue();
					if (tempiq[i][tempint+1]!=0.0)
					{
						tempval=tempval+recval[tempint]*tempiq[i][tempint+1];
						elementiqdef.set(j+1, new Double(tempiq[i][tempint+1]));
						elementiqdef.set(j+totest.size()+1, new Double(-1*tempiq[i][tempint+1]));
						tempdou=tempdou+Math.abs(tempiq[i][tempint+1]);
					}
				}
				for (int j=1; j<tempiq[0].length; j++)
				{
					if (!totest.contains(new Integer(j-1)))
					{
						tempval=tempval+recval[j-1]*tempiq[i][j];
					}
				}
				tempval=tempiq[i][0]+tempval;
				elementiqdef.set(0, new Double(tempval));
				if (tempdou>0.0)
				{
					if (!iqdef.contains(elementiqdef)) iqdef.add(elementiqdef);
				}
			}
		}
		for (int i=0; i<totest.size()*2; i++)
		{
			Vector<Double> elementiqdef=new Vector<Double>();
			for (int j=0; j<totest.size()*2+1; j++)
			{
				if (i==j-1) elementiqdef.add(new Double(1.0));
				else elementiqdef.add(new Double(0.0));
			}
			if (!iqdef.contains(elementiqdef)) iqdef.add(elementiqdef);
		}
		if (tempeq!=null)
		{
			for (int i=0; i<tempeq.length; i++)
			{
				Vector<Double> elementeqdef=new Vector<Double>();
				for (int j=0; j<totest.size()*2+1; j++)
				{
					elementeqdef.add(new Double(0.0));
				}
				tempval=0;
				tempdou=0;
				for (int j=0; j<totest.size(); j++)
				{
					tempint=(totest.get(j)).intValue();
					if (tempeq[i][tempint+1]!=0.0)
					{
						tempval=tempval+recval[tempint]*tempeq[i][tempint+1];
						elementeqdef.set(j+1, new Double(tempeq[i][tempint+1]));
						elementeqdef.set(j+totest.size()+1, new Double(-1*tempeq[i][tempint+1]));
						tempdou=tempdou+Math.abs(tempeq[i][tempint+1]);
					}
				}
				for (int j=1; j<tempeq[0].length; j++)
				{
					if (!totest.contains(new Integer(j-1)))
					{
						tempval=tempval+recval[j-1]*tempeq[i][j];
					}
				}
				tempval=tempeq[i][0]+tempval;
				elementeqdef.set(0, new Double(tempval));
				if (tempdou>0.0)
				{
					if (!eqdef.contains(elementeqdef)) eqdef.add(elementeqdef);
				}
			}
		}
		tempiq=new double[0][0];
		tempeq=new double[0][0];
		tempiq=null;
		tempeq=null;
		findsolutionsred(refe);
	}
	/**
	*Localize using all the variables
	*/
	private void localizeallvars(int refe)
	{
		double[][] tempiq=disiq.get(refe);
		double[][] tempeq=diseq.get(refe);
		iqdef.clear();
		eqdef.clear();
		if (tempiq!=null)
		{
			for (int i=0; i<tempiq.length; i++)
			{
				Vector<Double> elementiqdef=new Vector<Double>();
				for (int j=0; j<vectrefvname.size()*2+1; j++)
				{
					elementiqdef.add(new Double(0.0));
				}
				tempval=0;
				tempdou=0;
				for (int j=1; j<tempiq[0].length; j++)
				{
					tempval=tempval+recval[j-1]*tempiq[i][j];
					if (tempiq[i][j]!=0.0)
					{
						elementiqdef.set(j, new Double(tempiq[i][j]));
						elementiqdef.set(j+tempiq[0].length-1, new Double(-1*tempiq[i][j]));
					}
					else
					{
						elementiqdef.set(j, new Double(0.0));
						elementiqdef.set(j+tempiq[0].length-1, new Double(0.0));
					}
					tempdou=tempdou+Math.abs(tempiq[i][j]);
				}
				tempval=tempiq[i][0]+tempval;
				elementiqdef.set(0, new Double(tempval));
				if (tempdou>0.0)
				{
					if (!iqdef.contains(elementiqdef)) iqdef.add(elementiqdef);
				}
			}
		}
		for (int i=0; i<vectrefvname.size()*2; i++)
		{
			Vector<Double> elementiqdef=new Vector<Double>();
			for (int j=0; j<vectrefvname.size()*2+1; j++)
			{
				if (i==j-1) elementiqdef.add(new Double(1.0));
				else elementiqdef.add(new Double(0.0));
			}
			if (!iqdef.contains(elementiqdef)) iqdef.add(elementiqdef);
		}
		if (tempeq!=null)
		{
			for (int i=0; i<tempeq.length; i++)
			{
				Vector<Double> elementeqdef=new Vector<Double>();
				for (int j=0; j<vectrefvname.size()*2+1; j++)
				{
					elementeqdef.add(new Double(0.0));
				}
				tempval=0;
				tempdou=0;
				for (int j=1; j<tempeq[0].length; j++)
				{
					tempval=tempval+recval[j-1]*tempeq[i][j];
					if (tempeq[i][j]!=0.0)
					{
						elementeqdef.set(j, new Double(tempeq[i][j]));
						elementeqdef.set(j+tempeq[0].length-1, new Double(-1*tempeq[i][j]));
					}
					else
					{
						elementeqdef.set(j, new Double(0.0));
						elementeqdef.set(j+tempeq[0].length-1, new Double(0.0));
					}
					tempdou=tempdou+Math.abs(tempeq[i][j]);
				}
				tempval=tempeq[i][0]+tempval;
				elementeqdef.set(0, new Double(tempval));
				if (tempdou>0.0)
				{
					if (!eqdef.contains(elementeqdef)) eqdef.add(elementeqdef);
				}
			}
		}
		tempiq=new double[0][0];
		tempeq=new double[0][0];
		tempiq=null;
		tempeq=null;
		findsolutions(refe);
	}
	/**
	*Finds the solutions using the Polco library
	*/
	private void findsolutions(int refe)
	{
		defeq=null;
		defiq=null;
		if (eqdef.size()>0)
		{
			defeq=new double[eqdef.size()][vectrefvname.size()*2+1];
			for (int i=0; i<eqdef.size(); i++)
			{
				Vector<Double> elementeqdef=eqdef.get(i);
				for (int j=0; j<elementeqdef.size(); j++)
				{
					defeq[i][j]=(elementeqdef.get(j)).doubleValue();
				}
			}
		}
		if (iqdef.size()>0)
		{
			defiq=new double[iqdef.size()][vectrefvname.size()*2+1];
			for (int i=0; i<iqdef.size(); i++)
			{
				Vector<Double> elementiqdef=iqdef.get(i);
				for (int j=0; j<elementiqdef.size(); j++)
				{
					defiq[i][j]=(elementiqdef.get(j)).doubleValue();
				}
			}
		}
		if (!filetoprint.equals("")) printmatrices();
		es.setzeroalg(zeroalg);
		es.setEditsToSolve(diseq.get(refe), disiq.get(refe), recval, tolerance);
		cardinality=-1;
		try
		{
			es.getTotalSolutions(currentweights, defeq, defiq);
			cardinality=es.getCardinality();
			solutions=es.getSolutions();
			if (solutions.size()==0) cardinality=-1;
		}
		catch (Exception ees)
		{
			es.cleanMem();
		}
		if (cardinality!=-1)
		{
			if (vectrefvname.size()==2)
			{
				firstdz=false;
				seconddz=false;
				both=false;
				tempint=-1;
				for (int i=0; i<solutions.size(); i++)
				{
					actualsol=solutions.get(i);
					if (actualsol[0]!=0.0) firstdz=true;
					if (actualsol[1]!=0.0) seconddz=true;
					if ((actualsol[1]!=0.0) && (actualsol[0]!=0.0)) both=true;
				}
				if (!both)
				{
					if ((recval[0]!=0.0) && (recval[1]==0.0))
					{
						if (firstdz && seconddz) tempint=1;
					}
					else if ((recval[1]!=0.0) && (recval[0]==0.0))
					{
						if (firstdz && seconddz) tempint=0;
					}
				}
				if (tempint>=0)
				{
					currentpointer.clear();
					for (int i=0; i<solutions.size(); i++)
					{
						actualsol=solutions.get(i);
						if ((tempint==1) && (actualsol[1]!=0.0))
						{
							currentpointer.add(new Integer(1));
							double[] soltoconsider=new double[1];
							soltoconsider[0]=recval[1]+actualsol[1];
							defsolutions.add(soltoconsider);
							tempint=(vectrefvname.get(1)).intValue();
							String[] nametoconsider=new String[1];
							nametoconsider[0]=varref[tempint];
							defsolutionsvars.add(nametoconsider);
							if (useonlyintdetsol && actualsol[1]%1!=0)
							{
								detsolutionsvars.add(null);
								numdetsolforsol.add(new Integer(0));
							}
							else checkdeterministic(refe);
						}
						if ((tempint==0) && (actualsol[0]!=0.0))
						{
							currentpointer.add(new Integer(0));
							double[] soltoconsider=new double[1];
							soltoconsider[0]=recval[0]+actualsol[0];
							defsolutions.add(soltoconsider);
							tempint=(vectrefvname.get(0)).intValue();
							String[] nametoconsider=new String[1];
							nametoconsider[0]=varref[tempint];
							defsolutionsvars.add(nametoconsider);
							if (useonlyintdetsol && actualsol[0]%1!=0)
							{
								detsolutionsvars.add(null);
								numdetsolforsol.add(new Integer(0));
							}
							else checkdeterministic(refe);
						}
					}
				}
				else
				{
					currentpointer.clear();
					for (int i=0; i<solutions.size(); i++)
					{
						actualsol=solutions.get(i);
						realnumsol=0;
						for (int j=0; j<actualsol.length; j++)
						{
							if (actualsol[j]!=0.0) realnumsol++;
						}
						double[] soltoconsider=new double[realnumsol];
						String[] nametoconsider=new String[realnumsol];
						realnumsol=0;
						numsolwithdec=0;
						for (int j=0; j<actualsol.length; j++)
						{
							if (actualsol[j]!=0.0)
							{
								currentpointer.add(new Integer(j));
								soltoconsider[realnumsol]=recval[j]+actualsol[j];
								if (soltoconsider[realnumsol]%1!=0) numsolwithdec++;
								tempint=(vectrefvname.get(j)).intValue();
								nametoconsider[realnumsol]=varref[tempint];
								realnumsol++;
							}
						}
						defsolutions.add(soltoconsider);
						defsolutionsvars.add(nametoconsider);
						if (maxcardfordetsol>currentpointer.size())
						{
							if (useonlyintdetsol && numsolwithdec>0)
							{
								detsolutionsvars.add(null);
								numdetsolforsol.add(new Integer(0));
							}
							else checkdeterministic(refe);
						}
						else
						{
							detsolutionsvars.add(null);
							numdetsolforsol.add(new Integer(0));
						}
					}
				}
			}
			else
			{
				onesoldiffzero=false;
				numsoldiffzero=0;
				for (int i=0; i<solutions.size(); i++)
				{
					actualsol=solutions.get(i);
					tempval=0.0;
					for (int j=0; j<actualsol.length; j++)
					{
						if (actualsol[j]!=0.0)
							tempval=tempval+Math.abs(recval[j]+actualsol[j]);
					}
					if (tempval!=0.0) numsoldiffzero++;
				}
				if ((numsoldiffzero>0) && (numsoldiffzero<solutions.size()))
				{
					for (int i=0; i<solutions.size(); i++)
					{
						actualsol=solutions.get(i);
						tempval=0.0;
						for (int j=0; j<actualsol.length; j++)
						{
							if (actualsol[j]!=0.0)
								tempval=tempval+Math.abs(recval[j]+actualsol[j]);
						}
						if (tempval==0.0) solutions.set(i, null);
					}
				}
				indexsol.clear();
				for (int i=0; i<solutions.size(); i++)
				{
					if (solutions.get(i)!=null)
					{
						actualsol=solutions.get(i);
						tempval=0.0;
						for (int j=0; j<actualsol.length; j++)
						{
							if (actualsol[j]!=0.0)
							{
								if (recval[j]+actualsol[j]==0.0) tempval=tempval+1;
								else if (recval[j]==0.0) tempval=tempval+0.001;
								else tempval=tempval+Math.abs(actualsol[j]/(recval[j]+actualsol[j]));
							}
						}
						indexsol.add(new Double(tempval));
					}
					else indexsol.add(new Double(-1));
				}
				if (indexsol.size()==2)
				{
					vala=(indexsol.get(0)).doubleValue();
					valb=(indexsol.get(1)).doubleValue();
					if (vala>valb) swap(0, 1);
				}
				else if (indexsol.size()>2) quickSort(0, indexsol.size()-1);
				for (int i=0; i<solutions.size(); i++)
				{
					currentpointer.clear();
					if (solutions.get(i)!=null)
					{
						actualsol=solutions.get(i);
						realnumsol=0;
						for (int j=0; j<actualsol.length; j++)
						{
							if (actualsol[j]!=0.0) realnumsol++;
						}
						double[] soltoconsider=new double[realnumsol];
						String[] nametoconsider=new String[realnumsol];
						realnumsol=0;
						numsolwithdec=0;
						for (int j=0; j<actualsol.length; j++)
						{
							if (actualsol[j]!=0.0)
							{
								currentpointer.add(new Integer(j));
								soltoconsider[realnumsol]=recval[j]+actualsol[j];
								if (soltoconsider[realnumsol]%1!=0) numsolwithdec++;
								tempint=(vectrefvname.get(j)).intValue();
								nametoconsider[realnumsol]=varref[tempint];
								realnumsol++;
							}
						}
						defsolutions.add(soltoconsider);
						defsolutionsvars.add(nametoconsider);
						if (maxcardfordetsol>currentpointer.size())
						{
							if (useonlyintdetsol && numsolwithdec>0)
							{
								detsolutionsvars.add(null);
								numdetsolforsol.add(new Integer(0));
							}
							else checkdeterministic(refe);
						}
						else
						{
							detsolutionsvars.add(null);
							numdetsolforsol.add(new Integer(0));
						}
					}
				}
			}
			es.cleanMem();
		}
		defeq=new double[0][0];
		defiq=new double[0][0];
		defeq=null;
		defiq=null;
		eqdef.clear();
		iqdef.clear();
	}
	/**
	*Finds the solutions using the Polco library for the reduced system
	*/
	private void findsolutionsred(int refe)
	{
		defeq=null;
		defiq=null;
		if (eqdef.size()>0)
		{
			defeq=new double[eqdef.size()][totest.size()*2+1];
			for (int i=0; i<eqdef.size(); i++)
			{
				Vector<Double> elementeqdef=eqdef.get(i);
				for (int j=0; j<elementeqdef.size(); j++)
				{
					defeq[i][j]=(elementeqdef.get(j)).doubleValue();
				}
			}
		}
		if (iqdef.size()>0)
		{
			defiq=new double[iqdef.size()][totest.size()*2+1];
			for (int i=0; i<iqdef.size(); i++)
			{
				Vector<Double> elementiqdef=iqdef.get(i);
				for (int j=0; j<elementiqdef.size(); j++)
				{
					defiq[i][j]=(elementiqdef.get(j)).doubleValue();
				}
			}
		}
		if (!filetoprint.equals("")) printmatricesred();
		currentweights.clear();
		for (int i=0; i<totest.size(); i++)
		{
			tempint=(totest.get(i)).intValue();
			tempval=(valueweights.get(vectrefvname.get(tempint))).doubleValue();
			currentweights.add(new Double(tempval));
		}
		esr.setEditsToSolve(diseq.get(refe), disiq.get(refe), recval, tolerance);
		esr.setzeroalg(zeroalg);
		cardinality=-1;
		try
		{
			esr.getTotalSolutions(currentweights, defeq, defiq, totest);
			cardinality=esr.getCardinality();
			solutions=esr.getSolutions();
			if (solutions.size()==0) cardinality=-1;
		}
		catch (Exception ees)
		{
			esr.cleanMem();
		}
		if (cardinality!=-1)
		{
			onesoldiffzero=false;
			numsoldiffzero=0;
			for (int i=0; i<solutions.size(); i++)
			{
				actualsol=solutions.get(i);
				tempval=0.0;
				for (int j=0; j<actualsol.length; j++)
				{
					if (actualsol[j]!=0.0)
					{
						tempint=(totest.get(j)).intValue();
						tempval=tempval+Math.abs(recval[tempint]+actualsol[j]);
					}
				}
				if (tempval!=0.0) numsoldiffzero++;
			}
			if ((numsoldiffzero>0) && (numsoldiffzero<solutions.size()))
			{
				for (int i=0; i<solutions.size(); i++)
				{
					actualsol=solutions.get(i);
					tempval=0.0;
					for (int j=0; j<actualsol.length; j++)
					{
						if (actualsol[j]!=0.0)
						{
							tempint=(totest.get(j)).intValue();
							tempval=tempval+Math.abs(recval[tempint]+actualsol[j]);
						}
					}
					if (tempval==0.0) solutions.set(i, null);
				}
			}
			indexsol.clear();
			for (int i=0; i<solutions.size(); i++)
			{
				if (solutions.get(i)!=null)
				{
					actualsol=solutions.get(i);
					tempval=0.0;
					for (int j=0; j<actualsol.length; j++)
					{
						if (actualsol[j]!=0.0)
						{
							tempint=(totest.get(j)).intValue();
							if (recval[tempint]+actualsol[j]==0.0) tempval=tempval+1;
							else tempval=tempval+Math.abs(recval[tempint]/(recval[tempint]+actualsol[j]));
						}
					}
					indexsol.add(new Double(tempval));
				}
				else indexsol.add(new Double(-1));
			}
			if (indexsol.size()==2)
			{
				vala=(indexsol.get(0)).doubleValue();
				valb=(indexsol.get(1)).doubleValue();
				if (vala>valb) swap(0, 1);
			}
			else if (indexsol.size()>2) quickSort(0, indexsol.size()-1);
			for (int i=0; i<solutions.size(); i++)
			{
				currentpointer.clear();
				if (solutions.get(i)!=null)
				{
					actualsol=solutions.get(i);
					realnumsol=0;
					for (int j=0; j<actualsol.length; j++)
					{
						if (actualsol[j]!=0.0) realnumsol++;
					}
					double[] soltoconsider=new double[realnumsol];
					String[] nametoconsider=new String[realnumsol];
					realnumsol=0;
					numsolwithdec=0;
					for (int j=0; j<actualsol.length; j++)
					{
						if (actualsol[j]!=0.0)
						{
							tempint=(totest.get(j)).intValue();
							currentpointer.add(new Integer(tempint));
							soltoconsider[realnumsol]=recval[tempint]+actualsol[j];
							if (soltoconsider[realnumsol]%1!=0) numsolwithdec++;
							tempint=(vectrefvname.get(tempint)).intValue();
							nametoconsider[realnumsol]=varref[tempint];
							realnumsol++;
						}
					}
					defsolutions.add(soltoconsider);
					defsolutionsvars.add(nametoconsider);
					if (maxcardfordetsol>currentpointer.size())
					{
						if (useonlyintdetsol && numsolwithdec>0)
						{
							detsolutionsvars.add(null);
							numdetsolforsol.add(new Integer(0));
						}
						else checkdeterministic(refe);
					}
					else
					{
						detsolutionsvars.add(null);
						numdetsolforsol.add(new Integer(0));
					}
				}
			}
		}
		esr.cleanMem();
		defeq=new double[0][0];
		defiq=new double[0][0];
		defeq=null;
		defiq=null;
		eqdef.clear();
		iqdef.clear();
	}
	/**
	*Verify if the solutions are deterministic
	*/
	private void checkdeterministic(int refe)
	{
		checkfd.clear();
		iqfordet.clear();
		eqfordet.clear();

		double[][] tempiq=disiq.get(refe);
		double[][] tempeq=diseq.get(refe);
		if (tempiq!=null)
		{
			for (int i=0; i<tempiq.length; i++)
			{
				Vector<Double> elementiqdef=new Vector<Double>();
				for (int j=0; j<currentpointer.size()+1; j++)
				{
					elementiqdef.add(new Double(0.0));
				}
				tempval=0;
				tempdou=0;
				for (int j=0; j<currentpointer.size(); j++)
				{
					tempint=(currentpointer.get(j)).intValue();
					if (tempiq[i][tempint+1]!=0.0)
					{
						tempval=tempval+recval[tempint]*tempiq[i][tempint+1];
						elementiqdef.set(j+1, new Double(tempiq[i][tempint+1]));
						tempdou=tempdou+Math.abs(tempiq[i][tempint+1]);
					}
				}
				for (int j=1; j<tempiq[0].length; j++)
				{
					if (!currentpointer.contains(new Integer(j-1)))
					{
						tempval=tempval+recval[j-1]*tempiq[i][j];
					}
				}
				tempval=tempiq[i][0]+tempval;
				elementiqdef.set(0, new Double(tempval));
				if (tempdou>0.0)
				{
					if (!iqfordet.contains(elementiqdef)) iqfordet.add(elementiqdef);
				}
			}
		}
		if (tempeq!=null)
		{
			for (int i=0; i<tempeq.length; i++)
			{
				Vector<Double> elementeqdef=new Vector<Double>();
				for (int j=0; j<currentpointer.size()+1; j++)
				{
					elementeqdef.add(new Double(0.0));
				}
				tempval=0;
				tempdou=0;
				for (int j=0; j<currentpointer.size(); j++)
				{
					tempint=(currentpointer.get(j)).intValue();
					if (tempeq[i][tempint+1]!=0.0)
					{
						tempval=tempval+recval[tempint]*tempeq[i][tempint+1];
						elementeqdef.set(j+1, new Double(tempeq[i][tempint+1]));
						tempdou=tempdou+Math.abs(tempeq[i][tempint+1]);
					}
				}
				for (int j=1; j<tempeq[0].length; j++)
				{
					if (!currentpointer.contains(new Integer(j-1)))
					{
						tempval=tempval+recval[j-1]*tempeq[i][j];
					}
				}
				tempval=tempeq[i][0]+tempval;
				elementeqdef.set(0, new Double(tempval));
				if (tempdou>0.0)
				{
					if (!eqfordet.contains(elementeqdef)) eqfordet.add(elementeqdef);
				}
			}
		}
		viq=new double[iqfordet.size()][currentpointer.size()+1];
		for (int i=0; i<iqfordet.size(); i++)
		{
			Vector<Double> tiq=iqfordet.get(i);
			for (int j=0; j<tiq.size(); j++)
			{
				viq[i][j]=(tiq.get(j)).doubleValue();
			}
		}
		veq=null;
		if (eqfordet.size()>0)
		{
			veq=new double[eqfordet.size()][currentpointer.size()+1];
			for (int i=0; i<eqfordet.size(); i++)
			{
				Vector<Double> teq=eqfordet.get(i);
				for (int j=0; j<teq.size(); j++)
				{
					veq[i][j]=(teq.get(j)).doubleValue();
				}
			}
		}
		curtempdet.clear();
		try
		{
			if (!filetoprint.equals("")) printmatricesdet();
			checksol=pa.getDoubleRays(veq, viq);
			if (checksol!=null)
			{
				if (!filetoprint.equals("")) printarray2d(checksol, "Deterministic solutions");
				if (checksol.length>1)
				{
					tempsolfordet=new double[checksol[0].length-1];
					numsoldetdz=0;
					for (int i=0; i<checksol.length; i++)
					{
						if (checksol[i][0]!=0.0)
						{
							numsoldetdz++;
							if (numsoldetdz==1)
							{
								for (int j=1; j<checksol[0].length; j++)
								{
									tempsolfordet[j-1]=checksol[i][j]/checksol[i][0];
								}
							}
						}
					}
					if (numsoldetdz>1)
					{
						for (int i=1; i<checksol.length; i++)
						{
							if (checksol[i][0]!=0.0)
							{
								for (int j=1; j<checksol[0].length; j++)
								{
									valb=checksol[i][j]/checksol[i][0];
									if (!Double.isNaN(tempsolfordet[j-1]) && tempsolfordet[j-1]!=valb) tempsolfordet[j-1]=Double.NaN;
								}
							}
						}
						for (int j=0; j<checksol[0].length-1; j++)
						{
							if (!Double.isNaN(tempsolfordet[j]))
							{
								tempint=(currentpointer.get(j)).intValue();
								tempint=(vectrefvname.get(tempint)).intValue();
								if (!curtempdet.contains(varref[tempint])) curtempdet.add(varref[tempint]);
							}
						}
					}
					else
					{
						String[] nametoconsider=new String[currentpointer.size()];
						for (int j=0; j<currentpointer.size(); j++)
						{
							tempint=(currentpointer.get(j)).intValue();
							tempint=(vectrefvname.get(tempint)).intValue();
							nametoconsider[j]=varref[tempint];
						}
						detsolutionsvars.add(nametoconsider);
						numdetsolforsol.add(new Integer(nametoconsider.length));
						tempiq=new double[0][0];
						tempeq=new double[0][0];
						checksol=new double[0][0];
						pa=null;
						pa=new PolcoAdapter(opopa);
						iqfordet.clear();
						eqfordet.clear();
						tempiq=new double[0][0];
						tempeq=new double[0][0];
						tempiq=null;
						tempeq=null;
						viq=new double[0][0];
						veq=new double[0][0];
						viq=null;
						veq=null;
						return;
					}
				}
				else
				{
					String[] nametoconsider=new String[currentpointer.size()];
					for (int j=0; j<currentpointer.size(); j++)
					{
						tempint=(currentpointer.get(j)).intValue();
						tempint=(vectrefvname.get(tempint)).intValue();
						nametoconsider[j]=varref[tempint];
					}
					detsolutionsvars.add(nametoconsider);
					numdetsolforsol.add(new Integer(nametoconsider.length));
					tempiq=new double[0][0];
					tempeq=new double[0][0];
					checksol=new double[0][0];
					pa=null;
					pa=new PolcoAdapter(opopa);
					iqfordet.clear();
					eqfordet.clear();
					tempiq=new double[0][0];
					tempeq=new double[0][0];
					tempiq=null;
					tempeq=null;
					viq=new double[0][0];
					veq=new double[0][0];
					viq=null;
					veq=null;
					return;
				}
			}
			checksol=new double[0][0];
			pa=null;
			pa=new PolcoAdapter(opopa);
		}
		catch (Exception epa) {}
		if (curtempdet.size()==0)
		{
			detsolutionsvars.add(null);
			numdetsolforsol.add(new Integer(0));
		}
		else
		{
			String[] nametoconsider=new String[curtempdet.size()];
			for (int i=0; i<curtempdet.size(); i++)
			{
				nametoconsider[i]=curtempdet.get(i);
			}
			detsolutionsvars.add(nametoconsider);
			numdetsolforsol.add(new Integer(nametoconsider.length));
		}
		iqfordet.clear();
		eqfordet.clear();
		tempiq=new double[0][0];
		tempeq=new double[0][0];
		tempiq=null;
		tempeq=null;
		viq=new double[0][0];
		veq=new double[0][0];
		viq=null;
		veq=null;
	}
	/**
	*If called the method maximizes the solutions by considering those that have associated the maximum number of deterministic solutions
	*/
	public void maximizeDet()
	{
		maxdefsolutionsvars.clear();
		maxdetsolutionsvars.clear();
		maxdefsolutions.clear();
		maxdetsol=0;
		for (int i=0; i<numdetsolforsol.size(); i++)
		{
			tempint=(numdetsolforsol.get(i)).intValue();
			if (tempint>maxdetsol) maxdetsol=tempint;
		}
		diffdetsol=false;
		if (maxdetsol>0)
		{
			for (int i=0; i<numdetsolforsol.size(); i++)
			{
				tempint=(numdetsolforsol.get(i)).intValue();
				if (tempint!=maxdetsol)
				{
					diffdetsol=true;
					break;
				}
			}
		}
		if (diffdetsol)
		{
			for (int i=0; i<numdetsolforsol.size(); i++)
			{
				tempint=(numdetsolforsol.get(i)).intValue();
				if (tempint==maxdetsol)
				{
					String[] temparray1=defsolutionsvars.get(i);
					String[] defarray1=new String[temparray1.length];
					for (int j=0; j<temparray1.length; j++)
					{
						defarray1[j]=temparray1[j];
					}
					maxdefsolutionsvars.add(defarray1);
					String[] temparray2=detsolutionsvars.get(i);
					String[] defarray2=new String[temparray2.length];
					for (int j=0; j<temparray2.length; j++)
					{
						defarray2[j]=temparray2[j];
					}
					maxdetsolutionsvars.add(defarray2);
					double[] temparray3=defsolutions.get(i);
					double[] defarray3=new double[temparray3.length];
					for (int j=0; j<temparray3.length; j++)
					{
						defarray3[j]=temparray3[j];
					}
					maxdefsolutions.add(defarray3);
				}
			}
			defsolutionsvars.clear();
			detsolutionsvars.clear();
			defsolutions.clear();
			for (int i=0; i<maxdefsolutions.size(); i++)
			{
				String[] temparray1=maxdefsolutionsvars.get(i);
				String[] defarray1=new String[temparray1.length];
				for (int j=0; j<temparray1.length; j++)
				{
					defarray1[j]=temparray1[j];
				}
				defsolutionsvars.add(defarray1);
				String[] temparray2=maxdetsolutionsvars.get(i);
				String[] defarray2=new String[temparray2.length];
				for (int j=0; j<temparray2.length; j++)
				{
					defarray2[j]=temparray2[j];
				}
				detsolutionsvars.add(defarray2);
				double[] temparray3=maxdefsolutions.get(i);
				double[] defarray3=new double[temparray3.length];
				for (int j=0; j<temparray3.length; j++)
				{
					defarray3[j]=temparray3[j];
				}
				defsolutions.add(defarray3);
			}
			maxdefsolutionsvars.clear();
			maxdetsolutionsvars.clear();
			maxdefsolutions.clear();
		}
	}
	/**
	*Returns the current cardinality
	*/
	public double getcardinality()
	{
		return cardinality;
	}
	/**
	*Returns the vector of solutions in terms of new values
	*/
	public Vector<double[]> getSolutions()
	{
		return defsolutions;
	}
	/**
	*Returns the vector of solutions in terms of variable names
	*/
	public Vector<String[]> getSolutionsNames()
	{
		return defsolutionsvars;
	}
	/**
	*Returns the vector of deterministic solutions in terms of variable names
	*/
	public Vector<String[]> getDeterministicSolutions()
	{
		return detsolutionsvars;
	}
	/**
	*Print on a file the matrices
	*/
	public void printmatrices()
	{
		try
		{
    		BufferedWriter outmat = new BufferedWriter(new FileWriter(filetoprint, true));
    		String tempvname="";
    		for (int i=0; i<vectrefvname.size(); i++)
    		{
				tempint=(vectrefvname.get(i)).intValue();
				tempvname=tempvname+varref[tempint];
				if (i<(vectrefvname.size()-1)) tempvname=tempvname+"\t";
			}
			outmat.write("Variables "+tempvname+"\n");
			if (totest.size()>0)
			{
				tempvname="";
				for (int i=0; i<totest.size(); i++)
				{
					tempint=(totest.get(i)).intValue();
					tempint=(vectrefvname.get(tempint)).intValue();
					tempvname=tempvname+varref[tempint];
					if (i<(totest.size()-1)) tempvname=tempvname+"\t";
				}
				outmat.write("Variables actually tested "+tempvname+"\n");
			}
    		tempvname="";
    		for (int i=0; i<currentweights.size(); i++)
    		{
				tempdou=(currentweights.get(i)).doubleValue();
				tempvname=tempvname+String.valueOf(tempdou);
				if (i<(currentweights.size()-1)) tempvname=tempvname+"\t";
			}
			outmat.write("Weights "+tempvname+"\n");
			tempvname="";
			if (defeq!=null)
			{
			    outmat.write("EQ\n");
				for (int i=0; i<defeq.length; i++)
				{
					tempvname="";
					for (int j=0; j<defeq[0].length; j++)
					{
						tempvname=tempvname+String.valueOf(defeq[i][j]);
						if (j<(defeq[0].length-1))
							tempvname=tempvname+"\t";
					}
					tempvname=tempvname.trim()+"\n";
					outmat.write(tempvname);
				}
			}
		    outmat.write("IQ\n");
			for (int i=0; i<defiq.length; i++)
			{
				tempvname="";
				for (int j=0; j<defiq[0].length; j++)
				{
					tempvname=tempvname+String.valueOf(defiq[i][j]);
					if (j<(defiq[0].length-1))
						tempvname=tempvname+"\t";
				}
				tempvname=tempvname.trim()+"\n";
				outmat.write(tempvname);
			}
		    outmat.close();
		} catch (Exception exsc) {}
	}
	/**
	*Print on a file the matrices
	*/
	public void printmatricesred()
	{
		try
		{
    		BufferedWriter outmat = new BufferedWriter(new FileWriter(filetoprint, true));
    		String tempvname="";
    		for (int i=0; i<vectrefvname.size(); i++)
    		{
				tempint=(vectrefvname.get(i)).intValue();
				tempvname=tempvname+varref[tempint];
				if (i<(vectrefvname.size()-1)) tempvname=tempvname+"\t";
			}
			outmat.write("Variables "+tempvname+"\n");
			if (totest.size()>0)
			{
				tempvname="";
				for (int i=0; i<totest.size(); i++)
				{
					tempint=(totest.get(i)).intValue();
					tempint=(vectrefvname.get(tempint)).intValue();
					tempvname=tempvname+varref[tempint];
					if (i<(totest.size()-1)) tempvname=tempvname+"\t";
				}
				outmat.write("Variables actually tested "+tempvname+"\n");
			}
    		tempvname="";
    		for (int i=0; i<currentweights.size(); i++)
    		{
				tempdou=(currentweights.get(i)).doubleValue();
				tempvname=tempvname+String.valueOf(tempdou);
				if (i<(currentweights.size()-1)) tempvname=tempvname+"\t";
			}
			outmat.write("Weights "+tempvname+"\n");
			tempvname="";
			if (defeq!=null)
			{
			    outmat.write("EQ\n");
				for (int i=0; i<defeq.length; i++)
				{
					tempvname="";
					for (int j=0; j<defeq[0].length; j++)
					{
						tempvname=tempvname+String.valueOf(defeq[i][j]);
						if (j<(defeq[0].length-1))
							tempvname=tempvname+"\t";
					}
					tempvname=tempvname.trim()+"\n";
					outmat.write(tempvname);
				}
			}
		    outmat.write("IQ\n");
			for (int i=0; i<defiq.length; i++)
			{
				tempvname="";
				for (int j=0; j<defiq[0].length; j++)
				{
					tempvname=tempvname+String.valueOf(defiq[i][j]);
					if (j<(defiq[0].length-1))
						tempvname=tempvname+"\t";
				}
				tempvname=tempvname.trim()+"\n";
				outmat.write(tempvname);
			}
		    outmat.close();
		} catch (Exception exsc) {exsc.printStackTrace();}
	}
	/**
	*Print on a file an array
	*/
	public void printarray(double[] arraytoprint, String titlearray)
	{
		try
		{
    		BufferedWriter outmat = new BufferedWriter(new FileWriter(filetoprint, true));
			outmat.write(titlearray+"\n");
    		String tempvname="";
    		for (int i=0; i<arraytoprint.length; i++)
    		{
				tempvname=tempvname+String.valueOf(arraytoprint[i]);
				if (i<arraytoprint.length-1) tempvname=tempvname+"\t";
			}
			outmat.write(tempvname+"\n");
		    outmat.close();
		} catch (Exception exsc) {exsc.printStackTrace();}
	}
	/**
	*Print on a file an array
	*/
	public void printarray2d(double[][] arraytoprint, String titlearray)
	{
		try
		{
    		BufferedWriter outmat = new BufferedWriter(new FileWriter(filetoprint, true));
			outmat.write(titlearray+"\n");
    		for (int i=0; i<arraytoprint.length; i++)
    		{
	    		String tempvname="";
	    		for (int j=0; j<arraytoprint[0].length; j++)
	    		{
					tempvname=tempvname+String.valueOf(arraytoprint[i][j]);
					if (j<arraytoprint[0].length-1) tempvname=tempvname+"\t";
				}
				outmat.write(tempvname+"\n");
			}
			outmat.write("\n");
		    outmat.close();
		} catch (Exception exsc) {exsc.printStackTrace();}
	}
	/**
	*Print on a file the matrices
	*/
	public void printmatricesdet()
	{
		try
		{
    		BufferedWriter outmat = new BufferedWriter(new FileWriter(filetoprint, true));
    		String tempvname="";
    		for (int i=0; i<vectrefvname.size(); i++)
    		{
				tempint=(vectrefvname.get(i)).intValue();
				tempvname=tempvname+varref[tempint];
				if (i<(vectrefvname.size()-1)) tempvname=tempvname+"\t";
			}
			outmat.write("Deterministic matrices\n");
			if (veq!=null)
			{
			    outmat.write("EQ\n");
				for (int i=0; i<veq.length; i++)
				{
					tempvname="";
					for (int j=0; j<veq[0].length; j++)
					{
						tempvname=tempvname+String.valueOf(veq[i][j]);
						if (j<(veq[0].length-1))
							tempvname=tempvname+"\t";
					}
					tempvname=tempvname.trim()+"\n";
					outmat.write(tempvname);
				}
			}
		    outmat.write("IQ\n");
			for (int i=0; i<viq.length; i++)
			{
				tempvname="";
				for (int j=0; j<viq[0].length; j++)
				{
					tempvname=tempvname+String.valueOf(viq[i][j]);
					if (j<(viq[0].length-1))
						tempvname=tempvname+"\t";
				}
				tempvname=tempvname.trim()+"\n";
				outmat.write(tempvname);
			}
		    outmat.close();
		} catch (Exception exsc) {exsc.printStackTrace();}
	}
	/**
	*Used to sort the solutions
	*/
	private void swap(int index1, int index2)
	{
		vala=(indexsol.get(index1)).doubleValue();
		valb=(indexsol.get(index2)).doubleValue();
		indexsol.set(index1, new Double(valb));
		indexsol.set(index2, new Double(vala));
		double[] tempa=null;
		double[] tempb=null;
		if (solutions.get(index1)!=null)
		{
			tempdval=solutions.get(index1);
			tempa=new double[tempdval.length];
			for (int i=0; i<tempa.length; i++)
			{
				tempa[i]=tempdval[i];
			}
		}
		if (solutions.get(index2)!=null)
		{
			tempdval=solutions.get(index2);
			tempb=new double[tempdval.length];
			for (int i=0; i<tempb.length; i++)
			{
				tempb[i]=tempdval[i];
			}
		}
		solutions.set(index1, tempb);
		solutions.set(index2, tempa);
	}
	/**
	*Executes the quick sort for the solutions
	*/
	private void quickSort(int start, int end)
	{
		int i = start;
		int j = end;
		double center=(indexsol.get((start+end)/2)).doubleValue();
		do
		{
			while( (i < end) && (center> (indexsol.get(i)).doubleValue()))
				i++;
			while( (j > start) && (center< (indexsol.get(j)).doubleValue()))
				j--;
			if (i < j) swap(i, j);
			if (i <= j)
			{
				i++;
				j--;
			}
		}
		while(i <= j);
		if (start < j) quickSort(start, j);
		if (i < end) quickSort(i, end);
	}
}
