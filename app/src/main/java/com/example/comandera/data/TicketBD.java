package com.example.comandera.data;

import com.example.comandera.utils.DetalleDocumento;
import com.example.comandera.utils.Ticket;

import android.content.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketBD {
    private SQLServerConnection sqlServerConnection;

    public TicketBD(Context context) {
        sqlServerConnection = new SQLServerConnection(context);
    }

    public Ticket getTicketForMesa(int mesaId, int dispositivoId, int seccionId) {
        Ticket ticket = null;
        Connection connection = sqlServerConnection.connect();
        if (connection != null) {
            String query = "SELECT id, estado_documento, fecha, numero, serie_id FROM Cabecera_Documentos_Venta WHERE mesa_id = ? AND estado_documento = 0 AND dispositivo_id = ? AND tipo = 5 AND seccion_id = ?";
            try {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, mesaId);
                statement.setInt(2, dispositivoId);
                statement.setInt(3, seccionId);

                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    ticket = new Ticket();
                    ticket.setId(resultSet.getInt("id"));
                    ticket.setEstadoDocumento(resultSet.getInt("estado_documento"));
                    ticket.setFecha(resultSet.getString("fecha"));
                    ticket.setNumero(resultSet.getDouble("numero"));
                    ticket.setSerieId(resultSet.getDouble("serie_id"));
                }
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return ticket;
    }

    public long addDetalleDocumentoVenta(long cabeceraId, int articuloId, int cantidad, String desc_articulo, String desc_larga, int zonaId) {
        long result = -1;
        Connection connection = sqlServerConnection.connect();

        if (connection != null) {
            try {
                // Obtener el tipo de IVA
                String ivaQuery = "SELECT tipo_iva FROM tipos_iva WHERE id = (SELECT tipo_iva_id FROM articulos WHERE id=?)";
                PreparedStatement ivaStatement = connection.prepareStatement(ivaQuery);
                ivaStatement.setInt(1, articuloId);
                ResultSet ivaResultSet = ivaStatement.executeQuery();
                double tipoIva = 0;
                if (ivaResultSet.next()) {
                    tipoIva = ivaResultSet.getDouble("tipo_iva");
                }
                ivaResultSet.close();
                ivaStatement.close();

                // Obtener el precio neto del artículo
                String precioQuery = "SELECT precio_venta FROM tarifa_venta WHERE articulo_id = ? AND tipo_tarifa_id = (SELECT id FROM tipos_tarifa_venta WHERE zona_id = ?)";
                PreparedStatement precioStatement = connection.prepareStatement(precioQuery);
                precioStatement.setInt(1, articuloId);
                precioStatement.setInt(2, zonaId);
                ResultSet precioResultSet = precioStatement.executeQuery();
                double precioNeto = 0;
                if (precioResultSet.next()) {
                    precioNeto = precioResultSet.getDouble("precio_venta");
                }
                precioResultSet.close();
                precioStatement.close();

                // Calcular el precio final incluyendo el IVA
                double precioFinal = precioNeto + (precioNeto * tipoIva);

                // Calcular el total de la línea
                double totalLinea = precioFinal * cantidad;

                // Verificar si el artículo ya existe en el ticket
                String checkQuery = "SELECT Cantidad FROM Detalle_Documentos_Venta WHERE Cabecera_Id = ? AND Articulo_Id = ?";
                PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                checkStatement.setLong(1, cabeceraId);
                checkStatement.setInt(2, articuloId);
                ResultSet resultSet = checkStatement.executeQuery();

                if (resultSet.next()) {
                    // Si el artículo ya existe, incrementar la cantidad y actualizar precio y total_linea
                    int nuevaCantidad = resultSet.getInt("Cantidad") + cantidad;
                    totalLinea = precioFinal * nuevaCantidad;

                    String updateQuery = "UPDATE Detalle_Documentos_Venta SET Cantidad = ?, precio = ?, total_linea = ? WHERE Cabecera_Id = ? AND Articulo_Id = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setInt(1, nuevaCantidad);
                    updateStatement.setDouble(2, precioFinal);
                    updateStatement.setDouble(3, totalLinea);
                    updateStatement.setLong(4, cabeceraId);
                    updateStatement.setInt(5, articuloId);

                    updateStatement.executeUpdate();
                    updateStatement.close();

                    result = cabeceraId; // Devolver el ID de la cabecera como resultado
                } else {
                    // Si el artículo no existe, agregar una nueva línea con el precio, IVA y total_linea
                    String insertQuery = "INSERT INTO Detalle_Documentos_Venta (Cabecera_Id, Articulo_Id, Cantidad, Descripcion_articulo, Descripcion_larga, precio, cuota_iva, total_linea) VALUES (?,?,?,?,?,?,?,?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                    insertStatement.setLong(1, cabeceraId);
                    insertStatement.setInt(2, articuloId);
                    insertStatement.setInt(3, cantidad);
                    insertStatement.setString(4, desc_articulo);
                    insertStatement.setString(5, desc_larga);
                    insertStatement.setDouble(6, precioFinal);
                    insertStatement.setDouble(7, precioNeto * tipoIva);
                    insertStatement.setDouble(8, totalLinea);

                    insertStatement.executeUpdate();

                    // Obtener la ID de la nueva fila insertada
                    ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        result = generatedKeys.getLong(1);
                    }

                    insertStatement.close();
                }

                resultSet.close();
                checkStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    public long createNewTicket(int idSerie, int idSeccion, int idDispositivo, int idMesa, int idUsuarioTpv, int comensales, int articuloId, int cantidad, String desc_articulo, String desc_larga) {
        long newRowId = -1;
        Connection connection = sqlServerConnection.connect();
        if (connection != null) {
            try {
                int numeroCorriente = incrementarNumero(connection);

                String insertQuery = "INSERT INTO Cabecera_Documentos_Venta (Tipo, Fecha, Fecha_Contable, Serie_Id, Seccion_Id, Dispositivo_Id, Mesa_Id, Estado_Documento, Numero, Usuario_Ticket_Id, num_comensales) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                insertStatement.setInt(1, 5);
                Timestamp currentTimestamp = new Timestamp(new Date().getTime());
                insertStatement.setTimestamp(2, currentTimestamp);
                insertStatement.setTimestamp(3, currentTimestamp);
                insertStatement.setDouble(4, idSerie);
                insertStatement.setDouble(5, idSeccion);
                insertStatement.setDouble(6, idDispositivo);
                insertStatement.setDouble(7, idMesa);
                insertStatement.setInt(8, 0);  // Estado_Documento
                insertStatement.setDouble(9, numeroCorriente);
                insertStatement.setDouble(10, idUsuarioTpv);
                insertStatement.setInt(11, comensales);


                insertStatement.executeUpdate();

                // Obtener el ID de la cabecera recién creada
                ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    newRowId = generatedKeys.getLong(1);
                }

                generatedKeys.close();
                insertStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return newRowId;
    }

    public int incrementarNumero(Connection connection) throws SQLException {
        String getMaxNumeroQuery = "SELECT MAX(Numero) AS MaxNumero FROM Cabecera_Documentos_Venta";
        PreparedStatement getMaxNumeroStatement = connection.prepareStatement(getMaxNumeroQuery);

        ResultSet resultSet = getMaxNumeroStatement.executeQuery();
        double numeroCorriente = 1;

        if (resultSet.next()) {
            double maxNumero = resultSet.getDouble("MaxNumero");
            numeroCorriente = maxNumero + 1;
        }

        resultSet.close();
        getMaxNumeroStatement.close();
        return (int) numeroCorriente;
    }

    public List<DetalleDocumento> getDescripcionesLargasByCabeceraId(long cabeceraId) {
        List<DetalleDocumento> detalles = new ArrayList<>();
        Connection connection = sqlServerConnection.connect();
        if (connection != null) {
            String query = "SELECT descripcion_larga, cantidad,  precio, total_linea  FROM Detalle_Documentos_Venta WHERE Cabecera_Id = ?";
            try {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setLong(1, cabeceraId);

                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String descripcionLarga = resultSet.getString("descripcion_larga");
                    int cantidad = resultSet.getInt("cantidad");
                    double totalLinea = resultSet.getDouble("total_linea");
                    double pvp = resultSet.getDouble("precio");

                    detalles.add(new DetalleDocumento(descripcionLarga, cantidad, pvp, totalLinea));
                }

                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return detalles;
    }
}
