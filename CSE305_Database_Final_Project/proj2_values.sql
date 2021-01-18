insert into Locations values(11790, "Stony Brook", "NY");
insert into Locations values(11794, "Stony Brook", "NY");
insert into Locations values(93536, "Los Angeles", "CA");

insert into Persons (SSN, LastName, FirstName, Address, ZipCode, Email, Telephone) values(111111111, "Yang", "Shang", "123 Success Street", 11790, 'xyz@gmail.com', 5166328959); 
insert into  Persons (SSN, LastName, FirstName, Address, ZipCode, Email, Telephone) values(222222222, "Du", "Victor", "456 Fortune Road", 11790, '123@gmail.com', 5166324360); 
insert into  Persons (SSN, LastName, FirstName, Address, ZipCode, Email, Telephone) values(333333333, "Smith", "John", "789 Peace Blvd.",  93536,'124@gmail.com' , 3154434321); 
insert into  Persons (SSN, LastName, FirstName, Address, ZipCode, Email, Telephone) values(444444444, "Phillip", "Lewis", "135 Knowledge Lane", 11794, '143@gmail.com', 5166668888);
insert into  Persons (SSN, LastName, FirstName, Address, ZipCode, Email, Telephone) values(123456789, "Smith", "David", "123 College road", 11790, '12343@gmail.com', 5162152345); 
insert into  Persons (SSN, LastName, FirstName, Address, ZipCode, Email, Telephone) values(789123456, "Warren", "David", "456 Sunken Street", 11794, 'WarrenDavid@gmail.com', 6316329987);  


insert into Clients values("syang@cs.sunysb.edu", 1, 1234567812345678, 111111111);
insert into Clients values("vicdu@cs.sunysb.edu", 1, 5678123456781234, 222222222);
insert into Clients values("jsmith@ic.sunysb.edu", 1, 2345678923456789, 333333333);
insert into Clients values("pml@cs.sunysb.edu", 1, 6789234567892345, 444444444);

insert into Stocks values("GM", "General Motors", "automotive", 34.23, 250);
insert into Stocks values("IBM", "IBM", "computer", 91.41, 500);
insert into Stocks values("F", "Ford", "automotive", 9.0, 750);



insert into Accounts (DateOpened, ClientID, Stock, NumShare) values ("10-1-06",  444444444, "GM", 250);
insert into Accounts (DateOpened, ClientID, Stock, NumShare) values ("10-1-06",  444444444, "F", 100);
insert into Accounts (DateOpened, ClientID, Stock, NumShare) values ("10-1-06",  222222222, "IBM", 50);


insert into Employees (SSN, StartDate, HourlyRate) values(123456789, "11-1-05", 60.6);
insert into Employees (SSN, StartDate, HourlyRate) values(789123456, "2-2-06", 50.9);

insert into Orders (NumShare, PricePerShare, DateTime, Percentage, PriceType, OrderType) values(75, 34.23, '2019-01-01 00:00:00', 5, "Market", "buy");
insert into Orders (NumShare, PricePerShare, DateTime, Percentage, PriceType, OrderType) values(10, 91.4, '2019-01-01 00:00:00', 6, "trailing stop 10%", "sell");
insert into Orders (NumShare, PricePerShare, DateTime, Percentage, PriceType, OrderType) values(10, 9.0, '2019-01-01 00:00:02', 7, "	$90", "sell");


insert into Transactions (Fee, DateTimes, PricePerShare) values (34.55, '2018-01-01 00:00:00', 45.34);
insert into Transactions (Fee, DateTimes, PricePerShare) values (34.55, '2018-01-01 00:00:01', 5.34);
insert into Transactions (Fee, DateTimes, PricePerShare) values (34.55, '2018-01-01 00:00:02', 5.34);

insert into Trade (AccountId, BrokerId, TransactionId, OrderId, StockId) values (1,1,1,1, "GM");
insert into Trade (AccountId, BrokerId, TransactionId, OrderId, StockId) values (3,2,2,2, "IBM");
insert into Trade (AccountId, BrokerId, TransactionId, OrderId, StockId) values (3,2,3,3, "IBM");

INSERT INTO Passwords (PersonId, Password, Role, userName) values (789123456, aes_encrypt('password', 'database_key'), 'manager', 'WarrenDavid@gmail.com');
INSERT INTO Passwords (PersonId, Password, Role, userName) values (123456789, aes_encrypt('password1', 'database_key'), 'customerRepresentative', 'employee1@gmail.com');


