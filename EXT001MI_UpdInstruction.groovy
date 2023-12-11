// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update an instruction in EXTINS
// Transaction UpdInstruction
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: INIC - Instruction Code
 * @param: INNA - Name
 * @param: INTX - Text
 * 
*/


public class UpdInstruction extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    int inCONO
    String inINIC
    String inINNA
    String inINTX
    
    // Constructor 
    public UpdInstruction(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.utility = utility
       this.program = program
       this.logger = logger     
    } 
      
    public void main() {       
       // Set Company Number
       inCONO = program.LDAZD.CONO as Integer
  
       //Instruction Code
       if (mi.inData.get("INIC") != null) {
          inINIC = mi.inData.get("INIC").trim() 
       } else {
          inINIC = ""         
       }
        
       // Name
       if (mi.inData.get("INNA") != null) {
          inINNA = mi.inData.get("INNA").trim() 
       } else {
          inINNA = ""        
       }
        
       // Text
       if (mi.inData.get("INTX") != null) {
          inINTX = mi.inData.get("INTX").trim()
       } else {
          inINTX = ""        
       }
  
       // Validate instruction record
       Optional<DBContainer> EXTINS = findEXTINS(inCONO, inINIC)
       if(!EXTINS.isPresent()){
          mi.error("Instruction doesn't exist")   
          return             
       }     
      
       // Update record
       updEXTINSRecord()
       
    }
    
  
    //******************************************************************** 
    // Get EXTINS record
    //******************************************************************** 
    private Optional<DBContainer> findEXTINS(int cono, String inic){  
       DBAction query = database.table("EXTINS").index("00").build()
       def EXTINS = query.getContainer()
       EXTINS.set("EXCONO", cono)
       EXTINS.set("EXINIC", inic)
       if(query.read(EXTINS))  { 
         return Optional.of(EXTINS)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Update EXTINS record
    //********************************************************************    
    void updEXTINSRecord(){      
       DBAction action = database.table("EXTINS").index("00").build()
       DBContainer EXTINS = action.getContainer()
       
       EXTINS.set("EXCONO", inCONO)     
       EXTINS.set("EXINIC", inINIC)
  
       // Read with lock
       action.readLock(EXTINS, updateCallBackEXTINS)
       }
     
       Closure<?> updateCallBackEXTINS = { LockedResult lockedResult -> 
       
       if (inINNA == "?") {
          lockedResult.set("EXINNA", "")
       } else {
          if (inINNA != "") {
             lockedResult.set("EXINNA", inINNA)
          }
       }
       
       if (inINTX == "?") {
          lockedResult.set("EXINTX", "")
       } else {
          if (inINTX != "") {
             lockedResult.set("EXINTX", inINTX)
          }
       }
          
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)       
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }


} 

