create table users (
       id bigint not null auto_increment,
       login_id varchar(255) not null,
       point bigint,
       gender enum ('F','M') not null,
       name varchar(255) not null,
       birth varchar(255) not null,
       email varchar(255) not null,
       updated_at datetime(6) not null,
       created_at datetime(6) not null,
       deleted_at datetime(6),
       primary key (id)
) engine=InnoDB;