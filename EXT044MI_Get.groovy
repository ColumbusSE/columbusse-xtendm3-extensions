// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2021-09-27
// @version   1,0 
//
// Description 
// This API is to manage PPS044
// Transaction Get
// 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class Get extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  
  // Constructor 
  public Get(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
     this.mi = mi;
     this.database = database;  
     this.program = program;
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

     // Get record
     getRecord()
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
 //Get MITVEX record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("MITVEX").index("00").selectAllFields().build()
     DBContainer MITVEX = action.getContainer()
      
     // Key value for read
     MITVEX.set("EXCONO", mi.in.get("CONO"))
     MITVEX.set("EXITNO", mi.in.get("ITNO"))
     MITVEX.set("EXPRCS", "")
     MITVEX.set("EXSUFI", "")
     MITVEX.set("EXSUNO", mi.in.get("SUNO"))
     MITVEX.set("EXWHLO", mi.in.get("WHLO"))
     
    // Read  
    if (action.read(MITVEX)) {       
      String company = MITVEX.get("EXCONO")
      String item = MITVEX.get("EXITNO")
      String supplier = MITVEX.get("EXSUNO")
      String warehouse = MITVEX.get("EXWHLO")
      String facility = MITVEX.get("EXFACI")  
      String leadTime = MITVEX.get("EXLEA1")      
      
      // Send output value  
      mi.outData.put("CONO", String.valueOf(MITVEX.get("EXCONO")))
      mi.outData.put("ITNO", item)
      mi.outData.put("SUNO", supplier)
      mi.outData.put("WHLO", warehouse)
      mi.outData.put("FACI", facility)
      mi.outData.put("LEA1", leadTime) 
      mi.write()       
    }
  }  
}