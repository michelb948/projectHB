package com.mycompany.projetohb;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

public class ReservaServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Configurar o tipo de resposta
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Obtendo os parâmetros do formulário HTML
        String aula = request.getParameter("aula");
        String data = request.getParameter("data");
        String professor = request.getParameter("professor");
        String equipamento = request.getParameter("equipamento");

        // Conversão da data para o formato esperado no banco (YYYY-MM-DD)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dataReserva = null;
        try {
            dataReserva = dateFormat.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
            out.println("{\"status\": \"erro\", \"message\": \"Data inválida!\"}");
            return;
        }

        // Conectar ao banco de dados (assumindo que você já tem uma conexão configurada)
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/project_1", "root", "senha")) {
            // Criando um objeto Reserva
            int professorId = Integer.parseInt(professor);
            int equipamentoId = Integer.parseInt(equipamento);

            // Verificar se o equipamento está disponível
            Reserva reserva = new Reserva(equipamentoId, new java.sql.Date(dataReserva.getTime()), aula, professorId);
            boolean sucesso = reserva.reservarEquipamento(conn);

            if (sucesso) {
                out.println("{\"status\": \"sucesso\", \"message\": \"Reserva realizada com sucesso!\"}");
            } else {
                out.println("{\"status\": \"erro\", \"message\": \"Erro ao realizar a reserva!\"}");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("{\"status\": \"erro\", \"message\": \"Erro de conexão com o banco de dados!\"}");
        }
    }
}