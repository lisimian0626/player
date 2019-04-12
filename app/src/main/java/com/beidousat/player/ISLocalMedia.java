package com.beidousat.player;

/**
 * author: Hanson
 * date:   2018/2/2
 * describe:
 */
//@Entity(tableName = "local_video")
public class ISLocalMedia extends ISBaseBean {
//    @PrimaryKey(autoGenerate = true)
//    @ColumnInfo(name = "id")
    private int mId;
//    @ColumnInfo(name = "path")
    private String mPath;
//    @ColumnInfo(name = "title")
    private String mTitle;
//    @ColumnInfo(name = "duration")
    private String mDuration;
//    @ColumnInfo(name = "cover")
    private String mCover;
//    @Embedded(prefix = "song_")
    private ISSong mSong;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        mDuration = duration;
    }

    public String getCover() {
        return mCover;
    }

    public void setCover(String cover) {
        mCover = cover;
    }

    public ISSong getSong() {
        return mSong;
    }

    public void setSong(ISSong song) {
        mSong = song;
    }

}
