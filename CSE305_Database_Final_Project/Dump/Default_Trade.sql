-- MySQL dump 10.13  Distrib 5.7.26, for Linux (x86_64)
--
-- Host: localhost    Database: Default
-- ------------------------------------------------------
-- Server version	5.7.26-0ubuntu0.18.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Trade`
--

DROP TABLE IF EXISTS `Trade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Trade` (
  `AccountId` int(11) DEFAULT NULL,
  `BrokerId` int(11) DEFAULT NULL,
  `TransactionId` int(11) DEFAULT NULL,
  `OrderId` int(11) DEFAULT NULL,
  `StockId` char(20) DEFAULT NULL,
  KEY `AccountId` (`AccountId`),
  KEY `BrokerId` (`BrokerId`),
  KEY `TransactionId` (`TransactionId`),
  KEY `OrderId` (`OrderId`),
  KEY `StockId` (`StockId`),
  CONSTRAINT `Trade_ibfk_1` FOREIGN KEY (`AccountId`) REFERENCES `Accounts` (`AccountId`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `Trade_ibfk_2` FOREIGN KEY (`BrokerId`) REFERENCES `Employees` (`EmployeeId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `Trade_ibfk_3` FOREIGN KEY (`TransactionId`) REFERENCES `Transactions` (`TransactionId`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `Trade_ibfk_4` FOREIGN KEY (`OrderId`) REFERENCES `Orders` (`OrderId`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `Trade_ibfk_5` FOREIGN KEY (`StockId`) REFERENCES `Stocks` (`StockSymbol`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Trade`
--

LOCK TABLES `Trade` WRITE;
/*!40000 ALTER TABLE `Trade` DISABLE KEYS */;
INSERT INTO `Trade` VALUES (1,1,1,1,'GM'),(3,2,2,2,'IBM'),(3,2,3,3,'IBM');
/*!40000 ALTER TABLE `Trade` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-05-08 16:07:37
