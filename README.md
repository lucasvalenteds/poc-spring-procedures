# POC: Spring Procedures

It demonstrates different approaches to call procedures using JDBC and JPA.

We want to use each API to execute a Stored Procedure inside a Postgres database that transfers money from one bank account to another. The goal is to demonstrate how both APIs work and their current limitations when working with that specific DBMS.

The database state is provisioned using migration scripts managed by Flyway and the database is provisioned using a Docker container.

## How to run

| Description | Command |
| :--- | :--- |
| Run tests | `./gradlew test` |

