package com.ramusthastudio.bluemixbot.util;

public final class StickerHelper {
  public static final String[] JAMES_STICKER_CHARMING1 = new String[] {"119", "1", "100"};
  public static final String[] JAMES_STICKER_CHARMING2 = new String[] {"120", "1", "100"};
  public static final String[] JAMES_STICKER_BLOOD = new String[] {"121", "1", "100"};
  public static final String[] JAMES_STICKER_WHATEVER = new String[] {"122", "1", "100"};
  public static final String[] JAMES_STICKER_OH_NO = new String[] {"123", "1", "100"};
  public static final String[] JAMES_STICKER_COFFEE = new String[] {"124", "1", "100"};
  public static final String[] JAMES_STICKER_TWO_THUMBS = new String[] {"125", "1", "100"};
  public static final String[] JAMES_STICKER_SAD_PRAY = new String[] {"126", "1", "100"};
  public static final String[] JAMES_STICKER_AFRAID = new String[] {"127", "1", "100"};
  public static final String[] JAMES_STICKER_PIG_FACE = new String[] {"128", "1", "100"};
  public static final String[] JAMES_STICKER_SHOCK = new String[] {"129", "1", "100"};
  public static final String[] JAMES_STICKER_WHISHEL = new String[] {"130", "1", "100"};
  public static final String[] JAMES_STICKER_CRY_LEAVE = new String[] {"131", "1", "100"};
  public static final String[] JAMES_STICKER_CHEERS = new String[] {"132", "1", "100"};
  public static final String[] JAMES_STICKER_ANGRY = new String[] {"133", "1", "100"};
  public static final String[] JAMES_STICKER_POINT_YOU = new String[] {"134", "1", "100"};
  public static final String[] JAMES_STICKER_USELESS = new String[] {"135", "1", "100"};
  public static final String[] JAMES_STICKER_SELFIE = new String[] {"136", "1", "100"};
  public static final String[] JAMES_STICKER_WHISESS = new String[] {"137", "1", "100"};
  public static final String[] JAMES_STICKER_HORAY = new String[] {"138", "1", "100"};
  public static final String[] JAMES_STICKER_SCREAM = new String[] {"139", "1", "100"};

  public static class StickerMsg {
    private final String stkId;
    private final String stkPkgId;
    private final String stkVer;

    public StickerMsg(String[] aSticker) {
      stkId = aSticker[0];
      stkPkgId = aSticker[1];
      stkVer = aSticker[2];
    }
    public String id() { return stkId; }
    public String pkgId() { return stkPkgId; }
    public String ver() { return stkVer; }

    @Override public String toString() {
      return "StickerMsg{" +
          "stkId=" + stkId +
          ", stkPkgId=" + stkPkgId +
          ", stkVer=" + stkVer +
          '}';
    }
  }
}
