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

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import ADaMSoft.algorithms.clusters.EvaluateDistance;

/**
* This method implements the clustering alghoritms based on k-means
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class KMeansEvaluator
{
	Hashtable<Vector<String>, Vector<double[]>> seeds;
	Hashtable<Vector<String>, Vector<Double>> numforseed;
	Hashtable<Vector<String>, Vector<double[]>> sumforseed;
	Hashtable<Vector<String>, Vector<Double>> distance;
	int nseeds;
	double mindist;
	int minseed;
	boolean converged;
	EvaluateDistance ed;
	int actualiter=0;
	boolean ismahalanobis;
	double minunit;
	/**
	*Initialise the main Objects, that will contains the seeds and the other values used to update these
	*/
	public KMeansEvaluator (int nseeds, int vdt)
	{
		minunit=0.0;
		ismahalanobis=false;
		this.nseeds=nseeds;
		ed=new EvaluateDistance(vdt);
		seeds=new Hashtable<Vector<String>, Vector<double[]>>();
		numforseed=new Hashtable<Vector<String>, Vector<Double>>();
		sumforseed=new Hashtable<Vector<String>, Vector<double[]>>();
		distance=new Hashtable<Vector<String>, Vector<Double>>();
		converged=false;
	}
	/**
	*Receives the minimum unit for each group
	*/
	public void setminimum(double minunit)
	{
		this.minunit=minunit;
	}
	/**
	*Initialise the main Objects, that will contains the seeds and the other values used to update these, by receiving the seed values
	*/
	public KMeansEvaluator (Hashtable<Vector<String>, Vector<double[]>> seeds, int vdt)
	{
		ismahalanobis=false;
		this.seeds=seeds;
		ed=new EvaluateDistance(vdt);
		numforseed=new Hashtable<Vector<String>, Vector<Double>>();
		sumforseed=new Hashtable<Vector<String>, Vector<double[]>>();
		distance=new Hashtable<Vector<String>, Vector<Double>>();
		nseeds=0;
		for (Enumeration<Vector<String>> e = seeds.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv=e.nextElement();
			Vector<double[]> tempseed=seeds.get(gv);
			if (tempseed.size()>nseeds)
				this.nseeds=tempseed.size();
			Vector<Double> tempnum=new Vector<Double>();
			Vector<double[]> tempsum=new Vector<double[]>();
			Vector<Double> tempdist=new Vector<Double>();
			for (int i=0; i<tempseed.size(); i++)
			{
				double[] actualseed=tempseed.get(i);
				double[] actualsum=new double[actualseed.length];
				for (int j=0; j<actualsum.length; j++)
				{
					actualsum[j]=0;
				}
				tempsum.add(actualsum);
				tempnum.add(new Double(0));
				tempdist.add(new Double(0));
			}
			sumforseed.put(gv, tempsum);
			numforseed.put(gv, tempnum);
			distance.put(gv, tempdist);
		}
		converged=false;
	}
	public void setmahalanobis(Hashtable<Vector<String>, double[][]> vweights)
	{
		ed.setweights(vweights);
		ismahalanobis=true;
	}
	/**
	* Evaluate the group to which the vector of val belongs and update the information in order to update the seeds
	*/
	public void evaluateandupdate(Vector<String> groupval, double[] val)
	{
		Vector<double[]> currentseeds=seeds.get(groupval);
		Vector<Double> currentnumforseed=numforseed.get(groupval);
		Vector<double[]> currentsumforseed=sumforseed.get(groupval);
		Vector<Double> currentdistance=distance.get(groupval);
		if (currentseeds==null)
		{
			currentseeds=new Vector<double[]>();
			currentnumforseed=new Vector<Double>();
			currentsumforseed=new Vector<double[]>();
			currentdistance=new Vector<Double>();
			double[] tempseed=new double[val.length];
			double[] tempsumseed=new double[val.length];
			for (int j=0; j<val.length; j++)
			{
				tempseed[j]=val[j];
				tempsumseed[j]=val[j];
			}
			currentseeds.add(tempseed);
			currentsumforseed.add(tempsumseed);
			currentnumforseed.add(new Double(1));
			currentdistance.add(new Double(0));

			seeds.put(groupval,currentseeds);
			numforseed.put(groupval,currentnumforseed);
			sumforseed.put(groupval,currentsumforseed);
			distance.put(groupval, currentdistance);

		}
		else if ((currentseeds.size()<nseeds) && (actualiter==0))
		{
			double[] tempseed=new double[val.length];
			double[] tempsumseed=new double[val.length];
			for (int j=0; j<val.length; j++)
			{
				tempseed[j]=val[j];
				tempsumseed[j]=val[j];
			}
			currentseeds.add(tempseed);
			currentsumforseed.add(tempsumseed);
			currentnumforseed.add(new Double(1));
			currentdistance.add(new Double(0));
			seeds.put(groupval,currentseeds);
			numforseed.put(groupval,currentnumforseed);
			sumforseed.put(groupval,currentsumforseed);
			distance.put(groupval, currentdistance);
		}
		else
		{
			mindist=Double.MAX_VALUE;
			minseed=0;
			for (int i=0; i<currentseeds.size(); i++)
			{
				if (currentseeds.get(i)!=null)
				{
					double[] seedvalues=currentseeds.get(i);
					double actualdist=0;
					boolean isvalid=true;
					for (int j=0; j<val.length; j++)
					{
						if (Double.isNaN(val[j]))
							isvalid=false;
					}
					if (isvalid)
					{
						if (ismahalanobis)
							ed.setGroup(groupval);
						actualdist=ed.getdistance(val, seedvalues);
						if (actualdist<mindist)
						{
							mindist=actualdist;
							minseed=i;
						}
					}
				}
			}
			double[] sumofseed=currentsumforseed.get(minseed);
			double curdist=(currentdistance.get(minseed)).doubleValue();
			double numofseed=(currentnumforseed.get(minseed)).doubleValue();
			for (int i=0; i<val.length; i++)
			{
				sumofseed[i]=sumofseed[i]+val[i];
			}
			numofseed=numofseed+1;
			curdist=curdist+mindist;
			currentsumforseed.set(minseed, sumofseed);
			currentnumforseed.set(minseed, new Double(numofseed));
			currentdistance.set(minseed, new Double(curdist));
			distance.put(groupval, currentdistance);
			numforseed.put(groupval,currentnumforseed);
			sumforseed.put(groupval,currentsumforseed);
		}
	}
	/**
	* Only evaluate the group to which the vector of val belongs. Returns false if the groupval is not present between those encountered
	*/
	public boolean calculate(Vector<String> groupval, double[] val)
	{
		Vector<double[]> currentseeds=seeds.get(groupval);
		if (currentseeds==null)
			return false;
		mindist=Double.MAX_VALUE;
		minseed=0;
		for (int i=0; i<currentseeds.size(); i++)
		{
			if (currentseeds.get(i)!=null)
			{
				double[] seedvalues=currentseeds.get(i);
				double actualdist=0;
				boolean isvalid=true;
				for (int j=0; j<val.length; j++)
				{
					if (Double.isNaN(val[j]))
						isvalid=false;
				}
				if (isvalid)
				{
					if (ismahalanobis)
						ed.setGroup(groupval);
					actualdist=ed.getdistance(val, seedvalues);
					if (actualdist<mindist)
					{
						mindist=actualdist;
						minseed=i;
					}
				}
			}
		}
		return true;
	}
	/**
	*Returns the group to which the record belongs
	*/
	public int getgroup()
	{
		return minseed;
	}
	/**
	*Returns the sum of squares of distances for each cluster
	*/
	public Hashtable<Vector<String>, Vector<Double>> getdistance()
	{
		Hashtable<Vector<String>, Vector<Double>> newdistance=new Hashtable<Vector<String>, Vector<Double>>();
		for (Enumeration<Vector<String>> e = distance.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv=e.nextElement();
			Vector<Double> tempdistance=distance.get(gv);
			Vector<Double> newtempdistance=new Vector<Double>();
			for (int i=0; i<tempdistance.size(); i++)
			{
				double td=(tempdistance.get(i)).doubleValue();
				newtempdistance.add(new Double(td));
			}
			newdistance.put(gv, newtempdistance);
		}
		return newdistance;
	}
	/**
	*Returns the number of records for each cluster
	*/
	public Hashtable<Vector<String>, Vector<Double>> getnum()
	{
		Hashtable<Vector<String>, Vector<Double>> newnum=new Hashtable<Vector<String>, Vector<Double>>();
		for (Enumeration<Vector<String>> e = numforseed.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv=e.nextElement();
			Vector<Double> tempnum=numforseed.get(gv);
			Vector<Double> newtemnum=new Vector<Double>();
			for (int i=0; i<tempnum.size(); i++)
			{
				double td=(tempnum.get(i)).doubleValue();
				newtemnum.add(new Double(td));
			}
			newnum.put(gv, newtemnum);
		}
		return newnum;
	}
	/**
	* Return the minimum distance of the vector of values to the seed to which it is assigned
	*/
	public double getmindist()
	{
		return mindist;
	}
	/**
	*Return true if the alghoritm converged
	*/
	public boolean getstate()
	{
		return converged;
	}
	/**
	* Update the seeds
	*/
	public void updateseeds()
	{
		double variation=0;
		for (Enumeration<Vector<String>> e = sumforseed.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv=e.nextElement();
			Vector<Double> tempnum=numforseed.get(gv);
			Vector<double[]> tempsum=sumforseed.get(gv);
			Vector<double[]> tempseed=seeds.get(gv);
			Vector<Double> tempdistance=distance.get(gv);
			for (int i=0; i<tempnum.size(); i++)
			{
				double tempvariation=0;
				double actualnum=(tempnum.get(i)).doubleValue();
				if (actualnum<=minunit)
				{
					actualnum=0;
					tempseed.set(i, null);
					tempsum.set(i, null);
					tempnum.set(i, new Double(0));
					tempdistance.set(i, new Double(0));
				}
				else
				{
					double[] actualsum=tempsum.get(i);
					double[] actualseed=tempseed.get(i);
					for (int j=0; j<actualsum.length; j++)
					{
						tempvariation=tempvariation+Math.pow((actualseed[j]-(actualsum[j]/actualnum)),2);
						actualseed[j]=actualsum[j]/actualnum;
						actualsum[j]=0;
					}
					tempvariation=tempvariation/actualsum.length;
					variation+=tempvariation;
					actualnum=0;
					tempseed.set(i, actualseed);
					tempsum.set(i, actualsum);
					tempnum.set(i, new Double(actualnum));
					tempdistance.set(i, new Double(0));
				}
			}
			sumforseed.put(gv, tempsum);
			numforseed.put(gv, tempnum);
			seeds.put(gv, tempseed);
			distance.put(gv, tempdistance);
		}
		if (variation<=0.00000001)
			converged=true;
	}
	/**
	*Returns the seeds
	*/
	public Hashtable<Vector<String>, Vector<double[]>> getseeds()
	{
		return seeds;
	}
	/**
	*Sets the actual iteration
	*/
	public void setactualiter(int actualiter)
	{
		this.actualiter=actualiter;
	}
}
