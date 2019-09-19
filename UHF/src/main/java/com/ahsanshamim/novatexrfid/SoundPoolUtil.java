package com.ahsanshamim.novatexrfid;

import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.ahsanshamim.novatexrfid.R;

public class SoundPoolUtil {


	public static SoundPool sp ;
	public static Map<Integer, Integer> suondMap;
	public static Context context;
	static float audioCurrentVolume;

	static int playId=0;
	static int soundId=0;


	//初始化声音池
	public static void initSoundPool(Context context){
		SoundPoolUtil.context = context;
		sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		soundId=sp.load(context, R.raw.msg, 1);


		AudioManager am = (AudioManager)SoundPoolUtil.context.getSystemService(SoundPoolUtil.context.AUDIO_SERVICE);
		//返回当前AlarmManager最大音量
		float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		//返回当前AudioManager对象的音量值
		audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		//float volumnRatio = audioCurrentVolume/audioMaxVolume;

	}

	//放声音池声音
	public static  void play(final int number){
		sp.unload(soundId);
		soundId=sp.load(context, R.raw.msg, 1);
		sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				playId=sp.play(
						soundId, //播放的音乐Id
						audioCurrentVolume, //左声道音量
						audioCurrentVolume, //右声道音量
						1, //优先级，0为最低
						number, //循环次数，0无不循环，-1无永远循环
						1);//回放速度，值在0.5-2.0之间，1为正常速度
			}
		});
	}

}
