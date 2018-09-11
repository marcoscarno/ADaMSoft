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

/**
* This implements the expectation maximixation algorithm used inside the record linkage algorithm
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class EmAlgorithm
{
	int MAX_ITERATIONS, actual_iteration, numrecord, numvariables;
	double epsilon, sumrecord, p, delta, gmprod, guprod, gmprodnofreq, guprodnofreq, mproduct, uproduct;
	double[] m;
	double[] u;
	double[] frequencies;
	double[] gm;
	double[] gu;
	double[][] patterns;
	boolean errorparam;
	/**
	*Initialize the algorithm and receives the data
	*/
	public EmAlgorithm(double epsilon, int MAX_ITERATIONS)
	{
		errorparam=false;
		p=0.5;
		this.MAX_ITERATIONS=MAX_ITERATIONS;
		this.epsilon = epsilon;
		delta=1;
	}
	/**
	*Initialize the algorithm
	*/
	public void initialize(double[][] patterns, double[] frequencies)
	{
		errorparam=false;
		actual_iteration=0;
		this.frequencies = frequencies;
		this.numrecord = frequencies.length;
		this.numvariables = patterns[0].length;
		this.patterns=patterns;
		sumrecord=0;
		for (int i = 0; i < numrecord; i++)
		{
			sumrecord += frequencies[i];
		}
		m = new double[numvariables];
		u = new double[numvariables];
		for (int i=0; i<numvariables; i++)
		{
			m[i]=0.9;
			u[i]=0.125;
		}
		gm = new double[numrecord];
		gu = new double[numrecord];
		delta=1;
	}
	/**
	*This is the expectation step
	*/
	private void expectation()
	{
		for (int patCount = 0; patCount < numrecord; patCount++)
		{
			mproduct = 1.0;
			uproduct = 1.0;
			for (int varCount = 0; varCount < numvariables; varCount++)
			{
				mproduct = mproduct * (Math.pow(m[varCount], patterns[patCount][varCount]) * Math.pow(1.0 - m[varCount], 1 - patterns[patCount][varCount]));
				uproduct = uproduct * (Math.pow(u[varCount], patterns[patCount][varCount]) * Math.pow(1.0 - u[varCount], 1 - patterns[patCount][varCount]));
			}
			gm[patCount] = (p * mproduct / (p * mproduct + (1.0 - p) * uproduct));
			gu[patCount] = ((1.0 - p) * uproduct / (p * mproduct + (1.0 - p) * uproduct));
		}
	}
	/**
	*This is the maximization step
	*/
	private void maximization()
	{
		gmprodnofreq = 0.0;
		guprodnofreq = 0.0;
		for (int patCount = 0; patCount < numrecord; patCount++)
		{
			gmprodnofreq += gm[patCount] * frequencies[patCount];
			guprodnofreq += gu[patCount] * frequencies[patCount];
		}
		delta = 0.0;
		for (int varCount = 0; varCount < numvariables; varCount++)
		{
			gmprod = 0.0;
			guprod = 0.0;
			for (int patCount = 0; patCount < numrecord; patCount++)
			{
				gmprod += gm[patCount] * patterns[patCount][varCount] * frequencies[patCount];
				guprod += gu[patCount] * patterns[patCount][varCount] * frequencies[patCount];
			}
			delta += Math.abs(m[varCount] - (gmprod / gmprodnofreq)) + Math.abs(u[varCount] - (guprod / guprodnofreq));
			m[varCount] = (gmprod / gmprodnofreq);
			u[varCount] = (guprod / guprodnofreq);
			if (Double.isNaN(m[varCount])) errorparam=true;
			if (Double.isNaN(u[varCount])) errorparam=true;
			if (Double.isInfinite(m[varCount])) errorparam=true;
			if (Double.isInfinite(u[varCount])) errorparam=true;
		}
		p = (gmprodnofreq / sumrecord);
	}
	/**
	/Returns true in case of errors in the estimation of the parameters
	*/
	public boolean getstate()
	{
		return errorparam;
	}
	/**
	*This starts the evaluation of M and U
	*/
	public boolean calculate()
	{
		actual_iteration = 0;
		do
		{
			expectation();
			maximization();
			actual_iteration += 1;
			if (errorparam) actual_iteration=MAX_ITERATIONS;
		}while ((delta > epsilon) && (actual_iteration < MAX_ITERATIONS));
		if (!errorparam)
		{
			for (int i=0; i<m.length; i++)
			{
				if (m[i]==1.0) m[i]=0.9999999;
				if (u[i]==1.0) u[i]=0.9999999;
				if (m[i]==0.0) m[i]=0.0000001;
				if (u[i]==0.0) u[i]=0.0000001;
			}
		}
		return true;
	}
	/**
	*Returns the M array
	*/
	public double[] getMArray()
	{
		return m;
	}
	/**
	*Returns the U array
	*/
	public double[] getUArray()
	{
		return u;
	}
	/**
	*Returns the iteration done
	*/
	public int getIterations()
	{
		return this.actual_iteration;
	}
	/**
	*Returns the coefficient for the score when the value is 1
	*/
	public double[] getWA()
	{
		double[] wa=new double[m.length];
		for (int i=0; i<m.length; i++)
		{
			wa[i]=Math.log(m[i]/u[i]);
		}
		return wa;
	}
	/**
	*Returns the coefficient for the score when the value is 0
	*/
	public double[] getWD()
	{
		double[] wd=new double[m.length];
		for (int i=0; i<m.length; i++)
		{
			wd[i]=Math.log((1-m[i])/(1-u[i]));
		}
		return wd;
	}
}