package ru.job4j.grabber;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class.
 *
 * @author Alexander Bondarev(mailto:bondarew2507@gmail.com).
 * @since 24.02.2018.
 */
public class VacanciesParserTest {

    @Test
    public void whenFirstTimeParseShouldReturnNoEmptyList() {
        VacanciesParser parser = new VacanciesParser();
        int result = parser.parse().size();

        assertTrue(result > 0);
    }
}