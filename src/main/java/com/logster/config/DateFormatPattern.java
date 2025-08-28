package com.logster.config;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public record DateFormatPattern(Pattern regExp, DateTimeFormatter dateFormat) {
}
