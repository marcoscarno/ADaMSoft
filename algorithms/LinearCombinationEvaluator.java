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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;

/**
* This method evaluates the linear combination
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class LinearCombinationEvaluator
{
	Hashtable<Vector<String>, Hashtable<Integer, Double>> coeffval;
	String[] alldsvars;
	int totaldictvars;
	int colp;
	int colname;
	/**
	*Initialise the main Objects by receiving the name of the dictionary that contains the value of the variables that will be used to evaluate the linear combination
	*/
	public LinearCombinationEvaluator (DictionaryReader dict)
	{
		colname=0;
		colp=0;
		coeffval=new Hashtable<Vector<String>, Hashtable<Integer, Double>>();
		totaldictvars=dict.gettotalvar();
		alldsvars=new String[totaldictvars];
		for (int i=0; i<totaldictvars; i++)
		{
			alldsvars[i]=dict.getvarname(i);
		}
	}
	/**
	*Receives the information about the position of the parameters (colp) and of the name of the variable (colname)
	*/
	public void setparametersinfo(int colp, int colname)
	{
		this.colp=colp;
		this.colname=colname;
	}
	/**
	*Adds the current parameter to the object that stores the parameters values
	*/
	public void addparameters(Vector<String> groupval, String[] values)
	{
		Hashtable<Integer, Double> actcoeff=coeffval.get(groupval);
		if (actcoeff==null)
			actcoeff=new Hashtable<Integer, Double>();
		double temppara=Double.NaN;
		try
		{
			temppara=Double.valueOf(values[colp]);
		}
		catch(Exception e) {}
		int rifvarname=0;
		for (int i=0; i<totaldictvars; i++)
		{
			if (values[colname].equalsIgnoreCase(alldsvars[i]))
			{
				rifvarname=i;
				break;
			}
		}
		actcoeff.put(new Integer(rifvarname), new Double(temppara));
		coeffval.put(groupval, actcoeff);
	}
	/**
	*Returns the linear combination of the parameters with the values
	*/
	public double evalcombination(Vector<String> groupval, String[] values)
	{
		double predictedval=Double.NaN;
		Hashtable<Integer, Double> actcoeff=coeffval.get(groupval);
		if (actcoeff!=null)
		{
			predictedval=0;
			for (Enumeration<Integer> e = actcoeff.keys() ; e.hasMoreElements() ;)
			{
				Integer ai=e.nextElement();
				int posref=ai.intValue();
				double coeffv=(actcoeff.get(ai)).doubleValue();
				try
				{
					double rv=Double.parseDouble(values[posref]);
					if (!Double.isNaN(rv))
						predictedval=predictedval+rv*coeffv;
					else
						predictedval=Double.NaN;
				}
				catch (Exception ex)
				{
					predictedval=Double.NaN;
				}
			}
		}
		return predictedval;
	}
}
