# 🗂 Digital Assist Organizer – Java + MongoDB

A simple *command-line organizer app* built in *Java* using *MongoDB* for task storage.  
You can *add, view, search, and delete* tasks directly from your terminal.

---

## ⚙ Prerequisites

Before running, make sure you have:

1. *Java JDK 8 or higher*  
   - Check:  
     bash
     java -version
     
2. *MongoDB Server* running locally  
   - Default port: 27017
   - Check:  
     bash
     mongod
     
3. *MongoDB Java Driver JARs* (choose one option below 👇)

---



Use this if your Java code uses:
```java
DBCollection, DBCursor, BasicDBObject, MongoClient (com.mongodb.*)
