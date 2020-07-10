class AndroidOutputFormat {
  static const AAC_ADTS = 6;
  static const AMR_NB = 3;
  static const AMR_WB = 4;
  static const DEFAULT = 0;
  static const MPEG_2_TS = 8;
  static const MPEG_4 = 2; // H.264/AAC data encapsulated in MPEG2/TS
  static const OGG = 11;
  static const THREE_GPP = 1;
  static const WEBM = 9; // VP8/VORBIS data in a WEBM container
  static const MP3 = 20;
}

class AndroidAudioEncoder {
  static const AAC = 3; // AAC Low Complexity (AAC-LC) audio codec
  static const AAC_ELD = 5; // Enhanced Low Delay AAC (AAC-ELD) audio codec
  static const AMR_NB = 1; // AMR (Narrowband) audio codec
  static const AMR_WB = 2; // AMR (Wideband) audio codec
  static const DEFAULT = 0;
  static const HE_AAC = 4; // High Efficiency AAC (HE-AAC) audio codec
  static const OPUS = 7; // Opus audio codec
  static const VORBIS = 6; // Ogg Vorbis audio codec (Support is optional)
}
