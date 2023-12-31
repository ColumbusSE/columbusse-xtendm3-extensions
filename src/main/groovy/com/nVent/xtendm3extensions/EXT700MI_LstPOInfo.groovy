
// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2022-06-22
// @version   1.0 
//
// Description 
// This API transacation LstPOInfo is used to send PO data to ESKAR from M3 
//

import java.math.RoundingMode 
import java.math.BigDecimal
import java.lang.Math


public class LstPOInfo extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger  
  private final MICallerAPI miCaller
  
  // Definition 
  public Integer company  
  public String inPUNO 
  public int inPNLI 
  public int inPNLS 
  public String division
  public String supplier
  public String buyer 
  public int recNumber 
  public BigDecimal orderQtyBaseUnit 
  public double orderQtyBaseUnitRounded
  public BigDecimal invQtyBaseUnit 
  public double invQtyBaseUnitRounded
  public BigDecimal recQtyBaseUnit 
  public double recQtyBaseUnitRounded
  public double lineORQA 
  public double lineIVQA 
  public double lineRVQA   
  public double invLineIVNA 
  public String linePUUN 
  public double discount1 
  public double discount2  
  public double discount3 
  public double confirmedDiscount1 
  public double confirmedDiscount2 
  public double confirmedDiscount3 
  public double confirmedPrice  
  public double purchasePrice
  public double totalDiscount
  public double totalConfirmedDiscount
  public double lineAmount  
  public double lineQty  
  public double resultDiscount 
  public double resultConfirmedDiscount 
  public BigDecimal linePrice
  public double linePriceRounded
  public double invLineRCAC  
  public double invLineSERA  
  public double invLineIVQT  
  public double accRecCostAmount 
  public double accRecExcRate  
  public double accRecQty  
  public double accResult 
  public double unitCOFA  
  public double reportedQty
  public int unitDMCF 
  public double accInvQty  
  public double accInvAmount 
  public int regDate
  public String purchaseOrder
  public String inRegDate
  public boolean alreadySentOut
  public int countFGRECL  
  public String CC_CountryCode
  public String ID_CountryCode
  public double recCostAmount 
  public double recExcRate 
  public double result 
  public double calcCOFA1   
  public double calcDMCF1   
  public double calcCOFA2   
  public double calcDMCF2   
  public double resultFACT1   
  public double resultFACT2   
  public double resultFACTTotal   
  public String linePPUN  
  public String MPLINDlinePUOS
  public double MPLINDlineRPQA
  public int completeFlag  
  public int receivingNumberREPN
  public double calcFACP
  public double calcFACO
  public double unitPrice
  public double confQty
  public double confPrice
  public double confQtySum
  public double confPriceSum

    
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
  public String outNTAM  
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
  public String outFACP
  public String outFACO
  
  
  // Constructor 
  public LstPOInfo(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
  
      inPUNO = mi.in.get("PUNO")
      inPNLI = mi.in.get("PNLI")
      inPNLS = mi.in.get("PNLS")
      
      // Get Purchase order head
      Optional<DBContainer> MPLINE = findMPLINE(company, inPUNO, inPNLI, inPNLS)
      if (MPLINE.isPresent()) {
          DBContainer containerMPLINE = MPLINE.get() 
          outPUNO = inPUNO
          outLPUD = containerMPLINE.get("IBLPUD")
          outPNLI = containerMPLINE.get("IBPNLI")
          outPNLS = containerMPLINE.get("IBPNLS") 
          outITNO = containerMPLINE.get("IBITNO")
          outLNAM = containerMPLINE.get("IBLNAM")
          outPITD = containerMPLINE.get("IBPITD")
          outVTCD = containerMPLINE.get("IBVTCD")
          lineIVQA = containerMPLINE.get("IBIVQA")
          lineRVQA = containerMPLINE.get("IBRVQA")
          linePUUN = containerMPLINE.get("IBPUUN")
          linePPUN = containerMPLINE.get("IBPPUN")
          
          // Fields for calculation
          lineORQA = containerMPLINE.get("IBCFQA")
          if (lineORQA > 0) {
          } else {
             lineORQA = containerMPLINE.get("IBORQA")
          }
          
          // Fields for calculation
          discount1 = containerMPLINE.get("IBODI1")
          discount2 = containerMPLINE.get("IBODI2")
          discount3 = containerMPLINE.get("IBODI3")
          totalDiscount = discount1 + discount2 + discount3
          confirmedDiscount1 = containerMPLINE.get("IBCFD1")
          confirmedDiscount2 = containerMPLINE.get("IBCFD2")
          confirmedDiscount3 = containerMPLINE.get("IBCFD3")
          totalConfirmedDiscount = (confirmedDiscount1 + confirmedDiscount2 + confirmedDiscount3)
          confirmedPrice = containerMPLINE.get("IBCPPR")
          purchasePrice = containerMPLINE.get("IBPUPR")
          
          if (confirmedPrice > 0) {
            linePrice = purchasePrice * (100 - totalDiscount)
          } else {
            linePrice = confirmedPrice * (100 - totalConfirmedDiscount)
          }
          linePriceRounded = linePrice.setScale(0, RoundingMode.HALF_UP)   
      }
  
  
      outFACT = "           "   
      outFACP = "           "
      outFACO = "           "
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
          calcFACP = calcCOFA1
      } else {
          calcFACP = 1/calcCOFA1
      }
      outFACP = String.valueOf(calcFACP)

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
      outFACO = String.valueOf(calcFACO)
  
      //Calculate the UoM factor               
      resultFACT1 = Math.pow(calcCOFA2,((calcDMCF2 * -2) + 3))
      resultFACT2 = Math.pow(calcCOFA1,((calcDMCF1 * 2) - 3))
      resultFACTTotal = resultFACT1 * resultFACT2
      outFACT = String.valueOf(resultFACTTotal)
        
      // Calculate the qty with alternativ unit 
      orderQtyBaseUnit = lineORQA * calcFACO
      orderQtyBaseUnitRounded = orderQtyBaseUnit.setScale(0, RoundingMode.HALF_UP) 
      invQtyBaseUnit = lineIVQA * calcFACO
      invQtyBaseUnitRounded = invQtyBaseUnit.setScale(0, RoundingMode.HALF_UP) 
      recQtyBaseUnit = lineRVQA * calcFACO
      invQtyBaseUnitRounded = invQtyBaseUnit.setScale(0, RoundingMode.HALF_UP) 
  
      outORQT = String.valueOf(orderQtyBaseUnitRounded)
      outIVQT = String.valueOf(invQtyBaseUnit)
      outRVQT = String.valueOf(recQtyBaseUnit) 
      
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
          
          // Sum the ordered amount header level  
          List<DBContainer> resultMPLINESum = listMPLINE(company, inPUNO) 
          for (DBContainer poLineSum : resultMPLINESum){ 
             // Accumulate quantity   
             confQty = poLineSum.get("IBCFQA") 
             if (confQty > 0) {
             } else {
                confQty = poLineSum.get("IBORQA")  
             }
             confPrice = poLineSum.get("IBCPPR") 
             
             confQtySum =+ confQty 
             confPriceSum =+ confPrice 
          }
          outNTAM = String.valueOf(confQtySum * confPriceSum)
    
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
          
          // Calculate unitprice  
          accRecCostAmount = 0    
          accRecExcRate = 0     
          accRecQty = 0        
          resultDiscount = 0   
          accResult = 0       
      
          resultConfirmedDiscount = 0  
          
      
          if(confirmedPrice == 0d){
            // Calculate confirmed price from receiving lines
            resultDiscount = (1 - (0.01 * discount1)) * (1 - (0.01 * discount2)) * (1 - (0.01 * discount3))
            // Get information from receiving lines
            List<DBContainer> resultFGRECL = listFGRECL(company, division, inPUNO, inPNLI, inPNLS) 
            for (DBContainer recLine : resultFGRECL){
              int receivingNumber = recLine.get("F2REPN")  
    
              // Accumulate quantity   
              invLineRCAC = recLine.get("F2RCAC") 
              invLineSERA = recLine.get("F2SERA") 
              invLineIVQT = recLine.get("F2IVQT")  
    
              accRecCostAmount = invLineRCAC    
              accRecExcRate = invLineSERA       
              accRecQty = invLineIVQT * lineAmount    
       
              unitPrice = 0d 
    		  
              if (accRecExcRate != 0) {
                accResult = (accRecCostAmount / accRecExcRate)  * resultDiscount 
                BigDecimal recConfirmedPrice  = BigDecimal.valueOf(accResult) 
                recConfirmedPrice = recConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
                if(recConfirmedPrice == 0d){
                  if(accRecQty == 0d){ 
                     outUNPR = String.valueOf(lineQty)
    				         unitPrice = lineQty
                  } else { 
                     outUNPR = String.valueOf(accRecQty) 
    				         unitPrice = accRecQty
                  }
                } else { 
                  outUNPR = String.valueOf(recConfirmedPrice) 
    			        unitPrice = recConfirmedPrice
                } 
              } else {
                accResult = accRecCostAmount * resultDiscount   
                BigDecimal recConfirmedPrice  = BigDecimal.valueOf(accResult) 
                recConfirmedPrice = recConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
                if(recConfirmedPrice == 0d){ 
                   outUNPR = String.valueOf(accRecQty * lineAmount)
    			         unitPrice = accRecQty * lineAmount
                }else{ 
                   outUNPR = String.valueOf(recConfirmedPrice) 
    			         unitPrice = accRecQty * lineAmount
                } 
              } 
            }  
          } else {
             // Use confirmed price from orderline
             resultConfirmedDiscount = (1 - (0.01 * confirmedDiscount1)) * (1 - (0.01 * confirmedDiscount2)) * (1 - (0.01 * confirmedDiscount3))
             accResult = confirmedPrice * resultConfirmedDiscount
             BigDecimal poConfirmedPrice  = BigDecimal.valueOf(accResult) 
             poConfirmedPrice = poConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
             outUNPR = String.valueOf(poConfirmedPrice)
    		     unitPrice = poConfirmedPrice
          } 
          
          // Loop Rec invoice header information, to accumulate value  
          accInvQty = 0
          accInvAmount = 0
          
          recNumber = 0 
          List<DBContainer> resultFGRECLHead = listFGRECL(company, division, inPUNO, 0, 0) 
          for (DBContainer recLine : resultFGRECLHead){ 
            // Accumulate quantity   
            invLineRCAC = recLine.get("F2RCAC") 
            invLineSERA = recLine.get("F2SERA")  
         
            accRecCostAmount =+ invLineRCAC 
            accRecExcRate =+ invLineSERA 
          }
           
          // Summarize to output value 
          if(accRecExcRate != 0){
            accResult = (accRecCostAmount / accRecExcRate)  
            BigDecimal recConfirmedPrice  = BigDecimal.valueOf(accResult) 
            recConfirmedPrice = recConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
            if(recConfirmedPrice == 0d){  
              outDEAH = String.valueOf(0)
            }else{ 
              outDEAH = String.valueOf(recConfirmedPrice)
            } 
          }else{  
              outDEAH = String.valueOf(0)
          }  
         
           
          // Loop and send to output Rec invoice line information   
          outSUDO = ""  
          outRPQT = ""  
          outTRDT = ""  
          outREPN = ""  
          outGRIQ = ""  
          outGRAM = ""  
          outCOMP = ""  
          outDEAL = ""  
      
          alreadySentOut = false  
          List<DBContainer> resultFGRECL = listFGRECL(company, division, inPUNO, inPNLI, inPNLS) 
          for (DBContainer recLine : resultFGRECL){ 
            reportedQty = recLine.get("F2RPQA")
            // Output   
            outSUDO = String.valueOf(recLine.get("F2SUDO")) 
            outRPQT = String.valueOf(recLine.get("F2RPQA")) 
            outTRDT = String.valueOf(recLine.get("F2TRDT"))  
            outREPN = String.valueOf(recLine.get("F2REPN")) 
            receivingNumberREPN = recLine.get("F2REPN")
            outGRIQ = String.valueOf(recLine.get("F2IVQA"))
    
            //Get complete flag from MPLIND
            MPLINDlinePUOS = 0
            MPLINDlineRPQA = 0d
            List<DBContainer> resultMPLIND = listMPLIND(company, inPUNO, inPNLI, inPNLS, receivingNumberREPN) 
            for (DBContainer TransLine : resultMPLIND){
               MPLINDlinePUOS = TransLine.get("ICPUOS")   
               MPLINDlineRPQA = TransLine.get("ICRPQA")
               completeFlag = TransLine.get("ICOEND")
          
               if(completeFlag == 1){ 
                  outCOMP = "Yes" 
               }else{ 
                  outCOMP = "No" 
               } 
            }

            // Accumulate quantity   
            recCostAmount = recLine.get("F2RCAC") 
            recExcRate = recLine.get("F2SERA")   
         
            if(recExcRate != 0){
              result = (recCostAmount / recExcRate)  
              BigDecimal recConfirmedPrice  = BigDecimal.valueOf(result) 
              recConfirmedPrice = recConfirmedPrice.setScale(2, RoundingMode.HALF_UP) 
              if(recConfirmedPrice == 0d){  
                outDEAL = String.valueOf(0)  
                outGRAM = String.valueOf(0)  
              }else{ 
                outDEAL = String.valueOf(recConfirmedPrice)
                outGRAM = String.valueOf(recConfirmedPrice)
              } 
            }else{  
              outDEAL = String.valueOf(0)  
              outGRAM = String.valueOf(0) 
            } 
          
            outGRIQ = ""   
            outGRIA = ""   
            invLineIVQT = 0  
            invLineIVNA = 0  
            accInvQty = 0  
            accInvAmount = 0   
            // Get Rec invoice line information  (FGINLI)  
            // - rec number level   
            List<DBContainer> resultFGINLIRec = listFGINLI(company, inPUNO, inPNLI, inPNLS, recNumber) 
            for (DBContainer resultLine : resultFGINLIRec){ 
               // Accumulate quantity   
               invLineIVQT = resultLine.get("F5IVQT") 
               invLineIVNA = resultLine.get("F5IVNA") 
         
               accInvQty =+ invLineIVQT 
               accInvAmount =+ invLineIVNA 
            }
           
            outGRIQ = String.valueOf(accInvQty)
            outGRIA = String.valueOf(accInvAmount)  
            
            // - line level 
            recNumber = 0
            outTIVA = ""  
            invLineIVNA = 0 
            accInvAmount = 0 
            List<DBContainer> resultFGINLILine = listFGINLI(company, inPUNO, inPNLI, inPNLS, recNumber) 
            for (DBContainer resultLine : resultFGINLILine){  
              // Accumulate amount   
              invLineIVNA = resultLine.get("F5IVNA") 
              accInvAmount =+ invLineIVNA 
            } 
          
            outTIVA = String.valueOf(accInvAmount) 
          
            // - header level 
            recNumber = 0
            outIVNA = ""  
            invLineIVNA = 0 
            accInvAmount = 0 
            List<DBContainer> resultFGINLIHead = listFGINLI(company, inPUNO, 0, 0, recNumber) 
              for (DBContainer resultLine : resultFGINLIHead){  
                //Accumulate quantity  
                invLineIVNA = resultLine.get("F5IVNA") 
                accInvAmount =+ invLineIVNA 
              }
            
            outIVNA = String.valueOf(accInvAmount)    
           
            // Send Output parameter, for all received lines 
            setOutput()
            mi.write()   
            alreadySentOut = true   
          } 
         
          // Send Output parameter when no receiving lines exist, send the lines information
          if(!alreadySentOut){    
            setOutput()
            mi.write() 
          }                      
        
      } 
  
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
      DBContainer CMNDIV = query.getContainer()
      CMNDIV.set("CCCONO", CONO)
      CMNDIV.set("CCDIVI", DIVI)
      if(query.read(CMNDIV))  { 
        return Optional.of(CMNDIV)
      } 
    
      return Optional.empty()
    }
     
     
     //***************************************************************************** 
     // Call PPS200 GetLine for line info
     //***************************************************************************** 
     void getPOLineMI(String company, String purchaseOrderNumber, String lineNumber, String lineSuffix){   
          Map<String, String> params = [CONO: company, PUNO: purchaseOrderNumber, PONR: lineNumber, POSX: lineSuffix] 
          String lineAmount = null
          Closure<?> callback = {
            Map<String, String> response ->
            if(response.LNA2 != null){
              lineAmount = response.LNA2
            }
          }
          
          miCaller.call("PPS200MI","GetLine", params, callback)
     } 
     
    //******************************************************************** 
    // Get Division information MPLINE
    //******************************************************************** 
    private Optional<DBContainer> findMPLINE(Integer CONO, String PUNO, Integer PNLI, Integer PNLS){  
      DBAction query = database.table("MPLINE").index("00").selection("IBCONO", "IBPUNO", "IBPNLI", "IBPNLS", "IBITNO", "IBLPUD", "IBLNAM", "IBPITD", "IBVTCD", "IBORQA", "IBIVQA", "IBRVQA", "IBPUUN", "IBPPUN", "IBODI1", "IBODI2", "IBODI3", "IBCFD1", "IBCFD2", "IBCFD3", "IBCPPR").build()    
      DBContainer MPLINE = query.getContainer()
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
      DBAction query = database.table("MPHEAD").index("00").selection("IACONO", "IAPUNO", "IADIVI", "IASUNO", "IANTAM", "IAPUDT", "IABUYE").build()    //A 20210607
      DBContainer MPHEAD = query.getContainer()
      MPHEAD.set("IACONO", CONO)
      MPHEAD.set("IAPUNO", PUNO)
      if(query.read(MPHEAD))  { 
        return Optional.of(MPHEAD)
      } 
    
      return Optional.empty()
    } 
  
  
    //******************************************************************** 
    // Get record from MPLIND
    //******************************************************************** 
    private List<DBContainer> listMPLIND(int CONO, String PUNO, int PNLI, int PNLS, int REPN){ 
      List<DBContainer>TransLine = new ArrayList() 
      ExpressionFactory expression = database.getExpressionFactory("MPLIND")
      DBAction query = database.table("MPLIND").index("60").selection("ICOEND", "ICRPQA", "ICPUOS").build() 
      DBContainer MPLIND = query.getContainer() 
      MPLIND.set("ICCONO", CONO)   
      MPLIND.set("ICPUNO", PUNO) 
      MPLIND.set("ICPNLI", PNLI) 
      MPLIND.set("ICPNLS", PNLS) 
      MPLIND.set("ICREPN", REPN) 
  
      int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
      query.readAll(MPLIND, 5, pageSize, { DBContainer record ->  
       TransLine.add(record) 
      })
  
      return TransLine
    } 
     
   
    //******************************************************************** 
    // Accumulate value from MPLINE
    //******************************************************************** 
    private List<DBContainer> listMPLINE(Integer CONO, String PUNO){ 
      List<DBContainer>poLineSum = new ArrayList() 
      DBAction query = database.table("MPLINE").index("00").selection("IBORQA", "IBCFQA", "IBCPPR").build() 
      DBContainer MPLINE = query.getContainer() 
      MPLINE.set("IBCONO", CONO)   
      MPLINE.set("IBPUNO", PUNO) 
       
      int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
      query.readAll(MPLINE, 2, pageSize, { DBContainer record ->  
        poLineSum.add(record) 
      })

      return poLineSum
    } 
  
    
    //******************************************************************** 
    // Accumulate value from FGINLI
    //******************************************************************** 
    private List<DBContainer> listFGINLI(Integer CONO, String PUNO, Integer PNLI, Integer PNLS, Integer REPN){ 
      List<DBContainer>resultLine = new ArrayList() 
      DBAction query = database.table("FGINLI").index("20").selection("F5IVNA", "F5IVQT").build() 
      DBContainer FGINLI = query.getContainer() 
      FGINLI.set("F5CONO", CONO)   
      FGINLI.set("F5PUNO", PUNO) 
      FGINLI.set("F5PNLI", PNLI) 
      FGINLI.set("F5PNLS", PNLS) 
      FGINLI.set("F5REPN", REPN) 
      
      int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
      if(REPN == 0 && PNLI == 0 && PNLS == 0){
      int countFGINLI = query.readAll(FGINLI, 2, pageSize, { DBContainer record ->  
       resultLine.add(record) 
      })
      } else if(REPN == 0){
      int countFGINLI = query.readAll(FGINLI, 4, pageSize, { DBContainer record ->  
       resultLine.add(record) 
      })
      } else{
        int countFGINLI = query.readAll(FGINLI, 5, pageSize, { DBContainer record ->  
       resultLine.add(record) 
      })
      }
    
      return resultLine
    } 
   
     
    //******************************************************************** 
    // Accumulate value from FGRECL 
    //********************************************************************  
    private List<DBContainer> listFGRECL(int CONO, String DIVI, String PUNO, int PNLI, int PNLS){
      List<DBContainer>recLine = new ArrayList() 
      ExpressionFactory expression = database.getExpressionFactory("FGRECL")
      expression = expression.eq("F2RELP", "1").and(expression.gt("F2RPQA", String.valueOf(0))) 
      DBAction query = database.table("FGRECL").index("00").matching(expression).selection("F2IMST", "F2SUDO", "F2RPQT", "F2TRDT", "F2REPN", "F2IVQT", "F2RCAC", "F2SERA", "F2RPQA").build()
      DBContainer FGRECL = query.createContainer()
      FGRECL.set("F2CONO", CONO)
      FGRECL.set("F2DIVI", DIVI)
      FGRECL.set("F2PUNO", PUNO)
      FGRECL.set("F2PNLI", PNLI)

  	  int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
      query.readAll(FGRECL, 4, pageSize, { DBContainer record ->  
        recLine.add(record.createCopy()) 
      })

      return recLine
    }
    
    
    
    //******************************************************************** 
    // Get Supplier information CIDMAS
    //******************************************************************** 
    private Optional<DBContainer> findCIDMAS(Integer CONO, String SUNO){  
      DBAction query = database.table("CIDMAS").index("00").selection("IDCSCD").build()
      DBContainer CIDMAS = query.getContainer()
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
      DBContainer CEMAIL = query.getContainer()
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
      DBContainer MITAUN = query.getContainer()
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
        mi.outData.put("NTAM", outNTAM)  
        mi.outData.put("PUDT", outPUDT) 
        mi.outData.put("EMAL", outEMAL) 
        mi.outData.put("ORIG", outORIG)
        mi.outData.put("UNPR", outUNPR)
        mi.outData.put("DEAH", outDEAH)
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
        mi.outData.put("IVNA", outIVNA) 
        mi.outData.put("FACT", outFACT) 
        mi.outData.put("VTCD", outVTCD)    
        mi.outData.put("FACO", outFACO)   
        mi.outData.put("FACP", outFACP)   
    } 
    
}  