package dao;

import model.Login;

import java.sql.ResultSet;

public class LoginDao {
	/*
	 * This class handles all the database operations related to login functionality
	 */

	public static final String HASHKEY = "database_key";

	public Login login(String username, String password, String role) {
		/*
		 * Return a Login object with role as "manager", "customerRepresentative" or "customer" if successful login
		 * Else, return null
		 * The role depends on the type of the user, which has to be handled in the database
		 * username, which is the email address of the user, is given as method parameter
		 * password, which is the password of the user, is given as method parameter
		 * Query to verify the username and password and fetch the role of the user, must be implemented
		 */

		/*Sample data begins*/
		MysqlConn.initalizeConnection();
		String query =
				"SELECT PersonId, userName, Role, cast(aes_decrypt(Password, 'database_key') as char(32)) " +
				"FROM Passwords " +
				"WHERE userName = \'" + username + "\';";
		ResultSet rs = MysqlConn.runSelectQuery(query);
		// write your sql here
		try {
			while (rs.next()) {
				if (!rs.getString(4).equals(password)) break; // password is the 4th coloumn. Password is hashed as in the real world
				if(!rs.getString(3).equals(role)) break; // role is the 3rd column
				Login login = new Login();
				login.setPassword(password);
				login.setUsername(username);
				login.setRole(role);
				return login;
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return null;
		
	}
	
	public String addUser(Login login) {
		/*
		 * Query to insert a new record for user login must be implemented
		 * login, which is the "Login" Class object containing username and password for the new user, is given as method parameter
		 * The username and password from login can get accessed using getter methods in the "Login" model
		 * e.g. getUsername() method will return the username encapsulated in login object
		 * Return "success" on successful insertion of a new user
		 * Return "failure" for an unsuccessful database operation
		 */
		String query = "Insert into Passwords (PersonId, Password, Role, userName) values (\'" + newUserPersonIdHolder.personId + "\', aes_encrypt(\'" + login.getPassword() + "\', \'" + HASHKEY + "\'), \'" + login.getRole() + "\', \'" + login.getUsername() + "\')";
		MysqlConn.initalizeConnection();
		boolean result = MysqlConn.updateDeleteInsertQuery(query);
		return (result) ? "success" : "failure";
	}

	public static class newUserPersonIdHolder{
		public static String personId = "";
	}


}
