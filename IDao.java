package cn.tedu.media_player_v4.dal;

import java.util.List;

public interface IDao<T> {

	/**
	 * 获取数据
	 * 
	 * @return 数据的List集合
	 */
	List<T> getData();

}
