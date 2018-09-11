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

import java.util.HashSet;
import java.util.Vector;

import ADaMSoft.algorithms.clusters.MatrixCluster;
import ADaMSoft.algorithms.clusters.SingleCluster;


/**
* This method evaluates the Clusters from a triangular matrix
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ClustersEvaluator
{
	double[][] phimatrix;
	boolean type;
	/**
	*Initialise the main Objects, that will contains the matrix
	*/
	public ClustersEvaluator (double[][] phimatrix, boolean type)
	{
		this.phimatrix=phimatrix;
		this.type=type;
	}
	/**
	*Estimates the clusters
	*/
	public Vector<String[]> estimateclusters(String[] realvarnames)
	{
		MatrixCluster mc=new MatrixCluster(3, type);
		String[] varnames=new String[realvarnames.length];
		for (int i=0; i<realvarnames.length; i++)
		{
			varnames[i]="v_"+realvarnames[i];
		}
		Vector<SingleCluster> vsc=new Vector<SingleCluster>();
		double maxdist=Double.MAX_VALUE;
		if (!type)
			maxdist=-1.7976931348623157E308;
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
				if (type)
				{
					if (phimatrix[i][j]<maxdist)
					{
						maxdist=phimatrix[i][j];
						firstelement=tempfirstelement;
					}
				}
				else
				{
					if (phimatrix[i][j]>maxdist)
					{
						maxdist=phimatrix[i][j];
						firstelement=tempfirstelement;
					}
				}
				SingleCluster sct=new SingleCluster(nnam, nam, dist);
				vsc.add(sct);
				tempfirstelement++;
			}
		}
		mc.setfirstelement(firstelement);
		Vector<String[]> result=mc.joinclusters(vsc, phimatrix.length-1);
		return result;
	}
}
