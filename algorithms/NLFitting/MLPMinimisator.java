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

package ADaMSoft.algorithms.NLFitting;

import ADaMSoft.dataaccess.DictionaryReader;

import java.util.*;

/**
* This is used to minimise the given function for a MLP network
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class MLPMinimisator
{
    private double[]paramValue = null;      // function parameter values (returned at function minimum)
	private double minimum = 0.0D;          // value of the function to be minimised at the minimum
    private boolean convStatus = false;     // Status of minimisation on exiting minimisation method
                                		    // = true  -  convergence criterion was met
                                		    // = false -  convergence criterion not met - current estimates returned
    private int nMax = 3000;    		    //  Nelder and Mead simplex maximum number of iterations
    private int nIter = 0;      		    //  Nelder and Mead simplex number of iterations performed
    private int konvge = 3;     		    //  Nelder and Mead simplex number of restarts allowed
    private double rCoeff = 1.0D;   	    //  Nelder and Mead simplex reflection coefficient
    private double eCoeff = 2.0D;   	    //  Nelder and Mead simplex extension coefficient
    private double cCoeff = 0.5D;   	    //  Nelder and Mead simplex contraction coefficient
    private int minTest = 0;    		    //  Nelder and Mead minimum test
                                		    //      = 0; tests simplex sd < fTol
                                		    //  allows options for further tests to be added later
    private double simplexSd = 0.0D;    	//  simplex standard deviation
    DictionaryReader dict;
    DictionaryReader dicttest;
	Vector<Double> rmse;
	Vector<Double> rmsetest;
	boolean testcondition;
	ErrorFunction testg;
	ErrorFunction g;
	int numtimesSup;
	int convergencestatus;
	boolean learnstarted;
	double lastRMSEonTest;
	int ntimestest;
	int np;
	/**
	*This is the constructor; receive the info that the converge message must be added
	*/
    public MLPMinimisator(DictionaryReader dict, DictionaryReader dicttest)
    {
		convergencestatus=0;
		this.dict=dict;
		if (dicttest!=null)
			this.dicttest=dicttest;
		setRMSE();
		setRMSEtest();
		testcondition=false;
		testg=null;
		numtimesSup=0;
		lastRMSEonTest=Double.MAX_VALUE;
		ntimestest=100;
	}
	/**
	*Sets the Error Function used for the test set
	*/
	public void settestEF(ErrorFunction testg)
	{
		this.testg=testg;
	}
	/**
	*Nelder and Mead Simplex minimisation
	*/
    public void nelderMead(ErrorFunction g, double[] start, double fTol, int nMax)
    {
		this.g=g;
		learnstarted=false;
        np = start.length;  // number of unknown parameters;
        this.convStatus = true;
        int nnp = np+1; // Number of simplex apices

	    // set up arrays
	    this.paramValue = new double[np];
	    double[] step = new double[np];
	    double[]pmin = new double[np];   //Nelder and Mead Pmin

	    double[][] pp = new double[nnp][nnp];   //Nelder and Mead P
	    double[] yy = new double[nnp];          //Nelder and Mead y
	    double[] pbar = new double[nnp];        //Nelder and Mead P with bar superscript
	    double[] pstar = new double[nnp];       //Nelder and Mead P*
	    double[] p2star = new double[nnp];      //Nelder and Mead P**

        this.nMax=nMax;
        this.nIter=0;
        for(int i=0; i<np; i++)
        {
            step[i]=0.5;
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
	        pp[i][np]=start[i];
	    }
	    yy[np]=this.functionValue(start);
	    for (int j=0; j<np; ++j)
	    {
		    start[j]=start[j]+step[j];
		    for (int i=0; i<np; ++i)
		    	pp[i][j]=start[i];
		    yy[j]=this.functionValue(start);
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

	    while((test) && (!testcondition))
	    {
			learnstarted=true;
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
	        ystar=this.functionValue(pstar);
	        rmse.add(ystar);
            if (dicttest!=null)
            	rmsetest.add(functionValueTest(pstar));

	        ++this.nIter;

	        // check for y*<yi
	        if(ystar < ylo)
	        {
                // Form p**=(1+gamma).p*-gamma.pbar {Extension}
	            for (int i=0; i<np; ++i)
	            	p2star[i]=pstar[i]*(1.0D + this.eCoeff)-this.eCoeff*pbar[i];
	            // Calculate y**
	            y2star=this.functionValue(p2star);
	            ++this.nIter;
		        rmse.add(y2star);
	            if (dicttest!=null)
	            	rmsetest.add(functionValueTest(p2star));
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
	                y2star=this.functionValue(p2star);
	                ++this.nIter;
			        rmse.add(y2star);
		            if (dicttest!=null)
		            	rmsetest.add(functionValueTest(p2star));
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
		                    yy[j]=this.functionValue(pmin);
					        rmse.add(yy[j]);
				            if (dicttest!=null)
				            	rmsetest.add(functionValueTest(pmin));
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
		                yy[j]=this.functionValue(pmin);
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
		if (convergencestatus!=1)
		{
			for (int i=0; i<np; ++i)
			{
        	    pmin[i] = pp[i][ihi];
        	    paramValue[i] = pmin[i];
        	}
        }
    	this.minimum=ynewlo;

	}
    /**
    *Calculate the function value for minimisation
    */
	private double functionValue(double[] x)
	{
		double funcVal = g.evaluate(x, dict);
		if ((Double.isNaN(funcVal)) || (Double.isInfinite(funcVal)))
		{
			convergencestatus=-1;
			testcondition=true;
		}
	    return funcVal;
	}
	/**
	*Sets the number of times the rmse can be greater in the test set before to stop the weight learning
	*/
	public void setntimestest(int ntimestest)
	{
		this.ntimestest=ntimestest;
	}
    /**
    *Calculate the function value for minimisation
    */
	private double functionValueTest(double[] x)
	{
		if ((dicttest!=null) && (testg!=null) && (learnstarted))
        {
           	double actrmseontest=testg.evaluate(x, dicttest);
           	if (actrmseontest>lastRMSEonTest)
           	{
				numtimesSup++;
				if (numtimesSup>ntimestest)
				{
					convergencestatus=1;
					testcondition=true;
				}
				for (int i=0; i<np; i++)
				{
		            paramValue[i] = x[i];
		        }
			}
			else
			{
				lastRMSEonTest=actrmseontest;
				numtimesSup=0;
			}
			return actrmseontest;
		}
	    return Double.NaN;
	}
    /**
    * true if convergence was achieved
    * false if convergence not achieved before maximum number of iterations
    */
    public boolean getConvStatus()
    {
        return this.convStatus;
    }
    /**
    *Returns the simplex sd at minimum
    */
	public double getSimplexSd()
	{
	    return this.simplexSd;
	}
    /**
    *Returns the parameters value
    */
	public double[] getParamValues()
	{
	    return this.paramValue;
	}
	/**
	*Returns the function value at minimum
	*/
	public double getMinimum()
	{
	    return this.minimum;
	}
	/**
	* Reset the Nelder and Mead reflection coefficient [alpha]
	*/
	public void setNMreflect(double refl)
	{
	    this.rCoeff = refl;
	}
	/**
	* Reset the Nelder and Mead extension coefficient [beta]
	*/
	public void setNMextend(double ext)
	{
	    this.eCoeff = ext;
	}
	/**
	*Reset the Nelder and Mead contraction coefficient [gamma]
	*/
	public void setNMcontract(double con)
	{
	    this.cCoeff = con;
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
	/**
	*Reset the RMSE for the test set
	*/
	public void setRMSEtest()
	{
		if (rmsetest!=null)
		{
			rmsetest.clear();
			rmsetest=null;
		}
		rmsetest=new Vector<Double>();
	}
	/**
	*Returns the rmse for the test set
	*/
	public Vector<Double> getRMSEtest()
	{
		return rmsetest;
	}
	/**
	*Returns the convergence status
	*/
	public int getconvergencestatus()
	{
		return convergencestatus;
	}
}