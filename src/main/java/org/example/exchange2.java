DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS config;

CREATE TABLE users(
  id INT PRIMARY KEY AUTO_INCREMENT,
  chatId VARCHAR(100) NOT NULL,
  answer VARCHAR(100) NOT NULL
);

CREATE TABLE config(
  id INT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(255) NOT NULL,
  host VARCHAR(255) NOT NULL,
);

INSERT INTO config (token, host)
     VALUES ("001.1031916963.1477955322:1000000106", "https://api.vkteams.ext.lukoil.com/");

Exception in thread "main" org.h2.jdbc.JdbcSQLSyntaxErrorException: Синтаксическая ошибка в выражении SQL "CREATE TABLE config(\000d\000a  id INT PRIMARY KEY AUTO_INCREMENT,\000d\000a  \000d\000a[*]);\000d\000a\000d\000aINSERT INTO config (token, host)\000d\000a     VALUES (""001.1031916963.1477955322:1000000106"", ""https://api.vkteams.ext.lukoil.com/"");\000d\000a\000d\000a"; ожидалось "identifier"
Syntax error in SQL statement "CREATE TABLE config(\000d\000a  id INT PRIMARY KEY AUTO_INCREMENT,\000d\000a  \000d\000a[*]);\000d\000a\000d\000aINSERT INTO config (token, host)\000d\000a     VALUES (""001.1031916963.1477955322:1000000106"", ""https://api.vkteams.ext.lukoil.com/"");\000d\000a\000d\000a"; expected "identifier"; SQL statement:
CREATE TABLE config(
  id INT PRIMARY KEY AUTO_INCREMENT,
  
);

INSERT INTO config (token, host)
     VALUES ("001.1031916963.1477955322:1000000106", "https://api.vkteams.ext.lukoil.com/");

 [42001-232]
	at org.h2.message.DbException.getJdbcSQLException(DbException.java:514)
	at org.h2.message.DbException.getJdbcSQLException(DbException.java:489)
	at org.h2.message.DbException.getSyntaxError(DbException.java:261)
	at org.h2.command.Parser.readIdentifier(Parser.java:5527)
	at org.h2.command.Parser.parseTableColumnDefinition(Parser.java:8871)
	at org.h2.command.Parser.parseCreateTable(Parser.java:8819)
	at org.h2.command.Parser.parseCreate(Parser.java:6398)
	at org.h2.command.Parser.parsePrepared(Parser.java:645)
