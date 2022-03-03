// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2021-09-27
// @version   1,0 
//
// Description 
// This API is to manage PPS044
// Transaction Add
// 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class Add extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final LoggerAPI logger;
  
  // Constructor 
  public Add(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi;
     this.database = database;
     this.miCaller = miCaller;
     this.program = program;
     this.logger = logger;
  } 
    
  public void main() {       
     // Validate company
     Integer CONO = mi.in.get("CONO")      
     if (CONO == null) {
        CONO = program.LDAZD.CONO as Integer
     } 
     Optional<DBContainer> CMNCMP = findCMNCMP(CONO)  
     if(!CMNCMP.isPresent()){                         
       mi.error("Company " + CONO + " is invalid")   
       return                                         
     }   
      
     // Validate Item Number
     String ITNO = mi.in.get("ITNO")  
     Optional<DBContainer> MITMAS = findMITMAS(CONO, ITNO)
     if(!MITMAS.isPresent()){
        mi.error("Item " + ITNO + " is invalid")   
        return             
     }
      
     // Validate Warehouse
     String WHLO = mi.in.get("WHLO")  
     Optional<DBContainer> MITWHL = findMITWHL(CONO, WHLO)
     if(!MITWHL.isPresent()){
        mi.error("Warehouse " + WHLO + " is invalid")   
        return             
     }  
 
     // Validate Supplier
     String SUNO = mi.in.get("SUNO")  
     Optional<DBContainer> CIDMAS = findCIDMAS(CONO, SUNO)
     if(!CIDMAS.isPresent()){
        mi.error("Supplier " + SUNO + " is invalid")   
        return             
     }  

     // GetLeadtime
     String LEA1 = mi.in.get("LEA1")  

     // Validate MITVEX
     Optional<DBContainer> MITVEX = findMITVEX(CONO, ITNO, SUNO, WHLO)
     if(MITVEX.isPresent()){
        mi.error("MITVEX record already exists")   
        return             
     } else {
        // Write record 
        addRecord(CONO, ITNO, SUNO, WHLO, LEA1)    
     }  
     
     // Validate MITBAL
     Optional<DBContainer> MITBAL = findMITBAL(CONO, ITNO, WHLO)
     if(!MITBAL.isPresent()){
        mi.error("MITBAL record doesn't exist")   
        return             
     } else {
        // Update MITBAL with new Lead Time values
        updMITBAL(CONO, ITNO, WHLO)       
     }  

  }
  

 public  boolean isNullOrEmpty(String key) {
        if(key != null && !key.isEmpty())
            return false;
        return true;
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
  // Get Supplier record
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
  // Get Warehouse record
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
  // Get MITBAL information
  //******************************************************************** 
  private Optional<DBContainer> findMITBAL(Integer CONO, String ITNO, String WHLO){  
    DBAction query = database.table("MITBAL").index("00").selection("MBITNO", "MBWHLO").build()
    def MITBAL = query.getContainer()
    MITBAL.set("MBCONO", CONO)
    MITBAL.set("MBITNO", ITNO)
    MITBAL.set("MBWHLO", WHLO)
    if(query.read(MITBAL))  { 
      return Optional.of(MITBAL)
  } 
  
    return Optional.empty()
  }

  //******************************************************************** 
  // Get MITVEX record
  //******************************************************************** 
  private Optional<DBContainer> findMITVEX(Integer CONO, String ITNO, String SUNO, String WHLO){  
     DBAction query = database.table("MITVEX").index("00").selection("EXITNO", "EXSUNO", "EXWHLO").build()
     def MITVEX = query.getContainer()
     MITVEX.set("EXCONO", CONO)
     MITVEX.set("EXITNO", ITNO)
     MITVEX.set("EXPRCS", "")
     MITVEX.set("EXSUFI", "")
     MITVEX.set("EXSUNO", SUNO)
     MITVEX.set("EXWHLO", WHLO)
     if(query.read(MITVEX))  { 
       return Optional.of(MITVEX)
     } 
  
     return Optional.empty()
  }

 //******************************************************************** 
 // Add MITVEX record
 //********************************************************************     
 void addRecord(Integer CONO, String ITNO, String SUNO, String WHLO, String LEA1){     
       int supplierLeadTime = mi.in.get("LEA1")
       
       String facility = ""
       
       DBAction action = database.table("MITVEX").index("00").selectAllFields().build()
       DBContainer MITVEX = action.createContainer()
       MITVEX.set("EXCONO", CONO)
       MITVEX.set("EXITNO", ITNO)
       MITVEX.set("EXSUNO", SUNO)
       MITVEX.set("EXWHLO", WHLO)
       
      // Get Facility from the Warehouse
      Optional<DBContainer> MITWHL = findMITWHL(CONO, WHLO)
      if(MITWHL.isPresent()){
         // Record found, continue to get information  
         DBContainer containerMITWHL = MITWHL.get() 
         facility = containerMITWHL.getString("MWFACI")   
      }  

       //Check if null value for those that are not mandatory  
       MITVEX.set("EXFACI", facility) 
       
       if(!isNullOrEmpty(mi.in.get("LEA1").toString())){  
         MITVEX.set("EXLEA1", mi.in.get("LEA1"))
       }else { 
         MITVEX.set("EXLEA1", ' ')   
       } 
     
       MITVEX.set("EXCHID", program.getUser())
       MITVEX.set("EXCHNO", 1) 
       LocalDateTime now = LocalDateTime.now();    
       DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
       String formatDate = now.format(format1);    
       DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");  
       String formatTime = now.format(format2);        
          
       //Converting String into int using Integer.parseInt()
       int regdate=Integer.parseInt(formatDate); 
       int regtime=Integer.parseInt(formatTime); 
       MITVEX.set("EXRGDT", regdate) 
       MITVEX.set("EXLMDT", regdate) 
       MITVEX.set("EXRGTM", regtime)
       action.insert(MITVEX)         
 } 
 
 
  //******************************************************************** 
  // Update MITBAL with Lead time
  //********************************************************************    
 void updMITBAL(Integer CONO, String ITNO, String WHLO){        
     DBAction action = database.table("MITBAL").index("00").selection("MBCONO", "MBWHLO", "MBITNO", "MBLEA1", "MBLEAT", "MBLMDT", "MBCHNO", "MBCHID").build()
     DBContainer MITBAL = action.getContainer()
           
     MITBAL.set("MBCONO", CONO)
     MITBAL.set("MBWHLO", WHLO)
     MITBAL.set("MBITNO", ITNO)

     // Read with lock
     action.readLock(MITBAL, updateCallBackMITBAL)     
 }

    
 Closure<?> updateCallBackMITBAL = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int supplierLeadTime = mi.in.get("LEA1")
     
     int totalLeadTime = lockedResult.get("MBLEAT")
     int newTotalLeadTime = totalLeadTime + supplierLeadTime 
     
     int changeNo = lockedResult.get("MBCHNO")
     int newChangeNo = changeNo + 1 
     
     //Set Supplier Lead Time
     lockedResult.set("MBLEA1", supplierLeadTime) 
     
     //Set Total Lead Time
     lockedResult.set("MBLEAT", newTotalLeadTime) 
        
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("MBLMDT", changeddate)  
      
     lockedResult.set("MBCHNO", newChangeNo) 
     lockedResult.set("MBCHID", program.getUser())
     lockedResult.update()
 }
 
     
} 

