package com.redhat.parodos.tasks.jdbc;

import java.util.List;
import java.util.Map;

interface JdbcService {

	List<Map<String, Object>> query(String url, String statement);

	void update(String url, String statement);

	void execute(String url, String statement);

}
