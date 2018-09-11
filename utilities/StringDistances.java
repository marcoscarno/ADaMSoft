/**
* Copyright (c) 2016 MS
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

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.Hashtable;
import java.util.LinkedList;

/**
* This class contains different distance functions for strings
* @author marco.scarno@gmail.com
* @date 29/03/2016
*/
public class StringDistances
{
    private char QGRAMSTARTPADDING = '#';
    private char QGRAMENDPADDING = '#';
	private double CHAR_EXACT_MATCH_SCORE = +5;
	private String delimiters = "\r\n\t \u00A0";
	private double CHAR_MISMATCH_MATCH_SCORE = -3;
	private double CHAR_APPROX_MATCH_SCORE = +3;
	private int SOUNDEXLENGTH = 6;
	private Vector<Vector<Character>> approx=new Vector<Vector<Character>>();
	double distvalue;
	double retdist=0;
	String[] parts;
	/**
	*Sets the value used for the cost function
	*/
	private void initApprox()
	{
		Vector<Character> a=new Vector<Character>();
		a.add(new Character('d'));
		a.add(new Character('t'));
		approx.add(a);
		Vector<Character> b=new Vector<Character>();
		b.add(new Character('g'));
		b.add(new Character('j'));
		approx.add(b);
		Vector<Character> c=new Vector<Character>();
		c.add(new Character('l'));
		c.add(new Character('r'));
		approx.add(c);
		Vector<Character> d=new Vector<Character>();
		d.add(new Character('m'));
		d.add(new Character('n'));
		approx.add(d);
		Vector<Character> e=new Vector<Character>();
		e.add(new Character('b'));
		e.add(new Character('p'));
		e.add(new Character('v'));
		approx.add(e);
		Vector<Character> f=new Vector<Character>();
		f.add(new Character('a'));
		f.add(new Character('e'));
		f.add(new Character('i'));
		f.add(new Character('o'));
		f.add(new Character('u'));
		approx.add(f);
		Vector<Character> g=new Vector<Character>();
		g.add(new Character(','));
		g.add(new Character('.'));
		approx.add(g);
	}
	/**
	*Returns the distance between the strings and by receiving the information on the distance to evaluate
	*/
	public double getDistanceForParts(String string1, String string2, int type)
	{
		retdist=0;
		parts=(string2.replaceAll("\\s+", " ")).split(" ");
		for (int i=0; i<parts.length; i++)
		{
			if (type==0) retdist+=ChapmanLengthDeviation(string1, parts[i]);
			else if (type==1) retdist+=CosineSimilarity(string1, parts[i]);
			else if (type==2) retdist+=MatchingCoefficient(string1, parts[i]);
			else if (type==3) retdist+=OverlapCoefficient(string1, parts[i]);
			else if (type==4) retdist+=DiceSimilarity(string1, parts[i]);
			else if (type==5) retdist+=JaroWinkler(string1, parts[i]);
			else if (type==6) retdist+=JaccardSimilarity(string1, parts[i]);
			else if (type==7) retdist+=QGramsDistance(string1, parts[i]);
			else if (type==8) retdist+=Levenshtein(string1, parts[i]);
			else if (type==9) retdist+=BlockDistance(string1, parts[i]);
			else if (type==10) retdist+=MongeElkan(string1, parts[i]);
			else if (type==11) retdist+=ChapmanOrderedNameCompoundSimilarity(string1, parts[i]);
			else if (type==12) retdist+=Jaro(string1, parts[i]);
			else if (type==13) retdist+=EuclideanDistance(string1, parts[i]);
			else if (type==14) retdist+=SoundEx(string1, parts[i]);
			else if (type==15) retdist+=NeedlemanWunch(string1, parts[i]);
		}
		return retdist/parts.length;
	}
	/**
	*Returns the distance between the strings and by receiving the information on the distance to evaluate
	*/
	public double getDistance(String string1, String string2, int type)
	{
		if (type==0) return ChapmanLengthDeviation(string1, string2);
		else if (type==1) return CosineSimilarity(string1, string2);
		else if (type==2) return MatchingCoefficient(string1, string2);
		else if (type==3) return OverlapCoefficient(string1, string2);
		else if (type==4) return DiceSimilarity(string1, string2);
		else if (type==5) return JaroWinkler(string1, string2);
		else if (type==6) return JaccardSimilarity(string1, string2);
		else if (type==7) return QGramsDistance(string1, string2);
		else if (type==8) return Levenshtein(string1, string2);
		else if (type==9) return BlockDistance(string1, string2);
		else if (type==10) return MongeElkan(string1, string2);
		else if (type==11) return ChapmanOrderedNameCompoundSimilarity(string1, string2);
		else if (type==12) return Jaro(string1, string2);
		else if (type==13) return EuclideanDistance(string1, string2);
		else if (type==14) return SoundEx(string1, string2);
		else if (type==15) return NeedlemanWunch(string1, string2);
		else return Double.NaN;
	}
	/**
	*Return the name of the distance
	*/
	public String getDistanceName(int type)
	{
		if (type==0) return "Chapman Length Deviation";
		else if (type==1) return "Cosine Similarity";
		else if (type==2) return "Matching Coefficient";
		else if (type==3) return "Overlap Coefficient";
		else if (type==4) return "Dice Similarity";
		else if (type==5) return "Jaro Winkler";
		else if (type==6) return "Jaccard Similarity";
		else if (type==7) return "QGrams Distance";
		else if (type==8) return "Levenshtein";
		else if (type==9) return "Block Distance";
		else if (type==10) return "Monge Elkan";
		else if (type==11) return "Chapman Ordered Name Compound Similarity";
		else if (type==12) return "Jaro";
		else if (type==13) return "Euclidean Distance";
		else if (type==14) return "SoundEx";
		else if (type==15) return "Needleman Wunch";
		else return "";
	}
	/**
	*ChapmanMeanLength implements a simple mean length metric, i.e. strings of the similar lengths are more similar regardless of the content.
	*/
	public double ChapmanLengthDeviation(String string1, String string2)
	{
 		if (string1.length() >= string2.length())
 		{
			return (double) ((float)string2.length() / (float)string1.length());
		}
		else
		{
			return (double) ((float)string1.length() / (float)string2.length());
		}
    }
    /**
    *Cosine Similarity algorithm providing a similarity measure between two strings from the angular divergence within term based vector space
    */
	public double CosineSimilarity(String string1, String string2)
	{
		ArrayList<String> str1Tokens = TokeniserWhitespace(string1);
		ArrayList<String> str2Tokens = TokeniserWhitespace(string2);
        Set<String> allTokens = new HashSet<String>();
        allTokens.addAll(str1Tokens);
        int termsInString1 = allTokens.size();
        Set<String> secondStringTokens = new HashSet<String>();
        secondStringTokens.addAll(str2Tokens);
        int termsInString2 = secondStringTokens.size();
        allTokens.addAll(secondStringTokens);
        int commonTerms = (termsInString1 + termsInString2) - allTokens.size();
		return (double) ((float) (commonTerms) / (float) (Math.pow((float) termsInString1, 0.5f) * Math.pow((float) termsInString2, 0.5f)));
    }
    /**
    *Implements the Matching Coefficient algorithm providing a similarity measure between two strings
    */
	public double MatchingCoefficient(String string1, String string2)
	{
		ArrayList<String> str1Tokens = TokeniserWhitespace(string1);
		ArrayList<String> str2Tokens = TokeniserWhitespace(string2);
		int totalPossible = (int) Math.max(str1Tokens.size(), str2Tokens.size());
        float totalFound = 0.0f;
        for (Object str1Token : str1Tokens)
		{
			String sToken = (String) str1Token;
			boolean found = false;
			for (Object str2Token : str2Tokens)
			{
				String tToken = (String) str2Token;
                if (sToken.equals(tToken))
                {
					found = true;
				}
			}
			if (found) totalFound++;
        }
        return (double)(totalFound / (float) totalPossible);
    }
    /**
    *Implements the Overlap Coefficient algorithm providing a similarity measure between two string where it is determined to what degree a string is a subset of another
    */
	public double OverlapCoefficient(String string1, String string2)
	{
        ArrayList<String> str1Tokens = TokeniserWhitespace(string1);
        ArrayList<String> str2Tokens = TokeniserWhitespace(string2);
		Set<String> allTokens = new HashSet<String>();
        allTokens.addAll(str1Tokens);
		int termsInString1 = allTokens.size();
		Set<String> secondStringTokens = new HashSet<String>();
        secondStringTokens.addAll(str2Tokens);
		int termsInString2 = secondStringTokens.size();
        allTokens.addAll(secondStringTokens);
        int commonTerms = (termsInString1 + termsInString2) - allTokens.size();
        return (double)((float) (commonTerms) / (float) Math.min(termsInString1, termsInString2));
    }
    /**
    *Implements the DiceSimilarity algorithm providing a similarity measure between two strings using the vector space of present terms
    */
	public double DiceSimilarity(String string1, String string2)
	{
		ArrayList<String> str1Tokens = TokeniserWhitespace(string1);
		ArrayList<String> str2Tokens = TokeniserWhitespace(string2);
		Set<String> allTokens = new HashSet<String>();
        allTokens.addAll(str1Tokens);
        int termsInString1 = allTokens.size();
        Set<String> secondStringTokens = new HashSet<String>();
        secondStringTokens.addAll(str2Tokens);
        int termsInString2 = secondStringTokens.size();
		allTokens.addAll(secondStringTokens);
		int commonTerms = (termsInString1 + termsInString2) - allTokens.size();
		return (double) (2.0f * commonTerms) / (termsInString1 + termsInString2);
    }
    /**
    *Implements the Jaro-Winkler algorithm providing a similarity measure between two strings allowing character transpositions to a degree adjusting the weighting for common prefixes
    */
 	public double JaroWinkler(String string1, String string2)
 	{
		float dist = 0.0f;
		int halflen = (int)((Math.min(string1.length(), string2.length())) / 2) + ((Math.min(string1.length(), string2.length())) % 2);
		StringBuffer common1 = getCommonCharacters(string1, string2, halflen);
		StringBuffer common2 = getCommonCharacters(string2, string1, halflen);
		if (common1.length() == 0 || common2.length() == 0)
		{
			dist=0.0f;
		}
		else if (common1.length() != common2.length())
		{
			dist=0.0f;
        }
        else
        {
			float transpositions = 0.0f;
			for (int i = 0; i < common1.length(); i++)
			{
				if (common1.charAt(i) != common2.charAt(i)) transpositions++;
			}
			transpositions /= 2.0f;
			dist=(common1.length() / ( string1.length()) + common2.length() / ( string2.length()) +(common1.length() - transpositions) / ( common1.length())) / 3.0f;
		}
		int n = (int) Math.min(6, Math.min(string1.length(), string2.length()));
		int prefixLength=n;
        for (int i = 0; i < n; i++)
        {
			if (string1.charAt(i) != string2.charAt(i))
			{
				prefixLength=i;
			}
		}
		return (double)(dist + ((float) prefixLength * 0.1f * (1.0f - dist)));
    }
    /**
    *Implements the Jaccard Similarity algorithm providing a similarity measure between two strings
    */
	public double JaccardSimilarity(String string1, String string2)
	{
		ArrayList<String> str1Tokens = TokeniserWhitespace(string1);
        ArrayList<String> str2Tokens = TokeniserWhitespace(string2);
        Set<String> allTokens = new HashSet<String>();
        allTokens.addAll(str1Tokens);
        int termsInString1 = allTokens.size();
        Set<String> secondStringTokens = new HashSet<String>();
        secondStringTokens.addAll(str2Tokens);
        int termsInString2 = secondStringTokens.size();
		allTokens.addAll(secondStringTokens);
		int commonTerms = (termsInString1 + termsInString2) - allTokens.size();
        return (double)(((float) (commonTerms)) / ((float) (allTokens.size())));
    }
    /**
    *Implements the Q Grams Distance algorithm providing a similarity measure between two strings using the qGram approach check matching qGrams/possible matching qGrams
    */
	public double QGramsDistance(String string1, String string2)
	{
		ArrayList<String> str1Tokens = TokeniserQGram3Extended(string1);
		ArrayList<String> str2Tokens = TokeniserQGram3Extended(string2);
		double maxQGramsMatching = str1Tokens.size() + str2Tokens.size();
        if (maxQGramsMatching == 0)
        {
			return 0.0;
		}
		else
		{
			Set<String> allTokens = new HashSet<String>();
			allTokens.addAll(str1Tokens);
			allTokens.addAll(str2Tokens);
			Iterator<String> allTokensIt = allTokens.iterator();
			double difference = 0.0;
			while (allTokensIt.hasNext())
			{
				String token = allTokensIt.next();
				double matchingQGrams1 = 0;
				if (str1Tokens.contains(token))
				{
					for (String str1Token : str1Tokens)
					{
						if (str1Token.equals(token))
						{
							matchingQGrams1++;
						}
					}
				}
				double matchingQGrams2 = 0;
				if (str2Tokens.contains(token))
				{
					for (String str2Token : str2Tokens)
					{
						if (str2Token.equals(token))
						{
							matchingQGrams2++;
						}
					}
				}
				difference+=Math.abs(matchingQGrams1 - matchingQGrams2);
			}
			return ((maxQGramsMatching - difference) / maxQGramsMatching);
        }
    }
    /**
    *Implements the basic Levenshtein algorithm providing a similarity measure between two strings
    */
	public double Levenshtein(String string1, String string2)
	{
		float levensteinDistance = 0.0f;
		float[][] d;
        float cost;
        int n = string1.length();
        int m = string2.length();
        if ((n != 0) && (m != 0))
        {
			d = new float[n + 1][m + 1];
			for (int i = 0; i <= n; i++)
			{
				d[i][0] = i;
			}
			for (int j = 0; j <= m; j++)
			{
				d[0][j] = j;
			}
			for (int i = 1; i <= n; i++)
			{
				for (int j = 1; j <= m; j++)
				{
					if (string1.charAt(i - 1) == string2.charAt(j - 1)) cost=0.0f;
					else cost=1.0f;
					d[i][j] = Math.min(d[i - 1][j] + 1, Math.min(d[i][j - 1] + 1, d[i - 1][j - 1] + cost));
				}
			}
			levensteinDistance=d[n][m];
		}
        float maxLen = string1.length();
        if (maxLen < string2.length()) maxLen = string2.length();
        if (maxLen == 0) return 1.0;
		return (double)(1.0 - (levensteinDistance / maxLen));
    }
    /**
    *Implements the Block distance algorithm whereby vector space block distance is used to determine a similarity
    */
	public double BlockDistance(String string1, String string2)
	{
        ArrayList<String> str1Tokens = TokeniserWhitespace(string1);
        ArrayList<String> str2Tokens = TokeniserWhitespace(string2);
		float totalPossible = (str1Tokens.size() + str2Tokens.size());
		float totalDistance = 0.0f;
        Set<Object> allTokens = new HashSet<Object>();
        allTokens.addAll(str1Tokens);
        allTokens.addAll(str2Tokens);
		for (Object allToken : allTokens)
		{
			String token = (String) allToken;
			int countInString1 = 0;
			int countInString2 = 0;
			for (Object str1Token : str1Tokens)
			{
				String sToken = (String) str1Token;
				if (sToken.equals(token)) countInString1++;
			}
			for (Object str2Token : str2Tokens)
			{
				String sToken = (String) str2Token;
				if (sToken.equals(token)) countInString2++;
			}
			if (countInString1 > countInString2)
				totalDistance += (countInString1 - countInString2);
			else
				totalDistance += (countInString2 - countInString1);
		}
        return (double)(totalPossible - totalDistance) / totalPossible;
    }
    /**
    *Implements the Monge Elkan algorithm providing a matching style similarity measure between two strings
    */
    public double MongeElkan(String string1, String string2)
    {
		ArrayList<String> str1Tokens = TokeniserWhitespace(string1);
        ArrayList<String> str2Tokens = TokeniserWhitespace(string2);
		float sumMatches = 0.0f;
        float maxFound;
        for (Object str1Token : str1Tokens)
        {
			maxFound = 0.0f;
            for (Object str2Token : str2Tokens)
            {
				float found = (float)SmithWatermanGotoh((String) str1Token, (String) str2Token);
                if (found > maxFound) maxFound = found;
            }
            sumMatches += maxFound;
        }
        return (float)(sumMatches / (float) str1Tokens.size());
    }
    /**
    *Implements the Chapman Ordered Name Compound Similarity algorithm whereby terms are matched and tested against the standard
    * soundex algorithm - this is intended to provide a better rating for lists of proper names
    */
	public double ChapmanOrderedNameCompoundSimilarity(String string1, String string2)
	{
		ArrayList<?> str1Tokens = TokeniserWhitespace(string1);
		ArrayList<?> str2Tokens = TokeniserWhitespace(string2);
        int str1TokenNum = str1Tokens.size();
        int str2TokenNum = str2Tokens.size();
        int minTokens = (int)Math.min(str1TokenNum, str2TokenNum);
		float SKEW_AMMOUNT = 1.0f;
        float sumMatches = 0.0f;
        for (int i = 1; i <= minTokens; i++)
        {
			float strWeightingAdjustment = ((1.0f/minTokens)+(((((minTokens-i)+0.5f)-(minTokens/2.0f))/minTokens)*SKEW_AMMOUNT*(1.0f/minTokens)));
            String sToken = (String) str1Tokens.get(str1TokenNum-i);
            String tToken = (String) str2Tokens.get(str2TokenNum-i);
			float found1 = (float)SoundEx(sToken, tToken);
			float found2 = (float)SoundEx(sToken, tToken);
            sumMatches += ((0.5f * (found1+found2)) * strWeightingAdjustment);
        }
        return (double)sumMatches;
    }
	/**
	* gets the similarity of the two strings using Jaro distance.
	*/
	public double Jaro(String string1, String string2)
	{
		int halflen = (int)((Math.min(string1.length(), string2.length())) / 2) + ((Math.min(string1.length(), string2.length())) % 2);
		StringBuffer common1 = getCommonCharacters(string1, string2, halflen);
		StringBuffer common2 = getCommonCharacters(string2, string1, halflen);
        if (common1.length() == 0 || common2.length() == 0) return 0.0;
        if (common1.length() != common2.length()) return 0.0;
        float transpositions = 0.0f;
        for (int i = 0; i < common1.length(); i++)
        {
            if (common1.charAt(i) != common2.charAt(i)) transpositions=transpositions+1.0f;
        }
        transpositions = transpositions /2.0f;
        distvalue=(double)(((float)common1.length() / (float)string1.length() + (float)common2.length() / (float)string2.length() + ((float)common1.length() - transpositions) / (float)common1.length()) / 3.0f);
	    return distvalue;
	}
	/**
	* Gets the similarity of the two strings using EuclideanDistance
	* the 0-1 return is calcualted from the maximum possible Euclidean
	* distance between the strings from the number of terms within them.
	*/
    public double EuclideanDistance(String string1, String string2)
    {
		ArrayList<String> str1Tokens = TokeniserWhitespace(string1);
		ArrayList<String> str2Tokens = TokeniserWhitespace(string2);
		float totalPossible = (float)Math.sqrt((str1Tokens.size()*str1Tokens.size()) + (str2Tokens.size()*str2Tokens.size()));
		Set<String> allTokens = new HashSet<String>();
		allTokens.addAll(str1Tokens);
		allTokens.addAll(str2Tokens);
		float totalDistance = 0.0f;
        for (String token : allTokens)
        {
			int countInString1 = 0;
			int countInString2 = 0;
			for (String sToken : str1Tokens)
			{
				if (sToken.equals(token)) countInString1++;
			}
			for (final String sToken : str2Tokens)
			{
				if (sToken.equals(token))  countInString2++;
            }
			totalDistance += ((countInString1 - countInString2) * (countInString1 - countInString2));
		}
        totalDistance = (float)Math.sqrt(totalDistance);
        return (double)((totalPossible - totalDistance) / totalPossible);
    }
 	/**
 	* Gets the similarity of the two strings using Needleman Wunch distance.
	*/
    public double NeedlemanWunch(String string1, String string2)
    {
        float cost;
        int n = string1.length();
        int m = string2.length();
        if (n == 0) return 0;
        if (m == 0) return 0;
        float[][] d = new float[n + 1][m + 1];
        for (int i = 0; i <= n; i++) d[i][0] = i;
        for (int j = 0; j <= m; j++) d[0][j] = j;
        for (int i = 1; i <= n; i++)
        {
			for (int j = 1; j <= m; j++)
			{
				if (string1.charAt(i-1) == string2.charAt(j-1)) cost=0.0f;
				else cost=1.0f;
				d[i][j] = (float)Math.min(d[i - 1][j] + 2.0, Math.min(d[i][j - 1] + 2.0, d[i - 1][j - 1] + cost));
			}
		}
        float needlemanWunch=d[n][m];
        float maxValue = 2.0f* Math.max(string1.length(), string2.length());
		return (double)(1.0f - (needlemanWunch / maxValue));
    }
    /**
    *TokeniserWhitespace implements a simple whitespace tokeniser
    */
	private ArrayList<String> TokeniserWhitespace(String input)
	{
        ArrayList<String> returnVect = new ArrayList<String>();
        int curPos = 0;
        while (curPos < input.length())
        {
            char ch = input.charAt(curPos);
            if (Character.isWhitespace(ch))
            {
				curPos++;
            }
            int nextGapPos = input.length();
            for (int i = 0; i < delimiters.length(); i++)
            {
				final int testPos = input.indexOf(delimiters.charAt(i), curPos);
				if (testPos < nextGapPos && testPos != -1)
				{
					nextGapPos = testPos;
				}
			}
			String term = input.substring(curPos, nextGapPos);
            if(!term.trim().equals(""))
            {
				returnVect.add(term);
            }
            if (curPos==nextGapPos) curPos++;
            else curPos = nextGapPos;
        }
        return returnVect;
    }
	/**
	*TokeniserQGram3Extended implements a tokeniser splitting the string into qGrams
	* extending beyond the ends of the string input, using padding characters.
	*/
    private ArrayList<String> TokeniserQGram3Extended(String input)
    {
		if (input.length()>1000000)
			input=input.substring(0,1000000);
		ArrayList<String> returnVect = new ArrayList<String>();
		StringBuffer adjustedString = new StringBuffer();
		adjustedString.append(QGRAMSTARTPADDING);
		adjustedString.append(QGRAMSTARTPADDING);
        adjustedString.append(input);
		adjustedString.append(QGRAMENDPADDING);
        adjustedString.append(QGRAMENDPADDING);
        int curPos = 0;
        int length = adjustedString.length()-2;
		while (curPos < length)
		{
			returnVect.add(adjustedString.substring(curPos, curPos + 3));
			curPos++;
		}
		return returnVect;

	}
    /**
	* Returns a string buffer of characters from string1 within string2 if they are of a given
	* distance seperation from the position in string1.
	*/
	private StringBuffer getCommonCharacters(String string1, String string2, int distanceSep)
	{
		StringBuffer returnCommons = new StringBuffer();
		StringBuffer copy = new StringBuffer(string2);
		for (int i = 0; i < string1.length(); i++)
		{
			char ch = string1.charAt(i);
			boolean foundIt = false;
			for (int j = Math.max(0, i - distanceSep); (!foundIt) && (j <= Math.min(i + distanceSep, string2.length() - 1)); j++)
			{
				if (copy.charAt(j) == ch)
				{
					foundIt = true;
					returnCommons.append(ch);
					copy.setCharAt(j, '\000');
				}
			}
		}
	    return returnCommons;
	}
	/**
	*Implements the SmithWatermanGotoh distance between two strings
	*/
	private double SmithWatermanGotoh(String string1, String string2)
	{
        float cost;
        int windowSize = 100;
        int n = string1.length();
        int m = string2.length();
        if ((n == 0) || (m==0)) return 0.0;
        float[][] d = new float[n][m];
        float maxSoFar = 0.0f;
        for (int i = 0; i < n; i++)
        {
			cost = (float)getCost(string1, i, string2, 0);
			if (i == 0) d[0][0] = Math.max(0, cost);
			else
			{
				float maxGapCost = 0.0f;
				int windowStart = i-windowSize;
				if (windowStart < 1)
				{
					windowStart = 1;
                }
                for (int k = windowStart; k < i; k++)
                {
					maxGapCost = (float)Math.max(maxGapCost, d[i - k][0] - gGapFunc(string1, i - k, i));
                }
                d[i][0] = (float)Math.max(0, Math.max(maxGapCost, cost));
			}
            if (d[i][0] > maxSoFar)  maxSoFar = d[i][0];
		}
        for (int j = 0; j < m; j++)
        {
            cost = (float)getCost(string1, 0, string2, j);
            if (j == 0)  d[0][0] = Math.max(0, cost);
			else
			{
				float maxGapCost = 0.0f;
				int windowStart = j-windowSize;
				if (windowStart < 1)
				{
					windowStart = 1;
                }
                for (int k = windowStart; k < j; k++)
                {
					maxGapCost = (float)Math.max(maxGapCost, d[0][j - k] - gGapFunc(string2, j - k, j));
				}
                d[0][j] = (float)Math.max(0, Math.max(maxGapCost, cost));
            }
			if (d[0][j] > maxSoFar) maxSoFar = d[0][j];
		}
		for (int i = 1; i < n; i++)
		{
			for (int j = 1; j < m; j++)
			{
				cost = (float)getCost(string1, i, string2, j);
				float maxGapCost1 = 0.0f;
                float maxGapCost2 = 0.0f;
				int windowStart = i-windowSize;
                if (windowStart < 1) windowStart = 1;
                for (int k = windowStart; k < i; k++)
                {
					maxGapCost1 = (float)Math.max(maxGapCost1, d[i - k][j] - gGapFunc(string1, i - k, i));
				}
				windowStart = j-windowSize;
                if (windowStart < 1) windowStart = 1;
                for (int k = windowStart; k < j; k++)
                {
					maxGapCost2 = (float)Math.max(maxGapCost2, d[i][j - k] - gGapFunc(string2, j - k, j));
				}
				d[i][j] = (float)Math.max(Math.max(0, maxGapCost1), Math.max(maxGapCost2, d[i - 1][j - 1] + cost));
                if (d[i][j] > maxSoFar) maxSoFar = d[i][j];
            }
        }
        float maxValue = (float)Math.min(string1.length(), string2.length());
        if (maxValue == 0) return 1.0;
        else return (double)(maxSoFar/(maxValue*5.0f));
	}
    /**
	* Implements a cost function as used in Monge Elkan where by an exact match
	* no match or an approximate match whereby a set of characters are in an approximate range.
	* for pairings in {dt} {gj} {lr} {mn} {bpv} {aeiou} {,.}
	*/
	private double getCost(String str1, int string1Index, String str2, int string2Index)
	{
		if (approx==null) initApprox();
		if (str1.length() <= string1Index || string1Index < 0) return CHAR_MISMATCH_MATCH_SCORE;
		if (str2.length() <= string2Index || string2Index < 0) return CHAR_MISMATCH_MATCH_SCORE;
        if (str1.charAt(string1Index) == str2.charAt(string2Index)) return CHAR_EXACT_MATCH_SCORE;
		Character si = Character.toLowerCase(str1.charAt(string1Index));
		Character ti = Character.toLowerCase(str2.charAt(string2Index));
		for (int i=0; i<approx.size(); i++)
		{
			Vector<Character> aApprox=approx.get(i);
			if (aApprox.contains(si) && aApprox.contains(ti))
				return CHAR_APPROX_MATCH_SCORE;
		}
		return CHAR_MISMATCH_MATCH_SCORE;
    }
    /**
    *Implements the Soundex algorithm providing a similarity measure between two soundex codes
    */
	public double SoundEx(String string1, String string2)
	{
		String soundex1 = calcSoundEx(string1, 6);
		String soundex2 = calcSoundEx(string2, 6);
		return JaroWinkler(soundex1, soundex2);
    }
    /**
    *Calculates a soundex code for a given string/name
    */
	private String calcSoundEx(String wordString, int soundExLen)
	{
		String tmpStr;
		String wordStr;
		char curChar;
		char lastChar;
		final int wsLen;
		final char firstLetter;
        if (soundExLen > 10) soundExLen = 10;
        if (soundExLen < 4) soundExLen = 4;
        if (wordString.length() == 0) return ("");
        wordString = wordString.toUpperCase();
        wordStr = wordString;
        wordStr = wordStr.replaceAll("[^A-Z]", " ");
        wordStr = wordStr.replaceAll("\\s+", "");
        if (wordStr.length() == 0) return ("");
        firstLetter = wordStr.charAt(0);
        if(wordStr.length() > (SOUNDEXLENGTH*4)+1) wordStr = "-" + wordStr.substring(1,SOUNDEXLENGTH*4);
        else wordStr = "-" + wordStr.substring(1);
        wordStr = wordStr.replaceAll("[AEIOUWH]", "0");
        wordStr = wordStr.replaceAll("[BPFV]", "1");
        wordStr = wordStr.replaceAll("[CSKGJQXZ]", "2");
        wordStr = wordStr.replaceAll("[DT]", "3");
        wordStr = wordStr.replaceAll("[L]", "4");
        wordStr = wordStr.replaceAll("[MN]", "5");
        wordStr = wordStr.replaceAll("[R]", "6");
        wsLen = wordStr.length();
        lastChar = '-';
        tmpStr = "-";
        for (int i = 1; i < wsLen; i++)
        {
			curChar = wordStr.charAt(i);
            if (curChar != lastChar)
            {
				tmpStr += curChar;
				lastChar = curChar;
			}
		}
        wordStr = tmpStr;
        wordStr = wordStr.substring(1);
        wordStr = wordStr.replaceAll("0", "");
        wordStr += "000000000000000000";
        wordStr = firstLetter + "-" + wordStr;
        wordStr = wordStr.substring(0, soundExLen);
        return (wordStr);
    }
	/**
     * Get cost between characters.
     *
     * @param stringToGap         - the string to get the cost of a gap
     * @param stringIndexStartGap - the index within the string to test a start gap from
     * @param stringIndexEndGap   - the index within the string to test a end gap to
     * @return the cost of a Gap G
     */
	private double gGapFunc(String stringToGap, int stringIndexStartGap, int stringIndexEndGap)
	{
		if (stringIndexStartGap >= stringIndexEndGap) return 0.0;
		else return 5.0 + ((stringIndexEndGap - 1) - stringIndexStartGap);
    }
}
