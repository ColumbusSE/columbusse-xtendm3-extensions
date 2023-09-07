/** Name: CREXTPRT.OutputProcessExtension.groovy
 *
 * This extension is used to create additional output data
 *
 * in the program actual considered output files are
 *          - OIS641PF
 *
 * Date         Changed By                         Description
 * 08.06.2023   Frank Zahlten (Columbus)           creation
 * 23.08.2023   Jessica Bjorklund (Columbus)       change logic for SPUN = HN
 * 05.09.2023   Jessica Bjorklund (Columbus)       separate subtotals for CAD and USD
 *
 */
import java.util.Map;
import java.text.DecimalFormat;
import M3.DBContainer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

public class OutputProcessExtension extends ExtendM3Trigger {

	private final MethodAPI method;
	private final LoggerAPI logger;
	private final ProgramAPI program;
	private final DatabaseAPI database;

	private String printerFile = "";
	private String jobNumber = "";
	private String structure = "";
	private String strUde2 = "";
	private int variant = 0;
	private int rpbk  = 0;
	private int intCons_2 = 2;
	private int intCons_0 = 0;
	private HashMap<String, Object> fieldMap;

	private String strOrno = "";
	private String strDlix = "";
	private long lDlix = 0l;
	private long longZero = 0l;

	private int maxCountArr = 500;
	private int intIndexMax = 0;
	private int regdate= 0;
	private int regtime= 0;

	private String[] arrDlix = new String [maxCountArr];
	private String[] arrUde2 = new String [maxCountArr];

	private int intRorc = 0;
	private String strRidn = "";
	private int intRidl = 0;
	private int intRidx = 0;
	private String strFaci = "";
	private String[] arrRidn = new String [maxCountArr];
	private String[] arrFaci = new String [maxCountArr];
	private int[] arrRidl = new int [maxCountArr];
	private int[] arrRidx = new int [maxCountArr];

	private String strCsno = "";
	private String strCucd = "";      
	private double doubleLnam = 0d;
	private double doubleLnamCucd = 0d;     
	private String[] arrCsno = new String [maxCountArr];
	private double[] arrLnam = new double [maxCountArr];
	private String[] arrCucd = new String [maxCountArr];     
	private double[] arrLnau = new double [maxCountArr];     
	private double[] arrLnad = new double [maxCountArr];     

	private java.util.List<DBContainer> records = [];
	private int intCono = program.LDAZD.get("CONO");

	public OutputProcessExtension(MethodAPI method, LoggerAPI logger, ProgramAPI program, DatabaseAPI database) {
		this.logger = logger;
		this.method = method;
		this.program = program;
		this.database = database;
	}


	public void main() {

		String formatDate = "";
		String formatTime = "";
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		formatDate = now.format(format1);
		DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");
		formatTime = now.format(format2);
		regdate=Integer.parseInt(formatDate);
		regtime=Integer.parseInt(formatTime);

		logger.debug ("OutputProcessExtension");

		printerFile = method.getArgument(0) as String;
		logger.debug ("CREXTPRT - printerfile is " + printerFile);

		if (printerFile.equals("OIS641PF")) {
			workOnOIS641PF();
		}
	}

	/**
	 * start working for print out data of OIS641PF
	 */
	void workOnOIS641PF() {

		//check if the correct structure of the printer block in the printer file is used
		jobNumber = method.getArgument(1) as String;
		structure = method.getArgument(2) as String;
		variant = method.getArgument(3) as int;
		rpbk  = method.getArgument(4) as int;

		logger.debug ("workOnOIS641PF - structure: " + structure);

		if (!structure.equals("M3_STD_01-01")			//Standard name of the structure
		&&  !structure.equals("NV_CUS_01-01")			//customer specific name of the structure
		) {
			return;
		}

		logger.debug ("workOnOIS641PF - rbpk: " + rpbk);
		if (rpbk == 6) {
			//program gets active connected to section 6
			workOnOIS641PF_6();
		}

		if (rpbk == 40) {
			//program gets active connected to section 40
			workOnOIS641PF_40();
		}
	}

	/**
	 * start working for print out data of OIS641PF section 6
	 */
	void workOnOIS641PF_6() {

		logger.debug ("workOnOIS641PF - start workOnOIS641PF_6()");
		// check if the print block is including data
		fieldMap = method.getArgument(5) as HashMap;
		if (fieldMap == null) {
			logger.debug ("got no data from fieldMap");
			return;
		}

		logger.debug ("workOnOIS641PF fieldmap content" + fieldMap);

		//initialize later used arrays
		for (int i = 0; i < maxCountArr; i++) {
			arrRidn[i] = " ";
			arrFaci[i] = " ";
			arrRidl[i] = 0;
			arrRidx[i] = 0;
			arrDlix[i] = " ";
			arrUde2[i] = " ";
		}
		
		//  creates EXT641 data based on given DLIX or ORNO
		if (!workOnGivenDLIX()) {
			if (!workOnGivenORNO()) {
				return;
			}
		}
	
		//  sum up EXT641 data per UDE2 and CSNO and store EXT642 data
		intIndexMax = -1;
		createEXT642();
		
	}
	
	/**
	 * workOnGivenDLIX
	 *
	 * use given DLIX and create EXT641 data
	 */
	public boolean workOnGivenDLIX() {
		boolean returnValue = false;
		// >>>> if DLIX is passed start working on this single DLIX
		strDlix = fieldMap.get("OQDLIX");
		if (strDlix == null) {
			strDlix = "0";
		}

		lDlix = Long.parseLong(strDlix);
		if (lDlix > longZero) {
			logger.debug ("workOnOIS641PF DLIX: " + strDlix);
			workOnOIS641_6_DLIX(lDlix);                //>>>> start working based on given DLIX
			returnValue = true;;
		}
		return returnValue;
	}

	/**
	 * workOnGivenORNO
	 *
	 * phase 1 - data selection
	 * use given ORNO to get DLIX from ODLINE and collect in array arrDlix
	 * use arrDlix to get and collect UDE2 from DRADTR in array arrUde2
	 * use UDE2 from arrUde2 to get DLIX from DRADTR and collect in arrDlix
	 *
	 * phase 2 - data creation
	 * use arrDlix and create EXT641 data for every single DLIX
	 *
	 */
	public boolean workOnGivenORNO() {
		boolean returnValue = false;
		// >>>> get DLIX via ORNO and start working for all detected DLIX data
		strOrno = fieldMap.get("OAORNO");
		if (strOrno.trim().isEmpty()) {
			logger.debug ("workOnOIS641PF ORNO from fieldMap is empty");
			strOrno = "";
			return returnValue;
		}

		logger.debug ("workOnOIS641PF ORNO: " + strOrno);

		//select connected DLIX via ODLINE
		//                          ======
		DBAction query = database.table("ODLINE")
				.index("00")
				.selection("UBDLIX")
				.build();
		DBContainer container = query.getContainer();
		container.set("UBCONO", intCono);
		container.set("UBORNO", strOrno);
		query.readAll(container, 2, selectDLIXFromODLINE);

		//no DLIX was found
		if (arrDlix[0] == " " || arrDlix[0] == null) {
			logger.debug ("workOnOIS641PF  >>> no DLIX was found");
			return returnValue;
		}

		getDlixConnectedUde2();

		getUde2ConnectedDlix();

		//phase 2 data creation - start working per DLIX
		for (int i = 0; i < maxCountArr; i++ ) {
			if (arrDlix[i] != " " && arrDlix[i] != null) {
				strDlix = arrDlix[i];
				logger.debug ("workOnOIS641PF start detailed section 6 work for DLIX " + strDlix);
				lDlix = Long.parseLong(strDlix);
				workOnOIS641_6_DLIX(lDlix);           //>>>> start working based on determined DLIX
				returnValue = true;
			}
		}
		return returnValue;
	}
	
	
	/*
	 * check DLIX from table ODLINE if it is already saved in array addDlix
	 */
	Closure<?> selectDLIXFromODLINE = { DBContainer container ->
		records << container;

		lDlix = container.get("UBDLIX");
		strDlix = Long.toString(lDlix).trim();
		if (lDlix <= 0l) {
			logger.debug ("workOnOIS641PF selectDLIX DLIX is zero");
			return;
		}
		fillArrDlix(strDlix);
	}

	/**
	 * getDlixConnectedUde2 - select track id's connected to DLIX via DRADTR
	 */
	public void getDlixConnectedUde2() {
		logger.debug("workOnOIS641_6_getDlixConnectedUde2");
		for (int i2 = 0; i2 < maxCountArr; i2++ ) {
			if (arrDlix[i2] != " " && arrDlix[i2] != null) {
				strDlix = arrDlix[i2].trim();
				logger.debug ("workOnOIS641PF getDlixConnectedUde2 for " + strDlix);
				lDlix = Long.parseLong(strDlix);
				//only if a DLIX <> 0l is given following commands will get used
				if (lDlix != 0l) {
					DBAction action = database.table("DRADTR")
							.index("00")
							.selection("DRUDE2")
							.build();
					DBContainer DRADTR = action.getContainer();
					// Key value for read
					DRADTR.set("DRCONO", intCono);
					DRADTR.set("DRTLVL", intCons_2);
					DRADTR.set("DRCONN", intCons_0);
					DRADTR.set("DRDLIX", lDlix);
					if (action.read(DRADTR)) {
						strUde2 = DRADTR.get("DRUDE2");
						if (!strUde2.trim().isEmpty()) {
							fillArrUde2(strUde2.trim());
						}
					}
				}
			}
		}
	}

	/**
	 * getUde2ConnectedDlix - check for further DLIX in DRADTR based on TRACK ID
	 */
	public void getUde2ConnectedDlix() {
		logger.debug("workOnOIS641_6 getUde2ConnectedDlix() ");
		for (int i3 = 0; i3 < maxCountArr; i3++ ) {
			if (arrUde2[i3] != " " && arrUde2[i3] != null) {
				strUde2 = arrUde2[i3].trim();
				ExpressionFactory expression = database.getExpressionFactory("TABLE");
				expression = expression.eq("DRUDE2", strUde2);
				DBAction queryDRADTR = database.table("DRADTR")
						.index("00")
						.matching(expression)
						.selection("DRDLIX", "DRUDE2")
						.build();
				DBContainer DRADTR = queryDRADTR.getContainer();
				// Key value for read
				DRADTR.set("DRCONO", intCono);
				DRADTR.set("DRTLVL", intCons_2);
				DRADTR.set("DRCONN", intCons_0);
				queryDRADTR.readAll(DRADTR, 3, ude2ConnectedDlix);
			}
		}
	}

	/*
	 * pass DLIX from DRADTR to method fillArrDlix
	 */
	Closure<?> ude2ConnectedDlix = { DBContainer container ->
		records << container;
		logger.debug("workOnOIS641_6 getUde2ConnectedDlix() ude2ConnectedDlix ");
		lDlix = container.get("DRDLIX");
		strDlix = Long.toString(lDlix).trim();
		logger.debug("workOnOIS641_6 getUde2ConnectedDlix() ude2ConnectedDlix call FillArrDlix for DLIX" + strDlix);
		fillArrDlix(strDlix);
	}

	/**
	 * fillArrDlix - check if given DLIX is already in arrDlix
	 */
	public void fillArrDlix(String dlix) {
		//check arrDlix is already including DLIX
		logger.debug ("workOnOIS641PF fillArrDlix DLIX: " + dlix);
		for (int i4 = 0; i4 < maxCountArr; i4++ ) {
			logger.debug ("workOnOIS641PF fillArrDlix - check arrDlix i: "+ i4 + " DLIX: " + dlix + " arrDlix[i]: " + arrDlix[i4]);
			if (arrDlix[i4] == dlix)  {
				logger.debug ("workOnOIS641PF fillArrDlix - arrDlix[i] == dlix DLIX: " + dlix);
				break;
			}
			if (arrDlix[i4] == " " || arrDlix[i4] == null) {
				logger.debug ("workOnOIS641PF fillArrDlix - DLIX " + dlix + " is added to arrDlix  i4: " + i4);
				arrDlix[i4] = dlix;
				break;
			}
		}
	}

	/**
	 * fillArrUde2 - check if given UDE2 is already in arrUde2
	 */
	public void fillArrUde2(String ude2) {
		logger.debug ("workOnOIS641PF fillArrUde2 UDE2: " + ude2);
		for (int i1 = 0; i1 < maxCountArr; i1++ ) {
			if (arrUde2[i1].trim() == ude2.trim())  {
				logger.debug ("workOnOIS641PF fillArrUde2 - arrUde[i] == ude2 UDE2: " + ude2);
				break;
			}
			if (arrUde2[i1] == " " || arrUde2[i1] == null) {
				logger.debug ("workOnOIS641PF fillArrUde2 - Ude2 " + ude2 + " is added to arrUde2  i1: " + i1);
				arrUde2[i1] = ude2.trim();
				break;
			}
		}
	}
	
	/**
	 * select data from DRADTA and MHDISL and call createEXT641 for record creation
	 */
	public void workOnOIS641_6_DLIX(long dlix) {

		//step 1 - get TrackId from DRADTA
		logger.debug("workOnOIS641_6_DLIX  DLIX " + dlix.toString());
		DBAction action = database.table("DRADTR")
				.index("00")
				.selection("DRUDE2")
				.build();
		DBContainer DRADTR = action.getContainer();
		// Key value for read
		DRADTR.set("DRCONO", intCono);
		DRADTR.set("DRTLVL", intCons_2);
		DRADTR.set("DRCONN", intCons_0);
		DRADTR.set("DRDLIX", dlix);
		strUde2 = " ";
		if (action.read(DRADTR)) {
			strUde2 = DRADTR.get("DRUDE2");
		} else {
			return;
		}

		//step 2 - get ODLINE key data from MHDISL
		getDetailsFromMHDISL(dlix);
		if (arrRidn[0] == " " || arrRidn[0] == null) {
			return;
		}

		//step 3 - get ODLINE data and create EXT641 data per ODLINE record
		for (int i = 0; i < maxCountArr; i++ ) {
			if (arrRidn[i] != " " && arrRidn[i] != null)  {
				logger.debug ("workOnOIS641PF createEXT641 RIDN: " + arrRidn[i] + " " + arrRidl[i] + " " +  arrRidx[i]);
				createEXT641(dlix, arrRidn[i], arrRidl[i], arrRidx[i], arrFaci[i]);
			}
		}
	}

	/**
	 *  getDetailsFromMHDISL get ODLINE key data from MHDISL
	 */
	private void getDetailsFromMHDISL(long dlix) {

		logger.debug ("workOnOIS641PF getDetailsFromMHDISL Dlix: " +  dlix.toString());

		//select MHDISL data for given DLIX
		DBAction query = database.table("MHDISL")
				.index("00")
				.selection("URRORC", "URRIDN", "URRIDL", "URRIDX", "URFACI")
				.build();
		DBContainer container = query.getContainer();
		container.set("URCONO", intCono);
		container.set("URDLIX", dlix);
		query.readAll(container, 2, selectDataFromMHDISL);
		return;
	}

	/*
	 * check DLIX from table ODLINE if it is already saved in array addDlix
	 */
	Closure<?> selectDataFromMHDISL = { DBContainer containerMHDISL ->
		records << containerMHDISL;

		intRorc = containerMHDISL.get("URRORC");
		logger.debug ("workOnOIS641PF closure selectDataFromMHDISL RORC: " +  intRorc.toString());
		if (intRorc != 3) {
			return;
		}
		strRidn = containerMHDISL.get("URRIDN");
		intRidl = containerMHDISL.get("URRIDL");
		intRidx = containerMHDISL.get("URRIDX");
		strFaci = containerMHDISL.get("URFACI");

		logger.debug ("workOnOIS641PF closure selectDataFromMHDISL "
				+ " RIDN " + strRidn
				+ " RIDL " + intRidl.toString()
				+ " RIDX " + intRidx.toString()
				);

		for (int i = 0; i < maxCountArr; i++) {
			logger.debug ("workOnOIS641PF - selectDataFromMHDISL arr [i] : " + i + "arrRidn[]i " + arrRidn[i] + " " + arrRidl[i] + " " + arrRidx[i]);
			if (arrRidn[i] == strRidn && arrRidl[i] == intRidl && arrRidx[i] == intRidx) {
				logger.debug ("workOnOIS641PF - selectDataFromMHDISL arrRidn " + strRidn + " " + intRidl + " " + intRidx + " exist");
				break;
			}
			if ((arrRidn[i] == null || arrRidn[i] == " ") && arrRidl[i] == 0 && arrRidx[i] == 0) {
				logger.debug ("workOnOIS641PF - selectDataFromMHDISL arrRidn " + strRidn + " " + intRidl + " " + intRidx + " added");
				arrRidn[i] = strRidn;
				arrRidl[i] = intRidl;
				arrRidx[i] = intRidx;
				arrFaci[i] = strFaci;
				break;
			}
		}
	}


	/**
	 * createEXT641 create EXT641 data with information from ODLIN and DRADTR
	 */
	void createEXT641(long dlix, String orno, int ponr, int posx, String faci) {

		logger.debug ("workOnOIS641PF createEXT641"
				+ " DLIX " + dlix.toString()
				+ " ORNO " + orno
				+ " PONR " + ponr.toString()
				+ " POSX " + posx.toString()
				);
				
		doubleLnam = 0d
		//get LNAM from OOLINE/LNA2
		DBAction action_OOLINE = database.table("OOLINE")
				.index("00")
				.selection("OBLNA2")
				.build();
		DBContainer OOLINE = action_OOLINE.createContainer();
		OOLINE.set("OBCONO", intCono);
		OOLINE.set("OBORNO", orno);
		OOLINE.set("OBPONR", ponr);
		OOLINE.set("OBPOSX", posx);
		if (action_OOLINE.read(OOLINE)) {
			doubleLnam = OOLINE.get("OBLNA2");
		} else {
			doubleLnam = 0d;
		}
		
		//use passed FACI value, orginal from MHDISL
		strFaci = faci.trim();
		
		DBAction query = database.table("ODLINE")
				.index("00")
				.selection("UBORNO", "UBDLIX", "UBITNO", "UBSPUN", "UBNEPR", "UBDLQA", "UBIVQA", "UBFACI", "UBWHLO", "UBTEPY", "UBDLQS", "UBIVQS")    //A JBTST
				.build();
		DBContainer container = query.getContainer();
		container.set("UBCONO", intCono);
		container.set("UBORNO", orno);
		container.set("UBPONR", ponr);
		container.set("UBPOSX", posx);
		container.set("UBDLIX", dlix);
		query.readAll(container, 5, workOnDataFromODLINE);
	}

	/*
	 * store ODLINE detailed data in EXT641
	 */
	Closure<?> workOnDataFromODLINE = { DBContainer containerODLINE ->
		records << containerODLINE;

		logger.debug ("workOnOIS641PF createEXT641 workOnDataFromODLINE");


		//get CUCD from ODHEAD    
		DBAction action_ODHEAD = database.table("ODHEAD")
				.index("00")
				.selection("UACUCD")
				.build();
		DBContainer ODHEAD = action_ODHEAD.createContainer();
		ODHEAD.set("UACONO", intCono);
		ODHEAD.set("UAORNO", containerODLINE.get("UBORNO"));
		ODHEAD.set("UAWHLO", containerODLINE.get("UBWHLO"));
		ODHEAD.set("UADLIX", containerODLINE.get("UBDLIX"));
		ODHEAD.set("UATEPY", containerODLINE.get("UBTEPY"));
		if (action_ODHEAD.read(ODHEAD)) {
			strCucd = ODHEAD.get("UACUCD");
		} else {
			strCucd = "";
		}


		//get CSNO from MITFAC
		DBAction action_MITFAC = database.table("MITFAC")
				.index("00")
				.selection("M9CSNO")
				.build();
		DBContainer MITFAC = action_MITFAC.createContainer();
		MITFAC.set("M9CONO", intCono);
		MITFAC.set("M9FACI", strFaci);
		MITFAC.set("M9ITNO", containerODLINE.get("UBITNO"));
		if (action_MITFAC.read(MITFAC)) {
			strCsno = MITFAC.get("M9CSNO");
		} else {
			strCsno = "";
		}
		if (strCsno.trim().isEmpty()) {
			strCsno = "dummy";
		}
		logger.debug ("workOnOIS641PF createEXT641 workOnDataFromODLINE MITFAC-CSNO : " + strCsno);

		//check exsiting record in EXT641
		DBAction action_EXT641 = database.table("EXT641")
				.index("00")
				.selectAllFields()
				.build();
		DBContainer EXT641 = action_EXT641.createContainer();
		EXT641.set("EXCONO", intCono);
		EXT641.set("EXUDE2", strUde2);
		EXT641.set("EXDLIX", containerODLINE.get("UBDLIX"));
		EXT641.set("EXORNO", containerODLINE.get("UBORNO"));
		EXT641.set("EXPONR", containerODLINE.get("UBPONR"));
		EXT641.set("EXPOSX", containerODLINE.get("UBPOSX"));
		if (action_EXT641.read(EXT641)) {
			logger.debug ("workOnOIS641PF workOnDataFromODLINE EXT641 Record is already existing"
					+ " EXDLIX " + containerODLINE.get("UBDLIX")
					+ " EXORNO " + containerODLINE.get("UBORNO")
					+ " EXPONR " + containerODLINE.get("UBPONR")
					+ " EXPOSX " + containerODLINE.get("UBPOSX")
					);
			return;
		}
		logger.debug ("workOnOIS641PF workOnDataFromODLINE EXT641 Record will be written"
				+ " EXCSNO " + strCsno
				+ " EXORNO " + containerODLINE.get("UBORNO")
				+ " EXPONR " + containerODLINE.get("UBPONR")
				+ " EXPOSX " + containerODLINE.get("UBPOSX")
				+ " EXITNO " + containerODLINE.get("UBITNO")
				+ " EXFACI " + containerODLINE.get("UBFACI")
				+ " EXBJNO " + jobNumber
				);
		//insert the new record in 641
		EXT641.set("EXCSNO", strCsno);
		EXT641.set("EXBJNO", jobNumber);
		EXT641.set("EXITNO", containerODLINE.get("UBITNO"));
		EXT641.set("EXSPUN", containerODLINE.get("UBSPUN"));
		double deliveryQty = containerODLINE.getDouble("UBDLQA");
		if (deliveryQty == 0d) {
		  deliveryQty = containerODLINE.getDouble("UBIVQA");
		}
		String unitOfMeasure = containerODLINE.getString("UBSPUN").trim();	
		double netPrice = containerODLINE.getDouble("UBNEPR");		
		EXT641.set("EXNEPR", netPrice);
		EXT641.set("EXORQA", deliveryQty);
		EXT641.set("EXFACI", containerODLINE.get("UBFACI"));
		EXT641.set("EXLNAM", doubleLnam);
		
		double deliveryQtySP = containerODLINE.getDouble("UBDLQS");  
		if (deliveryQtySP == 0d) {                                   
		  deliveryQtySP = containerODLINE.getDouble("UBIVQS");       
		}                                                          
		doubleLnamCucd = deliveryQtySP * netPrice;                   
		EXT641.set("EXORQS", deliveryQtySP);                       
		EXT641.set("EXLNAU", doubleLnamCucd);                       
		EXT641.set("EXCUCD", strCucd);                              
		
		EXT641.set("EXRGDT", regdate as int);
		EXT641.set("EXRGTM", regtime as int);
		EXT641.set("EXLMDT", regdate as int);
		EXT641.set("EXCHNO", 1);
		EXT641.set("EXCHID", program.getUser());
		action_EXT641.insert(EXT641);
	}

	/**
	 * createEXT642
	 *
	 * collect data from EXT641 and aggregate LNAM per CSNO
	 * create EXT642 data
	 */
	void createEXT642() {

		logger.debug ("workOnOIS641PF createEXT642 started");
		
		for (int i1 = 0; i1 < maxCountArr; i1++ ) {
			if (!arrUde2[i1].trim().isEmpty())  {
				strUde2 = arrUde2[i1];
				
				//initialize later used array information
				for (int i = 0; i < maxCountArr; i++) {
					arrCsno[i] = " ";
					arrLnam[i] = 0d;
					arrCucd[i] = " ";    
					arrLnau[i] = 0d;    
				}
	
				//read all EXT641 data for the creation of EXT642 data
				DBAction queryEXT641 = database.table("EXT641")
						.index("10")
						.selection("EXLNAM", "EXCSNO", "EXCUCD", "EXLNAU")    
						.build();
				DBContainer EXT641 = queryEXT641.getContainer();
				logger.debug ("workOnOIS641PF createEXT642 readAll 00 mit CONO" + intCono + " UDE2 " + strUde2);
				EXT641.set("EXCONO", intCono);
				EXT641.set("EXBJNO", jobNumber);
				EXT641.set("EXUDE2", strUde2);
				queryEXT641.readAll(EXT641, 3, sumUpFromEXT641);
				if (intIndexMax < 0) {
					logger.debug("workOnOIS641PF createEXT642 EXT642 no data in arrCsno");
					return;
				}
	
				//create records in EXT642
				logger.debug ("workOnOIS641PF createEXT642 before addRecordEXT642");
				for (int i = 0; i < maxCountArr; i++) {
					if (arrCsno[i] != " "  && arrCsno[i] != null) {
						addRecordEXT642(arrCsno[i], arrLnam[i], arrCucd[i], arrLnau[i], arrLnad[i]);    
					}
				}
			}
		}
	}

	/*
	 * sumUpFromEXT641 - sum up LNAM per DLIX CSNO based on EXT641 values
	 */
	Closure<?> sumUpFromEXT641 = { DBContainer container ->
		records << container;

		logger.debug ("workOnOIS641PF createEXT642 sumUpFromEXT641 started");
		strCsno = container.get("EXCSNO");
		doubleLnam = container.get("EXLNAM");
		strCucd = container.getString("EXCUCD").trim();          
		doubleLnamCucd = container.get("EXLNAU");    

		logger.debug ("workOnOIS641PF createEXT642 sumUpFromEXT641 for strCsno: " + strCsno + " LNAM: " + doubleLnam.toString());
    
		for (int i = 0; i < maxCountArr; i++) {
			if (arrCsno[i] == strCsno) {
				logger.debug ("workOnOIS641PF - sumUpFromEXT641 arrCsno " + strCsno + " updated with " + doubleLnam.toString());
				if (strCucd.equals("CAD")) {           
				   arrLnau[i] += doubleLnamCucd;         
				} else {                             
				   arrLnad[i] += doubleLnamCucd;         
				}                                    
				arrLnam[i] += doubleLnam;             
				break;
			}
			
			if (arrCsno[i] == null || arrCsno[i] == " ") {
				logger.debug ("workOnOIS641PF - sumUpFromEXT641 arrCsno " + strCsno + " inserted with " + doubleLnam.toString());
				arrCsno[i] = strCsno;
				if (strCucd.equals("CAD")) {           
			     arrLnau[i] = doubleLnamCucd;     
				} else {                           
				   arrLnad[i] = doubleLnamCucd;     
				}
				arrLnam[i] = doubleLnam;           
				intIndexMax = i;
				break;
			}
			
		}

	}

	/**
	 * addRecordEXT642 - add a record to table EXT642
	 */
	void addRecordEXT642(String csno, double lnam, String cucd, double lnau, double lnad) {   
		//check exsiting record in EXT642
		logger.debug ("workOnOIS641PF addRecordEXT642 started"
				+ " csno " + csno
				+ " lnam " + lnam.toString()
				);
		DBAction action_EXT642 = database.table("EXT642")
				.index("00")
				.selection("EXLNAM", "EXBJNO")
				.build();
		DBContainer EXT642 = action_EXT642.createContainer();
		EXT642.set("EXCONO", intCono);
		EXT642.set("EXUDE2", strUde2);
		EXT642.set("EXCSNO", csno);
		if (action_EXT642.read(EXT642)) {
			logger.debug ("workOnOIS641PF addRecordEXT642 EXT642 Record is already existing"
					+ " EXUDE2 " + strUde2
					+ " EXCSNO " + csno);
			return;
		}
		//insert the new record in EXT642
		EXT642.set("EXCUCD", "USD");    
		EXT642.set("EXCUCU", "CAD");    
		EXT642.set("EXLNAU", lnau);     
		EXT642.set("EXLNAD", lnad);     
		EXT642.set("EXLNAM", lnam);
		EXT642.set("EXBJNO", jobNumber);
		EXT642.set("EXRGDT", regdate as int);
		EXT642.set("EXRGTM", regtime as int);
		EXT642.set("EXLMDT", regdate as int);
		EXT642.set("EXCHNO", 1);
		EXT642.set("EXCHID", program.getUser());
		action_EXT642.insert(EXT642);
		logger.debug ("workOnOIS641PF addRecordEXT642 EXT642 created"
				+ " EXUDE2 " + strUde2
				+ " EXCSNO " + csno);
	}

	/**
	 * remove possibly created data in EXT641 and EXT642, selected by the given BJNO
	 */
	void workOnOIS641PF_40() {

		logger.debug ("workOnOIS641PF workOnOIS641PF_40() job number " + jobNumber);

		//start working for EXT641
		DBAction queryEXT641 = database.table("EXT641")
				.index("10")
				.selection("EXUDE2","EXDLIX", "EXORNO", "EXPONR", "EXPOSX")
				.build();
		DBContainer containerEXT641 = queryEXT641.getContainer();
		containerEXT641.set("EXCONO", intCono);
		containerEXT641.set("EXBJNO", jobNumber);
		queryEXT641.readAll(containerEXT641, 2, removeFromEXT641);

		//start working for EXT642
		DBAction queryEXT642 = database.table("EXT642")
				.index("10")
				.selection("EXUDE2","EXCSNO")
				.build();
		DBContainer containerEXT642 = queryEXT642.getContainer();
		containerEXT642.set("EXCONO", intCono);
		containerEXT642.set("EXBJNO", jobNumber);
		queryEXT642.readAll(containerEXT642, 2, removeFromEXT642);
	}

	/*
	 * removeFromEXT641 remove data from EXT641
	 */
	Closure<?> removeFromEXT641 = { DBContainer container ->
		records << container;

		logger.debug ("workOnOIS641PF removeFromEXT641"
				+ " EXCONO " + intCono.toString()
				+ " EXUDE2 " + container.get("EXUDE2")
				+ " EXDLIX " + container.get("EXDLIX")
				+ " EXORNO " + container.get("EXORNO")
				+ " EXPONR " + container.get("EXPONR")
				+ " EXPOSX " + container.get("EXPOSX")
				);
		DBAction actionEXT641 = database.table("EXT641")
				.index("00")
				.build();
		DBContainer EXT641 = actionEXT641.createContainer();
		EXT641.set("EXCONO", intCono);
		EXT641.set("EXUDE2", container.get("EXUDE2"));
		EXT641.set("EXDLIX", container.get("EXDLIX"));
		EXT641.set("EXORNO", container.get("EXORNO"));
		EXT641.set("EXPONR", container.get("EXPONR"));
		EXT641.set("EXPOSX", container.get("EXPOSX"));
		actionEXT641.readLock(EXT641, doDelete);    
	}

	/*
	 * removeFromEXT642 remove data from EXT642
	 */
	Closure<?> removeFromEXT642 = { DBContainer container ->
		records << container;

		logger.debug ("workOnOIS641PF removeFromEXT642"
				+ " EXCONO " + intCono.toString()
				+ " EXUDE2 " + container.get("EXUDE2")
				+ " EXCSNO " + container.get("EXCSNO")
				);
		DBAction actionEXT642 = database.table("EXT642")
				.index("00")
				.build();
		DBContainer EXT642 = actionEXT642.createContainer();
		EXT642.set("EXCONO", intCono);
		EXT642.set("EXUDE2", container.get("EXUDE2"));
		EXT642.set("EXCSNO", container.get("EXCSNO"));
		actionEXT642.readLock(EXT642, doDelete);        
	}

	/*
	 * doDelete - do the delete on a concrete locked record
	 */
	Closure<?> doDelete = { LockedResult lockedResult ->
		lockedResult.delete();

	}
}