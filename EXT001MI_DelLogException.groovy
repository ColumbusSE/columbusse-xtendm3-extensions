// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a log exception from EXTEXC
// Transaction DelLogException
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: ECOD - Exception Code
 * 
*/


 public class DelLogException extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
  
    int CONO
    String inECOD

    // Constructor 
    public DelLogException(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
      this.mi = mi
      this.database = database 
      this.program = program
      this.logger = logger
    } 
    
    public void main() { 
      // Set Company Number
      CONO = program.LDAZD.CONO as Integer

      // Exception Code
      if (mi.in.get("ECOD") != null) {
        inECOD = mi.in.get("ECOD") 
      } else {
        inECOD = ""     
      }


      // Validate log exception record
      Optional<DBContainer> EXTEXC = findEXTEXC(CONO, inECOD)
      if(!EXTEXC.isPresent()){
        mi.error("Exception Code doesn't exist")   
        return             
      } else {
        // Delete records 
        deleteEXTEXCRecord() 
      } 
     
    }



    //******************************************************************** 
    // Get EXTEXC record
    //******************************************************************** 
    private Optional<DBContainer> findEXTEXC(int CONO, String ECOD){  
      DBAction query = database.table("EXTEXC").index("00").build()
      def EXTEXC = query.getContainer()
      EXTEXC.set("EXCONO", CONO)
      EXTEXC.set("EXECOD", ECOD)
      if(query.read(EXTEXC))  { 
        return Optional.of(EXTEXC)
      } 
  
      return Optional.empty()
    }
  

    //******************************************************************** 
    // Delete record from EXTEXC
    //******************************************************************** 
    void deleteEXTEXCRecord(){ 
      DBAction action = database.table("EXTEXC").index("00").build()
      DBContainer EXTEXC = action.getContainer()
      EXTEXC.set("EXCONO", CONO) 
      EXTEXC.set("EXECOD", inECOD)
      action.readLock(EXTEXC, deleterCallbackEXTEXC)
    }
    
    Closure<?> deleterCallbackEXTEXC = { LockedResult lockedResult ->  
      lockedResult.delete()
    }
  

 }