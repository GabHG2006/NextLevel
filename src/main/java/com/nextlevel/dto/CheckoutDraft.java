package com.nextlevel.dto;

public class CheckoutDraft {

    private String correo;
    private String nombre;
    private String apellido;
    private String documento;
    private String celular;
    private String direccion;
    private String ciudad = "Cartagena de Indias";
    private String departamento = "Bolívar";
    private String tipoEntrega = "DOMICILIO";
    private boolean aceptaTerminos;
    private boolean aceptaPrivacidad;

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getCiudad() { return ciudad == null || ciudad.isBlank() ? "Cartagena de Indias" : ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getDepartamento() { return departamento == null || departamento.isBlank() ? "Bolívar" : departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getTipoEntrega() { return tipoEntrega == null || tipoEntrega.isBlank() ? "DOMICILIO" : tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }
    public boolean isAceptaTerminos() { return aceptaTerminos; }
    public void setAceptaTerminos(boolean aceptaTerminos) { this.aceptaTerminos = aceptaTerminos; }
    public boolean isAceptaPrivacidad() { return aceptaPrivacidad; }
    public void setAceptaPrivacidad(boolean aceptaPrivacidad) { this.aceptaPrivacidad = aceptaPrivacidad; }
}
