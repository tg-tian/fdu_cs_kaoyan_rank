package fdu.kaoyanrank.entity;

import lombok.Data;

@Data
public class ExamScore {
    private Long id;
    private String examNoHash;
    private Integer englishScore;
    private Integer politicsScore;
    private Integer mathScore;
    private Integer score408;
}
