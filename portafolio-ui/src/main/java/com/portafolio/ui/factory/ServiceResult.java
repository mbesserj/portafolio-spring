package com.portafolio.ui.factory;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Clase que encapsula el resultado de operaciones de servicios,
 * proporcionando manejo de errores tipo-seguro y API fluida.
 */
public final class ServiceResult<T> {
    private final ResultType type;
    private final T data;
    private final String message;
    private final Exception exception;

    private ServiceResult(ResultType type, T data, String message, Exception exception) {
        this.type = type; 
        this.data = data; 
        this.message = message; 
        this.exception = exception;
    }

    // --- MÉTODOS FACTORY ---
    public static <T> ServiceResult<T> success(T data) { 
        return new ServiceResult<>(ResultType.SUCCESS, data, "OK", null); 
    }
    
    public static <T> ServiceResult<T> error(String msg, Exception ex) { 
        return new ServiceResult<>(ResultType.ERROR, null, msg, ex); 
    }
    
    // --- MÉTODOS DE CONSULTA ---
    public boolean isSuccess() { 
        return type == ResultType.SUCCESS; 
    }
    
    public boolean isError() { 
        return !isSuccess(); 
    }
    
    public T getData() { 
        if(isError()) {
            throw new IllegalStateException("No hay datos en un resultado fallido"); 
        }
        return data; 
    }
    
    public String getMessage() { 
        return message; 
    }
    
    /**
     * Obtiene el mensaje de error si el resultado es un error.
     * @return El mensaje de error o null si es exitoso
     */
    public String getErrorMessage() {
        return isError() ? message : null;
    }
    
    /**
     * Obtiene la excepción asociada si existe.
     * @return La excepción o null si no hay
     */
    public Exception getException() {
        return exception;
    }
    
    // --- MÉTODOS FUNCIONALES ---
    /**
     * Transforma el resultado exitoso usando una función que puede fallar.
     */
    public <U> ServiceResult<U> flatMap(Function<T, ServiceResult<U>> mapper) {
        if (isSuccess()) {
            try { 
                return mapper.apply(data); 
            } catch (Exception e) { 
                return error("Error en flatMap", e); 
            }
        }
        return new ServiceResult<>(this.type, null, this.message, this.exception);
    }
    
    /**
     * Transforma el resultado exitoso usando una función simple.
     */
    public <U> ServiceResult<U> map(Function<T, U> mapper) {
        if (isSuccess()) {
            try {
                return success(mapper.apply(data));
            } catch (Exception e) {
                return error("Error en map", e);
            }
        }
        return new ServiceResult<>(this.type, null, this.message, this.exception);
    }
    
    /**
     * Ejecuta una acción si el resultado es exitoso.
     */
    public ServiceResult<T> ifSuccess(Consumer<T> action) { 
        if (isSuccess()) {
            action.accept(data); 
        }
        return this; 
    }
    
    /**
     * Ejecuta una acción si el resultado es un error.
     */
    public ServiceResult<T> ifError(Consumer<String> action) { 
        if (isError()) {
            action.accept(message); 
        }
        return this; 
    }
    
    /**
     * Ejecuta una acción si el resultado es un error, pasando tanto mensaje como excepción.
     */
    public ServiceResult<T> ifError(Consumer<String> messageAction, Consumer<Exception> exceptionAction) {
        if (isError()) {
            messageAction.accept(message);
            if (exception != null && exceptionAction != null) {
                exceptionAction.accept(exception);
            }
        }
        return this;
    }
    
    /**
     * Proporciona un valor por defecto si el resultado es un error.
     */
    public T orElse(T defaultValue) {
        return isSuccess() ? data : defaultValue;
    }
    
    /**
     * Proporciona un valor por defecto usando un supplier si el resultado es un error.
     */
    public T orElseGet(java.util.function.Supplier<T> supplier) {
        return isSuccess() ? data : supplier.get();
    }
    
    /**
     * Lanza una excepción si el resultado es un error.
     */
    public T orElseThrow() throws Exception {
        if (isError()) {
            throw exception != null ? exception : new RuntimeException(message);
        }
        return data;
    }
    
    /**
     * Lanza una excepción personalizada si el resultado es un error.
     */
    public <X extends Throwable> T orElseThrow(java.util.function.Supplier<X> exceptionSupplier) throws X {
        if (isError()) {
            throw exceptionSupplier.get();
        }
        return data;
    }
    
    // --- MÉTODOS DE UTILIDAD ---
    @Override
    public String toString() {
        if (isSuccess()) {
            return String.format("ServiceResult.Success[data=%s]", data);
        } else {
            return String.format("ServiceResult.Error[message=%s, exception=%s]", 
                message, exception != null ? exception.getClass().getSimpleName() : "null");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ServiceResult<?> that = (ServiceResult<?>) obj;
        return type == that.type &&
               java.util.Objects.equals(data, that.data) &&
               java.util.Objects.equals(message, that.message);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(type, data, message);
    }
    
    // --- ENUMERACIÓN DE TIPOS ---
    public enum ResultType { 
        SUCCESS, 
        ERROR 
    }
}