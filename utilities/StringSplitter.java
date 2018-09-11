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
import ADaMSoft.keywords.Keywords;


/**
* This class is used to split a String
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class StringSplitter
{
	String separator;
	int parts;
	String[] values;
	String[] formats;
	String[] tv;
	Vector<String> ttv;
	Vector<String> tfv;
	String tev;
	String ttev;
	String oldsep;
	boolean foundend;
	String thousanddlm;
	String decimaldlm;
	double testisnum;
	int posapices;
	boolean avoidquotationmarks;
	/**
	*Initialize the objects
	*/
	public StringSplitter()
	{
		separator="\t";
		oldsep=separator;
		parts=0;
		ttv=new Vector<String>();
		tfv=new Vector<String>();
		thousanddlm="";
		decimaldlm="";
		posapices=0;
	}
	/**
	*Receive the optional info on the thousand separator
	*/
	public void setthousanddlm(String thousanddlm)
	{
		this.thousanddlm=thousanddlm;
	}
	/**
	*Receive the optional info on the decimal separator
	*/
	public void setdecimaldlm(String decimaldlm)
	{
		this.decimaldlm=decimaldlm;
	}
	/**
	*Sets the delimiter
	*/
	public void setdlm(String dlm)
	{
		separator=dlm;
		oldsep=separator;
		if (separator.equals("|"))
			separator="\\|";
		if (separator.equals("."))
			separator="\\.";
		if (separator.equals("*"))
			separator="\\*";
	}
	/*
	/Sets the type of interpretation of the quotation marks
	*/
	public void setQuotationMarks(boolean avoidquotationmarks)
	{
		this.avoidquotationmarks=avoidquotationmarks;
	}
	/**
	*Parse the text
	*/
	public void settext(String text)
	{
		try
		{
			text=text.replaceAll("\"\"","'");
		}
		catch (Exception e) {}
		ttv.clear();
		values=new String[0];
		parts=0;
		try
		{
			tv=text.split(separator,-1);
			for (int i=0; i<tv.length; i++)
			{
				tv[i]=tv[i].trim();
				if ( (tv[i].startsWith("\"")) && (!avoidquotationmarks))
				{
					foundend=false;
					if (tv[i].length()>1)
						tv[i]=tv[i].substring(1);
					else
						tv[i]="";
					posapices=-1;
					try
					{
						posapices=tv[i].indexOf("\"");
					}
					catch (Exception fap) {}
					if ((posapices>=0) && (posapices<tv[i].length()-1))
					{
						foundend=true;
						tfv.add(Keywords.TEXTSuffix);
						ttv.add("\""+tv[i]);
					}
					if (!foundend)
					{
						tev="";
						for (int j=i; j<tv.length; j++)
						{
							tev=tev+tv[j];
							if (i<tv.length-1)
								tev=tev+oldsep;
							posapices=-1;
							try
							{
								posapices=tv[j].indexOf("\"");
							}
							catch (Exception fap) {}
							if ((posapices>=0) && (posapices<tv[j].length()-1))
								break;
							if (tv[j].trim().endsWith("\""))
							{
								foundend=true;
								if (tev.length()>1)
									tev=tev.substring(0,tev.length()-2);
								else
									tev="";
								ttev=tev;
								try
								{
									if (!thousanddlm.equals(""))
										ttev=ttev.replace(thousanddlm,"");
								}
								catch (Exception e) {}
								try
								{
									if (!decimaldlm.equals(""))
									{
										if (!decimaldlm.equals("."))
											ttev=ttev.replace(decimaldlm,".");
									}
								}
								catch (Exception e) {}
								try
								{
									testisnum=Double.parseDouble(ttev);
									if (!Double.isNaN(testisnum))
									{
										if (!Double.isInfinite(testisnum))
											ttv.add(ttev);
										else
											ttv.add(tev);
									}
									else
										ttv.add(tev);
								}
								catch (Exception en)
								{
									ttv.add(tev);
								}
								i=j;
								break;
							}
						}
					}
					if (!foundend)
						ttv.add("\""+tv[i]);
				}
				else
				{
					tev=tv[i];
					try
					{
						if (!thousanddlm.equals(""))
							tev=tev.replace(thousanddlm,"");
					}
					catch (Exception e) {}
					try
					{
						if (!decimaldlm.equals(""))
						{
							if (!decimaldlm.equals("."))
								tev=tev.replace(decimaldlm,".");
						}
					}
					catch (Exception e) {}
					try
					{
						testisnum=Double.parseDouble(tev);
						if (!Double.isNaN(testisnum))
						{
							if (!Double.isInfinite(testisnum))
								ttv.add(tev);
							else
								ttv.add(tv[i]);
						}
						else
							ttv.add(tv[i]);
					}
					catch (Exception en)
					{
						ttv.add(tv[i]);
					}
				}
			}
			parts=ttv.size();
			values=new String[parts];
			for (int i=0; i<parts; i++)
			{
				values[i]=ttv.get(i);
			}
		}
		catch (Exception e) {}
	}
	/**
	*Parse the text and recognizes the writing formats
	*/
	public void settextwithformat(String text)
	{
		try
		{
			text=text.replaceAll("\"\"","'");
		}
		catch (Exception e) {}
		ttv.clear();
		tfv.clear();
		values=new String[0];
		parts=0;
		try
		{
			tv=text.split(separator,-1);
			for (int i=0; i<tv.length; i++)
			{
				tv[i]=tv[i].trim();
				if ( (tv[i].startsWith("\"")) && (!avoidquotationmarks))
				{
					foundend=false;
					if (tv[i].length()>1)
						tv[i]=tv[i].substring(1);
					else
						tv[i]="";
					posapices=-1;
					try
					{
						posapices=tv[i].indexOf("\"");
					}
					catch (Exception fap) {}
					if ((posapices>=0) && (posapices<tv[i].length()-1))
					{
						foundend=true;
						tfv.add(Keywords.TEXTSuffix);
						ttv.add("\""+tv[i]);
					}
					if (!foundend)
					{
						tev="";
						for (int j=i; j<tv.length; j++)
						{
							tev=tev+tv[j];
							if (i<tv.length-1)
								tev=tev+oldsep;
							posapices=-1;
							try
							{
								posapices=tv[j].indexOf("\"");
							}
							catch (Exception fap) {}
							if ((posapices>=0) && (posapices<tv[j].length()-1))
								break;
							if (tv[j].trim().endsWith("\""))
							{
								foundend=true;
								if (tev.length()>1)
									tev=tev.substring(0,tev.length()-2);
								else
									tev="";
								ttv.add(tev);
								ttev=tev;
								try
								{
									if (!thousanddlm.equals(""))
										ttev=ttev.replace(thousanddlm,"");
								}
								catch (Exception e) {}
								try
								{
									if (!decimaldlm.equals(""))
									{
										if (!decimaldlm.equals("."))
											ttev=ttev.replace(decimaldlm,".");
									}
								}
								catch (Exception e) {}
								ttev=ttev.trim();
								if (!ttev.equals(""))
								{
									try
									{
										Double.parseDouble(ttev);
										if ( (ttev.indexOf(".")<0) && (ttev.startsWith("0")) && (ttev.length()>1) )
											tfv.add(Keywords.TEXTSuffix);
										else if ( (ttev.indexOf(".")<0) && (ttev.startsWith("0")) && (ttev.length()==1) )
											tfv.add(Keywords.NUMSuffix+Keywords.INTSuffix);
										else if ( (ttev.indexOf(".")>1) && (ttev.startsWith("0")) )
											tfv.add(Keywords.TEXTSuffix);
										else if ((ttev.indexOf(".")<0) && (!ttev.startsWith("0")) )
											tfv.add(Keywords.NUMSuffix+Keywords.INTSuffix);
										else
											tfv.add(Keywords.NUMSuffix);
									}
									catch (Exception en)
									{
										tfv.add(Keywords.TEXTSuffix);
									}
								}
								else
									tfv.add(Keywords.NUMSuffix+Keywords.INTSuffix);
								i=j;
								break;
							}
						}
					}
					if (!foundend)
					{
						tfv.add(Keywords.TEXTSuffix);
						ttv.add("\""+tv[i]);
					}
				}
				else
				{
					tev=tv[i].trim();
					try
					{
						if (!thousanddlm.equals(""))
							tev=tev.replace(thousanddlm,"");
					}
					catch (Exception e) {}
					try
					{
						if (!decimaldlm.equals(""))
						{
							if (!decimaldlm.equals("."))
								tev=tev.replace(decimaldlm,".");
						}
					}
					catch (Exception e) {}
					if (!tev.equals(""))
					{
						try
						{
							Double.parseDouble(tev);
							if ( (tev.indexOf(".")<0) && (tev.startsWith("0")) && (tev.length()>1) )
								tfv.add(Keywords.TEXTSuffix);
							else if ( (tev.indexOf(".")<0) && (tev.startsWith("0")) && (tev.length()==1) )
								tfv.add(Keywords.NUMSuffix+Keywords.INTSuffix);
							else if ( (tev.indexOf(".")>1) && (tev.startsWith("0")) )
								tfv.add(Keywords.TEXTSuffix);
							else if ((tev.indexOf(".")<0) && (!tev.startsWith("0")) )
								tfv.add(Keywords.NUMSuffix+Keywords.INTSuffix);
							else
								tfv.add(Keywords.NUMSuffix);
						}
						catch (Exception en)
						{
							tfv.add(Keywords.TEXTSuffix);
						}
					}
					else
						tfv.add(Keywords.NUMSuffix+Keywords.INTSuffix);
					ttv.add(tv[i]);
				}
			}
			parts=ttv.size();
			values=new String[parts];
			for (int i=0; i<parts; i++)
			{
				values[i]=ttv.get(i);
			}
		}
		catch (Exception e) {}
	}
	/**
	*Returns the number of text parts
	*/
	public int getparts()
	{
		return parts;
	}
	/**
	*Return the ith value
	*/
	public String getval(int i)
	{
		return values[i];
	}
	/**
	*Returns the array of values
	*/
	public String[] getvals()
	{
		return values;
	}
	/**
	*Returns the vector of writing formats
	*/
	public Vector<String> getformat()
	{
		return tfv;
	}
	/**
	*Returns the ith writing format
	*/
	public String getformat(int i)
	{
		return tfv.get(i);
	}
}
