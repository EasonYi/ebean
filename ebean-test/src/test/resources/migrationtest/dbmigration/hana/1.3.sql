-- Migrationscripts for ebean unittest
-- drop dependencies
alter table migtest_ckey_detail drop constraint  fk_migtest_ckey_detail_parent;
alter table migtest_fk_cascade drop constraint  fk_migtest_fk_cascade_one_id;
alter table migtest_fk_none drop constraint  fk_migtest_fk_none_one_id;
alter table migtest_fk_none_via_join drop constraint  fk_migtest_fk_none_via_join_one_id;
alter table migtest_fk_set_null drop constraint  fk_migtest_fk_set_null_one_id;
delimiter $$
do
begin
declare exit handler for sql_error_code 397 begin end;
exec 'alter table migtest_e_basic drop constraint ck_migtest_e_basic_status';
end;
$$;
delimiter $$
do
begin
declare exit handler for sql_error_code 397 begin end;
exec 'alter table migtest_e_basic drop constraint ck_migtest_e_basic_status2';
end;
$$;
delimiter $$
do
begin
declare exit handler for sql_error_code 397 begin end;
exec 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_description';
end;
$$;
alter table migtest_e_basic drop constraint  fk_migtest_e_basic_user_id;
delimiter $$
do
begin
declare exit handler for sql_error_code 397 begin end;
exec 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_status_indextest1';
end;
$$;
delimiter $$
do
begin
declare exit handler for sql_error_code 397 begin end;
exec 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_name';
end;
$$;
delimiter $$
do
begin
declare exit handler for sql_error_code 397 begin end;
exec 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest4';
end;
$$;
delimiter $$
do
begin
declare exit handler for sql_error_code 397 begin end;
exec 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest5';
end;
$$;
delimiter $$
do
begin
declare exit handler for sql_error_code 397 begin end;
exec 'alter table migtest_e_enum drop constraint ck_migtest_e_enum_test_status';
end;
$$;
delimiter $$
do
begin
declare exit handler for sql_error_code 261 begin end;
exec 'drop index ix_migtest_e_basic_indextest3';
end;
$$;
delimiter $$
do
begin
declare exit handler for sql_error_code 261 begin end;
exec 'drop index ix_migtest_e_basic_indextest6';
end;
$$;
-- apply changes
create column table migtest_e_ref (
  id                            integer generated by default as identity not null,
  name                          nvarchar(127) not null,
  constraint uq_migtest_e_ref_name unique (name),
  constraint pk_migtest_e_ref primary key (id)
);


update migtest_e_basic set status2 = 'N' where status2 is null;

update migtest_e_basic set user_id = 23 where user_id is null;

alter table migtest_e_history2 drop system versioning;

alter table migtest_e_history3 drop system versioning;
alter table migtest_e_history4 drop system versioning;
alter table migtest_e_history6 drop system versioning;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number2 = 7 where test_number2 is null;
-- apply alter tables
alter table migtest_e_basic alter (status nvarchar(1) default null,
   status2 nclob not null);
alter table migtest_e_basic alter (status2 nvarchar(1) default 'N' not null,
   user_id integer default 23 not null);
alter table migtest_e_basic add (description_file blob,
   old_boolean boolean default false not null,
   old_boolean2 boolean,
   eref_id integer);
alter table migtest_e_history2 alter (test_string nvarchar(255) default null);
alter table migtest_e_history2 add (obsolete_string1 nvarchar(255),
   obsolete_string2 nvarchar(255));
alter table migtest_e_history2_history alter (test_string nvarchar(255) default null);
alter table migtest_e_history2_history add (obsolete_string1 nvarchar(255),
   obsolete_string2 nvarchar(255));
alter table migtest_e_history4 alter (test_number decimal);
alter table migtest_e_history4 alter (test_number integer);
alter table migtest_e_history4_history alter (test_number decimal);
alter table migtest_e_history4_history alter (test_number integer);
alter table migtest_e_history6 alter (test_number1 integer default null,
   test_number2 integer default 7 not null);
alter table migtest_e_history6_history alter (test_number1 integer default null,
   test_number2 integer default 7 not null);
-- apply post alter
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
alter table migtest_e_basic add constraint ck_migtest_e_basic_status2 check ( status2 in ('N','A','I'));
-- cannot create unique index "uq_migtest_e_basic_indextest2" on table "migtest_e_basic" with nullable columns;
-- cannot create unique index "uq_migtest_e_basic_indextest6" on table "migtest_e_basic" with nullable columns;
alter table migtest_e_enum add constraint ck_migtest_e_enum_test_status check ( test_status in ('N','A','I'));
comment on column migtest_e_history.test_string is '';
comment on table migtest_e_history is '';
alter table migtest_e_history2 add system versioning history table migtest_e_history2_history not validated;
alter table migtest_e_history3 add system versioning history table migtest_e_history3_history not validated;
alter table migtest_e_history4 add system versioning history table migtest_e_history4_history not validated;
alter table migtest_e_history6 add system versioning history table migtest_e_history6_history not validated;
-- explicit index "ix_migtest_e_basic_indextest1" for single column "indextest1" of table "migtest_e_basic" is not necessary;
-- explicit index "ix_migtest_e_basic_indextest5" for single column "indextest5" of table "migtest_e_basic" is not necessary;
-- foreign keys and indices
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade on update restrict;
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null on update restrict;
-- explicit index "ix_migtest_e_basic_eref_id" for single column "eref_id" of table "migtest_e_basic" is not necessary;
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;

