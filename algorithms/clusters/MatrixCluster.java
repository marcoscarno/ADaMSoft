/**
* Copyright (c) 2017 ADaMSoft
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

package ADaMSoft.algorithms.clusters;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

/**
* This method receive the vector of Single Cluster and gives back the new Vector of Single clusters made by considering the given joining method
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class MatrixCluster
{
	int joinmethod;
	Vector<String[]> result;
	int actualdim;
	boolean associatewithminimum;
	int firstelement;
	/**
	*Initialize the method by receiving the info on the joining method and the condition to take the minimum distance or not in order to join the units<p>
	*1 means single<p>
	*2 means complete<p>
	*3 means average
	*/
	public MatrixCluster (int joinmethod, boolean associatewithminimum)
	{
		result=new Vector<String[]>();
		this.joinmethod=joinmethod;
		this.associatewithminimum=associatewithminimum;
		firstelement=-1;
	}
	/**
	*Used to set the first element to use in the algorithm
	*/
	public void setfirstelement(int firstelement)
	{
		this.firstelement=firstelement;
	}
	/**
	*Receive the vector of single clusters and proceed on their joining
	*/
	public Vector<String[]> joinclusters(Vector<SingleCluster> vsc, int ielements)
	{
		double maxdist=Double.MAX_VALUE;
		if (!associatewithminimum)
			maxdist=-1.7976931348623157E308;
		if (firstelement==-1)
		{
			for (int i=0; i<vsc.size(); i++)
			{
				SingleCluster sct=vsc.get(i);
				Vector<Double> dtemp=sct.getdistances();
				double actualdistance=getrefdistance(dtemp);
				if (associatewithminimum)
				{
					if (actualdistance<maxdist)
					{
						firstelement=i;
						maxdist=actualdistance;
					}
				}
				else
				{
					if (actualdistance>maxdist)
					{
						firstelement=i;
						maxdist=actualdistance;
					}
				}
			}
		}
		for (int el=0; el<ielements-1; el++)
		{
			Vector<SingleCluster> tempvsc=new Vector<SingleCluster>();
			String[] tempres=new String[5];
			SingleCluster refcl=vsc.get(firstelement);
			Vector<Double> refdist=refcl.getdistances();
			HashSet<String> refnam=refcl.getnames();
			HashSet<String> refcnam=refcl.getclustername();
			Iterator<String> itn=refcnam.iterator();
			int p=0;
			while(itn.hasNext())
			{
				tempres[p]=itn.next();
				p++;
			}
			if (refnam.size()==2)
				maxdist=(refdist.get(0)).doubleValue();
			tempres[3]=String.valueOf(refnam.size());
			tempres[2]=String.valueOf(maxdist);
			tempres[4]="CL"+String.valueOf(el+1);
			result.add(tempres);
			maxdist=Double.MAX_VALUE;
			if (!associatewithminimum)
				maxdist=-1.7976931348623157E308;
			boolean isunit=false;
			int newfirstelement=-1;
			int pointerelement=0;
			for (int i=0; i<vsc.size(); i++)
			{
				if (i!=firstelement)
				{
					SingleCluster testcl=vsc.get(i);
					Vector<Double> testdist=testcl.getdistances();
					HashSet<String> testnam=testcl.getnames();
					HashSet<String> testclnam=testcl.getclustername();
					isunit=iscontained(refnam, testnam);
					if (!isunit)
					{
						double actualdistance=getrefdistance(testdist);
						if (associatewithminimum)
						{
							if (actualdistance<maxdist)
							{
								newfirstelement=pointerelement;
								maxdist=actualdistance;
							}
						}
						else
						{
							if (actualdistance>maxdist)
							{
								newfirstelement=pointerelement;
								maxdist=actualdistance;
							}
						}
						tempvsc.add(new SingleCluster(testclnam, testnam, testdist));
						pointerelement++;
					}
					else
					{
						HashSet<String> ttestnam=new HashSet<String>();
						Iterator<String> itna=testnam.iterator();
						while(itna.hasNext())
						{
							ttestnam.add(itna.next());
						}
						itna=refnam.iterator();
						while(itna.hasNext())
						{
							ttestnam.add(itna.next());
						}
						boolean checkd=isconsidered(tempvsc, ttestnam);
						if (!checkd)
						{
							Vector<Double> ttestdist=new Vector<Double>();
							String defcname=getremainingname(refcnam, testclnam);
							itna=refcnam.iterator();
							while(itna.hasNext())
							{
								String refdn=itna.next();
								Vector<Double> resdist=getdistances(defcname, refdn, vsc);
								for (int k=0; k<resdist.size(); k++)
								{
									ttestdist.add(resdist.get(k));
								}
							}
							double actualdistance=getrefdistance(ttestdist);
							if (associatewithminimum)
							{
								if (actualdistance<maxdist)
								{
									newfirstelement=pointerelement;
									maxdist=actualdistance;
								}
							}
							else
							{
								if (actualdistance>maxdist)
								{
									newfirstelement=pointerelement;
									maxdist=actualdistance;
								}
							}
							pointerelement++;
							HashSet<String> newcnames=new HashSet<String>();
							newcnames.add(defcname);
							newcnames.add(tempres[4]);
							tempvsc.add(new SingleCluster(newcnames, ttestnam, ttestdist));
						}
					}
				}
			}
			vsc.clear();
			vsc=null;
			vsc=new Vector<SingleCluster>();
			firstelement=newfirstelement;
			for (int i=0; i<tempvsc.size(); i++)
			{
				SingleCluster tsct=tempvsc.get(i);
				HashSet<String> ncn=tsct.getclustername();
				HashSet<String> nncn=new HashSet<String>();
				HashSet<String> ntestnam=tsct.getnames();
				Vector<Double> ntestdist=tsct.getdistances();
				Vector<Double> dtestdist=new Vector<Double>();
				HashSet<String> dtestnam=new HashSet<String>();
				for (int c=0; c<ntestdist.size(); c++)
				{
					dtestdist.add(ntestdist.get(c));
				}
				Iterator<String> itcn=ntestnam.iterator();
				while(itcn.hasNext())
				{
					String ta=itcn.next();
					dtestnam.add(ta);
				}
				itcn=ncn.iterator();
				while(itcn.hasNext())
				{
					String ta=itcn.next();
					nncn.add(ta);
				}
				vsc.add(new SingleCluster(nncn, dtestnam, dtestdist));
			}
			if (firstelement==-1) break;
		}
		String[] deftempres=new String[5];
		SingleCluster drefcl=vsc.get(0);
		HashSet<String> drefcnam=drefcl.getclustername();
		Iterator<String> ditn=drefcnam.iterator();
		int p=0;
		while(ditn.hasNext())
		{
			deftempres[p]=ditn.next();
			p++;
		}
		HashSet<String> drefnam=drefcl.getnames();
		deftempres[3]=String.valueOf(drefnam.size());
		deftempres[2]=String.valueOf(maxdist);
		deftempres[4]="CL"+String.valueOf(ielements);
		result.add(deftempres);
		return result;
	}
	/**
	*Used to find the real distance in the current cluster
	*/
	private double getrefdistance(Vector<Double> dist)
	{
		double refdistance=Double.NaN;
		if (joinmethod==1)
			refdistance=Double.MAX_VALUE;
		else if (joinmethod==2)
			refdistance=-1.7976931348623157E308;
		else
			refdistance=0;
		if (joinmethod==1)
		{
			for (int i=0; i<dist.size(); i++)
			{
				double tval=(dist.get(i)).doubleValue();
				if (tval<refdistance)
					refdistance=tval;
			}
		}
		else if (joinmethod==2)
		{
			for (int i=0; i<dist.size(); i++)
			{
				double tval=(dist.get(i)).doubleValue();
				if (tval>refdistance)
					refdistance=tval;
			}
		}
		else
		{
			for (int i=0; i<dist.size(); i++)
			{
				double tval=(dist.get(i)).doubleValue();
				refdistance=refdistance+tval;
			}
			refdistance=refdistance/dist.size();
		}
		return refdistance;
	}
	/**
	*Return true is some elements in base are contained in tosearch
	*/
	private boolean iscontained (HashSet<String> base, HashSet<String> tosearch)
	{
		boolean rescontained=false;
		Iterator<String> ibase=base.iterator();
		while(ibase.hasNext())
		{
			String tsa=ibase.next();
			if (tosearch.contains(tsa))
				rescontained=true;
		}
		return rescontained;
	}
	/**
	*Return true if the cluster is already done
	*/
	private boolean isconsidered (Vector<SingleCluster> tempvsc, HashSet<String> tosearch)
	{
		int numchecks=0;
		for (int i=0; i<tempvsc.size(); i++)
		{
			HashSet<String> examined=(tempvsc.get(i)).getnames();
			if (examined.size()!=tosearch.size())
				numchecks++;
			else
			{
				int tempchecks=0;
				Iterator<String> ibase=tosearch.iterator();
				while(ibase.hasNext())
				{
					String tsa=ibase.next();
					if (examined.contains(tsa))
						tempchecks++;
				}
				if (tempchecks!=tosearch.size())
					numchecks++;
			}
		}
		if (numchecks!=tempvsc.size())
			return true;
		else
			return false;
	}
	/**
	*Returns the remaining name that can be used
	*/
	private String getremainingname(HashSet<String> refcnam, HashSet<String> testclnam)
	{
		String tsa="";
		HashSet<String> ntestclnam=new HashSet<String>();
		Iterator<String> ibase=testclnam.iterator();
		while(ibase.hasNext())
		{
			tsa=ibase.next();
			ntestclnam.add(tsa);
		}
		ibase=refcnam.iterator();
		while(ibase.hasNext())
		{
			tsa=ibase.next();
			ntestclnam.remove(tsa);
		}
		ibase=ntestclnam.iterator();
		while(ibase.hasNext())
		{
			tsa=ibase.next();
		}
		return tsa;
	}
	/**
	*Returns the distances of all the unit that correspond to the cluster with the given references
	*/
	private Vector<Double> getdistances(String n1, String n2, Vector<SingleCluster> rsc)
	{
		Vector<Double> resultd=new Vector<Double>();
		for (int i=0; i<rsc.size(); i++)
		{
			SingleCluster rrsc=rsc.get(i);
			HashSet<String> testcn=rrsc.getclustername();
			int testiscluster=0;
			if (testcn.contains(n1))
				testiscluster++;
			if (testcn.contains(n2))
				testiscluster++;
			if (testiscluster==2)
			{
				return rrsc.getdistances();
			}
		}
		return resultd;
	}
}
