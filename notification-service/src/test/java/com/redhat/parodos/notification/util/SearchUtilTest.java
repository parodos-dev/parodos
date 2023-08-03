package com.redhat.parodos.notification.util;

import com.redhat.parodos.notification.enums.SearchCriteria;
import com.redhat.parodos.notification.enums.State;
import com.redhat.parodos.notification.exceptions.SearchByStateAndTermNotSupportedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchUtilTest {

	@Test
	void getSearchCriteria() {
		assertEquals(SearchCriteria.BY_USERNAME, SearchUtil.getSearchCriteria(null, null));
		assertEquals(SearchCriteria.BY_USERNAME, SearchUtil.getSearchCriteria(null, ""));
		assertEquals(SearchCriteria.BY_USERNAME_AND_STATE_UNREAD, SearchUtil.getSearchCriteria(State.UNREAD, null));
		assertEquals(SearchCriteria.BY_USERNAME_AND_STATE_ARCHIVED, SearchUtil.getSearchCriteria(State.ARCHIVED, ""));
		assertEquals(SearchCriteria.BY_USERNAME_AND_SEARCH_TERM, SearchUtil.getSearchCriteria(null, "test"));
		Exception exception = assertThrows(SearchByStateAndTermNotSupportedException.class, () -> {
			SearchUtil.getSearchCriteria(State.UNREAD, "test");
		});
		assertEquals("Search by state and search term combined not supported", exception.getMessage());
	}

}
