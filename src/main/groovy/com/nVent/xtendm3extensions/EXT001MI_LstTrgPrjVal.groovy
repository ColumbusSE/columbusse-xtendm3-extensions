// @author    Susanna Kellander (susanna.kellander@columbusglobal.com)
// @date      2020-09-25
// @version   1,0 
//
// Description 
// This API transacation LstTrgPrjVal is used for migration, list records with index 10 in dB EXTXRF
//
// 2.0        210214  Changed to dB EXTXRE    Susanna Kellander
//
public class LstTrgPrjVal extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  
  // Constructor 
  public LstTrgPrjVal(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
  } 
    
  public void main() { 
    
    // List record via target key value
    LstRecord()
  }
 
   public  boolean isNullOrEmpty(String key) {
        if(key != null && !key.isEmpty())
            return false;
        return true;
    }
    
  void LstRecord(){ 
     int company = mi.in.get("ZCON") 
     
     DBAction action = database.table("EXTXRE").index("10").selectAllFields().build()
     DBContainer ext = action.getContainer()
      
     ext.set("EXCONO", company)
     ext.set("EXMPID", mi.in.get("ZMPI"))
     ext.set("EXQUAL", mi.in.get("ZQUA"))
     ext.set("EXTK01", mi.in.get("ZTK1"))
     ext.set("EXTK02", mi.in.get("ZTK2"))
     
    
     // Read with 4 or 5 keys depending on input  
      String firstKey = mi.in.get("ZTK1") 
      String secondKey = mi.in.get("ZTK2")  
      
     if(isNullOrEmpty(firstKey) && isNullOrEmpty(secondKey)){  
        action.readAll(ext, 3, releasedItemProcessor) 
     }else if(isNullOrEmpty(secondKey)){  
        action.readAll(ext, 4, releasedItemProcessor) 
     }else {
        action.readAll(ext, 5, releasedItemProcessor) 
     }
  } 
  
 
    
    Closure<?> releasedItemProcessor = { DBContainer ext -> 
      String proj = ext.get("EXMPID")
      String qualifier = ext.get("EXQUAL")
      String sourcekey1 = ext.get("EXSK01")
      String sourcekey2 = ext.get("EXSK02")
      String targetkey1 = ext.get("EXTK01")
      String targetkey2 = ext.get("EXTK02") 
      String status = ext.get("EXSTAT")
      String inclusive = ext.get("EXINCL") 
      String text1 = ext.get("EXTX01")
      String text2 = ext.get("EXTX02")
      String text3 = ext.get("EXTX03")
      String text4 = ext.get("EXTX04")
      String text5 = ext.get("EXTX05")
      String text6 = ext.get("EXTX06")
      String text7 = ext.get("EXTX07")
      String text8 = ext.get("EXTX08")
      String comments = ext.get("EXA180")
      String user = ext.get("EXCHID")
      
      mi.outData.put("ZCON", String.valueOf(ext.get("EXCONO")))
      mi.outData.put("ZMPI", proj)
      mi.outData.put("ZQUA", qualifier)
      mi.outData.put("ZSK1", sourcekey1)
      mi.outData.put("ZSK2", sourcekey2)
      mi.outData.put("ZTK1", targetkey1)
      mi.outData.put("ZTK2", targetkey2)
      mi.outData.put("ZINC", inclusive)
      mi.outData.put("ZSTS", status ) 
      mi.outData.put("ZTX1", text1 ) 
      mi.outData.put("ZTX2", text2 ) 
      mi.outData.put("ZTX3", text3 ) 
      mi.outData.put("ZTX4", text4 ) 
      mi.outData.put("ZTX5", text5 ) 
      mi.outData.put("ZTX6", text6 ) 
      mi.outData.put("ZTX7", text7 ) 
      mi.outData.put("ZTX8", text8 )  
      mi.outData.put("Z180", comments ) 
      
      mi.outData.put("ZRGD", String.valueOf(ext.get("EXRGDT")))
      mi.outData.put("ZRGT", String.valueOf(ext.get("EXRGTM")))
      mi.outData.put("ZLMD", String.valueOf(ext.get("EXLMDT")))
      mi.outData.put("ZCHN", String.valueOf(ext.get("EXCHNO")))
      
      mi.outData.put("ZCHD", user)  
      mi.write() 
   } 
}