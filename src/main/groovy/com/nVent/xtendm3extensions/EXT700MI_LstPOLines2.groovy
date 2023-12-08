
// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2022-06-22
// @version   1.0 
//
// Description 
// This API transacation LstPOLines is used to send PO data to ESKAR from M3
//


public class LstPOLines2 extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger  
  
  public int regDate
  public String purchaseOrder
  public String inRegDate

  // Definition of output fields
  public String outPNLI  
  public String outPNLS  
  public String outCONO 
  public String outDIVI
  public String outPUNO
  public String outSUNO   
  public String outITNO
  public String outGRNR
  public String outPUST


  // Constructor 
  public LstPOLines2(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
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
      
      // If Registration date and/or Purchae order are filled it will be used 
      inRegDate = mi.in.get("RGDT") 
      if(isNullOrEmpty(inRegDate)){ 
        regDate = 0
      }else{
        regDate = mi.in.get("RGDT")
      } 
      
      purchaseOrder = mi.in.get("PUNO")  

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
    mi.outData.put("PUST", outPUST)  
    mi.outData.put("GRNR", outGRNR)  
  } 
    
  //******************************************************************** 
  // List all information
  //********************************************************************  
   void lstRecord(){   
     
     // List all Purchase Order lines
     ExpressionFactory expression = database.getExpressionFactory("MPLINE")

	   // Depending on input value (Registrationdate and Purchase order)
	   // Only get invoice lines in status 70-80
     if (regDate != 0) {
        expression = expression.le("IBPUSL", "80").and(expression.ge("IBPUSL", "15")).and(expression.eq("IBRGDT", String.valueOf(regDate)))    
     } else if (regDate == 0) {
        expression = expression.le("IBPUSL", "80").and(expression.ge("IBPUSL", "15"))                                             
     } else {
        expression = expression.le("IBPUSL", "80").and(expression.ge("IBPUSL", "15"))   
     }

     // List Purchase order line  	 
     DBAction actionline = database.table("MPLINE").index("06").matching(expression).selection("IBCONO", "IBSUNO", "IBITNO", "IBPUNO", "IBPNLI", "IBPNLS", "IBRGDT", "IBPUST", "IBPUSL").build()    
	   DBContainer line = actionline.getContainer()   
     
     line.set("IBCONO", CONO) 
	   line.set("IBPUNO", purchaseOrder)                                                       
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     
     if(!isNullOrEmpty(purchaseOrder)){                                                      
       actionline.readAll(line, 2, pageSize, releasedLineProcessor)                
     } else {                                                                     
	     actionline.readAll(line, 1, pageSize, releasedLineProcessor)                
     }                                                                            

   } 
    
  //******************************************************************** 
  // List Purchase order line - main loop - MPLINE
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->   
      outCONO = line.get("IBCONO")
      outPUNO = line.get("IBPUNO") 
      outSUNO = line.get("IBSUNO") 
      outITNO = line.get("IBITNO") 
      outPNLI = String.valueOf(line.get("IBPNLI")) 
      outPNLS = String.valueOf(line.get("IBPNLS"))  
      outPUST = line.get("IBPUST")
      if (outPUST >= "70" && outPUST <= "80") {
         outGRNR = "Y "
      } else {
         outGRNR = "N "
      }
    
      setOutput()
      mi.write() 
  }  

}  