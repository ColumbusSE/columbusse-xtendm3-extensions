// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2022-05-10
// @version   1.0 
//
// Description 
// This API is used to get more detailed information about a specific invoice
// Transaction GetInvoice
// 

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class GetInvoice extends ExtendM3Transaction {
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
  public String outCINO 
  public String outYEA4
  public String outCUAM
  public String outACAM
  public String outIVDT
  public String outDUDT
  public String outPYCD
  public String outCUCD   
  public String outOINA
  public String outUSDC
  public String outUSDA
  public String outTEPY
  public String outTEPX
  public String outVDSC
  public String outTECD
  public String outTECX
  public String outTECZ
  public String outCDAM
  
  public Integer CONO
  public String DIVI
  public String inCONO 
  public String inCINO
  public int inYEA4          
  public String TRCD
  public double CUAM  
  public double ACAM
  public double ARAT
  public double USDA
  public int ACDT
  public int IVDT
  public int YEA4
  public int JRNO
  public int JSNO
  public double sumCUAM
  public double CUAM20
  public double CUAM10
  public String LOCD
  public String discountTerms
  public double discountAmount
  public double discountPercent
  public double USDCurrencyRate
  public int USDRateDate
  public String localCurrency
  public double USDCvalue
  public boolean rateFound
  
  // Constructor 
  public GetInvoice(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
     
     inCINO = mi.in.get("CINO")
     inYEA4 = mi.in.get("YEA4")
     
     // Start the listing invoices in FSLEDG
     getInvoice()
        
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
  // Get TEDL text from CSYTAB                                              
  //********************************************************************    
   private Optional<DBContainer> findCSYTAB(Integer CONO, String STKY, String LNCD, String STCO){  
      DBAction query = database.table("CSYTAB").index("00").selection("CTCONO", "CTDIVI", "CTSTCO", "CTSTKY", "CTLNCD", "CTPARM").build()     
      def CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO", CONO)
      CSYTAB.set("CTDIVI", "")
      CSYTAB.set("CTSTCO", STCO)
      CSYTAB.set("CTSTKY", STKY)
      CSYTAB.set("CTLNCD", "")
      
      if(query.read(CSYTAB))  { 
        return Optional.of(CSYTAB)
      } 
    
      return Optional.empty()
    }
    
    
    //******************************************************************** 
    // Get Additional Info from FGLEDG
    //******************************************************************** 
    private Optional<DBContainer> findFGLEDG(Integer CONO, String DIVI, Integer YEA4, Integer JRNO, Integer JSNO){  
      DBAction query = database.table("FGLEDG").index("00").selection("EGCONO", "EGDIVI", "EGYEA4", "EGJRNO", "EGJSNO", "EGVDSC", "EGCUAM").build()   
      def FGLEDG = query.getContainer()
      FGLEDG.set("EGCONO", CONO)
      FGLEDG.set("EGDIVI", DIVI)
      FGLEDG.set("EGYEA4", YEA4)
      FGLEDG.set("EGJRNO", JRNO)
      FGLEDG.set("EGJSNO", JSNO)
    
      if(query.read(FGLEDG))  { 
        return Optional.of(FGLEDG)
      } 
  
      return Optional.empty()
    }


    //******************************************************************** 
    // Get Cash Discount from FSCASH
    //******************************************************************** 
    private Optional<DBContainer> findFSCASH(Integer CONO, String DIVI, Integer YEA4, Integer JRNO, Integer JSNO){    
      DBAction query = database.table("FSCASH").index("10").selection("ESCONO", "ESDIVI", "ESYEA4", "ESJRNO", "ESJSNO", "ESTECD", "ESCDAM", "ESCDP1").build()   
      def FSCASH = query.getContainer()
      FSCASH.set("ESCONO", CONO)
      FSCASH.set("ESDIVI", DIVI)
      FSCASH.set("ESYEA4", YEA4)
      FSCASH.set("ESJRNO", JRNO)
      FSCASH.set("ESJSNO", JSNO)
      
      if(query.readAll(FSCASH, 5, discountProcessor))  { 
        return Optional.of(FSCASH)
      } 
    
      return Optional.empty()
    }
  
  
    //******************************************************************** 
    // List Discount from FSCASH
    //********************************************************************  
    Closure<?> discountProcessor = { DBContainer FSCASH ->   
      discountTerms = FSCASH.getString("ESTECD")
      discountAmount = FSCASH.get("ESCDAM")
      discountPercent = FSCASH.get("ESCDP1")
      discountPercent = discountPercent / 100
      discountAmount = discountAmount * discountPercent
      discountAmount = discountAmount.round(2)
    }


    //******************************************************************** 
    // Get Exchange Rate from CCURRA
    //******************************************************************** 
    private Optional<DBContainer> findCCURRA(Integer CONO, String DIVI, Integer CUTD){    
      DBAction query = database.table("CCURRA")
      .index("10")
      .selection("CUCONO", "CUDIVI", "CUCRTP", "CUCUCD", "CUCUTD", "CUARAT", "CULOCD")
      .reverse()
      .build()   
      def CCURRA = query.getContainer()
      CCURRA.set("CUCONO", CONO)
      CCURRA.set("CUDIVI", DIVI)
      CCURRA.set("CUCRTP", 1)
      CCURRA.set("CUCUCD", "USD")
      CCURRA.set("CUCUTD", CUTD)
      
      rateFound = false
            
      if(query.readAll(CCURRA, 4, rateProcessor))  { 
        return Optional.of(CCURRA)
      } 
    
      return Optional.empty()
    }
  
  
    //******************************************************************** 
    // List Exchange Rate from CCURRA
    //********************************************************************  
    Closure<?> rateProcessor = { DBContainer CCURRA ->   
    
       LOCD = CCURRA.getString("CULOCD")

       double USDCurrencyRateValue = CCURRA.get("CUARAT")
       double USDRateDateValue = CCURRA.get("CUCUTD")
       if (USDRateDateValue <= IVDT && !rateFound) {
          rateFound = true
          USDCurrencyRate = CCURRA.get("CUARAT")
          USDRateDate = CCURRA.get("CUCUTD")
          USDCvalue = Double.valueOf(USDCurrencyRate)
          USDA = ACAM / USDCvalue
          USDA = USDA.round(2)
       }
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
    mi.outData.put("DIVI", outDIVI)
    mi.outData.put("PYNO", outPYNO)  
    mi.outData.put("CUNO", outCUNO)  
    mi.outData.put("CINO", outCINO) 
    mi.outData.put("YEA4", outYEA4)  
    mi.outData.put("CUAM", outCUAM) 
    mi.outData.put("ACAM", outACAM)
    mi.outData.put("IVDT", outIVDT)  
    mi.outData.put("DUDT", outDUDT)
    mi.outData.put("CUCD", outCUCD) 
    mi.outData.put("USDC", outUSDC)
    mi.outData.put("USDA", outUSDA)
    mi.outData.put("OINA", outOINA)
    mi.outData.put("TEPY", outTEPY)
    mi.outData.put("TEPX", outTEPX)
    mi.outData.put("TYPE", outVDSC)
    mi.outData.put("CDAM", outCDAM)
    mi.outData.put("TECD", outTECD)
    mi.outData.put("TECX", outTECX)
    mi.outData.put("TECZ", outTECZ)
  } 

   
   //******************************************************************** 
   // Get invoice from FSLEDG
   //********************************************************************  
   void getInvoice(){   
     
     sumCUAM = 0
     
     // Get Invoice
     ExpressionFactory expression = database.getExpressionFactory("FSLEDG")
   
     // Get Invoice 
     DBAction actionline = database.table("FSLEDG")
     .index("39")
     .matching(expression)
     .selection("ESCONO", "ESDIVI", "ESCINO", "ESYEA4", "ESPYNO", "ESCUNO", "ESCUCD", "ESCUAM", "ESRECO", "ESTRCD", "ESIVDT", "ESDUDT", "ESINYR", "ESTEPY", "ESARAT", "ESACDT")
     .build()

     DBContainer line = actionline.getContainer()  
        
     // Read with 4 key fields 
     line.set("ESCONO", CONO)  
     line.set("ESDIVI", DIVI)
     line.set("ESCINO", inCINO)
     line.set("ESINYR", inYEA4)
     
     int pageSize = mi.getMaxRecords() <= 0 ? 1000 : mi.getMaxRecords()           

     actionline.readAll(line, 4, pageSize, releasedLineProcessor)   
   
   } 

    
  //******************************************************************** 
  // List FSLEDG records - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line -> 

    TRCD = String.valueOf(line.get("ESTRCD"))
    
    if (TRCD == "10") {
      // Output selectAllFields 
      outCONO = String.valueOf(line.get("ESCONO")) 
      outDIVI = String.valueOf(line.get("ESDIVI"))  
      outPYNO = String.valueOf(line.get("ESPYNO"))
      outCUNO = String.valueOf(line.get("ESCUNO"))
      outCINO = String.valueOf(line.get("ESCINO"))
      outYEA4 = String.valueOf(line.get("ESYEA4"))
      outCUAM = String.valueOf(line.get("ESCUAM"))
      outIVDT = String.valueOf(line.get("ESIVDT"))
      outDUDT = String.valueOf(line.get("ESDUDT"))
      outCUCD = String.valueOf(line.get("ESCUCD")) 
      outTEPY = String.valueOf(line.get("ESTEPY")) 
      
      // TEDL text                                                        
      Optional<DBContainer> CSYTABforTEPY = findCSYTAB(CONO, outTEPY, "", "TEPY")        
      if (CSYTABforTEPY.isPresent()) {                                           
        // Record found, continue to get information                      
        DBContainer containerCSYTAB = CSYTABforTEPY.get()                        
        outTEPX = containerCSYTAB.getString("CTPARM")                     
      } else {                                                            
        outTEPX = ""                                                      
      }   
      
      ARAT = line.get("ESARAT")    
      IVDT = line.get("ESIVDT")
      CUAM = line.get("ESCUAM")                      
      ACAM = ARAT * CUAM                             
      outACAM = String.valueOf(ACAM)  
      
      localCurrency = outCUCD
    
      ACDT = line.get("ESACDT")
      YEA4 = line.get("ESYEA4")                      
      JRNO = line.get("ESJRNO")                      
      JSNO = line.get("ESJSNO")                     
      
      // Get type from FGLEDG if ACDT >= 20220401 (only for new invoices created in M3)
      if (ACDT >= 20220401) {
        Optional<DBContainer> FGLEDG = findFGLEDG(CONO, outDIVI, YEA4, JRNO, JSNO)    
        if (FGLEDG.isPresent()) {                                           
          // Record found, continue to get information                      
          DBContainer containerFGLEDG = FGLEDG.get()  
          Double testCUAM = containerFGLEDG.get("EGCUAM")
          if (containerFGLEDG.getString("EGVDSC") == "OI20001" && testCUAM >= 0) {
             outVDSC = "I"
          } else if (containerFGLEDG.getString("EGVDSC") == "OI20001" && testCUAM < 0) {
             outVDSC = "C"
          } else if (containerFGLEDG.getString("EGVDSC") == "GL01201" && testCUAM >= 0) {
             outVDSC = "D"
          } else if (containerFGLEDG.getString("EGVDSC") == "GL01201" && testCUAM < 0) {
             outVDSC = "U"
          } else if (containerFGLEDG.getString("EGVDSC") == "AR20001" && testCUAM >= 0) {
             outVDSC = "I"
          } else if (containerFGLEDG.getString("EGVDSC") == "AR20101" && testCUAM < 0) {
             outVDSC = "C"
          } else if (containerFGLEDG.getString("EGVDSC") == "AR10001" && testCUAM >= 0) {
             outVDSC = "I"
          } else if (containerFGLEDG.getString("EGVDSC") == "AR10002" && testCUAM >= 0) {
             outVDSC = "I"
          } else if (containerFGLEDG.getString("EGVDSC") == "AR10101" && testCUAM < 0) {
             outVDSC = "C"
          }          
        } else {                                                            
          outVDSC = ""                                                      
        }   
      } else {
          outVDSC = "" 
      }
      
      // Cash Discount   
      Optional<DBContainer> FSCASH = findFSCASH(CONO, outDIVI, YEA4, JRNO, JSNO)       
      if (FSCASH.isPresent()) {   
        // Record found, continue to get information                     
        DBContainer containerFSCASH = FSCASH.get()                       
        outTECD = discountTerms                                        
        outCDAM = discountAmount                                         
      } else {                                                            
        outTECD = ""                                                      
        outCDAM = ""                                                      
      } 
      
      Optional<DBContainer> CCURRA = findCCURRA(CONO, outDIVI, IVDT)       
      if (CCURRA.isPresent()) {   
        // Record found, continue to get information                     
        DBContainer containerCCURRA = CCURRA.get()                       
        outUSDC = USDCurrencyRate
        outUSDA = String.valueOf(USDA)  
      } else {  
        outUSDC = ""
        outUSDA = ""
      } 

      
      // TEDL text                                                        
      Optional<DBContainer> CSYTABforTECD = findCSYTAB(CONO, outTECD, "", "TECD")        
      if (CSYTABforTECD.isPresent()) {                                           
        // Record found, continue to get information                      
        DBContainer containerCSYTAB = CSYTABforTECD.get()                        
        outTECX = containerCSYTAB.getString("CTPARM")    
        outTECZ = containerCSYTAB.getString("CTPARM")  
      } else {                                                            
        outTECX = ""   
        outTECZ = ""
      }   

    }
    CUAM10 = 0
    CUAM20 = 0
    //Calculate left of invoice amount
    if (TRCD == "20") {
      CUAM20 = line.get("ESCUAM")
      if (sumCUAM >= 0 && CUAM20 > 0) {
        sumCUAM = sumCUAM + CUAM20
      } else if (sumCUAM <= 0 && CUAM20 < 0) {
        sumCUAM = sumCUAM + CUAM20
      } else if (sumCUAM >= 0 && CUAM20 < 0) {
        sumCUAM = sumCUAM + CUAM20
      } else if (sumCUAM <= 0 && CUAM20 > 0) {
        sumCUAM = sumCUAM + CUAM20
      }
    } 
    if (TRCD == "10") {
      CUAM10 = line.get("ESCUAM")
      if (sumCUAM >= 0 && CUAM10 > 0) {
        sumCUAM = sumCUAM + CUAM10
      } else if (sumCUAM <= 0 && CUAM10 < 0) {
        sumCUAM = sumCUAM + CUAM10
      } else if (sumCUAM >= 0 && CUAM10 < 0) {
        sumCUAM = sumCUAM + CUAM10
      } else if (sumCUAM <= 0 && CUAM10 > 0) {
        sumCUAM = sumCUAM + CUAM10
      }
    }
    
    sumCUAM = sumCUAM.round(2)
    outOINA = sumCUAM
    
  } 
}