package ru.job4j.grabber.service;

import ru.job4j.grabber.model.Vacancy;

import java.util.Collection;

/**
 * @author Alexander Bondarev(mailto:bondarew2507@gmail.com).
 * @since 17.02.2018.
 */
public interface IDBService {

    void add(Collection<Vacancy> vacancies);

    void delete();

    void close();
}
