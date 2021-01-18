package dao;

import java.awt.peer.SystemTrayPeer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Customer;
import model.Location;

public class CustomerDao {
	/*
	 * This class handles all the database operations related to the customer table
	 */

	public Customer getDummyCustomer() {
		Location location = new Location();
		location.setZipCode(11790);
		location.setCity("Stony Brook");
		location.setState("NY");

		Customer customer = new Customer();
		customer.setId("111-11-1111");
		customer.setAddress("123 Success Street");
		customer.setLastName("Lu");
		customer.setFirstName("Shiyong");
		customer.setEmail("shiyong@cs.sunysb.edu");
		customer.setLocation(location);
		customer.setTelephone("5166328959");
		customer.setCreditCard("1234567812345678");
		customer.setRating(1);

		return customer;
	}

	public List<Customer> getDummyCustomerList() {
		/*Sample data begins*/
		List<Customer> customers = new ArrayList<Customer>();

		for (int i = 0; i < 10; i++) {
			customers.add(getDummyCustomer());
		}
		/*Sample data ends*/

		return customers;
	}

	/*
	 * @param String searchKeyword
	 * @return ArrayList<Customer> object
	 */
//	public List<Customer> getCustomers(String searchKeyword) {
//		/*
//		 * This method fetches one or more customers based on the searchKeyword and returns it as an ArrayList
//		 */
//		List<Customer> customers = new ArrayList<Customer>();
//
//		/*
//		 * The students code to fetch data from the database based on searchKeyword will be written here
//		 * Each record is required to be encapsulated as a "Customer" class object and added to the "customers" List
//		 */
//
//			String query = "Select Clients.ClientId, Locations.City, Locations.State, Locations.ZipCode, Clients.CreditCardNumber, Clients.Email, Clients.Rating, Persons.FirstName, Persons.LastName, Persons.Address, Persons.Telephone " +
//					"from Clients, Persons, Locations " +
//					"where Clients.ClientId = Persons.SSN and Locations.ZipCode = Persons.ZipCode  and (FirstName = \'" + searchKeyword + "\'  or LastName =  \'" + searchKeyword + "\');";
//
//			MysqlConn.initalizeConnection();
//			ResultSet re = MysqlConn.runSelectQuery(query);
//
//		try {
//			while (re.next()) {
//				Customer customer = new Customer();
//				customer.setClientId(re.getString("ClientId"));
//				customer.setAddress(re.getString("Address"));
//				customer.setLastName(re.getString("LastName"));
//				customer.setFirstName(re.getString("FirstName"));
//				customer.setTelephone(re.getString("Telephone"));
//				customer.setEmail(re.getString("Email"));
//				customer.setCreditCard(re.getString("CreditCardNumber"));
//				customer.setRating(re.getInt("Rating"));
//
//				Location location = new Location();
//				location.setZipCode(re.getInt("ZipCode"));
//				location.setState(re.getString("State"));
//				location.setCity(re.getString("City"));
//				customer.setLocation(location);
//
//				customers.add(customer);
//			}
//
//		} catch (Exception e) {
//			System.out.println("debug: Encounter this error: " + e.toString());
//			e.printStackTrace();
//		}
//
////		return getDummyCustomerList();
//		return customers;
//	}

	public List<Customer> getCustomers(String searchKeyword) {
		/*
		 * This method fetches one or more customers based on the searchKeyword and returns it as an ArrayList
		 */
		List<Customer> customers = new ArrayList<Customer>();

		/*
		 * The students code to fetch data from the database based on searchKeyword will be written here
		 * Each record is required to be encapsulated as a "Customer" class object and added to the "customers" List
		 */
		String query = "";
		if(searchKeyword == null) {
			query = "Select Clients.ClientId, Locations.City, Locations.State, Locations.ZipCode, Clients.CreditCardNumber, Clients.Email, Clients.Rating, Persons.FirstName, Persons.LastName, Persons.Address, Persons.Telephone " +
					"from Clients, Persons, Locations " +
					"where Clients.ClientId = Persons.SSN and Locations.ZipCode = Persons.ZipCode;";
		}else{

		}

		MysqlConn.initalizeConnection();
		ResultSet re = MysqlConn.runSelectQuery(query);

		try {
			while (re.next()) {
				Customer customer = new Customer();
				customer.setClientId(re.getString("ClientId"));
				customer.setAddress(re.getString("Address"));
				customer.setLastName(re.getString("LastName"));
				customer.setFirstName(re.getString("FirstName"));
				customer.setTelephone(re.getString("Telephone"));
				customer.setEmail(re.getString("Email"));
				customer.setCreditCard(re.getString("CreditCardNumber"));
				customer.setRating(re.getInt("Rating"));
				Location location = new Location();
				location.setZipCode(re.getInt("ZipCode"));
				location.setState(re.getString("State"));
				location.setCity(re.getString("City"));
				customer.setLocation(location);
				customers.add(customer);
			}

		} catch (Exception e) {
			System.out.println("debug: Encounter this error: " + e.toString());
			e.printStackTrace();
		}

//    return getDummyCustomerList();
		return customers;
	}


	public Customer getHighestRevenueCustomer() {
		/*
		 * This method fetches the customer who generated the highest total revenue and returns it
		 * The students code to fetch data from the database will be written here
		 * The customer record is required to be encapsulated as a "Customer" class object
		 */
		Customer customer = new Customer();


		String query = "Select Clients.ClientId, Persons.FirstName, Persons.LastName, Locations.ZipCode, Locations.State, Locations.City, Persons.Telephone, Clients.Rating, Clients.Email, Clients.CreditCardNumber, sum(Transactions.Fee) as Revenue " +
				"From Transactions, Clients, Persons, Locations, Trade " +
				"Where Clients.ClientId = Persons.SSN and Persons.ZipCode = Locations.ZipCode and Transactions.TransactionId = Trade.TransactionId " +
				"Group by Clients.ClientId " +
				"ORDER by Revenue DESC " +
				"LIMIT 1;";

		MysqlConn.initalizeConnection();
		ResultSet re = MysqlConn.runSelectQuery(query);

		try {
			while (re.next()) {
				customer.setClientId(re.getString("ClientId"));
				//customer.setAddress(re.getString("Address"));
				customer.setLastName(re.getString("LastName"));
				customer.setFirstName(re.getString("FirstName"));
				customer.setTelephone(re.getString("Telephone"));
				customer.setEmail(re.getString("Email"));
				customer.setCreditCard(re.getString("CreditCardNumber"));
				customer.setRating(re.getInt("Rating"));

				Location location = new Location();
				location.setZipCode(re.getInt("ZipCode"));
				location.setState(re.getString("State"));
				location.setCity(re.getString("City"));
				customer.setLocation(location);
				return customer;

			}

		} catch (Exception e) {
			System.out.println("debug: Encounter this error: " + e.toString());
			e.printStackTrace();
			System.out.println(query);

		}

		return null;
	}

	public Customer getCustomer(String customerID) {

		/*
		 * This method fetches the customer details and returns it
		 * customerID, which is the Customer's ID who's details have to be fetched, is given as method parameter
		 * The students code to fetch data from the database will be written here
		 * The customer record is required to be encapsulated as a "Customer" class object
		 */

		Customer customer = new Customer();
		String query = "select Persons.FirstName, Persons.LastName, Persons.Address, Locations.City, Locations.State, Locations.ZipCode, Persons.Telephone, Clients.ClientId, Clients.CreditCardNumber, Clients.Rating " +
				"From Clients, Persons, Locations " +
				"WHERE Clients.ClientId = \'" + customerID + "\' AND Clients.ClientId = Persons.SSN and Locations.ZipCode = Persons.ZipCode;";

		MysqlConn.initalizeConnection();
		ResultSet re = MysqlConn.runSelectQuery(query);

		try {
			while (re.next()) {
				//customer.setClientId(re.getString("ClientId"));
				customer.setFirstName(re.getString("FirstName"));
				customer.setLastName(re.getString("LastName"));
				customer.setAddress(re.getString("Address"));

				Location location = new Location();
				location.setZipCode(re.getInt("ZipCode"));
				location.setState(re.getString("State"));
				location.setCity(re.getString("City"));
				customer.setLocation(location);

				customer.setTelephone(re.getString("Telephone"));
				customer.setSsn(re.getString("ClientId"));
				customer.setCreditCard(re.getString("CreditCardNumber"));
				customer.setRating(re.getInt("Rating"));

				return customer;
			}
		} catch (Exception e) {
			System.out.println("debug: Encounter this error: " + e.toString());
			e.printStackTrace();
			System.out.println("public Customer getCustomer(String customerID)");
		}
		//return null;
		return getDummyCustomer();
	}


	public String deleteCustomer(String customerID) {

		/*
		 * This method deletes a customer returns "success" string on success, else returns "failure"
		 * The students code to delete the data from the database will be written here
		 * customerID, which is the Customer's ID who's details have to be deleted, is given as method parameter
		 */

		/*Sample data begins*/
		//customerID = getCustomerID("syang@cs.sunysb.edu");
		String query = String.format("Delete from Persons where SSN = %s;", customerID);
		System.out.println(query);
		MysqlConn.initalizeConnection();
		boolean success = MysqlConn.updateDeleteInsertQuery(query);

		if (!success) return "failure";
		return (success) ? "success" : "failure";


	}




	public String getCustomerID(String email) {
		/*
		 * This method returns the Customer's ID based on the provided email address
		 * The students code to fetch data from the database will be written here
		 * username, which is the email address of the customer, who's ID has to be returned, is given as method parameter
		 * The Customer's ID is required to be returned as a String
		 */

		String query = "select Clients.ClientId, Clients.Email " +
				"From Clients, Persons " +
				"WHERE Clients.ClientId = Persons.SSN and Clients.Email = \'" + email +"\';";

		MysqlConn.initalizeConnection();
		ResultSet re = MysqlConn.runSelectQuery(query);

		try{
			while(re.next()){
				return re.getString("ClientId");
			}
		}catch (Exception e){
			System.out.println("debug: Encounter this error: " + e.toString());
			e.printStackTrace();
		}

		return "";

		//return "111-11-1111";
	}


	public String addCustomer(Customer customer) {

		/*
		 * All the values of the add customer form are encapsulated in the customer object.
		 * These can be accessed by getter methods (see Customer class in model package).
		 * e.g. firstName can be accessed by customer.getFirstName() method.
		 * The sample code returns "success" by default.
		 * You need to handle the database insertion of the customer details and return "success" or "failure" based on result of the database insertion.
		 */

		/*Sample data begins*/
		String query = String.format("INSERT IGNORE INTO  Locations " +
						"values(%s,\'%s\', \'%s\');",
				Integer.toString(customer.getLocation().getZipCode()),
				customer.getLocation().getCity(),
				customer.getLocation().getState(),
				Integer.toString(customer.getLocation().getZipCode())
		);
		MysqlConn.initalizeConnection();
		boolean success = MysqlConn.updateDeleteInsertQuery(query);

		query = String.format("INSERT INTO  Persons " +
						"values(%s,\'%s\', \'%s\', \'%s\', %s, \'%s\' ,%s)",
				customer.getSsn(),
				customer.getLastName(),
				customer.getFirstName(),
				customer.getAddress(),
				customer.getLocation().getZipCode(),
				customer.getEmail(),
				customer.getTelephone()
		);
		success = MysqlConn.updateDeleteInsertQuery(query);
		if (!success) return "failure";

		query = String.format("INSERT INTO  Clients (Email, Rating, CreditCardNumber, ClientId ) " +
						"values(\'%s\',%s, %s, %s);",
				customer.getEmail(),
				customer.getRating(),
				customer.getCreditCard(),
				customer.getSsn()
		);
		System.out.println(query);

		success = MysqlConn.updateDeleteInsertQuery(query);
		if (!success) {
			query = String.format("DELETE FROM `Persons` WHERE SSN = %s;", customer.getSsn());
			MysqlConn.updateDeleteInsertQuery(query);
			return "failure";
		} else{
			LoginDao.newUserPersonIdHolder.personId = customer.getSsn();
			return "success";
		}



		//return "success";
		/*Sample data ends*/

	}

	public String editCustomer(Customer customer) {
		/*
		 * All the values of the edit customer form are encapsulated in the customer object.
		 * These can be accessed by getter methods (see Customer class in model package).
		 * e.g. firstName can be accessed by customer.getFirstName() method.
		 * The sample code returns "success" by default.
		 * You need to handle the database update and return "success" or "failure" based on result of the database update.
		 */

		/*Sample data begins*/
		MysqlConn.initalizeConnection();

		String query = String.format("UPDATE `Locations` " +
						"SET City = \'%s\',  State = \'%s\' " +
						"WHERE ZipCode  = %s;",
				customer.getLocation().getCity(),
				customer.getLocation().getState(),
				Integer.toString(customer.getLocation().getZipCode())
		);
		boolean success = MysqlConn.updateDeleteInsertQuery(query);

		if(!success){
			query = String.format("INSERT INTO Locations (ZipCode, City, State) " +
							"VALUES(%s, \'%s\', \'%s\')",
					Integer.toString(customer.getLocation().getZipCode()),
					customer.getLocation().getCity(),
					customer.getLocation().getState()
			);
			MysqlConn.updateDeleteInsertQuery(query);
		}

		query = String.format("UPDATE `Persons` " +
						"SET LastName = \'%s\', FirstName = \'%s\', Address = \'%s\', ZipCode = %s, Email = \'%s\', Telephone = %s " +
						"WHERE SSN = " +
						"(SELECT Clients.ClientId " +
						"FROM Clients " +
						"WHERE ClientId = %s);",
				customer.getLastName(),
				customer.getFirstName(),
				customer.getAddress(),
				Integer.toString(customer.getLocation().getZipCode()),
				customer.getEmail(),
				customer.getTelephone(),
				customer.getSsn().replaceAll("[\\s\\-()]", "")
		);
		System.out.println(query);
		success = MysqlConn.updateDeleteInsertQuery(query);
		if(!success) return "failure";

		query = String.format("UPDATE Clients\n" +
						"SET Rating = %s, CreditCardNumber = %s \n" +
						"WHERE ClientId = %s;",
				customer.getRating(),
				customer.getCreditCard(),
				customer.getSsn().replaceAll("[\\s\\-()]", "")
		);
		System.out.println(query);
		success = MysqlConn.updateDeleteInsertQuery(query);
		return (success) ? "success" : "failure";




		//return "success";
		/*Sample data ends*/

	}

	public List<Customer> getCustomerMailingList() {

		/*
		 * This method fetches the all customer mailing details and returns it
		 * The students code to fetch data from the database will be written here
		 */

		List<Customer> customers = new ArrayList<Customer>();


		String query = "select Clients.ClientId, Persons.SSN, Persons.FirstName, Persons.LastName, Persons.Address, Locations.City, Locations.State, Locations.ZipCode, Clients.Email\n" +
				"From Clients, Locations, Persons\n" +
				"where Clients.ClientId = Persons.SSN and Persons.ZipCode = Locations.ZipCode;";

		MysqlConn.initalizeConnection();
		ResultSet re = MysqlConn.runSelectQuery(query);

		try{
			while(re.next()){
				Customer customer = new Customer();
				customer.setClientId(re.getString("ClientId"));
				customer.setSsn(re.getString("SSN"));
				customer.setFirstName(re.getString("FirstName"));
				customer.setLastName(re.getString("LastName"));
				customer.setAddress(re.getString("Address"));
				customer.setEmail(re.getString("Email"));

				Location location = new Location();
				location.setZipCode(re.getInt("ZipCode"));
				location.setState(re.getString("State"));
				location.setCity(re.getString("City"));
				customer.setLocation(location);

				customers.add(customer);
			}
		}catch (Exception e){
			System.out.println("debug: Encounter this error: " + e.toString());
			e.printStackTrace();
		}


		return customers;
	}

	public List<Customer> getAllCustomers() {
		/*
		 * This method fetches returns all customers
		 */
		//return getDummyCustomerList();


		List<Customer> customers = new ArrayList<Customer>();

		/*
		 * The students code to fetch data from the database based on searchKeyword will be written here
		 * Each record is required to be encapsulated as a "Customer" class object and added to the "customers" List
		 */
		try{
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/project3", "root", "Mysql123");
			Statement st = con.createStatement();
//			ResultSet re = st.executeQuery("select * from customer where FirstName Like \'%" + searchKeyword + "%\'"
//			+ "or LastName Like \'%" + searchKeyword + "%\'");
			ResultSet re = st.executeQuery("create view ClientsAndPersons as (" +
					"Select Clients.ClientId, Persons.FirstName, Persons.LastName, Persons.Address, Persons.ZipCode, Persons.Telephone " +
					"From Clients, Persons, Locations " +
					"where Clients.ClientId = Persons.SSN " +
					"group by Clients.ClientId);" +
					"select * " +
					"from ClientsAndPersons, Locations" +
					"where ClientsAndPersons.ZipCode = Locations.ZipCode");
			while(re.next()){
				Customer customer = new Customer();
				customer.setClientId(re.getString("ClientId"));
				customer.setAddress(re.getString("Address"));
				customer.setLastName(re.getString("LastName"));
				customer.setFirstName(re.getString("FirstName"));
				customer.setTelephone(re.getString("Telephone"));
				customer.setEmail(re.getString("Email"));
				customer.setCreditCard(re.getString("CreditCardNumber"));
				customer.setRating(re.getInt("Rating"));

				Location location = new Location();
				location.setZipCode(re.getInt("ZipCode"));
				location.setState(re.getString("State"));
				location.setCity(re.getString("City"));
				customer.setLocation(location);

				customers.add(customer);
			}

		}catch (Exception e){
			System.out.println("debug: Encounter this error: " + e.toString());
			e.printStackTrace();
		}

//		return getDummyCustomerList();
		return customers;

	}
}