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

import ADaMSoft.algorithms.clusters.EvaluateDistance;
import ADaMSoft.dataaccess.GroupedMatrix2Dfile;
import ADaMSoft.dataaccess.Matrix2DFile;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
* This method implements the clustering alghoritms based on the fuzzy k-means
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class FuzzyKMeansEvaluator
{
	double fuzzyness;
	int nseeds;
	double accuracy;
	VarGroupModalities vgm;
	Hashtable<Vector<String>, Vector<double[]>> seeds;
	Vector<Vector<String>> groupref;
	String tempdir;
	int nvar;
	String message;
	double[] possibilities;
	Hashtable<Vector<String>, double[]> stats;
	Hashtable<Vector<String>, Double> num;
	int vdt;
	boolean ismahalanobis;
	Hashtable<Vector<String>, String> lasterror;
	Hashtable<Vector<String>, String> erroronevaluate;
	EvaluateDistance ed;
	/**
	*Initialise the main Objects, that will contains the seeds and the other values used to update these
	*/
	public FuzzyKMeansEvaluator (int nseeds, double fuzzyness, double accuracy, String tempdir, int nvar, int vdt)
	{
		erroronevaluate=new Hashtable<Vector<String>, String>();
		lasterror=new Hashtable<Vector<String>, String>();
		ismahalanobis=false;
		this.nseeds=nseeds;
		this.fuzzyness=fuzzyness;
		this.accuracy=accuracy;
		this.tempdir=tempdir;
		this.nvar=nvar;
		this.vdt=vdt;
		ed=new EvaluateDistance(vdt);
		message="";
		possibilities=new double[nseeds];
	}
	public void setmahalanobis(Hashtable<Vector<String>, double[][]> vweights)
	{
		ed.setweights(vweights);
		ismahalanobis=true;
	}
	/**
	*Used to receive the range of possible values in order to initialize the seeds
	*/
	public boolean initialise(VarGroupModalities vgm)
	{
		this.vgm=vgm;
		seeds=new Hashtable<Vector<String>, Vector<double[]>>();
		int numgroup=vgm.getTotal();
		if (numgroup==0)
			numgroup=1;
		groupref=vgm.getfinalmodalities();
		for (int g=0; g<numgroup; g++)
		{
			Vector<String> tempgroup=vgm.getvectormodalities(g);
			Vector<double[]> tempseeds=new Vector<double[]>();
			for (int i=0; i<nseeds; i++)
			{
				double[] tempval=new double[nvar];
				for (int j=0; j<nvar; j++)
				{
					tempval[j]=0;
				}
				tempseeds.add(tempval);
			}
			seeds.put(tempgroup, tempseeds);
		}
		return true;
	}
	/**
	*Return the message if an error occour
	*/
	public String getmessage()
	{
		return message;
	}
	/**
	*Used to receive the seeds from the external
	*/
	public boolean setseeds (Hashtable<Vector<String>, Vector<double[]>> newseeds)
	{
		seeds=new Hashtable<Vector<String>, Vector<double[]>>();
		stats=new Hashtable<Vector<String>, double[]>();
		num=new Hashtable<Vector<String>, Double>();
		for (Enumeration<Vector<String>> e = newseeds.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> groupvalues=e.nextElement();
			Vector<double[]> seedval=newseeds.get(groupvalues);
			Vector<double[]> tempseeds=new Vector<double[]>();
			for (int i=0; i<seedval.size(); i++)
			{
				double[] temp=seedval.get(i);
				double[] tempval=new double[temp.length];
				for (int j=0; j<temp.length; j++)
				{
					tempval[j]=temp[j];
				}
				tempseeds.add(tempval);
			}
			seeds.put(groupvalues, tempseeds);
		}
		return true;
	}
	/**
	*Evaluate the new seeds
	*/
	public boolean evaluate(int niter, GroupedMatrix2Dfile filedata)
	{
		boolean isok=true;
		for (int g=0; g<groupref.size(); g++)
		{
			Vector<String> groupvalues=groupref.get(g);
			erroronevaluate.put(groupvalues, "");
			int nobs=filedata.getRows(groupvalues);
			int maxnumseeds=nseeds;
			Vector<double[]> tempseeds=seeds.get(groupvalues);
			if (nobs<nseeds)
			{
				maxnumseeds=nobs;
				for(int i = maxnumseeds; i <nseeds; i++)
				{
					double[] tts=new double[nvar];
					for(int j = 0; j <nvar; j++)
					{
						tts[j]=Double.NaN;
					}
					tempseeds.set(i, tts);
				}
				lasterror.put(groupvalues, "0");
				erroronevaluate.put(groupvalues, "%1914%");
			}
			if (nobs==1)
			{
				maxnumseeds=nobs;
				for(int i = maxnumseeds; i <nseeds; i++)
				{
					double[] tts=new double[nvar];
					for(int j = 0; j <nvar; j++)
					{
						tts[j]=Double.NaN;
					}
					tempseeds.set(i, tts);
				}
				double[] tts=new double[nvar];
				for(int j = 0; j <nvar; j++)
				{
					tts[j]=filedata.read(groupvalues, 0, j);
				}
				tempseeds.set(0, tts);
				lasterror.put(groupvalues, "0");
				erroronevaluate.put(groupvalues, "%1915%");
			}
			else
			{
				Matrix2DFile u=new Matrix2DFile(tempdir, maxnumseeds);
				for (int i=0; i<nobs; i++)
				{
					double[] tempu=new double[maxnumseeds];
					double tempsumu=0;
					for (int j=0; j<maxnumseeds; j++)
					{
						tempu[j]=Math.random();
						tempsumu+=tempu[j];
					}
					for (int j=0; j<maxnumseeds; j++)
					{
						tempu[j]=tempu[j]/tempsumu;
					}
					if (!u.write(tempu))
					{
						message=u.getMessage();
						isok=false;
						g=groupref.size();
						i=nobs;
						break;
					}
				}

				for(int i = 0; i <maxnumseeds; i++)
				{
					for(int j = 0; j <nvar; j++)
					{
						double numt=0.0;
						double den=0.0;
						for(int m = 0; m <nobs; m++)
						{
							double tv=Math.pow(u.read(m, i), fuzzyness);
							numt+=filedata.read(groupvalues, m, j)*tv;
							den+=tv;
						}
						double[] tts=tempseeds.get(i);
						tts[j]=numt/den;
					}
				}

				double paramfuzzy=2.0/(fuzzyness-1.0);
				double actualerror=0;

				boolean erroroniter=false;

				for(int f = 0; f <= niter; f++)
				{
					actualerror=0;
					for(int j = 0; j<nobs; j++)
					{
						double[] sumu = new double[maxnumseeds];
						for(int i=0; i<maxnumseeds; i++)
						{
							double[] valvalue=new double[nvar];
							double[] tts=tempseeds.get(i);
							for(int l=0; l<nvar; l++)
							{
								valvalue[l]=filedata.read(groupvalues, j, l);
							}
							if (ismahalanobis)
								ed.setGroup(groupvalues);
							sumu[i]=ed.getdistance(tts, valvalue);
						}
						double tempdist=0;
						for(int i = 0; i<maxnumseeds; i++)
						{
							double tempden=0;
							for (int l=0; l<maxnumseeds; l++)
							{
								tempden+=Math.pow(sumu[i]/sumu[l], paramfuzzy);
							}
							tempdist+=Math.pow((u.read(j, i)-1/tempden),2);
							u.write(1/tempden, j, i);
						}
						actualerror+=Math.sqrt(tempdist/maxnumseeds);
					}
					actualerror=actualerror/nobs;
					//Here the new seeds are evaluated
					for(int i = 0; i <maxnumseeds; i++)
					{
						double[] tts=tempseeds.get(i);
						for(int j = 0; j <nvar; j++)
						{
							double numt=0.0;
							double den=0.0;
							for(int m = 0; m <nobs; m++)
							{
								double tv=Math.pow(u.read(m, i), fuzzyness);
								numt+=filedata.read(groupvalues, m, j)*tv;
								den+=tv;
							}
							tts[j]=numt/den;
						}
					}
					if (actualerror<accuracy)
						break;
					if (Double.isNaN(actualerror))
					{
						erroroniter=true;
						break;
					}
				}
				if (!erroroniter)
					erroronevaluate.put(groupvalues, "");
				else
					erroronevaluate.put(groupvalues, "%1916%");
				lasterror.put(groupvalues, String.valueOf(actualerror));
				u.close();
			}
		}
		return isok;
	}
	/**
	*Evaluate the possibilities, for each observation, of belonging to a seed
	*/
	public boolean possibility(Vector<String> groupvalues, double[] values)
	{
		Vector<double[]> tempseed=seeds.get(groupvalues);
		double paramfuzzy=2.0/(fuzzyness-1.0);
		int nobs=tempseed.size();
		if (nobs==0)
		{
			message="%942%<br>\n";
			return false;
		}
		int maxnumseeds=nseeds;
		if (nobs<nseeds)
			maxnumseeds=nobs;
		for (int i=0; i<nseeds; i++)
		{
			possibilities[i]=0;
		}
		double[] sumu = new double[maxnumseeds];
		for(int i=0; i<maxnumseeds; i++)
		{
			double[] tts=tempseed.get(i);
			if (ismahalanobis)
				ed.setGroup(groupvalues);
			sumu[i]=ed.getdistance(tts, values);
		}
		boolean nodom=false;
		for(int i = 0; i<maxnumseeds; i++)
		{
			if (sumu[i]==0)
				nodom=true;
		}
		if (!nodom)
		{
			for(int i = 0; i<maxnumseeds; i++)
			{
				double tempden=0;
				for (int l=0; l<maxnumseeds; l++)
				{
					tempden+=Math.pow(sumu[i]/sumu[l], paramfuzzy);
				}
				possibilities[i]=1/tempden;
			}
		}
		else
		{
			for(int i = 0; i<maxnumseeds; i++)
			{
				if (sumu[i]==0)
					possibilities[i]=1;
			}
		}
		return true;
	}
	/**
	*Evaluate the statistics for the current seeds
	*/
	public boolean addstat(Vector<String> groupvalues, double[] values)
	{
		Vector<double[]> tempseed=seeds.get(groupvalues);
		double paramfuzzy=2.0/(fuzzyness-1.0);
		double[] tempstat=stats.get(groupvalues);
		double tempnum=0;
		if (tempstat==null)
		{
			tempstat=new double[4];
			tempstat[0]=0;
			tempstat[1]=0;
			tempstat[2]=0;
			tempstat[3]=0;
		}
		else
			tempnum=(num.get(groupvalues)).doubleValue();
		int nobs=tempseed.size();
		if (nobs==0)
		{
			message="%942%\n";
			return false;
		}
		int maxnumseeds=nseeds;
		if (nobs<nseeds)
			maxnumseeds=nobs;
		for (int i=0; i<nseeds; i++)
		{
			possibilities[i]=0;
		}
		double[] sumu = new double[maxnumseeds];
		for(int i=0; i<maxnumseeds; i++)
		{
			double[] tts=tempseed.get(i);
			if (ismahalanobis)
				ed.setGroup(groupvalues);
			sumu[i]=ed.getdistance(tts, values);
		}
		boolean nodom=false;
		for(int i = 0; i<maxnumseeds; i++)
		{
			if (sumu[i]==0)
				nodom=true;
		}
		if (!nodom)
		{
			for(int i = 0; i<maxnumseeds; i++)
			{
				double tempden=0;
				for (int l=0; l<maxnumseeds; l++)
				{
					tempden+=Math.pow(sumu[i]/sumu[l], paramfuzzy);
				}
				possibilities[i]=1/tempden;
			}
			double maxuk=0;
			for(int i = 0; i<maxnumseeds; i++)
			{
				tempstat[0]=tempstat[0]+Math.pow(possibilities[i],2);
				tempstat[1]=tempstat[1]+Math.pow(possibilities[i],2);
				tempstat[2]=tempstat[2]+possibilities[i]*Math.log(possibilities[i]);
				if (possibilities[i]>maxuk)
					maxuk=possibilities[i];
			}
			tempstat[3]=tempstat[3]+maxuk;
		}
		else
		{
			tempstat[0]=tempstat[0]+1;
			tempstat[1]=tempstat[1]+1;
			tempstat[2]=tempstat[2]+0;
			tempstat[3]=tempstat[3]+1;
		}
		tempnum=tempnum+1;
		num.put(groupvalues, new Double(tempnum));
		stats.put(groupvalues, tempstat);
		return true;
	}
	/**
	*Returns several statistics for the groups
	*/
	public Hashtable<Vector<String>, double[]> getstat()
	{
		Hashtable<Vector<String>, double[]> finalstat=new Hashtable<Vector<String>, double[]>();
		for (Enumeration<Vector<String>> e = stats.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv=e.nextElement();
			double[] tempstat=stats.get(gv);
			double tempnum=(num.get(gv)).doubleValue();
			tempstat[0]=tempstat[0]/tempnum;
			tempstat[1]=(nseeds*tempstat[1]/tempnum-1)/(nseeds-1);
			tempstat[2]=-1*tempstat[2]/tempnum;
			tempstat[3]=tempstat[3]/tempnum;
			finalstat.put(gv, tempstat);
		}
		return finalstat;
	}
	/**
	*Returns the number of the current grouping variables
	*/
	public int getgroupnum()
	{
		return groupref.size();
	}
	/**
	*Returns the values of the i-th grouping variable
	*/
	public Vector<String> getgroupval(int i)
	{
		return groupref.get(i);
	}
	/**
	*Returns the possibility of belonging to each seed
	*/
	public double[] getpossibilities()
	{
		return possibilities;
	}
	/**
	*Returns the new seeds
	*/
	public Vector<double[]> getseeds(Vector<String> groupvalues)
	{
		Vector<double[]> tempseed=seeds.get(groupvalues);
		Vector<double[]> newseeds=new Vector<double[]>();
		for (int i=0; i<nseeds; i++)
		{
			double[] coord=tempseed.get(i);
			int seedok=0;
			for (int j=0; j<coord.length; j++)
			{
				if (!Double.isNaN(coord[j]))
					seedok++;
			}
			if (seedok!=0)
				newseeds.add(coord);
		}
		return newseeds;
	}
	/**
	*Ends the algorithm
	*/
	public void endfuzzykmeans()
	{
		seeds=null;
		System.gc();
	}
	public Hashtable<Vector<String>, String> getlasterror()
	{
		return lasterror;
	}
	public Hashtable<Vector<String>, String> geterroronevaluate()
	{
		return erroronevaluate;
	}
}
