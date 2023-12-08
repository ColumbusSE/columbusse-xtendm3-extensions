
// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-08
// @version   1.0 
//
// Description 
// This API transacation LstPOInfo2 is used to send PO data to ESKAR from M3 
//

import java.math.RoundingMode 
import java.math.BigDecimal
import java.lang.Math


public class LstPOInfo2 extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger  
  private final MICallerAPI miCaller
  
  // Definition 
  public Integer company  
  public String inCONO
  public String inPUNO 
  public int inPNLI 
  public int inPNLS 
  public String division
  public String supplier
  public String buyer 
  public int recNumber 
  public double lineORQA 
  public double lineIVQA 
  public double lineRVQA   
  public double lineLNAM
  public double invLineIVNA 
  public String linePUUN 
  public double discount1 
  public double discount2  
  public double discount3 
  public double confirmedDiscount1 
  public double confirmedDiscount2 
  public double confirmedDiscount3 
  public double confirmedPrice  
  public double totalConfirmedDiscount
  public double totalDiscount
  public double purchasePrice
  public double linePrice
  public double lineAmount  
  public double lineQty  
  public double invLineRCAC  
  public double invLineSERA  
  public double invLineIVQT  
  public double invlineICAC
  public double accRecRepQty
  public double accRecCostAmount 
  public double accRecInvAmount
  public double accRecExcRate  
  public double accRecQty  
  public double unitCOFA  
  public int unitDMCF 
  public double accInvQty  
  public double accInvAmount 
  public String CC_CountryCode
  public String ID_CountryCode
  public double recCostAmount 
  public double recExcRate 
  public double calcCOFA1   
  public double calcDMCF1   
  public double calcCOFA2   
  public double calcDMCF2 
  public double calcFACP
  public double calcFACO
  public double calcFACT
  public String linePPUN  
  public double invLineRPQA
  public String MPLINDlinePUOS
  public double MPLINDlineRPQA
  public int completeFlag  
  public double lineDelAmount
  public double invHeadRPQA
  public double sumInvHeadRPQA
  public double headORQA
  public double headRVQA
  public double headLinePrice 
  public double headLineAmount 
  public double headLineFactor
  public double headPrice
  public double headConfirmedPrice
  public double headPurchasePrice
  public String headPUUN
  public String headPPUN
  public String headITNO
  public double headDiscount1
  public double headDiscount2
  public double headDiscount3
  public double headConfirmedDiscount1
  public double headConfirmedDiscount2
  public double headConfirmedDiscount3
  public double sumheadOrderedAmount
  public double sumheadDeliveredAmount
  public double sumHeadLineAmount
  public double headOrderedAmount
  public double headDeliveredAmount
  public double unitPrice
  public String status
  public Integer statusInt
  public Integer highestStatus
  public double repQty

    
  // Definition of output fields
  public String outLPUD
  public String outPNLI  
  public String outPNLS  
  public String outITNO  
  public String outLNAM
  public String outPITD  
  public String outORQT 
  public String outRVQT  
  public String outCONO 
  public String outDIVI
  public String outPUNO
  public String outSUNO   
  public String outPUDT 
  public String outEMAL
  public String outORIG
  public String outUNPR
  public String outDEAH
  public String outGRAM
  public String outDEAL 
  public String outSUDO 
  public String outRPQT
  public String outTRDT  
  public String outREPN 
  public String outIVQT
  public String outCOMP
  public String outGRIQ
  public String outGRIA
  public String outTIVA
  public String outIVNA
  public String outFACT  
  public String outVTCD
  public String outFACO
  public String outFACP
  
  
  // Constructor 
  public LstPOInfo2(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger 
     this.miCaller = miCaller
  } 
 

  //******************************************************************** 
  // Main 
  //********************************************************************  
  public void main() { 
    
    // Validate company
    company = mi.in.get("CONO")      
    if (company == null) {
      company = program.LDAZD.CONO as Integer
    } 
    inCONO = String.valueOf(company)
    inPUNO = mi.in.get("PUNO")
    inPNLI = mi.in.get("PNLI")
    inPNLS = mi.in.get("PNLS")
    

    clearOutput()
    lineORQA = 0d
    lineIVQA = 0d
    lineRVQA = 0d
    linePUUN = ""
    linePPUN = ""
    lineLNAM = 0d
    discount1 = 0d
    discount2 = 0d
    discount3 = 0d
    totalDiscount = 0d
    confirmedDiscount1 = 0d
    confirmedDiscount2 = 0d
    confirmedDiscount3 = 0d
    totalConfirmedDiscount = 0d
    confirmedPrice = 0d
    purchasePrice = 0d
    linePrice = 0d
    lineAmount = 0d
    unitPrice = 0d



    //***********************************************************************
    // Get Purchase order line
    //***********************************************************************
    Optional<DBContainer> MPLINE = findMPLINE(company, inPUNO, inPNLI, inPNLS)
    if (MPLINE.isPresent()) {
      // Record found, continue to get information  
      DBContainer containerMPLINE = MPLINE.get() 

      // Output selectAllFields 
      outPUNO = inPUNO
      outLPUD = containerMPLINE.get("IBLPUD")
      outPNLI = containerMPLINE.get("IBPNLI")
      outPNLS = containerMPLINE.get("IBPNLS") 
      outITNO = containerMPLINE.get("IBITNO")
      outPITD = containerMPLINE.get("IBPITD")
      outVTCD = containerMPLINE.get("IBVTCD")
      
      // Fields for calculation
      lineORQA = containerMPLINE.get("IBCFQA")
      if (lineORQA > 0) {
      } else {
         lineORQA = containerMPLINE.get("IBORQA")
      }
      lineIVQA = containerMPLINE.get("IBIVQA")
      lineRVQA = containerMPLINE.get("IBRVQA")
      linePUUN = containerMPLINE.get("IBPUUN")
      linePPUN = containerMPLINE.get("IBPPUN")
      lineLNAM = containerMPLINE.get("IBLNAM")
      
      discount1 = containerMPLINE.get("IBODI1")
      discount2 = containerMPLINE.get("IBODI2")
      discount3 = containerMPLINE.get("IBODI3")
      confirmedDiscount1 = containerMPLINE.get("IBCFD1")
      confirmedDiscount2 = containerMPLINE.get("IBCFD2")
      confirmedDiscount3 = containerMPLINE.get("IBCFD3")
      confirmedPrice = containerMPLINE.get("IBCPPR")
      purchasePrice = containerMPLINE.get("IBPUPR")

      //Calc the price
      linePrice = calcPrice(company, discount1, discount2, discount3, confirmedDiscount1, confirmedDiscount2, confirmedDiscount3, confirmedPrice, purchasePrice)
      
      lineAmount = linePrice * lineORQA

      //Get UoM factors
      getUoMFactors(company, outITNO, linePPUN, linePUUN)
      outFACP = String.valueOf(calcFACP)
      outFACO = String.valueOf(calcFACO)
      outFACT = String.valueOf(calcFACT)
        
	    outORQT = lineORQA
	    outRVQT = lineRVQA

      unitPrice = linePrice * calcFACT
      BigDecimal unitPriceRounded  = BigDecimal.valueOf(unitPrice) 
      unitPriceRounded = unitPriceRounded.setScale(4, RoundingMode.HALF_UP) 
      unitPrice = unitPriceRounded  

      outUNPR = String.valueOf(unitPrice) 

      //L Ordered Amount
      double lineOrderedAmount = lineORQA * (linePrice * calcFACT)
      BigDecimal lineOrderedAmountRounded  = BigDecimal.valueOf(lineOrderedAmount) 
      lineOrderedAmountRounded = lineOrderedAmountRounded.setScale(2, RoundingMode.HALF_UP) 
      lineOrderedAmount = lineOrderedAmountRounded  
      outLNAM = lineOrderedAmount

      //L Delivered Amount
      double lineDeliveredAmount = lineRVQA * (linePrice * calcFACT)
      BigDecimal lineDeliveredAmountRounded  = BigDecimal.valueOf(lineDeliveredAmount) 
      lineDeliveredAmountRounded = lineDeliveredAmountRounded.setScale(2, RoundingMode.HALF_UP) 
      lineDeliveredAmount = lineDeliveredAmountRounded  
      outDEAL = lineDeliveredAmount

      outSUDO = ""  
      outRPQT = ""  
      outTRDT = ""  
      outREPN = ""  
      outGRIQ = ""  
      outGRAM = ""  
      outCOMP = ""  

      // Get Purchase order head
      Optional<DBContainer> MPHEAD = findMPHEAD(company, inPUNO)
      if (MPHEAD.isPresent()) {
         // Record found, continue to get information  
        DBContainer containerMPHEAD = MPHEAD.get()  
        
        // output fields   
        outCONO = String.valueOf(containerMPHEAD.get("IACONO"))
        outDIVI = containerMPHEAD.getString("IADIVI")
        outPUNO = containerMPHEAD.getString("IAPUNO")
        outSUNO = containerMPHEAD.getString("IASUNO")   
        outPUDT = String.valueOf(containerMPHEAD.get("IAPUDT"))
  
        // Fields from MPHEAD to use in the other read
        division = containerMPHEAD.getString("IADIVI")  
        supplier = containerMPHEAD.getString("IASUNO")  
        buyer = containerMPHEAD.getString("IABUYE")   
       
        // Get Email address for Buyer
        Optional<DBContainer> CEMAIL = findCEMAIL(company, buyer)
        if(CEMAIL.isPresent()){
          // Record found, continue to get information  
          DBContainer containerCEMAIL = CEMAIL.get()    
          outEMAL = containerCEMAIL.getString("CBEMAL")
        } 
      
        // Get Supplier information 
        Optional<DBContainer> CIDMAS = findCIDMAS(company, supplier)
        if(CIDMAS.isPresent()){
          // Record found, continue to get information  
          DBContainer containerCIDMAS = CIDMAS.get() 
          ID_CountryCode = containerCIDMAS.getString("IDCSCD")   
        }  
         
        // Get division information
        Optional<DBContainer> CMNDIV = findCMNDIV(company, division)
        if(CMNDIV.isPresent()){
          // Record found, continue to get information  
          DBContainer containerCMNDIV = CMNDIV.get() 
          CC_CountryCode = containerCMNDIV.getString("CCCSCD")   
        } 
         
        // Compare division's country code and the Supplier's 
        if(CC_CountryCode != ID_CountryCode){ 
          outORIG = String.valueOf("FOR")
        }else{ 
          outORIG = String.valueOf("DOM")
        } 
      }


      // Get information from receiving invoice lines - sum per PO line level
      outTIVA = ""  
      outIVQT = ""
      invLineIVNA = 0d 
      accInvQty = 0d 
      invLineRPQA = 0d
      accInvAmount = 0d 
      invLineIVQT = 0d
      List<DBContainer> ResultFGINLIline = listFGINLIline(company, inPUNO, inPNLI, inPNLS) 
      for (DBContainer InvLineLine : ResultFGINLIline){
	        invLineIVQT = InvLineLine.get("F5IVQA") 
          invLineIVNA = InvLineLine.get("F5IVNA") 
          accInvQty = accInvQty + invLineIVQT 
          accInvAmount = accInvAmount + invLineIVNA 
      }
      outTIVA = String.valueOf(accInvAmount) 
      outIVQT = String.valueOf(accInvQty)
    }

      // Get information from receiving lines
      outSUDO = ""  
      outRPQT = ""  
      outTRDT = ""  
      outREPN = ""  
      outGRIQ = ""  
      outGRAM = ""  
      outCOMP = ""  

      List<DBContainer> ResultFGRECLline = listFGRECLline(company, division, inPUNO, inPNLI, inPNLS) 
      for (DBContainer RecLineLine : ResultFGRECLline){
          int receivingNumber = RecLineLine.get("F2REPN")  

          // Accumulate quantity   
          invLineRCAC = RecLineLine.get("F2RCAC") 
          invLineSERA = RecLineLine.get("F2SERA") 
          invLineIVQT = RecLineLine.get("F2IVQA")  
          invLineRPQA = RecLineLine.get("F2RPQA")   
          invlineICAC = RecLineLine.get("F2ICAC")   

          accRecCostAmount = accRecCostAmount + invLineRCAC   
          accRecInvAmount = accRecInvAmount + invlineICAC
          accRecExcRate = invLineSERA    
          accRecRepQty = accRecRepQty + invLineRPQA
          accRecQty = accRecQty + (invLineIVQT * lineAmount)  
          
          double recLineGRAmount = accRecRepQty * (linePrice * calcFACT)
          BigDecimal recLineGRAmountRounded  = BigDecimal.valueOf(recLineGRAmount) 
          recLineGRAmountRounded = recLineGRAmountRounded.setScale(2, RoundingMode.HALF_UP) 
          recLineGRAmount = recLineGRAmountRounded  

          //Get receiving info
          MPLINDlinePUOS = 0
          MPLINDlineRPQA = 0d
          List<DBContainer> ResultMPLIND = listMPLIND(company, inPUNO, inPNLI, inPNLS, receivingNumber, "0") 
          for (DBContainer TransLine : ResultMPLIND){
              MPLINDlinePUOS = TransLine.get("ICPUOS")   
              MPLINDlineRPQA = TransLine.get("ICRPQA")
              completeFlag = TransLine.get("ICOEND")

              if(completeFlag == 1){ 
                 outCOMP = "Yes" 
              }else{ 
                 outCOMP = "No" 
              } 
          }

          repQty = 0d
          checkMPLINDstatus(company, inPUNO, inPNLI, inPNLS, receivingNumber) 
          
          MPLINDlinePUOS = 0
          MPLINDlineRPQA = 0d
          if (highestStatus > 60) {
            List<DBContainer> ResultMPLINDRep64 = listMPLIND(company, inPUNO, inPNLI, inPNLS, receivingNumber, "64") 
            for (DBContainer TransLine : ResultMPLINDRep64){
                MPLINDlineRPQA = TransLine.get("ICRPQA")
            }
          } else {
               MPLINDlineRPQA = repQty
          }

          // Output   
          outSUDO = String.valueOf(RecLineLine.get("F2SUDO")) 
          double CAWE =  RecLineLine.get("F2CAWE")
          double RPQT =  RecLineLine.get("F2RPQA")
          if (CAWE != 0) {
             RPQT = CAWE
          }
          if (highestStatus > 60) {
            RPQT = RPQT - MPLINDlineRPQA
          } else if (highestStatus == 60) {
            RPQT = MPLINDlineRPQA
          } else {
            RPQT = 0d
          }
          
          outRPQT = String.valueOf(RPQT) 

          double receivedAmount = 0d
          
          receivedAmount = RPQT * unitPrice
          BigDecimal receivedAmountRounded  = BigDecimal.valueOf(receivedAmount) 
          receivedAmountRounded = receivedAmountRounded.setScale(2, RoundingMode.HALF_UP) 
          receivedAmount = receivedAmountRounded  

          outGRAM = String.valueOf(receivedAmount)
          outTRDT = String.valueOf(RecLineLine.get("F2TRDT"))  
          outREPN = String.valueOf(RecLineLine.get("F2REPN")) 
          outGRIQ = String.valueOf(RecLineLine.get("F2IVQA")) 
          double GRIQ =  RecLineLine.get("F2IVQA")
          outGRIA = String.valueOf(GRIQ * unitPrice)
          setOutput()
          mi.write()   

      } 

  } 
  
  //*******************************************************************************************
  // Calc the price
  //********************************************************************************************
  private double calcPrice(int CONO, double ODI1, double ODI2, double ODI3, double CFD1, double CFD2, double CFD3, double CPPR, double PUPR) { 
      totalDiscount = ODI1 + ODI2 + ODI3
      totalConfirmedDiscount = CFD1 + CFD2 + CFD3

      if (CPPR > 0) {
         linePrice = (CPPR * (100 - totalConfirmedDiscount))/100
      } else {
         linePrice = (PUPR * (100 - totalDiscount))/100
      }
      
      return linePrice
  }
  
  
  //**************************************************************************************** 
  // Get and set Unit of Measure Factors 
  //*****************************************************************************************
  void getUoMFactors(int CONO, String ITNO, String PPUN, String PUUN) {

      calcFACP = 0d
      calcFACO = 0d
      calcFACT = 0d
      
      // Calculate with Unit of measure factor   
      // Get COFA and DMCF from PPUN Price UoM            
      Optional<DBContainer> MITAUN1 = findMITAUN(company, outITNO, 2, linePPUN)
      if(MITAUN1.isPresent()){
        // Record found, continue to get information  
        DBContainer containerMITAUN1 = MITAUN1.get() 
        calcCOFA1 = containerMITAUN1.get("MUCOFA")
        calcDMCF1 = containerMITAUN1.get("MUDMCF") 
      } else { 
        calcCOFA1 = 1
        calcDMCF1 = 1
      }
      if (calcDMCF1 == 1) {
        calcFACP = 1/calcCOFA1
      } else {
        calcFACP = calcCOFA1
      }

      // Get COFA and DMCF from PUUN Qty UoM           
      Optional<DBContainer> MITAUN2 = findMITAUN(company, outITNO, 2, linePUUN)
      if(MITAUN2.isPresent()){
        // Record found, continue to get information  
        DBContainer containerMITAUN2 = MITAUN2.get() 
          calcCOFA2 = containerMITAUN2.get("MUCOFA")
          calcDMCF2 = containerMITAUN2.get("MUDMCF") 
        } else { 
          calcCOFA2 = 1
          calcDMCF2 = 1
        }
        if (calcDMCF2 == 1) {
          calcFACO = calcCOFA2
        } else {
          calcFACO = 1/calcCOFA2
        }
        calcFACT = calcFACP * calcFACO
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
  // Get Division information CMNDIV
  //******************************************************************** 
  private Optional<DBContainer> findCMNDIV(Integer CONO, String DIVI){  
    DBAction query = database.table("CMNDIV").index("00").selection("CCCONO", "CCCSCD", "CCDIVI").build()
    def CMNDIV = query.getContainer()
    CMNDIV.set("CCCONO", CONO)
    CMNDIV.set("CCDIVI", DIVI)
    if(query.read(CMNDIV))  { 
      return Optional.of(CMNDIV)
    } 
  
    return Optional.empty()
  }
   
   
  //******************************************************************** 
  // Get Division information MPLINE
  //******************************************************************** 
  private Optional<DBContainer> findMPLINE(Integer CONO, String PUNO, Integer PNLI, Integer PNLS){  
    DBAction query = database.table("MPLINE").index("00").selection("IBPUPR", "IBCONO", "IBPUNO", "IBPNLI", "IBPNLS", "IBITNO", "IBLPUD", "IBLNAM", "IBPITD", "IBVTCD", "IBORQA", "IBIVQA", "IBRVQA", "IBPUUN", "IBPPUN", "IBODI1", "IBODI2", "IBODI3", "IBCFD1", "IBCFD2", "IBCFD3", "IBCPPR").build()    
    def MPLINE = query.getContainer()
    MPLINE.set("IBCONO", CONO)
    MPLINE.set("IBPUNO", PUNO)
    MPLINE.set("IBPNLI", PNLI)
    MPLINE.set("IBPNLS", PNLS)
    if(query.read(MPLINE))  { 
      return Optional.of(MPLINE)
    } 
  
    return Optional.empty()
  } 


  //******************************************************************** 
  // Get information from MPHEAD
  //******************************************************************** 
  private Optional<DBContainer> findMPHEAD(Integer CONO, String PUNO){  
    DBAction query = database.table("MPHEAD").index("00").selection("IACONO", "IAPUNO", "IADIVI", "IASUNO", "IANTAM", "IAPUDT", "IABUYE", "IACOAM").build()    
    def MPHEAD = query.getContainer()
    MPHEAD.set("IACONO", CONO)
    MPHEAD.set("IAPUNO", PUNO)
    if(query.read(MPHEAD))  { 
      return Optional.of(MPHEAD)
    } 
  
    return Optional.empty()
  } 
  
  
  //******************************************************************** 
  // Accumulate value from FGINLI - PO line level
  //******************************************************************** 
  private List<DBContainer> listFGINLIline(Integer CONO, String PUNO, Integer PNLI, Integer PNLS){ 
    List<DBContainer>InvLine = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("FGINLI")
    DBAction query = database.table("FGINLI").index("20").selection("F5IVQA", "F5IVNA").build() 
    DBContainer FGINLIline = query.getContainer() 
    FGINLIline.set("F5CONO", CONO)   
    FGINLIline.set("F5PUNO", PUNO) 
    FGINLIline.set("F5PNLI", PNLI) 
    FGINLIline.set("F5PNLS", PNLS) 

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(FGINLIline, 3, pageSize, { DBContainer record ->  
     InvLine.add(record) 
    })

    return InvLine
  } 


  //******************************************************************** 
  // Get record from MPLIND
  //******************************************************************** 
  private List<DBContainer> listMPLIND(int CONO, String PUNO, int PNLI, int PNLS, int REPN, String PUOS){ 
    List<DBContainer>TransLine = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("MPLIND")
    expression = expression.eq("ICPUNO", PUNO).and(expression.eq("ICCONO", String.valueOf(CONO))).and(expression.eq("ICPNLI", String.valueOf(inPNLI))).and(expression.eq("ICPNLS", String.valueOf(inPNLS))).and(expression.eq("ICREPN", String.valueOf(REPN)))    
    DBAction query = database.table("MPLIND").index("60").selection("ICOEND", "ICRPQA", "ICPUOS").build() 
    DBContainer MPLIND = query.getContainer() 
    MPLIND.set("ICCONO", CONO)   
    MPLIND.set("ICPUNO", PUNO) 
    MPLIND.set("ICPNLI", PNLI) 
    MPLIND.set("ICPNLS", PNLS) 
    MPLIND.set("ICREPN", REPN) 
    MPLIND.set("ICPUOS", PUOS)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    if (PUOS.equals("0")) {
       query.readAll(MPLIND, 5, pageSize, { DBContainer record ->  
       TransLine.add(record) 
      })
    } else {
       query.readAll(MPLIND, 6, pageSize, { DBContainer record ->  
       TransLine.add(record) 
      })
    }
    
    return TransLine
  } 
   

   //******************************************************************** 
   // Check highest status in MPLIND
   //********************************************************************  
   void checkMPLINDstatus(int CONO, String PUNO, int PNLI, int PNLS, int REPN){ 
     
     highestStatus = 0

     //Read all to check highest status
     DBAction queryMPLIND = database.table("MPLIND").index("60").selection("ICOEND", "ICRPQA", "ICPUOS").build() 
     DBContainer containerMPLIND = queryMPLIND.getContainer()
     containerMPLIND.set("ICCONO", CONO)
     containerMPLIND.set("ICPUNO", PUNO)
     containerMPLIND.set("ICPNLI", PNLI)
     containerMPLIND.set("ICPNLS", PNLS)
     containerMPLIND.set("ICREPN", REPN)

     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     queryMPLIND.readAll(containerMPLIND, 5, pageSize, releasedLineProcessorMPLIND)
   } 

    
  //******************************************************************** 
  // Check status - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessorMPLIND = { DBContainer containerMPLIND -> 
      status = containerMPLIND.get("ICPUOS") 
      statusInt = Integer.valueOf(status)
      repQty = containerMPLIND.get("ICRPQA") 

      if (statusInt > highestStatus) {
         highestStatus = statusInt
      }
  }

   
  //******************************************************************** 
  // Accumulate value from FGRECL - PO Line level
  //********************************************************************  
  private List<DBContainer> listFGRECLline(int CONO, String DIVI, String PUNO, int PNLI, int PNLS){
    List<DBContainer>RecLineLine = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("FGRECL")
    expression = expression.ne("F2RPQA", String.valueOf(0)) 
    def query = database.table("FGRECL").index("00").matching(expression).selection("F2REPN", "F2RCAC", "F2SERA", "F2IVQA", "F2RPQA", "F2ICAC", "F2SUDO", "F2CAWE", "F2TRDT").build()
    def FGRECLline = query.createContainer()
    FGRECLline.set("F2CONO", CONO)
    FGRECLline.set("F2DIVI", DIVI)
    FGRECLline.set("F2PUNO", PUNO)
    FGRECLline.set("F2PNLI", PNLI)
    FGRECLline.set("F2PNLS", PNLS) 

	  int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(FGRECLline, 4, pageSize, { DBContainer record ->  
      RecLineLine.add(record.createCopy()) 
    })

    return RecLineLine
  }
  
  
  
  //******************************************************************** 
  // Get Supplier information CIDMAS
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
  // Get Email address CEMAIL
  //******************************************************************** 
  private Optional<DBContainer> findCEMAIL(Integer CONO, String BUYE){   
    DBAction query = database.table("CEMAIL").index("00").selection("CBEMAL").build()
    def CEMAIL = query.getContainer()
    CEMAIL.set("CBCONO", CONO)
    CEMAIL.set("CBEMTP", "04")
    CEMAIL.set("CBEMKY", BUYE)
    if(query.read(CEMAIL))  { 
      return Optional.of(CEMAIL)
    } 
    
    return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Get Alternativ unit MITAUN
  //******************************************************************** 
   private Optional<DBContainer> findMITAUN(Integer CONO, String ITNO, Integer AUTP, String ALUN){  
    DBAction query = database.table("MITAUN").index("00").selection("MUCOFA", "MUDMCF", "MUAUTP", "MUALUN").build()
    def MITAUN = query.getContainer()
    MITAUN.set("MUCONO", CONO)
    MITAUN.set("MUITNO", ITNO)
    MITAUN.set("MUAUTP", AUTP)
    MITAUN.set("MUALUN", ALUN)
    if(query.read(MITAUN))  { 
      return Optional.of(MITAUN)
    } 
    
    return Optional.empty()
  }   
  

  //******************************************************************** 
  // Clear Output data
  //******************************************************************** 
  void clearOutput() {
    outCONO = ""
    outDIVI = ""
    outPUNO = ""
    outSUNO = ""
    outLPUD = ""
    outPNLI = ""  
    outPNLS = "" 
    outITNO = "" 
    outLNAM = ""
    outPITD = "" 
    outORQT = ""
    outIVQT = ""
    outRVQT = ""    
    outPUDT = ""
    outEMAL = "" 
    outORIG = ""
    outUNPR = ""
    outDEAH = ""
    outGRAM = ""
    outDEAL = ""
    outSUDO = ""
    outRPQT = ""
    outTRDT = ""
    outREPN = ""
    outGRIQ = ""
    outCOMP = ""
    outGRIA = ""
    outTIVA = ""
    outIVNA = ""
    outFACT = ""
    outVTCD = ""   
    outFACO = ""
    outFACP = ""
  }
  
  
  //******************************************************************** 
  // Set Output data
  //******************************************************************** 
  void setOutput() {
    mi.outData.put("CONO", outCONO)  
    mi.outData.put("DIVI", outDIVI)
    mi.outData.put("PUNO", outPUNO)
    mi.outData.put("SUNO", outSUNO)
    mi.outData.put("LPUD", outLPUD)
    mi.outData.put("PNLI", outPNLI)  
    mi.outData.put("PNLS", outPNLS)  
    mi.outData.put("ITNO", outITNO)  
    mi.outData.put("LNAM", outLNAM)
    mi.outData.put("PITD", outPITD)  
    mi.outData.put("ORQT", outORQT)
    mi.outData.put("IVQT", outIVQT)
    mi.outData.put("RVQT", outRVQT)    
    mi.outData.put("PUDT", outPUDT) 
    mi.outData.put("EMAL", outEMAL) 
    mi.outData.put("ORIG", outORIG)
    mi.outData.put("UNPR", outUNPR)
    mi.outData.put("GRAM", outGRAM)
    mi.outData.put("DEAL", outDEAL) 
    mi.outData.put("SUDO", outSUDO) 
    mi.outData.put("RPQT", outRPQT) 
    mi.outData.put("TRDT", outTRDT)  
    mi.outData.put("REPN", outREPN) 
    mi.outData.put("GRIQ", outGRIQ)
    mi.outData.put("COMP", outCOMP)
    mi.outData.put("GRIA", outGRIA)
    mi.outData.put("TIVA", outTIVA)
    mi.outData.put("FACT", outFACT) 
    mi.outData.put("VTCD", outVTCD)    
    mi.outData.put("FACO", outFACO) 
    mi.outData.put("FACP", outFACP) 
  } 
    
}  