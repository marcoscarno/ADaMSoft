/**
* Copyright (c) 2017 ADaMSoft
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

package ADaMSoft.gui;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.BoxLayout;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.UndoManager;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.RunStep;
import ADaMSoft.supervisor.ExecuteRunner;
import ADaMSoft.utilities.EndSession;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepInfoGetter;
import ADaMSoft.utilities.License;

/**
* This is the class that creates the Graphical User Interface
* @author marco.scarno@gmail.com
* @date 12/05/2017
*/

public class MainGUI implements ActionListener, UndoableEditListener
{
	Color backTab, foreTab;
	public static boolean muchlog;
	public static JDesktopPane desktop;
	public static JEditorPane MessageArea, OutputArea;
	public static JTextPane EditorArea;
	public static JFrame frame;
	public static JProgressBar barSteps, pbar, genbar;
	public static JInternalFrame frameResults, frameExecutor, frameEnvironment;
	public static JTabbedPane tabsResults, tabsExecutor, tabsEnvironment;
	public static String updatetoinstall;
	public static JPanel menuMessageArea, panelMessageArea;
	public static JScrollPane scrollerMessage, scrollerMenuMessage;
	public static JButton saveMessageArea, deleteMessageArea, deleteOutput, saveOutput;
	public static JButton loadscript, executescript, haltscript, savescript, newscript, addscriptms;
	public static JButton runStep, searchStep, addStepinEditor, runIO;
	public static JButton newPath, delPath, viewDS;
	public static JButton newDefine, delDefine;
	public static JButton newSetting, delSetting;
	public static JButton executeMacrostep;
	public static JButton createInitfiles, IncreaseSizeElements, DecreaseSizeElements, Update, License, Author, viewSources;
	boolean deleted_save_operations, pastexecution, treeOnDS, treeOnDocument;
	protected UndoManager undoManager = new UndoManager();
	Hashtable<String, String> ref_steps;
	TreeMap<String, String> importprocs;
	JTree Steps_Tree, IOTree, PATH_Tree, SETTING_Tree, DEFINE_Tree, MACROSTEP_Tree;
	int LookAndFeel;
	ImageIcon YesWrite, NoWrite, YesRead, NoRead;
	JLabel totalMemory, maxMemory, currentstep, writestate, writeinfo, writingio, readstate,
	readingio, readinfo;
	double tdouble, mdouble;
	String textperc, ttext, mtext, lastopeneddir, tempmenusel;
	DecimalFormat percentmem, intnum;
	public static Document doc, blank;
	boolean isExecuting, updateDownloaded;
	String stepSelection, dsSelection, pathSelection, defineSelection, macrostepSelection, settingSelection, nameSettingSelection;
	public static int numrow, numcol;
	String out_logfile, out_outfile, temp_content;
	String window_selected;
	String[] temp_string_parts;
	Hashtable<String, Vector<String>> listmessages;
	HTMLDocument blankoutini, blanklogini;
	Vector<TreePath> openedPaths;
	TreeSet<String> list_ds;
	SystemWatcher sw;
	boolean state_exeingui;
	boolean minimal;
	boolean already_exit;
	DefaultMutableTreeNode steps_tree;
	/**
	 * Create and start the graphical user interface of ADaMSoft
	 */
	public MainGUI()
	{
		already_exit=false;
		state_exeingui=false;
		String test_exeingui=System.getProperty("exeingui");
		if (test_exeingui!=null)
		{
			if (test_exeingui.equalsIgnoreCase("true")) state_exeingui=true;
		}
		String test_minimal=System.getProperty("minimal");
		if (test_minimal!=null)
		{
			if (test_minimal.equalsIgnoreCase("true")) minimal=true;
		}
		muchlog=false;
		Keywords.MainGUI=this;
		openedPaths= new Vector<TreePath>();
		list_ds=new TreeSet<String>();
		updatetoinstall="";
		updateDownloaded=false;
		Keywords.modifiedscript=false;
		window_selected="";
		out_logfile=System.getProperty("out_logfile");
		out_outfile=System.getProperty("out_outfile");
		isExecuting=false;
		lastopeneddir=(String)System.getProperty("lastOpenedDirectory");
		ImageIcon MsgSave=new ImageIcon(MainGUI.class.getResource("Save.gif"));
		ImageIcon MsgDel=new ImageIcon(MainGUI.class.getResource("Delete.gif"));

		ImageIcon OutDel=new ImageIcon(MainGUI.class.getResource("Delete.gif"));
		ImageIcon OutSave=new ImageIcon(MainGUI.class.getResource("Save.gif"));

		ImageIcon EdLoad=new ImageIcon(MainGUI.class.getResource("Load.gif"));
		ImageIcon EdRun=new ImageIcon(MainGUI.class.getResource("Run.gif"));
		ImageIcon EdSave=new ImageIcon(MainGUI.class.getResource("Save.gif"));
		ImageIcon EdDel=new ImageIcon(MainGUI.class.getResource("Delete.gif"));
		ImageIcon EdAddMs=new ImageIcon(MainGUI.class.getResource("AddMs.gif"));
		ImageIcon EdHalt=new ImageIcon(MainGUI.class.getResource("Halt.gif"));

		ImageIcon PathLoad=new ImageIcon(MainGUI.class.getResource("Load.gif"));
		ImageIcon PathDel=new ImageIcon(MainGUI.class.getResource("Delete.gif"));
		ImageIcon ViewTable=new ImageIcon(MainGUI.class.getResource("ViewTable.png"));

		ImageIcon DefineLoad=new ImageIcon(MainGUI.class.getResource("Load.gif"));
		ImageIcon DefineDel=new ImageIcon(MainGUI.class.getResource("Delete.gif"));

		ImageIcon SettingLoad=new ImageIcon(MainGUI.class.getResource("Load.gif"));
		ImageIcon SettingDel=new ImageIcon(MainGUI.class.getResource("Delete.gif"));

		ImageIcon ProcRun=new ImageIcon(MainGUI.class.getResource("Run.gif"));
		ImageIcon IORun=new ImageIcon(MainGUI.class.getResource("Run.gif"));

		ImageIcon iconCMD = new ImageIcon(MainGUI.class.getResource("CommandScript.png"));
		ImageIcon iconMessages = new ImageIcon(MainGUI.class.getResource("Messages.png"));
		ImageIcon iconOutput = new ImageIcon(MainGUI.class.getResource("Output.png"));

		ImageIcon MacrostepRun=new ImageIcon(MainGUI.class.getResource("Run.gif"));

		ImageIcon iconPath = new ImageIcon(MainGUI.class.getResource("PATH.png"));
		ImageIcon iconDefine = new ImageIcon(MainGUI.class.getResource("Define.png"));
		ImageIcon iconSetting = new ImageIcon(MainGUI.class.getResource("Setting.png"));
		ImageIcon iconMacrostep = new ImageIcon(MainGUI.class.getResource("AddMs.gif"));

		ImageIcon IconProcs=new ImageIcon(MainGUI.class.getResource("Procedures.gif"));
		ImageIcon IconIO=new ImageIcon(MainGUI.class.getResource("ImportExport.gif"));

		ImageIcon IconSysMon=new ImageIcon(MainGUI.class.getResource("SystemMonitor.png"));
		ImageIcon IconOption=new ImageIcon(MainGUI.class.getResource("Options.png"));

		ImageIcon IconSearchProc=new ImageIcon(MainGUI.class.getResource("SearchForProc.png"));
		ImageIcon iconStepCMD = new ImageIcon(MainGUI.class.getResource("CommandScript.png"));

	    YesWrite = new ImageIcon(MainGUI.class.getResource("ADaMSoftRed.png"));
	    NoWrite = new ImageIcon(MainGUI.class.getResource("ADaMSoftGreen.png"));

	    YesRead = new ImageIcon(MainGUI.class.getResource("ADaMSoftRed.png"));
	    NoRead = new ImageIcon(MainGUI.class.getResource("ADaMSoftGreen.png"));

		desktop = new JDesktopPane();
		frame = new JFrame();

		boolean resizable = true;
		boolean closeable = false;
		boolean maximizable = true;
		boolean iconifiable = true;

		String titleResults = Keywords.Language.getMessage(3932);
		String titleExecutor = Keywords.Language.getMessage(3926);
		String titleEnvironment = Keywords.Language.getMessage(3933);

		frameResults = new JInternalFrame(titleResults, resizable, closeable, maximizable, iconifiable);
		frameExecutor = new JInternalFrame(titleExecutor, resizable, closeable, maximizable, iconifiable);
		frameEnvironment = new JInternalFrame(titleEnvironment, resizable, closeable, maximizable, iconifiable);

		MessageArea = new JEditorPane();
		MessageArea.setContentType("text/html");
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(out_logfile));
			temp_content="";
			String line = in.readLine();
			while(line != null)
			{
				temp_content=temp_content+line;
				line = in.readLine();
			}
			in.close();
			MessageArea.setText(temp_content);
		}
		catch (Exception e){}
		MessageArea.setEditable(false);

		scrollerMessage = new JScrollPane(MessageArea);
		scrollerMessage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollerMessage.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		menuMessageArea=new JPanel();
		menuMessageArea.setLayout(new BoxLayout(menuMessageArea, BoxLayout.X_AXIS));
		deleteMessageArea=new JButton(MsgDel);
		deleteMessageArea.setToolTipText(Keywords.Language.getMessage(98));
		deleteMessageArea.addActionListener(this);
		deleteMessageArea.setEnabled(false);
		JPanel pdeleteMessageArea=new JPanel();
		pdeleteMessageArea.setLayout(new BoxLayout(pdeleteMessageArea, BoxLayout.X_AXIS));
		pdeleteMessageArea.add(deleteMessageArea);
		TitledBorder titledeleteMessageArea = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3957));
		pdeleteMessageArea.setBorder(titledeleteMessageArea);
		saveMessageArea=new JButton(MsgSave);
		saveMessageArea.setToolTipText(Keywords.Language.getMessage(99));
		saveMessageArea.addActionListener(this);
		saveMessageArea.setEnabled(false);
		JPanel psaveMessageArea=new JPanel();
		psaveMessageArea.setLayout(new BoxLayout(psaveMessageArea, BoxLayout.X_AXIS));
		psaveMessageArea.add(saveMessageArea);
		TitledBorder titlesaveMessageArea = BorderFactory.createTitledBorder(Keywords.Language.getMessage(95));
		psaveMessageArea.setBorder(titlesaveMessageArea);
		menuMessageArea.add(psaveMessageArea);
		menuMessageArea.add(Box.createGlue());
		menuMessageArea.add(pdeleteMessageArea);
		scrollerMenuMessage = new JScrollPane(menuMessageArea);
		scrollerMenuMessage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerMenuMessage.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		panelMessageArea = new JPanel();
		panelMessageArea.setLayout( new BorderLayout() );
		panelMessageArea.add(scrollerMessage, BorderLayout.CENTER );
		panelMessageArea.add(scrollerMenuMessage, BorderLayout.SOUTH );

		OutputArea = new JEditorPane();
		OutputArea.setContentType("text/html");
		OutputArea.setText("&nbsp;");
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(out_outfile));
			temp_content="";
			String line = in.readLine();
			while(line != null)
			{
				temp_content=temp_content+line;
				line = in.readLine();
			}
			in.close();
			if (!temp_content.equals(""))
				OutputArea.setText(temp_content);
		}
		catch (Exception e){}
		OutputArea.setEditable(false);
		JScrollPane scrollerOutput = new JScrollPane(OutputArea);
		scrollerOutput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollerOutput.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		JPanel menuOutput=new JPanel();
		menuOutput.setLayout(new BoxLayout(menuOutput, BoxLayout.X_AXIS));
		deleteOutput=new JButton(OutDel);
		deleteOutput.setToolTipText(Keywords.Language.getMessage(1610));
		deleteOutput.addActionListener(this);
		JPanel pdeleteOutput=new JPanel();
		pdeleteOutput.setLayout(new BoxLayout(pdeleteOutput, BoxLayout.X_AXIS));
		pdeleteOutput.add(deleteOutput);
		TitledBorder titledeleteOutput = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3957));
		pdeleteOutput.setBorder(titledeleteOutput);
		saveOutput=new JButton(OutSave);
		saveOutput.setToolTipText(Keywords.Language.getMessage(1609));
		saveOutput.addActionListener(this);
		JPanel psaveOutput=new JPanel();
		psaveOutput.setLayout(new BoxLayout(psaveOutput, BoxLayout.X_AXIS));
		psaveOutput.add(saveOutput);
		TitledBorder titlesaveOutput = BorderFactory.createTitledBorder(Keywords.Language.getMessage(95));
		psaveOutput.setBorder(titlesaveOutput);
		deleteOutput.setEnabled(false);
		saveOutput.setEnabled(false);
		menuOutput.add(psaveOutput);
		menuOutput.add(Box.createGlue());
		menuOutput.add(pdeleteOutput);
		JScrollPane scrollerMenuOutput= new JScrollPane(menuOutput);
		scrollerMenuOutput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerMenuOutput.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel panelOutput = new JPanel();
		panelOutput.setLayout( new BorderLayout() );
		panelOutput.add(scrollerOutput, BorderLayout.CENTER );
		panelOutput.add(scrollerMenuOutput, BorderLayout.SOUTH );

		tabsResults=new JTabbedPane();
		tabsResults.addTab(Keywords.Language.getMessage(87), iconMessages, panelMessageArea);
		tabsResults.addTab(Keywords.Language.getMessage(1605), iconOutput, panelOutput);

		frameResults.getContentPane().add(tabsResults);

		EditorArea = new JTextPane();

		JScrollPane scrollerEditor = new JScrollPane(EditorArea);
		scrollerEditor.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerEditor.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel menuExecutor=new JPanel();
		menuExecutor.setLayout(new BoxLayout(menuExecutor, BoxLayout.X_AXIS));

		loadscript=new JButton(EdLoad);
		JPanel ploadscript=new JPanel();
		ploadscript.setLayout(new BoxLayout(ploadscript, BoxLayout.X_AXIS));
		ploadscript.add(loadscript);
		TitledBorder titleloadscript = BorderFactory.createTitledBorder(Keywords.Language.getMessage(94));
		ploadscript.setBorder(titleloadscript);
		executescript=new JButton(EdRun);
		JPanel pexecutescript=new JPanel();
		pexecutescript.setLayout(new BoxLayout(pexecutescript, BoxLayout.X_AXIS));
		pexecutescript.add(executescript);
		TitledBorder titleexecutescript = BorderFactory.createTitledBorder(Keywords.Language.getMessage(170));
		pexecutescript.setBorder(titleexecutescript);
		haltscript=new JButton(EdHalt);
		JPanel phaltscript=new JPanel();
		phaltscript.setLayout(new BoxLayout(phaltscript, BoxLayout.X_AXIS));
		phaltscript.add(haltscript);
		TitledBorder titlehaltscript = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3956));
		phaltscript.setBorder(titlehaltscript);
		savescript=new JButton(EdSave);
		JPanel psavescript=new JPanel();
		psavescript.setLayout(new BoxLayout(psavescript, BoxLayout.X_AXIS));
		psavescript.add(savescript);
		TitledBorder titlesavescript = BorderFactory.createTitledBorder(Keywords.Language.getMessage(95));
		psavescript.setBorder(titlesavescript);
		newscript=new JButton(EdDel);
		JPanel pnewscript=new JPanel();
		pnewscript.setLayout(new BoxLayout(pnewscript, BoxLayout.X_AXIS));
		pnewscript.add(newscript);
		TitledBorder titlenewscript = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3957));
		pnewscript.setBorder(titlenewscript);
		addscriptms=new JButton(EdAddMs);
		JPanel paddscriptms=new JPanel();
		paddscriptms.setLayout(new BoxLayout(paddscriptms, BoxLayout.X_AXIS));
		paddscriptms.add(addscriptms);
		TitledBorder titleaddscriptms = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3958));
		paddscriptms.setBorder(titleaddscriptms);

		loadscript.setToolTipText(Keywords.Language.getMessage(103));
		executescript.setToolTipText(Keywords.Language.getMessage(105));
		haltscript.setToolTipText(Keywords.Language.getMessage(155));
		savescript.setToolTipText(Keywords.Language.getMessage(104));
		newscript.setToolTipText(Keywords.Language.getMessage(102));
		addscriptms.setToolTipText(Keywords.Language.getMessage(2216));

		loadscript.addActionListener(this);
		executescript.addActionListener(this);
		haltscript.addActionListener(this);
		haltscript.setEnabled(false);
		savescript.addActionListener(this);
		newscript.addActionListener(this);
		addscriptms.addActionListener(this);
		executescript.setEnabled(false);
		haltscript.setEnabled(false);
		savescript.setEnabled(false);
		newscript.setEnabled(false);
		addscriptms.setEnabled(false);

		EditorArea.addKeyListener(new KeyListener()
		{
			public void keyTyped(KeyEvent e)
			{
				if (EditorArea.getText().trim().equals(""))
				{
					executescript.setEnabled(false);
					haltscript.setEnabled(false);
					savescript.setEnabled(false);
					newscript.setEnabled(false);
					addscriptms.setEnabled(false);
				}
				else
				{
					Keywords.modifiedscript=true;
					executescript.setEnabled(true);
					savescript.setEnabled(true);
					newscript.setEnabled(true);
					addscriptms.setEnabled(true);
				}
				Keywords.modifiedscript = true;
			}
			public void keyPressed(KeyEvent e)
			{
				if (EditorArea.getText().trim().equals(""))
				{
					executescript.setEnabled(false);
					haltscript.setEnabled(false);
					savescript.setEnabled(false);
					newscript.setEnabled(false);
					addscriptms.setEnabled(false);
				}
				else
				{
					Keywords.modifiedscript=true;
					executescript.setEnabled(true);
					savescript.setEnabled(true);
					newscript.setEnabled(true);
					addscriptms.setEnabled(true);
				}
				Keywords.modifiedscript = true;
			}
			public void keyReleased(KeyEvent e)
			{
				int keycode=e.getKeyCode();
				boolean ctrlkey = e.isControlDown();
				if(keycode==90 && ctrlkey)
				{
					try
					{
						if(undoManager.canUndo())
						{
							undoManager.undo();
						}
					}
					catch (Exception ex){}
				}
				else if(keycode==89  && ctrlkey)
				{
					try
					{
						if(undoManager.canRedo())
						{
							undoManager.redo();
						}
					}
					catch (Exception ex){}
				}
				if (EditorArea.getText().trim().equals(""))
				{
					executescript.setEnabled(false);
					haltscript.setEnabled(false);
					savescript.setEnabled(false);
					newscript.setEnabled(false);
					addscriptms.setEnabled(false);
				}
				else
				{
					Keywords.modifiedscript=true;
					executescript.setEnabled(true);
					savescript.setEnabled(true);
					newscript.setEnabled(true);
					addscriptms.setEnabled(true);
				}
			}
		});
		EditorArea.getDocument().addUndoableEditListener(this);
		EditorArea.getDocument().addDocumentListener(new DocumentListener()
		{
		    public void insertUpdate(DocumentEvent e)
		    {
				if (EditorArea.getText().trim().equals(""))
				{
					executescript.setEnabled(false);
					haltscript.setEnabled(false);
					savescript.setEnabled(false);
					newscript.setEnabled(false);
					addscriptms.setEnabled(false);
				}
				else
				{
					Keywords.modifiedscript=true;
					executescript.setEnabled(true);
					savescript.setEnabled(true);
					newscript.setEnabled(true);
					addscriptms.setEnabled(true);
				}
		    }
		    public void removeUpdate(DocumentEvent e)
		    {
				if (EditorArea.getText().trim().equals(""))
				{
					executescript.setEnabled(false);
					haltscript.setEnabled(false);
					savescript.setEnabled(false);
					newscript.setEnabled(false);
					addscriptms.setEnabled(false);
				}
				else
				{
					Keywords.modifiedscript=true;
					executescript.setEnabled(true);
					savescript.setEnabled(true);
					newscript.setEnabled(true);
					addscriptms.setEnabled(true);
				}
		    }
		    public void changedUpdate(DocumentEvent e)
		    {
				if (EditorArea.getText().trim().equals(""))
				{
					executescript.setEnabled(false);
					haltscript.setEnabled(false);
					savescript.setEnabled(false);
					newscript.setEnabled(false);
					addscriptms.setEnabled(false);
				}
				else
				{
					Keywords.modifiedscript=true;
					executescript.setEnabled(true);
					savescript.setEnabled(true);
					newscript.setEnabled(true);
					addscriptms.setEnabled(true);
				}
		    }
		});

		menuExecutor.add(ploadscript);
		menuExecutor.add(Box.createGlue());
		menuExecutor.add(pexecutescript);
		menuExecutor.add(Box.createGlue());
		menuExecutor.add(phaltscript);
		menuExecutor.add(Box.createGlue());
		menuExecutor.add(psavescript);
		menuExecutor.add(Box.createGlue());
		menuExecutor.add(pnewscript);
		menuExecutor.add(Box.createGlue());
		menuExecutor.add(paddscriptms);

		tabsExecutor=new JTabbedPane();

		JPanel panelEdit = new JPanel();
		panelEdit.setLayout( new BorderLayout() );
		panelEdit.add(scrollerEditor, BorderLayout.CENTER );

		JScrollPane scrollerPME = new JScrollPane(menuExecutor);
		scrollerPME.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerPME.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		panelEdit.add(scrollerPME, BorderLayout.SOUTH);

		steps_tree = new DefaultMutableTreeNode(Keywords.Language.getMessage(3927));

		ref_steps=new Hashtable<String, String>();

		StepInfoGetter psif=new StepInfoGetter();
		TreeMap<String, TreeMap<String, String>> proc_info = psif.getStepInfo(Keywords.PROC);
		importprocs=new TreeMap<String, String>();
		if (!proc_info.isEmpty())
		{
			DefaultMutableTreeNode nodeGroup = new DefaultMutableTreeNode(Keywords.Language.getMessage(3929));
			for (Iterator<String> i = proc_info.keySet().iterator(); i.hasNext();)
			{
				String group=i.next();
				DefaultMutableTreeNode nodeGroupStep = new DefaultMutableTreeNode(group);
				TreeMap<String, String> allgdoc=proc_info.get(group);
				for (Iterator<String> j = allgdoc.keySet().iterator(); j.hasNext();)
				{
					String name=j.next();
					String perc=allgdoc.get(name);
					if (perc.endsWith("2dict"))
						importprocs.put(Keywords.Language.getMessage(name), perc);
					DefaultMutableTreeNode nodeStep = new DefaultMutableTreeNode(Keywords.Language.getMessage(name));
					nodeGroupStep.add(nodeStep);
					ref_steps.put(group.trim()+"_"+Keywords.Language.getMessage(name).trim(), perc);
				}
				nodeGroup.add(nodeGroupStep);
			}
			steps_tree.add(nodeGroup);
		}
		listmessages=psif.getTexts();

		StepInfoGetter sif=new StepInfoGetter();
		TreeMap<String, TreeMap<String, String>> document_info = sif.getStepInfo(Keywords.DOCUMENT);
		if (!document_info.isEmpty())
		{
			DefaultMutableTreeNode nodeGroup = new DefaultMutableTreeNode(Keywords.Language.getMessage(3928));
			for (Iterator<String> i = document_info.keySet().iterator(); i.hasNext();)
			{
				String group=i.next();
				DefaultMutableTreeNode nodeGroupStep= new DefaultMutableTreeNode(group);
				TreeMap<String, String> allgdoc=document_info.get(group);
				for (Iterator<String> j = allgdoc.keySet().iterator(); j.hasNext();)
				{
					String name=j.next();
					DefaultMutableTreeNode nodeStep = new DefaultMutableTreeNode(Keywords.Language.getMessage(name));
					nodeGroupStep.add(nodeStep);
					String perc=allgdoc.get(name);
					ref_steps.put(group.trim()+"_"+Keywords.Language.getMessage(name).trim(), perc);
				}
				nodeGroup.add(nodeGroupStep);
			}
			steps_tree.add(nodeGroup);
		}

		TreeMap<String, TreeMap<String, String>> report_info = sif.getStepInfo(Keywords.REPORT);
		if (!report_info.isEmpty())
		{
			DefaultMutableTreeNode nodeGroup = new DefaultMutableTreeNode(Keywords.Language.getMessage(3930));
			for (Iterator<String> i = report_info.keySet().iterator(); i.hasNext();)
			{
				String group=i.next();
				DefaultMutableTreeNode nodeGroupStep= new DefaultMutableTreeNode(group);
				TreeMap<String, String> allgdoc=report_info.get(group);
				for (Iterator<String> j = allgdoc.keySet().iterator(); j.hasNext();)
				{
					String name=j.next();
					DefaultMutableTreeNode nodeStep = new DefaultMutableTreeNode(Keywords.Language.getMessage(name));
					nodeGroupStep.add(nodeStep);
					String perc=allgdoc.get(name);
					ref_steps.put(group.trim()+"_"+Keywords.Language.getMessage(name).trim(), perc);
				}
				nodeGroup.add(nodeGroupStep);
			}
			steps_tree.add(nodeGroup);
		}

		TreeMap<String, TreeMap<String, String>> toc_info = sif.getStepInfo(Keywords.TOC);
		if (!toc_info.isEmpty())
		{
			DefaultMutableTreeNode nodeGroup= new DefaultMutableTreeNode(Keywords.Language.getMessage(3931));
			for (Iterator<String> i = toc_info.keySet().iterator(); i.hasNext();)
			{
				String group=i.next();
				DefaultMutableTreeNode nodeGroupStep= new DefaultMutableTreeNode(group);
				TreeMap<String, String> allgdoc=toc_info.get(group);
				for (Iterator<String> j = allgdoc.keySet().iterator(); j.hasNext();)
				{
					String name=j.next();
					DefaultMutableTreeNode nodeStep = new DefaultMutableTreeNode(Keywords.Language.getMessage(name));
					nodeGroupStep.add(nodeStep);
					String perc=allgdoc.get(name);
					ref_steps.put(group.trim()+"_"+Keywords.Language.getMessage(name).trim(), perc);
				}
				nodeGroup.add(nodeGroupStep);
			}
			steps_tree.add(nodeGroup);
		}

		Steps_Tree = new JTree(steps_tree);
		JScrollPane scrollerSTree = new JScrollPane(Steps_Tree);
		scrollerSTree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerSTree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel menuStep=new JPanel();
		menuStep.setLayout(new BoxLayout(menuStep, BoxLayout.X_AXIS));

		runStep=new JButton(ProcRun);
		JPanel prunStep=new JPanel();
		prunStep.setLayout(new BoxLayout(prunStep, BoxLayout.X_AXIS));
		prunStep.add(runStep);
		TitledBorder titlerunStep = BorderFactory.createTitledBorder(Keywords.Language.getMessage(170));
		prunStep.setBorder(titlerunStep);
		runStep.setToolTipText(Keywords.Language.getMessage(3927));
		runStep.addActionListener(this);
		runStep.setEnabled(false);

		searchStep=new JButton(IconSearchProc);
		JPanel psearchStep=new JPanel();
		psearchStep.setLayout(new BoxLayout(psearchStep, BoxLayout.X_AXIS));
		psearchStep.add(searchStep);
		TitledBorder titlesearchStep = BorderFactory.createTitledBorder(Keywords.Language.getMessage(4006));
		psearchStep.setBorder(titlesearchStep);
		searchStep.setToolTipText(Keywords.Language.getMessage(4007));
		searchStep.addActionListener(this);
		searchStep.setEnabled(true);

		addStepinEditor=new JButton(iconStepCMD);
		JPanel paddStepinEditor=new JPanel();
		paddStepinEditor.setLayout(new BoxLayout(paddStepinEditor, BoxLayout.X_AXIS));
		paddStepinEditor.add(addStepinEditor);
		TitledBorder titleaddStepinEditor = BorderFactory.createTitledBorder(Keywords.Language.getMessage(4008));
		paddStepinEditor.setBorder(titleaddStepinEditor);
		addStepinEditor.setToolTipText(Keywords.Language.getMessage(4009));
		addStepinEditor.addActionListener(this);
		addStepinEditor.setEnabled(false);

		menuStep.add(prunStep);
		menuStep.add(Box.createGlue());
		menuStep.add(psearchStep);
		menuStep.add(Box.createGlue());
		menuStep.add(paddStepinEditor);

		stepSelection="";

		Steps_Tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				stepSelection="";
				tempmenusel="";
				addStepinEditor.setEnabled(false);
				runStep.setEnabled(false);
				try
				{
					DefaultMutableTreeNode selectedNode =(DefaultMutableTreeNode)Steps_Tree.getLastSelectedPathComponent();
					if (selectedNode!=null )
					{
						if (selectedNode.getParent()!=null)
						{
							tempmenusel=selectedNode.getParent().toString();
							tempmenusel=tempmenusel.trim()+"_"+selectedNode.toString();
							if (ref_steps.get(tempmenusel)!=null && !isExecuting)
							{
								stepSelection=ref_steps.get(tempmenusel);
								addStepinEditor.setEnabled(true);
								runStep.setEnabled(true);
							}
						}
					}
				}
				catch (Exception ecs){}
			}
		});
		Steps_Tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		JPanel panelStep = new JPanel();
		panelStep.setLayout( new BorderLayout() );
		panelStep.add(scrollerSTree, BorderLayout.CENTER );

		JScrollPane scrollerSME = new JScrollPane(menuStep);
		scrollerSME.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerSME.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		panelStep.add(scrollerSME, BorderLayout.SOUTH);

		DefaultMutableTreeNode nodeIO = new DefaultMutableTreeNode(Keywords.Language.getMessage(2625));
		if (!importprocs.isEmpty())
		{
			DefaultMutableTreeNode nodeI = new DefaultMutableTreeNode(Keywords.Language.getMessage(2517));
			for (Iterator<String> i = importprocs.keySet().iterator(); i.hasNext();)
			{
				String group=i.next();
				DefaultMutableTreeNode nodeGroupDocument = new DefaultMutableTreeNode(group);
				nodeI.add(nodeGroupDocument);
			}
			nodeIO.add(nodeI);
		}
		DefaultMutableTreeNode nodeExport = new DefaultMutableTreeNode(Keywords.Language.getMessage(2873));
		nodeIO.add(nodeExport);

		IOTree=new JTree(nodeIO);
		IOTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		JScrollPane scrollerIOTree = new JScrollPane(IOTree);
		scrollerIOTree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerIOTree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel menuIO=new JPanel();
		menuIO.setLayout(new BoxLayout(menuIO, BoxLayout.X_AXIS));

		runIO=new JButton(IORun);
		JPanel prunIO=new JPanel();
		prunIO.setLayout(new BoxLayout(prunIO, BoxLayout.X_AXIS));
		prunIO.add(runIO);
		TitledBorder titlerunIO = BorderFactory.createTitledBorder(Keywords.Language.getMessage(170));
		prunIO.setBorder(titlerunIO);
		runIO.setToolTipText(Keywords.Language.getMessage(3927));
		runIO.addActionListener(this);
		runIO.setEnabled(false);

		IOTree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				stepSelection="";
				try
				{
					DefaultMutableTreeNode selectedNode =
					(DefaultMutableTreeNode)IOTree.getLastSelectedPathComponent();
					if (selectedNode!=null )
					{
						if (importprocs.get(selectedNode.toString())!=null && !isExecuting)
						{
							stepSelection=importprocs.get(selectedNode.toString());
							runIO.setEnabled(true);
						}
						else
						{
							stepSelection="";
							runIO.setEnabled(false);
						}
					}
					if (selectedNode.toString().equals(Keywords.Language.getMessage(2873)))
					{
						stepSelection="ProcExportdatatable";
						runIO.setEnabled(true);
					}
				}
				catch (Exception ecs){}
			}
		});

		menuIO.add(Box.createGlue());
		menuIO.add(prunIO);
		menuIO.add(Box.createGlue());

		JPanel panelIO = new JPanel();
		panelIO.setLayout( new BorderLayout() );
		panelIO.add(scrollerIOTree, BorderLayout.CENTER );

		JScrollPane scrollerIOME = new JScrollPane(menuIO);
		scrollerIOME.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerIOME.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		panelIO.add(scrollerIOME, BorderLayout.SOUTH);

		tabsExecutor.addTab(Keywords.Language.getMessage(2625), IconIO, panelIO);
		tabsExecutor.addTab(Keywords.Language.getMessage(3927), IconProcs, panelStep);
		tabsExecutor.addTab(Keywords.Language.getMessage(88)+" "+Keywords.Language.getMessage(2156), iconCMD, panelEdit);

		frameExecutor.getContentPane().add(tabsExecutor);

		DefaultMutableTreeNode nodePath = new DefaultMutableTreeNode(Keywords.Language.getMessage(185));
		PATH_Tree=new JTree(nodePath);
		PATH_Tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				dsSelection="";
				pathSelection="";
				treeOnDS=false;
				treeOnDocument=false;
				try
				{
					DefaultMutableTreeNode selectedNode =(DefaultMutableTreeNode)PATH_Tree.getLastSelectedPathComponent();
					try
					{
						String dataset_reference=selectedNode.getParent().toString();
						if (Keywords.Language.getMessage(1396).equals(dataset_reference))
						{
							treeOnDS=true;
							viewDS.setEnabled(true);
							delPath.setEnabled(true);
							dsSelection=selectedNode.toString();
						}
						else if (Keywords.Language.getMessage(185).equals(dataset_reference))
						{
							treeOnDS=false;
							viewDS.setEnabled(false);
							delPath.setEnabled(true);
							pathSelection=selectedNode.toString();
						}
						else if (Keywords.Language.getMessage(1397).equals(dataset_reference))
						{
							treeOnDocument=true;
							viewDS.setEnabled(true);
							delPath.setEnabled(true);
							dsSelection=selectedNode.toString();
						}
					}
					catch (Exception ef){}
					if (dsSelection.equals("") && pathSelection.equals(""))
					{
						viewDS.setEnabled(false);
						delPath.setEnabled(false);
					}
				}
				catch (Exception ecs){}
			}
		});
		PATH_Tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		JScrollPane scrollerPathTree = new JScrollPane(PATH_Tree);
		scrollerPathTree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerPathTree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel menuPATH=new JPanel();
		menuPATH.setLayout(new BoxLayout(menuPATH, BoxLayout.X_AXIS));

		newPath=new JButton(PathLoad);
		newPath.setToolTipText(Keywords.Language.getMessage(223));
		newPath.addActionListener(this);
		newPath.setEnabled(true);
		JPanel pnewPath=new JPanel();
		pnewPath.setLayout(new BoxLayout(pnewPath, BoxLayout.X_AXIS));
		pnewPath.add(newPath);
		TitledBorder titlenewPath = BorderFactory.createTitledBorder(Keywords.Language.getMessage(226));
		pnewPath.setBorder(titlenewPath);

		delPath=new JButton(PathDel);
		delPath.setToolTipText(Keywords.Language.getMessage(222));
		delPath.addActionListener(this);
		delPath.setEnabled(false);
		JPanel pdelPath=new JPanel();
		pdelPath.setLayout(new BoxLayout(pdelPath, BoxLayout.X_AXIS));
		pdelPath.add(delPath);
		TitledBorder titledelPath = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3959));
		pdelPath.setBorder(titledelPath);

		viewDS=new JButton(ViewTable);
		viewDS.setToolTipText(Keywords.Language.getMessage(221));
		viewDS.addActionListener(this);
		viewDS.setEnabled(false);
		JPanel pviewDS=new JPanel();
		pviewDS.setLayout(new BoxLayout(pviewDS, BoxLayout.X_AXIS));
		pviewDS.add(viewDS);
		TitledBorder titleviewDS = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3960));
		pviewDS.setBorder(titleviewDS);

		menuPATH.add(pnewPath);
		menuPATH.add(Box.createGlue());
		menuPATH.add(pdelPath);
		menuPATH.add(Box.createGlue());
		menuPATH.add(pviewDS);

		JPanel panelPath = new JPanel();
		panelPath.setLayout( new BorderLayout() );
		panelPath.add(scrollerPathTree, BorderLayout.CENTER );

		JScrollPane scrollerPM = new JScrollPane(menuPATH);
		scrollerPM.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerPM.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		panelPath.add(scrollerPM, BorderLayout.SOUTH);

		tabsEnvironment=new JTabbedPane();

		tabsEnvironment.addTab(Keywords.Language.getMessage(185), iconPath, panelPath);

		DefaultMutableTreeNode nodeDefine = new DefaultMutableTreeNode(Keywords.Language.getMessage(188));
		DEFINE_Tree=new JTree(nodeDefine);
		DEFINE_Tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				defineSelection="";
				try
				{
					DefaultMutableTreeNode selectedNode =(DefaultMutableTreeNode)DEFINE_Tree.getLastSelectedPathComponent();
					try
					{
						String define_reference=selectedNode.getParent().toString();
						if (Keywords.Language.getMessage(188).equals(define_reference))
						{
							delDefine.setEnabled(true);
							defineSelection=selectedNode.toString();
							temp_string_parts=defineSelection.split("=");
							defineSelection=temp_string_parts[0];
						}
						else delDefine.setEnabled(false);
					}
					catch (Exception ef)
					{
						delDefine.setEnabled(false);
					}
				}
				catch (Exception ecs){}
			}
		});
		DEFINE_Tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		JScrollPane scrollerDefineTree = new JScrollPane(DEFINE_Tree);
		scrollerDefineTree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerDefineTree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel menuDEFINE=new JPanel();
		menuDEFINE.setLayout(new BoxLayout(menuDEFINE, BoxLayout.X_AXIS));

		newDefine=new JButton(DefineLoad);
		newDefine.setToolTipText(Keywords.Language.getMessage(209));
		newDefine.addActionListener(this);
		newDefine.setEnabled(true);
		JPanel pnewDefine=new JPanel();
		pnewDefine.setLayout(new BoxLayout(pnewDefine, BoxLayout.X_AXIS));
		pnewDefine.add(newDefine);
		TitledBorder titlenewDefine = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3961));
		pnewDefine.setBorder(titlenewDefine);

		delDefine=new JButton(DefineDel);
		delDefine.setToolTipText(Keywords.Language.getMessage(207));
		delDefine.addActionListener(this);
		delDefine.setEnabled(false);
		JPanel pdelDefine=new JPanel();
		pdelDefine.setLayout(new BoxLayout(pdelDefine, BoxLayout.X_AXIS));
		pdelDefine.add(delDefine);
		TitledBorder titledelDefine = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3959));
		pdelDefine.setBorder(titledelDefine);

		menuDEFINE.add(pnewDefine);
		menuDEFINE.add(Box.createGlue());
		menuDEFINE.add(pdelDefine);

		JPanel panelDefine = new JPanel();
		panelDefine.setLayout( new BorderLayout() );
		panelDefine.add(scrollerDefineTree, BorderLayout.CENTER );

		JScrollPane scrollerDM = new JScrollPane(menuDEFINE);
		scrollerDM.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerDM.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		panelDefine.add(scrollerDM, BorderLayout.SOUTH);

		tabsEnvironment.addTab(Keywords.Language.getMessage(188), iconDefine, panelDefine);

		DefaultMutableTreeNode nodeSetting = new DefaultMutableTreeNode(Keywords.Language.getMessage(186));
		SETTING_Tree=new JTree(nodeSetting);
		SETTING_Tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				settingSelection="";
				nameSettingSelection="";
				try
				{
					DefaultMutableTreeNode selectedNode =(DefaultMutableTreeNode)SETTING_Tree.getLastSelectedPathComponent();
					String setting_reference=selectedNode.getParent().toString();
					if (Keywords.Language.getMessage(186).equals(setting_reference))
					{
						newSetting.setEnabled(true);
						delSetting.setEnabled(false);
						settingSelection=selectedNode.toString();
					}
					else
					{
						try
						{
							setting_reference=selectedNode.getParent().getParent().toString();
							if (Keywords.Language.getMessage(186).equals(setting_reference))
							{
								delSetting.setEnabled(true);
								newSetting.setEnabled(false);
								settingSelection=selectedNode.getParent().toString();
								nameSettingSelection=selectedNode.toString();
							}
							else
							{
								newSetting.setEnabled(false);
								delSetting.setEnabled(false);
							}
						}
						catch (Exception ec)
						{
							newSetting.setEnabled(false);
							delSetting.setEnabled(false);
						}
					}
				}
				catch (Exception ecs){}
			}
		});
		SETTING_Tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		JScrollPane scrollerSettingTree = new JScrollPane(SETTING_Tree);
		scrollerSettingTree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerSettingTree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel menuSETTING=new JPanel();
		menuSETTING.setLayout(new BoxLayout(menuSETTING, BoxLayout.X_AXIS));

		newSetting=new JButton(SettingLoad);
		newSetting.setToolTipText(Keywords.Language.getMessage(399));
		newSetting.addActionListener(this);
		newSetting.setEnabled(false);
		JPanel pnewSetting=new JPanel();
		pnewSetting.setLayout(new BoxLayout(pnewSetting, BoxLayout.X_AXIS));
		pnewSetting.add(newSetting);
		TitledBorder titlenewSetting = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3961));
		pnewSetting.setBorder(titlenewSetting);

		delSetting=new JButton(SettingDel);
		delSetting.setToolTipText(Keywords.Language.getMessage(402));
		delSetting.addActionListener(this);
		delSetting.setEnabled(false);
		JPanel pdelSettinge=new JPanel();
		pdelSettinge.setLayout(new BoxLayout(pdelSettinge, BoxLayout.X_AXIS));
		pdelSettinge.add(delSetting);
		TitledBorder titledelSetting = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3959));
		pdelSettinge.setBorder(titledelSetting);

		menuSETTING.add(pnewSetting);
		menuSETTING.add(Box.createGlue());
		menuSETTING.add(pdelSettinge);

		JPanel panelSetting = new JPanel();
		panelSetting.setLayout( new BorderLayout() );
		panelSetting.add(scrollerSettingTree, BorderLayout.CENTER );

		JScrollPane scrollerSM = new JScrollPane(menuSETTING);
		scrollerSM.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerSM.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		panelSetting.add(scrollerSM, BorderLayout.SOUTH);

		tabsEnvironment.addTab(Keywords.Language.getMessage(186), iconSetting, panelSetting);

		DefaultMutableTreeNode nodeMacrostep = new DefaultMutableTreeNode(Keywords.Language.getMessage(2188));
		MACROSTEP_Tree=new JTree(nodeMacrostep);
		MACROSTEP_Tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				macrostepSelection="";
				DefaultMutableTreeNode selectedNode =(DefaultMutableTreeNode)MACROSTEP_Tree.getLastSelectedPathComponent();
				try
				{
					String macrostep_reference=selectedNode.getParent().toString();
					if (Keywords.Language.getMessage(2188).equalsIgnoreCase(macrostep_reference))
					{
						executeMacrostep.setEnabled(true);
						macrostepSelection=selectedNode.toString();
					}
					else executeMacrostep.setEnabled(false);
				}
				catch (Exception ef)
				{
					executeMacrostep.setEnabled(false);
				}
			}
		});
		MACROSTEP_Tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		JScrollPane scrollerMacrostepTree = new JScrollPane(MACROSTEP_Tree);
		scrollerMacrostepTree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerMacrostepTree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel menuMACROSTEP=new JPanel();
		menuMACROSTEP.setLayout(new BoxLayout(menuMACROSTEP, BoxLayout.X_AXIS));

		executeMacrostep=new JButton(MacrostepRun);
		executeMacrostep.setToolTipText(Keywords.Language.getMessage(2215));
		executeMacrostep.addActionListener(this);
		executeMacrostep.setEnabled(false);
		JPanel pexecuteMacrostep=new JPanel();
		pexecuteMacrostep.setLayout(new BoxLayout(pexecuteMacrostep, BoxLayout.X_AXIS));
		pexecuteMacrostep.add(executeMacrostep);
		TitledBorder titleexecuteMacrostep = BorderFactory.createTitledBorder(Keywords.Language.getMessage(170));
		pexecuteMacrostep.setBorder(titleexecuteMacrostep);

		menuMACROSTEP.add(Box.createGlue());
		menuMACROSTEP.add(pexecuteMacrostep);
		menuMACROSTEP.add(Box.createGlue());

		JPanel panelMacrostep = new JPanel();
		panelMacrostep.setLayout( new BorderLayout() );
		panelMacrostep.add(scrollerMacrostepTree, BorderLayout.CENTER );

		JScrollPane scrollerMM = new JScrollPane(menuMACROSTEP);
		scrollerMM.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerMM.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		panelMacrostep.add(scrollerMM, BorderLayout.SOUTH);

		tabsEnvironment.addTab(Keywords.Language.getMessage(2188), iconMacrostep, panelMacrostep);

        JPanel PanelSystem = new JPanel(new GridLayout(2, 1));
		JPanel panelSysGen = new JPanel();
		BoxLayout layoutSysGen = new BoxLayout(panelSysGen, BoxLayout.Y_AXIS);
		panelSysGen.setLayout(layoutSysGen);

		JPanel panelSysIO = new JPanel(new GridLayout(2, 3));

		totalMemory=new JLabel("");
		maxMemory=new JLabel("");

		tdouble = (Runtime.getRuntime().totalMemory())/1024;
		mdouble = (Runtime.getRuntime().maxMemory())/1024;
		percentmem = new DecimalFormat("##.#");
		intnum = new DecimalFormat("########################");
		textperc = percentmem.format(100*tdouble/mdouble);
		ttext=Keywords.Language.getMessage(2527)+"= ";
		mtext=Keywords.Language.getMessage(2528)+"= ";
		totalMemory.setText(ttext+intnum.format(tdouble)+ " ("+textperc+"%)");
		maxMemory.setText(mtext+intnum.format(mdouble));

		TitledBorder titleSystem = BorderFactory.createTitledBorder(Keywords.Language.getMessage(2525));

		PanelSystem.setBorder(titleSystem);
		PanelSystem.add(totalMemory);
		PanelSystem.add(maxMemory);

		TitledBorder titleio = BorderFactory.createTitledBorder(Keywords.Language.getMessage(2529));

		writeinfo=new JLabel(Keywords.Language.getMessage(2530));
		readinfo=new JLabel(Keywords.Language.getMessage(2531));
		readstate=new JLabel("");
		writestate=new JLabel("");

		writestate.setIcon(NoWrite);
		readstate.setIcon(NoRead);

		writingio=new JLabel(" - ");
		readingio=new JLabel(" - ");

		panelSysIO.add(writeinfo);
		panelSysIO.add(writingio);
		panelSysIO.add(writestate);
		panelSysIO.add(readinfo);
		panelSysIO.add(readingio);
		panelSysIO.add(readstate);

		panelSysIO.setBorder(titleio);

		TitledBorder titlecs = BorderFactory.createTitledBorder(Keywords.Language.getMessage(2907));

		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		Keywords.general_percentage_total=0;
		Keywords.general_percentage_done=0;

		JPanel panelcstep=new JPanel(new GridLayout(1,1));
		currentstep=new JLabel("-");
		panelcstep.add(currentstep);
		panelcstep.setBorder(titlecs);

		TitledBorder titleci = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3696));
		JPanel panelistep=new JPanel(new GridLayout(1,1));
		pbar=new JProgressBar();
		//pbar.setStringPainted(true);
		pbar.setMaximum(Keywords.percentage_total);
		pbar.setValue(Keywords.percentage_done);
		panelistep.add(pbar);
		panelistep.setBorder(titleci);

		TitledBorder titleGenPerc = BorderFactory.createTitledBorder(Keywords.Language.getMessage(3934));
		JPanel panelgp=new JPanel(new GridLayout(1,1));
		genbar=new JProgressBar();
		genbar.setMaximum(Keywords.general_percentage_total);
		genbar.setValue(Keywords.general_percentage_done);
		//genbar.setStringPainted(true);
		panelgp.add(genbar);
		panelgp.setBorder(titleGenPerc);

		panelSysGen.add(Box.createVerticalGlue());
		panelSysGen.add(PanelSystem);
		panelSysGen.add(Box.createVerticalGlue());
		panelSysGen.add(panelSysIO);
		panelSysGen.add(Box.createVerticalGlue());
		panelSysGen.add(panelgp);
		panelSysGen.add(Box.createVerticalGlue());
		panelSysGen.add(panelcstep);
		panelSysGen.add(Box.createVerticalGlue());
		panelSysGen.add(panelistep);
		panelSysGen.add(Box.createVerticalGlue());

		JScrollPane scrollerSystemMonitor = new JScrollPane(panelSysGen);
		scrollerSystemMonitor.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerSystemMonitor.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		tabsEnvironment.addTab(Keywords.Language.getMessage(2526), IconSysMon, scrollerSystemMonitor);

		JPanel panelOption = new JPanel();
		BoxLayout layout = new BoxLayout(panelOption, BoxLayout.Y_AXIS);
		panelOption.setLayout(layout);

		IncreaseSizeElements=new JButton(Keywords.Language.getMessage(3935));
		DecreaseSizeElements=new JButton(Keywords.Language.getMessage(3936));
		/*Update=new JButton(Keywords.Language.getMessage(1711));*/
		License=new JButton(Keywords.Language.getMessage(121));
		Author=new JButton(Keywords.Language.getMessage(124));
		createInitfiles=new JButton(Keywords.Language.getMessage(3978));
		viewSources=new JButton(Keywords.Language.getMessage(3979));

		IncreaseSizeElements.setAlignmentX(Component.CENTER_ALIGNMENT);
		DecreaseSizeElements.setAlignmentX(Component.CENTER_ALIGNMENT);
		/*Update.setAlignmentX(Component.CENTER_ALIGNMENT);*/
		License.setAlignmentX(Component.CENTER_ALIGNMENT);
		Author.setAlignmentX(Component.CENTER_ALIGNMENT);
		createInitfiles.setAlignmentX(Component.CENTER_ALIGNMENT);
		viewSources.setAlignmentX(Component.CENTER_ALIGNMENT);

		IncreaseSizeElements.addActionListener(this);
		DecreaseSizeElements.addActionListener(this);
		/*Update.addActionListener(this);*/
		License.addActionListener(this);
		Author.addActionListener(this);
		createInitfiles.addActionListener(this);
		viewSources.addActionListener(this);

		panelOption.add(Box.createVerticalGlue());
		panelOption.add(createInitfiles);
		panelOption.add(Box.createVerticalGlue());
		panelOption.add(IncreaseSizeElements);
		panelOption.add(Box.createVerticalGlue());
		panelOption.add(DecreaseSizeElements);
		panelOption.add(Box.createVerticalGlue());
		/*panelOption.add(Update);
		panelOption.add(Box.createVerticalGlue());*/
		panelOption.add(License);
		panelOption.add(Box.createVerticalGlue());
		panelOption.add(Author);
		panelOption.add(Box.createVerticalGlue());
		panelOption.add(viewSources);
		panelOption.add(Box.createVerticalGlue());

		JScrollPane scrollerOptions = new JScrollPane(panelOption);
		scrollerOptions.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollerOptions.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		tabsEnvironment.addTab(Keywords.Language.getMessage(110), IconOption, scrollerOptions);

		backTab=tabsEnvironment.getBackgroundAt(4);
		foreTab=tabsEnvironment.getForegroundAt(4);

		frameEnvironment.getContentPane().add(tabsEnvironment);

		frameEnvironment.addInternalFrameListener(new InternalFrameListener()
		{
			public void internalFrameActivated(InternalFrameEvent e)
			{
				setEnvironment();
			}
			public void internalFrameClosed(InternalFrameEvent e)
			{
			}
			public void internalFrameClosing(InternalFrameEvent e)
			{
			}
			public void internalFrameDeactivated(InternalFrameEvent e)
			{
			}
			public void internalFrameDeiconified(InternalFrameEvent e)
			{
				setEnvironment();
			}
			public void internalFrameIconified(InternalFrameEvent e)
			{
			}
			public void internalFrameOpened(InternalFrameEvent e)
			{
				setEnvironment();
			}
		});
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(frame);
			SwingUtilities.updateComponentTreeUI(frameExecutor);
			SwingUtilities.updateComponentTreeUI(frameResults);
			SwingUtilities.updateComponentTreeUI(frameEnvironment);
		} catch (Exception e) {}

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();

		frameResults.setLocation(width/3+30, 0);
		frameExecutor.setLocation(width/3+30, height/2-20);
		frameEnvironment.setLocation(0, 0);

		frameResults.setSize(2*width/3-60, height/2-40);
		frameExecutor.setSize(2*width/3-60, height/2-60);
		frameEnvironment.setSize(width/3+20, height-80);

		java.net.URL url = MainGUI.class.getResource(Keywords.simpleicon);
		Image icon = Toolkit.getDefaultToolkit().getImage(url);
		ImageIcon ADaMSoftICON=new ImageIcon(url);

		frameResults.setFrameIcon(ADaMSoftICON);
		frameExecutor.setFrameIcon(ADaMSoftICON);
		frameEnvironment.setFrameIcon(ADaMSoftICON);

		frameResults.setVisible(true);
		frameExecutor.setVisible(true);
		frameEnvironment.setVisible(true);

		desktop.add(frameResults);
		desktop.add(frameExecutor);
		desktop.add(frameEnvironment);

		desktop.setBackground(Color.white);

		frame.setTitle(Keywords.Language.getMessage(90));
		frame.setBackground(Color.white);

		frame.setSize(height, width);
		frame.setIconImage(icon);

		frame.getContentPane().add(desktop, BorderLayout.CENTER);
		frame.setVisible(true);

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		sw=new SystemWatcher();
		if (!minimal) sw.start();
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				if (!minimal)
				{
					int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(4017), Keywords.Language.getMessage(134), JOptionPane.YES_NO_OPTION);
					if (rr == 0)
					{
						if (Keywords.modifiedscript && !EditorArea.getText().equals(""))
						{
							deleted_save_operations=false;
							rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(141), Keywords.Language.getMessage(134), JOptionPane.YES_NO_OPTION);
							if (rr == 0)
								savescriptwithname();
							if (deleted_save_operations) return;
						}
						sw.endSystemInfo();
						already_exit=true;
						EndSession endadamsoft=new EndSession();
						if (!endadamsoft.getresult())
							JOptionPane.showMessageDialog(null, endadamsoft.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
						if (!updatetoinstall.equals(""))
						{
							try
							{
								int confirm = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(1716), Keywords.Language.getMessage(134), JOptionPane.YES_NO_OPTION);
								if (confirm == 1)
								{
									(new File(updatetoinstall)).delete();
									System.exit(0);
								}
								Runtime.getRuntime().exec("java -jar "+System.getProperty("main_directory")+"UpdateADaMSoft.jar "+System.getProperty("main_directory"));
							}
							catch (Exception ef)
							{
								JOptionPane.showMessageDialog(null, ef.toString(), "Error", JOptionPane.ERROR_MESSAGE);
								(new File(updatetoinstall)).delete();
							}
						}
						System.exit(0);
					}
				}
				else
				{
					sw.endSystemInfo();
					EndSession endadamsoft=new EndSession();
					if (!endadamsoft.getresult())
						JOptionPane.showMessageDialog(null, endadamsoft.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
			public void windowClosed(WindowEvent e)
			{
				if (!already_exit)
				{
					EndSession endadamsoft=new EndSession();
					endadamsoft.getresult();
				}
				System.exit(0);
			}
		});
		CheckNewRelease cnr=new CheckNewRelease();
		if (!state_exeingui) cnr.start();
		String inifile=(String)System.getProperty("IniFile");
		String cmdtoopen=System.getProperty("cmdtoopen");
		if (cmdtoopen.equals(""))
		{
			if (inifile.equals("") && !state_exeingui)
			{
				JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3986));
				tabsEnvironment.setSelectedIndex(5);
			}
		}
		else
		{
			String textSetup = "";
			try
			{
				File open = new File(cmdtoopen);
				if (!cmdtoopen.toLowerCase().startsWith("http://"))
				{
					boolean existsscript = (open.exists());
					if (existsscript)
					{
						try
						{
							lastopeneddir = open.getParent();
							lastopeneddir=lastopeneddir.replaceAll("\\\\","/");
							if (!lastopeneddir.endsWith(System.getProperty("file.separator")))
								lastopeneddir=lastopeneddir+System.getProperty("file.separator");
							System.setProperty("lastOpenedDirectory", lastopeneddir);
						}
						catch (Exception fs){}
						BufferedInputStream in = new BufferedInputStream((open.toURI()).toURL().openStream());
						textSetup = "";
						byte[] buffer = new byte[1024];
						int readb;
						while ((readb = in.read(buffer, 0, buffer.length)) != -1)
						{
							textSetup = textSetup + new String(buffer, 0, readb);
						}
						in.close();
						if (!textSetup.equals(""))
						{
							writeToEditor(textSetup);
							Keywords.modifiedscript=true;
							tabsExecutor.setTitleAt(2, Keywords.Language.getMessage(88)+" ("+open.getName().substring(0, open.getName().indexOf("."))+")");
						}
					}
				}
				else
				{
					java.net.URL fileUrl=new java.net.URL(cmdtoopen);
					BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
					String str;
					textSetup="";
					while ((str = in.readLine()) != null)
					{
						textSetup=textSetup+str;
					}
					in.close();
					if (!textSetup.equals(""))
					{
						writeToEditor(textSetup);
						Keywords.modifiedscript=true;
					}
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3937)+"\n"+e.toString());
			}
			if (EditorArea.getText().trim().equals(""))
			{
				executescript.setEnabled(false);
				haltscript.setEnabled(false);
				savescript.setEnabled(false);
				newscript.setEnabled(false);
				addscriptms.setEnabled(false);
			}
			else
			{
				executescript.setEnabled(true);
				savescript.setEnabled(true);
				newscript.setEnabled(true);
				addscriptms.setEnabled(true);
			}
			String exeingui=System.getProperty("exeingui");
			if (exeingui.equalsIgnoreCase("true") && !textSetup.equals(""))
			{
				Keywords.stop_script=false;
				textSetup = textSetup.trim();
				try
				{
					for (int p=0; p<tabsEnvironment.getTabCount(); p++)
					{
						tabsEnvironment.setBackgroundAt(p, Color.red);
						tabsEnvironment.setForegroundAt(p, Color.red);
					}
				}
				catch (Exception etab)
				{
					if (!System.getProperty("gui_error").equals(""))
					{
						StringWriter SWex = new StringWriter();
						PrintWriter PWex = new PrintWriter(SWex);
						etab.printStackTrace(PWex);
						try
						{
							String gui_error=System.getProperty("gui_error");
							BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
							g_error.write(SWex.toString()+"\n");
							g_error.close();
							System.setProperty("gui_error", gui_error);
						}
						catch (Exception ef){}
					}
				}
				pastexecution=false;
				loadscript.setEnabled(false);
				executescript.setEnabled(false);
				haltscript.setEnabled(true);
				savescript.setEnabled(false);
				newscript.setEnabled(false);
				addscriptms.setEnabled(false);
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				Keywords.general_percentage_total=0;
				Keywords.general_percentage_done=0;
				Keywords.numread=0.0;
				Keywords.operationReading=false;
				Keywords.numwrite=0.0;
				Keywords.operationWriting=false;
				Keywords.currentExecutedStep="Processing statements";
				ExecuteScript es=new ExecuteScript(textSetup);
				es.start();
				if (minimal)
				{
					frameResults.setVisible(false);
					frameExecutor.setVisible(false);
					frameEnvironment.setVisible(false);
				}
			}
		}
	}
	public static void writeToEditor(String text)
	{
		try
		{
			doc = EditorArea.getDocument();
			blank = new DefaultStyledDocument();
			EditorArea.setDocument(blank);
			try
			{
				doc.insertString(doc.getLength(), text, null);
			}
			catch (BadLocationException e) {}
			EditorArea.setDocument(doc);
			EditorArea.setCaretPosition(EditorArea.getDocument().getLength());
		}
		catch (Exception e) {}
	}

	public void undoableEditHappened(UndoableEditEvent e) throws NullPointerException
	{
		try
		{
			if (e.getEdit()!=null)
				undoManager.addEdit(e.getEdit());
		}
		catch (Exception eee) {}
	}
	public void actionPerformed(ActionEvent Event)
	{
		Object Source = Event.getSource();
		if (Source==newSetting)
		{
			new SettingGUI(settingSelection, null, 2);
		}
		if (Source==addStepinEditor)
		{
			try
			{
				String tempsyntax="";
				boolean firstsecond=false;
				Class<?> classCommand = Class.forName(Keywords.SoftwareName+ ".procedures." + stepSelection);
				stepSelection=stepSelection.toUpperCase();
				try
				{
					stepSelection=stepSelection.replaceAll("PROC", "PROC ");
					stepSelection=stepSelection.replaceAll("ADAMSDOC", "ADAMSDOC ");
					stepSelection=stepSelection.replaceAll("REPORT", "REPORT  ");
					stepSelection=stepSelection.replaceAll("TOC", "TOC ");
				}
				catch (Exception ed)
				{

				}
				String syntaxtoadd="\n/*General syntax automatically added*/\n/*Note that in UPPERCASE are the mandatory parameters*/\n\n"+stepSelection+"\n";
				RunStep comm = (RunStep) classCommand.newInstance();
				LinkedList<?> param = comm.getparameters();
				Iterator<?> i = param.iterator();
				while(i.hasNext())
				{
					tempsyntax="";
					GetRequiredParameters par = (GetRequiredParameters)i.next();
					if (par.getLevel()==2 && !firstsecond)
					{
						syntaxtoadd=syntaxtoadd+";\n";
						firstsecond=true;
					}
					if (par.getType().equalsIgnoreCase("note"))
						tempsyntax=tempsyntax+" /*"+Keywords.Language.getMessage(par.getLabel()).toLowerCase()+"*/";
					else
					{
						if (par.isMandatory())
							tempsyntax=par.getName().toUpperCase();
						else
							tempsyntax=par.getName().toLowerCase();
						if (par.getType().indexOf("=")>0)
						{
							tempsyntax=tempsyntax+" <"+Keywords.Language.getMessage(par.getLabel());
							try
							{
								String[] partp=par.getType().split("=");
								if (partp[1].indexOf(",")>0)
								{
									tempsyntax=tempsyntax+" OPTIONS (VALUE, MEANING): ";
									String[] partpp=partp[1].split(",");
									for (int h=0; h<partpp.length; h++)
									{
										String[] ms=partpp[h].split("_");
										tempsyntax=tempsyntax+" ("+ms[1]+", "+Keywords.Language.getMessage(Integer.parseInt(ms[0]))+") ";
									}
								}
							}
							catch (Exception ep){}
							tempsyntax=tempsyntax+">";
						}
						else
							tempsyntax=tempsyntax+" <"+Keywords.Language.getMessage(par.getLabel()).toLowerCase()+">";
					}
					if (par.getLevel()==2)
					{
						syntaxtoadd=syntaxtoadd+" "+tempsyntax.trim()+";\n";
						firstsecond=true;
					}
					else
						syntaxtoadd=syntaxtoadd+" "+tempsyntax.trim()+"\n";
				}
				syntaxtoadd=syntaxtoadd+"\nRUN;\n";
				writeToEditor(syntaxtoadd);
				JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(4014));
			}
			catch (Exception e)
			{
			}

		}
		if (Source==License)
		{
			try
			{
				Keywords.semwritelog.acquire();
				BufferedWriter outlogfile= new BufferedWriter(new FileWriter(new File(out_logfile), true));
				outlogfile.write((new License()).getLicense());
				outlogfile.close();
				Keywords.semwritelog.release();
				JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3976));
			}
			catch (Exception e){}
		}
		if (Source==viewSources)
		{
			lastopeneddir=(String)System.getProperty("lastOpenedDirectory");
			JFileChooser sb = new JFileChooser(new File(lastopeneddir));
		    sb.setDialogTitle(Keywords.Language.getMessage(3985));
		    sb.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    sb.setAcceptAllFileFilterUsed(false);
			try
			{
				if (sb.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					File save = sb.getSelectedFile();
					String sourcesdir=save.toString();
					if (!sourcesdir.endsWith(File.separator))
						sourcesdir=sourcesdir+File.separator;
					sourcesdir=sourcesdir+"ADaMSoftSources";
					if (!sourcesdir.endsWith(File.separator))
						sourcesdir=sourcesdir+File.separator;
					boolean exists = (new File(sourcesdir)).exists();
					if (!exists)
					{
						boolean success = (new File(sourcesdir)).mkdir();
						if (!success)
						{
							JOptionPane.showMessageDialog(null, "Error creating the directory: "+sourcesdir, "Attention", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					URL fp = getClass().getProtectionDomain().getCodeSource().getLocation();
					URI ffp=new URI(fp.toURI().getScheme(), null, fp.toURI().getPath(), fp.toURI().getQuery(), fp.toURI().getFragment());
					String fpath=ffp.getPath();
					@SuppressWarnings("resource")
					JarFile jar = new JarFile(new File(fpath));
					Enumeration<JarEntry> entries=jar.entries();
					while(entries.hasMoreElements())
					{
						String entryname=((JarEntry)entries.nextElement()).getName();
						if (entryname.indexOf("Sources/ADaMSoftSources")==0)
						{
							String tempentryname=entryname.substring("Sources/ADaMSoftSources".length()+1);
							File efile = new File(tempentryname);
							String parentDir = efile.getParent();
							String parentFile=entryname.substring(entryname.lastIndexOf("/")+1);
							if (!parentFile.equals(""))
							{
								String tempsubdir=sourcesdir;
								if (parentDir!=null)
								{
									try
									{
										parentDir=parentDir.replaceAll("\\\\","/");
									}
									catch (Exception fs){}
									String[] subdirs=parentDir.split("/");
									for (int j=0; j<subdirs.length; j++)
									{
										tempsubdir=tempsubdir+File.separator+subdirs[j];
										boolean existssd = (new File(tempsubdir)).exists();
										if (!existssd)
										{
											boolean successsd = (new File(tempsubdir)).mkdir();
											if (!successsd)
											{
												JOptionPane.showMessageDialog(null, "Error creating the directory: "+tempsubdir, "Attention", JOptionPane.ERROR_MESSAGE);
												return;
											}
										}
									}
								}
								File exfile = new File(tempsubdir, parentFile);
								ZipEntry entry = jar.getEntry(entryname);
								BufferedInputStream in = new BufferedInputStream(jar.getInputStream(entry));
								BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(exfile));
								byte[] buffer = new byte[2048];
								for (;;)
								{
									int nBytes = in.read(buffer);
									if (nBytes <= 0) break;
									out.write(buffer, 0, nBytes);
								}
								out.flush();
								out.close();
								in.close();
							}
						}
					}
					jar.close();
					JOptionPane.showMessageDialog(desktop, "Sources extracted correctly in the directory: "+sourcesdir, "Result", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(null, "Error extracting the sources: "+e.toString(), "Attention", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (Source==Author)
		{
			try
			{
				Keywords.semwritelog.acquire();
				BufferedWriter outlogfile= new BufferedWriter(new FileWriter(new File(out_logfile), true));
				outlogfile.write((new License()).getAuthors());
				outlogfile.close();
				Keywords.semwritelog.release();
				JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3977));
			}
			catch (Exception e){}
		}
		if (Source==delSetting)
		{
			int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(3974)+" "+nameSettingSelection+" ("+settingSelection+")",Keywords.Language.getMessage(3963),JOptionPane.YES_NO_OPTION);
			if (rr != 0)
				return;
			new ExecuteRunner(2, "SETTING "+settingSelection+"; Name "+nameSettingSelection+"; Clear; end;");
			setEnvironment();
		}
		if (Source==Update)
		{
			new ADaMSoftUpdater();
			updateDownloaded=true;
			Update.setEnabled(false);
		}
		if (Source==runIO && !stepSelection.equals(""))
		{
			window_selected="runIO";
			try
			{
				Class<?> classCommand = Class.forName(Keywords.SoftwareName+ ".procedures." + stepSelection);
				RunStep comm = (RunStep) classCommand.newInstance();
				LinkedList<?> par = comm.getparameters();
				numrow = new Double(frame.getHeight()).intValue() - 52;
				numcol = new Double(frame.getWidth()).intValue() - 14;
				if (!stepSelection.toUpperCase().startsWith(Keywords.REPORT.toUpperCase()))
				{
					if ((numrow > 0) && (numcol > 0))
					{
						pastexecution=false;
						new ScriptGenerator(stepSelection, par);
					}
				}
				else
				{
					if ((numrow > 0) && (numcol > 0))
					{
						pastexecution=false;
						new ReportScriptGenerator(stepSelection, par);
					}
				}
			}
			catch (Exception e) {}
		}
		if (Source==delPath)
		{
			if (treeOnDS)
			{
				int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(3964)+" "+dsSelection,Keywords.Language.getMessage(3963),JOptionPane.YES_NO_OPTION);
				if (rr != 0)
					return;
				new ExecuteRunner(2, "proc delete dict="+dsSelection+"; run;");
				setEnvironment();
			}
			else if (treeOnDocument==true)
			{
				int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(3973)+" "+dsSelection,Keywords.Language.getMessage(3963),JOptionPane.YES_NO_OPTION);
				if (rr != 0)
					return;
				String[] ref=dsSelection.toString().split("\\.");
				new ExecuteRunner(2, "adamsdoc delete docpath="+ref[0]+"; docname="+ref[1]+"; run;");
				setEnvironment();
			}
			else
			{
				int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(3965)+" "+pathSelection,Keywords.Language.getMessage(3963),JOptionPane.YES_NO_OPTION);
				if (rr != 0)
					return;
				new ExecuteRunner(2, "PATH "+pathSelection+"=CLEAR;");
				setEnvironment();
			}
		}
		if (Source==executeMacrostep)
		{
			new ExecuteRunner(2, "EXEMACROSTEP "+macrostepSelection+";");
		}
		if (Source==newDefine)
		{
			JTextField definename= new JTextField(40);
			JTextField definevalue = new JTextField(40);
			Object[] message =
			{
				Keywords.Language.getMessage(217), definename,
				Keywords.Language.getMessage(216), definevalue
			};
			int option = JOptionPane.showConfirmDialog(desktop, message, Keywords.Language.getMessage(214), JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION)
			{
				if (!definename.getText().equals("") && !definevalue.getText().equals(""))
				{
					new ExecuteRunner(2, "define "+definename.getText()+"="+definevalue.getText()+";");
					setEnvironment();
				}
			}
		}
		if (Source==delDefine)
		{
			int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(3966)+" "+defineSelection,Keywords.Language.getMessage(3963),JOptionPane.YES_NO_OPTION);
			if (rr != 0)
				return;
			new ExecuteRunner(2, "define "+defineSelection+"=clear;");
			setEnvironment();
		}
		if (Source==viewDS)
		{
			if (treeOnDocument==false)
			{
				Object[] options_viewer = {Keywords.Language.getMessage(4033),
						Keywords.Language.getMessage(4034),
	                    };
				int n_views = JOptionPane.showOptionDialog(desktop,
					Keywords.Language.getMessage(4035),
					Keywords.Language.getMessage(4036),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options_viewer,
					options_viewer[0]);
				if (n_views==1)
				{
					new ExecuteRunner(2, "EXPORTDS2OUT "+dsSelection+";");
				}
				else
				{
					if (Keywords.opened_datasets.contains(dsSelection))
					{
						JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3962), "Warning", JOptionPane.WARNING_MESSAGE);
						return;
					}
					else Keywords.opened_datasets.add(dsSelection);
					pastexecution=false;
					Keywords.currentExecutedStep="Viewing data set";
					String[] ref=dsSelection.toString().split("\\.");
					Keywords.general_percentage_total=0;
					Keywords.general_percentage_done=0;
					Keywords.numread=0.0;
					Keywords.operationReading=false;
					Keywords.numwrite=0.0;
					Keywords.operationWriting=false;
					new DataSetViewer(Keywords.project.getPath(ref[0])+ref[1], ref[0], ref[1], dsSelection);
				}
			}
			else
			{
				String[] ref=dsSelection.toString().split("\\.");
				JTextField newdocname= new JTextField(40);
				Object[] message =
				{
					Keywords.Language.getMessage(160), newdocname
				};
				int option = JOptionPane.showConfirmDialog(desktop, message, Keywords.Language.getMessage(3972), JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.OK_OPTION)
				{
					if (!newdocname.getText().equals(""))
					{
						new ExecuteRunner(2, "ADAMSDOC Restore docpath="+ref[0]+" outpath="+ref[0]+"; docname "+ref[1]+"; outname "+newdocname.getText()+"; run;");
						setEnvironment();
					}
				}
			}
		}
		if (Source==newPath)
		{
			pastexecution=false;
			Keywords.currentExecutedStep="Operations on PATH";
			Keywords.general_percentage_total=0;
			Keywords.general_percentage_done=0;
			Keywords.numread=0.0;
			Keywords.operationReading=false;
			Keywords.numwrite=0.0;
			Keywords.operationWriting=false;
			new PathGUI("", true);
		}
		if (Source==saveMessageArea)
		{
			lastopeneddir=(String)System.getProperty("lastOpenedDirectory");
			JFileChooser sb = new JFileChooser(new File(lastopeneddir));
			sb.setFileFilter(new javax.swing.filechooser.FileFilter()
			{
				public boolean accept(File f)
				{
					return (f.getName().endsWith(".html") || f.isDirectory());
				}
				public String getDescription()
				{
					return Keywords.Language.getMessage(138);
				}
			});
			sb.setAcceptAllFileFilterUsed(false);
			try
			{
				if (sb.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					File save = sb.getSelectedFile();
					File newFile;
					if (!save.getName().endsWith(".html"))
						newFile = new File(save.getAbsolutePath()+ ".html");
					else
						newFile = new File(save.getAbsolutePath());
					boolean exists = (newFile.exists());
					if (exists)
					{
						int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(135),Keywords.Language.getMessage(134),JOptionPane.YES_NO_OPTION);
						if (rr != 0)
							return;
					}
					Keywords.semwritelog.acquire();
					BufferedWriter outfile= new BufferedWriter(new FileWriter(newFile));
					temp_content = MessageArea.getText();
					outfile.write(temp_content);
					outfile.close();
					lastopeneddir = save.getParent();
					try
					{
						lastopeneddir=lastopeneddir.replaceAll("\\\\","/");
					}
					catch (Exception fs){}
					if (!lastopeneddir.endsWith(System.getProperty("file.separator")))
						lastopeneddir=lastopeneddir+System.getProperty("file.separator");
					System.setProperty("lastOpenedDirectory", lastopeneddir);
					Keywords.semwritelog.release();
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3952)+"\n"+e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				Keywords.semwritelog.release();
				return;
			}
		}
		if (Source==saveOutput)
		{
			deleted_save_operations=false;
			saveoutwithname();
		}
		if (Source==addscriptms)
		{
			temp_content=EditorArea.getSelectedText();
			if (temp_content==null || temp_content.equals(""))
			{
				int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(3967), Keywords.Language.getMessage(134), JOptionPane.YES_NO_OPTION);
				if (rr != 0)
					return;
			}
			temp_content=EditorArea.getText();
			String msname = (String)JOptionPane.showInputDialog(desktop,Keywords.Language.getMessage(2219), Keywords.Language.getMessage(2218),	JOptionPane.PLAIN_MESSAGE, null, null,"");
			if (msname!=null)
			{
				if (!msname.equals(""))
				{
					int prescom=temp_content.indexOf("/*");
					try
					{
						while (prescom>=0)
						{
							int posend=temp_content.indexOf("*/");
							if (posend<0)
							{
								JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(2110), "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							temp_content=temp_content.substring(0, prescom)+temp_content.substring(posend+2);
							prescom=temp_content.indexOf("/*");
						}
					}
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(2110)+"\n"+e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					temp_content=Keywords.MACROSTEP+" "+msname+";\n"+temp_content+"\n MEND;";
					new ExecuteRunner(2, temp_content);
					setEnvironment();
				}
			}
			else
				JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(2117), "Error", JOptionPane.ERROR_MESSAGE);
		}
		if (Source==deleteOutput)
		{
			try
			{
				if (!OutputArea.getText().equals(""))
				{
					deleted_save_operations=false;
					int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(3953), Keywords.Language.getMessage(134), JOptionPane.YES_NO_OPTION);
					if (rr == 0)
					{
						saveoutwithname();
						if (deleted_save_operations) return;
					}
				}
				Keywords.semwriteOut.acquire();
				BufferedWriter outfile= new BufferedWriter(new FileWriter(new File(out_outfile)));
				outfile.write("");
				outfile.close();
				Keywords.semwriteOut.release();
				OutputArea.setText("");
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3952)+"\n"+e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				Keywords.semwriteOut.release();
			}
		}
		if (Source==deleteMessageArea)
		{
			try
			{
				MessageArea.setText("");
				Keywords.semwritelog.acquire();
				String logfile="<i>"+Keywords.Language.getMessage(90) + "<br>\n";
				logfile=logfile+Keywords.Language.getMessage(145) + ": "+ (System.getProperty("os.name")) + " - "+ Keywords.Language.getMessage(125) + ": "+ (System.getProperty("os.version"))+"<br>\n";
				logfile=logfile+ Keywords.Language.getMessage(126) + ": "+ (System.getProperty("java.version"))+"<br>\n";
				int infototmemory= (int)((Runtime.getRuntime().maxMemory())/1024);
				logfile=logfile+Keywords.Language.getMessage(3168) + ": "+ String.valueOf(infototmemory)+"<br>\n";
				Object[] paramsdate = new Object[] { new Date(), new Date(0) };
				logfile=logfile+MessageFormat.format(Keywords.Language.getMessage(127)+ " {0}", paramsdate)+"<br>\n";
				logfile=logfile+Keywords.Language.getMessage(128) + ": "+ System.getProperty(Keywords.WorkDir)+"<br>\n";
				logfile=logfile+Keywords.Language.getMessage(2630) + "\n"+ Keywords.Language.getMessage(3056)+"<br><br></i>\n";
				BufferedWriter outlogfile= new BufferedWriter(new FileWriter(new File(out_logfile)));
				outlogfile.write(logfile);
				outlogfile.close();
				Keywords.semwritelog.release();
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(790)+"\n"+e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				Keywords.semwritelog.release();
			}
		}
		if (Source==createInitfiles)
		{
			window_selected="runStep";
			try
			{
				Class<?> classCommand = Class.forName(Keywords.SoftwareName+ ".procedures." + "ProcInitadamsoft");
				RunStep comm = (RunStep) classCommand.newInstance();
				LinkedList<?> par = comm.getparameters();
				numrow = new Double(frame.getHeight()).intValue() - 52;
				numcol = new Double(frame.getWidth()).intValue() - 14;
				if ((numrow > 0) && (numcol > 0))
				{
					new ScriptGenerator("ProcInitadamsoft", par);
				}
			}
			catch (Exception e)
			{
			}
		}
		if (Source==runStep && !stepSelection.equals(""))
		{
			window_selected="runStep";
			try
			{
				Class<?> classCommand = Class.forName(Keywords.SoftwareName+ ".procedures." + stepSelection);
				RunStep comm = (RunStep) classCommand.newInstance();
				LinkedList<?> par = comm.getparameters();
				numrow = new Double(frame.getHeight()).intValue() - 52;
				numcol = new Double(frame.getWidth()).intValue() - 14;
				if (!stepSelection.toUpperCase().startsWith(Keywords.REPORT.toUpperCase()))
				{
					if ((numrow > 0) && (numcol > 0))
					{
						pastexecution=false;
						new ScriptGenerator(stepSelection, par);
					}
				}
				else
				{
					if ((numrow > 0) && (numcol > 0))
					{
						pastexecution=false;
						new ReportScriptGenerator(stepSelection, par);
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		if (Source==loadscript)
		{
			lastopeneddir=(String)System.getProperty("lastOpenedDirectory");
			JFileChooser sb = new JFileChooser(new File(lastopeneddir));
			sb.setFileFilter(new javax.swing.filechooser.FileFilter()
			{
				public boolean accept(File f)
				{
					return (f.getName().endsWith(Keywords.ScriptExtension) || f.isDirectory());
				}
				public String getDescription()
				{
					return Keywords.Language.getMessage(140);
				}
			});
			sb.setAcceptAllFileFilterUsed(false);
			try
			{
				if (sb.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					File open = sb.getSelectedFile();
					lastopeneddir = open.getParent();
					boolean existsscript = (open.exists());
					if (existsscript)
					{
						BufferedInputStream in = new BufferedInputStream((open.toURI()).toURL().openStream());
						String textSetup = "";
						byte[] buffer = new byte[1024];
						int readb;
						while ((readb = in.read(buffer, 0, buffer.length)) != -1) {
							textSetup = textSetup + new String(buffer, 0, readb);
						}
						in.close();
						writeToEditor(textSetup);
						try
						{
							lastopeneddir=lastopeneddir.replaceAll("\\\\","/");
						}
						catch (Exception fs){}
						if (!lastopeneddir.endsWith(System.getProperty("file.separator")))
							lastopeneddir=lastopeneddir+System.getProperty("file.separator");
						System.setProperty("lastOpenedDirectory", lastopeneddir);
						Keywords.modifiedscript=true;
						tabsExecutor.setTitleAt(2, Keywords.Language.getMessage(88)+" ("+open.getName().substring(0, open.getName().indexOf("."))+")");
					}
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3937)+"\n"+e.toString());
			}
			if (EditorArea.getText().trim().equals(""))
			{
				executescript.setEnabled(false);
				haltscript.setEnabled(false);
				savescript.setEnabled(false);
				newscript.setEnabled(false);
				addscriptms.setEnabled(false);
			}
			else
			{
				executescript.setEnabled(true);
				savescript.setEnabled(true);
				newscript.setEnabled(true);
				addscriptms.setEnabled(true);
			}
		}
		if (Source==searchStep)
		{
			String s = (String)JOptionPane.showInputDialog(desktop, Keywords.Language.getMessage(4010), Keywords.Language.getMessage(4007), JOptionPane.PLAIN_MESSAGE);
			if (s!=null && (s.length() > 0))
			{
				Vector<String> list_procs=new Vector<String>();
				boolean add=false;
				for (Enumeration<String> en=listmessages.keys(); en.hasMoreElements();)
				{
					String procp=en.nextElement();
					String[] partprocp=procp.split(" ");
					add=false;
					for (int i=0; i<partprocp.length; i++)
					{
						if (partprocp[i].toLowerCase().indexOf(s.toLowerCase())>=0)
						{
							add=true;
							break;
						}
					}
					if (add)
					{
						Vector<String> tp=listmessages.get(procp);
						list_procs.add(tp.get(1)+" ("+tp.get(0)+")");
					}
				}
				if (list_procs.size()>0)
				{
					Object[] lp=new Object[list_procs.size()];
					for (int i=0; i<list_procs.size(); i++)
					{
						lp[i]=list_procs.get(i);
					}
					s = (String)JOptionPane.showInputDialog(desktop,
							Keywords.Language.getMessage(4012),
		                    Keywords.Language.getMessage(4011),
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    lp,
		                    lp[0]);
					if (s!=null && (s.length() > 0))
					{
						String intree=s.substring(0, s.indexOf("("));
						String outree=s.substring(s.indexOf("(")+1,s.length()-1);
						TreePath selectedPath=null;
						for (int i = 0; i < Steps_Tree.getRowCount(); i++)
						{
							Steps_Tree.expandRow(i);
							TreePath temptp=Steps_Tree.getPathForRow(i);
							if (temptp.getLastPathComponent().toString().trim().equalsIgnoreCase(intree.trim()))
							{
								selectedPath=temptp;
							}
							else if (!temptp.getLastPathComponent().toString().trim().equalsIgnoreCase(outree.trim()))
							{
								if (temptp.getPath().length==3)
									Steps_Tree.collapseRow(i);
							}
						}
						Steps_Tree.setSelectionPath(selectedPath);
						Steps_Tree.expandPath(selectedPath);
						Steps_Tree.makeVisible(selectedPath);
						Steps_Tree.setSelectionPath(selectedPath);
					}
				}
				else
				{
					JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(4013));
				}
			}
		}
		if (Source==newscript)
		{
			if (!EditorArea.getText().trim().equals(""))
			{
				int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(141), Keywords.Language.getMessage(134), JOptionPane.YES_NO_OPTION);
				if (rr == 0)
					savescriptwithname();
			}
			EditorArea.setText("");
			tabsExecutor.setTitleAt(2, Keywords.Language.getMessage(88)+" "+Keywords.Language.getMessage(2156));
		}
		if (Source==IncreaseSizeElements)
		{
			int size=EditorArea.getFont().getSize();
			size=size+1;
			if (size>24) size=24;
			EditorArea.setFont(new Font(EditorArea.getFont().getFontName(), EditorArea.getFont().getStyle(), size));
			size=OutputArea.getFont().getSize();
			size=size+1;
			if (size>24) size=24;
			OutputArea.setFont(new Font(OutputArea.getFont().getFontName(), OutputArea.getFont().getStyle(), size));
			size=MessageArea.getFont().getSize();
			size=size+1;
			if (size>24) size=24;
			MessageArea.setFont(new Font(MessageArea.getFont().getFontName(), MessageArea.getFont().getStyle(), size));
			MessageArea.repaint();
			MessageArea.validate();
			size=Steps_Tree.getFont().getSize();
			size=size+2;
			if (size>24) size=24;
			Steps_Tree.setRowHeight(size+4);
			Steps_Tree.setFont(new Font(Steps_Tree.getFont().getFontName(), Steps_Tree.getFont().getStyle(), size));
			size=IOTree.getFont().getSize();
			size=size+2;
			if (size>24) size=24;
			IOTree.setRowHeight(size+4);
			IOTree.setFont(new Font(IOTree.getFont().getFontName(), IOTree.getFont().getStyle(), size));
			size=PATH_Tree.getFont().getSize();
			size=size+2;
			if (size>24) size=24;
			PATH_Tree.setRowHeight(size+4);
			PATH_Tree.setFont(new Font(PATH_Tree.getFont().getFontName(), PATH_Tree.getFont().getStyle(), size));
			PATH_Tree.repaint();
			PATH_Tree.validate();
			size=SETTING_Tree.getFont().getSize();
			size=size+2;
			if (size>24) size=24;
			SETTING_Tree.setRowHeight(size+4);
			SETTING_Tree.setFont(new Font(SETTING_Tree.getFont().getFontName(), SETTING_Tree.getFont().getStyle(), size));
			size=DEFINE_Tree.getFont().getSize();
			size=size+2;
			if (size>24) size=24;
			DEFINE_Tree.setRowHeight(size+4);
			DEFINE_Tree.setFont(new Font(DEFINE_Tree.getFont().getFontName(), DEFINE_Tree.getFont().getStyle(), size));
			size=MACROSTEP_Tree.getFont().getSize();
			size=size+2;
			if (size>24) size=24;
			MACROSTEP_Tree.setRowHeight(size+4);
			MACROSTEP_Tree.setFont(new Font(MACROSTEP_Tree.getFont().getFontName(), MACROSTEP_Tree.getFont().getStyle(), size));

		}
		if (Source==DecreaseSizeElements)
		{
			int size=EditorArea.getFont().getSize();
			size=size-1;
			if (size<8) size=8;
			EditorArea.setFont(new Font(EditorArea.getFont().getFontName(), EditorArea.getFont().getStyle(), size));
			size=OutputArea.getFont().getSize();
			size=size-1;
			if (size<8) size=8;
			OutputArea.setFont(new Font(OutputArea.getFont().getFontName(), OutputArea.getFont().getStyle(), size));
			size=MessageArea.getFont().getSize();
			size=size-1;
			if (size<8) size=8;
			MessageArea.setFont(new Font(MessageArea.getFont().getFontName(), MessageArea.getFont().getStyle(), size));
			size=Steps_Tree.getFont().getSize();
			size=size-1;
			if (size<8) size=8;
			Steps_Tree.setRowHeight(size+4);
			Steps_Tree.setFont(new Font(Steps_Tree.getFont().getFontName(), Steps_Tree.getFont().getStyle(), size));
			size=IOTree.getFont().getSize();
			size=size-2;
			if (size<8) size=8;
			IOTree.setRowHeight(size+4);
			IOTree.setFont(new Font(IOTree.getFont().getFontName(), IOTree.getFont().getStyle(), size));
			size=PATH_Tree.getFont().getSize();
			size=size-2;
			if (size<8) size=8;
			PATH_Tree.setRowHeight(size+4);
			PATH_Tree.setFont(new Font(PATH_Tree.getFont().getFontName(), PATH_Tree.getFont().getStyle(), size));
			size=SETTING_Tree.getFont().getSize();
			size=size-2;
			if (size<8) size=8;
			SETTING_Tree.setRowHeight(size+4);
			SETTING_Tree.setFont(new Font(SETTING_Tree.getFont().getFontName(), SETTING_Tree.getFont().getStyle(), size));
			size=DEFINE_Tree.getFont().getSize();
			size=size-2;
			if (size<8) size=8;
			DEFINE_Tree.setRowHeight(size+4);
			DEFINE_Tree.setFont(new Font(DEFINE_Tree.getFont().getFontName(), DEFINE_Tree.getFont().getStyle(), size));
			size=MACROSTEP_Tree.getFont().getSize();
			size=size-2;
			if (size<8) size=8;
			MACROSTEP_Tree.setRowHeight(size+4);
			MACROSTEP_Tree.setFont(new Font(MACROSTEP_Tree.getFont().getFontName(), MACROSTEP_Tree.getFont().getStyle(), size));
		}
		if (Source==savescript)
		{
			savescriptwithname();
			Keywords.modifiedscript=false;
		}
		if (Source==executescript)
		{
			Keywords.stop_script=false;
			String statementsSelected = EditorArea.getSelectedText();
			String text = null;
			if (statementsSelected != null)
				text = statementsSelected;
			else
				text = EditorArea.getText();
			text = text.trim();
			if (!text.equals(""))
			{
				try
				{
					for (int p=0; p<tabsEnvironment.getTabCount(); p++)
					{
						tabsEnvironment.setBackgroundAt(p, Color.red);
						tabsEnvironment.setForegroundAt(p, Color.red);
					}
				}
				catch (Exception etab)
				{
					if (!System.getProperty("gui_error").equals(""))
					{
						StringWriter SWex = new StringWriter();
						PrintWriter PWex = new PrintWriter(SWex);
						etab.printStackTrace(PWex);
						try
						{
							String gui_error=System.getProperty("gui_error");
							BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
							g_error.write(SWex.toString()+"\n");
							g_error.close();
							System.setProperty("gui_error", gui_error);
						}
						catch (Exception ef){}
					}
				}
				pastexecution=false;
				loadscript.setEnabled(false);
				executescript.setEnabled(false);
				haltscript.setEnabled(true);
				savescript.setEnabled(false);
				newscript.setEnabled(false);
				addscriptms.setEnabled(false);
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				Keywords.general_percentage_total=0;
				Keywords.general_percentage_done=0;
				Keywords.numread=0.0;
				Keywords.operationReading=false;
				Keywords.numwrite=0.0;
				Keywords.operationWriting=false;
				Keywords.currentExecutedStep="Processing statements";
				ExecuteScript es=new ExecuteScript(text);
				es.start();
			}
			else
				JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(142));
		}
		if (Source==haltscript)
		{
			haltscript.setEnabled(false);
			Keywords.stop_script=true;
			JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(4021), "Warning", JOptionPane.WARNING_MESSAGE);
		}
	}
	private void savescriptwithname()
	{
		lastopeneddir=(String)System.getProperty("lastOpenedDirectory");
		JFileChooser sb = new JFileChooser(new File(lastopeneddir));
		sb.setFileFilter(new javax.swing.filechooser.FileFilter()
		{
			public boolean accept(File f)
			{
				return (f.getName().endsWith(Keywords.ScriptExtension) || f.isDirectory());
			}
			public String getDescription()
			{
				return Keywords.Language.getMessage(140);
			}
		});
		sb.setAcceptAllFileFilterUsed(false);
		try
		{
			deleted_save_operations=false;
			if (sb.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
			{
				File save = sb.getSelectedFile();
				File newFile;
				try
				{
					if (!save.getName().endsWith(Keywords.ScriptExtension))
						newFile = new File(save.getAbsolutePath()+ Keywords.ScriptExtension);
					else
						newFile = new File(save.getAbsolutePath());
					boolean exists = (newFile.exists());
					if (exists)
					{
						int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(135),Keywords.Language.getMessage(134),JOptionPane.YES_NO_OPTION);
						if (rr != 0)
							return;
					}
					lastopeneddir = save.getParent();
					try
					{
						lastopeneddir=lastopeneddir.replaceAll("\\\\","/");
					}
					catch (Exception fs){}
					if (!lastopeneddir.endsWith(System.getProperty("file.separator")))
						lastopeneddir=lastopeneddir+System.getProperty("file.separator");
					System.setProperty("lastOpenedDirectory", lastopeneddir);
					FileWriter writer = new FileWriter(newFile);
					PrintWriter outfile = new PrintWriter(writer);
					String text = EditorArea.getText().replace("\r","");
					outfile.println(text);
					outfile.close();
					tabsExecutor.setTitleAt(2, Keywords.Language.getMessage(88)+" ("+newFile.getName().substring(0, newFile.getName().indexOf("."))+")");
					return;
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3937)+"\n"+ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else deleted_save_operations=true;
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3937)+"\n"+e.toString());
		}
	}
	private void saveoutwithname()
	{
		lastopeneddir=(String)System.getProperty("lastOpenedDirectory");
		JFileChooser sb = new JFileChooser(new File(lastopeneddir));
		sb.setFileFilter(new javax.swing.filechooser.FileFilter()
		{
			public boolean accept(File f)
			{
				return (f.getName().endsWith(".html") || f.isDirectory());
			}
			public String getDescription()
			{
				return Keywords.Language.getMessage(3954);
			}
		});
		sb.setAcceptAllFileFilterUsed(false);
		try
		{
			deleted_save_operations=false;
			if (sb.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
			{
				File save = sb.getSelectedFile();
				File newFile;
				try
				{
					if (!save.getName().endsWith(".html"))
						newFile = new File(save.getAbsolutePath()+ ".html");
					else
						newFile = new File(save.getAbsolutePath());
					boolean exists = (newFile.exists());
					if (exists)
					{
						int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(135),Keywords.Language.getMessage(134),JOptionPane.YES_NO_OPTION);
						if (rr != 0)
							return;
					}
					lastopeneddir = save.getParent();
					try
					{
						lastopeneddir=lastopeneddir.replaceAll("\\\\","/");
					}
					catch (Exception fs){}
					if (!lastopeneddir.endsWith(System.getProperty("file.separator")))
						lastopeneddir=lastopeneddir+System.getProperty("file.separator");
					System.setProperty("lastOpenedDirectory", lastopeneddir);
					Keywords.semwriteOut.acquire();
					FileWriter writer = new FileWriter(newFile);
					PrintWriter outfile = new PrintWriter(writer);
					temp_content = OutputArea.getText();
					outfile.write(temp_content);
					outfile.close();
					Keywords.semwriteOut.release();
					return;
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3937)+"\n"+ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else deleted_save_operations=true;
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3937)+"\n"+e.toString());
		}
	}
	private void setEnvironment()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
		      public void run(){
		openedPaths.clear();
		int rows= PATH_Tree.getRowCount();
		for(int i=0;i<rows;i++)
		{
			if(PATH_Tree.isExpanded(i))
			{
				TreePath current=PATH_Tree.getPathForRow(i);
				if(current.getPathCount()>1)
				{
					openedPaths.add(current);
				}
			}
		}
		DefaultMutableTreeNode nodePaths = (DefaultMutableTreeNode)PATH_Tree.getModel().getRoot();
		nodePaths.removeAllChildren();
		DefaultTreeModel model = (DefaultTreeModel) (PATH_Tree.getModel());
		model.reload();
		//PATH_Tree.repaint();
		TreeMap<String, String> currentPaths=Keywords.project.getNamesAndPaths();
		if (currentPaths.size() > 0)
		{
			for (Iterator<String> it = currentPaths.keySet().iterator(); it.hasNext();)
			{
				String namepath = it.next();
				DefaultMutableTreeNode nodePath = new DefaultMutableTreeNode(namepath);
				String namedir = currentPaths.get(namepath);
				try
				{
					File defineddir = new File(namedir);
					FileFilter fileFilterDatasets = new FileFilter()
					{
						public boolean accept(File f)
						{
							String name=f.getName().toLowerCase();
							return name.endsWith(Keywords.DictionaryExtension.toLowerCase());
						}
					};
					File[] children = defineddir.listFiles(fileFilterDatasets);
					if (children != null && children.length!=0)
					{
						list_ds.clear();
						for (int f = 0; f < children.length; f++)
						{
							String filename = children[f].getName();
							try
							{
								filename=filename.toLowerCase();
								filename=filename.replaceAll(Keywords.DictionaryExtension.toLowerCase(),"");
								String firstChard=filename.substring(0,1);
								filename = filename.replaceFirst(firstChard,firstChard.toUpperCase());
								list_ds.add(namepath+"."+filename);
							}
							catch (Exception ee){}
						}
						if (list_ds.size()>0)
						{
							DefaultMutableTreeNode nodeDataSets = new DefaultMutableTreeNode(Keywords.Language.getMessage(1396));
							Iterator<String> itr=list_ds.iterator();
							while(itr.hasNext())
							{
								DefaultMutableTreeNode nodeDataSet = new DefaultMutableTreeNode(itr.next());
								nodeDataSets.add(nodeDataSet);
							}
							nodePath.add(nodeDataSets);
						}
					}
					FileFilter fileFilterDocuments = new FileFilter()
					{
						public boolean accept(File f)
						{
							String name=f.getName().toLowerCase();
							return name.endsWith(Keywords.DocExtension.toLowerCase());
						}
					};
					children = defineddir.listFiles(fileFilterDocuments);
					if (children != null && children.length!=0)
					{
						DefaultMutableTreeNode nodeDocuments = new DefaultMutableTreeNode(Keywords.Language.getMessage(1397));
						for (int f = 0; f < children.length; f++)
						{
							String filename = children[f].getName();
							try
							{
								filename=filename.toLowerCase();
								filename=filename.replaceAll(Keywords.DocExtension.toLowerCase(),"");
								String firstChard=filename.substring(0,1);
								filename = filename.replaceFirst(firstChard,firstChard.toUpperCase());
								DefaultMutableTreeNode nodeDocument = new DefaultMutableTreeNode(namepath+"."+filename);
								nodeDocuments.add(nodeDocument);
							}
							catch (Exception ee){}
						}
						nodePath.add(nodeDocuments);
					}
				} catch (Exception ex) {}
				nodePaths.add(nodePath);
			}
		}
		model = (DefaultTreeModel) (PATH_Tree.getModel());
		model.reload();
		PATH_Tree.setModel(new DefaultTreeModel(nodePaths));
		try
		{
			PATH_Tree.repaint();
			//PATH_Tree.validate();
		}
		catch (Exception er){}

		DefaultMutableTreeNode nodeDefines = (DefaultMutableTreeNode)DEFINE_Tree.getModel().getRoot();
		nodeDefines.removeAllChildren();
		DefaultTreeModel modelDefine = (DefaultTreeModel) (DEFINE_Tree.getModel());
		modelDefine.reload();
		//DEFINE_Tree.repaint();
		TreeMap<String, String> currentDefine=Keywords.project.getNamesAndDefinitions();
		if (currentDefine.size() > 0)
		{
			for (Iterator<String> it = currentDefine.keySet().iterator(); it.hasNext();)
			{
				String namedefine = it.next();
				String valuedefine=currentDefine.get(namedefine);
				DefaultMutableTreeNode nodeDefine = new DefaultMutableTreeNode(namedefine+"="+valuedefine);
				nodeDefines.add(nodeDefine);
			}
		}
		modelDefine = (DefaultTreeModel) (DEFINE_Tree.getModel());
		modelDefine.reload();
		DEFINE_Tree.setModel(new DefaultTreeModel(nodeDefines));
		try
		{
			DEFINE_Tree.repaint();
			//DEFINE_Tree.validate();
		}
		catch (Exception er){}

		DefaultMutableTreeNode nodeSettings = (DefaultMutableTreeNode)SETTING_Tree.getModel().getRoot();
		nodeSettings.removeAllChildren();
		DefaultTreeModel modelSettings = (DefaultTreeModel) (SETTING_Tree.getModel());
		modelSettings.reload();
		//SETTING_Tree.repaint();
		Vector<String> settings = Keywords.project.getSettingsTypes();
		if (settings.size() > 0)
		{
			for (int i = 0; i < Keywords.KeywordsForSetting.length; i++)
			{
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(Keywords.KeywordsForSetting[i]);
				Vector<String> names = Keywords.project.getSettingNames(Keywords.KeywordsForSetting[i]);
				Iterator<String> j = names.iterator();
				while (j.hasNext())
				{
					String name = j.next();
					DefaultMutableTreeNode nodeset = new DefaultMutableTreeNode(name);
					node.add(nodeset);
				}
				nodeSettings.add(node);
			}
			Vector<String> outnames = Keywords.project.getSettingNames(Keywords.OUT);
			DefaultMutableTreeNode nodeout = new DefaultMutableTreeNode(Keywords.OUT);
			if (outnames.size() > 0)
			{
				for (int i = 0; i < outnames.size(); i++)
				{
					String name = outnames.get(i);
					DefaultMutableTreeNode nodeset = new DefaultMutableTreeNode(name);
					nodeout.add(nodeset);
				}
			}
			nodeSettings.add(nodeout);
			for (int i = 0; i < settings.size(); i++)
			{
				String tempsetname = settings.get(i);
				boolean notdefset = false;
				for (int k = 0; k < Keywords.KeywordsForSetting.length; k++)
				{
					if (tempsetname	.equalsIgnoreCase(Keywords.KeywordsForSetting[k]))
						notdefset = true;
				}
				if (tempsetname.equalsIgnoreCase(Keywords.OUT))
					notdefset = true;
				if (!notdefset)
				{
					DefaultMutableTreeNode othernode = new DefaultMutableTreeNode(Keywords.Language.getMessage(397));
					DefaultMutableTreeNode nodeset = new DefaultMutableTreeNode(tempsetname);
					Vector<String> setnamefortype = Keywords.project.getSettingNames(Keywords.OUT);
					for (int h = 0; h < setnamefortype.size(); h++)
					{
						String tempothersetname = setnamefortype.get(h);
						DefaultMutableTreeNode othernodeset = new DefaultMutableTreeNode(tempothersetname);
						nodeset.add(othernodeset);
					}
					othernode.add(nodeset);
					nodeSettings.add(othernode);
				}
			}
		}
		else
		{
			for (int i = 0; i < Keywords.KeywordsForSetting.length; i++)
			{
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(Keywords.KeywordsForSetting[i]);
				nodeSettings.add(node);
			}
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(Keywords.OUT);
			nodeSettings.add(node);
		}
		modelSettings = (DefaultTreeModel) (SETTING_Tree.getModel());
		modelSettings.reload();
		SETTING_Tree.setModel(new DefaultTreeModel(nodeSettings));
		try
		{
			SETTING_Tree.repaint();
			//SETTING_Tree.validate();
		}
		catch (Exception er){}

		DefaultMutableTreeNode nodeMacrosteps = (DefaultMutableTreeNode)MACROSTEP_Tree.getModel().getRoot();
		nodeMacrosteps.removeAllChildren();
		DefaultTreeModel modelMacrosteps = (DefaultTreeModel) (MACROSTEP_Tree.getModel());
		modelMacrosteps.reload();
		//MACROSTEP_Tree.repaint();
		TreeMap<String, Vector<String>> definedmacrosteps = Keywords.project.getAllMacroStep();
		if (definedmacrosteps.size() > 0)
		{
			for (Iterator<String> it = definedmacrosteps.keySet().iterator(); it.hasNext();)
			{
				String namems = it.next();
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(namems);
				nodeMacrosteps.add(node);
			}
		}
		modelMacrosteps = (DefaultTreeModel) (MACROSTEP_Tree.getModel());
		modelMacrosteps.reload();
		MACROSTEP_Tree.setModel(new DefaultTreeModel(nodeMacrosteps));
		try
		{
			MACROSTEP_Tree.repaint();
			//MACROSTEP_Tree.validate();
		}
		catch (Exception er){}

		Iterator<TreePath> it =openedPaths.iterator();
		while(it.hasNext())
		{
			TreePath current = it.next();
			DefaultMutableTreeNode currentNode, child=null;
			DefaultMutableTreeNode parent=nodePaths;
			for(int i=1;i<current.getPathCount();i++)
			{
				currentNode=(DefaultMutableTreeNode)current.getPathComponent(i);
				if (currentNode!=null)
				{
					child=findChild(currentNode.toString(), parent);
					if(child!=null)
					{
						parent=child;
					}
					else
					{
						break;
					}
				}
			}
			if(child!=null)
			{
				PATH_Tree.expandPath(new TreePath(child.getPath()));
			}
		}
		      }
	    });
	}
	private class ExecuteScript extends Thread
	{
		String text;
		public ExecuteScript(String text)
		{
			this.text=text;
		}
		public void run() throws NullPointerException
		{
			new ExecuteRunner(2, text);
		}
	}
	public class deleteLog extends Thread
	{
		public deleteLog()
		{
		}
		public void run()
		{
			int rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(4031), Keywords.Language.getMessage(134), JOptionPane.YES_NO_OPTION);
			if (rr == 0)
			{
				rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(4032), Keywords.Language.getMessage(134), JOptionPane.YES_NO_OPTION);
				if (rr==0)
				{
					lastopeneddir=(String)System.getProperty("lastOpenedDirectory");
					JFileChooser sb = new JFileChooser(new File(lastopeneddir));
					sb.setFileFilter(new javax.swing.filechooser.FileFilter()
					{
						public boolean accept(File f)
						{
							return (f.getName().endsWith(".html") || f.isDirectory());
						}
						public String getDescription()
						{
							return Keywords.Language.getMessage(138);
						}
					});
					sb.setAcceptAllFileFilterUsed(false);
					try
					{
						if (sb.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
						{
							File save = sb.getSelectedFile();
							File newFile;
							if (!save.getName().endsWith(".html"))
								newFile = new File(save.getAbsolutePath()+ ".html");
							else
								newFile = new File(save.getAbsolutePath());
							boolean exists = (newFile.exists());
							if (exists)
							{
								rr = JOptionPane.showInternalConfirmDialog(desktop,Keywords.Language.getMessage(135),Keywords.Language.getMessage(134),JOptionPane.YES_NO_OPTION);
								if (rr != 0)
									return;
							}
							Keywords.semwritelog.acquire();
							BufferedWriter outfile= new BufferedWriter(new FileWriter(newFile));
							temp_content = MessageArea.getText();
							outfile.write(temp_content);
							outfile.close();
							lastopeneddir = save.getParent();
							try
							{
								lastopeneddir=lastopeneddir.replaceAll("\\\\","/");
							}
							catch (Exception fs){}
							if (!lastopeneddir.endsWith(System.getProperty("file.separator")))
								lastopeneddir=lastopeneddir+System.getProperty("file.separator");
							System.setProperty("lastOpenedDirectory", lastopeneddir);
							Keywords.semwritelog.release();
						}
					}
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(3952)+"\n"+e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
						Keywords.semwritelog.release();
						return;
					}
				}
				try
				{
					MessageArea.setText("");
					Keywords.semwritelog.acquire();
					String logfile="<i>"+Keywords.Language.getMessage(90) + "<br>\n";
					logfile=logfile+Keywords.Language.getMessage(145) + ": "+ (System.getProperty("os.name")) + " - "+ Keywords.Language.getMessage(125) + ": "+ (System.getProperty("os.version"))+"<br>\n";
					logfile=logfile+ Keywords.Language.getMessage(126) + ": "+ (System.getProperty("java.version"))+"<br>\n";
					int infototmemory= (int)((Runtime.getRuntime().maxMemory())/1024);
					logfile=logfile+Keywords.Language.getMessage(3168) + ": "+ String.valueOf(infototmemory)+"<br>\n";
					Object[] paramsdate = new Object[] { new Date(), new Date(0) };
					logfile=logfile+MessageFormat.format(Keywords.Language.getMessage(127)+ " {0}", paramsdate)+"<br>\n";
					logfile=logfile+Keywords.Language.getMessage(128) + ": "+ System.getProperty(Keywords.WorkDir)+"<br>\n";
					logfile=logfile+Keywords.Language.getMessage(2630) + "\n"+ Keywords.Language.getMessage(3056)+"<br><br></i>\n";
					BufferedWriter outlogfile= new BufferedWriter(new FileWriter(new File(out_logfile)));
					outlogfile.write(logfile);
					outlogfile.close();
					Keywords.semwritelog.release();
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(desktop, Keywords.Language.getMessage(790)+"\n"+e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
					Keywords.semwritelog.release();
				}
			}
			muchlog=false;
		}
	}
	public class SystemWatcher extends Thread
	{
		boolean executeSystemInfo;
		long log_modified;
		long out_modified;
		long log_length;
		long out_length;
		BufferedReader inlogfile;
		BufferedReader inoutfile;
		String linefile;
		int permission_toaccess;
		String text_toinsert;
		StringBuffer sb;
		deleteLog dlog;
		String temp_content_out;
		public SystemWatcher()
		{
			executeSystemInfo=true;
			log_modified=(new File(out_logfile)).lastModified();
			out_modified=(new File(out_outfile)).lastModified();
			log_length=(new File(out_logfile)).length();
			out_length=(new File(out_outfile)).length();
			text_toinsert="";
			muchlog=false;
			dlog=new deleteLog();
			temp_content_out="";
		}
		public void endSystemInfo()
		{
			executeSystemInfo=false;
		}
		public void run()
		{
			if (minimal)
			{
				while (executeSystemInfo)
				{
					if ((new File(out_outfile)).lastModified()!=out_modified || (new File(out_outfile)).length()!=out_length)
					{
						try
						{
							temp_content_out="";
							permission_toaccess=Keywords.semwriteOut.availablePermits();
							if (permission_toaccess>0)
							{
								Keywords.semwriteOut.acquire();
								BufferedWriter outoutfile= new BufferedWriter(new FileWriter(new File(out_outfile)));
								outoutfile.write("");
								outoutfile.close();
								Keywords.semwriteOut.release();
								out_modified=(new File(out_outfile)).lastModified();
								out_length=(new File(out_outfile)).length();
							}
						}
						catch (Exception e) {}
					}
					if ((new File(out_logfile)).lastModified()!=log_modified || (new File(out_logfile)).length()!=log_length)
					{
						try
						{
							permission_toaccess=Keywords.semwritelog.availablePermits();
							{
								BufferedWriter outlogfile= new BufferedWriter(new FileWriter(new File(out_logfile)));
								outlogfile.write("");
								outlogfile.close();
							}
							Keywords.semwritelog.release();
						}
						catch (Exception e) {}
					}
					try
					{
						Thread.sleep(1000);
					}
					catch (Exception e) {}
				}
			}
			if (!minimal)
			{
				while (executeSystemInfo)
				{
					tdouble = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024;
					mdouble = (Runtime.getRuntime().maxMemory())/1024;
					textperc = percentmem.format(100*tdouble/mdouble);
					SwingUtilities.invokeLater(new Runnable()
					{
						  public void run()
						  {
								totalMemory.setText(ttext+intnum.format(tdouble)+ " ("+textperc+"%)");
								maxMemory.setText(mtext+intnum.format(mdouble));
								if (Keywords.currentExecutedStep.equals("")) currentstep.setText("-");
								if (!Keywords.currentExecutedStep.equals("")) currentstep.setText(Keywords.currentExecutedStep);
								if (Keywords.operationWriting)
								{
									writestate.setIcon(YesWrite);
									writingio.setText(" "+intnum.format(Keywords.numwrite)+" ");
								}
								else
								{
									writestate.setIcon(NoWrite);
									writingio.setText(" - ");
								}
								if (Keywords.operationReading)
								{
									readstate.setIcon(YesRead);
									readingio.setText(" "+intnum.format(Keywords.numread)+" ");
								}
								else
								{
									readingio.setText(" - ");
									readstate.setIcon(NoRead);
								}
								if (!Keywords.currentExecutedStep.equals(""))
								{
									try
									{
										if (Keywords.percentage_total<Keywords.percentage_done) Keywords.percentage_done=Keywords.percentage_total-1;
										if (Keywords.percentage_total>0 && Keywords.percentage_done>0)
										{
											if (Keywords.percentage_done>Keywords.percentage_total) Keywords.percentage_total=Keywords.percentage_done+1;
											pbar.setMaximum(Keywords.percentage_total);
											pbar.setValue(Keywords.percentage_done);
										}
										if (Keywords.general_percentage_total>0 && Keywords.general_percentage_done>0)
										{
											if (Keywords.general_percentage_done>Keywords.general_percentage_total) Keywords.general_percentage_total=Keywords.general_percentage_done+1;
											genbar.setMaximum(Keywords.general_percentage_total);
											genbar.setValue(Keywords.general_percentage_done);
										}
										try
										{
											pbar.repaint();
											genbar.repaint();
											pbar.setStringPainted(true);
											genbar.setStringPainted(true);
										}
										catch (Exception rep){}
									}
									catch (Exception epb){}
									SetRunningInterface();
									pastexecution=false;
								}
								else
								{
									pbar.setMaximum(0);
									pbar.setValue(0);
									genbar.setMaximum(0);
									genbar.setValue(0);
									pbar.setStringPainted(false);
									genbar.setStringPainted(false);
								}
							}
					});
					if ((new File(out_outfile)).lastModified()!=out_modified || (new File(out_outfile)).length()!=out_length)
					{
						try
						{
							temp_content_out="";
							permission_toaccess=Keywords.semwriteOut.availablePermits();
							if (permission_toaccess>0)
							{
								Keywords.semwriteOut.acquire();
								try
								{
									inoutfile = new BufferedReader(new FileReader(out_outfile));
									linefile = inoutfile.readLine();
									while(linefile != null)
									{
										temp_content_out=temp_content_out+linefile;
										linefile = inoutfile.readLine();
									}
									inoutfile.close();
									inoutfile=null;
								}
								catch (Exception ed)
								{
									if (!System.getProperty("gui_error").equals(""))
									{
										StringWriter SWex = new StringWriter();
										PrintWriter PWex = new PrintWriter(SWex);
										ed.printStackTrace(PWex);
										try
										{
											String gui_error=System.getProperty("gui_error");
											BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
											g_error.write(SWex.toString()+"\n");
											g_error.close();
											System.setProperty("gui_error", gui_error);
										}
										catch (Exception ef){}
									}
								}
								if (inoutfile!=null)
								{
									try
									{
										inoutfile.close();
										inoutfile=null;
									}
									catch (Exception ef){}
								}
								if (!temp_content_out.equals(""))
								{
									BufferedWriter outoutfile= new BufferedWriter(new FileWriter(new File(out_outfile)));
									outoutfile.write("");
									outoutfile.close();
								}
								Keywords.semwriteOut.release();
								out_modified=(new File(out_outfile)).lastModified();
								out_length=(new File(out_outfile)).length();
								if (!temp_content_out.equals(""))
								{
									temp_content_out=temp_content_out+"<br><br>";
									try
									{
										SwingUtilities.invokeLater(new Runnable()
										{
											  public void run(){
													HTMLDocument docout=(HTMLDocument) OutputArea.getDocument();
													if (docout!=null)
													{
														HTMLDocument blank = new HTMLDocument();
														OutputArea.setDocument(blank);
														OutputArea.repaint();
														try
														{
															docout.insertAfterEnd(docout.getCharacterElement(docout.getLength()),temp_content_out);
														}
														catch (Exception eee){}
														OutputArea.setDocument(docout);
														OutputArea.setCaretPosition(OutputArea.getDocument().getLength());
														OutputArea.repaint();
													}
											  }
										});
									}
									catch (Exception ed)
									{
										if (!System.getProperty("gui_error").equals(""))
										{
											StringWriter SWex = new StringWriter();
											PrintWriter PWex = new PrintWriter(SWex);
											ed.printStackTrace(PWex);
											try
											{
												String gui_error=System.getProperty("gui_error");
												BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
												g_error.write(SWex.toString()+"\n");
												g_error.close();
												System.setProperty("gui_error", gui_error);
											}
											catch (Exception ef){}
										}
									}
								}
							}
						}
						catch (Exception e)
						{
							if (!System.getProperty("gui_error").equals(""))
							{
								StringWriter SWex = new StringWriter();
								PrintWriter PWex = new PrintWriter(SWex);
								e.printStackTrace(PWex);
								try
								{
									String gui_error=System.getProperty("gui_error");
									BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
									g_error.write(SWex.toString()+"\n");
									g_error.close();
									System.setProperty("gui_error", gui_error);
								}
								catch (Exception ef){}
							}
						}
						pastexecution=false;
					}
					if ((new File(out_logfile)).lastModified()!=log_modified || (new File(out_logfile)).length()!=log_length)
					{
						try
						{
							temp_content="";
							permission_toaccess=Keywords.semwritelog.availablePermits();
							if (permission_toaccess>0)
							{
								Keywords.semwritelog.acquire();
								try
								{
									inlogfile = new BufferedReader(new FileReader(out_logfile));
									linefile = inlogfile.readLine();
									while(linefile != null)
									{
										temp_content=temp_content+linefile;
										linefile = inlogfile.readLine();
									}
									inlogfile.close();
									inlogfile=null;
									Thread.sleep(10);
								}
								catch (Exception ed)
								{
									if (!System.getProperty("gui_error").equals(""))
									{
										StringWriter SWex = new StringWriter();
										PrintWriter PWex = new PrintWriter(SWex);
										ed.printStackTrace(PWex);
										try
										{
											String gui_error=System.getProperty("gui_error");
											BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
											g_error.write(SWex.toString()+"\n");
											g_error.close();
											System.setProperty("gui_error", gui_error);
										}
										catch (Exception ef){}
									}
								}
								if (inlogfile!=null)
								{
									try
									{
										inlogfile.close();
										inlogfile=null;
									}
									catch (Exception ef){}
								}
								if (!temp_content.equals(""))
								{
									BufferedWriter outlogfile= new BufferedWriter(new FileWriter(new File(out_logfile)));
									outlogfile.write("");
									outlogfile.close();
								}
								Keywords.semwritelog.release();
								if (!temp_content.equals(""))
								{
									try
									{
										SwingUtilities.invokeLater(new Runnable()
										{
											  public void run(){
													HTMLDocument doc=(HTMLDocument) MessageArea.getDocument();
													if (doc.getLength()>300000) muchlog=true;
													if (doc!=null)
													{
														HTMLDocument blank = new HTMLDocument();
														MessageArea.setDocument(blank);
														MessageArea.repaint();
														try
														{
															doc.insertAfterEnd(doc.getCharacterElement(doc.getLength()),temp_content);
														}
														catch (Exception eee){}
														MessageArea.setDocument(doc);
														MessageArea.setCaretPosition(MessageArea.getDocument().getLength());
														MessageArea.repaint();
													}
											  }
										});
									}
									catch (Exception ed)
									{
										if (!System.getProperty("gui_error").equals(""))
										{
											StringWriter SWex = new StringWriter();
											PrintWriter PWex = new PrintWriter(SWex);
											ed.printStackTrace(PWex);
											try
											{
												String gui_error=System.getProperty("gui_error");
												BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
												g_error.write(SWex.toString()+"\n");
												g_error.close();
												System.setProperty("gui_error", gui_error);
											}
											catch (Exception ef){}
										}
									}
								}
								log_modified=(new File(out_logfile)).lastModified();
								log_length=(new File(out_logfile)).length();
							}
						}
						catch (Exception e)
						{
							if (!System.getProperty("gui_error").equals(""))
							{
								StringWriter SWex = new StringWriter();
								PrintWriter PWex = new PrintWriter(SWex);
								e.printStackTrace(PWex);
								try
								{
									String gui_error=System.getProperty("gui_error");
									BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
									g_error.write(SWex.toString()+"\n");
									g_error.close();
									System.setProperty("gui_error", gui_error);
								}
								catch (Exception ef){}
							}
						}
						pastexecution=false;
					}
					if (Keywords.currentExecutedStep.equals("") && !pastexecution && Keywords.opened_datasets.size()==0)
					{
						SwingUtilities.invokeLater(new Runnable()
						{
							  public void run()
							  {
								  SetStoppedInterface();
							  }
						});
					}
					try
					{
						Thread.sleep(1000);
						if (!Keywords.currentExecutedStep.equals("")) pastexecution=false;
						if (Keywords.currentExecutedStep.equals("") && !loadscript.isEnabled()) SetStoppedInterface();
						if (muchlog && !dlog.isAlive())
						{
							dlog=new deleteLog();
							dlog.start();
						}
					}
					catch (Exception e)
					{
						if (!System.getProperty("gui_error").equals(""))
						{
							StringWriter SWex = new StringWriter();
							PrintWriter PWex = new PrintWriter(SWex);
							e.printStackTrace(PWex);
							try
							{
								String gui_error=System.getProperty("gui_error");
								BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
								g_error.write(SWex.toString()+"\n");
								g_error.close();
								System.setProperty("gui_error", gui_error);
							}
							catch (Exception ef){}
						}
					}
				}
			}
		}
	}
	private DefaultMutableTreeNode findChild(String name, DefaultMutableTreeNode parent)
	{
		if (parent!=null)
		{
			for(int i=0;i<parent.getChildCount();i++)
			{
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)parent.getChildAt(i);
				if(child.getUserObject().toString().equals(name))
				{
					return child;
				}
			}
		}
		return null;
	}
	private void SetRunningInterface()
	{
		try
		{
			for (int p=0; p<tabsEnvironment.getTabCount(); p++)
			{
				tabsEnvironment.setBackgroundAt(p, Color.red);
				tabsEnvironment.setForegroundAt(p, Color.red);
			}
		}
		catch (Exception etab)
		{
			if (!System.getProperty("gui_error").equals(""))
			{
				StringWriter SWex = new StringWriter();
				PrintWriter PWex = new PrintWriter(SWex);
				etab.printStackTrace(PWex);
				try
				{
					String gui_error=System.getProperty("gui_error");
					BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
					g_error.write(SWex.toString()+"\n");
					g_error.close();
					System.setProperty("gui_error", gui_error);
				}
				catch (Exception ef){}
			}
		}
		createInitfiles.setEnabled(false);
		viewSources.setEnabled(false);
		saveMessageArea.setEnabled(false);
		deleteMessageArea.setEnabled(false);
		deleteOutput.setEnabled(false);
		saveOutput.setEnabled(false);
		loadscript.setEnabled(false);
		executescript.setEnabled(false);
		haltscript.setEnabled(true);
		savescript.setEnabled(false);
		newscript.setEnabled(false);
		addscriptms.setEnabled(false);
		runStep.setEnabled(false);
		searchStep.setEnabled(false);
		addStepinEditor.setEnabled(false);
		runIO.setEnabled(false);
		newPath.setEnabled(false);
		delPath.setEnabled(false);
		if (!Keywords.currentExecutedStep.equals("Viewing data set")) viewDS.setEnabled(false);
		if (Keywords.currentExecutedStep.equals("Viewing data set"))
		{
			if (treeOnDS) viewDS.setEnabled(true);
			haltscript.setEnabled(false);
		}
		if (Keywords.currentExecutedStep.equals("Operations on PATH")) haltscript.setEnabled(false);
		newDefine.setEnabled(false);
		delDefine.setEnabled(false);
		newSetting.setEnabled(false);
		delSetting.setEnabled(false);
		executeMacrostep.setEnabled(false);
		IncreaseSizeElements.setEnabled(false);
		DecreaseSizeElements.setEnabled(false);
		/*Update.setEnabled(false);*/
		License.setEnabled(false);
		Author.setEnabled (false);
		IOTree.clearSelection();
		Steps_Tree.clearSelection();
	}
	private void SetStoppedInterface()
	{
		try
		{
			try
			{
				for (int p=0; p<tabsEnvironment.getTabCount(); p++)
				{
					tabsEnvironment.setBackgroundAt(p, backTab);
					tabsEnvironment.setForegroundAt(p, foreTab);
				}
			}
			catch (Exception etab)
			{
				if (!System.getProperty("gui_error").equals(""))
				{
					StringWriter SWex = new StringWriter();
					PrintWriter PWex = new PrintWriter(SWex);
					etab.printStackTrace(PWex);
					try
					{
						String gui_error=System.getProperty("gui_error");
						BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
						g_error.write(SWex.toString()+"\n");
						g_error.close();
						System.setProperty("gui_error", gui_error);
					}
					catch (Exception efd){}
				}
			}
			Keywords.stop_script=false;
			setEnvironment();
			haltscript.setEnabled(false);
			createInitfiles.setEnabled(true);
			viewSources.setEnabled(true);
			IncreaseSizeElements.setEnabled(true);
			DecreaseSizeElements.setEnabled(true);
			/*if (!updateDownloaded) Update.setEnabled(true);
			else Update.setEnabled(false);*/
			License.setEnabled(true);
			Author.setEnabled (true);
			newPath.setEnabled(true);
			newDefine.setEnabled(true);
			newSetting.setEnabled(false);
			if (EditorArea.getText().trim().equals(""))
			{
				loadscript.setEnabled(true);
				executescript.setEnabled(false);
				savescript.setEnabled(false);
				newscript.setEnabled(false);
				addscriptms.setEnabled(false);
			}
			else
			{
				loadscript.setEnabled(true);
				executescript.setEnabled(true);
				savescript.setEnabled(true);
				newscript.setEnabled(true);
				addscriptms.setEnabled(true);
			}
			deleteMessageArea.setEnabled(true);
			saveMessageArea.setEnabled(true);
			deleteOutput.setEnabled(true);
			saveOutput.setEnabled(true);
			if (!stepSelection.equals("") && window_selected.equals("runStep")) runStep.setEnabled(true);
			if (!stepSelection.equals("") && window_selected.equals("runIO")) runIO.setEnabled(true);
			searchStep.setEnabled(true);
			addStepinEditor.setEnabled(runStep.isEnabled());
			Keywords.general_percentage_total=0;
			Keywords.general_percentage_done=0;
			Keywords.percentage_total=0;
			Keywords.percentage_done=0;
			pastexecution=true;
			writestate.setIcon(NoWrite);
			writingio.setText(" - ");
			readingio.setText(" - ");
			readstate.setIcon(NoRead);
		}
		catch (Exception ef)
		{
			if (!System.getProperty("gui_error").equals(""))
			{
				StringWriter SWex = new StringWriter();
				PrintWriter PWex = new PrintWriter(SWex);
				ef.printStackTrace(PWex);
				try
				{
					String gui_error=System.getProperty("gui_error");
					BufferedWriter g_error= new BufferedWriter(new FileWriter(new File(gui_error),true));
					g_error.write(SWex.toString()+"\n");
					g_error.close();
					System.setProperty("gui_error", gui_error);
				}
				catch (Exception efg){}
			}
		}
	}
}
