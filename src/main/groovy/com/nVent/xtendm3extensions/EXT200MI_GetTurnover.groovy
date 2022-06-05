// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2022-04-30
// @version   1.0 
// 
// Description 
// This API is to get the payer's turnover amounts from this year and last year (ARS260)
// Transaction GetTurnover
// 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;
import java.time.LocalDate

public class GetTurnover extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger; 
  private final MICallerAPI miCaller; 

  // Definition of output fields
  public String outCONO 
  public String outDIVI
  public String outPYNO 
  public String outACYP
  public String outCUNO
  public String outTTUR
  public String outPTUR
  public String outTUSD
  public String outPUSD
  public String outCUCD
  
  public Integer CONO
  public String inCONO 
  public String DIVI
  public String PYNO
  public String ACYP
  public String CUNO
  public double TTUR
  public double PTUR
  public double TUSD
  public double PUSD
  public double sumTTUR
  public double sumPTUR

  // Constructor 
  public GetTurnover(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
     
     DIVI = mi.in.get("DIVI") 
     Optional<DBContainer> CMNDIV = findCMNDIV(CONO, DIVI)  
     if(!CMNDIV.isPresent()){                         
       mi.error("Division " + DIVI + " is invalid")   
       return                                         
     }   

     String currentPeriod = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
     int periodToday = Integer.parseInt(currentPeriod) 
     String currentYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
     int lastYear = Integer.parseInt(currentYear) - 1     
     String lastYearString = String.valueOf(lastYear)
     
     // Start the listing invoices in FSLBAL
     getTurnoverCurrentYear(currentPeriod, currentYear)
     outTTUR = String.valueOf(sumTTUR)
     
     getTurnoverLastYear(lastYearString)
     outPTUR = String.valueOf(sumPTUR)
     
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

  /**
   * Get date in yyyyMM format
   * @return date
   */
  public String currentDateY6AsString() {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
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
    mi.outData.put("DIVI", outDIVI) 
    mi.outData.put("PYNO", outPYNO) 
    mi.outData.put("CUNO", outCUNO)
    mi.outData.put("CUCD", outCUCD)
    mi.outData.put("TTUR", outTTUR)  
    mi.outData.put("PTUR", outPTUR)
  } 

   
   //******************************************************************** 
   // Find the turnover balance for the current period
   //********************************************************************  
   void getTurnoverCurrentYear(String currentPeriod, String currentYear){   
     
     double TTUR = 0
     String startPeriod = currentYear + "01"
     
     // List all records for PYNO and ACYP
     ExpressionFactory expression = database.getExpressionFactory("FSLBAL")
   
     expression = expression.ge("BSACYP", startPeriod).and(expression.le("BSACYP", currentPeriod)) 
     
     DBAction actionline = database.table("FSLBAL").index("30").matching(expression).selection("BSCONO", "BSDIVI", "BSPYNO", "BSCUNO", "BSACYP", "BSCUCD", "BSTUOV", "BSCUCD", "BSINAM").build()

     DBContainer line = actionline.getContainer()  
     
     // Read  
     line.set("BSCONO", CONO)  
     line.set("BSDIVI", DIVI)
     line.set("BSPYNO", PYNO)
     
     int pageSize = mi.getMaxRecords() <= 0 ? 1000 : mi.getMaxRecords()           

     actionline.readAll(line, 3, pageSize, releasedLineProcessorCurrentYear)   
   
   } 
   
   
   //******************************************************************** 
   // Find the turnover balance for the last year
   //********************************************************************  
   void getTurnoverLastYear(String lastYear){   
     
     double PTUR = 0
     
     // List all records for PYNO and ACYP
     ExpressionFactory expression = database.getExpressionFactory("FSLBAL")
   
     expression = expression.ge("BSACYP", lastYear + "01").and(expression.le("BSACYP", lastYear + "12"))
     
     DBAction actionline = database.table("FSLBAL").index("30").matching(expression).selection("BSCONO", "BSDIVI", "BSPYNO", "BSCUNO", "BSACYP", "BSCUCD", "BSTUOV", "BSCUCD", "BSINAM").build()

     DBContainer line = actionline.getContainer()  
     
     // Read  
     line.set("BSCONO", CONO)  
     line.set("BSDIVI", DIVI)
     line.set("BSPYNO", PYNO)
     
     int pageSize = mi.getMaxRecords() <= 0 ? 1000 : mi.getMaxRecords()           

     actionline.readAll(line, 3, pageSize, releasedLineProcessorLastYear)   
   
   } 


    
  //******************************************************************** 
  // List FSLEDG records for Payment Date - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessorCurrentYear = { DBContainer line ->        
        outDIVI = line.getString("BSDIVI")
        outPYNO = line.getString("BSPYNO")
        outCUNO = line.getString("BSCUNO")
        outCUCD = line.getString("BSCUCD")
        TTUR = line.get("BSTUOV")
        sumTTUR = sumTTUR + TTUR
        TTUR.round(2)
  }
  
  //******************************************************************** 
  // List FSLEDG records for Payment Date - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessorLastYear = { DBContainer line ->     
        PTUR = line.get("BSTUOV")
        sumPTUR = sumPTUR + PTUR
        PTUR.round(2)
  }

  
}