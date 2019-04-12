package com.beidousat.player;

/**
 * author: Hanson
 * date:   2018/1/17
 * describe:
 */
public class ISSong extends ISBaseSong {
//    @SerializedName("song_file_size")
//    @ColumnInfo(name = "song_file_size")
    private long mSize;
//    @Ignore
    private long mSungStatics;
//    @Ignore
    private int isHD;
//    @SerializedName("is_grade_lib")
//    @Ignore
    private int isScore;
//    @SerializedName("mp3_file_path")
//    @ColumnInfo(name = "mp3_file_path")
    private String mMp3FilePath;
//    @SerializedName("melody_file_path")
//    @ColumnInfo(name = "melody_file_path")
    private String mAccompanyFilePath;
//    @SerializedName("lyric_file_path")
//    @ColumnInfo(name = "lyric_file_path")
    private String mLyricFilePath;
//    @Ignore
//    @SerializedName("collection")
//    private ISCollection mCollection;
//    @SerializedName("img")
    private String mCover;
//    @SerializedName("singer_img")
    private String mSingerImg;

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public String getCover() {
        return mCover;
    }

    public void setCover(String cover) {
        mCover = cover;
    }

    public long getSungStatics() {
        return mSungStatics;
    }

    public void setSungStatics(long sungStatics) {
        mSungStatics = sungStatics;
    }

    public boolean isHD() {
        return isHD != 0;
    }

    public void setHD(int HD) {
        isHD = HD;
    }

    public boolean isScore() {
        return isScore != 0;
    }

    public void setScore(int score) {
        isScore = score;
    }

    public String getMp3FilePath() {
        return mMp3FilePath;
    }

    public void setMp3FilePath(String mp3FilePath) {
        mMp3FilePath = mp3FilePath;
    }

    public String getAccompanyFilePath() {
        return mAccompanyFilePath;
    }

    public void setAccompanyFilePath(String accompanyFilePath) {
        mAccompanyFilePath = accompanyFilePath;
    }

    public String getLyricFilePath() {
        return mLyricFilePath;
    }

    public void setLyricFilePath(String lyricFilePath) {
        mLyricFilePath = lyricFilePath;
    }

    public String getLrcFilename() {
        return getFilename(mLyricFilePath);
    }

    public String getMusicFilename() {
        return getFilename(mMp3FilePath);
    }

    public String getAccompanyFilename() {
        return getFilename(mAccompanyFilePath);
    }

    private String getFilename(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
    }

//    public ISCollection getCollection() {
//        return mCollection;
//    }
//
//    public void setCollection(ISCollection collection) {
//        mCollection = collection;
//    }

    public String getSingerImg() {
        return mSingerImg;
    }

    public void setSingerImg(String singerImg) {
        mSingerImg = singerImg;
    }
}
