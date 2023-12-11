// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list log exceptions from EXTEXC
// Transaction LstLogException
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: ECOD - Exception Code
 * 
*/

/**
 * OUT
 * @return : CONO - Company
 * @return : ECOD - Exception Code
 * @return : ECNA - Exception Code Name
 * 
*/


public class LstLogException extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  int inCONO
  String inECOD
  
  // Constructor 
  public LstLogException(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     inCONO = program.LDAZD.CONO as Integer
    
     // Exception Code
     if (mi.in.get("ECOD") != null) {
        inECOD = mi.in.get("ECOD") 
     } else {
        inECOD = ""     
     }

     // List exceptions from EXTEXC
     listLogException()
  }
 

  //******************************************************************** 
  // List exceptions from EXTEXC
  //******************************************************************** 
  void listLogException(){ 
     DBAction action = database.table("EXTEXC").index("00").selectAllFields().reverse().build()
     DBContainer ext = action.getContainer()
      
     ext.set("EXCONO", inCONO)
     
     if (inECOD != "") {  
        ext.set("EXECOD", inECOD)
     }
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
     
     // Read with ECOD as key if entered, else read all 
     if (inECOD != "") {  
        action.readAll(ext, 2, pageSize, releasedItemProcessor) 
     } else {
        action.readAll(ext, 1, pageSize, releasedItemProcessor) 
     }
  } 

    Closure<?> releasedItemProcessor = { DBContainer ext -> 
      mi.outData.put("CONO", ext.get("EXCONO").toString())
      mi.outData.put("ECOD", ext.getString("EXECOD"))
      mi.outData.put("ECNA", ext.getString("EXECNA"))
      mi.write() 
   } 
}