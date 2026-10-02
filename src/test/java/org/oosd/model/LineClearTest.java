package org.oosd.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineClearTest {

    //PARAMETERIZED: runs once per row of data
    @ParameterizedTest(name = "{0} line(s) = {1} points")
    @CsvSource({"1, 100", "2, 300", "3, 600", "4, 1000"})
    void pointsMatchTheSpecification(int rows, int points) {
        assertEquals(points, LineClear.of(rows).points());
    }
}
