// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2022-04-30
// @version   1.0 
// 
// Description 
// This API is used to get the payer's last payment date and total payed for this date
// Transaction GetPayments
// 

//**************************************************************************** 
// Date    Version     Developer 
// 220430  1.0         Jessica Bjorklund, Columbus   New API transaction
// 220622  2.0         Jessica Bjorklund, Columbus   Change of logic for PYDT, use index 26 instead of 20
//**************************************************************************** 


import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class GetPayments extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger; 
  private final MICallerAPI miCaller; 

  // Definition of output fields
  public String outCONO 
  public String outDIVI
  public String outPYNO  
  public String outCUNO
  public String outPYDT
  public String outPYTO
  
  public Integer CONO
  public String inCONO 
  public String DIVI
  public String PYNO
  public String CUNO
  public int lastPYDT
  public int PYDT
  public double CUAM
  public double PYTO

  // Constructor 
  public GetPayments(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
     
     DIVI = mi.in.get("DIVI") 
     Optional<DBContainer> CMNDIV = findCMNDIV(CONO, DIVI)  
     if(!CMNDIV.isPresent()){                         
       mi.error("Division " + DIVI + " is invalid")   
       return                                         
     }   

     PYNO = mi.in.get("PYNO")   
     Optional<DBContainer> OCUSMA = findOCUSMA(CONO, PYNO)  
     if(!OCUSMA.isPresent()){                         
       mi.error("Payer " + PYNO + " is invalid")   
       return                                         
     }   

   
     // Start the listing invoices in FSLEDG
     getLastPaymentDate()
     getLastPaymentAmount()
     
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
  private Optional<DBContainer> findCMNDIV(Integer CONO, String DIVI){  
    DBAction query = database.table("CMNDIV").index("00").selection("CCCONO", "CCDIVI").build()   
    def CMNDIV = query.getContainer()
    CMNDIV.set("CCCONO", CONO)
    CMNDIV.set("CCDIVI", DIVI)
    
    if(query.read(CMNDIV))  { 
      return Optional.of(CMNDIV)
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
    outPYNO = PYNO
    outCUNO = CUNO
    outPYDT = String.valueOf(lastPYDT)
    outPYTO = String.valueOf(PYTO)

    mi.outData.put("PYNO", outPYNO)  
    mi.outData.put("CUNO", outCUNO)  
    mi.outData.put("PYDT", outPYDT)  
    mi.outData.put("PYTO", outPYTO)     
  } 

   
   //******************************************************************** 
   // Find the last payment date for the payer
   //********************************************************************  
   void getLastPaymentDate(){   
     
     lastPYDT = 0
     PYDT = 0
     
     // List all Invoices with TRCD = 20
     ExpressionFactory expression = database.getExpressionFactory("FSLEDG")
   
     expression = expression.eq("ESTRCD", String.valueOf(20)).and(expression.eq("ESDIVI", DIVI))
     
     // List invoices 
     DBAction actionlinePYDT = database.table("FSLEDG").index("26")
     .matching(expression)
     .selection("ESCONO", "ESDIVI", "ESPYNO", "ESCUNO", "ESDTP5", "ESCUAM", "ESCUCD")
     .reverse()
     .build()

     DBContainer linePYDT = actionlinePYDT.getContainer()  
     
     // Read  
     linePYDT.set("ESCONO", CONO)  
     linePYDT.set("ESPYNO", PYNO)
     linePYDT.set("ESDTP5", 99999999)
     
     int pageSize = mi.getMaxRecords() <= 0 ? 1000 : mi.getMaxRecords()           

     actionlinePYDT.readAll(linePYDT, 2, pageSize, releasedLineProcessorPYDT)   
   
   } 

    
  //******************************************************************** 
  // List FSLEDG records for Payment Date - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessorPYDT = { DBContainer linePYDT ->     
    
    //Find Last Payment Date
    if (lastPYDT == 0) {
      PYDT = linePYDT.get("ESDTP5")
      lastPYDT = linePYDT.get("ESDTP5")
      CUNO = ""
      CUNO = linePYDT.get("ESCUNO")
    }
       
  }
  
  
   //******************************************************************** 
   // Find the last payment amount for the payer
   //********************************************************************  
   void getLastPaymentAmount(){   
     
     CUAM = 0
     PYTO = 0
     
     // List all Invoices with TRCD = 20 and last payment date
     ExpressionFactory expression = database.getExpressionFactory("FSLEDG")
   
     expression = expression.eq("ESTRCD", String.valueOf(20)).and(expression.eq("ESDTP5", String.valueOf(lastPYDT))) 
     
     // List invoices with TRCD = 20
     DBAction actionlinePYTO = database.table("FSLEDG").index("20").matching(expression).selection("ESCONO", "ESDIVI", "ESPYNO", "ESCUNO", "ESDTP5", "ESCUAM", "ESCUCD").build()

     DBContainer linePYTO = actionlinePYTO.getContainer()  
     
     // Read  
     linePYTO.set("ESCONO", CONO)       
     linePYTO.set("ESDIVI", DIVI)
     linePYTO.set("ESPYNO", PYNO)
     
     int pageSize = mi.getMaxRecords() <= 0 ? 1000 : mi.getMaxRecords()           

     actionlinePYTO.readAll(linePYTO, 3, pageSize, releasedLineProcessorPYTO)   
   
   } 

    
  //******************************************************************** 
  // List FSLEDG records for Payment Date - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessorPYTO = { DBContainer linePYTO ->     
    
    CUAM = 0  
    CUAM = linePYTO.get("ESCUAM")
    PYTO = PYTO + CUAM
    PYTO = PYTO.round(2)
  } 
}