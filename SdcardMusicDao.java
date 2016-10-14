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
	 * 获取歌曲的数据
	 * 
	 * @return 歌曲数据的List集合，该返回值永不为null。
	 */
	public List<Music> getData() {
		// 创建对象
		List<Music> musics = new ArrayList<Music>();
		
		// 检查SDCARD的状态
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			// 获取Music文件夹的File对象
			File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
			// 检查Music文件夹是否存在
			if (musicDir.exists()) {
				// 获取Music文件夹下的文件列表(File[])
				File[] files = musicDir.listFiles();
				// 检查文件列表是否有效
				if (files != null && files.length > 0) {
					// 遍历文件列表
					for (int i = 0; i < files.length; i++) {
						// -- 检查是否是文件
						if (files[i].isFile()) {
							// -- 检查是否是.mp3文件
							String fileName = files[i].getName();
							if (fileName.toLowerCase(Locale.CHINA).endsWith(".mp3")) {
								// -- -- 创建当前遍历到的歌曲的数据对象，并封装数据
								Music music = new Music();
								music.setTitle(fileName.substring(0, fileName.length() - 4));
								music.setPath(files[i].getAbsolutePath());
								// -- -- 将歌曲数据添加到集合中
								musics.add(music);
							}
						}
					}
				}
			}
		}

		// 调试：检查获取的数据是否正确
		Log.i("tedu", "调试：检查获取的数据是否正确");
		for (int i = 0; i < musics.size(); i++) {
			Log.v("tedu", "MusicDao -> " + musics.get(i));
		}
		Log.i("tedu", "调试：检查获取的数据完成！");

		// 返回
		return musics;
	}
}
