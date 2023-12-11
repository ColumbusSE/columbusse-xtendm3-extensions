// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add a new permit to EXTPTT
// Transaction AddPermitType
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: PTPC - Permit Type
 * @param: PTNA - Name
 * @param: PTSW - Slash Withheld
 * @param: PTDE - Description
 * @param: PTDT - Expiration Date
 * 
*/


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class AddPermitType extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    // Constructor 
    public AddPermitType(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.utility = utility
       this.program = program
       this.logger = logger
    } 
      
    public void main() {       
       // Set Company Number
       int CONO = program.LDAZD.CONO as Integer
  
       // Permit Type
       String inPTPC
       if (mi.in.get("PTPC") != null) {
          inPTPC = mi.in.get("PTPC") 
       } else {
          inPTPC = ""         
       }
        
       // Name
       String inPTNA
       if (mi.in.get("PTNA") != null) {
          inPTNA = mi.in.get("PTNA") 
       } else {
          inPTNA = ""        
       }
       
       // Slash Withheld
       int inPTSW
       if (mi.in.get("PTSW") != null) {
          inPTSW = mi.in.get("PTSW") 
       } else {
          inPTSW = 0       
       }
       
       // Description
       String inPTDE
       if (mi.in.get("PTDE") != null) {
          inPTDE = mi.in.get("PTDE") 
       } else {
          inPTDE = ""        
       }
  
       // Expiration Date
       int inPTDT
       if (mi.in.get("PTDT") != null) {
          inPTDT = mi.in.get("PTDT") 
       } else {
          inPTDT = 0        
       }
  
       //Validate date format
       boolean validPTDT = isDateValid(String.valueOf(inPTDT), "yyyyMMdd") 
       if(!validPTDT){
          mi.error("Expiration Date is not valid")   
          return  
       } 
       
       // Validate permit record
       Optional<DBContainer> EXTPTT = findEXTPTT(CONO, inPTPC)
       if(EXTPTT.isPresent()){
          mi.error("Permit already exists")   
          return             
       } else {
          // Write record 
          addEXTPTTRecord(CONO, inPTPC, inPTNA, inPTSW, inPTDE, inPTDT)          
       }  
  
    }
    

    //******************************************************************** 
    // Check if date format is correct
    //******************************************************************** 
    public boolean isDateValid(String date, String format) {
      try {
        LocalDate.parse(date, DateTimeFormatter.ofPattern(format))
        return true
      } catch (DateTimeParseException e) {
        return false
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
    // Add EXTPTT record 
    //********************************************************************     
    void addEXTPTTRecord(int CONO, String PTPC, String PTNA, int PTSW, String PTDE, int PTDT){     
         DBAction action = database.table("EXTPTT").index("00").build()
         DBContainer EXTPTT = action.createContainer()
         EXTPTT.set("EXCONO", CONO)
         EXTPTT.set("EXPTPC", PTPC)
         EXTPTT.set("EXPTNA", PTNA)
         EXTPTT.set("EXPTSW", PTSW)
         EXTPTT.set("EXPTDE", PTDE)
         EXTPTT.set("EXPTDT", PTDT)     
         EXTPTT.set("EXCHID", program.getUser())
         EXTPTT.set("EXCHNO", 1)  
         int regdate = utility.call("DateUtil", "currentDateY8AsInt")
         int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
         EXTPTT.set("EXRGDT", regdate) 
         EXTPTT.set("EXLMDT", regdate) 
         EXTPTT.set("EXRGTM", regtime)
         action.insert(EXTPTT)         
   } 

     
} 

