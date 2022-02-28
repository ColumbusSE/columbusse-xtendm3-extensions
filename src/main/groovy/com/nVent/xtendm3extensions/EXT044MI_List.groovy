// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2021-09-27
// @version   1,0 
//
// Description 
// This API is to manage PPS044
// Transaction List
// 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class List extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;

  // Definition of output fields
  public String outCONO 
  public String outITNO
  public String outSUNO
  public String outWHLO 
  public String outFACI 
  public String outLEA1 
  public String ITNO
  public String SUNO
  public String WHLO
  public Integer CONO
  public int inITNOExists 
  public int inSUNOExists 
  public int inWHLOExists    
  
  // Constructor 
  public List(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
     this.mi = mi;
     this.database = database;  
     this.program = program;
  } 
    
  public void main() { 
     // Validate company
     CONO = mi.in.get("CONO")      
     if (CONO == null) {
        CONO = program.LDAZD.CONO as Integer
     } 
     Optional<DBContainer> CMNCMP = findCMNCMP(CONO)  
     if(!CMNCMP.isPresent()){                         
       mi.error("Company " + CONO + " is invalid")   
       return                                         
     }   
      
     // Validate Item Number
     ITNO = mi.in.get("ITNO")  
     if(!isNullOrEmpty(ITNO)){ 
         inITNOExists = 1
         Optional<DBContainer> MITMAS = findMITMAS(CONO, ITNO)
         if(!MITMAS.isPresent()){
            mi.error("Item " + ITNO + " is invalid")   
            return             
         }
     } else {
         inITNOExists = 0
     }
      
     // Validate Warehouse
     WHLO = mi.in.get("WHLO") 
     if(!isNullOrEmpty(WHLO)){ 
         inWHLOExists = 1
         Optional<DBContainer> MITWHL = findMITWHL(CONO, WHLO)
         if(!MITWHL.isPresent()){
            mi.error("Warehouse " + WHLO + " is invalid")   
            return             
         }  
     } else {
         inWHLOExists = 0
     }
 
     // Validate Supplier
     SUNO = mi.in.get("SUNO")  
     if(!isNullOrEmpty(SUNO)){ 
         inSUNOExists = 1
         Optional<DBContainer> CIDMAS = findCIDMAS(CONO, SUNO)
         if(!CIDMAS.isPresent()){
            mi.error("Supplier " + SUNO + " is invalid")   
            return             
         }  
     } else {
         inSUNOExists = 0
     }

     // Start the listing in MITVEX
     lstMITVEXRecord()

  }
 
  //******************************************************************** 
  // Get Company record
  //******************************************************************** 
  private Optional<DBContainer> findCMNCMP(Integer CONO){                             
      DBAction query = database.table("CMNCMP").index("00").selection("JICONO").build()   
      DBContainer CMNCMP = query.getContainer()                                           
      CMNCMP.set("JICONO", CONO)                                                         
      if(query.read(CMNCMP))  {                                                           
        return Optional.of(CMNCMP)                                                        
      }                                                                                  
      return Optional.empty()                                                            
  }                                                                                   

 //******************************************************************** 
 // Get Item record
 //******************************************************************** 
 private Optional<DBContainer> findMITMAS(Integer CONO, String ITNO){  
    DBAction query = database.table("MITMAS").index("00").selection("MMCONO", "MMITNO", "MMITDS").build()     
    def MITMAS = query.getContainer()
    MITMAS.set("MMCONO", CONO)
    MITMAS.set("MMITNO", ITNO)
    
    if(query.read(MITMAS))  { 
      return Optional.of(MITMAS)
    } 
  
    return Optional.empty()
  }
  
  //******************************************************************** 
  // Get Supplier information
  //******************************************************************** 
  private Optional<DBContainer> findCIDMAS(Integer CONO, String SUNO){  
    DBAction query = database.table("CIDMAS").index("00").selection("IDCSCD").build()
    def CIDMAS = query.getContainer()
    CIDMAS.set("IDCONO", CONO)
    CIDMAS.set("IDSUNO", SUNO)
    if(query.read(CIDMAS))  { 
      return Optional.of(CIDMAS)
    } 
  
    return Optional.empty()
  }

  //******************************************************************** 
  // Get Warehouse information
  //******************************************************************** 
  private Optional<DBContainer> findMITWHL(Integer CONO, String WHLO){  
    DBAction query = database.table("MITWHL").index("00").selection("MWFACI").build()
    def MITWHL = query.getContainer()
    MITWHL.set("MWCONO", CONO)
    MITWHL.set("MWWHLO", WHLO)
    if(query.read(MITWHL))  { 
      return Optional.of(MITWHL)
  } 
  
    return Optional.empty()
  }

  //******************************************************************** 
  // Check if null or empty
  //********************************************************************  
  public  boolean isNullOrEmpty(String key) {
      if(key != null && !key.isEmpty())
          return false;
      return true;
  }

     
  //******************************************************************** 
  // Set Output data
  //******************************************************************** 
  void setOutPut() {     
    mi.outData.put("CONO", outCONO) 
    mi.outData.put("ITNO", outITNO)
    mi.outData.put("SUNO", outSUNO)  
    mi.outData.put("WHLO", outWHLO)  
    mi.outData.put("FACI", outFACI)  
    mi.outData.put("LEA1", outLEA1)  
  } 

  //******************************************************************** 
  // List all matching records from MITVEX
  //********************************************************************  
   void lstMITVEXRecord(){   
     
     // List all Additional Info lines
     ExpressionFactory expression = database.getExpressionFactory("MITVEX")
   
     // Depending on input value
     if (inITNOExists>0 && inSUNOExists>0 && inWHLOExists>0) {
       expression = expression.eq("EXCONO", String.valueOf(CONO)).and(expression.eq("EXITNO", ITNO)).and(expression.eq("EXSUNO", SUNO)).and(expression.eq("EXWHLO", WHLO))
     } else if (inITNOExists>0 && inSUNOExists>0) {
       expression = expression.eq("EXCONO", String.valueOf(CONO)).and(expression.eq("EXITNO", ITNO)).and(expression.eq("EXSUNO", SUNO))
     } else if (inITNOExists>0) {
       expression = expression.eq("EXCONO", String.valueOf(CONO)).and(expression.eq("EXITNO", ITNO))
     } else {
       expression = expression.eq("EXCONO", String.valueOf(CONO))
     }
     
     // List Additional info lines 
     DBAction actionline = database.table("MITVEX").index("00").matching(expression).selection("EXCONO", "EXITNO", "EXSUNO", "EXWHLO", "EXFACI", "EXLEA1").build()  

     DBContainer line = actionline.getContainer()  
     
     // Read with one key  
     line.set("EXCONO", CONO) 
     
     int pageSize = mi.getMaxRecords() <= 0 ? 1000 : mi.getMaxRecords()          

     actionline.readAll(line, 1, pageSize, releasedLineProcessor)   
   
   } 

    
  //******************************************************************** 
  // List MITVEX records - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->     
    // Output selectAllFields 
    outCONO = String.valueOf(line.get("EXCONO")) 
    outITNO = String.valueOf(line.get("EXITNO"))  
    outSUNO = String.valueOf(line.get("EXSUNO"))
    outWHLO = String.valueOf(line.get("EXWHLO"))
    outFACI = String.valueOf(line.get("EXFACI"))
    outLEA1 = String.valueOf(line.get("EXLEA1"))
      
    // Send Output
    setOutPut()
    mi.write() 
  } 
}