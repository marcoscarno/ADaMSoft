/**
* Copyright (c) 2018 MS
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

import java.util.*;
import java.net.*;

import ADaMSoft.utilities.JSoupConnection;

import java.util.concurrent.*;

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
* This is the procedure that loads the structure of several web sites that are specified in a data set
* @author marco.scarno@gmail.com
* @date 20/06/2018
*/
public class ProcMultiwebspider implements RunStep
{
	DataWriter dw;
	public static Vector<String> tempmsg;
	public static int sitesnu;
	public static int realtreated;
	/**
	* Starts the execution of Proc MultiWebSpider and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		tempmsg=new Vector<String>();
		sitesnu=0;
		realtreated=0;
		Vector<StepResult> results = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.OUT.toLowerCase(), Keywords.varmainsite};
		String [] optionalparameters=new String[] {Keywords.vardescriptor, Keywords.varmaxdepth, Keywords.varfilterurls,
		Keywords.varuseragent, Keywords.varcookies, Keywords.varfollowredirects, Keywords.varignorecontenttype,
		Keywords.varignorehttperrors, Keywords.varmethod, Keywords.vartimeout, Keywords.varmaxpages, Keywords.numthreads,
		Keywords.maxtime, Keywords.varmaxtime, Keywords.char_cookie_separator};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
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

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

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
		if (!tmp_varcookies.equals("") && pos_varcookies==-1) return new Result("%3530% ("+tmp_varcookies+")\n", false, null);
		if (!tmp_varfollowredirects.equals("") && pos_varfollowredirects==-1) return new Result("%3531% ("+tmp_varfollowredirects+")<br>\n", false, null);
		if (!tmp_varignorecontenttype.equals("") && pos_varignorecontenttype==-1) return new Result("%3532% ("+tmp_varignorecontenttype+")<br>\n", false, null);
		if (!tmp_varignorehttperrors.equals("") && pos_varignorehttperrors==-1) return new Result("%3533% ("+tmp_varignorehttperrors+")<br>\n", false, null);
		if (!tmp_varmethod.equals("") && pos_varmethod==-1) return new Result("%3534% ("+tmp_varmethod+")<br>\n", false, null);
		if (!tmp_vartimeout.equals("") && pos_vartimeout==-1) return new Result("%3535% ("+tmp_vartimeout+")<br>\n", false, null);
		if (!tmp_varmaxpages.equals("") && pos_varmaxpages==-1) return new Result("%3736% ("+tmp_varmaxpages+")<br>\n", false, null);
		if (!tmp_varmaxtime.equals("") && pos_varmaxtime==-1) return new Result("%4026% ("+tmp_varmaxtime+")<br>\n", false, null);

		dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DataReader data = new DataReader(dict);

		if (!data.open(null, 1, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		ObjectsForQueue ofq=new ObjectsForQueue();

		String[] values=null;
		int numwebsites=0;
		String refn="";
		String verifymd="";
		int tempint;
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
		ofq.fillqueue();

		DataSetUtilities dsu=new DataSetUtilities();
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		dsu.addnewvar("site_descriptor", "%3536%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("main_site", "%3491%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("parent", "%3460%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("url", "%3461%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("level", "%3462%", Keywords.NUMSuffix+"I", tempmd, tempmd);
		dsu.addnewvar("url_keyword", "%3465%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("url_description", "%3464%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("html_doctype", "%3492%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("page_type", "%3493%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("doc_size", "%3494%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
		dsu.addnewvar("response_time", "%3495%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
		Hashtable<String, String> clt=new Hashtable<String, String>();
		clt.put("1", "%3509%");
		clt.put("0", "%3510%");
		dsu.addnewvar("query_result", "%3508%", Keywords.TEXTSuffix, clt, tempmd);
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			return new Result(dw.getmessage(), false, null);
		}

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

		Keywords.percentage_total=numwebsites;

		Semaphore sem=new Semaphore(1, true);

		WorkQueue queue=ofq.getqueue();

		String tempdir=(String)parameters.get(Keywords.WorkDir);

		SpiderRetriever[] spiret = new SpiderRetriever[numthread];
		for (int i=0; i<spiret.length; i++)
		{
			spiret[i] = new SpiderRetriever(queue, sem, dw, tempdir, char_cookie_separator);
			spiret[i].start();
		}

		String keyword="MultiWebspider ";
		String description="MultiWebspider ";
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		double total_starttime=(new java.util.Date()).getTime();
		double current_time=(new java.util.Date()).getTime();
		int finished=0;
		boolean real_finished=true;
		try
		{
			while (finished<spiret.length)
			{
				finished=0;
				for (int i=0; i<spiret.length; i++)
				{
					if (!spiret[i].getExecutionState()) finished++;
				}
				Thread.sleep(10000);
				current_time=(new java.util.Date()).getTime();
				if (maxtime>0 && ((current_time-total_starttime)/1000)>maxtime)
				{
					finished=spiret.length;
					real_finished=false;
				}
			}
			if (!real_finished)
			{
				queue.force_stop();
				for (int i=0; i<spiret.length; i++)
				{
					if (spiret[i].getExecutionState())
					{
						spiret[i].forceCloseFiles();
						spiret[i].interrupt();
					}
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
			return new Result("%3772%<br>\n", false, null);
		}

		results.add(new LocalMessageGetter("%3762%: "+String.valueOf(numwebsites)+"<br>\n"));
		if (sitesnu>0) results.add(new LocalMessageGetter("%3763%: "+String.valueOf(sitesnu)+"<br>\n"));

		try
		{
			for (int i=0; i<spiret.length; i++)
			{
				spiret[i]=null;
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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
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
		parameters.add(new GetRequiredParameters(Keywords.numthreads,"text", false, 3738,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.maxtime,"text", false, 4027,dep,"",2));
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
		retprocinfo[1]="3512";
		return retprocinfo;
	}
}
/**
*This is the thread that retrieves the content
*/
class SpiderRetriever extends Thread
{
	Semaphore sem;
	DataWriter dw;
    WorkQueue q;
	String tempdir, descriptor, mainsite, smaxdepth, tempfilterurls, useragent, tempcookies, method, timeout, tempmaxpag, ptype,
			tempuv, temp_keyword, temp_description, thtml_doctype, temptext, acvalue, docvalue, tempmaxp, actuallink, avalue,
			newturl, tempmaxt, newUrl, response;
	boolean followredirects, ignorecontenttype, ignorehttperrors, contverification, isfirststep, checkfilter, retryfs, issublink;
	Hashtable<String, String> cookies;
	int sitesnu, maxlevel, timeoutmillis, maxpag, curtotalvisit, totalvisit, tdoc_size, tempnrec, mainstatus;
	String[] outvalues;
	Vector<String> actual_step;
	Vector<String[]> subsites;
	String[] tempsub;
	double tma, maxtime, starttime, endtime;
	JSoupConnection jsco;
	HashSet<String> links_visited;
	Vector<String> links_tovisit;
	Hashtable<String, Vector<String>> parents;
	Vector<String> first_step;
	long start_time, end_time;
	URLConnection conn;
	FastTempDataSet fdt;
	Vector<String> filterurls;
	boolean exespdider, mainredirect;
	URL obj;
	HttpURLConnection conne;
	int char_cookie_separator;
	SpiderRetriever(WorkQueue q, Semaphore sem, DataWriter dw, String tempdir, int char_cookie_separator)
    {
		cookies=new Hashtable<String, String>();
        this.q = q;
        this.sem=sem;
        this.dw=dw;
        this.tempdir=tempdir;
        sitesnu=0;
		outvalues=new String[12];
		actual_step=new Vector<String>();
		exespdider=true;
		this.char_cookie_separator=char_cookie_separator;
    }
    public void forceCloseFiles()
    {
		try
		{
			fdt.forceClose();
		}
		catch (Exception e){}
		exespdider=false;
	}
    /**
    *Return true if the thread is executing
    */
    public boolean getExecutionState()
    {
		return exespdider;
	}
    public void run()
    {
        try
        {
			exespdider=true;
			ObjectForQueue cofq;
            while (exespdider)
            {
				try
				{
					sem.acquire();
					cofq=q.getWork();
					sem.release();
				}
				catch (Exception esem)
				{
					if (sem.availablePermits()==0) sem.release();
					exespdider=false;
					break;
				}
                if (cofq == null)
                {
					exespdider=false;
                    break;
                }
                descriptor=cofq.getname();
                subsites=cofq.getcontent();
				totalvisit=0;
                for (int s=0; s<subsites.size(); s++)
                {
					fdt=new FastTempDataSet(tempdir);
					tempsub=subsites.get(s);
					mainsite    = tempsub[0];
					if (!mainsite.toLowerCase().startsWith("http"))
						mainsite="http://"+mainsite;
					mainsite=mainsite.toLowerCase();
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
							ProcMultiwebspider.tempmsg.add("%3539% ("+mainsite+")\n");
						}
					}
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
							ProcMultiwebspider.tempmsg.add("%3537% ("+mainsite+")\n");
							cookies.clear();
						}
					}
					followredirects =false; if (!tempsub[6].equals("")) followredirects=true;
					ignorecontenttype =false; if (!tempsub[7].equals("")) ignorecontenttype=true;
					ignorehttperrors  =false; if (!tempsub[8].equals("")) ignorehttperrors=true;
					method=tempsub[9];
					if (method==null) method="";
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
							ProcMultiwebspider.tempmsg.add("%3538% ("+mainsite+")\n");
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
					getMainSite();
					if (analyze(newUrl, first_step) && !newUrl.equals(""))
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
								analyze(actuallink, actual_step);
							}
						}
					}
					else ProcMultiwebspider.sitesnu++;
					totalvisit=totalvisit+curtotalvisit;
					links_visited.clear();
					links_tovisit.clear();
					parents.clear();
					first_step.clear();
					jsco=null;
					fdt.endwrite();
					fdt.opentoread();
					sem.acquire();
					tempnrec=fdt.getrecords();
					for (int i=0; i<tempnrec; i++)
					{
						dw.write(fdt.read());
					}
					sem.release();
					fdt.endread();
					fdt.deletefile();
					fdt=null;
					System.gc();
				}
				Keywords.percentage_done++;
				ProcMultiwebspider.realtreated++;
            }
			links_visited=null;
			links_tovisit=null;
			parents=null;
			first_step=null;
			jsco=null;
        }
        catch (InterruptedException e)
        {
			exespdider=false;
		}
        catch (Exception ed)
        {
			exespdider=false;
		}
    }
	/**
	*Used to iterate trough the links found
	*/
	private boolean analyze(String url, Vector<String> ref_step)
	{
		if (!RoboSafe.verify(url)) return false;
		start_time = System.currentTimeMillis();
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
			retryfs=false;
			Document doc=null;
			try
			{
				doc=jsco.getHtmlDocument(url);
			}
			catch (Exception e)
			{
				retryfs=true;
			}
			if (doc==null) retryfs=true;
			if (!isfirststep && retryfs)
			{
				outvalues[0]=descriptor;
				outvalues[1]=mainsite;
				outvalues[2]="";
				if (ref_step.size()>0) outvalues[2]=ref_step.get(ref_step.size()-1);
				outvalues[3]=url;
				outvalues[4]=String.valueOf(ref_step.size());
				outvalues[5]="";
				outvalues[6]="";
				outvalues[7]="";
				outvalues[8]=jsco.getDocType();
				outvalues[9]="";
				if (jsco.getDocSize()>0) outvalues[9]=String.valueOf(jsco.getDocSize());
				end_time = System.currentTimeMillis();
				outvalues[10]=String.valueOf(end_time-start_time);
				outvalues[11]="0";
				if (jsco.getDocSize()>0) outvalues[11]="1";
				fdt.write(outvalues);
				return false;
			}
			if (isfirststep && retryfs)
			{
				retryfs=false;
				doc=null;
				try
				{
					newturl=url;
					if (!newturl.endsWith("/")) newturl=newturl+"/";
					doc=jsco.getHtmlDocument(newturl+"index.html");
				}
				catch (Exception e)
				{
					retryfs=true;
				}
				if (doc==null) retryfs=true;
				else url=newturl;
				if (retryfs)
				{
					retryfs=false;
					doc=null;
					try
					{
						newturl=url;
						if (!newturl.endsWith("/")) newturl=newturl+"/";
						doc=jsco.getHtmlDocument(newturl+"index.php");
					}
					catch (Exception e)
					{
						retryfs=true;
					}
					if (doc==null) retryfs=true;
					else url=newturl;
					if (retryfs)
					{
						outvalues[0]=descriptor;
						outvalues[1]=mainsite;
						outvalues[2]="";
						outvalues[3]=url;
						outvalues[4]="0";
						outvalues[5]="";
						outvalues[6]="";
						outvalues[7]="";
						outvalues[8]=jsco.getDocType();
						outvalues[9]="";
						if (jsco.getDocSize()>0) outvalues[9]=String.valueOf(jsco.getDocSize());
						end_time = System.currentTimeMillis();
						outvalues[10]=String.valueOf(end_time-start_time);
						outvalues[11]="0";
						if (jsco.getDocSize()>0) outvalues[11]="1";
						fdt.write(outvalues);
						return false;
					}
				}
			}
			String alocation=jsco.getRealLocation();
			alocation=alocation.replaceAll("\\s+","&nbsp;");
			Elements el=doc.getAllElements();
			Iterator<Element> it_elements = el.iterator();
	 		while(it_elements.hasNext())
	 		{
				Element current=it_elements.next();
				Attributes atts=current.attributes();
				Iterator<Attribute> ite=atts.iterator();
				while(ite.hasNext())
				{
					Attribute a=ite.next();
					if (a.getKey().equalsIgnoreCase("href"))
					{
						issublink=true;
						if (current.tagName().toLowerCase().equalsIgnoreCase("input")) issublink=false;
						if (current.tagName().toLowerCase().equalsIgnoreCase("script")) issublink=false;
						if (current.tagName().toLowerCase().equalsIgnoreCase("img")) issublink=false;
						try
						{
							URL aURL = new URL(alocation);
							java.net.URI anURI=aURL.toURI();
							if (a.getValue()!=null)
							{
								avalue=a.getValue();
								avalue=avalue.replaceAll("\\s+","%20");
								if (avalue.indexOf("://")<0 && !avalue.startsWith("/") && !avalue.endsWith("/")) avalue="/"+avalue;
								if (avalue.indexOf("://")<0 && !avalue.startsWith("/") && avalue.endsWith("/") && !avalue.startsWith("/") && !alocation.endsWith("/")) avalue="/"+avalue;
								java.net.URI resultURI=anURI.resolve(avalue);
								temptext=resultURI.toString();
								if (temptext.indexOf("#")>0) temptext=temptext.substring(0, temptext.indexOf("#"));
								if (filterurls.size()>0)
								{
									checkfilter=false;
									for (int i=0; i<filterurls.size(); i++)
									{
										if (temptext.toLowerCase().indexOf(filterurls.get(i).toLowerCase())>=0) checkfilter=true;
									}
									if (!checkfilter) issublink=false;
								}
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
			}
			outvalues[0]=descriptor;
			outvalues[1]=mainsite;
			outvalues[2]="";
			if (ref_step.size()>0) outvalues[2]=ref_step.get(ref_step.size()-1);
			outvalues[3]=url;
			outvalues[4]=String.valueOf(ref_step.size());
			getdocinfo(doc);
			outvalues[5]=temp_keyword;
			outvalues[6]=temp_description;
			outvalues[7]=thtml_doctype;
			outvalues[8]=jsco.getDocType();
			outvalues[9]="";
			outvalues[11]="0";
			if (jsco.getDocSize()>0)
			{
				outvalues[9]=String.valueOf(jsco.getDocSize());
				outvalues[11]="1";
			}
			end_time = System.currentTimeMillis();
			outvalues[10]=String.valueOf(end_time-start_time);
			fdt.write(outvalues);
			return true;
		}
		catch (Exception e)
		{
			outvalues[0]=descriptor;
			outvalues[1]=mainsite;
			outvalues[2]="";
			if (ref_step.size()>0) outvalues[2]=ref_step.get(ref_step.size()-1);
			outvalues[3]=url;
			outvalues[4]=String.valueOf(ref_step.size());
			outvalues[5]="";
			outvalues[6]="";
			outvalues[7]="";
			outvalues[8]=jsco.getDocType();
			outvalues[9]="";
			if (jsco.getDocSize()>0) outvalues[9]=String.valueOf(jsco.getDocSize());
			end_time = System.currentTimeMillis();
			outvalues[10]=String.valueOf(end_time-start_time);
			outvalues[11]="0";
			if (jsco.getDocSize()>0) outvalues[11]="1";
			fdt.write(outvalues);
			return false;
		}
	}
	private void getdocinfo(Document doc)
	{
		temp_keyword="";
		temp_description="";
		thtml_doctype="";
		try
		{
			List<org.jsoup.nodes.Node>nods = doc.childNodes();
			for (org.jsoup.nodes.Node node : nods)
			{
				if (node instanceof DocumentType)
				{
					DocumentType documentType = (DocumentType)node;
					thtml_doctype=documentType.toString();
					thtml_doctype=thtml_doctype.replaceAll("<","");
					thtml_doctype=thtml_doctype.replaceAll(">","");
					thtml_doctype=thtml_doctype.replaceAll("!DOCTYPE","");
					thtml_doctype=thtml_doctype.trim();
					if (thtml_doctype.toLowerCase().indexOf("htm")>=0) thtml_doctype="html";
						break;
				}
			}
		}
		catch (Exception edt) {}
		Elements metak = doc.select("meta[name=keywords]");
		Elements metad = doc.select("meta[name=description]");
		for (Element link : metak)
		{
			tempuv=link.toString();
			try
			{
				if (tempuv.indexOf("content=\"")>0)
				{
					tempuv=tempuv.substring(tempuv.indexOf("content=\"")+9);
					tempuv=tempuv.substring(0, tempuv.indexOf("\""));
					tempuv=tempuv.replaceAll("\t"," ");
					tempuv=tempuv.replaceAll("\0"," ");
					tempuv=tempuv.replaceAll("\f"," ");
					tempuv=tempuv.replaceAll("\r"," ");
					tempuv=tempuv.replaceAll("\n"," ");
					tempuv=tempuv.replaceAll("\n"," ");
					tempuv=tempuv.replaceAll("\\s+"," ");
					tempuv=tempuv.trim();
					if (!tempuv.equals(""))
						temp_keyword+=tempuv;
				}
			}
			catch (Exception er) {}
		}
		for (Element link : metad)
		{
			tempuv=link.toString();
			try
			{
				if (tempuv.indexOf("content=\"")>0)
				{
					tempuv=tempuv.substring(tempuv.indexOf("content=\"")+9);
					tempuv=tempuv.substring(0, tempuv.indexOf("\""));
					tempuv=tempuv.replaceAll("\t"," ");
					tempuv=tempuv.replaceAll("\0"," ");
					tempuv=tempuv.replaceAll("\f"," ");
					tempuv=tempuv.replaceAll("\r"," ");
					tempuv=tempuv.replaceAll("\n"," ");
					tempuv=tempuv.replaceAll("\n"," ");
					tempuv=tempuv.replaceAll("\\s+"," ");
					tempuv=tempuv.trim();
					if (!tempuv.equals(""))
						temp_description+=tempuv;
				}
			}
			catch (Exception er) {}
		}
	}
	/**
	*Test and return the redirect main URL
	*/
	private void getMainSite()
	{
		mainstatus=0;
		newUrl=mainsite;
		try
		{
			obj = new URL(mainsite);
			conne = (HttpURLConnection)obj.openConnection();
			conne.setReadTimeout(timeoutmillis);
			if (!method.equals("")) conne.setRequestMethod(method);
			conne.connect();
			mainredirect = false;
			mainstatus = conne.getResponseCode();
			if (mainstatus != HttpURLConnection.HTTP_OK)
			{
				if (mainstatus == HttpURLConnection.HTTP_MOVED_TEMP
					|| mainstatus == HttpURLConnection.HTTP_MOVED_PERM
					|| mainstatus == HttpURLConnection.HTTP_SEE_OTHER)
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
			conne.disconnect();
			obj = new URL(newUrl);
			conne = (HttpURLConnection)obj.openConnection();
			conne.setReadTimeout(timeoutmillis);
			if (!method.equals("")) conne.setRequestMethod(method);
			conne.connect();
			mainstatus = conne.getResponseCode();
			response = conne.getResponseMessage();
			newUrl=conne.getURL().toString();
			conne.disconnect();
			if (!newUrl.startsWith("http"))
			{
				if (!mainsite.endsWith("/") && !newUrl.startsWith("/"))
					newUrl=mainsite+"/"+newUrl;
			}
    	}
	    catch (Exception e)
		{
	    	mainstatus = -1;
	    	newUrl="";
		}
	}
}

