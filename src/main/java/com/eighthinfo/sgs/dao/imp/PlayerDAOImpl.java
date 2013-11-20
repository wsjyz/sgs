package com.eighthinfo.sgs.dao.imp;

import com.eighthinfo.sgs.dao.BaseDAO;
import com.eighthinfo.sgs.dao.PlayerDAO;
import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.utils.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-18
 * Time: 下午5:36
 * To change this template use File | Settings | File Templates.
 */
@Repository("playerDAO")
public class PlayerDAOImpl extends BaseDAO implements PlayerDAO {


    @Override
    public int savePlayerRoom(String nickName, String roomId) {

        if(findRoomPlayerCounts(nickName,roomId) > 0){
              return 0;
        }

        int seatNo = getJdbcTemplate().queryForInt("select max(seat_no) from t_room_player where room_id=?",roomId);

        if(seatNo < 6){
            seatNo = seatNo + 1;
        }

        if(org.apache.commons.lang3.StringUtils.isNotBlank(nickName)
                && org.apache.commons.lang3.StringUtils.isNotBlank(roomId)){

            getJdbcTemplate().update("insert into t_room_player(id,nick_name,seat_no,room_id) values(?,?,?,?)",
                    StringUtils.genUUID(), nickName,seatNo, roomId);

        }
        return seatNo;
    }

    @Override
    public int findRoomPlayerCounts(String nickName, String roomId) {
        String sql = "select count(*) from t_room_player where room_id=? and nick_name=?";
        int counts = getJdbcTemplate().queryForInt(sql,new Object[]{roomId,nickName});
        return counts;
    }

    @Override
    public List<RoomPlayer> findRoomPlayer(String roomId) {

        String sql = "select nick_name,seat_no from t_room_player where room_id=?";

        return getJdbcTemplate().query(sql,new Object[]{roomId},new RowMapper<RoomPlayer>() {
            @Override
            public RoomPlayer mapRow(ResultSet resultSet, int i) throws SQLException {

                RoomPlayer roomPlayer = new RoomPlayer();

                roomPlayer.setNickName(resultSet.getString("nick_name"));
                roomPlayer.setSeatNo(resultSet.getInt("seat_no"));
                return roomPlayer;
            }
        });

    }
}
