/****************************************************************************************
 Extension Name: EXT700MI/LstPOLines3
 Type: ExtendM3Transaction
 Script Author: Jessica Bjorklund
 Date: 2025-06-22
 Description:
   This API transacation LstpoNumberLines3 is used to send PO data to ESKAR from M3
    
 Revision History:
 Name                    Date             Version          Description of Changes
 Jessica Bjorklund       2025-06-22       1.0              Creation
******************************************************************************************/


// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2025-06-22
// @version   1.0 
//
// Description 
// This API transacation LstPOLines3 is used to send PO data to ESKAR from M3
//


public class LstPOLines3 extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger  
  
  
  public String purchaseOrder
  public int lineNumber
  public int lineSuffix				   				   

  // Definition of output fields
  public String outPNLI  
  public String outPNLS  
  public String outCONO 
  public String outPUNO
  public String outSUNO   
  public String outITNO
					   
					   
  /*
   * Transaction EXT700MI/LstPOLines3 Interface
   * @param mi - Infor MI Interface
   * @param database - Infor Database Interface
   * @param program - Infor Program Interface
   * @param logger - Infor Logging Interface
  */

  // Constructor 
  public LstPOLines3(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger 
  } 
 

  //******************************************************************** 
  // Main 
  //********************************************************************  
  public void main() { 
      // Get LDA company of not entered 
      int CONO = getCONO()  
      
      String poNo = mi.in.get("PUNO")
      String poNumberLine = mi.in.get("PNLI")
      String poSubLine = mi.in.get("PNLS")
       
      if (isNullOrEmpty(poNo)) { 
          purchaseOrder = 0
	    } else {
		  purchaseOrder = poNo  
      } 
      
	    if (isNullOrEmpty(poNumberLine)) { 
          lineNumber = 0
	    } else {
		  lineNumber = mi.in.get("PNLI")  
      } 
      
	    if (isNullOrEmpty(poSubLine)) { 
          lineSuffix = 0
	    } else {
		  lineSuffix = mi.in.get("PNLS") 
      } 

      // Start the listing in MPLINE
      lstRecord()
   
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
  // Check if null or empty
  //********************************************************************  
   public  boolean isNullOrEmpty(String key) {
        if(key != null && !key.isEmpty())
            return false
        return true
    }
    
    
  //******************************************************************** 
  // Set Output data
  //******************************************************************** 
  void setOutput() {
    mi.outData.put("CONO", outCONO) 
    mi.outData.put("PUNO", outPUNO)
    mi.outData.put("SUNO", outSUNO)
    mi.outData.put("ITNO", outITNO)
    mi.outData.put("PNLI", outPNLI)  
    mi.outData.put("PNLS", outPNLS)  
  } 
    
  //******************************************************************** 
  // List all information
  //********************************************************************  
   void lstRecord() {   
     
     // List all Purchase Order lines
     ExpressionFactory expression = database.getExpressionFactory("MPLINE")

     expression = expression.le("IBPUST", "80").and(expression.ge("IBPUST", "70")).and(expression.ge("IBPUNO", purchaseOrder))

     // List Purchase order line  	 
     DBAction actionline = database.table("MPLINE").index("00").matching(expression).selection("IBCONO", "IBSUNO", "IBITNO", "IBPUNO", "IBPNLI", "IBPNLS", "IBRGDT", "IBPUST").build()    
	 DBContainer line = actionline.getContainer()   
	 
     line.set("IBCONO", CONO) 

     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)                

   } 
    
   //******************************************************************** 
   // List Purchase order line - main loop - MPLINE
   //********************************************************************  
   Closure<?> releasedLineProcessor = { DBContainer line ->   
      String poNumber = line.getString("IBPUNO") 
      int poNumberLine = line.get("IBPNLI")
      int poNumberLineSuffix = line.get("IBPNLS")
      
      //For the starting order. If the order number is the same 
      if (poNumber == purchaseOrder) {
            if (poNumberLine == lineNumber) {
                if (poNumberLineSuffix < lineSuffix) {
                } else {
                   outCONO = line.get("IBCONO")
          		   outPUNO = line.get("IBPUNO") 
          		   outSUNO = line.get("IBSUNO") 
          		   outITNO = line.get("IBITNO") 
          		   outPNLI = String.valueOf(line.get("IBPNLI")) 
          		   outPNLS = String.valueOf(line.get("IBPNLS"))  
                   setOutput()
                   mi.write() 
                }
            } else {
              if (poNumberLine < lineNumber) {
              } else {
                 outCONO = line.get("IBCONO")
				 outPUNO = line.get("IBPUNO") 
				 outSUNO = line.get("IBSUNO") 
				 outITNO = line.get("IBITNO") 
				 outPNLI = String.valueOf(line.get("IBPNLI")) 
				 outPNLS = String.valueOf(line.get("IBPNLS"))  
                 setOutput()
                 mi.write() 
                }
              }
      } else {
         outCONO = line.get("IBCONO")
		 outPUNO = line.get("IBPUNO") 
		 outSUNO = line.get("IBSUNO") 
		 outITNO = line.get("IBITNO") 
		 outPNLI = String.valueOf(line.get("IBPNLI")) 
		 outPNLS = String.valueOf(line.get("IBPNLS"))  
         setOutput()
         mi.write() 
      }
  }
}