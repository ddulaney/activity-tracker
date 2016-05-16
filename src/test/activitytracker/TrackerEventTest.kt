package activitytracker

import activitytracker.TrackerEvent.Companion.parseDateTime
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.StringReader

class TrackerEventTest {

    @Test fun `convert event object into csv line`() {
        val dateTime = parseDateTime("2016-03-03T01:02:03.000")
        val event = TrackerEvent(dateTime, "user", "IdeState", "Active", "banners", "Editor", "/path/to/file", "", 1938, 57)
        val eventAsCsv = "2016-03-03T01:02:03.000Z,user,IdeState,Active,banners,Editor,/path/to/file,,1938,57\r\n"

        assertThat(formatAsTrackerEvent(event), equalTo(eventAsCsv))
    }

    @Test fun `convert csv line to event object`() {
        val dateTime = parseDateTime("2016-03-03T01:02:03.000")
        val event = TrackerEvent(dateTime, "user", "IdeState", "Active", "banners", "Editor", "/path/to/file", "", 1938, 57)
        val parsedEvent = parseAsTrackerEvent("2016-03-03T01:02:03.000Z,user,IdeState,Active,banners,Editor,/path/to/file,,1938,57")

        assertThat(parsedEvent, equalTo(event))
    }

    @Test fun `convert csv line with some millis to event object`() {
        val dateTime = parseDateTime("2016-03-03T01:02:03.012+00:00")
        val event = TrackerEvent(dateTime, "user", "IdeState", "Active", "banners", "Editor", "/path/to/file", "", 1938, 57)
        val parsedEvent = parseAsTrackerEvent("2016-03-03T01:02:03.12+00:00,user,IdeState,Active,banners,Editor,/path/to/file,,1938,57")

        assertThat(parsedEvent, equalTo(event))
    }

    @Test fun `convert csv line with no millis to event object`() {
        val dateTime = parseDateTime("2016-03-03T01:02:03.000Z")
        val event = TrackerEvent(dateTime, "user", "IdeState", "Active", "banners", "Editor", "/path/to/file", "", 1938, 57)
        val parsedEvent = parseAsTrackerEvent("2016-03-03T01:02:03Z,user,IdeState,Active,banners,Editor,/path/to/file,,1938,57")

        assertThat(parsedEvent, equalTo(event))
    }

    @Test fun `convert csv line with no timezone (old log format) to event object`() {
        val dateTime = parseDateTime("2016-03-03T01:02:03.123Z")
        val event = TrackerEvent(dateTime, "user", "IdeState", "Active", "banners", "Editor", "/path/to/file", "", 1938, 57)
        val parsedEvent = parseAsTrackerEvent("2016-03-03T01:02:03.123,user,IdeState,Active,banners,Editor,/path/to/file,,1938,57")

        assertThat(parsedEvent, equalTo(event))
    }

    private fun formatAsTrackerEvent(event: TrackerEvent): String {
        StringBuilder().let {
            event.toCsv(CSVPrinter(it, CSVFormat.RFC4180))
            return it.toString()
        }
    }

    private fun parseAsTrackerEvent(csvLine: String): TrackerEvent {
        val parser = CSVParser(StringReader(csvLine), CSVFormat.RFC4180)
        return TrackerEvent.fromCsv(parser.records.first())
    }
}

