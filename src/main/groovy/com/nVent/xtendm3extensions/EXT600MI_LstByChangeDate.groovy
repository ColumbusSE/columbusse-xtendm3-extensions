// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2021-06-12
// @version   1,0 
//
// Description 
// This API transacation LstByChangeDate is used to send data to PriceFX from M3
//

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
  public int Company 
  public String Division
  public int ChangeDate
  public int DeliveryNumber
  public int InvoiceNumber
  public String OrderNumber
  public String ItemNumber
  public int OrderLine
  public int OrderLineSuffix
  public String Warehouse
  public String PaymentTerms
  public String InvoiceDate
  public String DeliveryTerms
  public String Year
  public String Customer
  public String CurrencyCode
  public String LanguageCode
  public String CompanyString
  public String DeliveryNumberString
  public String OrderLineString
  public String OrderLineSuffixString
  public String OrderStatus
  public String SalesPrice
  public String NetPrice
  public String LineAmount
  public int SentFlag
  public int InCONO
  public int InLMDT 
  public long AttributeNumber

  // Definition of output fields
  public String OutIVNO
  public String OutIVDT
  public String OutCUNO 
  public String OutCUCD
  public String OutTEPY 
  public String OutITNO  
  public String OutITDS  
  public String OutLTYP  
  public String OutSPUN  
  public String OutQTY6  
  public String OutQTY4  
  public String OutDCOS
  public String OutSAPR
  public String OutNEPR
  public String OutLNAM
  public String OutHIE1
  public String OutHIE2
  public String OutHIE3
  public String OutHIE4
  public String OutHIE5
  public String OutFACI
  public String OutORTP
  public String OutADID
  public String OutORNO
  public String OutRSCD
  public String OutTEDL
  public String OutTEL1
  public String OutSMCD
  public String OutWCON
  public String OutORDT
  public String OutOFNO
  public String OutAGNO
  public String OutORST
  public String OutDLIX
  public String OutADRT
  public String OutCONO
  public String OutNAME
  public String OutTOWN
  public String OutCSCD
  public String OutPONO
  public String OutLOCD
  public String OutATAV
  public String OutYEA4


  // Constructor 
  public LstByChangeDate(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
     this.logger = logger; 
     this.miCaller = miCaller;
  } 
     
     
                
  //******************************************************************** 
  // Get Company from LDA
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
    DBAction query = database.table("ODHEAD").index("00").selection("UACONO", "UAORNO", "UACUNO", "UADLIX", "UAWHLO", "UAIVNO", "UAIVDT", "UACUNO", "UACUCD", "UATEPY", "UAORST", "UAYEA4", "UATEDL").build()    
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
   private getDeliveryAddress(String Company, String DeliveryNumber, String AddressType){   
        def params = [CONO: CompanyString, DLIX: DeliveryNumberString, ADRT: "01"] 
        String name = null
        String town = null
        String country = null
        String postalCode = null
        def callback = {
        Map<String, String> response ->
        logger.info("Response = ${response}")
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
      
        OutNAME = name
        OutTOWN = town
        OutCSCD = country
        OutPONO = postalCode
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
   private getAdditionalDelLineInfo(String Company, String OrderNumber, String DeliveryNumber, String Warehouse, String OrderLine, String OrderLineSuffix, String PaymentTerms){   
        def params = [CONO: CompanyString, ORNO: OrderNumber, DLIX: DeliveryNumberString, WHLO: Warehouse, PONR: OrderLine, POSX: OrderLineSuffix, TEPY: PaymentTerms] 
        String invQty = null
        String delQty = null
        String costAmount = null
        def callback = {
        Map<String, String> response ->
        logger.info("Response = ${response}")
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
      
        OutQTY4 = invQty
        OutQTY6 = delQty
        OutDCOS = costAmount
   } 

   //***************************************************************************** 
   // Get division related info from MNS150MI.GetBasicData
   // Input 
   // Company
   // Delivery Number
   //***************************************************************************** 
   private getDivisionInfo(String Company, String Division){   
        def params = [CONO: CompanyString, DIVI: Division] 
        String localCurrency = null
        def callback = {
        Map<String, String> response ->
        logger.info("Response = ${response}")
        if(response.LOCD != null){
          localCurrency = response.LOCD
        }
        }

        miCaller.call("MNS100MI","GetBasicData", params, callback)
      
        OutLOCD = localCurrency
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
   void UpdEXTPFX(){ 
     DBAction action = database.table("EXTPFX").index("00").selectAllFields().build()
     DBContainer ext = action.getContainer()
      
     //Set key fields
     ext.set("EXCONO", Company)
     ext.set("EXORNO", OrderNumber)
     ext.set("EXPONR", OrderLine)
     ext.set("EXPOSX", OrderLineSuffix)
     ext.set("EXDLIX", DeliveryNumber)
     ext.set("EXWHLO", Warehouse)
     ext.set("EXTEPY", PaymentTerms)
     
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
   void AddEXTPFX(){ 
     DBAction action = database.table("EXTPFX").index("00").selectAllFields().build()
     DBContainer ext = action.createContainer()
     
     //Set key fields
     ext.set("EXCONO", Company)
     ext.set("EXORNO", OrderNumber)
     ext.set("EXPONR", OrderLine)
     ext.set("EXPOSX", OrderLineSuffix)
     ext.set("EXDLIX", DeliveryNumber)
     ext.set("EXWHLO", Warehouse)
     ext.set("EXTEPY", PaymentTerms)
     
     //Set flag
     ext.set("EXEPFX", 1);
     
     ext.set("EXDIVI", Division)
   
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
  // Main 
  //********************************************************************  
  public void main() { 
      // Get LDA company of not entered 
      int InCONO = getCONO()  
      
      InCONO = mi.in.get("CONO")  
      InLMDT = mi.in.get("LMDT")  

      // Start the listing in CIDMAS
      LstInvoiceLines()
   
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
  void SetOutPut() {
    mi.outData.put("IVNO", OutIVNO)
    mi.outData.put("IVDT", OutIVDT)
    mi.outData.put("CUNO", OutCUNO)
    mi.outData.put("CUCD", OutCUCD)  
    mi.outData.put("TEPY", OutTEPY)  
    mi.outData.put("ITNO", OutITNO)  
    mi.outData.put("ITDS", OutITDS)  
    mi.outData.put("LTYP", OutLTYP)  
    mi.outData.put("SPUN", OutSPUN)  
    mi.outData.put("QTY6", OutQTY6)  
    mi.outData.put("QTY4", OutQTY4)  
    mi.outData.put("DCOS", OutDCOS)
    mi.outData.put("SAPR", OutSAPR)
    mi.outData.put("NEPR", OutNEPR)
    mi.outData.put("LNAM", OutLNAM)
    mi.outData.put("HIE1", OutHIE1)
    mi.outData.put("HIE2", OutHIE2)
    mi.outData.put("HIE3", OutHIE3)
    mi.outData.put("HIE4", OutHIE4)
    mi.outData.put("HIE5", OutHIE5)
    mi.outData.put("FACI", OutFACI)
    mi.outData.put("ORTP", OutORTP)
    mi.outData.put("ORNO", OutORNO)
    mi.outData.put("WCON", OutWCON)
    mi.outData.put("ADID", OutADID)
    mi.outData.put("RSCD", OutRSCD)
    mi.outData.put("SMCD", OutSMCD)
    mi.outData.put("OFNO", OutOFNO)
    mi.outData.put("ORDT", OutORDT)
    mi.outData.put("AGNO", OutAGNO)
    mi.outData.put("ORST", OutORST)
    mi.outData.put("NAME", OutNAME)
    mi.outData.put("TOWN", OutTOWN)
    mi.outData.put("CSCD", OutCSCD)
    mi.outData.put("PONO", OutPONO)
    mi.outData.put("LOCD", OutLOCD)
    mi.outData.put("DLIX", OutDLIX)
    mi.outData.put("ATVN", OutATAV)
    mi.outData.put("YEA4", OutYEA4)
    mi.outData.put("IVDT", OutIVDT)
    mi.outData.put("TEDL", OutTEDL)
    mi.outData.put("TEL1", OutTEL1)
  } 
    
  //******************************************************************** 
  // List all information
  //********************************************************************  
   void LstInvoiceLines(){   
     
     // List all Invoice Delivery Lines
     ExpressionFactory expression = database.getExpressionFactory("ODLINE")
   
     // Depending on input value (Change Date)
     expression = expression.eq("UBLMDT", String.valueOf(InLMDT))

     // List Invoice Delivery Lines   
     DBAction actionline = database.table("ODLINE").index("00").matching(expression).selection("UBCONO", "UBLMDT", "UBORNO", "UBPONR", "UBPOSX", "UBWHLO", "UBTEPY", "UBIVNO", "UBITNO", "UBLTYP", "UBSPUN", "UBSAPR", "UBNEPR", "UBLNAM", "UBDCOS").build()  

     DBContainer line = actionline.getContainer()  
     
     // Read with one key  
     line.set("UBCONO", CONO)  
     actionline.readAll(line, 1, releasedLineProcessor)   
   
   } 
 

  //******************************************************************** 
  // List Order Delivery Lnes - main loop - ODLINE
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->   
  
  // Fields from ODHEAD to use in the other read
  Company = line.get("UBCONO")
  Division = line.get("UBDIVI")
  DeliveryNumber = line.get("UBDLIX") 
  InvoiceNumber = line.get("UBIVNO") 
  OrderNumber = line.get("UBORNO")
  OrderLine = line.get("UBPONR")
  OrderLineSuffix = line.get("UBPOSX")
  PaymentTerms = line.get("UBTEPY")
  Warehouse = line.get("UBWHLO")
  ItemNumber = line.get("UBITNO")
  OrderLineString = line.get("UBPONR")
  OrderLineSuffixString = line.get("UBPOSX")
  SalesPrice = line.get("UBSAPR")
  NetPrice = line.get("UBNEPR")
  LineAmount = line.get("UBLNAM")
  
  
  // Get Sent flag from EXTPFX 
  Optional<DBContainer> EXTPFX = findEXTPFX(Company, OrderNumber, OrderLine, OrderLineSuffix, DeliveryNumber, Warehouse, PaymentTerms)
  if(EXTPFX.isPresent()){
    // Record found, continue to get information  
    DBContainer containerEXTPFX = EXTPFX.get() 
    
    SentFlag = containerEXTPFX.get("EXEPFX")  

  } 
 
  
  // Get Delivery Head Info 
  Optional<DBContainer> ODHEAD = findODHEAD(Company, OrderNumber, Warehouse, DeliveryNumber, PaymentTerms)
  if(ODHEAD.isPresent()){
    // Record found, continue to get information  
    DBContainer containerODHEAD = ODHEAD.get() 
    
    CompanyString = containerODHEAD.get("UACONO")  
    DeliveryNumberString = containerODHEAD.get("UADLIX")  
    OrderStatus = containerODHEAD.getString("UAORST")
    Customer = containerODHEAD.getString("UACUNO")  
    CurrencyCode = containerODHEAD.getString("UACUCD")   
    PaymentTerms = containerODHEAD.getString("UATEPY")  
    DeliveryTerms = containerODHEAD.getString("UATEDL")  
    InvoiceDate = String.valueOf(containerODHEAD.get("UAIVDT"))  
    Year = String.valueOf(containerODHEAD.get("UAYEA4"))  
    
  } 
  
  
  if (OrderStatus >= "70") {
        OutCONO = String.valueOf(line.get("UBCONO"))
        OutDLIX = String.valueOf(line.get("UBDLIX"))
        OutIVNO = String.valueOf(line.get("UBIVNO")) 
        OutITNO = String.valueOf(line.get("UBITNO"))  
        OutLTYP = String.valueOf(line.get("UBLTYP"))
        OutSPUN = String.valueOf(line.get("UBSPUN"))
        OutCUNO = Customer
        OutCUCD = CurrencyCode 
        OutTEPY = PaymentTerms
        OutORST = OrderStatus
        OutDLIX = DeliveryNumber
        OutSAPR = SalesPrice
        OutNEPR = NetPrice
        OutLNAM = LineAmount
        OutTEDL = DeliveryTerms
        OutIVDT = InvoiceDate
        OutYEA4 = Year
        
        // Get Order Head Info 
        Optional<DBContainer> OOHEAD = findOOHEAD(Company, OrderNumber)
        if(OOHEAD.isPresent()){
          // Record found, continue to get information  
          DBContainer containerOOHEAD = OOHEAD.get() 
          OutORDT = String.valueOf(containerOOHEAD.get("OAORDT"))  
          OutFACI = containerOOHEAD.getString("OAFACI")  
          OutORTP = containerOOHEAD.getString("OAORTP")   
          OutWCON = containerOOHEAD.getString("OAWCON")   
          OutORNO = containerOOHEAD.getString("OAORNO")   
          LanguageCode = containerOOHEAD.getString("OALNCD")   
        } else {
          OutORDT = ""
          OutFACI = ""
          OutORTP = ""
          OutWCON = ""
          OutORNO = ""
          LanguageCode = ""
        } 

        // TEDL text
        Optional<DBContainer> CSYTAB = findCSYTAB(Company, DeliveryTerms, LanguageCode)
        if(CSYTAB.isPresent()){
          // Record found, continue to get information  
          DBContainer containerCSYTAB = CSYTAB.get() 
          OutTEL1 = containerCSYTAB.getString("CTPARM")  
        } else {
          OutTEL1 = ""
        } 

        // Get Order Line Info 
        Optional<DBContainer> OOLINE = findOOLINE(Company, OrderNumber, OrderLine, OrderLineSuffix)
        if(OOLINE.isPresent()){
          // Record found, continue to get information  
          DBContainer containerOOLINE = OOLINE.get() 
          OutADID = containerOOLINE.getString("OBADID")  
          OutRSCD = containerOOLINE.getString("OBRSCD")   
          OutSMCD = containerOOLINE.getString("OBSMCD")   
          OutOFNO = containerOOLINE.getString("OBOFNO")   
          OutAGNO = containerOOLINE.getString("OBAGNO") 
          AttributeNumber = containerOOLINE.get("OBATNR") 
        } else {
          OutADID = ""
          OutRSCD = ""
          OutSMCD = ""
          OutOFNO = ""
          OutAGNO = ""
          AttributeNumber = 0
        }

     
        // Get Item information 
        Optional<DBContainer> MITMAS = findMITMAS(Company, ItemNumber)
        if(MITMAS.isPresent()){
          // Record found, continue to get information  
          DBContainer containerMITMAS = MITMAS.get() 
          OutITDS = containerMITMAS.getString("MMITDS")   
          OutHIE1 = containerMITMAS.getString("MMHIE1")   
          OutHIE2 = containerMITMAS.getString("MMHIE2")   
          OutHIE3 = containerMITMAS.getString("MMHIE3")  
          OutHIE4 = containerMITMAS.getString("MMHIE4")  
          OutHIE5 = containerMITMAS.getString("MMHIE5")  
        } else {
          OutITDS = ""
          OutHIE1 = ""
          OutHIE2 = ""
          OutHIE3 = ""
          OutHIE4 = ""
          OutHIE5 = ""
        }
        
        // Get Attribute information 
        Optional<DBContainer> MOATTR = findMOATTR(Company, AttributeNumber, "FOCUS", "0")
        if(MOATTR.isPresent()){
          // Record found, continue to get information  
          DBContainer containerMOATTR = MOATTR.get() 
          OutATAV = containerMOATTR.getString("AHATAV")   
        } else {
          OutATAV = ""
        }

        getDeliveryAddress(CompanyString, DeliveryNumberString, "01")
        
        getAdditionalDelLineInfo(CompanyString, OrderNumber, DeliveryNumberString, Warehouse, OrderLineString, OrderLineSuffixString, PaymentTerms)
        
        getDivisionInfo(CompanyString, Division)
        
      // Send Output
      SetOutPut()
      
      // Get Delivery Head Info 
      Optional<DBContainer> EXTPFXrecord = findEXTPFX(Company, OrderNumber, OrderLine, OrderLineSuffix, DeliveryNumber, Warehouse, PaymentTerms)
      if(EXTPFXrecord.isPresent()){
        UpdEXTPFX()
      } else {
        AddEXTPFX()
      }
      
      mi.write() 

   }


  } 
}
 