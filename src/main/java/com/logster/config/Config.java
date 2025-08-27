package com.logster.config;

import java.util.List;

public record Config (  List<DateFormatPattern> dateFormats, List<String> ignoreFileExtensions){
}
