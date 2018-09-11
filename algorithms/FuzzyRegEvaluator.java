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

import ADaMSoft.dataaccess.GroupedMatrix2Dfile;

import ADaMSoft.algorithms.Algebra2DFile.Algebra;

import java.util.Vector;
import java.util.Hashtable;

/**
* This method evaluates the parameters value for a fuzzy linear regression model
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class FuzzyRegEvaluator
{
	int niter;
	int ncoeff;
	GroupedMatrix2Dfile x;
	GroupedMatrix2Dfile m;
	GroupedMatrix2Dfile l;
	GroupedMatrix2Dfile r;
	GroupedMatrix2Dfile lam;
	GroupedMatrix2Dfile rho;
	VarGroupModalities vgm;
	Hashtable<Vector<String>, double[]> coefficient;
	Hashtable<Vector<String>, Vector<Double>> history;

	Hashtable<Vector<String>, Double> bv;
	Hashtable<Vector<String>, Double> dv;
	Hashtable<Vector<String>, Double> gv;
	Hashtable<Vector<String>, Double> hv;

	GroupedMatrix2Dfile a;
	String tempdir;
	String errorMsg;
	boolean isError;
	double accuracy;
	/**
	*Initialise the main Objects
	*/
	public FuzzyRegEvaluator (int niter, int ncoeff, String tempdir, double accuracy)
	{
		this.niter=niter;
		this.ncoeff=ncoeff;
		this.tempdir=tempdir;
		this.accuracy=accuracy;
		coefficient=new Hashtable<Vector<String>, double[]>();
		history=new Hashtable<Vector<String>, Vector<Double>>();

		bv=new Hashtable<Vector<String>, Double>();
		dv=new Hashtable<Vector<String>, Double>();
		gv=new Hashtable<Vector<String>, Double>();
		hv=new Hashtable<Vector<String>, Double>();

		errorMsg="";
		isError=false;
	}
	/**
	*Receives the matrices of the values
	*/
	public void setparam(VarGroupModalities vgm, GroupedMatrix2Dfile x, GroupedMatrix2Dfile m, GroupedMatrix2Dfile l, GroupedMatrix2Dfile r, GroupedMatrix2Dfile lam, GroupedMatrix2Dfile rho)
	{
		this.vgm=vgm;
		this.x=x;
		this.m=m;
		this.l=l;
		this.r=r;
		this.lam=lam;
		this.rho=rho;
		int numgroup=vgm.getTotal();
		if (numgroup==0)
			numgroup=1;
		a=new GroupedMatrix2Dfile(tempdir, 1);
		for (int g=0; g<numgroup; g++)
		{
			Vector<String> tempgroup=vgm.getvectormodalities(g);
			for (int b=0; b<ncoeff; b++)
			{
				double[] values=new double[1];
				values[0]=Math.random()-0.5;
				a.write(tempgroup, values);
			}
			bv.put(tempgroup, new Double(Math.random()-0.5));
			dv.put(tempgroup, new Double(Math.random()-0.5));
			gv.put(tempgroup, new Double(Math.random()-0.5));
			hv.put(tempgroup, new Double(Math.random()-0.5));
		}
	}
	/**
	*Starts the procedure
	*/
	public void evaluate()
	{
		Algebra ag=new Algebra(tempdir, vgm);
		for (int i=0; i<niter; i++)
		{
			GroupedMatrix2Dfile xa=ag.mult(x, a, 1); //OK
			if (xa==null)
			{
				isError=true;
				break;
			}
			GroupedMatrix2Dfile msubxa=ag.sub(m, xa); //OK
			if (msubxa==null)
			{
				isError=true;
				xa.closeAll();
				break;
			}
			GroupedMatrix2Dfile p1=ag.ATAmult(msubxa, 1); //OK
			if (p1==null)
			{
				isError=true;
				xa.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile laml=ag.multtv(lam, l, 1); //OK
			if (laml==null)
			{
				isError=true;
				xa.closeAll();
				p1.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile msublaml=ag.sub(m, laml); //OK
			if (msublaml==null)
			{
				isError=true;
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile xabsumd=ag.multadd(x, a, bv, dv); //OK
			if (xabsumd==null)
			{
				isError=true;
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile lamxabsumd=ag.multtv(lam, xabsumd, 1); //OK
			if (lamxabsumd==null)
			{
				isError=true;
				xabsumd.closeAll();
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile xasublamxabsumd=ag.sub(xa, lamxabsumd); //OK
			if (xasublamxabsumd==null)
			{
				isError=true;
				lamxabsumd.closeAll();
				xabsumd.closeAll();
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile msublamlsubxasublamxabsumd=ag.sub(msublaml, xasublamxabsumd); //OK
			if (msublamlsubxasublamxabsumd==null)
			{
				isError=true;
				xasublamxabsumd.closeAll();
				lamxabsumd.closeAll();
				xabsumd.closeAll();
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile p2=ag.ATAmult(msublamlsubxasublamxabsumd, 1); //OK
			if (p2==null)
			{
				isError=true;
				msublamlsubxasublamxabsumd.closeAll();
				xasublamxabsumd.closeAll();
				lamxabsumd.closeAll();
				xabsumd.closeAll();
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile rhor=ag.multtv(rho, r, 1); //OK
			if (rhor==null)
			{
				isError=true;
				p2.closeAll();
				msublamlsubxasublamxabsumd.closeAll();
				xasublamxabsumd.closeAll();
				lamxabsumd.closeAll();
				xabsumd.closeAll();
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile msumrhor=ag.sum(m, rhor); //OK
			if (msumrhor==null)
			{
				isError=true;
				rhor.closeAll();
				p2.closeAll();
				msublamlsubxasublamxabsumd.closeAll();
				xasublamxabsumd.closeAll();
				lamxabsumd.closeAll();
				xabsumd.closeAll();
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile xagsumh=ag.multadd(x, a, gv, hv); //OK
			if (xagsumh==null)
			{
				isError=true;
				msumrhor.closeAll();
				rhor.closeAll();
				p2.closeAll();
				msublamlsubxasublamxabsumd.closeAll();
				xasublamxabsumd.closeAll();
				lamxabsumd.closeAll();
				xabsumd.closeAll();
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile rhoxagsumh=ag.multtv(rho, xagsumh, 1); //OK
			if (rhoxagsumh==null)
			{
				isError=true;
				xagsumh.closeAll();
				msumrhor.closeAll();
				rhor.closeAll();
				p2.closeAll();
				msublamlsubxasublamxabsumd.closeAll();
				xasublamxabsumd.closeAll();
				lamxabsumd.closeAll();
				xabsumd.closeAll();
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile xasumrhoxagsumh=ag.sum(xa, rhoxagsumh); //OK
			if (xasumrhoxagsumh==null)
			{
				isError=true;
				rhoxagsumh.closeAll();
				xagsumh.closeAll();
				msumrhor.closeAll();
				rhor.closeAll();
				p2.closeAll();
				msublamlsubxasublamxabsumd.closeAll();
				xasublamxabsumd.closeAll();
				lamxabsumd.closeAll();
				xabsumd.closeAll();
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile msumrhorsubxasumrhoxagsumh=ag.sub(msumrhor, xasumrhoxagsumh); //OK
			if (msumrhorsubxasumrhoxagsumh==null)
			{
				isError=true;
				xasumrhoxagsumh.closeAll();
				rhoxagsumh.closeAll();
				xagsumh.closeAll();
				msumrhor.closeAll();
				rhor.closeAll();
				p2.closeAll();
				msublamlsubxasublamxabsumd.closeAll();
				xasublamxabsumd.closeAll();
				lamxabsumd.closeAll();
				xabsumd.closeAll();
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}
			GroupedMatrix2Dfile p3=ag.ATAmult(msumrhorsubxasumrhoxagsumh, 1); //OK
			if (p3==null)
			{
				isError=true;
				msumrhorsubxasumrhoxagsumh.closeAll();
				xasumrhoxagsumh.closeAll();
				rhoxagsumh.closeAll();
				xagsumh.closeAll();
				msumrhor.closeAll();
				rhor.closeAll();
				p2.closeAll();
				msublamlsubxasublamxabsumd.closeAll();
				xasublamxabsumd.closeAll();
				lamxabsumd.closeAll();
				xabsumd.closeAll();
				msublaml.closeAll();
				xa.closeAll();
				p1.closeAll();
				laml.closeAll();
				msubxa.closeAll();
				break;
			}

			boolean converged=true;
			int numgroup=vgm.getTotal();
			if (numgroup==0)
				numgroup=1;
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				double func=p1.read(tempgroup,0,0)+p2.read(tempgroup,0,0)+p3.read(tempgroup,0,0);
				Vector<Double> temphist=history.get(tempgroup);
				if (temphist==null)
				{
					temphist=new Vector<Double>();
					converged=false;
				}
				else
				{
					int totalit=temphist.size();
					double lastfunc=(temphist.get(totalit-1)).doubleValue();
					if (Math.abs(lastfunc-func)>accuracy)
						converged=false;
				}
				temphist.add(new Double(func));
				history.put(tempgroup, temphist);
			}

			xa.closeAll();
			msubxa.closeAll();
			p1.closeAll();
			laml.closeAll();
			msublaml.closeAll();
			xabsumd.closeAll();
			lamxabsumd.closeAll();
			xasublamxabsumd.closeAll();
			msublamlsubxasublamxabsumd.closeAll();
			p2.closeAll();
			rhor.closeAll();
			msumrhor.closeAll();
			xagsumh.closeAll();
			rhoxagsumh.closeAll();
			xasumrhoxagsumh.closeAll();
			msumrhorsubxasumrhoxagsumh.closeAll();
			p3.closeAll();

			if (converged)
			{
				break;
			}

			/*Starting of evaluating the new coefficients*/

			GroupedMatrix2Dfile xt3x=ag.ATAmult(x, 3); //OK
			if (xt3x==null)
			{
				isError=true;
				break;
			}
			GroupedMatrix2Dfile xtlamxb=ag.ATVAmult(x, lam, bv, 2); //OK
			if (xtlamxb==null)
			{
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile xtlam2xb2=ag.ATVVAmult(x, lam, bv, 2); //OK
			if (xtlam2xb2==null)
			{
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile xtrhoxg=ag.ATVAmult(x, rho, gv, 2); //OK
			if (xtrhoxg==null)
			{
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile xtrho2xg2=ag.ATVVAmult(x, rho, gv, 2); //OK
			if (xtrho2xg2==null)
			{
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile c1=ag.sub(xt3x, xtlamxb); //OK
			if (c1==null)
			{
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile c2=ag.sum(c1, xtlam2xb2); //OK
			if (c2==null)
			{
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile c3=ag.sum(c2, xtrhoxg); //OK
			if (c3==null)
			{
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile c4=ag.sum(c3, xtrho2xg2); //OK
			if (c4==null)
			{
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile cc1=ag.inv(c4); //OK
			if (cc1==null)
			{
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile mbsumlsubd=ag.sumvmultcsubcost(m, l, bv, dv); //OK
			if (mbsumlsubd==null)
			{
				cc1.closeAll();
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile lammbsumlsubd=ag.multtv(lam, mbsumlsubd, 1); //OK
			if (lammbsumlsubd==null)
			{
				mbsumlsubd.closeAll();
				cc1.closeAll();
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile lbsubbd=ag.subvcost(l, bv, bv, dv); //OK
			if (lbsubbd==null)
			{
				lammbsumlsubd.closeAll();
				mbsumlsubd.closeAll();
				cc1.closeAll();
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile lam2lbsubbd=ag.multtvpow(lam, lbsubbd, 2); //OK
			if (lam2lbsubbd==null)
			{
				lbsubbd.closeAll();
				lammbsumlsubd.closeAll();
				mbsumlsubd.closeAll();
				cc1.closeAll();
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile mgsumrsubh=ag.sumvmultcsubcost(m, r, gv, hv); //OK
			if (mgsumrsubh==null)
			{
				lam2lbsubbd.closeAll();
				lbsubbd.closeAll();
				lammbsumlsubd.closeAll();
				mbsumlsubd.closeAll();
				cc1.closeAll();
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile rhomgsumrsubh=ag.multtv(rho, mgsumrsubh, 1); //OK
			if (rhomgsumrsubh==null)
			{
				mgsumrsubh.closeAll();
				lam2lbsubbd.closeAll();
				lbsubbd.closeAll();
				lammbsumlsubd.closeAll();
				mbsumlsubd.closeAll();
				cc1.closeAll();
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile rgsubgh=ag.subvcost(r, gv, gv, hv); //OK
			if (rgsubgh==null)
			{
				rhomgsumrsubh.closeAll();
				mgsumrsubh.closeAll();
				lam2lbsubbd.closeAll();
				lbsubbd.closeAll();
				lammbsumlsubd.closeAll();
				mbsumlsubd.closeAll();
				cc1.closeAll();
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile rho2rgsubgh=ag.multtvpow(rho, rgsubgh, 2); //OK
			if (rho2rgsubgh==null)
			{
				rgsubgh.closeAll();
				rhomgsumrsubh.closeAll();
				mgsumrsubh.closeAll();
				lam2lbsubbd.closeAll();
				lbsubbd.closeAll();
				lammbsumlsubd.closeAll();
				mbsumlsubd.closeAll();
				cc1.closeAll();
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile cc2=ag.sumfivev(m, lammbsumlsubd, lam2lbsubbd, rhomgsumrsubh, rho2rgsubgh, 3, -1, 1, 1, 1); //OK
			if (cc2==null)
			{
				rho2rgsubgh.closeAll();
				rgsubgh.closeAll();
				rhomgsumrsubh.closeAll();
				mgsumrsubh.closeAll();
				lam2lbsubbd.closeAll();
				lbsubbd.closeAll();
				lammbsumlsubd.closeAll();
				mbsumlsubd.closeAll();
				cc1.closeAll();
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile cc3=ag.ATVmult(x, cc2); //OK
			if (cc3==null)
			{
				cc2.closeAll();
				rho2rgsubgh.closeAll();
				rgsubgh.closeAll();
				rhomgsumrsubh.closeAll();
				mgsumrsubh.closeAll();
				lam2lbsubbd.closeAll();
				lbsubbd.closeAll();
				lammbsumlsubd.closeAll();
				mbsumlsubd.closeAll();
				cc1.closeAll();
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile coeff=ag.mult(cc1, cc3, 1); //OK
			if (coeff==null)
			{
				cc3.closeAll();
				cc2.closeAll();
				rho2rgsubgh.closeAll();
				rgsubgh.closeAll();
				rhomgsumrsubh.closeAll();
				mgsumrsubh.closeAll();
				lam2lbsubbd.closeAll();
				lbsubbd.closeAll();
				lammbsumlsubd.closeAll();
				mbsumlsubd.closeAll();
				cc1.closeAll();
				c4.closeAll();
				c3.closeAll();
				c2.closeAll();
				c1.closeAll();
				xtrho2xg2.closeAll();
				xtrhoxg.closeAll();
				xtlam2xb2.closeAll();
				xtlamxb.closeAll();
				xt3x.closeAll();
				isError=true;
				break;
			}

			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int p=coeff.getRows(tempgroup);
				for (int j=0; j<p; j++)
				{
					a.write(tempgroup, coeff.read(tempgroup, j, 0), j, 0);
				}
			}

			coeff.closeAll();
			cc3.closeAll();
			cc2.closeAll();
			rho2rgsubgh.closeAll();
			rgsubgh.closeAll();
			rhomgsumrsubh.closeAll();
			mgsumrsubh.closeAll();
			lam2lbsubbd.closeAll();
			lbsubbd.closeAll();
			lammbsumlsubd.closeAll();
			mbsumlsubd.closeAll();
			cc1.closeAll();
			c4.closeAll();
			c3.closeAll();
			c2.closeAll();
			c1.closeAll();
			xtrho2xg2.closeAll();
			xtrhoxg.closeAll();
			xtlam2xb2.closeAll();
			xtlamxb.closeAll();
			xt3x.closeAll();

			xa=ag.mult(x, a, 1);
			laml=ag.multtv(lam, l, 1);
			rhor=ag.multtv(rho, r, 1);

			/*Evaluating the new values for the bv*/
			GroupedMatrix2Dfile xtlam2x=ag.ATVAmult(xa, lam, 1, 2); //OK
			if (xtlam2x==null)
			{
				xa.closeAll();
				laml.closeAll();
				rhor.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile mxalamllamd=ag.sumfourv(m, xa, laml, lam, -1, 1 , 1, -1, null, null, null, dv); //OK
			if (mxalamllamd==null)
			{
				xtlam2x.closeAll();
				xa.closeAll();
				laml.closeAll();
				rhor.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile lammxalamllamd=ag.multtv(lam, mxalamllamd, 1); //OK
			if (lammxalamllamd==null)
			{
				mxalamllamd.closeAll();
				xtlam2x.closeAll();
				xa.closeAll();
				laml.closeAll();
				rhor.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile xalammxalamllamd=ag.multvtv(xa, lammxalamllamd); //OK
			if (xalammxalamllamd==null)
			{
				lammxalamllamd.closeAll();
				mxalamllamd.closeAll();
				xtlam2x.closeAll();
				xa.closeAll();
				laml.closeAll();
				rhor.closeAll();
				isError=true;
				break;
			}
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				double b1=xtlam2x.read(tempgroup, 0, 0);
				double b2=xalammxalamllamd.read(tempgroup, 0, 0);
				bv.put(tempgroup, new Double(b2/b1));
			}

			xalammxalamllamd.closeAll();
			lammxalamllamd.closeAll();
			mxalamllamd.closeAll();
			xtlam2x.closeAll();
			laml.closeAll();

			GroupedMatrix2Dfile xtrho2x=ag.ATVAmult(xa, rho, 1, 2); //OK
			if (xtrho2x==null)
			{
				xa.closeAll();
				rhor.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile mxarholrhoh=ag.sumfourv(m, xa, rhor, rho, 1, -1 , 1, -1, null, null, null, hv); //Ok
			if (mxarholrhoh==null)
			{
				xtrho2x.closeAll();
				xa.closeAll();
				rhor.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile rhomxarholrhoh=ag.multtv(rho, mxarholrhoh, 1); //OK
			if (rhomxarholrhoh==null)
			{
				mxarholrhoh.closeAll();
				xtrho2x.closeAll();
				xa.closeAll();
				rhor.closeAll();
				isError=true;
				break;
			}
			GroupedMatrix2Dfile xarhomxarholrhoh=ag.multvtv(xa, rhomxarholrhoh); //OK
			if (xarhomxarholrhoh==null)
			{
				rhomxarholrhoh.closeAll();
				mxarholrhoh.closeAll();
				xtrho2x.closeAll();
				xa.closeAll();
				rhor.closeAll();
				isError=true;
				break;
			}
			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				double b1=xtrho2x.read(tempgroup, 0, 0);
				double b2=xarhomxarholrhoh.read(tempgroup, 0, 0);
				gv.put(tempgroup, new Double(b2/b1));
			}

			rhomxarholrhoh.closeAll();
			mxarholrhoh.closeAll();
			xtrho2x.closeAll();
			xarhomxarholrhoh.closeAll();
			rhor.closeAll();

			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int p=lam.getRows(tempgroup);
				double v1=0;
				double v2=0;
				double v3=0;
				double v4=0;
				double v5=0;
				double b=(bv.get(tempgroup)).doubleValue();
				for (int j=0; j<p; j++)
				{
					double t1=lam.read(tempgroup, j, 0);
					v1+=t1*t1;
					double t2=l.read(tempgroup, j, 0);
					v2+=t1*t1*t2;
					double t3=xa.read(tempgroup, j, 0);
					v3+=t1*t1*t3*b;
					double t4=m.read(tempgroup, j, 0);
					v4+=t1*t4;
					v5+=t1*t3;
				}
				double dd=(v2-v3-v4+v5)/v1;
				dv.put(tempgroup, new Double(dd));
			}

			for (int g=0; g<numgroup; g++)
			{
				Vector<String> tempgroup=vgm.getvectormodalities(g);
				int p=rho.getRows(tempgroup);
				double v1=0;
				double v2=0;
				double v3=0;
				double v4=0;
				double v5=0;
				double gt=(gv.get(tempgroup)).doubleValue();
				for (int j=0; j<p; j++)
				{
					double t1=rho.read(tempgroup, j, 0);
					v1+=t1*t1;
					double t2=r.read(tempgroup, j, 0);
					v2+=t1*t1*t2;
					double t3=xa.read(tempgroup, j, 0);
					v3+=t1*t1*t3*gt;
					double t4=m.read(tempgroup, j, 0);
					v4+=t1*t4;
					v5+=t1*t3;
				}
				hv.put(tempgroup, new Double((v2-v3+v4-v5)/v1));
			}
			xa.closeAll();

			isError=ag.getState();
			if (isError)
			{
				errorMsg=ag.getMess();
				break;
			}
		}
		if (isError)
		{
			a.closeAll();
			errorMsg=ag.getMess();
		}
	}
	public Hashtable<Vector<String>, Vector<Double>> gethistory()
	{
		return history;
	}
	public Hashtable<Vector<String>, Double> getbv()
	{
		return bv;
	}
	public Hashtable<Vector<String>, Double> getdv()
	{
		return dv;
	}
	public Hashtable<Vector<String>, Double> getgv()
	{
		return gv;
	}
	public Hashtable<Vector<String>, Double> gethv()
	{
		return hv;
	}
	public Hashtable<Vector<String>, double[]> getcoeff()
	{
		int numgroup=vgm.getTotal();
		if (numgroup==0)
			numgroup=1;
		for (int g=0; g<numgroup; g++)
		{
			Vector<String> tempgroup=vgm.getvectormodalities(g);
			int p=a.getRows(tempgroup);
			double[] c=new double[p];
			for (int j=0; j<p; j++)
			{
				c[j]=a.read(tempgroup, j, 0);
			}
			coefficient.put(tempgroup, c);
		}
		a.closeAll();
		return coefficient;
	}
	public String getErrorMsg()
	{
		return errorMsg+"<br>";
	}
	public boolean getError()
	{
		return isError;
	}
}