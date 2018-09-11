/**
* Copyright © 2015 ADaMSoft
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

package ADaMSoft.keywords;

import java.util.concurrent.Semaphore;
import java.util.LinkedList;
import java.util.HashSet;

import java.util.Vector;
import java.util.Hashtable;

import ADaMSoft.gui.MainGUI;
import ADaMSoft.utilities.LanguageReader;
import ADaMSoft.utilities.Project;
import ADaMSoft.dataaccess.MemoryValue;

/**
* This is the class that defines all the keywords
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/
public class Keywords
{
	/*The following keywords are used in the ini file */
	public static String schema="schema";
	public static String proxyHost="proxyHost";
	public static String proxyPort="proxyPort";
	public static String proxyUser="proxyUser";
	public static String proxyPassword="proxyPassword";
	public static String pdffonts="PdfFonts";
	public static String varmaxtime="varmaxtime";

	public static String launcherlocation="launcherlocation";
	public static String javamemory="javamemory";

	public static String iniproxyHost="iniproxyHost";
	public static String iniproxyPort="iniproxyPort";
	public static String iniproxyUser="iniproxyUser";
	public static String iniproxyPassword="iniproxyPassword";
	public static String iniMaxDBRecords="iniMaxDBRecords";
	public static String iniMaxDataBuffered="iniMaxDataBuffered";
	public static String iniFileBufferDim="iniFileBufferDim";
	public static String just_html="just_html";

	public static String varquery="varquery";
	public static String waitbetween="waitbetween";

	public static String varmaxpages="varmaxpages";

	public static String servertype="servertype";
	public static String aserver="aserver";
	public static String ADaMSoftINI="ADaMSoftINI";
	public static String ADaMSoftStarter="ADaMSoftStarter";
	public static String shellcommand="shellcommand";
	public static String cexamaxqueue="cexamaxqueue";

	public static String parameterfileandtagsets="parameterfileandtagsets";

	public static String numthreads="numthreads";
	public static String considerfreq="considerfreq";
	public static String limit_temp_files="limit_temp_files";

	public static String vertical_results="vertical_results";

	/*The following keywords are used by the Setup Interpreter*/

	public static String[] VersionJavaCompiler=new String[] {"1.9", "9", "10", "1.10"};

	public static String[] ProcForServer=new String[] {"PROCCONNECT2SERVER", "PROCSERVERADMIN", "PROCUPLOAD", "PROCDOWNLOAD"};
	public static String [] KeywordsForPathAndName=new String[] {"DICT", "OUTDICT","OUTREPORT"};
	public static String [] KeywordsForPath       =new String[] {"DATA", "DOCPATH", "OUTPATH", "PATH"};
	public static String [] KeywordsForSetting    =new String[] {"CODELABEL","HTMLDSLAYOUT", "HTMLLAYOUT","XLSDSLAYOUT", "XLSLAYOUT","HSQLDBLOCAL", "HSQLDBREMOTE", "MAILSERVER", "MSODBC", "MYSQL", "BIGSQL", "PDFLAYOUT", "PDFDSLAYOUT", "POSTGRESQL","REMOTE", "ORACLE", "ASERVER", "SQLSERVER"};
	public static String [] KeywordsWithEnd       =new String[] {"SETTING"};
	public static String [] KeywordsWithRun       =new String[] {"PROC","ADAMSDOC","DATASET","DICTIONARY","REPORT","TOC","EXT", "SCL", "SQL"};
	public static String [] SimpleKeywords        =new String[] {"PATH", "MSG", "OPTION", "PROJECT", "EXECUTE", "DEFINE", "EXEMACROSTEP","DELMACROSTEP","EXPORTDS2OUT"};
	public static String [] KeywordsForOutDs      =new String[] {"OUT"};
	public static String [] KeywordsForDBSettings =new String[] {"BIGSQL", "HSQLDBLOCAL", "HSQLDBREMOTE", "MSODBC", "MYSQL", "POSTGRESQL", "ORACLE", "SQLSERVER"};
	public static String [] datatypewithpath =new String[] {"adamsoft", "dlmfile", "fixedtxt", "xlsfile", "adamsoftnoc", "sas"};
	public static String 	LocalizeNumbers="LocalizeNumbers";
	public static String 	AutoConvertValues="AutoConvertValues";
	public static String SQL       ="SQL";
	public static String outdb       ="OUTDB";
	public static HashSet<String> SQL_DB       =new HashSet<String>();
	public static String MEMORY    ="MEMORY";
	public static String dbset     ="dbset";
	public static String viewout   ="viewout";
	public static String logdir    ="logdir";
	public static String dictnet   ="dictnet";
	public static String dicttest  ="dicttest";
	public static String docpath   ="docpath";
	public static String outpath   ="outpath";
	public static String EXPORTDS2OUT   ="EXPORTDS2OUT";
	public static String LAYOUT    ="LAYOUT";
	public static String DICT      ="DICT";
	public static String OUT       ="OUT";
	public static String OUTV      ="OUTV";
	public static String OUTRE     ="OUTRE";
	public static String OUTIE     ="OUTIE";
	public static String OUTNET    ="OUTNET";
	public static String OUTHIST   ="OUTHIST";
	public static String OUTI     ="OUTI";
	public static String OUTC     ="OUTC";
	public static String OUTR     ="OUTR";
	public static String OUTD     ="OUTD";
	public static String OUTU     ="OUTU";
	public static String OUTS     ="OUTS";
	public static String OUTM     ="OUTM";
	public static String OUTE     ="OUTE";
	public static String OUTSTMD  ="OUTSTMD";
	public static String OUTSTREG ="OUTSTREG";
	public static String OUTTRAIN   ="OUTTRAIN";
	public static String OUTTEST    ="OUTTEST";
	public static String out       ="out";
	public static String view      ="view";
	public static String VIEW      ="VIEW";
	public static String memtable  ="memtable";
	public static String REMOTE    ="REMOTE";
	public static String ASERVER   ="ASERVER";
	public static String SETTING   ="SETTING";
	public static String exeupdate ="exeupdate";
	public static String cexfile   ="cexfile";
	public static String cmdfile   ="cmdfile";
	public static String where   ="where";

	public static String char_cookie_separator="char_cookie_separator";

	public static String procrcaller="procrcaller";

	public static String vartokeep="vartokeep";

	public static String startdir   ="startdir";
	public static String duckduckgo   ="duckduckgo";

	public static String saveasext ="saveasext";
	public static String parameternum ="parameternum";
	public static String parametertext="parametertext";

	public static String saveas ="saveas";
	public static String varwordsdocs ="varwordsdocs";

	public static String filestoselect="filestoselect";

	public static String MACROSTEP     ="MACROSTEP";
	public static String MEND          ="MEND";
	public static String EXEMACROSTEP  ="EXEMACROSTEP";
	public static String DELMACROSTEP  ="DELMACROSTEP";

	public static String dictxy="dictxy";
	public static String dictxint="dictxint";
	public static String OUTEST="OUTEST";
	public static String degree="degree";

	public static String varxdata="varxdata";
	public static String varydata="varydata";
	public static String varxint="varxint";

	public static String END       ="END";
	public static String EXT       ="EXT";
	public static String RUN       ="RUN";
	public static String PATH      ="PATH";
	public static String MSG       ="MSG";
	public static String HELP      ="HELP";
	public static String OPTION    ="OPTION";
	public static String PROC      ="PROC";
	public static String DEFINE    ="DEFINE";
	public static String DOCUMENT  ="ADAMSDOC";
	public static String Information="Information";
	public static String document  ="adamsdoc";
	public static String DATASET   ="DATASET";
	public static String SCL       ="SCL";
	public static String JAVACODE  ="JAVACODE";
	public static String DICTIONARY="DICTIONARY";
	public static String REPORT    ="REPORT";
	public static String TOC       ="TOC";
	public static String USE       ="USE";
	public static String SAVE      ="SAVE";
	public static String PROJECT   ="PROJECT";
	public static String CODELABEL ="CODELABEL";
	public static String EXECUTE   ="EXECUTE";
	public static String DEBUG     ="DEBUG";
	public static String NODEBUG   ="NODEBUG";
	public static String other     ="other";
	public static String Paths     ="Paths";
	public static String Pathname  ="Pathname";
	public static String Pathtree  ="Pathtree";
	public static String Defines   ="Defines";
	public static String Definename  ="Definename";
	public static String Definevalue  ="Definevalue";
	public static String out_datatype ="out_datatype";
	public static String _datatype ="_datatype";
	public static String datatype ="datatype";
	public static String out_dict ="out_dict";
	public static String out_data ="out_data";
	public static String _dict ="_dict";
	public static String _data ="_data";
	public static String client_host ="client_host";
	public static String SORTED ="SORTED";
	public static String out_suffix ="out_";
	public static String nohaltonerror ="nohaltonerror";
	public static String haltonerror ="haltonerror";
	public static String nolog ="nolog";
	public static String log ="log";
	public static String work="work";
	public static String workdir="workdir";
	public static String clear="clear";
	public static String suffixvariable="v";
	public static String docpwd="docpwd";
	public static String tablename="tablename";
	public static String remotedictfile="remotedictfile";
	public static String onesheetfords="onesheetfords";

	public static String STARTSWITH="STARTSWITH";
	public static String ENDSWITH="ENDSWITH";
	public static String INF="INF";
	public static String IGNORECASE="IGNORECASE";

	public static String consider_couple="consider_couple";
	public static String consider_triple="consider_triple";

	public static String associatewithmax="associatewithmax";

	public static String sasdataset="sasdataset";
	public static String sas="sas";

	public static String SETARRAY="SETARRAY";
	public static String ARRAY="ARRAY";
	public static String ARRAYNUM="ARRAYNUM";

	public static String varphrase="varphrase";
	public static String varrefterm="varrefterm";
	public static String varrefweight="varrefweight";
	public static String varrefpolarity="varrefpolarity";
	public static String negations="negations";

	/*Here are the keywords used in the setting sintax*/
	public static String server   ="server";
	public static String user     ="user";
	public static String password ="password";
	public static String port     ="port";
	public static String service  ="service";

	public static String avoidquotationmarks  ="avoidquotationmarks";
	public static String trimchars="trimchars";

	/*Here are the keywords that represent the variables of the main class*/
	public static String WorkDir="WorkDir";
	public static String ServerWorkDir="ServerWorkDir";
	public static String LanguagesDir="LanguagesDir";
	public static String HelpProceduresDir="HelpProceduresDir";
	public static String MethodsDir="MethodsDir";
	public static String KeywordsDir="KeywordsDir";
	public static String ADaMSoftDir="ADaMSoftDir";
	public static String ConfigFile="ConfigFile";
	public static String MaxDataBuffered="MaxDataBuffered";
	public static String MaxDBRecords="MaxDBRecords";
	public static String FileBufferDim="FileBufferDim";
	public static String SoftwareName="ADaMSoft";
	public static String DocumentName="ADaMSoft";
	public static String DefaultLanguage="DefaultLanguage";
	public static String DefaultCountry="DefaultCountry";
	public static String ServerUpdate="ServerUpdate";
	public static String LogDir="LogDir";
	public static String AuthIP="AuthIP";
	public static String RemoteUser="RemoteUser";
	public static String AuthLimIP="AuthLimIP";
	public static String RemoteLimUser="RemoteLimUser";
	public static String PortListening="PortListening";
	public static String remoteuser="remoteuser";
	public static String GUISettings="GUISettings";

	/*Here are the keywords used to represent the dictionary information*/
	public static String excel="excel";
	public static String tabdlmfile="tabdlmfile";

	public static String LabelOfVariable="LabelOfVariable";
	public static String CodeLabels="CodeLabels";
	public static String VariableName="VariableName";
	public static String MissingDataValues="MissingDataValues";
	public static String Code="Code";
	public static String MissingData="MissingData";
	public static String Rule="Rule";
	public static String LabelVar="Label";
	public static String Variable="Variable";
	public static String CodeLabel="CodeLabel";
	public static String LABEL="LABEL";
	public static String LabelVAR="Label";
	public static String VariableReference="VariableReference";
	public static String ExcelVariableColumn="ExcelVariableColumn";
	public static String FixedFileVariableStart="FixedFileVariableStart";
	public static String FixedFileVariableEnd="FixedFileVariableEnd";
	public static String NumberOfDecimals="NumberOfDecimals";
	public static String PostgresqlVariableName="PostgresqlVariableName";
	public static String POSTGRESQL="POSTGRESQL";
	public static String BigSQLVariableName="BigSQLVariableName";
	public static String BIGSQL="BIGSQL";
	public static String postgresql="postgresql";
	public static String MysqlVariableName="MysqlVariableName";
	public static String oraclevariablename="oraclevariablename";
	public static String VariableNumber="VariableNumber";
	public static String VariableType="VariableType";
	public static String VariablesList="VariablesList";
	public static String DataSetInfo="DataSetInfo";
	public static String DataTableType="DataTableType";
	public static String DataTableInfo="DataTableInfo";
	public static String FixedVariablesInfo="FixedVariablesInfo";
	public static String TableVariablesInfo="TableVariablesInfo";
	public static String VariableFormat="VariableFormat";
	public static String VariableGroup="VariableGroup";
	public static String NUMSuffix="NUM";
	public static String TEXTSuffix="TEXT";
	public static String INTSuffix="I";
	public static String EXPSuffix="E";
	public static String DECSuffix="D";
	public static String DATESuffix="DATE";
	public static String TIMESuffix="TIME";
	public static String DTSuffix="DATETIME";

	public static String IMPLEMENTS="IMPLEMENTS";
	public static String IMPORT="IMPORT";

	public static String aserver_address="aserver_address";
	public static String aserver_port="aserver_port";
	public static String aserver_user="aserver_user";
	public static String aserver_password="aserver_password";

	public static String maxcard="maxcard";
	public static String filetype="filetype";

	public static String mailtitle="mailtitle";

	public static String outpng="outpng";
	public static String background_png="background_png";
	public static String background_color="background_color";
	public static String fontscalar_min="fontscalar_min";
	public static String fontscalar_max="fontscalar_max";

	public static String vartofmtwdate="vartofmtwdate";
	public static String datefmt="datefmt";

	public static String dlmfile="dlmfile";
	public static String rowlabel="rowlabel";
	public static String statistics="statistics";
	public static String varclass="varclass";
	public static String codethousanddlm="codethousanddlm";
	public static String codedecimaldlm="codedecimaldlm";
	public static String put="put";

	/*Here are the keywords used to represent the Project Setting information*/
	public static String Settings="Settings";
	public static String Setting="Setting";
	public static String Values="Values";
	public static String Path="Path";
	public static String Define="Define";
	public static String CommandScripts="CommandScripts";
	public static String varitems="varitems";
	public static String varseries="varseries";
	public static String fillseries="fillseries";

	public static String consider_numbers="consider_numbers";

	public static String rcode="rcode";
	public static String rfile="rfile";
	public static String rcmd="rcmd";
	public static String rbatchoptions="rbatchoptions";

	/*Here are the extensions for the files*/
	public static String DictionaryExtension=".ADaMSdic";
	public static String DataExtension      =".ADaMSdat";
	public static String DataExtensionNoc      =".ADaMSdatnoc";
	public static String SortExtension      =".ADaMSsort";
	public static String ProjectExtension   =".ADaMSps";
	public static String ScriptExtension    =".ADaMScmd";
	public static String LogExtension       =".ADaMSlog";
	public static String DocExtension       =".ADaMSDoc";
	public static String IniExtension       =".ini";
	public static String ProcMenuExtension  =".ADaMSPmenu";
	public static String CompressedExecutable  =".ADaMScex";
	public static String InfoExecutable     ="Info.ADaMScex";

	/*Here are the extension of the icons*/
	public static String simpleicon="ADaMSoft.png";
	public static String simplelogo="logo.gif";

	/*Here are the keywords related to the compressed executable*/
	public static String mainscript="mainscript";
	public static String sendlog="sendlog";
	public static String sendout="sendout";
	public static String smtpport="smtpport";
	public static String mailto="mailto";
	public static String mailuser="mailuser";

	public static String noviewrout="noviewrout";

	/*Here are the field for the TOC*/
	public static String adamsdoc="Adamsdoc name";
	public static String adamsdict="Dictionary name";
	public static String tabletype="Data table type";
	public static String TOCType="TOCType";
	public static String Keyword="Keyword";
	public static String Description="Description";
	public static String Name="Name";
	public static String Type="Type";
	public static String author="author";
	public static String CreationDate="CreationDate";
	public static String Position="Position";
	public static String DataSet="Data Set";

	/*Here the keywords that are fields of the XML dictionary file*/
	/*Probably these can be the same also in other languages*/
	public static String dictdata           ="DATA";
	public static String dictnumbersheet    ="NUMBERSHEET";
	public static String dictnamesheet      ="NAMESHEET";
	public static String dictlabel          ="LABEL";
	public static String dictserver         ="SERVER";

	/*Here the keywords for the DataAccess methods*/
	public static String DATA               ="DATA";
	public static String DLM                ="DLM";
	public static String NUMBERSHEET        ="NUMBERSHEET";
	public static String NAMESHEET          ="NAMESHEET";
	public static String SERVER             ="SERVER";
	public static String MYSQL               ="MYSQL";
	public static String TABLE              ="TABLE";
	public static String USER               ="USER";
	public static String URLDB              ="URL";
	public static String DRIVER             ="DRIVER";
	public static String PASSWORD           ="PASSWORD";
	public static String PORT               ="PORT";
	public static String URLDRIVER          ="URL";

	/*Here the keywords used for the procedures*/
	public static String orderbyrowcodes            ="orderbyrowcodes";
	public static String orderbycolcodes            ="orderbycolcodes";
	public static String noclforvarrow            ="noclforvarrow";
	public static String userule            ="userule";
	public static String sturges            ="sturges";
	public static String scott              ="scott";
	public static String noidconvert        ="noidconvert";
	public static String novgconvert        ="novgconvert";
	public static String noclforvg          ="noclforvg";
	public static String orderclbycode      ="orderclbycode";
	public static String var                ="var";
	public static String varcoeffname       ="varcoeffname";
	public static String varcoeffval        ="varcoeffval";
	public static String varcode                ="varcode";
	public static String varlabe                ="varlabel";
	public static String replace            ="replace";
	public static String dictcl            ="dictcl";
	public static String force             ="force";
	public static String condition         ="condition";
	public static String ifdicta         ="ifdicta";
	public static String ifdictb         ="ifdictb";
	public static String both         ="both";
	public static String onlynew      ="onlynew";
	public static String shortvarrowinfo="shortvarrowinfo";
	public static String shortvarcolinfo="shortvarcolinfo";
	public static String nozerorows="nozerorows";

	public static String addfirstvars="addfirstvars";
	public static String addlastvars="addlastvars";

	public static String usewritefmt="usewritefmt";

	public static String dsname="dsname";

	public static String reduceedits="reduceedits";

	public static String zipfile="zipfile";
	public static String dirref="dirref";
	public static String fileref="fileref";
	public static String addtozip="addtozip";
	public static String replacezipfile="replacezipfile";
	public static String replacefile="replacefile";
	public static String secondstep="secondstep";

	public static String overwrite            ="overwrite";
	public static String noreplace          ="No Replace";
	public static String replaceall         ="all";
	public static String replaceformat      ="codelabel";
	public static String replacemissing     ="missing";
	public static String docname            ="docname";
	public static String docfile            ="docfile";
	public static String decryptwith        ="decryptwith";
	public static String decrypdstwith      ="decryptdswith";
	public static String outname            ="outname";
	public static String mysql              ="mysql";
	public static String oracle             ="oracle";
	public static String ORACLE             ="ORACLE";
	public static String type               ="type";
	public static String remote_server      ="remote_server";
	public static String remote_port        ="remote_port";
	public static String remote_user        ="remote_user";
	public static String remote_password    ="remote_password";
	public static String ServerPortListening="ServerPortListening";
	public static String keyword            ="keyword";
	public static String description        ="description";
	public static String position           ="position";
	public static String filename           ="filename";
	public static String encryptwith        ="encryptwith";
	public static String encryptdswith      ="encryptdswith";
	public static String encrypted          ="encrypted";
	public static String dict               ="dict";
	public static String dicti              ="dicti";
	public static String dictstmd           ="dictstmd";
	public static String vargroup           ="vargroup";
	public static String vargroupinuniverse ="vargroupinuniverse";
	public static String label              ="label";
	public static String uselabelasvarname  ="uselabelasvarname";
	public static String on                 ="ON";
	public static String no                 ="NO";
	public static String documenttoc        ="ADAMSDOC TOC";
	public static String toctype            ="toctype";
	public static String xlsfile            ="xlsfile";
	public static String field              ="field";
	public static String sheet              ="sheet";
	public static String outdict            ="outdict";
	public static String db                 ="db";
	public static String table              ="table";
	public static String txtfile            ="txtfile";
	public static String weight             ="weight";
	public static String eight             ="eight";
	public static String dlm                ="dlm";
	public static String Sorted             ="Sorted";
	public static String order              ="order";
	public static String descending         ="descending";
	public static String ascending          ="ascending";
	public static String vardescending		="vardescending";
	public static String ALL         ="ALL";
	public static String FORMATS     ="FORMATS";
	public static String MISSINGS    ="MISSINGS";
	public static String FORMAT     ="FORMAT";
	public static String MISSING    ="MISSING";
	public static String firstrow   ="firstrow";
	public static String lastrow   ="lastrow";
	public static String firstcol   ="firstcol";
	public static String outjpg    ="outjpg";
	public static String legend    ="legend";
	public static String nolegend    ="nolegend";
	public static String cross    ="cross";
	public static String orientation    ="orientation";
	public static String lastcol    ="lastcol";
	public static String parts      ="parts";
	public static String useinterpol ="useinterpol";
	public static String varoutname     ="varoutname";
	public static String tablealign     ="tablealign";
	public static String numalign      ="numalign";
	public static String textalign     ="textalign";

	/*These are the keywords for the SETTING DB*/
	public static String db_user            ="db_user";
	public static String db_password        ="db_password";
	public static String db_server          ="db_server";
	public static String db_port            ="db_port";
	public static String db_driver          ="db_driver";
	public static String db_url             ="db_url";

	public static String SQLSERVER             ="SQLSERVER";
	public static String sqlserver_db             ="sqlserver_db";
	public static String sqlserver_user           ="sqlserver_user";
	public static String sqlserver_password       ="sqlserver_password";
	public static String sqlserver_port           ="sqlserver_port";
	public static String sqlserver_server         ="sqlserver_server";
	public static String sqlserver                ="sqlserver";
	public static String Sqlserver                ="Sqlserver";
	public static String SqlServerVariableName    ="SqlserverVariableName";

	public static String bigsql_user           ="bigsql_user";
	public static String bigsql_password       ="bigsql_password";
	public static String bigsql_port           ="bigsql_port";
	public static String bigsql_server         ="bigsql_server";
	public static String bigsql             ="bigsql";

	public static String msodbc_db             ="msodbc_db";
	public static String msodbc_user           ="msodbc_user";
	public static String msodbc_password       ="msodbc_password";
	public static String msodbc                ="msodbc";
	public static String Msodbc                ="Msodbc";
	public static String MSODBC                ="MSODBC";
	public static String MsodbcVariableName    ="MsodbcVariableName";

	public static String HSQLDBREMOTE          ="HSQLDBREMOTE";
	public static String HSQLDBLOCAL           ="HSQLDBLOCAL";
	public static String dbfile                ="dbfile";
	public static String HsqldbLocalVariableName="HsqldbLocalVariableName";
	public static String HsqldbRemoteVariableName="HsqldbRemoteVariableName";
	public static String Hsqldblocal           ="Hsqldblocal";
	public static String Hsqldbremote          ="Hsqldbremote";
	public static String hsqldblocal           ="hsqldblocal";
	public static String hsqldbremote          ="hsqldbremote";

	public static String hsqldblocal_dbfile    ="hsqldblocal_dbfile";
	public static String hsqldblocal_password  ="hsqldblocal_password";
	public static String hsqldblocal_user      ="hsqldblocal_user";

	public static String hsqldbremote_db       ="hsqldbremote_db";
	public static String hsqldbremote_server   ="hsqldbremote_server";
	public static String hsqldbremote_user     ="hsqldbremote_user";
	public static String hsqldbremote_port     ="hsqldbremote_port";
	public static String hsqldbremote_password ="hsqldbremote_password";

	/*These keywords are used in Winidams2dict procedure*/
	public static String winidamsdic        ="winidamsdic";
	public static String winidamsdat        ="winidamsdat";
	public static String fixedtxt           ="fixedtxt";

	/*These keywords are used in Dictionary*/
	public static String descriptiondictionary    ="description ";
	public static String keyworddictionary        ="keyword ";
	public static String labeldictionary          ="label ";
	public static String writefmtdictionary       ="writefmt ";
	public static String addcodelabeldictionary   ="codelabel ";
	public static String delete                   ="delete";
	public static String addmd                    ="md ";
	public static String rename                   ="rename ";
	public static String setmdzero                   ="setmdzero";
	public static String changedatapath="changedatapath";

	/*These keywords are used in Dataset*/
	public static String newvar   ="newvar ";
	public static String retain   ="retain ";
	public static String keep     ="keep ";
	public static String drop     ="drop ";
	public static String OUTPUT   ="OUTPUT";
	public static String OUTPUTREP="OUTPUT()";
	public static String THENDO   ="THENDO";
	public static String ENDDO    ="ENDDO";
	public static String OPENB    ="OPENB";
	public static String CLOSEB    ="CLOSEB";
	public static String ENDSCRIPT ="ENDSCRIPT";
	public static String exebefore="exebefore";
	public static String endexebefore="endexebefore";
	public static String _NULL_     ="_NULL_";
	public static String SeMiCoLoN  ="SeMiCoLoN";
	public static String freqcounter="freqcounter";
	public static String evalstat   ="evalstat";

	public static String newmethod="newobject";
	public static String endnewmethod="endnewobject";

	/*These keywords are used in Report*/
	public static String layout       ="layout";
	public static String dslayout     ="dslayout";
	public static String htmldslayout     ="htmldslayout";
	public static String htmllayout     ="htmllayout";
	public static String xlslayout     ="xlslayout";
	public static String xlsdslayout     ="xlsdslayout";
	public static String outreport    ="outreport";
	public static String htmltitle        ="htmltitle";
	public static String pdftitle        ="pdftitle";
	public static String paper        ="paper";
	public static String titlealign        ="titlealign";
	public static String writepage        ="writepage";
	public static String background       ="background";
	public static String borderwidth       ="borderwidth";
	public static String titlefonts        ="titlefonts";
	public static String titlesize        ="titlesize";
	public static String pagefonts        ="pagefonts";
	public static String pagesize        ="pagesize";
	public static String pdflayout       ="pdflayout";
	public static String pdfdslayout       ="pdfdslayout";
	public static String labelalign       ="labelalign";
	public static String varalign       ="varalign";

	public static String dateformat       ="dateformat";
	public static String useeuclidean     ="useeuclidean";

	public static String norandomimpute     ="norandomimpute";

	public static String nocaption="nocaption";
	public static String captionalign="captionalign";
	public static String captionfont="captionfont";
	public static String captionsize="captionsize";
	public static String captioncolor="captioncolor";
	public static String captionstyle="captionstyle";
	public static String cellspacing="cellspacing";
	public static String cellpadding="cellpadding";
	public static String width="width";
	public static String height="height";
	public static String labelcolor="labelcolor";
	public static String labelhalign="labelhalign";
	public static String labelvalign="labelvalign";
	public static String labelfont="labelfont";
	public static String labelsize="labelsize";
	public static String labelbgcolor="labelbgcolor";
	public static String varcolor="varcolor";
	public static String varhalign="varhalign";
	public static String varvalign="varvalign";
	public static String varfont="varfont";
	public static String varsize="varsize";
	public static String varbgcolor="varbgcolor";
	public static String div="div";
	public static String varsymbol="varsymbol";
	public static String varlabelinfo="varlabelinfo";
	public static String firstrowanchor="div";
	public static String firstcolanchor="varsymbol";
	public static String cellanchor="varlabelinfo";

	public static String padding="padding";
	public static String colorpalettefirst="colorpalettefirst";
	public static String colorpalettelast="colorpalettelast";
	public static String colorpalettestep="colorpalettestep";
	public static String fontscalar="fontscalar";
	public static String collisionnode="collisionnode";
	public static String fontoptions="fontoptions";

	/*These keywords are for Univariate*/
	public static String statistic   ="statistic";
	public static String alpha       ="alpha";
	public static String outstyle    ="outstyle";
	public static String CLM         ="CLM";
	public static String CSS         ="CSS";
	public static String CV          ="CV";
	public static String MAX         ="MAX";
	public static String MEAN        ="MEAN";
	public static String mean        ="mean";
	public static String MIN         ="MIN";
	public static String N           ="N";
	public static String NMISS       ="NMISS";
	public static String RANGE       ="RANGE";
	public static String STD         ="STD";
	public static String SUM         ="SUM";
	public static String sum         ="sum";
	public static String SUMW        ="SUMW";
	public static String TTEST1       ="TTEST1";
	public static String PTTEST1      ="PTTEST1";
	public static String TTEST2       ="TTEST2";
	public static String PTTEST2      ="PTTEST2";
	public static String VARTEST      ="VARTEST";
	public static String PVARTEST      ="PVARTEST";
	public static String USS         ="USS";
	public static String VARIANCE    ="VARIANCE";
	public static String GEOMETRICMEAN="GEOMETRICMEAN";
	public static String LASTRECORD  ="LASTRECORD";
	public static String FIRSTRECORD ="FIRSTRECORD";
	public static String varrow      ="varrow";
	public static String statrow     ="statrow";
	public static String aggreg      ="aggreg";
	public static String Univariate  ="Univariate";
	public static String samplevariance="samplevariance";
	public static String correlation   ="correlation";
	public static String replications  ="replications";
	public static String errortype     ="errortype";
	public static String absvalue      ="absvalue";
	public static String quadratic     ="quadratic";
	public static String logaritmic    ="logaritmic";

	public static String normtype    ="normtype";
	public static String maxabscolsum="maxabscolsum";
	public static String euclidean   ="euclidean";
	public static String frobenius   ="frobenius";
	public static String infinity    ="infinity";
	public static String power       ="power";
	public static String solution    ="solution";
	public static String normal      ="normal";
	public static String transposed  ="transposed";
	public static String delonend    ="delonend";
	public static String charstosubwspace="charstosubwspace";

	public static String Sortmerge  ="Sortmerge";
	public static String nodupkey   ="nodupkey";
	public static String Matmult    ="Matmult";
	public static String Matcond    ="Matcond";
	public static String Matdet     ="Matdet";
	public static String Matnorm    ="Matnorm";
	public static String Matinv     ="Matinv";
	public static String Matrank    ="Matrank";
	public static String Mattrace   ="Mattrace";
	public static String Matpow     ="Matpow";
	public static String Mattranspose="Mattrasnpose";
	public static String Matsolve    ="Matsolve";
	public static String Matproperties="Matproperties";
	public static String properties="properties";
	public static String usecov    ="usecov";
	public static String withmd    ="withmd";
	public static String ncomp     ="ncomp";
	public static String useperc   ="useperc";
	public static String correctanswers   ="correctanswers";

	public static String pairwise="pairwise";
	public static String outfunction="outfunction";
	public static String hidfunction="hidfunction";
	public static String linear="linear";
	public static String tanh="tanh";
	public static String logistic="logistic";
	public static String marquardt="marquardt";
	public static String minunit="minunit";
	public static String nounsure="nounsure";

	public static String cond="cond";
	public static String det="det";
	public static String norm="norm";
	public static String twonorm="twonorm";
	public static String normf="normf";
	public static String norminfinity="norminfinity";
	public static String rank="rank";
	public static String trace="trace";
	public static String isrectangular="isrectangular";
	public static String issquare="issquare";
	public static String density="density";
	public static String isdiagonal="isdiagonal";
	public static String isdiagonallydominantbycolumn="isdiagonallydominantbycolumn";
	public static String isdiagonallydominantbyrow="isdiagonallydominantbyrow";
	public static String isidentity="isidentity";
	public static String islowerbidiagonal="islowerbidiagonal";
	public static String islowertriangular="islowertriangular";
	public static String isnonnegative="isnonnegative";
	public static String isorthogonal="isorthogonal";
	public static String ispositive="ispositive";
	public static String issingular="issingular";
	public static String isskewsymmetric="isskewsymmetric";
	public static String isstrictlylowertriangular="isstrictlylowertriangular";
	public static String isstrictlytriangular="isstrictlytriangular";
	public static String isstrictlyuppertriangular="isstrictlyuppertriangular";
	public static String issymmetric="issymmetric";
	public static String istriangular="istriangular";
	public static String istridiagonal="istridiagonal";
	public static String isunittriangular="isunittriangular";
	public static String isupperbidiagonal="isupperbidiagonal";
	public static String isuppertriangular="isuppertriangular";
	public static String iszero="iszero";
	public static String lowerbandwidth="lowerbandwidth";
	public static String semibandwidth="semibandwidth";
	public static String upperbandwidth="upperbandwidth";

	public static String epoch="epoch";
	public static String hidden="hidden";
	//public static String learnrate="learnrate";
	public static String testtimes="testtimes";
	public static String theta="theta";
	public static String ihweightint="ihweightinterval";
	public static String howeightint="howeightinterval";
	public static String elast="elast";
	public static String depvar="depvar";
	public static String successvalue="successvalue";
	public static String vertical="vertical";
	public static String binmax="binmax";
	public static String binmin="binmin";
	public static String distdegree="distdegree";
	public static String distribution="distribution";
	public static String bins="bins";
	public static String chisquare="chisquare";
	public static String phisquare="phisquare";
	public static String cramerv  ="cramerv";
	public static String pchisquare  ="pchisquare";
	public static String student="student";
	public static String poisson="poisson";
	public static String linktype="linktype";
	public static String todisk="todisk";
	public static String varid="varid";
	public static String Distance="Distance";
	public static String LSingle="single";
	public static String LComplete="complete";
	public static String LAverage="average";
	public static String LCentroid="centroid";
	public static String EuclideanDistance="Euclidean";
	public static String ManhattanDistance="Manhattan";
	public static String ChebyshevDistance="Chebyshev";
	public static String SquaredEuclideanDistance="SquaredEuclidean";
	public static String MahalanobisDistance="MahalanobisDistance";
	public static String emptyclusterstrategy="emptyclusterstrategy";
	public static String CanberraDistance="Canberra";
	public static String EarthMoversDistance="EarthMovers";

	public static String generateerror="generateerror";
	public static String farthestpoint="farthestpoint";
	public static String largestpointsnumber="largestpointsnumber";
	public static String largestvariance="largestvariance";

	public static String noconversion="noconversion";
	public static String ngroup="ngroup";
	public static String iterations="iterations";
	public static String noint="noint";
	public static String numsubsets="numsubsets";
	public static String initweights="initweights";
	public static String cases="cases";
	public static String fuzzycoeff="fuzzycoeff";
	public static String accuracy="accuracy";
	public static String varname="varname";
	public static String varval="varval";
	public static String varfreq="varfreq";
	public static String variniweight="variniweight";
	public static String weightname="weightname";
	public static String weightmax="weightmax";
	public static String weightmin="weightmin";
	public static String bootestimate="bootestimate";
	public static String standarderror="standarderror";
	public static String typeofstat="typeofstat";
	public static String bias="bias";
	public static String confidenceinterval="confidenceinterval";

	public static String logfile="logfile";
	public static String structure="structure";
	public static String query="query";
	public static String chartodelete="chartodelete";
	public static String charstartquery="charstartquery";
	public static String noupdate="noupdate";
	public static String replicate="replicate";

	public static String function="function";
	public static String startvalue="startvalue";
	public static String stepvalue="stepvalue";
	public static String tolerance="tolerance";

	public static String simplexreflectioncoeff="simplexreflectioncoeff";
	public static String simplexextensioncoeff="simplexextensioncoeff";
	public static String simplexcontractioncoeff="simplexcontractioncoeff";

	public static String roc="roc";
	public static String roe="roe";

	public static String varsup="varsup";
	public static String novar="novar";
	public static String noobs="noobs";
	public static String scatter="scatter";
	public static String freqtype="freqtype";
	public static String simplecounts="simplecounts";
	public static String rowfreq="rowfreq";
	public static String rowpercentfreq="rowpercentfreq";
	public static String colfreq="colfreq";
	public static String colpercentfreq="colpercentfreq";
	public static String relfreq="relfreq";
	public static String relpercentfreq="relpercentfreq";
	public static String cumulative="cumulative";
	public static String mdhandling="mdhandling";
	public static String casewise="casewise";
	public static String pairwisenomd="pairwisenomd";
	public static String pairwisewithmd="pairwisewithmd";
	public static String mdsubst="mdsubst";

	/* These are the parameters for the graphical procedures*/
	public static String varx="varx";
	public static String varm="varm";
	public static String varr="varr";
	public static String varl="varl";
	public static String varrmf="varrmf";
	public static String varlmf="varlmf";
	public static String varobs="varobs";
	public static String vartime="vartime";
	public static String varmin="varmin";
	public static String varmax="varmax";

	public static String maxdonors="maxdonors";
	public static String mustmatch="mustmatch";

	public static String useborndie="useborndie";
	public static String uselowmem="uselowmem";

	public static String vary="vary";
	public static String varvalue="varvalue";
	public static String varpred="varpred";
	public static String varbar="varbar";
	public static String varstack="varstack";
	public static String varref="varref";
	public static String varnum="varnum";
	public static String varlabel="varlabel";
	public static String labelx="labelx";
	public static String labely="labely";
	public static String title="title";
	public static String outimg="outimg";
	public static String imgwidth="imgwidth";
	public static String imgheight="imgheight";
	public static String maxX="xmax";
	public static String maxY="ymax";
	public static String minY="ymin";
	public static String minX="xmin";
	public static String groupsep=": ";
	public static String imgtype="imgtype";
	public static String percent="percent";
	public static String varlabelcolor="varlabelcolor";

	/*These are the parameters for the ngram procedure*/
	public static String casesensitive="casesensitive";
	public static String withchars="withchars";
	public static String nvalue="nvalue";
	public static String directory="directory";
	public static String filterfile="filter";
	public static String nosubdir="nosubdir";
	public static String hostname="hostname";

	/*These are the parameters for the wordCount procedure*/
	public static final String minvalue = "minvalue";
	public static final String maxvalue = "maxvalue";

	public static final String uweight = "uweight";
	public static final String lweight = "lweight";

	/*These are the parameters for the Vartransform procedure*/
	public static final String keepallvars = "keepallvars";
	public static final String replacevars = "replacevars";
	public static final String transform   = "transform";
	public static final String notransform   = "notransform";
	public static final String devfrommean        = "devfrommean";
	public static final String absdevfrommean     = "absdevfrommean";
	public static final String squaredevfrommean     = "squaredevfrommean";
	public static final String standardize     = "standardize";
	public static final String divformax     = "divformax";
	public static final String normalize01     = "normalize01";
	public static final String meannormalize     = "meannormalize";
	public static final String sumnormalize     = "sumnormalize";
	public static final String sumpctnormalize     = "sumpctnormalize";
	public static final String devfrommeandivradqn     = "devfrommeandivradqn";

	public static final String itemweight     = "itemweight";

	public static final String highlow     = "highlow";
	public static final String highmediumlow     = "highmediumlow";
	public static final String highermediumlower     = "highermediumlower";

	/*These parameters are for the Freq procedure*/
	public static final String varcol="varcol";
	public static final String totalonrows="rowwithcoltotals";
	public static final String totaloncols="colwithrowtotals";

	public static final String parameter="parameter";
	public static final String constrain="constrain";
	public static final String assignonlytomax="assignonlytomax";

	public static final String forceequals="forceequals";
	public static final String useprob="useprob";

	/*These parameters are for the Email procedure*/
	public static final String mailserver="mailserver";
	public static final String to="to";
	public static final String from="from";
	public static final String address="address";
	public static final String username="username";
	public static final String mailserver_address="mailserver_address";
	public static final String mailserver_username="mailserver_username";
	public static final String mailserver_password="mailserver_password";
	public static final String mailserver_port="mailserver_port";
	public static final String subject="subject";
	public static final String attach="attach";
	public static final String mailtext="mailtext";
	public static final String useauth="useauth";
	public static final String starttls="starttls";
	public static final String ssl_enable="ssl_enable";
	public static final String set_transport="set_transport";

	public static final String ftp="ftp";
	public static final String ftpuser="ftpuser";
	public static final String ftppassword="ftppassword";
	public static final String subdir="subdir";
	public static final String url="url";
	public static final String localdir="localdir";
	public static final String treefile="treefile";
	public static final String separate="separate";
	public static final String nogaininfo="nogaininfo";
	public static final String tablewithfreq="tablewithfreq";

	public static final String casual="casual";

	public static final String filewedits="filewedits";
	public static final String simplifyedits="simplifyedits";

	/*These parameters are for the Mysql2dict procedure*/
	public static String mysql_db="mysql_db";
	public static String mysql_server="mysql_server";
	public static String mysql_password="mysql_password";
	public static String mysql_port="mysql_port";
	public static String mysql_user="mysql_user";

	public static String oracle_server="oracle_server";
	public static String oracle_table="oracle_table";
	public static String oracle_port="oracle_port";
	public static String oracle_user="oracle_user";
	public static String oracle_password="oracle_password";
	public static String oracle_service="oracle_service";
	public static String oracle_sid="oracle_sid";
	public static String sid="sid";

	public static String postgresql_db="postgresql_db";
	public static String postgresql_server="postgresql_server";
	public static String postgresql_password="postgresql_password";
	public static String postgresql_port="postgresql_port";
	public static String postgresql_user="postgresql_user";
	public static String varincprob="varincprob";
	public static String popdim="popdim";
	public static String setprogvarnames="setprogvarnames";

	public static String collapse="collapse";
	public static String relgain="relgain";

	public static String addsolutions="addsolutions";
	public static String noforcemd="noforcemd";

	public static String maxdistance="maxdistance";
	public static String thicknumber="thicknumber";
	public static String thicksize="thicksize";
	public static String unitfontsize="unitfontsize";
	public static String thickfontsize="thickfontsize";
	public static String barwidth="barwidth";
	public static String useoriginalnames="useoriginalnames";

	public static String fontsize="fontsize";
	public static String fontname="fontname";

	public static String onlydict="onlydict";
	public static String onlytable="onlytable";

	public static String noverbose="noverbose";
	public static String varcond="varcond";

	public static String uselocalefornumbers="uselocalefornumbers";
	public static String numdecimals="numdecimals";
	public static String nouselocalefornumbers="nouselocalefornumbers";

	public static String csspath="csspath";
	public static String maxsecs="maxsecs";

	public static String htmlfile="htmlfile";
	public static String nocodelabel="nocodelabel";

	public static String headerfile="headerfile";
	public static String footerfile="footerfile";

	public static String orderbyval="orderbyval";

	public static String words="words";
	public static String ignorecase="ignorecase";
	public static String exact="exact";
	public static String divby="divby";

	public static String iterative="iterative";
	public static String chisquareandp="chisquareandp";

	public static String lenwlenv="lenwlenv";
	public static String lenwords="lenwords";
	public static String lenvars="lenvars";
	public static String lenvlenw="lenvlenw";
	public static String nomd="nomd";
	public static String numlinestoread="numlinestoread";
	public static String onegraph="onegraph";
	public static String sortable="sortable";
	public static String nocheckhtml="nocheckhtml";

	public static String consideralsoall="consideralsoall";
	public static String maxvar="maxvar";

	public static String considerall="considerall";

	public static String remotedict="remotedict";

	public static String idvar="idvar";
	public static String blockvar="blockvar";
	public static String link="link";
	public static String vardistances="vardistances";
	public static String limits="limits";
	public static String epsilon="epsilon";

	public static String setzero="setzero";

	public static String pointforcorrect="scoreforcorrected";
	public static String pointforerrated="scoreforerrated";
	public static String pointformissing="scoreformissing";

	public static String lowlimit="lowlimit";
	public static String uplimit="uplimit";
	public static String divideby3="divideby3";

	public static String vartodelaccents="vartodelaccents";
	public static String vartotrim="vartotrim";
	public static String vartoreplace="vartoreplace";
	public static String vartoreplacenonascii="vartoreplacenonascii";
	public static String vartoupcase="vartoupcase";
	public static String vartolowcase="vartolowcase";
	public static String lowcase="lowcase";
	public static String vartotrimfromright="vartotrimfromright";
	public static String vartotrimfromleft="vartotrimfromleft";
	public static String codecharstoreplace="codecharstoreplace";
	public static String varscore="varscore";
	public static String threshold="threshold";
	public static String applycoeffonscores="applycoeffonscores";
	public static String outfirst="outfirst";
	public static String checksol="checksol";
	public static String nomaxdetsol="nomaxdetsol";
	public static String maxtime="maxtime";
	public static String itemweights="itemweights";

	public static String dirsciafiles="dirsciafiles";

	public static String mindonors="mindonors";
	public static String useonlyintdetsol="useonlyintdetsol";
	public static String maxcardfordetsol="maxcardfordetsol";

	public static String reducedforsecondary="reducedforsecondary";

	public static String outfile="outfile";

	/*these are required by the assistant*/
	public static String NODE="NODE";
	public static String NOTE="NOTE";
	public static String TEXT="TEXT";
	public static String LINK="LINK";
	public static String JUMPTO="JUMPTO";
	public static String considerpartofb="considerpartofb";

	public static String ActualRelease="NA";

	/*Here is the object that rapresent the project status*/
	public static Project project;

	/*Here is the object that rapresent the language reader*/
	public static LanguageReader Language;

	public static boolean modifiedproject=false;
	public static boolean modifiedscript=false;

	public static MainGUI MainGUI;

	public static LinkedList<String> openeddataviewer=new LinkedList<String>();

	public static Hashtable<String, String> configvalues;
	public static Vector<String> configvaluesforserver;
	public static Hashtable<String,Vector<MemoryValue[]>> MemoriesDatasets=new Hashtable<String,Vector<MemoryValue[]>>();

	/*This contains the number used to compile the code for data steps, for fitting function, ecc.*/
	public static int numfitting=0;
	public static int numdataset=0;
	public static int numreadview=0;

	public static String varsonestepsol="varsonestepsol";
	public static String usememory="usememory";
	public static String varaux="varaux";

	public static boolean laststepstate=false;
	public static String laststepmessage="";

	public static boolean operationWriting=false;
	public static boolean operationReading=false;

	public static String currentExecutedStep="";

	public static String referencevalues="referencevalues";

	public static String white="white";
	public static String blue="blue";
	public static String purple="purple";
	public static String lime="lime";
	public static String silver="silver";
	public static String grey="grey";
	public static String green="green";
	public static String olive="olive";
	public static String maroon="maroon";
	public static String aqua="aqua";
	public static String red="red";

	public static String mainsite="mainsite";
	public static String maxdepth="maxdepth";
	public static String filterurls="filterurls";

	public static String colorvars="colorvars";
	public static String colorvalues="colorvalues";
	public static String colordecisions="colordecisions";

	public static String infile="infile";
	public static String maxlength="maxlength";
	public static String minlength="minlength";

	public static String charstoreplace="charstoreplace";
	public static String charstodelete="charstodelete";
	public static String onlyascii="onlyascii";
	public static String nonumbers="nonumbers";
	public static String joinwords="joinwords";
	public static String usepercfreq="usepercfreq";
	public static String trettaggerexe="treetaggerexe";
	public static String parameterfile="parameterfile";
	public static String secparameterfile="secparameterfile";
	public static String addprob="addprob";
	public static String varclasswitheffectcode="varclasswitheffectcode";
	public static String dichotomous="dichotomous";
	public static String mainvalue="mainvalue";
	public static String limitvalue="limitvalue";
	public static String indir="indir";
	public static String filterfiles="filterfiles";
	public static String minwords="minwords";
	public static String varpathfiles="varpathfiles";
	public static String varaddinfodoc="varaddinfodoc";
	public static String vargroupby="vargroupby";
	public static String varsw="varsw";
	public static String vargow="vargow";
	public static String shortmsgs="shortmsgs";

	public static String minfreq="minfreq";

	public static double numread=0;
	public static double numwrite=0;

	public static String useragent="useragent";
	public static String cookies="cookies";
	public static String followredirects="followredirects";
	public static String ignorecontenttype="ignorecontenttype";
	public static String ignorehttperrors="ignorehttperrors";
	public static String method="method";
	public static String timeout="timeout";

	public static String varfreqterm="varfreqterm";

	public static String varmainsite="varmainsite";
	public static String varmaxdepth="varmaxdepth";
	public static String varfilterurls="varfilterurls";
	public static String varuseragent="varuseragent";
	public static String varcookies="varcookies";
	public static String varfollowredirects="varfollowredirects";
	public static String varignorecontenttype="varignorecontenttype";
	public static String varignorehttperrors="varignorehttperrors";
	public static String varmethod="varmethod";
	public static String vartimeout="vartimeout";
	public static String vardescriptor="vardescriptor";

	public static String varwords="varwords";
	public static String varfreqs="varfreqs";

	public static String similaritymetric="similaritymetric";
	public static String seedsimilarity="seedsimilarity";
	public static String varwordstosearch="varwordstosearch";
	public static String varwithannotations="varwithannotations";
	public static String virtualdoc="virtualdoc";

	public static String chapmanlengthdeviation="chapmanlengthdeviation";
	public static String cosinesimilarity="cosinesimilarity";
	public static String matchingcoefficient="matchingcoefficient";
	public static String overlapcoefficient="overlapcoefficient";
	public static String dicesimilarity="dicesimilarity";
	public static String jarowinkler="jarowinkler";
	public static String jaccardsimilarity="jaccardsimilarity";
	public static String qgramsdistance="qgramsdistance";
	public static String levenshtein="levenshtein";
	public static String blockdistance="blockdistance";
	public static String mongeelkan="mongeelkan";
	public static String chapmanorderednamecompoundsimilarity="chapmanorderednamecompoundsimilarity";
	public static String jaro="jaro";
	public static String soundex="soundex";
	public static String needlemanwunch="needlemanwunch";
	public static String equals="equals";

	public static String top_x_pos="top_x_pos";
	public static String top_y_pos="top_y_pos";
	public static String bottom_x_pos="bottom_x_pos";
	public static String bottom_y_pos="bottom_y_pos";
	public static String right_x_pos="right_x_pos";
	public static String right_y_pos="right_y_pos";
	public static String left_x_pos="left_x_pos";
	public static String left_y_pos="left_y_pos";

	public static String minunits="minunits";
	public static String onlyproj="onlyproj";
	public static String docsprefix="docsprefix";
	public static String explainedinertia="explainedinertia";
	public static String varrefdocs="varrefdocs";
	public static String varnewdocs="varnewdocs";
	public static String varwordsrefdocs="varwordsrefdocs";
	public static String varwordsnewdocs="varwordsnewdocs";

	public static String noaddfileinfo="noaddfileinfo";
	public static String onefilefordoc="onefilefordoc";

	public static String minfreqhashtags="minfreqhashtags";
	public static String varhashtags="varhashtags";

	public static String avoidunknown="avoidunknown";

	public static String avoidextension="avoidextension";
	public static String tagnames="tagnames";

	public static String vartweets="vartweets";
	public static String minfrequsers="minfrequsers";
	public static String noretwett="noretwett";

	public static String file_encoding="file_encoding";
	public static String dirout="dirout";
	public static String vartext="vartext";
	public static String varfilename="varfilename";
	public static String oldvardom="oldvardom";

	public static String searchengine="searchengine";
	public static String bing="bing";
	public static String google="google";
	public static String istella="istella";

	public static String projectname="projectname";
	public static String renamevar="renamevar";
	public static String vartoretain="vartoretain";
	public static String varpresence="varpresence";
	public static String vartorecode="vartorecode";
	public static String varmissing="varmissing";
	public static String addidentifier="addidentifier";

	public static String sciadatafile="sciadatafile";
	public static String sciadatainfo="sciadatainfo";

	public static String identifynewsentences="identifynewsentences";
	public static String replacenewlines="replacenewlines";
	public static String writealsoifempty="writealsoifempty";
	public static String writeifpresentfor="writeifpresentfor";
	public static String nowritesummary="nowritesummary";

	public static String varmainpage="varmainpage";
	public static String nocheckvalues="nocheckvalues";
	public static String limit_reference="limit_reference";

	public static String vartodeletewords="vartodeletewords";
	public static String wordstodelete="wordstodelete";

	public static String allvarstext="allvarstext";
	public static String maxterms="maxterms";

	public static String separate_contents="separate_contents";

	public static int percentage_total=0;
	public static int percentage_done=0;

	public static int general_percentage_total=0;
	public static int general_percentage_done=0;

	public static int user_percentage_total=0;
	public static int user_percentage_done=0;

	public static String add_two_terms="add_two_terms";
	public static String add_three_terms="add_three_terms";

	public static String sentence_detector_file="sentence_detector_file";

	public static String consider_sentence="consider_sentence";
	public static String sentence_identifiers="sentence_identifiers";

	public static String javac_path="";

	public static String path_javac="path_javac";

	public static boolean procedure_error=false;
	public static boolean steppercentageviewer=true;

	public static String varcondition_id="varcondition_id";
	public static String varcondition_nodename="varcondition_nodename";
	public static String varcondition_owntext="varcondition_owntext";
	public static String varcondition_tagname="varcondition_tagname";
	public static String varcondition_text="varcondition_text";
	public static String varcondition_value="varcondition_value";
	public static String varcondition_attrkey="varcondition_attrkey";
	public static String varcondition_attrvalue="varcondition_attrvalue";
	public static String varlevel_ref="varlevel_ref";
	public static String varselect="varselect";

	public static String script_extension="script_extension";

	public static String term_multiplier="term_multiplier";
	public static String numtrials="numtrials";

	public static Hashtable<String, Vector<MemoryValue[]>> CommonDSTable=new Hashtable<String, Vector<MemoryValue[]>>();
	public static Hashtable<String, Object> SHAREDOBJECTS=new Hashtable<String, Object>();

	public static Semaphore semwritelog=new Semaphore(1);
	public static Semaphore semwriteOut=new Semaphore(1);
	public static Vector<String> opened_datasets=new Vector<String>();
	public static boolean stop_script=false;
}
