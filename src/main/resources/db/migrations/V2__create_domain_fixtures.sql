insert into people (name) values ('John Smith');
insert into people (name) values ('Mary Jane');

insert into accounts (balance, owner) values (1000, (select id from people where name = 'John Smith'));
insert into accounts (balance, owner) values (2500, (select id from people where name = 'Mary Jane'));
