// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a log grade from EXTGRD
// Transaction GetLogGrade
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: GRAD - Grade Code
 * 
*/

/**
 * OUT
 * @param: CONO - Company Number
 * @param: GRAD - Grade Code
 * @param: GRNA - Name
 * 
*/


public class GetLogGrade extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  int inCONO
  String inGRAD
  
  // Constructor 
  public GetLogGrade(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database  
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer

     // Grade Code
     if (mi.in.get("GRAD") != null) {
        inGRAD = mi.in.get("GRAD") 
     } else {
        inGRAD = ""         
     }
     
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTGRD record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTGRD").index("00").selectAllFields().build()
     DBContainer EXTGRD = action.getContainer()
     EXTGRD.set("EXCONO", inCONO)
     EXTGRD.set("EXGRAD", inGRAD)
     
    // Read  
    if (action.read(EXTGRD)) {  
      mi.outData.put("CONO", EXTGRD.get("EXCONO").toString())
      mi.outData.put("GRAD", EXTGRD.getString("EXGRAD"))
      mi.outData.put("GRNA", EXTGRD.getString("EXGRNA"))
      mi.write()  
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}