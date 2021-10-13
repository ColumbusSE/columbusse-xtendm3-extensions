/**
 * This extension is used to create records in TXS300 file CSYECT
 *
 * Name: EXT300MI.AddRecCSYECT.groovy
 *
 * Date         Changed By                         Description
 * 210902       Frank Zahlten (Columbus)           create records in TXS300 file CSYECT, no standard API exist
 * 211011       Frank Zahlten (Columbus)           additional input field WHLO
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddRecCSYECT extends ExtendM3Transaction {
	private final MIAPI mi;
	private final ProgramAPI program;
	private final DatabaseAPI database;
	private final MICallerAPI miCaller;
	private final LoggerAPI logger;

	private String vrcdTS00;
	private String vrcdTS01;
	private String vrcdTS02;
	private String vrcdTS07;
	private String vrcdTS08;
	private String vrcdTS09;
	private String vrcdTS10;
	private String vrcdTS11;
	private String cscdIISO;

	private String ctparm;

	private String mitwhlWHCD;
	private String mitwhlECAR;

	public AddRecCSYECT(MIAPI mi, ProgramAPI program, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger) {
		this.mi = mi;
		this.program = program;
		this.database = database;
		this.miCaller = miCaller;
		this.logger = logger;
	}

	public void main() {
		if(!validateInput()) {
			mi.write()
			return
		}
		if (!writeRecCSYECT()) {
			mi.write()
			return
		}
	}

	//*****************************************************
	// validateInput - Validate entered MODF and CONO
	//*****************************************************
	boolean validateInput(){
		String company = mi.in.get("CONO")
		if (company == null) {
			mi.error("Company " + company + " must be entered")
			return false
		}
		if(validateCompany(company )){
			mi.error("Company " + company + " is invalid")
			return false
		}

		int cono = mi.in.get("CONO")

		//check BSCD equal CSCD
		String bscd = mi.in.get("BSCD")
		String cscd = mi.in.get("CSCD")
		if (cscd.contentEquals(bscd)) {
			mi.error("Base country code" + bscd + " and supplier/warehouse country code " + cscd +" should not have the same value")
			return false
		}

		//smiple check VRCD for ECTP = 3
		String vrcd = mi.in.get("VRCD") //transaction type
		String StringECTP = mi.in.get("ECTP") //info type
		String ectp = StringECTP.trim().charAt(0)
		String vrcdFirstPosition = vrcd.charAt(0)
		if (ectp == '2'
		&&  vrcd.trim().length() > 0) {
			if  (vrcdFirstPosition > "2") {
				mi.error("For info type 2 the first digit of the transaction type must be less than 2")
				return false;
			}
		}
		//check VRCD in CSYTAB
		if(!validateCSYTAB(cono, "VRCD", vrcd , "  " )){
			mi.error("Business type - trade statistics (TST)" + vrcd +" does not exist")
			return false
		}

		//check info type Depending on vrcdTS00
		if (vrcdTS00 == "1") {
			if (ectp != '1'
			&&  ectp != '3'
			&&  ectp != '4' ) {
				mi.error("Business type "+ vrcd + " is invalid for info.type " + ectp)
				return false;
			}
		}
		if (vrcdTS00 == "2") {
			if (ectp != '2'
			&&  ectp != '3'
			&&  ectp != '5' ) {
				mi.error("Business type "+ vrcd + " is invalid for info.type " + ectp)
				return false;
			}
		}

		//check customer
		String cuno = mi.in.get("CUNO") //transaction type
		if (ectp == '2'
		||  ectp == '3'
		||  ectp == '5') {
			if (!validateOCUSMA(cono, cuno)) {
				mi.error("Customer number " + cuno +" does not exist")
				return false
			}
		} else {
			mi.error("Import isn't considered in this customer specific API program! ECTP = " + ectp)
			return false
		}

		//check customs statictic no
		String csno = mi.in.get("CSNO") //transaction type
		if (!validateCSYCSN(cono, csno)) {
			mi.error("Customs statistical number " + csno +" does not exist")
			return false
		}

		//check BSCD in CSYTAB
		bscd = mi.in.get("BSCD") //transaction type
		if (!validateCSYTAB(cono, "CSCD", bscd, "  " )) {
			mi.error("Base country" + bscd +" does not exist")
			return false
		}

		String whlo = mi.in.get("WHLO") //
		if (!validateMITWHL(cono,whlo)) {
			mi.error("Warehouse " + whlo +" does not exist")
			return false
		}

		//check CSCD in CSYTAB
		//String cscd = mi.in.get("CSCD") //transaction type
		if (!validateCSYTAB(cono, "CSCD", cscd , "  " )) {
			mi.error("Country" + cscd +" does not exist")
			return false
		}

		if (ectp != '3') {
			if (vrcdTS02 == "1") {
				if (cscdIISO.trim().length() == 0) {
					mi.error("Country code ISO must be entered")
					return false
				}
			}
			if (vrcdTS02 == "2") {
				if (cscdIISO.trim().length() > 0) {
					mi.error("Input from CSCD for IISO is not allowed. See CRS240 for this")
					return false
				}
			}
		}

		//check trade country
		String land = mi.in.get("LAND") //trade country
		if (ectp < '3') {
			if (!validateCSYTAB(cono, "CSEC", land , "  " )) {
				mi.error("Trade country" + land +" does not exist")
				return false
			}
		}

		//check ORCO
		String orco = mi.in.get("ORCO") //country of origin
		if (ectp != '3') {
			if (vrcdTS11 == "1") {
				if (orco.trim().length() == 0) {
					mi.error("Country of origin must be entered")
					return false
				}
			}
			if (vrcdTS11 == "2") {
				if (orco.trim().length() != 0) {
					mi.error("Country of origin is not allowed. See CRS240 for this")
					return false
				}
			}
			if (orco.trim().length() != 0) {
				if (!validateCSYTAB(cono, "CSCD", orco , "  " )) {
					mi.error("Country of origin ORCO" + orco +" does not exist")
					return false
				}
			}
		}

		//check country of origin
		int intEUOR = mi.in.get("EUOR") //country
		String euor = intEUOR.toString();
		//mi.error("vor dem check -- Country of origin Wert lautet " + euor )
		//mi.write();
		if (euor.trim().length() == 1) {
			euor = "00" + euor.trim()
		}
		if (euor.trim().length() == 2) {
			euor = "0" + euor.trim()
		}
		if (ectp < '3') {
			if (!validateCSYTAB(cono, "CSEC", euor , "  " )) {
				mi.error("Country of origin EUOR " + euor +" does not exist")
				return false
			}
		}

		//check language
		String lncd = mi.in.get("LNCD")
		if (!validateCSYTAB(cono, "LNCD", lncd , "  " )) {
			mi.error("Language" + lncd +" does not exist")
			return false
		}

		//check delivery method
		String modl = mi.in.get("MODL")
		if (ectp != '3') {
			if (vrcdTS09 == "1") {
				if (modl.trim().length() == 0) {
					mi.error("Delivery method must be entered")
					return false
				}
			}
			if (vrcdTS09 == "2") {
				if (modl.trim().length() != 0) {
					mi.error("Delivery method is not allowed. See CRS240 for this")
					return false
				}
			}
			if (modl.trim().length() > 0) {
				if (ectp != '3') {
					if (!validateCSYTAB(cono, "MODL", modl , "  " )) {
						mi.error("Delivery method" + modl +" does not exist")
						return false
					}
				}
			}
		}

		//check TEDL delivery condition
		String tedl = mi.in.get("TEDL")
		if (ectp != '3') {
			if (vrcdTS08 == "1") {
				if (tedl.trim().length() == 0) {
					mi.error("Delivery term must be entered")
					return false
				}
			}
			if (vrcdTS08 == "2") {
				if (tedl.trim().length() != 0) {
					mi.error("Delivery term is not allowed. See CRS240 for this")
					return false
				}
			}
		}
		if (tedl.trim().length() != 0) {
			if (!validateCSYTAB(cono, "TEDL", tedl , "  " )) {
				mi.error("Delivery term " + tedl +" does not exist")
				return false
			}
		}

		//check ECCC consumption code
		String eccc = mi.in.get("ECCC")
		eccc = chkInput(eccc);
		if (ectp != '3') {
			if (vrcdTS01 == "1") {
				if (eccc.trim().length() == 0) {
					mi.error("Consumption code ECCC must be entered")
					return false
				}
			}
			if (vrcdTS01 == "2") {
				if (eccc.trim().length() != 0) {
					mi.error("Consumption code ECCC is not allowed. See CRS240 for this")
					return false
				}
			}

		}
		if (eccc.trim().length() != 0) {
			if (ectp != '3') {
				if (!validateCSYTAB(cono, "ECCC", eccc , "  " )) {
					mi.error("Consumption code ECCC - trade stat " + eccc +" does not exist")
					return false
				}
			}
		}

		//check ECLC consumption code
		String eclc = mi.in.get("ECLC")
		eclc = chkInput(eclc);
		if (ectp != '3') {
			if (vrcdTS07 == "1") {
				if (eccc.trim().length() == 0) {
					mi.error("Labor code - trade statistics ECLC must be entered")
					return false
				}
			}
			if (vrcdTS07 == "2") {
				if (eccc.trim().length() != 0) {
					mi.error("Labor code - trade statistics ECLC is not allowed. See CRS240 for this")
					return false
				}
			}

		}
		if (ectp != '3') {
			if (orco.trim().length() != 0) {
				if (!validateCSYTAB(cono, "ECLC", eclc , "  " )) {
					mi.error("Labor code - trade statistics ECLC " + eclc +" does not exist")
					return false
				}
			}
		}

		//check ECQT format and value
		String stringECQT = mi.in.get("ECQT")
		double ECQT = isDouble(stringECQT)
		if (ECQT == 0d) {
			mi.error("Quantity ECQT " + stringECQT +" is not valid")
			return false
		}

		//check ECAM format and value
		String stringECAM = mi.in.get("ECAM")
		double ECAM = isDouble(stringECAM)
		if (ECAM == 0d) {
			mi.error("Amount ECAM " + stringECAM +" is not valid")
			return false
		}

		//check CUAM format and value
		String stringCUAM = mi.in.get("CUAM")
		double CUAM = isDouble(stringCUAM)
		if (CUAM == 0d) {
			mi.error("Amount CUAM " + stringCUAM +" is not valid")
			return false
		}

		//check LOAM format and value
		String stringLOAM = mi.in.get("LOAM")
		double LOAM = isDouble(stringLOAM)
		if (LOAM == 0d) {
			mi.error("Amount LOAM " + stringLOAM +" is not valid")
			return false
		}

		//check ECNW format and value
		String stringECNW = mi.in.get("ECNW")
		double ECNW = isDouble(stringECNW)
		if (ECNW == 0d) {
			mi.error("Amount ECNW " + stringECNW +" is not valid")
			return false
		}

		return true
	}

	//*****************************************************
	// chkStringInput - check if given string is null
	// Input
	// string
	//*****************************************************
	String chkInput(String string) {
		if (string == null) {
			return " ";
		}
		return string;
	}

	//*****************************************************
	// validateCompany - Validate given or retrieved CONO
	// Input
	// Company - from Input
	//*****************************************************
	boolean validateCompany(String company){
		// Run MI program
		def parameter = [CONO: company]
		List <String> result = []
		Closure<?> handler = {Map<String, String> response ->
			return response.CONO == 0}
		miCaller.call("MNS095MI", "Get", parameter, handler)
	}

	//*****************************************************
	// validateCSYTAB - Validate given CSYTAB value
	// Input
	// Company - from input
	// Constant value - from program
	// Key value - from input
	// Language code - from program
	//*****************************************************
	boolean validateCSYTAB(int cono, String stco, String stky, String lncd){
		//DBAction action = database.table("CSYTAB").index("00").selectAllFields().build()
		DBAction action = database.table("CSYTAB").index("00").selection("CTCONO", "CTDIVI", "CTSTCO", "CTSTKY", "CTLNCD", "CTPARM").build()
		DBContainer sytab = action.createContainer()

		sytab.set("CTCONO", cono)
		sytab.set("CTDIVI", "   ")
		sytab.set("CTSTCO", stco)
		sytab.set("CTSTKY", stky)
		sytab.set("CTLNCD", lncd)
		if (!action.read(sytab)) {
			return false
		}

		vrcdTS00 = ""
		vrcdTS01 = ""
		vrcdTS02 = ""
		vrcdTS07 = ""
		vrcdTS08 = ""
		vrcdTS09 = ""
		vrcdTS10 = ""
		vrcdTS11 = ""
		ctparm = ""
		ctparm = sytab.get("CTPARM")
		if (stco == ("VRCD")) {
			vrcdTS00 = ctparm.charAt(0)
			vrcdTS01 = ctparm.charAt(1)
			vrcdTS02 = ctparm.charAt(2)
			vrcdTS07 = ctparm.charAt(7)
			vrcdTS08 = ctparm.charAt(8)
			vrcdTS09 = ctparm.charAt(9)
			vrcdTS10 = ctparm.charAt(10)
			vrcdTS11 = ctparm.charAt(11)
		}
		if (stco == ("CSCD")) {
			cscdIISO = ctparm.substring(4, 6)
		}
		return true
	}

	//*****************************************************
	// validateOCUSMA - Validate given value CUNO
	// Input
	// Company - from input
	// customer no - from inpupt
	//*****************************************************
	boolean validateOCUSMA(int cono, String cuno){

		DBAction action_cusma = database.table("OCUSMA").index("00").selection("OKCONO", "OKCUNO").build()
		DBContainer cusma = action_cusma.createContainer()

		cusma.set("OKCONO", cono)
		cusma.set("OKCUNO", cuno)
		if (!action_cusma.read(cusma)) {
			return false
		}

		return true
	}

	//*****************************************************
	// validateMITWHL - Validate given value WHLO
	// Input
	// Company - from input
	// Warehouse- from inpupt
	//*****************************************************
	boolean validateMITWHL(int cono, String whlo){

		DBAction action_itwhl = database.table("MITWHL").index("00").selection("MWCONO", "MWWHLO", "MWCSCD", "MWECAR").build()
		DBContainer itwhl = action_itwhl.createContainer()

		itwhl.set("MWCONO", cono)
		itwhl.set("MWWHLO", whlo)
		if (!action_itwhl.read(itwhl)) {
			return false
		}
		mitwhlWHCD = itwhl.get("MWCSCD")
		mitwhlECAR = itwhl.get("MWECAR")
		return true
	}

	//*****************************************************
	// validateCSYCSN - Validate given value csno
	// Input
	// Company - from input
	// customs statistical no - from input
	//*****************************************************
	boolean validateCSYCSN(int cono, String csno){

		DBAction action_sycsn = database.table("CSYCSN").index("00").selection("CKCONO", "CKCSNO").build()
		DBContainer sycsn = action_sycsn.createContainer()

		sycsn.set("CKCONO", cono)
		sycsn.set("CKCSNO", csno)
		if (!action_sycsn.read(sycsn)) {
			return false
		}

		return true
	}

	//*****************************************************
	// isNullOrEmpty - check given String data and return
	//                 true when the data is null or empty
	//*****************************************************
	public  boolean isNullOrEmpty(String fieldValue) {
		if(fieldValue != null && !fieldValue.isEmpty())
			return false;
		return true;
	}

	public double isDouble(String stringValue) {
		double returnValue = 0d
		try {
			returnValue = Double.parseDouble(stringValue);
		} catch (NumberFormatException e) {
			returnValue = 0d
		}
		return returnValue
	}

	//*****************************************************
	// writeRecCSYECT - Start write record process by reading
	//                  CSYECT and write, when the record isn't
	//                  already existing
	//*****************************************************
	boolean writeRecCSYECT(){

		int CONO = mi.in.get("CONO")
		String LNCD = mi.in.get("LNCD")
		String DIVI =  mi.in.get("DIVI")
		int ECTP = mi.in.get("ECTP")
		String REFE = mi.in.get("REFE")
		String BSCD = mi.in.get("BSCD")
		String CUNO = mi.in.get("CUNO")
		String CSNO = mi.in.get("CSNO")
		String CSCD = mi.in.get("CSCD")
		int EUOR = mi.in.get("EUOR")
		String ORCO = mi.in.get("ORCO")
		String YEA4 = mi.in.get("YEA4")
		String SINO = mi.in.get("SINO")
		int IVDT = mi.in.get("IVDT")
		int ACDT = mi.in.get("ACDT")
		String VRCD = mi.in.get("VRCD")
		String VRDL = mi.in.get("VRDL")
		String LAND = mi.in.get("LAND")
		String TEDL = mi.in.get("TEDL")
		String MODL = mi.in.get("MODL")
		String stringECQT = mi.in.get("ECQT")
		double ECQT = isDouble(stringECQT)
		String ECLC = mi.in.get("ECLC")
		String ECCC = mi.in.get("ECCC")
		String stringECAM = mi.in.get("ECAM")
		double ECAM = isDouble(stringECAM)
		String stringECNW = mi.in.get("ECNW")
		double ECNW = isDouble(stringECNW)
		String stringCUAM = mi.in.get("CUAM")
		double CUAM = isDouble(stringCUAM)
		String stringLOAM = mi.in.get("LOAM")
		double LOAM = isDouble(stringLOAM)
		String FSVL = mi.in.get("FSVL")
		String CSPY = mi.in.get("CSPY")
		int DLDT = mi.in.get("DLDT")
		long SSVL = mi.in.get("SSVL")
		String VRNO = mi.in.get("VRNO")
		String VRIN = mi.in.get("VRIN")
		String WHLO = mi.in.get("WHLO")
		String WHCD = mitwhlWHCD;
		String ECAR = mitwhlECAR;

		//DBAction action = database.table("MHDISH").index("00").selectAllFields().build()
		DBAction action = database.table("CSYECT").index("00").selectAllFields().build()
		DBContainer syect = action.getContainer()

		//check that the record isn't already existing
		syect.set("CWCONO", CONO)
		syect.set("CWDIVI", DIVI)
		syect.set("CWECTP", ECTP)
		syect.set("CWREFE", REFE)
		if (action.read(syect)) {
			String CHID = syect.get("CWCHID")
			String RGDT = syect.get("CWRGDT")
			mi.error("CSYECT record was alreaddy created by " + CHID + " on " + RGDT)
			return false
		}

		if (!isNullOrEmpty(LNCD)){
			syect.set("CWLNCD", mi.in.get("LNCD"))
		} else {
			syect.set("CWLNCD", ' ')
		}
		if (!isNullOrEmpty(BSCD)){
			syect.set("CWBSCD", mi.in.get("BSCD"))
		} else {
			syect.set("CWBSCD", ' ')
		}
		if (!isNullOrEmpty(CUNO)){
			syect.set("CWCUSP", mi.in.get("CUNO"))
		} else {
			syect.set("CWCUSP", ' ')
		}
		if (!isNullOrEmpty(CSNO)){
			syect.set("CWCSNO", mi.in.get("CSNO"))
		} else {
			syect.set("CWCSNO", ' ')
		}
		if (!isNullOrEmpty(CSCD)){
			syect.set("CWCSCD", mi.in.get("CSCD"))
			syect.set("CWIISO", mi.in.get("CSCD"))
		} else {
			syect.set("CWCSCD", ' ')
			syect.set("CWIISO", ' ')
		}
		syect.set("CWEUOR", EUOR)
		//--------------------------
		if (!isNullOrEmpty(ORCO)){
			syect.set("CWORCO", mi.in.get("ORCO"))
		} else {
			syect.set("CWORCO", ' ')
		}
		if (!isNullOrEmpty(YEA4)){
			syect.set("CWYEA4", mi.in.get("YEA4"))
		} else {
			syect.set("CWYEA4", ' ')
		}
		if (!isNullOrEmpty(SINO)){
			syect.set("CWSINO", mi.in.get("SINO"))
		} else {
			syect.set("CWSINO", ' ')
		}
		syect.set("CWIVDT",IVDT)
		syect.set("CWACDT", ACDT)
		//--------------------------
		if (!isNullOrEmpty(VRCD)){
			syect.set("CWVRCD", mi.in.get("VRCD"))
		} else {
			syect.set("CWVRCD", ' ')
		}
		if (!isNullOrEmpty(VRDL)){
			syect.set("CWVRDL", mi.in.get("VRDL"))
		} else {
			syect.set("CWVRDL", ' ')
		}
		if (!isNullOrEmpty(LAND)){
			syect.set("CWLAND", mi.in.get("LAND"))
		} else {
			syect.set("CWLAND", ' ')
		}
		if (!isNullOrEmpty(TEDL)){
			syect.set("CWTEDL", mi.in.get("TEDL"))
		} else {
			syect.set("CWTEDL", ' ')
		}
		if (!isNullOrEmpty(MODL)){
			syect.set("CWMODL", mi.in.get("MODL"))
		} else {
			syect.set("CWMODL", ' ')
		}
		//--------------------------
		if (!isNullOrEmpty(ECLC)){
			syect.set("CWECLC", mi.in.get("ECLC"))
		} else {
			syect.set("CWECLC", ' ')
		}
		if (!isNullOrEmpty(ECCC)){
			syect.set("CWECCC", mi.in.get("ECCC"))
		} else {
			syect.set("CWECCC", ' ')
		}
		syect.set("CWECQT", ECQT)
		syect.set("CWECAM", ECAM)
		syect.set("CWCUAM", CUAM)
		syect.set("CWECNW", ECNW)
		//--------------------------
		syect.set("CWLOAM", LOAM)
		if (!isNullOrEmpty(FSVL)){
			syect.set("CWFSVL", mi.in.get("FSVL"))
		} else {
			syect.set("CWFSVL", ' ')
		}
		if (!isNullOrEmpty(CSPY)){
			syect.set("CWCSPY", mi.in.get("CSPY"))
		} else {
			syect.set("CWCSPY", ' ')
		}
		syect.set("CWDLDT", DLDT)
		syect.set("CWSSVL", SSVL)
		//--------------------------
		if (!isNullOrEmpty(VRNO)){
			syect.set("CWVRNO", mi.in.get("VRNO"))
		} else {
			syect.set("CWVRNO", ' ')
		}
		if (!isNullOrEmpty(VRIN)){
			syect.set("CWVRIN", mi.in.get("VRIN"))
		} else {
			syect.set("CWVRIN", ' ')
		}
		//--------------------------
		if (!isNullOrEmpty(WHLO)){
			syect.set("CWWHLO", mi.in.get("WHLO"))
		} else {
			syect.set("CWWHLO", ' ')
		}
		if (!isNullOrEmpty(WHCD)){
			syect.set("CWWHCD", WHCD)
		} else {
			syect.set("CWWHCD", ' ')
		}
		if (!isNullOrEmpty(ECAR)){
			syect.set("CWECAR", ECAR)
		} else {
			syect.set("CWECAR", ' ')
		}

		syect.set("CWCHID", program.getUser())
		syect.set("CWCHNO", 1)
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		String formatDate = now.format(format1);
		DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");
		String formatTime = now.format(format2);

		//Converting String into int using Integer.parseInt()
		int regdate=Integer.parseInt(formatDate);
		int regtime=Integer.parseInt(formatTime);
		syect.set("CWRGDT", regdate)
		syect.set("CWLMDT", regdate)
		syect.set("CWRGTM", regtime)

		action.insert(syect)  //<<<<<<<<<<

		return true;
	}
}
