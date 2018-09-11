/**
* Copyright (c) 2015 MS
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

package ADaMSoft.utilities;

/**
* This class find the coefficient and the variable name from a string that express a linear relation
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/ public class VarCoeff
{
	double coeff;
	String varname;
	String scoeff;
	String error="";
	/**
	*Starts the class
	*/
	public VarCoeff()
	{
		coeff=1;
		varname="";
		scoeff="1";
		error="";
	}
	/**
	*Receives the string that contains the coefficient and the variable name separated by the multiply char
	*/
	public void setpart(String varc)
	{
		coeff=1;
		varname="";
		scoeff="1";
		error="";
		if (varc.indexOf("*")>0)
		{
			String[] varcoeff=varc.split("\\*");
			if (varcoeff.length>2)
			{
				error="%2447% ("+varc+")\n";
			}
			else
			{
				try
				{
					double tc=Double.parseDouble(varcoeff[0]);
					try
					{
						double ttc=Double.parseDouble(varcoeff[1]);
						tc=tc*ttc;
						coeff=tc;
						scoeff=String.valueOf(coeff);
					}
					catch (Exception ee)
					{
						varname=varcoeff[1];
						coeff=tc;
						scoeff=String.valueOf(coeff);
					}
				}
				catch (Exception e)
				{
					try
					{
						double ttc=Double.parseDouble(varcoeff[1]);
						coeff=ttc;
						scoeff=String.valueOf(coeff);
						varname=varcoeff[0];
					}
					catch (Exception ee)
					{
						error="%2447% ("+varc+")\n";
					}
				}
			}
		}
		else
		{
			try
			{
				double tc=Double.parseDouble(varc);
				coeff=tc;
				scoeff=String.valueOf(coeff);
			}
			catch (Exception e)
			{
				varname=varc;
			}
		}
	}
	/**
	*Returns the error text if different from an empty string
	*/
	public String geterror()
	{
		return error;
	}
	/**
	*Return the variable name
	*/
	public String getvarname()
	{
		return varname;
	}
	/**
	*Return the coefficient as a string
	*/
	public String getscoeff()
	{
		return scoeff;
	}
	/**
	*Return the coefficient as a double
	*/
	public double getcoeff()
	{
		return coeff;
	}
}
