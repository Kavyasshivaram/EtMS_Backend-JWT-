-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: etms_db_final
-- ------------------------------------------------------
-- Server version	8.0.36

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
-- Table structure for table `course_master`
--

DROP TABLE IF EXISTS `course_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_master` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_name` varchar(255) DEFAULT NULL,
  `description` text,
  `duration` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `syllabus_file_name` varchar(255) DEFAULT NULL,
  `syllabus_file_path` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_master`
--

LOCK TABLES `course_master` WRITE;
/*!40000 ALTER TABLE `course_master` DISABLE KEYS */;
INSERT INTO `course_master` VALUES (1,'Full Stack Java','A Java Full Stack course teaches both front-end and back-end development using Java technologies.\r\nIt covers core concepts like HTML, CSS, JavaScript, along with frameworks such as Spring Boot and Hibernate.\r\nStudents learn to build dynamic, database-driven web applications from scratch.\r\nThe course also includes REST APIs, version control (Git), and deployment practices.\r\nBy the end, learners can develop complete end-to-end applications and are job-ready for full stack roles.','6 Months','ACTIVE','1774075021680_SMTP_Email_Verification_Guide.pdf','C:\\Users\\KAVYA S\\OneDrive\\Desktop\\LMS\\fianlprojectetms\\fianlprojectetms\\Etms_Backend-main\\Etms_Backend-main/uploads/syllabus/1774075021680_SMTP_Email_Verification_Guide.pdf'),(2,'Full Stack Python',NULL,NULL,'ACTIVE',NULL,NULL),(3,'MERN Stack',NULL,NULL,'ACTIVE',NULL,NULL),(4,'Generative AI',NULL,NULL,'ACTIVE',NULL,NULL),(5,'Cyber Security',NULL,NULL,'ACTIVE',NULL,NULL),(6,'Data Analytics',NULL,NULL,'ACTIVE',NULL,NULL),(7,'Digital Marketing',NULL,NULL,'ACTIVE',NULL,NULL),(8,'AI ML','Artificial Intelligence (AI) and Machine Learning (ML) are transforming the way technology interacts with the world. AI refers to the ability of machines to perform tasks that typically require human intelligence, such as decision-making, problem-solving, and language understanding. ML, a subset of AI, enables systems to learn from data and improve their performance over time without being explicitly programmed. Together, they are used in applications like recommendation systems, virtual.','6 Months','ACTIVE','1774027565683_SMTP_Email_Verification_Guide.pdf','C:\\Users\\KAVYA S\\OneDrive\\Desktop\\LMS\\fianlprojectetms\\fianlprojectetms\\Etms_Backend-main\\Etms_Backend-main/uploads/syllabus/1774027565683_SMTP_Email_Verification_Guide.pdf');
/*!40000 ALTER TABLE `course_master` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-21 17:57:05
