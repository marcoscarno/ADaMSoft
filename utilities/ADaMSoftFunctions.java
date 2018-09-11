/**
* Copyright © 2015 MS
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

package ADaMSoft.utilities;

import org.jasypt.util.text.BasicTextEncryptor;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLDocument;

import ADaMSoft.dataaccess.MemoryValue;
import ADaMSoft.gui.MainGUI;
import ADaMSoft.keywords.Keywords;
import cern.clhep.PhysicalConstants;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleFactory3D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix3D;
import cern.jet.math.Arithmetic;
import cern.jet.random.Beta;
import cern.jet.random.Binomial;
import cern.jet.random.BreitWigner;
import cern.jet.random.BreitWignerMeanSquare;
import cern.jet.random.ChiSquare;
import cern.jet.random.Exponential;
import cern.jet.random.ExponentialPower;
import cern.jet.random.Gamma;
import cern.jet.random.HyperGeometric;
import cern.jet.random.Hyperbolic;
import cern.jet.random.Logarithmic;
import cern.jet.random.NegativeBinomial;
import cern.jet.random.Normal;
import cern.jet.random.Poisson;
import cern.jet.random.StudentT;
import cern.jet.random.Uniform;
import cern.jet.random.VonMises;
import cern.jet.random.Zeta;
import cern.jet.stat.Probability;
import corejava.Format;

/**
* This class contains different functions that can be applied to the Data Step
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/

public class ADaMSoftFunctions extends ADaMSoftWebFunctions
{
	public static final double PI=PhysicalConstants.pi;
	public static final double AVOGADRO=PhysicalConstants.Avogadro;
	public static final double EPSILON2=PhysicalConstants.epsilon0;
	public static final double PI2=PhysicalConstants.pi2;
	public static final double E=Math.E;
	public static final double MAXNUM=Double.MAX_VALUE;
	public static final double MINNUM=Double.MIN_VALUE;
	public static final double NEGINF=Double.NEGATIVE_INFINITY;
	public static final double POSINF=Double.POSITIVE_INFINITY;
	public static final double NAN=Double.NaN;
	double retvalv;
    private boolean isEmpty(CharSequence cs)
    {
		return cs == null || cs.length() == 0;
    }
	/**
	*Transform a text (String) in a number (double)
	*/
	public void WAITMSGFOROUT(boolean statemsg)
	{
		if (statemsg) System.setProperty("waitmsgforout","true");
		else System.setProperty("waitmsgforout","false");
	}
    /**
     * <p>Searches a String for substrings delimited by a start and end tag,
     * returning all matching substrings in an array.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * A {@code null} open/close returns {@code null} (no match).
     * An empty ("") open/close returns {@code null} (no match).</p>
     *
     * <pre>
     * StringUtils.substringsBetween("[a][b][c]", "[", "]") = ["a","b","c"]
     * StringUtils.substringsBetween(null, *, *)            = null
     * StringUtils.substringsBetween(*, null, *)            = null
     * StringUtils.substringsBetween(*, *, null)            = null
     * StringUtils.substringsBetween("", "[", "]")          = []
     * </pre>
     *
     * @param str  the String containing the substrings, null returns null, empty returns empty
     * @param open  the String identifying the start of the substring, empty returns null
     * @param close  the String identifying the end of the substring, empty returns null
     * @return a String Array of substrings, or {@code null} if no match
     */
	public String[] SPLITBETWEEN(String text, String open, String close)
	{
		try
		{
			String str=text;
			if (str == null || isEmpty(open) || isEmpty(close))
			{
				return null;
			}
			int strLen = str.length();
			if (strLen == 0) return null;
			Pattern patternopen = Pattern.compile(open);
			Pattern patternopencheck = Pattern.compile(open);
			Pattern patternclose = Pattern.compile(close);
			Matcher matcherclose = null;
			int closeLen = close.length();
			int openLen = open.length();
			Vector<String> list = new Vector<String>();
			Matcher matcheropen=patternopen.matcher(str);
			Matcher matcheropencheck=null;
			String newcheck="";
			boolean realadd=true;
			while (matcheropen.find())
			{
				int start=matcheropen.start();
				if (start>0)
				{
					newcheck=str.substring(start + openLen-1);
					matcherclose=patternclose.matcher(newcheck);
					if (matcherclose.find())
					{
						int end = matcherclose.start()+start+openLen-1;
						if (end<0)
						{
							break;
						}
						else
						{
							realadd=true;
							try
							{
								matcheropencheck=patternopencheck.matcher(newcheck);
								if (matcheropencheck.find())
								{
									int newstart=matcheropencheck.start()+start;
									if (newstart>start && newstart<end)
									{
										realadd=false;
										str=str.substring(start + openLen-1);
									}
								}
							}
							catch (Exception e){}
							if (realadd)
							{
								list.add(str.substring(start, end+closeLen-1));
								str=str.substring(end + closeLen);
							}
						}
						matcheropen=patternopen.matcher(str);
					}
					else break;
				}
				else break;
			}
			if (list.isEmpty()) return null;
			return list.toArray(new String [list.size()]);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	/**
	*Transform a text (String) in a number (double)
	*/
	public double TEXT2NUM(String value)
	{
		if(value!=null)
		{
			if (!value.equals(""))
			{
				try
				{
					retvalv=Double.parseDouble(value);
					if ( (!Double.isNaN(retvalv)) && (!Double.isInfinite(retvalv)) )
						return retvalv;
				}
				catch (Exception en){}
			}
		}
		return Double.NaN;
	}
	/**
	*Transform a number (double) in a text (String)
	*/
	public String NUM2TEXT(double value)
	{
		if ( (Double.isNaN(value)) || (Double.isInfinite(value)) )
			return "";
		try
		{
			return String.valueOf(value);
		}
		catch (Exception etn)
		{
			return "";
		}
	}
	/**
	*Transform a number (double) in a text (String)
	*/
	public String NUM2TEXT(double value, String formatval)
	{
		if ( (Double.isNaN(value)) || (Double.isInfinite(value)) )
			return "";
		if (formatval==null)
			return "";
		if (formatval.toUpperCase().startsWith((Keywords.NUMSuffix).toUpperCase()))
		{
			if ((formatval.length()>Keywords.NUMSuffix.length())&& (formatval.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			{
				String num=formatval.substring(Keywords.NUMSuffix.length());
				if ((num.toUpperCase()).startsWith(Keywords.INTSuffix))
				{
					try
					{
						Format f=new Format("%f");
						String ret=f.format(value);
						ret=ret.substring(0,ret.indexOf("."));
						ret=ret.trim();
						return ret;
					}
					catch (Exception ex)
					{
						return "";
					}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.DECSuffix))
				{
					String num1=formatval.substring(Keywords.NUMSuffix.length()+Keywords.DECSuffix.length());
					try
					{
						int numFormatDEC=Integer.parseInt(num1.trim());
						String formatType="%."+numFormatDEC+"f";
						Format f=new Format(formatType);
						String ret=(f.format(value)).trim();
						return ret;
					}
					catch (Exception ex)
					{
						return "";
					}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.EXPSuffix))
				{
					try
					{
						Format f=new Format("%E");
						String ret=(f.format(value)).trim();
						return ret;
					}
					catch (Exception ex) {}
				}
			}
		}
		else
		{
			try
			{
				Locale lc= Locale.getDefault();
				SimpleDateFormat sdf = new SimpleDateFormat(formatval, lc);
				Calendar cal = Calendar.getInstance();
				long offset = cal.get(Calendar.ZONE_OFFSET);
				return sdf.format(new Date((long)value-offset));

			}
			catch (Exception e)
			{
				return String.valueOf(value);
			}
		}
		return String.valueOf(value);
	}
	/**
	*Transform a number (double) in a text (String) that can be used for dates
	*/
	public String NUM2TEXT(double value, String formatval, String language, String country)
	{
		if ( (Double.isNaN(value)) || (Double.isInfinite(value)) )
			return "";
		if (formatval==null)
			return "";
		try
		{
			Locale lc=new Locale(language, country.toUpperCase());
			SimpleDateFormat sdf = new SimpleDateFormat(formatval, lc);
			return sdf.format(new Date((long)value));
		}
		catch (Exception e)
		{
			return String.valueOf(value);
		}
	}
	/**
	*Return true if the value is a number
	*/
	public boolean ISNUM(double value)
	{
		if (Double.isNaN(value))
			return false;
		else if (Double.isInfinite(value))
			return false;
		return true;
	}
	/**
	*Return false if the value is a number
	*/
	public boolean ISNAN(double value)
	{
		if (Double.isNaN(value))
			return true;
		else if (Double.isInfinite(value))
			return true;
		return false;
	}
	/**
	*Return the ABS value of a number.
	*/
	public double ABS(double value)
	{
		if (!ISNUM(value))
			return Double.NaN;
		else
			return Math.abs(value);
	}
	/**
	*Return the number tha contains the ABS value of the received text
	*/
	public double ABS(String val)
	{
		double value=TEXT2NUM(val);
		if (!ISNUM(value))
			return Double.NaN;
		else
			return Math.abs(value);
	}
	/**
	*Put the first char of a path in capital letter, the other will be written in lower case.
	*/
	public String toAdamsFormat(String path)
	{
		String separator="/";
		if(!path.toLowerCase().startsWith("http://"))
			separator=System.getProperty("file.separator");
		String file = "";
		if (path.lastIndexOf(".")<=0)
			file = path.substring(path.lastIndexOf(separator)+1);
		else
			file = path.substring(path.lastIndexOf(separator)+1,path.lastIndexOf("."));
		file = file.toLowerCase();
		String firstChar=file.substring(0,1);
		file = file.replaceFirst(firstChar,firstChar.toUpperCase());
		//take the directory and the file extension
		String dir="";
		if (path.lastIndexOf(separator)>=0)
			dir = path.substring(0,path.lastIndexOf(separator)+1);
		String ext="";
		if (path.lastIndexOf(".")>0)
			ext = path.substring(path.lastIndexOf("."));
		return dir+file+ext;
	}
	/**
	* Return the millis elapsed from the epoch (1° January 1970).
	* @param val A date in string format.
	* @param format The format of the date in val.
	* @return The count of millis elapsed from the epoch.
	*/
	public double TODATEFROMTODAY(String val, String format)
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			Calendar nowdate = Calendar.getInstance();
			int currentyear=nowdate.get(Calendar.YEAR);
			currentyear=currentyear-100;
			nowdate.set(currentyear, 1, 1);
			Date cdate=nowdate.getTime();
			sdf.set2DigitYearStart(cdate);
			return (double)sdf.parse(val).getTime();
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	* Return the millis elapsed from the epoch (1° January 1970).
	* @param val A date in string format.
	* @param format The format of the date in val.
	* @return The count of millis elapsed from the epoch.
	*/
	public double TODATE(String val, String format)
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return (double)sdf.parse(val).getTime();
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	* Return the millis elapsed from the epoch (1° January 1970).
	* @param val A date in string format.
	* @param format The format of the date in val.
	* @param language The desidered language
	* @param coutry The desidered country
	* @return The count of millis elapsed from the epoch.
	*/
	public double TODATEFROMTODAY(String val, String format, String language, String country)
	{
		Locale lc=null;
		try
		{
			lc=new Locale(language, country.toUpperCase());

		}
		catch (Exception e)
		{
			return Double.NaN;
		}
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(format, lc);
			Calendar nowdate = Calendar.getInstance();
			int currentyear=nowdate.get(Calendar.YEAR);
			currentyear=currentyear-100;
			nowdate.set(currentyear, 1, 1);
			Date cdate=nowdate.getTime();
			sdf.set2DigitYearStart(cdate);
			return (double)sdf.parse(val).getTime();
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Return the millis elapsed from the epoch (1° January 1970).
	* @param val A date in string format.
	* @param format The format of the date in val.
	* @param language The desidered language
	* @param coutry The desidered country
	* @return The count of millis elapsed from the epoch.
	*/
	public double TODATE(String val, String format, String language, String country)
	{
		Locale lc=null;
		try
		{
			lc=new Locale(language, country.toUpperCase());

		}
		catch (Exception e)
		{
			return Double.NaN;
		}
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(format, lc);
			return (double)sdf.parse(val).getTime();
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	* Returns the number of day between two dates.
	* @param date1 A date in numerical format.
	* @param date2 A date in numerical format.
	* @return The number of day between two dates.
	*/
	public double DATEDIFF(double date1, double date2)
	{
		try
		{
			double dif=0;
			if(date1<date2)
			{
				dif=date2-date1;
			}
			else
			{
				dif=date1-date2;
			}
			return dif/86400000;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the day of the month from date in numerical format.
	* @param date1 A date in numerical format.
	* @return The day of the month from date in numerical format.
	*/
	public double DAY(double date1)
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis((long) date1);
			return cal.get(Calendar.DAY_OF_MONTH);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the hour of the day from date in numerical format.
	* @param date1 A date in numerical format.
	* @return The hour of the day from date in numerical format.
	*/
	public double HOUR(double date1)
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis((long) date1);
			return cal.get(Calendar.HOUR_OF_DAY);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the minute of the hour from date in numerical format.
	* @param date1 A date in numerical format.
	* @return The minute of the hour from date in numerical format.
	*/
	public double MINUTE(double date1)
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis((long) date1);
			return cal.get(Calendar.MINUTE);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the month of the year from date in numerical format.
	* @param date1 A date in numerical format.
	* @return The month of the year from date in numerical format.
	*/
	public double MONTH(double date1)
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis((long) date1);
			return cal.get(Calendar.MONTH)+1;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the year from date in numerical format.
	* @param date1 A date in numerical format.
	* @return The month of the year from date in numerical format.
	*/
	public double YEAR(double date1)
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis((long) date1);
			return cal.get(Calendar.YEAR);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}


	/**
	* Returns the second of the minute from date in numerical format.
	* @param date1 A date in numerical format.
	* @return The second of the minute from date in numerical format.
	*/
	public double SECOND(double date1)
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis((long) date1);
			return cal.get(Calendar.SECOND);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the current date in numerical format.
	* @return The current date in numerical format.
	*/
	public double NOW()
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			return cal.getTimeInMillis();
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the current hour in numerical format H24.
	* @return The current hour in numerical format H24.
	*/
	public double TIME()
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.set(1970, 1, 1);
			return cal.getTimeInMillis();
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the current day of the month in numerical format.
	* @return The current day of the month in numerical format..
	*/
	public double TODAY()
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			return cal.get(Calendar.DAY_OF_MONTH);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the current day of the week in numerical format.
	* @return The current day of the week in numerical format..
	*/
	public double WEEKDAY()
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			return cal.get(Calendar.DAY_OF_WEEK);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the current month in numerical format.
	* @return The current month in numerical format..
	*/
	public double MONTH()
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			return cal.get(Calendar.MONTH)+1;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the current year in numerical format.
	* @return The current year in numerical format..
	*/
	public double YEAR()
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			return cal.get(Calendar.YEAR);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the date part in numerical format.
	* @param date1 A date in numerical format.
	* @return The date part in numerical format.
	*/
	public double DATEPART(double date1)
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis((long)date1);
			cal.set(Calendar.HOUR_OF_DAY,0);
			cal.set(Calendar.MINUTE,0);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MILLISECOND,0);
			return cal.getTimeInMillis();
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	* Returns the hour part in numerical format.
	* @param date1 A date in numerical format.
	* @return The hour part in numerical format.
	*/
	public double TIMEPART(double date1)
	{
		try
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis((long)date1);
			cal.set(Calendar.MONTH,1);
			cal.set(Calendar.DAY_OF_MONTH,1);
			cal.set(Calendar.YEAR,1970);
			return cal.getTimeInMillis();
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/**
	*Returns the arc cosine value of the argument passed as string.
	*/
	public double ARCOS(String value)
	{
		double val = TEXT2NUM(value);
		if (!ISNUM(val))
			return Double.NaN;
		else
			return Math.acos(val);
	}
	/**
	*Returns the arc cosine value of the received number
	*/
	public double ARCOS(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		else
			return Math.acos(value);
	}
	/**
	*Returns the arc sine value of the argument passed as string.
	*/
	public double ARCSIN(String value)
	{
		double val = TEXT2NUM(value);
		if (!ISNUM(val))
			return Double.NaN;
		else
			return Math.asin(val);
	}
	/**
	*Returns the arc sine value of the received argument
	*/
	public double ARCSIN(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		else
			return Math.asin(value);
	}
	/**
	*Returns the arc tangent value of the argument passed as string.
	*/
	public double ATAN(String value)
	{
		double val = TEXT2NUM(value);
		if (!ISNUM(val))
			return Double.NaN;
		else
			return Math.atan(val);
	}
	/**
	*Returns the arcotangent value of the argument passed.
	*/
	public double ATAN(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		else
			return Math.atan(value);
	}
	/**
	*Returns the cosine value of the argument passed as string.
	*/
	public double COS(String value)
	{
		double val = TEXT2NUM(value);
		if (!ISNUM(val))
			return Double.NaN;
		else
			return Math.cos(val);
	}
	/**
	*Returns the cosine value of the received argument
	*/
	public double COS(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		else
			return Math.cos(value);
	}
	/**
	*Returns the sine value of the argument passed as string.
	*/
	public double SIN(String value)
	{
		double val = TEXT2NUM(value);
		if (!ISNUM(val))
			return Double.NaN;
		else
			return Math.sin(val);
	}
	/**
	*Returns the sine value of the received argument
	*/
	public double SIN(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		else
			return Math.sin(value);
	}
	/**
	* Returns the tangent value of the argument passed as string.
	*/
	public double TAN(String value)
	{
		double val = TEXT2NUM(value);
		if (!ISNUM(val))
			return Double.NaN;
		else
			return Math.tan(val);
	}
	/**
	*Returns the tangent value of the received argument
	*/
	public double TAN(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		else
			return Math.tan(value);
	}
	/**
	*Returns the exponential (the constant e raised to the power val) value of the argument passed as string.
	*/
	public double EXP(String value)
	{
		double val = TEXT2NUM(value);
		if (!ISNUM(val))
			return Double.NaN;
		else
			return Math.exp(val);
	}
	/**
	*Returns the exponential (the constant e raised to the power val) value of the argument passed.
	*/
	public double EXP(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		else
			return Math.exp(value);
	}
	/**
	*Returns the factorial value of the argument passed as string.
	*/
	public double FACT(String value)
	{
		double val = TEXT2NUM(value);
		if (!ISNUM(val))
			return Double.NaN;
		else
			return Arithmetic.factorial((int)val);
	}
	/**
	*Returns the factorial value of the received argument
	*/
	public double FACT(double value){
		if(!ISNUM(value))
			return Double.NaN;
		else
		try
		{
			return Arithmetic.factorial((int)value);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the binomial (n over k) of the argument passed as string.
	*/
	public double BINOMIAL(String n, String k)
	{
		double nn = TEXT2NUM(n);
		double kn = TEXT2NUM(k);
		return BINOMIAL(nn, kn);
	}
	/**
	*Returns the binomial (n over k) of the received argument
	*/
	public double BINOMIAL(double n, double k)
	{
		if(!ISNUM(n) || !ISNUM(k) )
			return Double.NaN;
		try
		{
			return Arithmetic.binomial(n, (long)k);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}

	/*Probability functions*/

	/**
	*Returns probability of the error function value of the argument passed as string.
	*/
	public double ERF(String value)
	{
		double val = TEXT2NUM(value);
		return ERF(val);
	}
	/**
	*Returns probability of the error function value of the argument passed.
	*/
	public double ERF(double value)
	{
		if (!ISNUM(value))
			return Double.NaN;
		else
		{
			try
			{
				return Probability.errorFunction(value);
			}
			catch (Exception e)
			{
				return Double.NaN;
			}
		}
	}
	/**
	*Returns the error function complemented value of the argument passed as string.
	*/
	public double ERFC(String value)
	{
		double val = TEXT2NUM(value);
		return ERFC(val);
	}
	/**
	*Returns the error function complemented value of the argument passed.
	*/
	public double ERFC(double value)
	{
		if (!ISNUM(value))
			return Double.NaN;
		try
		{
			return Probability.errorFunctionComplemented(value);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the integral from zero to x of the gamma probability density function passed as string.
	* @param value1 the paramater alpha of the gamma distribution.
	* @param value2 the paramater beta or lambda of the gamma distribution.
	* @param value3 integration end point.
	* @return Returns the integral from zero to x of the gamma probability density function.
	*/
	public double GAMMA(String value1, String value2, String value3)
	{
		double val1 = TEXT2NUM(value1);
		double val2 = TEXT2NUM(value2);
		double val3 = TEXT2NUM(value3);
		return GAMMA(val1,val2,val3);
	}
	/**
	*Returns the integral from zero to x of the gamma probability density function.
	* @param value1 the paramater alpha of the gamma distribution.
	* @param value2 the paramater beta or lambda of the gamma distribution.
	* @param value3 integration end point.
	* @return Returns the integral from zero to x of the gamma probability density function.
	*/
	public double GAMMA(double value1, double value2, double value3)
	{
		if (!ISNUM(value1) || !ISNUM(value2) || !ISNUM(value3))
			return Double.NaN;
		try
		{
			return Probability.gamma(value1, value2, value3);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the Incomplete Beta Function evaluated from zero to xx; formerly named ibeta
	*@param value1 the alpha parameter of the beta distribution, received as String
	*@param value2 the beta parameter of the beta distribution, received as String
	*@param value3 the integration end point, received as String
	*/
	public double INCOMPLETEBETA(String value1, String value2, String value3)
	{
		double val1 = TEXT2NUM(value1);
		double val2 = TEXT2NUM(value2);
		double val3 = TEXT2NUM(value3);
		return INCOMPLETEBETA(val1,val2,val3);
	}
	/**
	*Returns the Incomplete Beta Function evaluated from zero to xx; formerly named ibeta
	*@param value1 the alpha parameter of the beta distribution
	*@param value2 the beta parameter of the beta distribution
	*@param value3 the integration end point.
	*/
	public double INCOMPLETEBETA(double value1, double value2, double value3)
	{
		if(!ISNUM(value1) || !ISNUM(value2) || !ISNUM(value3))
			return Double.NaN;
		try
		{
			return cern.jet.stat.Gamma.incompleteBeta(value1, value2, value3);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the Incomplete Gamma function; formerly named igamma
	*@param value1 the parameter of the gamma distribution, received as String
	*@param value2 the integration end point, received as String
	*/
	public double INCOMPLETEGAMMA(String value1, String value2)
	{
		double val1 = TEXT2NUM(value1);
		double val2 = TEXT2NUM(value2);
		return INCOMPLETEGAMMA(val1,val2);
	}
	/**
	*Returns the Incomplete Gamma function; formerly named igamma
	*@param value1 the parameter of the gamma distribution
	*@param value2 the integration end point
	*/
	public double INCOMPLETEGAMMA(double value1, double value2)
	{
		if(!ISNUM(value1) || !ISNUM(value2) )
			return Double.NaN;
		try
		{
			return cern.jet.stat.Gamma.incompleteGamma(value1, value2);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the Complemented Incomplete Gamma function; formerly named igamc
	*@param value1 the parameter of the gamma distribution, received as String
	*@param value2 the integration end point, received as String
	*/
	public double INCOMPLETEGAMMAC(String value1, String value2)
	{
		double val1 = TEXT2NUM(value1);
		double val2 = TEXT2NUM(value2);
		return INCOMPLETEGAMMAC(val1,val2);
	}
	/**
	*Returns the Complemented Incomplete Gamma function; formerly named igamc
	*@param value1 the parameter of the gamma distribution
	*@param value2 the integration end point
	*/
	public double INCOMPLETEGAMMAC(double value1, double value2)
	{
		if(!ISNUM(value1) || !ISNUM(value2) )
			return Double.NaN;
		try
		{
			return cern.jet.stat.Gamma.incompleteGammaComplement(value1, value2);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the natural logarithm of the gamma function; formerly named lgamma
	*@param value1 the integration end point, received as String
	*/
	public double LOGGAMMA(String value1)
	{
		double val1 = TEXT2NUM(value1);
		return LOGGAMMA(val1);
	}
	/**
	*Returns the natural logarithm of the gamma function; formerly named lgamma
	*@param value1 the integration end point
	*/
	public double LOGGAMMA(double value1)
	{
		if(!ISNUM(value1))
			return Double.NaN;
		try
		{
			return cern.jet.stat.Gamma.logGamma(value1);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the area from zero to x under the beta density function. This function is identical to the incomplete beta integral function INCOMPLETEBETA(a, b, x). The complemented function is 1 - P(1-x) = Gamma.incompleteBeta(b, a, x)
	*/
	public double BETA(String value1, String value2, String value3)
	{
		double val1 = TEXT2NUM(value1);
		double val2 = TEXT2NUM(value2);
		double val3 = TEXT2NUM(value3);
		return BETA(val1, val2, val3);
	}
	/**
	*Returns the area from zero to x under the beta density function. This function is identical to the incomplete beta integral function INCOMPLETEBETA(a, b, x). The complemented function is 1 - P(1-x) = Gamma.incompleteBeta(b, a, x)
	*/
	public double BETA(double value1, double value2, double value3)
	{
		if(!ISNUM(value1) || !ISNUM(value2) || !ISNUM(value3))
			return Double.NaN;
		try
		{
			return Probability.beta(value1, value2, value3);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the area under the right hand tail (from x to infinity) of the beta density function. This function is identical to the incomplete beta integral function INCOMPLETEBETA(b, a, x)
	*/
	public double BETACOMPLEMENTED(String value1, String value2, String value3)
	{
		double val1 = TEXT2NUM(value1);
		double val2 = TEXT2NUM(value2);
		double val3 = TEXT2NUM(value3);
		return BETACOMPLEMENTED(val1, val2, val3);
	}
	/**
	*Returns the area from zero to x under the beta density function. This function is identical to the incomplete beta integral function INCOMPLETEBETA(a, b, x). The complemented function is 1 - P(1-x) = INCOMPLETEBETA(b, a, x)
	*/
	public double BETACOMPLEMENTED(double value1, double value2, double value3)
	{
		if(!ISNUM(value1) || !ISNUM(value2) || !ISNUM(value3))
			return Double.NaN;
		try
		{
			return Probability.betaComplemented(value1, value2, value3);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the sum of the terms 0 through k of the Binomial probability density
	*@param k end term
	*@param n the number of trials
	*@param p the probability of success (must be in (0.0,1.0)).
	*/
	public double BINOMIAL(String k, String n, String p)
	{
		double val1 = TEXT2NUM(k);
		double val2 = TEXT2NUM(n);
		double val3 = TEXT2NUM(p);
		return BINOMIAL(val1, val2, val3);
	}
	/**
	*Returns the sum of the terms 0 through k of the Binomial probability density; All arguments must be positive
	*@param k end term
	*@param n the number of trials
	*@param p the probability of success (must be in (0.0,1.0)).
	*/
	public double BINOMIAL(double k, double n, double p)
	{
		if(!ISNUM(k) || !ISNUM(n) || !ISNUM(p))
			return Double.NaN;
		try
		{
			int ki=(int)k;
			int ni=(int)n;
			return Probability.binomial(ki, ni, p);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the sum of the terms k+1 through n of the Binomial probability density
	*@param k end term
	*@param n the number of trials
	*@param p the probability of success (must be in (0.0,1.0)).
	*/
	public double BINOMIALCOMPLEMENTED(String k, String n, String p)
	{
		double val1 = TEXT2NUM(k);
		double val2 = TEXT2NUM(n);
		double val3 = TEXT2NUM(p);
		return BINOMIALCOMPLEMENTED(val1, val2, val3);
	}
	/**
	*Returns the sum of the terms k+1 through n of the Binomial probability density
	*@param k end term
	*@param n the number of trials
	*@param p the probability of success (must be in (0.0,1.0)).
	*/
	public double BINOMIALCOMPLEMENTED(double k, double n, double p)
	{
		if(!ISNUM(k) || !ISNUM(n) || !ISNUM(p))
			return Double.NaN;
		try
		{
			int ki=(int)k;
			int ni=(int)n;
			return Probability.binomialComplemented(ki, ni, p);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the area under the left hand tail (from 0 to x) of the Chi square probability density function with v degrees of freedom, where x is the Chi-square variable
	*@param v degrees of freedom
	*@param x the integration end point, received as String
	*/
	public double CHISQUARE(String v, String x)
	{
		double val1 = TEXT2NUM(v);
		double val2 = TEXT2NUM(x);
		return CHISQUARE(val1,val2);
	}
	/**
	*Returns the area under the left hand tail (from 0 to x) of the Chi square probability density function with v degrees of freedom, where x is the Chi-square variable
	*@param v degrees of freedom
	*@param x the integration end point
	*/
	public double CHISQUARE(double v, double x)
	{
		if(!ISNUM(v) || !ISNUM(x) )
			return Double.NaN;
		try
		{
			return Probability.chiSquare(v, x);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the area under the left hand tail (from x to infinity) of the Chi square probability density function with v degrees of freedom, where x is the Chi-square variable
	*@param v degrees of freedom
	*@param x the integration end point, received as String
	*/
	public double CHISQUARECOMPLEMENTED(String v, String x)
	{
		double val1 = TEXT2NUM(v);
		double val2 = TEXT2NUM(x);
		return CHISQUARECOMPLEMENTED(val1,val2);
	}
	/**
	*Returns the area under the left hand tail (from x to infinity) of the Chi square probability density function with v degrees of freedom, where x is the Chi-square variable
	*@param v degrees of freedom
	*@param x the integration end point
	*/
	public double CHISQUARECOMPLEMENTED(double v, double x)
	{
		if(!ISNUM(v) || !ISNUM(x) )
			return Double.NaN;
		try
		{
			return Probability.chiSquareComplemented(v, x);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the sum of the terms 0 through k of the Negative Binomial Distribution. In a sequence of Bernoulli trials, this is the probability that k or fewer failures precede the n-th success
	*@param k end term
	*@param n the number of trials
	*@param p the probability of success (must be in (0.0,1.0)).
	*/
	public double NEGATIVEBINOMIAL(String k, String n, String p)
	{
		double val1 = TEXT2NUM(k);
		double val2 = TEXT2NUM(n);
		double val3 = TEXT2NUM(p);
		return BINOMIAL(val1, val2, val3);
	}
	/**
	*Returns the sum of the terms 0 through k of the Negative Binomial Distribution; All arguments must be positive. In a sequence of Bernoulli trials, this is the probability that k or fewer failures precede the n-th success
	*@param k end term
	*@param n the number of trials
	*@param p the probability of success (must be in (0.0,1.0)).
	*/
	public double NEGATIVEBINOMIAL(double k, double n, double p)
	{
		if(!ISNUM(k) || !ISNUM(n) || !ISNUM(p))
			return Double.NaN;
		try
		{
			int ki=(int)k;
			int ni=(int)n;
			return Probability.negativeBinomial(ki, ni, p);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the sum of the terms k+1 of the Negative Binomial Distribution. In a sequence of Bernoulli trials, this is the probability that k or fewer failures precede the n-th success
	*@param k end term
	*@param n the number of trials
	*@param p the probability of success (must be in (0.0,1.0)).
	*/
	public double NEGATIVEBINOMIALCOMPLEMENTED(String k, String n, String p)
	{
		double val1 = TEXT2NUM(k);
		double val2 = TEXT2NUM(n);
		double val3 = TEXT2NUM(p);
		return NEGATIVEBINOMIALCOMPLEMENTED(val1, val2, val3);
	}
	/**
	*Returns the sum of the terms k+1 of the Negative Binomial Distribution; All arguments must be positive. In a sequence of Bernoulli trials, this is the probability that k or fewer failures precede the n-th success
	*@param k end term
	*@param n the number of trials
	*@param p the probability of success (must be in (0.0,1.0)).
	*/
	public double NEGATIVEBINOMIALCOMPLEMENTED(double k, double n, double p)
	{
		if(!ISNUM(k) || !ISNUM(n) || !ISNUM(p))
			return Double.NaN;
		try
		{
			int ki=(int)k;
			int ni=(int)n;
			return Probability.negativeBinomialComplemented(ki, ni, p);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the area under the Normal (Gaussian) probability density function, integrated from minus infinity to x (assumes mean is zero, variance is one)
	*@param x end term
	*/
	public double NORMAL(String x)
	{
		double val1 = TEXT2NUM(x);
		return NORMAL(val1);
	}
	/**
	*Returns the area under the Normal (Gaussian) probability density function, integrated from minus infinity to x (assumes mean is zero, variance is one)
	*@param x end term
	*/
	public double NORMAL(double x)
	{
		if(!ISNUM(x))
			return Double.NaN;
		try
		{
			return Probability.normal(x);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the area under the Normal (Gaussian) probability density function, integrated from minus infinity to x.
	*@param mean the mean
	*@param variance the variance
	*@param x end term
	*/
	public double NORMAL(String mean, String variance, String x)
	{
		double val1 = TEXT2NUM(mean);
		double val2 = TEXT2NUM(variance);
		double val3 = TEXT2NUM(x);
		return NORMAL(val1, val2, val3);
	}
	/**Returns the area under the Normal (Gaussian) probability density function, integrated from minus infinity to x.
	*@param mean the mean
	*@param variance the variance
	*@param x end term
	*/
	public double NORMAL(double mean, double variance, double x)
	{
		if(!ISNUM(mean) || !ISNUM(variance) || !ISNUM(x))
			return Double.NaN;
		try
		{
			return Probability.normal(mean, variance, x);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the value, x, for which the area under the Normal (Gaussian) probability density function (integrated from minus infinity to x) is equal to the argument y (assumes mean is zero, variance is one); formerly named ndtri
	*@param x end term
	*/
	public double NORMALINVERSE(String x)
	{
		double val1 = TEXT2NUM(x);
		return NORMALINVERSE(val1);
	}
	/**
	*Returns the value, x, for which the area under the Normal (Gaussian) probability density function (integrated from minus infinity to x) is equal to the argument y (assumes mean is zero, variance is one); formerly named ndtri
	*@param x end term
	*/
	public double NORMALINVERSE(double x)
	{
		if(!ISNUM(x))
			return Double.NaN;
		try
		{
			return Probability.normalInverse(x);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the sum of the first k terms of the Poisson distribution
	*@param k number of terms
	*@param mean the mean of the poisson distribution
	*/
	public double POISSON(String k, String mean)
	{
		double val1 = TEXT2NUM(k);
		double val2 = TEXT2NUM(mean);
		return POISSON(val1, val2);
	}
	/**
	*Returns the sum of the first k terms of the Poisson distribution
	*@param k number of terms
	*@param mean the mean of the poisson distribution
	*/
	public double POISSON(double k, double mean)
	{
		if(!ISNUM(k) || !ISNUM(mean))
			return Double.NaN;
		try
		{
			int ki=(int)k;
			return Probability.poisson(ki, mean);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the sum of the terms k+1 to Infinity of the Poisson distribution
	*@param k number of terms
	*@param mean the mean of the poisson distribution
	*/
	public double POISSONCOMPLEMENTED(String k, String mean)
	{
		double val1 = TEXT2NUM(k);
		double val2 = TEXT2NUM(mean);
		return POISSONCOMPLEMENTED(val1, val2);
	}
	/**
	*Returns the sum of the terms k+1 to Infinity of the Poisson distribution
	*@param k number of terms
	*@param mean the mean of the poisson distribution
	*/
	public double POISSONCOMPLEMENTED(double k, double mean)
	{
		if(!ISNUM(k) || !ISNUM(mean))
			return Double.NaN;
		try
		{
			int ki=(int)k;
			return Probability.poissonComplemented(ki, mean);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the integral from minus infinity to t of the Student-t distribution with k > 0 degrees of freedom
	*@param k degrees of freedom
	*@param t integration end point
	*/
	public double STUDENTT(String k, String t)
	{
		double val1 = TEXT2NUM(k);
		double val2 = TEXT2NUM(t);
		return STUDENTT(val1, val2);
	}
	/**
	*Returns the sum of the terms k+1 to Infinity of the Poisson distribution
	*@param k degrees of freedom
	*@param t integration end point
	*/
	public double STUDENTT(double k, double t)
	{
		if(!ISNUM(k) || !ISNUM(t))
			return Double.NaN;
		try
		{
			return Probability.studentT(k, t);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the value, t, for which the area under the Student-t probability density function (integrated from minus infinity to t) is equal to 1-alpha/2. The value returned corresponds to usual Student t-distribution lookup table for talpha(size>
	*@param alpha probability
	*@param size data set size
	*/
	public double STUDENTTINVERSE(String alpha, String size)
	{
		double val1 = TEXT2NUM(alpha);
		double val2 = TEXT2NUM(size);
		return STUDENTTINVERSE(val1, val2);
	}
	/**
	*Returns the value, t, for which the area under the Student-t probability density function (integrated from minus infinity to t) is equal to 1-alpha/2. The value returned corresponds to usual Student t-distribution lookup table for talpha(size>
	*@param alpha probability
	*@param size data set size
	*/
	public double STUDENTTINVERSE(double alpha, double size)
	{
		if(!ISNUM(alpha) || !ISNUM(size))
			return Double.NaN;
		try
		{
			int sizei=(int)size;
			return Probability.studentTInverse(alpha, sizei);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the natural logarithm (base e) of a double  value passed as string.
	*/
	public double LOG(String value)
	{
		double val = TEXT2NUM(value);
		return LOG(val);
	}
	/**
	*Returns the natural logarithm (base e) of a double  value.
	*/
	public double LOG(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		return Math.log(value);
	}
	/**
	*Returns the base 10 logarithm of a double value passed as string.
	*/
	public double LOG10(String value)
	{
		double val = TEXT2NUM(value);
		return LOG10(val);
	}
	/**
	*Returns the base 10 logarithm of a double value.
	*/
	public double LOG10(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		return Math.log10(value);
	}
	/**
	*Returns the base 2 logarithm of a double value passed as string.
	*/
	public double LOG2(String value)
	{
		double val = TEXT2NUM(value);
		return LOG2(val);
	}
	/**
	*Returns the base 2 logarithm of a double value passed as string.
	*/
	public double LOG2(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		try
		{
			return Math.log(value)/Math.log(2);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the remainder number of the division between value1 and value2 passed as string.
	* @param value1 The first value.
	* @param value2 The second value.
	* @return Returns the remainder number of the division between value1 and value2.
	*/
	public double MOD(String value1, String value2)
	{
		double val1 = TEXT2NUM(value1);
		double val2 = TEXT2NUM(value2);
		return MOD(val1,val2);
	}
	/**
	*Returns the remainder number of the division between value1 and value2.
	* @param value1 The first value.
	* @param value2 The second value.
	* @return Returns the remainder number of the division between value1 and value2.
	*/
	public double MOD(double value1, double value2)
	{
		if(!ISNUM(value1) || !ISNUM(value2))
			return Double.NaN;
		return value1%value2;
	}
	/**
	*Returns the signum function of the argument passed as string.
	*/
	public double SIGN(String value)
	{
		double val = TEXT2NUM(value);
		return SIGN(val);
	}
	/**
	*Returns the signum function of the argument.
	*/
	public double SIGN(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		return Math.signum(value);
	}
	/**
	*Returns the square root function of the argument passed as string.
	*/
	public double SQRT(String value)
	{
		double val = TEXT2NUM(value);
		return SQRT(val);
	}
	/**
	*Returns the square root function of the argument.
	*/
	public double SQRT(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		return Math.sqrt(value);
	}
	/**
	*Returns the hyperbolic sine function of the argument passed as string.
	*/
	public double SINH(String value)
	{
		double val = TEXT2NUM(value);
		return SINH(val);
	}
	/**
	*Returns the hyperbolic sine function of the argument.
	*/
	public double SINH(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		return Math.sinh(value);
	}
	/**
	*Returns the hyperbolic cosine function of the argument passed as string.
	*/
	public double COSH(String value)
	{
		double val = TEXT2NUM(value);
		return COSH(val);
	}
	/**
	*Returns the hyperbolic cosine function of the argument.
	*/
	public double COSH(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		return Math.cosh(value);
	}
	/**
	*Returns the hyperbolic tangent function of the argument passed as string.
	*/
	public double TANH(String value)
	{
		double val = TEXT2NUM(value);
		return TANH(val);
	}
	/**
	*Returns the hyperbolic tangent function of the argument.
	*/
	public double TANH(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		return Math.tanh(value);
	}

	/*Truncation functions*/
	/**
	*Returns the smallest (closest to negative infinity) double value that is greater than or equal to the argument (passed as string) and is equal to a mathematical integer.
	*/
	public double CEIL(String value)
	{
		double val = TEXT2NUM(value);
		return CEIL(val);
	}
	/**
	*Returns the smallest (closest to negative infinity) double value that is greater than or equal to the argument and is equal to a mathematical integer.
	*/
	public double CEIL(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		return Math.ceil(value);
	}
	/**
	*Returns the largest (closest to positive infinity) double value that is less than or equal to the argument (passed as string) and is equal to a mathematical integer.
	*/
	public double FLOOR(String value)
	{
		double val = TEXT2NUM(value);
		return FLOOR(val);
	}
	/**
	*Returns the largest (closest to positive infinity) double value that is less than or equal to the argument and is equal to a mathematical integer.
	*/
	public double FLOOR(double value)
	{
		if(!ISNUM(value))
			return Double.NaN;
		return Math.floor(value);
	}
	/* Functions that operates on a string*/
	/**
	*Removes multiple blanks from a string.
	*/
	public String COMPBL(String string)
	{
		if(string==null)
			return "";
		else if (string.equals(""))
			return "";
		try
		{
			return string.replaceAll("\\s{n,}", " ");
		}
		catch (Exception e)
		{
			return "";
		}
	}
	/**
	*Removes the string a from b.
	*/
	public String COMPBL(String a, String b)
	{
		if(a==null)
			return "";
		else if (b==null)
			return a;
		try
		{
			return a.replaceAll(b, "");
		}
		catch (Exception e)
		{
			return "";
		}
	}
	/**
	*Removes quotation mark from the string.
	*/
	public String DEQUOTE(String string)
	{
		if(string==null)
			return "";
		try
		{
			return string.replaceAll("\"", "");
		}
		catch (Exception e)
		{
			return "";
		}
	}
	/**
	*Search a character expression in string and returns the position where such expression is
	*/
	public double INDEX(String string, String pattern)
	{
		if(string==null)
			return Double.NaN;
		else if(pattern==null)
			return -1;
		return string.indexOf(pattern);
	}
	/**
	*Returns the length of string.
	*/
	public double LENGTH(String string)
	{
		if(string==null)
			return Double.NaN;
		return string.length();
	}
	/**
	*Converts all letters in an argument to lowercase.
	*/
	public String LOWCASE(String string)
	{
		if(string==null)
			return "";
		return string.toLowerCase();
	}
	/**
	*Return true if string contains a missing value, false otherwise.
	*/
	public boolean ISMISSING(String string)
	{
		if(string==null)
			return true;
		return string.equals("");
	}
	/**
	*Return true if string contains a missing value, false otherwise.
	*/
	public boolean ISMISSING(double value)
	{
		return ISNAN(value);
	}
	/**
	*Adds double quotation marks to a string.
	*/
	public String QUOTE(String string)
	{
		if(string==null)
			return "";
		return "\""+string+"\"";
	}
	/**
	*Repeat n times the string
	*/
	public String REPEAT(String string, double repetitions)
	{
		if(string==null)
			return "";
		String result="";
		if(ISNUM(repetitions))
		{
			for(int i=0;i<repetitions;i++)
			{
				result=result+string;
				if (i<(repetitions-1))
					result=result+" ";
			}
			return result;
		}
		else return string;
	}
	/**
	*Reverses a string.
	*/
	public String REVERSE(String string)
	{
		if(string==null)
			return "";
		String result="";
		for(int i=0;i<string.length();i++)
		{
			result=result+string.charAt(i)+result;
		}
		return result;
	}
	/**
	*Select a given word from a character expression.
	* @param string a string.
	* @param regex a regular expression.
	* @param position the position desired.
	* @return the given word from a character expression.
	*/
	public String SPLIT(String string, String regex, double position)
	{
		if(string==null)
			return "";
		else if(regex==null)
			return string;
		if(!ISNUM(position))
			return string;
		String[] result=string.split(regex);
		if(position<result.length)
			return result[(int)position];
		else
			return "";
	}
	/**
	*Removes trailing blanks from string.
	*/
	public String TRIM(String string)
	{
		if(string==null)
			return "";
		return string.trim();
	}
	/**
	*Converts all letters in an argument to uppercase.
	*/
	public String UPCASE(String string)
	{
		if(string==null)
			return "";
		return string.toUpperCase();
	}

	/*Random number generator functions*/

	/**
	* Return a random number from the beta distribution, takes the parameters from a string.
	* @param alpha the alpha parameter of the distribution.
	* @param beta the alpha parameter of the distribution.
	* @return Return a random number from the distribution beta
	*/
	public double RNDBETA(String alpha, String beta)
	{
		double val1 = TEXT2NUM(alpha);
		double val2 = TEXT2NUM(beta);
		return RNDBETA(val1,val2);
	}
	/**
	*Return a random number from the beta distribution.
	* @param alpha the alpha parameter of the distribution.
	* @param beta the alpha parameter of the distribution.
	* @return Return a random number from the distribution beta
	*/
	public double RNDBETA(double alpha, double beta)
	{
		if(!ISNUM(alpha) || !ISNUM(beta))
			return Double.NaN;
		try
		{
			return Beta.staticNextDouble(alpha, beta);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the binomial distribution, takes the parameters from a string.
	* @param n the alpha parameter of the distribution.
	* @param p the alpha parameter of the distribution.
	* @return Return a random number from the distribution binomial.
	*/
	public double RNDBINOMIAL(String n, String p)
	{
		double val1 = TEXT2NUM(n);
		double val2 = TEXT2NUM(p);
		return RNDBINOMIAL(val1,val2);
	}
	/**Return a random number from the binomial distribution.
	 * @param n the alpha parameter of the distribution.
	 * @param p the alpha parameter of the distribution.
	 * @return Return a random number from the distribution binomial.
	 */
	public double RNDBINOMIAL(double n, double p)
	{
		if(!ISNUM(n) || !ISNUM(p))
			return Double.NaN;
		try
		{
			return Binomial.staticNextInt((int) n, p);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the lorentz distribution, takes the parameters from a string.
	* @param mean the mean parameter of the distribution.
	* @param gamma the gamma parameter of the distribution.
	* @param cut the cut parameter of the distribution.
	* @return Return a random number from the lorentz distribution.
	*/
	public double RNDLORENTZ(String mean, String gamma, String cut)
	{
		double val1 = TEXT2NUM(mean);
		double val2 = TEXT2NUM(gamma);
		double val3 = TEXT2NUM(cut);
		return RNDLORENTZ(val1,val2,val3);
	}
	/**
	*Return a random number from the lorentz distribution.
	* @param mean the mean parameter of the distribution.
	* @param gamma the gamma parameter of the distribution.
	* @param cut the cut parameter of the distribution.
	* @return Return a random number from the lorentz distribution.
	*/
	public double RNDLORENTZ(double mean, double gamma, double cut)
	{
		if(!ISNUM(mean) || !ISNUM(gamma) || !ISNUM(cut))
			return Double.NaN;
		try
		{
			return BreitWigner.staticNextDouble(mean, gamma, cut);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the msbreitwigner distribution, takes the parameters from a string.
	* @param mean the mean parameter of the distribution.
	* @param gamma the gamma parameter of the distribution.
	* @param cut the cut parameter of the distribution.
	* @return Return a random number from the msbreitwigner distribution.
	*/
	public double RNDMSBREITWIGNER(String mean, String gamma, String cut)
	{
		double val1 = TEXT2NUM(mean);
		double val2 = TEXT2NUM(gamma);
		double val3 = TEXT2NUM(cut);
		return RNDMSBREITWIGNER(val1,val2,val3);
	}
	/**
	*Return a random number from the msbreitwigner distribution.
	* @param mean the mean parameter of the distribution.
	* @param gamma the gamma parameter of the distribution.
	* @param cut the cut parameter of the distribution.
	* @return Return a random number from the msbreitwigner distribution.
	*/
	public double RNDMSBREITWIGNER(double mean, double gamma, double cut)
	{
		if(!ISNUM(mean) || !ISNUM(gamma) || !ISNUM(cut))
			return Double.NaN;
		try
		{
			return BreitWignerMeanSquare.staticNextDouble(mean, gamma, cut);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the chisquare distribution, takes the parameters from a string.
	* @param freedom freedom degree of the distribution.
	* @return Return a random number from the chisquare distribution.
	*/
	public double RNDCHISQUARE(String freedom)
	{
		double val1 = TEXT2NUM(freedom);
		return RNDCHISQUARE(val1);
	}
	/**
	*Return a random number from the chisquare distribution.
	* @param freedom freedom degree of the distribution.
	* @return Return a random number from the chisquare distribution.
	*/
	public double RNDCHISQUARE(double freedom)
	{
		if(!ISNUM(freedom))
			return Double.NaN;
		try
		{
			return ChiSquare.staticNextDouble(freedom);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the exponential distribution, takes the parameters from a string.
	* @param lambda the lambda parameter of the distribution.
	* @return Return a random number from the exponential distribution.
	*/
	public double RNDEXPONENTIAL(String lambda)
	{
		double val1 = TEXT2NUM(lambda);
		return RNDEXPONENTIAL(val1);
	}
	/**
	*Return a random number from the exponential distribution.
	* @param lambda the lambda parameter of the distribution.
	* @return Return a random number from the exponential distribution.
	*/
	public double RNDEXPONENTIAL(double lambda)
	{
		if(!ISNUM(lambda))
			return Double.NaN;
		try
		{
			return Exponential.staticNextDouble(lambda);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the exponential power distribution, takes the parameters from a string.
	* @param tau the tau parameter of the distribution.
	* @return Return a random number from the exponential power distribution.
	*/
	public double RNDEXPONENTIALPOWER(String tau)
	{
		double val1 = TEXT2NUM(tau);
		return RNDEXPONENTIALPOWER(val1);
	}
	/**
	*Return a random number from the exponential power distribution.
	* @param tau the tau parameter of the distribution.
	* @return Return a random number from the exponential power distribution.
	*/
	public double RNDEXPONENTIALPOWER(double tau)
	{
		if(!ISNUM(tau))
			return Double.NaN;
		try
		{
			return ExponentialPower.staticNextDouble(tau);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the gamma distribution, takes the parameters from a string.
	* @param alpha the alpha parameter of the distribution.
	* @param lambda the lambda parameter of the distribution.
	* @return Return a random number from the gamma distribution
	*/
	public double RNDGAMMA(String alpha, String lambda)
	{
		double val1 = TEXT2NUM(alpha);
		double val2 = TEXT2NUM(lambda);
		return RNDGAMMA(val1, val2);
	}
	/**
	*Return a random number from the gamma distribution, takes the parameters from a string.
	* @param alpha the alpha parameter of the distribution.
	* @param lambda the lambda parameter of the distribution.
	* @return Return a random number from the gamma distribution
	*/
	public double RNDGAMMA(double alpha, double lambda)
	{
		if(!ISNUM(alpha) || !ISNUM(lambda))
			return Double.NaN;
		try
		{
			return Gamma.staticNextDouble(alpha, lambda);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the hyperbolic distribution, takes the parameters from a string.
	* @param alpha the alpha parameter of the distribution.
	* @param beta the lambda parameter of the distribution.
	* @return Return a random number from the hyperbolic distribution
	*/
	public double RNDHYPERBOLIC(String alpha, String beta)
	{
		double val1 = TEXT2NUM(alpha);
		double val2 = TEXT2NUM(beta);
		return RNDHYPERBOLIC(val1, val2);
	}
	/**
	*Return a random number from the hyperbolic distribution.
	* @param alpha the alpha parameter of the distribution.
	* @param beta the lambda parameter of the distribution.
	* @return Return a random number from the hyperbolic distribution
	*/
	public double RNDHYPERBOLIC(double alpha, double beta)
	{
		if(!ISNUM(alpha) || !ISNUM(beta))
			return Double.NaN;
		try
		{
			return Hyperbolic.staticNextDouble(alpha, beta);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the hyper geometric distribution, takes the parameters from a string.
	* @param N the N parameter of the distribution.
	* @param M the M parameter of the distribution.
	* @param n the n parameter of the distribution.
	* @return Return a random number from the hyper geometric distribution
	*/
	public double RNDHYPGEOMETRIC(String N, String M, String n)
	{
		double val1 = TEXT2NUM(N);
		double val2 = TEXT2NUM(M);
		double val3 = TEXT2NUM(n);
		return RNDHYPGEOMETRIC(val1,val2,val3);
	}
	/**
	*Return a random number from the hyper geometric distribution.
	* @param N the N parameter of the distribution.
	* @param M the M parameter of the distribution.
	* @param n the n parameter of the distribution.
	* @return Return a random number from the hyper geometric distribution
	*/
	public double RNDHYPGEOMETRIC(double N, double M, double n)
	{
		if(!ISNUM(N) || !ISNUM(M) || !ISNUM(n))
			return Double.NaN;
		try
		{
			return HyperGeometric.staticNextInt((int)N, (int)M, (int)n);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the logarithmic distribution, takes the parameters from a string.
	* @param p the p parameter of the distribution.
	* @return Return a random number from the logarithmic distribution.
	*/
	public double RNDLOG(String p)
	{
		double val1 = TEXT2NUM(p);
		return RNDLOG(val1);
	}
	/**
	*Return a random number from the logarithmic distribution.
	* @param p the p parameter of the distribution.
	* @return Return a random number from the logarithmic distribution.
	*/
	public double RNDLOG(double p)
	{
		if(!ISNUM(p))
			return Double.NaN;
		try
		{
			return Logarithmic.staticNextDouble(p);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the negative binomial distribution, takes the parameters from a string.
	* @param n the n parameter of the distribution.
	* @param p the p parameter of the distribution.
	* @return Return a random number from the negative binomial distribution.
	*/
	public double RNDNEGBINOMIAL(String n, String p)
	{
		double val1 = TEXT2NUM(n);
		double val2 = TEXT2NUM(p);
		return RNDNEGBINOMIAL(val1,val2);
	}
	/**
	*Return a random number from the negative binomial distribution.
	* @param n the n parameter of the distribution.
	* @param p the p parameter of the distribution.
	* @return Return a random number from the negative binomial distribution.
	*/
	public double RNDNEGBINOMIAL(double n, double p)
	{
		if(!ISNUM(n) || !ISNUM(p))
			return Double.NaN;
		try
		{
			return NegativeBinomial.staticNextInt((int) n, p);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the normal distribution, takes the parameters from a string.
	* @param mean the mean parameter of the distribution.
	* @param standard_deviation the standard deviation parameter of the distribution.
	* @return Return a random number from the normal distribution
	*/
	public double RNDNORMAL(String mean, String standard_deviation)
	{
		double val1 = TEXT2NUM(mean);
		double val2 = TEXT2NUM(standard_deviation);
		return RNDNORMAL(val1,val2);
	}
	/**
	*Return a random number from the normal distribution.
	* @param mean the mean parameter of the distribution.
	* @param standard_deviation the standard deviation parameter of the distribution.
	* @return Return a random number from the normal distribution
	*/
	public double RNDNORMAL(double mean, double standard_deviation)
	{
		if(!ISNUM(mean) || !ISNUM(standard_deviation))
			return Double.NaN;
		try
		{
			return Normal.staticNextDouble(mean, standard_deviation) ;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	* Return a random number from the normal distribution.
	* @return Return a random number from the normal distribution
	*/
	public double RNDNORMAL()
	{
		try
		{
			return Normal.staticNextDouble(0, 1) ;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the poisson distribution, takes the parameters from a string.
	* @param mean the mean parameter of the distribution.
	* @return Return a random number from the poisson distribution.
	*/
	public double RNDPOISSON(String mean)
	{
		double val1 = TEXT2NUM(mean);
		return RNDPOISSON(val1);
	}
	/**
	*Return a random number from the poisson distribution.
	* @param mean the mean parameter of the distribution.
	* @return Return a random number from the poisson distribution.
	*/
	public double RNDPOISSON(double mean)
	{
		if(!ISNUM(mean))
			return Double.NaN;
		try
		{
			return Poisson.staticNextInt(mean);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the student distribution, takes the parameters from a string.
	* @param freedom the freedom degrees parameter of the distribution.
	* @return Return a random number from the student distribution.
	*/
	public double RNDSTUDENT(String freedom)
	{
		double val1 = TEXT2NUM(freedom);
		return RNDSTUDENT(val1);
	}
	/**
	*Return a random number from the student distribution.
	* @param freedom the freedom degrees parameter of the distribution.
	* @return Return a random number from the student distribution.
	*/
	public double RNDSTUDENT(double freedom)
	{
		if(!ISNUM(freedom))
			return Double.NaN;
		try
		{
			return StudentT.staticNextDouble(freedom);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the uniform distribution, takes the parameters from a string.
	* @param from the "from" degrees parameter of the distribution.
	* @param to the "to" degrees parameter of the distribution.
	* @return Return a random number from the uniform distribution.
	*/
	public double RNDUNIFORM(String from, String to)
	{
		double val1 = TEXT2NUM(from);
		double val2 = TEXT2NUM(to);
		return RNDUNIFORM(val1,val2);
	}
	/**
	*Return a random number from the uniform distribution.
	* @param from the "from" degrees parameter of the distribution.
	* @param to the "to" degrees parameter of the distribution.
	* @return Return a random number from the uniform distribution.
	*/
	public double RNDUNIFORM(double from, double to)
	{
		if(!ISNUM(from) || !ISNUM(to))
			return Double.NaN;
		try
		{
			return Uniform.staticNextDoubleFromTo(from, to) ;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	* Return a random number from the uniform distribution.
	* @return Return a random number from the uniform distribution.
	*/
	public double RNDUNIFORM()
	{
		try
		{
			return Uniform.staticNextDoubleFromTo(0, 1) ;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Return a random number from the Von Mises distribution, takes the parameters from a string.
	* @param freedom the freedom degrees parameter of the distribution.
	* @return Return a random number from the Von Mises distribution.
	*/
	public double RNDVONMISES(String freedom)
	{
		double val1 = TEXT2NUM(freedom);
		return RNDVONMISES(val1);
	}
	/**
	*Return a random number from the Von Mises distribution.
	* @param freedom the freedom degrees parameter of the distribution.
	* @return Return a random number from the Von Mises distribution.
	*/
	public double RNDVONMISES(double freedom)
	{
		if(!ISNUM(freedom))
			return Double.NaN;
		try
		{
			return VonMises.staticNextDouble(freedom);
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	* Return a random number from the zeta distribution, takes the parameters from a string.
	* @param ro the ro parameter of the distribution.
	* @param pk the pk parameter of the distribution.
	* @return Return a random number from the zeta distribution.
	*/
	public double RNDZETA(String ro, String pk)
	{
		double val1 = TEXT2NUM(ro);
		double val2 = TEXT2NUM(pk);
		return RNDUNIFORM(val1,val2);
	}
	/**
	* Return a random number from the zeta distribution.
	* @param ro the ro parameter of the distribution.
	* @param pk the pk parameter of the distribution.
	* @return Return a random number from the zeta distribution.
	*/
	public double RNDZETA(double ro, double pk)
	{
		if(!ISNUM(ro) || !ISNUM(pk))
			return Double.NaN;
		try
		{
			return Zeta.staticNextInt(ro, pk) ;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	* Append a message to the message area
	* @param text The double text to append
	*/
	public void APPENDTOMSGAREA(double message)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				Document dc = MainGUI.MessageArea.getDocument();
				EditorKit ed = MainGUI.MessageArea.getEditorKit();
				CharArrayWriter cw = new CharArrayWriter();
				ed.write(cw, dc, 0, dc.getLength());
				String text_toinsert = cw.toString();
				text_toinsert = text_toinsert.replace("</body>","<div><br>"+String.valueOf(message)+"<br></div></body>");
				MainGUI.MessageArea.setText(text_toinsert);
				MainGUI.MessageArea.setCaretPosition(MainGUI.MessageArea.getDocument().getLength());
				MainGUI.MessageArea.repaint();
			}
			catch (Exception e){}
		}
	}
	/**
	* Append a message to the message area
	* @param text The boolean value to append
	*/
	public void APPENDTOMSGAREA(boolean message)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				Document dc = MainGUI.MessageArea.getDocument();
				EditorKit ed = MainGUI.MessageArea.getEditorKit();
				CharArrayWriter cw = new CharArrayWriter();
				ed.write(cw, dc, 0, dc.getLength());
				String text_toinsert = cw.toString();
				text_toinsert = text_toinsert.replace("</body>","<div><br>"+String.valueOf(message)+"<br></div></body>");
				MainGUI.MessageArea.setText(text_toinsert);
				MainGUI.MessageArea.setCaretPosition(MainGUI.MessageArea.getDocument().getLength());
				MainGUI.MessageArea.repaint();
			}
			catch (Exception e){}
		}
	}
	/**
	* Append a message to the message area
	* @param text The int text to append
	*/
	public void APPENDTOMSGAREA(int message)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				Document dc = MainGUI.MessageArea.getDocument();
				EditorKit ed = MainGUI.MessageArea.getEditorKit();
				CharArrayWriter cw = new CharArrayWriter();
				ed.write(cw, dc, 0, dc.getLength());
				String text_toinsert = cw.toString();
				text_toinsert = text_toinsert.replace("</body>","<div><br>"+String.valueOf(message)+"<br></div></body>");
				MainGUI.MessageArea.setText(text_toinsert);
				MainGUI.MessageArea.setCaretPosition(MainGUI.MessageArea.getDocument().getLength());
				MainGUI.MessageArea.repaint();
			}
			catch (Exception e){}
		}
	}
	/**
	* Append a message to the message area
	* @param text The text to append
	*/
	public void APPENDTOMSGAREA(String message)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				Document dc = MainGUI.MessageArea.getDocument();
				EditorKit ed = MainGUI.MessageArea.getEditorKit();
				CharArrayWriter cw = new CharArrayWriter();
				ed.write(cw, dc, 0, dc.getLength());
				String text_toinsert = cw.toString();
				text_toinsert = text_toinsert.replace("</body>","<div><br>"+message+"<br></div></body>");
				MainGUI.MessageArea.setText(text_toinsert);
				MainGUI.MessageArea.setCaretPosition(MainGUI.MessageArea.getDocument().getLength());
				MainGUI.MessageArea.repaint();
			}
			catch (Exception e){}
		}
	}
	/**
	*Write the message to the output area deleting what was there written
	*/
	public void WRITETOOUTAREA(String message)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				HTMLDocument dc = new HTMLDocument();
				EditorKit ed = MainGUI.OutputArea.getEditorKit();
				CharArrayWriter cw = new CharArrayWriter();
				ed.write(cw, dc, 0, dc.getLength());
				String text_toinsert = cw.toString();
				text_toinsert = text_toinsert.replace("</body>","<div>"+message+"</div></body>");
				MainGUI.OutputArea.setText(text_toinsert);
				MainGUI.OutputArea.setCaretPosition(MainGUI.OutputArea.getDocument().getLength());
				MainGUI.OutputArea.repaint();
			}
			catch (Exception e){}
		}
	}
	/**
	*Write the message to the message area deleting what was there written
	*/
	public void WRITETOMSGAREA(String message)
	{
		final String msgs=message;
		if (Keywords.MainGUI!=null)
		{
			try
			{
				SwingUtilities.invokeLater(new Runnable()
				{
				      public void run(){
							HTMLDocument doc=(HTMLDocument) MainGUI.MessageArea.getDocument();
							if (doc!=null)
							{
								HTMLDocument blank = new HTMLDocument();
								MainGUI.MessageArea.setDocument(blank);
								MainGUI.MessageArea.repaint();
								doc=new HTMLDocument();
							    try
							    {
							    	doc.insertString(0, msgs, null);
							    }
							    catch (Exception eee){}
							    MainGUI.MessageArea.setDocument(doc);
							    MainGUI.MessageArea.setCaretPosition(MainGUI.MessageArea.getDocument().getLength());
							    MainGUI.MessageArea.repaint();
							}
				      }
			    });
			}
			catch (Exception im){}
		}
	}
	/**
	* Append a message to the output area
	* @param text The boolean value to append
	*/
	public void APPENDTOOUTAREA(boolean message)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				Document dc = MainGUI.OutputArea.getDocument();
				EditorKit ed = MainGUI.OutputArea.getEditorKit();
				CharArrayWriter cw = new CharArrayWriter();
				ed.write(cw, dc, 0, dc.getLength());
				String text_toinsert = cw.toString();
				text_toinsert = text_toinsert.replace("</body>","<div><br>"+String.valueOf(message)+"<br></div></body>");
				MainGUI.OutputArea.setText(text_toinsert);
				MainGUI.OutputArea.setCaretPosition(MainGUI.OutputArea.getDocument().getLength());
				MainGUI.OutputArea.repaint();
			}
			catch (Exception e){}
		}
	}
	/**
	* Append a message to the output area
	* @param text The double text to append
	*/
	public void APPENDTOOUTAREA(double message)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				Document dc = MainGUI.OutputArea.getDocument();
				EditorKit ed = MainGUI.OutputArea.getEditorKit();
				CharArrayWriter cw = new CharArrayWriter();
				ed.write(cw, dc, 0, dc.getLength());
				String text_toinsert = cw.toString();
				text_toinsert = text_toinsert.replace("</body>","<div><br>"+String.valueOf(message)+"<br></div></body>");
				MainGUI.OutputArea.setText(text_toinsert);
				MainGUI.OutputArea.setCaretPosition(MainGUI.OutputArea.getDocument().getLength());
				MainGUI.OutputArea.repaint();
			}
			catch (Exception e){}
		}
	}
	/**
	* Append a message to the output area
	* @param text The int text to append
	*/
	public void APPENDTOOUTAREA(int message)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				Document dc = MainGUI.OutputArea.getDocument();
				EditorKit ed = MainGUI.OutputArea.getEditorKit();
				CharArrayWriter cw = new CharArrayWriter();
				ed.write(cw, dc, 0, dc.getLength());
				String text_toinsert = cw.toString();
				text_toinsert = text_toinsert.replace("</body>","<div><br>"+String.valueOf(message)+"<br></div></body>");
				MainGUI.OutputArea.setText(text_toinsert);
				MainGUI.OutputArea.setCaretPosition(MainGUI.OutputArea.getDocument().getLength());
				MainGUI.OutputArea.repaint();
			}
			catch (Exception e){}
		}
	}
	/**
	* Append a message to the output area
	* @param text The text to append
	*/
	public void APPENDTOOUTAREA(String message)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				Document dc = MainGUI.OutputArea.getDocument();
				EditorKit ed = MainGUI.OutputArea.getEditorKit();
				CharArrayWriter cw = new CharArrayWriter();
				ed.write(cw, dc, 0, dc.getLength());
				String text_toinsert = cw.toString();
				text_toinsert = text_toinsert.replace("</body>","<div><br>"+message+"<br></div></body>");
				MainGUI.OutputArea.setText(text_toinsert);
				MainGUI.OutputArea.setCaretPosition(MainGUI.OutputArea.getDocument().getLength());
				MainGUI.OutputArea.repaint();
			}
			catch (Exception e){}
		}
	}
	/**
	* Append a message to the editor area
	* @param text The text to append
	*/
	public void APPENDTOEDITOR(String message)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				Document doc = (MainGUI.EditorArea).getDocument();
				Document blank = new DefaultStyledDocument();
				MainGUI.EditorArea.setDocument(blank);
				try
				{
					doc.insertString(doc.getLength(), message, null);
				}
				catch (Exception e) {}
				MainGUI.EditorArea.setDocument(doc);
				MainGUI.EditorArea.setCaretPosition(MainGUI.EditorArea.getDocument().getLength());
				Keywords.modifiedscript=true;
			}
			catch (Exception eee) {}
		}
	}
	/**
	* Save the contents of the editor area
	* @param filename The name of the filename
	*/
	public boolean SAVEEDITOR(String filename)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				File save = new File(filename);
				File newFile;
				if (!save.getName().endsWith(Keywords.ScriptExtension))
					newFile = new File(save.getAbsolutePath()+ Keywords.ScriptExtension);
				else
					newFile = new File(save.getAbsolutePath());
				boolean exists = (newFile.exists());
				if (exists)
					return false;
				FileWriter writer = new FileWriter(newFile);
				PrintWriter outfile = new PrintWriter(writer);
				String text = MainGUI.EditorArea.getText().replace("\r","");
				outfile.println(text);
				outfile.close();
				return true;
			}
			catch (Exception e)
			{
				return false;
			}
		}
		return false;
	}
	/**
	* Save the contents of the editor area
	* @param filename The name of the filename
	*/
	public boolean SAVEEDITOR(String filename, boolean replace)
	{
		if (Keywords.MainGUI!=null)
		{
			try
			{
				File save = new File(filename);
				File newFile;
				if (!save.getName().endsWith(Keywords.ScriptExtension))
					newFile = new File(save.getAbsolutePath()+ Keywords.ScriptExtension);
				else
					newFile = new File(save.getAbsolutePath());
				boolean exists = (newFile.exists());
				if ((exists) && (!replace))
					return false;
				boolean resdel=(new File(filename)).delete();
				if (!resdel)
					return false;
				FileWriter writer = new FileWriter(newFile);
				PrintWriter outfile = new PrintWriter(writer);
				String text = MainGUI.EditorArea.getText().replace("\r","");
				outfile.println(text);
				outfile.close();
				return true;
			}
			catch (Exception e)
			{
				return false;
			}
		}
		return false;
	}
	/**
	* Clear the content of the editor area
	*/
	public void CLEANEDITOR()
	{
		if (Keywords.MainGUI!=null)
		{
			MainGUI.EditorArea.setText("");
			Keywords.modifiedscript=false;
		}
	}
	/**
	* Returns 0 if the date is equal to the datetext, interpreted using the given format
	* 1 if the date is greater than the datetext, -1 if it is less than the datetext
	* Returns NaN if the datetext is not convertible or the date is NaN
	*/
	public double COMPAREDATE(double date, String datetext, String format)
	{
		if (datetext.equals(""))
			return Double.NaN;
		if (Double.isNaN(date))
			return Double.NaN;
		double datecheck=Double.NaN;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			datecheck=(double)sdf.parse(datetext).getTime();
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
		if (Double.isNaN(datecheck))
			return Double.NaN;
		if (date==datecheck)
			return 0;
		if (date>datecheck)
			return 1;
		if (date<datecheck)
			return -1;
		return Double.NaN;
	}
	/**
	* Returns 0 if the date is equal to the datetext, interpreted using the given format and the language and country
	* 1 if the date is greater than the datetext, -1 if it is less than the datetext
	* Returns NaN if the datetext is not convertible or the date is NaN
	*/
	public double COMPAREDATE(double date, String datetext, String format, String language, String country)
	{
		if (datetext.equals(""))
			return Double.NaN;
		Locale lc=null;
		try
		{
			lc=new Locale(language, country.toUpperCase());

		}
		catch (Exception e)
		{
			return Double.NaN;
		}
		if (Double.isNaN(date))
			return Double.NaN;
		double datecheck=Double.NaN;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(format, lc);
			datecheck=(double)sdf.parse(datetext).getTime();
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
		if (Double.isNaN(datecheck))
			return Double.NaN;
		if (date==datecheck)
			return 0;
		if (date>datecheck)
			return 1;
		if (date<datecheck)
			return -1;
		return Double.NaN;
	}
	/**
	* Returns 0 if num1 is equal to num2
	* 1 if num1 is greater than num2, -1 if num1 is less than num2
	* Returns NaN if num1 or num2 are NaN
	*/
	public double COMPARENUM(double num1, double num2)
	{
		if (Double.isNaN(num1))
			return Double.NaN;
		if (Double.isNaN(num2))
			return Double.NaN;
		if (num1==num2)
			return 0;
		if (num1>num2)
			return 1;
		if (num1<num2)
			return -1;
		return Double.NaN;
	}
	/**
	* Returns 0 if text1 is equal to text2
	* 1 if text1 is greater than text2, -1 if text1 is less than text2
	* Returns NaN if text1 or text2 are missing
	* Note: this function compares two strings lexicographically.
	* The comparison is based on the Unicode value of each character in the strings.
	* The character sequence represented by this String object is compared lexicographically to
	* the character sequence represented by the argument string.
	* The result is a negative integer if this String object lexicographically precedes the argument string.
	* The result is a positive integer if this String object lexicographically follows the argument string.
	* The result is zero if the strings are equal.
	* Note, also, that if the two strings contains numbers, then the comparison is made on the
	* double values of the strings.
	*/
	public double COMPARETEXT(String text1, String text2)
	{
		if (text1.equals(""))
			return Double.NaN;
		if (text1.equals(""))
			return Double.NaN;
		double v1=Double.NaN;
		double v2=Double.NaN;
		try
		{
			v1=Double.parseDouble(text1);
		}
		catch (Exception en){}
		try
		{
			v2=Double.parseDouble(text2);
		}
		catch (Exception en){}
		if ((!Double.isNaN(v1)) && (!Double.isNaN(v2)))
		{
			if (v1==v2)
				return 0;
			if (v1>v2)
				return 1;
			if (v1<v2)
				return -1;
			return Double.NaN;
		}
		else
			return -1*(double)text1.compareTo(text2);
	}
	/**
	* Returns 0 if text1 is equal to text2
	* 1 if text1 is greater than text2, -1 if text1 is less than text2
	* Returns NaN if text1 or text2 are missing
	* Note: this function compares two strings lexicographically.
	* The comparison is based on the Unicode value of each character in the strings.
	* The character sequence represented by this String object is compared lexicographically to
	* the character sequence represented by the argument string.
	* The result is a negative integer if this String object lexicographically precedes the argument string.
	* The result is a positive integer if this String object lexicographically follows the argument string.
	* The result is zero if the strings are equal.
	* Note, also, that if the two strings contains numbers, then the comparison is made on the
	* double values of the strings.
	* This function considers the two string indipendent from the case
	*/
	public double COMPARETEXTNOCASE(String text1, String text2)
	{
		if (text1.equals(""))
			return Double.NaN;
		if (text1.equals(""))
			return Double.NaN;
		double v1=Double.NaN;
		double v2=Double.NaN;
		try
		{
			v1=Double.parseDouble(text1);
		}
		catch (Exception en){}
		try
		{
			v2=Double.parseDouble(text2);
		}
		catch (Exception en){}
		if ((!Double.isNaN(v1)) && (!Double.isNaN(v2)))
		{
			if (v1==v2)
				return 0;
			if (v1>v2)
				return 1;
			if (v1<v2)
				return -1;
			return Double.NaN;
		}
		else
			return -1*(double)(text1.toLowerCase()).compareTo(text2.toLowerCase());
	}
	/**
	*Returns true if a (as num) and b are equal
	*/
	public boolean EQ (String a, double b)
	{
		double av=Double.NaN;
		try
		{
			av=Double.parseDouble(a);
		}
		catch (Exception en){}
		if (!Double.isNaN(av))
		{
			if (av==b)
				return true;
		}
		return false;
	}
	/**
	*Returns true if a and b (as num) are equal
	*/
	public boolean EQ (double a, String b)
	{
		double bv=Double.NaN;
		try
		{
			bv=Double.parseDouble(b);
		}
		catch (Exception en){}
		if (!Double.isNaN(bv))
		{
			if (bv==a)
				return true;
		}
		return false;
	}
	/**
	*Returns true if a and b are equal
	*/
	public boolean EQ (String a, String b)
	{
		if (a.equals(b))
			return true;
		return false;
	}
	/**
	*Returns true if a and b are equal
	*/
	public boolean EQ (double a, double b)
	{
		if (a==b)
			return true;
		return false;
	}
	/**
	*Returns true if a (as num) is greater than b
	*/
	public boolean GT (String a, double b)
	{
		double av=Double.NaN;
		try
		{
			av=Double.parseDouble(a);
		}
		catch (Exception en){}
		if (!Double.isNaN(av))
		{
			if (av>b)
				return true;
		}
		return false;
	}
	/**
	*Returns true if a is greater than b (as num)
	*/
	public boolean GT (double a, String b)
	{
		double bv=Double.NaN;
		try
		{
			bv=Double.parseDouble(b);
		}
		catch (Exception en){}
		if (!Double.isNaN(bv))
		{
			if (a>bv)
				return true;
		}
		return false;
	}
	/**
	*Returns true if a is greater than b
	*/
	public boolean GT (String a, String b)
	{
		if (a.compareTo(b)>0)
		{
			return true;
		}
		return false;
	}
	/**
	*Returns true if a is greater than b
	*/
	public boolean GT (double a, double b)
	{
		if (a>b)
			return true;
		return false;
	}
	/**
	*Returns true if a (as num) is less than b
	*/
	public boolean LT (String a, double b)
	{
		double av=Double.NaN;
		try
		{
			av=Double.parseDouble(a);
		}
		catch (Exception en){}
		if (!Double.isNaN(av))
		{
			if (av<b)
				return true;
		}
		return false;
	}
	/**
	*Returns true if a is less than b (as num)
	*/
	public boolean LT (double a, String b)
	{
		double bv=Double.NaN;
		try
		{
			bv=Double.parseDouble(b);
		}
		catch (Exception en){}
		if (!Double.isNaN(bv))
		{
			if (a<bv)
				return true;
		}
		return false;
	}
	/**
	*Returns true if a is less than b
	*/
	public boolean LT (String a, String b)
	{
		if (a.compareTo(b)<0)
		{
			return true;
		}
		return false;
	}
	/**
	*Returns true if a is less than b
	*/
	public boolean LT (double a, double b)
	{
		if (a<b)
			return true;
		return false;
	}
	/**
	* Save the text in the file specified
	* @param filename The name of the fil
	* @param text The text to save
	*/
	public boolean SAVETEXTINFILE(String text, String filename)
	{
		try
		{
			File save = new File(filename);
			boolean exists = (save.exists());
			if (exists)
				return false;
			FileWriter writer = new FileWriter(save);
			PrintWriter outfile = new PrintWriter(writer);
			try
			{
				text = text.replace("%n","\n");
			}
			catch (Exception ee) {}
			outfile.println(text);
			outfile.close();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	* Save the text in the file specified
	* @param filename The name of the fil
	* @param text The text to save
	*/
	public boolean SAVETEXTINSCRIPTFILE(String text, String filename)
	{
		try
		{
			File save = new File(filename);
			File newFile;
			if (!save.getName().endsWith(Keywords.ScriptExtension))
				newFile = new File(save.getAbsolutePath()+ Keywords.ScriptExtension);
			else
				newFile = new File(save.getAbsolutePath());
			boolean exists = (newFile.exists());
			if (exists)
				return false;
			FileWriter writer = new FileWriter(newFile);
			PrintWriter outfile = new PrintWriter(writer);
			try
			{
				text = text.replace("%n","\n");
			}
			catch (Exception ee) {}
			outfile.println(text);
			outfile.close();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Adds the current value to the frequency counter
	*/
	public boolean ADDFREQ(FreqCounter fc, String tval)
	{
		try
		{
			if (fc!=null)
			{
				fc.addval(tval);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Adds the current value to the frequency counter, by considering its frequency
	*/
	public boolean ADDFREQ(FreqCounter fc, String tval, double freq)
	{
		try
		{
			if (fc!=null)
			{
				fc.addval(tval, freq);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Sets the current value to the frequency counter, by considering its frequency
	*/
	public boolean SETFREQ(FreqCounter fc, String tval, double freq)
	{
		try
		{
			if (fc!=null)
			{
				fc.setval(tval, freq);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Adds the current array of values to the frequency counter
	*/
	public boolean ADDFREQ(FreqCounter fc, String[] tval)
	{
		try
		{
			if (fc!=null)
			{
				fc.addval(tval);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Adds the current array of values to the frequency counter, by considering its frequency
	*/
	public boolean ADDFREQ(FreqCounter fc, String[] tval, double freq)
	{
		try
		{
			if (fc!=null)
			{
				fc.addval(tval, freq);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Set the current array of values to the frequency counter, by considering its frequency
	*/
	public boolean SETFREQ(FreqCounter fc, String[] tval, double freq)
	{
		try
		{
			if (fc!=null)
			{
				fc.setval(tval, freq);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Adds the current double value to the frequency counter
	*/
	public boolean ADDFREQ(FreqCounter fc, double tval)
	{
		try
		{
			if (fc!=null)
			{
				fc.addval(tval);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Adds the current double value to the frequency counter, by considering its frequency
	*/
	public boolean ADDFREQ(FreqCounter fc, double tval, double freq)
	{
		try
		{
			if (fc!=null)
			{
				fc.addval(tval, freq);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Set the current double value to the frequency counter, by considering its frequency
	*/
	public boolean SETFREQ(FreqCounter fc, double tval, double freq)
	{
		try
		{
			if (fc!=null)
			{
				fc.setval(tval, freq);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Adds the current array of double values to the frequency counter
	*/
	public boolean ADDFREQ(FreqCounter fc, double[] tval)
	{
		try
		{
			if (fc!=null)
			{
				fc.addval(tval);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Adds the current array of double values to the frequency counter, by considering its frequency
	*/
	public boolean ADDFREQ(FreqCounter fc, double[] tval, double freq)
	{
		try
		{
			if (fc!=null)
			{
				fc.addval(tval, freq);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Set the current array of double values to the frequency counter, by considering its frequency
	*/
	public boolean SETFREQ(FreqCounter fc, double[] tval, double freq)
	{
		try
		{
			if (fc!=null)
			{
				fc.setval(tval, freq);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Return the number of distinct values for the current frequency counter
	*/
	public int NUMDISTINCTVAL(FreqCounter fc)
	{
		try
		{
			if (fc!=null)
			{
				return fc.getdiffvalues();
			}
			else
				return 0;
		}
		catch (Exception e)
		{
			return 0;
		}
	}
	/**
	*Clear the frequency counter
	*/
	public boolean CLEARFC(FreqCounter fc)
	{
		try
		{
			if (fc!=null)
			{
				fc.clearfc();
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Returns the number of distinct values for the current frequency counter referring to the value of the String
	*/
	public double GETFREQFOR(FreqCounter fc, String tval)
	{
		try
		{
			if (fc!=null)
			{
				return fc.getfreqfor(tval);
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the number of distinct values for the current frequency counter referring to the value of the array of String
	*/
	public double GETFREQFOR(FreqCounter fc, String[] tval)
	{
		try
		{
			if (fc!=null)
			{
				return fc.getfreqfor(tval);
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the number of distinct values for the current frequency counter referring to the value of the double
	*/
	public double GETFREQFOR(FreqCounter fc, double tval)
	{
		try
		{
			if (fc!=null)
			{
				return fc.getfreqfor(tval);
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the number of distinct values for the current frequency counter referring to the value of the array of doubles
	*/
	public double GETFREQFOR(FreqCounter fc, double[] tval)
	{
		try
		{
			if (fc!=null)
			{
				return fc.getfreqfor(tval);
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the value of the variable for the current frequency counter referring to the position indicated by the integer
	*/
	public String GETNAMEFOR(FreqCounter fc, int rif)
	{
		try
		{
			if (fc!=null)
			{
				return fc.getnamefor(rif);
			}
			else
				return "";
		}
		catch (Exception e)
		{
			return "";
		}
	}
	/**
	*Returns the value of the variables for the current frequency counter referring to the position indicated by the integer
	*/
	public String[] GETNAMESFOR(FreqCounter fc, int rif)
	{
		try
		{
			if (fc!=null)
			{
				return fc.getnamesfor(rif);
			}
			else
				return null;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	/**
	*Returns the frequencies associated to the value of the variables for the current frequency counter referring to the position indicated by the integer
	*/
	public double GETFREQAT(FreqCounter fc, int rif)
	{
		try
		{
			if (fc!=null)
			{
				return fc.getfreqat(rif);
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Adds the current double value to the current EvalStat object
	*/
	public boolean ADDVAL(EvalStat ev, double tval)
	{
		try
		{
			if (ev!=null)
			{
				ev.addval(tval);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Adds the current double value to the current EvalStat object by considering also the weight
	*/
	public boolean ADDVAL(EvalStat ev, double tval, double weight)
	{
		try
		{
			if (ev!=null)
			{
				ev.addval(tval, weight);
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Returns the means of the current EvalStat Object
	*/
	public double GETMEAN(EvalStat ev)
	{
		try
		{
			if (ev!=null)
			{
				return ev.getmean();
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the minimum of the current EvalStat Object
	*/
	public double GETMIN(EvalStat ev)
	{
		try
		{
			if (ev!=null)
			{
				return ev.getmin();
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the maximum of the current EvalStat Object
	*/
	public double GETMAX(EvalStat ev)
	{
		try
		{
			if (ev!=null)
			{
				return ev.getmax();
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the number of values inserted in the current EvalStat Object
	*/
	public double GETNUM(EvalStat ev)
	{
		try
		{
			if (ev!=null)
			{
				return ev.getnum();
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the sum of values inserted in the current EvalStat Object
	*/
	public double GETSUM(EvalStat ev)
	{
		try
		{
			if (ev!=null)
			{
				return ev.getsum();
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the sum of square of the values inserted in the current EvalStat Object
	*/
	public double GETSSQ(EvalStat ev)
	{
		try
		{
			if (ev!=null)
			{
				return ev.getssq();
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the sample standard deviation of the values inserted in the current EvalStat Object
	*/
	public double GETSAMPLESTD(EvalStat ev)
	{
		try
		{
			if (ev!=null)
			{
				return ev.getsamplestd();
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the corrected standard deviation of the values inserted in the current EvalStat Object
	*/
	public double GETSTD(EvalStat ev)
	{
		try
		{
			if (ev!=null)
			{
				return ev.getstd();
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the sample variance of the values inserted in the current EvalStat Object
	*/
	public double GETSAMPLEVAR(EvalStat ev)
	{
		try
		{
			if (ev!=null)
			{
				return ev.getsamplevar();
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the corrected variance of the values inserted in the current EvalStat Object
	*/
	public double GETVAR(EvalStat ev)
	{
		try
		{
			if (ev!=null)
			{
				return ev.getvar();
			}
			else
				return Double.NaN;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the corrected variance of the values inserted in the current EvalStat Object
	*/
	public boolean CLEARES(EvalStat ev)
	{
		try
		{
			if (ev!=null)
			{
				ev.cleares();
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Returns true if a (as num) is less or equals to b
	*/
	public boolean LE (String a, double b)
	{
		double av=Double.NaN;
		try
		{
			av=Double.parseDouble(a);
		}
		catch (Exception en){}
		if (!Double.isNaN(av))
		{
			if (av<=b)
				return true;
		}
		return false;
	}
	/**
	*Returns true if a is less or equals to b (as num)
	*/
	public boolean LE (double a, String b)
	{
		double bv=Double.NaN;
		try
		{
			bv=Double.parseDouble(b);
		}
		catch (Exception en){}
		if (!Double.isNaN(bv))
		{
			if (a<=bv)
				return true;
		}
		return false;
	}
	/**
	*Returns true if a is less or equals to b
	*/
	public boolean LE (String a, String b)
	{
		if (a.compareTo(b)<=0)
		{
			return true;
		}
		return false;
	}
	/**
	*Returns true if a is less or equals to b
	*/
	public boolean LE (double a, double b)
	{
		if (a<=b)
			return true;
		return false;
	}
	/**
	*Returns true if a (as num) is greater or equals to b
	*/
	public boolean GE (String a, double b)
	{
		double av=Double.NaN;
		try
		{
			av=Double.parseDouble(a);
		}
		catch (Exception en){}
		if (!Double.isNaN(av))
		{
			if (av>=b)
				return true;
		}
		return false;
	}
	/**
	*Returns true if a is greater or equals to b (as num)
	*/
	public boolean GE (double a, String b)
	{
		double bv=Double.NaN;
		try
		{
			bv=Double.parseDouble(b);
		}
		catch (Exception en){}
		if (!Double.isNaN(bv))
		{
			if (a>=bv)
				return true;
		}
		return false;
	}
	/**
	*Returns true if a is greater or equals to b
	*/
	public boolean GE (String a, String b)
	{
		if (a.compareTo(b)>=0)
		{
			return true;
		}
		return false;
	}
	/**
	*Returns true if a is greater or equals to b
	*/
	public boolean GE (double a, double b)
	{
		if (a>=b)
			return true;
		return false;
	}
	/**
	*Returns the closest double to the valuea ccording to the number of decimal represented with the number dec
	*/
	public double ROUNDDEC(double value, int dec)
	{
		try
		{
			for (int i=0; i<dec; i++)
			{
				value=value*10;
			}
			value=Math.rint(value);
			for (int i=0; i<dec; i++)
			{
				value=value/10;
			}
			return value;
		}
		catch (Exception e)
		{
			return Double.NaN;
		}
	}
	/**
	*Returns the criptyed value of str according to the pass
	*/
	public String CRYPT(String str, String pass)
	{
		try
		{
			BasicTextEncryptor bte=new BasicTextEncryptor();
			bte.setPassword(pass);
			return bte.encrypt(str);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	/**
	*Returns the decriptyed value of str according to the key
	*/
	public String DECRYPT(String str, String pass)
	{
		try
		{
			BasicTextEncryptor bte=new BasicTextEncryptor();
			bte.setPassword(pass);
			return bte.decrypt(str);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	/**
	*Returns the array that contains in its first element the regression coefficient and in the second the intercept for the given vector of values
	*/
	public double[] GETLINREGFROMY(Vector<Double> values)
	{
		double[] resc=new double[2];
		try
		{
			double mv=0;
			double mx=0;
			double num=0;
			for (int i=0; i<values.size(); i++)
			{
				if (ISNUM(values.get(i)))
				{
					mv=mv+values.get(i);
					mx=mx+i+1;
					num=num+1;
				}
			}
			mv=mv/num;
			mx=mx/num;
			double sxy=0;
			double sx=0;
			for (int i=0; i<values.size(); i++)
			{
				if (ISNUM(values.get(i)))
				{
					sxy=sxy+(values.get(i)-mv)*(i+1-mx);
					sx=sx+(Math.pow((i+1-mx),2));
				}
			}
			resc[0]=sxy/sx;
			resc[1]=mv-resc[0]*mx;
			return resc;
		}
		catch (Exception er)
		{
			return resc;
		}
	}
	/**
	*Returns the array that contains in its first element the regression coefficient and in the second the intercept for the given vector of an array of values
	*Note that the first element of the array is the x value and the second the y value
	*/
	public double[] GETLINREGFROMXY(Vector<double[]> values)
	{
		double[] resc=new double[2];
		try
		{
			double mv=0;
			double mx=0;
			double num=0;
			for (int i=0; i<values.size(); i++)
			{
				double[] val=values.get(i);
				if ((ISNUM(val[0])) && (ISNUM(val[1])))
				{
					mv=mv+val[1];
					mx=mx+val[0];
					num=num+1;
				}
			}
			mv=mv/num;
			mx=mx/num;
			double sxy=0;
			double sx=0;
			for (int i=0; i<values.size(); i++)
			{
				double[] val=values.get(i);
				if ((ISNUM(val[0])) && (ISNUM(val[1])))
				{
					sxy=sxy+(val[1]-mv)*(val[0]-mx);
					sx=sx+(Math.pow((val[0]-mx),2));
				}
			}
			resc[0]=sxy/sx;
			resc[1]=mv-resc[0]*mx;
			return resc;
		}
		catch (Exception er)
		{
			return resc;
		}
	}
	/**
	*Returns true if the last step went in error
	*/
	public boolean GETLASTSTEPERROR()
	{
		return Keywords.laststepstate;
	}
	/**
	*Returns the last error message
	*/
	public String GETLASTSTEPMESSAGE()
	{
		return MessagesReplacer.replaceMessages(Keywords.laststepmessage);
	}
	/**
	*Insert an array of double into the CommonDSTable
	*/
	public boolean NUMARRAY2CDSTABLE(String namedst, double[] val)
	{
		try
		{
			if (Keywords.CommonDSTable.get(namedst.toUpperCase())==null)
			{
				Vector<MemoryValue[]> mvt=new Vector<MemoryValue[]>();
				Keywords.CommonDSTable.put(namedst.toUpperCase(), mvt);
			}
			Vector<MemoryValue[]> tmvt=Keywords.CommonDSTable.get(namedst.toUpperCase());
			int maxsize=val.length;
			if (tmvt.size()>0)
			{
				MemoryValue[] tt=tmvt.get(0);
				maxsize=tt.length;
			}
			if (maxsize==val.length)
			{
				MemoryValue[] memvals= new MemoryValue[val.length];
				for(int i=0;i<memvals.length;i++)
				{
					memvals[i]=new MemoryValue(val[i]);
				}
				tmvt.add(memvals);
				Keywords.CommonDSTable.put(namedst.toUpperCase(), tmvt);
				return true;
			}
			return false;
		}
		catch (Exception enac)
		{
			return false;
		}
	}
	/**
	*Insert an array of string into the CommonDSTable
	*/
	public boolean TEXTARRAY2CDSTABLE(String namedst, String[] val)
	{
		try
		{
			if (Keywords.CommonDSTable.get(namedst.toUpperCase())==null)
			{
				Vector<MemoryValue[]> mvt=new Vector<MemoryValue[]>();
				Keywords.CommonDSTable.put(namedst.toUpperCase(), mvt);
			}
			Vector<MemoryValue[]> tmvt=Keywords.CommonDSTable.get(namedst.toUpperCase());
			int maxsize=val.length;
			if (tmvt.size()>0)
			{
				MemoryValue[] tt=tmvt.get(0);
				maxsize=tt.length;
			}
			if (maxsize==val.length)
			{
				MemoryValue[] memvals= new MemoryValue[val.length];
				for(int i=0;i<memvals.length;i++)
				{
					memvals[i]=new MemoryValue(val[i]);
				}
				tmvt.add(memvals);
				Keywords.CommonDSTable.put(namedst.toUpperCase(), tmvt);
				return true;
			}
			return false;
		}
		catch (Exception enac)
		{
			return false;
		}
	}
	/**
	*Delete a CommonDSTable
	*/
	public boolean DELCDSTABLE(String namedst)
	{
		if (Keywords.CommonDSTable.get(namedst.toUpperCase())!=null)
		{
			Keywords.CommonDSTable.remove(namedst.toUpperCase());
			return true;
		}
		return false;
	}
	/**
	*Returns the number of records inside a CommonDSTable
	*/
	public int NUMRECCDSTABLE(String namedst)
	{
		if (Keywords.CommonDSTable.get(namedst.toUpperCase())!=null)
		{
			Vector<MemoryValue[]> tmvt=Keywords.CommonDSTable.get(namedst.toUpperCase());
			return tmvt.size();
		}
		return -1;
	}
	/**
	*Returns the number of variables of the first record inside a CommonDSTable
	*/
	public int NUMVARCDSTABLE(String namedst)
	{
		if (Keywords.CommonDSTable.get(namedst.toUpperCase())!=null)
		{
			Vector<MemoryValue[]> tmvt=Keywords.CommonDSTable.get(namedst.toUpperCase());
			if (tmvt.size()>0)
			{
				MemoryValue[] memvals=tmvt.get(0);
				return memvals.length;
			}
			return 0;
		}
		return -1;
	}
	/**
	*Returns all the content of a CommonDSTable as an array of String
	*/
	public String[][] GETCDSTABLEASTEXT(String namedst)
	{
		if (Keywords.CommonDSTable.get(namedst.toUpperCase())!=null)
		{
			Vector<MemoryValue[]> tmvt=Keywords.CommonDSTable.get(namedst.toUpperCase());
			if (tmvt.size()>0)
			{
				MemoryValue[] memvals=tmvt.get(0);
				String[][] retarray=new String[tmvt.size()][memvals.length];
				for (int i=0; i<tmvt.size(); i++)
				{
					MemoryValue[] tmemvals=tmvt.get(i);
					for (int j=0; j<tmemvals.length; j++)
					{
						retarray[i][j]=tmemvals[j].toString();
					}
				}
				return retarray;
			}
			return null;
		}
		return null;
	}
	/**
	*Returns all the content of a CommonDSTable as an array of String
	*/
	public double[][] GETCDSTABLEASNUM(String namedst)
	{
		if (Keywords.CommonDSTable.get(namedst.toUpperCase())!=null)
		{
			Vector<MemoryValue[]> tmvt=Keywords.CommonDSTable.get(namedst.toUpperCase());
			if (tmvt.size()>0)
			{
				MemoryValue[] memvals=tmvt.get(0);
				double[][] retarray=new double[tmvt.size()][memvals.length];
				for (int i=0; i<tmvt.size(); i++)
				{
					MemoryValue[] tmemvals=tmvt.get(i);
					for (int j=0; j<tmemvals.length; j++)
					{
						retarray[i][j]=Double.parseDouble(tmemvals[j].toString());
					}
				}
				return retarray;
			}
			return null;
		}
		return null;
	}
	/**
	*Return a matrix2d
	*/
	public DoubleMatrix2D GETMATRIX2D(double[][] values)
	{
		DoubleMatrix2D mat=null;
		try
		{
			mat=DoubleFactory2D.dense.make(values.length, values[0].length);
			for (int i=0; i<values.length; i++)
			{
				for (int j=0; j<values[0].length; j++)
				{
					mat.set(i, j, values[i][j]);
				}
			}
		}
		catch (Exception emat) {}
		return mat;
	}
	/**
	*Return a matrix1d
	*/
	public DoubleMatrix1D GETMATRIX1D(double[] values)
	{
		DoubleMatrix1D mat=null;
		try
		{
			mat=DoubleFactory1D.dense.make(values.length);
			for (int i=0; i<values.length; i++)
			{
				mat.set(i, values[i]);
			}
		}
		catch (Exception emat) {}
		return mat;
	}
	/**
	*Return a matrix3d
	*/
	public DoubleMatrix3D GETMATRIX3D(double[][][] values)
	{
		DoubleMatrix3D mat=null;
		try
		{
			mat=DoubleFactory3D.dense.make(values.length, values[0].length, values[0][0].length);
			for (int i=0; i<values.length; i++)
			{
				for (int j=0; j<values[0].length; j++)
				{
					for (int k=0; k<values[0][0].length; k++)
					{
						mat.set(i, j, k, values[i][j][k]);
					}
				}
			}
		}
		catch (Exception emat) {}
		return mat;
	}
	/**
	*Return a 1d double array from a DoubleMatrix1D
	*/
	public double[] GETDOUBLE1D(DoubleMatrix1D mat)
	{
		try
		{
			return mat.toArray();
		}
		catch (Exception emat)
		{
			return null;
		}
	}
	/**
	*Return a 2d double array from a DoubleMatrix2D
	*/
	public double[][] GETDOUBLE2D(DoubleMatrix2D mat)
	{
		try
		{
			return mat.toArray();
		}
		catch (Exception emat)
		{
			return null;
		}
	}
	/**
	*Return a 3d double array from a DoubleMatrix3D
	*/
	public double[][][] GETDOUBLE3D(DoubleMatrix3D mat)
	{
		try
		{
			return mat.toArray();
		}
		catch (Exception emat)
		{
			return null;
		}
	}
}


