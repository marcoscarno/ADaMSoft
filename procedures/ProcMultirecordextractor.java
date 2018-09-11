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

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
* This is the procedure that writes a text file referred to a series of document specified by a data set
* @author marco.scarno@gmail.com
* @date 21/03/2017
*/
public class ProcMultirecordextractor implements RunStep
{
	BufferedWriter summaryexport;
	/**
	*Write the text contained in a document
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.dict, Keywords.vartext};
		String[] optionalparameters = new String[] {Keywords.varfilename, Keywords.varfreqterm, Keywords.dirout, Keywords.nowritesummary};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		boolean nowritesummary=(parameters.get(Keywords.nowritesummary) != null);

		String dirout = (String) parameters.get(Keywords.dirout);
		String tempdir=(String)parameters.get(Keywords.WorkDir);
		if (dirout==null) dirout=tempdir;
		dirout=dirout.replaceAll("\\\\","/");

		if (!dirout.endsWith("/")) dirout=dirout+"/";

		int posvar=-1;
		int posvfname=-1;
		int posvvfreq=-1;

		Keywords.percentage_done=0;
		Keywords.percentage_total=1;

		String vartext=(String)parameters.get(Keywords.vartext);
		String varfreqterm=(String)parameters.get(Keywords.varfreqterm);
		String varfilename=(String)parameters.get(Keywords.varfilename);
		DictionaryReader dict = (DictionaryReader) parameters.get(Keywords.dict);

		for (int i=0; i<dict.gettotalvar(); i++)
		{
			String tv=dict.getvarname(i);
			if (vartext.equalsIgnoreCase(tv)) posvar=i;
			if (varfilename!=null && varfilename.equalsIgnoreCase(tv)) posvfname=i;
			if (varfreqterm!=null && varfreqterm.equalsIgnoreCase(tv)) posvvfreq=i;
		}
		if (posvar==-1)
			return new Result("%1995% ("+vartext+")<br>\n", false, null);

		DataReader data = new DataReader(dict);
		if (!data.open(null, 1, false))
		{
			Keywords.procedure_error=true;
			return new Result(data.getmessage(), false, null);
		}

		summaryexport = null;

		if (!nowritesummary)
		{
			try
			{
				if ( (new File(dirout+"ExportSummary.txt") ).exists())
				{
					if ( !(new File(dirout+"ExportSummary.txt") ).delete())
						return new Result("%1995% ("+dirout+"ExportSummary.txt"+")<br>\n", false, null);
				}
				summaryexport=new BufferedWriter(new FileWriter(dirout+"ExportSummary.txt"));
			}
			catch (Exception e)
			{
				return new Result("%1995% ("+dirout+"ExportSummary.txt"+")<br>\n", false, null);
			}
		}

		int currecord=0;
		int posexported=0;
		String afilename="";
		boolean writeefile=true;
		Vector<String> errorw=new Vector<String>();
		String[] values=null;
		String[] repfreq=new String[0];
		String[] repfreqv=new String[0];
		int currep=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				currecord++;
				values[posvar]=values[posvar].replaceAll("\t"," ");
				values[posvar]=values[posvar].replaceAll("\0"," ");
				values[posvar]=values[posvar].replaceAll("\f"," ");
				values[posvar]=values[posvar].replaceAll("\uFFFD","");
				values[posvar]=values[posvar].replaceAll("\u00A0","");
				values[posvar]=values[posvar].replaceAll("\r"," ");
				values[posvar]=values[posvar].replaceAll("\n"," ");
				values[posvar]=values[posvar].replaceAll("\\s+"," ");
				values[posvar]=values[posvar].trim();
				afilename=dirout+"Record"+String.valueOf(currecord)+".txt";
				if (posvfname!=-1)
					afilename=dirout+values[posvfname];
				if (!afilename.endsWith(".txt")) afilename=afilename+".txt";
				writeefile=true;
				if ( (new File(afilename) ).exists())
				{
					if ( !(new File(afilename) ).delete())
						writeefile=false;
				}
				if (values[posvar].equals("")) writeefile=false;
				if (writeefile)
				{
					if (posvvfreq!=-1)
					{
						values[posvvfreq]=values[posvvfreq].replaceAll("\t"," ");
						values[posvvfreq]=values[posvvfreq].replaceAll("\0"," ");
						values[posvvfreq]=values[posvvfreq].replaceAll("\f"," ");
						values[posvvfreq]=values[posvvfreq].replaceAll("\uFFFD","");
						values[posvvfreq]=values[posvvfreq].replaceAll("\u00A0","");
						values[posvvfreq]=values[posvvfreq].replaceAll("\r"," ");
						values[posvvfreq]=values[posvvfreq].replaceAll("\n"," ");
						values[posvvfreq]=values[posvvfreq].replaceAll("\\s+"," ");
						values[posvvfreq]=values[posvvfreq].trim();
						repfreq=values[posvvfreq].split(" ");

					}
					try
					{
						BufferedWriter extfile=new BufferedWriter(new FileWriter(afilename));
						if (posvvfreq==-1)
							extfile.write(values[posvar]+"\n");
						else
						{
							repfreqv=values[posvar].split(" ");
							if (repfreqv.length==repfreq.length)
							{
								for (int i=0; i<repfreqv.length; i++)
								{
									currep=Integer.parseInt(repfreq[i]);
									for (int j=0; j<currep; j++)
									{
										extfile.write(repfreqv[i]);
										if (j<currep-1)
											extfile.write(" ");
									}
									if (i<repfreqv.length-1)
										extfile.write(" ");
								}
								extfile.write("\n");
							}
							else
							{
								extfile.write(values[posvar]+"\n");
								errorw.add("%3778% (Record: "+String.valueOf(currecord)+")");
							}
						}
						extfile.close();
						posexported++;
						if (!nowritesummary)
						{
							if (posvfname==-1)
								summaryexport.write("Record: "+String.valueOf(currecord)+"\t"+afilename+"\n");
							else
							{
								summaryexport.write("Record: "+values[posvfname]+"\t"+afilename+"\n");
							}
						}
						extfile.close();
					}
					catch (Exception ex)
					{
						errorw.add("%1997% (Record: "+String.valueOf(currecord)+"): "+ex.toString());
					}
				}
			}
		}
		data.close();
		if (!nowritesummary)
		{
			try
			{
				summaryexport.close();
			}
			catch (Exception ex) {}
		}

		result.add(new LocalMessageGetter("%1998%: "+String.valueOf(currecord)+"<br>\n"));
		result.add(new LocalMessageGetter("%2067%: "+String.valueOf(posexported)+"<br>\n"));
		if (errorw.size()>0)
		{
			result.add(new LocalMessageGetter("%2067%: "+String.valueOf(posexported)+"<br>\n"));
			for (int i=0; i<errorw.size(); i++)
			{
				result.add(new LocalMessageGetter(errorw.get(i)+"<br>"));
			}
		}

		return new Result("%2504%<br>\n", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters() {
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1989, dep, "", 1));
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.vartext, "var=all", true, 1202, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varfreqterm, "var=all", false, 3779, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.dirout, "dir", false, 1990, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varfilename, "var=all", false, 1991, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nowritesummary, "checkbox", false, 3773, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4170";
		info[1]="1992";
		return info;
	}
}
