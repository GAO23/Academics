package dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import model.Stock;

public class StockDao {

    public Stock getDummyStock() {
        Stock stock = new Stock();
        stock.setName("Apple");
        stock.setSymbol("AAPL");
        stock.setPrice(150.0);
        stock.setNumShares(1200);
        stock.setType("Technology");

        return stock;
    }

    public List<Stock> getDummyStocks() {
        List<Stock> stocks = new ArrayList<Stock>();

		/*Sample data begins*/
        for (int i = 0; i < 10; i++) {
            stocks.add(getDummyStock());
        }
		/*Sample data ends*/

        return stocks;
    }

    public List<Stock> getActivelyTradedStocks() {
		/*
		 * The students code to fetch data from the database will be written here
		 * Query to fetch details of all the stocks has to be implemented
		 * Return list of actively traded stocks
		 */

        ArrayList<Stock> stocks = new ArrayList<Stock>();
        MysqlConn.initalizeConnection();
        String query = "SELECT DISTINCT StockSymbol, CompanyName, Type, PricePerShare, NumShare " +
                "FROM Stocks, Trade " +
                "WHERE Trade.StockId = Stocks.StockSymbol;";

        ResultSet rs = MysqlConn.runSelectQuery(query);

        try {
            while(rs.next()){
                Stock stock = new Stock();
                stock.setName(rs.getString(2));
                stock.setNumShares(rs.getInt(5));
                stock.setSymbol(rs.getString(1));
                stock.setType(rs.getString(3));
                stock.setPrice(rs.getFloat(4));
                stocks.add(stock);
            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return stocks;


    }

	public List<Stock> getAllStocks() {
		
		/*
		 * The students code to fetch data from the database will be written here
		 * Return list of stocks
		 */

        ArrayList<Stock> stocks = new ArrayList<Stock>();
        MysqlConn.initalizeConnection();
        String query = "SELECT StockSymbol, CompanyName, Type, PricePerShare, NumShare " +
                "FROM Stocks";

        ResultSet rs = MysqlConn.runSelectQuery(query);

        try {
            while(rs.next()){
                Stock stock = new Stock();
                stock.setName(rs.getString(2));
                stock.setNumShares(rs.getInt(5));
                stock.setSymbol(rs.getString(1));
                stock.setType(rs.getString(3));
                stock.setPrice(rs.getFloat(4));
                stocks.add(stock);
            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return stocks;

	}

    public Stock getStockBySymbol(String stockSymbol)
    {
        /*
		 * The students code to fetch data from the database will be written here
		 * Return stock matching symbol
		 */
        /*
         * The students code to fetch data from the database will be written here
         * Return list of stocks
         */
        Stock stock = new Stock();
        MysqlConn.initalizeConnection();
        String query = "SELECT StockSymbol, CompanyName, Type, PricePerShare, NumShare " +
                "FROM Stocks " +
                "WHERE StockSymbol = \'" + stockSymbol +"\'";

        ResultSet rs = MysqlConn.runSelectQuery(query);

        try {
            while(rs.next()){
                stock.setName(rs.getString(2));
                stock.setNumShares(rs.getInt(5));
                stock.setSymbol(rs.getString(1));
                stock.setType(rs.getString(3));
                stock.setPrice(rs.getFloat(4));
            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return stock;
    }

    public String setStockPrice(String stockSymbol, double stockPrice) {
        /*
         * The students code to fetch data from the database will be written here
         * Perform price update of the stock symbol
         */
        MysqlConn.initalizeConnection();
        String query = String.format("UPDATE Stocks " +
                "SET PricePerShare = %s " +
                "WHERE StockSymbol = \'%s\'",
                stockPrice,
                stockSymbol
                );
        boolean success = MysqlConn.updateDeleteInsertQuery(query);
        return (success) ? "success" : "failure";
    }
	
	public List<Stock> getOverallBestsellers() {

		/*
		 * The students code to fetch data from the database will be written here
		 * Get list of bestseller stocks
		 */
		ArrayList<Stock> stocks = new ArrayList<Stock>();
		MysqlConn.initalizeConnection();
		String query = "SELECT Stocks.StockSymbol, Stocks.CompanyName, Stocks.Type, Stocks.PricePerShare, Stocks.NumShare " +
                "FROM Stocks, Orders, Trade " +
                "WHERE Orders.OrderId = Trade.OrderId AND Trade.StockId = Stocks.StockSymbol " +
                "Group By Stocks.StockSymbol " +
                "Order By Count(Stocks.StockSymbol) DESC";

        ResultSet rs = MysqlConn.runSelectQuery(query);

        try {
            while(rs.next()){
                Stock stock = new Stock();
                stock.setName(rs.getString(2));
                stock.setNumShares(rs.getInt(5));
                stock.setSymbol(rs.getString(1));
                stock.setType(rs.getString(3));
                stock.setPrice(rs.getFloat(4));
                stocks.add(stock);
            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return stocks;

	}

    public List<Stock> getCustomerBestsellers(String customerID) {

		/*
		 * The students code to fetch data from the database will be written here.
		 * Get list of customer bestseller stocks
		 */

        ArrayList<Stock> stocks = new ArrayList<Stock>();
        MysqlConn.initalizeConnection();
        String query = "SELECT Stocks.StockSymbol, Stocks.CompanyName, Stocks.Type, Stocks.PricePerShare, Stocks.NumShare " +
                "FROM Stocks, Orders, Trade, Clients, Accounts" +
                "WHERE Orders.OrderId = Trade.OrderId AND Trade.StockId = Stocks.StockSymbol AND Clients.ClientId = Accounts.ClientId AND Trade.AccountId = Accounts.AccountId AND Clients.ClientId = "+ customerID + " " +
                "Group By Stocks.StockSymbol " +
                "Order By Count(Stocks.StockSymbol) DESC";

        ResultSet rs = MysqlConn.runSelectQuery(query);

        try {
            while(rs.next()){
                Stock stock = new Stock();
                stock.setName(rs.getString(2));
                stock.setNumShares(rs.getInt(5));
                stock.setSymbol(rs.getString(1));
                stock.setType(rs.getString(3));
                stock.setPrice(rs.getFloat(4));
                stocks.add(stock);
            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return stocks;

    }

	public List getStocksByCustomer(String customerId) {

		/*
		 * The students code to fetch data from the database will be written here
		 * Get stockHoldings of customer with customerId
		 */

        ArrayList<Stock> stocks = new ArrayList<Stock>();
        MysqlConn.initalizeConnection();
        String query = "SELECT Stocks.StockSymbol, Stocks.CompanyName, Stocks.Type, Stocks.PricePerShare, Stocks.NumShare " +
                "FROM Stocks, Orders, Trade, Clients, Accounts " +
                "WHERE Orders.OrderId = Trade.OrderId AND Trade.StockId = Stocks.StockSymbol AND Clients.ClientId = Accounts.ClientId AND Trade.AccountId = Accounts.AccountId AND Clients.ClientId = "+ customerId + ";";

        ResultSet rs = MysqlConn.runSelectQuery(query);

        try {
            while(rs.next()){
                Stock stock = new Stock();
                stock.setName(rs.getString(2));
                stock.setNumShares(rs.getInt(5));
                stock.setSymbol(rs.getString(1));
                stock.setType(rs.getString(3));
                stock.setPrice(rs.getFloat(4));
                stocks.add(stock);
            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return stocks;
	}

    public List<Stock> getStocksByName(String name) {

		/*
		 * The students code to fetch data from the database will be written here
		 * Return list of stocks matching "name"
		 */

        ArrayList<Stock> stocks = new ArrayList<Stock>();
        MysqlConn.initalizeConnection();
        String query = "SELECT StockSymbol, CompanyName, Type, PricePerShare, NumShare " +
                "FROM Stocks " +
                "WHERE CompanyName = \'" + name + "\'";

        ResultSet rs = MysqlConn.runSelectQuery(query);

        try {
            while(rs.next()){
                Stock stock = new Stock();
                stock.setName(rs.getString(2));
                stock.setNumShares(rs.getInt(5));
                stock.setSymbol(rs.getString(1));
                stock.setType(rs.getString(3));
                stock.setPrice(rs.getFloat(4));
                stocks.add(stock);
            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return stocks;
    }

    public List<Stock> getStockSuggestions(String customerID) {

		/*
		 * The students code to fetch data from the database will be written here
		 * Return stock suggestions for given "customerId"
		 */

        ArrayList<Stock> stocks = new ArrayList<Stock>();
        MysqlConn.initalizeConnection();
        String query = "SELECT StockSymbol, CompanyName, Type, Stocks.PricePerShare, Stocks.NumShare " +
                "FROM Stocks, Accounts, Trade, Orders " +
                "WHERE Trade.OrderId = Orders.OrderId AND Orders.OrderType = 'Buy' AND Trade.AccountId = Accounts.AccountId AND Accounts.ClientId = \'" + customerID + "\'";

        ResultSet rs = MysqlConn.runSelectQuery(query);
        if(rs != null){
        try {
            while(rs.next()){
                Stock stock = new Stock();
                stock.setName(rs.getString(2));
                stock.setNumShares(rs.getInt(5));
                stock.setSymbol(rs.getString(1));
                stock.setType(rs.getString(3));
                stock.setPrice(rs.getFloat(4));
                stocks.add(stock);
            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        }
        return stocks;

    }
 
    public List<Stock> getStockPriceHistory(String stockSymbol) {


        ArrayList<Stock> priceHistory = new ArrayList<Stock>();

        MysqlConn.initalizeConnection();
        String query = "SELECT StockSymbol, CompanyName, Type, PricePerShare, NumShare " +
                "Stocks " +
                "WHERE Stocks.StockSymbol =  \'" + stockSymbol + "\'";

        ResultSet rs = MysqlConn.runSelectQuery(query);
        Stock stock = new Stock();
        
        try {
            while(rs.next()){
                    stock.setName(rs.getString(2));
                    stock.setNumShares(rs.getInt(5));
                    stock.setSymbol(rs.getString(1));
                    stock.setType(rs.getString(3));
                    stock.setPrice(rs.getFloat(4));
                    priceHistory.add(stock);
            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        
        query = "SELECT Orders.PricePerShare, Orders.NumShare " +
                "FROM Orders, Trade " +
                "WHERE S Orders.OrderId = Trade.OrderId AND Trade.StockId =  \'" + stockSymbol + "\'";
        rs = MysqlConn.runSelectQuery(query);
        try {
            while(rs.next()){

                    Stock temp = new Stock();
                    temp.setName(stock.getName());
                    temp.setNumShares(rs.getInt(2));
                    temp.setSymbol(stock.getSymbol());
                    temp.setType(stock.getType());
                    temp.setPrice(rs.getFloat(1));
                    priceHistory.add(temp);


            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }        
        
        
        
        return priceHistory;
    }
    public List<String> getStockTypes() {

		/*
		 * The students code to fetch data from the database will be written here.
		 * Populate types with stock types
		 */

        ArrayList<String> types = new ArrayList<String>();
        MysqlConn.initalizeConnection();
        String query = "SELECT Type " +
                "FROM Stocks;";

        ResultSet rs = MysqlConn.runSelectQuery(query);

        try {
            while(rs.next()){
                String s = rs.getString(1);
                types.add(s);
            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return types;

    }

    public List<Stock> getStockByType(String stockType) {

		/*
		 * The students code to fetch data from the database will be written here
		 * Return list of stocks of type "stockType"
		 */

        ArrayList<Stock> stocks = new ArrayList<Stock>();
        MysqlConn.initalizeConnection();
        String query = "SELECT StockSymbol, CompanyName, Type, PricePerShare, NumShare " +
                "FROM Stocks " +
                "WHERE Type = \'" + stockType + "\'";

        ResultSet rs = MysqlConn.runSelectQuery(query);

        try {
            while(rs.next()){
                Stock stock = new Stock();
                stock.setName(rs.getString(2));
                stock.setNumShares(rs.getInt(5));
                stock.setSymbol(rs.getString(1));
                stock.setType(rs.getString(3));
                stock.setPrice(rs.getFloat(4));
                stocks.add(stock);
            }
        }catch (Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return stocks;
    }
}
