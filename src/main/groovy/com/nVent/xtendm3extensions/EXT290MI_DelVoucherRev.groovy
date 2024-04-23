// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-01-30 
// @version   1.0 
//
// Description 
// API transaction DelVoucherRev will be used to delete a voucher reversal record from table EXTGLV
// ERP-9012


/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: YEA4 - Year
 * @param: VONO - Voucher
 * @param: VSER - Voucher Number Series
**/

 public class DelVoucherRev extends ExtendM3Transaction {
    private final MIAPI mi
    private final DatabaseAPI database
    private final ProgramAPI program
    private final LoggerAPI logger
  

  // Constructor 
  public DelVoucherRev(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger
  } 
    
  public void main() { 
     // Set Company Number
     int inCONO
     if (mi.in.get("CONO") != null && mi.in.get("CONO") != 0) {
        inCONO = mi.in.get("CONO")  
     } else {
        inCONO = program.LDAZD.CONO as Integer
     } 

     String inDIVI
     // Set Division     
     if (mi.in.get("DIVI") != null && mi.in.get("DIVI") != "") {
        inDIVI = mi.inData.get("DIVI").trim()
     } else {
        inDIVI = program.LDAZD.DIVI
     }
         
     // Year
     int inYEA4 
     if (mi.in.get("YEA4") != null) {
        inYEA4 = mi.in.get("YEA4") 
     } else {
        inYEA4 = 0        
     }
     
     // Voucher Number
     int inVONO 
     if (mi.in.get("VONO") != null) {
        inVONO = mi.in.get("VONO") 
     } else {
        inVONO = 0        
     }

     // Voucher Number Series
     String inVSER
     if (mi.in.get("VSER") != null && mi.in.get("VSER") != "") {
        inVSER = mi.inData.get("VSER").trim()
     } else {
        inVSER = ""        
     }


     // Validate record
     Optional<DBContainer> EXTGLV = findEXTGLV(inCONO, inDIVI, inYEA4, inVONO, inVSER)
     if(!EXTGLV.isPresent()){
        mi.error("No matching record exists")   
        return             
     } else {
        // Delete records 
        deleteEXTGLVRecord(inCONO, inDIVI, inYEA4, inVONO, inVSER)
     } 
     
  }



  //******************************************************************** 
  // Validate EXTGLV record
  //******************************************************************** 
  private Optional<DBContainer> findEXTGLV(int CONO, String DIVI, int YEA4, int VONO, String VSER){  
     DBAction query = database.table("EXTGLV").index("00").build()
     DBContainer EXTGLV = query.getContainer()
     EXTGLV.set("EXCONO", CONO)
     EXTGLV.set("EXDIVI", DIVI)
     EXTGLV.set("EXYEA4", YEA4)
     EXTGLV.set("EXVONO", VONO)
     EXTGLV.set("EXVSER", VSER)
     if(query.read(EXTGLV))  { 
       return Optional.of(EXTGLV)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTGLV
  //******************************************************************** 
  void deleteEXTGLVRecord(int CONO, String DIVI, int YEA4, int VONO, String VSER){   
     DBAction action = database.table("EXTGLV").index("00").build()
     DBContainer EXTGLV = action.getContainer()
     EXTGLV.set("EXCONO", CONO) 
     EXTGLV.set("EXDIVI", DIVI) 
     EXTGLV.set("EXYEA4", YEA4)
     EXTGLV.set("EXVONO", VONO)
     EXTGLV.set("EXVSER", VSER)
     action.readLock(EXTGLV, deleterCallbackEXTGLV)
  }
    
  Closure<?> deleterCallbackEXTGLV = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }