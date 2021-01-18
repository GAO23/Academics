package dao;

import model.*;

import java.sql.ResultSet;
import java.util.*;

public class OrderDao {

    public Order getDummyTrailingStopOrder() {
        TrailingStopOrder order = new TrailingStopOrder();

        order.setId(1);
        order.setDatetime(new Date());
        order.setNumShares(5);
        order.setPercentage(12.0);
        return order;
    }

    public Order getDummyMarketOrder() {
        MarketOrder order = new MarketOrder();

        order.setId(1);
        order.setDatetime(new Date());
        order.setNumShares(5);
        order.setBuySellType("buy");
        return order;
    }

    public Order getDummyMarketOnCloseOrder() {
        MarketOnCloseOrder order = new MarketOnCloseOrder();

        order.setId(1);
        order.setDatetime(new Date());
        order.setNumShares(5);
        order.setBuySellType("buy");
        return order;
    }

    public Order getDummyHiddenStopOrder() {
        HiddenStopOrder order = new HiddenStopOrder();

        order.setId(1);
        order.setDatetime(new Date());
        order.setNumShares(5);
        order.setPricePerShare(145.0);
        return order;
    }

    public List<Order> getDummyOrders() {
        List<Order> orders = new ArrayList<Order>();

        for (int i = 0; i < 3; i++) {
            orders.add(getDummyTrailingStopOrder());
        }

        for (int i = 0; i < 3; i++) {
            orders.add(getDummyMarketOrder());
        }

        for (int i = 0; i < 3; i++) {
            orders.add(getDummyMarketOnCloseOrder());
        }

        for (int i = 0; i < 3; i++) {
            orders.add(getDummyHiddenStopOrder());
        }

        return orders;
    }

    public String submitOrder(Order order, Customer customer, Employee employee, Stock stock) {

		/*
		 * Student code to place stock order
		 * Employee can be null, when the order is placed directly by Customer
         * */

		/*Sample data begins*/
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = sdf.format(order.getDatetime());

                String query = "insert into Orders (NumShare, DateTime) values(" + order.getNumShares() + ", \'" + currentTime + "\');";
		MysqlConn.initalizeConnection();
		boolean success = MysqlConn.updateDeleteInsertQuery(query);
                
		if (!success) return "failure";

                if (employee != null){
		query = String.format("insert into Trade (AccountId, BrokerId, OrderId, StockId) values(\'%s\',%s, %s, %s);",
				customer.getAccountNumber(),
				employee.getEmployeeID(),
				order.getId(),
                                stock.getSymbol()
		);
                
                }
                else{
                		query = String.format("insert into Trade (AccountId, OrderId, StockId) values(\'%s\',%s, %s);",
				customer.getAccountNumber(),
				order.getId(),
                                stock.getSymbol()
		);
                }
		System.out.println(query);

		success = MysqlConn.updateDeleteInsertQuery(query);
		if (!success) {
			return "failure";
		} else{
			return "success";
		}


		/*Sample data ends*/

    }

    public List<Order> getOrderByStockSymbol(String stockSymbol) {
        /*
         * Student code to get orders by stock symbol
         */

        ArrayList<Order> orders = new ArrayList<Order>();

        MysqlConn.initalizeConnection();
        String query = String.format("SELECT NumShare, PricePerShare, Orders.OrderId, DateTime, Percentage, PriceType, OrderType " +
                "FROM Orders, Trade " +
                "WHERE Orders.OrderId = Trade.OrderId AND Trade.StockId = \'%s\';",
                stockSymbol);

        ResultSet rs = MysqlConn.runSelectQuery(query);
        try {
            while (rs.next()) {
                Order order = new Order();
                order.setNumShares(rs.getInt(1));
                order.setId(rs.getInt(3));
                order.setDatetime(rs.getTime(4));
                orders.add(order);
            }
        }catch(Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return orders;
    }


    public List<Order> getOrderByCustomerName(String customerName) {
         /*
		 * Student code to get orders by customer name
         */
        ArrayList<Order> orders = new ArrayList<Order>();
        String[] name = customerName.split(" ");
        MysqlConn.initalizeConnection();
        String query = String.format("SELECT Orders.NumShare, PricePerShare, Orders.OrderId, DateTime, Percentage, PriceType, OrderType " +
                "FROM Orders, Trade, Accounts, Persons " +
                "WHERE Orders.OrderId = Trade.OrderId AND Trade.AccountId = Accounts.AccountId AND Accounts.ClientId = Persons.SSN AND Persons.FirstName = \'%s\' AND Persons.LastName = \'%s\';",
                name[0], name[1]);

        ResultSet rs = MysqlConn.runSelectQuery(query);
        try {
            while (rs.next()) {
                Order order = new Order();
                order.setNumShares(rs.getInt(1));
                order.setId(rs.getInt(3));
                order.setDatetime(rs.getTime(4));
                orders.add(order);
            }
        }catch(Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrderHistory(String customerId) {
        /*
		 * The students code to fetch data from the database will be written here
		 * Show orders for given customerId
		 */
       ArrayList<Order> orders = new ArrayList<Order>();

        MysqlConn.initalizeConnection();
        String query = String.format("SELECT NumShare, PricePerShare, Orders.OrderId, DateTime, Percentage, PriceType, OrderType " +
                "FROM Orders, Trade, Accounts " +
                "WHERE Orders.OrderId = Trade.OrderId AND Trade.AccountId = Accounts.AccountId AND Accounts.ClientId= \'%s\';",
                customerId);

        ResultSet rs = MysqlConn.runSelectQuery(query);
        try {
            while (rs.next()) {
                Order order = new Order();
                order.setNumShares(rs.getInt(1));
                order.setId(rs.getInt(3));
                order.setDatetime(rs.getTime(4));
                orders.add(order);
            }
        }catch(Exception e){
            System.out.println("debug: Ecounter this error: " + e.toString());
            e.printStackTrace();
        }
        return orders;
    }


    public List<OrderPriceEntry> getOrderPriceHistory(String orderId) {

        /*
		 * The students code to fetch data from the database will be written here
		 * Query to view price history of hidden stop order or trailing stop order
		 * Use setPrice to show hidden-stop price and trailing-stop price
		 */
        List<OrderPriceEntry> orderPriceHistory = new ArrayList<OrderPriceEntry>();

        for (int i = 0; i < 10; i++) {
            OrderPriceEntry entry = new OrderPriceEntry();
            entry.setOrderId(orderId);
            entry.setDate(new Date());
            entry.setStockSymbol("aapl");
            entry.setPricePerShare(150.0);
            entry.setPrice(100.0);
            orderPriceHistory.add(entry);
        }
        return orderPriceHistory;
    }
}
