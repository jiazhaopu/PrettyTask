package com.jzp.task.aync;//package com.jzp;
//
//import org.apache.commons.dbcp.BasicDataSource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.sql.Connection;
//import java.sql.DatabaseMetaData;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.sql.Timestamp;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map.Entry;
//
//import javax.sql.DataSource;
//
///**
// * CREATE TABLE `task_info` (
// *   `id` int(11) NOT NULL AUTO_INCREMENT,
// *   `schedule_type` int(11) DEFAULT NULL,
// *   `content` varchar(1000) DEFAULT NULL,
// *   `cron` varchar(100) DEFAULT NULL,
// *   `handler` varchar(100) DEFAULT NULL,
// *   `execute_times` int(11) DEFAULT '0',
// *   `max_execute_times` int(1) DEFAULT '0',
// *   `next_time` bigint(6) NOT NULL,
// *   `host` varchar(11) DEFAULT NULL,
// *   `status` int(11) DEFAULT NULL,
// *   `create_time` datetime DEFAULT NULL,
// *   `update_time` datetime DEFAULT NULL,
// *   PRIMARY KEY (`id`),
// *   KEY `idx_next_time` (`next_time`)
// * ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
// * @author 58
// *
// */
//public class MsgStorage {
//    private static String tableName = "task_info" ;
//    private static String insertSQL = "insert into %s(content,topic,tag,status) values(?,?,?,?) ";
//    private static String selectByIdSQL = "select * from %s where id=? ";
//    private static String selectMinIdOfWaitingSQL = "select min(id) from %s where status=? ";
//    private static String updateStatusSQL = "update %s set status=? where id=?";
//    private static String selectWaitingMsgSQL = "select * from %s where next_time <= ? order by next_time";
//    private static String selectWaitingMsgWithTopicsSQL = "select id,content,topic,tag,status,create_time from %s where status=?  and create_time >= ? and topic in ";
//    private static String deleteMsgSQL = "delete from %s where status=? and create_time <=?  limit ?";
//    //private static final String deleteMsgWithDayStr = "delete from mq_messages where status=? and create_time <= timestampadd(day,-%d,current_timestamp) limit ?";
//    //private static String deleteMsgWithDaySQL = null;
//    protected static final int waitingStatus = 1;
//    protected static final int sendedStatus = 2; //已经发送出去
//    private static final String driverClass = "com.mysql.jdbc.Driver";
//    private static long dayTimeDiff ;
//    private static int minuteTimeDiff = 1000 * 60 * 10 ; //10分钟
//    private static final Logger log = LoggerFactory.getLogger(MsgStorage.class);
//    private List<DBDataSource> dbDataSources;
//    private HashMap<String,DataSource> dataSourcesMap;
//    private Config config;
//    public MsgStorage(List<DBDataSource> dbDataSources){
//        this.dbDataSources = dbDataSources;
//        this.dataSourcesMap = new HashMap<String,DataSource>();
//        //this.config = config;
//    }
//
//    public void init(Config config){
//        log.info("start init MsgStorage db Size {} msg Store {} day",dbDataSources.size(),config.getHistoryMsgStoreTime());
//        this.config = config;
//        initSql();
//        for(DBDataSource dbSrc:dbDataSources){
//            BasicDataSource result = new BasicDataSource();
//            result.setDriverClassName(driverClass);
//            result.setUrl(dbSrc.getUrl());
//            result.setUsername(dbSrc.getUsername());
//            result.setPassword(dbSrc.getPassword());
//            result.setInitialSize(this.config.getMinIdleConnectionNum());
//            result.setMinIdle(this.config.getMinIdleConnectionNum());
//            result.setMaxWait(this.config.getMaxWaitTime());
//            result.setMaxActive(this.config.getMaxActiveConnectionNum());
//            result.setTestWhileIdle(true); //mysql 超时会断开连接
//            result.setValidationQuery("SELECT 1 FROM DUAL;");
//            dataSourcesMap.put(dbSrc.getUrl(), result);
//        }
//        log.info("init MsgStorage success");
//    }
//
//    // 业务方可以修改表名，一个库可以由多个message
//    public void initSql(){
//        insertSQL = String.format(insertSQL,tableName);
//        log.info("insertSQL {}",insertSQL);
//        selectByIdSQL = String.format(selectByIdSQL, tableName);
//        log.info("selectByIdSQL {}",selectByIdSQL);
//        selectMinIdOfWaitingSQL = String.format(selectMinIdOfWaitingSQL, tableName);
//        updateStatusSQL = String.format(updateStatusSQL, tableName);
//        log.info("updateStatusSQL {}",updateStatusSQL);
//        selectWaitingMsgSQL = String.format(selectWaitingMsgSQL, tableName);
//        selectWaitingMsgWithTopicsSQL = String.format(selectWaitingMsgWithTopicsSQL, tableName);
//        deleteMsgSQL = String.format(deleteMsgSQL, tableName);
//
//    }
//
//    public void close(){
//        log.info("start close MsgStorage");
//        Iterator<Entry<String, DataSource>> it = dataSourcesMap.entrySet().iterator();
//        while(it.hasNext()){
//            Entry<String,DataSource> entry = it.next();
//            BasicDataSource dataSrc = (BasicDataSource) entry.getValue();
//            try {
//                dataSrc.close();
//            } catch (SQLException e) {
//                // TODO Auto-generated catch block
//                log.error("dataSrc={} close fail ",dataSrc.getUrl(),e);
//            }
//        }
//    }
//
//    /**
//     * 查询task数据
//     */
////    public static Map.Entry<Long, String> insertTask(TaskInfo taskInfo) throws Exception{
////
////        DBDataSource dbDataSource = getDBDataSource(true);
////        DataSource dataSource = dataSourcesMap.get(dbDataSource.getUrl());
////        Connection con = dataSource.getConnection();
////        try {
////            PreparedStatement psmt = con.prepareStatement(insertSQL,Statement.RETURN_GENERATED_KEYS);
////            DatabaseMetaData metaData = con.getMetaData();
////            String url = metaData.getURL();
////            psmt.setLong(1, taskInfo.getBusinessId());
////            psmt.setInt(2, taskInfo.getBusinessType());
////            psmt.setString(3, taskInfo.getHandler());
////            psmt.setString(4, taskInfo.getContent());
////            psmt.setLong(5,taskInfo.getExpiredTime());
////            psmt.setLong(6,taskInfo.getNextTime());
////            psmt.setInt(7,taskInfo.getRetryTimes());
////            psmt.setInt(8,0);
////            psmt.setInt(9,WAITING);
////            psmt.setString(10, IpUtil.getIp());
////            psmt.setInt(11,taskInfo.getMinuteInterval());
////            psmt.executeUpdate();
////            ResultSet results = psmt.getGeneratedKeys();
////            Long id = null;
////            if(results.next()){
////                id = results.getLong(1);
////            }
////            Map.Entry<Long, String> idUrlPair = new AbstractMap.SimpleEntry<Long,String>(id,url);
////            return idUrlPair;
////        }finally {
////            con.close();
////        }
////
////    }
//
//    /**
//     *
//     * @param con 插入消息用的是业务方的连接，由业务方管理
//     * @param content
//     * @param topic
//     * @param tag
//     * @return
//     * @throws java.sql.SQLException
//     */
//    public static Integer insertMsg(Connection con,String content,String topic,String tag) throws SQLException{
//        PreparedStatement psmt = null;
//        ResultSet results = null;
//        try {
//            psmt = con.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
//            DatabaseMetaData metaData = con.getMetaData();
//            String url = metaData.getURL();
//            psmt.setString(1, content);
//            psmt.setString(2, topic);
//            psmt.setString(3, tag);
//            psmt.setInt(4, waitingStatus);
//            psmt.executeUpdate();
//            results = psmt.getGeneratedKeys();
//            Integer id = null;
//            if (results.next()) {
//                id = results.getInt(1);
//            }
//            return id;
////            Map.Entry<Long, String> idUrlPair = new AbstractMap.SimpleEntry<Long, String>(id, url);
////            return idUrlPair;
//        } catch (SQLException ex) {
//            throw ex;
//        } finally {
//            closeResultSet(results);
//            closePreparedStatement(psmt);
//        }
//    }
//
//    public  TaskInfo getTaskInfoById(Task task) throws SQLException{
//        if(task == null)
//            return null;
//        Integer id = task.getId();
//        String url = task.getHost();
//        DataSource dataSrc= dataSourcesMap.get(url);
//        Connection con = null;
//        PreparedStatement psmt = null;
//        ResultSet rs = null;
//        try {
//            con = dataSrc.getConnection();
//            psmt = con.prepareStatement(selectByIdSQL);
//            psmt.setLong(1, id);
//            rs = psmt.executeQuery();
//            TaskInfo taskInfo = new TaskInfo();
//            while (rs.next()) {
//                taskInfo.setId(rs.getInt("id"));
//                taskInfo.setContent(rs.getString("content"));
//                taskInfo.setCron(rs.getString("cron"));
//                taskInfo.setStatus(rs.getInt("status"));
//                taskInfo.setHandler(rs.getString("handler"));
//            }
//            return taskInfo;
//        } catch (SQLException ex) {
//            throw ex;
//        } finally {
//            closeResultSet(rs);
//            closePreparedStatement(psmt);
//            if (con != null)
//                con.close();
//        }
//    }
////
////    public int updateMsgStatus(Task task) throws SQLException{
////        String url = msg.getUrl();
////        DataSource dataSrc= dataSourcesMap.get(url);
////        Connection con = null;
////        PreparedStatement psmt = null;
////        try {
////            con = dataSrc.getConnection();
////            psmt = con.prepareStatement(updateStatusSQL);
////            psmt.setInt(1, sendedStatus);
////            psmt.setLong(2, task.getId());
////            return psmt.executeUpdate();
////        } catch (SQLException ex) {
////            throw ex;
////        } finally {
////            closePreparedStatement(psmt);
////            if (con != null)
////                con.close();
////        }
////    }
////
//    public int updateMsgStatus(DataSource dataSrc,Long id) throws SQLException{
//        Connection con = null;
//        PreparedStatement psmt = null;
//        try {
//            con = dataSrc.getConnection();
//            psmt = con.prepareStatement(updateStatusSQL);
//            psmt.setInt(1, sendedStatus);
//            psmt.setLong(2, id);
//            return psmt.executeUpdate();
//        } catch (SQLException ex) {
//            throw ex;
//        } finally {
//            closePreparedStatement(psmt);
//            if (con != null)
//                con.close();
//        }
//    }
//
//    public Long getMinIdOfWaitingMsg(DataSource dataSrc) throws SQLException{
//        Connection con = null;
//        PreparedStatement psmt = null;
//        try {
//            con = dataSrc.getConnection();
//            psmt = con.prepareStatement(selectMinIdOfWaitingSQL);
//            psmt.setInt(1, waitingStatus);
//            Long minId = null;
//            ResultSet rs = psmt.executeQuery();
//            if (rs.next()) {
//                minId = rs.getLong(1);
//            }
//            return minId;
//        } catch (SQLException ex) {
//            throw ex;
//        } finally {
//            closePreparedStatement(psmt);
//            if (con != null)
//                con.close();
//        }
//    }
//
//    /**
//     *
//     * @param pageSize
//     * @return
//     * @throws java.sql.SQLException
//     */
////    public List<TaskInfo> getWaitingMsg(DataSource dataSrc,int pageSize) throws SQLException{
////        Connection con = null;
////        PreparedStatement psmt = null;
////        ResultSet rs = null;
////        try {
////            con = dataSrc.getConnection();
////            String sql = selectWaitingMsgSQL;
////            boolean flag = false;
////
////            if(topicLists != null && !topicLists.isEmpty()){
////                //( ? ) order by id limit ?
////                StringBuilder sb = new StringBuilder(selectWaitingMsgWithTopicsSQL);
////                sb.append(" ( ");
////                for (int i = 0; i < topicLists.size(); i++) {
////                    if(i< topicLists.size() -1)
////                        sb.append(" ? ,");
////                    else
////                        sb.append(" ? ");
////                }
////                sb.append(" ) ");
////                sb.append(" order by id limit ? ;");
////                flag = true;
////                sql = sb.toString();
////            }
////
////            psmt = con.prepareStatement(sql);
////            psmt.setInt(1, waitingStatus);
////            psmt.setTimestamp(2, getSomeMinuteBeforeTimeStamp());
////            if(flag){
////                int j = 3;
////                for(int i=0;i<topicLists.size();i++,j++){
////                    String topic = topicLists.get(i);
////                    psmt.setString(j, topic);
////
////                }
////                psmt.setInt(j, pageSize);
////            }else{
////                psmt.setInt(3, pageSize);
////            }
////            rs = psmt.executeQuery();
////            List<MsgInfo> list = new ArrayList<MsgInfo>(pageSize);
////            while (rs.next()) {
////                MsgInfo msgInfo = new MsgInfo();
////                msgInfo.setId(rs.getLong(1));
////                msgInfo.setContent(rs.getString(2));
////                msgInfo.setTopic(rs.getString(3));
////                msgInfo.setTag(rs.getString(4));
////                msgInfo.setStatus(rs.getInt(5));
////                msgInfo.setCreate_time(rs.getTimestamp(6));
////                list.add(msgInfo);
////            }
////            return list;
////        } catch (SQLException ex) {
////            throw ex;
////        } finally {
////            closeResultSet(rs);
////            closePreparedStatement(psmt);
////            if (con != null)
////                con.close();
////        }
////    }
////
//    /**
//     *
//     * @param dataSrc
//     * @param limitNum sql 语句中已经指明只删除三天之前的发送成功的消息
//     * @return
//     * @throws java.sql.SQLException
//     */
//    public int deleteSendedMsg(DataSource dataSrc,int limitNum) throws SQLException{
//        Connection con = null;
//        PreparedStatement psmt = null;
//        try{
//            con = dataSrc.getConnection();
//            psmt =   con.prepareStatement(deleteMsgSQL);
//            psmt.setInt(1,sendedStatus);
//            psmt.setTimestamp(2, getSomeDayBeforeTimeStamp());
//            psmt.setInt(3, limitNum);
//            return psmt.executeUpdate();
//        }catch(SQLException ex){
//            throw ex;
//        }finally {
//            closePreparedStatement(psmt);
//            if(con != null)
//                con.close();
//        }
//
//    }
//
//    private Timestamp getSomeDayBeforeTimeStamp(){
//        long time = System.currentTimeMillis() - dayTimeDiff;
//        Timestamp timestamp = new Timestamp(time);
//        return timestamp;
//    }
//
//    private Timestamp getSomeMinuteBeforeTimeStamp(){
//        long time = System.currentTimeMillis() - minuteTimeDiff ;
//        Timestamp timestamp = new Timestamp(time);
//        return timestamp;
//    }
//
//
//
//    protected HashMap<String, DataSource> getDataSourcesMap() {
//        return dataSourcesMap;
//    }
//
//    protected void setDataSourcesMap(HashMap<String, DataSource> dataSourcesMap) {
//        this.dataSourcesMap = dataSourcesMap;
//    }
//
//    public static String getTablename() {
//        return tableName;
//    }
//
//
//
//    public List<DBDataSource> getDbDataSources() {
//        return dbDataSources;
//    }
//
//    public void setDbDataSources(List<DBDataSource> dbDataSources) {
//        this.dbDataSources = dbDataSources;
//    }
//
//    public static void closeResultSet(ResultSet rs){
//        if(rs != null){
//            try {
//                rs.close();
//            } catch (SQLException e) {
//                //
//                log.error("close Connection ResultSet error {} ",rs,e);
//            }
//        }
//    }
//
//    public static void closePreparedStatement(PreparedStatement psmt){
//        if(psmt != null){
//            try {
//                psmt.close();
//            } catch (SQLException e) {
//                //  Auto-generated catch block
//                log.error("close Connection PreparedStatement {} error ",psmt,e);
//            }
//        }
//    }
//
//
//
//
//
//
//}
