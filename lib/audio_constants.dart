class AndroidOutputFormat {
  static const AAC_ADTS = 6;
  static const AMR_NB = 3;
  static const AMR_WB = 4;
  static const MPEG_4 = 2; // H.264/AAC data encapsulated in MPEG2/TS
  static const OGG = 11;
  static const MP3 = 101;
}

class AndroidAudioEncoder {
  static const AAC = 3; // AAC Low Complexity (AAC-LC) audio codec
  static const AAC_ELD = 5; // Enhanced Low Delay AAC (AAC-ELD) audio codec
  static const AMR_NB = 1; // AMR (Narrowband) audio codec
  static const AMR_WB = 2; // AMR (Wideband) audio codec
  static const HE_AAC = 4; // High Efficiency AAC (HE-AAC) audio codec
  static const OPUS = 7; // Opus audio codec
  static const MP3 = 101;
}

class EncoderFormat {
  final int encoder;
  final int outputFormat;

  EncoderFormat(this.encoder, this.outputFormat);
}

EncoderFormat formatToEncoderFormat(AudioFormat audioFormat) {
  switch (audioFormat) {
    case AudioFormat.aac_ADTS:
      return EncoderFormat(
          AndroidAudioEncoder.AAC, AndroidOutputFormat.AAC_ADTS);
      break;
    case AudioFormat.aac_MPEG:
      return EncoderFormat(AndroidAudioEncoder.AAC, AndroidOutputFormat.MPEG_4);
      break;
    case AudioFormat.aac_eld_ADTS:
      return EncoderFormat(
          AndroidAudioEncoder.AAC_ELD, AndroidOutputFormat.AAC_ADTS);
      break;
    case AudioFormat.aac_eld_MPEG:
      return EncoderFormat(
          AndroidAudioEncoder.AAC_ELD, AndroidOutputFormat.MPEG_4);
      break;
    case AudioFormat.he_aac_ADTS:
      return EncoderFormat(
          AndroidAudioEncoder.HE_AAC, AndroidOutputFormat.AAC_ADTS);
      break;
    case AudioFormat.he_aac_MPEG:
      return EncoderFormat(
          AndroidAudioEncoder.HE_AAC, AndroidOutputFormat.MPEG_4);
      break;
    case AudioFormat.amr_nb:
      return EncoderFormat(
          AndroidAudioEncoder.AMR_NB, AndroidOutputFormat.AMR_NB);
      break;
    case AudioFormat.amr_wb:
      return EncoderFormat(
          AndroidAudioEncoder.AMR_WB, AndroidOutputFormat.AMR_WB);
      break;
    case AudioFormat.opus_OGG:
      return EncoderFormat(AndroidAudioEncoder.OPUS, AndroidOutputFormat.OGG);
      break;
    case AudioFormat.opus_MPEG:
      return EncoderFormat(
          AndroidAudioEncoder.OPUS, AndroidOutputFormat.MPEG_4);
      break;
    case AudioFormat.mp3:
      return EncoderFormat(AndroidAudioEncoder.MP3, AndroidOutputFormat.MP3);
      break;
  }
}

enum AudioFormat {
  aac_ADTS, // File extension: .aac; Availible for: android
  aac_MPEG, // File extension: .m4a; Availible for: android
  aac_eld_ADTS, // File extension: .aac; Availible for: android
  aac_eld_MPEG, // File extension: .m4a; Availible for: android
  he_aac_ADTS, // File extension: .aac; Availible for: android
  he_aac_MPEG, // File extension: .m4a; Availible for: android
  amr_nb, // File extension: .amr/.3ga; Availible for: android
  amr_wb, // File extension: .amr/.3ga; Availible for: android
  opus_OGG, // File extension: .ogg; Availible for: android
  opus_MPEG, // File extension: .m4a; Availible for: android
  mp3 // File extension: .mp3; Availible for: android
}
