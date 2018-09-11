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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.VarCoeff;

import ADaMSoft.keywords.Keywords;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import ADaMSoft.algorithms.EditsAnalyzer;

/**
* This is the procedure read the edits for the errors localizations
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcReadedits implements RunStep
{
	/**
	* Starts the execution of Proc Readedits and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.filewedits};
		String [] optionalparameters=new String[] {Keywords.noverbose, Keywords.checksol, Keywords.varcond, Keywords.reduceedits};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean varcond=(parameters.get(Keywords.varcond)!=null);
		boolean noverbose=(parameters.get(Keywords.noverbose)!=null);
		boolean checksol=(parameters.get(Keywords.checksol)!=null);
		boolean reduceedits=(parameters.get(Keywords.reduceedits)!=null);

		if (reduceedits && !varcond)
			return new Result("%3083%<br>\n", false, null);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		String filewedits=(String)parameters.get(Keywords.filewedits.toLowerCase());
		String keyword="Readedits "+filewedits;
		String description="Readedits "+filewedits;
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		String fileout=(String)parameters.get("fileout");

		Vector<StepResult> result = new Vector<StepResult>();

		java.net.URL fileUrl;
		try
		{
			if((filewedits.toLowerCase()).startsWith("http"))
				fileUrl =  new java.net.URL(filewedits);
			else
			{
				File file=new File(filewedits);
				fileUrl = file.toURI().toURL();
			}
		}
		catch (Exception e)
		{
			return new Result("%2444% ("+filewedits+")<br>\n", false, null);
		}
		String edits="";
		try
		{
		        BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
	        	String str;
				while ((str = in.readLine()) != null)
	        	{
					if (!str.trim().equals(""))
					{
						str=str.replaceAll(" ","");
						edits=edits+str;
					}
			}
			in.close();
		}
		catch (Exception e)
		{
			return new Result("%2445% ("+filewedits+")<br>\n"+e.toString()+"<br>\n", false, null);
		}
		Keywords.percentage_done++;

		Vector<String> origedits=new Vector<String>();
		Vector<String> tranedits=new Vector<String>();
		Vector<String> redundants=new Vector<String>();

		VarCoeff vc=new VarCoeff();
		String[] editsp=edits.split(";");
		String signs = "(<=|>=|=)";
		int type=0;
		Vector<Hashtable<String, String>> element=new Vector<Hashtable<String, String>>();
		TreeSet<String> varnames=new TreeSet<String>();
		Vector<String> bval=new Vector<String>();
		Vector<Integer> vectsign=new Vector<Integer>();
		int nrules=0;
		for (int i=0; i<editsp.length; i++)
		{
			HashSet<String> currentvarnames=new HashSet<String>();
			Hashtable<String, Double> tempelement=new Hashtable<String, Double>();
			editsp[i]=editsp[i].trim();
			if (!editsp[i].equals(""))
			{
				type=0;
				nrules++;
				origedits.add(editsp[i].toUpperCase());
				String[] fields = editsp[i].split(signs, -1);
				if (fields.length!=2)
					return new Result("%2446% "+editsp[i]+"<br>\n", false, null);
				if (editsp[i].indexOf(">=")>0)
					type=1;
				if (editsp[i].indexOf("<=")>0)
					type=-1;
				vectsign.add(new Integer(type));
				String sumdiff = "[\\+\\-]";
				double b=0.0;
				fields[0]=fields[0].trim();
				if (!fields[0].startsWith("-"))
				{
					if (!fields[0].startsWith("+"))
						fields[0]="+"+fields[0];
				}
				Pattern p = Pattern.compile(sumdiff);
				Matcher matcher = p.matcher(fields[0]);
				int first=0;
				String remaining="";
				String oldsign="";
				double multiplier=1;
				while (matcher.find())
				{
					int groupStart = matcher.start();
					int groupEnd = matcher.end();
					String temp=fields[0].substring(groupStart, groupEnd);
					remaining=(fields[0].substring(first, groupStart)).trim();
					first=groupEnd;
					if (!remaining.equals(""))
					{
						multiplier=1;
						if (oldsign.trim().equals("-"))
							multiplier=-1;
						vc.setpart(remaining);
						if (!vc.geterror().equals(""))
						{
							return new Result("%2448% "+editsp[i]+"<br>\n"+vc.geterror()+"<br>\n", false, null);
						}
						if (vc.getvarname().equals(""))
							b=b+multiplier*vc.getcoeff();
						else
						{
							if (currentvarnames.contains(vc.getvarname().toUpperCase()))
							{
								return new Result("%2449% "+editsp[i]+"<br>\n"+vc.getvarname()+"<br>\n", false, null);
							}
							currentvarnames.add(vc.getvarname().toUpperCase());
							varnames.add(vc.getvarname().toUpperCase());
							tempelement.put(vc.getvarname().toUpperCase(), new Double(multiplier*vc.getcoeff()));
						}
					}
					oldsign=temp;
				}
				remaining=fields[0].substring(first);
				if (!remaining.equals(""))
				{
					multiplier=1.0;
					vc.setpart(remaining);
					if (!vc.geterror().equals(""))
					{
						return new Result("%2448% "+editsp[i]+"<br>\n"+vc.geterror()+"<br>\n", false, null);
					}
					if (oldsign.trim().equals("-"))
						multiplier=-1;
					if (vc.getvarname().equals(""))
						b=b+multiplier*vc.getcoeff();
					else
					{
						if (currentvarnames.contains(vc.getvarname().toUpperCase()))
						{
							return new Result("%2449% "+editsp[i]+"<br>\n"+vc.getvarname()+"<br>\n", false, null);
						}
						currentvarnames.add(vc.getvarname().toUpperCase());
						varnames.add(vc.getvarname().toUpperCase());
						tempelement.put(vc.getvarname().toUpperCase(), new Double(multiplier*vc.getcoeff()));
					}
				}
				fields[1]=fields[1].trim();
				if (!fields[1].startsWith("-"))
				{
					if (!fields[1].startsWith("+"))
						fields[1]="+"+fields[1];
				}
				p = Pattern.compile(sumdiff);
				matcher = p.matcher(fields[1]);
				first=0;
				remaining="";
				oldsign="";
				while (matcher.find())
				{
					int groupStart = matcher.start();
					int groupEnd = matcher.end();
					String temp=fields[1].substring(groupStart, groupEnd);
					remaining=fields[1].substring(first, groupStart);
					first=groupEnd;
					if (!remaining.equals(""))
					{
						multiplier=-1.0;
						if (oldsign.trim().equals("-"))
							multiplier=1;
						vc.setpart(remaining);
						if (!vc.geterror().equals(""))
						{
							return new Result("%2448% "+editsp[i]+"<br>\n"+vc.geterror()+"<br>\n", false, null);
						}
						if (vc.getvarname().equals(""))
							b=b+multiplier*vc.getcoeff();
						else
						{
							if (currentvarnames.contains(vc.getvarname().toUpperCase()))
							{
								return new Result("%2449% "+editsp[i]+"<br>\n"+vc.getvarname()+"<br>\n", false, null);
							}
							currentvarnames.add(vc.getvarname().toUpperCase());
							varnames.add(vc.getvarname().toUpperCase());
							tempelement.put(vc.getvarname().toUpperCase(), new Double(multiplier*vc.getcoeff()));
						}
					}
					oldsign=temp;
				}
				remaining=fields[1].substring(first);
				if (!remaining.equals(""))
				{
					vc.setpart(remaining);
					if (!vc.geterror().equals(""))
					{
						return new Result("%2448% "+editsp[i]+"<br>\n"+vc.geterror()+"<br>\n", false, null);
					}
					multiplier=-1;
					if (oldsign.trim().equals("-"))
						multiplier=1;
					if (vc.getvarname().equals(""))
						b=b+multiplier*vc.getcoeff();
					else
					{
						if (currentvarnames.contains(vc.getvarname().toUpperCase()))
						{
							return new Result("%2449% "+editsp[i]+"<br>\n"+vc.getvarname()+"<br>\n", false, null);
						}
						currentvarnames.add(vc.getvarname().toUpperCase());
						varnames.add(vc.getvarname().toUpperCase());
						tempelement.put(vc.getvarname().toUpperCase(), new Double(multiplier*vc.getcoeff()));
					}
				}
				Hashtable<String, String> ts=new Hashtable<String, String>();
				for (Enumeration<String> en=tempelement.keys(); en.hasMoreElements();)
				{
					String vna = en.nextElement();
					double pva = Double.valueOf(tempelement.get(vna));
					if (type==-1)
						pva=-1*pva;
					String tempcf=String.valueOf(pva);
					ts.put(vna, tempcf);
				}
				if (type==-1)
				{
					if (b!=0)
						b=-1*b;
				}
				element.add(ts);
				bval.add(String.valueOf(b));
			}
		}
		result.add(new LocalMessageGetter("%2452% "+String.valueOf(nrules)+"<br>\n"));
		Keywords.percentage_done++;

		if (varnames.size()<2)
			return new Result("%2466%<br>\n", false, null);

		DataSetUtilities dsu=new DataSetUtilities();
		Iterator<String> itt = varnames.iterator();

		String[] namoutvar=new String[varnames.size()];
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		String varlistname="";
		String varlistnamefo="";
		dsu.addnewvar("_sign_", "%2451%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("_b_", "%2450%", Keywords.NUMSuffix, tempmd, tempmd);
		Hashtable<Integer, String> refvarnames=new Hashtable<Integer, String>();
		int ref=0;
		while(itt.hasNext())
		{
			String tvname=itt.next();
			refvarnames.put(new Integer(ref), tvname.toUpperCase());
			varlistname=varlistname+tvname+" ";
			namoutvar[ref]=tvname;
			dsu.addnewvar("var_"+tvname, tvname, Keywords.NUMSuffix, tempmd, tempmd);
			ref++;
		}
		varlistname=varlistname.trim();
		varlistname=varlistname.replaceAll(" ",";");
		varlistnamefo=varlistname.replaceAll(";","\t");

		int numconditions=element.size();

		boolean msgvarcond=false;

		if (varcond)
		{
			if (varnames.size()>180)
			{
				msgvarcond=true;
				varcond=false;
			}
			else
				numconditions=numconditions+varnames.size();
		}

		Vector<double[]> vectoriq=new Vector<double[]>();
		Vector<double[]> vectoreq=new Vector<double[]>();
		ref=0;
		int nrulepvar=0;
		int temprefred=-1;
		int csign=0;
		HashSet<Integer> refred=new HashSet<Integer>();
		for (int i=0; i<element.size(); i++)
		{
			double[] tempvectormat=new double[varnames.size()+1];
			for (int u=0; u<tempvectormat.length; u++)
			{
				tempvectormat[u]=0.0;
			}
			String tre="";
			Hashtable<String, String> ts=element.get(i);
			itt = varnames.iterator();
			int j=1;
			nrulepvar=0;
			temprefred=-1;
			csign=0;
			while(itt.hasNext())
			{
				String tvname=itt.next();
				if (ts.get(tvname)!=null)
				{
					tempvectormat[j]=Double.parseDouble(ts.get(tvname));
					if (tempvectormat[j]!=0)
					{
						nrulepvar++;
						temprefred=j-1;
						if (tempvectormat[j]<0)
							csign=-1;
					}
				}
				j++;
			}
			tempvectormat[0]=Double.parseDouble(bval.get(i));
			for (int k=1; k<tempvectormat.length; k++)
			{
				if (tempvectormat[k]>0)
				{
					if ((k==1) && (tempvectormat[k]==1))
						tre=tre+namoutvar[k-1];
					if ((k==1) && (tempvectormat[k]!=1))
						tre=tre+String.valueOf(tempvectormat[k])+"*"+namoutvar[k-1];
					if ((k>1) && (tempvectormat[k]==1))
						tre=tre+"+"+namoutvar[k-1];
					if ((k>1) && (tempvectormat[k]!=1))
						tre=tre+"+"+String.valueOf(tempvectormat[k])+"*"+namoutvar[k-1];
				}
				if (tempvectormat[k]<0)
				{
					if ((k==1) && (tempvectormat[k]==-1))
						tre=tre+"-"+namoutvar[k-1];
					if ((k==1) && (tempvectormat[k]!=-1))
						tre=tre+String.valueOf(tempvectormat[k])+"*"+namoutvar[k-1];
					if ((k>1) && (tempvectormat[k]==-1))
						tre=tre+"-"+namoutvar[k-1];
					if ((k>1) && (tempvectormat[k]!=-1))
						tre=tre+String.valueOf(tempvectormat[k])+"*"+namoutvar[k-1];
				}
			}
			if (tempvectormat[0]<0)
				tre=tre.trim()+String.valueOf(tempvectormat[0]);
			if (tempvectormat[0]>0)
				tre=tre.trim()+"+"+String.valueOf(tempvectormat[0]);
			int tsign=Integer.valueOf(vectsign.get(i));
			if (tsign==0)
			{
				tre=tre+"=0";
				vectoreq.add(tempvectormat);
			}
			else
			{
				tre=tre+">=0";
				vectoriq.add(tempvectormat);
			}
			if ((nrulepvar==1) && (csign==0))
				refred.add(new Integer(temprefred));
			tranedits.add(tre);
		}
		if (varcond)
		{
			for (int i=0; i<varnames.size(); i++)
			{
				if (!refred.contains(new Integer(i)))
				{
					double[] tempvectormat=new double[varnames.size()+1];
					tempvectormat[0]=0.0;
					for (int j=0; j<varnames.size(); j++)
					{
						tempvectormat[j+1]=0.0;
						if (i==j)
							tempvectormat[j+1]=1.0;
					}
					vectoriq.add(tempvectormat);
				}
				else
					redundants.add(namoutvar[i]+">=0");
			}
		}

		if (vectoriq.size()==0)
			return new Result("%2465%<br>\n", false, null);
		Keywords.percentage_done++;

		int eqedits=0;
		/*start check of duplicated edits*/
		HashSet<Vector<Double>> iqdup=new HashSet<Vector<Double>>();
		for (int i=0; i<vectoriq.size(); i++)
		{
			double[] tm=vectoriq.get(i);
			Vector<Double> iqdt=new Vector<Double>();
			for (int j=0; j<tm.length; j++)
			{
				iqdt.add(new Double(tm[j]));
			}
			iqdup.add(iqdt);
		}
		eqedits=vectoriq.size()-iqdup.size();
		double[][] iq=new double[iqdup.size()][varnames.size()+1];
		Iterator<Vector<Double>> itrqdup = iqdup.iterator();
		Vector<Double> tved=new Vector<Double>();
		int posinedit=0;
		while(itrqdup.hasNext())
		{
			tved=(itrqdup.next());
			for (int i=0; i<tved.size(); i++)
			{
				iq[posinedit][i]=(tved.get(i)).doubleValue();
			}
			posinedit++;
		}
		iqdup.clear();
		double[][] eq=null;
		if (vectoreq.size()>0)
		{
			HashSet<Vector<Double>> eqdup=new HashSet<Vector<Double>>();
			for (int i=0; i<vectoreq.size(); i++)
			{
				double[] tm=vectoreq.get(i);
				Vector<Double> eqdt=new Vector<Double>();
				for (int j=0; j<tm.length; j++)
				{
					eqdt.add(new Double(tm[j]));
				}
				eqdup.add(eqdt);
			}
			eqedits=eqedits+vectoreq.size()-eqdup.size();
			eq=new double[eqdup.size()][varnames.size()+1];
			itrqdup = eqdup.iterator();
			posinedit=0;
			while(itrqdup.hasNext())
			{
				tved=(itrqdup.next());
				for (int i=0; i<tved.size(); i++)
				{
					eq[posinedit][i]=(tved.get(i)).doubleValue();
				}
				posinedit++;
			}
			eqdup.clear();
		}

		String tempdir=(String)parameters.get(Keywords.WorkDir);
		EditsAnalyzer ea=new EditsAnalyzer(tempdir);
		Keywords.percentage_done++;

		if (fileout!=null)
		{
			try
			{
	    		BufferedWriter out = new BufferedWriter(new FileWriter(fileout));
				out.write("Constantt"+varlistnamefo+"\n");
				String tmatp="";
				if (eq!=null)
				{
				    out.write("EQ\n");
					for (int i=0; i<eq.length; i++)
					{
						tmatp="";
						for (int ma=0; ma<eq[0].length; ma++)
						{
							tmatp=tmatp+String.valueOf(eq[i][ma]);
							if (ma<(eq[0].length-1))
								tmatp=tmatp+"\t";
						}
						tmatp=tmatp.trim()+"\n";
						out.write(tmatp);
					}
				}
			    out.write("IQ\n");
				for (int i=0; i<iq.length; i++)
				{
					tmatp="";
					for (int ma=0; ma<iq[0].length; ma++)
					{
						tmatp=tmatp+String.valueOf(iq[i][ma]);
						if (ma<(iq[0].length-1))
							tmatp=tmatp+"\t";
					}
					tmatp=tmatp.trim()+"\n";
					out.write(tmatp);
				}
			    out.close();
			} catch (Exception exsc) {}
		}

		ea.setEdits(iq, eq);
		String disjoint="";
		int testde=ea.checkDisjointEdits();
		String namevarinde="";
		int totalsol=0;
		String msgrededit="";
		double tempcred=0;
		if (testde>1)
		{
			disjoint="%3038% ("+testde+"); %3039%:\n";
			for (int i=0; i<testde; i++)
			{
				disjoint=disjoint+"%3040% "+String.valueOf(i+1)+":\n";
				Vector<Integer> listv=ea.getvarinde(i);
				double[][] tdiq=ea.getdisiq(i);
				double[][] tdeq=ea.getdiseq(i);
				if (tdiq!=null)
				{
					for (int j=0; j<tdiq.length; j++)
					{
						for (int k=1; k<tdiq[0].length; k++)
						{
							if (tdiq[j][k]!=0.0)
							{
								posinedit=(listv.get(k-1)).intValue();
								namevarinde=refvarnames.get(new Integer(posinedit));
								if (tdiq[j][k]<0)
								{
									if (tdiq[j][k]==-1.0)
										disjoint=disjoint+"-"+namevarinde;
									else
										disjoint=disjoint+String.valueOf(tdiq[j][k])+"*"+namevarinde;
								}
								else
								{
									if (tdiq[j][k]!=1.0)
										disjoint=disjoint+"+"+String.valueOf(tdiq[j][k])+"*"+namevarinde;
									else
										disjoint=disjoint+"+"+namevarinde;
								}
							}
						}
						if (tdiq[j][0]>0.0)
							disjoint=disjoint+"-"+tdiq[j][0]+">=0\n";
						else if (tdiq[j][0]<0.0)
							disjoint=disjoint+tdiq[j][0]+">=0\n";
						else
							disjoint=disjoint+">=0\n";
					}
				}
				if (tdeq!=null)
				{
					for (int j=0; j<tdeq.length; j++)
					{
						for (int k=1; k<tdeq[0].length; k++)
						{
							if (tdeq[j][k]!=0.0)
							{
								posinedit=(listv.get(k-1)).intValue();
								namevarinde=refvarnames.get(new Integer(posinedit));
								if (tdeq[j][k]<0)
								{
									if (tdeq[j][k]==-1.0)
										disjoint=disjoint+"-"+namevarinde;
									else
										disjoint=disjoint+String.valueOf(tdeq[j][k])+"*"+namevarinde;
								}
								else
								{
									if (tdeq[j][k]!=1.0)
										disjoint=disjoint+"+"+String.valueOf(tdeq[j][k])+"*"+namevarinde;
									else
										disjoint=disjoint+"+"+namevarinde;
								}
							}
						}
						if (tdeq[j][0]>0.0)
							disjoint=disjoint+"-"+tdeq[j][0]+"=0\n";
						else if (tdeq[j][0]<0.0)
							disjoint=disjoint+tdeq[j][0]+"=0\n";
						else
							disjoint=disjoint+"=0\n";
					}
				}
				if (checksol)
				{
					if (tdiq!=null)
					{
						ea.verifysol(tdiq, tdeq);
						totalsol=ea.getTotalSolutions();
						disjoint=disjoint+"%3042%: "+String.valueOf(totalsol)+"\n";
						disjoint=disjoint+"%2556%: "+String.valueOf(ea.getTimeNeeded())+"\n";
					}
				}
				if (reduceedits)
				{
					boolean restestred=ea.reduceedits(tdiq, tdeq);
					if (!restestred)
					{
						msgrededit=msgrededit+"%3084% ("+String.valueOf(i+1)+").\n%3085%\n";
					}
					else
					{
						Vector<Vector<Double>> testrede=ea.getorigedit();
						if (testrede.size()==0) msgrededit=msgrededit+"%3086% "+String.valueOf(i+1)+" %3087%\n";
						else
						{
							msgrededit=msgrededit+"%3086% "+String.valueOf(i+1)+" %3088%:\n";
							for (int j=0; j<testrede.size(); j++)
							{
								Vector<Double> temparede=testrede.get(j);
								String actualeditred="";
								for (int k=1; k<temparede.size(); k++)
								{
									posinedit=(listv.get(k-1)).intValue();
									namevarinde=refvarnames.get(new Integer(posinedit));
									tempcred=(temparede.get(k)).doubleValue();
									if (tempcred!=0.0)
									{
										if (tempcred==1.0)
										{
											if (actualeditred.equals("")) actualeditred=namevarinde;
											else actualeditred=actualeditred+"+"+namevarinde;
										}
										else if (tempcred==-1.0)
										{
											if (actualeditred.equals("")) actualeditred="-"+namevarinde;
											else actualeditred=actualeditred+"-"+namevarinde;
										}
										else
										{
											if (tempcred>0) actualeditred=actualeditred+"+"+String.valueOf(tempcred)+"*"+namevarinde;
											else actualeditred=actualeditred+String.valueOf(tempcred)+"*"+namevarinde;
										}
									}
								}
								tempcred=(temparede.get(0)).doubleValue();
								if (tempcred>0) actualeditred=actualeditred+"+"+String.valueOf(tempcred)+">=0;";
								else if (tempcred<0) actualeditred=actualeditred+String.valueOf(tempcred)+">=0;";
								else actualeditred=actualeditred+">=0;";
								actualeditred=actualeditred+"\n";
								msgrededit=msgrededit+actualeditred;
							}
							Vector<Vector<Double>> testredue=ea.getreduedit();
							if (testredue.size()>0)
							{
								msgrededit=msgrededit+"%3091%:\n";
								for (int j=0; j<testredue.size(); j++)
								{
									Vector<Double> temparede=testredue.get(j);
									String actualeditred="";
									for (int k=1; k<temparede.size(); k++)
									{
										posinedit=(listv.get(k-1)).intValue();
										namevarinde=refvarnames.get(new Integer(posinedit));
										tempcred=(temparede.get(k)).doubleValue();
										if (tempcred!=0.0)
										{
											if (tempcred==1.0)
											{
												if (actualeditred.equals("")) actualeditred=namevarinde;
												else actualeditred=actualeditred+"+"+namevarinde;
											}
											else if (tempcred==-1.0)
											{
												if (actualeditred.equals("")) actualeditred="-"+namevarinde;
												else actualeditred=actualeditred+"-"+namevarinde;
											}
											else
											{
												if (tempcred>0) actualeditred=actualeditred+"+"+String.valueOf(tempcred)+"*"+namevarinde;
												else actualeditred=actualeditred+String.valueOf(tempcred)+"*"+namevarinde;
											}
										}
									}
									tempcred=(temparede.get(0)).doubleValue();
									if (tempcred>0) actualeditred=actualeditred+"+"+String.valueOf(tempcred)+">=0;";
									else if (tempcred<0) actualeditred=actualeditred+String.valueOf(tempcred)+">=0;";
									else actualeditred=actualeditred+">=0;";
									actualeditred=actualeditred+"\n";
									msgrededit=msgrededit+actualeditred;
								}
							}
						}
					}
				}
			}
		}
		else
		{
			result.add(new LocalMessageGetter("%3041%<br>\n"));
			if (checksol)
			{
				if (!ea.verifysol(iq, eq))
					return new Result("%2468%<br>\n", false, null);
				totalsol=ea.getTotalSolutions();
				result.add(new LocalMessageGetter("%2459%: "+String.valueOf(totalsol)+"<br>\n"));
				result.add(new LocalMessageGetter("%2556%: "+String.valueOf(ea.getTimeNeeded())+"<br>\n"));
			}
			if (reduceedits)
			{
				boolean restestred=ea.reduceedits(iq, eq);
				if (!restestred)
				{
					msgrededit=msgrededit+"%3089%\n";
				}
				else
				{
					msgrededit=msgrededit+"%3090%:\n";
					Vector<Vector<Double>> testrede=ea.getorigedit();
					for (int j=0; j<testrede.size(); j++)
					{
						Vector<Double> temparede=testrede.get(j);
						String actualeditred="";
						for (int k=1; k<temparede.size(); k++)
						{
							namevarinde=refvarnames.get(new Integer(k-1));
							tempcred=(temparede.get(k)).doubleValue();
							if (tempcred!=0.0)
							{
								if (tempcred==1.0)
								{
									if (actualeditred.equals("")) actualeditred=namevarinde;
									else actualeditred=actualeditred+"+"+namevarinde;
								}
								else if (tempcred==-1.0)
								{
									if (actualeditred.equals("")) actualeditred="-"+namevarinde;
									else actualeditred=actualeditred+"-"+namevarinde;
								}
								else
								{
									if (tempcred>0) actualeditred=actualeditred+"+"+String.valueOf(tempcred)+"*"+namevarinde;
									else actualeditred=actualeditred+String.valueOf(tempcred)+"*"+namevarinde;
								}
							}
						}
						tempcred=(temparede.get(0)).doubleValue();
						if (tempcred>0) actualeditred=actualeditred+"+"+String.valueOf(tempcred)+">=0;";
						else if (tempcred<0) actualeditred=actualeditred+String.valueOf(tempcred)+">=0;";
						else actualeditred=actualeditred+">=0;";
						actualeditred=actualeditred+"\n";
						msgrededit=msgrededit+actualeditred;
					}
					Vector<Vector<Double>> testredue=ea.getreduedit();
					if (testredue.size()>0)
					{
						msgrededit=msgrededit+"%3091%:\n";
						for (int j=0; j<testredue.size(); j++)
						{
							Vector<Double> temparede=testredue.get(j);
							String actualeditred="";
							for (int k=1; k<temparede.size(); k++)
							{
								namevarinde=refvarnames.get(new Integer(k-1));
								tempcred=(temparede.get(k)).doubleValue();
								if (tempcred!=0.0)
								{
									if (tempcred==1)
									{
										if (actualeditred.equals("")) actualeditred=namevarinde;
										else actualeditred=actualeditred+"+"+namevarinde;
									}
									else
									{
										if (tempcred>0) actualeditred=actualeditred+"+"+String.valueOf(tempcred)+"*"+namevarinde;
										else actualeditred=actualeditred+String.valueOf(tempcred)+"*"+namevarinde;
									}
								}
							}
							tempcred=(temparede.get(0)).doubleValue();
							if (tempcred>0) actualeditred=actualeditred+"+"+String.valueOf(tempcred)+">=0;";
							else if (tempcred<0) actualeditred=actualeditred+String.valueOf(tempcred)+">=0;";
							else actualeditred=actualeditred+">=0;";
							actualeditred=actualeditred+"\n";
							msgrededit=msgrededit+actualeditred;
						}
					}
				}
			}
		}
		Keywords.percentage_done++;

		if (checksol) result.add(new LocalMessageGetter("%2461%<br>\n"));

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] values=new String[varnames.size()+2];
		if (eq!=null)
		{
			for (int i=0; i<vectoreq.size(); i++)
			{
				values[0]="=";
				double[] tm=vectoreq.get(i);
				for (int j=0; j<tm.length; j++)
				{
					values[j+1]=String.valueOf(tm[j]);
					if (values[j+1].equals("-0.0"))
						values[j+1]="0.0";
				}
				dw.write(values);
			}
		}
		boolean implicit=false;
		double[][] coefficientsiq=new double[vectoriq.size()][varnames.size()+1];
		for (int i=0; i<vectoriq.size(); i++)
		{
			values[0]=">=";
			double[] tm=vectoriq.get(i);
			for (int j=0; j<tm.length; j++)
			{
				values[j+1]=String.valueOf(tm[j]);
				if (values[j+1].equals("-0.0"))
					values[j+1]="0.0";
				coefficientsiq[i][j]=tm[j];
			}
			dw.write(values);
		}

		for (int i=0; i<varnames.size(); i++)
		{
			for (int j=0; j<vectoriq.size()-1; j++)
			{
				for (int h=j+1; h<vectoriq.size(); h++)
				{
					if (coefficientsiq[j][i+1]*coefficientsiq[h][i+1]<0)
					{
						implicit=true;
						break;
					}
				}
			}
		}

		result.add(new LocalMessageGetter("%2462%<br>\n"+varlistname.toUpperCase()+"<br>\n"));

		if (!noverbose)
		{
			result.add(new LocalMessageGetter("%2453%<br>\n"));
			for (int i=0; i<tranedits.size(); i++)
			{
				String oldedit=origedits.get(i);
				String newedit=tranedits.get(i);
				result.add(new LocalMessageGetter(String.valueOf(i+1)+") %2454% "+oldedit+"<br>\n"));
				result.add(new LocalMessageGetter(String.valueOf(i+1)+") %2455% "+newedit+"<br><br>\n"));
			}
		}

		if (varcond)
		{
			if (redundants.size()>0)
			{
				result.add(new LocalMessageGetter("<br>\n%2463%:<br>\n"));
				for (int i=0; i<redundants.size(); i++)
				{
					result.add(new LocalMessageGetter(redundants.get(i)+"<br>\n"));
				}
				result.add(new LocalMessageGetter("<br>\n"));
			}
		}

		if (implicit)
			result.add(new LocalMessageGetter("%2479%<br>\n"));

		if (!disjoint.equals(""))
		{
			result.add(new LocalMessageGetter(disjoint+"<br>\n"));
		}

		if (!msgrededit.equals("")) result.add(new LocalMessageGetter(msgrededit+"<br>\n"));

		if (msgvarcond)
			result.add(new LocalMessageGetter("%2460%<br>\n"));

		if (eqedits>0)
				result.add(new LocalMessageGetter("%2612% ("+String.valueOf(eqedits)+")<br>\n"));

		Keywords.percentage_total=0;
		Keywords.percentage_done=0;

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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 2441, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.filewedits, "file=all", true, 2442, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noverbose, "checkbox", false, 2443, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.checksol, "checkbox", false, 3034, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.reduceedits, "checkbox", false, 3082, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcond, "checkbox", false, 2457, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="2439";
		retprocinfo[1]="2440";
		return retprocinfo;
	}
}
