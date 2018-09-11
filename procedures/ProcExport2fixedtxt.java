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

package ADaMSoft.procedures;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;

import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;

import ADaMSoft.keywords.Keywords;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.DecimalFormatSymbols;

/**
* This is the procedure that export a data set in a fixed field text file
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcExport2fixedtxt implements RunStep
{
	/**
	* Starts the execution of Proc Export2fixedtxt
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.txtfile, Keywords.dict, Keywords.put};
		String [] optionalparameters=new String[] {Keywords.where, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String txtfile=(String)parameters.get(Keywords.txtfile);
		String replace=(String)parameters.get(Keywords.replace);
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String put=(String)parameters.get(Keywords.put);
		String[] partsput=put.split(";");
		String msg="";
		Vector<String> variables=new Vector<String>();
		Vector<Integer> positions=new Vector<Integer>();
		Vector<String> formats=new Vector<String>();
		Vector<String[]> detailed_formats=new Vector<String[]>();
		for (int i=0; i<partsput.length; i++)
		{
			partsput[i]=partsput[i].replaceAll("\\s+"," ");
			String[] infovar=partsput[i].split(" ");
			if (infovar.length!=3)
				return new Result("%3411%<br>\n", false, null);
			variables.add(infovar[0]);
			if (infovar[1].startsWith("@"))
			{
				try
				{
					infovar[1]=infovar[1].replaceAll("@","");
					int po=Integer.parseInt(infovar[1]);
					positions.add(new Integer(po));
					formats.add(infovar[2].toUpperCase());
				}
				catch (Exception e)
				{
					return new Result("%2165% ("+partsput[i]+")<br>\n", false, null);
				}
			}
			else
			{
				try
				{
					infovar[2]=infovar[2].replaceAll("@","");
					int po=Integer.parseInt(infovar[2]);
					positions.add(new Integer(po));
					formats.add(infovar[1].toUpperCase());
				}
				catch (Exception e)
				{
					return new Result("%2165% ("+partsput[i]+")<br>\n", false, null);
				}
			}
		}
		boolean esv=false;
		String vartempp="";
		for (int i=0; i<variables.size(); i++)
		{
			vartempp=vartempp+variables.get(i)+" ";
			esv=false;
			for (int j=0; j<dict.gettotalvar(); j++)
			{
				if (variables.get(i).equalsIgnoreCase(dict.getvarname(j))) esv=true;
			}
			if (!esv) msg=msg+variables.get(i)+" ";
			for (int j=i+1; j<variables.size(); j++)
			{
				if (variables.get(i).equalsIgnoreCase(variables.get(j)))
					return new Result("%2175% ("+variables.get(j)+")<br>\n", false, null);
			}
		}
		if (!msg.equals("")) return new Result("%514%<br>\n"+msg.trim()+"<br>\n", false, null);
		boolean foundformat=false;
		for (int i=0; i<formats.size(); i++)
		{
			foundformat=false;
			if (formats.get(i).startsWith("TEXT"))
			{
				try
				{
					String[] df=new String[2];
					String tf=formats.get(i).substring(4);
					df[0]="TEXT";
					df[1]=tf;
					detailed_formats.add(df);
				}
				catch (Exception e)
				{
					return new Result("%2164%<br>\n"+formats.get(i)+"<br>\n", false, null);
				}
				foundformat=true;
			}
			else
			{
				try
				{
					String tf=formats.get(i).substring(3);
					if (tf.equals("DATE"))
					{
						String[] df=new String[2];
						df[0]="NUM";
						df[1]="DATE";
						detailed_formats.add(df);
						foundformat=true;
					}
					if (tf.equals("TIME"))
					{
						String[] df=new String[2];
						df[0]="NUM";
						df[1]="TIME";
						detailed_formats.add(df);
						foundformat=true;
					}
					if (tf.equals("DATETIME"))
					{
						String[] df=new String[2];
						df[0]="NUM";
						df[1]="DATETIME";
						detailed_formats.add(df);
						foundformat=true;
					}
					if (tf.startsWith("D") && !tf.equals("DATE"))
					{
						String[] df=new String[2];
						df[0]="NUMD";
						try
						{
							String tff=tf.substring(1);
							String[] ptff=tff.split("\\.");
							if (ptff.length!=2) return new Result("%2166%<br>\n"+formats.get(i)+"<br>\n", false, null);
							df[1]=tff;
						}
						catch (Exception ee)
						{
							return new Result("%2166%<br>\n"+formats.get(i)+"<br>\n", false, null);
						}
						detailed_formats.add(df);
						foundformat=true;
					}
					if (tf.startsWith("Z"))
					{
						String[] df=new String[2];
						df[0]="NUMZ";
						try
						{
							String tff=tf.substring(1);
							String[] ptff=tff.split("\\.");
							if (ptff.length!=2) return new Result("%2168%<br>\n"+formats.get(i)+"<br>\n", false, null);
							df[1]=tff;
						}
						catch (Exception ee)
						{
							return new Result("%2168%<br>\n"+formats.get(i)+"<br>\n", false, null);
						}
						detailed_formats.add(df);
						foundformat=true;
					}
					if (tf.startsWith("L"))
					{
						String[] df=new String[2];
						df[0]="NUML";
						try
						{
							String tff=tf.substring(1);
							String[] ptff=tff.split("\\.");
							if (ptff.length!=2) return new Result("%2170%<br>\n"+formats.get(i)+"<br>\n", false, null);
							df[1]=tff;
						}
						catch (Exception ee)
						{
							return new Result("%2170%<br>\n"+formats.get(i)+"<br>\n", false, null);
						}
						detailed_formats.add(df);
						foundformat=true;
					}
					if (tf.startsWith("X"))
					{
						String[] df=new String[2];
						df[0]="NUMX";
						try
						{
							String tff=tf.substring(1);
							String[] ptff=tff.split("\\.");
							if (ptff.length!=2) return new Result("%2169%<br>\n"+formats.get(i)+"<br>\n", false, null);
							df[1]=tff;
						}
						catch (Exception ee)
						{
							return new Result("%2169%<br>\n"+formats.get(i)+"<br>\n", false, null);
						}
						detailed_formats.add(df);
						foundformat=true;
					}
				}
				catch (Exception e)
				{
					return new Result("%2167%<br>\n"+formats.get(i)+"<br>\n", false, null);
				}
			}
			if (!foundformat) return new Result("%3412%<br>\n"+formats.get(i)+"<br>\n", false, null);
		}

		vartempp=vartempp.trim();

		VariableUtilities varu=new VariableUtilities(dict, null, vartempp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] var=varu.getanalysisvar();
		int[] replacerule=varu.getreplaceruleforsel(replace);

		String filepath=txtfile;
		if (!filepath.endsWith(".txt"))
			filepath=filepath+".txt";
		boolean exist=(new File(filepath)).exists();
		if (exist)
		{
			boolean success = (new File(filepath)).delete();
			if (!success)
				return new Result("%2172%<br>\n", false, null);
		}

		DataReader data = new DataReader(dict);

		if (!data.open(var, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		Vector<StepResult> result = new Vector<StepResult>();
		String msgpos="";
		String readfmt="";
		BufferedWriter out = null;
		try
		{
			out = new BufferedWriter(new FileWriter(filepath));
			String[] values=null;
			String res="";
			String tempv="";
			String refdtl="2008-12-12 12:12:56";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
			double tdtl=(double)sdf.parse(refdtl).getTime();
			refdtl = DateFormat.getDateTimeInstance().format(new Date((long)tdtl));
			int dtlen=refdtl.length();
			refdtl = DateFormat.getDateInstance().format(new Date((long)tdtl));
			int dlen=refdtl.length();
			refdtl = DateFormat.getTimeInstance().format(new Date((long)tdtl));
			int tlen=refdtl.length();

			int actualpos=0;

			String tres="";
			int refmsg=0;
			while (!data.isLast())
			{
				refmsg++;
				res="";
				values = data.getRecord();
				if (values!=null)
				{
					for (int i=0; i<values.length; i++)
					{
						try
						{
							values[i]=values[i].replaceAll("\n", " ");
						}
						catch (Exception enl) {}
						try
						{
							values[i]=values[i].replaceAll("\t", " ");
						}
						catch (Exception enl) {}
						try
						{
							values[i]=values[i].replaceAll("\r", " ");
						}
						catch (Exception enl) {}
						actualpos=(positions.get(i)).intValue();
						int actual=res.length();
						if (actualpos>0)
						{
							if (actualpos-1>res.length())
							{
								tres="";
								for (int j=0; j<(actualpos-1-res.length()); j++)
								{
									tres=tres+" ";
								}
								res=res+tres;
								actual=res.length();
							}
							else if (actualpos-1<res.length())
							{
								res=res.substring(0, actualpos-1);
								actual=res.length();
							}
						}
						String[] pdf=detailed_formats.get(i);
						if (pdf[0].equals("TEXT"))
						{
							int numFormatInt=Integer.parseInt(pdf[1]);
							tempv=values[i];
							if (tempv.length()>numFormatInt)
								tempv=tempv.substring(0,numFormatInt);
							else if (tempv.length()<numFormatInt)
							{
								for (int j=0; j<(numFormatInt-tempv.length()); j++)
								{
									res=res+" ";
								}
							}
							res=res+tempv;
						}
						else
						{
							if (pdf[0].equals("NUM") && pdf[1].equals("DATETIME"))
							{
								try
								{
									double roundval=Double.valueOf(values[i]).doubleValue();
									values[i] = DateFormat.getDateTimeInstance().format(new Date((long)roundval));
									if (values[i].length()<dtlen)
									{
										for (int j=0; j<(dtlen-values[i].length()); j++)
										{
											res=res+" ";
										}
									}
									res=res+values[i];
								}
								catch (Exception ee)
								{
									for (int j=0; j<dtlen; j++)
									{
										res=res+" ";
									}
								}
							}
							else if (pdf[0].equals("NUM") && pdf[1].equals("DATE"))
							{
								try
								{
									double roundval=Double.valueOf(values[i]).doubleValue();
									values[i] = DateFormat.getDateInstance().format(new Date((long)roundval));
									if (values[i].length()<dlen)
									{
										for (int j=0; j<(dlen-values[i].length()); j++)
										{
											res=res+" ";
										}
									}
									res=res+values[i];
								}
								catch (Exception ee)
								{
									for (int j=0; j<dlen; j++)
									{
										res=res+" ";
									}
								}
							}
							else if (pdf[0].equals("NUM") && pdf[1].equals("TIME"))
							{
								try
								{
									double roundval=Double.valueOf(values[i]).doubleValue();
									values[i] = DateFormat.getTimeInstance().format(new Date((long)roundval));
									if (values[i].length()<tlen)
									{
										for (int j=0; j<(tlen-values[i].length()); j++)
										{
											res=res+" ";
										}
									}
									res=res+values[i];
								}
								catch (Exception ee)
								{
									for (int j=0; j<tlen; j++)
									{
										res=res+" ";
									}
								}
							}
							else if (pdf[0].equals("NUMD"))
							{
								int maxl=0;
								try
								{
									String pattern="";
									String[] pn=pdf[1].split("\\.");
									int nn1=Integer.parseInt(pn[0].trim());
									if (!pn[1].equals("0"))
									{
										maxl=nn1+1;
										int nn2=Integer.parseInt(pn[1].trim());
										for (int j=0; j<nn1-nn2; j++)
										{
											pattern=pattern+"#";
										}
										pattern=pattern+".";
										for (int j=0; j<nn2; j++)
										{
											pattern=pattern+"0";
										}
									}
									else
									{
										maxl=nn1;
										for (int j=0; j<nn1; j++)
										{
											pattern=pattern+"#";
										}
									}
									double roundval=Double.valueOf(values[i]).doubleValue();
									DecimalFormat myFormatter = new DecimalFormat(pattern);
									values[i] = myFormatter.format(roundval);
									if (values[i].length()<maxl)
									{
										for (int j=0; j<(maxl-values[i].length()); j++)
										{
											res=res+" ";
										}
									}
									try
									{
										values[i]=values[i].replaceAll(",",".");
									}
									catch (Exception ex) {}
									res=res+values[i];
								}
								catch (Exception ex)
								{
									for (int j=0; j<maxl; j++)
									{
										res=res+" ";
									}
								}
							}
							else if (pdf[0].equals("NUMX"))
							{
								int maxl=0;
								try
								{
									String pattern="";
									String[] pn=pdf[1].split("\\.");
									int nn1=Integer.parseInt(pn[0].trim());
									if (!pn[1].equals("0"))
									{
										maxl=nn1+1;
										int nn2=Integer.parseInt(pn[1].trim());
										for (int j=0; j<nn1-nn2; j++)
										{
											pattern=pattern+"#";
										}
										pattern=pattern+".";
										for (int j=0; j<nn2; j++)
										{
											pattern=pattern+"0";
										}
									}
									else
									{
										maxl=nn1;
										for (int j=0; j<nn1; j++)
										{
											pattern=pattern+"#";
										}
									}
									double roundval=Double.valueOf(values[i]).doubleValue();
									DecimalFormatSymbols unusualSymbols =new DecimalFormatSymbols();
									unusualSymbols.setDecimalSeparator(',');
									DecimalFormat myFormatter = new DecimalFormat(pattern, unusualSymbols);
									values[i] = myFormatter.format(roundval);
									if (values[i].length()<maxl)
									{
										for (int j=0; j<(maxl-values[i].length()); j++)
										{
											res=res+" ";
										}
									}
									res=res+values[i];
								}
								catch (Exception ex)
								{
									for (int j=0; j<maxl; j++)
									{
										res=res+" ";
									}
								}
							}
							else if (pdf[0].equals("NUML"))
							{
								int maxl=0;
								try
								{
									String pattern="";
									String[] pn=pdf[1].split("\\.");
									int nn1=Integer.parseInt(pn[0].trim());
									if (!pn[1].equals("0"))
									{
										maxl=nn1+1;
										int nn2=Integer.parseInt(pn[1].trim());
										for (int j=0; j<nn1-nn2; j++)
										{
											pattern=pattern+"#";
										}
										pattern=pattern+".";
										for (int j=0; j<nn2; j++)
										{
											pattern=pattern+"0";
										}
									}
									else
									{
										maxl=nn1;
										for (int j=0; j<nn1; j++)
										{
											pattern=pattern+"#";
										}
									}
									double roundval=Double.valueOf(values[i]).doubleValue();
									NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
									DecimalFormat df = (DecimalFormat)nf;
									df.applyPattern(pattern);
									values[i] = df.format(roundval);
									if (values[i].length()<maxl)
									{
										for (int j=0; j<(maxl-values[i].length()); j++)
										{
											res=res+" ";
										}
									}
									res=res+values[i];
								}
								catch (Exception ex)
								{
									for (int j=0; j<maxl; j++)
									{
										res=res+" ";
									}
								}
							}
							else if (pdf[0].equals("NUMZ"))
							{
								int maxl=0;
								try
								{
									String pattern="";
									String[] pn=pdf[1].split("\\.");
									int nn1=Integer.parseInt(pn[0].trim());
									if (!pn[1].equals("0"))
									{
										maxl=nn1+1;
										int nn2=Integer.parseInt(pn[1].trim());
										for (int j=0; j<nn1-nn2; j++)
										{
											pattern=pattern+"0";
										}
										pattern=pattern+".";
										for (int j=0; j<nn2; j++)
										{
											pattern=pattern+"0";
										}
									}
									else
									{
										maxl=nn1;
										for (int j=0; j<nn1; j++)
										{
											pattern=pattern+"0";
										}
									}
									double roundval=Double.valueOf(values[i]).doubleValue();
									DecimalFormat myFormatter = new DecimalFormat(pattern);
									values[i] = myFormatter.format(roundval);
									try
									{
										values[i]=values[i].replaceAll(",",".");
									}
									catch (Exception ex) {}
									if (values[i].length()<maxl)
									{
										for (int j=0; j<(maxl-values[i].length()); j++)
										{
											res=res+" ";
										}
									}
									res=res+values[i];
								}
								catch (Exception ex)
								{
									for (int j=0; j<maxl; j++)
									{
										res=res+" ";
									}
								}
							}
						}
						if (refmsg==1)
						{
							msgpos=msgpos+"%2182% ("+variables.get(i)+", "+formats.get(i)+", "+String.valueOf(actual+1)+"-"+String.valueOf(res.length()-actual)+")<br>\n";
							readfmt=readfmt+String.valueOf(actual+1)+"-"+String.valueOf(res.length()-actual)+";";
						}
					}
				}
				out.write(res+"\n");
			}
			data.close();
			out.close();
		}
		catch (Exception e)
		{
			if (out!=null)
			{
				try
				{
					out.close();
				}
				catch (Exception ee) {}
			}
			return new Result("%2173%<br>\n"+e.toString()+"<br>\n", false, null);
		}
		result.add(new LocalMessageGetter(msgpos));
		result.add(new LocalMessageGetter("%2183%<br>\n"+readfmt));
		return new Result("%2180% ("+filepath+")<br>\n", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 541, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.txtfile, "filesave=txt", true, 2179, dep, "", 2));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.put, "textvarsws", true, 2176, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters("", "note", false, 2177, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3413, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2178, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3414, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3415, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4166";
		retprocinfo[1]="2159";
		return retprocinfo;
	}
}
