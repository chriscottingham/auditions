Problem description:

    Given a tomcat access log:
        parse it,
        enter each line as a row in a mysql table,
        given a start time, duration, and a count:
            add a row to a summary table for each ip address exceeding the count within the duration of the start time
            print a summary table to console for the ip addresses exceeding the count
        provide SQL DDL and queries for directly finding the high access ips

    Program should be executable with the following command-line instruction:
        java -cp "parser.jar" com.ef.Parser --startDate=2017-01-01.00:00:00 --duration=daily --threshold=500 --accesslog=some/file.log

On using the program:

It should happily parse the example command options:
java -cp "parser.jar" com.ef.Parser --startDate=2017-01-01.00:00:00 --duration=daily --threshold=500 --accesslog=some/file.log

The jar has its dependencies packaged with it.  It assumes a mysql instance running on localhost with a root user with no password.  I
could add some additional command line parsing options or a properties file to let the user specify these...


General Thoughts:

It wasn't stated whether an ORM like Hibernate was expected.  Since I have only two entities, I've tried to avoid pulling in that dependency, both for
simplicity and speed.  This decision has meant that I have some old-school mapping and sql-generation code, which is definitely on the painful side.  If it
wasn't log processing, or if I had more than, maybe, 3 entities, I would certainly turn to some kind of ORM, unless there was some business reason
against it. Still, the whole thing has been a reminder of why we use ORMs...

On database column types, there's some question about whether to use datetime or timestamp.  Having no constraint on timezone or value range, the decision seems
 arbitrary to me for this application, and datetime seems a little more robust.

I set up my local mysql instance with no root password for convenience.  Obviously not what you would do in anything other than a development environment.

The application could be multi-threaded.  Performance could certainly be gained from dumping newly parsed access log rows onto a queue and having a thread pool
pull them off and persist them.

I have no connection pooling.  Many use cases would benefit from, or require that a connection pool be used, like multiple threads writing to
the db, or if this was a streaming process without back-pressure.  In this case, though, I have a clear start and end point for my connection, and can do
everything in one session with no time constraints, so I didn't use a pool.

In all three persistable classes (LogEntry, HighAccessIncident, LogQueryCriteria), with the getColumnsString() method, I'm mixing persistence logic with the
domain objects, which is a violation of separation of concerns. But it's simple, and I think, without other constraints, the use case justifies it - though it
should be considered technical debt to be fixed up as soon as something more involved comes along.

It would be great to use a csv/pojo mapping library, and just tell it that the pipe character is the delimiter.  I couldn't find a suitable library, though.
Jackson CSV seems to not have appropriate date format handling.  Commons csv parses the whole data file and returns a single list of pojos, which would take up
too much memory for large log files.  SuperCSV is just clunky.  So I wrote my own flat file to pojo mapper.

I would like to organize the code differently than to kick off the process by calling Parser.main(...), as specified in the requirements document.  The parser
may be a primary actor, but I would really like to put main(...) in a class called LogInsights or the like, and have it register the analytics and persistence
listeners, and kick off the parse from there.

Looks like the SQL "with" clause got added to mysql 8, but I'm running 5.something locally, so using subqueries.

The sql tables could use some indices on the "group by" query columns.



On the task description:

Date format provided on command line is different than the one in the logs.  Two date format strings on Parser to reflect this.



On a completely different approach to this problem:

What about simply feeding the data to elastic search.  Its field extraction and query language should be able to handle this type of problem out-of-the-box.



Sql DDL:

create database wallethub;
create table access_log (id int auto_increment primary key, entry_time datetime, ip_address varchar(15), request varchar(256), http_status smallint, user_agent varchar(256));
create table high_access_incident (id int auto_increment primary key, ip_address varchar(15), occurrence_count int, start_date datetime, log_duration char(6), threshold int);


Data queries:

Day query:
mysql> select * from (select ip_address, count(ip_address) ip_count from access_log where date(entry_time) >= '2017-01-01' and date(entry_time) < date_add('2017-01-01', interval 1
day) group by ip_address) as first where first.ip_count > 500 order by first.ip_count;

Hour query:
mysql> select * from (select ip_address, count(ip_address) ip_count from access_log where entry_time >= '2017-01-01 15:00:00' and entry_time < date_add('2017-01-01 15:00:00', inter
val 1 hour) group by ip_address) as first where first.ip_count > 200 order by first.ip_count;

Look up access log entry by known ip:
mysql> select * from access_log where ip_address='192.168.1.1';


Program performance:

Total time with inserts: 1 minute 47 seconds.  About 120k records, at 1100 inserts per second.
