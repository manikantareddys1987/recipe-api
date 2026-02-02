package com.recipe.unit.model.search;

import com.recipe.model.search.DataOption;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataOptionTest {

    @Test
    public void simpleEnumExampleInsideClassTest() {
        DataOption option1 = DataOption.ALL;
        DataOption option2 = DataOption.ANY;
        assertEquals(option1, DataOption.valueOf("ALL"));
        assertEquals(option2, DataOption.valueOf("ANY"));
    }

    @Test
    public void whenInputEnterItReturnsCorrespondingEnum() {
        Optional<DataOption> all = DataOption.getDataOption("all");
        Optional<DataOption> any = DataOption.getDataOption("any");
        assertTrue(all.isPresent());
        assertTrue(any.isPresent());
        assertEquals(DataOption.ALL, all.get());
        assertEquals(DataOption.ANY, any.get());
    }
}
