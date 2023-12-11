// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get an instruction from EXTINS
// Transaction GetInstruction
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


public class GetInstruction extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  int inCONO
  String inINIC
  
  // Constructor 
  public GetInstruction(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
     this.mi = mi
     this.database = database  
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
    inCONO = program.LDAZD.CONO as Integer

     // Instruction Code
     if (mi.in.get("INIC") != null) {
        inINIC = mi.in.get("INIC") 
     } else {
        inINIC = ""         
     }

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTINS record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTINS").index("00").selectAllFields().build()
     DBContainer EXTINS = action.getContainer()
     EXTINS.set("EXCONO", inCONO)
     EXTINS.set("EXINIC", inINIC)
     
    // Read  
    if (action.read(EXTINS)) {             
      mi.outData.put("INIC", EXTINS.getString("EXINIC"))
      mi.outData.put("INNA", EXTINS.getString("EXINNA"))
      mi.outData.put("INTX", EXTINS.getString("EXINTX"))
      mi.write()  
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}