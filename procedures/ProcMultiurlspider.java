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

import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.util.*;
import java.net.*;
import java.util.concurrent.*;

import ADaMSoft.utilities.JSoupConnection;
import ADaMSoft.utilities.WorkQueue;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.utilities.ObjectsForQueue;
import ADaMSoft.utilities.ObjectForQueue;
import ADaMSoft.utilities.RoboSafe;

/**
* This is the procedure that identifies the external urls linked from web sites that are specified in a data set
* @author marco.scarno@gmail.com
* @date 19/02/2017
*/
public class ProcMultiurlspider implements RunStep
{
	DataWriter dw;
	public static Vector<String> tempmsg;
	public static int sitesnu;
	public static int realtreated;
	/**
	* Starts the execution of Proc MultiUrlSpider and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		tempmsg=new Vector<String>();
		sitesnu=0;
		realtreated=0;
		Vector<StepResult> result = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.OUT.toLowerCase(), Keywords.varmainsite};
		String [] optionalparameters=new String[] {Keywords.vardescriptor, Keywords.varmaxdepth, Keywords.varuseragent,
		Keywords.varcookies, Keywords.varfollowredirects, Keywords.varignorecontenttype, Keywords.varignorehttperrors,
		Keywords.varmethod, Keywords.vartimeout, Keywords.varmaxpages, Keywords.varmaxtime, Keywords.numthreads, Keywords.maxtime, Keywords.char_cookie_separator};
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
		if (!tmp_varcookies.equals("") && pos_varcookies==-1) return new Result("%3530% ("+tmp_varcookies+")<br>\n", false, null);
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
		String[] values=null;

		int numwebsites=0;

		ObjectsForQueue ofq=new ObjectsForQueue();
		String refn="";

		int tempint=0;
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
					refv[2]=values[pos_varmaxdepth];
					tempint=-1;
					try
					{
						double tempd=Double.parseDouble(values[pos_varmaxdepth]);
						tempint=(int)tempd;
					}
					catch (Exception ed){}
					if (tempint>100 || tempint<0) verifymd=verifymd+values[pos_varmaxdepth]+" ";
				}
				else refv[2]="";
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
		Keywords.percentage_done=0;

		DataSetUtilities dsu=new DataSetUtilities();
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		dsu.addnewvar("site_descriptor", "%3536%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("parent_keyword", "%3465%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("parent_description", "%3464%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("linked_url", "%3730%", Keywords.TEXTSuffix, tempmd, tempmd);
		Hashtable<String, String> clt=new Hashtable<String, String>();
		clt.put("1", "%3509%");
		clt.put("0", "%3510%");
		dsu.addnewvar("link_visited", "%3731%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("query_result", "%3508%", Keywords.TEXTSuffix, clt, tempmd);
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			return new Result(dw.getmessage(), false, null);
		}

		Semaphore sem=new Semaphore(1, true);

		WorkQueue queue=ofq.getqueue();

		UrlRetriever[] urlret = new UrlRetriever[numthread];
		for (int i=0; i<urlret.length; i++)
		{
			urlret[i] = new UrlRetriever(queue, sem, dw, char_cookie_separator);
			try
			{
				Thread.sleep(100);
			}
			catch (Exception et){}
			urlret[i].start();
		}
		double total_starttime=(new java.util.Date()).getTime();
		double current_time=(new java.util.Date()).getTime();
		int finished=0;
		boolean real_finished=true;
		try
		{
			while (finished<urlret.length)
			{
				finished=0;
				for (int i=0; i<urlret.length; i++)
				{
					if (!urlret[i].getExecutionState()) finished++;
				}
				Thread.sleep(10000);
				current_time=(new java.util.Date()).getTime();
				if (maxtime>0 && ((current_time-total_starttime)/1000)>maxtime)
				{
					finished=urlret.length;
					real_finished=false;
				}
			}
			if (!real_finished)
			{
				queue.force_stop();
				for (int i=0; i<urlret.length; i++)
				{
					if (urlret[i].getExecutionState())
					{
						urlret[i].endexeurlspider();
						urlret[i].interrupt();
					}
				}
			}
		}
		catch (Exception e){}
		if (!real_finished)
		{
			result.add(new LocalMessageGetter("%4029%<br>"));
			result.add(new LocalMessageGetter("%4030%: "+String.valueOf(realtreated)+"<br>"));
		}

		String keyword="MultiUrlspider ";
		String description="MultiUrlspider ";
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		for (int j=0; j<tempmsg.size(); j++)
		{
			result.add(new LocalMessageGetter(tempmsg.get(j)+"<br>"));
		}

		try
		{
			for (int i=0; i<urlret.length; i++)
			{
				urlret[i]=null;
			}
		}
		catch (Exception e){}
		System.gc();

		if (sitesnu==numwebsites)
		{
			dw.deletetmp();
			return new Result("%3772%<br>\n", false, null);
		}

		result.add(new LocalMessageGetter("%3733%: "+String.valueOf(numwebsites)+"<br>\n"));
		result.add(new LocalMessageGetter("%3734%: "+String.valueOf(sitesnu)+"<br>\n"));

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
		retprocinfo[1]="3732";
		return retprocinfo;
	}
}
/*
*This contains the information on the actual URL and level visited
*/
class LinkLevel
{
	int level=0;
	String link="";
	public LinkLevel(int level, String link)
	{
		this.level=level;
		this.link=link;
	}
	public String getlink()
	{
		return link;
	}
	public int getlevel()
	{
		return level;
	}
}

/**
*This is the thread to identify the external URL
*/
class UrlRetriever extends Thread
{
	Iterator<String> itl;
	Semaphore sem;
	DataWriter dw;
    WorkQueue q;
    Vector<String> filterurls;
	JSoupConnection jsco;
	ArrayList<LinkLevel> internallink=new ArrayList<LinkLevel>();
	HashSet<String> externallink=new HashSet<String>();
	HashSet<String> visitedlinks=new HashSet<String>();
	HashSet<String> extlinks=new HashSet<String>();
	String keywords, tempmaxt, newUrl;
	String descriptions;
	String[] outvalues;
	int maxlevel, timeoutmillis, maxpag, curtotalvisit, totalvisit, code, mainstatus;
	Vector<String[]> subsites;
	String[] tempsub;
	String mainsite, descriptor, tempurl, tempfilterurls, maxdepth, useragent, tempcookies, tempmaxp, tempvalue;
	Hashtable<String, String> cookies;
	double tma, starttime, endtime, maxtime, totalmtime;
	boolean followredirects, ignorecontenttype, ignorehttperrors, contverification;
	String method;
	String timeout, responsemsg;
	boolean exeurlspider, mainredirect;
	int culevel;
	URL obj;
	HttpURLConnection conn;
	int char_cookie_separator;
    UrlRetriever(WorkQueue q, Semaphore sem, DataWriter dw, int char_cookie_separator)
    {
		cookies=new Hashtable<String, String>();
        this.q = q;
        this.sem=sem;
        this.dw=dw;
		outvalues=new String[6];
		internallink=new ArrayList<LinkLevel>();
		externallink=new HashSet<String>();
		visitedlinks=new HashSet<String>();
		extlinks=new HashSet<String>();
		exeurlspider=true;
		this.char_cookie_separator=char_cookie_separator;
    }
    /**
    *Return true if the thread is executing
    */
    public boolean getExecutionState()
    {
		return exeurlspider;
	}
	public void endexeurlspider()
	{
		exeurlspider=false;
	}
    public void run()
    {
        try
        {
			exeurlspider=true;
			ObjectForQueue cofq;
            while (exeurlspider)
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
					exeurlspider=false;
					break;
				}
                if (cofq == null)
                {
					exeurlspider=false;
                    break;
                }
                descriptor=cofq.getname();
                subsites=cofq.getcontent();
				externallink.clear();
				extlinks.clear();
				visitedlinks.clear();
				internallink.clear();
				keywords="";
				descriptions="";
				totalvisit=0;
                for (int s=0; s<subsites.size(); s++)
                {
					tempsub=subsites.get(s);
					mainsite    = tempsub[0];
					if (!mainsite.toLowerCase().startsWith("http"))
						mainsite="http://"+mainsite;
					mainsite=mainsite.toLowerCase();
					maxdepth    = tempsub[2];
					maxlevel=2;
					if (!maxdepth.equals(""))
					{
						maxlevel=-1;
						try
						{
							tma=Double.parseDouble(maxdepth);
							maxlevel=(int)tma;
						}
						catch (Exception e) {}
						if (maxlevel<0)
						{
							maxlevel=2;
							ProcMultiurlspider.tempmsg.add("%3539% ("+mainsite+")<br>\n");
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
							ProcMultiurlspider.tempmsg.add("%3537% ("+mainsite+")<br>\n");
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
							ProcMultiurlspider.tempmsg.add("%3538% ("+mainsite+")\n");
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
					starttime=(new java.util.Date()).getTime();
					getMainSite();
					if (!newUrl.equals(""))
					{
						try
						{
							if (startSearch(0, newUrl))
							{
								visitedlinks.add(newUrl);
								curtotalvisit++;
								if (internallink.size()>0)
								{
									while (internallink.size()>0 && contverification)
									{
										LinkLevel templl=internallink.get(0);
										tempurl=templl.getlink();
										culevel=templl.getlevel();
										internallink.remove(0);
										curtotalvisit++;
										endtime=(new java.util.Date()).getTime();
										if (maxtime>0 && (maxtime*1000)<(endtime-starttime))
										{
											contverification=false;
											break;
										}
										if (maxpag>0 && curtotalvisit>=maxpag && contverification)
										{
											contverification=false;
											break;
										}
										if (culevel<=maxlevel && contverification) startSearch(culevel, tempurl);
										else contverification=false;
										if (internallink.isEmpty()) contverification=false;
									}
								}
							}
							totalvisit=totalvisit+curtotalvisit;
						}
						catch (Exception ef){}
					}
				}
				if (externallink.size()>0)
				{
					sem.acquire();
					itl= externallink.iterator();
					while (itl.hasNext())
					{
						tempurl=itl.next();
						outvalues[0]=descriptor;
						outvalues[1]=keywords;
						outvalues[2]=descriptions;
						outvalues[3]=tempurl;
						outvalues[4]=String.valueOf(totalvisit);
						if (totalvisit>0) outvalues[5]="1";
						if (totalvisit==0) outvalues[5]="0";
						dw.write(outvalues);
					}
					sem.release();
				}
				else
				{
					ProcMultiurlspider.sitesnu++;
					outvalues[0]=descriptor;
					outvalues[1]=keywords;
					outvalues[2]=descriptions;
					outvalues[3]="";
					outvalues[4]=String.valueOf(totalvisit);
					if (totalvisit>0) outvalues[5]="1";
					if (totalvisit==0) outvalues[5]="0";
					sem.acquire();
					dw.write(outvalues);
					sem.release();
				}
				extlinks.clear();
				externallink.clear();
				internallink.clear();
				visitedlinks.clear();
				ProcMultiurlspider.realtreated++;
				Keywords.percentage_done++;
            }
			jsco=null;
			internallink.clear();
			externallink.clear();
			visitedlinks.clear();
			extlinks.clear();
			internallink=null;
			externallink=null;
			visitedlinks=null;
			extlinks=null;
        }
        catch (InterruptedException e)
        {
			exeurlspider=false;
		}
        catch (Exception ed)
        {
			exeurlspider=false;
		}
    }
	/**
	*Used to iterate trough the links found
	*/
    private boolean startSearch(int clevel, String url)
    {
		if (!RoboSafe.verify(url)) return false;
		tempvalue="";
		try
		{
			Document doc = null;
			try
			{
				doc=jsco.getHtmlDocument(url);
			}
			catch (Exception e)
			{
				return false;
			}
			if (doc==null) return false;
			if (clevel==0)
			{
				String tempdescriptions="";
				String tempkeywords="";
				Elements metak = doc.select("meta[name=keywords]");
				Elements metad = doc.select("meta[name=description]");
				for (Element link : metak)
				{
					tempvalue=link.toString();
					try
					{
						if (tempvalue.indexOf("content=\"")>0)
						{
							tempvalue=tempvalue.substring(tempvalue.indexOf("content=\"")+9);
							tempvalue=tempvalue.substring(0, tempvalue.indexOf("\""));
							tempvalue=tempvalue.replaceAll("\t"," ");
							tempvalue=tempvalue.replaceAll("\0"," ");
							tempvalue=tempvalue.replaceAll("\f"," ");
							tempvalue=tempvalue.replaceAll("\r"," ");
							tempvalue=tempvalue.replaceAll("\n"," ");
							tempvalue=tempvalue.replaceAll("\n"," ");
							tempvalue=tempvalue.replaceAll("\\s+"," ");
							tempvalue=tempvalue.trim();
							if (!tempvalue.equals(""))
								tempkeywords+=tempvalue;
						}
					}
					catch (Exception er) {}
				}
				for (Element link : metad)
				{
					tempvalue=link.toString();
					try
					{
						if (tempvalue.indexOf("content=\"")>0)
						{
							tempvalue=tempvalue.substring(tempvalue.indexOf("content=\"")+9);
							tempvalue=tempvalue.substring(0, tempvalue.indexOf("\""));
							tempvalue=tempvalue.replaceAll("\t"," ");
							tempvalue=tempvalue.replaceAll("\0"," ");
							tempvalue=tempvalue.replaceAll("\f"," ");
							tempvalue=tempvalue.replaceAll("\r"," ");
							tempvalue=tempvalue.replaceAll("\n"," ");
							tempvalue=tempvalue.replaceAll("\n"," ");
							tempvalue=tempvalue.replaceAll("\\s+"," ");
							tempvalue=tempvalue.trim();
							if (!tempvalue.equals(""))
								tempdescriptions+=tempvalue;
						}
					}
					catch (Exception er) {}
				}
				metak=null;
				metad=null;
				keywords=tempkeywords;
				descriptions=tempdescriptions;
			}
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
					if (a.getKey().equals("href"))
					{
						if (a.getValue()!=null)
						{
							String currentvalue=a.getValue();
							if (!currentvalue.toLowerCase().startsWith("mailto"))
							{
								if (!currentvalue.startsWith("http"))
								{
									if (!currentvalue.startsWith("/"))
										currentvalue="/"+currentvalue;
									URL aurl = new URL(url);
									currentvalue="http://"+aurl.getHost()+currentvalue;
								}
								checkandinsert(currentvalue, url, clevel);
							}
						}
					}
				}
			}
			doc = null;
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}
	/*
	*Verifies and insert the link
	*/
	private void checkandinsert(String linkvalue, String origurl, int clev)
	{
		boolean verifyurl=false;
		boolean checkfu=true;
		try
		{
			URL ourl = new URL(origurl);
			URL lurl = new URL(linkvalue);
			String ohost=ourl.getHost();
			String lhost=lurl.getHost();
			if (!ohost.equalsIgnoreCase(lhost))
			{
				if (!extlinks.contains(lhost))
				{
					try
					{
						URL myurl = new URL(linkvalue);
						URLConnection testconn = myurl.openConnection();
						testconn.setConnectTimeout(timeoutmillis);
						testconn.connect();
						verifyurl=true;
					}
					catch (Exception falseurl){}
					if (verifyurl)
					{
						extlinks.add(lhost);
						externallink.add(lhost);
					}
				}
			}
			else
			{
				if (!visitedlinks.contains(linkvalue))
				{
					checkfu=true;
					if (filterurls.size()>0)
					{
						checkfu=false;
						for (int i=0; i<filterurls.size(); i++)
						{
							if (linkvalue.toLowerCase().indexOf(filterurls.get(i))>=0)
								checkfu=true;
						}
					}
					if (checkfu)
					{
						internallink.add(new LinkLevel(clev+1, linkvalue));
						visitedlinks.add(linkvalue);
					}
					else
					{
						verifyurl=false;
						try
						{
							URL myurl = new URL(linkvalue);
							URLConnection testconn = myurl.openConnection();
							testconn.connect();
							verifyurl=true;
						}
						catch (Exception falseurl){}
						if (verifyurl)
						{
							extlinks.add(lhost);
							externallink.add(lhost);
						}
					}
				}
			}
		}
		catch (Exception eu){}
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
			if (!method.equals("")) conn.setRequestMethod(method);
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
			if (!method.equals("")) conn.setRequestMethod(method);
			conn.connect();
			code = conn.getResponseCode();
			responsemsg = conn.getResponseMessage();
			newUrl=conn.getURL().toString();
			conn.disconnect();
    	}
	    catch (Exception e)
		{
	    	code = -1;
	    	newUrl="";
		}
	}
}
