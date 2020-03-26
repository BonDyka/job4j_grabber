package ru.job4j.grabber.model;

import java.sql.Timestamp;

/**
 * Model for job vacancy
 *
 * @author Alexander Bondarev(mailto:bondarew2507@gmail.com).
 * @since 21.01.2018.
 */
public class Vacancy {

    private String topic;

    private String author;

    private String description;

    private Timestamp createDate;

    public Vacancy(String topic, String author, String description, Timestamp createDate) {
        this.topic = topic;
        this.description = description;
        this.author = author;
        this.createDate = createDate;
    }

    public String getTopic() {
        return topic;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vacancy)) {
            return false;
        }

        Vacancy vacancy = (Vacancy) o;

        if (!topic.equals(vacancy.topic)) {
            return false;
        }
        if (!author.equals(vacancy.author)) {
            return false;
        }
        if (!description.equals(vacancy.description)) {
            return false;
        }
        return createDate.equals(vacancy.createDate);

    }

    @Override
    public int hashCode() {
        int result = topic.hashCode();
        result = 31 * result + author.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + createDate.hashCode();
        return result;
    }
}
