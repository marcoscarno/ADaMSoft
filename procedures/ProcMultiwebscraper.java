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

import org.jsoup.select.Elements;
import org.jsoup.nodes.*;
import org.jsoup.nodes.Element;

import java.util.*;
import java.net.*;

import ADaMSoft.utilities.JSoupConnection;
import java.util.concurrent.*;

import org.apache.tika.Tika;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.io.EOFException;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.FastTempDataSet;

import ADaMSoft.utilities.WorkQueue;
import ADaMSoft.utilities.ObjectsForQueue;
import ADaMSoft.utilities.ObjectForQueue;
import ADaMSoft.utilities.RoboSafe;

/**
* This is the procedure that loads the structures of several web sites that are specified in a data set
* @author marco.scarno@gmail.com
* @date 20/02/2017
*/
public class ProcMultiwebscraper implements RunStep
{
	DataWriter dw;
	DataWriter dwt;
	int maxdepth;
	public static Vector<String> tempmsg;
	public static int sitesnu;
	public static int realtreated;
	/**
	* Starts the execution of Proc MultiWebScraper and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean testproc;
		tempmsg=new Vector<String>();
		sitesnu=0;
		realtreated=0;
		Vector<StepResult> results = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.OUT.toLowerCase()+"content", Keywords.OUT.toLowerCase()+"tags", Keywords.varmainsite};
		String [] optionalparameters=new String[] {Keywords.vardescriptor, Keywords.varmaxdepth, Keywords.varfilterurls,
		Keywords.varuseragent, Keywords.varcookies, Keywords.varfollowredirects, Keywords.varignorecontenttype,
		Keywords.varignorehttperrors, Keywords.varmethod, Keywords.vartimeout, Keywords.avoidextension, Keywords.tagnames,
		Keywords.varmaxpages, Keywords.varmaxtime, Keywords.onlyascii, Keywords.numthreads, Keywords.maxtime,
		Keywords.separate_contents, Keywords.just_html, Keywords.char_cookie_separator};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		testproc =(parameters.get("testproc")!=null);
		if (testproc)
		{
			try
			{
				String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(fname, false));
				bw.write("Result of WEBSCRAPER TEST Procedure\n");
				bw.close();
			}
			catch (Exception eft){}
		}

		String temp_char_cookie_separator=(String)parameters.get(Keywords.char_cookie_separator);
		int char_cookie_separator=-1;
		if (temp_char_cookie_separator!=null)
		{
			try
			{
				char_cookie_separator=Integer.parseInt(temp_char_cookie_separator);
			}
			catch (Exception ecs)
			{
				return new Result("%4088%<br>\n", false, null);
			}
		}

		boolean onlyascii = (parameters.get(Keywords.onlyascii) != null);

		boolean separate_contents = (parameters.get(Keywords.separate_contents) != null);
		boolean just_html = (parameters.get(Keywords.just_html) != null);

		if (just_html && separate_contents)
			return new Result("%4028%<br>\n", false, null);

		String temp_maxtime=((String)(parameters.get(Keywords.maxtime)));
		double maxtime=0;
		if (temp_maxtime!=null)
		{
			try
			{
				maxtime=Double.parseDouble(temp_maxtime);
			}
			catch (Exception emt)
			{
				maxtime=-1;
			}
		}
		if (maxtime<0)
			return new Result("%4028%<br>\n", false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String[] avoidextension=new String[4];
        avoidextension[0]=".css";
        avoidextension[1]=".js";
        avoidextension[2]=".jpg";
        avoidextension[3]=".ico";
        if (parameters.get(Keywords.avoidextension)!=null)
        {
			try
			{
				avoidextension=((String)(parameters.get(Keywords.avoidextension))).split(";");
				for (int i=0; i<avoidextension.length; i++)
				{
					avoidextension[i]=avoidextension[i].trim();
				}
			}
			catch (Exception ec)
			{
				return new Result("%3718%<br>\n", false, null);
			}
		}
		String[] reftagnames=null;
        if (parameters.get(Keywords.tagnames)!=null)
        {
			try
			{
				reftagnames=((String)(parameters.get(Keywords.tagnames))).split(";");
				for (int i=0; i<reftagnames.length; i++)
				{
					reftagnames[i]=reftagnames[i].trim();
				}
			}
			catch (Exception ec)
			{
				return new Result("%3719%<br>\n", false, null);
			}
		}

		int pos_varmainsite=-1;
		int pos_vardescriptor=-1;
		int pos_varmaxdepth=-1;
		int pos_varfilterurls=-1;
		int pos_varuseragent=-1;
		int pos_varcookies=-1;
		int pos_varfollowredirects=-1;
		int pos_varignorecontenttype=-1;
		int pos_varignorehttperrors=-1;
		int pos_varmethod=-1;
		int pos_vartimeout=-1;
		int pos_varmaxpages=-1;
		int pos_varmaxtime=-1;

		String tmp_varmainsite=(String) parameters.get(Keywords.varmainsite);
		String tmp_vardescriptor=(String) parameters.get(Keywords.vardescriptor);
		String tmp_varmaxdepth=(String) parameters.get(Keywords.varmaxdepth);
		String tmp_varfilterurls=(String) parameters.get(Keywords.varfilterurls);
		String tmp_varuseragent=(String) parameters.get(Keywords.varuseragent);
		String tmp_varcookies=(String) parameters.get(Keywords.varcookies);
		String tmp_varfollowredirects=(String) parameters.get(Keywords.varfollowredirects);
		String tmp_varignorecontenttype=(String) parameters.get(Keywords.varignorecontenttype);
		String tmp_varignorehttperrors=(String) parameters.get(Keywords.varignorehttperrors);
		String tmp_varmethod=(String) parameters.get(Keywords.varmethod);
		String tmp_vartimeout=(String) parameters.get(Keywords.vartimeout);
		String tmp_varmaxpages=(String) parameters.get(Keywords.varmaxpages);
		String tmp_varmaxtime=(String) parameters.get(Keywords.varmaxtime);
		if (tmp_varmainsite==null) tmp_varmainsite="";
		if (tmp_vardescriptor==null) tmp_vardescriptor="";
		if (tmp_varmaxdepth==null) tmp_varmaxdepth="";
		if (tmp_varfilterurls==null) tmp_varfilterurls="";
		if (tmp_varuseragent==null) tmp_varuseragent="";
		if (tmp_varcookies==null) tmp_varcookies="";
		if (tmp_varfollowredirects==null) tmp_varfollowredirects="";
		if (tmp_varignorecontenttype==null) tmp_varignorecontenttype="";
		if (tmp_varignorehttperrors==null) tmp_varignorehttperrors="";
		if (tmp_varmethod==null) tmp_varmethod="";
		if (tmp_vartimeout==null) tmp_vartimeout="";
		if (tmp_varmaxpages==null) tmp_varmaxpages="";
		if (tmp_varmaxtime==null) tmp_varmaxtime="";

		for (int i=0; i<dict.gettotalvar(); i++)
		{
			String tempname=dict.getvarname(i);
			if (tempname.equalsIgnoreCase(tmp_varmainsite)) pos_varmainsite=i;
			if (tempname.equalsIgnoreCase(tmp_vardescriptor)) pos_vardescriptor=i;
			if (tempname.equalsIgnoreCase(tmp_varmaxdepth)) pos_varmaxdepth=i;
			if (tempname.equalsIgnoreCase(tmp_varfilterurls)) pos_varfilterurls=i;
			if (tempname.equalsIgnoreCase(tmp_varuseragent)) pos_varuseragent=i;
			if (tempname.equalsIgnoreCase(tmp_varcookies)) pos_varcookies=i;
			if (tempname.equalsIgnoreCase(tmp_varfollowredirects)) pos_varfollowredirects=i;
			if (tempname.equalsIgnoreCase(tmp_varignorecontenttype)) pos_varignorecontenttype=i;
			if (tempname.equalsIgnoreCase(tmp_varignorehttperrors)) pos_varignorehttperrors=i;
			if (tempname.equalsIgnoreCase(tmp_varmethod)) pos_varmethod=i;
			if (tempname.equalsIgnoreCase(tmp_vartimeout)) pos_vartimeout=i;
			if (tempname.equalsIgnoreCase(tmp_varmaxpages)) pos_varmaxpages=i;
			if (tempname.equalsIgnoreCase(tmp_varmaxtime)) pos_varmaxtime=i;
		}
		if (!tmp_varmainsite.equals("") && pos_varmainsite==-1) return new Result("%3525% ("+tmp_varmainsite+")<br>\n", false, null);
		if (!tmp_vardescriptor.equals("") && pos_vardescriptor==-1) return new Result("%3526% ("+tmp_vardescriptor+")<br>\n", false, null);
		if (!tmp_varmaxdepth.equals("") && pos_varmaxdepth==-1) return new Result("%3527% ("+tmp_varmaxdepth+")<br>\n", false, null);
		if (!tmp_varfilterurls.equals("") && pos_varfilterurls==-1) return new Result("%3528% ("+tmp_varfilterurls+")<br>\n", false, null);
		if (!tmp_varuseragent.equals("") && pos_varuseragent==-1) return new Result("%3529% ("+tmp_varuseragent+")<br>\n", false, null);
		if (!tmp_varcookies.equals("") && pos_varcookies==-1) return new Result("%3530% ("+tmp_varcookies+")<br>\n", false, null);
		if (!tmp_varfollowredirects.equals("") && pos_varfollowredirects==-1) return new Result("%3531% ("+tmp_varfollowredirects+")<br>\n", false, null);
		if (!tmp_varignorecontenttype.equals("") && pos_varignorecontenttype==-1) return new Result("%3532% ("+tmp_varignorecontenttype+")<br>\n", false, null);
		if (!tmp_varignorehttperrors.equals("") && pos_varignorehttperrors==-1) return new Result("%3533% ("+tmp_varignorehttperrors+")<br>\n", false, null);
		if (!tmp_varmethod.equals("") && pos_varmethod==-1) return new Result("%3534% ("+tmp_varmethod+")<br>\n", false, null);
		if (!tmp_vartimeout.equals("") && pos_vartimeout==-1) return new Result("%3535% ("+tmp_vartimeout+")<br>\n", false, null);
		if (!tmp_varmaxpages.equals("") && pos_varmaxpages==-1) return new Result("%3736% ("+tmp_varmaxpages+")<br>\n", false, null);
		if (!tmp_varmaxtime.equals("") && pos_varmaxtime==-1) return new Result("%4026% ("+tmp_varmaxtime+")<br>\n", false, null);

		dw=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"content");
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		if (!testproc)
		{
			dwt=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"tags");
			if (!dwt.getmessage().equals(""))
				return new Result(dwt.getmessage(), false, null);
		}

		DataReader data = new DataReader(dict);

		if (!data.open(null, 1, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		String[] values=null;
		int tempint=0;

		ObjectsForQueue ofq=new ObjectsForQueue();

		int numwebsites=0;
		String refn="";
		maxdepth=1;
		String verifymd="";
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				String[] refv=new String[13];
				if (pos_vardescriptor!=-1) refn=values[pos_vardescriptor];
				else refn=values[pos_varmainsite];
				if (pos_varmainsite!=-1) refv[0]=values[pos_varmainsite]; else refv[0]="";
				if (pos_vardescriptor!=-1) refv[1]=values[pos_vardescriptor]; else refv[1]=values[pos_varmainsite];
				if (pos_varmaxdepth!=-1)
				{
					tempint=-1;
					try
					{
						double tempd=Double.parseDouble(values[pos_varmaxdepth]);
						tempint=(int)tempd;
						if (tempint>maxdepth) maxdepth=tempint;
					}
					catch (Exception ed){}
					if (tempint>100 || tempint<0) verifymd=verifymd+values[pos_varmaxdepth]+" ";
				}
				if (pos_varmaxdepth!=-1) refv[2]=values[pos_varmaxdepth]; else refv[2]="";
				if (pos_varfilterurls!=-1) refv[3]=values[pos_varfilterurls]; else refv[3]="";
				if (pos_varuseragent!=-1) refv[4]=values[pos_varuseragent]; else refv[4]="";
				if (pos_varcookies!=-1) refv[5]=values[pos_varcookies]; else refv[5]="";
				if (pos_varfollowredirects!=-1) refv[6]=values[pos_varfollowredirects]; else refv[6]="";
				if (pos_varignorecontenttype!=-1) refv[7]=values[pos_varignorecontenttype]; else refv[7]="";
				if (pos_varignorehttperrors!=-1) refv[8]=values[pos_varignorehttperrors]; else refv[8]="";
				if (pos_varmethod!=-1) refv[9]=values[pos_varmethod]; else refv[9]="";
				if (pos_vartimeout!=-1) refv[10]=values[pos_vartimeout]; else refv[10]="";
				if (pos_varmaxpages!=-1) refv[11]=values[pos_varmaxpages]; else refv[11]="";
				if (pos_varmaxtime!=-1) refv[12]=values[pos_varmaxtime]; else refv[12]="";
				ofq.addelement(refn, refv);
			}
		}
		data.close();

		if (!verifymd.equals(""))
			return new Result("%3739% ("+verifymd.trim()+")<br>\n", false, null);

		numwebsites=ofq.getelem();
		if (testproc)
		{
			try
			{
				String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
				bw.write("Number of web sites to scrape: "+String.valueOf(numwebsites)+"\n");
				bw.close();
			}
			catch (Exception eft){}
		}

		ofq.fillqueue();

		DataSetUtilities dsu=new DataSetUtilities();
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		dsu.addnewvar("site_descriptor", "%3536%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("pages", "%3703%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("content", "%3704%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("content_size", "%3705%", Keywords.NUMSuffix, tempmd, tempmd);
		Hashtable<String, String> clt=new Hashtable<String, String>();
		clt.put("1", "%3509%");
		clt.put("0", "%3707%");
		dsu.addnewvar("scraping_result", "%3706%", Keywords.TEXTSuffix, clt, tempmd);
		dsu.addnewvar("scraping_time", "%4023%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("used_urls", "%4037%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("real_used_urls", "%4038%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("main_response_code", "%4039%", Keywords.TEXTSuffix, tempmd, tempmd);
		if (separate_contents)
			dsu.addnewvar("page_reference", "%4039%", Keywords.TEXTSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			return new Result(dw.getmessage(), false, null);
		}

		DataSetUtilities dsut=new DataSetUtilities();
		tempmd=new Hashtable<String, String>();
		if (!testproc)
		{
			dsut.addnewvar("site_descriptor", "%3536%", Keywords.TEXTSuffix, tempmd, tempmd);
			dsut.addnewvar("main_site", "%3491%", Keywords.TEXTSuffix, tempmd, tempmd);
			for (int i=0; i<maxdepth; i++)
			{
				dsut.addnewvar("level_"+String.valueOf(i+1), "%3708%: "+String.valueOf(i+1), Keywords.TEXTSuffix, tempmd, tempmd);
			}
			dsut.addnewvar("tag_name", "%3710%", Keywords.TEXTSuffix, tempmd, tempmd);
			dsut.addnewvar("key", "%3711%", Keywords.TEXTSuffix, tempmd, tempmd);
			dsut.addnewvar("value", "%3712%", Keywords.TEXTSuffix, tempmd, tempmd);
			if (!dwt.opendatatable(dsut.getfinalvarinfo()))
			{
				return new Result(dwt.getmessage(), false, null);
			}
		}

		String keyword="MultiWebscraper ";
		String description="MultiWebscraper ";
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		int numthread=2;
		String tmp_numthread=(String) parameters.get(Keywords.numthreads);
		if (tmp_numthread==null) tmp_numthread="";
		if (!tmp_numthread.equals(""))
		{
			numthread=-1;
			try
			{
				double tma=Double.parseDouble(tmp_numthread);
				numthread=(int)tma;
			}
			catch (Exception et) {}
			if (numthread<1) return new Result("%3737%<br>\n", false, null);
			if (numthread>1000) return new Result("%3737%<br>\n", false, null);
		}
		if (numwebsites<numthread) numthread=numwebsites;

		if (testproc)
		{
			try
			{
				String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
				bw.write("Number of threads: "+String.valueOf(numthread)+"\n");
				bw.close();
			}
			catch (Exception eft){}
		}

		Keywords.percentage_total=numwebsites;
		Semaphore sem=new Semaphore(1, true);

		WorkQueue queue=ofq.getqueue();

		String tempdir=(String)parameters.get(Keywords.WorkDir);

		ScraperRetriever[] scrret = new ScraperRetriever[numthread];
		for (int i=0; i<scrret.length; i++)
		{
			scrret[i] = new ScraperRetriever(queue, sem, dw, dwt, maxdepth, avoidextension, reftagnames, tempdir, onlyascii, testproc, i, separate_contents, just_html, char_cookie_separator);
			try
			{
				Thread.sleep(100);
			}
			catch (Exception et){}
			scrret[i].start();
		}

		double total_starttime=(new java.util.Date()).getTime();
		double current_time=(new java.util.Date()).getTime();
		int finished=0;
		boolean real_finished=true;
		try
		{
			while (finished<scrret.length)
			{
				finished=0;
				for (int i=0; i<scrret.length; i++)
				{
					if (!scrret[i].getExecutionState()) finished++;
				}
				Thread.sleep(10000);
				current_time=(new java.util.Date()).getTime();
				if (maxtime>0 && ((current_time-total_starttime)/1000)>maxtime)
				{
					finished=scrret.length;
					real_finished=false;
				}
			}
			if (!real_finished)
			{
				queue.force_stop();
				if (testproc)
				{
					try
					{
						String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
						BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
						bw.write("Threads not correctly finished!\n");
						bw.close();
					}
					catch (Exception eft){}
				}
				for (int i=0; i<scrret.length; i++)
				{
					if (scrret[i].getExecutionState())
					{
						scrret[i].forceCloseFiles();
						scrret[i].interrupt();
						if (testproc)
						{
							String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
							BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
							bw.write("Threads "+String.valueOf(i)+" not finished\n");
							bw.close();
						}
					}
				}
			}
			else
			{
				if (testproc)
				{
					try
					{
						String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
						BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
						bw.write("Threads correctly finished\n");
						bw.close();
					}
					catch (Exception eft){}
				}
			}
		}
		catch (Exception e){}
		if (!real_finished)
		{
			results.add(new LocalMessageGetter("%4029%<br>"));
			results.add(new LocalMessageGetter("%4030%: "+String.valueOf(realtreated)+"<br>"));
		}

		for (int j=0; j<tempmsg.size(); j++)
		{
			results.add(new LocalMessageGetter(tempmsg.get(j)+"<br>"));
		}
		if (sitesnu==numwebsites)
		{
			dw.deletetmp();
			if (!testproc) dwt.deletetmp();
			return new Result("%3772%<br>\n", false, null);
		}

		results.add(new LocalMessageGetter("%3762%: "+String.valueOf(numwebsites)+"<br>\n"));
		if (sitesnu>0) results.add(new LocalMessageGetter("%3763%: "+String.valueOf(sitesnu)+"<br>\n"));

		try
		{
			for (int i=0; i<scrret.length; i++)
			{
				scrret[i]=null;
			}
		}
		catch (Exception e){}
		System.gc();

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		results.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		if (!testproc)
		{
			resclose=dwt.close();
			if (!resclose)
				return new Result(dwt.getmessage(), false, null);
			Vector<Hashtable<String, String>> tablevariableinfot=dwt.getVarInfo();
			Hashtable<String, String> datatableinfot=dwt.getTableInfo();
			results.add(new LocalDictionaryWriter(dwt.getdictpath(), keyword, description, author, dwt.gettabletype(),
			datatableinfot, dsut.getfinalvarinfo(), tablevariableinfot, dsut.getfinalcl(), dsut.getfinalmd(), null));
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3513, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"content=", "setting=out", true, 3714, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"tags=", "setting=out", true, 3715, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varmainsite, "var=all", true, 3514, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vardescriptor, "var=all", false, 3524, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varmaxdepth, "var=all", false, 3515, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varfilterurls, "var=all", false, 3516, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varuseragent, "var=all", false, 3517, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcookies, "var=all", false, 3518, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varfollowredirects, "var=all", false, 3519, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varignorecontenttype, "var=all", false, 3520, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varignorehttperrors, "var=all", false, 3521, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varmethod, "var=all", false, 3522, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartimeout, "var=all", false, 3523, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varmaxpages, "var=all", false, 3735, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varmaxtime, "var=all", false, 4025, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.avoidextension, "longtext", false, 3716, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.tagnames, "longtext", false, 3717, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.numthreads,"text", false, 3738,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.maxtime,"text", false, 4027,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.onlyascii, "checkbox", false, 3740, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.separate_contents, "checkbox", false, 4067, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.just_html, "checkbox", false, 4075, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.char_cookie_separator,"text", false, 4089,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4179";
		retprocinfo[1]="3713";
		return retprocinfo;
	}
}
/**
*This is the thread that retrieves the content
*/
class ScraperRetriever extends Thread
{
	boolean separate_contents, just_html;
	Semaphore sem;
	DataWriter dw;
	DataWriter dwt;
    WorkQueue q;
    Vector<String> filterurls;
	JSoupConnection jsco;
	String keywords, response;
	String descriptions, newturl, newUrl;
	String[] outvalues;
	String[] outvaluest;
	boolean issublink, checkfilter, mainredirect, notvalid;
	int maxlevel, timeoutmillis, maxpag, curtotalvisit, totalvisit, maxdepth, tempnrec, refthread, mainstatus, code, headerf;
	String[] tempsub;
	String mainsite, descriptor, tempurl, tempfilterurls, useragent, tempcookies, tempmaxp, tempvalue, smaxdepth, temptext, actuallink, tempmaxt;
	Hashtable<String, String> cookies;
	double tma, starttime, endtime, maxtime, totalmtime;
	boolean followredirects, ignorecontenttype, ignorehttperrors, contverification;
	String method;
	String timeout;
	String tempdir;
	String avalue;
	FastTempDataSet fdt1;
	FastTempDataSet fdt2;
	String[] avoidextension;
	String[] reftagnames;
	String[] tempredfdt;
	String[] doccontent;
	HashSet<String> links_visited;
	Vector<String> links_tovisit;
	Hashtable<String, Vector<String>> parents;
	Vector<String> first_step;
	Vector<String> actual_step;
	String acvalue="";
	String docvalue="";
	Vector<String[]> subsites;
	boolean onlyascii, considerchar;
	boolean isfirststep, retryfs;
	boolean exescraper;
	boolean testproc, ishtml_page;
	boolean cycle_header;
	Vector<String> urls_list;
	Vector<String> newurls_list;
	Vector<String> main_response_codes;
	URL obj;
	Tika tika;
	HttpURLConnection conn;
	boolean tempredirect;
	int tempstatus;
	String content_tp;
	Vector<String> ref_co;
	boolean written_sufficient;
	boolean wrt_record;
	int char_cookie_separator;
	ScraperRetriever(WorkQueue q, Semaphore sem, DataWriter dw, DataWriter dwt, int maxdepth, String[] avoidextension, String[] reftagnames, String tempdir, boolean onlyascii, boolean testproc, int refthread, boolean separate_contents, boolean just_html, int char_cookie_separator)
    {
		cookies=new Hashtable<String, String>();
		this.separate_contents=separate_contents;
		this.just_html=just_html;
        this.q = q;
        this.sem=sem;
        this.dw=dw;
        this.dwt=dwt;
        this.maxdepth=maxdepth;
        this.tempdir=tempdir;
        this.avoidextension=avoidextension;
        this.reftagnames=reftagnames;
        this.testproc=testproc;
        this.refthread=refthread;
		outvaluest=new String[maxdepth+5];
		doccontent=new String[2];
		actual_step=new Vector<String>();
		urls_list=new Vector<String>();
		newurls_list=new Vector<String>();
		main_response_codes=new Vector<String>();
		this.onlyascii=onlyascii;
		exescraper=true;
		ref_co=new Vector<String>();
		this.char_cookie_separator=char_cookie_separator;
    }
    public void forceCloseFiles()
    {
		exescraper=false;
		try
		{
			fdt1.forceClose();
		}
		catch (Exception e){}
		if (!testproc)
		{
			try
			{
				fdt2.forceClose();
			}
			catch (Exception e){}
		}
	}
    public void run()
    {
        try
        {
			exescraper=true;
			ObjectForQueue cofq;
            while (exescraper)
            {
				try
				{
					Thread.sleep(1000);
					sem.acquire();
					cofq=q.getWork();
					sem.release();
				}
				catch (Exception esem)
				{
					if (sem.availablePermits()==0) sem.release();
					exescraper=false;
					break;
				}
                if (cofq == null)
                {
					exescraper=false;
                    break;
                }
                descriptor=cofq.getname();
				if (testproc)
				{
					try
					{
						String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
						BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
						bw.write("Threads "+String.valueOf(refthread)+" main treating: "+descriptor+"\n");
						bw.close();
					}
					catch (Exception eft){}
				}
                subsites=cofq.getcontent();
				keywords="";
				descriptions="";
				totalvisit=0;
				fdt1=new FastTempDataSet(tempdir);
				totalmtime=0;
				urls_list.clear();
				newurls_list.clear();
                for (int s=0; s<subsites.size(); s++)
                {
					if (!testproc)
						fdt2=new FastTempDataSet(tempdir);
					tempsub=subsites.get(s);
					mainsite    = tempsub[0];
					if (!mainsite.toLowerCase().startsWith("http"))
						mainsite="http://"+mainsite;
					mainsite=mainsite.toLowerCase();
					urls_list.add(mainsite);
					method=tempsub[9];
					if (method==null) method="";
					if (method.equals("")) method="GET";
					if (testproc)
					{
						try
						{
							String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
							BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
							bw.write("Threads "+String.valueOf(refthread)+" treating: "+mainsite+"\n");
							bw.close();
						}
						catch (Exception eft){}
					}
					smaxdepth    = tempsub[2];
					maxlevel=1;
					if (!smaxdepth.equals(""))
					{
						maxlevel=-1;
						try
						{
							tma=Double.parseDouble(smaxdepth);
							maxlevel=(int)tma;
						}
						catch (Exception e) {}
						if (maxlevel<0)
						{
							maxlevel=1;
							ProcMultiwebscraper.tempmsg.add("%3539% ("+mainsite+")\n");
						}
					}
					if (testproc) maxlevel=1;
					tempfilterurls    = tempsub[3];
					filterurls=new Vector<String>();
					if (tempfilterurls!=null)
					{
						String[] tempfu=tempfilterurls.split(",");
						for (int j=0; j<tempfu.length; j++)
						{
							if (!tempfu[j].trim().equals("")) filterurls.add(tempfu[j].toLowerCase());
						}
					}
					useragent=tempsub[4];
					tempcookies=tempsub[5];
					cookies.clear();
					if (!tempcookies.equals(""))
					{
						try
						{
							if (char_cookie_separator==-1) char_cookie_separator=59;
							char sepcook=(char)char_cookie_separator;
							String s_sepcook=String.valueOf(sepcook);
							String[] ttcookies=tempcookies.split(s_sepcook);
							for (int j=0; j<ttcookies.length; j++)
							{
								String[] tttcookies=ttcookies[j].split("=");
								cookies.put(tttcookies[0].trim(), tttcookies[1].trim());
							}
						}
						catch (Exception coo)
						{
							ProcMultiwebscraper.tempmsg.add("%3537% ("+mainsite+")\n");
							cookies.clear();
						}
					}
					followredirects =false; if (!tempsub[6].equals("")) followredirects=true;
					ignorecontenttype =false; if (!tempsub[7].equals("")) ignorecontenttype=true;
					ignorehttperrors  =false; if (!tempsub[8].equals("")) ignorehttperrors=true;
					timeout=tempsub[10];
					timeoutmillis=30000;
					tma=0;
					if (!timeout.equals(""))
					{
						timeoutmillis=-1;
						try
						{
							tma=Double.parseDouble(timeout);
							timeoutmillis=(int)tma;
						}
						catch (Exception et) {}
						if (timeoutmillis<0)
						{
							timeoutmillis=30000;
							ProcMultiwebscraper.tempmsg.add("%3538% ("+mainsite+")\n");
						}
					}
					maxpag=-1;
					tempmaxp=tempsub[11];
					if (!tempmaxp.equals(""))
					{
						try
						{
							tma=Double.parseDouble(tempmaxp);
							maxpag=(int)tma;
						}
						catch (Exception et) {}
					}
					if (testproc) maxpag=50;
					maxtime=0;
					tempmaxt=tempsub[12];
					if (!tempmaxt.equals(""))
					{
						try
						{
							maxtime=Double.parseDouble(tempmaxt);
						}
						catch (Exception et)
						{
							maxtime=0;
						}
					}
					if (maxtime<0) maxtime=0;
					getMainSite();
					newurls_list.add(newUrl);
					main_response_codes.add(String.valueOf(code));
					notvalid=false;
					if  (newUrl.equals("")) notvalid=true;
					jsco=new JSoupConnection();
					jsco.setcookies(cookies);
					jsco.setuseragent(useragent);
					jsco.setbooleans(followredirects, ignorecontenttype, ignorehttperrors);
					jsco.setmethod(method);
					jsco.settimeoutmillis(timeoutmillis);
					curtotalvisit=0;
					contverification=true;
					links_visited=new HashSet<String>();
					links_tovisit=new Vector<String>();
					parents=new Hashtable<String, Vector<String>>();
					first_step=new Vector<String>();
					contverification=true;
					actual_step.clear();
					starttime=(new java.util.Date()).getTime();
					ref_co.clear();
					if (analyze(newUrl, first_step) && !notvalid)
					{
						curtotalvisit++;
						while (links_tovisit.size()>0 && contverification)
						{
							actuallink=links_tovisit.remove(0);
							actual_step=parents.get(actuallink);
							endtime=(new java.util.Date()).getTime();
							if (maxtime>0 && (maxtime*1000)<(endtime-starttime))
							{
								contverification=false;
								break;
							}
							if (actual_step.size()<=maxlevel && contverification)
							{
								if (maxpag>0 && curtotalvisit>=maxpag)
								{
									contverification=false;
									break;
								}
								curtotalvisit++;
								if (contverification && !links_visited.contains(actuallink)) analyze(actuallink, actual_step);
							}
						}
					}
					else ProcMultiwebscraper.sitesnu++;
					endtime=(new java.util.Date()).getTime();
					totalmtime=totalmtime+(endtime-starttime)/1000;
					totalvisit=totalvisit+curtotalvisit;
					links_visited.clear();
					links_tovisit.clear();
					parents.clear();
					first_step.clear();
					jsco=null;
					if (!testproc)
					{
						fdt2.endwrite();
						fdt2.opentoread();
						try
						{
							sem.acquire();
							tempnrec=fdt2.getrecords();
							for (int i=0; i<tempnrec; i++)
							{
								dwt.write(fdt2.read());
							}
							sem.release();
						}
						catch (Exception esem)
						{
							if (sem.availablePermits()==0) sem.release();
						}
						fdt2.endread();
						fdt2.deletefile();
						fdt2=null;
					}
					if (testproc)
					{
						try
						{
							String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
							BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
							bw.write("Threads "+String.valueOf(refthread)+" end of treating: "+mainsite+"\n");
							bw.close();
						}
						catch (Exception eft){}
					}
				}
				if (testproc)
				{
					try
					{
						String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
						BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
						bw.write("Threads "+String.valueOf(refthread)+" end of main treating: "+descriptor+"\n");
						bw.close();
					}
					catch (Exception eft){}
				}
				if (!fdt1.endwrite())
				{
					if (testproc)
					{
						try
						{
							String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
							BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
							bw.write("Threads "+String.valueOf(refthread)+" error opening the temporary file: "+fdt1.getmessage()+"\n");
							bw.close();
						}
						catch (Exception eft){}
					}
				}
				if (!fdt1.opentoread())
				{
					if (testproc)
					{
						try
						{
							String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
							BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
							bw.write("Threads "+String.valueOf(refthread)+" error opening the temporary file: "+fdt1.getmessage()+"\n");
							bw.close();
						}
						catch (Exception eft){}
					}
				}
				else
				{
					if (!separate_contents)
					{
						outvalues=new String[9];
						outvalues[0]=descriptor;
						outvalues[1]=String.valueOf(totalvisit);
						outvalues[5]=String.valueOf(totalmtime);
						if (totalvisit==0)
						{
							outvalues[2]="";
							outvalues[3]="";
							outvalues[4]="0";
						}
						else
						{
							outvalues[2]="";
							tempnrec=fdt1.getrecords();
							for (int i=0; i<tempnrec; i++)
							{
								tempredfdt=fdt1.read();
								outvalues[2]=outvalues[2]+tempredfdt[0].replaceAll("\\s+"," ");
								if (i<tempnrec-1)
									outvalues[2]=outvalues[2].trim()+" ";
							}
							outvalues[3]=String.valueOf(outvalues[2].length());
							outvalues[4]="1";
						}
						outvalues[6]="";
						outvalues[7]="";
						outvalues[8]="";
						for (int u=0; u<urls_list.size(); u++)
						{
							outvalues[6]=outvalues[6]+urls_list.get(u);
							outvalues[7]=outvalues[7]+newurls_list.get(u);
							outvalues[8]=outvalues[8]+main_response_codes.get(u);
							if (u<urls_list.size()-1)
							{
								outvalues[6]=outvalues[6]+",";
								if (!outvalues[7].equals("")) outvalues[7]=outvalues[7]+",";
								outvalues[8]=outvalues[8]+",";
							}
						}
						if (!fdt1.endread())
						{
							if (testproc)
							{
								try
								{
									String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
									BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
									bw.write("Threads "+String.valueOf(refthread)+" error opening the temporary file: "+fdt1.getmessage()+"\n");
									bw.close();
								}
								catch (Exception eft){}
							}
						}
						fdt1.deletefile();
						fdt1=null;
						System.gc();
						try
						{
							sem.acquire();
							dw.write(outvalues);
							sem.release();
						}
						catch (Exception esem)
						{
							if (sem.availablePermits()==0) sem.release();
						}
					}
					else
					{
						written_sufficient=false;
						outvalues=new String[10];
						outvalues[0]=descriptor;
						outvalues[1]=String.valueOf(totalvisit);
						outvalues[5]=String.valueOf(totalmtime);
						outvalues[8]="";
						outvalues[9]="";
						if (totalvisit==0)
						{
							outvalues[2]="";
							outvalues[3]="";
							outvalues[4]="0";
							outvalues[6]="";
							outvalues[7]="";
							outvalues[8]="";
							for (int u=0; u<urls_list.size(); u++)
							{
								outvalues[6]=outvalues[6]+urls_list.get(u);
								outvalues[7]=outvalues[7]+newurls_list.get(u);
								outvalues[8]=outvalues[8]+main_response_codes.get(u);
								if (u<urls_list.size()-1)
								{
									outvalues[6]=outvalues[6]+",";
									if (!outvalues[7].equals("")) outvalues[7]=outvalues[7]+",";
									outvalues[8]=outvalues[8]+",";
								}
							}
							if (!fdt1.endread())
							{
								if (testproc)
								{
									try
									{
										String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
										BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
										bw.write("Threads "+String.valueOf(refthread)+" error opening the temporary file: "+fdt1.getmessage()+"\n");
										bw.close();
									}
									catch (Exception eft){}
								}
							}
							fdt1.deletefile();
							fdt1=null;
							System.gc();
							try
							{
								sem.acquire();
								dw.write(outvalues);
								sem.release();
							}
							catch (Exception esem)
							{
								if (sem.availablePermits()==0) sem.release();
							}
						}
						else
						{
							for (int ctp=0; ctp<ref_co.size(); ctp++)
							{
								outvalues[2]="";
								outvalues[3]="";
								outvalues[4]="1";
								outvalues[6]="";
								outvalues[7]="";
								outvalues[8]="";
								outvalues[9]=ref_co.get(ctp);
								for (int u=0; u<urls_list.size(); u++)
								{
									outvalues[6]=outvalues[6]+urls_list.get(u);
									outvalues[7]=outvalues[7]+newurls_list.get(u);
									outvalues[8]=outvalues[8]+main_response_codes.get(u);
									if (u<urls_list.size()-1)
									{
										outvalues[6]=outvalues[6]+",";
										if (!outvalues[7].equals("")) outvalues[7]=outvalues[7]+",";
										outvalues[8]=outvalues[8]+",";
									}
								}
								outvalues[2]="";
								tempnrec=fdt1.getrecords();
								for (int i=0; i<tempnrec; i++)
								{
									tempredfdt=fdt1.read();
									if (tempredfdt[1].equals(ref_co.get(ctp)))
									{
										outvalues[2]=outvalues[2]+tempredfdt[0].replaceAll("\\s+"," ");
										if (i<tempnrec-1)
											outvalues[2]=outvalues[2].trim()+" ";
									}
								}
								outvalues[2]=outvalues[2].trim();
								outvalues[3]=String.valueOf(outvalues[2].length());
								if (!fdt1.endread())
								{
									if (testproc)
									{
										try
										{
											String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
											BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
											bw.write("Threads "+String.valueOf(refthread)+" error opening the temporary file: "+fdt1.getmessage()+"\n");
											bw.close();
										}
										catch (Exception eft){}
									}
								}
								if (ctp==(ref_co.size()-1))
								{
									fdt1.deletefile();
									fdt1=null;
								}
								else
								{
									fdt1.opentoread();
								}
								System.gc();
								wrt_record=true;
								if (written_sufficient && outvalues[2].length()<=1) wrt_record=false;
								if (wrt_record)
								{
									try
									{
										sem.acquire();
										dw.write(outvalues);
										sem.release();
									}
									catch (Exception esem)
									{
										if (sem.availablePermits()==0) sem.release();
									}
								}
								if (!written_sufficient && outvalues[2].length()>1) written_sufficient=true;
							}
						}
					}
				}
				Keywords.percentage_done++;
				ProcMultiwebscraper.realtreated++;
            }
			links_visited=null;
			links_tovisit=null;
			parents=null;
			first_step=null;
			jsco=null;
        }
        catch (Exception ed)
        {
			exescraper=false;
		}
    }
    /**
    *Return true if the thread is executing
    */
    public boolean getExecutionState()
    {
		return exescraper;
	}
	/**
	*Used to iterate trough the links found
	*/
	private boolean analyze(String url, Vector<String> ref_step)
	{
		if (url.equals("")) return false;
		if (!RoboSafe.verify(url)) return false;
		isfirststep=false;
		if (ref_step.size()==0) isfirststep=true;
		temptext="";
		acvalue="";
		docvalue="";
		checkfilter=true;
		Vector<String> alink=new Vector<String>();
		for (int i=0; i<ref_step.size(); i++)
		{
			alink.add(ref_step.get(i));
		}
		alink.add(url);
		links_visited.add(url);
		try
		{
			retryfs=testConnection(url);
			newturl="";
			if (!retryfs)
			{
				if (testproc)
				{
					String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
					BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
					bw.write("Threads "+String.valueOf(refthread)+" not found: "+url+"\n");
					bw.close();
				}
				endtime=(new java.util.Date()).getTime();
				if (maxtime>0 && (maxtime*1000)<(endtime-starttime)) return false;
				if (!isfirststep) return false;
				if (!url.endsWith("/"))
					url=url+"/";
				newturl=url+"index.html";
				if (!testConnection(newturl))
				{
					endtime=(new java.util.Date()).getTime();
					if (maxtime>0 && (maxtime*1000)<(endtime-starttime)) return false;
					newturl=url+"index.php";
					if (!testConnection(newturl))
						return false;
				}
			}
			endtime=(new java.util.Date()).getTime();
			if (maxtime>0 && (maxtime*1000)<(endtime-starttime)) return false;
			if (ishtml_page)
			{
				if (!newturl.equals(""))
				{
					url=newturl;
					alink.add(url);
					links_visited.add(url);
				}
				if (testproc)
				{
					try
					{
						String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
						BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
						bw.write("Threads "+String.valueOf(refthread)+": identified an HTML page at: "+url+"\n");
						bw.close();
					}
					catch (Exception eft){}
				}
				org.jsoup.nodes.Document doc=null;
				try
				{
					doc=jsco.getHtmlDocument(url);
				}
				catch (Exception e)
				{
					if (testproc)
					{
						String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
						BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
						bw.write("Threads "+String.valueOf(refthread)+" error1: "+url+" "+e.toString()+"\n");
						bw.close();
					}
					return false;
				}
				endtime=(new java.util.Date()).getTime();
				if (maxtime>0 && (maxtime*1000)<(endtime-starttime)) return false;
				if (doc==null) return false;
				String alocation=jsco.getRealLocation();
				alocation=alocation.replaceAll("\\s+","%20");
				Elements el=doc.getAllElements();
				Iterator<Element> it_elements = el.iterator();
				HashSet<Vector<String>> tags_info=new HashSet<Vector<String>>();
		 		while(it_elements.hasNext())
		 		{
					Element current=it_elements.next();
					Attributes atts=current.attributes();
					Iterator<Attribute> ite=atts.iterator();
					while(ite.hasNext())
					{
						endtime=(new java.util.Date()).getTime();
						if (maxtime>0 && (maxtime*1000)<(endtime-starttime)) return false;
						Attribute a=ite.next();
						if (a.getKey().equalsIgnoreCase("href") || a.getKey().equalsIgnoreCase("src"))
						{
							issublink=true;
							if (current.tagName().toLowerCase().equalsIgnoreCase("input")) issublink=false;
							if (current.tagName().toLowerCase().equalsIgnoreCase("script")) issublink=false;
							if (current.tagName().toLowerCase().equalsIgnoreCase("img")) issublink=false;
							if (issublink)
							{
								try
								{
									URL aURL = new URL(alocation);
									java.net.URI anURI=aURL.toURI();
									avalue=a.getValue();
									avalue=avalue.replaceAll("\\s+","%20");
									if (avalue.indexOf("://")<0 && !avalue.startsWith("/") && !avalue.endsWith("/")) avalue="/"+avalue;
									if (avalue.indexOf("://")<0 && !avalue.startsWith("/") && avalue.endsWith("/") && !avalue.startsWith("/") && !alocation.endsWith("/")) avalue="/"+avalue;
									java.net.URI resultURI=anURI.resolve(avalue);
									temptext=resultURI.toString();
									if (temptext.indexOf("#")>0) temptext=temptext.substring(0, temptext.indexOf("#"));
									for (int j=0; j<avoidextension.length; j++)
									{
										if (temptext.toLowerCase().endsWith(avoidextension[j].toLowerCase())) issublink=false;
									}
									if (filterurls.size()>0)
									{
										checkfilter=false;
										for (int i=0; i<filterurls.size(); i++)
										{
											if (temptext.toLowerCase().indexOf(filterurls.get(i).toLowerCase())>=0) checkfilter=true;
										}
										if (!checkfilter) issublink=false;
									}
									if (issublink)
									{
										try
										{
											URL ourl = new URL(url);
											URL lurl = new URL(temptext);
											String ohost=ourl.getHost();
											String lhost=lurl.getHost();
											if (!ohost.equalsIgnoreCase(lhost) && !followredirects) issublink=false;
										}
										catch (Exception eu){}
										endtime=(new java.util.Date()).getTime();
										if (maxtime>0 && (maxtime*1000)<(endtime-starttime)) return false;
										if (issublink && !links_visited.contains(temptext))
										{
											if (!links_tovisit.contains(temptext))
											{
												parents.put(temptext, alink);
												links_tovisit.add(temptext);
											}
										}
									}
								}
								catch (Exception e){}
							}
						}
						Vector<String> current_tags_info=new Vector<String>();
						current_tags_info.add((current.tagName()).trim());
						acvalue="";
						if (a.getValue()!=null)
						{
							acvalue=a.getValue().replaceAll("\r"," ");
							acvalue=acvalue.replaceAll("\n"," ");
							acvalue=acvalue.replaceAll("\t"," ");
							acvalue=acvalue.replaceAll("\0"," ");
							acvalue=acvalue.replaceAll("\f"," ");
							acvalue=acvalue.replaceAll("\uFFFD","");
							acvalue=acvalue.replaceAll("\u00A0"," ");
							acvalue=org.apache.commons.lang.StringEscapeUtils.unescapeHtml(acvalue);
							acvalue=acvalue.replaceAll("\\s+"," ");
							acvalue=acvalue.trim();
						}
						if (!onlyascii)
						{
							current_tags_info.add(a.getKey());
							current_tags_info.add(acvalue);
						}
						else
						{
							current_tags_info.add(getAscii(a.getKey()));
							current_tags_info.add(getAscii(acvalue));
						}
						checkfilter=true;
						if (reftagnames!=null)
						{
							checkfilter=false;
							for (int i=0; i<reftagnames.length; i++)
							{
								if (current_tags_info.get(0).equalsIgnoreCase(reftagnames[i])) checkfilter=true;
							}
						}
						if (acvalue.toLowerCase().equals("")) checkfilter=false;
						if (checkfilter) tags_info.add(current_tags_info);
					}
				}
				if (tags_info.size()>0 && !testproc)
				{
					Iterator<Vector<String>> iterti = tags_info.iterator();
					while (iterti.hasNext())
					{
						for (int i=0; i<outvaluest.length; i++)
						{
							outvaluest[i]="";
						}
						outvaluest[0]=descriptor;
						outvaluest[1]=mainsite;
						for (int i=1; i<alink.size(); i++)
						{
							outvaluest[1+i]=alink.get(i);
						}
						Vector<String> current_tags_info=iterti.next();
						outvaluest[2+maxdepth]=current_tags_info.get(0).toLowerCase();
						outvaluest[3+maxdepth]=current_tags_info.get(1).toLowerCase();
						outvaluest[4+maxdepth]=current_tags_info.get(2).toLowerCase();
						if (!outvaluest[4+maxdepth].equals(""))
							fdt2.write(outvaluest);
						current_tags_info.clear();
					}
				}
				tags_info.clear();
				tags_info=null;
				if (testproc)
				{
					try
					{
						String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
						BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
						bw.write("Threads "+String.valueOf(refthread)+" parsing the html page\n");
						bw.close();
					}
					catch (Exception eft){}
				}
				try
				{
					doccontent[0]=doc.body().text().replaceAll("\r"," ");
					if (testproc)
					{
						try
						{
							String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
							BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
							bw.write("Threads "+String.valueOf(refthread)+" length of the html page: "+doccontent[0].length()+"\n");
							bw.close();
						}
						catch (Exception eft){}
					}
					doccontent[0]=doccontent[0].replaceAll("\n"," ");
					doccontent[0]=doccontent[0].replaceAll("\t"," ");
					doccontent[0]=doccontent[0].replaceAll("\0"," ");
					doccontent[0]=doccontent[0].replaceAll("\f"," ");
					doccontent[0]=doccontent[0].replaceAll("\uFFFD","");
					doccontent[0]=doccontent[0].replaceAll("\u00A0"," ");
					doccontent[0]=org.apache.commons.lang.StringEscapeUtils.unescapeHtml(doccontent[0]);
					if (onlyascii) doccontent[0]=getAscii(doccontent[0]);
					doccontent[0]=doccontent[0].toLowerCase();
					doccontent[0]=doccontent[0].replaceAll("\\s+"," ");
					doccontent[1]=content_tp;
					if (!ref_co.contains(content_tp)) ref_co.add(content_tp);
					if (!fdt1.write(doccontent))
					{
						if (testproc)
						{
							try
							{
								String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
								BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
								bw.write("Threads "+String.valueOf(refthread)+" error writing the temporary file: "+fdt1.getmessage()+"\n");
								bw.close();
							}
							catch (Exception eft){}
						}
					}
				}
				catch (Exception enh)
				{
					doccontent[0]="";
					fdt1.write(doccontent);
					if (testproc)
					{
						try
						{
							String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
							BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
							bw.write("Threads "+String.valueOf(refthread)+"error parsing the html page:"+enh.toString()+"\n");
							bw.close();
						}
						catch (Exception eft){}
					}
				}
				return true;
			}
			else
			{
				if (testproc)
				{
					try
					{
						String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
						BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
						bw.write("Threads "+String.valueOf(refthread)+" identified a no HTML page\n");
						bw.close();
					}
					catch (Exception eft){}
				}
				if (!just_html)
				{
					if (testproc)
					{
						try
						{
							String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
							BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
							bw.write("Threads "+String.valueOf(refthread)+" connecting to:"+url+"\n");
							bw.close();
						}
						catch (Exception eft){}
					}
					try
					{
						obj = new URL(url);
						conn = (HttpURLConnection)obj.openConnection();
						conn.setReadTimeout(timeoutmillis);
						conn.setRequestMethod(method);
						conn.connect();
						tempstatus = conn.getResponseCode();
						if (tempstatus == HttpURLConnection.HTTP_OK)
						{
							conn.disconnect();
							tika = new Tika();
							tika.setMaxStringLength(-1);
							doccontent[0]=tika.parseToString(obj);
							if (testproc)
							{
								try
								{
									String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
									BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
									bw.write("Threads "+String.valueOf(refthread)+" length of the content: "+doccontent[0]+"\n");
									bw.close();
								}
								catch (Exception eft){}
							}
							if (doccontent[0]==null)
							{
								if (testproc)
								{
									try
									{
										String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
										BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
										bw.write("Threads "+String.valueOf(refthread)+" content is null\n");
										bw.close();
									}
									catch (Exception eft){}
								}
								doccontent[0]="";
								fdt1.write(doccontent);
								conn.disconnect();
								tika=null;
								return true;
							}
							else
							{
								doccontent[0]=doccontent[0].replaceAll("\r"," ");
								doccontent[0]=doccontent[0].replaceAll("\n"," ");
								doccontent[0]=doccontent[0].replaceAll("\t"," ");
								doccontent[0]=doccontent[0].replaceAll("\0"," ");
								doccontent[0]=doccontent[0].replaceAll("\f"," ");
								doccontent[0]=doccontent[0].replaceAll("\uFFFD","");
								doccontent[0]=doccontent[0].replaceAll("\u00A0"," ");
								doccontent[0]=org.apache.commons.lang.StringEscapeUtils.unescapeHtml(doccontent[0]);
								if (onlyascii) doccontent[0]=getAscii(doccontent[0]);
								doccontent[0]=doccontent[0].toLowerCase();
								doccontent[0]=doccontent[0].replaceAll("\\s+"," ");
								doccontent[1]=content_tp;
								if (!ref_co.contains(content_tp)) ref_co.add(content_tp);
								if (!fdt1.write(doccontent))
								{
									if (testproc)
									{
										try
										{
											String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
											BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
											bw.write("Threads "+String.valueOf(refthread)+" error writing the temporary file: "+fdt1.getmessage()+"\n");
											bw.close();
										}
										catch (Exception eft){}
									}
								}
								tika=null;
								return true;
							}
						}
					}
					catch (EOFException eofeparser)
					{
						return false;
					}
					catch (IOException eioparser)
					{
						return false;
					}
					catch (Exception eparser)
					{
						return false;
					}
					catch (NoClassDefFoundError ncdf)
					{
						return false;
					}
				}
			}
		}
		catch (Exception e)
		{
			if (testproc)
			{
				try
				{
					StringWriter SWex = new StringWriter();
					PrintWriter PWex = new PrintWriter(SWex);
					e.printStackTrace(PWex);
					String fname=System.getProperty(Keywords.WorkDir)+"webscraper_test.txt";
					BufferedWriter bw = new BufferedWriter(new FileWriter(fname, true));
					bw.write("Threads "+String.valueOf(refthread)+" error_general: "+SWex.toString()+"\n");
					bw.close();
				}
				catch (Exception eft){}
			}
			return false;
		}
		return false;
	}
	/**
	*Return the printable ascii chars from a string
	*/
	private String getAscii(String intext)
	{
		StringBuilder start=new StringBuilder();
		start.append(intext);
		StringBuilder end=new StringBuilder();
		int sz = start.length();
		for (int i = 0; i < sz; i++)
		{
			if (start.charAt(i) >= 48 && start.charAt(i) <= 57) end.append(start.charAt(i));
			else if (start.charAt(i) >= 65 && start.charAt(i) <= 90) end.append(start.charAt(i));
			else if (start.charAt(i) >= 97 && start.charAt(i) <= 122) end.append(start.charAt(i));
			else if (start.charAt(i) >= 192 && start.charAt(i) <= 214) end.append(start.charAt(i));
			else if (start.charAt(i) >= 216 && start.charAt(i) <= 246) end.append(start.charAt(i));
			else if (start.charAt(i) >= 249 && start.charAt(i) <= 255) end.append(start.charAt(i));
			else end.append(" ");
		}
		intext=end.toString();
		start=null;
		intext=intext.replaceAll("\\s+"," ");
		intext=intext.trim();
		return intext;
	}
	/**
	*Test and return the redirect main URL
	*/
	private void getMainSite()
	{
		code=0;
		newUrl=mainsite;
		try
		{
			obj = new URL(mainsite);
			conn = (HttpURLConnection)obj.openConnection();
			conn.setReadTimeout(timeoutmillis);
			conn.setRequestMethod(method);
			conn.connect();
			mainredirect = false;
			code = conn.getResponseCode();
			if (code != HttpURLConnection.HTTP_OK)
			{
				if (code == HttpURLConnection.HTTP_MOVED_TEMP
					|| code == HttpURLConnection.HTTP_MOVED_PERM
					|| code == HttpURLConnection.HTTP_SEE_OTHER)
						mainredirect = true;
			}
			if (mainredirect)
			{
				newUrl = conn.getHeaderField("Location");
				if (!newUrl.startsWith("http"))
				{
					if (!mainsite.endsWith("/") && !newUrl.startsWith("/"))
						newUrl=mainsite+"/"+newUrl;
					else newUrl=mainsite+newUrl;
				}
			}
			conn.disconnect();
			obj = new URL(newUrl);
			conn = (HttpURLConnection)obj.openConnection();
			conn.setReadTimeout(timeoutmillis);
			conn.setRequestMethod(method);
			conn.connect();
			code = conn.getResponseCode();
			response = conn.getResponseMessage();
			newUrl=conn.getURL().toString();
			if (!newUrl.startsWith("http"))
			{
				if (!mainsite.endsWith("/") && !newUrl.startsWith("/"))
					newUrl=mainsite+"/"+newUrl;
			}
			conn.disconnect();
    	}
	    catch (Exception e)
		{
	    	code = -1;
	    	newUrl="";
		}
	}
	private boolean testConnection(String dest_url)
	{
		content_tp="";
		ishtml_page=false;
		try
		{
			obj = new URL(dest_url);
			conn = (HttpURLConnection)obj.openConnection();
			conn.setReadTimeout(timeoutmillis);
			conn.setRequestMethod(method);
			conn.connect();
			headerf=0;
			cycle_header=true;
			while (cycle_header)
			{
				if (conn.getHeaderField(headerf+1) == null || conn.getHeaderFieldKey(headerf+1) == null)
					break;
				if (conn.getHeaderFieldKey(headerf+1).equalsIgnoreCase("Content-Type"))
					content_tp=conn.getHeaderField(headerf+1);
				if (conn.getHeaderFieldKey(headerf+1).equalsIgnoreCase("Content-Type") && conn.getHeaderField(headerf+1).toLowerCase().startsWith("text/html"))
				{
					ishtml_page=true;
					break;
				}
				headerf++;
			}
			if (headerf==0) ishtml_page=true;
		    if (content_tp.indexOf("text/html")>=0) ishtml_page=true;
		    if (ishtml_page) content_tp="text/html";
		    if (content_tp.equals(""))
		    {
				content_tp="text/html";
				ishtml_page=true;
			}
			conn.disconnect();
			content_tp=content_tp.toLowerCase();
			content_tp=content_tp.replaceAll(";"," ");
			content_tp=content_tp.trim();
			return true;
		}
	    catch (Exception e)
	    {
		}
		return false;
	}
}
