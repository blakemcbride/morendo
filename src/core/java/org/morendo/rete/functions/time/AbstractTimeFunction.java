/*
 * Copyright 2002-2008 Peter Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.morendo.rete.functions.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Shared conversions for the time functions. Points in time are java.time.Instant; java.util.Date
 * and Calendar values from Java beans, numbers (epoch milliseconds) and ISO-8601 strings are
 * accepted as input as well.
 */
public abstract class AbstractTimeFunction {

    /** Epoch milliseconds of any supported representation. */
    protected long getMillisecondTime(Object date) {
        Instant instant = toInstant(date);
        if (instant != null) {
            return instant.toEpochMilli();
        }
        return Long.parseLong(date.toString());
    }

    /** Converts a supported representation to an Instant, or null if it cannot be converted. */
    protected Instant toInstant(Object value) {
        if (value instanceof Instant instantValue) {
            return instantValue;
        } else if (value instanceof Date dateValue) {
            return (dateValue).toInstant();
        } else if (value instanceof Calendar calendarValue) {
            return (calendarValue).toInstant();
        } else if (value instanceof ZonedDateTime zonedDateTime) {
            return (zonedDateTime).toInstant();
        } else if (value instanceof Number number) {
            return Instant.ofEpochMilli((number).longValue());
        } else if (value instanceof String string) {
            return parse(string);
        }
        return null;
    }

    /**
     * Parses an ISO-8601 instant ("2026-09-12T13:41:44Z"), an offset or zoned date-time, or a local
     * date-time (taken in the system time zone).
     */
    protected static Instant parse(String text) {
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            // not an instant
        }
        try {
            return ZonedDateTime.parse(text).toInstant();
        } catch (DateTimeParseException e) {
            // not zoned
        }
        try {
            return LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** The instant in the system time zone, for calendar-field comparisons. */
    protected static ZonedDateTime zoned(Instant instant) {
        return instant.atZone(ZoneId.systemDefault());
    }
}
