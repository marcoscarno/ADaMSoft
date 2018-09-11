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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import ADaMSoft.algorithms.ID3Evaluator;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that implements the id3 algorithm
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcId3 extends ObjectTransformer implements RunStep
{
	ID3Evaluator id3=null;
	String rootnode;
	double rootgain;
	Vector<ID3BasicNode> path;
	String[] varx;
	ID3Tree tree;
	boolean toreplace;
	int minunits;
	boolean gratio;
	/**
	* Starts the execution of Proc Id3
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		path=new Vector<ID3BasicNode>();
		tree= new ID3Tree();
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.varx, Keywords.vary};
		String [] optionalparameters=new String[] {Keywords.minunit, Keywords.successvalue, Keywords.where, Keywords.weight, Keywords.replace,Keywords.collapse,Keywords.relgain};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String successvalue=(String)parameters.get(Keywords.successvalue.toLowerCase());

		String minunit =(String)parameters.get(Keywords.minunit);
		if (minunit==null)
			minunits=0;
		else
		{
			try
			{
				minunits=string2int(minunit);
			}
			catch (Exception e)
			{
				return new Result("%1493%<br>\n", false, null);
			}
			if (minunits<1)
				return new Result("%1493%<br>\n", false, null);
		}

		String replace =(String)parameters.get(Keywords.replace);
		if (replace==null)
			toreplace=true;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			toreplace=false;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			toreplace=false;

		DataWriter dwi=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dwi.getmessage().equals(""))
			return new Result(dwi.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String tempvarx=(String)parameters.get(Keywords.varx.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.vary.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());
		boolean collapse=parameters.get(Keywords.collapse.toLowerCase())!=null;
		gratio=parameters.get(Keywords.relgain.toLowerCase())!=null;

		Hashtable<String, String> tcl=dict.getcodelabelfromname(tempvary);

		String[] testdepvar=tempvary.split(" ");
		if (testdepvar.length>1)
			return new Result("%832%<br>\n", false, null);

		VariableUtilities varu=new VariableUtilities(dict, null, null, weight, tempvarx, tempvary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		varx=varu.getrowvar();
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

		int[] rowrule=varu.getrowruleforsel();
		int[] colrule=varu.getcolruleforsel();
		int[] weightrule=varu.getweightruleforsel();

		ValuesParser vp=new ValuesParser(null, null, null, rowrule, colrule, weightrule);

		id3=new ID3Evaluator(varx);

		String[] values=null;
		String[] varxvalues=null;
		String[] varyvalues=null;
		double weightvalue=1;
		boolean notmissing=true;
		int validgroup=0;
		while (!data.isLast())
		{
			notmissing=true;
			values = data.getRecord();
			if (values!=null)
			{
				varxvalues=vp.getrowvar(values);
				varyvalues=vp.getcolvar(values);
				weightvalue=vp.getweight(values);
				if (varyvalues[0].equals(""))
				{
					notmissing=false;
				}
				if (Double.isNaN(weightvalue))
				{
					notmissing=false;
				}
				for (int i=0; i<varxvalues.length; i++)
				{
					if (varxvalues[i].equals(""))
						notmissing=false;
				}
				if (notmissing)
				{
					if (successvalue!=null)
					{
						if (varyvalues[0].equalsIgnoreCase(successvalue))
							varyvalues[0]="1";
						else
							varyvalues[0]="0";
					}
					id3.estimate(varxvalues, varyvalues[0], weightvalue);
					validgroup++;
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		if(gratio) id3.setfreqRel();

		values=new String[varx.length];
		for (int i=0; i<values.length; i++)
		{
			values[i]=null;
		}
		double[] gain=new double[varx.length];
		double[] temp=new double[gain.length];
		int neworder=0;
		for (int i=0; i<values.length; i++)
		{
			gain[i]=id3.getgain(values, i);
			temp[i]=gain[i];
		}
		Arrays.sort(temp);
		int maxgfound=0;
		for (int i=0; i<gain.length; i++)
		{
			if (gain[i]==temp[varx.length-1])
			{
				maxgfound++;
				neworder=i;
			}
		}
		if (maxgfound!=1)
		{
			String varmaxg="";
			for (int i=0; i<gain.length; i++)
			{
				if (gain[i]==temp[varx.length-1])
					varmaxg=varmaxg+" "+varx[i];
			}
			varmaxg=varmaxg.trim();
			return new Result("%1485% "+varmaxg+"<br>\n", false, null);
		}
		rootnode=varx[neworder];
		rootgain=temp[varx.length-1];
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		String keyword="ID3 "+dict.getkeyword();
		String description="ID3 "+dict.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());
		int varCounterg=0;
		int varCountern=0;
		int varCounterm=0;

		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();
		Hashtable<String, String> tempfixedvariableinfo=new Hashtable<String, String>();
		Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
		Hashtable<String, String> tempmissingdata=new Hashtable<String, String>();

		int vectorSize=0;
		tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"gain_"+varCounterg++);
		vectorSize++;
		tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.NUMSuffix);
		tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1477%");
		fixedvariableinfo.add(tempfixedvariableinfo);
		codelabel.add(tempcodelabel);
		missingdata.add(tempmissingdata);

		tempfixedvariableinfo=new Hashtable<String, String>();
		tempcodelabel=new Hashtable<String, String>();
		tempmissingdata=new Hashtable<String, String>();
		tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"node_"+varCountern++);
		vectorSize++;
		tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.TEXTSuffix);
		tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1478%");
		fixedvariableinfo.add(tempfixedvariableinfo);
		Hashtable<String, String> tvn=new  Hashtable<String, String>();
		tvn.put(rootnode, dict.getvarlabelfromname(rootnode));
		codelabel.add(tvn);
		missingdata.add(tempmissingdata);

		ID3BasicNode root = new  ID3BasicNode();
		root.setVarName(rootnode);
		path.add(root);
		createTree(values, neworder);
		if(collapse){
			tree.collapse();
		}
		Vector<LinkedList<String[]>>paths = tree.getPaths();
		int nodeCount = tree.getMaxPathSize();
		for (int i=0; i<nodeCount-2; i++)
		{
			tempfixedvariableinfo=new Hashtable<String, String>();
			tempcodelabel=new Hashtable<String, String>();
			tempmissingdata=new Hashtable<String, String>();
			tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"val_"+varCounterm++);
			vectorSize++;
			tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.TEXTSuffix);
			if (i==0)
				tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1479%");
			else
				tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1480%: "+(i+1));
			fixedvariableinfo.add(tempfixedvariableinfo);
			codelabel.add(tempcodelabel);
			missingdata.add(tempmissingdata);

			tempfixedvariableinfo=new Hashtable<String, String>();
			tempcodelabel=new Hashtable<String, String>();
			tempmissingdata=new Hashtable<String, String>();
			tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"gain_"+varCounterg++);
			vectorSize++;
			tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.NUMSuffix);
			tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1481%: "+(i+1));
			fixedvariableinfo.add(tempfixedvariableinfo);
			codelabel.add(tempcodelabel);
			missingdata.add(tempmissingdata);

			tempfixedvariableinfo=new Hashtable<String, String>();
			tempcodelabel=new Hashtable<String, String>();
			tempmissingdata=new Hashtable<String, String>();
			tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"node_"+varCountern++);
			vectorSize++;
			tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.TEXTSuffix);
			tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1482%: "+(i+1));
			fixedvariableinfo.add(tempfixedvariableinfo);
			Hashtable<String, String> tvnn=new  Hashtable<String, String>();
			for (int j=0; j<varx.length; j++)
			{
				tvnn.put(varx[j], dict.getvarlabelfromname(varx[j]));
			}
			codelabel.add(tvnn);
			missingdata.add(tempmissingdata);
		}

		tempfixedvariableinfo=new Hashtable<String, String>();
		tempcodelabel=new Hashtable<String, String>();
		tempmissingdata=new Hashtable<String, String>();
		tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"val_"+varCounterm++);
		vectorSize++;
		tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.TEXTSuffix);
		tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1480%: "+(nodeCount-1));
		fixedvariableinfo.add(tempfixedvariableinfo);
		codelabel.add(tempcodelabel);
		missingdata.add(tempmissingdata);

		int nodeColumn= vectorSize;

		tempfixedvariableinfo=new Hashtable<String, String>();
		tempcodelabel=new Hashtable<String, String>();
		tempmissingdata=new Hashtable<String, String>();
		tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"Decision");
		vectorSize++;
		tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.TEXTSuffix);
		tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1483%");
		fixedvariableinfo.add(tempfixedvariableinfo);
		if (toreplace)
			codelabel.add(tcl);
		else
			codelabel.add(tempcodelabel);
		missingdata.add(tempmissingdata);

		tempfixedvariableinfo=new Hashtable<String, String>();
		tempcodelabel=new Hashtable<String, String>();
		tempmissingdata=new Hashtable<String, String>();
		tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"Freq");
		vectorSize++;
		tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.NUMSuffix);
		tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1484%");
		fixedvariableinfo.add(tempfixedvariableinfo);
		codelabel.add(tempcodelabel);
		missingdata.add(tempmissingdata);

		tempfixedvariableinfo=new Hashtable<String, String>();
		tempcodelabel=new Hashtable<String, String>();
		tempmissingdata=new Hashtable<String, String>();
		tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"Freqp");
		vectorSize++;
		tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.NUMSuffix);
		tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1491%");
		fixedvariableinfo.add(tempfixedvariableinfo);
		codelabel.add(tempcodelabel);
		missingdata.add(tempmissingdata);

		Iterator<String> yMod = id3.getdy().iterator();

		int varCounter=0;
		while(yMod.hasNext())
		{
			String modality= yMod.next();
			tempfixedvariableinfo=new Hashtable<String, String>();
			tempcodelabel=new Hashtable<String, String>();
			tempmissingdata=new Hashtable<String, String>();
			vectorSize++;
			tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.NUMSuffix);
			String subsmod=tcl.get(modality);
			if (subsmod==null)
				subsmod=modality;
			subsmod=subsmod.replaceAll("\\s","");
			if (toreplace)
			{
				tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1486%: "+subsmod);
				tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"Freq_"+varCounter+"_"+subsmod);
			}
			else
			{
				tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"%1486%: "+modality);
				tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"Freq_"+varCounter+"_"+subsmod);
			}
			varCounter++;
			fixedvariableinfo.add(tempfixedvariableinfo);
			codelabel.add(tempcodelabel);
			missingdata.add(tempmissingdata);
		}


		boolean resopen=dw.opendatatable(fixedvariableinfo);
		if (!resopen)
			return new Result(dw.getmessage(), false, null);

		for(int i=0; i<paths.size();i++)
		{
			LinkedList<String[]> element = paths.get(i);
			String[] vals = new String[vectorSize];
			int counterColumn=0;
			double[] gains= getGainsForPath(element);
			for(int j=0;j<element.size()-1;j++){
				vals[counterColumn++]=String.valueOf(gains[j]);
				vals[counterColumn++]= element.get(j)[0];
				vals[counterColumn++]=element.get(j)[1];
			}
			vals[nodeColumn]=element.getLast()[0];
			double[] frequencies= getFreqForPath(element);
			vals[nodeColumn+1]= String.valueOf(frequencies[0]);
			double sumfreqy=0;
			for(int j=0;j<id3.getdy().size();j++)
			{
				sumfreqy+=frequencies[j+1];
				vals[nodeColumn+3+j]= double2String(frequencies[j+1]);
			}
			vals[nodeColumn+2]= double2String(100*frequencies[0]/sumfreqy);
			if(frequencies[0]!=0){
				dw.write(vals);
			}
		}
		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, fixedvariableinfo, tablevariableinfo, codelabel, missingdata, null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1464, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varx, "vars=all", true, 1465, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vary, "var=all", true, 1466, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.successvalue, "text", false, 1753, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.collapse, "checkbox", false, 1567, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.relgain, "checkbox", false, 1568, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.minunit, "text",false, 1492, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1462";
		retprocinfo[1]="1463";
		return retprocinfo;
	}

	/**
	* Get the gain for each variable
	*/
	private ID3BasicNode getbestgain(String[] values, String distinct)
	{
		ID3BasicNode result=new ID3BasicNode();
		result.setDistinct(distinct);
		double[] tempgain=new double[values.length];
		double[] temp=new double[values.length];
		for (int i=0; i<values.length; i++)
		{
			if (values[i]==null)
			{
				tempgain[i]=id3.getgain(values, i);
				temp[i]=tempgain[i];
			}
			else
			{
				tempgain[i]=0;
				temp[i]=0;
			}
		}
		Arrays.sort(temp);
		if (temp[values.length-1]==0)
		{
			return result;
		}
		int valdep=0;
		for (int i=0; i<tempgain.length; i++)
		{
			if (tempgain[i]==temp[tempgain.length-1])
				valdep=i;
		}
		result.setRef(valdep);
		result.setVarName(varx[valdep]);
		return result;
	}


	private void createTree(String[] superTree, int lastvarf)
	{
		TreeSet<String> vv=id3.getdx(lastvarf);
		Iterator<String> keysetIt = vv.iterator();
		while(keysetIt.hasNext())
		{
			String modality=keysetIt.next();
			superTree[lastvarf]=modality;
			ID3BasicNode node=getbestgain(superTree, modality);
			boolean newnode=true;
			if (node.getVarName()!=null)
			{
				double[] check=id3.getfreqy(superTree);
				for (int i=0; i<check.length; i++)
				{
					if (check[i]<minunits)
						newnode=false;
				}
				if (newnode)
				{
					path.add(node);
					createTree(superTree, node.getRef());
					path.removeElementAt(path.size()-1);
					superTree[lastvarf]=null;
				}
			}
			if (node.getVarName()==null || (!newnode))
			{
				//leaf
				double[] check=id3.getfreqy(superTree);
				double[] tempc=new double[check.length];
				for (int i=0; i<check.length; i++)
				{
					tempc[i]=check[i];
				}
				Arrays.sort(tempc);
				double maxfreq=tempc[check.length-1];
				for (int i=tempc.length-2; i>=0; i--)
				{
					if (tempc[i]==tempc[check.length-1])
						maxfreq+=tempc[i];
				}
				int valdep=0;
				for (int i=0; i<check.length; i++)
				{
					if (check[i]==tempc[check.length-1])
					{
						valdep=i;
					}
				}
				Iterator<String> keysetdv = id3.getdy().iterator();
				String realdv="";
				int tt=0;
				while(keysetdv.hasNext())
				{
					String tempdepv=keysetdv.next();
					if (tt==valdep)
						realdv=tempdepv;
					tt++;
				}
				if (maxfreq!=tempc[check.length-1])
					node.setVarName("-");
				else
					node.setVarName(realdv);
				path.add(node);
				tree.addPath(path);
				path.removeElementAt(path.size()-1);
				superTree[lastvarf]=null;
			}
		}
	}

	private double[] getFreqForPath(LinkedList<String[]> path){

		String[] vector = new String[varx.length];
		String mody = path.getLast()[0];
		for(int k=0;k<path.size()-1;k++){
			String var= path.get(k)[0];

			for(int i=0;i<varx.length;i++){
				if(varx[i].equals(var)){
					vector[i]=path.get(k)[1];
					break;
				}
			}
		}
		double[] result =new double[id3.getdy().size()+1];
		if(!mody.equals("-")){
			result[0] = id3.getfreq(vector, mody);
		}
		else{
			Iterator<String> it=id3.getdy().iterator();
			while(it.hasNext()){
				double freq = id3.getfreq(vector, it.next());
				if(result[0]==freq){
					result[0]+=freq;
				}
				else if(result[0]<freq){
					result[0]=freq;
				}
			}
		}
		Iterator<String> st = id3.getdy().iterator();
		int counter=1;
		while(st.hasNext()){
			String mod = st.next();
			result[counter++]=id3.getfreq(vector, mod);
		}
		return result;
	}

	private double[] getGainsForPath(LinkedList<String[]> path){

		double[] result = new double[path.size()-1];
		String[] vector = new String[varx.length];
		int nextPos=0;
		String var= path.get(0)[0];
		for(int i=0;i<varx.length;i++){
			if(varx[i].equals(var)){
				nextPos=i;
				break;
			}
		}
		for(int k=0;k<path.size()-1;k++){
			result[k]=id3.getgain(vector,nextPos);
			result[k]=id3.getgain(vector,nextPos);
			vector[nextPos]=path.get(k)[1];
			var= path.get(k+1)[0];
			for(int i=0;i<varx.length;i++){
				if(varx[i].equals(var)){
					nextPos=i;
					break;
				}
			}
		}
		return result;
	}
}


/**
*An internal node of the tree
*/
class Node
{
	HashMap<String,Node> children;
	String variable;
	boolean isLeaf;

	public Node(String variable){
		children= new HashMap<String, Node>();
		isLeaf=false;
		this.variable=variable;
	}

	public boolean isLeaf(){

		return isLeaf;
	}

	public String getVarName(){

		return variable;
	}

	public void addChild(String modality, Node child){

		children.put(modality, child);
	}

	public Node getChild(String modality){

		return children.get(modality);
	}

	public Vector<Node> getAllChildren(){

		return new Vector<Node>(children.values());
	}

	public Set<String> getModChildren(){

		return children.keySet();
	}

	public String toString(){

		return variable;
	}

	public void replaceChild(String mod, Node child){

		children.remove(mod);
		children.put(mod, child);
	}
}

/**
*The leaf of the tree
*/
class Leaf extends Node
{
	private double error;
	private double frequency;


	public Leaf(String modality){

		super(modality);
		isLeaf=true;
	}


	public double getError() {
		return error;
	}


	public void setError(double error) {
		this.error = error;
	}


	public double getFrequency() {
		return frequency;
	}


	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
}



class ID3Tree{

	public static final double CF =0.25;
	private Node tree;
	private int maxPathSize=0;

	private void addAllToNode(int index, Vector<ID3BasicNode> path, Node node){

		Node current = node;
		for(int i=index;i<path.size()-1;i++){
			ID3BasicNode snd= path.get(i);
			Node tmp = new Node(snd.getVarName());
			current.addChild(snd.getDistinct(), tmp);
			current = tmp;
		}
		ID3BasicNode snd= path.lastElement();
		Leaf tmp = new Leaf(snd.getVarName());
		current.addChild(snd.getDistinct(), tmp);
	}

	private int maxPath(Vector<LinkedList<String[]>> paths){
		int result=0;

		Iterator<LinkedList<String[]>> it = paths.iterator();
		while(it.hasNext()){
			int size=it.next().size();
			result=size>result?size:result;
		}
		return result;
	}

	private Vector<LinkedList<String[]>> visit(Node node){

		Vector<LinkedList<String[]>> result=null;
		if(node.isLeaf){
			result = new Vector<LinkedList<String[]>>();
			result.add(new LinkedList<String[]>());
			String[] record = new String[2];
			record[0]=node.getVarName();
			result.lastElement().addFirst(record);
			return result;
		}
		else{
			Iterator<String> it = (new TreeSet<String>(node.getModChildren())).iterator();
			while(it.hasNext()){
				String[] record = new String[2];
				record[0]=node.getVarName();
				String mod = it.next();
				Node current = node.getChild(mod);
				record[1]=mod;
				if(result==null){
					result=visit(current);
					Iterator<LinkedList<String[]>> it2 = result.iterator();
					while(it2.hasNext()){
						it2.next().addFirst(record);
					}
				}
				else{
					Vector<LinkedList<String[]>> tmp = visit(current);
					Iterator<LinkedList<String[]>> it2 = tmp.iterator();
					while(it2.hasNext()){
						it2.next().addFirst(record);
					}
					result.addAll(tmp);
				}
			}
			return result;
		}
	}

	private Node subCollapse(Node tree){

		if(!tree.isLeaf){
			String leafMod=null;
			Node subResult=null;
			Vector<String> childrend = new Vector<String>(tree.getModChildren());
			boolean allLeaf = true;
			for(int i=0;i<childrend.size();i++){
				String mod = childrend.get(i);
				Node chilNode = tree.getChild(mod);
				subResult = subCollapse(chilNode);
				if(subResult.isLeaf){
					if(leafMod==null){
						leafMod=subResult.variable;
					}
					if(!leafMod.equals(subResult.variable)){
						allLeaf=false;
					}
					tree.replaceChild(mod, subResult);
				}
				else{
					allLeaf=false;
				}
			}
			if(allLeaf){
				return subResult;
			}
			else{
				return tree;
			}
		}
		else{
			return tree;
		}
	}

	public Vector<LinkedList<String[]>> getPaths(){

		Vector<LinkedList<String[]>> result= visit(tree);
		maxPathSize=maxPath(result);
		return result;
	}


	public void addPath(Vector<ID3BasicNode> path){

		int counter=1;
		Node current;
		if(tree==null){
			tree=new Node(path.get(0).getVarName());
		}
		current = tree;
		for(int i=counter;i<path.size();i++){
			ID3BasicNode spn = path.get(i);
			Node node = current.getChild(spn.getDistinct());
			if(node==null){
				addAllToNode(i, path, current);
				break;
			}
			else{
				current=node;
			}
		}
	}

	public int getMaxPathSize() {

		return maxPathSize;
	}

	public void collapse(){

		tree=subCollapse(tree);
	}
}

class ID3BasicNode{

	private String varName, distinct;
	private int ref;
	public String getDistinct() {
		return distinct;
	}
	public void setDistinct(String distinct) {
		this.distinct = distinct;
	}
	public int getRef() {
		return ref;
	}
	public void setRef(int ref) {
		this.ref = ref;
	}
	public String getVarName() {
		return varName;
	}
	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String toString(){

		return distinct + " " + varName;
	}
}