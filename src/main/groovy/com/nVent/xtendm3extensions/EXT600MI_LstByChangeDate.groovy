// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2021-06-12
// @version   1,0  
//
// Description 
// This API transacation LstByChangeDate is used to send data to PriceFX from M3 
//

//**************************************************************************** 
// Date    Version     Developer 
// 210612  1.0         Jessica Bjorklund, Columbus   New API transaction
// 220620  2.0         Jessica Bjorklund, Columbus   Added PYNO to output
//**************************************************************************** 


import java.math.RoundingMode 
import java.math.BigDecimal
import java.lang.Math
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


public class LstByChangeDate extends ExtendM3Transaction {
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
  public int inLMDT 
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


  // Constructor 
  public LstByChangeDate(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
      int inCONO = getCONO()  
      
      inCONO = mi.in.get("CONO")  
      inLMDT = mi.in.get("LMDT")  

      // Start the listing in CIDMAS
      lstInvoiceLines()
   
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
  // Get Sent Flag from EXTPFX
  //******************************************************************** 
  private Optional<DBContainer> findEXTPFX(Integer CONO, String ORNO, Integer PONR, Integer POSX, Integer DLIX, String WHLO, String TEPY){  
    DBAction query = database.table("EXTPFX").index("00").selection("EXEPFX").build()    
    def EXTPFX = query.getContainer()
    EXTPFX.set("EXCONO", CONO)
    EXTPFX.set("EXORNO", ORNO)
    EXTPFX.set("EXPONR", PONR)
    EXTPFX.set("EXPOSX", POSX)
    EXTPFX.set("EXWHLO", WHLO)
    EXTPFX.set("EXDLIX", DLIX)
    EXTPFX.set("EXTEPY", TEPY)
    
    if(query.read(EXTPFX))  { 
      return Optional.of(EXTPFX)
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
        def params = [CONO: companyString, DLIX: deliveryNumberString, ADRT: "01"] 
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

   //***************************************************************************** 
   // Update EXTPFX with a sent flag (field EPFX)
   // Key fields (same as ODLINE)
   // Company
   // Order Number
   // Order Line
   // Order Suffix
   // Delivery Number
   // Warehouse
   // Payment Terms
   //***************************************************************************** 
   void updEXTPFX(){ 
     DBAction action = database.table("EXTPFX").index("00").selectAllFields().build()
     DBContainer ext = action.getContainer()
      
     //Set key fields
     ext.set("EXCONO", company)
     ext.set("EXORNO", orderNumber)
     ext.set("EXPONR", orderLine)
     ext.set("EXPOSX", orderLineSuffix)
     ext.set("EXDLIX", deliveryNumber)
     ext.set("EXWHLO", warehouse)
     ext.set("EXTEPY", paymentTerms)
     
     // Read with lock
     action.readLock(ext, updateCallBack)
     }

    
     Closure<?> updateCallBack = { LockedResult lockedResult -> 
      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int changeNo = lockedResult.get("EXCHNO")
     int newChangeNo = changeNo + 1 
     
     // Update the sent flag
     lockedResult.set("EXEPFX", 1) 

     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("EXLMDT", changeddate)  
      
     lockedResult.set("EXCHNO", newChangeNo) 
     lockedResult.set("EXCHID", program.getUser())
     lockedResult.update()
  }


   //***************************************************************************** 
   // Add new record to EXTPFX with a sent flag (field EPFX)
   // Key fields (same as ODLINE)
   // Company
   // Order Number
   // Order Line
   // Order Suffix
   // Delivery Number
   // Warehouse
   // Payment Terms
   //***************************************************************************** 
   void addEXTPFX(){ 
     DBAction action = database.table("EXTPFX").index("00").selectAllFields().build()
     DBContainer ext = action.createContainer()
     
     //Set key fields
     ext.set("EXCONO", company)
     ext.set("EXORNO", orderNumber)
     ext.set("EXPONR", orderLine)
     ext.set("EXPOSX", orderLineSuffix)
     ext.set("EXDLIX", deliveryNumber)
     ext.set("EXWHLO", warehouse)
     ext.set("EXTEPY", paymentTerms)
     
     //Set flag
     ext.set("EXEPFX", 1);
     
     ext.set("EXDIVI", division)
   
     ext.set("EXCHID", program.getUser())
     ext.set("EXCHNO", 1) 
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     DateTimeFormatter format2 = DateTimeFormatter.ofPattern("HHmmss");  
     String formatTime = now.format(format2);        
        
     //Converting String into int using Integer.parseInt()
     int regdate=Integer.parseInt(formatDate); 
     int regtime=Integer.parseInt(formatTime); 
     ext.set("EXRGDT", regdate) 
     ext.set("EXLMDT", regdate) 
     ext.set("EXRGTM", regtime)
     action.insert(ext)  
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
  } 
    
  //******************************************************************** 
  // List all information
  //********************************************************************  
   void lstInvoiceLines(){   
     
     // List all Invoice Delivery Lines
     ExpressionFactory expression = database.getExpressionFactory("ODLINE")
   
     // Depending on input value (Change Date)
     expression = expression.eq("UBLMDT", String.valueOf(inLMDT))

     // List Invoice Delivery Lines   
     DBAction actionline = database.table("ODLINE").index("00").matching(expression).selection("UBCONO", "UBLMDT", "UBORNO", "UBPONR", "UBPOSX", "UBWHLO", "UBTEPY", "UBIVNO", "UBITNO", "UBLTYP", "UBSPUN", "UBSAPR", "UBNEPR", "UBLNAM", "UBDCOS", "UBDIP1", "UBDIP2", "UBDIP3", "UBDIP4", "UBDIP5", "UBDIP6", "UBEXIN", "UBINPX").build()     

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
  
  // Get Sent flag from EXTPFX 
  Optional<DBContainer> EXTPFX = findEXTPFX(company, orderNumber, orderLine, orderLineSuffix, deliveryNumber, warehouse, paymentTerms)
  if(EXTPFX.isPresent()){
    // Record found, continue to get information  
    DBContainer containerEXTPFX = EXTPFX.get() 
    
    sentFlag = containerEXTPFX.get("EXEPFX")  

  } 
 
  
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
        } else {
          outADID = ""
          outRSCD = ""
          outSMCD = ""
          outOFNO = ""
          outAGNO = ""
          attributeNumber = 0
          outPONR = 0
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
      
      // Get Delivery Head Info 
      Optional<DBContainer> EXTPFXrecord = findEXTPFX(company, orderNumber, orderLine, orderLineSuffix, deliveryNumber, warehouse, paymentTerms)
      if(EXTPFXrecord.isPresent()){
        updEXTPFX()
      } else {
        addEXTPFX()
      }
      
      mi.write() 

   }


  } 
}
 