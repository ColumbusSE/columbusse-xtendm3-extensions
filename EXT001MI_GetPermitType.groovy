// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a permit type from EXTPTT
// Transaction GetPermitType
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


public class GetPermitType extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger
  
  // Definition 
  int inCONO
  String inPTPC
  
  // Constructor 
  public GetPermitType(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 // Get EXTPTT record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTPTT").index("00").selectAllFields().build()
     DBContainer EXTPTT = action.getContainer()
     EXTPTT.set("EXCONO", inCONO)
     EXTPTT.set("EXPTPC", inPTPC)
   
     // Read  
     if (action.read(EXTPTT)) {       
       mi.outData.put("PTPC", EXTPTT.getString("EXPTPC"))
       mi.outData.put("PTNA", EXTPTT.getString("EXPTNA"))
       mi.outData.put("PTSW", EXTPTT.get("EXPTSW").toString())
       mi.outData.put("PTDE", EXTPTT.getString("EXPTDE"))
       mi.outData.put("PTDT", EXTPTT.get("EXPTDT").toString())
       mi.write()       
    } else {
      mi.error("No record found")   
      return 
    }  
  }
}