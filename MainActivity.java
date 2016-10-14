package cn.tedu.media_player_v4.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.tedu.media_player_v4.R;
import cn.tedu.media_player_v4.adapter.MusicAdapter_local;
import cn.tedu.media_player_v4.adapter.SearchMusicAdapter;
import cn.tedu.media_player_v4.app.MusicApplication;
import cn.tedu.media_player_v4.dal.MediaStoreMusicDao;
import cn.tedu.media_player_v4.dal.MusicDaoFactory;
import cn.tedu.media_player_v4.entity.Music;
import cn.tedu.media_player_v4.entity.SongInfo;
import cn.tedu.media_player_v4.entity.SongUrl;
import cn.tedu.media_player_v4.fragment.HotMusicListFragment;
import cn.tedu.media_player_v4.fragment.NewMusicListFragment;
import cn.tedu.media_player_v4.model.LrcCallback;
import cn.tedu.media_player_v4.model.MusicListCallback;
import cn.tedu.media_player_v4.model.MusicModel;
import cn.tedu.media_player_v4.model.SongInfoCallback;
import cn.tedu.media_player_v4.service.DownloadService;
import cn.tedu.media_player_v4.service.PlayMusicService;
import cn.tedu.media_player_v4.service.PlayMusicService.MusicBinder;
import cn.tedu.media_player_v4.util.BitmapCallback;
import cn.tedu.media_player_v4.util.BitmapUtils;
import cn.tedu.media_player_v4.util.DateUtils;
import cn.tedu.media_player_v4.util.GlobalConsts;

public class MainActivity extends FragmentActivity {
	private ViewPager viewPager;
	private RadioGroup radioGroup;
	private RadioButton rbNew;
	private RadioButton rbHot;
	private ImageView ivCMPic;
	private TextView tvCMTitle;
	private RelativeLayout rlPlayMusic;
	private TextView tvPMTitle, tvPMSinger, tvPMLrc, tvPMCurrentTime, tvPMTotalTime;
	private ImageView ivPMBackground, ivPMAlbum;
	private SeekBar seekBar;
	MusicApplication app=new MusicApplication();
	private Button btnToSearch;
	private Button btnSearch;
	private Button btnCancel;
	private EditText etSearch;
	private RelativeLayout rlSearch;
	private ListView lvSearchResult;
	private RadioButton rbtn_net;
	private RadioButton  rbtn_local;

	private List<Fragment> fragments;
	private List<Music> searchMusics;
	private ServiceConnection conn;
	private MusicInfoReceiver receiver;
	protected MusicBinder binder;
	private MusicModel model;
	private CheckBox imgplay;
	private RadioGroup  rg_type;
	private RelativeLayout rl_list_net;
	private LinearLayout  ll_list_local;
    private int type=0;
    private ListView lv_local;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		model = new MusicModel();
		
	 
		// 初始化控件
		setViews();
		// 初始化viewpager的适配器
		setPagerAdapter();
		// 监听
		setListeners();
		// 绑定service
		bindMusicService();
		// 注册组件
		registComponents();
	}

	@Override
	protected void onDestroy() {
		// 取消service的绑定
		this.unbindService(conn);
		// 取消广播接收器的注册
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}

	/**
	 * 当点击后退键时执行
	 */
	public void onBackPressed() {
		if (rlPlayMusic.getVisibility() == View.VISIBLE) {
			// 隐藏
			rlPlayMusic.setVisibility(View.INVISIBLE);
			ScaleAnimation anim = new ScaleAnimation(1, 0, 1, 0, 0, rlPlayMusic.getHeight());
			anim.setDuration(300);
			rlPlayMusic.startAnimation(anim);

		} else {
			super.onBackPressed();
		}
	}

	/**
	 * 注册组件
	 */
	private void registComponents() {
		receiver = new MusicInfoReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_MUSIC_STARTED);
		filter.addAction(GlobalConsts.ACTION_NEXT_MUSIC);
		filter.addAction(GlobalConsts.ACTION_PRE_MUSIC);
		filter.addAction(GlobalConsts.ACTION_UPDATE_MUSIC_PROGRESS);
		this.registerReceiver(receiver, filter);
	}

	/**
	 * 绑定Service组件
	 */
	private void bindMusicService() {
		Intent intent = new Intent(this, PlayMusicService.class);
		conn = new ServiceConnection() {
			/** 连接异常断开时执行 */
			public void onServiceDisconnected(ComponentName name) {
			}

			/** 连接建立完成后执行 */
			public void onServiceConnected(ComponentName name, IBinder service) {
				binder = (MusicBinder) service;
				// 把binder对象传给NewMusicListFragment
				NewMusicListFragment f1 = (NewMusicListFragment) fragments.get(0);
				f1.setBinder(binder);
				HotMusicListFragment f2 = (HotMusicListFragment) fragments.get(1);
				f2.setBinder(binder);
			}
		};
		this.bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}

	// 监听
	private void setListeners() {
		// 给ivPMAlbum添加监听 点击后弹出AlertDialog 选择下载的版本
		lv_local.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				app.setPosition(arg2);
				String url=app.getMusics().get(arg2).getPath();
				
				
				// TODO Auto-generated method stub
				binder.playMusic(url);
			}
		});
		ivPMAlbum.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 获取当前正在播放的音乐
				try {
					MusicApplication app = MusicApplication.getApp();
					final Music m = app.getCurrentMusic();
					final List<SongUrl> urls = m.getUrls();
					// 弹出alertDialog 让用户选择版本
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					String[] items = new String[urls.size()];
					for (int i = 0; i < items.length; i++) {
						int size = urls.get(i).getFile_size();
						items[i] = Math.floor(size * 100.0 / 1024 / 1024) / 100.0 + "M";
					}
					builder.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							SongUrl url = urls.get(which);
							// 执行下载操作
							Log.i("info", "文件大小:" + url.getFile_size() + "文件路径:" + url.getFile_link());
							// 启动Service
							Intent intent = new Intent(MainActivity.this, DownloadService.class);
							intent.putExtra("url", url.getFile_link());
							intent.putExtra("filename", m.getTitle() + ".mp3");
							intent.putExtra("bitrate", url.getFile_bitrate() + "");
							intent.putExtra("total", url.getFile_size());
							startService(intent);
						}
					});
					AlertDialog dialog = builder.create();
					dialog.show();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// 给lvSearchResult列表添加监听
		lvSearchResult.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 把搜索结果列表 与 position存入application
				MusicApplication app = MusicApplication.getApp();
				app.setMusics(searchMusics,0);
				app.setPosition(position);
				// 当前选中的歌曲对象
				final Music m = searchMusics.get(position);
				String songId = m.getSong_id();
				model.loadSongInfoBySongId(songId, new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						// 把查询出来的基本信息给music对象属性赋值
						m.setUrls(urls);
						m.setInfo(info);
						// 播放音乐
						String fileLink = m.getUrls().get(0).getFile_link();
						binder.playMusic(fileLink);
						//TODO
					}
				});
			}
		});

		// 给btnSearch添加监听
		btnSearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 搜索歌曲
				String keyword = etSearch.getText().toString();
				if ("".equals(keyword)) {
					return;
				}
				// 发送请求 执行搜索
				model.searchMusicList(keyword, new MusicListCallback() {
					public void onMusicListLoaded(List<Music> musics) {
						// 把musics保存到成员变量
						searchMusics = musics;
						// 更新搜索结果列表
						SearchMusicAdapter searchAdapter = new SearchMusicAdapter(MainActivity.this, musics);
						lvSearchResult.setAdapter(searchAdapter);
					}
				});
			}
		});

		// 给btnCancel添加监听
		btnCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				rlSearch.setVisibility(View.INVISIBLE);
				TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -rlSearch.getHeight());
				anim.setDuration(300);
				rlSearch.startAnimation(anim);
			}
		});

		// 给btnToSearch添加监听
		btnToSearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				rlSearch.setVisibility(View.VISIBLE);
				TranslateAnimation anim = new TranslateAnimation(0, 0, -rlSearch.getHeight(), 0);
				anim.setDuration(300);
				rlSearch.startAnimation(anim);
			}
		});

		// 给rlPlayMusic添加touch事件 拦截事件
		rlPlayMusic.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// 自己消费touch事件
				return true;
			}
		});
		//aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
		
		// 给seekBar添加事件监听
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) { // 进度条的更新是由用户引起的
					binder.seekTo(progress);
				}
			}
		});

		// 给圆形ImageView添加事件监听 点击后弹出rlPlayMusic
		ivCMPic.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				rlPlayMusic.setVisibility(View.VISIBLE);
				// 使用动画显示整个rlPlayMusic
				ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1, 0, rlPlayMusic.getHeight());
				anim.setDuration(300);
				rlPlayMusic.startAnimation(anim);
			}
		});

		// viewpager控制radioGroup
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			public void onPageSelected(int position) {
				switch (position) {
				case 0: // 选择了第一页
					rbNew.setChecked(true);
					break;
				case 1: // 选中了第二页
					rbHot.setChecked(true);
					break;
				}
			}

			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			public void onPageScrollStateChanged(int arg0) {

			}
		});

		// radioGroup控制viewpager
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radioNew: // 选中了新歌榜
					viewPager.setCurrentItem(0);
					break;
				case R.id.radioHot: // 选中了热歌榜
					viewPager.setCurrentItem(1);
					break;
				}
			}
		});
		rg_type.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rbn_net:
					 ll_list_local.setVisibility(View.GONE);
					 rl_list_net.setVisibility(View.VISIBLE);
					 type=0;
					break;
				case R.id.rbn_local:
					ll_list_local.setVisibility(View.VISIBLE);
					rl_list_net.setVisibility(View.GONE);
					type=1;
					MediaStoreMusicDao dao=new MediaStoreMusicDao(getApplication());
					List<Music> music=dao.getData(); 
					Log.w("local", music.toString());
					 app.setMusics(music,1);	
						MusicAdapter_local adapter=new MusicAdapter_local(getApplication(),music);
						lv_local.setAdapter(adapter);
					break;
				
				}
				
			}
		});
		
	}

	/**
	 * 三个按钮的监听
	 * @param view
	 */
	public void controllMusic(View view){
		MusicApplication app = MusicApplication.getApp();
		switch (view.getId()) {
		case R.id.ivPMPre: //上一曲
			app.preMusic();  //让application执行上一曲的操作
			final Music m = app.getCurrentMusic();
			if(type==0){
			model.loadSongInfoBySongId(m.getSong_id(), new SongInfoCallback() {
				public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
					m.setUrls(urls);
					m.setInfo(info);
					//播放音乐
					
					String filelink = m.getUrls().get(0).getFile_link();
					binder.playMusic(filelink);
				}
			});}
			if(type==1){
				binder.playMusic(m.getPath());
			}
			break;
		case R.id.ivPMStart:  //暂停或播放
			boolean isPlaying=binder.startOrPause();
			
			if(isPlaying){
				imgplay.setChecked(true);
			}else{
				imgplay.setChecked(false);
			}
			break;
		case R.id.ivPMNext://下一曲
			app.nextMusic(); //跳转到下一曲
			final Music m2=app.getCurrentMusic();
			model.loadSongInfoBySongId(m2.getSong_id(), new SongInfoCallback() {
				public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
					m2.setUrls(urls);
					m2.setInfo(info);
					String url = m2.getUrls().get(0).getFile_link();
					binder.playMusic(url);
				}
			});
			
			break;
		}
	}

	// 初始化viewpager的适配器
	private void setPagerAdapter() {
		// 准备两个Fragment作为数据源
		fragments = new ArrayList<Fragment>();
		fragments.add(new NewMusicListFragment());
		fragments.add(new HotMusicListFragment());
		PagerAdapter pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);
		
		
	}

	/**
	 * 初始化控件
	 */
	private void setViews() {
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		rbNew = (RadioButton) findViewById(R.id.radioNew);
		rbHot = (RadioButton) findViewById(R.id.radioHot);

		ivCMPic = (ImageView) findViewById(R.id.ivCMPic);
		tvCMTitle = (TextView) findViewById(R.id.tvCMTitle);

		rlPlayMusic = (RelativeLayout) findViewById(R.id.rlPlayMusic);
		tvPMTitle = (TextView) findViewById(R.id.tvPMTitle);
		tvPMSinger = (TextView) findViewById(R.id.tvPMSinger);
		tvPMLrc = (TextView) findViewById(R.id.tvPMLrc);
		tvPMCurrentTime = (TextView) findViewById(R.id.tvPMCurrentTime);
		tvPMTotalTime = (TextView) findViewById(R.id.tvPMTotalTime);
		ivPMBackground = (ImageView) findViewById(R.id.ivPMBackground);
		ivPMAlbum = (ImageView) findViewById(R.id.ivPMAlbum);
		seekBar = (SeekBar) findViewById(R.id.seekBar);

		btnToSearch = (Button) findViewById(R.id.btnToSearch);
		btnSearch = (Button) findViewById(R.id.btnSearch);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		etSearch = (EditText) findViewById(R.id.etSearch);
		rlSearch = (RelativeLayout) findViewById(R.id.rlSearch);
		lvSearchResult = (ListView) findViewById(R.id.lvSearchResult);
		imgplay = (CheckBox) findViewById(R.id.ivPMStart);
		rbtn_net=(RadioButton) findViewById(R.id.rbn_net);
		rbtn_local=(RadioButton) findViewById(R.id.rbn_net);
		rg_type=(RadioGroup) findViewById(R.id.rg_type);
		
		rl_list_net=(RelativeLayout) findViewById(R.id.rl_net);
		ll_list_local=(LinearLayout) findViewById(R.id.lllocal);
		lv_local=(ListView) findViewById(R.id.lv_musics);
		
	}

	// 声明ViewPager的适配器类
	class MainPagerAdapter extends FragmentPagerAdapter {

		public MainPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

	}

	/**
	 * 广播接收器 接收音乐信息相关的广播
	 */
	class MusicInfoReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(GlobalConsts.ACTION_PRE_MUSIC)) {
				// 播放上一首歌曲
				Log.i("info", "接收到上一首歌曲广播...");
				ImageView ivPre = (ImageView) findViewById(R.id.ivPMPre);
				controllMusic(ivPre);
			} else if (action.equals(GlobalConsts.ACTION_NEXT_MUSIC)) {
				// 播放下一首歌曲
				Log.i("info", "接收到下一首歌曲广播...");
				ImageView ivNext = (ImageView) findViewById(R.id.ivPMNext);
				controllMusic(ivNext);
			} else if (action.equals(GlobalConsts.ACTION_UPDATE_MUSIC_PROGRESS)) {
				// 接收到了更新音乐进度的广播
				int total = intent.getIntExtra("total", 0);
				int progress = intent.getIntExtra("progress", 0);
				// 更新进度条
				seekBar.setMax(total);
				seekBar.setProgress(progress);
				// 更新两个textView
				String time = DateUtils.parseTime(progress);
				tvPMCurrentTime.setText(time);
				tvPMTotalTime.setText(DateUtils.parseTime(total));
				// 如果歌词已经加载完毕更新歌词信息
				Music m = MusicApplication.getApp().getCurrentMusic();
				HashMap<String, String> lrc = m.getLrc();
				if (lrc != null) { // 歌词是存在的
					String content = lrc.get(time);
					if (content != null) { // 当前时间需要更新歌词
						tvPMLrc.setText(content);
					}
				}
			} else if (action.equals(GlobalConsts.ACTION_MUSIC_STARTED)) {
				// 音乐开始播放 获取音乐的基本信息
				final Music m = MusicApplication.getApp().getCurrentMusic();
				// 更新底部栏中的imageView 与 TextView
				if(type==0){
				String smallPicPath = m.getPic_small();
				String title = m.getTitle();
				tvCMTitle.setText(title);
				if (smallPicPath == null || smallPicPath.equals("")) {
					// 没有路径
					smallPicPath = m.getInfo().getPic_small();
				}
				BitmapUtils.loadBitmap(smallPicPath, new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if (bitmap != null) { // 下载成功
							ivCMPic.setImageBitmap(bitmap);
							// 让imageView转起来
							RotateAnimation anim = new RotateAnimation(0, 360, ivCMPic.getWidth() / 2,
									ivCMPic.getHeight() / 2);
							anim.setDuration(10000);
							// 匀速旋转
							anim.setInterpolator(new LinearInterpolator());
							// 无限重复
							anim.setRepeatCount(Animation.INFINITE);
							ivCMPic.startAnimation(anim);
						} else {
							ivCMPic.setImageResource(R.drawable.icon_music);
						}
					}
				});
				// 更新播放界面中的ivPMAlbum
				String albumPic = m.getInfo().getAlbum_500_500();
				if (albumPic.equals("")) {
					albumPic = m.getInfo().getAlbum_1000_1000();
				}
				BitmapUtils.loadBitmap(albumPic, new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if (bitmap != null) { // 下载
							ivPMAlbum.setImageBitmap(bitmap);
						} else {
							ivPMAlbum.setImageResource(R.drawable.default_music_pic);
						}
					}
				});
				// 更新背景图片 ivPMBackground
				String backPic = m.getInfo().getArtist_480_800();
				if (backPic.equals("")) {
					backPic = m.getInfo().getArtist_640_1136();
				}
				if (backPic.equals("")) {
					backPic = m.getInfo().getArtist_500_500();
				}
				if (backPic.equals("")) {
					backPic = albumPic;
				}
				BitmapUtils.loadBitmap(backPic, 8, new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if (bitmap != null) { // 背景图片下载成功
							// 把背景图片模糊化处理
							BitmapUtils.loadBlurBitmap(bitmap, 10, new BitmapCallback() {
								public void onBitmapLoaded(Bitmap bitmap) {
									ivPMBackground.setImageBitmap(bitmap);
								}
							});
						} else {
							ivPMBackground.setImageResource(R.drawable.default_music_background);
						}
					}
				});
				// 更新tvPMTitle tvPMSinger
				tvPMTitle.setText(m.getInfo().getTitle());
				tvPMSinger.setText(m.getInfo().getAuthor());
				// 下载歌词 并且解析歌词
				if (m.getLrc() != null) { // 以前已经下载过了
					return;
				}
				String lrcPath = m.getInfo().getLrclink();
				if (lrcPath == null || lrcPath.equals("")) {
					Toast.makeText(context, "该歌曲没有歌词", Toast.LENGTH_SHORT).show();
					return;
				}
				model.loadLrc(lrcPath, new LrcCallback() {
					public void onLrcLoaded(HashMap<String, String> lrc) {
						m.setLrc(lrc); // 歌词加载解析保存完毕
					}
				});
			}
			}
			if(type==1){
				Log.w("=", "跳转");
			}
		}
	}

}
