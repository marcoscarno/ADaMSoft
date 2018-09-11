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
import java.util.Vector;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.colt.matrix.linalg.Property;

/**
* This method evaluates the parameters for a linear discriminant analysis
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class LdaEvaluator
{
	Hashtable<Vector<String>, double[]> coeff;
	Hashtable<Vector<String>, String> errormsg;
	Hashtable<Vector<String>, Hashtable<String, Double>> numobs;
	Hashtable<Vector<String>, Hashtable<String, double[]>> means;
	Hashtable<Vector<String>, Hashtable<String, double[][]>> cov;
	int dimmat;
	Hashtable<Vector<String>, Double> sumofeigen;
	/**
	*Initialise the main Objects
	*/
	public LdaEvaluator (int dimmat)
	{
		this.dimmat=dimmat;
		numobs=new Hashtable<Vector<String>, Hashtable<String, Double>>();
		means=new Hashtable<Vector<String>, Hashtable<String, double[]>>();
		cov=new Hashtable<Vector<String>, Hashtable<String, double[][]>>();
		sumofeigen=new Hashtable<Vector<String>, Double>();
	}
	/**
	*Adds the information on the current record
	*/
	public void addvalue(Vector<String> vargroupvalues, String[] varyvalues, double[] varxvalues,
						double weightvalue)
	{
		if (numobs.get(vargroupvalues)==null)
		{
			Hashtable<String, Double> tempnumobs=new Hashtable<String, Double>();
			tempnumobs.put(varyvalues[0], new Double(weightvalue));
			double[] tm=new double[varxvalues.length];
			for (int i=0; i<varxvalues.length; i++)
			{
				tm[i]=varxvalues[i]*weightvalue;
			}
			Hashtable<String, double[]> tempmeans=new Hashtable<String, double[]>();
			tempmeans.put(varyvalues[0], tm);
			double[][] tc=new double[varxvalues.length][varxvalues.length];
			for (int i=0; i<varxvalues.length; i++)
			{
				for (int j=0; j<varxvalues.length; j++)
				{
					tc[i][j]=varxvalues[i]*varxvalues[j]*weightvalue;
				}
			}
			Hashtable<String, double[][]> tempcov=new Hashtable<String, double[][]>();
			tempcov.put(varyvalues[0], tc);
			numobs.put(vargroupvalues, tempnumobs);
			means.put(vargroupvalues, tempmeans);
			cov.put(vargroupvalues, tempcov);
		}
		else
		{
			Hashtable<String, Double> tempnumobs=numobs.get(vargroupvalues);
			if (tempnumobs.get(varyvalues[0])==null)
				tempnumobs.put(varyvalues[0], new Double(weightvalue));
			else
			{
				double tnumobs=(tempnumobs.get(varyvalues[0])).doubleValue();
				tempnumobs.put(varyvalues[0], new Double(tnumobs+weightvalue));
			}
			Hashtable<String, double[]> tempmeans=means.get(vargroupvalues);
			if (tempmeans.get(varyvalues[0])==null)
			{
				double[] tm=new double[varxvalues.length];
				for (int i=0; i<varxvalues.length; i++)
				{
					tm[i]=varxvalues[i]*weightvalue;
				}
				tempmeans.put(varyvalues[0], tm);
			}
			else
			{
				double[] tm=tempmeans.get(varyvalues[0]);
				for (int i=0; i<varxvalues.length; i++)
				{
					tm[i]=tm[i]+varxvalues[i]*weightvalue;
				}
				tempmeans.put(varyvalues[0], tm);
			}
			Hashtable<String, double[][]> tempcov=cov.get(vargroupvalues);
			if (tempcov.get(varyvalues[0])==null)
			{
				double[][] tc=new double[varxvalues.length][varxvalues.length];
				for (int i=0; i<varxvalues.length; i++)
				{
					for (int j=0; j<varxvalues.length; j++)
					{
						tc[i][j]=varxvalues[i]*varxvalues[j]*weightvalue;
					}
				}
				tempcov.put(varyvalues[0], tc);
			}
			else
			{
				double[][] tc=tempcov.get(varyvalues[0]);
				for (int i=0; i<varxvalues.length; i++)
				{
					for (int j=0; j<varxvalues.length; j++)
					{
						tc[i][j]=tc[i][j]+varxvalues[i]*varxvalues[j]*weightvalue;
					}
				}
				tempcov.put(varyvalues[0], tc);
			}
			numobs.put(vargroupvalues, tempnumobs);
			means.put(vargroupvalues, tempmeans);
			cov.put(vargroupvalues, tempcov);
		}
	}
	/**
	*Evaluates the coefficients
	*/
	public void calculate()
	{
		errormsg=new Hashtable<Vector<String>, String>();
		coeff=new Hashtable<Vector<String>, double[]>();
		for (Enumeration<Vector<String>> e = numobs.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv= e.nextElement();
			Hashtable<String, Double> tempnumobs=numobs.get(gv);
			Hashtable<String, double[]> tempmeans=means.get(gv);
			Hashtable<String, double[][]> tempcov=cov.get(gv);
			double[][] sw=new double[dimmat][dimmat];
			double[][] sb=new double[dimmat][dimmat];
			double[][] vm=new double[tempnumobs.size()][dimmat];
			double[] realmeans=new double[dimmat];
			double[] tempcoeff=new double[dimmat];
			for (int i=0; i<dimmat; i++)
			{
				tempcoeff[i]=Double.NaN;
				realmeans[i]=0;
				for (int j=0; j<dimmat; j++)
				{
					sw[i][j]=0;
					sb[i][j]=0;
				}
				for (int j=0; j<tempnumobs.size(); j++)
				{
					vm[j][i]=0;
				}
			}
			boolean acterr=false;
			double totobs=0;
			int actualclass=0;
			for (Enumeration<String> f = tempnumobs.keys() ; f.hasMoreElements() ;)
			{
				String vref= f.nextElement();
				double tn=(tempnumobs.get(vref)).doubleValue();
				totobs+=tn;
				double[] tm=tempmeans.get(vref);
				double[][] tc=tempcov.get(vref);
				for (int i=0; i<dimmat; i++)
				{
					for (int j=0; j<dimmat; j++)
					{
						try
						{
							sw[i][j]=sw[i][j]+tn*(tc[i][j]/tn-((tm[i]/tn)*(tm[j]/tn)));
						}
						catch (Exception ex)
						{
							acterr=true;
						}
					}
					vm[actualclass][i]=tm[i]/tn;
					realmeans[i]=realmeans[i]+tm[i];
				}
				actualclass++;
			}
			if (!acterr)
			{
				try
				{
					for (int i=0; i<dimmat; i++)
					{
						realmeans[i]=realmeans[i]/totobs;
					}
					for (int i=0; i<dimmat; i++)
					{
						for (int j=0; j<dimmat; j++)
						{
							sw[i][j]=sw[i][j]/totobs;
							for (int k=0; k<actualclass; k++)
							{
								sb[i][j]=sb[i][j]+(vm[k][j]-realmeans[j])*(vm[k][i]-realmeans[i])*(1/(double)actualclass);
							}
						}
					}
					DoubleMatrix2D MatSB=DoubleFactory2D.dense.make(dimmat, dimmat);
					DoubleMatrix2D MatSW=DoubleFactory2D.dense.make(dimmat, dimmat);
					for (int i=0; i<dimmat; i++)
					{
						for (int j=0; j<dimmat; j++)
						{
							MatSB.set(i, j, sb[i][j]);
							MatSW.set(i, j, sw[i][j]);
						}
					}
					boolean issingular=false;
					Property property=new Property(Property.DEFAULT.tolerance());
					try
					{
						issingular=property.isSingular(MatSW);
					}
					catch (Exception eee) {}
					if (!issingular)
					{
						Algebra algebra=new Algebra();
						DoubleMatrix2D MatSWI=algebra.inverse(MatSW);
						DoubleMatrix2D MatSWISB=algebra.mult(MatSWI, MatSB);
						EigenvalueDecomposition ed=new EigenvalueDecomposition(MatSWISB);
						DoubleMatrix1D eigenval=ed.getRealEigenvalues();
						DoubleMatrix2D eigenvec=ed.getV();
						int maxeigenval=0;
						double actualeigenval=0;
						double valmaxe=-1.7976931348623157E308;
						for (int i=0; i<dimmat; i++)
						{
							actualeigenval=actualeigenval+eigenval.get(i);
							if (eigenval.get(i)>valmaxe)
							{
								maxeigenval=i;
								valmaxe=actualeigenval;
							}
						}
						for (int i=0; i<dimmat; i++)
						{
							tempcoeff[i]=eigenvec.get(i, maxeigenval);
						}
						sumofeigen.put(gv, new Double(100*valmaxe/actualeigenval));
						errormsg.put(gv,"");
					}
					else
					{
						sumofeigen.put(gv, new Double(Double.NaN));
						errormsg.put(gv,"%1940%");
					}
				}
				catch (Exception ex)
				{
					errormsg.put(gv,"%1934%");
					sumofeigen.put(gv, new Double(Double.NaN));
				}
			}
			else
			{
				sumofeigen.put(gv, new Double(Double.NaN));
				errormsg.put(gv,"%1934%");
			}
			coeff.put(gv, tempcoeff);
		}

	}
	/**
	*Returns the parameters
	*/
	public Hashtable<Vector<String>, double[]> getcoeff()
	{
		return coeff;
	}
	/**
	*Returns the indeces of representation
	*/
	public Hashtable<Vector<String>, Double> getIndexOfEigen()
	{
		return sumofeigen;
	}
	/**
	*Returns the error message
	*/
	public Hashtable<Vector<String>, String> geterrormsg()
	{
		return errormsg;
	}
}
