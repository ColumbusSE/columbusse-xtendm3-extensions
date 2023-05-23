// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-04
// @version   1.0  
//
// Description 
// This API transacation ExpInvCopy is used to send data to PriceFX from M3 based on printed invoice 
// copies for a specific day
//

//************************************************************************************************* 
// Date    Version     Developer 
// 230504  1.0         Jessica Bjorklund, Columbus   New API transaction to list reprinted invoices
//************************************************************************************************* 


import java.math.RoundingMode 
import java.math.BigDecimal
import java.lang.Math
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


public class ExpInvCopy extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger; 
  private final MICallerAPI miCaller; 

  
  // Definition 
  public int company 
  public String division
  public int deliveryNumber
  public int invoiceNumber
  public String orderNumber
  public String itemNumber
  public int orderLine
  public int orderLineSuffix
  public String warehouse
  public String paymentTerms
  public String invoiceDate
  public String deliveryTerms
  public String year
  public String customer
  public String currencyCode
  public String languageCode
  public String companyString
  public String deliveryNumberString
  public String orderLineString
  public String orderLineSuffixString
  public String orderStatus
  public String salesPrice
  public String netPrice
  public String lineAmount
  public int sentFlag
  public int inCONO
  public String inDIVI 
  //public String inYEA4
  public String inNBTY
  public String inNBID
  public long attributeNumber
  public double discount1                
  public double discount2                
  public double discount3                
  public double discount4                
  public double discount5                
  public double discount6                
  public double discountSum             
  public String invoiceNumberString   
  public String extInvoiceNumber      
  public String invoicePrefix         
  public String invCharge             
  public String invReference          
  public String informationType       
  public String payer 
  public String lastExportedInvoiceNumberSeries
  public String lastExportedInvoiceNumberStart
  public String lastExportedInvoiceNumberMax
  public String lastExportedInvoiceNumberEnd
  public String numberSeriesForDivision
  public String currentYear
  public String currentDate

  // Definition of output fields
  public String outIVNO
  public String outIVDT
  public String outCUNO 
  public String outCUCD
  public String outTEPY 
  public String outITNO  
  public String outITDS  
  public String outLTYP  
  public String outSPUN  
  public String outQTY6  
  public String outQTY4  
  public String outDCOS
  public String outSAPR
  public String outNEPR
  public String outLNAM
  public String outHIE1
  public String outHIE2
  public String outHIE3
  public String outHIE4
  public String outHIE5
  public String outFACI
  public String outORTP
  public String outADID
  public String outORNO
  public String outRSCD
  public String outTEDL
  public String outTEL1
  public String outSMCD
  public String outWCON
  public String outORDT
  public String outOFNO
  public String outAGNO
  public String outORST
  public String outDLIX
  public String outADRT
  public String outCONO
  public String outNAME
  public String outTOWN
  public String outCSCD
  public String outPONO
  public String outLOCD
  public String outATAV
  public String outYEA4
  public String outPONR   
  public String outDISC   
  public String outDISM   
  public String outCHGH   
  public String outCHGF   
  public String outCHGO   
  public String outAGNH   
  public String outPYNO   
  public String outARAT
  public String outWHLO
  public String outUCA2
  public String outUCA3
  public String outEXIN


  // Constructor 
  public ExpInvCopy(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
     this.logger = logger; 
     this.miCaller = miCaller;
  } 
     
  
  //******************************************************************** 
  // Main 
  //********************************************************************  
  public void main() { 
      // Get LDA company of not entered 
      inCONO = getCONO()  
      
      inDIVI = mi.in.get("DIVI")  
      
      currentYear = currentYearAsString()
      currentDate = currentDateY8AsString()

      // Start the listing
      lstInvoiceCopies()

  }
  
                
  //******************************************************************** 
  // Get company from LDA
  //******************************************************************** 
  private Integer getCONO() {
    int company = mi.in.get("CONO") as Integer
    if(company == null){
      company = program.LDAZD.CONO as Integer
    } 
    return company
    
  } 


  //******************************************************************** 
  // Get Order Delivery Header Info ODHEAD
  //******************************************************************** 
  private Optional<DBContainer> findODHEAD(Integer CONO, String ORNO, String WHLO, Integer DLIX, String TEPY){  
    DBAction query = database.table("ODHEAD").index("00").selection("UACONO", "UAORNO", "UACUNO", "UADLIX", "UAWHLO", "UAIVNO", "UAIVDT", "UACUNO", "UACUCD", "UATEPY", "UAORST", "UAYEA4", "UATEDL", "UAINPX", "UADIVI", "UAPYNO").build()    
    def ODHEAD = query.getContainer()
    ODHEAD.set("UACONO", CONO)
    ODHEAD.set("UAORNO", ORNO)
    ODHEAD.set("UAWHLO", WHLO)
    ODHEAD.set("UADLIX", DLIX)
    ODHEAD.set("UATEPY", TEPY)
    
    if(query.read(ODHEAD))  { 
      return Optional.of(ODHEAD)
    } 
  
    return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Get Order Header Info OOHEAD
  //******************************************************************** 
  private Optional<DBContainer> findOOHEAD(Integer CONO, String ORNO){  
    DBAction query = database.table("OOHEAD").index("00").selection("OACONO", "OAORNO", "OACUNO", "OAFACI", "OAORTP", "OAORDT", "OAWCON", "OAADID", "OALNCD").build()    
    def OOHEAD = query.getContainer()
    OOHEAD.set("OACONO", CONO)
    OOHEAD.set("OAORNO", ORNO)

    if(query.read(OOHEAD))  { 
      return Optional.of(OOHEAD)
    } 
  
    return Optional.empty()
  }


  //******************************************************************** 
  // Get Order Line Info OOLINE
  //******************************************************************** 
  private Optional<DBContainer> findOOLINE(Integer CONO, String ORNO, Integer PONR, Integer POSX){  
    DBAction query = database.table("OOLINE").index("00").selection("OBCONO", "OBORNO", "OBPONR", "OBPOSX", "OBADID", "OBRSCD", "OBSMCD", "OBOFNO", "OBAGNO", "OBATNR").build()    
    def OOLINE = query.getContainer()
    OOLINE.set("OBCONO", CONO)
    OOLINE.set("OBORNO", ORNO)
    OOLINE.set("OBPONR", PONR)
    OOLINE.set("OBPOSX", POSX)

    if(query.read(OOLINE))  { 
      return Optional.of(OOLINE)
    } 
  
    return Optional.empty()
  }


  //******************************************************************** 
  // Get Item information MITMAS
  //******************************************************************** 
 private Optional<DBContainer> findMITMAS(Integer CONO, String ITNO){  
    DBAction query = database.table("MITMAS").index("00").selection("MMCONO", "MMITNO", "MMITDS", "MMHIE1", "MMHIE2", "MMHIE3", "MMHIE4", "MMHIE5").build()     
    def MITMAS = query.getContainer()
    MITMAS.set("MMCONO", CONO)
    MITMAS.set("MMITNO", ITNO)
    
    if(query.read(MITMAS))  { 
      return Optional.of(MITMAS)
    } 
  
    return Optional.empty()
  }
  

  //******************************************************************** 
  // Get TEDL text from CSYTAB
  //******************************************************************** 
 private Optional<DBContainer> findCSYTAB(Integer CONO, String STKY, String LNCD){  
    DBAction query = database.table("CSYTAB").index("00").selection("CTCONO", "CTDIVI", "CTSTCO", "CTSTKY", "CTLNCD", "CTPARM").build()     
    def CSYTAB = query.getContainer()
    CSYTAB.set("CTCONO", CONO)
    CSYTAB.set("CTDIVI", "")
    CSYTAB.set("CTSTCO", "TEDL")
    CSYTAB.set("CTSTKY", STKY)
    CSYTAB.set("CTLNCD", LNCD)
    
    if(query.read(CSYTAB))  { 
      return Optional.of(CSYTAB)
    } 
  
    return Optional.empty()
  }
  
  
   //***************************************************************************** 
   // Get Delivery Address using MWS410MI.GetAdr
   // Input 
   // Company
   // Delivery Number
   // Address Type
   //***************************************************************************** 
   private getDeliveryAddress(String company, String deliveryNumber, String AddressType){   
        def params = [CONO: companyString, DLIX: deliveryNumberString, ADRT: "02"] 
        String name = null
        String town = null
        String country = null
        String postalCode = null
        def callback = {
        Map<String, String> response ->
        if(response.NAME != null){
          name = response.NAME 
        }
        if(response.TOWN != null){
          town = response.TOWN  
        }
        if(response.CSCD != null){
          country = response.CSCD  
        }
        if(response.PONO != null){
          postalCode = response.PONO  
        }
        }

        miCaller.call("MWS410MI","GetAdr", params, callback)
      
        outNAME = name
        outTOWN = town
        outCSCD = country
        outPONO = postalCode
   } 

   //***************************************************************************** 
   // Get calculated info from the del order line using OIS350MI.GetDelLine
   // Input 
   // Company
   // Order Number
   // Delivery Number
   // Warehouse
   // Line Number
   // Line Suffix
   // Payment Terms
   //***************************************************************************** 
   private getAdditionalDelLineInfo(String company, String orderNumber, String deliveryNumber, String warehouse, String orderLine, String orderLineSuffix, String paymentTerms){   
        def params = [CONO: companyString, ORNO: orderNumber, DLIX: deliveryNumberString, WHLO: warehouse, PONR: orderLine, POSX: orderLineSuffix, TEPY: paymentTerms] 
        String invQty = null
        String delQty = null
        String costAmount = null
        def callback = {
        Map<String, String> response ->
        if(response.QTY4 != null){
          invQty = response.QTY4 
        }
        if(response.QTY6 != null){
          delQty = response.QTY6  
        }
        if(response.DCOS != null){
         costAmount = response.DCOS  
        }
        }

        miCaller.call("OIS350MI","GetDelLine", params, callback)
      
        outQTY4 = invQty
        outQTY6 = delQty
        outDCOS = costAmount
   } 

   //***************************************************************************** 
   // Get division related info from MNS150MI.GetBasicData
   // Input 
   // Company
   // Delivery Number
   //***************************************************************************** 
   private getDivisionInfo(String company, String division){   
        def params = [CONO: companyString, DIVI: division] 
        String localCurrency = null
        def callback = {
        Map<String, String> response ->
        if(response.LOCD != null){
          localCurrency = response.LOCD
        }
        }

        miCaller.call("MNS100MI","GetBasicData", params, callback)
      
        outLOCD = localCurrency
   } 


   //***************************************************************************** 
   // Get calculated info from the del order line using OIS350MI.LstInvLineByTyp    
   // Input 
   // Company
   // Division
   // Year
   // Invoice Number
   // Information Type
   // Invoice Prefix
   // Extended Invoice Number
   //***************************************************************************** 
   private getInvoiceCharges(String companyString, String division, String year, String invoiceNumberString, String informationType, String invoicePrefix, String extInvoiceNumber){   
        def params = [CONO: companyString, DIVI: division, YEA4: year, IVNO: invoiceNumberString, IVTP: informationType, INPX: invoicePrefix, EXIN: extInvoiceNumber] 
        def callback = {
        Map<String, String> response ->
        if(response.AMT1 != null){
          invCharge = response.AMT1 
        }
        if(response.IVRF != null){
          invReference = response.IVRF 
        }

        }

        miCaller.call("OIS350MI","LstInvLineByTyp", params, callback)
   } 
   
   
  //******************************************************************** 
  // Get Attribute from MOATTR
  //******************************************************************** 
 private Optional<DBContainer> findMOATTR(Integer CONO, long ATNR, String ATID, String AVSQ){  
    DBAction query = database.table("MOATTR").index("00").selection("AHCONO", "AHATNR", "AHATID", "AHAVSQ", "AHATAV").build()  
    def MOATTR = query.getContainer()
    MOATTR.set("AHCONO", CONO)
    MOATTR.set("AHATNR", ATNR)
    MOATTR.set("AHATID", "FOCUS")
    MOATTR.set("AHAVSQ", 0)
    
    if(query.read(MOATTR))  { 
      return Optional.of(MOATTR)
    } 
  
    return Optional.empty()
  }

  //******************************************************************** 
  // Get Invoice Line Info from FSLEDG
  //******************************************************************** 
  private Optional<DBContainer> findFSLEDG(Integer CONO, String DIVI, Integer YEA4, Integer JRNO, Integer JSNO){  
    DBAction query = database.table("FSLEDG").index("00").selection("ESCONO", "ESDIVI", "ESYEA4", "ESJRNO", "ESJSNO", "ESCINO", "ESINYR").build()    
    def FSLEDG = query.getContainer()
    FSLEDG.set("ESCONO", CONO)
    FSLEDG.set("ESDIVI", DIVI)
    FSLEDG.set("ESYEA4", YEA4)
    FSLEDG.set("ESJRNO", JRNO)
    FSLEDG.set("ESJSNO", JSNO)

    if(query.read(FSLEDG))  { 
      return Optional.of(FSLEDG)
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
  // Get date in yyyyMMdd format
  // @return date
  //******************************************************************** 
  public String currentDateY8AsString() {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
  }

  //******************************************************************** 
  // Get date in yyyy format
  // @return year
  //******************************************************************** 
  public String currentYearAsString() {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
  }

  //******************************************************************** 
  // Get current rate
  //******************************************************************** 
  private Optional<String> getCurrencyRate(String cono, String divi, String cucd, String date) {
    Optional<String> r = Optional.empty() 
    def params = ["CONO":cono, "FDDI": divi, "TODI": divi, "FCUR": cucd, "TCUR": cucd, "CRTP": "1".toString(), "CUTD": date] // toString is needed to convert from gstring to string
    String customer = null
    def callback = {
    Map<String, String> response ->
      if(response.ARAT != null){
        r = Optional.of(response.ARAT.toString())
      }
    }
    
    miCaller.call("CRS055MI","SelExchangeRate", params, callback)
    
    return r
    
  }

    
  //******************************************************************** 
  // Set Output data
  //******************************************************************** 
  void setOutput() {
    mi.outData.put("IVNO", outIVNO)
    mi.outData.put("IVDT", outIVDT)
    mi.outData.put("CUNO", outCUNO)
    mi.outData.put("CUCD", outCUCD)  
    mi.outData.put("TEPY", outTEPY)  
    mi.outData.put("ITNO", outITNO)  
    mi.outData.put("ITDS", outITDS)  
    mi.outData.put("LTYP", outLTYP)  
    mi.outData.put("SPUN", outSPUN)  
    mi.outData.put("QTY6", outQTY6)  
    mi.outData.put("QTY4", outQTY4)  
    mi.outData.put("DCOS", outDCOS)
    mi.outData.put("SAPR", outSAPR)
    mi.outData.put("NEPR", outNEPR)
    mi.outData.put("LNAM", outLNAM)
    mi.outData.put("HIE1", outHIE1)
    mi.outData.put("HIE2", outHIE2)
    mi.outData.put("HIE3", outHIE3)
    mi.outData.put("HIE4", outHIE4)
    mi.outData.put("HIE5", outHIE5)
    mi.outData.put("FACI", outFACI)
    mi.outData.put("ORTP", outORTP)
    mi.outData.put("ORNO", outORNO)
    mi.outData.put("WCON", outWCON)
    mi.outData.put("ADID", outADID)
    mi.outData.put("RSCD", outRSCD)
    mi.outData.put("SMCD", outSMCD)
    mi.outData.put("OFNO", outOFNO)
    mi.outData.put("ORDT", outORDT)
    mi.outData.put("AGNO", outAGNO)
    mi.outData.put("ORST", outORST)
    mi.outData.put("NAME", outNAME)
    mi.outData.put("TOWN", outTOWN)
    mi.outData.put("CSCD", outCSCD)
    mi.outData.put("PONO", outPONO)
    mi.outData.put("LOCD", outLOCD)
    mi.outData.put("DLIX", outDLIX)
    mi.outData.put("ATVN", outATAV)
    mi.outData.put("YEA4", outYEA4)
    mi.outData.put("IVDT", outIVDT)
    mi.outData.put("TEDL", outTEDL)
    mi.outData.put("TEL1", outTEL1)
    mi.outData.put("PONR", outPONR)    
    mi.outData.put("DISC", outDISC)    
    mi.outData.put("DISM", outDISM)    
    mi.outData.put("CHGH", outCHGH)    
    mi.outData.put("CHGF", outCHGF)    
    mi.outData.put("CHGO", outCHGO)    
    mi.outData.put("AGNH", outAGNH)    
    mi.outData.put("PYNO", outPYNO) 
    mi.outData.put("ARAT", outARAT)
    mi.outData.put("WHLO", outWHLO)
    mi.outData.put("UCA2", outUCA2)
    mi.outData.put("UCA3", outUCA3)
    mi.outData.put("EXIN", outEXIN)
    lastExportedInvoiceNumberEnd = outIVNO
  } 
    
    
  //******************************************************************** 
  // List invoice copies
  //********************************************************************  
   void lstInvoiceCopies(){   
     
     // List all Invoice Lines for copy printing
     ExpressionFactory expression = database.getExpressionFactory("FSLEDX")
   
     expression = expression.eq("ESDIVI", inDIVI).and(expression.eq("ESSEXN", "392")).and(expression.eq("ESLMDT", currentDate))  

     // List Invoice Lines   
     DBAction actionlineFSLEDX = database.table("FSLEDX").index("20").matching(expression).selection("ESCONO", "ESDIVI", "ESSEXN", "ESYEA4", "ESJRNO", "ESJSNO").build()     

     DBContainer lineFSLEDX = actionlineFSLEDX.getContainer()  
     
     // Read with one key  
     lineFSLEDX.set("ESCONO", CONO)  
     
     int pageSize = mi.getMaxRecords() <= 0 ? 1000 : mi.getMaxRecords()           

     actionlineFSLEDX.readAll(lineFSLEDX, 1, pageSize, releasedLineProcessorFSLEDX)   
   
   } 
 

  //******************************************************************** 
  // List Order Invoice Lines - main loop - FSLEDX
  //********************************************************************  
  Closure<?> releasedLineProcessorFSLEDX = { DBContainer lineFSLEDX ->   
     //For each record, find the invoice number and use as input to lstInvoiceLines()
     
     // Fields from FSLEDX to use in the other read
     int journalNumber = lineFSLEDX.get("ESJRNO")
     int journalSeqNumber = lineFSLEDX.get("ESJSNO") 
     String cinoFromFSLEDG = ""  
      
     // Get Invoice Number from FSLEDG
     Optional<DBContainer> FSLEDG = findFSLEDG(inCONO, inDIVI, currentYear.toInteger(), journalNumber, journalSeqNumber)
     if(FSLEDG.isPresent()){
       // Record found, continue to get information  
       DBContainer containerFSLEDG = FSLEDG.get() 
       cinoFromFSLEDG = containerFSLEDG.getString("ESCINO")  
     } 
      
     lstInvoiceLines(cinoFromFSLEDG)    

  }

  
  //******************************************************************** 
  // List all information
  //********************************************************************  
   void lstInvoiceLines(String cino){   
     
     // List all Invoice Delivery Lines
     ExpressionFactory expression = database.getExpressionFactory("ODLINE")
   
     expression = expression.eq("UBDIVI", inDIVI).and(expression.eq("UBIVNO", cino))  

     // List Invoice Delivery Lines   
     DBAction actionline = database.table("ODLINE").index("20").matching(expression).selection("UBCONO", "UBLMDT", "UBORNO", "UBPONR", "UBPOSX", "UBWHLO", "UBTEPY", "UBIVNO", "UBITNO", "UBLTYP", "UBSPUN", "UBSAPR", "UBNEPR", "UBLNAM", "UBDCOS", "UBDIP1", "UBDIP2", "UBDIP3", "UBDIP4", "UBDIP5", "UBDIP6", "UBEXIN", "UBINPX", "UBYEA4").build()     

     DBContainer line = actionline.getContainer()  
     
     // Read with one key  
     line.set("UBCONO", CONO)  
     
     int pageSize = mi.getMaxRecords() <= 0 ? 1000 : mi.getMaxRecords()           

     actionline.readAll(line, 1, pageSize, releasedLineProcessor)   
   
   } 
 

  //******************************************************************** 
  // List Order Delivery Lnes - main loop - ODLINE
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->   
  
  // Fields from ODHEAD to use in the other read
  company = line.get("UBCONO")
  deliveryNumber = line.get("UBDLIX") 
  invoiceNumber = line.get("UBIVNO") 
  invoiceNumberString = line.get("UBIVNO")  
  orderNumber = line.get("UBORNO")
  orderLine = line.get("UBPONR")
  orderLineSuffix = line.get("UBPOSX")
  paymentTerms = line.get("UBTEPY")
  warehouse = line.get("UBWHLO")
  itemNumber = line.get("UBITNO")
  orderLineString = line.get("UBPONR")
  orderLineSuffixString = line.get("UBPOSX")
  salesPrice = line.get("UBSAPR")
  netPrice = line.get("UBNEPR")
  lineAmount = line.get("UBLNAM")
  extInvoiceNumber = line.get("UBEXIN")  
  discount1 = line.get("UBDIP1")     
  discount2 = line.get("UBDIP2")     
  discount3 = line.get("UBDIP3")     
  discount4 = line.get("UBDIP4")     
  discount5 = line.get("UBDIP5")     
  discount6 = line.get("UBDIP6")    

  //Sum of discounts
  discountSum = 0                                                              
  discountSum = discount1 + discount2 + discount3 + discount5 + discount6      
  
  // Get Delivery Head Info 
  Optional<DBContainer> ODHEAD = findODHEAD(company, orderNumber, warehouse, deliveryNumber, paymentTerms)
  if(ODHEAD.isPresent()){
    // Record found, continue to get information  
    DBContainer containerODHEAD = ODHEAD.get() 
    
    companyString = containerODHEAD.get("UACONO") 
    division = containerODHEAD.get("UADIVI") 
    deliveryNumberString = containerODHEAD.get("UADLIX")  
    orderStatus = containerODHEAD.getString("UAORST")
    customer = containerODHEAD.getString("UACUNO")  
    currencyCode = containerODHEAD.getString("UACUCD")   
    paymentTerms = containerODHEAD.getString("UATEPY")  
    deliveryTerms = containerODHEAD.getString("UATEDL")  
    invoiceDate = String.valueOf(containerODHEAD.get("UAIVDT"))  
    year = String.valueOf(containerODHEAD.get("UAYEA4")) 
    invoicePrefix = containerODHEAD.get("UAINPX")                   
    payer = containerODHEAD.get("UAPYNO")                           

  } 
  
  
  if (orderStatus >= "70") {
        outCONO = String.valueOf(line.get("UBCONO"))
        outDLIX = String.valueOf(line.get("UBDLIX"))
        outIVNO = String.valueOf(line.get("UBIVNO")) 
        outITNO = String.valueOf(line.get("UBITNO"))  
        outLTYP = String.valueOf(line.get("UBLTYP"))
        outSPUN = String.valueOf(line.get("UBSPUN"))
        outCUNO = customer
        outCUCD = currencyCode 
        outTEPY = paymentTerms
        outORST = orderStatus
        outDLIX = deliveryNumber
        outSAPR = salesPrice
        outNEPR = netPrice
        outLNAM = lineAmount
        outTEDL = deliveryTerms
        outIVDT = invoiceDate
        outYEA4 = year
        outDISC = discountSum   
        outDISM = discount4     
        outPYNO = payer
        outWHLO = warehouse
        outEXIN = extInvoiceNumber
        
        // Find exchange rate
        Optional<String> tmpARAT = getCurrencyRate(outCONO, division, outCUCD, invoiceDate)
        if(tmpARAT.isPresent()) {
          outARAT = tmpARAT.get()
        } else {
          outARAT = ""
        }
        
        // Get Order Head Info 
        Optional<DBContainer> OOHEAD = findOOHEAD(company, orderNumber)
        if(OOHEAD.isPresent()){
          // Record found, continue to get information  
          DBContainer containerOOHEAD = OOHEAD.get() 
          outORDT = String.valueOf(containerOOHEAD.get("OAORDT"))  
          outFACI = containerOOHEAD.getString("OAFACI")  
          outORTP = containerOOHEAD.getString("OAORTP")   
          outWCON = containerOOHEAD.getString("OAWCON")   
          outORNO = containerOOHEAD.getString("OAORNO")  
          outAGNH = containerOOHEAD.getString("OAAGNO")         
          languageCode = containerOOHEAD.getString("OALNCD")   
        } else {
          outORDT = ""
          outFACI = ""
          outORTP = ""
          outWCON = ""
          outORNO = ""
          languageCode = ""
        } 

        // TEDL text
        Optional<DBContainer> CSYTAB = findCSYTAB(company, deliveryTerms, languageCode)
        if(CSYTAB.isPresent()){
          // Record found, continue to get information  
          DBContainer containerCSYTAB = CSYTAB.get() 
          outTEL1 = containerCSYTAB.getString("CTPARM")  
        } else {
          outTEL1 = ""
        } 

        // Get Order Line Info 
        Optional<DBContainer> OOLINE = findOOLINE(company, orderNumber, orderLine, orderLineSuffix)
        if(OOLINE.isPresent()){
          // Record found, continue to get information  
          DBContainer containerOOLINE = OOLINE.get() 
          outADID = containerOOLINE.getString("OBADID")  
          outRSCD = containerOOLINE.getString("OBRSCD")   
          outSMCD = containerOOLINE.getString("OBSMCD")   
          outOFNO = containerOOLINE.getString("OBOFNO")   
          outAGNO = containerOOLINE.getString("OBAGNO") 
          attributeNumber = containerOOLINE.get("OBATNR") 
          outPONR = containerOOLINE.get("OBPONR") 
          outUCA2 = containerOOLINE.getString("OBUCA2") 
          outUCA3 = containerOOLINE.getString("OBUCA3") 
        } else {
          outADID = ""
          outRSCD = ""
          outSMCD = ""
          outOFNO = ""
          outAGNO = ""
          attributeNumber = 0
          outPONR = 0
          outUCA2 = ""
          outUCA3 = ""
        }

     
        // Get Item information 
        Optional<DBContainer> MITMAS = findMITMAS(company, itemNumber)
        if(MITMAS.isPresent()){
          // Record found, continue to get information  
          DBContainer containerMITMAS = MITMAS.get() 
          outITDS = containerMITMAS.getString("MMITDS")   
          outHIE1 = containerMITMAS.getString("MMHIE1")   
          outHIE2 = containerMITMAS.getString("MMHIE2")   
          outHIE3 = containerMITMAS.getString("MMHIE3")  
          outHIE4 = containerMITMAS.getString("MMHIE4")  
          outHIE5 = containerMITMAS.getString("MMHIE5")  
        } else {
          outITDS = ""
          outHIE1 = ""
          outHIE2 = ""
          outHIE3 = ""
          outHIE4 = ""
          outHIE5 = ""
        }
        
        // Get Attribute information 
        Optional<DBContainer> MOATTR = findMOATTR(company, attributeNumber, "FOCUS", "0")
        if(MOATTR.isPresent()){
          // Record found, continue to get information  
          DBContainer containerMOATTR = MOATTR.get() 
          outATAV = containerMOATTR.getString("AHATAV")   
        } else {
          outATAV = ""
        }

        getDeliveryAddress(companyString, deliveryNumberString, "01")
        
        getAdditionalDelLineInfo(companyString, orderNumber, deliveryNumberString, warehouse, orderLineString, orderLineSuffixString, paymentTerms)
        
        invCharge = 0
        outCHGH = ""
        informationType = "65"
        getInvoiceCharges(companyString, division, year, invoiceNumberString, informationType, invoicePrefix, extInvoiceNumber)    
        outCHGH = invCharge
        
        invCharge = 0
        outCHGF = ""
        informationType = "60"
        getInvoiceCharges(companyString, division, year, invoiceNumberString, informationType, invoicePrefix, extInvoiceNumber)    
        if (invReference != null) {
          if (invReference.startsWith("F")) {
            outCHGF = invCharge
          }
        }
        
        invCharge = 0
        outCHGO = ""
        informationType = "60"
        getInvoiceCharges(companyString, division, year, invoiceNumberString, informationType, invoicePrefix, extInvoiceNumber)    
        if (invReference != null) {
          if (!invReference.startsWith("F")) {
            outCHGO = invCharge
          }
        }

        
        getDivisionInfo(companyString, division)
        
      // Send Output
      setOutput()
      mi.write() 

   }


  }
  
}
 