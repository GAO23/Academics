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
-- Table structure for table `Persons`
--

DROP TABLE IF EXISTS `Persons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Persons` (
  `SSN` int(11) NOT NULL,
  `LastName` char(100) NOT NULL,
  `FirstName` char(100) NOT NULL,
  `Address` char(100) DEFAULT NULL,
  `ZipCode` int(11) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `Telephone` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`SSN`),
  KEY `ZipCode` (`ZipCode`),
  CONSTRAINT `Persons_ibfk_1` FOREIGN KEY (`ZipCode`) REFERENCES `Locations` (`ZipCode`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Persons`
--

LOCK TABLES `Persons` WRITE;
/*!40000 ALTER TABLE `Persons` DISABLE KEYS */;
INSERT INTO `Persons` VALUES (111111111,'Yang','Shang','123 Success Street',11790,'xyz@gmail.com',5166328959),(123456789,'Smith','David','123 College road',11790,'12343@gmail.com',5162152345),(222222222,'Du','Victor','456 Fortune Road',11790,'123@gmail.com',5166324360),(333333333,'Smith','John','789 Peace Blvd.',93536,'124@gmail.com',3154434321),(444444444,'Phillip','Lewis','135 Knowledge Lane',11794,'143@gmail.com',5166668888),(789123456,'Warren','David','456 Sunken Street',11794,'WarrenDavid@gmail.com',6316329987);
/*!40000 ALTER TABLE `Persons` ENABLE KEYS */;
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
