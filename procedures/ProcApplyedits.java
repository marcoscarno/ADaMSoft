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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;
import java.util.TreeMap;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluates the edits according to the values inside a data set
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcApplyedits extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Applyedits
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.OUTR.toLowerCase(), Keywords.dict, Keywords.dict+"e"};
		String [] optionalparameters=new String[] {Keywords.OUTC.toLowerCase(), Keywords.OUTV.toLowerCase(), Keywords.tolerance, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Keywords.percentage_total=4;
		Keywords.percentage_done=0;

		boolean isoutc =(parameters.get(Keywords.OUTC.toLowerCase())!=null);
		boolean isoutv =(parameters.get(Keywords.OUTV.toLowerCase())!=null);

		String replace =(String)parameters.get(Keywords.replace);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			return new Result(dw.getmessage(), false, null);
		}

		String stolerance =(String)parameters.get(Keywords.tolerance);
		double tolerance=0.00001;
		if (stolerance!=null)
		{
			try
			{
				tolerance=Double.parseDouble(stolerance);
			}
			catch (Exception en)
			{
				return new Result("%2596%<br>\n", false, null);
			}
			if ((tolerance>1) || (tolerance<0))
				return new Result("%2596%<br>\n", false, null);
		}

		DataWriter dwc=null;
		if (isoutc)
		{
			dwc=new DataWriter(parameters, Keywords.OUTC.toLowerCase());
			if (!dwc.getmessage().equals(""))
			{
				return new Result(dwc.getmessage(), false, null);
			}
		}
		DataWriter dwv=null;

		DictionaryReader dicte = (DictionaryReader)parameters.get(Keywords.dict+"e");
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		int refvar=dicte.gettotalvar()-2;

		String[] varref=new String[refvar];
		String[] vartoread=new String[dicte.gettotalvar()];
		int[] replacerule=new int[dicte.gettotalvar()];

		int posv=0;

		try
		{
			for (int i=0; i<dicte.gettotalvar(); i++)
			{
				String tv=dicte.getvarname(i);
				if ( (!tv.equals("_sign_")) && (!tv.equals("_b_")) )
				{
					String[] ttv=tv.split("_");
					String realvname="";
					for (int rv=1; rv<ttv.length; rv++)
					{
						realvname=realvname+ttv[rv];
						if (rv<(ttv.length-1))
							realvname=realvname+"_";
					}
					varref[posv]=realvname.trim();
					vartoread[posv]=tv;
					replacerule[posv]=1;
					posv++;
				}
			}
			vartoread[posv]="_b_";
			replacerule[posv]=1;
			vartoread[posv+1]="_sign_";
			replacerule[posv+1]=1;
		}
		catch (Exception eedit)
		{
			return new Result("%2909%<br>\n", false, null);
		}
		if (posv==0)
		{
			return new Result("%2910%<br>\n", false, null);
		}

		Hashtable<Integer, Integer> positionvars=new Hashtable<Integer, Integer>();
		boolean notfound=false;
		for (int i=0; i<refvar; i++)
		{
			notfound=false;
			for (int j=0; j<dict.gettotalvar(); j++)
			{
				if (varref[i].equalsIgnoreCase(dict.getvarname(j)))
				{
					notfound=true;
					positionvars.put(new Integer(i), new Integer(j));
					break;
				}
			}
			if (!notfound)
			{
				return new Result("%2469%: "+varref[i]+"<br>\n", false, null);
			}
		}

		Hashtable<Integer, Hashtable<Integer, Double>> coeffval=new Hashtable<Integer, Hashtable<Integer, Double>>();
		Hashtable<Integer, Integer> sigs=new Hashtable<Integer, Integer>();

		DataReader datae = new DataReader(dicte);
		if (!datae.open(vartoread, replacerule, false))
		{
			return new Result(datae.getmessage(), false, null);
		}

		Hashtable<String, String> editfmt=new Hashtable<String, String>();

		String[] values=null;
		int pose=0;
		double cf=0;
		String tempedit="";
		while (!datae.isLast())
		{
			values = datae.getRecord();
			tempedit="";
			for (int i=0; i<vartoread.length-2; i++)
			{
				cf=0;
				values[i]=values[i].trim();
				try
				{
					cf=Double.parseDouble(values[i]);
				}
				catch (Exception exnum)
				{
					cf=0;
				}
				if (cf!=0.0)
				{
					if (values[i].startsWith("-"))
						tempedit=tempedit+values[i]+"*"+varref[i];
					else
						tempedit=tempedit+"+"+values[i]+"*"+varref[i];
				}
			}
			cf=0;
			try
			{
				cf=Double.parseDouble(values[vartoread.length-2]);
			}
			catch (Exception exnum)
			{
				cf=0;
			}
			if (cf!=0.0)
			{
				if (values[vartoread.length-2].startsWith("-"))
					tempedit=tempedit+values[vartoread.length-2];
				else
					tempedit=tempedit+"+"+values[vartoread.length-2];
			}
			tempedit=tempedit+values[vartoread.length-1]+"0";
			editfmt.put(String.valueOf(pose+1), tempedit);
			posv=0;
			Hashtable<Integer, Double> tempcv=new Hashtable<Integer, Double>();
			for (int i=0; i<refvar; i++)
			{
				posv=(positionvars.get(new Integer(i))).intValue();
				try
				{
					cf=Double.parseDouble(values[i]);
				}
				catch (Exception exnum)
				{
					cf=0.0;
				}
				tempcv.put(new Integer(posv), new Double(cf));
			}
			try
			{
				cf=Double.parseDouble(values[refvar]);
			}
			catch (Exception exnum)
			{
				cf=0.0;
			}
			tempcv.put(new Integer(-1), new Double(cf));
			if (values[values.length-1].equals(">="))
				sigs.put(new Integer(pose), new Integer(0));
			else
				sigs.put(new Integer(pose), new Integer(1));
			coeffval.put(new Integer(pose), tempcv);
			pose++;
		}
		datae.close();
		Keywords.percentage_done=1;
		double tempcref=0;
		int tempvref=0;
		int rifrep=0;
		int addedpos=0;

		/*Check and eventually adds positivity conditions on the variables*/
		int[] cpvar=new int[refvar];
		for (int i=0; i<pose; i++)
		{
			Hashtable<Integer, Double> tc=coeffval.get(new Integer(i));
			rifrep=0;
			for (Enumeration<Integer> e = tc.keys() ; e.hasMoreElements() ;)
			{
				tempvref=(e.nextElement()).intValue();
				tempcref=(tc.get(new Integer(tempvref))).doubleValue();
				if ((tempvref>-1) && (tempvref!=0))
				{
					rifrep++;
				}
			}
			if (rifrep==1)
			{
				for (Enumeration<Integer> e = tc.keys() ; e.hasMoreElements() ;)
				{
					tempvref=(e.nextElement()).intValue();
					tempcref=(tc.get(new Integer(tempvref))).doubleValue();
					if ((tempvref>-1) && (tempvref!=0))
						cpvar[tempvref]=1;
				}
			}
		}
		for (int i=0; i<cpvar.length; i++)
		{
			if (cpvar[i]!=1)
			{
				addedpos++;
				editfmt.put(String.valueOf(pose+1), varref[i]+">=0");
				posv=0;
				Hashtable<Integer, Double> tempcv=new Hashtable<Integer, Double>();
				for (int j=0; j<refvar; j++)
				{
					cf=0.0;
					if (i==j)
						cf=1.0;
					tempcv.put(new Integer((positionvars.get(new Integer(j))).intValue()), new Double(cf));
				}
				tempcv.put(new Integer(-1), new Double(0.0));
				sigs.put(new Integer(pose), new Integer(0));
				coeffval.put(new Integer(pose), tempcv);
				pose++;
			}
		}

		tempcref=0;
		tempvref=0;
		rifrep=0;
		Hashtable<Integer, Double> involvedvar=new Hashtable<Integer, Double>();

		rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;

		String keyword="Applyedits "+dict.getkeyword();
		String description="Applyedits "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		double[][] result=new double[pose][4];
		for (int i=0; i<pose; i++)
		{
			for (int j=0; j<4; j++)
			{
				result[i][j]=0.0;
			}
		}
		DataReader data = new DataReader(dict);
		if (!data.open(null, rifrep, false))
		{
			return new Result(data.getmessage(), false, null);
		}

		Hashtable<String, String> tempmd=new Hashtable<String, String>();

		DataSetUtilities dsuc=new DataSetUtilities();
		if (isoutc)
		{
			Hashtable<String, String> fmtvarsimple=new Hashtable<String, String>();
			fmtvarsimple.put("9","%2564%");
			fmtvarsimple.put("99","%2563%");
			fmtvarsimple.put("1","%2562%");
			dsuc.setreplace(replace);
			dsuc.defineolddict(dict);
			dsuc.addnewvartoolddict("check_edit", "%2561%", Keywords.TEXTSuffix, fmtvarsimple, tempmd);
			dsuc.addnewvartoolddict("check_edit_ref", "%2480%", Keywords.TEXTSuffix, tempmd, tempmd);
			if (replace!=null)
			{
				if (replace.equalsIgnoreCase(Keywords.replaceall))
				{
					dsuc.setempycodelabels();
					dsuc.setempymissingdata();
				}
				else if (replace.equalsIgnoreCase(Keywords.replaceformat))
					dsuc.setempycodelabels();
				else if (replace.equalsIgnoreCase(Keywords.replacemissing))
					dsuc.setempymissingdata();
			}
			if (!dwc.opendatatable(dsuc.getfinalvarinfo()))
				return new Result(dwc.getmessage(), false, null);
		}
		DataSetUtilities dsuv=new DataSetUtilities();

		String[] newvalues=new String[2];

		tempcref=0;
		tempvref=0;
		double tempcurrv=0;
		double tempver=0;
		int[] temprecord=new int[pose];
		int[] presmd=new int[pose];
		int refe=0;
		int ndiff=0;
		int refdiff=-1;
		String binedit="";
		String typeedit="";
		boolean satisfied=true;
		int errated=0;
		int erratednodup=0;
		boolean notvalid=false;
		double numrecord=0;
		int nmissed=0;
		int nfailed=0;
		TreeMap<Integer, Integer> distfailed=new TreeMap<Integer, Integer>();
		int tempint=0;
		boolean thereismissing=false;
		while (!data.isLast())
		{
			numrecord++;
			values = data.getRecord();
			if (isoutc)
			{
				newvalues[0]="";
				newvalues[1]="";
			}
			ndiff=0;
			refdiff=-1;
			binedit="";
			typeedit="";
			notvalid=false;
			nfailed=0;
			thereismissing=false;
			for (int i=0; i<pose; i++)
			{
				satisfied=true;
				temprecord[i]=0;
				presmd[i]=0;
				Hashtable<Integer, Double> tc=coeffval.get(new Integer(i));
				tempcurrv=0;
				refe=0;
				for (Enumeration<Integer> e = tc.keys() ; e.hasMoreElements() ;)
				{
					tempvref=(e.nextElement()).intValue();
					tempcref=(tc.get(new Integer(tempvref))).doubleValue();
					if (tempvref>-1)
					{
						if (values[tempvref].equals(""))
							presmd[refe]=1;
						else
						{
							try
							{
								tempver=Double.parseDouble(values[tempvref]);
								tempcurrv=tempcurrv+tempcref*tempver;
							}
							catch (Exception etn)
							{
								presmd[refe]=1;
							}
						}
					}
					else
						tempcurrv=tempcurrv+tempcref;
					refe++;
				}
				if (presmd[i]==0)
				{
					int tempsig=sigs.get(new Integer(i));
					if (tempsig!=0)
					{
						if (Math.abs(tempcurrv)>tolerance)
						{
							notvalid=true;
							satisfied=false;
							temprecord[i]=1;
							refdiff=i;
							ndiff++;
							binedit=binedit+String.valueOf(i+1)+" ";
						}
					}
					else
					{
						if (tempcurrv<(-1*tolerance))
						{
							notvalid=true;
							satisfied=false;
							temprecord[i]=1;
							ndiff++;
							refdiff=i;
							binedit=binedit+String.valueOf(i+1)+" ";
						}
					}
					if (!satisfied)
					{
						nfailed++;
						for (Enumeration<Integer> eww = tc.keys() ; eww.hasMoreElements() ;)
						{
							tempvref=(eww.nextElement()).intValue();
							tempcref=(tc.get(new Integer(tempvref))).doubleValue();
							if ((tempvref>-1) && (tempcref!=0.0))
							{
								if (involvedvar.get(new Integer(tempvref))==null)
									involvedvar.put(new Integer(tempvref), new Double(1.0));
								else
								{
									double tempinv=(involvedvar.get(new Integer(tempvref))).doubleValue();
									involvedvar.put(new Integer(tempvref), new Double(tempinv+1.0));
								}
							}
						}
					}
				}
				else
				{
					thereismissing=true;
					binedit=binedit+"MD_"+String.valueOf(i+2)+" ";
					result[i][1]=result[i][1]+1;
				}
			}
			if (thereismissing) nmissed++;
			if (notvalid)
			{
				errated=errated+1;
				if (!thereismissing) erratednodup=erratednodup+1;
				if (!distfailed.containsKey(new Integer(nfailed)))
					distfailed.put(new Integer(nfailed), new Integer(1));
				else
				{
					tempint=(distfailed.get(new Integer(nfailed))).intValue();
					distfailed.put(new Integer(nfailed), new Integer(tempint+1));
				}
			}
			if (ndiff==1)
			{
				if (presmd[refdiff]==0)
				{
					result[refdiff][2]=result[refdiff][2]+1;
					for (int i=0; i<pose; i++)
					{
						if ((presmd[i]==0) && (temprecord[i]==0))
						{
							result[i][0]=result[i][0]+1;
						}
					}
				}
			}
			else
			{
				for (int i=0; i<pose; i++)
				{
					if (presmd[i]==0)
					{
						if (temprecord[i]==1)
							result[i][3]=result[i][3]+1;
						if (temprecord[i]==0)
							result[i][0]=result[i][0]+1;
					}
				}
			}
			typeedit="1";
			binedit=binedit.trim();
			if (binedit.indexOf("MD_")>=0)
				typeedit="99";
			if (binedit.indexOf("MD_")<0)
			{
				if (!binedit.equals(""))
					typeedit="9";
			}
			newvalues[0]=typeedit;
			newvalues[1]=binedit;
			if (isoutc)
			{
				String[] cvalues=dsuc.getnewvalues(values, newvalues);
				dwc.writenoapprox(cvalues);
			}
		}
		data.close();
		Keywords.percentage_done=2;

		DataSetUtilities dsu=new DataSetUtilities();

		dsu.addnewvar("edit_ref", "%2470%", Keywords.TEXTSuffix, editfmt, tempmd);
		dsu.addnewvar("edit_ok", "%2481%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("edit_ok_p", "%2637%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("edit_md", "%2471%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("edit_md_p", "%2638%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("edit_single", "%2472%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("edit_single_p", "%2639%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("edit_multi", "%2473%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("edit_multi_p", "%2640%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			dwc.deletetmp();
			return new Result(dw.getmessage(), false, null);
		}

		String[] wvalues=new String[9];

		for (int i=0; i<pose; i++)
		{
			wvalues[0]=String.valueOf(i+1);
			wvalues[1]=String.valueOf(result[i][0]);
			wvalues[2]=String.valueOf(100.0*result[i][0]/numrecord);
			wvalues[3]=String.valueOf(result[i][1]);
			wvalues[4]=String.valueOf(100.0*result[i][1]/numrecord);
			wvalues[5]=String.valueOf(result[i][2]);
			wvalues[6]=String.valueOf(100.0*result[i][2]/numrecord);
			wvalues[7]=String.valueOf(result[i][3]);
			wvalues[8]=String.valueOf(100.0*result[i][3]/numrecord);
			dw.write(wvalues);
		}

		Vector<StepResult> results = new Vector<StepResult>();
		int nr=(int)numrecord;
		results.add(new LocalMessageGetter("%2643%: "+String.valueOf(nr)+"<br>\n"));
		if (errated>0)
		{
			if (erratednodup!=errated)
			{
				results.add(new LocalMessageGetter("%3053%: "+String.valueOf(errated)+"<br>\n"));
				if (erratednodup>0)
					results.add(new LocalMessageGetter("%3054%: "+String.valueOf(erratednodup)+"<br>\n"));
				else
					results.add(new LocalMessageGetter("%3055%<br>\n"));
			}
			else
				results.add(new LocalMessageGetter("%2608%: "+String.valueOf(errated)+"<br>\n"));
		}
		else
			results.add(new LocalMessageGetter("%2458%<br>\n"));
		if (nmissed>0)
			results.add(new LocalMessageGetter("%2641%: "+String.valueOf(nmissed)+"<br>\n"));
		else
			results.add(new LocalMessageGetter("%2642%<br>\n"));

		if (addedpos>0)
			results.add(new LocalMessageGetter("%2613% ("+String.valueOf(addedpos)+")<br>\n"));

		DataSetUtilities dsur=new DataSetUtilities();
		DataWriter dwr=null;
		boolean isoutr=true;
		if (errated>0)
		{
			dwr=new DataWriter(parameters, Keywords.OUTR.toLowerCase());
			if (!dwr.getmessage().equals(""))
			{
				dwc.deletetmp();
				dw.deletetmp();
				return new Result(dwr.getmessage(), false, null);
			}
			dsur.addnewvar("num_edit", "%2644%", Keywords.NUMSuffix, tempmd, tempmd);
			dsur.addnewvar("num_rec", "%2645%", Keywords.NUMSuffix, tempmd, tempmd);
			dsur.addnewvar("num_rec_p", "%2646%", Keywords.NUMSuffix, tempmd, tempmd);
			if (!dwr.opendatatable(dsur.getfinalvarinfo()))
			{
				dwc.deletetmp();
				dw.deletetmp();
				return new Result(dwr.getmessage(), false, null);
			}
			String[] valuesr=new String[3];
			Iterator<Integer> intit = distfailed.keySet().iterator();
			while(intit.hasNext())
			{
				tempint=(intit.next()).intValue();
				int times=(distfailed.get(new Integer(tempint))).intValue();
				valuesr[0]=String.valueOf(tempint);
				valuesr[1]=String.valueOf(times);
				double dtimes=times/numrecord;
				valuesr[2]=String.valueOf(100*dtimes);
				dwr.write(valuesr);
			}
		}
		else
		{
			isoutr=false;
			results.add(new LocalMessageGetter("%2587%<br>\n"));
		}
		Keywords.percentage_done=3;

		if (isoutv)
		{
			if (involvedvar.size()>0)
			{
				dwv=new DataWriter(parameters, Keywords.OUTV.toLowerCase());
				if (!dwv.getmessage().equals(""))
				{
					dwc.deletetmp();
					dwr.deletetmp();
					dw.deletetmp();
					Keywords.procedure_error=true;
					return new Result(dwv.getmessage(), false, null);
				}
				Hashtable<String, String> fmtvarv=new Hashtable<String, String>();
				for (Enumeration<Integer> ewp = positionvars.keys() ; ewp.hasMoreElements() ;)
				{
					rifrep=(ewp.nextElement()).intValue();
					tempvref=(positionvars.get(new Integer(rifrep))).intValue();
					fmtvarv.put("var_"+dict.getvarname(tempvref), dict.getvarlabel(tempvref));
				}
				dsuv.addnewvar("ref_var", "%2592%", Keywords.TEXTSuffix, fmtvarv, tempmd);
				dsuv.addnewvar("ref_num", "%2591%", Keywords.NUMSuffix, tempmd, tempmd);
				if (!dwv.opendatatable(dsuv.getfinalvarinfo()))
				{
					dwc.deletetmp();
					dwr.deletetmp();
					dw.deletetmp();
					return new Result(dwv.getmessage(), false, null);
				}

				String[] vvalues=new String[2];
				double maxrefrec=0;
				for (Enumeration<Integer> ewi = involvedvar.keys() ; ewi.hasMoreElements() ;)
				{
					tempvref=(ewi.nextElement()).intValue();
					double refec=(involvedvar.get(new Integer(tempvref))).doubleValue();
					if (refec>maxrefrec)
						maxrefrec=refec;
				}
				for (Enumeration<Integer> ewi = involvedvar.keys() ; ewi.hasMoreElements() ;)
				{
					tempvref=(ewi.nextElement()).intValue();
					double refec=(involvedvar.get(new Integer(tempvref))).doubleValue();
					vvalues[0]="var_"+dict.getvarname(tempvref);
					vvalues[1]=String.valueOf(100*refec/maxrefrec);
					dwv.write(vvalues);
				}
			}
			else
			{
				isoutv=false;
				results.add(new LocalMessageGetter("%2604%<br>\n\n"));
			}
		}
		Keywords.percentage_done=0;
		Keywords.percentage_total=0;

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		if (isoutc)
		{
			resclose=dwc.close();
			if (!resclose)
				return new Result(dwc.getmessage(), false, null);
		}
		if (isoutv)
		{
			resclose=dwv.close();
			if (!resclose)
				return new Result(dwv.getmessage(), false, null);
		}
		if (isoutr)
		{
			resclose=dwr.close();
			if (!resclose)
				return new Result(dwr.getmessage(), false, null);
		}
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		results.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		if (isoutc)
		{
			Vector<Hashtable<String, String>> tablevariableinfoc=dwc.getVarInfo();
			Hashtable<String, String> datatableinfoc=dwc.getTableInfo();
			results.add(new LocalDictionaryWriter(dwc.getdictpath(), "Verifyedits "+dict.getkeyword(), "Verifyedits "+dict.getdescription(), author, dwc.gettabletype(),
			datatableinfoc, dsuc.getfinalvarinfo(), tablevariableinfoc, dsuc.getfinalcl(), dsuc.getfinalmd(), null));
		}
		if (isoutv)
		{
			Vector<Hashtable<String, String>> tablevariableinfov=dwv.getVarInfo();
			Hashtable<String, String> datatableinfov=dwv.getTableInfo();
			results.add(new LocalDictionaryWriter(dwv.getdictpath(), "Verifyedits "+dict.getkeyword(), "Verifyedits "+dict.getdescription(), author, dwv.gettabletype(),
			datatableinfov, dsuv.getfinalvarinfo(), tablevariableinfov, dsuv.getfinalcl(), dsuv.getfinalmd(), null));
		}
		if (isoutr)
		{
			Vector<Hashtable<String, String>> tablevariableinfor=dwr.getVarInfo();
			Hashtable<String, String> datatableinfor=dwr.getTableInfo();
			results.add(new LocalDictionaryWriter(dwr.getdictpath(), "Verifyedits "+dict.getkeyword(), "Verifyedits "+dict.getdescription(), author, dwr.gettabletype(),
			datatableinfor, dsur.getfinalvarinfo(), tablevariableinfor, dsur.getfinalcl(), dsur.getfinalmd(), null));
		}
		return new Result("", true, results);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 2474, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"e=", "dict", true, 2475, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 2476, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTR.toLowerCase()+"=", "setting=out", true, 2586, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTC.toLowerCase()+"=", "setting=out", false, 2478, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTV.toLowerCase()+"=", "setting=out", false, 2590, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.tolerance, "text", false, 2595, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="2439";
		retprocinfo[1]="2477";
		return retprocinfo;
	}
}
