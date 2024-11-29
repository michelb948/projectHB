package com.mycompany.projetohb;

import java.sql.*;

public class ConexaoDB {
    private static final String URL = "jdbc:mysql://localhost:3306/sistema_reservas";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    public static void fecharConexao(Connection conn, Statement stmt, ResultSet rs) throws SQLException {
        if (rs != null) rs.close();
        if (stmt != null) stmt.close();
        if (conn != null) conn.close();
    }
}
