
CREATE TABLE Locations (
ZipCode INTEGER not null,
City CHAR(20) NOT NULL,
State CHAR(20) NOT NULL,
primary key (ZipCode)
);

CREATE TABLE Persons (
SSN INTEGER,
LastName CHAR(100) NOT NULL,
FirstName CHAR(100) NOT NULL,
Address CHAR(100),
ZipCode INTEGER,
Email varchar(100),
Telephone BIGINT,
PRIMARY KEY (SSN),
FOREIGN KEY (ZipCode) REFERENCES Locations (ZipCode)
ON DELETE NO ACTION
ON UPDATE CASCADE );


CREATE TABLE Employees (
EmployeeId INTEGER auto_increment,
SSN INTEGER,
StartDate DATE,
HourlyRate DECIMAL(10,2),
PRIMARY KEY (EmployeeId),
FOREIGN KEY (SSN) REFERENCES Persons (SSN)
ON DELETE CASCADE
ON UPDATE CASCADE );



CREATE TABLE Clients (
Email CHAR(32),
Rating INTEGER,
CreditCardNumber BIGINT,
ClientId INTEGER,
PRIMARY KEY (ClientId),
FOREIGN KEY (ClientId) REFERENCES Persons(SSN)
ON DELETE CASCADE
ON UPDATE CASCADE );

CREATE TABLE Stocks (
StockSymbol CHAR(20) NOT NULL,
CompanyName CHAR(20) NOT NULL,
Type CHAR(20) NOT NULL,
PricePerShare DECIMAL(10, 2),
NumShare int,
PRIMARY KEY (StockSymbol));

CREATE TABLE Accounts (
AccountId INTEGER auto_increment,
DateOpened DATE,
ClientId INTEGER,
PRIMARY KEY (AccountId),
Stock char(20) NOT NULL,
NumShare int,
FOREIGN KEY (ClientId) REFERENCES Clients (ClientId)
ON DELETE SET NULL ON UPDATE CASCADE,
FOREIGN KEY (Stock) REFERENCES Stocks (StockSymbol)
ON DELETE NO ACTION ON UPDATE CASCADE);

CREATE TABLE Transactions (
TransactionId INTEGER auto_increment,
Fee DECIMAL(10,2),
DateTimes DATETIME,
PricePerShare DECIMAL(10,2),
PRIMARY KEY (TransactionId));

CREATE TABLE Orders(
NumShare INTEGER,
PricePerShare DECIMAL(10,2),
OrderId INTEGER auto_increment,
DateTime DATETIME,
Percentage int,
PriceType CHAR(50),
OrderType CHAR(50),
PRIMARY KEY (OrderId)
);




CREATE TABLE Trade (
AccountId INTEGER,
BrokerId INTEGER,
TransactionId INTEGER,
OrderId INTEGER,
StockId CHAR(20),
FOREIGN KEY (AccountId) REFERENCES Accounts (AccountId)
ON DELETE NO ACTION
ON UPDATE CASCADE,
FOREIGN KEY (BrokerId) REFERENCES Employees (EmployeeId)
ON DELETE SET NULL
ON UPDATE CASCADE,
FOREIGN KEY (TransactionId) REFERENCES Transactions (TransactionId)
ON DELETE NO ACTION
ON UPDATE CASCADE,
FOREIGN KEY (OrderId) REFERENCES Orders (OrderId)
ON DELETE NO ACTION
ON UPDATE CASCADE,
FOREIGN KEY (StockId) REFERENCES Stocks (StockSymbol)
ON DELETE NO ACTION
ON UPDATE CASCADE );

create Table Passwords(
PersonId integer unique,
Password VARBINARY(255) not null,
Role VARCHAR(100) not null,
userName varchar(100) not null unique,
FOREIGN KEY (PersonId) REFERENCES Persons (SSN)
ON DELETE cascade
ON UPDATE CASCADE
);

SET SQL_SAFE_UPDATES=0;


-- SET foreign_key_checks = 0;
-- 
-- SET foreign_key_checks = 1;



