/*
 * Copyright (C) 2013 Fairphone Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fairphone.launcher.gappsinstaller;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GappsInstallationAssets {
	public static final String MOUNT_SYSTEM_RO = "mount -o remount,ro /system";
	public static final String MOUNT_SYSTEM_RW = "mount -o remount,rw /system";
	public static final String SUPER_USER_COMMAND = "su";
	
	// install jars
		public static String [] install_files = {
				// app
			"system/app/ChromeBookmarksSyncAdapter.apk",
			"system/app/ConfigUpdater.apk",
			"system/app/GenieWidget.apk",
			"system/app/Gmail.apk",
			"system/app/GmsCore.apk",
			"system/app/GoogleBackupTransport.apk",
			"system/app/GoogleCalendarSyncAdapter.apk",
			"system/app/GoogleContactsSyncAdapter.apk",
			"system/app/GoogleEars.apk",
			"system/app/GoogleFeedback.apk",
			"system/app/GoogleLoginService.apk",
			"system/app/GooglePartnerSetup.apk",
			"system/app/GooglePlus.apk",
			"system/app/GoogleServicesFramework.apk",
			"system/app/GoogleTTS.apk",
			"system/app/LatinImeDictionaryPack.apk",
			"system/app/MediaUploader.apk",
			"system/app/NetworkLocation.apk",
			"system/app/OneTimeInitializer.apk",
			"system/app/Phonesky.apk",
			"system/app/QuickSearchBox.apk",
			"system/app/SetupWizard.apk",
			"system/app/TalkBack.apk",
			"system/app/VoiceSearchStub.apk",
			"system/app/GoogleMaps.apk",
			    // etc
			"system/etc/g.prop",
			"system/etc/permissions/com.google.android.maps.xml",
			"system/etc/permissions/com.google.android.media.effects.xml",
			"system/etc/permissions/com.google.widevine.software.drm.xml",
			"system/etc/permissions/features.xml",
//			"system/etc/preferred-apps/google.xml",
				// framework
			"system/framework/com.google.android.maps.jar",
			"system/framework/com.google.android.media.effects.jar",
			"system/framework/com.google.widevine.software.drm.jar",
				// libs
			"system/lib/libAppDataSearch.so",
			"system/lib/libfilterpack_facedetect.so",
			"system/lib/libfrsdk.so",
			"system/lib/libgames_rtmp_jni.so",
			"system/lib/libgcomm_jni.so",
			"system/lib/libgoogle_recognizer_jni_l.so",
			"system/lib/libgoogle_recognizer_jni.so",
			"system/lib/libgtalk_jni.so",
			"system/lib/libgtalk_stabilize.so",
			"system/lib/libjni_latinime.so",
			"system/lib/libpatts_engine_jni_api.so",
			"system/lib/libplus_jni_v8.so",
			"system/lib/librs.antblur_constant.so",
			"system/lib/librs.antblur_drama.so",
			"system/lib/librs.antblur.so",
			"system/lib/librs.drama.so",
			"system/lib/librs.film_base.so",
			"system/lib/librs.fixedframe.so",
			"system/lib/librs.grey.so",
			"system/lib/librs.image_wrapper.so",
			"system/lib/librs.retrolux.so",
			"system/lib/librsjni.so",
			"system/lib/libRSSupport.so",
			"system/lib/libspeexwrapper.so",
			"system/lib/libvcdecoder_jni.so",
			"system/lib/libvorbisencoder.so",
			"system/lib/libwebp_android.so",
				// tts
			"system/tts/lang_pico/de-DE_gl0_sg.bin",
			"system/tts/lang_pico/de-DE_ta.bin",
			"system/tts/lang_pico/es-ES_ta.bin",
			"system/tts/lang_pico/es-ES_zl0_sg.bin",
			"system/tts/lang_pico/fr-FR_nk0_sg.bin",
			"system/tts/lang_pico/fr-FR_ta.bin",
			"system/tts/lang_pico/it-IT_cm0_sg.bin",
			"system/tts/lang_pico/it-IT_ta.bin",
				// usr
			"system/usr/srec/en-US/acoustic_model",
			"system/usr/srec/en-US/c_fst",
			"system/usr/srec/en-US/clg",
			"system/usr/srec/en-US/compile_grammar.config",
			"system/usr/srec/en-US/contacts.abnf",
			"system/usr/srec/en-US/dict",
			"system/usr/srec/en-US/dictation.config",
			"system/usr/srec/en-US/embed_phone_nn_model",
			"system/usr/srec/en-US/embed_phone_nn_state_sym",
			"system/usr/srec/en-US/endpointer_dictation.config",
			"system/usr/srec/en-US/endpointer_voicesearch.config",
			"system/usr/srec/en-US/ep_acoustic_model",
			"system/usr/srec/en-US/g2p_fst",
			"system/usr/srec/en-US/google_hotword_clg",
			"system/usr/srec/en-US/google_hotword_logistic",
			"system/usr/srec/en-US/google_hotword.config",
			"system/usr/srec/en-US/grammar.config",
			"system/usr/srec/en-US/hmmsyms",
			"system/usr/srec/en-US/hotword_symbols",
			"system/usr/srec/en-US/lintrans_model",
			"system/usr/srec/en-US/metadata",
			"system/usr/srec/en-US/norm_fst",
			"system/usr/srec/en-US/normalizer",
			"system/usr/srec/en-US/offensive_word_normalizer",
			"system/usr/srec/en-US/phonelist",
			"system/usr/srec/en-US/rescoring_lm",
			"system/usr/srec/en-US/symbols"
		};		
		
		public static void executeSingleCommand(DataOutputStream os, InputStream is,
				String tmpCmd, boolean cmdRequiresAnOutput) throws IOException {
			os.writeBytes(tmpCmd+"\n");
			int readed = 0;
			byte[] buff = new byte[4096];

			// if cmd requires an output
			// due to the blocking behaviour of read(...)
			if (cmdRequiresAnOutput) {
			    while( is.available() <= 0) {
			        try { Thread.sleep(200); } catch(Exception ex) {}
			    }

			    while( is.available() > 0) {
			        readed = is.read(buff);
			        if ( readed <= 0 ) 
			        	break;
			    }
			}
		}
		
}
