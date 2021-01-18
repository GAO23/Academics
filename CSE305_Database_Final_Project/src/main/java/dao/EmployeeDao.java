package dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Customer;
import model.Employee;
import model.Location;
import model.Login;
import sun.rmi.runtime.Log;

public class EmployeeDao {
	/*
	 * This class handles all the database operations related to the employee table
	 */

    public Employee getDummyEmployee()
    {
        Employee employee = new Employee();

        Location location = new Location();
        location.setCity("Stony Brook");
        location.setState("NY");
        location.setZipCode(11790);

		/*Sample data begins*/
        employee.setEmail("shiyong@cs.sunysb.edu");
        employee.setFirstName("Shiyong");
        employee.setLastName("Lu");
        employee.setLocation(location);
        employee.setAddress("123 Success Street");
        employee.setStartDate("2006-10-17");
        employee.setTelephone("5166328959");
        employee.setEmployeeID("631-413-5555");
        employee.setHourlyRate(100);
		/*Sample data ends*/

        return employee;
    }

    public List<Employee> getDummyEmployees()
    {
       List<Employee> employees = new ArrayList<Employee>();

        for(int i = 0; i < 10; i++)
        {
            employees.add(getDummyEmployee());
        }

        return employees;
    }

	public String addEmployee(Employee employee) {

		/*
		 * All the values of the add employee form are encapsulated in the employee object.
		 * These can be accessed by getter methods (see Employee class in model package).
		 * e.g. firstName can be accessed by employee.getFirstName() method.
		 * The sample code returns "success" by default.
		 * You need to handle the database insertion of the employee details and return "success" or "failure" based on result of the database insertion.
		 */

		String query = String.format("INSERT IGNORE INTO  Locations " +
						"values(%s,\'%s\', \'%s\');",
				Integer.toString(employee.getLocation().getZipCode()),
				employee.getLocation().getCity(),
				employee.getLocation().getState(),
				Integer.toString(employee.getLocation().getZipCode())
		);

		MysqlConn.initalizeConnection();
		boolean success = MysqlConn.updateDeleteInsertQuery(query);
		// don't need to check success here because location can already exists

		query = String.format("INSERT INTO  Persons " +
				"values(%s,\'%s\', \'%s\', \'%s\', %s, \'%s\' ,%s)",
				employee.getSsn(),
				employee.getLastName(),
				employee.getFirstName(),
				employee.getAddress(),
				employee.getLocation().getZipCode(),
				employee.getEmail(),
				employee.getTelephone()
		);
		success = MysqlConn.updateDeleteInsertQuery(query);
		if (!success) return "failure";

		query = String.format("INSERT INTO  Employees (SSN, StartDate, HourlyRate) " +
						"values(%s, \'%s\', %s)",
				Integer.parseInt(employee.getSsn()),
				employee.getStartDate().toString(),
				Float.toString(employee.getHourlyRate())
		);
		success = MysqlConn.updateDeleteInsertQuery(query);
		if (!success) {
			query = String.format("DELETE FROM `Persons` WHERE SSN = %s;", employee.getSsn());
			MysqlConn.updateDeleteInsertQuery(query);
			return "failure";
		} else{
			LoginDao.newUserPersonIdHolder.personId = employee.getSsn();
			return "success";
		}
 	}

	public String editEmployee(Employee employee) {
		/*
		 * All the values of the edit employee form are encapsulated in the employee object.
		 * These can be accessed by getter methods (see Employee class in model package).
		 * e.g. firstName can be accessed by employee.getFirstName() method.
		 * The sample code returns "success" by default.
		 * You need to handle the database update and return "success" or "failure" based on result of the database update.
		 */
		MysqlConn.initalizeConnection();

		String query = String.format("UPDATE `Locations` " +
						"SET City = \'%s\',  State = \'%s\' " +
						"WHERE ZipCode  = %s;",
				employee.getLocation().getCity(),
				employee.getLocation().getState(),
				Integer.toString(employee.getLocation().getZipCode())
		);
		boolean success = MysqlConn.updateDeleteInsertQuery(query);

		if(!success){
			query = String.format("INSERT INTO Locations (ZipCode, City, State) " +
					"VALUES(%s, \'%s\', \'%s\')",
					Integer.toString(employee.getLocation().getZipCode()),
					employee.getLocation().getCity(),
					employee.getLocation().getState()
					);
			MysqlConn.updateDeleteInsertQuery(query);
		}

		query = String.format("UPDATE `Persons` " +
						"SET LastName = \'%s\', FirstName = \'%s\', Address = \'%s\', ZipCode = %s, Email = \'%s\', Telephone = %s " +
						"WHERE SSN = " +
						"(SELECT Employees.SSN " +
						"FROM Employees " +
						"WHERE EmployeeId = %s);",
				employee.getLastName(),
				employee.getFirstName(),
				employee.getAddress(),
				Integer.toString(employee.getLocation().getZipCode()),
				employee.getEmail(),
				employee.getTelephone(),
				employee.getEmployeeID()
		);
		System.out.println(query);
		success = MysqlConn.updateDeleteInsertQuery(query);
		if(!success) return "failure";

		query = String.format("UPDATE `Employees` " +
				"SET StartDate = %s, HourlyRate = %s " +
				"WHERE EmployeeId = %s;",
				employee.getStartDate(),
				employee.getHourlyRate(),
				employee.getEmployeeID()
		);
		System.out.println(query);
		success = MysqlConn.updateDeleteInsertQuery(query);
		return (success) ? "success" : "failure";

	}

	public String deleteEmployee(String employeeID) {
		/*
		 * employeeID, which is the Employee's ID which has to be deleted, is given as method parameter
		 * The sample code returns "success" by default.
		 * You need to handle the database deletion and return "success" or "failure" based on result of the database deletion.
		 */
		String query = String.format("DELETE FROM `Persons` WHERE SSN = " +
				"(SELECT SSN " +
				"FROM Passwords, Employees " +
				"WHERE EmployeeId = %s AND Employees.SSN = Passwords.PersonId);", employeeID);

		MysqlConn.initalizeConnection();
		boolean success = MysqlConn.updateDeleteInsertQuery(query);
		if(!success) return "failure";
		return (success) ? "success" : "failure";


	}

	
	public List<Employee> getEmployees() {

		/*
		 * The students code to fetch data from the database will be written here
		 * Query to return details about all the employees must be implemented
		 * Each record is required to be encapsulated as a "Employee" class object and added to the "employees" List
		 */

		List<Employee> employees = new ArrayList<Employee>();
		String query = "SELECT EmployeeId, Persons.SSN, StartDate, HourlyRate, LastName, FirstName, Address, Persons.ZipCode, Telephone, City, State, Email " +
				"FROM  Employees, Persons, Locations " +
				"WHERE Persons.SSN = Employees.SSN AND Persons.ZipCode = Locations.ZipCode;";

		MysqlConn.initalizeConnection();
		ResultSet rs = MysqlConn.runSelectQuery(query);

		try {
			while (rs.next()) {
				Employee employee = new Employee();
				employee.setEmployeeID(rs.getString(1));
				employee.setHourlyRate(rs.getFloat(4));
				employee.setStartDate(rs.getDate(3).toString());
				employee.setAddress(rs.getString(7));
				employee.setLastName(rs.getString(5));
				employee.setFirstName(rs.getString(6));
				employee.setAddress(rs.getString(7));
				Location location = new Location();
				location.setCity(rs.getString(10));
				location.setState(rs.getString(11));
				location.setZipCode(Integer.parseInt(rs.getString(8)));
				employee.setLocation(location);
				employee.setTelephone(rs.getString(9));
				employee.setEmail(rs.getString(12));
				employee.setSsn(rs.getString(2));
				employees.add(employee);
			}
		}catch(Exception e){
			System.out.println("debug: encounter error: " + e.toString());
			e.printStackTrace();
		}

		return employees;
	}

	public Employee getEmployee(String employeeID) {

		/*
		 * The students code to fetch data from the database based on "employeeID" will be written here
		 * employeeID, which is the Employee's ID who's details have to be fetched, is given as method parameter
		 * The record is required to be encapsulated as a "Employee" class object
		 */


		String query = "SELECT EmployeeId, Persons.SSN, StartDate, HourlyRate, LastName, FirstName, Address, Persons.ZipCode, Telephone, City, State, Email " +
				"FROM  Employees, Persons, Locations " +
				"WHERE EmployeeId = \'" + employeeID + "\' AND Persons.SSN = Employees.SSN AND Persons.ZipCode = Locations.ZipCode;";

		MysqlConn.initalizeConnection();
		ResultSet rs = MysqlConn.runSelectQuery(query);

		try{
			while(rs.next()){
				Employee employee = new Employee();
				employee.setEmployeeID(rs.getString(1));
				employee.setHourlyRate(rs.getFloat(4));
				employee.setStartDate(rs.getDate(3).toString());
				employee.setAddress(rs.getString(7));
				employee.setLastName(rs.getString(5));
				employee.setFirstName(rs.getString(6));
				employee.setAddress(rs.getString(7));
				Location location = new Location();
				location.setCity(rs.getString(10));
				location.setState(rs.getString(11));
				location.setZipCode(Integer.parseInt(rs.getString(8)));
				employee.setLocation(location);
				employee.setTelephone(rs.getString(9));
				employee.setEmail(rs.getString(12));
				employee.setSsn(rs.getString(2));
				return employee;
			}
		}catch (Exception e){
			System.out.println("debug: Encounter this error: " + e.toString());
			e.printStackTrace();
		}

		return null;
	}
	
	public Employee getHighestRevenueEmployee() {
		
		/*
		 * The students code to fetch employee data who generated the highest revenue will be written here
		 * The record is required to be encapsulated as a "Employee" class object
		 */

		Employee employee = new Employee();

		String query = "SELECT EmployeeId, Persons.SSN, StartDate, HourlyRate, LastName, FirstName, Address, Persons.ZipCode, Telephone, City, State, Email, sum(Transactions.Fee) as Revenue " +
				"FROM Transactions, Employees, Trade, Persons, Locations  " +
				"WHERE Persons.SSN  = Employees.SSN AND Persons.ZipCode = Locations.ZipCode AND Trade.TransactionId = Transactions.TransactionId AND Trade.BrokerId = Employees.EmployeeId " +
				"GROUP by Employees.EmployeeId " +
				"ORDER by Revenue DESC " +
				"LIMIT 1;";


		MysqlConn.initalizeConnection();
		ResultSet rs = MysqlConn.runSelectQuery(query);

		try{
			while(rs.next()){
				employee.setEmployeeID(rs.getString(1));
				employee.setHourlyRate(rs.getFloat(4));
				employee.setStartDate(rs.getDate(3).toString());
				employee.setAddress(rs.getString(7));
				employee.setLastName(rs.getString(5));
				employee.setFirstName(rs.getString(6));
				employee.setAddress(rs.getString(7));
				Location location = new Location();
				location.setCity(rs.getString(10));
				location.setState(rs.getString(11));
				location.setZipCode(Integer.parseInt(rs.getString(8)));
				employee.setLocation(location);
				employee.setTelephone(rs.getString(9));
				employee.setEmail(rs.getString(12));
				employee.setSsn(rs.getString(2));
				return employee;
			}
		}catch (Exception e){
			System.out.println("debug: Encounter this error: " + e.toString());
			e.printStackTrace();
		}

		return null;
	}

	public String getEmployeeID(String username) {
		/*
		 * The students code to fetch data from the database based on "username" will be written here
		 * username, which is the Employee's email address who's Employee ID has to be fetched, is given as method parameter
		 * The Employee ID is required to be returned as a String
		 */
		String query = "SELECT EmployeeId " +
				"FROM  Employees, Passwords " +
				"WHERE Employees.SSN = Passwords.PersonID AND Passwords.userName = \'" + username +"\';";

		MysqlConn.initalizeConnection();
		ResultSet rs = MysqlConn.runSelectQuery(query);

		try{
			while(rs.next()){
				return rs.getString(1);
			}
		}catch (Exception e){
			System.out.println("debug: Encounter this error: " + e.toString());
			e.printStackTrace();
		}

		return "";
	}

}
