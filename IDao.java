package cn.tedu.media_player_v4.dal;

import java.util.List;

public interface IDao<T> {

	/**
	 * ��ȡ����
	 * 
	 * @return ���ݵ�List����
	 */
	List<T> getData();

}
