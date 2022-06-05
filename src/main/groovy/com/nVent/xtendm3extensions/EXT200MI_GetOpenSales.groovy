// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2022-04-30
// @version   1.0 
// 
// Description 
// This API is used to get the payer's open sales orders and outstanding invoices 
// Transaction GetOpenSales
// 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter; 

public class GetOpenSales extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger; 
  private final MICallerAPI miCaller; 

  // Definition of output fields
  public String outCONO 
  public String outPYNO  
  public String outODIN
  public String outOINA
  public String outOVNI
  public String outNOOD
  
  public Integer CONO
  public String inCONO 
  public String PYNO
  public double ODIN
  public double OINA
  public double OVNI
  public double NOOD
  

  // Constructor 
  public GetOpenSales(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
     this.mi = mi;
     this.database = database;  
     this.program = program;
     this.logger = logger
     this.miCaller = miCaller;
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

     PYNO = mi.in.get("PYNO")   
     Optional<DBContainer> OCUSMA = findOCUSMA(CONO, PYNO)  
     if(!OCUSMA.isPresent()){                         
       mi.error("Payer " + PYNO + " is invalid")   
       return                                         
     }  
     
     Optional<DBContainer> CCUCRL = findCCUCRL(CONO, PYNO)  
     if(CCUCRL.isPresent()){  
        // Record found, continue to get information  
        DBContainer containerCCUCRL = CCUCRL.get()  
        outPYNO = containerCCUCRL.getString("CCPYNO")
        outODIN = containerCCUCRL.get("CCODIN")
        outOINA = containerCCUCRL.get("CCOINA")
        outOVNI = containerCCUCRL.get("CCOVNI")
        outNOOD = containerCCUCRL.get("CCNOOD")
     } else {
        outPYNO = ""
        outODIN = ""
        outOINA = ""
        outOVNI = ""
        outNOOD = ""
     }  
     
     // Send Output
     setOutPut()
     mi.write() 
     
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
  // Check Division
  //******************************************************************** 
  private Optional<DBContainer> findCCUCRL(Integer CONO, String PYNO){  
    DBAction query = database.table("CCUCRL").index("00").selection("CCCONO", "CCDIVI", "CCPYNO", "CCODIN", "CCOINA", "CCOVNI", "CCNOOD").build()   
    def CCUCRL = query.getContainer()
    CCUCRL.set("CCCONO", CONO)
    CCUCRL.set("CCDIVI", "")
    CCUCRL.set("CCPYNO", PYNO)
    
    if(query.read(CCUCRL))  { 
      return Optional.of(CCUCRL)
    } 
  
    return Optional.empty()
  }


  //******************************************************************** 
  // Check Payer
  //******************************************************************** 
  private Optional<DBContainer> findOCUSMA(Integer CONO, String PYNO){  
    DBAction query = database.table("OCUSMA").index("00").selection("OKCONO", "OKDIVI", "OKPYNO").build()   
    def OCUSMA = query.getContainer()
    OCUSMA.set("OKCONO", CONO)
    OCUSMA.set("OKDIVI", "")
    OCUSMA.set("OKCUNO", PYNO)
    
    if(query.read(OCUSMA))  { 
      return Optional.of(OCUSMA)
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
    mi.outData.put("PYNO", outPYNO)  
    mi.outData.put("ODIN", outODIN)  
    mi.outData.put("OINA", outOINA)
    mi.outData.put("OVNI", outOVNI)
    mi.outData.put("NOOD", outNOOD)
  } 

   

  
}