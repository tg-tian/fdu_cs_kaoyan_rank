package fdu.kaoyanrank.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class User {

    private Long id;

    private String examNoHash;

    private String idCardHash;

    private String role;

    private Integer status;

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
