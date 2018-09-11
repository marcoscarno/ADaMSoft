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
import java.util.Vector;
import java.util.TreeSet;
import java.util.Iterator;

/**
* This method implements the ID3 algorithm
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ID3Evaluator
{
	Hashtable<String, Hashtable<Vector<String>, Double>> freq;
	TreeSet<String> distincty;
	Vector<TreeSet<String>> distinctx;
	String[] varx;
	double[] sumY;
	boolean freqY;
	/**
	*Initialise the main Objects, that will contains the frequencies and the distinct values of the variables
	*/
	public ID3Evaluator (String[] varx)
	{
		this.varx=varx;
		freq=new Hashtable<String, Hashtable<Vector<String>, Double>>();
		distincty=new TreeSet<String>();
		distinctx=new Vector<TreeSet<String>>();
		for (int i=0; i<varx.length; i++)
		{
			distinctx.add(new TreeSet<String>());
		}
	}

	public String[] getVarx(){

		return varx;
	}

	public void setfreqRel(){

		freqY=true;
		sumY=new double[getdy().size()];
		String[] vals = new String[varx.length];
		int i=0;
		Iterator<String> keysetIt = distincty.iterator();
		while(keysetIt.hasNext())
		{
			String val = keysetIt.next();
			sumY[i++]=getfreq(vals, val);
		}
	}

	/**
	*Estimate the frequencies
	*/
	public void estimate(String[] valx, String valy, double f)
	{
		distincty.add(valy);
		Vector<String> tempve=new Vector<String>();
		for (int i=0; i<valx.length; i++)
		{
			distinctx.get(i).add(valx[i]);
			tempve.add(valx[i]);
		}
		if (freq.get(valy)==null)
		{
			Hashtable<Vector<String>, Double> tempf=new Hashtable<Vector<String>, Double>();
			tempf.put(tempve, new Double(f));
			freq.put(valy,tempf);
		}
		else
		{
			Hashtable<Vector<String>, Double> tempf=freq.get(valy);
			if (tempf.get(tempve)==null)
				tempf.put(tempve, new Double(f));
			else
			{
				double t=(tempf.get(tempve)).doubleValue();
				tempf.put(tempve, new Double(f+t));
			}
			freq.put(valy,tempf);
		}
	}
	/**
	* Returns the frequencies for a given values of the independent variable, for the dependent
	*/
	public double getfreq(String[] valx, String valy)
	{
		Hashtable<Vector<String>, Double> tempf=freq.get(valy);
		double retf=0;
		Iterator<Vector<String>> keysetIt = tempf.keySet().iterator();
		while(keysetIt.hasNext())
		{
			Vector<String> key= keysetIt.next();
			boolean match = true;
			for (int i=0; i<valx.length; i++)
			{
				if(valx[i]!=null){
					match &= valx[i].equals(key.get(i));
				}
			}
			if(match){
				retf+=(tempf.get(key)).doubleValue();
			}
		}
		return retf;
	}

	/**
	* Returns the frequencies independently from the dependent variable
	*/
	public double[] getfreqy(String[] valx)
	{
		double[] retf=new double[distincty.size()];
		Iterator<String> keysetIt = distincty.iterator();
		int i=0;
		while(keysetIt.hasNext())
		{
			String tempy=keysetIt.next();
			retf[i]=0;
			Hashtable<Vector<String>, Double> tempf=freq.get(tempy);
			Iterator<Vector<String>> keysetItt = tempf.keySet().iterator();
			while(keysetItt.hasNext())
			{
				Vector<String> key= keysetItt.next();
				boolean match = true;
				for (int j=0; j<valx.length; j++)
				{
					if(valx[j]!=null){
						match &= valx[j].equals(key.get(j));
					}
				}
				if(match){
					retf[i]+=(tempf.get(key)).doubleValue();
				}
			}
			i++;
		}
		if(freqY){
			for(int j=0;j<retf.length;j++){
				retf[j]=(retf[j]/sumY[j])*100;
				/*if(retf[j]<1){
					retf[j]=0;
				}*/
			}
		}
		return retf;
	}
	/**
	* Returns the different values of the dependent variable
	*/
	public TreeSet<String> getdy()
	{
		return distincty;
	}
	/**
	* Returns the different values of the pos-th independent variable
	*/
	public TreeSet<String> getdx(int pos)
	{
		return distinctx.get(pos);
	}
	/**
	* Returns the entropy for the given values of the independent variables
	*/
	public double getentropy (String[] valx)
	{
		double[] retf=getfreqy(valx);
		double sum=0;
		for (int i=0; i<retf.length; i++)
		{
			sum+=retf[i];
		}
		double entropy=0;
		for (int i=0; i<retf.length; i++)
		{
			if (retf[i]!=0)
				entropy-=(retf[i]/sum)*(Math.log(retf[i]/sum)/Math.log(2));
		}
		return entropy;
	}
	/**
	* Returns the gain for the given values of the independent variables
	*/
	public double getgain(String[] valx, int posvar)
	{
		int i=0;
		String[] valxx=new String[valx.length];
		for (i=0; i<valx.length; i++)
		{
			valxx[i]=valx[i];
		}
		TreeSet<String> tempx=getdx(posvar);
		double[] tempvx=new double[tempx.size()];
		double[] entrox=new double[tempx.size()];
		double sum=0;
		Iterator<String> keysetIt = tempx.iterator();
		i=0;
		while(keysetIt.hasNext())
		{
			valxx[posvar]=keysetIt.next();
			double[] tempf=getfreqy(valxx);
			tempvx[i]=0;
			entrox[i]=getentropy(valxx);
			for (int j=0; j<tempf.length; j++)
			{
				tempvx[i]+=tempf[j];
			}
			sum+=tempvx[i];
			i++;
		}
		double gain=0;
		for (i=0; i<tempx.size(); i++)
		{
			gain-=tempvx[i]/sum*entrox[i];
		}
		gain=getentropy(valx)+gain;
		if (Double.isNaN(gain))
			gain=0;
		return gain;
	}
}
