// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list log grades from EXTGRD
// Transaction LstLogGrade
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: GRAD - Grade Code
 * 
*/

/**
 * OUT
 * @return : CONO - Company
 * @return : GRAD - Grade Code
 * @return : GRNA - Grade Name
 * 
*/


public class LstLogGrade extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  int inCONO
  String inGRAD
  
  // Constructor 
  public LstLogGrade(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     inCONO = program.LDAZD.CONO as Integer
    
     // Exception Code
     if (mi.in.get("GRAD") != null) {
        inGRAD = mi.in.get("GRAD") 
     } else {
        inGRAD = ""     
     }

     // List Log Grades from EXTGRD
     listLogGrades()
  }
 

  //******************************************************************** 
  // List log grades from EXTGRD
  //******************************************************************** 
  void listLogGrades(){ 
     DBAction action = database.table("EXTGRD").index("00").selectAllFields().reverse().build()
     DBContainer ext = action.getContainer()
      
     ext.set("EXCONO", inCONO)
     
     if (inGRAD != "") {  
        ext.set("EXGRAD", inGRAD)
     }
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	
     
     // Read with GRAD as key if entered, else read all 
     if (inGRAD != "") {  
        action.readAll(ext, 2, pageSize, releasedItemProcessor) 
     } else {
        action.readAll(ext, 1, pageSize, releasedItemProcessor) 
     }
  } 

    Closure<?> releasedItemProcessor = { DBContainer ext -> 
      mi.outData.put("CONO", ext.get("EXCONO").toString())
      mi.outData.put("GRAD", ext.getString("EXGRAD"))
      mi.outData.put("GRNA", ext.getString("EXGRNA"))
      mi.write() 
   } 
}