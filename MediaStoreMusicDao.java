package cn.tedu.media_player_v4.dal;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import cn.tedu.media_player_v4.entity.Music;

public class MediaStoreMusicDao implements IDao<Music> {
	
	private Context context;

	public MediaStoreMusicDao(Context context) {
		super();
		this.context = context;
	}

	@Override
	public List<Music> getData() {
		// 准备返回值
		List<Music> musics = new ArrayList<Music>();

		// 准备ContentResolver
		ContentResolver cr = context.getContentResolver();

		// 准备Uri
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

		// 执行业务：
		// -- 查询
		String[] projection = {
				"_data", 		// 0 -> 路径
				"_size", 		// 1 -> 文件大小
				"title",		// 2 -> 歌曲标题
				"duration", 	// 3 -> 时长
				"album", 		// 4 -> 专辑
				"artist", 		// 5 -> 作者
				"album_artist",	// 6 -> 专辑作者
				"album_key"		// 7 -> 图片的标识
		};
		Cursor c = cr.query(uri, projection, null, null, null);
		// -- 遍历Cursor
		if (c.moveToFirst()) {
			for (; !c.isAfterLast(); c.moveToNext()) {
				Music music = new Music();
				music.setPath(c.getString(0));
				music.setSize(c.getInt(1));
				music.setTitle(c.getString(2));
				music.setDuration(c.getInt(3));
				music.setAlbum(c.getString(4));
				music.setArtist(c.getString(5));
				music.setAlbumArtist(c.getString(6));
				music.setAlbumKey(c.getString(7));
				music.setAlbumArt(getAlbumArtByKey(music.getAlbumKey()));
				musics.add(music);
			}
		}
		
		// 【测试】
		Log.i("tedu", "MusicDao.getData() -> 检验数据，开始：");
		for (int i = 0; i < musics.size(); i++) {
			Log.v("tedu", "" + musics.get(i));
		}
		Log.i("tedu", "MusicDao.getData() -> 检验数据，完成！");

		// 释放资源
		c.close();

		// 返回
		return musics;
	}
	
	
	private String getAlbumArtByKey(String albumKey) {
		// 声明返回值
		String albumArt = null;
		// 判断参数是否有效
		if (albumKey != null) {
			// 准备ContentResolver
			ContentResolver cr = context.getContentResolver();
			// 准备Uri
			Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
			// 执行业务：
			// -- 查询
			String[] projection = { "album_art" };
			String selection = "album_key=?";
			String[] selectionArgs = { albumKey };
			Cursor c = cr.query(uri, projection, selection, selectionArgs, null);
			// -- 处理Cursor
			if (c.moveToFirst()) {
				albumArt = c.getString(0);
			}
			// 释放资源
			c.close();
		}
		// 返回
		return albumArt;
	}

}
