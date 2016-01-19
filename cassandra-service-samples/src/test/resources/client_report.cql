CREATE KEYSPACE simple
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE client_report (
	region int,
	tpl_code text,
	p_year int,
	p_code int,
	client_id bigint,
	data text,
	ver timestamp,
	deleted boolean,
	PRIMARY KEY ((region), tpl_code, p_year, p_code, client_id)
);
