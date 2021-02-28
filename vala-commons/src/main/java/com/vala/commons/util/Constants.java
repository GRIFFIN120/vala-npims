package com.vala.commons.util;

import java.text.SimpleDateFormat;

public final class Constants {
    public final static String DIR_DESC = "desc";
    public final static String DIR_ASC = "asc";
    public final static String DEFAULT_ORDER_COLUMN = "timestamp";

    public final static String SORT_UP = "before";
    public final static String SORT_DOWN = "after";

    public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    public final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
}
