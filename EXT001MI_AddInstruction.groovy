// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add a instruction to EXTINS
// Transaction AddInstruction
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: INIC - Instruction Code
 * @param: INNA - Name
 * @param: INTX - Text
 * 
*/


public class AddInstruction extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    // Constructor 
    public AddInstruction(MIAPI mi, DatabaseAPI database, ProgramAPI program,  UtilityAPI utility, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.program = program
       this.utility = utility
       this.logger = logger
    } 
      
    public void main() {       
       // Set Company Number
       int CONO = program.LDAZD.CONO as Integer
  
       //Instruction Code
       String inINIC
       if (mi.in.get("INIC") != null) {
          inINIC = mi.in.get("INIC") 
       } else {
          inINIC = ""         
       }
        
       // Name
       String inINNA
       if (mi.in.get("INNA") != null) {
          inINNA = mi.in.get("INNA") 
       } else {
          inINNA = ""        
       }
        
       // Text
       String inINTX
       if (mi.in.get("INTX") != null) {
          inINTX = mi.in.get("INTX") 
       } else {
          inINTX = ""        
       }
  
       // Validate instruction record
       Optional<DBContainer> EXTINS = findEXTINS(CONO, inINIC)
       if(EXTINS.isPresent()){
          mi.error("Instruction already exists")   
          return             
       } else {
          // Write record 
          addEXTINSRecord(CONO, inINIC, inINNA, inINTX)          
       }  
  
    }
    
      
    //******************************************************************** 
    // Get EXTINS record
    //******************************************************************** 
    private Optional<DBContainer> findEXTINS(int CONO, String INIC){  
       DBAction query = database.table("EXTINS").index("00").build()
       def EXTINS = query.getContainer()
       EXTINS.set("EXCONO", CONO)
       EXTINS.set("EXINIC", INIC)
       if(query.read(EXTINS))  { 
         return Optional.of(EXTINS)
       } 
    
       return Optional.empty()
    }
    
    //******************************************************************** 
    // Add EXTINS record 
    //********************************************************************     
    void addEXTINSRecord(int CONO, String INIC, String INNA, String INTX){     
         DBAction action = database.table("EXTINS").index("00").build()
         DBContainer EXTINS = action.createContainer()
         EXTINS.set("EXCONO", CONO)
         EXTINS.set("EXINIC", INIC)
         EXTINS.set("EXINNA", INNA)
         EXTINS.set("EXINTX", INTX)     
         EXTINS.set("EXCHID", program.getUser())
         EXTINS.set("EXCHNO", 1)          
         int regdate = utility.call("DateUtil", "currentDateY8AsInt")
         int regtime = utility.call("DateUtil", "currentTimeAsInt")
         EXTINS.set("EXRGDT", regdate) 
         EXTINS.set("EXLMDT", regdate) 
         EXTINS.set("EXRGTM", regtime)
         action.insert(EXTINS)         
    } 
  
     
} 

