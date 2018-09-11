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

import java.util.Vector;

/**
* This class parse the values that are read from a table and gives back the var values, the grouping variables values and the weight variable value
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class ValuesParser
{
	int numvg;
	int numav;
	int numrv;
	int numcv;
	int numwv;
	int[] normalrule;
	int[] grouprule;
	int[] analysisrule;
	int[] rowrule;
	int[] colrule;
	int[] weightrule;
	boolean normaltype;
	String realvgval;
	double realnumvgval;
	/**
	* This is the first constructor of the methods that contains utilities on the variables.<p>
	* Receive several type of arrays of integer whose length is equal to the length of the array of values that will be read from the data table.<p>
	* The first array (normalrule) will contains the following rule:<p>
	* a 1 in position n indicates that a grouping variables will be found at position n;<p>
	* a 2 in position n indicates that an analysis variables will be found at position n;<p>
	* a 3 in position n indicates that a weighting variable will be found at position n;<p>
	* The second array (grouprule) will contains the following rule:<p>
	* a 1 in position n indicates that a grouping variables will be found at position n;<p>
	* The third array (analysisrule) will contains the following rule :<p>
	* a 1 in position n indicates that an analysis variables will be found at position n;<p>
	* The fourth array (rowrule) will contains the following rule :<p>
	* a 1 in position n indicates that a row variable will be found at position n;<p>
	* The fifth array (colrule) will contains the following rule :<p>
	* a 1 in position n indicates that a columns variable will be found at position n;<p>
	* The sixth array (weightrule) will contains the following rule :<p>
	* a 1 in position n indicates that a weighting variable will be found at position n;<p>
	*/
	public ValuesParser(int[] normalrule, int[] grouprule, int[] analysisrule, int[] rowrule, int[] colrule, int[] weightrule)
	{
		this.grouprule=grouprule;
		this.normalrule=normalrule;
		this.analysisrule=analysisrule;
		this.rowrule=rowrule;
		this.colrule=colrule;
		this.weightrule=weightrule;
		normaltype=false;
		numvg=0;
		numav=0;
		numrv=0;
		numcv=0;
		numwv=0;
		if (normalrule!=null)
		{
			normaltype=true;
			for (int i=0; i<normalrule.length; i++)
			{
				if (normalrule[i]==1)
					numvg++;
				if (normalrule[i]==2)
					numav++;
				if (normalrule[i]==3)
					numwv++;
			}
		}
		if (grouprule!=null)
		{
			for (int i=0; i<grouprule.length; i++)
			{
				if (grouprule[i]==1)
					numvg++;
			}
		}
		if (weightrule!=null)
		{
			for (int i=0; i<weightrule.length; i++)
			{
				if (weightrule[i]==1)
					numwv++;
			}
		}
		if (analysisrule!=null)
		{
			for (int i=0; i<analysisrule.length; i++)
			{
				if (analysisrule[i]==1)
					numav++;
			}
		}
		if (rowrule!=null)
		{
			for (int i=0; i<rowrule.length; i++)
			{
				if (rowrule[i]==1)
					numrv++;
			}
		}
		if (colrule!=null)
		{
			for (int i=0; i<colrule.length; i++)
			{
				if (colrule[i]==1)
					numcv++;
			}
		}
	}
	/**
	*Return a vector with each element equal to the modality of each grouping variables
	*/
	public Vector<String> getvargroup(String[][] values)
	{
		Vector<String> vargroup=new Vector<String>();
		if (numvg==0)
		{
			vargroup.add(null);
			return vargroup;
		}
		realnumvgval=Double.NaN;
		if (normaltype)
		{
			for (int i=0; i<normalrule.length; i++)
			{
				if (normalrule[i]==1)
				{
					realvgval=values[i][1].trim();
					try
					{
						realnumvgval=Double.parseDouble(realvgval);
						if (!Double.isNaN(realnumvgval))
							realvgval=String.valueOf(realnumvgval);
					}
					catch (Exception e) {}
					vargroup.add(realvgval);
				}
			}
			return vargroup;
		}
		else
		{
			for (int i=0; i<grouprule.length; i++)
			{
				if (grouprule[i]==1)
				{
					realvgval=values[i][1].trim();
					try
					{
						realnumvgval=Double.parseDouble(realvgval);
						if (!Double.isNaN(realnumvgval))
							realvgval=String.valueOf(realnumvgval);
					}
					catch (Exception e) {}
					vargroup.add(realvgval);
				}
			}
			return vargroup;
		}
	}

	/**
	*Return a vector with each element equal to the modality of each grouping variables
	*/
	public Vector<String> getvargroup(String[] values)
	{
		Vector<String> vargroup=new Vector<String>();
		if (numvg==0)
		{
			vargroup.add(null);
			return vargroup;
		}
		realnumvgval=Double.NaN;
		if (normaltype)
		{
			for (int i=0; i<normalrule.length; i++)
			{
				if (normalrule[i]==1)
				{
					realvgval=values[i].trim();
					try
					{
						realnumvgval=Double.parseDouble(realvgval);
						if (!Double.isNaN(realnumvgval))
							realvgval=String.valueOf(realnumvgval);
					}
					catch (Exception e) {}
					vargroup.add(realvgval);
				}
			}
			return vargroup;
		}
		else
		{
			for (int i=0; i<grouprule.length; i++)
			{
				if (grouprule[i]==1)
				{
					realvgval=values[i].trim();
					try
					{
						realnumvgval=Double.parseDouble(realvgval);
						if (!Double.isNaN(realnumvgval))
							realvgval=String.valueOf(realnumvgval);
					}
					catch (Exception e) {}
					vargroup.add(realvgval);
				}
			}
			return vargroup;
		}
	}
	/**
	*Return a vector with each element equal to the original different value of each grouping variables
	*/
	public Vector<String> getorigvargroup(String[] values)
	{
		Vector<String> vargroup=new Vector<String>();
		if (numvg==0)
		{
			vargroup.add(null);
			return vargroup;
		}
		if (normaltype)
		{
			for (int i=0; i<normalrule.length; i++)
			{
				if (normalrule[i]==1)
				{
					vargroup.add(values[i].trim());
				}
			}
			return vargroup;
		}
		else
		{
			for (int i=0; i<grouprule.length; i++)
			{
				if (grouprule[i]==1)
				{
					vargroup.add(values[i].trim());
				}
			}
			return vargroup;
		}
	}
	/**
	*Return a vector with each element equal to the original different value of each grouping variables
	*/
	public Vector<String> getorigvargroup(String[][] values)
	{
		Vector<String> vargroup=new Vector<String>();
		if (numvg==0)
		{
			vargroup.add(null);
			return vargroup;
		}
		if (normaltype)
		{
			for (int i=0; i<normalrule.length; i++)
			{
				if (normalrule[i]==1)
				{
					vargroup.add(values[i][1].trim());
				}
			}
			return vargroup;
		}
		else
		{
			for (int i=0; i<grouprule.length; i++)
			{
				if (grouprule[i]==1)
				{
					vargroup.add(values[i][1].trim());
				}
			}
			return vargroup;
		}
	}
	/**
	*Return true if the values of the grouping variables are all not missing, otherwise false
	*/
	public boolean vargroupisnotmissing(Vector<String> vargroup)
	{
		if (numvg==0)
			return true;
		else
		{
			boolean groupisnotmissing=true;
			for (int i=0; i<vargroup.size(); i++)
			{
				String temp=vargroup.get(i);
				if (temp.equals(""))
					groupisnotmissing=false;
			}
			return groupisnotmissing;
		}
	}
	/**
	*Return the values of the analysis variables in a string array
	*/
	public String[] getanalysisvar(String[] values)
	{
		String[] analysisvar=new String[numav];
		if (numav==0)
		{
			analysisvar=new String[1];
			analysisvar[0]="";
			return analysisvar;
		}
		if (normaltype)
		{
			int pointer=0;
			for (int i=0; i<normalrule.length; i++)
			{
				if (normalrule[i]==2)
				{
					analysisvar[pointer]=values[i].trim();
					pointer++;
				}
			}
			return analysisvar;
		}
		else
		{
			int pointer=0;
			for (int i=0; i<analysisrule.length; i++)
			{
				if (analysisrule[i]==1)
				{
					analysisvar[pointer]=values[i].trim();
					pointer++;
				}
			}
			return analysisvar;
		}
	}
	/**
	*Return the values of the analysis variables in a double array
	*/
	public double[] getanalysisvarasdouble(String[] values)
	{
		double[] analysisvar=new double[numav];
		if (numav==0)
		{
			return null;
		}
		if (normaltype)
		{
			int pointer=0;
			for (int i=0; i<normalrule.length; i++)
			{
				if (normalrule[i]==2)
				{
					analysisvar[pointer]=Double.NaN;
					if (values[i]!=null)
					{
						if (!values[i].equals(""))
						{
							try
							{
								analysisvar[pointer]=Double.parseDouble(values[i].trim());
							}
							catch (Exception nfe) {}
						}
					}
					pointer++;
				}
			}
			return analysisvar;
		}
		else
		{
			int pointer=0;
			for (int i=0; i<analysisrule.length; i++)
			{
				if (analysisrule[i]==1)
				{
					analysisvar[pointer]=Double.NaN;
					if (values[i]!=null)
					{
						if (!values[i].equals(""))
						{
							try
							{
								analysisvar[pointer]=Double.parseDouble(values[i].trim());
							}
							catch (Exception nfe) {}
						}
					}
					pointer++;
				}
			}
			return analysisvar;
		}
	}
	/**
	*Return the values of the weighting variables in a double
	*/
	public double getweight(String[] values)
	{
		double weight=Double.NaN;
		if (numwv==0)
			return 1;
		if (normaltype)
		{
			for (int i=0; i<normalrule.length; i++)
			{
				if ((normalrule[i]==3) && (!values[i].equals("")))
				{
					try
					{
						weight=Double.parseDouble(values[i].trim());
						return weight;
					}
					catch (Exception nfe)
					{
						return Double.NaN;
					}
				}
				if ((normalrule[i]==3) && (values[i].equals("")))
					return Double.NaN;
			}
		}
		else
		{
			for (int i=0; i<weightrule.length; i++)
			{
				if ((weightrule[i]==1) && (!values[i].equals("")))
				{
					try
					{
						weight=Double.parseDouble(values[i].trim());
						return weight;
					}
					catch (Exception nfe)
					{
						return Double.NaN;
					}
				}
				if ((weightrule[i]==1) && (values[i].equals("")))
					return Double.NaN;
			}
		}
		return Double.NaN;
	}
	/**
	*Return the values of the weighting variables in a double
	*/
	public double getweight(String[][] values)
	{
		double weight=Double.NaN;
		if (numwv==0)
			return 1;
		if (normaltype)
		{
			for (int i=0; i<normalrule.length; i++)
			{
				if ((normalrule[i]==3) && (!values[i][1].equals("")))
				{
					try
					{
						weight=Double.parseDouble(values[i][1].trim());
						return weight;
					}
					catch (Exception nfe)
					{
						return Double.NaN;
					}
				}
				if ((normalrule[i]==3) && (values[i][1].equals("")))
					return Double.NaN;
			}
		}
		else
		{
			for (int i=0; i<weightrule.length; i++)
			{
				if ((weightrule[i]==1) && (!values[i][1].equals("")))
				{
					try
					{
						weight=Double.parseDouble(values[i][1].trim());
						return weight;
					}
					catch (Exception nfe)
					{
						return Double.NaN;
					}
				}
				if ((weightrule[i]==1) && (values[i][1].equals("")))
					return Double.NaN;
			}
		}
		return Double.NaN;
	}
	/**
	*Return the values of the row variables in a string array
	*/
	public String[] getrowvar(String[] values)
	{
		String[] rowvar=new String[numrv];
		if (numrv==0)
		{
			rowvar=new String[1];
			rowvar[0]="";
			return rowvar;
		}
		int pointer=0;
		for (int i=0; i<rowrule.length; i++)
		{
			if (rowrule[i]==1)
			{
				rowvar[pointer]=values[i].trim();
				pointer++;
			}
		}
		return rowvar;
	}
	/**
	*Return the values of the row variables in a string array
	*/
	public String[][] getrowvar(String[][] values)
	{
		String[][] rowvar=new String[numrv][2];
		if (numrv==0)
		{
			rowvar=new String[1][2];
			rowvar[0][0]="";
			rowvar[0][1]="";
			return rowvar;
		}
		int pointer=0;
		for (int i=0; i<rowrule.length; i++)
		{
			if (rowrule[i]==1)
			{
				rowvar[pointer][0]=values[i][0].trim();
				rowvar[pointer][1]=values[i][1].trim();
				pointer++;
			}
		}
		return rowvar;
	}

	/**
	*Return the values of the row variables in a double array
	*/
	public double[] getrowvarasdouble(String[] values)
	{
		double[] rowvar=new double[numrv];
		if (numrv==0)
			return null;
		int pointer=0;
		for (int i=0; i<rowrule.length; i++)
		{
			if (rowrule[i]==1)
			{
				rowvar[pointer]=Double.NaN;
				if (!values[i].equals(""))
				{
					try
					{
						rowvar[pointer]=Double.parseDouble(values[i].trim());
					}
					catch (Exception nfe) {}
				}
				pointer++;
			}
		}
		return rowvar;
	}
	/**
	*Return the values of the column variables in a string array
	*/
	public String[] getcolvar(String[] values)
	{
		String[] colvar=new String[numcv];
		if (numcv==0)
		{
			colvar=new String[1];
			colvar[0]="";
			return colvar;
		}
		int pointer=0;
		for (int i=0; i<colrule.length; i++)
		{
			if (colrule[i]==1)
			{
				colvar[pointer]=values[i].trim();
				pointer++;
			}
		}
		return colvar;
	}
	/**
	*Return the values of the column variables in a string array
	*/
	public String[][] getcolvar(String[][] values)
	{
		String[][] colvar=new String[numcv][2];
		if (numcv==0)
		{
			colvar=new String[1][2];
			colvar[0][0]="";
			colvar[0][1]="";
			return colvar;
		}
		int pointer=0;
		for (int i=0; i<colrule.length; i++)
		{
			if (colrule[i]==1)
			{
				colvar[pointer][0]=values[i][0].trim();
				colvar[pointer][1]=values[i][1].trim();
				pointer++;
			}
		}
		return colvar;
	}	/**
	*Return the values of the column variables in a double array
	*/
	public double[] getcolvarasdouble(String[] values)
	{
		double[] colvar=new double[numcv];
		if (numcv==0)
			return null;
		int pointer=0;
		for (int i=0; i<colrule.length; i++)
		{
			if (colrule[i]==1)
			{
				colvar[pointer]=Double.NaN;
				if (!values[i].equals(""))
				{
					try
					{
						colvar[pointer]=Double.parseDouble(values[i].trim());
					}
					catch (Exception nfe) {}
				}
				pointer++;
			}
		}
		return colvar;
	}
}
