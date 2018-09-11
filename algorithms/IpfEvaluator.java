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

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
* This method evaluates the weights for each observation using the theoretical frequencies
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class IpfEvaluator
{
	Hashtable<Vector<String>, Hashtable<String, Double>> real;
	Hashtable<String, Hashtable<String, Double>> theo;
	double meanerror;
	String notexistentval;
	/**
	*Initialise the main Objects, that will contains the means, the number of valid cases, the sum of squares, etc.
	*/
	public IpfEvaluator (Hashtable<String, Hashtable<String, Double>> theo)
	{
		notexistentval="";
		this.theo=theo;
	}
	/**
	*Evaluates the new weights
	*/
	public boolean evaluator (Hashtable<Vector<String>, Hashtable<String, Double>> freq, String currentvar, boolean nocheckvalues)
	{
		if (real!=null)
			real.clear();
		meanerror=0;
		real=new Hashtable<Vector<String>, Hashtable<String, Double>>();
		int nvt=0;
		for (Enumeration<Vector<String>> e = freq.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> vt=e.nextElement();
			nvt++;
			Hashtable<String, Double> fre=freq.get(vt);
			Hashtable<String, Double> the=theo.get(currentvar.toLowerCase());
			Hashtable<String, Double> res=new Hashtable<String, Double>();
			for (Enumeration<String> et = fre.keys() ; et.hasMoreElements() ;)
			{
				String fr=et.nextElement();
				double f=(fre.get(fr)).doubleValue();
				if (the.get(fr)!=null)
				{
					double th=(the.get(fr)).doubleValue();
					double rea=th/f;
					meanerror=meanerror+Math.abs(th-f);
					res.put(fr, new Double(rea));
				}
				else if (!nocheckvalues)
				{
					notexistentval=fr;
					return false;
				}
			}
			real.put(vt, res);
		}
		meanerror=meanerror/nvt;
		return true;
	}
	/**
	*Gives back the weights
	*/
	public Hashtable<Vector<String>, Hashtable<String, Double>> getfactor()
	{
		return real;
	}
	/**
	*Gives back the mean error
	*/
	public double getmeanerror()
	{
		return meanerror;
	}
	public String getnotexistentval()
	{
		return notexistentval;
	}
}
