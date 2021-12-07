/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usa.controlador;

import com.usa.modelo.Producto;
import com.usa.modelo.RepositorioProducto;
import com.usa.vista.VentanaModificar;
import com.usa.vista.VentanaPrincipal;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Objects;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;

/**
 * 
 * @author AleJandro Giraldo
 */
public class Controlador implements ActionListener{
    /**
     * Repositorio en el que se encuentra la base de datos
     */
    RepositorioProducto repositorio;
    /**
     * Ventana principal de la aplicación
     */
    VentanaPrincipal ventanaPrin;
    /**
     * Ventana modificar de la aplicación
     */
    VentanaModificar ventanaMod;
    /**
     * Modelo default de la tabla
     */
    DefaultTableModel defaultTableModel;
    
    /**
     * Constructor de la clase
     */
    public Controlador() {
        super();
    }

    /**
     * Constructor de la clase controlador
     * @param repositorio repositorio de la base de datos
     * @param ventanaPrin Ventana principal de la aplicación
     * @param ventanaMod Ventana secundaria de la aplicación usada para modificar los productos
     */
    public Controlador(RepositorioProducto repositorio, VentanaPrincipal ventanaPrin, VentanaModificar ventanaMod) {
        this.repositorio = repositorio;
        this.ventanaPrin = ventanaPrin;
        this.ventanaMod = ventanaMod;
        agregarEventos();
        ventanaPrin.setVisible(true);
        actualizarTabla();
    }
    
    /**
     * Método agregar eventos, agrega los eventos a los botones de la ventana principal y la ventana modificar
     */
    private void agregarEventos() {
        ventanaPrin.getBtVpAgregar().addActionListener(this);
        ventanaPrin.getBtVpActualizar().addActionListener(this);
        ventanaPrin.getBtVpEliminar().addActionListener(this);
        ventanaPrin.getBtVpInforme().addActionListener(this);
        ventanaMod.getBtMpActualizar().addActionListener(this);
        
        
    }
    
    /**
     * Método agregar producto, agrega un producto a la base de datos.
     * En caso de que el producto ya exista lanza un mensaje de advertencia
     * Borra los campos de texto de la ventana principal después de haber actualizado la tabla
     */
    public void agregarProducto() {
        try {
            if(formularioValido()) {
                Producto producto = new Producto(ventanaPrin.getTfVpNombre().getText()
                        , Float.parseFloat(ventanaPrin.getTfVpPrecio().getText())
                        , Integer.parseInt(ventanaPrin.getTfVpInventario().getText()));
                
                repositorio.save(producto);
                JOptionPane.showMessageDialog(null, "Producto guardado con éxito");
            }
        } catch (DbActionExecutionException e) {
            JOptionPane.showMessageDialog(null, "El producto ya existe", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            actualizarTabla();
            borrarCampos();
        }
    }
    
    /**
     * Método eliminar producto.
     * Elimina un producto de la base de datos usando la posición en la que se encuentra el producto.
     * En caso de que se quiera eliminar un producto que no existe, lanza un mensaje de error
     */
    public void eliminarProducto() {
        try {
            JTable tablaProd = ventanaPrin.getTbVpTabla();
            int row = tablaProd.getSelectedRow();
            if(row == -1){
                JOptionPane.showMessageDialog(null, "Debe seleccionar una fila", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int codBusqueda = (int) tablaProd.getValueAt(row, 0);
                if(repositorio.existsById(codBusqueda)){
                    repositorio.deleteById(codBusqueda);
                }
            }
        } catch (DbActionExecutionException e) {
            JOptionPane.showMessageDialog(null, "El producto no existe", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            actualizarTabla();
        }
    }
    
    /**
     * Método actualizar producto
     * Llama a la ventana modificar después de haber seleccionado un producto.
     * Si no se selecciona un producto, lanza un mensaje de error
     */
    public void abrirActualizar() {
        int row = ventanaPrin.getTbVpTabla().getSelectedRow();
        if(row == -1) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una fila", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            ventanaMod.setLlaveProducto(row);
            ventanaMod.setRepositorio(repositorio);
            ventanaMod.setVisible(true);
        }
        
        
    }
    
    /**
     * Método actualizar producto
     * Permite actualizar el producto seleccionado en la base de datos.
     * En caso de que el producto no exista se muestra un mensaje de error.
     */
    public void actualizarProducto() {
        try {
            Integer codBusqueda = ventanaMod.getLlaveProducto();
            String nomBusqueda = ventanaMod.getTfMpNombre().getText();
            Float preBusqueda = Float.parseFloat(ventanaMod.getTfMpPrecio().getText());
            Integer invBusqueda = Integer.parseInt(ventanaMod.getTfMpInventario().getText());
            
            
            if(verificarExistencia(new Producto(codBusqueda+1, nomBusqueda, 0f, 0))) {
                Producto producto = new Producto(codBusqueda+1, nomBusqueda, preBusqueda, invBusqueda);
                repositorio.save(producto);
                actualizarTabla();
                ventanaMod.setVisible(false);
                JOptionPane.showMessageDialog(null, "Producto actualizado con éxito");
            } else {
                JOptionPane.showMessageDialog(null, "Producto no encontrado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DbActionExecutionException e) {
            JOptionPane.showMessageDialog(null, "El producto no existe", "Error", JOptionPane.ERROR_MESSAGE);
        } 
    }
   
    /**
     * Método generar informe, incluye toda la lógica para encontrar
     * el producto con mayor precio
     * el producto con menor precio
     * el promedio de los productos que se encuentran en la base de datos
     * el valor total de los productos que se encuentran en la base de datos
     */
    public void generarInforme() {
        DecimalFormatSymbols separador = new DecimalFormatSymbols();
        separador.setDecimalSeparator('.');
        DecimalFormat redondeo = new DecimalFormat("#.0", separador);
        redondeo.setRoundingMode(RoundingMode.HALF_UP);
        
        List<Producto> listaProds = (List<Producto>) repositorio.findAll();
        String mayorProducto = "";
        Float mayorPrecio = 0f;
        for(Producto producto : listaProds){
            Float precioAct = producto.getPrecio();
            String prodAct = producto.getNombre();
            
            if(precioAct > mayorPrecio) {
                mayorPrecio = precioAct;
                mayorProducto = prodAct;
            }
        }
        
        String menorProducto = "";
        Float menorPrecio = 999999999999999f;
        for(Producto producto : listaProds) {
            Float precioAct = producto.getPrecio();
            String prodAct = producto.getNombre();
            if(precioAct < menorPrecio) {
                menorPrecio = precioAct;
                menorProducto = prodAct; 
            }
        }
        
        Float total = 0f;
        Float promedioProd;
        Float cont = 0f;
        for(Producto producto : listaProds){
            total += producto.getPrecio();
            cont++;
        }
        promedioProd = total / cont;
        
        Float totalInventario = 0f;
        for(Producto producto : listaProds) {
            totalInventario += producto.getPrecio() * producto.getUnidades();
        }
        
        String informe = "Producto precio mayor: " + mayorProducto + "\n"
                       + "Producto precio menor: " + menorProducto + "\n"
                       + "Promedio productos: " + promedioProd + "\n"
                       + "Total inventario: " + totalInventario + "\n";
        
        JOptionPane.showMessageDialog(null, informe, "Informe", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Método actualizar tabla. Actualiza la tabla que se encuentra en la
     * ventana principal y actualiza la base de datos en MySQL
     */
    public void actualizarTabla() {
        String[] titulos = new String[]{"id", "nombre", "precio", "inventario"};
        defaultTableModel = new DefaultTableModel(titulos, 0);
        List<Producto> listaProductos = (List<Producto>) repositorio.findAll();
        for(Producto producto : listaProductos) {
            defaultTableModel.addRow(new Object[] {producto.getCodigo()
                                                   , producto.getNombre()
                                                   , producto.getPrecio()
                                                   , producto.getUnidades()});
            
        }
        ventanaPrin.getTbVpTabla().setModel(defaultTableModel);
        ventanaPrin.getTbVpTabla().setPreferredSize(new Dimension(350, defaultTableModel.getRowCount()*16));
        ventanaPrin.getvPScroll().setViewportView(ventanaPrin.getTbVpTabla());
    }
    
    /**
     * Método es número, verifica si el valor dentro de un campo de texto es número o no
     * @param numero valor a evaluar
     * @return boolean especificando si un valor es número o no
     */
    public boolean esNumero(String numero) {
        try {
            Float.parseFloat(numero);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    /**
     * Evalúa si los campos del formulario son validos, es decir si contienen texto
     * y corresponden al tipo de dato esperado en cada campo de texto
     * @return boolean especificando si el formulario es valido
     */
    public boolean formularioValido() {
        if("".equals(ventanaPrin.getTfVpNombre().getText()) 
           || "".equals(ventanaPrin.getTfVpPrecio())
           || "".equals(ventanaPrin.getTfVpInventario())) {
            JOptionPane.showMessageDialog(null, "Los campos no pueden ser vacios", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if(!esNumero(ventanaPrin.getTfVpPrecio().getText()) || !esNumero(ventanaPrin.getTfVpInventario().getText())) {
            JOptionPane.showMessageDialog(null, "Los campos precio e inventario deben ser númericos", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Verifica la existencia de un producto dentro de la base de datos
     * @param nombre
     * @return boolean especificando la existencia de un producto en la base de datos
     */
    public boolean verificarExistencia(String nombre) {
        List<Producto> listaProd = (List<Producto>) repositorio.findAll();
        for(Producto producto : listaProd){
            if(Objects.equals(nombre, producto.getNombre())){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica la existencia de un producto dentro de la base de datos usando el código del produco
     * @param producto
     * @return boolean especificando la existencia de un producto en la base de datos
     */
    public boolean verificarExistencia(Producto producto) {
        return repositorio.existsById(producto.getCodigo());
    }
    
    /**
     * Método que borra los campos de texto
     */
    public void borrarCampos() {
        ventanaPrin.getTfVpCodigo().setText("");
        ventanaPrin.getTfVpNombre().setText("");
        ventanaPrin.getTfVpPrecio().setText("");
        ventanaPrin.getTfVpInventario().setText("");
    }
    

    /**
     * Método que agrega funcionalidad a los botones de las ventanas principal y modificar
     * @param e evento o clic
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == ventanaPrin.getBtVpAgregar()) {
            agregarProducto();
        }
        
        if(evt.getSource() == ventanaPrin.getBtVpActualizar()) {
            abrirActualizar();
        }
        
        if(evt.getSource() == ventanaPrin.getBtVpEliminar()) {
            eliminarProducto();
        }
        
        if(evt.getSource() == ventanaPrin.getBtVpInforme()) {
            generarInforme();
        }
        
        if(evt.getSource() == ventanaMod.getBtMpActualizar()) {
            actualizarProducto();
        }
        
        
    }
    
}

