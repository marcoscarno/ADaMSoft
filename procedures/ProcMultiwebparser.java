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

import ADaMSoft.utilities.JSoupConnection;

import java.util.concurrent.*;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.FastTempDataSet;

import ADaMSoft.utilities.WorkQueue;

import ADaMSoft.utilities.ObjectsForQueue;
import ADaMSoft.utilities.ObjectForQueue;

/**
* This is the procedure that loads the structure of the html pages that are specified in a data set
* @author marco.scarno@gmail.com
* @date 20/06/2018
*/
public class ProcMultiwebparser implements RunStep
{
	DataWriter dw;
	/**
	* Starts the execution of Proc MultiWebSpider and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> results = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.OUT.toLowerCase(), Keywords.varmainpage};
		String [] optionalparameters=new String[] {Keywords.vardescriptor, Keywords.varuseragent, Keywords.varcookies,
		Keywords.varfollowredirects, Keywords.varignorecontenttype, Keywords.varignorehttperrors, Keywords.varmethod,
		Keywords.vartimeout, Keywords.numthreads, Keywords.waitbetween,
		Keywords.varcondition_id, Keywords.varcondition_nodename, Keywords.varcondition_owntext, Keywords.varcondition_tagname,
		Keywords.varcondition_text, Keywords.varcondition_value, Keywords.varcondition_attrkey, Keywords.varcondition_attrvalue,
		Keywords.varlevel_ref, Keywords.varselect, Keywords.char_cookie_separator};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
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

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		int pos_varmainpage=-1;
		int pos_vardescriptor=-1;
		int pos_varuseragent=-1;
		int pos_varcookies=-1;
		int pos_varfollowredirects=-1;
		int pos_varignorecontenttype=-1;
		int pos_varignorehttperrors=-1;
		int pos_varmethod=-1;
		int pos_vartimeout=-1;
		int pos_varcondition_id=-1;
		int pos_varcondition_nodename=-1;
		int pos_varcondition_owntext=-1;
		int pos_varcondition_tagname=-1;
		int pos_varcondition_text=-1;
		int pos_varcondition_value=-1;
		int pos_varcondition_attrkey=-1;
		int pos_varcondition_attrvalue=-1;
		int pos_varlevel_ref=-1;
		int pos_varselect=-1;

		String tmp_varmainpage=(String) parameters.get(Keywords.varmainpage);
		String tmp_vardescriptor=(String) parameters.get(Keywords.vardescriptor);
		String tmp_varuseragent=(String) parameters.get(Keywords.varuseragent);
		String tmp_varcookies=(String) parameters.get(Keywords.varcookies);
		String tmp_varfollowredirects=(String) parameters.get(Keywords.varfollowredirects);
		String tmp_varignorecontenttype=(String) parameters.get(Keywords.varignorecontenttype);
		String tmp_varignorehttperrors=(String) parameters.get(Keywords.varignorehttperrors);
		String tmp_varmethod=(String) parameters.get(Keywords.varmethod);
		String tmp_vartimeout=(String) parameters.get(Keywords.vartimeout);
		String tmp_varcondition_id=(String) parameters.get(Keywords.varcondition_id);
		String tmp_varcondition_nodename=(String) parameters.get(Keywords.varcondition_nodename);
		String tmp_varcondition_owntext=(String) parameters.get(Keywords.varcondition_owntext);
		String tmp_varcondition_tagname=(String) parameters.get(Keywords.varcondition_tagname);
		String tmp_varcondition_text=(String) parameters.get(Keywords.varcondition_text);
		String tmp_varcondition_value=(String) parameters.get(Keywords.varcondition_value);
		String tmp_varcondition_attrkey=(String) parameters.get(Keywords.varcondition_attrkey);
		String tmp_varcondition_attrvalue=(String) parameters.get(Keywords.varcondition_attrvalue);
		String tmp_varlevel_ref=(String) parameters.get(Keywords.varlevel_ref);
		String tmp_varselect=(String) parameters.get(Keywords.varselect);

		if (tmp_varmainpage==null) tmp_varmainpage="";
		if (tmp_vardescriptor==null) tmp_vardescriptor="";
		if (tmp_varuseragent==null) tmp_varuseragent="";
		if (tmp_varcookies==null) tmp_varcookies="";
		if (tmp_varfollowredirects==null) tmp_varfollowredirects="";
		if (tmp_varignorecontenttype==null) tmp_varignorecontenttype="";
		if (tmp_varignorehttperrors==null) tmp_varignorehttperrors="";
		if (tmp_varmethod==null) tmp_varmethod="";
		if (tmp_vartimeout==null) tmp_vartimeout="";
		if (tmp_varcondition_id==null) tmp_varcondition_id="";
		if (tmp_varcondition_nodename==null) tmp_varcondition_nodename="";
		if (tmp_varcondition_owntext==null) tmp_varcondition_owntext="";
		if (tmp_varcondition_tagname==null) tmp_varcondition_tagname="";
		if (tmp_varcondition_text==null) tmp_varcondition_text="";
		if (tmp_varcondition_value==null) tmp_varcondition_value="";
		if (tmp_varcondition_attrkey==null) tmp_varcondition_attrkey="";
		if (tmp_varcondition_attrvalue==null) tmp_varcondition_attrvalue="";
		if (tmp_varlevel_ref==null) tmp_varlevel_ref="";
		if (tmp_varselect==null) tmp_varselect="";

		for (int i=0; i<dict.gettotalvar(); i++)
		{
			String tempname=dict.getvarname(i);
			if (tempname.equalsIgnoreCase(tmp_varmainpage)) pos_varmainpage=i;
			if (tempname.equalsIgnoreCase(tmp_vardescriptor)) pos_vardescriptor=i;
			if (tempname.equalsIgnoreCase(tmp_varuseragent)) pos_varuseragent=i;
			if (tempname.equalsIgnoreCase(tmp_varcookies)) pos_varcookies=i;
			if (tempname.equalsIgnoreCase(tmp_varfollowredirects)) pos_varfollowredirects=i;
			if (tempname.equalsIgnoreCase(tmp_varignorecontenttype)) pos_varignorecontenttype=i;
			if (tempname.equalsIgnoreCase(tmp_varignorehttperrors)) pos_varignorehttperrors=i;
			if (tempname.equalsIgnoreCase(tmp_varmethod)) pos_varmethod=i;
			if (tempname.equalsIgnoreCase(tmp_vartimeout)) pos_vartimeout=i;
			if (tempname.equalsIgnoreCase(tmp_varcondition_id)) pos_varcondition_id=i;
			if (tempname.equalsIgnoreCase(tmp_varcondition_nodename)) pos_varcondition_nodename=i;
			if (tempname.equalsIgnoreCase(tmp_varcondition_owntext)) pos_varcondition_owntext=i;
			if (tempname.equalsIgnoreCase(tmp_varcondition_tagname)) pos_varcondition_tagname=i;
			if (tempname.equalsIgnoreCase(tmp_varcondition_text)) pos_varcondition_text=i;
			if (tempname.equalsIgnoreCase(tmp_varcondition_value)) pos_varcondition_value=i;
			if (tempname.equalsIgnoreCase(tmp_varcondition_attrkey)) pos_varcondition_attrkey=i;
			if (tempname.equalsIgnoreCase(tmp_varcondition_attrvalue)) pos_varcondition_attrvalue=i;
			if (tempname.equalsIgnoreCase(tmp_varlevel_ref)) pos_varlevel_ref=i;
			if (tempname.equalsIgnoreCase(tmp_varselect)) pos_varselect=i;
		}

		String error_variables="";
		if (!tmp_varmainpage.equals("") && pos_varmainpage==-1) error_variables=error_variables+" varmainpage";
		if (!tmp_vardescriptor.equals("") && pos_vardescriptor==-1) error_variables=error_variables+" vardescriptor";
		if (!tmp_varuseragent.equals("") && pos_varuseragent==-1) error_variables=error_variables+" varuseragent";
		if (!tmp_varcookies.equals("") && pos_varcookies==-1) error_variables=error_variables+" varcookies";
		if (!tmp_varfollowredirects.equals("") && pos_varfollowredirects==-1) error_variables=error_variables+" varfollowredirects";
		if (!tmp_varignorecontenttype.equals("") && pos_varignorecontenttype==-1) error_variables=error_variables+" varignorecontenttype";
		if (!tmp_varignorehttperrors.equals("") && pos_varignorehttperrors==-1) error_variables=error_variables+" varignorehttperrors";
		if (!tmp_varmethod.equals("") && pos_varmethod==-1) error_variables=error_variables+" varmethod";
		if (!tmp_vartimeout.equals("") && pos_vartimeout==-1) error_variables=error_variables+" vartimeout";
		if (!tmp_varcondition_id.equals("") && pos_varcondition_id==-1) error_variables=error_variables+" varcondition_id";
		if (!tmp_varcondition_nodename.equals("") && pos_varcondition_nodename==-1) error_variables=error_variables+" varcondition_nodename";
		if (!tmp_varcondition_owntext.equals("") && pos_varcondition_owntext==-1) error_variables=error_variables+" varcondition_owntext";
		if (!tmp_varcondition_tagname.equals("") && pos_varcondition_tagname==-1) error_variables=error_variables+" varcondition_tagname";
		if (!tmp_varcondition_text.equals("") && pos_varcondition_text==-1) error_variables=error_variables+" varcondition_text";
		if (!tmp_varcondition_value.equals("") && pos_varcondition_value==-1) error_variables=error_variables+" varcondition_value";
		if (!tmp_varcondition_attrkey.equals("") && pos_varcondition_attrkey==-1) error_variables=error_variables+" varcondition_attrkey";
		if (!tmp_varcondition_attrvalue.equals("") && pos_varcondition_attrvalue==-1) error_variables=error_variables+" varcondition_attrvalue";
		if (!tmp_varlevel_ref.equals("") && pos_varlevel_ref==-1) error_variables=error_variables+" varlevel_ref";
		if (!tmp_varselect.equals("") && pos_varselect==-1) error_variables=error_variables+" varselect";

		if (!error_variables.equals(""))
		{
			error_variables="%3888%:<br>\n"+error_variables.toUpperCase()+"<br>\n";
			return new Result(error_variables, false, null);
		}

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
		int real_num_websites=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				String[] refv=new String[19];
				real_num_websites++;
				if (pos_vardescriptor!=-1) refn=values[pos_vardescriptor];
				else refn=values[pos_varmainpage];
				if (pos_varmainpage!=-1) refv[0]=values[pos_varmainpage]; else refv[0]="";
				if (pos_vardescriptor!=-1) refv[1]=values[pos_vardescriptor]; else refv[1]="";
				if (pos_varuseragent!=-1) refv[2]=values[pos_varuseragent]; else refv[2]="";
				if (pos_varcookies!=-1) refv[3]=values[pos_varcookies]; else refv[3]="";
				if (pos_varfollowredirects!=-1) refv[4]=values[pos_varfollowredirects]; else refv[4]="";
				if (pos_varignorecontenttype!=-1) refv[5]=values[pos_varignorecontenttype]; else refv[5]="";
				if (pos_varignorehttperrors!=-1) refv[6]=values[pos_varignorehttperrors]; else refv[6]="";
				if (pos_varmethod!=-1) refv[7]=values[pos_varmethod]; else refv[7]="";
				if (pos_vartimeout!=-1) refv[8]=values[pos_vartimeout]; else refv[8]="";
				if (pos_varcondition_id!=-1) refv[9]=values[pos_varcondition_id]; else refv[9]="";
				if (pos_varcondition_nodename!=-1) refv[10]=values[pos_varcondition_nodename]; else refv[10]="";
				if (pos_varcondition_owntext!=-1) refv[11]=values[pos_varcondition_owntext]; else refv[11]="";
				if (pos_varcondition_tagname!=-1) refv[12]=values[pos_varcondition_tagname]; else refv[12]="";
				if (pos_varcondition_text!=-1) refv[13]=values[pos_varcondition_text]; else refv[13]="";
				if (pos_varcondition_value!=-1) refv[14]=values[pos_varcondition_value]; else refv[14]="";
				if (pos_varcondition_attrkey!=-1) refv[15]=values[pos_varcondition_attrkey]; else refv[15]="";
				if (pos_varcondition_attrvalue!=-1) refv[16]=values[pos_varcondition_attrvalue]; else refv[16]="";
				if (pos_varlevel_ref!=-1) refv[17]=values[pos_varlevel_ref]; else refv[17]="";
				if (pos_varselect!=-1) refv[18]=values[pos_varselect]; else refv[18]="";

				ofq.addelement(refn, refv);
			}
		}
		data.close();

		numwebsites=ofq.getelem();
		ofq.fillqueue();

		DataSetUtilities dsu=new DataSetUtilities();
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		dsu.addnewvar("page_descriptor", "%3889%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("main_page", "%3890%", Keywords.TEXTSuffix, tempmd, tempmd);

		dsu.addnewvar("element_level", "%3891%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("element_level_ref", "%3892%", Keywords.TEXTSuffix, tempmd, tempmd);

		dsu.addnewvar("element_id", "%3893%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("element_nodename", "%3894%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("element_owntext", "%3895%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("element_tagname", "%3896%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("element_text", "%3897%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("element_val", "%3898%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("element_attr_key", "%3899%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("element_attr_value", "%3900%", Keywords.TEXTSuffix, tempmd, tempmd);

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

		int waitbetween=0;
		String tmp_waitbetween=(String) parameters.get(Keywords.waitbetween);
		if (tmp_waitbetween==null) tmp_waitbetween="";
		if (!tmp_waitbetween.equals(""))
		{
			waitbetween=-1;
			try
			{
				double tma=Double.parseDouble(tmp_waitbetween);
				waitbetween=(int)tma;
			}
			catch (Exception et) {}
		}
		if (waitbetween<0)
			return new Result("%4041%<br>\n", false, null);

		Keywords.percentage_total=numwebsites;
		Keywords.percentage_done=0;

		Semaphore sem=new Semaphore(1, true);

		WorkQueue queue=ofq.getqueue();

		String tempdir=(String)parameters.get(Keywords.WorkDir);

		ParserRetriever[] parret = new ParserRetriever[numthread];
		for (int i=0; i<parret.length; i++)
		{
			parret[i] = new ParserRetriever(queue, sem, dw, tempdir, waitbetween, char_cookie_separator);
			try
			{
				Thread.sleep(100);
			}
			catch (Exception et){}
			parret[i].start();
		}

		String keyword="MultiWebParser ";
		String description="MultiWebParser ";
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		try
		{
			for (int i=0; i<parret.length; i++)
			{
				parret[i].join();
			}
		}
		catch (Exception e){}

		int sitesnu=0;
		int siteszero=0;
		for (int i=0; i<parret.length; i++)
		{
			Vector<String> tempmsg=parret[i].getretmsg();
			sitesnu=sitesnu+parret[i].getsitesnopar();
			siteszero=siteszero+parret[i].getsiteszero();
			for (int j=0; j<tempmsg.size(); j++)
			{
				results.add(new LocalMessageGetter(tempmsg.get(j)));
			}
		}

		if (sitesnu==real_num_websites)
		{
			dw.deletetmp();
			return new Result("%3772%<br>\n", false, null);
		}

		if (siteszero==real_num_websites)
		{
			dw.deletetmp();
			return new Result("%3920%<br>\n", false, null);
		}

		results.add(new LocalMessageGetter("%3916%: "+String.valueOf(numwebsites)+"<br>\n"));
		if (sitesnu>0) results.add(new LocalMessageGetter("%3917%: "+String.valueOf(sitesnu)+"<br>\n"));
		if (siteszero>0) results.add(new LocalMessageGetter("%3918%: "+String.valueOf(siteszero)+"<br>\n"));

		try
		{
			for (int i=0; i<parret.length; i++)
			{
				parret[i]=null;
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3902, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varmainpage, "var=all", true, 3903, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vardescriptor, "var=all", false, 3524, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varuseragent, "var=all", false, 3517, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcookies, "var=all", false, 3518, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varfollowredirects, "var=all", false, 3519, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varignorecontenttype, "var=all", false, 3520, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varignorehttperrors, "var=all", false, 3521, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varmethod, "var=all", false, 3522, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartimeout, "var=all", false, 3523, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.varselect, "var=all", false, 3904, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varlevel_ref, "var=all", false, 3905, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3906, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.varcondition_id, "var=all", false, 3907, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcondition_nodename, "var=all", false, 3908, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcondition_owntext, "var=all", false, 3909, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcondition_tagname, "var=all", false, 3910, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcondition_text, "var=all", false, 3911, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcondition_value, "var=all", false, 3912, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcondition_attrkey, "var=all", false, 3913, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcondition_attrvalue, "var=all", false, 3914, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.numthreads,"text", false, 3738,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.char_cookie_separator,"text", false, 4089,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.waitbetween,"text", false, 4040,dep,"",2));
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
		retprocinfo[1]="3915";
		return retprocinfo;
	}
}
/**
*This is the thread that retrieves the content
*/
class ParserRetriever extends Thread
{
	Semaphore sem;
	DataWriter dw;
    WorkQueue q;
	String tempdir, descriptor, mainpage, useragent, tempcookies, method, timeout, temptext;
	Vector<String> retmsg;
	FastTempDataSet fdt;
	boolean followredirects, ignorecontenttype, ignorehttperrors, exeparser, addelement, containelement;
	Vector<String[]> subsites;
	String[] tempsub;
	Hashtable<String, String> cookies;
	Document doc;
	int timeoutmillis, tempnrec, arrived, sitesnu, checkelement, siteszero, waitbetween;
	double tma;
	JSoupConnection jsco;
	Elements el;
	Iterator<Element> it_elements;
	String[] outvalues;
	Vector<String> rf17;
	String[] trf;
	Vector<String> rf9;
	Vector<String> rf10;
	Vector<String> rf11;
	Vector<String> rf12;
	Vector<String> rf13;
	Vector<String> rf14;
	Vector<String> rf15;
	Vector<String> rf16;

	Vector<String> nrf9;
	Vector<String> nrf10;
	Vector<String> nrf11;
	Vector<String> nrf12;
	Vector<String> nrf13;
	Vector<String> nrf14;
	Vector<String> nrf15;
	Vector<String> nrf16;

	Vector<String> mrf9;
	Vector<String> mrf10;
	Vector<String> mrf11;
	Vector<String> mrf12;
	Vector<String> mrf13;
	Vector<String> mrf14;
	Vector<String> mrf15;
	Vector<String> mrf16;

	int char_cookie_separator;

	ParserRetriever(WorkQueue q, Semaphore sem, DataWriter dw, String tempdir, int waitbetween, int char_cookie_separator)
    {
		this.char_cookie_separator=char_cookie_separator;
		cookies=new Hashtable<String, String>();
        this.q = q;
        this.sem=sem;
        this.dw=dw;
        this.tempdir=tempdir;
        this.waitbetween=waitbetween;
        retmsg=new Vector<String>();
        sitesnu=0;
        siteszero=0;
		outvalues=new String[12];
		exeparser=true;
		rf9=new Vector<String>();
		rf10=new Vector<String>();
		rf11=new Vector<String>();
		rf12=new Vector<String>();
		rf13=new Vector<String>();
		rf14=new Vector<String>();
		rf15=new Vector<String>();
		rf16=new Vector<String>();

		nrf9=new Vector<String>();
		nrf10=new Vector<String>();
		nrf11=new Vector<String>();
		nrf12=new Vector<String>();
		nrf13=new Vector<String>();
		nrf14=new Vector<String>();
		nrf15=new Vector<String>();
		nrf16=new Vector<String>();

		mrf9=new Vector<String>();
		mrf10=new Vector<String>();
		mrf11=new Vector<String>();
		mrf12=new Vector<String>();
		mrf13=new Vector<String>();
		mrf14=new Vector<String>();
		mrf15=new Vector<String>();
		mrf16=new Vector<String>();

		rf17=new Vector<String>();
    }
	public int getsitesnopar()
	{
		return sitesnu;
	}
	public int getsiteszero()
	{
		return siteszero;
	}
    public Vector<String> getretmsg()
    {
		return retmsg;
	}
    public void run()
    {
        try
        {
			exeparser=true;
			ObjectForQueue cofq;
            while (exeparser)
            {
				sem.acquire();
				cofq=q.getWork();
				sem.release();
                if (cofq == null)
                {
					exeparser=false;
                    break;
                }
                descriptor=cofq.getname();
                subsites=cofq.getcontent();
                for (int s=0; s<subsites.size(); s++)
                {
					if (waitbetween>0)
					{
						try
						{
							Thread.sleep(waitbetween);
						}
						catch (Exception et){}
					}
					fdt=new FastTempDataSet(tempdir);
					tempsub=subsites.get(s);
					mainpage    = tempsub[0];
					if (!mainpage.toLowerCase().startsWith("http"))
						mainpage="http://"+mainpage;
					mainpage=mainpage.toLowerCase();
					useragent=tempsub[2];
					tempcookies=tempsub[3];
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
							retmsg.add("%3537% ("+mainpage+")\n");
							cookies.clear();
						}
					}
					followredirects =false; if (!tempsub[4].equals("")) followredirects=true;
					ignorecontenttype =false; if (!tempsub[5].equals("")) ignorecontenttype=true;
					ignorehttperrors  =false; if (!tempsub[6].equals("")) ignorehttperrors=true;
					method=tempsub[7];
					if (method==null) method="";
					timeout=tempsub[8];
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
							retmsg.add("%3538% ("+mainpage+")\n");
						}
					}
					outvalues=null;
					outvalues=new String[12];
					el=null;
					jsco=new JSoupConnection();
					jsco.setcookies(cookies);
					jsco.setuseragent(useragent);
					jsco.setbooleans(followredirects, ignorecontenttype, ignorehttperrors);
					jsco.setmethod(method);
					jsco.settimeoutmillis(timeoutmillis);
					doc=null;
					outvalues[0]=descriptor;
					outvalues[1]=mainpage;
					rf17.clear();
					rf9.clear();
					rf10.clear();
					rf11.clear();
					rf12.clear();
					rf13.clear();
					rf14.clear();
					rf15.clear();
					rf16.clear();

					nrf9.clear();
					nrf10.clear();
					nrf11.clear();
					nrf12.clear();
					nrf13.clear();
					nrf14.clear();
					nrf15.clear();
					nrf16.clear();

					mrf9.clear();
					mrf10.clear();
					mrf11.clear();
					mrf12.clear();
					mrf13.clear();
					mrf14.clear();
					mrf15.clear();
					mrf16.clear();

					if (!tempsub[17].equals(""))
					{
						trf=(tempsub[17].toLowerCase()).split(" ");
						for (int i=0; i<trf.length; i++)
						{
							if (trf[i].equals("id")) rf17.add("id");
							if (trf[i].equals("nodename")) rf17.add("nodename");
							if (trf[i].equals("owntext")) rf17.add("owntext");
							if (trf[i].equals("tagname")) rf17.add("tagname");
							if (trf[i].equals("text")) rf17.add("text");
							if (trf[i].equals("val")) rf17.add("val");
						}
					}
					if (!tempsub[9].equals(""))
					{
						trf=(tempsub[9].toLowerCase()).split(" ");
						for (int i=0; i<trf.length; i++)
						{
							if (trf[i].startsWith("!"))
							{
								trf[i]=trf[i].replaceAll("!","");
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf9.add(trf[i].toLowerCase());
								}
								nrf9.add(trf[i].toLowerCase());
							}
							else
							{
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf9.add(trf[i].toLowerCase());
								}
								rf9.add(trf[i].toLowerCase());
							}
						}
					}
					if (!tempsub[10].equals(""))
					{
						trf=(tempsub[10].toLowerCase()).split(" ");
						for (int i=0; i<trf.length; i++)
						{
							if (trf[i].startsWith("!"))
							{
								trf[i]=trf[i].replaceAll("!","");
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf10.add(trf[i].toLowerCase());
								}
								nrf10.add(trf[i].toLowerCase());
							}
							else
							{
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf10.add(trf[i].toLowerCase());
								}
								rf10.add(trf[i].toLowerCase());
							}
						}
					}
					if (!tempsub[11].equals(""))
					{
						trf=(tempsub[11].toLowerCase()).split(" ");
						for (int i=0; i<trf.length; i++)
						{
							if (trf[i].startsWith("!"))
							{
								trf[i]=trf[i].replaceAll("!","");
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf11.add(trf[i].toLowerCase());
								}
								nrf11.add(trf[i].toLowerCase());
							}
							else
							{
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf11.add(trf[i].toLowerCase());
								}
								rf11.add(trf[i].toLowerCase());
							}
						}
					}
					if (!tempsub[12].equals(""))
					{
						trf=(tempsub[12].toLowerCase()).split(" ");
						for (int i=0; i<trf.length; i++)
						{
							if (trf[i].startsWith("!"))
							{
								trf[i]=trf[i].replaceAll("!","");
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf12.add(trf[i].toLowerCase());
								}
								nrf12.add(trf[i].toLowerCase());
							}
							else
							{
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf12.add(trf[i].toLowerCase());
								}
								rf12.add(trf[i].toLowerCase());
							}
						}
					}
					if (!tempsub[13].equals(""))
					{
						trf=(tempsub[13].toLowerCase()).split(" ");
						for (int i=0; i<trf.length; i++)
						{
							if (trf[i].startsWith("!"))
							{
								trf[i]=trf[i].replaceAll("!","");
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf13.add(trf[i].toLowerCase());
								}
								nrf13.add(trf[i].toLowerCase());
							}
							else
							{
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf13.add(trf[i].toLowerCase());
								}
								rf13.add(trf[i].toLowerCase());
							}
						}
					}
					if (!tempsub[14].equals(""))
					{
						trf=(tempsub[14].toLowerCase()).split(" ");
						for (int i=0; i<trf.length; i++)
						{
							if (trf[i].startsWith("!"))
							{
								trf[i]=trf[i].replaceAll("!","");
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf14.add(trf[i].toLowerCase());
								}
								nrf14.add(trf[i].toLowerCase());
							}
							else
							{
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf14.add(trf[i].toLowerCase());
								}
								rf14.add(trf[i].toLowerCase());
							}
						}
					}
					if (!tempsub[15].equals(""))
					{
						trf=(tempsub[15].toLowerCase()).split(" ");
						for (int i=0; i<trf.length; i++)
						{
							if (trf[i].startsWith("!"))
							{
								trf[i]=trf[i].replaceAll("!","");
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf15.add(trf[i].toLowerCase());
								}
								nrf15.add(trf[i].toLowerCase());
							}
							else
							{
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf15.add(trf[i].toLowerCase());
								}
								rf15.add(trf[i].toLowerCase());
							}
						}
					}
					if (!tempsub[16].equals(""))
					{
						trf=(tempsub[16].toLowerCase()).split(" ");
						for (int i=0; i<trf.length; i++)
						{
							if (trf[i].startsWith("!"))
							{
								trf[i]=trf[i].replaceAll("!","");
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf16.add(trf[i].toLowerCase());
								}
								nrf16.add(trf[i].toLowerCase());
							}
							else
							{
								if (trf[i].indexOf("*")>=0)
								{
									trf[i]=trf[i].replaceAll("\\*","");
									mrf16.add(trf[i].toLowerCase());
								}
								rf16.add(trf[i].toLowerCase());
							}
						}
					}
					arrived=0;
					try
					{
						doc=jsco.simpleGetHtmlDocument(mainpage);
						if (tempsub[18].equals("")) el=doc.getAllElements();
						else el=doc.select(tempsub[18]);
						if (el!=null)
						{
							if (el.size()>0)
							{
								it_elements = el.iterator();
								while(it_elements.hasNext())
								{
									Element current=it_elements.next();
									iterateElements(current);
								}
								fdt.endwrite();
								arrived=1;
								fdt.opentoread();
								arrived=2;
								tempnrec=fdt.getrecords();
								arrived=3;
								if (tempnrec>0)
								{
									sem.acquire();
									for (int i=0; i<tempnrec; i++)
									{
										dw.write(fdt.read());
									}
									sem.release();
								}
								arrived=4;
								fdt.endread();
								arrived=5;
							}
						}
					}
					catch (Exception e)
					{
						retmsg.add("%3901%:\n"+e.toString()+" ("+mainpage+")\n");
					}
					if (arrived<4) sitesnu++;
					if (tempnrec==0)
					{
						retmsg.add("%3919% ("+mainpage+")\n");
						siteszero++;
					}
					if (arrived==0)
					{
						fdt.endwrite();
						fdt.opentoread();
						fdt.getrecords();
						fdt.endread();
					}
					if (arrived==1)
					{
						fdt.opentoread();
						fdt.getrecords();
						fdt.endread();
					}
					if (arrived==2)
					{
						fdt.getrecords();
						fdt.endread();
					}
					if (arrived==3) fdt.endread();
					if (arrived==4) fdt.endread();
					fdt.deletefile();
					fdt=null;
					System.gc();
					jsco=null;
				}
                Keywords.percentage_done++;
            }
        }
        catch (InterruptedException e)
        {
		}
        catch (Exception ed)
        {
		}
    }
    /**
    *Used to iterate over the elements of the page
    */
	private void iterateElements(Element cur_el)
	{
		try
		{
			addelement=true;
			Elements pars=cur_el.parents();
			if (pars==null) outvalues[2]="";
			else outvalues[2]=String.valueOf(pars.size());
			outvalues[3]=outvalues[2];
			if (rf17.size()>0 && pars!=null)
			{
				if (pars.size()>0)
				{
					outvalues[3]="";
					for (int i=0; i<pars.size(); i++)
					{
						temptext="";
						Element cp=pars.get(i);
						if (rf17.contains("id") && !cp.id().equals("")) temptext=temptext+" "+cp.id();
						if (rf17.contains("nodename") && !cp.nodeName().equals("")) temptext=temptext+" "+cp.nodeName();
						if (rf17.contains("owntext") && !cp.ownText().equals("")) temptext=temptext+" "+cp.ownText();
						if (rf17.contains("tagname") && !cp.tagName().equals("")) temptext=temptext+" "+cp.tagName();
						if (rf17.contains("text") && !cp.text().equals("")) temptext=temptext+" "+cp.text();
						if (rf17.contains("val") && !cp.val().equals("")) temptext=temptext+" "+cp.val();
						temptext=temptext.trim();
						if (!temptext.equals(""))
						{
							if (outvalues[3].equals("")) outvalues[3]=temptext;
							else outvalues[3]=temptext+"->"+outvalues[3];
						}
					}
					if (outvalues[3].equals("")) outvalues[3]=outvalues[2];
				}
			}
			temptext="";
			outvalues[4]=cur_el.id();
			if (rf17.size()>0 && rf17.contains("id") && !outvalues[4].equals("")) temptext=temptext+" "+outvalues[4];
			if (rf9.size()>0)
			{
				if (mrf9.size()==0)
				{
					if (!rf9.contains(outvalues[4].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf9.size(); i++)
					{
						if (rf9.contains(mrf9.get(i)))
						{
							containelement=true;
							if (outvalues[4].toLowerCase().indexOf(mrf9.get(i))>=0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			if (nrf9.size()>0)
			{
				if (mrf9.size()==0)
				{
					if (nrf9.contains(outvalues[4].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf9.size(); i++)
					{
						if (nrf9.contains(mrf9.get(i)))
						{
							containelement=true;
							if (outvalues[4].toLowerCase().indexOf(mrf9.get(i))<0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			outvalues[5]=cur_el.nodeName() ;
			if (rf17.size()>0 && rf17.contains("nodename") && !outvalues[5].equals("")) temptext=temptext+" "+outvalues[5];
			if (rf10.size()>0)
			{
				if (mrf10.size()==0)
				{
					if (!rf10.contains(outvalues[5].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf10.size(); i++)
					{
						if (rf10.contains(mrf10.get(i)))
						{
							containelement=true;
							if (outvalues[5].toLowerCase().indexOf(mrf10.get(i))>=0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			if (nrf10.size()>0)
			{
				if (mrf10.size()==0)
				{
					if (nrf10.contains(outvalues[5].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf10.size(); i++)
					{
						if (nrf10.contains(mrf10.get(i)))
						{
							containelement=true;
							if (outvalues[5].toLowerCase().indexOf(mrf10.get(i))<0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			outvalues[6]=cur_el.ownText();
			if (rf17.size()>0 && rf17.contains("owntext") && !outvalues[6].equals("")) temptext=temptext+" "+outvalues[6];
			if (rf11.size()>0)
			{
				if (mrf11.size()==0)
				{
					if (!rf11.contains(outvalues[6].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf11.size(); i++)
					{
						if (rf11.contains(mrf11.get(i)))
						{
							containelement=true;
							if (outvalues[6].toLowerCase().indexOf(mrf11.get(i))>=0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			if (nrf11.size()>0)
			{
				if (mrf11.size()==0)
				{
					if (nrf11.contains(outvalues[6].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf11.size(); i++)
					{
						if (nrf11.contains(mrf11.get(i)))
						{
							containelement=true;
							if (outvalues[6].toLowerCase().indexOf(mrf11.get(i))<0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			outvalues[7]=cur_el.tagName();
			if (rf17.size()>0 && rf17.contains("tagname") && !outvalues[7].equals("")) temptext=temptext+" "+outvalues[7];
			if (rf12.size()>0)
			{
				if (mrf12.size()==0)
				{
					if (!rf12.contains(outvalues[7].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf12.size(); i++)
					{
						if (rf12.contains(mrf12.get(i)))
						{
							containelement=true;
							if (outvalues[7].toLowerCase().indexOf(mrf12.get(i))>=0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			if (nrf12.size()>0)
			{
				if (mrf12.size()==0)
				{
					if (nrf12.contains(outvalues[7].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf12.size(); i++)
					{
						if (nrf12.contains(mrf12.get(i)))
						{
							containelement=true;
							if (outvalues[7].toLowerCase().indexOf(mrf12.get(i))<0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			outvalues[8]=cur_el.text() ;
			if (rf17.size()>0 && rf17.contains("text") && !outvalues[8].equals("")) temptext=temptext+" "+outvalues[8];
			if (rf13.size()>0)
			{
				if (mrf13.size()==0)
				{
					if (!rf13.contains(outvalues[8].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf13.size(); i++)
					{
						if (rf13.contains(mrf13.get(i)))
						{
							containelement=true;
							if (outvalues[8].toLowerCase().indexOf(mrf13.get(i))>=0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			if (nrf13.size()>0)
			{
				if (mrf13.size()==0)
				{
					if (nrf13.contains(outvalues[8].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf13.size(); i++)
					{
						if (nrf13.contains(mrf13.get(i)))
						{
							containelement=true;
							if (outvalues[8].toLowerCase().indexOf(mrf13.get(i))<0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			outvalues[9]=cur_el.val() ;
			if (rf17.size()>0 && rf17.contains("val") && !outvalues[9].equals("")) temptext=temptext+" "+outvalues[9];
			if (rf14.size()>0)
			{
				if (mrf14.size()==0)
				{
					if (!rf14.contains(outvalues[9].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf14.size(); i++)
					{
						if (rf14.contains(mrf14.get(i)))
						{
							containelement=true;
							if (outvalues[9].toLowerCase().indexOf(mrf14.get(i))>=0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			if (nrf14.size()>0)
			{
				if (mrf14.size()==0)
				{
					if (nrf14.contains(outvalues[9].toLowerCase())) addelement=false;
				}
				else
				{
					checkelement=0;
					containelement=false;
					for (int i=0; i<mrf14.size(); i++)
					{
						if (nrf14.contains(mrf14.get(i)))
						{
							containelement=true;
							if (outvalues[9].toLowerCase().indexOf(mrf14.get(i))<0) checkelement++;
						}
					}
					if (containelement && checkelement==0) addelement=false;
				}
			}
			temptext=temptext.trim();
			if (!temptext.equals(""))
			{
				if (outvalues[3].equals("")) outvalues[3]=temptext;
				else outvalues[3]=outvalues[3]+"->"+temptext;
			}
			outvalues[10]="";
			outvalues[11]="";
			Attributes atts=cur_el.attributes();
			if (atts.size()>0)
			{
				Iterator<Attribute> ite=atts.iterator();
				while(ite.hasNext())
				{
					Attribute a=ite.next();
					if (a.getKey()!=null)
						outvalues[10]=a.getKey();
					else
						outvalues[10]="";
					if (rf15.size()>0)
					{
						if (mrf15.size()==0)
						{
							if (!rf15.contains(outvalues[10].toLowerCase())) addelement=false;
						}
						else
						{
							checkelement=0;
							containelement=false;
							for (int i=0; i<mrf15.size(); i++)
							{
								if (rf15.contains(mrf15.get(i)))
								{
									containelement=true;
									if (outvalues[10].toLowerCase().indexOf(mrf15.get(i))>=0) checkelement++;
								}
							}
							if (containelement && checkelement==0) addelement=false;
						}
					}
					if (nrf15.size()>0)
					{
						if (mrf15.size()==0)
						{
							if (nrf15.contains(outvalues[10].toLowerCase())) addelement=false;
						}
						else
						{
							checkelement=0;
							containelement=false;
							for (int i=0; i<mrf15.size(); i++)
							{
								if (nrf15.contains(mrf15.get(i)))
								{
									containelement=true;
									if (outvalues[10].toLowerCase().indexOf(mrf15.get(i))<0) checkelement++;
								}
							}
							if (containelement && checkelement==0) addelement=false;
						}
					}
					if (a.getValue()!=null)
						outvalues[11]=a.getValue();
					else
						outvalues[11]="";
					if (rf16.size()>0)
					{
						if (mrf16.size()==0)
						{
							if (!rf16.contains(outvalues[11].toLowerCase())) addelement=false;
						}
						else
						{
							checkelement=0;
							containelement=false;
							for (int i=0; i<mrf16.size(); i++)
							{
								if (rf16.contains(mrf16.get(i)))
								{
									containelement=true;
									if (outvalues[11].toLowerCase().indexOf(mrf16.get(i))>=0) checkelement++;
								}
							}
							if (containelement && checkelement==0) addelement=false;
						}
					}
					if (nrf16.size()>0)
					{
						if (mrf16.size()==0)
						{
							if (nrf16.contains(outvalues[11].toLowerCase())) addelement=false;
						}
						else
						{
							checkelement=0;
							containelement=false;
							for (int i=0; i<mrf16.size(); i++)
							{
								if (nrf16.contains(mrf16.get(i)))
								{
									containelement=true;
									if (outvalues[11].toLowerCase().indexOf(mrf16.get(i))<0) checkelement++;
								}
							}
							if (containelement && checkelement==0) addelement=false;
						}
					}
					if (addelement) fdt.write(outvalues);
				}
			}
			else
			{
				if (rf15.size()>0) addelement=false;
				if (nrf15.size()>0) addelement=false;
				if (rf16.size()>0) addelement=false;
				if (nrf16.size()>0) addelement=false;
				if (addelement) fdt.write(outvalues);
			}
		}
		catch (Exception e){}
		Elements chi=cur_el.children();
		if (chi.size()>0)
		{
			for (int i=0; i<chi.size(); i++)
			{
				iterateElements(chi.get(i));
			}
			cur_el.empty();
		}
	}
}

