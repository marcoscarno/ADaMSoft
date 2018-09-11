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
import ADaMSoft.dataaccess.DataWriter;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StringDistances;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;
import ADaMSoft.utilities.StepUtilities;


/**
* This is the procedure that annotates words in  document
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcAnnotatewordsindoc extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Annotatewordsindoc
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.dict+"withannotations", Keywords.OUT.toLowerCase(), Keywords.varwords, Keywords.varwordstosearch, Keywords.varwithannotations, Keywords.similaritymetric};
		String [] optionalparameters=new String[] {Keywords.casesensitive, Keywords.seedsimilarity, Keywords.where, Keywords.replace};
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Keywords.percentage_total=3;
		Keywords.percentage_done=0;

		String similaritymetric=(String)parameters.get(Keywords.similaritymetric.toLowerCase());
		String[] metrics=new String[] {Keywords.chapmanlengthdeviation, Keywords.cosinesimilarity,Keywords.matchingcoefficient,
									Keywords.overlapcoefficient,Keywords.dicesimilarity,Keywords.jarowinkler,
									Keywords.jaccardsimilarity,Keywords.qgramsdistance, Keywords.levenshtein,Keywords.blockdistance,
									Keywords.mongeelkan,Keywords.chapmanorderednamecompoundsimilarity, Keywords.jaro,
									Keywords.soundex,Keywords.needlemanwunch,Keywords.equals};
		int selectedmetric=steputilities.CheckOption(metrics, similaritymetric);
		if (selectedmetric==0)
		{
			Keywords.percentage_total=0;
			return new Result("%1775% "+Keywords.similaritymetric.toUpperCase()+"\n"+steputilities.getMessage(), false, null);
		}

		boolean casesensitive =(parameters.get(Keywords.casesensitive)!=null);

		String replace =(String)parameters.get(Keywords.replace);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			Keywords.percentage_total=0;
			return new Result(dw.getmessage(), false, null);
		}

		DictionaryReader dictwa = (DictionaryReader)parameters.get(Keywords.dict+"withannotations");
		String varwordstosearch=(String)parameters.get(Keywords.varwordstosearch.toLowerCase());
		String varwithannotations=(String)parameters.get(Keywords.varwithannotations.toLowerCase());
		String varwords=(String)parameters.get(Keywords.varwords.toLowerCase());
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		double seedsimilarity=-1;
		String tempseedsimilarity=(String)parameters.get(Keywords.seedsimilarity.toLowerCase());
		if (tempseedsimilarity!=null)
		{
			try
			{
				seedsimilarity=Double.parseDouble(tempseedsimilarity);
			}
			catch (Exception e){}
			if (seedsimilarity<0 || seedsimilarity>1)
			{
				Keywords.percentage_total=0;
				return new Result("%3628%<br>\n", false, null);
			}
		}

		String[] tv=varwordstosearch.split(" ");
		if (tv.length!=1)
		{
			Keywords.percentage_total=0;
			return new Result("%3623%<br>\n", false, null);
		}
		tv=varwithannotations.split(" ");
		if (tv.length!=1)
		{
			Keywords.percentage_total=0;
			return new Result("%3624%<br>\n", false, null);
		}
		tv=varwords.split(" ");
		if (tv.length!=1)
		{
			Keywords.percentage_total=0;
			return new Result("%3625%<br>\n", false, null);
		}

		int pos_wts=-1;
		int pos_vwa=-1;
		for (int i=0; i<dictwa.gettotalvar(); i++)
		{
			if (varwordstosearch.equalsIgnoreCase(dictwa.getvarname(i))) pos_wts=i;
			if (varwithannotations.equalsIgnoreCase(dictwa.getvarname(i))) pos_vwa=i;
		}
		if (pos_wts==-1)
		{
			Keywords.percentage_total=0;
			return new Result("%3626%<br>\n", false, null);
		}
		if (pos_vwa==-1)
		{
			Keywords.percentage_total=0;
			return new Result("%3627%<br>\n", false, null);
		}

		Hashtable<String, String> reference_words=new Hashtable<String, String>();
		DataReader datawa = new DataReader(dictwa);
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		if (!datawa.open(null, rifrep, false))
		{
			Keywords.percentage_total=0;
			return new Result(datawa.getmessage(), false, null);
		}
		String[] values=null;
		while (!datawa.isLast())
		{
			values = datawa.getRecord();
			if (values!=null)
			{
				if (!values[pos_wts].equals("") && !values[pos_vwa].equals(""))
				{
					if (casesensitive)
						reference_words.put(values[pos_wts].toLowerCase(), values[pos_vwa]);
					else
						reference_words.put(values[pos_wts], values[pos_vwa]);
				}
			}
		}
		datawa.close();
		Keywords.percentage_done=2;
		if (reference_words.size()==0)
		{
			Keywords.percentage_total=0;
			Keywords.percentage_done=0;
			return new Result("%3633%<br>\n", false, null);
		}

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();
		dsu.defineolddict(dict);
		dsu.addnewvartoolddict("annotation", "%3629%", Keywords.TEXTSuffix, temph, temph);
		dsu.addnewvartoolddict("metric_value", "%3630%", Keywords.NUMSuffix, temph, temph);
		if (replace!=null)
		{
			if (replace.equalsIgnoreCase(Keywords.replaceall))
			{
				dsu.setempycodelabels();
				dsu.setempymissingdata();
			}
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				dsu.setempycodelabels();
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				dsu.setempymissingdata();
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			Keywords.percentage_total=0;
			Keywords.percentage_done=0;
			return new Result(dw.getmessage(), false, null);
		}

		VariableUtilities varu=new VariableUtilities(dict, null, varwords, null, null, null);
		if (varu.geterror())
		{
			Keywords.percentage_total=0;
			Keywords.percentage_done=0;
			return new Result(varu.getmessage(), false, null);
		}

		String[] totalvar=varu.getallvar();

		int[] replacerule=varu.getreplaceruleforall(replace);

		DataReader data = new DataReader(dict);
		if (!data.open(totalvar, replacerule, false))
		{
			Keywords.percentage_total=0;
			Keywords.percentage_done=0;
			return new Result(data.getmessage(), false, null);
		}
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where))
			{
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				return new Result(data.getmessage(), false, null);
			}
		}

		int[] allvarstype=varu.getnormalruleforall();

		String[] actualword;
		ValuesParser vp=new ValuesParser(allvarstype, null, null, null, null, null);
		String actualtw="";
		double actualdistance=0;
		StringDistances sd=new StringDistances();
		double max_distance=0.0;
		int numsub=0;
		String testword="";
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				actualword=vp.getanalysisvar(values);
				testword=actualword[0];
				if (casesensitive) testword=testword.toLowerCase();
				String[] newvalues=new String[2];
				newvalues[0]="";
				newvalues[1]="";
				max_distance=-1.7976931348623157E308;
				for (Enumeration<String> e = reference_words.keys() ; e.hasMoreElements() ;)
				{
					actualdistance=-1.7976931348623157E308;
					actualtw = e.nextElement();
					if (selectedmetric==1) actualdistance=sd.getDistance(testword, actualtw, 0);
					else if (selectedmetric==2) actualdistance=sd.getDistance(testword, actualtw, 1);
					else if (selectedmetric==3) actualdistance=sd.getDistance(testword, actualtw, 2);
					else if (selectedmetric==4) actualdistance=sd.getDistance(testword, actualtw, 3);
					else if (selectedmetric==5) actualdistance=sd.getDistance(testword, actualtw, 4);
					else if (selectedmetric==6) actualdistance=sd.getDistance(testword, actualtw, 5);
					else if (selectedmetric==7) actualdistance=sd.getDistance(testword, actualtw, 6);
					else if (selectedmetric==8) actualdistance=sd.getDistance(testword, actualtw, 7);
					else if (selectedmetric==9) actualdistance=sd.getDistance(testword, actualtw, 8);
					else if (selectedmetric==10) actualdistance=sd.getDistance(testword, actualtw, 9);
					else if (selectedmetric==11) actualdistance=sd.getDistance(testword, actualtw, 10);
					else if (selectedmetric==12) actualdistance=sd.getDistance(testword, actualtw, 11);
					else if (selectedmetric==13) actualdistance=sd.getDistance(testword, actualtw, 12);
					else if (selectedmetric==14) actualdistance=sd.getDistance(testword, actualtw, 14);
					else if (selectedmetric==15) actualdistance=sd.getDistance(testword, actualtw, 15);
					else if (testword.equals(actualtw)) actualdistance=1;
					if (seedsimilarity>=0)
					{
						if (actualdistance>seedsimilarity)
						{
							if (actualdistance>max_distance)
							{
								newvalues[0]=reference_words.get(actualtw);
								newvalues[1]=String.valueOf(actualdistance);
								max_distance=actualdistance;
							}
						}
					}
					else
					{
						if (actualdistance>max_distance)
						{
							newvalues[0]=reference_words.get(actualtw);
							newvalues[1]=String.valueOf(actualdistance);
							max_distance=actualdistance;
						}
					}

				}
				if (!Double.isNaN(max_distance))
				{
					if (max_distance>-1.7976931348623157E308) numsub++;
				}
				String[] wvalues=dsu.getnewvalues(values, newvalues);
				dw.write(wvalues);
			}
		}
		data.close();
		result.add(new LocalMessageGetter("%3634%: "+String.valueOf(numsub)+"<br>\n"));
		Keywords.percentage_done=0;
		Keywords.percentage_total=0;

		String keyword="Annotate words "+dict.getkeyword();
		String description="Annotate words "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3598, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"withannotations=", "dict", true, 3599, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 3600, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varwords, "var=all", true, 3601, dep, "", 2));
		String[] depa = new String[1];
		depa[0]=Keywords.dict+"withannotations";
		parameters.add(new GetRequiredParameters(Keywords.varwordstosearch, "var=all", true, 3602, depa, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varwithannotations, "var=all", true, 3603, depa, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.casesensitive, "checkbox", false, 467, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.similaritymetric,
		"listsingle=3604_"+Keywords.chapmanlengthdeviation+
		",3605_"+Keywords.cosinesimilarity+
		",3606_"+Keywords.matchingcoefficient+
		",3607_"+Keywords.overlapcoefficient+
		",3608_"+Keywords.dicesimilarity+
		",3609_"+Keywords.jarowinkler+
		",3610_"+Keywords.jaccardsimilarity+
		",3611_"+Keywords.qgramsdistance+
		",3612_"+Keywords.levenshtein+
		",3613_"+Keywords.blockdistance+
		",3614_"+Keywords.mongeelkan+
		",3615_"+Keywords.chapmanorderednamecompoundsimilarity+
		",3616_"+Keywords.jaro+
		",3617_"+Keywords.soundex+
		",3618_"+Keywords.needlemanwunch+
		",3619_"+Keywords.equals,
		true, 3620, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.seedsimilarity,"text", false, 3621,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 3631, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3632, dep, "", 2));
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
		retprocinfo[0]="4175";
		retprocinfo[1]="3622";
		return retprocinfo;
	}
}
