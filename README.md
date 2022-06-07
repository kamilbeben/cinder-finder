
Matchmaking application, for now it supports Elden Ring. I'm planning to expand it to support other soulsborne games in the future.

https://furledfinger.com

# Development
## Client
### Requirements
Will most likely work on newer versions, this is what it has been tested on so far
 - Node 14.19.3
 - NPM 6.14.7

### Install and run dev server
It will be available on http://localhost:3333
```bash
npm install
npm run dev
```

## Server
### Requirements
Will most likely work on newer versions, this is what it has been tested on so far
 - Open JDK 11
 - Maven 3.6.0
 - PostgreSQL 11

### PostgreSQL setup
First, enable password login
```bash
# path could be different, it's based on the installation. If you're not sure where to look, try `sudo find / -name "pg_hba.conf"`
sudo vim /etc/postgresql/11/main/pg_hba.conf
```
```diff
# "local" is for Unix domain socket connections only
- local   all             all                                     ident
+ local   all             all                                     md5
# IPv4 local connections:
- host    all             all             127.0.0.1/32            ident
+ host    all             all             127.0.0.1/32            md5
```

Log in as admin to `postgres` database
```bash
pasl postgres -U postgres # default admin is 'postgres'
```
Create user and database
```sql
CREATE USER furledfinger with password 'furledfinger';

CREATE DATABASE "furledfinger"
  WITH OWNER "furledfinger"
  ENCODING 'UTF8'
  TEMPLATE template0;
```

Make sure that after your changes the tests are still passing
