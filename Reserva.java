package com.mycompany.projetohb;

import java.sql.*;

public class Reserva {
    private int id;
    private int equipamentoId;
    private Date data;
    private String aula;
    private int professorId;

    // Construtor
    public Reserva(int equipamentoId, Date data, String aula, int professorId) {
        this.equipamentoId = equipamentoId;
        this.data = data;
        this.aula = aula;
        this.professorId = professorId;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEquipamentoId() {
        return equipamentoId;
    }

    public void setEquipamentoId(int equipamentoId) {
        this.equipamentoId = equipamentoId;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public int getProfessorId() {
        return professorId;
    }

    public void setProfessorId(int professorId) {
        this.professorId = professorId;
    }

    // Método para verificar se já existe reserva para o mesmo equipamento, aula e data
    private boolean verificarConflito(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM reservas WHERE equipamento_id = ? AND data = ? AND aula = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, equipamentoId);
            checkStmt.setDate(2, data);
            checkStmt.setString(3, aula);
            ResultSet rs = checkStmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Método para reservar equipamento
    public boolean reservarEquipamento(Connection conn) {
        // Verifica se já existe conflito
        try {
            if (verificarConflito(conn)) {
                // Se houver conflito de reserva, retorna false
                System.out.println("Já existe uma reserva para o equipamento, aula e data especificados.");
                return false;
            }

            // Caso não haja conflito, prossegue com a reserva
            String sql = "INSERT INTO reservas (equipamento_id, data, aula, professor_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, equipamentoId);
                stmt.setDate(2, data);
                stmt.setString(3, aula);
                stmt.setInt(4, professorId);

                // Executa a inserção da reserva
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método para cancelar uma reserva
    public boolean cancelarReserva(Connection conn) {
        String sql = "DELETE FROM reservas WHERE equipamento_id = ? AND data = ? AND aula = ? AND professor_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipamentoId);
            stmt.setDate(2, data);
            stmt.setString(3, aula);
            stmt.setInt(4, professorId);

            // Executa a remoção da reserva
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}