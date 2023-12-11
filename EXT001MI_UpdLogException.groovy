// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update a log exception in EXTEXC
// Transaction UpdLogException
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: ECOD - Exception Code
 * @param: ECNA - Name
 * 
*/


public class UpdLogException extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    int inCONO
    String inECOD
    String inECNA
    
    // Constructor 
    public UpdLogException(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.utility = utility
       this.program = program
       this.logger = logger     
    } 
      
    public void main() {       
       // Set Company Number
       inCONO = program.LDAZD.CONO as Integer
  
       // Exception Code
       if (mi.inData.get("ECOD") != null) {
          inECOD = mi.inData.get("ECOD").trim() 
       } else {
          inECOD = ""         
       }
        
       // Name
       if (mi.inData.get("ECNA") != null) {
          inECNA = mi.inData.get("ECNA").trim() 
       } else {
          inECNA = ""        
       }
        
  
       // Validate log exception record
       Optional<DBContainer> EXTEXC = findEXTEXC(inCONO, inECOD)
       if(!EXTEXC.isPresent()){
          mi.error("Log Exception doesn't exist")   
          return             
       }     
      
       // Update record
       updEXTEXCRecord()
       
    }
    
      
    //******************************************************************** 
    // Get EXTEXC record
    //******************************************************************** 
    private Optional<DBContainer> findEXTEXC(int cono, String ecod){  
       DBAction query = database.table("EXTEXC").index("00").build()
       def EXTEXC = query.getContainer()
       EXTEXC.set("EXCONO", cono)
       EXTEXC.set("EXECOD", ecod)
       if(query.read(EXTEXC))  { 
         return Optional.of(EXTEXC)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Update EXTEXC record
    //********************************************************************    
    void updEXTEXCRecord(){      
       DBAction action = database.table("EXTEXC").index("00").build()
       DBContainer EXTEXC = action.getContainer()
       
       EXTEXC.set("EXCONO", inCONO)     
       EXTEXC.set("EXECOD", inECOD)
  
       // Read with lock
       action.readLock(EXTEXC, updateCallBackEXTEXC)
       }
     
       Closure<?> updateCallBackEXTEXC = { LockedResult lockedResult -> 
       
       if (inECNA == "?") {
          lockedResult.set("EXECNA", "")
       } else {
          if (inECNA != "") {
             lockedResult.set("EXECNA", inECNA)
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

