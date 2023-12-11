// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add Log Exception to EXTEXC
// Transaction AddLogException
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: ECOD - Exception Code
 * @param: ECNA - Name
 * 
*/


public class AddLogException extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  // Constructor 
  public AddLogException(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.utility = utility
     this.program = program
     this.logger = logger
  } 
    
  public void main() {       
     // Set Company Number
     int CONO = program.LDAZD.CONO as Integer

     // Exception Code
     String inECOD
     if (mi.in.get("ECOD") != null) {
        inECOD = mi.in.get("ECOD") 
     } else {
        inECOD = ""         
     }
      
     // Name
     String inECNA
     if (mi.in.get("ECNA") != null) {
        inECNA = mi.in.get("ECNA") 
     } else {
        inECNA = ""        
     }
     

     // Validate Exception record
     Optional<DBContainer> EXTEXC = findEXTEXC(CONO, inECOD)
     if(EXTEXC.isPresent()){
        mi.error("Log Exception already exists")   
        return             
     } else {
        // Write record 
        addEXTEXCRecord(CONO, inECOD, inECNA)          
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
  // Add EXTEXC record 
  //********************************************************************     
  void addEXTEXCRecord(int CONO, String ECOD, String ECNA){     
       DBAction action = database.table("EXTEXC").index("00").build()
       DBContainer EXTEXC = action.createContainer()
       EXTEXC.set("EXCONO", CONO)
       EXTEXC.set("EXECOD", ECOD)
       EXTEXC.set("EXECNA", ECNA)   
       EXTEXC.set("EXCHID", program.getUser())
       EXTEXC.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")          
       EXTEXC.set("EXRGDT", regdate) 
       EXTEXC.set("EXLMDT", regdate) 
       EXTEXC.set("EXRGTM", regtime)
       action.insert(EXTEXC)         
 } 

     
} 

