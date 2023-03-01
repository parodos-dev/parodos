package com.redhat.parodos.notification.util;

import com.redhat.parodos.notification.enums.SearchCriteria;
import com.redhat.parodos.notification.enums.State;
import com.redhat.parodos.notification.exceptions.SearchByStateAndTermNotSupportedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchUtilTest {

	@Test
	void getSearchCriteria() {
		SearchUtil searchUtil = new SearchUtil();
		assertEquals(SearchCriteria.BY_USERNAME, searchUtil.getSearchCriteria(null, null));
		assertEquals(SearchCriteria.BY_USERNAME, searchUtil.getSearchCriteria(null, ""));
		assertEquals(SearchCriteria.BY_USERNAME_AND_STATE_UNREAD, searchUtil.getSearchCriteria(State.UNREAD, null));
		assertEquals(SearchCriteria.BY_USERNAME_AND_STATE_ARCHIVED, searchUtil.getSearchCriteria(State.ARCHIVED, ""));
		assertEquals(SearchCriteria.BY_USERNAME_AND_SEARCH_TERM, searchUtil.getSearchCriteria(null, "test"));
		Exception exception = assertThrows(SearchByStateAndTermNotSupportedException.class, () -> {
			searchUtil.getSearchCriteria(State.UNREAD, "test");
		});
		assertEquals(String.format("Search by state and search term combined not supported"), exception.getMessage());
	}

}