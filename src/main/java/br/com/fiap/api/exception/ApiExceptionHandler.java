package br.com.fiap.api.exception;

import br.com.fiap.api.dto.ErroResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOG = Logger.getLogger(ApiExceptionHandler.class.getName());
    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<ErroResponseDto> notFound(EntidadeNaoEncontradaException e) {
        return ResponseEntity.status(404).body(new ErroResponseDto("Não encontrado", e.getMessage()));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErroResponseDto> banco(SQLException e) {
        LOG.log(Level.SEVERE, "Falha no banco (codigo=" + e.getErrorCode() + ", SQLState=" + e.getSQLState() + ")", e);
        if (e.getErrorCode() == 1)
            return ResponseEntity.status(409).body(new ErroResponseDto("Conflito", "Registro com valor único já cadastrado"));
        if (e.getErrorCode() == 2292)
            return ResponseEntity.status(409).body(new ErroResponseDto("Registro em uso",
                    "Este item possui histórico e não pode ser excluído. Você pode editá-lo com ativo=0."));
        if (e.getErrorCode() == 2291)
            return ResponseEntity.status(409).body(new ErroResponseDto("Referência inexistente", "Um registro relacionado não existe mais"));
        return ResponseEntity.internalServerError().body(new ErroResponseDto("Erro no banco", "Não foi possível realizar a operação"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponseDto> regra(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(new ErroResponseDto("Operação não permitida", e.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException e) {
        Map<String, String> erros = new HashMap<>();
        for (FieldError field : e.getFieldErrors())
            erros.put(field.getField(), field.getDefaultMessage());
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResponseDto> formato(Exception e) {
        return ResponseEntity.badRequest().body(new ErroResponseDto("Dados inválidos", "Confira o JSON e os tipos dos campos e identificadores"));
    }
}

