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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.gui.MainGUI;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import javax.imageio.ImageIO;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that draws a dendogram
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcDendrogram implements RunStep{

	private int width=-1, height=-1;
	private Hashtable<Vector<String>,ClusterTree> mapping;
	private DictionaryReader dict;
	private Vector<String> variables;
	/**
	* Starts the execution of Proc Dendrogram
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.outjpg, Keywords.where, Keywords.maxdistance, Keywords.thicknumber, Keywords.thicksize, Keywords.unitfontsize, Keywords.thickfontsize ,Keywords.imgwidth, Keywords.imgheight, Keywords.title, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		dict = (DictionaryReader)parameters.get(Keywords.dict);
		String width = (String) parameters.get(Keywords.imgwidth);
		String height = (String) parameters.get(Keywords.imgheight);
		String fileName = (String) parameters.get(Keywords.outjpg);
		String title = (String) parameters.get(Keywords.title);
		String replace =(String)parameters.get(Keywords.replace);
		String thickSize =(String)parameters.get(Keywords.thicksize);
		String thickNumber =(String)parameters.get(Keywords.thicknumber);
		String fontSize =(String)parameters.get(Keywords.unitfontsize);
		String thickFontSize =(String)parameters.get(Keywords.thickfontsize);
		String cutAt =(String)parameters.get(Keywords.maxdistance);
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;

		if (fileName!=null)
		{
			if(!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg"))
				fileName+=".jpg";
		}

		if(width!=null){
			try{
				this.width = Integer.parseInt(width);
			}
			catch(Exception e){}
		}
		if(width!=null){
			try{
				this.height = Integer.parseInt(height);
			}
			catch(Exception e){}
		}

		int numvarc=dict.gettotalvar();
		variables= new Vector<String>();
		int groupVarCount=0;
		for (int i=0; i<numvarc; i++)
		{
			String tempname=dict.getvarname(i);
			if ((tempname.toLowerCase()).startsWith("g_"))
			{
				variables.add(tempname);
				groupVarCount++;
			}
		}

		variables.add("distance");
		variables.add("first");
		variables.add("second");
		variables.add("newcluster");

		int[] replacerule=new int[variables.size()];

		for (int i=0; i<variables.size()-3; i++)
		{
			replacerule[i]=1;
		}
		replacerule[variables.size()-1]=0;
		replacerule[variables.size()-2]=0;
		replacerule[variables.size()-3]=0;

		DataReader data = new DataReader(dict);
		String[] vals = new String[variables.size()];
		for(int i=0;i<variables.size();i++){
			vals[i]=variables.get(i);
			if (vals[i].equals("first"))
				replacerule[i]=rifrep;
			if (vals[i].equals("second"))
				replacerule[i]=rifrep;
			if (vals[i].equals("newcluster"))
				replacerule[i]=rifrep;
		}
		if (!data.open(vals, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int validgroup=0;
		int pos0=groupVarCount;
		int pos1=pos0+1;
		int pos2=pos1+1;
		int pos3=pos2+1;
		mapping = new Hashtable<Vector<String>,ClusterTree>();
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				validgroup++;
				Vector<String> v = new Vector<String>();
				v.add("Clusters");
				if(groupVarCount!=0){
					v.clear();
					for(int i=0;i<groupVarCount;i++){
						v.add(values[i]);
					}
				}
				ClusterTree ct = mapping.get(v);
				if(ct==null){
					ct = new ClusterTree();
				}
				try
				{
					if (!values[pos0].equals(""))
					{
						ct.addClusterTriple(values[pos1],values[pos2],values[pos3],Double.parseDouble(values[pos0]));
						mapping.put(v,ct);
					}
				}
				catch (Exception ee) {}
			}
		}
		data.close();
		if (validgroup==0)
			return new Result("%2807%<br>\n", false, null);

		int counter=0;

		double ts=0;
		int tn=0;
		if(thickNumber!=null)
		{
			try
			{
				tn = Integer.parseInt(thickNumber);
			}
			catch (Exception e)
			{
				return new Result("%1500%<br>\n", false, null);
			}
		}
		if(thickSize!=null)
		{
			try
			{
				ts = Double.parseDouble(thickSize);
			}
			catch (Exception e)
			{
				return new Result("%1501%<br>\n", false, null);
			}
		}
		double lfs = 16;
		if(fontSize!=null)
		{
			try
			{
				lfs = Double.parseDouble(fontSize);
			}
			catch (Exception e)
			{
				return new Result("%1502%<br>\n", false, null);
			}
		}
		double thfs = 10;
		if(thickFontSize!=null)
		{
			try
			{
				thfs = Double.parseDouble(thickFontSize);
			}
			catch (Exception e)
			{
				return new Result("%1503%<br>\n", false, null);
			}
		}
		double ctat = 0;
		if(cutAt!=null)
		{
			try
			{
				ctat = Double.parseDouble(cutAt);
			}
			catch (Exception e)
			{
				return new Result("%1504%<br>\n", false, null);
			}
		}

		Vector<StepResult> result = new Vector<StepResult>();
		for(Vector<String> v: mapping.keySet()){
			try {
				Dendrogram dg = new Dendrogram(mapping.get(v),title==null?"":title,ts,tn,lfs,thfs,ctat);
				if(fileName==null)
				{
					String frameTitle="";
					int countergroup=0;
					for(String s: v){
						frameTitle+=dict.getvarlabelfromname(groupVarCount==0?"":variables.get(countergroup++))+ ":" +s + " ";
					}
					JInternalFrame frame = new JInternalFrame(frameTitle,true, true,true,true);
					Container container = frame.getContentPane();
					JScrollPane sp = new JScrollPane(dg);
					sp.setWheelScrollingEnabled(true);
					JSlider js = new JSlider(0,100);
					js.setValue((int)(Dendrogram.INITIAL_RATIO*100));
					js.setMinorTickSpacing(5);
					js.setMajorTickSpacing(20);
					js.setPaintTicks(true);
					js.setPaintLabels(true);
					js.addChangeListener(new MyChangeListener(dg));
					MainGUI.desktop.add(frame);
					container.setPreferredSize(new Dimension(400, 100));
					container.add(js,BorderLayout.SOUTH);
					container.add(sp,BorderLayout.CENTER);
					frame.pack();
					frame.setVisible(true);
				}
				else
				{
					if(mapping.size()>1){
						int index = fileName.lastIndexOf(".");
						fileName = fileName.substring(0,index)+counter+++fileName.substring(index,fileName.length());
					}
					try {
						StepResult stepResult =  getPrintableResult(fileName,dg);
						result.add(stepResult);
					} catch (Exception e) {}
				}
			} catch (Exception e)
			{
				return new Result("%1505%<br>\n", false, null);
			}
		}
		return new Result("", true, result);
	}

	private StepResult getPrintableResult(String fileName,Dendrogram dg) throws Exception
	{
		Dimension dm = dg.getPreferredSize();
		Image image = dg.createImage((int)dm.getWidth(),(int)dm.getHeight());
		Image scaledImage = image.getScaledInstance(this.width,this.height,Image.SCALE_AREA_AVERAGING);
		BufferedImage bufferedScaledImmage = new BufferedImage(scaledImage.getWidth(null),scaledImage.getHeight(null),BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d =bufferedScaledImmage.createGraphics();
		g2d.drawImage(scaledImage,0,0,null);
		g2d.dispose();
		ByteArrayOutputStream baos =new ByteArrayOutputStream();
		ImageIO.write(bufferedScaledImmage, "jpg", baos);
		return new LocalFileSave(fileName, baos.toByteArray());
	}

	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1195, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.thicknumber,"text",false,1495,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.thicksize,"text",false,1496,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.unitfontsize,"text",false,1497,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.thickfontsize,"text",false,1498,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.maxdistance,"text",false,1499,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.imgwidth,"text",false,798,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.outjpg, "filesave=.jpg", false, 802, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.imgwidth,"text",false,798,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.imgheight,"text",false,799,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.title,"text",false,800,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1089";
		retprocinfo[1]="1196";
		return retprocinfo;
	}

}

class ClusterTree{


	private HashSet<Subtree> trees;

	public void addClusterTriple(String child, String child2, String father, double distance) {
		Subtree s = new Subtree(child,child2, father, distance);
		Subtree stl,str;
		stl=str=null;
		for(Subtree st: trees){
			if(st.toString().equalsIgnoreCase(child)){
				s.setLChild(st);
				stl = st;
			}
			else if(st.toString().equalsIgnoreCase(child2)){
				s.setRChild(st);
				str=st;
			}
		}
		if(stl!=null){
			trees.remove(stl);
		}
		if(str!=null){
			trees.remove(str);
		}
		trees.add(s);
	}

	public Subtree getRoot(){

		return trees.iterator().next();
	}

	public int getClusterCount(){

		return trees.size();
	}

	public ClusterTree(){

		trees=new HashSet<Subtree>();
	}

}

class Subtree{
	private Subtree lChild;
	private Subtree rChild;
	private String label;
	private double distance;
	public Subtree(String label) {
		this.label = label;
	}

	public Subtree(String child, String child2, String label, double distance) {
		lChild = new Subtree(child);
		rChild = new Subtree(child2);
		this.label = label;
		this.distance = distance;
	}
	public Subtree getLChild() {
		return lChild;
	}

	public Subtree getRChild() {
		return rChild;
	}

	public String toString(){

		return label;
	}

	public boolean isLeaf(){

		return (lChild==null && rChild==null);
	}

	public void setLChild(Subtree child) {
		lChild = child;
	}

	public void setRChild(Subtree child) {
		rChild = child;
	}

	public double getDistance() {
		return distance;
	}
}

class Dendrogram extends JPanel{

	private static final long serialVersionUID = 1L;
	private ClusterTree ct;

	private static final int STARTING_TREE = 200;
	private static final int LABEL_X_MARGIN = 5;
	private static final int LABEL_Y_MARGIN = 5;
	private static final int LEAF_X_OFFSET = 15;
	private static final int MAX_LABEL_SIZE = 64;
	private static final int NUM_LABEL_SIZE = 64;
	private static final int STEP_LENGTH = 2000;
	private static final int BORDER_SIZE = 100;
	private static final int DISTANCE_LINE_Y_OFFSET = 100;
	private static final int THIK_SIZE = 30;
	private static final int TITLE_SIZE = 96;
	private static final int TITLE_X_OFFSET = 10;
	private static final int FONT_MULTIPLIER= 16;

	public static final double INITIAL_RATIO = 0.4;
	private Rectangle2D r2d;
	private int leafCounter;
	private float linesLeftMargin;
	private Subtree root;
	private String  maxLabel;
	private Font font;
	private Color[] colors = {Color.BLACK,Color.BLUE,Color.CYAN,Color.DARK_GRAY,Color.GRAY, Color.GREEN,Color.LIGHT_GRAY,Color.MAGENTA,Color.ORANGE,Color.PINK,Color.RED,Color.YELLOW};
	private double ratio;
	private int maxWidth,maxHeight;
	private double totalDistance;
	private String title;
	private double thickSize;
	private int thickCount;
	private double leafFontSize;
	private double thickFontSize;
	private double cutAt;


	public Dendrogram(ClusterTree ct, String tilte, double thickSize, int thickCount, double leafFontSize, double thickFontSize, double cutAt) throws Exception {
		this.ct = ct;
		if(ct.getClusterCount()!=1){
			throw new Exception();
		}
		leafCounter=0;
		root = ct.getRoot();
		maxLabel="";
		ratio=INITIAL_RATIO;
		totalDistance=0;
		maxLabel(root);
		this.title=tilte;
		Image img = new BufferedImage(800,600,BufferedImage.TYPE_INT_RGB);
		this.thickSize=thickSize;
		this.thickCount=thickCount;
		this.leafFontSize=leafFontSize/FONT_MULTIPLIER;
		this.thickFontSize=thickFontSize/FONT_MULTIPLIER;
		this.cutAt=(cutAt==0)?Double.POSITIVE_INFINITY:cutAt;
		paintComponent(img.getGraphics());
	}

	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		setBackground(Color.WHITE);
		Subtree st = ct.getRoot();
		font = new Font("serif", Font.PLAIN,(int)(MAX_LABEL_SIZE*ratio*leafFontSize));
		r2d = font.getStringBounds(maxLabel,((Graphics2D) g).getFontRenderContext());
		drawTitle(g);
		font = new Font("serif", Font.PLAIN,(int)(MAX_LABEL_SIZE*ratio*leafFontSize));
		maxWidth=maxHeight=0;
		drawCluster(g,st);
		drawDistance(g);
		leafCounter=0;
		setPreferredSize(new Dimension(maxWidth+(int)(BORDER_SIZE*ratio),(title.equals("")?0:(int)(STARTING_TREE*ratio)+LABEL_Y_MARGIN) +maxHeight+(int)((DISTANCE_LINE_Y_OFFSET+BORDER_SIZE)*ratio)));
		revalidate();
	}

	private void maxLabel(Subtree st){

		if(totalDistance<st.getDistance()){
			totalDistance=st.getDistance();
		}
		if(st.isLeaf()){
			maxLabel=(maxLabel.length()<st.toString().length())?st.toString():maxLabel;
		}
		else{
			if(st.getLChild()!=null){
				maxLabel(st.getLChild());
			}
			if(st.getRChild()!=null){
				maxLabel(st.getRChild());
			}
		}
	}

	private void drawTitle(Graphics g){

		Font f = g.getFont();
		font = new Font("serif", Font.BOLD,(int)(TITLE_SIZE*ratio));
		g.setFont(font);
		g.drawString(title,TITLE_X_OFFSET,(int)(STARTING_TREE*ratio*2/3));
		g.setFont(f);
	}

	private void drawDistance(Graphics g){

		int pointAX, pointAY, pointBX, pointBY;
		pointAX = (int) (linesLeftMargin);
		pointAY = (int) (maxHeight+DISTANCE_LINE_Y_OFFSET*ratio);

		g.setColor(Color.BLACK);
		if(thickSize==0)
		{
			thickSize = Math.pow(10,Math.floor(Math.log10(totalDistance>cutAt?cutAt:totalDistance)-1));
		}
		if(thickCount==0)
		{
			thickCount = (int) Math.ceil((totalDistance>cutAt?cutAt:totalDistance)/thickSize);
		}
		else
			thickSize=(totalDistance>cutAt?cutAt:totalDistance)/thickCount;
		int	thickLength = (int) ((thickSize*(maxWidth-pointAX))/(totalDistance>cutAt?cutAt:totalDistance));
		for(int i=0;i<thickCount+1;i++){
			int xa,xb,ya,yb;
			xa = pointAX+(i*thickLength);
			ya = pointAY;
			xb = pointAX+(i*thickLength);
			yb = pointAY+(int)(THIK_SIZE*ratio);
			g.drawLine(xa,ya,xb,yb);	//tacca principale
			NumberFormat formatter = NumberFormat.getNumberInstance();
			formatter.setMaximumFractionDigits((int)Math.abs(Math.floor(Math.log10(totalDistance>cutAt?cutAt:totalDistance)))+1);
			font =new Font("serif", Font.PLAIN,(int)(NUM_LABEL_SIZE*ratio*thickFontSize));
			String label = formatter.format(i*thickSize);	//etichetta
			int offset =(int) font.getStringBounds(label,((Graphics2D) g).getFontRenderContext()).getHeight()/2;
			g.setFont(font);
			g.drawString(label,xb,yb+offset+1);
		}
		for(int i=0;i<thickCount*10;i++){ //sotto tacche delle unità
			g.drawLine(pointAX+(i*thickLength/10),pointAY,pointAX+(i*thickLength/10),pointAY+(int)((THIK_SIZE*ratio))/2);
		}
		pointBX = (thickLength*thickCount+pointAX);
		pointBY = pointAY;
		g.drawLine(pointAX,pointAY,pointBX,pointBY);	//traccio la linea orizzontale
		maxWidth=pointBX;

	}

	public Image createImage(int width, int height){
		BufferedImage buffImm = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = buffImm.createGraphics();
		g2d.setBackground(Color.WHITE);
		g2d.clearRect(0, 0, width, height);
		g2d.setColor(Color.BLACK);
		paintComponent(g2d);
		g2d.dispose();
		return buffImm;
	}

	private Point2D drawCluster(Graphics g, Subtree st){

		Point2D pointA, pointB;
		pointA=pointB=null;

		if(st.isLeaf()){
			return DrawLeaf(st.toString(),g);
		}
		else{
			if(st.getLChild()!=null){
				pointA = drawCluster(g,st.getLChild());
			}
			if(st.getRChild()!=null){
				pointB = drawCluster(g,st.getRChild());
			}

			if(st.getDistance()>cutAt){
				g.setColor(Color.RED);
			}
			else{
				g.setColor(Color.BLACK);
			}
			if(pointA!=null && pointB!=null){
				int endX =Math.round(linesLeftMargin+Math.round(1/(totalDistance>cutAt?cutAt:totalDistance)*STEP_LENGTH*(st.getDistance()>=cutAt?cutAt:st.getDistance())*ratio));
				//int endX =Math.round(linesLeftMargin+Math.round(1/totalDistance*STEP_LENGTH*st.getDistance()*ratio));
				if(pointA.getX()<endX){ //modifiche introdotte per evitare che vengano disegnate linee che partono oltre il limite
					g.drawLine((int)pointA.getX(),(int)pointA.getY(),endX,(int)pointA.getY());
				}
				if(pointB.getX()<endX){
					g.drawLine((int)pointB.getX(),(int)pointB.getY(),endX,(int)pointB.getY());
				}
				if(st.getDistance()<cutAt){
					g.drawLine(endX,(int)pointA.getY(),endX,(int)pointB.getY());
				}
				maxWidth=endX>maxWidth?endX:maxWidth;
				//maxWidth=Math.round(linesLeftMargin+Math.round(1/totalDistance*STEP_LENGTH*st.getDistance()*ratio));
				return new Point2D.Double(endX,Math.abs(pointA.getY()+pointB.getY())/2);
			}
		}
		return null;
	}

	private Point2D.Float DrawLeaf(String label, Graphics g){

		g.setColor(Color.BLACK);
		int ypos=getYPosition(++leafCounter);
		g.setFont(font);
		g.drawString(label,LABEL_X_MARGIN,ypos);
		g.setColor(colors[(leafCounter+colors.length)%colors.length]);
		g.fillOval(LEAF_X_OFFSET+LABEL_X_MARGIN+(int)r2d.getWidth(),ypos-font.getSize()+2,font.getSize(),font.getSize());
		linesLeftMargin=LEAF_X_OFFSET+LABEL_X_MARGIN+Math.round(r2d.getWidth())+font.getSize();
		return new Point2D.Float(linesLeftMargin,ypos-font.getSize()/2+2);
	}

	private int getYPosition(int index){

		int position = (title.equals("")?0:(int)(STARTING_TREE*ratio)+LABEL_Y_MARGIN) + ((font.getSize()+LABEL_Y_MARGIN)*index);
		if(maxHeight<position){
			maxHeight = position;
		}
		return position;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}
}


class MyChangeListener implements ChangeListener{

	  private Dendrogram dg;


	  public MyChangeListener(Dendrogram dg){

		  this.dg=dg;
	  }

    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider)e.getSource();
        if(!slider.getValueIsAdjusting()){
      	dg.setRatio((double)slider.getValue()/100);
      	dg.repaint();
        }
      }
  }
