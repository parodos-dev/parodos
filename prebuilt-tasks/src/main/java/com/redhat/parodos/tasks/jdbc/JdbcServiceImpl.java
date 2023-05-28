package com.redhat.parodos.tasks.jdbc;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class JdbcServiceImpl implements JdbcService {

	@Override
	public List<Map<String, Object>> query(String url, String statement) {
		return createJdbcTemplate(url).query(statement, new ColumnMapRowMapper());
	}

	@Override
	public void update(String url, String statement) {
		createJdbcTemplate(url).update(statement);
	}

	@Override
	public void execute(String url, String statement) {
		createJdbcTemplate(url).execute(statement);
	}

	private JdbcTemplate createJdbcTemplate(String url) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl(url);

		return new JdbcTemplate(dataSource);
	}

}
