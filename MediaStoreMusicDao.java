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
		// ׼������ֵ
		List<Music> musics = new ArrayList<Music>();

		// ׼��ContentResolver
		ContentResolver cr = context.getContentResolver();

		// ׼��Uri
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

		// ִ��ҵ��
		// -- ��ѯ
		String[] projection = {
				"_data", 		// 0 -> ·��
				"_size", 		// 1 -> �ļ���С
				"title",		// 2 -> ��������
				"duration", 	// 3 -> ʱ��
				"album", 		// 4 -> ר��
				"artist", 		// 5 -> ����
				"album_artist",	// 6 -> ר������
				"album_key"		// 7 -> ͼƬ�ı�ʶ
		};
		Cursor c = cr.query(uri, projection, null, null, null);
		// -- ����Cursor
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
		
		// �����ԡ�
		Log.i("tedu", "MusicDao.getData() -> �������ݣ���ʼ��");
		for (int i = 0; i < musics.size(); i++) {
			Log.v("tedu", "" + musics.get(i));
		}
		Log.i("tedu", "MusicDao.getData() -> �������ݣ���ɣ�");

		// �ͷ���Դ
		c.close();

		// ����
		return musics;
	}
	
	
	private String getAlbumArtByKey(String albumKey) {
		// ��������ֵ
		String albumArt = null;
		// �жϲ����Ƿ���Ч
		if (albumKey != null) {
			// ׼��ContentResolver
			ContentResolver cr = context.getContentResolver();
			// ׼��Uri
			Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
			// ִ��ҵ��
			// -- ��ѯ
			String[] projection = { "album_art" };
			String selection = "album_key=?";
			String[] selectionArgs = { albumKey };
			Cursor c = cr.query(uri, projection, selection, selectionArgs, null);
			// -- ����Cursor
			if (c.moveToFirst()) {
				albumArt = c.getString(0);
			}
			// �ͷ���Դ
			c.close();
		}
		// ����
		return albumArt;
	}

}
