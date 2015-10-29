package com.seojunkyo.soma.controlhome.databases;

import android.provider.BaseColumns;

/**
 * Created by seojunkyo on 15. 10. 27..
 */
public final class DataBases {

    public static final class CreateDB implements BaseColumns{
        public static final String SPACE = "장소";
        public static final String ADDRESS = "주소";
        public static final String _TABLENAME = "HOME";
        public static final String _CREATE =
                "create table "+_TABLENAME+"("
                        +_ID+" integer primary key autoincrement, "
                        +SPACE+" text not null , "
                        +ADDRESS+" text not null , ";
    }
}
