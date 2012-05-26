package org.smardi.CliqService;

import java.util.*;

import android.content.*;
import android.media.*;

public class SoundManager {
	//싱글턴 패턴 적용
	private static SoundManager s_instance;
	
	public static SoundManager getInstance() {
		if(s_instance == null) {
			s_instance = new SoundManager();
		}
		return s_instance;
	}
	
	
	//멤버변수
	private SoundPool 		m_SoundPool;
	private HashMap			m_SoundPoolMap;
	private AudioManager	m_AudioManager;
	private Context			mContext;
	
	/**
	 * mSoundPool: sound를 만들고 플레이하는 데 사용하는 객체
	 * mSoundPoolMap: 한번 load된 후에 저장하는 Hashmap
	 * mAudioManager: 원하는 소리를 플레이하기 위한 서비스를 핸들
	 * mContext: application Context를 가져옴
	 * @param _context
	 */
	public void Init(Context _context) {
		//멤버 변수 생성과 초기화
		m_SoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		m_SoundPoolMap = new HashMap();
		m_AudioManager = (AudioManager)_context.getSystemService(Context.AUDIO_SERVICE);
		mContext = _context;
	}
	
	
	public void addSound(int _index, int _soundID) {
		int id = m_SoundPool.load(mContext, _soundID, 1);	//사운드를 로드
		m_SoundPoolMap.put(_index, id);	//해시맵에 아이디 값을 받아온 인덱스로 저장
	}
	
	public void play(int _index) {
		//사운드 재생
		float streamVolume = m_AudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume = streamVolume/m_AudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		m_SoundPool.play((Integer)m_SoundPoolMap.get(_index), streamVolume, streamVolume, 1, 0, 1f);
	}
	
	public void play(int _index, float volume) {
		//사운드 재생
		m_SoundPool.play((Integer)m_SoundPoolMap.get(_index), volume, volume, 1, 0, 1f);
	}
	
	public void playLooped(int _index) {
		//사운드 반복 재생
		float streamVolume = m_AudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume = streamVolume/m_AudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		m_SoundPool.play((Integer)m_SoundPoolMap.get(_index), streamVolume, streamVolume, 1, -1, 1f);
	}
}
