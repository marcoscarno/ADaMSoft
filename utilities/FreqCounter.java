/**
* Copyright (c) MS
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

import java.util.Iterator;
import java.util.Vector;
import java.util.TreeMap;

/**
* This method creates an object that can be used to count the different values of a variable inside a data step
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class FreqCounter
{
	TreeMap<Vector<String>, Double> freq;
	Vector<Vector<String>> tname;
	/**
	* Initialize the objects
	*/
	public FreqCounter ()
	{
		freq=new TreeMap<Vector<String>, Double>(new VectorStringComparator());
	}
	/**
	*Adds a single value
	*/
	public void addval(String tval)
	{
		if (tname!=null)
			tname=null;
		Vector<String> vtval=new Vector<String>();
		vtval.add(tval);
		if (!freq.containsKey(vtval))
		{
			freq.put(vtval, new Double(1));
		}
		else
		{
			double td=freq.get(vtval).doubleValue();
			freq.put(vtval, new Double(1+td));
		}
	}
	/**
	*Adds a single value and its frequency
	*/
	public void setval(String tval, double freqs)
	{
		if (!Double.isNaN(freqs))
		{
			if (tname!=null)
				tname=null;
			Vector<String> vtval=new Vector<String>();
			vtval.add(tval);
			freq.put(vtval, new Double(freqs));
		}
	}
	/**
	*Adds a single value and its frequency
	*/
	public void addval(String tval, double freqs)
	{
		if (!Double.isNaN(freqs))
		{
			if (tname!=null)
				tname=null;
			Vector<String> vtval=new Vector<String>();
			vtval.add(tval);
			if (!freq.containsKey(vtval))
			{
				freq.put(vtval, new Double(freqs));
			}
			else
			{
				double td=freq.get(vtval).doubleValue();
				freq.put(vtval, new Double(freqs+td));
			}
		}
	}
	/**
	*Adds an array of values
	*/
	public void addval(String[] tval)
	{
		if (tname!=null)
			tname=null;
		Vector<String> vtval=new Vector<String>();
		for (int i=0; i<tval.length; i++)
		{
			vtval.add(tval[i]);
		}
		if (!freq.containsKey(vtval))
		{
			freq.put(vtval, new Double(1));
		}
		else
		{
			double td=freq.get(vtval).doubleValue();
			freq.put(vtval, new Double(1+td));
		}
	}
	/**
	*Adds an array of values and its frequency
	*/
	public void setval(String[] tval, double freqs)
	{
		if (!Double.isNaN(freqs))
		{
			if (tname!=null)
				tname=null;
			Vector<String> vtval=new Vector<String>();
			for (int i=0; i<tval.length; i++)
			{
				vtval.add(tval[i]);
			}
			freq.put(vtval, new Double(freqs));
		}
	}
	/**
	*Adds an array of values and its frequency
	*/
	public void addval(String[] tval, double freqs)
	{
		if (!Double.isNaN(freqs))
		{
			if (tname!=null)
				tname=null;
			Vector<String> vtval=new Vector<String>();
			for (int i=0; i<tval.length; i++)
			{
				vtval.add(tval[i]);
			}
			if (!freq.containsKey(vtval))
			{
				freq.put(vtval, new Double(freqs));
			}
			else
			{
				double td=freq.get(vtval).doubleValue();
				freq.put(vtval, new Double(freqs+td));
			}
		}
	}
	/**
	*Adds an value as double
	*/
	public void addval(double tval)
	{
		if (!Double.isNaN(tval))
		{
			if (tname!=null)
				tname=null;
			Vector<String> vtval=new Vector<String>();
			vtval.add(String.valueOf(tval));
			if (!freq.containsKey(vtval))
			{
				freq.put(vtval, new Double(1));
			}
			else
			{
				double td=freq.get(vtval).doubleValue();
				freq.put(vtval, new Double(1+td));
			}
		}
	}
	/**
	*Adds an value as double and its frequency
	*/
	public void setval(double tval, double freqs)
	{
		if (!Double.isNaN(freqs))
		{
			if (tname!=null)
				tname=null;
			Vector<String> vtval=new Vector<String>();
			vtval.add(String.valueOf(tval));
			freq.put(vtval, new Double(freqs));
		}
	}
	/**
	*Adds an value as double and its frequency
	*/
	public void addval(double tval, double freqs)
	{
		if (!Double.isNaN(freqs))
		{
			if (tname!=null)
				tname=null;
			Vector<String> vtval=new Vector<String>();
			vtval.add(String.valueOf(tval));
			if (!freq.containsKey(vtval))
			{
				freq.put(vtval, new Double(freqs));
			}
			else
			{
				double td=freq.get(vtval).doubleValue();
				freq.put(vtval, new Double(freqs+td));
			}
		}
	}
	/**
	*Adds an array of values as double
	*/
	public void addval(double[] tval)
	{
		if (tname!=null)
			tname=null;
		Vector<String> vtval=new Vector<String>();
		for (int i=0; i<tval.length; i++)
		{
			vtval.add(String.valueOf(tval[i]));
		}
		if (!freq.containsKey(vtval))
		{
			freq.put(vtval, new Double(1));
		}
		else
		{
			double td=freq.get(vtval).doubleValue();
			freq.put(vtval, new Double(1+td));
		}
	}
	/**
	*Adds an array of values as double and their frequency
	*/
	public void setval(double[] tval, double freqs)
	{
		if (!Double.isNaN(freqs))
		{
			if (tname!=null)
				tname=null;
			Vector<String> vtval=new Vector<String>();
			for (int i=0; i<tval.length; i++)
			{
				vtval.add(String.valueOf(tval[i]));
			}
			freq.put(vtval, new Double(freqs));
		}
	}
	/**
	*Adds an array of values as double and their frequency
	*/
	public void addval(double[] tval, double freqs)
	{
		if (!Double.isNaN(freqs))
		{
			if (tname!=null)
				tname=null;
			Vector<String> vtval=new Vector<String>();
			for (int i=0; i<tval.length; i++)
			{
				vtval.add(String.valueOf(tval[i]));
			}
			if (!freq.containsKey(vtval))
			{
				freq.put(vtval, new Double(freqs));
			}
			else
			{
				double td=freq.get(vtval).doubleValue();
				freq.put(vtval, new Double(freqs+td));
			}
		}
	}
	/**
	*Return the number of different values
	*/
	public int getdiffvalues()
	{
		return freq.size();
	}
	/**
	*clear the content of the frequency counter
	*/
	public void clearfc()
	{
		if (tname!=null)
			tname=null;
		if (freq!=null)
			freq.clear();
	}
	/**
	*Return the frequency for the received values
	*/
	public double getfreqfor(String tval)
	{
		Vector<String> vtval=new Vector<String>();
		vtval.add(tval);
		if (!freq.containsKey(vtval))
			return 0;
		else
		{
			return freq.get(vtval).doubleValue();
		}
	}
	/**
	*Return the frequency for the received array of values
	*/
	public double getfreqfor(String[] tval)
	{
		Vector<String> vtval=new Vector<String>();
		for (int i=0; i<tval.length; i++)
		{
			vtval.add(tval[i]);
		}
		if (!freq.containsKey(vtval))
			return 0;
		else
		{
			return freq.get(vtval).doubleValue();
		}
	}
	/**
	*Return the frequency for the received double value
	*/
	public double getfreqfor(double tval)
	{
		Vector<String> vtval=new Vector<String>();
		vtval.add(String.valueOf(tval));
		if (!freq.containsKey(vtval))
			return 0;
		else
		{
			return freq.get(vtval).doubleValue();
		}
	}
	/**
	*Return the frequency for the received array of double value
	*/
	public double getfreqfor(double[] tval)
	{
		Vector<String> vtval=new Vector<String>();
		for (int i=0; i<tval.length; i++)
		{
			vtval.add(String.valueOf(tval[i]));
		}
		if (!freq.containsKey(vtval))
			return 0;
		else
		{
			return freq.get(vtval).doubleValue();
		}
	}
	/**
	*Return the name of the ith elements
	*/
	public String getnamefor(int rif)
	{
		if (tname==null)
		{
			tname=new Vector<Vector<String>>();
			Iterator<Vector<String>> it=freq.keySet().iterator();
			while(it.hasNext())
			{
				tname.add(it.next());
			}
		}
		String name="";
		Vector<String> result=tname.get(rif);
		for (int i=0; i<result.size(); i++)
		{
			name=name+result.get(i)+" ";
		}
		name=name.trim();
		return name;
	}
	/**
	*Return the array of names of the ith elements
	*/
	public String[] getnamesfor(int rif)
	{
		if (tname==null)
		{
			tname=new Vector<Vector<String>>();
			Iterator<Vector<String>> it=freq.keySet().iterator();
			while(it.hasNext())
			{
				tname.add(it.next());
			}
		}
		Vector<String> result=tname.get(rif);
		String[] name=new String[result.size()];
		for (int i=0; i<result.size(); i++)
		{
			name[i]=result.get(i);
		}
		return name;
	}
	/**
	*Return the ferquency for the ith elements
	*/
	public double getfreqat(int rif)
	{
		if (tname==null)
		{
			tname=new Vector<Vector<String>>();
			Iterator<Vector<String>> it=freq.keySet().iterator();
			while(it.hasNext())
			{
				tname.add(it.next());
			}
		}
		Vector<String> result=tname.get(rif);
		if (freq.get(result)==null)
			return 0;
		else
		{
			return freq.get(result).doubleValue();
		}
	}
}
