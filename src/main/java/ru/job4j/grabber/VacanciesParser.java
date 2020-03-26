package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.model.Vacancy;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class represent parser for vacancies from site www.sql.ru.
 *
 * @author Alexander Bondarev(mailto:bondarew2507@gmail.com).
 * @since 20.02.2018.
 */
public class VacanciesParser {

    private static final String START_PAGE = "http://www.sql.ru/forum/job-offers";

    private static final String[] PARSER_MONTH_SHORT_NAME = new String[] {
            "янв.", "февр.", "мар.", "апр.", "мая", "июн.", "июл.","авг.", "cент.", "окт.", "нояб.", "дек."};

    private static final String[] SITE_MONTH_SHORT_NAME = new String[] {
            "янв", "фев", "мар", "апр", "май", "июн", "июл","авг", "cен", "окт", "ноя", "дек"};

    private static final Logger LOG = LoggerFactory.getLogger(VacanciesParser.class);

    private final SimpleDateFormat format = new SimpleDateFormat("dd MMM yy, HH:mm", Locale.getDefault());

    private Timestamp lastParsingDate;

    private String currentPage;

    public VacanciesParser() {
        this.currentPage = START_PAGE;
        this.lastParsingDate = this.setFirstParseDate();
    }

    public Timestamp getLastParsingDate() {
        return new Timestamp(lastParsingDate.getTime());
    }

    /**
     * Method parse {@link Vacancy} from SQL.ru and returns {@link List<Vacancy>}.
     *
     * @return {@link List<Vacancy>}.
     */
    public List<Vacancy> parse() {
        LOG.info("Start parsing.");
        List<Vacancy> result = new ArrayList<>();
        int currentPageNumber = 1;
        boolean isParsingComplete = false;
        do {
            if (currentPageNumber > 1) {
                this.currentPage = String.format("%s/%s", START_PAGE, currentPageNumber);
            }
            try {
                Document currentPage = Jsoup.connect(this.currentPage).get();
                LOG.info("Connect to parsed document " + this.currentPage);
                Elements topics = currentPage.select("tr:has(.postslisttopic)");
                for (Element topic : topics) {
                    String topicText = topic.text().toLowerCase();
                    if (topicText.contains("java") && !topicText.contains("script") && !topicText.contains("закрыт")) {
                        Elements data = topic.select("td");
                        Timestamp date = this.parseDate(data.get(5).text());
                        if (date.before(this.lastParsingDate)) {
                            isParsingComplete = true;
                            break;
                        }
                        String url = topic.select("td.postslisttopic > a[href]").first().attr("href");
                        result.add(parseVacancy(url));
                    }
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            currentPageNumber++;
        } while (!isParsingComplete);
        this.lastParsingDate = new Timestamp(System.currentTimeMillis());
        this.currentPage = START_PAGE;
        LOG.info("End parsing.");
        return result;
    }

    private Timestamp setFirstParseDate() {
        Calendar firstParseDate = Calendar.getInstance();
        firstParseDate.set(firstParseDate.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 1);
        return new Timestamp(firstParseDate.getTimeInMillis());
    }

    private Vacancy parseVacancy(String url) throws IOException {
        Document vacansyPage = Jsoup.connect(url).get();
        Element msgTable = vacansyPage.select("table.msgTable").first();
        String topic = msgTable.selectFirst("td.messageHeader").text();
        Elements msgBody = msgTable.select("td.msgBody");
        String author = msgBody.first().selectFirst("a[href]").text();
        String description = msgBody.get(1).text();
        Element msgFooter = msgTable.selectFirst("td.msgFooter");
        Timestamp createDate = parseDate(msgFooter.text());
        return new Vacancy(topic, author, description, createDate);
    }

    private Timestamp parseDate(String date) {
        Calendar calendar = Calendar.getInstance();
        if (date.contains("сегодня")) {
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(date.substring(9, 11)));
            calendar.set(Calendar.MINUTE, Integer.parseInt(date.substring(12, 14)));
        } else if (date.contains("вчера")) {
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(date.substring(7, 9)));
            calendar.set(Calendar.MINUTE, Integer.parseInt(date.substring(10, 12)));
        } else {
            try {
                calendar.setTime(format.parse(setCorrectMonthName(date)));
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return new Timestamp(calendar.getTimeInMillis());
    }

    private String setCorrectMonthName(String dateInString) {
        for (int i = 0; i < SITE_MONTH_SHORT_NAME.length; i++) {
            if (dateInString.contains(SITE_MONTH_SHORT_NAME[i])) {
                dateInString = dateInString.replace(SITE_MONTH_SHORT_NAME[i], PARSER_MONTH_SHORT_NAME[i]);
                break;
            }
        }
        return dateInString;
    }

}
