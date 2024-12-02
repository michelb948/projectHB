package com.mycompany.projetohb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    // Método para verificar se o professor existe
    private boolean verificarProfessorExistente(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM professores WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, professorId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Método para verificar se o equipamento existe
    private boolean verificarEquipamentoExistente(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM equipamentos WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipamentoId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Método para verificar se já existe conflito de reserva (mesmo equipamento, aula e data, para outro professor)
    private boolean verificarConflito(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM reservas WHERE equipamento_id = ? AND data = ? AND aula = ? AND professor_id != ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, equipamentoId);
            checkStmt.setDate(2, data);
            checkStmt.setString(3, aula);
            checkStmt.setInt(4, professorId);  // Verifica se outro professor já reservou o equipamento
            ResultSet rs = checkStmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Método para reservar equipamento
    public boolean reservarEquipamento(Connection conn) {
        try {
            conn.setAutoCommit(false);  // Inicia a transação

            // Validações
            if (!verificarProfessorExistente(conn)) {
                conn.rollback();  // Desfaz a transação em caso de erro
                System.out.println("Professor não encontrado.");
                return false;
            }

            if (!verificarEquipamentoExistente(conn)) {
                conn.rollback();  // Desfaz a transação em caso de erro
                System.out.println("Equipamento não encontrado.");
                return false;
            }

            // Verifica se já existe um conflito para a reserva
            if (verificarConflito(conn)) {
                conn.rollback();  // Desfaz a transação em caso de conflito
                System.out.println("Já existe uma reserva para o equipamento, aula e data especificados.");
                return false;
            }

            // Caso não haja conflitos, prossegue com a reserva
            String sql = "INSERT INTO reservas (equipamento_id, data, aula, professor_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, equipamentoId);
                stmt.setDate(2, data);
                stmt.setString(3, aula);
                stmt.setInt(4, professorId);

                // Executa a inserção da reserva
                int rowsAffected = stmt.executeUpdate();
                conn.commit();  // Confirma a transação
                return rowsAffected > 0;
            } catch (SQLException e) {
                conn.rollback();  // Desfaz a transação em caso de erro
                e.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);  // Restaura o autocommit após a transação
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para cancelar uma reserva
    public boolean cancelarReserva(Connection conn) {
        try {
            // Validação: verifica se o professor e o equipamento existem
            if (!verificarProfessorExistente(conn)) {
                System.out.println("Professor não encontrado.");
                return false;
            }

            if (!verificarEquipamentoExistente(conn)) {
                System.out.println("Equipamento não encontrado.");
                return false;
            }

            // Executa a exclusão da reserva
            String sql = "DELETE FROM reservas WHERE equipamento_id = ? AND data = ? AND aula = ? AND professor_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, equipamentoId);
                stmt.setDate(2, data);
                stmt.setString(3, aula);
                stmt.setInt(4, professorId);

                // Executa a remoção da reserva
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método para listar todas as reservas para uma data específica
    public static List<Reserva> listarReservasPorData(Connection conn, Date data) throws SQLException {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT * FROM reservas WHERE data = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, data);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int equipamentoId = rs.getInt("equipamento_id");
                String aula = rs.getString("aula");
                int professorId = rs.getInt("professor_id");
                reservas.add(new Reserva(equipamentoId, data, aula, professorId));
            }
        }
        return reservas;
    }

    // Método para verificar se o equipamento está disponível (sem conflito de reserva)
    public static boolean verificarDisponibilidadeEquipamento(Connection conn, int equipamentoId, Date data, String aula) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservas WHERE equipamento_id = ? AND data = ? AND aula = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipamentoId);
            stmt.setDate(2, data);
            stmt.setString(3, aula);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) == 0;  // Retorna true se não houver reservas
        }
    }
}