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

package ADaMSoft.algorithms.NLFitting;

import ADaMSoft.dataaccess.DictionaryReader;

import java.util.*;

/**
* This is used to minimise the given function
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ErrorMinimisator
{
    private int nParam=0;       		    // number of unknown parameters to be estimated
    private double[]paramValue = null;      // function parameter values (returned at function minimum)
    private double lastFunctValNoCnstrnt=0.0D;// Last function value with no constraint penalty
	private double minimum = 0.0D;          // value of the function to be minimised at the minimum
    private boolean convStatus = false;     // Status of minimisation on exiting minimisation method
                                		    // = true  -  convergence criterion was met
                                		    // = false -  convergence criterion not met - current estimates returned
    private int scaleOpt=0;     		    // if = 0; no scaling of initial estimates
                                		    //  if = 1; initial simplex estimates scaled to unity
                                		    //  if = 2; initial estimates scaled by user provided values in scale[]
                                		    //  (default = 0)
    private double[] scale = null;  	    // values to scale initial estimate (see scaleOpt above)
    private boolean penalty = false; 	    // true if single parameter penalty function is included
    private boolean sumPenalty = false; 	// true if multiple parameter penalty function is included
    private int nConstraints = 0; 		    // number of single parameter constraints
    private int nSumConstraints = 0; 		// number of multiple parameter constraints
    private Vector<Object> penalties = new Vector<Object>();// 3 method index,
                                                            // number of single parameter constraints,
                                                            // then repeated for each constraint:
                                                            //  penalty parameter index,
                                                            //  below or above constraint flag,
                                                            //  constraint boundary value
    private Vector<Object> sumPenalties = new Vector<Object>();// constraint method index,
                                                            // number of multiple parameter constraints,
                                                            // then repeated for each constraint:
                                                            //  number of parameters in summation
                                                            //  penalty parameter indices,
                                                            //  summation signs
                                                            //  below or above constraint flag,
                                                            //  constraint boundary value
    private int[] penaltyCheck = null;  	// = -1 values below the single constraint boundary not allowed
                                        	// = +1 values above the single constraint boundary not allowed
    private int[] sumPenaltyCheck = null;  	// = -1 values below the multiple constraint boundary not allowed
                                        	// = +1 values above the multiple constraint boundary not allowed
    private double penaltyWeight = 1.0e30;  // weight for the penalty functions
    private int[] penaltyParam = null;   	// indices of paramaters subject to single parameter constraint
    private int[][] sumPenaltyParam = null; // indices of paramaters subject to multiple parameter constraint
    private int[][] sumPlusOrMinus = null;  // sign before each parameter in multiple parameter summation
    private int[] sumPenaltyNumber = null;  // number of paramaters in each multiple parameter constraint

    private double[] constraints = null; 	// single parameter constraint values
    protected double constraintTolerance = 1e-4;    // tolerance in constraining parameter/s to a fixed value
    private double[] sumConstraints = null; // multiple parameter constraint values
    private int constraintMethod = 0;       // constraint method number
                                            //  =0: cliff to the power two (only method at present)
    private int nMax = 3000;    		    //  Nelder and Mead simplex maximum number of iterations
    private int nIter = 0;      		    //  Nelder and Mead simplex number of iterations performed
    private int konvge = 3;     		    //  Nelder and Mead simplex number of restarts allowed
    private int kRestart = 0;       	    //  Nelder and Mead simplex number of restarts taken
    private double fTol = 1e-8;     	    //  Nelder and Mead simplex convergence tolerance
    private double rCoeff = 1.0D;   	    //  Nelder and Mead simplex reflection coefficient
    private double eCoeff = 2.0D;   	    //  Nelder and Mead simplex extension coefficient
    private double cCoeff = 0.5D;   	    //  Nelder and Mead simplex contraction coefficient
    private double[] startH = null; 	    //  Nelder and Mead simplex initial estimates
    private double[] step = null;   	    //  Nelder and Mead simplex step values
    private int minTest = 0;    		    //  Nelder and Mead minimum test
                                		    //      = 0; tests simplex sd < fTol
                                		    //  allows options for further tests to be added later
    private double simplexSd = 0.0D;    	//  simplex standard deviation
    String message;
    DictionaryReader dict;
	Vector<Double> rmse;
	/**
	*This is the constructor; receive the info that the converge message must be added
	*/
    public ErrorMinimisator(DictionaryReader dict)
    {
		this.dict=dict;
		setRMSE();
	}
	/**
	*Nelder and Mead Simplex minimisation
	*/
    public void nelderMead(ErrorFunction g, double[] start, double[] step, double fTol, int nMax)
    {
		message="";
        int np = start.length;  // number of unknown parameters;
        this.nParam = np;
        this.convStatus = true;
        int nnp = np+1; // Number of simplex apices
        this.lastFunctValNoCnstrnt=0.0D;

        if(this.scaleOpt<2)this.scale = new double[np];

	    // set up arrays
	    this.paramValue = new double[np];
	    this.startH = new double[np];
	    this.step = new double[np];
	    double[]pmin = new double[np];   //Nelder and Mead Pmin

	    double[][] pp = new double[nnp][nnp];   //Nelder and Mead P
	    double[] yy = new double[nnp];          //Nelder and Mead y
	    double[] pbar = new double[nnp];        //Nelder and Mead P with bar superscript
	    double[] pstar = new double[nnp];       //Nelder and Mead P*
	    double[] p2star = new double[nnp];      //Nelder and Mead P**

        // Set any single parameter constraint parameters
        if(this.penalty)
        {
            Integer itemp = (Integer)this.penalties.get(1);
            this.nConstraints = itemp.intValue();
            this.penaltyParam = new int[this.nConstraints];
            this.penaltyCheck = new int[this.nConstraints];
            this.constraints = new double[this.nConstraints];
            Double dtemp = null;
            int j=2;
            for(int i=0;i<this.nConstraints;i++)
            {
                itemp = (Integer)this.penalties.get(j);
                this.penaltyParam[i] = itemp.intValue();
                j++;
                itemp = (Integer)this.penalties.get(j);
                this.penaltyCheck[i] = itemp.intValue();
                j++;
                dtemp = (Double)this.penalties.get(j);
                this.constraints[i] = dtemp.doubleValue();
                j++;
            }
        }

        // Set any multiple parameter constraint parameters
        if(this.sumPenalty)
        {
            Integer itemp = (Integer)this.sumPenalties.get(1);
            this.nSumConstraints = itemp.intValue();
            this.sumPenaltyParam = new int[this.nSumConstraints][];
            this.sumPlusOrMinus = new int[this.nSumConstraints][];
            this.sumPenaltyCheck = new int[this.nSumConstraints];
            this.sumPenaltyNumber = new int[this.nSumConstraints];
            this.sumConstraints = new double[this.nSumConstraints];
            int[] itempArray = null;
            Double dtemp = null;
            int j=2;
            for(int i=0;i<this.nSumConstraints;i++)
            {
                itemp = (Integer)this.sumPenalties.get(j);
                this.sumPenaltyNumber[i] = itemp.intValue();
                j++;
                itempArray = (int[])this.sumPenalties.get(j);
                this.sumPenaltyParam[i] = itempArray;
                j++;
                itempArray = (int[])this.sumPenalties.get(j);
                this.sumPlusOrMinus[i] = itempArray;
                j++;
                itemp = (Integer)this.sumPenalties.get(j);
                this.sumPenaltyCheck[i] = itemp.intValue();
                j++;
                dtemp = (Double)this.sumPenalties.get(j);
                this.sumConstraints[i] = dtemp.doubleValue();
                j++;
            }
        }

        // Store unscaled start values
        for(int i=0; i<np; i++)
        	this.startH[i]=start[i];

        // scale initial estimates and step sizes
        if(this.scaleOpt>0)
        {
            boolean testzero=false;
            for(int i=0; i<np; i++)
            	if(start[i]==0.0D) testzero=true;
            if(testzero)
            {
				message=message+"%1659%\n";
                this.scaleOpt=0;
            }
        }
        switch(this.scaleOpt)
        {
            case 0: for(int i=0; i<np; i++) scale[i]=1.0D;
                    	break;
            case 1: for(int i=0; i<np; i++)
            		{
                        scale[i]=1.0/start[i];
                        step[i]=step[i]/start[i];
                        start[i]=1.0D;
                    }
                    break;
            case 2: for(int i=0; i<np; i++)
            		{
                        step[i]*=scale[i];
                        start[i]*= scale[i];
                    }
                    break;
        }

        // set class member values
        this.fTol=fTol;
        this.nMax=nMax;
        this.nIter=0;
        for(int i=0; i<np; i++)
        {
            this.step[i]=step[i];
            this.scale[i]=scale[i];
        }

	    // initial simplex
	    double sho=0.0D;
	    for (int i=0; i<np; ++i)
	    {
 	        sho=start[i];
	 	    pstar[i]=sho;
		    p2star[i]=sho;
		    pmin[i]=sho;
	    }

	    int jcount=this.konvge;  // count of number of restarts still available

	    for (int i=0; i<np; ++i)
	    {
	        pp[i][nnp-1]=start[i];
	    }
	    yy[nnp-1]=this.functionValue(g, start);
	    for (int j=0; j<np; ++j)
	    {
		    start[j]=start[j]+step[j];
		    for (int i=0; i<np; ++i)
		    	pp[i][j]=start[i];
		    yy[j]=this.functionValue(g, start);
		    start[j]=start[j]-step[j];
	    }

	    // loop over allowed iterations
        double  ynewlo=0.0D;    // current value lowest y
	    double 	ystar = 0.0D;   // Nelder and Mead y*
	    double  y2star = 0.0D;  // Nelder and Mead y**
	    double  ylo = 0.0D;     // Nelder and Mead y(low)
	    // variables used in calculating the variance of the simplex at a putative minimum
	    double 	curMin = 00D, sumnm = 0.0D, summnm = 0.0D, zn = 0.0D;
	    int ilo=0;  // index of low apex
	    int ihi=0;  // index of high apex
	    int ln=0;   // counter for a check on low and high apices
	    boolean test = true;    // test becomes false on reaching minimum

	    while(test)
	    {
	        // Determine h
	        ylo=yy[0];
	        ynewlo=ylo;
    	    ilo=0;
	        ihi=0;
	        for (int i=1; i<nnp; ++i)
	        {
		        if (yy[i]<ylo)
		        {
			        ylo=yy[i];
			        ilo=i;
		        }
		        if (yy[i]>ynewlo)
		        {
			        ynewlo=yy[i];
			        ihi=i;
		        }
	        }
	        // Calculate pbar
	        for (int i=0; i<np; ++i)
	        {
		        zn=0.0D;
		        for (int j=0; j<nnp; ++j)
		        {
			        zn += pp[i][j];
		        }
		        zn -= pp[i][ihi];
		        pbar[i] = zn/np;
	        }

	        // Calculate p=(1+alpha).pbar-alpha.ph {Reflection}
	        for (int i=0; i<np; ++i)
	        	pstar[i]=(1.0 + this.rCoeff)*pbar[i]-this.rCoeff*pp[i][ihi];

	        // Calculate y*
	        ystar=this.functionValue(g, pstar);
	        rmse.add(ystar);

	        ++this.nIter;

	        // check for y*<yi
	        if(ystar < ylo)
	        {
                // Form p**=(1+gamma).p*-gamma.pbar {Extension}
	            for (int i=0; i<np; ++i)
	            	p2star[i]=pstar[i]*(1.0D + this.eCoeff)-this.eCoeff*pbar[i];
	            // Calculate y**
	            y2star=this.functionValue(g, p2star);
	            ++this.nIter;
		        rmse.add(y2star);
                if(y2star < ylo)
                {
                    // Replace ph by p**
		            for (int i=0; i<np; ++i)
		            	pp[i][ihi] = p2star[i];
	                yy[ihi] = y2star;
	            }
	            else
	            {
	                //Replace ph by p*
	                for (int i=0; i<np; ++i)
	                	pp[i][ihi]=pstar[i];
	                yy[ihi]=ystar;
	            }
	        }
	        else
	        {
	            // Check y*>yi, i!=h
		        ln=0;
	            for (int i=0; i<nnp; ++i)
	            	if (i!=ihi && ystar > yy[i]) ++ln;
	            if (ln==np )
	            {
	                // y*>= all yi; Check if y*>yh
                    if(ystar<=yy[ihi])
                    {
                        // Replace ph by p*
	                    for (int i=0; i<np; ++i)
	                    	pp[i][ihi]=pstar[i];
	                    yy[ihi]=ystar;
	                }
	                // Calculate p** =beta.ph+(1-beta)pbar  {Contraction}
	                for (int i=0; i<np; ++i)
	                	p2star[i]=this.cCoeff*pp[i][ihi] + (1.0 - this.cCoeff)*pbar[i];
	                // Calculate y**
	                y2star=this.functionValue(g, p2star);
	                ++this.nIter;
			        rmse.add(y2star);
	                // Check if y**>yh
	                if(y2star>yy[ihi])
	                {
	                    //Replace all pi by (pi+pl)/2
	                    for (int j=0; j<nnp; ++j)
	                    {
		                    for (int i=0; i<np; ++i)
		                    {
			                    pp[i][j]=0.5*(pp[i][j] + pp[i][ilo]);
			                    pmin[i]=pp[i][j];
		                    }
		                    yy[j]=this.functionValue(g, pmin);
					        rmse.add(yy[j]);
	                    }
	                    this.nIter += nnp;
	                }
	                else
	                {
	                    // Replace ph by p**
		                for (int i=0; i<np; ++i)
		                	pp[i][ihi] = p2star[i];
	                    yy[ihi] = y2star;
	                }
	            }
	            else
	            {
	                // replace ph by p*
	                for (int i=0; i<np; ++i)
	                	pp[i][ihi]=pstar[i];
	                yy[ihi]=ystar;
	            }
	        }

            // test for convergence
            // calculte sd of simplex and minimum point
            sumnm=0.0;
	        ynewlo=yy[0];
	        ilo=0;
	        for (int i=0; i<nnp; ++i)
	        {
	            sumnm += yy[i];
	            if(ynewlo>yy[i])
	            {
	                ynewlo=yy[i];
	                ilo=i;
	            }
	        }
	        sumnm /= (double)(nnp);
	        summnm=0.0;
	        for (int i=0; i<nnp; ++i)
	        {
		        zn=yy[i]-sumnm;
	            summnm += zn*zn;
	        }
	        curMin=Math.sqrt(summnm/np);

	        // test simplex sd
	        switch(this.minTest)
	        {
	            case 0:
                    if(curMin<fTol)
                    	test=false;
                    break;
			}
            this.minimum=ynewlo;
	        if(!test)
	        {
	            // store parameter values
	            for (int i=0; i<np; ++i)
	            	pmin[i]=pp[i][ilo];
	            yy[nnp-1]=ynewlo;
	            // store simplex sd
	            this.simplexSd = curMin;
	            // test for restart
	            --jcount;
	            if(jcount>0)
	            {
	                test=true;
	   	            for (int j=0; j<np; ++j)
	   	            {
		                pmin[j]=pmin[j]+step[j];
		                for (int i=0; i<np; ++i)pp[i][j]=pmin[i];
		                yy[j]=this.functionValue(g, pmin);
		                pmin[j]=pmin[j]-step[j];
	                 }
	            }
	        }

	        if(test && this.nIter>this.nMax)
	        {
	            this.convStatus = false;
	            // store current estimates
	            for (int i=0; i<np; ++i)
	            	pmin[i]=pp[i][ilo];
	            yy[nnp-1]=ynewlo;
                test=false;
            }
        }

		for (int i=0; i<np; ++i)
		{
            pmin[i] = pp[i][ihi];
            paramValue[i] = pmin[i]/this.scale[i];
        }
    	this.minimum=ynewlo;
        rmse.add(ynewlo);
    	this.kRestart=this.konvge-jcount;

	}
	/**
	*Starts the algorithms
	*/
    public void nelderMead(ErrorFunction g, double[] start, double[] step, double fTol)
    {
        int nMaxx = this.nMax;
        this.nelderMead(g, start, step, fTol, nMaxx);
    }
    /**
    *Calculate the function value for minimisation
    */
	private double functionValue(ErrorFunction g, double[] x)
	{
	    double funcVal = -3.0D;
	    double[] param = new double[this.nParam];
	    // rescale
	    for(int i=0; i<this.nParam; i++)
	    	param[i]=x[i]/scale[i];

        // single parameter penalty functions
        double tempFunctVal = this.lastFunctValNoCnstrnt;
        boolean test=true;
        if(this.penalty)
        {
            int k=0;
            for(int i=0; i<this.nConstraints; i++)
            {
                k = this.penaltyParam[i];
				switch(penaltyCheck[i])
				{
					case -1: if(param[k]<constraints[i])
                    {
						funcVal = tempFunctVal + this.penaltyWeight*(constraints[i]-param[k])*(constraints[i]-param[k]);
						test=false;
					}
					break;
					case 0: if(param[k]<constraints[i]*(1.0-this.constraintTolerance))
					{
						funcVal = tempFunctVal + this.penaltyWeight*(constraints[i]*(1.0-this.constraintTolerance)-param[k])*(constraints[i]*(1.0-this.constraintTolerance)-param[k]);
						test=false;
					}
					if(param[k]>constraints[i]*(1.0+this.constraintTolerance))
					{
						funcVal = tempFunctVal + this.penaltyWeight*(param[k]-constraints[i]*(1.0+this.constraintTolerance))*(param[k]-constraints[i]*(1.0+this.constraintTolerance));
						test=false;
					}
					break;
					case 1:  if(param[k]>constraints[i])
					{
						funcVal = tempFunctVal + this.penaltyWeight*(param[k]-constraints[i])*(param[k]-constraints[i]);
						test=false;
						}
					break;
				}
            }
        }

        // multiple parameter penalty functions
        if(this.sumPenalty)
        {
            int kk = 0;
            int pSign = 0;
            double sumPenaltySum = 0.0D;
            for(int i=0; i<this.nSumConstraints; i++)
            {
                for(int j=0; j<this.sumPenaltyNumber[i]; j++)
                {
                    kk = this.sumPenaltyParam[i][j];
                    pSign = this.sumPlusOrMinus[i][j];
                    sumPenaltySum += param[kk]*pSign;
                }
				switch(this.sumPenaltyCheck[i])
				{
					case -1: if(sumPenaltySum<sumConstraints[i])
					{
						funcVal = tempFunctVal + this.penaltyWeight*(sumConstraints[i]-sumPenaltySum)*(sumConstraints[i]-sumPenaltySum);
						test=false;
					}
					break;
					case 0: if(sumPenaltySum<sumConstraints[i]*(1.0-this.constraintTolerance))
					{
						funcVal = tempFunctVal + this.penaltyWeight*(sumConstraints[i]*(1.0-this.constraintTolerance)-sumPenaltySum)*(sumConstraints[i]*(1.0-this.constraintTolerance)-sumPenaltySum);
						test=false;
					}
					if(sumPenaltySum>sumConstraints[i]*(1.0+this.constraintTolerance))
					{
						funcVal = tempFunctVal + this.penaltyWeight*(sumPenaltySum-sumConstraints[i]*(1.0+this.constraintTolerance))*(sumPenaltySum-sumConstraints[i]*(1.0+this.constraintTolerance));
						test=false;
					}
					break;
					case 1:  if(sumPenaltySum>sumConstraints[i])
					{
						funcVal = tempFunctVal + this.penaltyWeight*(sumPenaltySum-sumConstraints[i])*(sumPenaltySum-sumConstraints[i]);
						test=false;
					}
					break;
				}
            }
        }

        if(test)
        {
            funcVal = g.evaluate(param, dict);
            this.lastFunctValNoCnstrnt = funcVal;
        }
	    return funcVal;
	}
	// Reset the tolerance used in a fixed value constraint
	public void setConstraintTolerance(double tolerance){
	    this.constraintTolerance = tolerance;
	}

	// add a single parameter constraint boundary for the minimisation
	public void addConstraint(int paramIndex, int conDir, double constraint)
	{
	    this.penalty=true;
        // First element reserved for method number if other methods than 'cliff' are added later
		if(this.penalties.isEmpty())this.penalties.add(new Integer(this.constraintMethod));

		// add constraint
	    if(penalties.size()==1)
	    {
		    this.penalties.add(new Integer(1));
		}
		else
		{
		    int nPC = ((Integer)this.penalties.get(1)).intValue();
            nPC++;
            this.penalties.set(1, new Integer(nPC));
		}
		this.penalties.add(new Integer(paramIndex));
 	    this.penalties.add(new Integer(conDir));
 	    this.penalties.add(new Double(constraint));
 	}

    // add a multiple parameter constraint boundary for the minimisation
	public void addConstraint(int[] paramIndices, int[] plusOrMinus, int conDir, double constraint)
	{
	    int nCon = paramIndices.length;
	    int nPorM = plusOrMinus.length;
	    if(nCon!=nPorM)throw new IllegalArgumentException("num of parameters, " + nCon + ", does not equal number of parameter signs, " + nPorM);
	    this.sumPenalty=true;
        // First element reserved for method number if other methods than 'cliff' are added later
		if(this.sumPenalties.isEmpty())
			this.sumPenalties.addElement(new Integer(this.constraintMethod));

		// add constraint
		if(sumPenalties.size()==1)
		{
		    this.sumPenalties.add(new Integer(1));
		}
		else
		{
		    int nPC = ((Integer)this.sumPenalties.get(1)).intValue();
            nPC++;
            this.sumPenalties.set(1, new Integer(nPC));
		}
		this.sumPenalties.add(new Integer(nCon));
		this.sumPenalties.add(paramIndices);
		this.sumPenalties.add(plusOrMinus);
 	    this.sumPenalties.add(new Integer(conDir));
 	    this.sumPenalties.add(new Double(constraint));
 	}

 	// Set constraint method
 	public void setConstraintMethod(int conMeth)
 	{
 	    this.constraintMethod = conMeth;
 	    if(!this.penalties.isEmpty())this.penalties.set(0, new Integer(this.constraintMethod));
 	}

	// remove all constraint boundaries for the minimisation
	public void removeConstraints()
	{
	    // check if single parameter constraints already set
	    if(!this.penalties.isEmpty())
	    {
		    int m=this.penalties.size();
		    // remove single parameter constraints
    		for(int i=m-1; i>=0; i--)
    		{
		        this.penalties.remove(i);
		    }
		}
		this.penalty = false;
		this.nConstraints = 0;

	    // check if mutiple parameter constraints already set
	    if(!this.sumPenalties.isEmpty())
	    {
		    int m=this.sumPenalties.size();
		    // remove multiple parameter constraints
    		for(int i=m-1; i>=0; i--)
    		{
		        this.sumPenalties.remove(i);
		    }
		}
		this.sumPenalty = false;
		this.nSumConstraints = 0;
	}
	public String getMessage()
	{
		message=message+"%1662%\n";

        if(!this.convStatus)
        {
			message=message+"%1663%\n";
        }
		message=message+"%1669%: "+this.nParam+"\n";
		message=message+"%1670%: "+this.nIter+"\n";
		message=message+"%1671%: "+this.nMax+"\n";
		message=message+"%1672%: "+this.kRestart+"\n";
		message=message+"%1673%: "+this.konvge+"\n";
		message=message+"%1674%: "+this.simplexSd+"\n";
		message=message+"%1675%: "+this.fTol+"\n";
        return message;
	}
    /**
    * true if convergence was achieved
    * false if convergence not achieved before maximum number of iterations
    */
    public boolean getConvStatus()
    {
        return this.convStatus;
    }
    // Reset scaling factors (scaleOpt 0 and 1, see below for scaleOpt 2)
    public void setScale(int n)
    {
        if(n<0 || n>1)throw new IllegalArgumentException("The argument must be 0 (no scaling) 1(initial estimates all scaled to unity) or the array of scaling factors");
        this.scaleOpt=n;
    }
    // Reset scaling factors (scaleOpt 2, see above for scaleOpt 0 and 1)
    public void setScale(double[] sc)
    {
        this.scale=sc;
        this.scaleOpt=2;
    }

    // Get scaling factors
    public double[] getScale()
    {
        return this.scale;
    }

	// Reset the minimisation convergence test option
	public void setMinTest(int n)
	{
	    if(n<0 || n>1)throw new IllegalArgumentException("minTest must be 0 or 1");
	    this.minTest=n;
	}

    // Get the minimisation convergence test option
	public int getMinTest()
	{
	    return this.minTest;
	}

	// Get the simplex sd at the minimum
	public double getSimplexSd()
	{
	    return this.simplexSd;
	}

	// Get the values of the parameters at the minimum
	public double[] getParamValues()
	{
	    return this.paramValue;
	}

	// Get the function value at minimum
	public double getMinimum()
	{
	    return this.minimum;
	}

	// Get the number of iterations in Nelder and Mead
	public int getNiter()
	{
	    return this.nIter;
	}


	// Set the maximum number of iterations allowed in Nelder and Mead
	public void setNmax(int nmax)
	{
	    this.nMax = nmax;
	}

	// Get the maximum number of iterations allowed in Nelder and Mead
	public int getNmax()
	{
	    return this.nMax;
	}

	// Get the number of restarts in Nelder and Mead
	public int getNrestarts()
	{
	    return this.kRestart;
	}

    // Set the maximum number of restarts allowed in Nelder and Mead
	public void setNrestartsMax(int nrs)
	{
	    this.konvge = nrs;
	}

	// Get the maximum number of restarts allowed in Nelder amd Mead
	public int getNrestartsMax()
	{
	    return this.konvge;
	}

	// Reset the Nelder and Mead reflection coefficient [alpha]
	public void setNMreflect(double refl)
	{
	    this.rCoeff = refl;
	}

	// Get the Nelder and Mead reflection coefficient [alpha]
	public double getNMreflect()
	{
	    return this.rCoeff;
	}

    // Reset the Nelder and Mead extension coefficient [beta]
	public void setNMextend(double ext)
	{
	    this.eCoeff = ext;
	}
	// Get the Nelder and Mead extension coefficient [beta]
	public double getNMextend()
	{
	    return this.eCoeff;
	}

	// Reset the Nelder and Mead contraction coefficient [gamma]
	public void setNMcontract(double con)
	{
	    this.cCoeff = con;
	}

	// Get the Nelder and Mead contraction coefficient [gamma]
	public double getNMcontract()
	{
	    return cCoeff;
	}

	// Set the minimisation tolerance
	public void setTolerance(double tol)
	{
	    this.fTol = tol;
	}
	// Get the minimisation tolerance
	public double getTolerance()
	{
	    return this.fTol;
	}
	public String getmessage()
	{
	    return this.message;
	}
	/**
	*Reset the RMSE
	*/
	public void setRMSE()
	{
		if (rmse!=null)
		{
			rmse.clear();
			rmse=null;
		}
		rmse=new Vector<Double>();
	}
	/**
	*Returns the rmse
	*/
	public Vector<Double> getRMSE()
	{
		return rmse;
	}
}