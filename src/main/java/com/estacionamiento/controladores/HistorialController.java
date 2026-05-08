package com.estacionamiento.controladores;

import com.estacionamiento.dao.HistorialEventoDAO;
import com.estacionamiento.modelos.HistorialEvento;
import java.util.List;

public class HistorialController {

    private final HistorialEventoDAO dao = new HistorialEventoDAO();

    public boolean registrarEvento(HistorialEvento evento) throws Exception {
        return dao.guardarEvento(evento);
    }

    public List<HistorialEvento> obtenerEventosPorCliente(int clienteId) throws Exception {
        return dao.obtenerEventosPorCliente(clienteId);
    }

    public List<HistorialEvento> obtenerEventosPorRegistro(int registroId) throws Exception {
        return dao.obtenerEventosPorRegistro(registroId);
    }
}
