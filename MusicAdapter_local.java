package cn.tedu.media_player_v4.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.tedu.media_player_v4.R;
import cn.tedu.media_player_v4.entity.Music;
import cn.tedu.media_player_v4.model.MusicModel;
import cn.tedu.media_player_v4.util.DateUtils;

public class MusicAdapter_local extends BaseAdapter<Music> {
	public MusicAdapter_local(Context context, List<Music> data) {
		super(context, data);
	}
	
	private static final int TYPE_NORMAL = 0;
	private static final int TYPE_SELECTED = 1;
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		return getData().get(position).isPlaying() ? TYPE_SELECTED : TYPE_NORMAL;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 1. 获取数据
		Music music = getData().get(position);
		
		// 2. 检查convertView
		ViewHolder holder;
		if (convertView == null) {
			if (getItemViewType(position) == TYPE_NORMAL) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_music, null);
			} else {
				convertView = getLayoutInflater().inflate(R.layout.list_item_music_selected, null);
			}
			holder = new ViewHolder();
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_music_item_title);
			holder.tvDuration = (TextView) convertView.findViewById(R.id.tv_music_item_duration);
			holder.tvAlbum = (TextView) convertView.findViewById(R.id.tv_music_item_album);
			holder.tvArtist = (TextView) convertView.findViewById(R.id.tv_music_item_artist);
			holder.ivImage = (ImageView) convertView.findViewById(R.id.iv_music_item_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		// 4. 显示数据
		holder.tvTitle.setText(music.getTitle());
		holder.tvDuration.setText(DateUtils.parseTime(music.getDuration()));
		holder.tvAlbum.setText(music.getAlbum());
		holder.tvArtist.setText(music.getArtist());
		
		if (music.getAlbumArt() == null) {
			holder.ivImage.setImageResource(R.drawable.default_music_pic);
		} else {
			Bitmap bm = BitmapFactory.decodeFile(music.getAlbumArt());
			holder.ivImage.setImageBitmap(bm);
		}
		
		// 5. 返回
		return convertView;
	}
	
	class ViewHolder {
		ImageView ivImage;
		TextView tvTitle;
		TextView tvDuration;
		TextView tvAlbum;
		TextView tvArtist;
	}

}
