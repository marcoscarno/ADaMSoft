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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.utilities.SortHashtable;

/**
* This method evaluates the frequencies from the received arrays of strings
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class FrequenciesTabulator
{
	Hashtable<Vector<String>, FreqTable[][]> freqs;
	Hashtable<Vector<String>, MarginalFreqTable[][]> totrows;
	Hashtable<Vector<String>, MarginalFreqTable[][]> totcols;
	Hashtable<Vector<String>, double[][]> totals;
	Hashtable<Vector<String>, double[][]> table;
	VarList[] rowvarlist;
	VarList[] colvarlist;
	int trow=1;
	int tcol=1;
	boolean norow;
	boolean nocol;
	boolean	totonrows;
	boolean totoncols;
	int numrows;
	int numcols;
	int typetable;
	boolean onerow;
	boolean orderbyrow;
	boolean orderbycol;
	Vector<Hashtable<String, String>> rowdisv;
	Vector<Hashtable<String, String>> coldisv;
	boolean donecontainer=false;
	boolean noclrows;
	/**
	*Starts the method by receiving the info that are requested the total on rows, the total on columns
	* and the type of the frequencies table (absolute value, row frequencies, row percent frequencies, ..)
	*/
	public FrequenciesTabulator(boolean totonrows, boolean totoncols, int type)
	{
		noclrows=false;
		freqs=new Hashtable<Vector<String>, FreqTable[][]>();
		totrows=new Hashtable<Vector<String>, MarginalFreqTable[][]>();
		totcols=new Hashtable<Vector<String>, MarginalFreqTable[][]>();
		totals=new Hashtable<Vector<String>, double[][]>();
		this.totonrows=totonrows;
		this.totoncols=totoncols;
		this.typetable=type;
		orderbyrow=false;
		orderbycol=false;
		rowdisv=new Vector<Hashtable<String, String>>();
		coldisv=new Vector<Hashtable<String, String>>();
	}
	/**
	*If this method is called then it means that no code labels for the row variables are used
	*/
	public void setNoClRow()
	{
		noclrows=true;
	}
	/**
	*Receive the info that the variable row is present or the variable column
	*/
	public void setTableVars(boolean norow, boolean nocol, boolean onerow)
	{
		this.norow=norow;
		this.nocol=nocol;
		this.onerow=onerow;
	}
	/**
	*If called the values will be sorted according to the original values of the row variables
	*/
	public void orderbyrowcodes()
	{
		orderbyrow=true;
	}
	/**
	*If called the values will be sorted according to the original values of the column variables
	*/
	public void orderbycolcodes()
	{
		orderbycol=true;
	}
	/**
	*Estimates the frequencies
	*/
	public void evaluate(Vector<String> groupval, String[][] valrow, String[][] valcol, double f)
	{
		if (!donecontainer)
		{
			for (int i=0; i<valcol.length; i++)
			{
				Hashtable<String, String> tcoldisv=new Hashtable<String, String>();
				coldisv.add(tcoldisv);
			}
			if (!norow)
				trow=valrow.length;
			else
				trow=1;
			if (!nocol)
				tcol=valcol.length;
			else
				tcol=1;
			FreqTable[][] freq=new FreqTable[trow][tcol];
			MarginalFreqTable[][] totrow=new MarginalFreqTable[trow][tcol];
			MarginalFreqTable[][] totcol=new MarginalFreqTable[trow][tcol];
			double[][] total=new double[trow][tcol];
			for (int i=0; i<trow; i++)
			{
				Hashtable<String, String> trowdisv=new Hashtable<String, String>();
				rowdisv.add(trowdisv);
				for (int j=0; j<tcol; j++)
				{
					FreqTable tempf=new FreqTable();
					MarginalFreqTable temptr=new MarginalFreqTable();
					MarginalFreqTable temptc=new MarginalFreqTable();
					freq[i][j]=tempf;
					totrow[i][j]=temptr;
					totcol[i][j]=temptc;
					total[i][j]=0;
				}
			}
			donecontainer=true;
			freqs.put(groupval, freq);
			totrows.put(groupval, totrow);
			totcols.put(groupval, totcol);
			totals.put(groupval, total);
		}
		FreqTable[][] freq=freqs.get(groupval);
		MarginalFreqTable[][] totrow=totrows.get(groupval);
		MarginalFreqTable[][] totcol=totcols.get(groupval);
		double[][] total=totals.get(groupval);
		if (freq==null)
		{
			freq=new FreqTable[trow][tcol];
			totrow=new MarginalFreqTable[trow][tcol];
			totcol=new MarginalFreqTable[trow][tcol];
			total=new double[trow][tcol];
			for (int i=0; i<trow; i++)
			{
				for (int j=0; j<tcol; j++)
				{
					FreqTable tempf=new FreqTable();
					MarginalFreqTable temptr=new MarginalFreqTable();
					MarginalFreqTable temptc=new MarginalFreqTable();
					freq[i][j]=tempf;
					totrow[i][j]=temptr;
					totcol[i][j]=temptc;
					total[i][j]=0;
				}
			}
		}
		for (int i=0; i<valcol.length; i++)
		{
			if (!valcol[i][1].equals(""))
			{
				Hashtable<String, String> tcoldisv=coldisv.get(i);
				if (tcoldisv.get(valcol[i][1])==null)
					tcoldisv.put(valcol[i][1], valcol[i][0]);
				else
				{
					String tdcv=tcoldisv.get(valcol[i][1]);
					try
					{
						double tndcvn=Double.parseDouble(valcol[i][0]);
						double tndcvp=Double.parseDouble(tdcv);
						if (tndcvn>tndcvp)
							tcoldisv.put(valcol[i][1], valcol[i][0]);
					}
					catch (Exception edcv)
					{
						if (valcol[i][0].compareTo(tdcv)>0)
							tcoldisv.put(valcol[i][1], valcol[i][0]);
					}
				}
				coldisv.set(i, tcoldisv);
			}

		}
		double tf=0;
		boolean notreat=false;
		for (int i=0; i<trow; i++)
		{
			if (!valrow[i][1].equals(""))
			{
				Hashtable<String, String> trowdisv=rowdisv.get(i);
				if (trowdisv.get(valrow[i][1])==null)
					trowdisv.put(valrow[i][1], valrow[i][0]);
				else
				{
					String tdrv=trowdisv.get(valrow[i][1]);
					try
					{
						double tndrvn=Double.parseDouble(valrow[i][0]);
						double tndrvp=Double.parseDouble(tdrv);
						if (tndrvn>tndrvp)
							trowdisv.put(valrow[i][1], valrow[i][0]);
					}
					catch (Exception edrv)
					{
						if (valrow[i][0].compareTo(tdrv)>0)
							trowdisv.put(valrow[i][1], valrow[i][0]);
					}
				}
				rowdisv.set(i, trowdisv);
			}
			for (int j=0; j<tcol; j++)
			{
				notreat=false;
				if ((!norow) && (valrow[i][1].equals("")))
					notreat=true;
				if ((!nocol) && (valcol[j][1].equals("")))
					notreat=true;
				if (!notreat)
				{
					FreqTable tempf=freq[i][j];
					MarginalFreqTable temptr=totrow[i][j];
					MarginalFreqTable temptc=totcol[i][j];
					Vector<String> tv=new Vector<String>();
					if (!norow)
					{
						tv.add(valrow[i][1]);
						if (temptr.get(valrow[i][1])==null)
							temptr.put(valrow[i][1], f);
						else
						{
							tf=(temptr.get(valrow[i][1])).doubleValue();
							temptr.put(valrow[i][1], f+tf);
						}
						if ((nocol) && (totoncols))
						{
							if (temptc.get("")==null)
								temptc.put("", f);
							else
							{
								tf=(temptc.get("")).doubleValue();
								temptc.put("", f+tf);
							}
						}
					}
					if (!nocol)
					{
						tv.add(valcol[j][1]);
						if (temptc.get(valcol[j][1])==null)
							temptc.put(valcol[j][1], f);
						else
						{
							tf=(temptc.get(valcol[j][1])).doubleValue();
							temptc.put(valcol[j][1], f+tf);
						}
						if ((norow) && (totonrows))
						{
							if (temptr.get("")==null)
								temptr.put("", f);
							else
							{
								tf=(temptr.get("")).doubleValue();
								temptr.put("", f+tf);
							}
						}
					}
					if (tempf.get(tv)==null)
						tempf.put(tv, f);
					else
					{
						tf=(tempf.get(tv)).doubleValue();
						tempf.put(tv, f+tf);
					}
					tf=total[i][j]+f;
					freq[i][j]=tempf;
					total[i][j]=tf;
					totrow[i][j]=temptr;
					totcol[i][j]=temptc;
				}
			}
		}
		freqs.put(groupval, freq);
		totrows.put(groupval,totrow);
		totcols.put(groupval,totcol);
		totals.put(groupval,total);
	}
	/**
	*Used to consolidate the frequencies table
	*/
	public void calculate()
	{
		for (int i=0; i<rowdisv.size(); i++)
		{
			Hashtable<String, String> trowdisv=rowdisv.get(i);
			int numd=0;
			int numm=0;
			double pv=Double.NaN;
			double maxv=-1.7976931348623157E308;
			String maxs="";
			for (Enumeration<String> enr = trowdisv.keys() ; enr.hasMoreElements() ;)
			{
				String tkvrowdisv=enr.nextElement();
				String tvvrowdisv=trowdisv.get(tkvrowdisv);
				if (!tvvrowdisv.equals(""))
				{
					try
					{
						pv=Double.parseDouble(tvvrowdisv);
						if (!Double.isNaN(pv))
						{
							if (!Double.isInfinite(pv))
							{
								numd++;
								if (pv>maxv)
									maxv=pv;
							}
						}
					}
					catch (Exception e)
					{
						if (maxs.equals(""))
							maxs=tvvrowdisv;
						else
						{
							if (tvvrowdisv.compareTo(maxs)>0)
								maxs=tvvrowdisv;
						}
					}
				}
				else
				{
					numm++;
				}
			}
			if (numm>0)
			{
				if (numd==(trowdisv.size()-numm))
				{
					for (Enumeration<String> enr = trowdisv.keys() ; enr.hasMoreElements() ;)
					{
						String tkvrowdisv=enr.nextElement();
						String tvvrowdisv=trowdisv.get(tkvrowdisv);
						if (tvvrowdisv.equals(""))
						{
							maxv++;
							int tmaxv=(int)maxv;
							trowdisv.put(tkvrowdisv, String.valueOf(tmaxv));
						}
					}
					rowdisv.set(i, trowdisv);
				}
				else
				{
					int nm=0;
					for (Enumeration<String> enr = trowdisv.keys() ; enr.hasMoreElements() ;)
					{
						String tkvrowdisv=enr.nextElement();
						String tvvrowdisv=trowdisv.get(tkvrowdisv);
						if (tvvrowdisv.equals(""))
						{
							nm++;
							maxs=maxs+"_m_"+String.valueOf(nm);
							trowdisv.put(tkvrowdisv, maxs);
						}
					}
					rowdisv.set(i, trowdisv);
				}
			}
		}

		for (int i=0; i<coldisv.size(); i++)
		{
			Hashtable<String, String> tcoldisv=coldisv.get(i);
			int numd=0;
			int numm=0;
			double pv=Double.NaN;
			double maxv=-1.7976931348623157E308;
			String maxs="";
			for (Enumeration<String> enr = tcoldisv.keys() ; enr.hasMoreElements() ;)
			{
				String tkvcoldisv=enr.nextElement();
				String tvvcoldisv=tcoldisv.get(tkvcoldisv);
				if (!tvvcoldisv.equals(""))
				{
					try
					{
						pv=Double.parseDouble(tvvcoldisv);
						if (!Double.isNaN(pv))
						{
							if (!Double.isInfinite(pv))
							{
								numd++;
								if (pv>maxv)
									maxv=pv;
							}
						}
					}
					catch (Exception e)
					{
						if (maxs.equals(""))
							maxs=tvvcoldisv;
						else
						{
							if (tvvcoldisv.compareTo(maxs)>0)
								maxs=tvvcoldisv;
						}
					}
				}
				else
				{
					numm++;
				}
			}
			if (numm>0)
			{
				if (numd==(tcoldisv.size()-numm))
				{
					for (Enumeration<String> enr = tcoldisv.keys() ; enr.hasMoreElements() ;)
					{
						String tkvcoldisv=enr.nextElement();
						String tvvcoldisv=tcoldisv.get(tkvcoldisv);
						if (tvvcoldisv.equals(""))
						{
							maxv++;
							int tmaxv=(int)maxv;
							tcoldisv.put(tkvcoldisv, String.valueOf(tmaxv));
						}
					}
					coldisv.set(i, tcoldisv);
				}
				else
				{
					int nm=0;
					for (Enumeration<String> enr = tcoldisv.keys() ; enr.hasMoreElements() ;)
					{
						String tkvcoldisv=enr.nextElement();
						String tvvcoldisv=tcoldisv.get(tkvcoldisv);
						if (tvvcoldisv.equals(""))
						{
							nm++;
							maxs=maxs+"_m_"+String.valueOf(nm);
							tcoldisv.put(tkvcoldisv, maxs);
						}
					}
					coldisv.set(i, tcoldisv);
				}
			}
		}
		rowvarlist=new VarList[trow];
		colvarlist=new VarList[tcol];
		for (int i=0; i<trow; i++)
			rowvarlist[i]=new VarList();
		for (int i=0; i<tcol; i++)
			colvarlist[i]=new VarList();
		numrows=0;
		numcols=0;
		if (totonrows)
			numcols=tcol;
		if (totoncols)
			numrows=trow;
		if(!norow)
		{
			for (int i=0; i<trow; i++)
			{
				SortHashtable shr=new SortHashtable(rowdisv.get(i));
				if (!orderbyrow)
					shr.setsortindex(0);
				shr.executesort();
				rowvarlist[i].setorder(shr.getsortedhashtable());
				numrows=numrows+rowvarlist[i].size();
			}
		}
		else
			numrows++;
		if(!nocol)
		{
			for (int i=0; i<tcol; i++)
			{
				SortHashtable shc=new SortHashtable(coldisv.get(i));
				if (!orderbycol)
					shc.setsortindex(0);
				shc.executesort();
				colvarlist[i].setorder(shc.getsortedhashtable());
				numcols=numcols+colvarlist[i].size();
			}
		}
		else
			numcols++;
		table=new Hashtable<Vector<String>, double[][]>();
		double den=0;
		double f=0;
		for (Enumeration<Vector<String>> e = freqs.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv=e.nextElement();
			FreqTable[][] freq=freqs.get(gv);
			MarginalFreqTable[][] temptr=totrows.get(gv);
			MarginalFreqTable[][] temptc=totcols.get(gv);
			double[][] TOF=new double[numrows][numcols];
			int x=0;
			int adderx=0;
			int addery=0;
			double[][] ttotals=totals.get(gv);
			for (int i=0; i<trow; i++)
			{
				if ((!norow) && (i>0))
				{
					VarList tvr=rowvarlist[i-1];
					x+=tvr.size();
					if (totoncols)
						x++;
				}
				int y=0;
				for (int j=0; j<tcol; j++)
				{
					if ((!nocol) && (j>0))
					{
						VarList tvc=colvarlist[j-1];
						y+=tvc.size();
						if (totonrows)
							y++;
					}
					Vector<String> allnr=new Vector<String>();
					Vector<String> allnc=new Vector<String>();
					if (!norow)
					{
						VarList tvr=rowvarlist[i];
						allnr=tvr.getallnames();
					}
					else
						allnr.add("");
					if (!nocol)
					{
						VarList tvc=colvarlist[j];
						allnc=tvc.getallnames();
					}
					else
						allnc.add("");
					Vector<String> tempnv=new Vector<String>();
					MarginalFreqTable temptcol=temptc[i][j];
					MarginalFreqTable temptrow=temptr[i][j];
					for (int k=0; k<allnr.size(); k++)
					{
						for (int h=0; h<allnc.size(); h++)
						{
							tempnv.clear();
							if (!allnr.get(k).equals(""))
								tempnv.add(allnr.get(k));
							if (!allnc.get(h).equals(""))
								tempnv.add(allnc.get(h));
							f=0;
							if (freq[i][j].get(tempnv)!=null)
								f=(freq[i][j].get(tempnv)).doubleValue();
							if (!norow)
							{
								VarList tvr=rowvarlist[i];
								adderx=tvr.getpos(allnr.get(k));
							}
							if (!nocol)
							{
								VarList tvc=colvarlist[j];
								addery=tvc.getpos(allnc.get(h));
							}
							if (typetable==2)
							{
								if (!norow)
								{
									if (temptrow.get(allnr.get(k))!=null)
										den=(temptrow.get(allnr.get(k))).doubleValue();
									else
										den=0;
								}
								else
								{
									if (temptrow.get("")!=null)
										den=(temptrow.get("")).doubleValue();
									else
										den=0;
								}
								f=f/den;
							}
							else if (typetable==3)
							{
								if (!norow)
								{
									if (temptrow.get(allnr.get(k))!=null)
										den=(temptrow.get(allnr.get(k))).doubleValue();
									else
										den=0;
								}
								else
								{
									if (temptrow.get("")!=null)
										den=(temptrow.get("")).doubleValue();
									else
										den=0;
								}
								f=100*f/den;
							}
							else if (typetable==4)
							{
								if (!nocol)
								{
									if (temptcol.get(allnc.get(h))!=null)
										den=(temptcol.get(allnc.get(h))).doubleValue();
									else
										den=0;
								}
								else
								{
									if (temptcol.get("")!=null)
										den=(temptcol.get("")).doubleValue();
									else
										den=0;
								}
								f=f/den;
							}
							else if (typetable==5)
							{
								if (!nocol)
								{
									if (temptcol.get(allnc.get(h))!=null)
										den=(temptcol.get(allnc.get(h))).doubleValue();
									else
										den=0;
								}
								else
								{
									if (temptcol.get("")!=null)
										den=(temptcol.get("")).doubleValue();
									else
										den=0;
								}
								f=100*f/den;
							}
							else if (typetable==6)
							{
								den=ttotals[i][j];
								f=f/den;
							}
							else if (typetable==7)
							{
								den=ttotals[i][j];
								f=100*f/den;
							}
							if (Double.isNaN(f))
								f=0;
							else if (Double.isInfinite(f))
								f=0;
							TOF[x+adderx][y+addery]=f;
						}
						if (totonrows)
						{
							if (!norow)
							{
								VarList tvr=rowvarlist[i];
								den=0;
								for (int h=0; h<allnr.size(); h++)
								{
									if (temptrow.get(allnr.get(h))==null)
										f=0;
									else
										f=(temptrow.get(allnr.get(h))).doubleValue();
									den=den+f;
								}
								for (int h=0; h<allnr.size(); h++)
								{
									if (temptrow.get(allnr.get(h))==null)
										TOF[x+tvr.getpos(allnr.get(h))][y+allnc.size()]=0;
									else
									{
										f=(temptrow.get(allnr.get(h))).doubleValue();
										if (typetable==2)
										{
											f=1;
										}
										else if (typetable==3)
										{
											f=100;
										}
										else if (typetable==4)
										{
											f=f/den;
										}
										else if (typetable==5)
										{
											f=100*f/den;
										}
										else if (typetable==6)
										{
											den=ttotals[i][j];
											f=f/den;
										}
										else if (typetable==7)
										{
											den=ttotals[i][j];
											f=100*f/den;
										}
										if (Double.isNaN(f))
											f=0;
										else if (Double.isInfinite(f))
											f=0;
										TOF[x+tvr.getpos(allnr.get(h))][y+allnc.size()]=f;
									}
								}
							}
							else
							{
								if (temptrow.get("")==null)
									TOF[x][y+allnc.size()]=0;
								else
								{
									f=(temptrow.get("")).doubleValue();
									if (typetable==2)
									{
										f=1;
									}
									else if (typetable==3)
									{
										f=100;
									}
									else if (typetable==4)
									{
										den=ttotals[i][j];
										f=f/den;
									}
									else if (typetable==5)
									{
										den=ttotals[i][j];
										f=100*f/den;
									}
									else if (typetable==6)
									{
										den=ttotals[i][j];
										f=f/den;
									}
									else if (typetable==7)
									{
										den=ttotals[i][j];
										f=100*f/den;
									}
									if (Double.isNaN(f))
										f=0;
									else if (Double.isInfinite(f))
										f=0;
									TOF[x][y+allnc.size()]=f;
								}
							}
						}
					}
					if (totoncols)
					{
						if (!nocol)
						{
							VarList tvc=colvarlist[j];
							den=0;
							for (int h=0; h<allnc.size(); h++)
							{
								if (temptcol.get(allnc.get(h))==null)
									f=0;
								else
									f=(temptcol.get(allnc.get(h))).doubleValue();
								den=den+f;
							}
							for (int h=0; h<allnc.size(); h++)
							{
								if (temptcol.get(allnc.get(h))==null)
									TOF[x+allnr.size()][y+tvc.getpos(allnc.get(h))]=0;
								else
								{
									f=(temptcol.get(allnc.get(h))).doubleValue();
									if (typetable==2)
									{
										f=f/den;
									}
									else if (typetable==3)
									{
										f=100*f/den;
									}
									else if (typetable==4)
									{
										f=1;
									}
									else if (typetable==5)
									{
										f=100;
									}
									else if (typetable==6)
									{
										den=ttotals[i][j];
										f=f/den;
									}
									else if (typetable==7)
									{
										den=ttotals[i][j];
										f=100*f/den;
									}
									if (Double.isNaN(f))
										f=0;
									else if (Double.isInfinite(f))
										f=0;
									TOF[x+allnr.size()][y+tvc.getpos(allnc.get(h))]=f;
								}
							}
						}
						else
						{
							if (temptcol.get("")==null)
								TOF[x+allnr.size()][y]=0;
							else
							{
								f=(temptcol.get("")).doubleValue();
								if (typetable==2)
								{
									den=ttotals[i][j];
									f=f/den;
								}
								else if (typetable==3)
								{
									den=ttotals[i][j];
									f=100*f/den;
								}
								else if (typetable==4)
								{
									f=1;
								}
								else if (typetable==5)
								{
									f=100;
								}
								else if (typetable==6)
								{
									den=ttotals[i][j];
									f=f/den;
								}
								else if (typetable==7)
								{
									den=ttotals[i][j];
									f=100*f/den;
								}
								if (Double.isNaN(f))
									f=0;
								else if (Double.isInfinite(f))
									f=0;
								TOF[x+allnr.size()][y]=f;
							}
						}
						if (totonrows)
						{
							f=ttotals[i][j];
							if (typetable==2)
							{
								f=1;
							}
							else if (typetable==3)
							{
								f=100;
							}
							else if (typetable==4)
							{
								f=1;
							}
							else if (typetable==5)
							{
								f=100;
							}
							else if (typetable==6)
							{
								f=1;
							}
							else if (typetable==7)
							{
								f=100;
							}
							if (Double.isNaN(f))
								f=0;
							else if (Double.isInfinite(f))
								f=0;
							TOF[x+allnr.size()][y+allnc.size()]=f;
						}
					}
				}
			}
			table.put(gv, TOF);
		}
	}
	/**
	*Gives back the total number of rows in the frequencies table
	*/
	public int getnumrows()
	{
		return numrows;
	}
	/**
	*Gives back the total number of colums (variables) in the frequencies table
	*/
	public int getnumcols()
	{
		return numcols;
	}
	/**
	*Gives back the labels of the variables that will be created in the resulting table
	*/
	public String[][] getcolnames(String[] varcol, String[] varcollabels)
	{
		String[][] colnames=new String[numcols][2];
		if ((nocol) && (!totonrows))
		{
			colnames[0][0]="RowTotal";
			colnames[0][1]="%1620%";
			return colnames;
		}
		else if ((nocol) && (totonrows))
		{
			colnames[0][0]="RowTotal";
			colnames[0][1]="%1620%";
			colnames[1][0]="Total";
			colnames[1][1]="%1013%";
			return colnames;
		}
		else
		{
			int baseval=0;
			for (int i=0; i<tcol; i++)
			{
				Hashtable<String, String> tcoldisv=coldisv.get(i);
				VarList tvc=colvarlist[i];
				Vector<String> allnc=tvc.getallnames();
				for (int j=0; j<allnc.size(); j++)
				{
					String gv=allnc.get(j);
					String rif=tcoldisv.get(gv);
					colnames[baseval][0]=varcol[i]+"_"+rif;
					colnames[baseval][1]=varcollabels[i]+": "+gv;
					baseval++;
				}
				if (totonrows)
				{
					colnames[baseval][0]="Total_"+varcol[i];
					colnames[baseval][1]="%1013%: "+varcollabels[i];
					baseval++;
				}
			}
		}
		return colnames;
	}
	/**
	*Gives back the code label of the resulting table
	*/
	public String[][] getrownames(String[] varrow, String[] varrowlabels)
	{
		String[][] rownames=new String[numrows][2];
		if ((norow) && (!totoncols))
		{
			rownames[0][0]="ColTotal";
			if (!noclrows)
				rownames[0][1]="%1621%";
			else
				rownames[0][1]="ColTotal";
			return rownames;
		}
		else if ((norow) && (totoncols))
		{
			rownames[0][0]="ColTotal";
			if (!noclrows)
				rownames[0][1]="%1621%";
			else
				rownames[0][1]="ColTotal";
			rownames[1][0]="Total";
			if (!noclrows)
				rownames[1][1]="%1013%";
			else
				rownames[1][1]="Total";
			return rownames;
		}
		else
		{
			int baseval=0;
			for (int i=0; i<trow; i++)
			{
				VarList tvr=rowvarlist[i];
				Vector<String> allnr=tvr.getallnames();
				Hashtable<String, String> trowdisv=rowdisv.get(i);
				for (int j=0; j<allnr.size(); j++)
				{
					String gv=allnr.get(j);
					String rif=trowdisv.get(gv);
					if (!onerow)
					{
						rownames[baseval][0]=varrow[i]+"_"+rif;
						rownames[baseval][1]=varrowlabels[i]+": "+gv;
					}
					else
					{
						rownames[baseval][0]=rif;
						rownames[baseval][1]=gv;
					}
					baseval++;
				}
				if (totoncols)
				{
					rownames[baseval][0]="Total_"+varrow[i];
					if (!noclrows)
						rownames[baseval][1]="%1013%: "+varrowlabels[i];
					else
						rownames[baseval][1]="Total: "+varrowlabels[i];
					baseval++;
				}
			}
		}
		return rownames;
	}
	/**
	*This is the frequencies table
	*/
	public Hashtable<Vector<String>, double[][]> gettable()
	{
		return table;
	}
}
/**
*This class contains the row, the column and the frequencies
*/
class FreqTable
{
	Hashtable<Vector<String>, Double> ft;
	public FreqTable()
	{
		ft=new Hashtable<Vector<String>, Double>();
	}
	public Double get(Vector<String> tv)
	{
		return ft.get(tv);
	}
	public void put(Vector<String> tv, double t)
	{
		ft.put(tv, new Double(t));
	}
}
/**
*This class contains the marginal frequencies
*/
class MarginalFreqTable
{
	Hashtable<String, Double> fmt;
	public MarginalFreqTable()
	{
		fmt=new Hashtable<String, Double>();
	}
	public Double get(String tv)
	{
		return fmt.get(tv);
	}
	public void put(String tv, double t)
	{
		fmt.put(tv, new Double(t));
	}
	public Vector<String> getallkeys()
	{
		Vector<String> allkeys=new Vector<String>();
		for (Enumeration<String> e = fmt.keys() ; e.hasMoreElements() ;)
		{
			allkeys.add(e.nextElement());
		}
		return allkeys;
	}
}
/**
*This class contains the different variables
*/
class VarList
{
	Hashtable<String, Integer> order;
	Vector<String> allnames;
	public VarList()
	{
		order=new Hashtable<String, Integer>();
		allnames=new Vector<String>();
	}
	public void setorder(Hashtable<String, Integer> order)
	{
		this.order=order;
		for (int i=0; i<order.size(); i++)
		{
			allnames.add("");
		}
		for (Enumeration<String> e = order.keys() ; e.hasMoreElements() ;)
		{
			String vn=e.nextElement();
			int pos=(order.get(vn)).intValue();
			allnames.set(pos, vn);
		}
	}
	public int size()
	{
		return(order.size());
	}
	public Vector<String> getallnames()
	{
		return allnames;
	}
	public int getpos(String val)
	{
		return (order.get(val)).intValue();
	}
}
