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

import java.util.Vector;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.linalg.LinAlgOperations;
import ch.javasoft.math.operator.impl.BigFractionOperators;
import ch.javasoft.math.operator.impl.DoubleOperators;
import ch.javasoft.polco.PolyhedralCone;
import ch.javasoft.polco.config.PolcoConfig;
import ch.javasoft.polco.impl.DefaultInequalityCone;
import ch.javasoft.polco.impl.DefaultPolyhedralCone;
import ch.javasoft.polco.main.Polco;
import ch.javasoft.util.ExceptionUtil;
import ch.javasoft.xml.config.MissingReferableException;
import ch.javasoft.xml.config.XmlArgException;
import ch.javasoft.xml.config.XmlConfig;
import ch.javasoft.xml.config.XmlConfigException;

/**
* This implements the polco adapter to search the solutions
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class EditsSolver
{
	private final Polco polco;
	private PolcoSolutorAdapter<BigFraction, BigFraction[]> callback;
	private PolcoSolutorAdapterSimple<BigFraction, BigFraction[]> callbacksimple;
	int dimension;
	double[][] tempeq;
	double[][] tempiq;
	double[] recval;
	double tolerance;
	boolean is_simple=false;
	double zeroalg=0.00000000001;
	/**
	 * Creates a new polco adapter instance using default options. The XML
	 * configuration file is read and all configuration settings are
	 * initialized.
	 *
	 * @exception	XmlConfigException if the configuration is erroneous
	 */
	public EditsSolver() throws XmlConfigException
	{
		this(new Options());
	}
	/**
	 * Creates a new polco adapter instance using the specified options. The XML
	 * configuration file is read and all configuration settings are
	 * initialized.
	 *
	 * @exception	XmlConfigException if the configuration is erroneous
	 */
	public EditsSolver(Options options) throws XmlConfigException
	{
		try
		{
			try
			{
				final XmlConfig xmlConfig = PolcoConfig.resolveXmlConfig(options.toArgs());
				this.polco = new Polco(xmlConfig);
			}
			catch (XmlArgException ex)
			{
				throw ex;
			}
			catch (MissingReferableException ex)
			{
				throw ex;
			}
		}
		catch (Exception ex)
		{
			throw ExceptionUtil.toRuntimeExceptionOr(XmlConfigException.class, ex);
		}
	}
	/**
	*Receives the edits in order to verify the solutions
	*/
	public void setEditsToSolve(double[][] tempeq, double[][] tempiq, double[] recval, double tolerance)
	{
		this.tempeq=tempeq;
		this.tempiq=tempiq;
		this.recval=recval;
		this.tolerance=tolerance;
	}
	public void setzeroalg(double zeroalg)
	{
		this.zeroalg=zeroalg;
	}
	/**
	*Return true if all the rays where found
	*/
	public void getTotalSolutions(Vector<Double> weight, double[][] eq, double[][] iq)
	{
		is_simple=false;
		dimension=weight.size();
		final LinAlgOperations<Double, double[]> dblOps = DoubleOperators.DEFAULT.getLinAlgOperations();
		final LinAlgOperations<BigFraction, BigFraction[]> fraOps = BigFractionOperators.INSTANCE.getLinAlgOperations();
		final PolyhedralCone<Double, double[]> cone;
		if (eq == null || eq.length == 0)
		{
			cone = new DefaultInequalityCone<Double, double[]>(dblOps, iq);
		}
		else
		{
			cone = new DefaultPolyhedralCone<Double, double[]>(dblOps, eq, iq);
		}
		try
		{
			callback = new PolcoSolutorAdapter<BigFraction, BigFraction[]>(fraOps.getArrayOperations());
			callback.setWeights(weight);
			callback.setEdits(tempeq, tempiq, recval, tolerance);
			callback.setzeroalg(zeroalg);
			polco.call(cone, callback, fraOps);
			callback.yeld();
			return;
		}
		catch (java.lang.IndexOutOfBoundsException iobe)
		{
			return;
		}
		catch (Exception ecb)
		{
			return;
		}
	}
	/**
	*Return true if all the rays where found
	*/
	public void getTotalSolutions(double[][] eq, double[][] iq)
	{
		is_simple=true;
		final LinAlgOperations<Double, double[]> dblOps = DoubleOperators.DEFAULT.getLinAlgOperations();
		final LinAlgOperations<BigFraction, BigFraction[]> fraOps = BigFractionOperators.INSTANCE.getLinAlgOperations();
		final PolyhedralCone<Double, double[]> cone;
		if (eq == null || eq.length == 0)
		{
			cone = new DefaultInequalityCone<Double, double[]>(dblOps, iq);
		}
		else
		{
			cone = new DefaultPolyhedralCone<Double, double[]>(dblOps, eq, iq);
		}
		try
		{
			callbacksimple = new PolcoSolutorAdapterSimple<BigFraction, BigFraction[]>(fraOps.getArrayOperations());
			callbacksimple.setEdits(eq, iq);
			polco.call(cone, callbacksimple, fraOps);
			callbacksimple.yeld();
			return;
		}
		catch (java.lang.IndexOutOfBoundsException iobe)
		{
			return;
		}
		catch (Exception ecb)
		{
			return;
		}
	}
	/**
	*Return the number of solutions
	*/
	public int getnumsol()
	{
		if (!is_simple)
			return callback.getnumsol();
		else
			return callbacksimple.getnumsol();
	}
	/**
	*Return the current cardinality
	*/
	public double getCardinality()
	{
		return callback.getCardinality();
	}
	/**
	*Return the solutions
	*/
	public Vector<double[]> getSolutions()
	{
		if (!is_simple)
			return callback.getSolutions();
		else
			return callbacksimple.getSolutions();
	}
	/**
	*Clear memory
	*/
	public void cleanMem()
	{
		recval=new double[0];
		recval=null;
		if (!is_simple)
		{
			callback.cleanMem();
			callback=null;
		}
		else
		{
			callbacksimple.cleanMem();
			callbacksimple=null;
		}
	}
}
