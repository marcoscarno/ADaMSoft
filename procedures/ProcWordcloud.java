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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;
import java.util.TreeMap;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.kennycason.kumo.*;
import com.kennycason.kumo.bg.*;
import com.kennycason.kumo.font.*;
import com.kennycason.kumo.font.scale.*;
import com.kennycason.kumo.palette.*;

import java.lang.reflect.Field;

import java.awt.*;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.VectorStringComparator;
import ADaMSoft.utilities.StringComparator;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that plots a word cloud
* @author marco.scarno@gmail.com
* @date 15/02/2018
*/
public class ProcWordcloud implements RunStep
{
	/**
	* Starts the execution of Proc Word Cloud
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] { Keywords.weight, Keywords.replace, Keywords.vargroup,
													Keywords.imgwidth,
													Keywords.imgheight,
													Keywords.outpng,
													Keywords.padding,
													Keywords.background_png,
													Keywords.background_color,
													Keywords.colorpalettefirst,
													Keywords.colorpalettelast,
													Keywords.colorpalettestep,
													Keywords.fontscalar_min,
													Keywords.fontscalar_max,
													Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String tempvarg=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String tempweight=(String)parameters.get(Keywords.weight.toLowerCase());
		int eight=400;
		int width=400;
		String sWidth = (String)parameters.get(Keywords.imgwidth);
		String sEight = (String)parameters.get(Keywords.imgheight);
		String outpng = (String)parameters.get(Keywords.outpng);
		if (outpng !=null)
		{
			if (!outpng.toLowerCase().endsWith(".png"))
				outpng=outpng+".png";
		}
		if(sEight!=null)
		{
			try
			{
				eight = Integer.parseInt(sEight);
			}
			catch (Exception e)
			{
				return new Result("%4252%<br>\n", false, null);
			}
			if (eight<50) return new Result("%4252%<br>\n", false, null);
		}
		if(sWidth!=null)
		{
			try
			{
				width = Integer.parseInt(sWidth);
			}
			catch (Exception e)
			{
				return new Result("%4253%<br>\n", false, null);
			}
			if (width<50) return new Result("%4253%<br>\n", false, null);
		}
		int padding=1;
		String spadding= (String)parameters.get(Keywords.padding);
		if (spadding!=null)
		{
			try
			{
				padding = Integer.parseInt(spadding);
			}
			catch (Exception e)
			{
				return new Result("%4254%<br>\n", false, null);
			}
			if (padding<1) return new Result("%4254%<br>\n", false, null);
		}
		String background_png= (String)parameters.get(Keywords.background_png);
		if (background_png!=null)
		{
			boolean existb=(new File(background_png)).exists();
			if (!existb) return new Result("%4255%<br>\n", false, null);
		}
		Color colorback=Color.WHITE;
		String background_color= (String)parameters.get(Keywords.background_color);
		int integer_colorback=-1;
		if (background_color!=null)
		{
			try
			{
				if (background_color.indexOf(",")>0)
				{
					String[] pc=background_color.split(",");
					int R=Integer.valueOf(pc[0].trim());
					int G=Integer.valueOf(pc[1].trim());
					int B=Integer.valueOf(pc[2].trim());
					colorback = new Color(R, G, B);
					String hex = Integer.toHexString(colorback.getRGB() & 0xffffff);
					if (hex.length() < 6)  hex = "0" + hex;
					integer_colorback=Integer.parseInt(hex, 16);
				}
				else
				{
					Field field = Class.forName("java.awt.Color").getField(background_color);
					colorback = (Color)field.get(null);
					String hex = Integer.toHexString(colorback.getRGB() & 0xffffff);
					if (hex.length() < 6)  hex = "0" + hex;
					integer_colorback=Integer.parseInt(hex, 16);
				}
			}
			catch (Exception e)
			{
				return new Result("%4256%<br>\n", false, null);
			}
		}
		int pale=0;
		Color colorpfirst=Color.ORANGE;
		String colorpalettefirst= (String)parameters.get(Keywords.colorpalettefirst);
		int integer_colorpfirst=-1;
		if (colorpalettefirst!=null)
		{
			try
			{
				if (colorpalettefirst.indexOf(",")>0)
				{
					String[] pc=colorpalettefirst.split(",");
					int R=Integer.valueOf(pc[0].trim());
					int G=Integer.valueOf(pc[1].trim());
					int B=Integer.valueOf(pc[2].trim());
					colorpfirst = new Color(R, G, B);
					String hex = Integer.toHexString(colorpfirst.getRGB() & 0xffffff);
					if (hex.length() < 6)  hex = "0" + hex;
					integer_colorpfirst=Integer.parseInt(hex, 16);
					pale++;
				}
				else
				{
					Field field = Class.forName("java.awt.Color").getField(colorpalettefirst);
					colorpfirst = (Color)field.get(null);
					String hex = Integer.toHexString(colorpfirst.getRGB() & 0xffffff);
					if (hex.length() < 6)  hex = "0" + hex;
					integer_colorpfirst=Integer.parseInt(hex, 16);
;					pale++;
				}
			}
			catch (Exception e)
			{
				return new Result("%4258%<br>\n", false, null);
			}
		}
		Color colorplast=Color.RED;
		String colorpalettelast= (String)parameters.get(Keywords.colorpalettelast);
		int integer_colorplast=-1;
		if (colorpalettelast!=null)
		{
			try
			{
				if (colorpalettelast.indexOf(",")>0)
				{
					String[] pc=colorpalettelast.split(",");
					int R=Integer.valueOf(pc[0].trim());
					int G=Integer.valueOf(pc[1].trim());
					int B=Integer.valueOf(pc[2].trim());
					colorplast = new Color(R, G, B);
					String hex = Integer.toHexString(colorplast.getRGB() & 0xffffff);
					if (hex.length() < 6)  hex = "0" + hex;
					integer_colorplast=Integer.parseInt(hex, 16);
					pale++;
				}
				else
				{
					Field field = Class.forName("java.awt.Color").getField(colorpalettelast);
					colorplast = (Color)field.get(null);
					String hex = Integer.toHexString(colorplast.getRGB() & 0xffffff);
					if (hex.length() < 6)  hex = "0" + hex;
					integer_colorplast=Integer.parseInt(hex, 16);
					pale++;
				}
			}
			catch (Exception e)
			{
				return new Result("%4259%<br>\n", false, null);
			}
		}
		String colorpalettestep= (String)parameters.get(Keywords.colorpalettestep);
		int scps=1;
		if(colorpalettestep!=null)
		{
			try
			{
				scps = Integer.parseInt(colorpalettestep);
				pale++;
			}
			catch (Exception e)
			{
				return new Result("%4260%<br>\n", false, null);
			}
		}
		if (pale>0 && pale!=3) return new Result("%4261%<br>\n", false, null);
		int fsp=0;
		String fontscalar_min= (String)parameters.get(Keywords.fontscalar_min);
		int fsmin=10;
		if(fontscalar_min!=null)
		{
			try
			{
				fsmin = Integer.parseInt(fontscalar_min);
				fsp++;
			}
			catch (Exception e)
			{
				return new Result("%4262%<br>\n", false, null);
			}
		}
		String fontscalar_max= (String)parameters.get(Keywords.fontscalar_max);
		int fsmax=40;
		if(fontscalar_max!=null)
		{
			try
			{
				fsmax = Integer.parseInt(fontscalar_max);
				fsp++;
			}
			catch (Exception e)
			{
				return new Result("%4263%<br>\n", false, null);
			}
		}
		if (fsp>0 && fsp!=2) return new Result("%4264%<br>\n", false, null);
		String replace =(String)parameters.get(Keywords.replace);
		VariableUtilities varu=new VariableUtilities(dict, tempvarg, null, tempweight, tempvar, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] var=varu.getrowvar();
		if (var.length>1)
			return new Result("%4251%<br>\n", false, null);
		int[] refdvar=new int[var.length];
		for (int i=0; i<refdvar.length; i++)
		{
			refdvar[i]=0;
		}
		String[] varg=varu.getgroupvar();
		String[] reqvar=varu.getreqvar();
		int[] replacerule=varu.getreplaceruleforsel(replace);
		DataReader data = new DataReader(dict);
		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		int[] grouprule=varu.getgroupruleforsel();
		int[] rowrule=varu.getrowruleforsel();
		int[] weightrule=varu.getweightruleforsel();
		ValuesParser vp=new ValuesParser(null, grouprule, null, rowrule, null, weightrule);
		Hashtable<String, Hashtable<String, Double>> freqval=new Hashtable<String, Hashtable<String, Double>>();
		int validgroup=0;
		double temp_value=0;
		String vga="";
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				Vector<String> vargroupvalues=vp.getvargroup(values);
				String[] varrowvalues = vp.getrowvar(values);
				double weightvalue=vp.getweight(values);
				if (!Double.isNaN(weightvalue))
				{
					validgroup++;
					if (varg.length==0)
					{
						if (freqval.get("")==null)
						{
							Hashtable<String, Double> temp_freqval=new Hashtable<String, Double>();
							temp_freqval.put(varrowvalues[0], new Double(weightvalue));
							freqval.put("", temp_freqval);
						}
						else
						{
							Hashtable<String, Double> temp_freqval=freqval.get("");
							if (temp_freqval.get(varrowvalues[0])==null) temp_freqval.put(varrowvalues[0], new Double(weightvalue));
							else
							{
								temp_value=temp_freqval.get(varrowvalues[0]).doubleValue();
								temp_freqval.put(varrowvalues[0], new Double(temp_value+weightvalue));
							}
							freqval.put("", temp_freqval);
						}
					}
					else
					{
						vga="";
						for (int i=0; i<vargroupvalues.size(); i++)
						{
							vga=vga+vargroupvalues.get(i)+" ";
						}
						vga=vga.trim();
						if (freqval.get(vga)==null)
						{
							Hashtable<String, Double> temp_freqval=new Hashtable<String, Double>();
							temp_freqval.put(varrowvalues[0], new Double(weightvalue));
							freqval.put(vga, temp_freqval);
						}
						else
						{
							Hashtable<String, Double> temp_freqval=freqval.get(vga);
							if (temp_freqval.get(varrowvalues[0])==null) temp_freqval.put(varrowvalues[0], new Double(weightvalue));
							else
							{
								temp_value=temp_freqval.get(varrowvalues[0]).doubleValue();
								temp_freqval.put(varrowvalues[0], new Double(temp_value+weightvalue));
							}
							freqval.put(vga, temp_freqval);
						}
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		Dimension dimension = new Dimension(width, eight);

		LayeredWordCloud layeredWordCloud = new LayeredWordCloud(freqval.size(), dimension, CollisionMode.PIXEL_PERFECT);
		layeredWordCloud.setBackgroundColor(colorback);
		int actual_layer=0;
		int distance=0;
		for (Enumeration<String> en=freqval.keys(); en.hasMoreElements();)
		{
			vga=en.nextElement();
			Hashtable<String, Double> temp_freqval=freqval.get(vga);
			Vector<WordFrequency> actual_freq=new Vector<WordFrequency>();
			for (Enumeration<String> enn=temp_freqval.keys(); enn.hasMoreElements();)
			{
				String term=enn.nextElement();
				temp_value=temp_freqval.get(term).doubleValue();
				actual_freq.add(new WordFrequency(term, (int)temp_value));
			}
			layeredWordCloud.setPadding(actual_layer, padding);
			if (fsp>0)
				layeredWordCloud.setFontScalar(actual_layer, new LinearFontScalar(fsmin, fsmax));
			if (background_png!=null)
			{
				try
				{
					layeredWordCloud.setBackground(actual_layer, new PixelBoundryBackground(background_png));
				}
				catch (Exception b){}
			}
			if (pale>0)
			{
				if (freqval.size()>1)
				{
					if (actual_layer>0)
					{
						distance=integer_colorplast-integer_colorpfirst;
						if (distance<0) distance=-1*distance;
						integer_colorplast=integer_colorplast+distance+122;
						integer_colorpfirst=integer_colorpfirst+distance+122;
						if (integer_colorplast>16777210) integer_colorplast=200;
						if (integer_colorpfirst>16777210) integer_colorpfirst=200;
						String temp_f=Integer.toHexString(integer_colorpfirst);
						String temp_l=Integer.toHexString(integer_colorplast);
						colorpfirst=Color.decode("#"+temp_f);
						colorplast=Color.decode("#"+temp_l);
					}
				}
				layeredWordCloud.setColorPalette(actual_layer, new LinearGradientColorPalette(colorpfirst, colorplast, scps));
			}
			layeredWordCloud.build(actual_layer, actual_freq);
			actual_layer++;
		}
		Vector<StepResult> result = new Vector<StepResult>();
		String workdirg=(String)parameters.get(Keywords.WorkDir);

		java.util.Date dateProcedure=new java.util.Date();
		long timeProcedure=dateProcedure.getTime();

		String temp_file=workdirg+String.valueOf(timeProcedure)+".png";
		(new File(temp_file)).delete();
		layeredWordCloud.writeToFile(temp_file);
		if (outpng!=null)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try
			{
				FileInputStream fin = new FileInputStream(new File(temp_file));
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
				return new Result("%794%<br>\n", false, null);
			}
			String path=outpng;
			result.add(new LocalFileSave(path,baos.toByteArray()));
			(new File(temp_file)).delete();
		}
		else
		{
			result.add(new LocalImageViewer(temp_file, "WordCloud"));
		}
		return new Result("",true,result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 4265, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 4266, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "var=all", false, 4267, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.imgwidth,"text",false,798,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.imgheight,"text",false,799,dep,"",2));

		parameters.add(new GetRequiredParameters(Keywords.outpng, "filesave=.png", false, 4268, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4269, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.background_png, "text", false, 4271, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.background_color, "text", false, 4273, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4272, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.padding, "text", false, 4270, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.colorpalettefirst, "text", false, 4274, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.colorpalettelast, "text", false, 4275, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.colorpalettestep, "text", false, 4276, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.fontscalar_min, "text", false, 4277, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.fontscalar_max, "text", false, 4278, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="804";
		retprocinfo[1]="4279";
		return retprocinfo;
	}
}
