package cn.tedu.media_player_v4.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;

public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {
	private Context context;
	private List<T> data;
	private LayoutInflater layoutInflater;

	public BaseAdapter(Context context, List<T> data) {
		super();
		setContext(context);
		setData(data);
		layoutInflater = LayoutInflater.from(context);
	}
	
	protected final LayoutInflater getLayoutInflater() {
		return layoutInflater;
	}

	protected final Context getContext() {
		return context;
	}

	protected final List<T> getData() {
		return data;
	}

	private void setData(List<T> data) {
		if (data == null) {
			data = new ArrayList<T>();
		}
		this.data = data;
	}

	private void setContext(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("参数Context不允许为null！！！");
		}
		this.context = context;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

}
