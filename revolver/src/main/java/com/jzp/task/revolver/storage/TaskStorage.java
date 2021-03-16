package com.jzp.task.revolver.storage;

import com.jzp.task.revolver.constants.TaskStatus;
import com.jzp.task.revolver.context.Context;
import com.jzp.task.revolver.log.ILogger;
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

  private static final String selectByIdSQL = "select * from " + tableName + " where id=? ";
  private static final String insertSQL
      = "INSERT INTO " + tableName
      + " (schedule_type,content,cron,handler,execute_times,max_execute_times,next_time,host,status,name,create_time) "
      + "VALUES (?,?,?,?,?,?,?,?,?,?,now());";

  private static final String selectForUpdate = "select * from " + tableName + " where id=? for update";

  private static final String updateHostSql =
      "update " + tableName + " set host=?, update_time = now() where id = ? and host = ?";

  private static final String selectAllWaitingExceptHostSql =
      "select * from " + tableName + " where host != ? and status in (" + needGoOnStatus()
          + ") AND (execute_times<max_execute_times or max_execute_times = 0)";

  private static final String selectWaitingBeforeNextTimeByHost = "select * from " + tableName
      + " where next_time < ? and host = ? and status in (" + needGoOnStatus()
      + ") AND (execute_times<max_execute_times or max_execute_times = 0)";

  private static final String driverClass = "com.mysql.jdbc.Driver";
  private static final Logger LOGGER = LoggerFactory.getLogger(TaskStorage.class);
  private static List<DBDataSource> dbDataSources;
  private static HashMap<String, DataSource> dataSourcesMap;

  public TaskStorage(List<DBDataSource> dbDataSources) {
    TaskStorage.dbDataSources = dbDataSources;
    TaskStorage.dataSourcesMap = new HashMap<String, DataSource>();
  }

  private static String needGoOnStatus() {
    StringBuilder builder = new StringBuilder();
    List<Integer> status = TaskStatus.needGoOn();
    for (Integer integer : status) {
      builder.append(integer).append(",");
    }
    if (builder.length() > 0) {
      return builder.substring(0, builder.length() - 1);
    }
    return builder.toString();
  }


  /**
   * 初始化数据库连接池
   */
  public void init() {
    LOGGER.info("start init TaskStorage db Size {}", dbDataSources.size());
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
    LOGGER.info("init TaskStorage success");
  }

  public void close() {
    LOGGER.info("start close TaskStorage");
    Iterator<Entry<String, DataSource>> it = dataSourcesMap.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, DataSource> entry = it.next();
      BasicDataSource dataSrc = (BasicDataSource) entry.getValue();
      try {
        dataSrc.close();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        LOGGER.error("dataSrc={} close fail ", dataSrc.getUrl(), e);
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

    try (Connection con = getConnection(true)) {
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
      psmt.setString(10, taskInfo.getName());

      psmt.executeUpdate();
      ResultSet results = psmt.getGeneratedKeys();
      Integer id = null;
      if (results.next()) {
        id = results.getInt(1);
      }
      taskInfo.setId(id);
      return taskInfo;
    } catch (Exception e) {
      logException(taskInfo.toString(), e);
      throw e;
    }

  }

  public TaskInfo getTaskById(Integer id) {
    if (id == null)
      return null;
    try (Connection con = getConnection(false)) {
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

  public int updateNextTimeWithoutException(int id, long nextTime) {
    try {
      return updateNextTime(id, nextTime);
    } catch (Exception e) {
      logException("id=" + id, e);
    }
    return 0;
  }

  public int updateStatus(int id, int fromStatus, int toStatus) throws Exception {
    StringBuilder builder = new StringBuilder("update ").append(tableName).append(" set");
    builder.append(" status=").append(toStatus).append(" where id=").append(id);
    builder.append(" and status=").append(fromStatus);
    return executeUpdate(builder.toString());
  }

  public int restart(int id, int fromStatus, long nextTime) throws Exception {
    StringBuilder builder = new StringBuilder("update ").append(tableName).append(" set");
    builder.append(" status=").append(TaskStatus.NEW.getCode()).append(",");
    builder.append(" next_time=").append(nextTime);
    builder.append(" where id=").append(id);
    builder.append(" and status=").append(fromStatus);
    return executeUpdate(builder.toString());
  }


  public int updateNextTime(int id, long nextTime) throws Exception {
    StringBuilder builder = new StringBuilder("update ").append(tableName).append(" set");
    builder.append(" next_time=").append(nextTime);
    builder.append(" where id=").append(id);
    return executeUpdate(builder.toString());
  }

  public int updateExecute(TaskInfo taskInfo) throws Exception {
    StringBuilder builder = new StringBuilder("update ").append(tableName).append(" set");
    builder.append(" status=").append(taskInfo.getStatus()).append(",");
    builder.append(" execute_times = execute_times + 1").append(",");
    if (taskInfo.getNextTime() != null) {
      builder.append(" next_time=").append(taskInfo.getNextTime()).append(",");
    }
    builder.append(" update_time=").append("now()").
        append(" where id=").append(taskInfo.getId());
    return executeUpdate(builder.toString());
  }


  public TaskInfo selectForUpdate(Integer id) throws SQLException {
    Connection con = getConnection(true);
    try {
      PreparedStatement psmt = con.prepareStatement(selectForUpdate);
      psmt.setInt(1, id);
      ResultSet rs = psmt.executeQuery();
      TaskInfo taskInfo = null;
      while (rs.next()) {
        taskInfo = getTask(rs);
      }
      return taskInfo;
    } catch (SQLException ex) {
      logException("id=" + id, ex);
      return null;
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        logException("id=" + id, e);
      }
    }
  }

  public int updateHost(Integer id, String oldHost, String newHost) throws Exception {
    Connection con = getConnection(true);
    try {
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

  public HashMap<String, DataSource> getDataSourcesMap() {
    return dataSourcesMap;
  }

  public void setDataSourcesMap(HashMap<String, DataSource> dataSourcesMap) {
    TaskStorage.dataSourcesMap = dataSourcesMap;
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
      taskInfo.setName(rs.getString("name"));
      return taskInfo;
    } catch (Exception e) {
      LOGGER.error("getTask err. [rs={}]", rs, e);
    }
    return null;
  }

  public List<TaskInfo> getWaitingTaskExceptMy() throws Exception {
    try (Connection con = getConnection(false)) {
      PreparedStatement psmt = con.prepareStatement(selectAllWaitingExceptHostSql);
      psmt.setString(1, IPUtils.getHostAddress());
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
    }
  }

  public List<TaskInfo> selectMyWaitingBeforeNextTime(long beforeNextTime) throws Exception {
    try (Connection con = getConnection(false)) {
      PreparedStatement psmt = con.prepareStatement(selectWaitingBeforeNextTimeByHost);
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
    }
  }

  private Connection getConnection(boolean master) throws SQLException {
    DataSource dataSource = getDataSource(master);
    Connection con;
    try {
      con = dataSource.getConnection();
    } catch (Exception e) {
      LOGGER.error("getConnection err. [dataSource='{}']", dataSource, e);
      throw e;
    }
    return con;
  }


  public int executeUpdate(String updateSql) throws Exception {
    Connection con = getConnection(true);
    try {
      PreparedStatement psmt = con.prepareStatement(updateSql);
      return psmt.executeUpdate();
    } catch (SQLException ex) {
      logException(updateSql, ex);
      return 0;
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        logException(updateSql, e);
      }
    }
  }



}
