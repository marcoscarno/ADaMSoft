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

import java.util.TreeMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.HashSet;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.VariableUtilities;

import opennlp.tools.sentdetect.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
* This is the procedure that writes the words in a record into more records
* @author marco.scarno@gmail.com
* @date 03/04/2017
*/
public class ProcWords2records implements RunStep
{
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean noout=false;
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.dict, Keywords.OUT.toLowerCase(), Keywords.var};
		String[] optionalparameters = new String[] {Keywords.vardescriptor, Keywords.withchars, Keywords.minlength, Keywords.maxlength,Keywords.onlyascii, Keywords.consider_sentence, Keywords.sentence_identifiers, Keywords.sentence_detector_file};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DataWriter dw = new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		String minval = (String) parameters.get(Keywords.minlength);
		String maxval = (String) parameters.get(Keywords.maxlength);
		int minvalue = 0;
		int maxvalue = 100;
		try
		{
			if (minval != null)
			{
				minvalue = Integer.parseInt(minval);
			}
			if (maxval != null)
			{
				maxvalue = Integer.parseInt(maxval);
			}
		}
		catch (Exception e)
		{
			return new Result("%3325%<br>\n", false, null);
		}
		if (minvalue<0) return new Result("%3325%<br>\n", false, null);
		if (maxvalue<0) return new Result("%3325%<br>\n", false, null);
		if (minvalue>maxvalue) return new Result("%3325%<br>\n", false, null);
		boolean usechars = (parameters.get(Keywords.withchars) != null);
		boolean onlyascii = (parameters.get(Keywords.onlyascii) != null);
		boolean consider_sentence = (parameters.get(Keywords.consider_sentence) != null);
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vardescriptor=(String)parameters.get(Keywords.vardescriptor.toLowerCase());
		String sentence_identifiers=(String)parameters.get(Keywords.sentence_identifiers.toLowerCase());
		if (sentence_identifiers==null) sentence_identifiers=".;?!";
		sentence_identifiers=sentence_identifiers.replaceAll(" ","");
		HashSet<Integer> temp_si=new HashSet<Integer>();
		for (int i=0; i<sentence_identifiers.length(); i++)
		{
			int temp_sent=sentence_identifiers.codePointAt(i);
			if (temp_sent!=46)
				temp_si.add(new Integer(temp_sent));
		}
		temp_si.add(new Integer(46));
		if (vartemp.indexOf(" ")>0) return new Result("%4080%<br>\n", false, null);
		if (vardescriptor!=null)
		{
			if (vardescriptor.indexOf(" ")>0) return new Result("%4081%<br>\n", false, null);
		}
		String sentence_detector_file=(String)parameters.get(Keywords.sentence_detector_file.toLowerCase());
		InputStream modelIn=null;
		SentenceModel model=null;
		if (sentence_detector_file!=null)
		{
			boolean existfile=new File(sentence_detector_file).exists();
			if (!existfile) return new Result("%4188% ("+sentence_detector_file+")<br>\n", false, null);
			try
			{
				modelIn = new FileInputStream(sentence_detector_file);
				model = new SentenceModel(modelIn);
			}
			catch (Exception e)
			{
				return new Result("%4190%<br>"+e.toString()+"<br>\n", false, null);
			}
		}
		if (!consider_sentence && sentence_detector_file!=null)
		{
			return new Result("%4189%<br>\n", false, null);
		}

		VariableUtilities varu=new VariableUtilities(dict, vardescriptor, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] totalvar=new String[1];
		totalvar[0]=vartemp;
		if (vardescriptor!=null)
		{
			totalvar=new String[2];
			totalvar[0]=vartemp;
			totalvar[1]=vardescriptor;
		}
		int[] replacerule=new int[totalvar.length];
		for (int i=0; i<totalvar.length; i++)
		{
			replacerule[i]=0;
		}
		DataReader data = new DataReader(dict);
		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		DataSetUtilities dsu=new DataSetUtilities();
		String keyword = "Words2records "+dict.getkeyword();
		String description = "Words2records " + dict.getdescription();
		String author = (String)parameters.get(Keywords.client_host.toLowerCase());
		Hashtable<String, String> tempmd1=new Hashtable<String, String>();
		Hashtable<String, String> tempmd2=new Hashtable<String, String>();
		Hashtable<String, String> tempmd3=new Hashtable<String, String>();
		dsu.addnewvar("vardescriptor", "%4082%", Keywords.TEXTSuffix, tempmd1, tempmd1);
		dsu.addnewvar("word", "%1142%", Keywords.TEXTSuffix, tempmd2, tempmd2);
		if (usechars)
			dsu.addnewvar("ascii_codes", "%1140%", Keywords.TEXTSuffix, tempmd3, tempmd3);
		if (!dw.opendatatable(dsu.getfinalvarinfo())) return new Result(dw.getmessage(), false, null);
		String[] values=new String[1];
		if (vardescriptor!=null) values=new String[2];
		String[] outvalues=new String[2];
		if (usechars) outvalues=new String[3];
		int validgroup=0;
		int current_record=0;
		String[] parts=new String[0];
		boolean write_record=true;
		StringBuilder start=new StringBuilder();
		StringBuilder end=new StringBuilder();
		int curr_code=0;
		String[] parts_sentences=null;
		int current_sentence=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			current_record++;
			if (values!=null)
			{
				outvalues[0]=String.valueOf(current_record);
				if (vardescriptor!=null)
				{
					outvalues[0]=values[1];
				}
				if (consider_sentence && sentence_detector_file==null)
				{
					current_sentence=0;
					start=new StringBuilder();
					start.append(values[0].toLowerCase());
					end=new StringBuilder();
					int sz = start.length();
					for (int i = 0; i < sz; i++)
					{
						curr_code=start.charAt(i);
						if (temp_si.contains(new Integer(curr_code))) end.append(".");
						else end.append(start.charAt(i));
					}
					values[0]=end.toString();
					start=null;
					parts_sentences=values[0].split("\\.");
					for (int p=0; p<parts_sentences.length; p++)
					{
						current_sentence++;
						if (!parts_sentences[p].equals(""))
						{
							if (onlyascii)
							{
								start=new StringBuilder();
								start.append(parts_sentences[p].toLowerCase());
								end=new StringBuilder();
								sz = start.length();
								for (int i = 0; i < sz; i++)
								{
									if (start.charAt(i) >= 97 && start.charAt(i) <= 122) end.append(start.charAt(i));
									else if (start.charAt(i) >= 224 && start.charAt(i) <= 246) end.append(start.charAt(i));
									else if (start.charAt(i) >= 249 && start.charAt(i) <= 255) end.append(start.charAt(i));
									else end.append(" ");
								}
								parts_sentences[p]=end.toString();
								start=null;
							}
							parts_sentences[p]=parts_sentences[p].replaceAll("\\s+"," ");
							parts_sentences[p]=parts_sentences[p].trim();
							parts_sentences[p]=parts_sentences[p].toLowerCase();
							if (!parts_sentences[p].equals(""))
							{
								parts=parts_sentences[p].split(" ");
								for (int i=0; i<parts.length; i++)
								{
									write_record=true;
									if (parts[i].equals("")) write_record=false;
									if (minvalue>0 && parts[i].length()<minvalue) write_record=false;
									if (maxvalue>0 && parts[i].length()>maxvalue) write_record=false;
									if (write_record)
									{
										outvalues[0]=String.valueOf(current_record)+"_"+String.valueOf(current_sentence);
										if (vardescriptor!=null)
										{
											outvalues[0]=values[1]+"_"+String.valueOf(current_sentence);
										}
										outvalues[1]=parts[i].toLowerCase();
										if (usechars)
										{
											outvalues[2]="";
											for (int h = 0; h < parts[i].length(); h++)
											{
												outvalues[2] += parts[i].codePointAt(h) + " ";
											}
										}
										validgroup++;
										dw.write(outvalues);
									}
								}
							}
						}
					}
				}
				if (consider_sentence && sentence_detector_file!=null)
				{
					current_sentence=0;
					SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
					parts_sentences = sentenceDetector.sentDetect(values[0]);
					for (int p=0; p<parts_sentences.length; p++)
					{
						current_sentence++;
						parts_sentences[p]=parts_sentences[p].trim();
						if (!parts_sentences[p].equals(""))
						{
							if (onlyascii)
							{
								start=new StringBuilder();
								start.append(parts_sentences[p].toLowerCase());
								end=new StringBuilder();
								int sz = start.length();
								for (int i = 0; i < sz; i++)
								{
									if (start.charAt(i) >= 97 && start.charAt(i) <= 122) end.append(start.charAt(i));
									else if (start.charAt(i) >= 224 && start.charAt(i) <= 246) end.append(start.charAt(i));
									else if (start.charAt(i) >= 249 && start.charAt(i) <= 255) end.append(start.charAt(i));
									else end.append(" ");
								}
								parts_sentences[p]=end.toString();
								start=null;
							}
							parts_sentences[p]=parts_sentences[p].replaceAll("\\s+"," ");
							parts_sentences[p]=parts_sentences[p].trim();
							parts_sentences[p]=parts_sentences[p].toLowerCase();
							if (!parts_sentences[p].equals(""))
							{
								parts=parts_sentences[p].split(" ");
								for (int i=0; i<parts.length; i++)
								{
									write_record=true;
									if (parts[i].equals("")) write_record=false;
									if (minvalue>0 && parts[i].length()<minvalue) write_record=false;
									if (maxvalue>0 && parts[i].length()>maxvalue) write_record=false;
									if (write_record)
									{
										outvalues[0]=String.valueOf(current_record)+"_"+String.valueOf(current_sentence);
										if (vardescriptor!=null)
										{
											outvalues[0]=values[1]+"_"+String.valueOf(current_sentence);
										}
										outvalues[1]=parts[i].toLowerCase();
										if (usechars)
										{
											outvalues[2]="";
											for (int h = 0; h < parts[i].length(); h++)
											{
												outvalues[2] += parts[i].codePointAt(h) + " ";
											}
										}
										validgroup++;
										dw.write(outvalues);
									}
								}
							}
						}
					}
				}
				else
				{
					if (onlyascii)
					{
						start=new StringBuilder();
						start.append(values[0].toLowerCase());
						end=new StringBuilder();
						int sz = start.length();
						for (int i = 0; i < sz; i++)
						{
							if (start.charAt(i) >= 97 && start.charAt(i) <= 122) end.append(start.charAt(i));
							else if (start.charAt(i) >= 224 && start.charAt(i) <= 246) end.append(start.charAt(i));
							else if (start.charAt(i) >= 249 && start.charAt(i) <= 255) end.append(start.charAt(i));
							else end.append(" ");
						}
						values[0]=end.toString();
						start=null;
					}
					values[0]=values[0].replaceAll("\\s+"," ");
					values[0]=values[0].trim();
					values[0]=values[0].toLowerCase();
					if (!values[0].equals(""))
					{
						parts=values[0].split(" ");
						for (int i=0; i<parts.length; i++)
						{
							write_record=true;
							if (parts[i].equals("")) write_record=false;
							if (minvalue>0 && parts[i].length()<minvalue) write_record=false;
							if (maxvalue>0 && parts[i].length()>maxvalue) write_record=false;
							if (write_record)
							{
								outvalues[1]=parts[i].toLowerCase();
								if (usechars)
								{
									outvalues[2]="";
									for (int h = 0; h < parts[i].length(); h++)
									{
										outvalues[2] += parts[i].codePointAt(h) + " ";
									}
								}
								validgroup++;
								dw.write(outvalues);
							}
						}
					}
				}
			}
		}
		data.close();
		if (validgroup==0)
		{
			dw.deletetmp();
			return new Result("%666%<br>\n", false, null);
		}
		boolean resclose = dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo = dw.getVarInfo();
		Hashtable<String, String> datatableinfo = dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword,description, author,
		dw.gettabletype(), datatableinfo,dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(),dsu.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 4084, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 4085, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vardescriptor, "vars=all", false, 4086, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.withchars, "checkbox", false, 468, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.minlength, "text", false, 480, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.maxlength, "text", false, 481, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.onlyascii, "checkbox", false, 3341, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.consider_sentence, "checkbox", false, 4150, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.sentence_identifiers, "text", false, 4151, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.sentence_detector_file, "file=all", false, 4186, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4187, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4178";
		info[1]="4083";
		return info;
	}
}
