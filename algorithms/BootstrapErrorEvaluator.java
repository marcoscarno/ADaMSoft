/**
* Copyright © 2017 ADaMSoft
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

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import ADaMSoft.dataaccess.GroupedMatrix2Dfile;
import cern.jet.random.Uniform;


/**
* This method evaluates the bootstrap error for several variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class BootstrapErrorEvaluator
{
	int nvar;
	int nsample;
	GroupedMatrix2Dfile var;
	VarGroupModalities vgm;
	Hashtable<Vector<String>, double[][]> bootp;
	Hashtable<Vector<String>, double[]> stat;
	int type;
	int startrandom;
	boolean todisk;
	/**
	*Initialise the main Objects; if type equals to 1 means to evaluate the bootstrap mean, if todisk means that the data will not stored into memory
	*/
	public BootstrapErrorEvaluator(int nvar, int nsample, int type, boolean todisk)
	{
		this.nvar=nvar;
		this.nsample=nsample;
		this.type=type;
		this.todisk=todisk;
		bootp=new Hashtable<Vector<String>, double[][]>();
		stat=new Hashtable<Vector<String>, double[]>();
	}
	/**
	*Receives the matrices of the values and starts to evaluate the means (or the sum) for each sample
	*/
	public void evaluate(VarGroupModalities vgm, GroupedMatrix2Dfile var)
	{
		int numgroup=vgm.getTotal();
		if (numgroup==0)
			numgroup=1;
		for (int g=0; g<numgroup; g++)
		{
			Vector<String> tempgroup=vgm.getvectormodalities(g);
			int nobs=var.getRows(tempgroup);
			double[][] bootstrapvalue=new double[nsample][nvar];
			double[] realstat=new double[nvar];
			double totalforstratum=0;
			Hashtable<Integer, Integer> times=new Hashtable<Integer, Integer>();
			double[][] tempval=null;
			if (!todisk)
				tempval=new double[nobs][nvar+1];
			double rw=0;
			for (int k=0; k<nvar; k++)
			{
				realstat[k]=0;
			}
			for (int i=0; i<nobs; i++)
			{
				double w=var.read(tempgroup, i, 0);
				if (!todisk)
					tempval[i][0]=w;
				totalforstratum=totalforstratum+w;
				for (int j=1; j<nvar+1; j++)
				{
					rw=var.read(tempgroup, i, j);
					if (!todisk)
						tempval[i][j]=rw;
					realstat[j-1]=realstat[j-1]+w*rw;
				}
			}
			startrandom=(new Long((new Date()).getTime())).intValue();
			Uniform random=new Uniform(0, nobs, startrandom);
			double neww=0;
			int rif=0;
			int ttimes=0;
			int tobs=0;
			double newsum=0;
			double dtimes=0;
			if (type==1)
			{
				for (int k=0; k<nvar; k++)
				{
					realstat[k]=realstat[k]/totalforstratum;
				}
			}
			for (int i=0; i<nsample; i++)
			{
				for (int k=0; k<nvar; k++)
				{
					bootstrapvalue[i][k]=0;
				}
				for (int j=0; j<nobs-1; j++)
				{
					rif=(new Double(random.nextDouble())).intValue();
					if (times.get(new Integer(rif))==null)
						times.put(new Integer(rif), new Integer(1));
					else
					{
						ttimes=(times.get(new Integer(rif))).intValue()+1;
						times.put(new Integer(rif), new Integer(ttimes));
					}
				}
				newsum=0;
				for (Enumeration<Integer> e = times.keys() ; e.hasMoreElements() ;)
				{
					tobs=(e.nextElement()).intValue();
					ttimes=(times.get(new Integer(tobs))).intValue();
					dtimes=(double)ttimes;
					if (!todisk)
						neww=dtimes*(tempval[tobs][0]/totalforstratum)*(nobs/(nobs-1));
					else
						neww=dtimes*(var.read(tempgroup, tobs, 0)/totalforstratum)*(nobs/(nobs-1));
					newsum=newsum+neww;
					for (int j=0; j<nvar; j++)
					{
						if (!todisk)
							bootstrapvalue[i][j]=bootstrapvalue[i][j]+tempval[tobs][j+1]*neww;
						else
							bootstrapvalue[i][j]=bootstrapvalue[i][j]+var.read(tempgroup, tobs, j+1)*neww;
					}
				}
				for (int j=0; j<nvar; j++)
				{
					bootstrapvalue[i][j]=bootstrapvalue[i][j]/newsum;
					if (type==2)
						bootstrapvalue[i][j]=bootstrapvalue[i][j]*totalforstratum;
				}
				times.clear();

			}
			stat.put(tempgroup, realstat);
			bootp.put(tempgroup, bootstrapvalue);
		}
	}
	/**
	*Returns the array of the boostrap values
	*/
	public Hashtable<Vector<String>, double[][]> getboot()
	{
		return bootp;
	}
	/**
	*Returns the statistic evaluate for all the values
	*/
	public Hashtable<Vector<String>, double[]> getstat()
	{
		return stat;
	}
}