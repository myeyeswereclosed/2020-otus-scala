begin;

create extension if not exists "uuid-ossp";

delete from flyway_schema_history;
drop table if exists book_author;
drop table if exists author;
drop table if exists comment;
drop table if exists book;

create table if not exists book(
    id uuid primary key not null default uuid_generate_v4(),
    title varchar(255) not null,
    pages_number int not null,
    year_of_publishing int
);

create table if not exists author(
    id uuid primary key not null default uuid_generate_v4(),
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    constraint uq__author_first_name_last_name unique (first_name, last_name)
);

create table if not exists book_author
(
    id bigserial primary key not null,
    book_id uuid not null
        constraint fk__book_author__book_id references book
            on update cascade on delete cascade,
    author_id uuid not null
        constraint fk__book_author__author_id references author
            on update cascade on delete cascade,
    constraint uq__book_author_book_id_author_id
        unique (book_id, author_id)
);

create table comment
(
    id uuid primary key not null default uuid_generate_v4(),
    text varchar not null,
    book_id uuid not null
        constraint fk__comment__book_id references book
            on update cascade on delete cascade,
    made_at timestamptz not null default now()
);

commit;