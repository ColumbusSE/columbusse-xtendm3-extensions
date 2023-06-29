// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-06-21
// @version   1.0 
//
// Description 
// Calculate and display Sales Price based on cost in MCHEAD together with order currency
//

import java.time.LocalDate;
import java.lang.Math;
import java.math.BigDecimal
import java.math.RoundingMode 
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class UpdateCost_PEINZ extends ExtendM3Trigger {
  private final ProgramAPI program;
  private final DatabaseAPI database; 
  private final InteractiveAPI interactive;
  private final LoggerAPI logger;
  private final MICallerAPI miCaller; 
  
  int company
  String division
  String divisionOrder
  String currentDate
  String orderNumber
  int orderLineNumber
  int orderLineSuffix
  String currency
  String currencyRate
  double costingSum7
  double newPrice
  String decimalFormat
  String orderLineWarehouse
  String warehouseDivi
  String divisionCurrency
  double oldSalesPriceDouble
  String oldSalesPriceString

  public UpdateCost_PEINZ(ProgramAPI program, DatabaseAPI database, InteractiveAPI interactive, LoggerAPI logger, MICallerAPI miCaller) {
    this.program = program
    this.database = database
    this.interactive = interactive
    this.logger = logger
    this.miCaller = miCaller
  }
  
  public void main() {
    company = program.LDAZD.CONO
    decimalFormat = program.LDAZD.DCFM
    division = program.LDAZD.DIVI

    currentDate = currentDateY8AsString()
    orderNumber = interactive.display.fields.OAORNO
    orderLineNumber = interactive.display.fields.OBPONR
    orderLineSuffix = interactive.display.fields.OBPOSX
    currency = interactive.display.fields.OACUCD
    orderLineWarehouse = interactive.display.fields.OBWHLO
    oldSalesPriceString = interactive.display.fields.WBSAPR
    
    String oldSalesPriceStringFormat = oldSalesPriceString.replaceAll(",", "")
    double oldSalesPriceDouble = Double.parseDouble(oldSalesPriceStringFormat)

    getCostPrice()
    
    if (costingSum7 != null && costingSum7 > 0d) {
        // Find exchange rate
        getDivisionOrder(String.valueOf(company), orderNumber)
        getWarehouse(orderLineWarehouse)
        getDivisionCurrency(String.valueOf(company), divisionOrder)
        getRate(String.valueOf(company), divisionOrder, divisionCurrency, currentDate)
        newPrice = costingSum7 * currencyRate.toDouble()
        BigDecimal newPriceRounded  = BigDecimal.valueOf(newPrice) 
        newPriceRounded = newPriceRounded.setScale(2, RoundingMode.HALF_UP) 
        
        String newPriceFormat
        if (decimalFormat.trim() == ",") {
           newPriceFormat = String.valueOf(newPriceRounded).replace(".", ",")
        } else {
           newPriceFormat = String.valueOf(newPriceRounded)
        }
        String test = newPriceFormat
        interactive.display.fields.WBSAPR = newPriceFormat
      }

  }
  

  //******************************************************************** 
  // Get current rate
  //******************************************************************** 
  void getRate(String cono, String divi, String cucd, String date) {
    def params = ["CONO":cono, "FDDI": divi, "TODI": divi, "FCUR": cucd, "TCUR": cucd, "CRTP": "1".toString(), "CUTD": date] // toString is needed to convert from gstring to string
    def callback = {
    Map<String, String> response ->
      if(response.ARAT != null){
        currencyRate = response.ARAT.toString()
      }
    }
    
    miCaller.call("CRS055MI","SelExchangeRate", params, callback)
    
  }


  //******************************************************************** 
  // Get delivery warehouse
  //******************************************************************** 
  void getWarehouse(String whlo) {
    def params = ["WHLO": whlo] 
    def callback = {
    Map<String, String> response ->
      if(response.DIVI != null){
        warehouseDivi = response.DIVI.toString()
      }
    }
    
    miCaller.call("MMS005MI","GetWarehouse", params, callback)
    
  }
  

  //******************************************************************** 
  // Get currency for division
  //******************************************************************** 
  void getDivisionCurrency(String cono, String divi) {
    def params = ["CONO":cono, "DIVI": divi] 
    def callback = {
    Map<String, String> response ->
      if(response.CUCD != null){
        divisionCurrency = response.CUCD.toString()
      }
    }
    
    miCaller.call("MNS100MI","GetBasicData", params, callback)
    
  }


  //******************************************************************** 
  // Get divsion for order
  //******************************************************************** 
  void getDivisionOrder(String cono, String orno) {
    def params = ["CONO":cono, "ORNO": orno] 
    def callback = {
    Map<String, String> response ->
      if(response.DIVI != null){
        divisionOrder = response.DIVI.toString()
      }
    }
    
    miCaller.call("OIS100MI","GetHead", params, callback)
    
  }
  
  
  //******************************************************************** 
  // Get date in yyyyMMdd format
  // @return date
  //******************************************************************** 
  public String currentDateY8AsString() {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
  }

  
  //******************************************************************** 
  // Get Cost Price from MCHEAD
  //********************************************************************  
  void getCostPrice(){   
     
     costingSum7 = 0
     
     ExpressionFactory expression = database.getExpressionFactory("MCHEAD")
   
     expression = expression.eq("KOCONO", String.valueOf(company)).and(expression.eq("KOCROC", String.valueOf(3))).and(expression.eq("KORORN", orderNumber.trim())).and(expression.eq("KORORL", String.valueOf(orderLineNumber))).and(expression.eq("KORORX", String.valueOf(orderLineSuffix))) 

     // Get cost line   
     DBAction actionlineMCHEAD = database.table("MCHEAD").index("10").matching(expression).selection("KOCONO", "KOFACI", "KOCROC", "KORORN", "KORORL", "KORORX", "KOCSU7").build()     

     DBContainer lineMCHEAD = actionlineMCHEAD.getContainer()  
     
     // Read with one key  
     lineMCHEAD.set("KOCONO", company)  

     actionlineMCHEAD.readAll(lineMCHEAD, 1, releasedLineProcessorMCHEAD)   
   
   } 
 

  //******************************************************************** 
  // Get Cost Price Line - main loop - MCHEAD
  //********************************************************************  
  Closure<?> releasedLineProcessorMCHEAD = { DBContainer lineMCHEAD ->   
       costingSum7 = lineMCHEAD.get("KOCSU7")
     if (costingSum7 > 0) {
        costingSum7 = lineMCHEAD.get("KOCSU7")
     }
  }


}