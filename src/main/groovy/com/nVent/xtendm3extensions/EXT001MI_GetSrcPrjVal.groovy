// @author    Susanna Kellander (susanna.kellander@columbusglobal.com)
// @date      2020-09-25
// @version   1,0 
//
// Description 
// This API transacation GetSrcPrjVal is used for migration, get records with index 00 in dB EXTXRF
//
// 2.0        210214  Changed to dB EXTXRE    Susanna Kellander
//
public class GetSrcPrjVal extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  
  // Constructor 
  public GetSrcPrjVal(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
  } 
    
  public void main() { 
    
    // Get record
    GetRecord()
  }
 
 
    
  void GetRecord(){ 
     int company = mi.in.get("ZCON") 
     
     DBAction action = database.table("EXTXRE").index("00").selectAllFields().build()
     DBContainer ext = action.getContainer()
      
     // Key value for read
     ext.set("EXCONO", company)
     ext.set("EXMPID", mi.in.get("ZMPI"))
     ext.set("EXQUAL", mi.in.get("ZQUA"))
     ext.set("EXSK01", mi.in.get("ZSK1"))
     ext.set("EXSK02", mi.in.get("ZSK2"))
     ext.set("EXTK01", mi.in.get("ZTK1"))
     ext.set("EXTK02", mi.in.get("ZTK2"))
     
     // Read  
    if (action.read(ext)) {
       
      String proj = ext.get("EXMPID")
      String qualifier = ext.get("EXQUAL")
      String sourcekey1 = ext.get("EXSK01")
      String sourcekey2 = ext.get("EXSK02")
      String targetkey1 = ext.get("EXTX01")
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
      
      
      // Send output value  
      mi.outData.put("ZCON", String.valueOf(ext.get("EXCONO")))
      mi.outData.put("ZMPI", proj)
      mi.outData.put("ZQUA", qualifier)
      mi.outData.put("ZSK1", sourcekey1)
      mi.outData.put("ZSK2", sourcekey2)
      mi.outData.put("ZTK1", targetkey1)
      mi.outData.put("ZTK2", targetkey2)
      mi.outData.put("ZSTS", status ) 
      mi.outData.put("ZINC", inclusive)
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
}