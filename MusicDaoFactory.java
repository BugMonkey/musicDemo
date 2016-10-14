package cn.tedu.media_player_v4.dal;

import android.content.Context;
import cn.tedu.media_player_v4.entity.Music;

public abstract class MusicDaoFactory {
	
	private MusicDaoFactory() {
		
	}

	public static IDao<Music> newInstance(Context context) {
		return new MediaStoreMusicDao(context);
		
	}
}
