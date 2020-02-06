package DBUtils;


import com.educarea.ServerApp.AppContext;

import java.sql.*;

public class DBWorker {
    private static String url = null;
    private static String user = null;
    private static String password = null;
    protected static final Object lockObj = new Object();

    private static final String settings = "?verifyServerCertificate=false"+
            "&useSSL=false"+
            "&autoReconnect=true"+
            "&requireSSL=false"+
            "&useLegacyDatetimeCode=false"+
            "&amp"+
            "&serverTimezone=UTC"+
            "&allowPublicKeyRetrieval=true";

    protected static boolean alreadyInit = false;
    protected static boolean alreadyConnect = false;

    protected static Connection connection = null;


    public static void init(String url, String user, String password){
        if (alreadyInit) return;
        DBWorker.url = url+settings;
        DBWorker.user = user;
        DBWorker.password = password;
        alreadyInit = true;
    }

    public static void connect() throws SQLException {
        if (alreadyConnect) return;
        try {
            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);
            alreadyConnect = true;
        } catch (SQLException e) {
            AppContext.log.severe("can't connect to DB",e);
            throw e;
        }
    }

    public static void disconnectAndReset(){
        url = null;
        user = null;
        password = null;
        alreadyInit = false;
        if (!alreadyConnect) return;
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        alreadyConnect = false;
    }


    public static class Builder implements AutoCloseable{

        private boolean returnResult = false;
        private PreparedStatement pstmt = null;
        private ResultSet resultSet = null;
        private String sql = "";
        private String []parameters;
        private String []types;

        public Builder(boolean returnResult){
            this.returnResult = returnResult;
        };

        public void build() throws SQLException {
            if (!checkData()){
                throw new SQLException("sql empty or parameters length not eqluals types length");
            }else {
                synchronized (lockObj) {
                    preparePstmt();
                }
            }
        }

        public ResultSet getResultSet() {
            return resultSet;
        }

        public Builder setParameters(String... parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder setTypes(String... types) {
            this.types = types;
            return this;
        }

        public Builder setSql(String sql){
            this.sql = sql;
            return this;
        }


        private void preparePstmt() throws SQLException {
            pstmt = connection.prepareStatement(sql);
            for (int i = 0; i < parameters.length; i++) {
                switch (types[i]){
                    case "int": {
                        if (parameters[i]==null){
                            pstmt.setNull(i+1,Types.INTEGER);
                        }else {
                            pstmt.setInt(i + 1, Integer.parseInt(parameters[i]));
                        }
                    }
                        break;
                    case "String": {
                        if (parameters[i]==null) {
                            pstmt.setNull(i+1,Types.VARCHAR);
                        }else {
                            pstmt.setString(i + 1, parameters[i]);
                        }
                    }
                        break;
                    default:
                        throw new SQLException("Undefined type");
                }
            }
            if (returnResult){
                resultSet = pstmt.executeQuery();
            }else {
                pstmt.executeUpdate();
            }
        }

        private boolean checkData(){
            if (sql.equals("")) return false;
            if (parameters != null && types != null){
                if (parameters.length == types.length){
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }

        @Override
        public void close() throws Exception {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (resultSet != null){
                try{
                    resultSet.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}
