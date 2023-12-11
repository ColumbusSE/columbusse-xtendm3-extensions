// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a grades from EXTGRD
// Transaction DelLogGrade
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: GRAD - Grade Code
 * 
*/


 public class DelLogGrade extends ExtendM3Transaction {
    private final MIAPI mi
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
  
    int CONO
    String inGRAD

    // Constructor 
    public DelLogGrade(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database 
       this.program = program
       this.logger = logger
    } 
    
    public void main() { 
       // Set Company Number
       CONO = program.LDAZD.CONO as Integer
  
       // Grade Code
       if (mi.in.get("GRAD") != null) {
          inGRAD = mi.in.get("GRAD") 
       } else {
          inGRAD = ""     
       }
  
  
       // Validate log grade record
       Optional<DBContainer> EXTGRD = findEXTGRD(CONO, inGRAD)
       if(!EXTGRD.isPresent()){
          mi.error("Log Grade doesn't exist")   
          return             
       } else {
          // Delete records 
          deleteEXTGRDRecord() 
       } 
       
    }
  
  
    //******************************************************************** 
    // Get EXTGRD record
    //******************************************************************** 
    private Optional<DBContainer> findEXTGRD(int CONO, String GRAD){  
       DBAction query = database.table("EXTGRD").index("00").build()
       def EXTGRD = query.getContainer()
       EXTGRD.set("EXCONO", CONO)
       EXTGRD.set("EXGRAD", GRAD)
       if(query.read(EXTGRD))  { 
         return Optional.of(EXTGRD)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Delete record from EXTGRD
    //******************************************************************** 
    void deleteEXTGRDRecord(){ 
       DBAction action = database.table("EXTGRD").index("00").build()
       DBContainer EXTGRD = action.getContainer()
       EXTGRD.set("EXCONO", CONO) 
       EXTGRD.set("EXGRAD", inGRAD)
       action.readLock(EXTGRD, deleterCallbackEXTGRD)
    }
      
    Closure<?> deleterCallbackEXTGRD = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }