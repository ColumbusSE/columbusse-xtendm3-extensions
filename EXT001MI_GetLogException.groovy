// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a log exception from EXTEXC
// Transaction GetLogException
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: ECOD - Exception Code
 * 
*/

/**
 * OUT
 * @param: CONO - Company Number
 * @param: ECOD - Exception Code
 * @param: ECNA - Name
 * 
*/


public class GetLogException extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  int inCONO
  String inECOD
  
  // Constructor 
  public GetLogException(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database  
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer

     // Exception Code
     if (mi.in.get("ECOD") != null) {
        inECOD = mi.in.get("ECOD") 
     } else {
        inECOD = ""         
     }
     
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTEXC record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTEXC").index("00").selectAllFields().build()
     DBContainer EXTEXC = action.getContainer()
     EXTEXC.set("EXCONO", inCONO)
     EXTEXC.set("EXECOD", inECOD)
     
    // Read  
    if (action.read(EXTEXC)) {  
      mi.outData.put("CONO", EXTEXC.get("EXCONO").toString())
      mi.outData.put("ECOD", EXTEXC.getString("EXECOD"))
      mi.outData.put("ECNA", EXTEXC.getString("EXECNA"))
      mi.write() 
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}