# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table log (
  id                            integer not null,
  transaction_id                varchar(255),
  timestamp                     timestamp,
  amount_btc                    integer,
  constraint pk_log primary key (id)
);


# --- !Downs

drop table if exists log;

