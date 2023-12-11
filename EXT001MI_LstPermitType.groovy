// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list permit types from EXTPTT
// Transaction LstPermitType
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: PTPC - Permit Type
 * 
*/

/**
 * OUT
 * @param: PTPC - Permit Type
 * @param: PTNA - Name
 * @param: PTSW - Slash Withheld
 * @param: PTDE - Description
 * @param: PTDT - Expiration Date
 * 
*/

public class LstPermitType extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  private final LoggerAPI logger
  
  // Definition 
  int inCONO
  String inPTPC

  // Constructor 
  public LstPermitType(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer
     
     // Permit Type
     if (mi.in.get("PTPC") != null) {
        inPTPC = mi.in.get("PTPC") 
     } else {
        inPTPC = ""     
     }

     // List contracts from EXTPTT
     listPermitType()
  }


  //******************************************************************** 
  // List Permit Types from EXTPTT
  //******************************************************************** 
  void listPermitType(){ 
     DBAction action = database.table("EXTPTT").index("00").selectAllFields().reverse().build()
     DBContainer ext = action.getContainer()
     
     ext.set("EXCONO", inCONO)  
      
     if (inPTPC != null && inPTPC != "") {  
        ext.set("EXPTPC", inPTPC)
     }
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
     
     // Read with PTPC as key if entered, else read all 
     if (inPTPC != null && inPTPC != "") {  
        action.readAll(ext, 2, pageSize, releasedItemProcessor) 
     } else {
        action.readAll(ext, 1, pageSize, releasedItemProcessor) 
     }
  } 

    Closure<?> releasedItemProcessor = { DBContainer ext -> 
       mi.outData.put("PTPC", ext.getString("EXPTPC"))
       mi.outData.put("PTNA", ext.getString("EXPTNA"))
       mi.outData.put("PTSW", ext.get("EXPTSW").toString())
       mi.outData.put("PTDE", ext.getString("EXPTDE"))
       mi.outData.put("PTDT", ext.get("EXPTDT").toString())
       mi.write() 
   } 
}