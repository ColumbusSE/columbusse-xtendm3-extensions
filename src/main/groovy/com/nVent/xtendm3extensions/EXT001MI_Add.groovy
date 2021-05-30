// @author    Susanna Kellander (susanna.kellander@columbusglobal.com)
// @date      2020-09-25
// @version   1,0 
//
// Description 
// This API transacation Add is used for migration, saved in dB EXTXRF  
// 
// 2.0        210214  Changed to dB EXTXRE              Susanna Kellander
// 3.0        210215  Change handle of verify company   Susanna Kellander

import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class Add extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final LoggerAPI logger;
  
  // Constructor 
  public Add(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi;
     this.database = database;
     this.miCaller = miCaller;
     this.program = program;
     this.logger = logger;
  } 
    
  public void main() { 
      
      // Validate company
      Integer CONO = mi.in.get("ZCON")                 
      Optional<DBContainer> CMNCMP = findCMNCMP(CONO)  
      if(!CMNCMP.isPresent()){                         
      mi.error("Company " + CONO + " is invalid")   
        return                                         
      }                                                
      
    // Write record 
    AddRecord()
  }
  

 public  boolean isNullOrEmpty(String key) {
        if(key != null && !key.isEmpty())
            return false;
        return true;
    }
    

    private Optional<DBContainer> findCMNCMP(Integer CONO){                             
      DBAction query = database.table("CMNCMP").index("00").selection("JICONO").build()   
      DBContainer CMNCMP = query.getContainer()                                           
      CMNCMP.set("JICONO", CONO)                                                         
      if(query.read(CMNCMP))  {                                                           
        return Optional.of(CMNCMP)                                                        
      }                                                                                  
      return Optional.empty()                                                            
    }                                                                                   
    
     void AddRecord(){ 
       
     int company = mi.in.get("ZCON") 
     String SK2 = mi.in.get("ZSK2")  
     String TK2 = mi.in.get("ZTK2")  
     String STS = mi.in.get("ZSTS")  
     String INC = mi.in.get("ZINC")  
     String TX1 = mi.in.get("ZTX1")
     String TX2 = mi.in.get("ZTX2")
     String TX3 = mi.in.get("ZTX3")
     String TX4 = mi.in.get("ZTX4")
     String TX5 = mi.in.get("ZTX5")
     String TX6 = mi.in.get("ZTX6")
     String TX7 = mi.in.get("ZTX7")
     String TX8 = mi.in.get("ZTX8")
     String T180 = mi.in.get("Z180")   
     
     DBAction action = database.table("EXTXRE").index("00").selectAllFields().build()
     DBContainer ext = action.createContainer()
     ext.set("EXCONO", company)
     ext.set("EXMPID", mi.in.get("ZMPI"))
     ext.set("EXQUAL", mi.in.get("ZQUA"))
     ext.set("EXSK01", mi.in.get("ZSK1"))
     ext.set("EXTK01", mi.in.get("ZTK1"))
     
     //Check if null value for those that are not mandatory  
     if(!isNullOrEmpty(SK2)){  
       ext.set("EXSK02", mi.in.get("ZSK2")) 
     }else { 
       ext.set("EXSK02", ' ')   
     } 
     if(!isNullOrEmpty(TK2)){  
        ext.set("EXTK02", mi.in.get("ZTK2"))
     }else { 
       ext.set("EXTK02", ' ')   
     } 
     if(!isNullOrEmpty(STS)){  
       ext.set("EXSTAT", mi.in.get("ZSTS"))
     }else { 
       ext.set("EXSTAT", ' ')   
     } 
     if(!isNullOrEmpty(INC)){  
       ext.set("EXINCL", mi.in.get("ZINC"))
     }else { 
       ext.set("EXINCL", ' ')   
     }
     if(!isNullOrEmpty(TX1)){  
       ext.set("EXTX01", mi.in.get("ZTX1"))
     }else { 
       ext.set("EXTX01", ' ')   
     } 
     if(!isNullOrEmpty(TX2)){  
       ext.set("EXTX02", mi.in.get("ZTX2"))
     }else { 
       ext.set("EXTX02", ' ')   
     } 
     if(!isNullOrEmpty(TX3)){  
       ext.set("EXTX03", mi.in.get("ZTX3"))
     }else { 
       ext.set("EXTX03", ' ')   
     } 
     if(!isNullOrEmpty(TX4)){  
       ext.set("EXTX04", mi.in.get("ZTX4"))
     }else { 
       ext.set("EXTX04", ' ')   
     } 
     if(!isNullOrEmpty(TX5)){  
       ext.set("EXTX05", mi.in.get("ZTX5"))
     }else { 
       ext.set("EXTX05", ' ')   
     } 
     if(!isNullOrEmpty(TX6)){  
       ext.set("EXTX06", mi.in.get("ZTX6"))
     }else { 
       ext.set("EXTX06", ' ')   
     } 
     if(!isNullOrEmpty(TX7)){  
       ext.set("EXTX07", mi.in.get("ZTX7"))
     }else { 
       ext.set("EXTX07", ' ')   
     } 
     if(!isNullOrEmpty(TX8)){  
       ext.set("EXTX08", mi.in.get("ZTX8"))
     }else { 
       ext.set("EXTX08", ' ')   
     } 
     if(!isNullOrEmpty(T180)){  
       ext.set("EXA180", mi.in.get("Z180"))
     }else { 
       ext.set("EXA180", ' ')   
     }  
   
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
} 