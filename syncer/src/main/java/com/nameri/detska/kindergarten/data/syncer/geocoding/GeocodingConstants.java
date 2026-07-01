package com.nameri.detska.kindergarten.data.syncer.geocoding;

import java.util.Map;

public class GeocodingConstants {

    static final String FIELD_STATUS = "status";
    static final String FIELD_RESULTS = "results";
    static final String FIELD_ADDRESS_COMPONENTS = "address_components";
    static final String FIELD_TYPES = "types";
    static final String FIELD_LONG_NAME = "long_name";
    static final String FIELD_SHORT_NAME = "short_name";
    static final String FIELD_GEOMETRY = "geometry";
    static final String FIELD_LOCATION = "location";
    static final String FIELD_LATITUDE = "lat";
    static final String FIELD_LONGITUDE = "lng";

    static final String STATUS_OK = "OK";
    static final String STATUS_ZERO_RESULTS = "ZERO_RESULTS";

    static final String TYPE_LOCALITY = "locality";

    static final Map<Character, String> BG_TO_LATIN = Map.ofEntries(
        Map.entry('А', "A"), Map.entry('а', "a"),
        Map.entry('Б', "B"), Map.entry('б', "b"),
        Map.entry('В', "V"), Map.entry('в', "v"),
        Map.entry('Г', "G"), Map.entry('г', "g"),
        Map.entry('Д', "D"), Map.entry('д', "d"),
        Map.entry('Е', "E"), Map.entry('е', "e"),
        Map.entry('Ж', "Zh"), Map.entry('ж', "zh"),
        Map.entry('З', "Z"), Map.entry('з', "z"),
        Map.entry('И', "I"), Map.entry('и', "i"),
        Map.entry('Й', "Y"), Map.entry('й', "y"),
        Map.entry('К', "K"), Map.entry('к', "k"),
        Map.entry('Л', "L"), Map.entry('л', "l"),
        Map.entry('М', "M"), Map.entry('м', "m"),
        Map.entry('Н', "N"), Map.entry('н', "n"),
        Map.entry('О', "O"), Map.entry('о', "o"),
        Map.entry('П', "P"), Map.entry('п', "p"),
        Map.entry('Р', "R"), Map.entry('р', "r"),
        Map.entry('С', "S"), Map.entry('с', "s"),
        Map.entry('Т', "T"), Map.entry('т', "t"),
        Map.entry('У', "U"), Map.entry('у', "u"),
        Map.entry('Ф', "F"), Map.entry('ф', "f"),
        Map.entry('Х', "H"), Map.entry('х', "h"),
        Map.entry('Ц', "Ts"), Map.entry('ц', "ts"),
        Map.entry('Ч', "Ch"), Map.entry('ч', "ch"),
        Map.entry('Ш', "Sh"), Map.entry('ш', "sh"),
        Map.entry('Щ', "Sht"), Map.entry('щ', "sht"),
        Map.entry('Ъ', "A"), Map.entry('ъ', "a"),
        Map.entry('Ь', "Y"), Map.entry('ь', "y"),
        Map.entry('Ю', "Yu"), Map.entry('ю', "yu"),
        Map.entry('Я', "Ya"), Map.entry('я', "ya")
    );
}
