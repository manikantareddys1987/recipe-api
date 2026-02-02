package com.recipe.unit.model.search;

import com.recipe.model.search.SearchOperation;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SearchOperationTest {
    @Test
    public void simpleEnumExampleInsideClassTest() {
        SearchOperation contains = SearchOperation.CONTAINS;
        SearchOperation doesNotContain = SearchOperation.DOES_NOT_CONTAIN;
        SearchOperation equal = SearchOperation.EQUAL;
        SearchOperation notEqual = SearchOperation.NOT_EQUAL;
        assertEquals(contains, SearchOperation.valueOf("CONTAINS"));
        assertEquals(doesNotContain, SearchOperation.valueOf("DOES_NOT_CONTAIN"));
        assertEquals(equal, SearchOperation.valueOf("EQUAL"));
        assertEquals(notEqual, SearchOperation.valueOf("NOT_EQUAL"));
    }

    @Test
    public void whenInputEnterItReturnsCorrespondingEnum() {
        Optional<SearchOperation> cn = SearchOperation.getOperation("cn");
        Optional<SearchOperation> nc = SearchOperation.getOperation("nc");
        Optional<SearchOperation> eq = SearchOperation.getOperation("eq");
        Optional<SearchOperation> ne = SearchOperation.getOperation("ne");
        assertTrue(cn.isPresent());
        assertTrue(nc.isPresent());
        assertTrue(eq.isPresent());
        assertTrue(ne.isPresent());
        assertEquals(SearchOperation.CONTAINS, cn.get());
        assertEquals(SearchOperation.DOES_NOT_CONTAIN, nc.get());
        assertEquals(SearchOperation.EQUAL, eq.get());
        assertEquals(SearchOperation.NOT_EQUAL, ne.get());
    }
}
