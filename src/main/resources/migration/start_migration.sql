CREATE TABLE ACCOUNT(
ID INT AUTO_INCREMENT PRIMARY KEY,
BALANCE DOUBLE,
NAME VARCHAR(255)
);

insert into ACCOUNT(balance, name) values (100500, 'Account1');
insert into ACCOUNT(balance, name) values (1000, 'Account2');

CREATE TABLE TRANSFER(
ID INT AUTO_INCREMENT PRIMARY KEY,
fromAccountId INT,
toAccountId INT,
amount DOUBLE,
date TIMESTAMP,

  foreign key (fromAccountId) references ACCOUNT(id),
  foreign key (toAccountId) references ACCOUNT(id)
);


commit;

