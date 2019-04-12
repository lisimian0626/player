package com.beidousat.player;

import java.io.Serializable;

/**
 * author: Hanson
 * date:   2018/1/17
 * describe:
 */

public class ISBaseSong extends ISBaseBean implements Serializable {
//    @SerializedName("id")
//    @PrimaryKey
//    @ColumnInfo(name = "id")
    protected int mId;
//    @SerializedName("s_id")
//    @ColumnInfo(name = "s_id")
    protected int mSId;
//    @SerializedName("simp_name")
//    @ColumnInfo(name = "simp_name")
    protected String mSimpName;
//    @SerializedName("singer_name")
//    @ColumnInfo(name = "singer_name")
    protected String mSinger;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getSimpName() {
        return mSimpName;
    }

    public void setSimpName(String simpName) {
        mSimpName = simpName;
    }

    public String getSinger() {
        return mSinger;
    }

    public void setSinger(String singer) {
        mSinger = singer;
    }

    public int getSId() {
        return mSId;
    }

    public void setSId(int SId) {
        mSId = SId;
    }
}
