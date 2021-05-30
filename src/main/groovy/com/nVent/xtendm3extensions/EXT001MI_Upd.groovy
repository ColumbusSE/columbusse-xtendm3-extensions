// @author    Susanna Kellander (susanna.kellander@columbusglobal.com)
// @date      2020-09-25
// @version   1,0 
//
// Description 
// This API transacation Upd is used for migration, saved in dB EXTXRF  
//
// 2.0        210214  Changed to dB EXTXRE    Susanna Kellander
//
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class Upd extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  
  
  public Upd(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
  } 
    
  public void main() { 
    
    // Update record 
    UpdRecord()
  }
 
  public  boolean isNullOrEmpty(String key) {
        if(key != null && !key.isEmpty())
            return false;
        return true;
    }
    
    
     void UpdRecord(){ 
     int company = mi.in.get("ZCON") 
     
     DBAction action = database.table("EXTXRE").index("00").selectAllFields().build()
     DBContainer ext = action.getContainer()
      
     
     ext.set("EXCONO", company)
     ext.set("EXMPID", mi.in.get("ZMPI"))
     ext.set("EXQUAL", mi.in.get("ZQUA"))
     ext.set("EXSK01", mi.in.get("ZSK1"))
     ext.set("EXSK02", mi.in.get("ZSK2"))
     ext.set("EXTK01", mi.in.get("ZTK1"))
     ext.set("EXTK02", mi.in.get("ZTK2"))
     
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
     
     // Update the fields if filled
     if(!isNullOrEmpty(mi.in.get("ZINC").toString())){ 
        lockedResult.set("EXINCL", mi.in.get("ZINC")) 
     }
     if(!isNullOrEmpty(mi.in.get("ZSTS").toString())){
        lockedResult.set("EXSTAT", mi.in.get("ZSTS")) 
     }
     if(!isNullOrEmpty(mi.in.get("ZTX1").toString())){
        lockedResult.set("EXTX01", mi.in.get("ZTX1"))
     }
     if(!isNullOrEmpty(mi.in.get("ZTX2").toString())){
        lockedResult.set("EXTX02", mi.in.get("ZTX2"))
     }
     if(!isNullOrEmpty(mi.in.get("ZTX3").toString())){
        lockedResult.set("EXTX03", mi.in.get("ZTX3"))
     }
     if(!isNullOrEmpty(mi.in.get("ZTX4").toString())){
        lockedResult.set("EXTX04", mi.in.get("ZTX4"))
     }
     if(!isNullOrEmpty(mi.in.get("ZTX5").toString())){
        lockedResult.set("EXTX05", mi.in.get("ZTX5"))
     }
     if(!isNullOrEmpty(mi.in.get("ZTX6").toString())){
        lockedResult.set("EXTX06", mi.in.get("ZTX6"))
     }
     if(!isNullOrEmpty(mi.in.get("ZTX7").toString())){
        lockedResult.set("EXTX07", mi.in.get("ZTX7"))
     }
     if(!isNullOrEmpty(mi.in.get("ZTX8").toString())){
        lockedResult.set("EXTX08", mi.in.get("ZTX8"))
     }
     if(!isNullOrEmpty(mi.in.get("Z180").toString())){
        lockedResult.set("EXA180", mi.in.get("Z180"))  
     }
        
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("EXLMDT", changeddate)  
      
     lockedResult.set("EXCHNO", newChangeNo) 
     lockedResult.set("EXCHID", program.getUser())
     lockedResult.update()
     }
    
}