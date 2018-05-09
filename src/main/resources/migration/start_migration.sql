CREATE TABLE ACCOUNT(
ID INT AUTO_INCREMENT PRIMARY KEY,
MONEY DOUBLE,
NAME VARCHAR(255)
);

insert into ACCOUNT(money, name) values (100500, 'Account1');
insert into ACCOUNT(money, name) values (1000, 'Account2');

CREATE TABLE TRANSFER(
ID INT AUTO_INCREMENT PRIMARY KEY,
from_id INT,
to_id INT,
money DOUBLE
);


commit;

