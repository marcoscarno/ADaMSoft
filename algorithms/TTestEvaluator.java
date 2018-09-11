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
import java.util.Enumeration;
import java.lang.Math;

import cern.jet.stat.Probability;

/**
* This method evaluates the value of the statistic T for the difference between two means
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class TTestEvaluator
{
	Hashtable<Vector<String>, double[]> ttest1;
	Hashtable<Vector<String>, double[]> ttest2;
	Hashtable<Vector<String>, double[]> pttest1;
	Hashtable<Vector<String>, double[]> pttest2;
	Hashtable<Vector<String>, double[]> vartest;
	Hashtable<Vector<String>, double[]> pvartest;
	boolean samplevariance;
	/**
	*Initialise the main Objects, that will contains the previosuly evaluated statistic
	*/
	public TTestEvaluator (Hashtable<Vector<String>, double[]>sum, Hashtable<Vector<String>, double[]>n, Hashtable<Vector<String>, double[]> sq, boolean samplevariance, Hashtable<Vector<String>, double[]>nnp)
	{
		ttest1=new Hashtable<Vector<String>, double[]>();
		ttest2=new Hashtable<Vector<String>, double[]>();
		pttest1=new Hashtable<Vector<String>, double[]>();
		pttest2=new Hashtable<Vector<String>, double[]>();
		vartest=new Hashtable<Vector<String>, double[]>();
		pvartest=new Hashtable<Vector<String>, double[]>();
		for (Enumeration<Vector<String>> e = sum.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> tempgroup=e.nextElement();
			double[] s=sum.get(tempgroup);
			double[] nv=n.get(tempgroup);
			double[] nvp=nnp.get(tempgroup);
			double[] q=sq.get(tempgroup);
			int dimvar=s.length;
			double[] othersum=new double[dimvar];
			double[] othervar=new double[dimvar];
			double[] othern=new double[dimvar];
			double[] othernp=new double[dimvar];
			boolean existother=false;
			for (int i=0; i<dimvar; i++)
			{
				othersum[i]=0;
				othervar[i]=0;
				othern[i]=0;
				othernp[i]=0;
			}
			for (Enumeration<Vector<String>> f = sum.keys() ; f.hasMoreElements() ;)
			{
				Vector<String> othergroup=f.nextElement();
				if (!othergroup.equals(tempgroup))
				{
					existother=true;
					double[] others=sum.get(othergroup);
					double[] othernv=n.get(othergroup);
					double[] othernvp=nnp.get(othergroup);
					double[] othersq=sq.get(othergroup);
					for (int i=0; i<dimvar; i++)
					{
						if ((!Double.isNaN(others[i])) && (!Double.isNaN(othernv[i])) && (!Double.isNaN(othersq[i])))
						{
							othersum[i]=othersum[i]+others[i];
							othern[i]=othern[i]+othernv[i];
							othernp[i]=othernp[i]+othernvp[i];
							othervar[i]=othervar[i]+othersq[i];
						}
					}

				}
			}
			if (existother)
			{
				double[] resttest1=new double[dimvar];
				double[] resttest2=new double[dimvar];
				double[] prob1=new double[dimvar];
				double[] prob2=new double[dimvar];
				double[] statf=new double[dimvar];
				double[] pstatf=new double[dimvar];
				for (int i=0; i<dimvar; i++)
				{
					double thismean=s[i]/nv[i];
					double thisstd=(q[i]/nv[i])-(thismean*thismean);
					if ((!samplevariance) && (nv[i]>1))
					{
						thisstd=(thisstd*nv[i])/(nv[i]-1);
					}
					thisstd=Math.sqrt(thisstd);
					double thisvar=Math.pow(thisstd, 2);

					double othermeans=othersum[i]/othern[i];
					double otherstd=(othervar[i]/othern[i])-(othermeans*othermeans);
					if ((!samplevariance) && (nv[i]>1))
					{
						otherstd=(otherstd*othern[i])/(othern[i]-1);
					}
					otherstd=Math.sqrt(otherstd);
					double othervars=Math.pow(otherstd, 2);

					statf[i]=Double.NaN;
					pstatf[i]=Double.NaN;

					/*try
					{
						double a=nv[i]-1;
						double b=othern[i]-1;
						if (thisvar>=othervars)
						{
							statf[i]=thisvar/othervars;
							pstatf[i]=2*Probability.betaComplemented(a/2, b/2, 1/(1+(a/b)*statf[i]));
						}
						else
						{
							statf[i]=othervars/thisvar;
							pstatf[i]=2*Probability.betaComplemented(b/2, a/2, 1/(1+(b/a)*statf[i]));
						}
					}
					catch (Exception exint) {}*/

					try
					{
						double a=nvp[i]-1;
						double b=othernp[i]-1;
						if (thisvar>=othervars)
						{
							statf[i]=thisvar/othervars;
							pstatf[i]=2*Probability.betaComplemented(a/2, b/2, 1/(1+(a/b)*statf[i]));
						}
						else
						{
							statf[i]=othervars/thisvar;
							pstatf[i]=2*Probability.betaComplemented(b/2, a/2, 1/(1+(b/a)*statf[i]));
						}
					}
					catch (Exception exint) {}

					double diffmean=Math.abs(othermeans-thismean);

					/*double equalvars=(nv[i]-1)*Math.pow(thisstd,2)+(othern[i]-1)*Math.pow(otherstd,2);
					equalvars=Math.sqrt(equalvars/(nv[i]+othern[i]-2));*/

					double equalvars=(nvp[i]-1)*Math.pow(thisstd,2)+(othernp[i]-1)*Math.pow(otherstd,2);
					equalvars=Math.sqrt(equalvars/(nvp[i]+othernp[i]-2));

					/*double diffvars=Math.sqrt((Math.pow(thisstd,2)/nv[i])+(Math.pow(otherstd,2)/othern[i]));*/
					double diffvars=Math.sqrt((Math.pow(thisstd,2)/nvp[i])+(Math.pow(otherstd,2)/othernp[i]));

					/*double w1=Math.pow(Math.pow(thisstd,2)/nv[i],2);
					double w2=Math.pow(Math.pow(otherstd,2)/othern[i],2);
					double gl=Math.pow((Math.sqrt(w1)+Math.sqrt(w2)),2)/(w1/(nv[i]-1)+w2/(othern[i]-1));*/

					double w1=Math.pow(Math.pow(thisstd,2)/nvp[i],2);
					double w2=Math.pow(Math.pow(otherstd,2)/othernp[i],2);
					double gl=Math.pow((Math.sqrt(w1)+Math.sqrt(w2)),2)/(w1/(nvp[i]-1)+w2/(othernp[i]-1));

					try
					{
						/*resttest1[i]=diffmean/(equalvars*(Math.sqrt((1/nv[i])+(1/othern[i]))));*/
						resttest1[i]=diffmean/(equalvars*(Math.sqrt((1/nvp[i])+(1/othernp[i]))));
					}
					catch (Exception ex)
					{
						resttest1[i]=Double.NaN;
					}
					try
					{
						resttest2[i]=diffmean/diffvars;
					}
					catch (Exception ex)
					{
						resttest2[i]=Double.NaN;
					}
					try
					{
						/*prob1[i]=2*Probability.studentT(nv[i]+othern[i]-2, -1*resttest1[i]);*/
						prob1[i]=2*Probability.studentT(nvp[i]+othernp[i]-2, -1*resttest1[i]);
					}
					catch (Exception ex)
					{
						prob1[i]=Double.NaN;
					}
					try
					{
						prob2[i]=2*Probability.studentT(gl, -1*resttest2[i]);
					}
					catch (Exception ex)
					{
						prob2[i]=Double.NaN;
					}
				}
				ttest1.put(tempgroup, resttest1);
				ttest2.put(tempgroup, resttest2);
				pttest1.put(tempgroup, prob1);
				pttest2.put(tempgroup, prob2);
				vartest.put(tempgroup, statf);
				pvartest.put(tempgroup, pstatf);
			}
			else
			{
				double[] resttest1=new double[dimvar];
				double[] resttest2=new double[dimvar];
				double[] prob1=new double[dimvar];
				double[] prob2=new double[dimvar];
				double[] statf=new double[dimvar];
				double[] pstatf=new double[dimvar];
				for (int i=0; i<dimvar; i++)
				{
					statf[i]=Double.NaN;
					pstatf[i]=Double.NaN;
					double tempmean=s[i]/nv[i];
					double tempstd=(q[i]/nv[i])-(tempmean*tempmean);
					if ((!samplevariance) && (nv[i]>1))
						tempstd=(tempstd*nv[i])/(nv[i]-1);
					tempstd=Math.sqrt(tempstd);
					try
					{
						/*resttest1[i]=tempmean/(tempstd/(Math.sqrt(nv[i])));*/
						resttest1[i]=tempmean/(tempstd/(Math.sqrt(nvp[i])));
					}
					catch (Exception ex)
					{
						resttest1[i]=Double.NaN;
					}
					try
					{
						/*resttest2[i]=tempmean/(tempstd/(Math.sqrt(nv[i])));*/
						resttest2[i]=tempmean/(tempstd/(Math.sqrt(nvp[i])));
					}
					catch (Exception ex)
					{
						resttest2[i]=Double.NaN;
					}
					try
					{
						/*prob1[i]=1-Probability.studentT(nv[i]-1, resttest1[i]);*/
						prob1[i]=1-Probability.studentT(nvp[i]-1, resttest1[i]);
					}
					catch (Exception ex)
					{
						prob1[i]=Double.NaN;
					}
					try
					{
						/*prob2[i]=1-Probability.studentT(nv[i]-1, resttest2[i]);*/
						prob2[i]=1-Probability.studentT(nvp[i]-1, resttest2[i]);
					}
					catch (Exception ex)
					{
						prob2[i]=Double.NaN;
					}
				}
				ttest1.put(tempgroup, resttest1);
				ttest2.put(tempgroup, resttest2);
				pttest1.put(tempgroup, prob1);
				pttest2.put(tempgroup, prob2);
				vartest.put(tempgroup, statf);
				pvartest.put(tempgroup, pstatf);
			}
		}
	}
	/**
	*Gives back the value of the ttest statistic
	*/
	public Hashtable<Vector<String>, double[]> gettest1()
	{
		return ttest1;
	}
	/**
	*Gives back the value of the ttest statistic
	*/
	public Hashtable<Vector<String>, double[]> gettest2()
	{
		return ttest2;
	}
	/**
	*Gives back the value of the ttest statistic
	*/
	public Hashtable<Vector<String>, double[]> getptest1()
	{
		return pttest1;
	}
	/**
	*Gives back the value of the ttest statistic
	*/
	public Hashtable<Vector<String>, double[]> getptest2()
	{
		return pttest2;
	}
	/**
	*Gives back the value of the variance test statistic
	*/
	public Hashtable<Vector<String>, double[]> getvartest()
	{
		return vartest;
	}
	/**
	*Gives back the value of the variance test probablity statistic
	*/
	public Hashtable<Vector<String>, double[]> getpvartest()
	{
		return pvartest;
	}
}
