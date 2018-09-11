/**
* Copyright (c) 2015 ADaMSoft
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
import corejava.Format;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


import ADaMSoft.keywords.Keywords;

/**
* This method returns a text for the value according to the passed format correspondence
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class NewWriteFormat
{
	/**
	* Returns a text for the value according to the passed format and other options
	*/
	public static String[] getwriteformat(String[] record, Vector<String> writeformat, int defdec, boolean localrep)
	{
		if (writeformat==null)
			return record;
		if (record.length!=writeformat.size())
			return record;
		for(int i=0;i<record.length;i++)
		{
			String writefmt=writeformat.get(i);
			if ((writefmt.length()==Keywords.NUMSuffix.length())&& (writefmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			{
				try
				{
					if (!localrep)
					{
						if (defdec==-1)
						{
							double roundval=Double.valueOf(record[i]).doubleValue();
							Format f=new Format("%f");
							record[i]=f.format(roundval);
							record[i]=record[i].trim();
						}
						else
						{
							double roundval=Double.valueOf(record[i]).doubleValue();
							if (defdec==0)
							{
								record[i]=String.valueOf(Math.round(roundval));
							}
							else
							{
								String formatType="%."+defdec+"f";
								Format f=new Format(formatType);
								record[i]=f.format(roundval);
								record[i]=record[i].trim();
							}
						}
					}
					else
					{
						if (defdec==-1)
						{
							double roundval=Double.valueOf(record[i]).doubleValue();
							NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
							DecimalFormat df = (DecimalFormat)nf;
							record[i] = df.format(roundval);
						}
						else
						{
							double roundval=Double.valueOf(record[i]).doubleValue();
							NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
							nf.setMaximumFractionDigits(defdec);
							record[i] = nf.format(roundval);
						}
					}
				}
				catch (Exception ex) {}
			}
			else if ((writefmt.length()>Keywords.NUMSuffix.length())&& (writefmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			{
				String num=writefmt.substring(Keywords.NUMSuffix.length());
				if ((num.toUpperCase()).startsWith(Keywords.DTSuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i]).doubleValue();
						record[i] = DateFormat.getDateTimeInstance().format(new Date((long)roundval));
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.DATESuffix))
				{
					try
					{
						if (System.getProperty("writedatefmt")==null)
						{
							double roundval=Double.valueOf(record[i]).doubleValue();
							record[i] = DateFormat.getDateInstance().format(new Date((long)roundval));
						}
						else
						{
							String fval=System.getProperty("writedatefmt");
							if (fval.equals(""))
							{
								double roundval=Double.valueOf(record[i]).doubleValue();
								record[i] = DateFormat.getDateInstance().format(new Date((long)roundval));
							}
							else
							{
								double roundval=Double.valueOf(record[i]).doubleValue();
								Locale lc= Locale.getDefault();
								SimpleDateFormat sdf = new SimpleDateFormat(fval, lc);
								Calendar cal = Calendar.getInstance();
								long offset = cal.get(Calendar.ZONE_OFFSET);
								record[i]=sdf.format(new Date((long)roundval-offset));
							}
						}
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.TIMESuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i]).doubleValue();
						record[i] = DateFormat.getTimeInstance().format(new Date((long)roundval));
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.INTSuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i]).doubleValue();
						Format f=new Format("%f");
						record[i]=f.format(roundval);
						record[i]=record[i].substring(0,record[i].indexOf("."));
						record[i]=record[i].trim();
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.DECSuffix))
				{
					String num1=writefmt.substring(Keywords.NUMSuffix.length()+Keywords.DECSuffix.length());
					try
					{
						double roundval=Double.valueOf(record[i]).doubleValue();
						int numFormatDEC=Integer.parseInt(num1.trim());
						if (!localrep)
						{
							if (numFormatDEC==0)
							{
								record[i]=String.valueOf(Math.round(roundval));
							}
							else
							{
								String formatType="%."+numFormatDEC+"f";
								Format f=new Format(formatType);
								record[i]=f.format(roundval);
								record[i]=record[i].trim();
							}
						}
						else
						{
							NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
							nf.setMaximumFractionDigits(numFormatDEC);
							record[i] = nf.format(roundval);
						}
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.EXPSuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i]).doubleValue();
						Format f=new Format("%E");
						record[i]=f.format(roundval);
						record[i]=record[i].trim();
					}
					catch (Exception ex) {}
				}
			}
			else if ((writefmt.length()>Keywords.TEXTSuffix.length())&& (writefmt.toUpperCase().startsWith(Keywords.TEXTSuffix.toUpperCase())))
			{
				String num=writefmt.substring(Keywords.TEXTSuffix.length());
				int numFormatInt=0;
				try
				{
					numFormatInt=Integer.parseInt(num);
					record[i]=record[i].substring(0,numFormatInt);
				}
				catch (Exception ex) {}
			}
		}
		return record;
	}
}
