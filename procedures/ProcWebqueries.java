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

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.UncheckedIOException;

import org.jsoup.nodes.Element;
import org.jsoup.select.*;

import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.List;
import com.google.gson.Gson;

import java.util.*;
import java.io.*;

import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.dataaccess.DataReader;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import java.util.logging.Level;

/**
* This is the procedure that executes the queries in Google, Bing, DuckDuckGo or Istella
* @author marco.scarno@gmail.com
* @date 20/06/2018
*/
public class ProcWebqueries implements RunStep
{
	DataWriter dw;
	/**
	* Starts the execution of Proc Webqueries and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Keywords.procedure_error=false;
		Vector<StepResult> results = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.OUT.toLowerCase(), Keywords.varquery, Keywords.searchengine};
		String [] optionalparameters=new String[] {Keywords.waitbetween, Keywords.vertical_results};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean vertical_results=(parameters.get(Keywords.vertical_results)!=null);
		String searchengine =(String)parameters.get(Keywords.searchengine);
		if (searchengine==null)
			searchengine=Keywords.bing;
		String[] ft=new String[] {Keywords.bing, Keywords.duckduckgo, Keywords.istella};
		int searchtype=steputilities.CheckOption(ft, searchengine);
		if (searchtype==0)
			return new Result("%1775% "+Keywords.searchengine.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		int waitfor=0;
		if (parameters.get(Keywords.waitbetween)!=null)
		{
			waitfor=-1;
			try
			{
				waitfor=Integer.parseInt((String)parameters.get(Keywords.waitbetween));
			}
			catch (Exception e){}
		}
		if (waitfor<0)
		{
			return new Result("%3720%<br>\n", false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String vq=(String)parameters.get(Keywords.varquery);

		boolean foundv=false;
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			String tempname=dict.getvarname(i);
			if (tempname.equalsIgnoreCase(vq)) foundv=true;
		}
		if (!foundv)
		{
			return new Result("%3724% ("+vq+")<br>\n", false, null);
		}

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DataSetUtilities dsu=new DataSetUtilities();

		dsu.setreplace(null);

		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);
		if (!vertical_results)
		{
			if (searchtype<4)
			{
				for (int i=0; i<10; i++)
				{
					dsu.addnewvartoolddict("title_"+String.valueOf(i), "%3722% ("+String.valueOf(i)+")", Keywords.TEXTSuffix, temph, temph);
					dsu.addnewvartoolddict("result_"+String.valueOf(i), "%3721% ("+String.valueOf(i)+")", Keywords.TEXTSuffix, temph, temph);
					dsu.addnewvartoolddict("url_"+String.valueOf(i), "%3723% ("+String.valueOf(i)+")", Keywords.TEXTSuffix, temph, temph);
				}
			}
			else
			{
				for (int i=0; i<10; i++)
				{
					dsu.addnewvartoolddict("title_"+String.valueOf(i), "%3722% ("+String.valueOf(i)+")", Keywords.TEXTSuffix, temph, temph);
					dsu.addnewvartoolddict("url_"+String.valueOf(i), "%3723% ("+String.valueOf(i)+")", Keywords.TEXTSuffix, temph, temph);
				}
			}
		}
		else
		{
			if (searchtype<4)
			{
				dsu.addnewvartoolddict("title_results_search", "%3722%", Keywords.TEXTSuffix, temph, temph);
				dsu.addnewvartoolddict("result_search", "%3721%", Keywords.TEXTSuffix, temph, temph);
				dsu.addnewvartoolddict("url_search", "%3723%", Keywords.TEXTSuffix, temph, temph);
			}
			else
			{
				dsu.addnewvartoolddict("title_results_search", "%3722%", Keywords.TEXTSuffix, temph, temph);
				dsu.addnewvartoolddict("url_search", "%3723%", Keywords.TEXTSuffix, temph, temph);
			}
		}


		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		VariableUtilities varu=new VariableUtilities(dict, null, (String)parameters.get(Keywords.varquery), null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] totalvar=varu.getallvar();

		int[] replacerule=varu.getreplaceruleforall(null);

		int[] allvarstype=varu.getnormalruleforall();

		ValuesParser vp=new ValuesParser(allvarstype, null, null, null, null, null);

		DataReader data = new DataReader(dict);
		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		int totrec=0;
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null) totrec++;
		}
		data.close();
		if (!data.open(totalvar, replacerule, false))
		{
			return new Result(data.getmessage(), false, null);
		}
		where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		Keywords.percentage_total=totrec;

		String address = "";
		if (searchtype==3) address="http://www.istella.it/search/?key=";
		if (searchtype==2) address="https://duckduckgo.com/html/?q=";
		if (searchtype==1) address="https://www.bing.com/search?q=";
		String charset = "UTF-8";

		int total=0;

		String keyword="Webqueries "+dict.getkeyword();
		String description="Webqueries "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		int newv=30;
		if (searchtype==4)
			newv=20;
		if (vertical_results)
		{
			newv=3;
			if (searchtype==4) newv=2;
		}
		String[] newvalues=new String[newv];
		int done=0;
		int ref=0;
		String addaddress="";
		Vector<String> reft=new Vector<String>();
		Vector<String> refr=new Vector<String>();
		Vector<String> refh=new Vector<String>();
		String tempu="";
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				String[] query=vp.getanalysisvar(values);
				for (int i=0; i<newv; i++)
				{
					newvalues[i]="";
				}
				if (searchtype==4)
				{
					Vector<String[]> google_info=new Vector<String[]>();
					try
					{
						java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
						java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
						WebClient webClient = new WebClient();
						HtmlPage myPage = webClient.getPage("http://www.google.com/search?q="+query[0]+"&num=10");
						Document doc = Jsoup.parse(myPage.asXml());
						Elements titles = doc.select("h3.r > a");
						for(Element e: titles)
						{
							String[] temp_google=new String[2];
							temp_google[0]=e.text();
							temp_google[1]=e.attr("href");
							google_info.add(temp_google);
						}
						webClient.close();
					}
					catch (Exception e){}
					if (!vertical_results)
					{
						ref=0;
						for (int i=0; i<10; i++)
						{
							newvalues[ref]="";
							ref++;
							newvalues[ref]="";
							ref++;
						}
						ref=0;
						int max_pointer=10;
						if (google_info.size()<10) max_pointer=google_info.size();
						for (int i=0; i<max_pointer; i++)
						{
							String[] temp_google=google_info.get(i);
							newvalues[ref]=temp_google[0];
							ref++;
							newvalues[ref]=temp_google[1];
							ref++;
						}
					}
					else
					{
						int max_pointer=10;
						if (google_info.size()<10) max_pointer=google_info.size();
						for (int i=0; i<max_pointer; i++)
						{
							String[] temp_google=google_info.get(i);
							newvalues[0]=temp_google[0];
							newvalues[1]=temp_google[1];
							String[] wvalues=dsu.getnewvalues(values, newvalues);
							dw.write(wvalues);
						}
					}
				}
				else if (searchtype==2)
				{
					try
					{
						reft.clear();
						refr.clear();
						refh.clear();
						URL url = new URL(address + URLEncoder.encode(query[0], charset));
						addaddress=url.toString();
						Document doc=Jsoup.connect(addaddress).timeout(5000).userAgent("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)").get();
						Elements el=doc.select(".result__a");
						Iterator<Element> it_elements = el.iterator();
						boolean start=false;
						boolean starthref=false;
						ref=0;
				 		while(it_elements.hasNext())
				 		{
							Element current=it_elements.next();
							Attributes atts=current.attributes();
							Iterator<Attribute> ite=atts.iterator();
							while(ite.hasNext())
							{
								Attribute a=ite.next();
								if (a.getValue()!=null)
								{
									if (a.getValue().equalsIgnoreCase("result__a"))
									{
										refr.add(current.text());
										start=true;
										starthref=true;
									}
									if (start && starthref && a.getKey().equalsIgnoreCase("href"))
									{
										tempu=a.getValue();
										try
										{
											tempu=tempu.substring(tempu.indexOf("uddg")+5);
											tempu = URLDecoder.decode(tempu,"UTF-8");
										}
										catch (Exception e) {}
										refh.add(tempu);
										reft.add(current.text());
										starthref=false;
										ref++;
									}
								}
							}
						}
					}
					catch (Exception e) {}
					if (!vertical_results)
					{
						for (int i=0; i<10; i++)
						{
							if (reft.get(i)!=null) newvalues[ref]=reft.get(i);
							if (reft.get(i)==null) newvalues[ref]="";
							ref++;
							if (refr.get(i)!=null) newvalues[ref]=refr.get(i);
							if (refr.get(i)==null) newvalues[ref]="";
							ref++;
							if (refh.get(i)!=null) newvalues[ref]=refh.get(i);
							if (refh.get(i)==null) newvalues[ref]="";
							ref++;
						}
					}
					else
					{
						for (int i=0; i<refh.size(); i++)
						{
							newvalues[0]=reft.get(i);
							newvalues[1]=refr.get(i);
							newvalues[2]=refh.get(i);
							String[] wvalues=dsu.getnewvalues(values, newvalues);
							dw.write(wvalues);
						}
					}
				}
				else if (searchtype==1)
				{
					reft.clear();
					refh.clear();
					refr.clear();
					try
					{
						URL url = new URL(address + URLEncoder.encode(query[0], charset));
						addaddress=url.toString();
						Document doc=Jsoup.connect(addaddress).timeout(5000).ignoreContentType(true).ignoreHttpErrors(true).followRedirects(true).get();
						Elements el=doc.getAllElements();
						Iterator<Element> it_elements = el.iterator();
						boolean start=false;
						boolean starthref=false;
						ref=0;
				 		while(it_elements.hasNext())
				 		{
							Element current=it_elements.next();
							Attributes atts=current.attributes();
							Iterator<Attribute> ite=atts.iterator();
							while(ite.hasNext())
							{
								Attribute a=ite.next();
								if (a.getValue()!=null)
								{
									if (a.getValue().equalsIgnoreCase("b_algo"))
									{
										refr.add(current.text());
										start=true;
										starthref=true;
									}
									if (start && starthref && a.getKey().equalsIgnoreCase("href"))
									{
										refh.add(a.getValue());
										reft.add(current.text());
										starthref=false;
										ref++;
									}
								}
							}
						}
					}
					catch (IOException ee) {}
					catch (RuntimeException eee) {}
					catch (Exception e) {}
					finally{}
					ref=0;
					if (!vertical_results)
					{
						for (int i=0; i<10; i++)
						{
							if (reft.get(i)!=null) newvalues[ref]=reft.get(i);
							if (reft.get(i)==null) newvalues[ref]="";
							ref++;
							if (refr.get(i)!=null) newvalues[ref]=refr.get(i);
							if (refr.get(i)==null) newvalues[ref]="";
							ref++;
							if (refh.get(i)!=null) newvalues[ref]=refh.get(i);
							if (refh.get(i)==null) newvalues[ref]="";
							ref++;
						}
					}
					else
					{
						for (int i=0; i<refh.size(); i++)
						{
							newvalues[0]=reft.get(i);
							newvalues[1]=refr.get(i);
							newvalues[2]=refh.get(i);
							String[] wvalues=dsu.getnewvalues(values, newvalues);
							dw.write(wvalues);
						}
					}
				}
				else if (searchtype==3)
				{
					reft.clear();
					refh.clear();
					refr.clear();
					try
					{
						URL url = new URL(address + URLEncoder.encode(query[0], charset));
						addaddress=url.toString();
						Document doc=Jsoup.connect(addaddress).timeout(5000).userAgent("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)").get();
						Elements el=doc.getAllElements();
						Iterator<Element> it_elements = el.iterator();
						boolean start=false;
						boolean starthref=false;
						ref=0;
				 		while(it_elements.hasNext())
				 		{
							Element current=it_elements.next();
							Attributes atts=current.attributes();
							Iterator<Attribute> ite=atts.iterator();
							while(ite.hasNext())
							{
								Attribute a=ite.next();
								if (a.getValue()!=null)
								{
									if (a.getValue().equalsIgnoreCase("item_risultati_web"))
									{
										refr.add(current.text());
										start=true;
										starthref=true;
									}
									if (start && starthref && a.getKey().equalsIgnoreCase("href"))
									{
										refh.add(a.getValue());
										reft.add(current.text());
										starthref=false;
										ref++;
									}
								}
							}
						}
					}
					catch (Exception e) {}
					ref=0;
					if (!vertical_results)
					{
						for (int i=0; i<10; i++)
						{
							if (reft.get(i)!=null) newvalues[ref]=reft.get(i);
							if (reft.get(i)==null) newvalues[ref]="";
							ref++;
							if (refr.get(i)!=null) newvalues[ref]=refr.get(i);
							if (refr.get(i)==null) newvalues[ref]="";
							ref++;
							if (refh.get(i)!=null) newvalues[ref]=refh.get(i);
							if (refh.get(i)==null) newvalues[ref]="";
							ref++;
						}
					}
					else
					{
						for (int i=0; i<refh.size(); i++)
						{
							newvalues[0]=reft.get(i);
							newvalues[1]=refr.get(i);
							newvalues[2]=refh.get(i);
							String[] wvalues=dsu.getnewvalues(values, newvalues);
							dw.write(wvalues);
						}
					}
				}
				if (!vertical_results)
				{
					String[] wvalues=dsu.getnewvalues(values, newvalues);
					dw.write(wvalues);
				}
			}
			if (waitfor>0)
			{
				try
				{
					Thread.sleep(waitfor);
				}
				catch (Exception e) {}
			}
			Keywords.percentage_done=done;
			done++;
		}
		data.close();

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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3725, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 3726, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varquery, "var=all", true, 3727, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.searchengine, "listsingle=3885_" + Keywords.bing+",3886_"+Keywords.duckduckgo+",3887_"+Keywords.istella,false, 3884, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.waitbetween, "text", false, 3728, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vertical_results,"checkbox", false, 4230,dep,"",2));
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
		retprocinfo[0]="4181";
		retprocinfo[1]="3729";
		return retprocinfo;
	}
}
