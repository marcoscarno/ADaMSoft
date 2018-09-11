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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import java.util.HashSet;

import ADaMSoft.algorithms.clusters.MatrixCluster;
import ADaMSoft.algorithms.clusters.SingleCluster;


/**
* This method evaluates the Connections indexes (Chi Square, Phi Square, Cramer V) for each couple of qualitative variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class CrossedConnectionsEvaluator
{
	Hashtable<Vector<String>, Vector<Hashtable<String, Vector<Hashtable<String, Double>>>>> freqg;
	Hashtable<Vector<String>, Vector<TreeSet<String>>> vardiffvalg;
	int conntype;
	Hashtable<Vector<String>, double[][]> phig;
	Hashtable<Vector<String>, double[][]> dimensions;
	/**
	*Initialise the main Objects, that will contains the frequencies and the distinct values of the variables;
	*If conntype=1 then the connection matrix contains the chi squares;
	*If conntype=2 then the connection matrix contains the phi squares;
	*If conntype=3 then the connection matrix contains the Cramer V;
	*/
	public CrossedConnectionsEvaluator (int conntype)
	{
		phig=new Hashtable<Vector<String>, double[][]>();
		dimensions=new Hashtable<Vector<String>, double[][]>();
		this.conntype=conntype;
		freqg=new Hashtable<Vector<String>, Vector<Hashtable<String, Vector<Hashtable<String, Double>>>>>();
		vardiffvalg=new Hashtable<Vector<String>, Vector<TreeSet<String>>>();
	}
	/**
	*Estimate the frequencies
	*/
	public void estimate(Vector<String> groupval, String[] valx, double f)
	{
		Vector<TreeSet<String>> vardiffval=vardiffvalg.get(groupval);
		Vector<Hashtable<String, Vector<Hashtable<String, Double>>>> freq=freqg.get(groupval);
		if (freq==null)
		{
			vardiffval=new Vector<TreeSet<String>>();
			for (int i=0; i<valx.length; i++)
			{
				TreeSet<String> temp=new TreeSet<String>();
				vardiffval.add(temp);
			}
			vardiffvalg.put(groupval, vardiffval);
			freq=new Vector<Hashtable<String, Vector<Hashtable<String, Double>>>>();
			freqg.put(groupval, freq);
		}

		for (int i=0; i<valx.length; i++)
		{
			TreeSet<String> tempdiffval=vardiffval.get(i);
			if (!valx[i].equals(""))
				tempdiffval.add(valx[i]);
			vardiffval.set(i, tempdiffval);
			if ((freq.size()-1)<i)
			{
				Hashtable<String, Vector<Hashtable<String, Double>>> temp=new Hashtable<String, Vector<Hashtable<String, Double>>>();
				freq.add(temp);
			}
			for (int j=0; j<valx.length; j++)
			{
				Hashtable<String, Vector<Hashtable<String, Double>>> temp=freq.get(i);
				if (temp.get(valx[i])==null)
				{
					Vector<Hashtable<String, Double>> tem=new Vector<Hashtable<String, Double>>();
					Hashtable<String, Double> te=new Hashtable<String, Double>();
					te.put(valx[j], new Double(f));
					tem.add(te);
					temp.put(valx[i], tem);
				}
				else
				{
					Vector<Hashtable<String, Double>> tem=temp.get(valx[i]);
					if ((tem.size()-1)<j)
					{
						Hashtable<String, Double> te=new Hashtable<String, Double>();
						te.put(valx[j], new Double(f));
						tem.add(te);
						temp.put(valx[i], tem);
					}
					else
					{
						Hashtable<String, Double> te=tem.get(j);
						double real=0;
						if (te.get(valx[j])!=null)
							real=(te.get(valx[j])).doubleValue();
						te.put(valx[j], new Double(real+f));
						tem.set(j, te);
						temp.put(valx[i], tem);
					}
				}
				freq.set(i, temp);
			}
		}
		freqg.put(groupval, freq);
		vardiffvalg.put(groupval, vardiffval);
	}
	/**
	* Estimate the connection matrix
	*/
	public void estimateconnections()
	{
		for (Enumeration<Vector<String>> e = freqg.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv= e.nextElement();
			Vector<Hashtable<String, Vector<Hashtable<String, Double>>>> freq=freqg.get(gv);
			double[][] phimatrix=new double[freq.size()][freq.size()];
			double[][] dimphimatrix=new double[freq.size()][freq.size()];
			int maxsize=0;
			for (int i=0; i<freq.size(); i++)
			{
				Hashtable<String, Vector<Hashtable<String, Double>>> temp=freq.get(i);
				if (temp.size()>maxsize)
					maxsize=temp.size();
			}
			double[][][][] tempfreq=new double[freq.size()][freq.size()][maxsize][maxsize];
			int ri=0;
			int rc=0;
			Vector<TreeSet<String>> vardiffval=vardiffvalg.get(gv);
			for (int i=0; i<freq.size(); i++)
			{
				Hashtable<String, Vector<Hashtable<String, Double>>> temp=freq.get(i);
				TreeSet<String> tempvardiffvali=vardiffval.get(i);
				Iterator<String> keysetfvi = tempvardiffvali.iterator();
				ri=0;
				while(keysetfvi.hasNext())
				{
					String tv=keysetfvi.next();
					if (temp.get(tv)!=null)
					{
						Vector<Hashtable<String, Double>> tem=temp.get(tv);
						for (int j=0; j<tem.size(); j++)
						{
							Hashtable<String, Double>te=tem.get(j);
							TreeSet<String> tempvardiffvalc=vardiffval.get(j);
							Iterator<String> keysetfvc = tempvardiffvalc.iterator();
							rc=0;
							while(keysetfvc.hasNext())
							{
								String tvv=keysetfvc.next();
								if (te.get(tvv)!=null)
									tempfreq[i][j][ri][rc]=(te.get(tvv)).doubleValue();
								rc++;
							}
						}
					}
					ri++;
				}
			}
			for (int i=0; i<freq.size(); i++)
			{
				for (int j=0; j<freq.size(); j++)
				{
					phimatrix[i][j]=0;
					dimphimatrix[i][j]=0;
					int n=0;
					double[] pr=new double[maxsize];
					double[] pc=new double[maxsize];
					for (int r=0; r<maxsize; r++)
					{
						pr[r]=0;
						pc[r]=0;
					}
					int nr=0;
					int nc=0;
					for (int r=0; r<maxsize; r++)
					{
						double tr=0;
						for (int c=0; c<maxsize; c++)
						{
							n+=tempfreq[i][j][r][c];
							if (tempfreq[i][j][r][c]!=0)
								tr+=tempfreq[i][j][r][c];
						}
						pr[r]+=tr;
						if (tr!=0)
							nr++;
					}
					for (int c=0; c<maxsize; c++)
					{
						double tc=0;
						for (int r=0; r<maxsize; r++)
						{
							if (tempfreq[i][j][r][c]!=0)
								tc+=tempfreq[i][j][r][c];
						}
						pc[c]+=tc;
						if (tc!=0)
							nc++;
					}
					for (int r=0; r<nr; r++)
					{
						for (int c=0; c<nc; c++)
						{
							double den=pr[r]*pc[c];
							if (den!=0)
								phimatrix[i][j]+=((Math.pow(tempfreq[i][j][r][c],2))/den);
						}
					}
					phimatrix[i][j]=phimatrix[i][j]-1;
					if (conntype==1)
						phimatrix[i][j]=phimatrix[i][j]*n;
					else if (conntype==3)
					{
						if (nr<=nc)
							phimatrix[i][j]=Math.sqrt(phimatrix[i][j]/(nr-1));
						else
							phimatrix[i][j]=Math.sqrt(phimatrix[i][j]/(nc-1));
					}
					dimphimatrix[i][j]=(nr-1)*(nc-1);
				}
			}
			phig.put(gv, phimatrix);
			dimensions.put(gv, dimphimatrix);
		}
	}
	/**
	* Return the matrix
	*/
	public Hashtable<Vector<String>, double[][]> getmatrix()
	{
		return phig;
	}
	/**
	* Return the dimension matrix used for the phi pvalue
	*/
	public Hashtable<Vector<String>, double[][]> getdimmatrix()
	{
		return dimensions;
	}
	/**
	*Estimates the clusters
	*/
	public Hashtable<Vector<String>, Vector<String[]>> estimateclusters(String[] realvarnames)
	{
		MatrixCluster mc=new MatrixCluster(3, false);
		Hashtable<Vector<String>, Vector<String[]>> clusters=new Hashtable<Vector<String>, Vector<String[]>>();
		for (Enumeration<Vector<String>> e = phig.keys() ; e.hasMoreElements() ;)
		{
			String[] varnames=new String[realvarnames.length];
			for (int i=0; i<realvarnames.length; i++)
			{
				varnames[i]="v_"+realvarnames[i];
			}
			Vector<String> gv= e.nextElement();
			double[][] phimatrix=phig.get(gv);
			Vector<SingleCluster> vsc=new Vector<SingleCluster>();
			double maxdist=-1.7976931348623157E308;
			int firstelement=0;
			int tempfirstelement=0;
			for (int i=0; i<phimatrix.length-1; i++)
			{
				for (int j=i+1; j<phimatrix.length; j++)
				{
					Vector<Double> dist=new Vector<Double>();
					dist.add(new Double(phimatrix[i][j]));
					HashSet<String> nam=new HashSet<String>();
					nam.add(varnames[i]);
					nam.add(varnames[j]);
					HashSet<String> nnam=new HashSet<String>();
					nnam.add(varnames[i]);
					nnam.add(varnames[j]);
					if (phimatrix[i][j]>maxdist)
					{
						maxdist=phimatrix[i][j];
						firstelement=tempfirstelement;
					}
					SingleCluster sct=new SingleCluster(nnam, nam, dist);
					vsc.add(sct);
					tempfirstelement++;
				}
			}
			mc.setfirstelement(firstelement);
			Vector<String[]> result=mc.joinclusters(vsc, phimatrix.length-1);
			clusters.put(gv, result);
		}
		return clusters;
	}
}
