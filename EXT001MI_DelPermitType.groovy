// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a permit type from EXTPTT
// Transaction DelPermitType
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: PTPC - Permit Type
 * 
*/


 public class DelPermitType extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    int CONO
    String inPTPC
  
    // Constructor 
    public DelPermitType(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.program = program
       this.logger = logger
    } 
      
    public void main() { 
       // Set Company Number
       CONO = program.LDAZD.CONO as Integer
  
       // Permit Type
       if (mi.in.get("PTPC") != null) {
          inPTPC = mi.in.get("PTPC") 
       } else {
          inPTPC = ""     
       }
  
       // Validate permit type
       Optional<DBContainer> EXTPTT = findEXTPTT(CONO, inPTPC)
       if(!EXTPTT.isPresent()){
          mi.error("Payment Type doesn't exist")   
          return             
       } else {
          // Delete records 
          deleteEXTPTTRecord() 
       } 
       
    }
  
  
  
    //******************************************************************** 
    // Get EXTPTT record
    //******************************************************************** 
    private Optional<DBContainer> findEXTPTT(int CONO, String PTPC){  
       DBAction query = database.table("EXTPTT").index("00").build()
       def EXTPTT = query.getContainer()
       EXTPTT.set("EXCONO", CONO)
       EXTPTT.set("EXPTPC", PTPC)
       if(query.read(EXTPTT))  { 
         return Optional.of(EXTPTT)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Delete record in EXTPTT
    //******************************************************************** 
    void deleteEXTPTTRecord(){ 
       DBAction action = database.table("EXTPTT").index("00").build()
       DBContainer EXTPTT = action.getContainer()
       EXTPTT.set("EXCONO", CONO) 
       EXTPTT.set("EXPTPC", inPTPC)
       action.readLock(EXTPTT, deleterCallbackEXTPTT)
    }
      
    Closure<?> deleterCallbackEXTPTT = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }