package com.jzp.task.revolver;

import com.jzp.task.revolver.constants.ScheduleType;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.log.ILogger;
import com.jzp.task.revolver.model.TaskInfo;
import com.jzp.task.revolver.utils.CronUtil;
import com.jzp.task.revolver.utils.IPUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.sql.DataSource;

public class TaskStorage implements ILogger {

  private static final String tableName = "task_info";

  private static String selectByIdSQL = "select * from " + tableName + " where id=? ";
  private static final String insertSQL
      = "INSERT INTO " + tableName
      + " (schedule_type,content,cron,handler,execute_times,max_execute_times,next_time,host,status,create_time) "
      + "VALUES (?,?,?,?,?,?,?,?,?,now());";

  private static final String selectMinIdOfWaitingSQL = "select min(id) from " + tableName + " where status=? ";

  private static final String updateStatusSQL = "update " + tableName
      + " set status=? ,execute_times = ?,next_time = ?, host = ? , update_time = now() where id=?";

  private static final String updateHostSql =
      "update " + tableName + " set host=?, update_time = now() where id = ? and host = ?";

  private static final String selectWaitingSQL =
      "select id,business_id,business_type,handler,content,expired_time,retry_times,executed_times from " + tableName +
          " where status=? AND ip = ? AND next_time <= ? limit ?";

  private static final String selectWaitingTaskHostSql =
      "select * from " + tableName + " where status != 2 AND execute_times<max_execute_times";

  private static final String selectWaitingBeforeNextTime = "select * from " + tableName
      + " where next_time < ? and host = ? and status != 2 AND execute_times<max_execute_times";

  private static final String selectWaitingIdByHost =
      "select id from " + tableName + " where host = ? and status != 2 AND execute_times<max_execute_times";

  public static final int WAITING = 0;

  public static final int FAIL = 1;

  public static final int DONE = 2;

  public static final int CLOSED = 3;

  private static final String driverClass = "com.mysql.jdbc.Driver";
  private static final Logger log = LoggerFactory.getLogger(TaskStorage.class);
  private static List<DBDataSource> dbDataSources;
  private static HashMap<String, DataSource> dataSourcesMap;

  public TaskStorage(List<DBDataSource> dbDataSources) {
    TaskStorage.dbDataSources = dbDataSources;
    TaskStorage.dataSourcesMap = new HashMap<String, DataSource>();
  }

  /**
   * 初始化数据库连接池
   */
  public void init() {
    log.info("start init TaskStorage db Size {}", dbDataSources.size());
    for (DBDataSource dbSrc : dbDataSources) {
      BasicDataSource result = new BasicDataSource();
      result.setDriverClassName(driverClass);
      result.setUrl(dbSrc.getUrl());
      result.setUsername(dbSrc.getUsername());
      result.setPassword(dbSrc.getPassword());
      result.setInitialSize(Context.getConfig().getMinIdleConnectionNum());
      result.setMinIdle(Context.getConfig().getMinIdleConnectionNum());
      result.setMaxWait(Context.getConfig().getMaxWaitTime());
      result.setMaxActive(Context.getConfig().getMaxActiveConnectionNum());
      result.setTestWhileIdle(true); //mysql 超时会断开连接
      result.setValidationQuery("SELECT 1 FROM DUAL;");
      dataSourcesMap.put(dbSrc.getUrl(), result);
    }
    log.info("init TaskStorage success");
  }

  public void close() {
    log.info("start close TaskStorage");
    Iterator<Entry<String, DataSource>> it = dataSourcesMap.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, DataSource> entry = it.next();
      BasicDataSource dataSrc = (BasicDataSource) entry.getValue();
      try {
        dataSrc.close();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        log.error("dataSrc={} close fail ", dataSrc.getUrl(), e);
      }
    }
  }

  /**
   * @param master 是否主库，如果没有符合条件的，返回第一个，如果没有连接，返回null
   * @return
   */
  public DBDataSource getDBDataSource(boolean master) {

    if (dbDataSources == null) {
      return null;
    }
    for (DBDataSource dataSource : dbDataSources) {
      if (dataSource.isMaster() == master) {
        return dataSource;
      }
    }
    return dbDataSources.get(0);
  }

  public DataSource getDataSource(boolean master) {
    return dataSourcesMap.get(getDBDataSource(master).getUrl());
  }

  public TaskInfo register(TaskInfo taskInfo) throws Exception {

    if (ScheduleType.FIXED_TIME.getCode().equals(taskInfo.getScheduleType())) {
      taskInfo.setMaxExecuteTimes(1);
    }

    taskInfo.setNextTime(CronUtil.nextExecuteTime(taskInfo));

    DataSource dataSource = getDataSource(true);
    Connection con = dataSource.getConnection();
    try {
      PreparedStatement psmt = con.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
      DatabaseMetaData metaData = con.getMetaData();
      psmt.setInt(1, taskInfo.getScheduleType());
      psmt.setString(2, taskInfo.getContent());
      psmt.setString(3, taskInfo.getCron());
      psmt.setString(4, taskInfo.getHandler());
      psmt.setLong(5, taskInfo.getExecuteTimes());
      psmt.setInt(6, taskInfo.getMaxExecuteTimes());
      psmt.setLong(7, taskInfo.getNextTime());
      psmt.setString(8, taskInfo.getHost());
      psmt.setInt(9, taskInfo.getStatus());
//            psmt.set(10, taskInfo.getCreateTime());
//            psmt.setInt(11,taskInfo.getMinuteInterval());
      psmt.executeUpdate();
      ResultSet results = psmt.getGeneratedKeys();
      Integer id = null;
      if (results.next()) {
        id = results.getInt(1);
      }
      taskInfo.setId(id);
      return taskInfo;
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("register=" + taskInfo.toString());
      logException(taskInfo.toString(), e);
      throw e;
    } finally {
      con.close();
    }

  }

  public TaskInfo getTaskById(Integer id) {
    if (id == null)
      return null;
    DataSource dataSrc = getDataSource(false);
    try (Connection con = dataSrc.getConnection()) {
      PreparedStatement psmt = con.prepareStatement(selectByIdSQL);
      psmt.setInt(1, id);
      ResultSet rs = psmt.executeQuery();
      TaskInfo taskInfo = null;
      while (rs.next()) {
        taskInfo = getTask(rs);
      }
      return taskInfo;
    } catch (Exception e) {
      logException("taskId=" + id, e);
      return null;
    }
  }

  public int updateTask(TaskInfo taskInfo) {
    DataSource master = getDataSource(true);
    Connection con = null;
    try {
      con = master.getConnection();
    } catch (Exception e) {
      logException(taskInfo.toString(), e);
    }
    try {
      assert con != null;
      PreparedStatement psmt = con.prepareStatement(updateStatusSQL);
      psmt.setInt(1, taskInfo.getStatus());
      psmt.setInt(2, taskInfo.getExecuteTimes());
      psmt.setLong(3, taskInfo.getNextTime());
      psmt.setString(4, taskInfo.getHost());
      psmt.setInt(5, taskInfo.getId());
      return psmt.executeUpdate();
    } catch (SQLException ex) {
      logException(taskInfo.toString(), ex);
      return 0;
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        logException(taskInfo.toString(), e);
      }
    }
  }

  public int updateHost(Integer id, String oldHost, String newHost) {
    DataSource master = getDataSource(true);
    Connection con = null;
    try {
      con = master.getConnection();
    } catch (Exception e) {
      logException("taskId=" + id, e);
    }
    try {
      assert con != null;
      PreparedStatement psmt = con.prepareStatement(updateHostSql);
      psmt.setString(1, newHost);
      psmt.setInt(2, id);
      psmt.setString(3, oldHost);
      return psmt.executeUpdate();
    } catch (SQLException ex) {
      logException("taskId=" + id, ex);
      return 0;
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        logException("taskId=" + id, e);
      }
    }
  }
//
//    public Long getMinIdOfWaitingTask(DataSource dataSrc) throws SQLException{
//        try (Connection con = dataSrc.getConnection()) {
//            PreparedStatement psmt = con.prepareStatement(selectMinIdOfWaitingSQL);
//            psmt.setInt(1, WAITING);
//            Long minId = null;
//            ResultSet rs = psmt.executeQuery();
//            if (rs.next()) {
//                minId = rs.getLong(1);
//            }
//            return minId;
//        } catch (SQLException ex) {
//            logException(ex);
//            throw ex;
//        }
//    }

  /**
   * //     * @param pageSize
   *
   * @return
   * @throws java.sql.SQLException
   */
//    public List<TaskInfo> getTasks(int pageSize, int status) throws Exception{
//        DataSource master = getDataSource(false);
//        Connection con = null;
//        try {
//            con = master.getConnection();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        try {
//            assert con != null;
//            PreparedStatement psmt = con.prepareStatement(selectWaitingSQL);
//            psmt.setInt(1, status);
//            psmt.setString(2,IPUtils.getHostName());
//            psmt.setLong(3,System.currentTimeMillis());
//            psmt.setInt(4, pageSize);
//            ResultSet rs = psmt.executeQuery();
//            List<TaskInfo> list = new ArrayList<TaskInfo>(pageSize);
//            while (rs.next()) {
//                TaskInfo task = getTask(rs);
//                if (task!=null){
//                    list.add(task);
//                }
//            }
//            log.info("listSize:{},getTasks:{}",list.size(),psmt.toString());
//            return list;
//        } catch (Exception ex) {
//            logException(ex);
//            throw ex;
//        } finally {
//            if (con != null)
//                con.close();
//        }
//    }
  public HashMap<String, DataSource> getDataSourcesMap() {
    return dataSourcesMap;
  }

  public void setDataSourcesMap(HashMap<String, DataSource> dataSourcesMap) {
    TaskStorage.dataSourcesMap = dataSourcesMap;
  }

  public static String getTablename() {
    return tableName;
  }

  private static TaskInfo getTask(ResultSet rs) {
    try {
      TaskInfo taskInfo = new TaskInfo();
      taskInfo.setId(rs.getInt("id"));
      taskInfo.setContent(rs.getString("content"));
      taskInfo.setCron(rs.getString("cron"));
      taskInfo.setStatus(rs.getInt("status"));
      taskInfo.setHandler(rs.getString("handler"));
      taskInfo.setCreateTime(rs.getDate("create_time"));
      taskInfo.setExecuteTimes(rs.getInt("execute_times"));
      taskInfo.setHost(rs.getString("host"));
      taskInfo.setMaxExecuteTimes(rs.getInt("max_execute_times"));
      taskInfo.setNextTime(rs.getLong("next_time"));
      taskInfo.setScheduleType(rs.getInt("schedule_type"));
      taskInfo.setUpdateTime(rs.getDate("update_time"));
      return taskInfo;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<TaskInfo> getWaitingTask() throws Exception {
    DataSource master = getDataSource(false);
    Connection con = null;
    try {
      con = master.getConnection();
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      assert con != null;
      PreparedStatement psmt = con.prepareStatement(selectWaitingTaskHostSql);
      ResultSet rs = psmt.executeQuery();
      List<TaskInfo> list = new ArrayList<>();
      while (rs.next()) {
        TaskInfo taskInfo = getTask(rs);
        list.add(taskInfo);
      }
      return list;
    } catch (Exception ex) {
      logException("", ex);
      throw ex;
    } finally {
      if (con != null)
        con.close();
    }
  }



  public List<Integer> selectWaitingIdByHost(String host) throws Exception {
    DataSource master = getDataSource(false);
    Connection con = null;
    try {
      con = master.getConnection();
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      assert con != null;
      PreparedStatement psmt = con.prepareStatement(selectWaitingIdByHost);
      psmt.setString(1, host);
      ResultSet rs = psmt.executeQuery();
      List<Integer> list = new ArrayList<>();
      while (rs.next()) {
        list.add(rs.getInt("id"));
      }
      return list;
    } catch (Exception ex) {
      logException("host=" + host, ex);
      throw ex;
    } finally {
      if (con != null)
        con.close();
    }
  }

  public List<TaskInfo> selectWaitingBeforeNextTime(long beforeNextTime) throws Exception {
    DataSource master = getDataSource(false);
    Connection con = null;
    try {
      con = master.getConnection();
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      assert con != null;
      PreparedStatement psmt = con.prepareStatement(selectWaitingBeforeNextTime);
      psmt.setLong(1, beforeNextTime);
      psmt.setString(2, IPUtils.getHostAddress());
      ResultSet rs = psmt.executeQuery();
      List<TaskInfo> list = new ArrayList<>();
      while (rs.next()) {
        list.add(getTask(rs));
      }
      return list;
    } catch (Exception ex) {
      logException("beforeNextTime=" + beforeNextTime, ex);
      throw ex;
    } finally {
      if (con != null)
        con.close();
    }
  }



}
