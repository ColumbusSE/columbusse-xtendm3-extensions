// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update log grades in EXTGRD
// Transaction UpdLogGrade
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: GRAD - Grade Code
 * @param: GRNA - Name
 * 
*/


public class UpdLogGrade extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    int inCONO
    String inGRAD
    String inGRNA
    
    // Constructor 
    public UpdLogGrade(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.utility = utility
       this.program = program
       this.logger = logger     
    } 
      
    public void main() {       
       // Set Company Number
       inCONO = program.LDAZD.CONO as Integer
  
       // Grade Code
       if (mi.inData.get("GRAD") != null) {
          inGRAD = mi.inData.get("GRAD").trim() 
       } else {
          inGRAD = ""         
       }
        
       // Name
       if (mi.inData.get("GRNA") != null) {
          inGRNA = mi.inData.get("GRNA").trim() 
       } else {
          inGRNA = ""        
       }
        
  
       // Validate log grade record
       Optional<DBContainer> EXTGRD = findEXTGRD(inCONO, inGRAD)
       if(!EXTGRD.isPresent()){
          mi.error("Log Grade doesn't exist")   
          return             
       }     
      
       // Update record
       updEXTGRDRecord()
       
    }
    
      
    //******************************************************************** 
    // Get EXTGRD record
    //******************************************************************** 
    private Optional<DBContainer> findEXTGRD(int cono, String grad){  
       DBAction query = database.table("EXTGRD").index("00").build()
       def EXTGRD = query.getContainer()
       EXTGRD.set("EXCONO", cono)
       EXTGRD.set("EXGRAD", grad)
       if(query.read(EXTGRD))  { 
         return Optional.of(EXTGRD)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Update EXTGRD record
    //********************************************************************    
    void updEXTGRDRecord(){      
       DBAction action = database.table("EXTGRD").index("00").build()
       DBContainer EXTGRD = action.getContainer()
       
       EXTGRD.set("EXCONO", inCONO)     
       EXTGRD.set("EXGRAD", inGRAD)
  
       // Read with lock
       action.readLock(EXTGRD, updateCallBackEXTGRD)
       }
     
       Closure<?> updateCallBackEXTGRD = { LockedResult lockedResult -> 
       
       if (inGRNA == "?") {
          lockedResult.set("EXGRNA", "")
       } else {
          if (inGRNA != "") {
             lockedResult.set("EXGRNA", inGRNA)
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

