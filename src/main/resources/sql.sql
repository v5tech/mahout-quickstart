create database mahout;

use mahout;

create table intro(
	uid varchar(20) not null,
	iid varchar(50) not null,
	val varchar(50) not null,
	time varchar(50) default null
);


CREATE TABLE taste_preferences (
   user_id BIGINT NOT NULL,
   item_id BIGINT NOT NULL,
   preference FLOAT NOT NULL,
   PRIMARY KEY (user_id, item_id),
   INDEX (user_id),
   INDEX (item_id)
 );


load data local infile 'E:/developer/workspace/mahout-quickstart/src/main/resources/dataset.csv' replace into table intro fields terminated by ',' lines terminated by '\n' (@col1,@col2,@col3) set uid=@col1,iid=@col2,val=@col3;

mysql> load data local infile 'E:/developer/workspace/mahout-quickstart/src/main/resources/dataset.csv' replace into table intro fields terminated by ',' lines terminated by '\n' (@col1,@col2,@col3) set uid=@col1,iid=@col2,val=@col3;
Query OK, 32 rows affected (0.08 sec)
Records: 32  Deleted: 0  Skipped: 0  Warnings: 0

mysql> select * from intro;
+-----+-----+------+
| uid | iid | val  |
+-----+-----+------+
 |1   | 10  | 1.0
 |1   | 11  | 2.0
 |1   | 12  | 5.0
 |1   | 13  | 5.0
 |1   | 14  | 5.0
 |1   | 15  | 4.0
 |1   | 16  | 5.0
 |1   | 17  | 1.0
 |1   | 18  | 5.0
 |2   | 10  | 1.0
 |2   | 11  | 2.0
 |2   | 15  | 5.0
 |2   | 16  | 4.5
 |2   | 17  | 1.0
 |2   | 18  | 5.0
 |3   | 11  | 2.5
 |3   | 12  | 4.5
 |3   | 13  | 4.0
 |3   | 14  | 3.0
 |3   | 15  | 3.5
 |3   | 16  | 4.5
 |3   | 17  | 4.0
 |3   | 18  | 5.0
 |4   | 10  | 5.0
 |4   | 11  | 5.0
 |4   | 12  | 5.0
 |4   | 13  | 0.0
 |4   | 14  | 2.0
 |4   | 15  | 3.0
 |4   | 16  | 1.0
 |4   | 17  | 4.0
| 4   | 18  | 1.0  |
+-----+-----+------+
32 rows in set (0.02 sec)
