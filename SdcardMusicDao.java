package cn.tedu.media_player_v4.dal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;
import cn.tedu.media_player_v4.entity.Music;

/**
 * Music Data Access Object
 */
class SdcardMusicDao implements IDao<Music> {

	/**
	 * ��ȡ����������
	 * 
	 * @return �������ݵ�List���ϣ��÷���ֵ����Ϊnull��
	 */
	public List<Music> getData() {
		// ��������
		List<Music> musics = new ArrayList<Music>();
		
		// ���SDCARD��״̬
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			// ��ȡMusic�ļ��е�File����
			File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
			// ���Music�ļ����Ƿ����
			if (musicDir.exists()) {
				// ��ȡMusic�ļ����µ��ļ��б�(File[])
				File[] files = musicDir.listFiles();
				// ����ļ��б��Ƿ���Ч
				if (files != null && files.length > 0) {
					// �����ļ��б�
					for (int i = 0; i < files.length; i++) {
						// -- ����Ƿ����ļ�
						if (files[i].isFile()) {
							// -- ����Ƿ���.mp3�ļ�
							String fileName = files[i].getName();
							if (fileName.toLowerCase(Locale.CHINA).endsWith(".mp3")) {
								// -- -- ������ǰ�������ĸ��������ݶ��󣬲���װ����
								Music music = new Music();
								music.setTitle(fileName.substring(0, fileName.length() - 4));
								music.setPath(files[i].getAbsolutePath());
								// -- -- ������������ӵ�������
								musics.add(music);
							}
						}
					}
				}
			}
		}

		// ���ԣ�����ȡ�������Ƿ���ȷ
		Log.i("tedu", "���ԣ�����ȡ�������Ƿ���ȷ");
		for (int i = 0; i < musics.size(); i++) {
			Log.v("tedu", "MusicDao -> " + musics.get(i));
		}
		Log.i("tedu", "���ԣ�����ȡ��������ɣ�");

		// ����
		return musics;
	}
}
