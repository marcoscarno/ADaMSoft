/**
* Copyright (c) 2016 MS
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
public class EditsSearcher
{
	private final Polco polco;
	private PolcoSolAdapter<BigFraction, BigFraction[]> callback;
	/**
	 * Creates a new polco adapter instance using default options. The XML
	 * configuration file is read and all configuration settings are
	 * initialized.
	 *
	 * @exception	XmlConfigException if the configuration is erroneous
	 */
	public EditsSearcher() throws XmlConfigException
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
	public EditsSearcher(Options options) throws XmlConfigException
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
	*Return true if all the rays where found
	*/
	public int getTotalSolutions(double[][] eq, double[][] iq)
	{
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
			callback = new PolcoSolAdapter<BigFraction, BigFraction[]>(fraOps.getArrayOperations());
			polco.call(cone, callback, fraOps);
			return callback.yeld();
		}
		catch (java.lang.IndexOutOfBoundsException iobe)
		{
			return 0;
		}
		catch (Exception ecb)
		{
			return 0;
		}
	}
}
