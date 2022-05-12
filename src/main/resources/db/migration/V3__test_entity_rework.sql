
CREATE SEQUENCE global_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;


drop table test_entity;

CREATE TABLE test_entity (
  id BIGINT NOT NULL DEFAULT NEXTVAL('global_seq'),
  name TEXT
);
