// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list instructions from EXTINS
// Transaction LstInstruction
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: INIC - Instruction Code
 * 
*/

/**
 * OUT
 * @param: INIC - Instruction Code
 * @param: INNA - Name
 * @param: INTX - Text
 * 
*/


public class LstInstruction extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  int inCONO
  String inINIC
  
  // Constructor 
  public LstInstruction(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     inCONO = program.LDAZD.CONO as Integer
    
     // Instruction Code
     if (mi.in.get("INIC") != null) {
        inINIC = mi.in.get("INIC") 
     } else {
        inINIC = ""     
     }

     // List contracts from EXTINS
     listInstruction()
  }
 

  //******************************************************************** 
  // List instructions from EXTINS
  //******************************************************************** 
  void listInstruction(){ 
     DBAction action = database.table("EXTINS").index("00").selectAllFields().reverse().build()
     DBContainer ext = action.getContainer()
      
     ext.set("EXCONO", inCONO)
     
     if (inINIC != "") {  
        ext.set("EXINIC", inINIC)
     }
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()	 
     
     // Read with INIC as key if entered, else read all 
     if (inINIC != "") {  
        action.readAll(ext, 2, pageSize, releasedItemProcessor) 
     } else {
        action.readAll(ext, 1, pageSize, releasedItemProcessor) 
     }
  } 

    Closure<?> releasedItemProcessor = { DBContainer ext -> 
      mi.outData.put("INIC", ext.getString("EXINIC"))
      mi.outData.put("INNA", ext.getString("EXINNA"))
      mi.outData.put("INTX", ext.getString("EXINTX"))
      mi.write() 
   } 
}