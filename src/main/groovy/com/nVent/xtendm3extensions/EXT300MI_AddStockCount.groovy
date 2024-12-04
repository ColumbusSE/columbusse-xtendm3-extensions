// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-05-01 
// @version   1.0 
//
// Description 
// API transaction AddStockCount will be used to add a Stock Count record to table EXTSTK
// ERP-9265

// Date         Changed By                         Description
// 2024-05-10   Jessica Bjorklund (Columbus)       Creation
// 2024-09-10   Jessica Bjorklund (Columbus)       Set on-hand balance to 0 if MITLOC record not found
// 2024-11-21   Jessica Bjorklund (Columbus)       Add validation of Inventory Date



/**
 * IN
 * @param: CONO - Company Number
 * @param: STNB - Physical Inventory Number
 * @param: STRN - Line Number
 * @param: RENU - Recount Number
*/


public class AddStockCount extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  int inCONO
  int inSTNB
  int inSTRN
  int inRENU
  int companyNumber
  String warehouse
  String itemNumber
  String location
  String lotNumber
  int inventoryDate
  int inventoryTime
  String container
  int receivingNumber
  double balance
  double onHandBalance
  double sumTRQT
  double transactionQty

  // Constructor 
  public AddStockCount(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger
     this.utility = utility
  } 
    
  public void main() {       
     // Set Company Number
     if (mi.in.get("CONO") != null && mi.in.get("CONO") != 0) {
        inCONO = mi.in.get("CONO")  
     } else {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Physical Inventory Number
     if (mi.in.get("STNB") != null) {
        inSTNB = mi.in.get("STNB") 
     } else {
        inSTNB = 0        
     }
     
     // Line Number
     if (mi.in.get("STRN") != null) {
        inSTRN = mi.in.get("STRN") 
     } else {
        inSTRN = 0        
     }

     // Recount Number
     if (mi.in.get("RENU") != null) {
        inRENU = mi.in.get("RENU") 
     } else {
        inRENU = 0        
     }


     // Validate Stock Count in MITTKD
     Optional<DBContainer> MITTKD = findMITTKD(inCONO, inSTNB, inSTRN, inRENU)
     if (MITTKD.isPresent()) {
        //Record found, get field information
        DBContainer containerMITTKD = MITTKD.get()  
        companyNumber = containerMITTKD.get("SDCONO")
        warehouse = containerMITTKD.getString("SDWHLO")
        itemNumber = containerMITTKD.getString("SDITNO")
        location = containerMITTKD.getString("SDWHSL")
        lotNumber = containerMITTKD.getString("SDBANO")
        inventoryDate = containerMITTKD.get("SDSTDI")
        inventoryTime = containerMITTKD.get("SDSTTM")
        container = containerMITTKD.getString("SDCAMU")
        receivingNumber = containerMITTKD.get("SDREPN")
     } else {
        mi.error("MITTKD record does not exist")   
        return             
     } 
     
     if (inventoryDate > 0) {  
     } else {
       return
     }
     
     logger.debug("companyNumber ${companyNumber}")
     logger.debug("warehouse ${warehouse}")
     logger.debug("itemNumber ${itemNumber}")
     logger.debug("location ${location}")
     logger.debug("lotNumber ${lotNumber}")
     logger.debug("container ${container}")
     logger.debug("receivingNumber ${receivingNumber}")

     // Validate record in MITLOC
     Optional<DBContainer> MITLOC = findMITLOC(companyNumber, warehouse, itemNumber, location, lotNumber, container, receivingNumber) 
     if (MITLOC.isPresent()) {
        //Record found, get field information
        DBContainer containerMITLOC = MITLOC.get()  
        onHandBalance = containerMITLOC.get("MLSTQT")
     } else {
        onHandBalance = 0d
     } 
     
     
     logger.debug("onHandBalance ${onHandBalance}")
     
     
     sumTRQT = 0d
     getMITTRArecords()
     balance = onHandBalance - sumTRQT
     
     logger.debug("sumTRQT ${sumTRQT}")
     logger.debug("balance ${balance}")

     // Validate Stock Count record
     Optional<DBContainer> EXTSTK = findEXTSTK(inCONO, inSTNB, inSTRN, inRENU)
     if (EXTSTK.isPresent()) {
        // Update record
        updEXTSTKRecord()
     } else {
        // Add record
        addEXTSTKRecord(inCONO, inSTNB, inSTRN, inRENU, balance)
     }

  }
  
    


  //******************************************************************** 
  // Validate EXTSTK record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSTK(int CONO, int STNB, int STRN, int RENU){  
     DBAction query = database.table("EXTSTK").index("00").build()
     DBContainer EXTSTK = query.getContainer()
     EXTSTK.set("EXCONO", CONO)
     EXTSTK.set("EXSTNB", STNB)
     EXTSTK.set("EXSTRN", STRN)
     EXTSTK.set("EXRENU", RENU)
     if(query.read(EXTSTK))  { 
       return Optional.of(EXTSTK)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Validate MITTKD record
  //******************************************************************** 
  private Optional<DBContainer> findMITTKD(int CONO, int STNB, int STRN, int RENU){  
     DBAction query = database.table("MITTKD").index("00").selectAllFields().build()
     DBContainer MITTKD = query.getContainer()
     MITTKD.set("SDCONO", CONO)
     MITTKD.set("SDSTNB", STNB)
     MITTKD.set("SDSTRN", STRN)
     MITTKD.set("SDRENU", RENU)
     if(query.read(MITTKD))  { 
       return Optional.of(MITTKD)
     } 
  
     return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Validate MITLOC record
  //******************************************************************** 
  private Optional<DBContainer> findMITLOC(int CONO, String WHLO, String ITNO, String WHSL, String BANO, String CAMU, int REPN){  
     DBAction query = database.table("MITLOC").index("00").selection("MLSTQT").build()
     DBContainer MITLOC = query.getContainer()
     MITLOC.set("MLCONO", CONO)
     MITLOC.set("MLWHLO", WHLO)
     MITLOC.set("MLITNO", ITNO)
     MITLOC.set("MLWHSL", WHSL)
     MITLOC.set("MLBANO", BANO)
     MITLOC.set("MLCAMU", CAMU)
     MITLOC.set("MLREPN", REPN)
     if(query.read(MITLOC))  { 
       return Optional.of(MITLOC)
     } 
  
     return Optional.empty()
  }
  
  

   //******************************************************************** 
   // Find MITTRA records with date greater than inventoryDate
   //********************************************************************  
   void getMITTRArecords(){   
     
     // List MITTRA records
     ExpressionFactory expression = database.getExpressionFactory("MITTRA")
     expression = expression.ge("MTTRDT", String.valueOf(inventoryDate)).and(expression.ge("MTTRTM", String.valueOf(inventoryTime)))
     
     DBAction actionline = database.table("MITTRA").index("10")
     .matching(expression)
     .selection("MTTRDT", "MTTRTM", "MTTRQT")
     .build()

     DBContainer line = actionline.getContainer()  
     
     // Read  
     line.set("MTCONO", inCONO)  
     line.set("MTWHLO", warehouse)
     line.set("MTITNO", itemNumber)

     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()         

     actionline.readAll(line, 3, pageSize, releasedLineProcessor)   
   
   } 

    
  //******************************************************************** 
  // List MITTRA records for Inventory Date - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->     
    
      transactionQty = 0d
      transactionQty = line.get("MTTRQT")
      sumTRQT = sumTRQT + transactionQty
      
      logger.debug("inside list MITTRA transactionQty ${transactionQty}")
      logger.debug("inside list MITTRA sumTRQT ${sumTRQT}")

  }


  //******************************************************************** 
  // Update EXTSTK record
  //********************************************************************    
  void updEXTSTKRecord(){      
     DBAction action = database.table("EXTSTK").index("00").build()
     DBContainer EXTSTK = action.getContainer()     
     EXTSTK.set("EXCONO", inCONO)
     EXTSTK.set("EXSTNB", inSTNB)
     EXTSTK.set("EXSTRN", inSTRN)
     EXTSTK.set("EXRENU", inRENU)

     // Read with lock
     action.readLock(EXTSTK, updateCallBackEXTSTK)
  }
   
  Closure<?> updateCallBackEXTSTK = { LockedResult lockedResult ->      
     
     lockedResult.set("EXSTQI", balance)

     int changeNo = lockedResult.get("EXCHNO")
     int newChangeNo = changeNo + 1 
     int changedate = utility.call("DateUtil", "currentDateY8AsInt")
     lockedResult.set("EXLMDT", changedate)       
     lockedResult.set("EXCHNO", newChangeNo) 
     lockedResult.set("EXCHID", program.getUser())
     lockedResult.update()
  }
  
  
  //******************************************************************** 
  // Add EXTSTK record 
  //********************************************************************     
  void addEXTSTKRecord(int CONO, int STNB, int STRN, int RENU, double STQI) {   
       DBAction action = database.table("EXTSTK").index("00").build()
       DBContainer EXTSTK = action.createContainer()
       EXTSTK.set("EXCONO", CONO)
       EXTSTK.set("EXSTNB", STNB)
       EXTSTK.set("EXSTRN", STRN)
       EXTSTK.set("EXRENU", RENU)
       EXTSTK.set("EXSTQI", STQI)
       EXTSTK.set("EXCHID", program.getUser())
       EXTSTK.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTSTK.set("EXRGDT", regdate) 
       EXTSTK.set("EXLMDT", regdate) 
       EXTSTK.set("EXRGTM", regtime)
       action.insert(EXTSTK)         
 } 

     
} 

