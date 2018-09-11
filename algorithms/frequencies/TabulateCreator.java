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

package ADaMSoft.algorithms.frequencies;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import ADaMSoft.utilities.VectorStringComparatorWithMd;
import ADaMSoft.keywords.Keywords;

/**
* This method is used inside the tabulate step
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class TabulateCreator
{
	Hashtable<Integer, Hashtable<Vector<String>, double[]>> rowres;
	Hashtable<Integer, Hashtable<Vector<String>, double[]>> colres;
	Hashtable<Vector<Integer>, Hashtable<Vector<String>, double[]>> result;

	Hashtable<Integer, Hashtable<Vector<String>, Vector<String>>> rowval;
	Hashtable<Integer, Hashtable<Vector<String>, Vector<String>>> colval;
	Hashtable<Vector<Integer>, Hashtable<Vector<String>, Vector<String>>> resval;

	Hashtable<Integer, Vector<String>> rowvars;
	Hashtable<Integer, Vector<String>> colvars;
	String[] varclass;
	Hashtable<String, Integer> varposition;

	Hashtable<Integer, LinkedList<ResTabulate>> defrowres;
	Hashtable<Integer, LinkedList<ResTabulate>> defcolres;
	Hashtable<Vector<Integer>, LinkedList<ResTabulate>> defresult;

	/**
	*Starts the method
	*/
	public TabulateCreator(String[] varclass, Hashtable<String, Integer> varposition)
	{
		this.varclass=varclass;
		this.varposition=varposition;
		rowres=new Hashtable<Integer, Hashtable<Vector<String>, double[]>>();
		colres=new Hashtable<Integer, Hashtable<Vector<String>, double[]>>();
		result=new Hashtable<Vector<Integer>, Hashtable<Vector<String>, double[]>>();

		rowval=new Hashtable<Integer, Hashtable<Vector<String>, Vector<String>>>();
		colval=new Hashtable<Integer, Hashtable<Vector<String>, Vector<String>>>();
		resval=new Hashtable<Vector<Integer>, Hashtable<Vector<String>, Vector<String>>>();

		defrowres=new Hashtable<Integer, LinkedList<ResTabulate>>();
		defcolres=new Hashtable<Integer, LinkedList<ResTabulate>>();
		defresult=new Hashtable<Vector<Integer>, LinkedList<ResTabulate>>();

	}
	/**
	*Receives the information on the row variable positions on the table
	*/
	public void setrowvars(Hashtable<Integer, Vector<String>> rowvars)
	{
		this.rowvars=rowvars;
	}
	/**
	*Receives the information on the column variable positions on the table
	*/
	public void setcolvars(Hashtable<Integer, Vector<String>> colvars)
	{
		this.colvars=colvars;
	}
	/**
	*Receives the actual values
	*/
	public void setValues(String[][] values, double weightvalue)
	{
		boolean valtoconsider=true;
		boolean isclass=false;
		if (colvars.size()==0)
		{
			for (Enumeration<Integer> en=rowvars.keys(); en.hasMoreElements();)
			{
				Integer keyname=en.nextElement();
				Vector<String> varnames=rowvars.get(keyname);
				Hashtable<Vector<String>, double[]> temprowres=rowres.get(keyname);
				Hashtable<Vector<String>, Vector<String>> temprowval=rowval.get(keyname);
				if (temprowres==null)
				{
					temprowres=new Hashtable<Vector<String>, double[]>();
					temprowval=new Hashtable<Vector<String>, Vector<String>>();
				}
				Vector<String> diffvalues=new Vector<String>();
				Vector<String> origvalues=new Vector<String>();
				int[] position=new int[varnames.size()];
				valtoconsider=true;
				double refvalue=1;
				for (int i=0; i<varnames.size(); i++)
				{
					isclass=false;
					if (!varnames.get(i).equals("1"))
					{
						position[i]=(varposition.get(varnames.get(i).toLowerCase())).intValue();
						for (int j=0; j<varclass.length; j++)
						{
							if (varnames.get(i).equalsIgnoreCase(varclass[j]))
								isclass=true;
						}
						if (isclass)
						{
							if (!values[position[i]][1].equals(""))
							{
								diffvalues.add(values[position[i]][1]);
								origvalues.add(values[position[i]][0]);
							}
							else
								valtoconsider=false;
						}
						else
						{
							try
							{
								double test=Double.parseDouble(values[position[i]][1]);
								refvalue=refvalue*test;
								if (Double.isNaN(test))
									valtoconsider=false;
								else
								{
									diffvalues.add("");
									origvalues.add("");
								}
							}
							catch (Exception e)
							{
								valtoconsider=false;
							}
						}
					}
					else
					{
						isclass=true;
						diffvalues.add("1");
						origvalues.add("1");
					}
				}
				if (valtoconsider)
				{
					double[] valuestoconsider=new double[3];
					valuestoconsider[0]=weightvalue;
					valuestoconsider[1]=weightvalue*refvalue;
					valuestoconsider[2]=weightvalue*refvalue*refvalue;
					if (temprowres.get(diffvalues)==null)
					{
						temprowres.put(diffvalues, valuestoconsider);
						temprowval.put(diffvalues, origvalues);
					}
					else
					{
						double[] tvaluestoconsider=temprowres.get(diffvalues);
						valuestoconsider[0]=tvaluestoconsider[0]+valuestoconsider[0];
						valuestoconsider[1]=tvaluestoconsider[1]+valuestoconsider[1];
						valuestoconsider[2]=tvaluestoconsider[2]+valuestoconsider[2];
						temprowres.put(diffvalues, valuestoconsider);
						Vector<String> torigvalues=temprowval.get(diffvalues);
						for (int v=0; v<torigvalues.size(); v++)
						{
							String va=torigvalues.get(v);
							String vb=origvalues.get(v);
							try
							{
								double tva=Double.parseDouble(va);
								double tvb=Double.parseDouble(vb);
								if (tvb>tva)
									torigvalues.set(v, vb);
							}
							catch (Exception edcv)
							{
								if (vb.compareTo(va)>0)
									torigvalues.set(v, vb);
							}
						}
						temprowval.put(diffvalues, torigvalues);
					}
					rowres.put(keyname, temprowres);
					rowval.put(keyname, temprowval);
				}
			}
		}
		else if (rowvars.size()==0)
		{
			for (Enumeration<Integer> en=colvars.keys(); en.hasMoreElements();)
			{
				Integer keyname=en.nextElement();
				Vector<String> varnames=colvars.get(keyname);
				Hashtable<Vector<String>, double[]> tempcolres=colres.get(keyname);
				Hashtable<Vector<String>, Vector<String>> tempcolval=colval.get(keyname);
				if (tempcolres==null)
				{
					tempcolres=new Hashtable<Vector<String>, double[]>();
					tempcolval=new Hashtable<Vector<String>, Vector<String>>();
				}
				Vector<String> diffvalues=new Vector<String>();
				Vector<String> origvalues=new Vector<String>();
				int[] position=new int[varnames.size()];
				valtoconsider=true;
				double refvalue=1;
				for (int i=0; i<varnames.size(); i++)
				{
					isclass=false;
					if (!varnames.get(i).equals("1"))
					{
						position[i]=(varposition.get(varnames.get(i).toLowerCase())).intValue();
						for (int j=0; j<varclass.length; j++)
						{
							if (varnames.get(i).equalsIgnoreCase(varclass[j]))
								isclass=true;
						}
						if (isclass)
						{
							if (!values[position[i]][1].equals(""))
							{
								diffvalues.add(values[position[i]][1]);
								origvalues.add(values[position[i]][0]);
							}
							else
								valtoconsider=false;
						}
						else
						{
							try
							{
								double test=Double.parseDouble(values[position[i]][1]);
								refvalue=refvalue*test;
								if (Double.isNaN(test))
									valtoconsider=false;
								else
								{
									diffvalues.add("");
									origvalues.add("");
								}
							}
							catch (Exception e)
							{
								valtoconsider=false;
							}
						}
					}
					else
					{
						isclass=true;
						diffvalues.add("1");
						origvalues.add("1");
					}
				}
				if (valtoconsider)
				{
					double[] valuestoconsider=new double[3];
					valuestoconsider[0]=weightvalue;
					valuestoconsider[1]=weightvalue*refvalue;
					valuestoconsider[2]=weightvalue*refvalue*refvalue;
					if (tempcolres.get(diffvalues)==null)
					{
						tempcolres.put(diffvalues, valuestoconsider);
						tempcolval.put(diffvalues, origvalues);
					}
					else
					{
						double[] tvaluestoconsider=tempcolres.get(diffvalues);
						valuestoconsider[0]=tvaluestoconsider[0]+valuestoconsider[0];
						valuestoconsider[1]=tvaluestoconsider[1]+valuestoconsider[1];
						valuestoconsider[2]=tvaluestoconsider[2]+valuestoconsider[2];
						tempcolres.put(diffvalues, valuestoconsider);
						Vector<String> torigvalues=tempcolval.get(diffvalues);
						for (int v=0; v<torigvalues.size(); v++)
						{
							String va=torigvalues.get(v);
							String vb=origvalues.get(v);
							try
							{
								double tva=Double.parseDouble(va);
								double tvb=Double.parseDouble(vb);
								if (tvb>tva)
									torigvalues.set(v, vb);
							}
							catch (Exception edcv)
							{
								if (vb.compareTo(va)>0)
									torigvalues.set(v, vb);
							}
						}
						tempcolval.put(diffvalues, torigvalues);
					}
					colres.put(keyname, tempcolres);
					colval.put(keyname, tempcolval);
				}
			}
		}
		else
		{
			for (Enumeration<Integer> enr=rowvars.keys(); enr.hasMoreElements();)
			{
				Integer keynamer=enr.nextElement();
				Vector<String> diffvaluesr=new Vector<String>();
				Vector<String> varnamesr=rowvars.get(keynamer);
				Vector<String> origvaluesr=new Vector<String>();
				int[] positionr=new int[varnamesr.size()];
				valtoconsider=true;
				double refvaluer=1;
				for (int i=0; i<varnamesr.size(); i++)
				{
					isclass=false;
					if (!varnamesr.get(i).equals("1"))
					{
						positionr[i]=(varposition.get(varnamesr.get(i).toLowerCase())).intValue();
						for (int j=0; j<varclass.length; j++)
						{
							if (varnamesr.get(i).equalsIgnoreCase(varclass[j]))
								isclass=true;
						}
						if (isclass)
						{
							if (!values[positionr[i]][1].equals(""))
							{
								diffvaluesr.add(values[positionr[i]][1]);
								origvaluesr.add(values[positionr[i]][0]);
							}
							else
								valtoconsider=false;
						}
						else
						{
							try
							{
								double test=Double.parseDouble(values[positionr[i]][1]);
								refvaluer=refvaluer*test;
								if (Double.isNaN(test))
									valtoconsider=false;
								else
								{
									diffvaluesr.add("");
									origvaluesr.add("");
								}
							}
							catch (Exception e)
							{
								valtoconsider=false;
							}
						}
					}
					else
					{
						isclass=true;
						diffvaluesr.add("1");
						origvaluesr.add("1");
					}
				}
				if (valtoconsider)
				{
					for (Enumeration<Integer> enc=colvars.keys(); enc.hasMoreElements();)
					{
						Integer keynamec=enc.nextElement();
						Vector<Integer> keyname=new Vector<Integer>();
						keyname.add(keynamer);
						keyname.add(keynamec);
						Vector<String> varnamesc=colvars.get(keynamec);
						Hashtable<Vector<String>, double[]> tempres=result.get(keyname);
						Hashtable<Vector<String>, Vector<String>> tempresval=resval.get(keyname);
						if (tempres==null)
						{
							tempres=new Hashtable<Vector<String>, double[]>();
							tempresval=new Hashtable<Vector<String>, Vector<String>>();
						}
						Vector<String> diffvaluesc=new Vector<String>();
						Vector<String> origvaluesc=new Vector<String>();
						int[] positionc=new int[varnamesc.size()];
						double refvaluec=1;
						for (int i=0; i<varnamesc.size(); i++)
						{
							isclass=false;
							if (!varnamesc.get(i).equals("1"))
							{
								positionc[i]=(varposition.get(varnamesc.get(i).toLowerCase())).intValue();
								for (int j=0; j<varclass.length; j++)
								{
									if (varnamesc.get(i).equalsIgnoreCase(varclass[j]))
										isclass=true;
								}
								if (isclass)
								{
									if (!values[positionc[i]].equals(""))
									{
										diffvaluesc.add(values[positionc[i]][1]);
										origvaluesc.add(values[positionc[i]][0]);
									}
									else
										valtoconsider=false;
								}
								else
								{
									try
									{
										double test=Double.parseDouble(values[positionc[i]][1]);
										refvaluec=refvaluec*test;
										if (Double.isNaN(test))
											valtoconsider=false;
										else
										{
											diffvaluesc.add("");
											origvaluesc.add("");
										}
									}
									catch (Exception e)
									{
										valtoconsider=false;
									}
								}
							}
							else
							{
								isclass=true;
								diffvaluesc.add("1");
								origvaluesc.add("1");
							}
						}
						if (valtoconsider)
						{
							Vector<String> origvalues=new Vector<String>();
							Vector<String> diffvalues=new Vector<String>();
							for (int i=0; i<diffvaluesr.size(); i++)
							{
								diffvalues.add(diffvaluesr.get(i));
								origvalues.add(origvaluesr.get(i));
							}
							for (int i=0; i<diffvaluesc.size(); i++)
							{
								origvalues.add(origvaluesc.get(i));
								diffvalues.add(diffvaluesc.get(i));
							}
							double[] valuestoconsider=new double[3];
							valuestoconsider[0]=weightvalue;
							valuestoconsider[1]=weightvalue*refvaluec*refvaluer;
							valuestoconsider[2]=weightvalue*refvaluec*refvaluec*refvaluer*refvaluer;
							if (tempres.get(diffvalues)==null)
							{
								tempres.put(diffvalues, valuestoconsider);
								tempresval.put(diffvalues, origvalues);
							}
							else
							{
								double[] tvaluestoconsider=tempres.get(diffvalues);
								valuestoconsider[0]=tvaluestoconsider[0]+valuestoconsider[0];
								valuestoconsider[1]=tvaluestoconsider[1]+valuestoconsider[1];
								valuestoconsider[2]=tvaluestoconsider[2]+valuestoconsider[2];
								tempres.put(diffvalues, valuestoconsider);
								Vector<String> torigvalues=tempresval.get(diffvalues);
								for (int v=0; v<torigvalues.size(); v++)
								{
									String va=torigvalues.get(v);
									String vb=origvalues.get(v);
									try
									{
										double tva=Double.parseDouble(va);
										double tvb=Double.parseDouble(vb);
										if (tvb>tva)
											torigvalues.set(v, vb);
									}
									catch (Exception edcv)
									{
										if (vb.compareTo(va)>0)
											torigvalues.set(v, vb);
									}
								}
								tempresval.put(diffvalues, torigvalues);
							}
							result.put(keyname, tempres);
							resval.put(keyname, tempresval);
						}
					}
				}
			}
		}
	}
	/**
	*This method is used to set up the order of the resulting table
	*/
	public void calculate(boolean orderbyval)
	{
		if (colvars.size()==0)
		{
			boolean veciseq=true;
			int toteq=0;
			for (Enumeration<Integer> en=rowval.keys(); en.hasMoreElements();)
			{
				LinkedList<ResTabulate> tempdefrowres=new LinkedList<ResTabulate>();
				Integer keyname=en.nextElement();
				Hashtable<Vector<String>, Vector<String>> temprowval=rowval.get(keyname);
				TreeSet<Vector<String>> tsc=new TreeSet<Vector<String>>(new VectorStringComparatorWithMd());
				for (Enumeration<Vector<String>> enn=temprowval.keys(); enn.hasMoreElements();)
				{
					Vector<String> ttva=enn.nextElement();
					Vector<String> ttvb=temprowval.get(ttva);
					if (orderbyval)
						tsc.add(ttvb);
					else
						tsc.add(ttva);
				}
				if (!orderbyval)
				{
					Iterator<Vector<String>> itsc = tsc.iterator();
					Hashtable<Vector<String>, double[]> temprowres=rowres.get(keyname);
					while(itsc.hasNext())
					{
						Vector<String> tempttva=itsc.next();
						tempdefrowres.add(new ResTabulate(tempttva, temprowres.get(tempttva)));
					}
				}
				else
				{
					Iterator<Vector<String>> itsc = tsc.iterator();
					Hashtable<Vector<String>, double[]> temprowres=rowres.get(keyname);
					while(itsc.hasNext())
					{
						Vector<String> tempttvb=itsc.next();
						veciseq=false;
						for (Enumeration<Vector<String>> enn=temprowval.keys(); enn.hasMoreElements();)
						{
							Vector<String> ttva=enn.nextElement();
							Vector<String> ttvb=temprowval.get(ttva);
							if (!veciseq)
							{
								if (tempttvb.size()==ttvb.size())
								{
									toteq=0;
									for (int i=0; i<ttva.size(); i++)
									{
										String testva=tempttvb.get(i);
										String testvb=ttvb.get(i);
										if (testva.equals(testvb))
											toteq++;
									}
									if (toteq==tempttvb.size())
									{
										tempdefrowres.add(new ResTabulate(ttva, temprowres.get(ttva)));
										veciseq=true;
									}
								}
							}
						}
					}
				}
				defrowres.put(keyname, tempdefrowres);
			}
		}
		else if (rowvars.size()==0)
		{
			boolean veciseq=true;
			int toteq=0;
			for (Enumeration<Integer> en=colval.keys(); en.hasMoreElements();)
			{
				LinkedList<ResTabulate> tempdefcolres=new LinkedList<ResTabulate>();
				Integer keyname=en.nextElement();
				Hashtable<Vector<String>, Vector<String>> tempcolval=colval.get(keyname);
				TreeSet<Vector<String>> tsc=new TreeSet<Vector<String>>(new VectorStringComparatorWithMd());
				for (Enumeration<Vector<String>> enn=tempcolval.keys(); enn.hasMoreElements();)
				{
					Vector<String> ttva=enn.nextElement();
					Vector<String> ttvb=tempcolval.get(ttva);
					if (orderbyval)
						tsc.add(ttvb);
					else
						tsc.add(ttva);
				}
				if (!orderbyval)
				{
					Iterator<Vector<String>> itsc = tsc.iterator();
					Hashtable<Vector<String>, double[]> tempcolres=colres.get(keyname);
					while(itsc.hasNext())
					{
						Vector<String> tempttva=itsc.next();
						tempdefcolres.add(new ResTabulate(tempttva, tempcolres.get(tempttva)));
					}
				}
				else
				{
					Iterator<Vector<String>> itsc = tsc.iterator();
					Hashtable<Vector<String>, double[]> tempcolres=colres.get(keyname);
					while(itsc.hasNext())
					{
						Vector<String> tempttvb=itsc.next();
						veciseq=false;
						for (Enumeration<Vector<String>> enn=tempcolval.keys(); enn.hasMoreElements();)
						{
							Vector<String> ttva=enn.nextElement();
							Vector<String> ttvb=tempcolval.get(ttva);
							if (!veciseq)
							{
								if (tempttvb.size()==ttvb.size())
								{
									toteq=0;
									for (int i=0; i<ttva.size(); i++)
									{
										String testva=tempttvb.get(i);
										String testvb=ttvb.get(i);
										if (testva.equals(testvb))
											toteq++;
									}
									if (toteq==tempttvb.size())
									{
										tempdefcolres.add(new ResTabulate(ttva, tempcolres.get(ttva)));
										veciseq=true;
									}
								}
							}
						}
					}
				}
				defcolres.put(keyname, tempdefcolres);
			}
		}
		else
		{
			boolean veciseq=true;
			int toteq=0;
			for (Enumeration<Vector<Integer>> en=resval.keys(); en.hasMoreElements();)
			{
				LinkedList<ResTabulate> tempdefresult=new LinkedList<ResTabulate>();
				Vector<Integer> keyname=en.nextElement();
				Hashtable<Vector<String>, Vector<String>> tempresval=resval.get(keyname);
				TreeSet<Vector<String>> tsc=new TreeSet<Vector<String>>(new VectorStringComparatorWithMd());
				for (Enumeration<Vector<String>> enn=tempresval.keys(); enn.hasMoreElements();)
				{
					Vector<String> ttva=enn.nextElement();
					Vector<String> ttvb=tempresval.get(ttva);
					if (orderbyval)
						tsc.add(ttvb);
					else
						tsc.add(ttva);
				}
				if (!orderbyval)
				{
					Iterator<Vector<String>> itsc = tsc.iterator();
					Hashtable<Vector<String>, double[]> tempresult=result.get(keyname);
					while(itsc.hasNext())
					{
						Vector<String> tempttva=itsc.next();
						tempdefresult.add(new ResTabulate(tempttva, tempresult.get(tempttva)));
					}
				}
				else
				{
					Iterator<Vector<String>> itsc = tsc.iterator();
					Hashtable<Vector<String>, double[]> tempresult=result.get(keyname);
					while(itsc.hasNext())
					{
						Vector<String> tempttvb=itsc.next();
						veciseq=false;
						for (Enumeration<Vector<String>> enn=tempresval.keys(); enn.hasMoreElements();)
						{
							Vector<String> ttva=enn.nextElement();
							Vector<String> ttvb=tempresval.get(ttva);
							if (!veciseq)
							{
								if (tempttvb.size()==ttvb.size())
								{
									toteq=0;
									for (int i=0; i<ttva.size(); i++)
									{
										String testva=tempttvb.get(i);
										String testvb=ttvb.get(i);
										if (testva.equals(testvb))
											toteq++;
									}
									if (toteq==tempttvb.size())
									{
										tempdefresult.add(new ResTabulate(ttva, tempresult.get(ttva)));
										veciseq=true;
									}
								}
							}
						}
					}
				}
				defresult.put(keyname, tempdefresult);
			}
		}
	}
	/**
	*Used to set up the label of the different statistics
	*/
	public String getcodestat(String stat)
	{
		String[] acceptedstats={Keywords.simplecounts, Keywords.rowfreq, Keywords.rowpercentfreq,
		Keywords.colfreq, Keywords.colpercentfreq, Keywords.relfreq, Keywords.relpercentfreq,
		Keywords.MEAN, Keywords.SUM, Keywords.STD, Keywords.N};
		String[] codestats={"%1145%", "%1146%", "%1147%","%1148%", "%1149%","%1150%","%1151%",
		"%681%", "%687%", "%686%","%683%"};
		for (int i=0; i<acceptedstats.length; i++)
		{
			if (stat.equalsIgnoreCase(acceptedstats[i]))
				return codestats[i];
		}
		return stat;
	}
	/**
	*Return the result for the table when no row are selected
	*/
	public Hashtable<Integer, LinkedList<ResTabulate>> getcolres()
	{
		return defcolres;
	}
	/**
	*Return the result for the table when no columns are selected
	*/
	public Hashtable<Integer, LinkedList<ResTabulate>> getrowres()
	{
		return defrowres;
	}
	/**
	*Return the complete results
	*/
	public Hashtable<Vector<Integer>, LinkedList<ResTabulate>> getresult()
	{
		return defresult;
	}
	/**
	*Checks if in the received object composed by ResTabulate is the current Vector
	*/
	public double[] checkandget(Vector<Integer> stattouse, Vector<String> valuestosearch)
	{
		int toteq=0;
		LinkedList<ResTabulate> temptotresult=defresult.get(stattouse);
		Iterator<ResTabulate> igvrt = temptotresult.iterator();
		while(igvrt.hasNext())
		{
			ResTabulate temprestabulate=igvrt.next();
			Vector<String> tempgroup=temprestabulate.getVec();
			if (tempgroup.size()==valuestosearch.size())
			{
				toteq=0;
				for (int i=0; i<tempgroup.size(); i++)
				{
					String testva=tempgroup.get(i);
					String testvb=valuestosearch.get(i);
					if (testva.equals(testvb))
						toteq++;
				}
				if (toteq==tempgroup.size())
				{
					return temprestabulate.getFre();
				}
			}
		}
		return null;
	}
}
