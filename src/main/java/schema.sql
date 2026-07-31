-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: library_db
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `book_requests`
--

DROP TABLE IF EXISTS `book_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_requests` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `book_id` int NOT NULL,
  `request_date` date NOT NULL,
  `status` enum('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `fk_request_user` (`user_id`),
  KEY `fk_request_book` (`book_id`),
  CONSTRAINT `fk_request_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_request_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_requests`
--

LOCK TABLES `book_requests` WRITE;
/*!40000 ALTER TABLE `book_requests` DISABLE KEYS */;
INSERT INTO `book_requests` VALUES (1,2,4,'2026-07-25','PENDING'),(2,3,5,'2026-07-26','PENDING'),(3,4,10,'2026-07-27','APPROVED'),(4,8,1,'2026-07-28','PENDING'),(5,9,11,'2026-07-28','REJECTED'),(6,2,2,'2026-07-30','PENDING');
/*!40000 ALTER TABLE `book_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `books`
--

DROP TABLE IF EXISTS `books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `books` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `author` varchar(100) NOT NULL,
  `category` varchar(50) DEFAULT NULL,
  `total_copies` int NOT NULL,
  `available_copies` int NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `books_chk_1` CHECK ((`available_copies` <= `total_copies`)),
  CONSTRAINT `books_chk_2` CHECK ((`available_copies` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `books`
--

LOCK TABLES `books` WRITE;
/*!40000 ALTER TABLE `books` DISABLE KEYS */;
INSERT INTO `books` VALUES (1,'Introduction to Java','Herbert Schildt','Programming',5,4),(2,'Database System Concepts','Korth','Database',3,3),(3,'Head First Servlets and JSP','Kathy Sierra','Web Development',2,1),(4,'Clean Code','Robert C. Martin','Programming',4,3),(5,'Introduction to Algorithms','Thomas H. Cormen','Computer Science',2,1),(6,'The Design of Everyday Things','Don Norman','Design',3,3),(7,'Learning SQL','Alan Beaulieu','Database',6,4),(8,'HTML and CSS: Design and Build Websites','Jon Duckett','Web Development',4,3),(9,'Cracking the Coding Interview','Gayle Laakmann McDowell','Career',3,2),(10,'Design Patterns','Erich Gamma','Computer Science',4,3),(11,'Compilers: Principles, Techniques, and Tools','Alfred Aho','Computer Science',5,4);
/*!40000 ALTER TABLE `books` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `student_request_summary`
--

DROP TABLE IF EXISTS `student_request_summary`;
/*!50001 DROP VIEW IF EXISTS `student_request_summary`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `student_request_summary` AS SELECT 
 1 AS `request_id`,
 1 AS `user_id`,
 1 AS `username`,
 1 AS `book_id`,
 1 AS `book_title`,
 1 AS `request_date`,
 1 AS `status`,
 1 AS `total_fines`,
 1 AS `active_loans`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `book_id` int NOT NULL,
  `issue_date` date NOT NULL,
  `due_date` date NOT NULL,
  `return_date` date DEFAULT NULL,
  `fine_amount` decimal(10,2) DEFAULT '0.00',
  `status` enum('ISSUED','RETURNED') DEFAULT 'ISSUED',
  PRIMARY KEY (`id`),
  KEY `fk_transaction_student` (`student_id`),
  KEY `fk_transaction_book` (`book_id`),
  CONSTRAINT `fk_transaction_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_transaction_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES (1,3,1,'2026-06-01','2026-06-15','2026-06-06',0.00,'RETURNED'),(2,4,4,'2026-06-01','2026-06-15','2026-06-17',2.00,'RETURNED'),(3,5,7,'2026-07-02','2026-07-16',NULL,14.00,'ISSUED'),(4,6,9,'2026-07-05','2026-07-19',NULL,11.00,'ISSUED'),(5,3,2,'2026-05-15','2026-05-29','2026-05-31',2.00,'RETURNED'),(6,2,3,'2026-06-10','2026-06-24','2026-06-26',2.00,'RETURNED'),(7,2,5,'2026-07-01','2026-07-15',NULL,15.00,'ISSUED'),(8,8,10,'2026-07-16','2026-07-30',NULL,0.00,'ISSUED'),(9,9,6,'2026-06-15','2026-06-29','2026-06-28',0.00,'RETURNED'),(10,4,8,'2026-07-16','2026-07-30',NULL,0.00,'ISSUED'),(11,5,1,'2026-07-16','2026-07-30',NULL,0.00,'ISSUED'),(12,6,2,'2026-06-12','2026-06-26','2026-06-28',2.00,'RETURNED'),(13,8,4,'2026-07-01','2026-07-15',NULL,15.00,'ISSUED'),(14,9,7,'2026-07-16','2026-07-30',NULL,0.00,'ISSUED'),(15,2,11,'2026-05-20','2026-06-03','2026-06-02',0.00,'RETURNED'),(16,2,1,'2026-05-01','2026-05-15','2026-05-14',0.00,'RETURNED'),(17,2,4,'2026-05-20','2026-06-03','2026-06-05',2.00,'RETURNED'),(18,3,8,'2026-04-10','2026-04-24','2026-04-22',0.00,'RETURNED'),(19,3,11,'2026-07-01','2026-07-15',NULL,15.00,'ISSUED'),(20,4,2,'2026-03-15','2026-03-29','2026-03-31',2.00,'RETURNED'),(21,4,10,'2026-06-10','2026-06-24','2026-06-24',0.00,'RETURNED'),(22,5,5,'2026-05-10','2026-05-24','2026-05-20',0.00,'RETURNED'),(23,6,1,'2026-04-01','2026-04-15','2026-04-17',2.00,'RETURNED'),(24,6,3,'2026-07-16','2026-07-30',NULL,0.00,'ISSUED'),(25,8,7,'2026-06-20','2026-07-04','2026-07-02',0.00,'RETURNED'),(26,9,2,'2026-05-18','2026-06-01','2026-06-03',2.00,'RETURNED');
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `role` enum('ADMIN','STUDENT') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin1','123','ADMIN'),(2,'student1','123','STUDENT'),(3,'student2','123','STUDENT'),(4,'student3','123','STUDENT'),(5,'student4','123','STUDENT'),(6,'student5','123','STUDENT'),(7,'admin2','123','ADMIN'),(8,'student6','123','STUDENT'),(9,'student7','123','STUDENT');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structure for view `student_request_summary`
--

/*!50001 DROP VIEW IF EXISTS `student_request_summary`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `student_request_summary` AS select `br`.`id` AS `request_id`,`br`.`user_id` AS `user_id`,`u`.`username` AS `username`,`br`.`book_id` AS `book_id`,`b`.`title` AS `book_title`,`br`.`request_date` AS `request_date`,`br`.`status` AS `status`,coalesce((select sum(`t`.`fine_amount`) from `transactions` `t` where (`t`.`student_id` = `br`.`user_id`)),0) AS `total_fines`,(select count(0) from `transactions` `t` where ((`t`.`student_id` = `br`.`user_id`) and (`t`.`status` = 'ISSUED'))) AS `active_loans` from ((`book_requests` `br` join `users` `u` on((`br`.`user_id` = `u`.`id`))) join `books` `b` on((`br`.`book_id` = `b`.`id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-31 10:31:43
