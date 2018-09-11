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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;
import java.awt.Shape;
import java.awt.Font;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.gui.GraphViewer;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;

/**
 * This is the procedure that create a xy scatter chart for a variable
* @author marco.scarno@gmail.com
* @date 01/03/2018
 */
public class ProcScatter implements RunStep
{
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String[] requiredparameters = new String[] { Keywords.dict, Keywords.varx, Keywords.vary};
		String[] optionalparameters = new String[] { Keywords.imgwidth, Keywords.where, Keywords.imgheight, Keywords.varlabel, Keywords.labely,
				Keywords.labelx, Keywords.title, Keywords.outjpg, Keywords.varlabelcolor, Keywords.fontsize, Keywords.fontname, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DictionaryReader dict = (DictionaryReader) parameters.get(Keywords.dict);
		String rep =(String)parameters.get(Keywords.replace);
		if (rep==null)
			rep="";
		String varx = (String) parameters.get(Keywords.varx);
		String vary = (String) parameters.get(Keywords.vary);
		String tempvargroup = (String) parameters.get(Keywords.vargroup.toLowerCase());
		String varlabel = (String) parameters.get(Keywords.varlabel);
		String varlabelcolor = (String) parameters.get(Keywords.varlabelcolor);
		int eight = 400;
		int width = 400;
		String sminX = (String) parameters.get(Keywords.minX);
		String sminY = (String) parameters.get(Keywords.minY);
		String smaxX = (String) parameters.get(Keywords.maxX);
		String smaxY = (String) parameters.get(Keywords.maxY);
		double minX=Double.NaN;
		double minY=Double.NaN;
		double maxX=Double.NaN;
		double maxY=Double.NaN;
		String sWidth = (String) parameters.get(Keywords.imgwidth);
		String sEight = (String) parameters.get(Keywords.imgheight);
		String fosize = (String) parameters.get(Keywords.fontsize);
		String outjpg = (String) parameters.get(Keywords.outjpg);

		String fontname = (String) parameters.get(Keywords.fontname);

		if (outjpg !=null)
		{
			if (!outjpg.toLowerCase().endsWith(".jpg"))
				outjpg=outjpg+".jpg";
		}
		if (sEight != null)
		{
			try
			{
				eight = Integer.parseInt(sEight);
			}
			catch (Exception en)
			{
				return new Result("%947%<br>\n", false, null);
			}
		}
		int font_size=10;
		if (fosize != null)
		{
			try
			{
				font_size = Integer.parseInt(fosize);
			}
			catch (Exception en)
			{
				return new Result("%4284%<br>\n", false, null);
			}
		}
		if (font_size>32 || font_size<8) return new Result("%4284%<br>\n", false, null);
		if (sWidth != null)
		{
			try
			{
				width = Integer.parseInt(sWidth);
			}
			catch (Exception en)
			{
				return new Result("%946%<br>\n", false, null);
			}
		}
		if (sminX != null)
		{
			try
			{
				minX = Double.parseDouble(sminX);
			}
			catch (Exception en)
			{
				return new Result("%948%<br>\n", false, null);
			}
		}
		if (smaxX != null)
		{
			try
			{
				maxX = Double.parseDouble(smaxX);
			}
			catch (Exception en)
			{
				return new Result("%949%<br>\n", false, null);
			}
		}
		if (sminY != null)
		{
			try
			{
				minY = Double.parseDouble(sminY);
			}
			catch (Exception en)
			{
				return new Result("%2649%<br>\n", false, null);
			}
		}
		if (smaxY != null)
		{
			try
			{
				maxY = Double.parseDouble(smaxY);
			}
			catch (Exception en)
			{
				return new Result("%2650%<br>\n", false, null);
			}
		}
		if (varx.split(" ").length > 1 || vary.split(" ").length > 1 || (tempvargroup != null && tempvargroup.split(" ").length > 1))
		{
			return new Result("%793%<br>\n", false, null);
		}
		if (varlabelcolor != null && varlabelcolor.split(" ").length > 1)
		{
			return new Result("%2652%<br>\n", false, null);
		}
		if (varlabel != null && varlabel.split(" ").length > 1)
		{
			return new Result("%2651%<br>\n", false, null);
		}
		Vector<String> rvars=new Vector<String>();
		rvars.add(varx.trim());
		rvars.add(vary.trim());
		int reqvars=2;
		int posgv=-1;
		int posvc=-1;
		int posvl=-1;
		if (tempvargroup != null)
		{
			rvars.add(tempvargroup.trim());
			posgv=reqvars;
			reqvars++;
		}
		if (varlabel != null)
		{
			rvars.add(varlabel.trim());
			posvl=reqvars;
			reqvars++;
		}
		if (varlabelcolor != null)
		{
			rvars.add(varlabelcolor.trim());
			posvc=reqvars;
		}

		String title = (String) parameters.get(Keywords.title);
		String labelx = (String) parameters.get(Keywords.labelx);
		String labely = (String) parameters.get(Keywords.labely);

		DataReader data = new DataReader(dict);
		String[] reqvar = new String[rvars.size()];
		int[] reprule=new int[reqvar.length];
		for (int i = 0; i < rvars.size(); i++)
		{
			reqvar[i] = rvars.get(i);
			reprule[i]=0;
			if (rep.equalsIgnoreCase(Keywords.replaceall))
				reprule[i]=1;
			else if (rep.equalsIgnoreCase(Keywords.replaceformat))
				reprule[i]=2;
			else if (rep.equalsIgnoreCase(Keywords.replacemissing))
				reprule[i]=3;
		}
		if (!data.open(reqvar, reprule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		if (title==null)
			title=dict.getvarlabelfromname(varx)+"-"+dict.getvarlabelfromname(vary);

		HashMap<String, Point> map = new HashMap<String, Point>();
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				if(!values[0].equals("") && !values[1].equals(""))
				{
					double xv = 0, yv = 0;
					try
					{
						xv = Double.parseDouble(values[0]);
					}
					catch (NumberFormatException e)
					{
						data.close();
						return new Result("%944% ("+reqvar[0]+"="+values[0]+")<br>\n", false, null);
					}
					try
					{
						yv = Double.parseDouble(values[1]);
					}
					catch (NumberFormatException e)
					{
						data.close();
						return new Result("%944% ("+reqvar[1]+"="+values[1]+")<br>\n", false, null);
					}
					String key="";
					if (tempvargroup != null)
					{
						key=values[posgv];
					}
					Point p = map.get(key);
					if (p == null)
						p = new Point(key);
					try
					{
						p.addElement(xv, yv);
					}
					catch (SeriesException e) {}
					Color tempcolor=Color.black;
					String annot="";
					if (posvl != -1)
						annot=values[posvl];
					if (posvc!= -1)
					{
						String info = (values[posvc].toLowerCase()).trim();
						if (info.equals("red"))
							tempcolor=Color.RED;
						if (info.equals("blue"))
							tempcolor=Color.blue;
						if (info.equals("cyan"))
							tempcolor=Color.cyan;
						if (info.equals("gray"))
							tempcolor=Color.gray;
						if (info.equals("green"))
							tempcolor=Color.green;
						if (info.equals("magenta"))
							tempcolor=Color.magenta;
						if (info.equals("orange"))
							tempcolor=Color.orange;
						if (info.equals("pink"))
							tempcolor=Color.pink;
						if (info.equals("white"))
							tempcolor=Color.white;
						if (info.equals("yellow"))
							tempcolor=Color.yellow;
						if (info.indexOf(",")>0)
						{
							String[] code = info.split(",");
							if (code.length == 3)
							{
								int r, g, b;
								r = Integer.parseInt(code[0].trim());
								g = Integer.parseInt(code[1].trim());
								b = Integer.parseInt(code[2].trim());
								if (r > 255 || g > 255 || b > 255 || r < 0 || g < 0 || b < 0)
								{
									tempcolor=Color.black;
								}
								else
								{
									tempcolor=new Color(r, g, b);
								}
							}
						}
					}
					XYTextAnnotation ta = new XYTextAnnotation(annot, xv, yv);

					if (fontname!=null)
					{
						try
						{
							Font myFont = new Font(fontname, Font.PLAIN, font_size);
							ta.setFont(myFont);
						}
						catch (Exception efont)
						{
							Font myFont = new Font("Serif", Font.PLAIN, font_size);
							ta.setFont(myFont);
						}
					}
					else
					{
						Font myFont = new Font("Serif", Font.PLAIN, font_size);
						ta.setFont(myFont);
					}

					ta.setTextAnchor(TextAnchor.BOTTOM_LEFT);
					p.addAnotations(ta, tempcolor);
					map.put(key, p);
				}
			}
		}
		data.close();
		Vector<JFreeChart> results = new Vector<JFreeChart>();
		if (tempvargroup==null)
		{
			DefaultTableXYDataset dataset = new DefaultTableXYDataset();
			Point series=map.get("");
			dataset.addSeries(series.getSeries());
			JFreeChart chart = ChartFactory.createScatterPlot(title, labelx != null ? labelx : "", labely != null ? labely : "",dataset, PlotOrientation.VERTICAL,false,true,false);
			XYPlot plot = chart.getXYPlot();
			ValueMarker marker = new ValueMarker(0);
			plot.addDomainMarker(marker);
			plot.addRangeMarker(marker);
			Vector<XYTextAnnotation> annotations = series.getAnnotations();
			Vector<Color> color=series.getColor();
			XYItemRenderer renderer = plot.getRenderer();
			Shape cross = ShapeUtilities.createDiagonalCross(2, 1);
			for (int i=0; i<annotations.size(); i++)
			{
				Color tempc=color.get(i);
		        renderer.setBasePaint(tempc);
				renderer.setBaseShape(cross);
				XYTextAnnotation an=annotations.get(i);
				an.setPaint(tempc);
				renderer.setSeriesPaint(i, tempc);
				plot.addAnnotation(an);
			}
			NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
			NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis();
				if (sminX != null)
				{
					try
					{
						domainAxis.setLowerBound(minX);
					}
					catch (Exception e)
					{
						return new Result("%812% " + minX + "<br>\n", false, null);
					}
				}
				else
				{
					domainAxis.setLowerBound(-1*series.getlimx());
				}
				if (smaxX != null)
				{
					try
					{
						domainAxis.setUpperBound(maxX);
					}
					catch (Exception e)
					{
						return new Result("%812% " + maxX + "<br>\n", false, null);
					}
				}
				else
				{
					domainAxis.setUpperBound(series.getlimx());
				}
				if (sminY != null)
				{
					try
					{
						rangeAxis.setLowerBound(minY);
					}
					catch (Exception e)
					{
						return new Result("%812% " + minY + "<br>\n", false, null);
					}
				}
				else
				{
					rangeAxis.setLowerBound(-1*series.getlimy());
				}
				if (smaxY != null)
				{
					try
					{
						rangeAxis.setUpperBound(maxY);
					}
					catch (Exception e)
					{
						return new Result("%812% " + maxY + "<br>\n", false, null);
					}
				}
				else
				{
					rangeAxis.setUpperBound(series.getlimy());
				}
				chart.getPlot().setBackgroundPaint(Color.white);
			results.add(chart);
		}
		else
		{
			Iterator<String> iteratorm = map.keySet().iterator();
			while (iteratorm.hasNext())
			{
				String key = iteratorm.next();
      			DefaultTableXYDataset dataset = new DefaultTableXYDataset();
				Point series=map.get(key);
				dataset.addSeries(series.getSeries());
				String newtitle=title+" ("+dict.getvarlabelfromname(tempvargroup)+": "+key+")";
				JFreeChart chart = ChartFactory.createScatterPlot(newtitle, labelx != null ? labelx : "", labely != null ? labely : "",dataset, PlotOrientation.VERTICAL,false,true,false);
				XYPlot plot = chart.getXYPlot();
				ValueMarker marker = new ValueMarker(0);
				plot.addDomainMarker(marker);
				plot.addRangeMarker(marker);
				Vector<XYTextAnnotation> annotations = series.getAnnotations();
				Vector<Color> color=series.getColor();
				XYItemRenderer renderer = plot.getRenderer();
				Shape cross = ShapeUtilities.createDiagonalCross(2, 1);
				for (int i=0; i<annotations.size(); i++)
				{
					Color tempc=color.get(i);
					renderer.setSeriesPaint(i, tempc);
			        renderer.setBasePaint(tempc);
					renderer.setBaseShape(cross);
					XYTextAnnotation an=annotations.get(i);
					an.setPaint(tempc);
					renderer.setSeriesPaint(i, tempc);
					plot.addAnnotation(an);
				}
				NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
				NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis();
				if (sminX != null)
				{
					try
					{
						domainAxis.setLowerBound(minX);
					}
					catch (Exception e)
					{
						return new Result("%812% " + minX + "<br>\n", false, null);
					}
				}
				else
				{
					domainAxis.setLowerBound(-1*series.getlimx());
				}
				if (smaxX != null)
				{
					try
					{
						domainAxis.setUpperBound(maxX);
					}
					catch (Exception e)
					{
						return new Result("%812% " + maxX + "<br>\n", false, null);
					}
				}
				else
				{
					domainAxis.setUpperBound(series.getlimx());
				}
				if (sminY != null)
				{
					try
					{
						rangeAxis.setLowerBound(minY);
					}
					catch (Exception e)
					{
						return new Result("%812% " + minY + "<br>\n", false, null);
					}
				}
				else
				{
					rangeAxis.setLowerBound(-1*series.getlimy());
				}
				if (smaxY != null)
				{
					try
					{
						rangeAxis.setUpperBound(maxY);
					}
					catch (Exception e)
					{
						return new Result("%812% " + maxY + "<br>\n", false, null);
					}
				}
				else
				{
					rangeAxis.setUpperBound(series.getlimy());
				}
				chart.getPlot().setBackgroundPaint(Color.white);
				results.add(chart);
			}
		}
		Vector<StepResult> result = new Vector<StepResult>();
		String workdirg=(String)parameters.get(Keywords.WorkDir);
		Iterator<JFreeChart> it = results.iterator();
		int i = 0;
		while (it.hasNext())
		{
			if (outjpg == null)
			{
				new GraphViewer(it.next());
			}
			else
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try
				{
					ChartUtilities.saveChartAsJPEG(new File(workdirg+"temp.jpg"), it.next(), width,eight);
					FileInputStream fin = new FileInputStream(new File(workdirg+"temp.jpg"));
					byte buffer[] = new byte[4096];
					int read = 0;
					do
					{
						read = fin.read(buffer);
						if(read != -1)
						{
							baos.write(buffer, 0, read);
						}
					} while(read != -1);
					fin.close();
				}
				catch (IOException e)
				{
					return new Result("%794%_\n", false, null);
				}
				String path = outjpg;
				if (results.size() > 1)
				{
					if (outjpg.indexOf('.') != 0)
					{
						path = outjpg.replace(".", i++ + ".");
					}
					else
					{
						return new Result("%795%<br>\n", false, null);
					}
				}
				result.add(new LocalFileSave(path, baos.toByteArray()));
				(new File(workdirg+"temp.jpg")).delete();
			}
		}
		return new Result("", true, result);
	}

	/**
	 * Returns the parameters for the procedure
	 */
	public LinkedList<GetRequiredParameters> getparameters() {
		LinkedList<GetRequiredParameters> parameters = new LinkedList<GetRequiredParameters>();
		String[] dep = { "" };
		parameters.add(new GetRequiredParameters(Keywords.dict + "=", "dict", true, 721, dep, "", 1));
		dep = new String[1];
		dep[0] = Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varx, "var=all", true, 813, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vary, "var=all", true, 814, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varlabel, "var=all", false, 817, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varlabelcolor, "var=all", false, 818, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.imgwidth, "text", false, 798, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.imgheight, "text", false, 799, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.title, "text", false, 800, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.labelx, "text", false, 815, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.labely, "text", false, 816, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.maxX, "text", false, 819, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.minX, "text", false, 821, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.maxY, "text", false, 820, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.minY, "text", false, 822, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.fontsize, "text", false, 4283, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.fontname, "text", false, 4285, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.outjpg, "filesave=.jpeg", false, 802, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}

	/**
	 * Returns the group and the name of the procedure
	 */
	public String[] getstepinfo() {
		String[] retprocinfo = new String[2];
		retprocinfo[0] = "804";
		retprocinfo[1] = "823";
		return retprocinfo;
	}
}

class Point
{
	XYSeries s;
	Vector<XYTextAnnotation> annotations;
	Vector<Color> color;
	double limx=-1.7976931348623157E308;
	double limy=-1.7976931348623157E308;
	public Point(String label)
	{
		s = new XYSeries(label, true, false);
		annotations = new Vector<XYTextAnnotation>();
		color=new Vector<Color>();
	}
	public void addElement(double x, double y)
	{
		if (Math.abs(x)>limx)
			limx=Math.abs(x);
		if (Math.abs(y)>limy)
			limy=Math.abs(y);
		s.add(x,y);
	}
	public double getlimx()
	{
		return limx+limx*0.1;
	}
	public double getlimy()
	{
		return limy+limy*0.1;
	}
	public XYSeries getSeries()
	{
		return s;
	}
	public void addAnotations(XYTextAnnotation annotation, Color tempcolor)
	{
		annotations.add(annotation);
		color.add(tempcolor);
	}
	public Vector<XYTextAnnotation> getAnnotations()
	{
		return annotations;
	}
	public Vector<Color> getColor()
	{
		return color;
	}
}
