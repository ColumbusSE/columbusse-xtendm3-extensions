// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-04-11
// @version   1.0 
//
// Description 
// This API will list records from table EXTPAR
// Transaction LstPAR



/**
 * IN
 * @param: CONO - Company Number
 * @param: FACI - Facility
 * @param: PLGR - Work Center
 * @param: TRDT - Transaction Date
 * @param: TRTM - Transaction Time
 * @param: TMSX - Time Suffix
 * 
*/

/**
 * OUT
 * @param: CONO	- Company Number
 * @param: FACI- Name
 * @param: PLGR- Work Center
 * @param: TRDT- Transaction Date
 * @param: TRTM- Transaction Time
 * @param: SHFC- Shift
 * @param: DEPT- Department
 * @param: REAR- Planning Area
 * @param: UPIT- Used Mch Run Time
 * @param: RUDI- Run Disturbance
 * @param: SEDI- Setup Disturbance
 * @param: STDT- Start Date
 * @param: OSTM- Start Time
 * @param: FIDT- Finish Date
 * @param: OFTM- Finish Time
 * @param: KIWG- Object
 * @param: TX60- Text
 * @param: RESP- Responsible
 * 
*/


public class LstPAR extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inFACI
  String inPLGR
  int inTRDT  
  int numberOfFields
  
  // Constructor 
  public LstPAR(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
      // Set Company Number
      inCONO = mi.in.get("CONO")      
      if (inCONO == null || inCONO == 0) {
         inCONO = program.LDAZD.CONO as Integer
      } 
  
       //Facility
       if (mi.in.get("FACI") != null && mi.in.get("FACI") != "") {
          inFACI = mi.inData.get("FACI").trim() 
       } else {
          inFACI = ""         
       }
        
       // Work Center
       if (mi.in.get("PLGR") != null && mi.in.get("PLGR") != "") {
          inPLGR = mi.inData.get("PLGR").trim() 
       } else {
          inPLGR = ""        
       }
        
       // Transaction Date
       if (mi.in.get("TRDT") != null) {
          inTRDT = mi.in.get("TRDT") 
       } else {
          inTRDT = 0        
       }

     // List records from EXTPAR
     listPAR()
  }
 

  //******************************************************************** 
  // List PAR records from EXTPAR
  //******************************************************************** 
  void listPAR(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTPAR")

     numberOfFields = 0

     if (inCONO != 0) {
       numberOfFields = 1
       expression = expression.eq("EXCONO", String.valueOf(inCONO))
     }

     if (inFACI != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXFACI", inFACI))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXFACI", inFACI)
         numberOfFields = 1
       }
     }

     if (inPLGR != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXPLGR", inPLGR))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXPLGR", inPLGR)
         numberOfFields = 1
       }
     }

     if (inTRDT != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXTRDT", String.valueOf(inTRDT)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXTRDT", String.valueOf(inTRDT))
         numberOfFields = 1
       }
     }

     DBAction actionline = database.table("EXTPAR").index("00").matching(expression).selectAllFields().build()
     DBContainer line = actionline.getContainer()   
     
     line.set("EXCONO", inCONO)

     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()     
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
  }

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("FACI", line.getString("EXFACI"))
      mi.outData.put("PLGR", line.getString("EXPLGR"))
      mi.outData.put("TRDT", line.get("EXTRDT").toString())
      mi.outData.put("TRTM", line.get("EXTRTM").toString())
      mi.outData.put("TMSX", line.get("EXTMSX").toString())
      mi.outData.put("DEPT", line.getString("EXDEPT"))
      mi.outData.put("REAR", line.getString("EXREAR"))
      mi.outData.put("SHFC", line.getString("EXSHFC"))
      mi.outData.put("UPIT", line.getDouble("EXUPIT").toString())
      mi.outData.put("RUDI", line.getString("EXRUDI"))
      mi.outData.put("SEDI", line.getString("EXSEDI"))
      mi.outData.put("STDT", line.get("EXSTDT").toString())
      mi.outData.put("OSTM", line.get("EXOSTM").toString())
      mi.outData.put("FIDT", line.get("EXFIDT").toString())
      mi.outData.put("OFTM", line.get("EXOFTM").toString())
      mi.outData.put("KIWG", line.getString("EXKIWG"))
      mi.outData.put("TX60", line.getString("EXTX60"))
      mi.outData.put("RESP", line.getString("EXRESP"))
      mi.write() 
   } 
}