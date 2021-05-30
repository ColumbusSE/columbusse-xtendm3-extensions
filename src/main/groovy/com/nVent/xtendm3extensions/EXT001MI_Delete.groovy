// @author    Susanna Kellander (susanna.kellander@columbusglobal.com)
// @date      2020-09-25
// @version   1,0 
//
// Description 
// This API transacation Delete is used for migration, records in dB EXTXRF is deleted
// 
// 2.0        210214  Changed to dB EXTXRE    Susanna Kellander
//
public class Delete extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  private final LoggerAPI logger;
  
  // Constructor 
  public Delete(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
     this.logger = logger;
  } 
    
  public void main() { 
    
    // Update record 
    DeleteRecord()
  }
 
   public  boolean isNullOrEmpty(String key) {
        if(key != null && !key.isEmpty())
            return false;
        return true;
    }
    
     void DeleteRecord(){ 
     int company = mi.in.get("ZCON") 
     String SK2 = mi.in.get("ZSK2")
     
     DBAction action = database.table("EXTXRE").index("00").selectAllFields().build()
     DBContainer ext = action.getContainer()
      
     
     ext.set("EXCONO", company)
     ext.set("EXMPID", mi.in.get("ZMPI"))
     ext.set("EXQUAL", mi.in.get("ZQUA"))
     ext.set("EXSK01", mi.in.get("ZSK1"))
      if(!isNullOrEmpty(SK2)){
         ext.set("EXSK02", mi.in.get("ZSK2"))
      }else{ 
         ext.set("EXSK02",' ') 
      }
    
     ext.set("EXTK01", mi.in.get("ZTK1"))
     ext.set("EXTK02", mi.in.get("ZTK2")) 
     
     // Read with lock,  3 or more keys depending on input
     String firstKey = mi.in.get("ZSK1")
     String secondKey = mi.in.get("ZSK2")
     String thirdKey = mi.in.get("ZTK1")
     String fourthKey = mi.in.get("ZTK2")

        if(!isNullOrEmpty(firstKey) && !isNullOrEmpty(secondKey)  && !isNullOrEmpty(thirdKey)  && !isNullOrEmpty(fourthKey)){
            action.readAllLock(ext, 7,deleterCallback)
        }else if(!isNullOrEmpty(firstKey) && !isNullOrEmpty(secondKey)  && !isNullOrEmpty(thirdKey)  && isNullOrEmpty(fourthKey)){
            action.readAllLock(ext, 6, deleterCallback)
        }else if(!isNullOrEmpty(firstKey) && !isNullOrEmpty(secondKey)  && isNullOrEmpty(thirdKey)  && isNullOrEmpty(fourthKey)){
            action.readAllLock(ext, 5, deleterCallback)
        }else if(!isNullOrEmpty(firstKey) && isNullOrEmpty(secondKey)  && isNullOrEmpty(thirdKey)  && isNullOrEmpty(fourthKey)){
            action.readAllLock(ext, 4, deleterCallback)
        }else {
           action.readAllLock(ext, 3, deleterCallback)
        }
     }

    
     Closure<?> deleterCallback = { LockedResult lockedResult ->  

     lockedResult.delete()
     }
    
}